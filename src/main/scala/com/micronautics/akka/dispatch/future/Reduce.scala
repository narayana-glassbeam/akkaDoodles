package com.micronautics.akka.dispatch.future

import akka.actor.ActorSystem
import akka.dispatch.Future


/** '''Future.reduce()''' {{{def reduce [T, R >: T] (futures: Traversable[Future[T]])(op: (R, T) => T)(implicit dispatcher: MessageDispatcher): Future[R]}}}
  * Non-blocking reduce, executed on the [[java.lang.Thread]] that completes the last [[akka.dispatch.Future]].
  * This example is based on the code fragment shown in Viktor Klang's '''Future of Akka''' presentation.
  * @see http://days2011.scala-lang.org/node/138/283 */
object Reduce extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val map = (1 to 100) map (x => Future { expensiveCalc(x) })
  
  Future.reduce(map)(_ + _) onComplete { f => 
    f match {
      case Right(result)   => println("Result: " + result)
      case Left(exception) => println("Exception: " + exception)
    }
    System.exit(0)
  }

  /** Pretend that method consumes a lot of computational resource */
  def expensiveCalc(x:Int) = { x * x }
}