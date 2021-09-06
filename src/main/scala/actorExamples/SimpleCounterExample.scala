package actorExamples

import actorExamples.SimpleCounter._
import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object SimpleCounterExample extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "actor-system")


  implicit val timeout: Timeout = Timeout(10.seconds)
  val simpleCounter : ActorRef[CounterCommand] = Await.result(actorSystem.ask(SpawnProtocol.Spawn(SimpleCounter.behavior(), "simple-counter", Props.empty,_)), 10.seconds)

  simpleCounter ! Display
  simpleCounter ! Increment
  simpleCounter ! Increment
  simpleCounter ! Display
  simpleCounter ! Decrement
  simpleCounter ! Decrement
  simpleCounter ! Display

  actorSystem.terminate()

}

object SimpleCounter {

  trait CounterCommand
  // actor messages
  case object Increment extends CounterCommand
  case object Decrement extends CounterCommand
  case object Display extends CounterCommand

  // state
  private var counter = 0;

  // behavior
  def behavior() : Behavior[CounterCommand] = Behaviors.receiveMessage {
    case Decrement => counter = counter - 1
      println("Dec count")
      Behaviors.same
    case Display =>
      println("Get count")
      println(counter)
      Behaviors.same
    case Increment => counter = counter + 1
      println("Inc count")
      Behaviors.same
  }

}


