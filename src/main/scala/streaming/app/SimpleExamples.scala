package streaming.app

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

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
  source.to(sink).run()

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
  val sk2 = Sink.foreach[String](println)
  val sk3 = Sink.head[Int]

  //** **************** flows ***********************

  val mapFlow = Flow[Int].map(x => x * 2)
  val takeFlow = Flow[Int].take(10)

  //** **************** Graph ***********************

  val graph: RunnableGraph[NotUsed] = source.via(mapFlow).to(sk1)
  graph.run()

  //** **************** APIs ***********************

  source.map(x => x)
  source.filter(???)
  source.groupBy(???, ???)
  source.collect(???)
  source.concat(???)
  source.zip(???)
  source.reduce(???)
  source.fold(???)(???)
  source.drop(???)
  source.dropWhile(???)
  source.runForeach(???)

  //** ***************************************
  List(1, 2, 3).map(x => x * x).filter(y => y > 5)

  source.map(x => x * x).filter(y => y > 5).runWith(Sink.seq)
  // operator Fusion
  // ******************************************
  // async and scaling up

  source.map(x => x * x).async.filter(y => y > 5).async.runWith(Sink.seq)

  // Backpressure :

//   3. Operator fusion
  val source1 = Source(1 to 10)
  val sink1 = Sink.foreach[Int](println)

  val complexFlow1 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x + 1
  }

  val complexFlow2 = Flow[Int].map { x =>
    Thread.sleep(1000)
    x * 10
  }

//   operator fusion
  source.via(complexFlow1).via(complexFlow2).to(sink1).run()
//
//  // async boundary
//  simpleSource.via(complexFlow1).async // runs on one actor
//    .via(complexFlow2).async //runs on other actor
//    .to(simpleSink)  // runs on third actor
//    //.run()

  // 4.BackPressure
}
