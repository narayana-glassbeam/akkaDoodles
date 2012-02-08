import akka.actor._
import akka.actor.Actor._
//import akka.dispatch.{Future, CompletableFuture, DefaultCompletableFuture, FutureTimeoutException}

/** Inversion of actor messaging control example. !, !! and !!! are not defined on the Actor, 
  * they are defined on the message
  * @author Derek Wyatt */

/*
// Something to derive from
trait Response

// Useful for later implicits
class FutureTimeout(val value: Long)

// A place to hold the global default for now
object Request {
  val futureTimeout = new FutureTimeout(5000)
}

// Wraps the closure that lets us respond to the client in the way it wishes
class Responder[Rsp](function: (Rsp, ActorRef) => Unit) {
  def respond(rsp: Rsp)(implicit responder: ActorRef) = function(rsp, responder)
}

// Puts the request / response protocol into a pair that we can unapply
case class ReqRspPair[Req, Rsp](req: Req, responder: Responder[Rsp])

// Creates the binding of request to response
// provides methods for making the calls and setting up the responder
trait Request[Req, Rsp <: Response] {
  import Request.futureTimeout
  def !(to: ActorRef)(implicit sender: ActorRef): Unit = {
    val rsp = (response: Rsp, responder: ActorRef) => {
      try {
        sender.!(response)(Some(responder))
      } catch {
        case e =>
          println(sender + " entered into a Request / Response contract and did not " +
            "handle the response from the server (" + to + "): " + e)
      }
    }
    to.!(ReqRspPair(this, new Responder[Rsp](rsp)))(Some(sender))
  }

  // Synchronous two-way request / response
  def !!(to: ActorRef)(implicit timeout: FutureTimeout = futureTimeout, sender: ActorRef): Option[Rsp] = {
    val future = new DefaultCompletableFuture[Rsp](timeout.value)
    val rsp = (response: Rsp, responder: ActorRef) => {
      def go(response: Rsp) {
        future.completeWithResult(response)
      }
      go(response)
    }
    // I'm sure we're losing a lot by not calling !! on the ActorRef
    // but if I do that, then the future I get back is too late... I need
    // to put the future into the Responder binding so we can abstract it
    // away.  Probably going to need to hack Akka itself to get this done
    // "for real"
    to.!(ReqRspPair(this, new Responder[Rsp](rsp)))(Some(sender))
    try {
      future.await
    } catch {
      case e: FutureTimeoutException =>
    }
    future.resultOrException
  }

  // Future-based two-way request / response
  def !!!(to: ActorRef)(implicit timeout: FutureTimeout = futureTimeout, sender: ActorRef): CompletableFuture[Rsp] = {
    val future = new DefaultCompletableFuture[Rsp](timeout.value)
    val rsp = (response: Rsp, responder: ActorRef) => {
      def go(response: Rsp) {
        future.completeWithResult(response)
      }
      go(response)
    }
    // Same caveat as above
    to.!(ReqRspPair(this, new Responder[Rsp](rsp)))(Some(sender))
    future
  }
}

/************************************************
 * Application stuff
 ************************************************/

// Back to basic case classes.  The inheritance gives us what we need
object ServerProtocol {
  case class LookupResult(value: String) extends Response
  case class PerformLookup(key: String) extends Request[PerformLookup, LookupResult]
  case class IntResult(value: Int) extends Response
  // case objects don't work
  case class GetInt() extends Request[GetInt, IntResult]
}

// The Server actor implementation
class Server extends Actor {
  // Import the protocol for ease of use
  import ServerProtocol._
  self.id = "ActorServer"
  // Actor could help us here by providing an implicit
  implicit val requiredResponder: ActorRef = self
  def receive = {
    // Extract the information and respond
    case ReqRspPair(GetInt(), responder) =>
      println("Server *** GetInt: " + self.sender)
      responder.respond(IntResult(10))

    // Extract the information and respond
    case ReqRspPair(PerformLookup(key), responder) =>
      println("Server *** PerformLookup: " + self.sender)
      responder.respond(LookupResult("Value"))
  }
}

// The Client actor implementation
class Client(server: ActorRef) extends Actor {
  // Import the protocol for ease of use
  import ServerProtocol._
  self.id = "ActorClient"
  // Actor could help us here by providing an implicit
  implicit val requiredSender: ActorRef = self
  def receive = {
    case 'Go =>
      self ! 'SendRequest1

    // The ! usage of the protocol
    case 'SendRequest1 =>
      PerformLookup("Key") ! server

    // The other half of the ! usage
    case LookupResult(value) =>
      println("Client ... Client got " + value + " from " + self.sender)
      self ! 'SendRequest2

    // The !! usage of the protocol
    case 'SendRequest2 =>
      val value = PerformLookup("Key") !! server
      println("Client ... Client got synchronous " + value.get.value)
      self ! 'SendRequest3

    // The !!! usage of the protocol
    case 'SendRequest3 =>
      val future = PerformLookup("Key") !!! server
      future.await
      println("Client ... Client got future " + future.result.get.value)
      self ! 'SendRequest4

    case 'SendRequest4 =>
      GetInt() ! server

    // The other half of the ! usage
    case IntResult(value) =>
      println("Client ... Client got " + value + " from " + self.sender)
  }
}

object Main {
  def main(args: Array[String]) {
    val s = actorOf[Server].start
    val c = actorOf(new Client(s)).start
    c ! 'Go
    Thread.sleep(1000)
    c.stop
    s.stop
  }*/
