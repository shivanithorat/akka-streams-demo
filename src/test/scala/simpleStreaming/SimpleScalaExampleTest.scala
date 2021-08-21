package simpleStreaming

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.event.Logging
import akka.stream.Attributes
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class SimpleScalaExampleTest extends org.scalatest.FunSuite {


//  test("xyz"){
//    implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "demo-api")
//
//    val mySource = Source(List("1", "2", "3"))
//    def analyse(s: String) = s
//
//    //#log-custom
//    // customise log levels
//    mySource
//      .log("before-map")
//      .withAttributes(Attributes
//        .logLevels(onElement = Logging.DebugLevel, onFinish = Logging.InfoLevel, onFailure = Logging.DebugLevel))
//      .map(analyse)
//    }

    test("simple test") {
      implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "demo-api")
      val sourceUnderTest = Source.repeat(1).map(_ * 2)

      val future = sourceUnderTest.async.log("Source").withAttributes(Attributes.logLevels(Logging.WarningLevel))
        .take(10)
        .runWith(Sink.seq)
      val res = Await.result(future, 2.seconds)
      assert(res == Seq.fill(10)(2))

    }

    test("simple test with proper slow sink") {
      implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "demo-api")
      val sourceUnderTest = Source(1 to 100).log("Source: ").withAttributes(Attributes.logLevels(Logging.WarningLevel))

      val flow = Flow[Int].map(x => {
        Thread.sleep(1000)
        x
      }).log("Flow: ").withAttributes(Attributes.logLevels(Logging.WarningLevel))


      val future = sourceUnderTest.async.via(flow).runWith(Sink.ignore)

      Await.result(future, 2.minute)
    }


}

