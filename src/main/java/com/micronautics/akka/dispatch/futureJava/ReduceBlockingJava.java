package com.micronautics.akka.dispatch.futureJava;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Function2;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;

/** Invoke Future as a non-blocking function call, executed on another thread.
 * This example uses reduce to sum the results of a Future of an expensive computation.
 * Reduce is executed on the thread of the last Future to be completed, and apply blocks until the futures have all been processed.
 * 
 * The call to Futures.future() forwards the Callables to Future.apply(), part of the Scala API. 
 * They are then sent to the ExecutionContext to be run. 
 * If the ExecutionContext is an Akka dispatcher then it does some additional preparation before queuing the futures for processing.
 * Futures.reduce() registers callback functions with each of the futures in order to collect all the results needed in order to produce the final result using applyFunction().
 * 
 * Future.reduce accepts a Function2[R, R, T]. 
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class ReduceBlockingJava {
    /** daemonExecutorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates daemon threads. */
    private final ExecutionContext daemonContext = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
    };

    /** Maximum length of time to block while waiting for futures to complete */
    private final Duration timeout = Duration.create(10, SECONDS);

    /** Collection of futures, which Futures.sequence will turn into a Future of a collection.
     * These futures will run under daemonContext. */
    private final ArrayList<Future<Long>> daemonFutures = new ArrayList<Future<Long>>();

    /** Composable function for both versions. */
    private final Function2<Long, Long, Long> sum = new Function2<Long, Long, Long>() {
    	public Long apply(Long result, Long item) { return result + item; }
    };


    {   /* Build array of Futures that will run on daemon threads. ExpensiveCalc implements Callable */
    	for (int i=1; i<=100; i++)
    	    daemonFutures.add(Futures.future(new ExpensiveCalc(i), daemonContext));
    }

    private void doit() {
        Future<Long> resultFuture = Futures.reduce(daemonFutures, sum, daemonContext);
        // Await.result() blocks until the Future completes
        Long result = (Long) Await.result(resultFuture, timeout);
        System.out.println("Blocking Java reduce result: " + result);
    }
    
    /** Demonstrates how to invoke reduce() and block until a result is available */
    public static void main(String[] args) {
    	new ReduceBlockingJava().doit();
    }
    
    private class ExpensiveCalc implements Callable<Long> {
        private Integer x;
        
    	public ExpensiveCalc(Integer x) { this.x = x; }
		
    	/** Pretend this method consumes a lot of computational resource */
    	@Override
		public Long call() { return 1L * x * x; }
    }
}