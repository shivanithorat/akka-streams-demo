package streaming.app

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

object Streamer extends App {
  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "Server")
  import system.executionContext

  private val payloadSize = 10
  def attachPayload(num: Int): String =
    num.toString + "-" + ("*" * payloadSize) + "\n"

  //*************************************************

  val numberStream: Source[Int, NotUsed] =
    Source
      .fromIterator(() => Iterator.from(1))
      .map { x =>
        printf(x + ", ")
        x
      }

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity =
        HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          numberStream.map(n => ByteString(attachPayload(n)))
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
