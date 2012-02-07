/** Pattern for async exception handling?
  * @author Derek Wyatt */

  import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.MustMatchers

import akka.actor._
import akka.actor.Actor._
import akka.testkit.TestKit
import akka.util.duration._

class PatternTests extends WordSpec
                      with BeforeAndAfterEach
                      with MustMatchers
                      with TestKit {

  override def beforeEach {
    RemoteServer.failOnTag = ""
  }

  "Pattern" should { // {{{1
    "run fine" in { // {{{2
      val a = actorOf[Router].start
      a ! Send("Hithere")
      expectMsg (50 millis) {
        case msg =>
          msg must be (Receive("Hithere received"))
      }
      a !! GetSendCount must be (Some(1))
      a.stop
    } // }}}2
    "run fine when death occurs" in { // {{{2
      RemoteServer.failOnTag = "Die"
      val a = actorOf[Router].start
      a ! Send("Die")
      expectMsg (50 millis) {
        case msg =>
          msg must be (SendFailed("Die"))
      }
      a !! GetSendCount must be (Some(1))
      a.stop
    } // }}}2
  } // }}}1
} 