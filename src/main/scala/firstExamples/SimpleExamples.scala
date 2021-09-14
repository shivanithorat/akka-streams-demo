package firstExamples

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

object SimpleExamples extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "simple-first-demo")

  implicit val ec: ExecutionContext = actorSystem.executionContext

  println("Simple Stream Examples")

  //  ****************** simple flow ***********************

  // simple source
  val source = Source(1 to 10)

  // simple sink
  val sink = Sink.foreach[Int](x => println(x))

  // simple graph
  source.to(sink) //.run()

  // ** *************** introducing flow **********************

  // simple flow
  val squaredFlow = Flow[Int].map(x => x * x)

  // simple graph with flow
  source.via(squaredFlow).to(sink) //.run()

  //** **************** sources ***********************

  val sc1 = Source(1 to 10)
  val sc2 = Source(List("A", "B", "C"))
  val sc3 = Source.future(Future(42))
  val infiniteSource = Source.repeat(1)
  val periodicSource = Source.tick(1.second, 1.second, "tick")

  //** **************** sinks ***********************

  val sk1 = Sink.ignore
  val sk2 = Sink.foreach[Int](println)
  val sk3 = Sink.head[Int]

  //** **************** flows ***********************

  val mapFlow = Flow[Int].map(x => x * 2)
  val takeFlow = Flow[Int].take(10)

  //** **************** Graph ***********************

  val graph = source.via(mapFlow).to(sk2)
}
