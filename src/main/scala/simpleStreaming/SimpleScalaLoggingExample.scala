package simpleStreaming

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.scaladsl.{Flow, Sink, Source}

object SimpleScalaLoggingExample extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "demo-api")

  def analyse(s: Int) = s

  val mySource = Source(1 to 10)

  val flow = Flow[Int].map(x => x * x)
  mySource
//    .log("before-map")
//    .withAttributes(Attributes
//      .logLevels(onElement = Logging.WarningLevel, onFinish = Logging.InfoLevel, onFailure = Logging.DebugLevel))
    .async.via(flow)
    .map(analyse)
    .to(Sink.foreach(println)).run()

}
