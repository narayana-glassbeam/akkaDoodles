package com.micronautics.akka.actor

import akka.actor.Actor


/** '''ActorRef.forward()'''
  * {{{def forward (message: Any)(implicit context: ActorContext): Unit}}}
  * Forwards the message and passes the original sender actor as the sender.
  * Works with '!' and '?'/'ask'. */
object forward extends Actor {
  this.forward(that)
  
  def receive = {
    case _ =>
  }
}

private object that extends Actor {
  def receive = {
    case _ =>
  }
}

