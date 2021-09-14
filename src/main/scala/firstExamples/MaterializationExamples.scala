package firstExamples

import akka.{Done, NotUsed}
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object MaterializationExamples extends App {

  type SomeValue = NotUsed

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "simple-first-demo")

  implicit val ec: ExecutionContext = actorSystem.executionContext

  // 1. Choosing a materialized value

  // ProblemStatement: Take a stream of 10 integers , transform them and print integers to the console.

  val simpleSource: Source[Int, SomeValue] = Source(1 to 10)

  val simpleFlow: Flow[Int, Int, SomeValue] = Flow[Int].map(x => {
    println(s"From Flow: ${x + 1}")
    x + 1
  })
  val simpleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

  val graphPrev = simpleSource.via(simpleFlow).to(simpleSink) // .run()

  // choosing a Materialised value
  val graph3 = simpleSource
    .viaMat(simpleFlow)((sourceMatV, flowMatV) => flowMatV)
    .toMat(simpleSink)((flowMatV, sinkMatV) => sinkMatV)
//    .run()

  //syntactic sugar
  //val graph3: RunnableGraph[Future[Done]] = simpleSource.viaMat(simpleFlow)(Keep.right).toMat(simpleSink)(Keep.right)

  // 2. Excercise
  // Problem statement : Get the total number of words emitted by the stream
  val sentences =
    List("Hi I am Molly", "I am a teacher", "I teach kinder garden")

  // Steps
  // 1. create source of sentences
  // 2. we need to count words in each sentence
  // 3. add all the counts

  val sentenceSource = Source(sentences)
  val wordCounterFlow = Flow[String].map(str => str.split(" ").length)
  val totalCounterSink = Sink.reduce[Int](_ + _)
  val wordCounterGraph: RunnableGraph[Future[Int]] =
    sentenceSource.via(wordCounterFlow).toMat(totalCounterSink)(Keep.right)

  private val eventualInt: Future[Int] = wordCounterGraph.run()
  eventualInt.onComplete {
    case Failure(exception) => println(exception)
    case Success(value)     => println(s"Total word count is $value")
  }

}
