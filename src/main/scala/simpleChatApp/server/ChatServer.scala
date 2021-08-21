package simpleChatApp.server

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import simpleChatApp.service.ChatService

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object ChatServer extends App {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "chat-actor-system")
  implicit val context: ExecutionContext = actorSystem.executionContext

  val chatService = new ChatService
  private val bindingFuture = Http().newServerAt("localhost", 8080).bind(chatService.webSocketRoute)

  println("Server now online at localhost:8080")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())

}
