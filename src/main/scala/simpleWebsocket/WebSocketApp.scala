package simpleWebsocket
import akka.NotUsed
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import simpleAkkaHttp.AkkaHttpSimple.{actorSystem, bindingFuture}

import scala.concurrent.Future
import scala.io.StdIn

object WebSocketApp extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "demo-websocket")
  import actorSystem.executionContext;

  private val numberSource: Source[Int, NotUsed] = Source.fromIterator(() => Iterator.from(1))
  val webSocketHandlerFlow: Flow[Message, Message, Any] = Flow.fromSinkAndSource(Sink.ignore, numberSource)
    .map(msg => TextMessage(msg.toString))

  val route: Route = pathSingleSlash {
    handleWebSocketMessages(webSocketHandlerFlow)
  }

  private val bindingFuture: Future[Http.ServerBinding] = Http().newServerAt("localhost", 8090).bind(route)

  println("Server now online at localhost:8080")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())

}
