package com.micronautics.akka.dispatch.futureScala

import java.net.URL
import java.util.concurrent.Executors

import akka.dispatch.ExecutionContext
import akka.dispatch.Future
import scalax.io.JavaConverters.asInputConverter
import scalax.io.Codec

/** 
  * Map creates a new Future by applying a function to a successful Future result. 
  * If this Future is completed with an exception then the new Future will also contain this exception.
  * Map produces simpler code than using Future.filter.
  * 
  * {{{def map [A] (f: (T) => A): Future[A]}}}
  * 
  * This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
  */
object Map extends App {
  val executorService = Executors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(executorService)
  val urls = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  )
  
  urls map { url => 
    Future {
      httpGet(url)
    } map { // implicit onComplete handler; Future value is provided to body
      case pageContents if pageContents.indexOf("Simpler Concurrency") >= 0 => 
        println("Blocking Scala map result: " + url)
      case _ => // ignore pages that do not contain the string "Simpler Concurrency"
    } recover {
      case ex => 
        println(ex.getClass().getName() + " for " + url)
    } onComplete { f =>
      println("Shutting down")
	  executorService.shutdown(); // terminates program  
	}
  }

  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String):String = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
}