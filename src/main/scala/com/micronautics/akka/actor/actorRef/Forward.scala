package com.micronautics.akka.actor.actorRef

import akka.actor.Actor


/** '''ActorRef.forward()'''
  * {{{def forward (message: Any)(implicit context: ActorContext): Unit}}}
  * Forwards the message and passes the original sender actor as the sender.
  * Works with '!' and '?'/'ask'. */
object Forward extends Actor {
  self.forward(that)
  
  def receive = {
    case _ =>
  }
}

private object that extends Actor {
  def receive = {
    case _ =>
  }
}

