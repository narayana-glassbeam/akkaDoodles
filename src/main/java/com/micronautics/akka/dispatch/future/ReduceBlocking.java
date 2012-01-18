package com.micronautics.akka.dispatch.future;

import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Function2;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;
import com.micronautics.util.HttpGetter;

/** TODO need a better use case. 
 * Invoke Future as a non-blocking function call, executed on another thread.
 *  This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
 *  Non-blocking fold is executed on the thread of the last Future to be completed.
 * If this Future is completed with an exception then the new Future will also contain this exception.
 * 
 * The call to Futures.future() forwards the HttpGetter Callables to Future.apply(), part of the Scala API. 
 * They are then sent to the ExecutionContext to be run. 
 * If the ExecutionContext is an Akka dispatcher then it does some additional preparation before queuing the futures for processing.
 * Futures.reduce() registers callback functions with each of the futures in order to collect all the results needed in order to produce the final result using applyFunction().
 * 
 * The Callable is not in scope of applyFunction2.apply(), unlike some other composable functions. 
 * That means that public properties from cannot be retrieved from the Callable.
 * If this is important, HttpGetter.call() should be modified to return a result object, perhaps a HashMap, that wraps the url and the resulting content.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class ReduceBlocking {
    /** daemonExecutorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates daemon threads. */
    private ExecutionContext daemonContext = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
    };

    /** Maximum length of time to block while waiting for futures to complete */
    private Duration timeout = Duration.create(10, SECONDS);

    /** Collection of futures, which Futures.sequence will turn into a Future of a collection.
     * These futures will run under daemonContext. */
    private ArrayList<Future<String>> daemonFutures = new ArrayList<Future<String>>();

    /** Composable function for both versions.
     * Akka has a <a href="http://www.assembla.com/spaces/akka/tickets/1663-future-reduce-should-accept-function2-r-t-r-">bug</a> which means that this class is broken. 
     * Future.reduce is currently defined to accept a Function2[R, T, T]. 
     * R could be a supertype of T, but that can currently not be exploited since the operation must return a T. 
     * Correct would be Function2<R,T,R>. It's wrong for Java and Scala. */
    private Function2<ArrayList<String>, String, ArrayList<String>> applyFunction = new Function2<ArrayList<String>, String, ArrayList<String>>() {
        public ArrayList<String> apply(ArrayList<String> result, String contents) {
            if (contents.indexOf("Simpler Concurrency")>0)
                result.add(contents);
            return result;
        }
    };


    {   /* Build array of Futures that will run on daemon threads. Remember that HttpGetter implements Callable */
    	daemonFutures.add(Futures.future(new HttpGetter("http://akka.io/"), daemonContext));
        daemonFutures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), daemonContext));
        daemonFutures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), daemonContext));
    }

    public void doit() {
        /* Commented to compile until Bug 1663 is fixed. 
        Future<ArrayList<String>> resultFuture = Futures.reduce(daemonFutures, applyFunction, daemonContext);
        // Await.result() blocks until the Future completes
        ArrayList<String> result = (ArrayList<String>) Await.result(resultFuture, timeout);
        System.out.println("Blocking reduce: " + result.size() + " web pages contained 'Simpler Concurrency'.");
        */
    }
    
    /** Demonstrates how to invoke reduce() and block until a result is available */
    public static void main(String[] args) {
    	new ReduceBlocking().doit();
    }
}