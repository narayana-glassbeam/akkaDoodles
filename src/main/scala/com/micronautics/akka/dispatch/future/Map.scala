package com.micronautics.akka.dispatch.future

import java.net.URL
import scalax.io._
import scalax.io.JavaConverters._
import akka.actor.ActorSystem
import akka.dispatch.Future


/** 
  * Futures.map() is not defined for Java. Is this an oversight?
  * Map creates a new Future by applying a function to a successful Future result. 
  * If this Future is completed with an exception then the new Future will also contain this exception.
  * Map produces simpler code than using Future.filter.
  * 
  * {{{def map [A] (f: (T) => A): Future[A]}}}
  * 
  * This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
  */
object Map extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val urls = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  )
  
  urls map { url => 
    Future {
      httpGet(url)
    } map { // implicit onComplete handler; Future value is provided to body
      case pageContents if pageContents.indexOf("Simpler Concurrency") >= 0 => println("Result: " + url)
      case _ => // ignore pages that do not contain the string "Simpler Concurrency"
    } recover {
      case ex => 
        println(ex.getClass().getName() + " for " + url)
    }
  }

  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String):String = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
}