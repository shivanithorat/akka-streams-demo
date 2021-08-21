package simpleChatApp.service

import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern._
import akka.event.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, GraphDSL, Merge}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{Attributes, CompletionStrategy, FlowShape, OverflowStrategy}
import akka.util.Timeout
import simpleChatApp.actor.ChatStateActor
import simpleChatApp.actor.ChatStateActor._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class ChatService(implicit val actorSystem: ActorSystem[SpawnProtocol.Command]) {

  private implicit val timeout: Timeout = Timeout(10.seconds)

  val webSocketRoute: Route = (get & parameter("user")){ user =>
      handleWebSocketMessages(chatFlow(user))
    }
  // sink actor
  private val chatStateActorF: Future[ActorRef[ChatCommand]] = actorSystem.ask(SpawnProtocol.Spawn(ChatStateActor(), "chat-actor", Props.empty, _))
  private val chatStateActor: ActorRef[ChatCommand] = Await.result(chatStateActorF, timeout.duration)

  // source actor
  private val chatSourceActor = ActorSource.actorRef[ChatCommand](completionMatcher = {
    case EndChat =>
  }, failureMatcher = {
    case ChatFailure => new RuntimeException("Exception occurred in Chat Source Actor")
  }, bufferSize = 10,
    overflowStrategy = OverflowStrategy.dropHead
  ).log("chat-source-actor: ").withAttributes(Attributes.logLevels(Logging.WarningLevel))

  def chatFlow(userName: String): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(chatSourceActor){ implicit builder =>
    (chatSourceActorShape) =>
    import GraphDSL.Implicits._

    val materializedValue = builder.materializedValue
      .map(chatSourceActorRef => UserJoined(userName, chatSourceActorRef))

    val merge = builder.add(Merge[ChatCommand](2))

    // from outside to inside
    val messageToChatEventFlow = builder.add(Flow[Message].map {
      case TextMessage.Strict(message) =>
        if (message == "bye") UserLeft(userName)
        else UserMessaged(userName, message)
    })

    // from inside to outside
    val chatEventToMessageFlow = builder.add(Flow[ChatCommand].map{
      case BroadCastMessage(message) => TextMessage(message)
    })

    val chatStateActorSink = ActorSink.actorRef[ChatCommand](chatStateActor, EndChat, ex => ChatFailure)

    materializedValue ~> merge ~> chatStateActorSink
    messageToChatEventFlow ~> merge
    chatSourceActorShape ~> chatEventToMessageFlow

    FlowShape(messageToChatEventFlow.in, chatEventToMessageFlow.out)

  })



}

