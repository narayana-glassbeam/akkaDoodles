package com.micronautics.akka.dispatch.future

import akka.actor.ActorSystem
import akka.dispatch.Future
import scalax.io._
import scalax.io.JavaConverters._


/** 
  * '''Future.withFilter()''' Scala 2.8 introduced {{{withFilter()}}}, whose main difference from {{{filter}}}} is that 
  * instead of returning a new filtered collection, {{{withFilter}}} filters on demand. 
  * The behavior of the  {{{filter()}}} method is based on the strictness of the collection.
  * 
  * {{{for(x <- c; if cond) yield {...}}}}
  * 
  * is translated on Scala 2.7 into
  * 
  * {{{c.filter(x => cond).map(x => {...})}}}
  * 
  * or, by Scala 2.8+, into
  * 
  * {{{c.withFilter(x => cond).map(x => {...})}}}
  * 
  * Also, the expression
  * 
  * {{{for (x <- c; if cond) yield {...}}}}
  * 
  * is translated to 
  * 
  * {{{c.withFilter(x => cond).map(x => {...})}}} 
  * 
  * One big difference between {{{List.withFilter()}}} and {{{Future.withFilter()}}} is that {{{List}}} is defined as a 
  * collection, but {{{Future}}} defines only one object. This makes it difficult for me to understand how or when {{{Future.withFilter()}}} might 
  * be a better choice than {{{Future.filter()}}}. All but one of the use cases I could think of used {{{List[Future].withFilter()}}}, 
  * and that means invoking {{{List.withFilter()}}}, not {{{Future.withFilter()}}}.
  *
  * {{{Future.withFilter()}}} creates a new {{{FutureWithFilter}}} and that the evaluation is lazy. Perhaps one 
  * benefit of {{{Future.withFilter()}}} might be that a {{{Future}}} could be lazily filtered multiple times by applying 
  * {{{Future.withFilter()}}} many times, and then evaluating the resulting {{{FutureWithFilters}}} on demand.
  */
object WithFilter extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  var found = false
  List.range(1,10).withFilter(_ % 2 == 1 && !found).foreach(x => if (x == 5) found = true else println(x))  
}