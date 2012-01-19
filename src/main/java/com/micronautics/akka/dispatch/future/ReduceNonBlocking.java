package com.micronautics.akka.dispatch.future;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Function2;
import akka.japi.Procedure2;

/** Invoke Future as a non-blocking function call, executed on another thread.
 * This example uses reduce to sum the results of a Future of an expensive computation.
 * Non-blocking reduce is executed on the thread of the last Future to be completed.
 * 
 * The call to Futures.future() forwards the Callables to Future.apply(), part of the Scala API. 
 * They are then sent to the ExecutionContext to be run. 
 * If the ExecutionContext is an Akka dispatcher then it does some additional preparation before queuing the futures for processing.
 * Futures.reduce() registers callback functions with each of the futures in order to collect all the results needed in order to produce the final result using applyFunction().
 * 
 * Future.reduce accepts a Function2[R, R, T]. 
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class ReduceNonBlocking {
    /** executorService creates regular threads, which continue running when the application tries to exit. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates regular threads. */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    /** Collection of futures, which Futures.sequence will turn into a Future of a collection.
     * These futures will run under a regular context. */
    private ArrayList<Future<Long>> futures = new ArrayList<Future<Long>>();

    /** Composable function for both versions. */
    private Function2<Long, Long, Long> sum = new Function2<Long, Long, Long>() {
    	public Long apply(Long result, Long item) { return result + item; }
    };
    
    /** onComplete handler for nonblocking version */
    private Procedure2<Throwable, Long> completionFunction = new Procedure2<Throwable, Long>() {
        
    	/** This method is executed asynchronously, probably after the mainline has completed */
        public void apply(Throwable exception, Long result) {
            if (result != null) {
                System.out.println("Nonblocking Java reduce result: " + result);
            } else {
                System.out.println("Nonblocking Java reduce exception: " + exception);
            }
            executorService.shutdown(); // terminates program
        }
    };


    {   /* Build array of Futures that will run on regular threads. ExpensiveCalc implements Callable */
    	for (int i=1; i<=100; i++)
    	    futures.add(Futures.future(new ExpensiveCalc(i), context));
    }


    /** Demonstrates how to invoke reduce() asynchronously.
     * Regular threads are used, because execution continues past the invocation of onComplete(), and the callback to onComplete()
     * needs to be available after the main program has finished execution. If daemon threads were used, the program
     * would exit before the onComplete() callback was invoked. This means that onComplete() must contain a means of
     * terminating the program, or setting up another callback for some other purpose. The program could be terminated
     * with a call to System.exit(0), or by invoking executorService.shutdown() to shut down the thread. */
    void doit() {
        Future<Long> resultFuture = Futures.reduce(futures, sum, context);
        resultFuture.onComplete(completionFunction);
    }

    public static void main(String[] args) {
        new ReduceNonBlocking().doit();
    }
    
    private class ExpensiveCalc implements Callable<Long> {
        private Integer x;
        
    	public ExpensiveCalc(Integer x) { this.x = x; }
		
    	@Override
		public Long call() { return 1L * x * x; }
    }
}