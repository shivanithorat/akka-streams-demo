package streaming.app

import akka.NotUsed
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpRequest,
  HttpResponse,
  _
}
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Doubler extends App {

  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "Doubler")
  import system.executionContext

  private val payloadSize = 10
  def attachPayload(num: Int): String =
    num.toString + "-" + ("*" * payloadSize) + "\n"

  def extractNum(str: ByteString): Int =
    str.utf8String.takeWhile(_ != '-').toInt

  //*************************************************

  def numberStream: Source[Int, _] = {
    Source
      .future(
        Http().singleRequest(HttpRequest(uri = "http://localhost:8080/"))
      )
      .flatMapConcat(res => {
        res.entity.dataBytes.map { x =>
          val num = extractNum(x) * 2
          printf(num + ", ")
          num
        }
      })
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
    Http().newServerAt("localhost", 9090).bindFlow(routes)

  serverBindingF.onComplete {
    case Failure(exception) => println(exception.getMessage)
    case Success(value)     => println("Doubler started at localhost:9090")
  }
}
