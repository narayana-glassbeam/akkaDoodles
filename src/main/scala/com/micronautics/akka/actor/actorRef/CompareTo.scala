package com.micronautics.akka.actor.actorRef

import akka.actor.Actor

/** '''ActorRef.compareTo()'''
  * {{{def compareTo (other: ActorRef): Int}}} */
object CompareTo extends Actor {
  self.compareTo(this.self)
  //self.equals(that)
  
  def receive = {
    case _ =>
  }
}