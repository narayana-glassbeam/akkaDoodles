/** A simple Request/Response one-shot Akka Actor example
  * @author Derek Wyatt */
  
ReqRspActor.spawn(self)(new ReqRspActor(someServer, DoSomethingForMe) {
  import ReqRspActor._
  def receiveResponse: Receive = {
    case HereYouGo(data) =>
      println("Got my data: " + data)
  }
  override def handleProblem(message: Any): ResendOrNot.ResendOrNot = {
    match message {
      case _ =>
        ResendOrNot.Resend
    }
  }
})