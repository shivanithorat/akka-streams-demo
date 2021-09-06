package simpleStepsChatApp

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ChatAppActor {

  private val users = scala.collection.mutable.LinkedHashMap[String, ActorRef[String]]()

  trait ChatCommand
  case class UserJoined(userName: String, actorRef: ActorRef[String]) extends ChatCommand
  case class UserLeft(userName: String) extends ChatCommand
  case class UserMessaged(userName: String, message: String) extends ChatCommand

  case object EndChat extends ChatCommand
  case object ChatFailure extends ChatCommand

  def apply(): Behavior[ChatCommand] = Behaviors.receiveMessage {
    case UserJoined(userName, actorRef) =>
      users += (userName -> actorRef)
      actorRef ! s"Welcome to chat $userName"
      notifyAll(userName, s"$userName has joined the chat")
      Behaviors.same
    case UserLeft(userName) =>
      users.get(userName).foreach(_ ! "Bye")
      users -= userName
      notifyAll(userName, s"$userName has left the chat")
      Behaviors.same
    case UserMessaged(userName, message) =>
      users.filter(tuple => tuple._1 != userName).values
        .foreach(refs => refs ! s"$userName >> $message")
      Behaviors.same
  }

  def notifyAll(userName: String, message: String) = {
    users.filter(tuple => tuple._1 != userName).values.foreach(_ ! message)
  }
}
