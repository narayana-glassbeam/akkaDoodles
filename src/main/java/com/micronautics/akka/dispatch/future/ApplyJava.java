package com.micronautics.akka.dispatch.future;

import static akka.dispatch.Futures.future;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.Callable;
import scala.Either;
import scala.Option;
import akka.actor.ActorSystem;
import akka.dispatch.Await;
import akka.dispatch.Future;
import akka.dispatch.MessageDispatcher;
import akka.japi.Function;
import akka.util.Duration;


/** Invoke Future as a non-blocking function call, executed on another thread. */
class ApplyJava {
    private ActorSystem system = ActorSystem.create();
    private MessageDispatcher dispatcher = system.dispatcher();
    private Duration timeout = Duration.create(1, SECONDS);
    
    
    void blocking() {
        Future<Integer> resultFuture = future(new Callable<Integer>() {
          public Integer call() {
            return 2 + 3;
          }
        }, dispatcher);
        // Await.result() blocks until the Future completes
        Integer result = (Integer) Await.result(resultFuture, timeout);
        System.out.println("Result: " + result);
    }

    void nonBlocking() {
        Future<Integer> resultFuture = future(new Callable<Integer>() {
          public Integer call() {
            return 2 + 3;
          }
        }, dispatcher);
        /*resultFuture.onComplete(new Function<Future<Integer>>() { 
            public void apply(Future<Integer> future) {
                // This block is executed asynchronously
                Option<Either<Throwable,Integer>> result = future.value();
                System.out.println("Result: " + result);
                System.exit(0);
            }
        });*/
    }

    public static void main(String[] args) {
        ApplyJava example = new ApplyJava();
        example.blocking();
        example.nonBlocking();
    }
}