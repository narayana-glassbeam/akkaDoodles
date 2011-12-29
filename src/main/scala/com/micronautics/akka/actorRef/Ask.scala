package com.micronautics.akka.actorRef

import akka.actor.Actor

/** '''ActorRef.ask()''' or '''?''' performs send() and blocks for response.
  * {{{def compareTo (other: ActorRef): Int}}} */
object Ask extends Actor {
  self.ask("msg", 1000)
  
  def receive = {
    case _ =>
  }
}