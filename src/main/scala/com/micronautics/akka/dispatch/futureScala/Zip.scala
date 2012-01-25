package com.micronautics.akka.dispatch.futureScala

import java.util.concurrent.Executors

import akka.dispatch.ExecutionContext
import akka.dispatch.Future


/** '''Future.zip()''' Non-blocking function call, executed on another thread.
  */
object Zip extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  val f1 = Future { 6 / 0 }
  val f2 = Future { "asdf" + "qwer" }
  val f3 = Future { 2 + 3 }
  (f1 zip f2) onComplete { f =>
    f match {
      case Right(result)   => println("Nonblocking Zip Scala result: " + result)
      case Left(exception) => println("Nonblocking Zip Scala exception: " + exception)
    }
    executorService.shutdown(); // terminates program
  }
}