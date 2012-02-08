import akka.actor.{PoisonPill, OneForOneStrategy, Actor}

/** Pattern for async exception handling?
  * @author Derek Wyatt */

/* class NewRouter extends Actor {
  self.faultHandler = OneForOneStrategy(List(classOf[Throwable]), 5, 5000)
  var sendCount = 0
  def receive = {
    case Send(tag) =>
      val routeBackTo = self.sender
      val riskyWorker = actorOf(new Actor {
        self.lifeCycle = Permanent
        def receive = {
          case "go" =>
            RemoteServer.send(tag, routeBackTo)
            self ! PoisonPill
        }
        override def postRestart(reason: Throwable) {
          // if reason can't be retried
          routeBackTo.foreach(_ ! SendFailed(tag))
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
*/