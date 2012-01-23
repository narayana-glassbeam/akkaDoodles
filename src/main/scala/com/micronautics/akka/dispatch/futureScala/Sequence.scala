package com.micronautics.akka.dispatch.futureScala

import akka.actor.ActorSystem
import akka.dispatch.Future


/** '''Future.sequence()''' {{{Traversable[Future[T]] => Future[Traversable[T]]}}}
  * Simple way of working with Futures.
  * 
  * {{{def sequence [A, M[_] <: Traversable[_]] (in: M[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]], dispatcher: MessageDispatcher): Future[M[A]]}}}
  * 
  * Taken from Viktor Klang's "Future of Akka" presentation
  * @see http://days2011.scala-lang.org/node/138/283 */
object Sequence extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val stringFutures = for (i <- 1 to 10) yield Future { i.toString }
  
  Future sequence stringFutures onComplete { f => 
    f match {
      case Right(result)   => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }
}