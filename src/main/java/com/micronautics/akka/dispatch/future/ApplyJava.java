package com.micronautics.akka.dispatch.future;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.micronautics.concurrent.DaemonExecutors;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.util.Duration;


/** Invoke Future as a non-blocking function call, executed on another thread.
 * This is the simplest example because it does not use composition; it just shows how to work with a single Future.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class ApplyJava {
	// The following could be used to obtain the default dispatcher, but it incurs the overhead of Actors
	// a context or a dispatcher can be provided as the second argument to Futures.future
	//import akka.actor.ActorSystem;
	//import akka.dispatch.MessageDispatcher;
    //private ActorSystem system = ActorSystem.create();
    //private MessageDispatcher dispatcher = system.dispatcher();
    
    /** executorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService executorService = DaemonExecutors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    /** Maximum length of time to wait for future to complete */
    private Duration timeout = Duration.create(1, SECONDS);
    
    
    void blocking() {
        Future<Integer> resultFuture = Futures.future(new Callable<Integer>() {
          public Integer call() {
            return 2 + 3;
          }
        }, context);
        // Await.result() blocks until the Future completes
        Integer result = (Integer) Await.result(resultFuture, timeout);
        System.out.println("Result: " + result);
    }

    void nonBlocking() {
        Future<Integer> resultFuture = Futures.future(new Callable<Integer>() {
          public Integer call() {
            return 2 + 3;
          }
        }, context);
        /*resultFuture.onComplete(new Function<Future<Integer>>() { 
            public void apply(Future<Integer> future) {
                // This block is executed asynchronously
                Option<Either<Throwable,Integer>> result = future.value();
                System.out.println("Result: " + result);
            }
        });*/
    }

    public static void main(String[] args) {
        ApplyJava example = new ApplyJava();
        example.blocking();
        example.nonBlocking();
    }
}