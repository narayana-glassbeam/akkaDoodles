package com.micronautics.akka.dispatch.futureJava;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Procedure2;

class ScopeSin {
	private int offset = 6;
	
    /** executorService creates regular threads, which continue running when the application tries to exit. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates regular threads. */
    private final ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    /** onComplete handler for nonblocking version */
    private final Procedure2<Throwable,Integer> completionFunction = new Procedure2<Throwable,Integer>() {
        
    	/** This method is executed asynchronously, probably after the mainline has completed */
        public void apply(Throwable exception, Integer result) {
        	System.out.println("Entered onComplete, offset = " + offset);
            if (result != null) {
                System.out.println("ScopeSin Java result: " + result);
            } else {
                System.out.println("Exception: " + exception);
            }
            executorService.shutdown(); // terminates this thread, and the program if no other threads are active
        }
    };

    private final Callable<Integer> callableSin1 = new Callable<Integer>() {
        public Integer call() {
            return 2 + 3 + offset;
        }
    };

    private final Callable<Integer> callableSin2 = new Callable<Integer>() {
        public Integer call() {
            return 2 + 3 + accessor();
        }
    };


    private int accessor() { return offset; }
    
    /** Demonstrates how to invoke fold() asynchronously.
     * Regular threads are used, because execution continues past onComplete(), and the callback to onComplete()
     * needs to be available after the main program has finished execution. If daemon threads were used, the program
     * would exit before the onComplete() callback was invoked. This means that onComplete() must contain a means of
     * terminating the program, or setting up another callback for some other purpose. The program could be terminated
     * with a call to System.exit(0), or by invoking executorService.shutdown() to shut down the thread. */
    public void doit() {
        Future<Integer> resultFutureSin1 = Futures.future(callableSin1, context);
        Future<Integer> resultFutureSin2 = Futures.future(callableSin2, context);
        offset = 42;
        resultFutureSin1.onComplete(completionFunction);
        resultFutureSin2.onComplete(completionFunction);
        System.out.println("End of mainline, offset = " + offset);
    }

    public static void main(String[] args) {
        new ScopeSin().doit();
    }
}