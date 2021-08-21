package simpleChatApp.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ChatStateActor {

  private val users = scala.collection.mutable.LinkedHashMap[String, ActorRef[ChatCommand]]()

  trait ChatCommand
  case class UserJoined(userName: String, actorRef: ActorRef[ChatCommand]) extends ChatCommand
  case class UserLeft(userName: String) extends ChatCommand
  case class UserMessaged(userName: String, message: String) extends ChatCommand
  case class BroadCastMessage(message: String) extends ChatCommand
  case object EndChat extends ChatCommand
  case object ChatFailure extends ChatCommand

  def apply(): Behavior[ChatCommand] = Behaviors.receiveMessage {
    case UserJoined(userName, actorRef) =>
      users += (userName -> actorRef)
      actorRef ! BroadCastMessage(s"Welcome to chat $userName")
      notifyAll(userName, s"$userName has joined the chat")
      Behaviors.same
    case UserLeft(userName) =>
      users.get(userName).foreach(_ ! EndChat)
      users -= userName
      notifyAll(userName, s"$userName has left the chat")
      println("After notifying completed stream")
      Behaviors.same
    case UserMessaged(userName, message) =>
      users.filter(tuple => tuple._1 != userName).values
        .foreach(refs => refs ! BroadCastMessage(s"$userName >> $message"))
      Behaviors.same
    case EndChat =>
      Behaviors.same
    case ChatFailure =>
      Behaviors.stopped
  }

  def notifyAll(userName: String, message: String) = {
    users.filter(tuple => tuple._1 != userName).values.foreach(_ ! BroadCastMessage(message))
  }
}
