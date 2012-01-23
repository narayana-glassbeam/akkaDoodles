package com.micronautics.akka.dispatch.futureJava;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Function;
import akka.japi.Procedure2;

import com.micronautics.util.HttpGetter;

/** Invoke Future as a non-blocking function call, executed on another thread.
 * This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
 * Non-blocking fold is executed on the thread of the last Future to be completed.
 * If this Future is completed with an exception then the new Future will also contain this exception.
 * 
 * The call to Futures.future() forwards the HttpGetter Callables to Future.apply(), part of the Scala API. 
 * They are then sent to the ExecutionContext to be run. 
 * If the ExecutionContext is an Akka dispatcher then it does some additional preparation before queuing the futures for processing.
 * Futures.traverse() registers callback functions with each of the futures in order to collect all the results needed in order to produce the final result using applyFunction().
 * 
 * The Callable is not in scope of applyFunction2.apply(), unlike some other composable functions. 
 * That means that public properties from cannot be retrieved from the Callable.
 * If this is important, HttpGetter.call() should be modified to return a result object, perhaps a HashMap, that wraps the url and the resulting content.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class TraverseNonBlocking {
    /** executorService creates regular threads, which continue running when the application tries to exit. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates regular threads. */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    /** Collection of String, which Futures.traverse will turn into a Future of a collection.
     * The futures will run under a regular context. */
    private ArrayList<String> urls = new ArrayList<String>();

    /** Composable function for both versions */
    private Function<String, Future<String>> applyFunction = new Function<String, Future<String>>() {
        public Future<String> apply(final String url) {
            return Futures.future(new HttpGetter(url, "Simpler Concurrency"), context);
        }
    };
    
    /** onComplete handler for nonblocking version */
    private Procedure2<Throwable,LinkedList<String>> completionFunction = new Procedure2<Throwable,LinkedList<String>>() {
        
    	/** This method is executed asynchronously, probably after the mainline has completed */
        public void apply(Throwable exception, LinkedList<String> result) {
            if (result != null) {
                int matchCount = 0;
                for (String r : result)
                	if (r.length()>0)
                		matchCount++;
                System.out.println("Nonblocking traverse: " + matchCount + " web pages contained 'Simpler Concurrency'.");
            } else {
                System.out.println("Exception: " + exception);
            }
            executorService.shutdown(); // terminates program
        }
    };


    {   /* Build array of URL Strings. */
    	urls.add("http://akka.io/");
        urls.add("http://www.playframework.org/");
        urls.add("http://nbronson.github.com/scala-stm/");
    }


    /** Demonstrates how to invoke traverse() asynchronously.
     * Regular threads are used, because execution continues past onComplete(), and the callback to onComplete()
     * needs to be available after the main program has finished execution. If daemon threads were used, the program
     * would exit before the onComplete() callback was invoked. This means that onComplete() must contain a means of
     * terminating the program, or setting up another callback for some other purpose. The program could be terminated
     * with a call to System.exit(0), or by invoking executorService.shutdown() to shut down the thread. */
    void doit() {
        Future<Iterable<String>> resultFuture = Futures.traverse(urls, applyFunction, context);
        resultFuture.onComplete(completionFunction);
    }

    public static void main(String[] args) {
        new TraverseNonBlocking().doit();
    }
}