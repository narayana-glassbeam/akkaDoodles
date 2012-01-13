package com.micronautics.akka.dispatch.future;

import static akka.dispatch.Futures.future;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Arrays;  
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import scala.Either;
import scala.Option;
import akka.actor.ActorSystem;
import akka.dispatch.Await;
import akka.dispatch.Future;
import akka.dispatch.MessageDispatcher;
import akka.japi.Function;
import akka.util.Duration;


public class FilterJava {
    private ActorSystem system = ActorSystem.create();
    private MessageDispatcher dispatcher = system.dispatcher();
    private Duration timeout = Duration.create(1, SECONDS);
    private List<String> urls = new LinkedList<String> (Arrays.asList(new String[] {
        "http://akka.io/",
        "http://www.playframework.org/",
        "http://nbronson.github.com/scala-stm/"
    })); 

    private static void blocking() {
        // TODO figure this out
        /*urls map ( url => 
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
      )*/
    }

    private static void nonBlocking() {
    }

    public static void main(String[] args) {
        FilterJava example = new FilterJava();
        example.blocking();
        example.nonBlocking();
    }
}
