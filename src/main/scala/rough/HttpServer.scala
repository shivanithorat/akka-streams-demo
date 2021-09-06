package rough

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.stream.scaladsl.Source

object HttpServer extends App {

  implicit val system: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "test")

  /*
   * Typical stream use cases.
   * - Processing large datasets, Like a big log file
   * - handling user clicks
   * - Chat apps
   * - Video/Audio streaming : also mention all the companies involved in RS
   * - IoT sensor data
   *
   *
   *
   *
   *
   *
   * Akka streams : Statically Typed
   *
   * Publisher -> Source
   * Subscriber --> Sink
   * Transformer --> Flows
   *
   *        |-->    Source emits elements
   *    --> | -->   flow transforms the elements
   *    --> |       Sink Consumes the elements
   *
   *
   *
   *
   * ---------------------
   * Graph and Stream : declaring and executing (Materialising)
   *
   * - Materialised value
   *
   * - overflow stratergy and buffering
   *
   *
   * ---------------------
   * Back Pressure
   *
   *
   * ---------------------
   * Laziness
   *
   *
   * -------------------------------
   * Reactive streams with TCP protocol
   *
   *
   * -------------------------------
   *
   *
   *
   *
   *
   *
   * HttpServer
   * --- stream of requests --> request handler (request => response) -----Stream of responses // speed control from UI.
   *
   *
   *
   *
   * ------------------------------------------
   * Hot and Cold sources
   *
   *
   * ------------------------------------------
   *
   * Talk about Event service experience and the latency achieved
   *
   *
   *
   * ----------------------------------------------
   */

  val square: Int => Int = x => {
    println("squaring " + x)
    x * x
  }

  Source
    .fromIterator(() => (1 to 10).iterator)
    .map(square)
    .map(square)
    .runForeach(y => println("after double squaring " + y))

}
