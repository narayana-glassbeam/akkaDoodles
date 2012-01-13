package com.micronautics.akka.dispatch.future

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{DefaultHttpClient, BasicResponseHandler}
import akka.actor.ActorSystem
import akka.dispatch.Future


/** 
  * '''Future.map()''' Map creates a new Future by applying a function to a successful Future result. 
  * If this Future is completed with an exception then the new Future will also contain this exception.
  * Map produces simpler code than using Future.filter.
  * 
  * {{{def map [A] (f: (T) => A): Future[A]}}}
  * 
  * This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
  */
object Map extends App {
  implicit val defaultDispatcher = ActorSystem("MySystem").dispatcher
  val httpclient = new DefaultHttpClient
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
    val httpget = new HttpGet(urlStr)
    val brh = new BasicResponseHandler
    httpclient.execute(httpget, brh)
  }
}