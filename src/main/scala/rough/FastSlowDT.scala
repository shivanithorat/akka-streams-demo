package rough

import akka.NotUsed
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import akka.stream.scaladsl.Source

object Server extends App {
  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "server-system")

  val source = Source
    .fromIterator(() => Iterator.from(1))
    .map { x => println("generated :" + x); x }

  val fastConsumer =
    Flow[Int].throttle(1, 1.seconds).map { x => println("fast :" + x); x }

  val slowConsumer =
    Flow[Int].throttle(1, 5.seconds).map { x => println("slow :" + x); x }

  source.async.via(fastConsumer).async.via(slowConsumer).run()

}
