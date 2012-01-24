package com.micronautics.akka.dispatch.futureScala

import akka.dispatch.Future
import java.util.concurrent.Executors
import akka.dispatch.ExecutionContext


/** '''Future.apply()''' Non-blocking function call, executed on another thread.
  *
  * {{{def apply (): T @util.continuations.package.cps[akka.dispatch.Future[Any]]}}}
  */
object Apply extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  Future { 2 + 3 } onComplete { f =>
    f match {
      case Right(result)   => println("Apply Scala result: " + result)
      case Left(exception) => println("Apply Scala exception: " + exception)
    }
    executorService.shutdown(); // terminates program
  }
}