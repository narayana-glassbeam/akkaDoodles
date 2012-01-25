package com.micronautics.akka.dispatch.futureScala

import java.util.concurrent.Executors

import akka.dispatch.ExecutionContext
import akka.dispatch.Future


/** '''Future.or()''' Non-blocking function call, executed on another thread.
  */
object Or extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  val f1 = Future { 6 / 0 }
  val f2 = Future { "asdf" + "qwer" }
  val f3 = Future { println("Evaluating f3"); 2 + 3 }
  (f1 or f2) onComplete { f =>
    f match {
      case Right(result)   => println("Nonblocking Or Scala result: " + result)
      case Left(exception) => println("Nonblocking Or Scala exception: " + exception)
    }
    executorService.shutdown(); // terminates program
  }
}