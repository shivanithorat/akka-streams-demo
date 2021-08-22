package simpleStepsChatApp

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, parameter, path}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

object Step1 extends App {


/*****************************/

  def greeterFlow(userName: String): Flow[Message, Message, Any] =
    Flow[Message].collect {
      case tm: TextMessage => tm
    }

/*****************************/


}
