package simpleAkkaHttp

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.Attributes
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.Random

object StreamingAkkaHttp extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "simpleHttpStreaming")
  implicit val executionContext: ExecutionContext = actorSystem.executionContext

  val number = Source.fromIterator(() => Iterator.continually(Random.nextInt()))
    .log("From Source").withAttributes(Attributes.logLevels(Logging.DebugLevel))


  val route = path("random") {
    get {
      complete(
        HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          number.map(n => ByteString(s"$n\n"))
        )
      )
    }
  }

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route);

  println("Server now online at localhost:8080")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => actorSystem.terminate()) // and shutdown when done
}
