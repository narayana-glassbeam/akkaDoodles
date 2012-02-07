/** A simple Request/Response one-shot Akka Actor example
  * @author Derek Wyatt */
object ReqRspActor {
  object ReqRspProtocol {
    case class UnknownMessage(msg: String) extends Exception(msg)
    case class FutureException(exception: Throwable)
  }

  object ResendOrNot extends Enumeration {
    type ResendOrNot = Value
    val Resend, DoNotResend = Value
  }

  def spawn(owner: ActorRef)(logic: => ReqRspActor): Unit = {
    if (owner.faultHandler == NoFaultHandlingStrategy)
      owner.faultHandler = OneForOneStrategy(List(classOf[Throwable]), 3, 1000)
    val a = actorOf(logic)
    owner.startLink(a)
    a ! 'Go
  }
}

abstract class ReqRspActor(recipient: ActorRef, message: Any) extends Actor {
  import ReqRspActor._
  import ReqRspActor.ResendOrNot._
  self.lifeCycle = Permanent
  def myref = self

  def receiveResponse: Receive

  private def sendRequest(to: ActorRef, msg: Any): Unit = {
    (to !!! msg) onComplete { (future) =>
      future.exception match {
        case Some(exception) =>
          if (handleProblem(ReqRspProtocol.FutureException(exception)) == DoNotResend)
            stop
          else
            sendRequest(recipient, message)
        case _ =>
          receiveResponse(future.result.get)
          stop
      }
    }
  }

  def receive: Receive = {
    case 'Go =>
      sendRequest(recipient, message)
      become(unknownHandler)
  }

  protected def unknownHandler: Receive = {
    case msg =>
      handleProblem(ReqRspProtocol.UnknownMessage("Unrecognized message was received: (" + msg + ")"))
      stop
  }

  protected def stop {
    self ! PoisonPill
  }

  def handleProblem(message: Any): ResendOrNot = {
    DoNotResend
  }
}