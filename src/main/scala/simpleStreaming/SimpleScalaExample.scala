package simpleStreaming

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}

import scala.concurrent.Future

object SimpleScalaExample extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "demo-api")

  val squaredFlow: Flow[Int, Int, NotUsed] = Flow[Int].map(x => x * x)

  // Publisher
  val fastSourceWithTransformations: Source[Int, NotUsed] = Source(1 to 50).
    via(squaredFlow)

  // Subscriber
  val slowSink: Sink[Int, Future[Done]] = Sink.foreach[Int]{ x =>
//    Thread.sleep(1000)
    println(s"Consumed by sink: ${x}")
  }

  // Running the graph
  fastSourceWithTransformations.async.to(slowSink).run()

}
