/** Pattern for async exception handling?
  * @author Derek Wyatt */
  
"run fine when death occurs2" in { // {{{2
  RemoteServer.failOnTag = "Die"
  val a = actorOf[NewRouter].start
  a ! Send("Die")
  expectMsg (100 millis) {
    case msg =>
      msg must be (SendFailed("Die"))
  }
  a !! GetSendCount must be (Some(1))
  a.stop
} // }}}2 