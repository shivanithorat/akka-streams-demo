package simpleStepsChatApp

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Props, SpawnProtocol}
import akka.event.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{Attributes, FlowShape, OverflowStrategy}
import akka.util.Timeout
import simpleStepsChatApp.ChatAppActor._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class Step3(implicit val actorSystem: ActorSystem[SpawnProtocol.Command]) {

  // sink actor
  private implicit val timeout: Timeout = Timeout(10.seconds)
  private val chatStateActorF: Future[ActorRef[ChatCommand]] = actorSystem.ask(SpawnProtocol.Spawn(ChatAppActor(), "chat-app-actor", Props.empty, _))
  private val chatStateActor: ActorRef[ChatCommand] = Await.result(chatStateActorF, timeout.duration)

  // source actor
  private val chatSourceActor = ActorSource.actorRef[String](completionMatcher = {
    case "Bye" =>
  }, failureMatcher = {
    case "Failure" => new RuntimeException("Exception occurred in Chat Source Actor")
  }, bufferSize = 10,
    overflowStrategy = OverflowStrategy.dropHead
  ).log("chat-user-actor: ").withAttributes(Attributes.logLevels(Logging.WarningLevel))


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
      val chatEventToMessageFlow = builder.add(Flow[String].map{
        message => TextMessage(message)
      })

      val chatStateActorSink = ActorSink.actorRef[ChatCommand](chatStateActor, EndChat, ex => ChatFailure)

      materializedValue ~> merge ~> chatStateActorSink
      messageToChatEventFlow ~> merge
      chatSourceActorShape ~> chatEventToMessageFlow

      FlowShape(messageToChatEventFlow.in, chatEventToMessageFlow.out)

  })

}
