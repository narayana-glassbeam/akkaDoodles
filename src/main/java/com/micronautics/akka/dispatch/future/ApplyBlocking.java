package com.micronautics.akka.dispatch.future;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;

/** Invoke Future as a non-blocking function call, executed on another thread.
 * This is the simplest example because it does not use composition; it just shows how to work with a single Future.
 * If this Future is completed with an exception then the new Future will also contain this exception.
 * 
 * The call to Futures.future() forwards the Callables to Future.apply(), part of the Scala API. 
 * They are then sent to the ExecutionContext to be run. 
 * If the ExecutionContext is an Akka dispatcher then it does some additional preparation before queuing the futures for processing.
 * Await.result() registers callback functions with each of the futures in order to collect all the results needed in order to produce the final result using applyFunction().
 * 
 * The Callable is not in scope of applyFunction2.apply(), unlike some other composable functions. 
 * That means that public properties from cannot be retrieved from the Callable.
 * If this is important, HttpGetter.call() should be modified to return a result object, perhaps a HashMap, that wraps the url and the resulting content.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class ApplyBlocking {
    /** daemonExecutorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates daemon threads. */
    private ExecutionContext daemonContext = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
    };

    /** Maximum length of time to block while waiting for futures to complete */
    private Duration timeout = Duration.create(1, SECONDS);

    private Callable<Integer> callable = new Callable<Integer>() {
        public Integer call() {
            return 2 + 3;
        }
    };
        
        
    public void doit() {
        Future<Integer> resultFuture = Futures.future(callable, daemonContext);
        // Await.result() blocks until the Future completes
        Integer result = (Integer) Await.result(resultFuture, timeout);
        System.out.println("Blocking apply() result: " + result);
    }
    
    /** Demonstrates how to invoke apply() and block until a result is available */
    public static void main(String[] args) {
    	new ApplyBlocking().doit();
    }
}