package com.micronautics.akka.dispatch.futureScala

import akka.dispatch.Future
import java.util.concurrent.Executors
import akka.dispatch.ExecutionContext


object ScopeSin extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  var offset = 6
  def accessor = offset
  
  Future { 
    2 + 3 + offset // will be executed asynchronously
  } onComplete { f =>
    println("Entered onComplete, offset = " + offset)
    f match {
      case Right(result)   => println("ScopeSin Scala result: " + result)
      case Left(exception) => println("ScopeSin Scala exception: " + exception)
    }
    executorService.shutdown(); // terminates this thread, and the program if no other threads are active
  }
  Future { 2 + 3 + accessor } onComplete { f =>
    println("Entered onComplete, offset = " + offset)
    f match {
      case Right(result)   => println("ScopeSin Scala result: " + result)
      case Left(exception) => println("ScopeSin Scala exception: " + exception)
    }
    executorService.shutdown(); // terminates this thread, and the program if no other threads are active
  }
  offset = 42
  println("End of mainline, offset = " + offset)
}