package com.micronautics.akka.dispatch.future;

import static akka.dispatch.Futures.future;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import scala.Either;
import scala.Option;
import scala.runtime.BoxedUnit;
import akka.dispatch.Await;
import akka.dispatch.Future;
import akka.dispatch.ExecutionContext;
import akka.dispatch.MessageDispatcher;
import akka.japi.Function;
import akka.util.Duration;


/** Invoke Future as a non-blocking function call, executed on another thread.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class ApplyJava {
	private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private MessageDispatcher dispatcher = ExecutionContext.fromExecutorService(executorService);
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
        resultFuture.onComplete(new Function<Either<Throwable, Integer>, BoxedUnit>() { 
            public void apply(Future<Integer> whatever) {
                // This block is executed asynchronously
                Option<Either<Throwable,Integer>> result = whatever.right().get().value();
                System.out.println("Result: " + result);
                System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        ApplyJava example = new ApplyJava();
        example.blocking();
        example.nonBlocking();
    }
}