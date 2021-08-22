package firstExamples

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}

object BackpressureExample extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "simple-first-demo")


  val fastSource = Source(1 to 100)

  val simpleFlow = Flow[Int].map({x =>
    println(x)
    x
  })

  val slowSink = Sink.foreach[Int]({x =>
    Thread.sleep(1000)
    println(s"From sink $x")
  })

  // operator fusion
  // output ?
  fastSource.via(simpleFlow).to(slowSink)//.run()

  // async with backpressure
  // output ?
  fastSource.async.via(simpleFlow).async.to(slowSink)//.run()


  // async with drop
  val bufferedFlow = simpleFlow.buffer(10, overflowStrategy = OverflowStrategy.dropHead)
  // output??
  fastSource.async.via(bufferedFlow).async.to(slowSink).run()

  /*
  *  1-16 elements will go through as Sink will buffer them
  *  17-27 elements flow will buffer, since the sink is not ready it will start dropping elements
  *  Flow will buffer last 91-100 elements and those will be passed to the sink
  * */

}
