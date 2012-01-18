package com.micronautics.akka.dispatch.future;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Function;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;
import com.micronautics.util.HttpGetter;

/** Invoke Future as a non-blocking function call, executed on another thread.
 *  This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
 *  Non-blocking fold is executed on the thread of the last Future to be completed.
 * If this Future is completed with an exception then the new Future will also contain this exception.
 * 
 * The call to Futures.future() forwards the HttpGetter Callables to Future.apply(), part of the Scala API. 
 * They are then sent to the ExecutionContext to be run. 
 * If the ExecutionContext is an Akka dispatcher then it does some additional preparation before queuing the futures for processing.
 * Futures.sequence() registers callback functions with each of the futures in order to collect all the results needed in order to produce the final result using applyFunction().
 * 
 * The Callable is not in scope of applyFunction2.apply(), unlike some other composable functions. 
 * That means that public properties from cannot be retrieved from the Callable.
 * If this is important, HttpGetter.call() should be modified to return a result object, perhaps a HashMap, that wraps the url and the resulting content.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class TraverseBlocking {
    /** daemonExecutorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates daemon threads. */
    private ExecutionContext daemonContext = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
    };

    /** Maximum length of time to block while waiting for futures to complete */
    private Duration timeout = Duration.create(10, SECONDS);

    /** Collection of String, which Futures.traverse will turn into a Future of a collection.
     * The futures will run under daemonContext. */
    private ArrayList<String> urls = new ArrayList<String>();

    /** Accumulates result during fold(), also provides initial results, if desired. */
    protected ArrayList<String> initialValue = new ArrayList<String>();

    /** Composable function for both versions. Remember that HttpGetter implements Callable. */
    private Function<String, Future<String>> applyFunction = new Function<String, Future<String>>() {
        public Future<String> apply(final String url) {
            return Futures.future(new HttpGetter(url, "Simpler Concurrency"), daemonContext);
        }
    };


    {   /* Build array of URL Strings. */
    	urls.add("http://akka.io/");
        urls.add("http://www.playframework.org/");
        urls.add("http://nbronson.github.com/scala-stm/");
    }

    public void doit() {
        Future<Iterable<String>> resultFuture = Futures.traverse(urls, applyFunction, daemonContext);
        // Await.result() blocks until the Future completes
        LinkedList<String> result = (LinkedList<String>) Await.result(resultFuture, timeout);
        int matchCount = 0;
        for (String r : result)
        	if (r.length()>0)
        		matchCount++;
        System.out.println("Blocking traverse: " + matchCount + " web pages contained 'Simpler Concurrency'.");
    }
    
    /** Demonstrates how to invoke sequence() and block until a result is available */
    public static void main(String[] args) {
    	new TraverseBlocking().doit();
    }
}