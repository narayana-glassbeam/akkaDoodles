package com.micronautics.akka.actor.actorRef

import akka.actor.Actor

/** '''ActorRef.ask()''' or '''?''' performs send() and blocks for response.
  * {{{def compareTo (other: ActorRef): Int}}} */
object Ask extends Actor {
  // TODO v2.0-M3 changed, need to fix: 
  //self.ask("msg", 1000)
  
  def receive = {
    case _ =>
  }
}