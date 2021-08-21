package simpleChatApp

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import simpleChatApp.service.ChatService

class ChatServiceTest extends AnyFunSuite with Matchers with ScalatestRouteTest{

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "chat-actor-system")

  test("this should get greeter message") {
    val wsClient = WSProbe()
    val chatApp = new ChatService()
    WS("/?user=John", wsClient.flow) ~> chatApp.webSocketRoute ~> check {
      wsClient.expectMessage("Welcome to chat John")
      wsClient.sendMessage("bye")
      wsClient.expectCompletion()
    }

  }

  test("this should register multiple users") {
    val wsClient1 = WSProbe()
    val wsClient2 = WSProbe()
    val chatApp = new ChatService()
    WS("/?user=Sam", wsClient1.flow) ~> chatApp.webSocketRoute ~> check {
      wsClient1.expectMessage("Welcome to chat Sam")
    }
    WS("/?user=Archie", wsClient2.flow) ~> chatApp.webSocketRoute ~> check {
      wsClient2.expectMessage("Welcome to chat Archie")
    }
  }

}



// What to do of chat failure??