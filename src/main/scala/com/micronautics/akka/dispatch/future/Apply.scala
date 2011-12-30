package com.micronautics.akka.dispatch.future

import akka.actor.ActorSystem
import akka.dispatch.Future


/** '''Future.apply()'''
  * Non-blocking function call, executed on another thread. */
object Apply extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  Future.apply { 2 + 3 } onComplete { f => 
    f match {
      case Right(result)   => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }
}