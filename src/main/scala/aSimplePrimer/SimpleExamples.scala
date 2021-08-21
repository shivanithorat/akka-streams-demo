package aSimplePrimer

import akka.Done
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object SimpleExamples extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "simple-first-demo")
  implicit val ec: ExecutionContext = actorSystem.executionContext

/******************* simple flow ************************/

  // simple source
  val source = Source(1 to 50)

  // simple sink
  val sink = Sink.foreach[Int](x => println(x))

  // simple graph
  source.to(sink)//.run()

/***************** introducing flow ***********************/

  // simple flow
  val squaredFlow = Flow[Int].map(x =>  x * x)

  // simple graph with flow
  source.via(squaredFlow).to(sink)//.run()

  /****************** sources ************************/

  val sc1 = Source(1 to 10)
  val sc2 = Source(List("A", "B", "C"))
  val sc3 = Source.future(Future(42))

  /****************** sinks ************************/

  val sk1 = Sink.ignore
  val sk2 = Sink.foreach[String](println)
  val sk3 = Sink.head[Int] // retrieves the head and then closes the stream



  // 2. Materializing streams

  val simpleSource = Source(1 to 10)
  val simpleFlow = Flow[Int].map(x => {
    println(s"From Flow: ${x + 1}")
    x + 1
  })
  val simpleSink = Sink.foreach[Int](println)
  val graph3 = simpleSource
    .viaMat(simpleFlow)((sourceMat, flowMat) => flowMat)
    .toMat(simpleSink)((flowMat, sinkMat) => sinkMat)


  // syntactic sugar
  // val graph3: RunnableGraph[Future[Done]] = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)
  graph3.run().onComplete {
    case Failure(exception) => println(s"Stream processing failed with: $exception")
    case Success(value) => println("Stream processing complete")
  }

  // operator fusion
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

  // operator fusion
  simpleSource.via(complexFlow1).via(complexFlow2).to(simpleSink).run()

  // async boundary
  simpleSource.via(complexFlow1).async // runs on one actor
    .via(complexFlow2).async //runs on other actor
    .to(simpleSink)  // runs on third actor
    .run()

}
