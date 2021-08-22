package simpleStepsChatApp

import akka.actor.typed._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.io.StdIn

object ChatServiceAppStep extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "chat-actor-system")
  implicit val context: ExecutionContext = actorSystem.executionContext
  private implicit val timeout: Timeout = Timeout(10.seconds)

  val webSocketRoute: Route = (get & parameter("user")){ user =>
    handleWebSocketMessages(chatFlow(user))
  }

  def chatFlow(userName: String): Flow[Message, Message, Any] = Step2.chatFlow(userName)


  private val bindingFuture = Http().newServerAt("localhost", 8080)
    .bind(webSocketRoute)

  println("Server now online at localhost:8080")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())


}

