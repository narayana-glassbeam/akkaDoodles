package com.micronautics.akka.dispatch.futureScala

import java.net.URL

import com.micronautics.concurrent.DaemonExecutors

import akka.dispatch.Await
import akka.dispatch.ExecutionContext
import akka.dispatch.Future
import akka.util.duration.intToDurationInt
import scalax.io.JavaConverters.asInputConverter
import scalax.io.Codec

/**
  * This example uses future filters to return a list of Future[String] containing the contents of web pages that contain the string {{{Simpler Concurrency}}}.
  * Future filters are applied after the future completes.
  *
  * {{{Future}}}s that fail a filter contain a {{{MatchError}}} exception (on the Left).
  */
object FilterBlocking extends App {
  val daemonExecutorService = DaemonExecutors.newFixedThreadPool(10)
  implicit val context = ExecutionContext.fromExecutor(daemonExecutorService)
  val urlStrs = List (
    "http://akka.io/",
    "http://www.playframework.org/",
    "http://nbronson.github.com/scala-stm/"
  )

  val contentList = for (urlStr<-urlStrs) yield filterPage(urlStr)
  // Use List.filter to prune out all the pages that had a MatchError (they are stored as empty strings), 
  // then print surrounding 80 characters of matching pages 
  contentList filter { _.length>0 } foreach { contents =>
    val matchStart = contents.indexOf("Simpler Concurrency")
    println("Scala Filter blocking result: ..." + contents.substring(matchStart-40, matchStart+40) + "...")
  }

  /** @return the contents of the page if it contains the string "Simpler Concurrency",
   * otherwise return the empty string */
  def filterPage(urlStr:String):String = {
     val f1 = Future(httpGet(urlStr))
     val f2 = f1.filter(_.indexOf("Simpler Concurrency")>=0).recover {
       // value for f2 is the empty string if the evaluated future fails the filter predicate
       case m: MatchError ⇒ "" 
    }
    Await.result(f2, 5 seconds)
  }
  
  /** Fetches contents of web page pointed to by urlStr */
  def httpGet(urlStr:String):String = {
    new URL(urlStr).asInput.slurpString(Codec.UTF8)
  }
}