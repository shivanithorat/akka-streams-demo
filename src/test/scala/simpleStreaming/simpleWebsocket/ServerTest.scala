package simpleStreaming.simpleWebsocket

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.util.ByteString
import org.scalatest.FunSuite
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

class ServerTest extends FunSuite with Matchers with ScalatestRouteTest{


  test("should create empty game service") {
    val gameService = new GameService()
    val wsClient = WSProbe()
    WS("/greeter", wsClient.flow) ~> gameService.websocketRoute ~>
      check {
        // check response for WS Upgrade headers
        isWebSocketUpgrade shouldEqual true

        wsClient.expectMessage("welcome player")
        // manually run a WS conversation
        wsClient.sendMessage("Peter")
        wsClient.expectMessage("Peter")

        wsClient.sendMessage(BinaryMessage(ByteString("abcdef")))
        wsClient.expectNoMessage(100.millis)

        wsClient.sendMessage("John")
        wsClient.expectMessage("John")

        wsClient.sendCompletion()
        wsClient.expectCompletion()
      }

  }


  test ("should register player") {

    val gameService = new GameService()
    val wsClient = WSProbe()
    WS("/greeter", wsClient.flow) ~> gameService.websocketRoute ~>
      check {
        wsClient.expectMessage("welcome player")
      }
  }

}

class GameService {

  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "demo-app")


  def greeter: Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(){ implicit builder =>
    import GraphDSL.Implicits._

    val matValue = builder.materializedValue.map(x => TextMessage("welcome player"))
    val messagePassingFlow = builder.add(Flow[Message].map(x => x))
    val merge = builder.add(Merge[Message](2))

    matValue ~> merge.in(0)
    merge ~> messagePassingFlow

    FlowShape(merge.in(1), messagePassingFlow.out)

  })



  val websocketRoute: Route =
    (get & parameter("player")) { player =>
      handleWebSocketMessages(greeter)
    }


}

