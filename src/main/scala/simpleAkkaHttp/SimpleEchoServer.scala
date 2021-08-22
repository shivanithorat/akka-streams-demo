package simpleAkkaHttp

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, path}
import akka.stream.scaladsl.Flow

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object SimpleEchoServer extends App {


    implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(),"simpleHttp")
    implicit val executionContext: ExecutionContext = actorSystem.executionContext

    def greeterFlow: Flow[Message, Message, Any] =
      Flow[Message].collect {
        case tm: TextMessage => tm
      }

   val route =
    path("greeter") {
      handleWebSocketMessages(greeterFlow)
    }


  // This first
  private val bindingFuture: Future[Http.ServerBinding] = Http().newServerAt("localhost", 8080).bind(route)
    println("Server now online at localhost:8080")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
}
