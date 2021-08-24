package streaming.app

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http

import akka.NotUsed
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString

import scala.concurrent.duration._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ServerApp extends App {

  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "Server")
  import system.executionContext

  //*************************************************

  val numberStream: Source[Int, NotUsed] =
    Source
      .fromIterator(() => Iterator.from(1))
      .throttle(1, 50.millis) // infinite source

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity =
        HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          numberStream.map(n => ByteString(s"$n\n"))
        )
      )
    case _ => HttpResponse(StatusCodes.NotFound)
  }

  val routes: Flow[HttpRequest, HttpResponse, NotUsed] =
    Flow.fromFunction(requestHandler)

  //*************************************************
  private val serverBindingF: Future[Http.ServerBinding] =
    Http().newServerAt("localhost", 8080).bindFlow(routes)

  serverBindingF.onComplete {
    case Failure(exception) => println(exception.getMessage)
    case Success(value)     => println("Server started at localhost:8080")
  }

}
