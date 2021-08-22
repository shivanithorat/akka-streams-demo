package simpleStepsChatApp

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Source}
import akka.stream.typed.scaladsl.ActorSink
import simpleChatApp.actor.ChatStateActor.{BroadCastMessage, ChatCommand, ChatFailure, EndChat, UserJoined, UserLeft, UserMessaged}

object Step2 extends App {

  def chatFlow(userName: String): Flow[Message, Message, Any] = Flow.fromGraph(GraphDSL.create(){ implicit builder =>
      import GraphDSL.Implicits._

      val materializedValue = builder.materializedValue
        .map(graphMatValue => TextMessage(s"Welcome $userName"))

      // from outside to inside
      val messageFlow = builder.add(Flow[Message].collect {
        case tm: TextMessage => tm
      })

      val merge = builder.add(Merge[Message](2))

     materializedValue ~> merge.in(0)
     messageFlow ~> merge.in(1)

      FlowShape(messageFlow.in, merge.out)

  })

}
