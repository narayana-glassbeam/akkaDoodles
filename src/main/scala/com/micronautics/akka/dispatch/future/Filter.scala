package com.micronautics.akka.dispatch.future

import java.net.URL
import scalax.io._
import scalax.io.JavaConverters._
import akka.actor.ActorSystem
import akka.dispatch.Future
import scala.MatchError

/** 
  * '''Future.filter()''' {{{def filter (pred: (T) => Boolean): Future[T]}}}
  * 
  * This example uses future filters to return a list of Future[String] containing the URLs of web pages that contain the string {{{Simpler Concurrency}}}.
  * 
  * Filters are applied after the future completes, so onComplete() is called implicitly by the filter. If you provide Future.onComplete(), the filter will be evaluated before your onComplete()
  * 
  * There is no way for a {{{Future}}} to represent the lack of results, so {{{Future}}}s that fail a filter contain a {{{MatchError}}} exception (on the Left).
  */
object Filter extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val urls = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  ) 
  
  // print URLs of pages containing the string "Simpler Concurrency"
  urls map ( url => 
    Future{
      httpGet(url)}
    filter ( pageContents => 
      pageContents.indexOf("Simpler Concurrency")>=0 // invoked after future completes
    ) onComplete { f => // runs after the filter is evaluated
	  f match {
	    case Right(result) => println("Result: " + url)
	    case Left(_:MatchError) => // if the filter does not match, the exception will contain a benign MatchError
	    case Left(exception) => 
  	      val msg = exception.getMessage()
	      println(exception.getClass().getName() + " " + msg.substring(msg.lastIndexOf("(")) + " for " + url)
	  }
    }
  )
  
  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String):String = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
}