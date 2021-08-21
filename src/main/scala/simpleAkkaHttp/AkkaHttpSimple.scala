package simpleAkkaHttp

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object AkkaHttpSimple extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(),"simpleHttp")
  implicit val executionContext: ExecutionContext = actorSystem.executionContext

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello to akka-http</h1>"))
      }
    }

  private val bindingFuture: Future[Http.ServerBinding] = Http().newServerAt("localhost", 8080).bind(route)
  println("Server now online at localhost:8080")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
}
