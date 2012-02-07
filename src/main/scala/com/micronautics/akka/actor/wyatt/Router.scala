import akka.actor._
import akka.actor.Actor._
import akka.config.Supervision._

import scala.collection.mutable.Map

case class Send(tag: String)
case class SendFailed(tag: String)
case class Receive(tag: String)
case object GetSendCount

/** Pattern for async exception handling?
  * @author Derek Wyatt */
object RemoteServer {
  var failOnTag = ""
  def send(tag: String, echoTo: Option[ActorRef]) {
    if (tag == failOnTag)
      throw new RuntimeException("I'm supposed to fail")
    echoTo.foreach(_ ! Receive(tag + " received"))
  }
}

class Router extends Actor {
  var sendCount = 0
  def receive = {
    case Send(tag) =>
      val routeBackTo = self.sender
      val riskyWorker = actorOf(new Actor {
        def receive = {
          case "go" =>
            try {
              RemoteServer.send(tag, routeBackTo)
            } catch {
              case e =>
                routeBackTo.foreach(_ ! SendFailed(tag))
            }
          case msg @ Receive(tag) =>
            routeBackTo.foreach(_ ! msg)
            self ! PoisonPill
        }
      })
      self.startLink(riskyWorker)
      sendCount += 1
      riskyWorker ! "go"

    case GetSendCount =>
      self.reply_?(sendCount)
  }
} 