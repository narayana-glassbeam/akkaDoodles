package com.micronautics.akka.dispatch.future;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Function2;
import akka.japi.Procedure2;
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
 * Futures.fold() registers callback functions with each of the futures in order to collect all the results needed in order to produce the final result using applyFunction().
 * 
 * The Callable is not in scope of applyFunction2.apply(), unlike some other composable functions. 
 * That means that public properties from cannot be retrieved from the Callable.
 * If this is important, HttpGetter.call() should be modified to return a result object, perhaps a HashMap, that wraps the url and the resulting content.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class FoldJava {
    /** daemonExecutorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    /** executorService creates regular threads, which continue running when the application tries to exit. */
    private final ExecutorService executorService       = Executors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control */
    private ExecutionContext daemonContext = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
    };

    /** Akka uses the execution context to manage futures under its control */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    /** Maximum length of time to wait for futures to complete */
    private Duration timeout = Duration.create(10, SECONDS);

    /** Collection of futures, which Futures.sequence will turn into a Future of a collection.
     * These futures will run under daemonContext. */
    private ArrayList<Future<String>> daemonFutures = new ArrayList<Future<String>>();

    /** Collection of futures, which Futures.sequence will turn into a Future of a collection.
     * These futures will run under a regular context. */
    private ArrayList<Future<String>> futures       = new ArrayList<Future<String>>();

    protected ArrayList<String> result = new ArrayList<String>();

    private Function2<ArrayList<String>, String, ArrayList<String>> applyFunction = new Function2<ArrayList<String>, String, ArrayList<String>>() {
        public ArrayList<String> apply(ArrayList<String> result, String contents) {
            if (contents.indexOf("Simpler Concurrency")>0)
                result.add(contents);
            return result;
        }
    };

    private Procedure2<Throwable,ArrayList<String>> completionFunction = new Procedure2<Throwable,ArrayList<String>>() {
        /** This method is executed asynchronously, probably after the mainline has completed */
        public void apply(Throwable exception, ArrayList<String> result) {
            if (result != null) {
                System.out.println("onComplete version: " + result.size() + " web pages contained 'Simpler Concurrency'.");
            } else {
                System.out.println("Exception: " + exception);
            }
            executorService.shutdown(); // terminates program
        }
    };


    {   /* Build array of Futures that will run on daemon threads. Remember that HttpGetter implements Callable */
    	daemonFutures.add(Futures.future(new HttpGetter("http://akka.io/"), daemonContext));
        daemonFutures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), daemonContext));
        daemonFutures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), daemonContext));
    }

    {   /* Build array of Futures that will run on regular threads. Remember that HttpGetter implements Callable */
    	futures.add(Futures.future(new HttpGetter("http://akka.io/"), context));
        futures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), context));
        futures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), context));
    }


    void blocking() {
    	result.clear();
        Future<ArrayList<String>> resultFuture = Futures.fold(result, daemonFutures, applyFunction, daemonContext);
        // Await.result() blocks until the Future completes
        ArrayList<String> result = (ArrayList<String>) Await.result(resultFuture, timeout);
        System.out.println("blocking version: " + result.size() + " web pages contained 'Simpler Concurrency'.");
    }

    /** Regular threads are used, because execution continues past onComplete(), and the callback to onComplete()
     * needs to be available after the main program has finished execution. If daemon threads were used, the program
     * would exit before the onComplete() callback was invoked. This means that onComplete() must contain a means of
     * terminating the program, or setting up another callback for some other purpose. The program could be terminated
     * with a call to System.exit(0), or by suspending the thread. */
    void nonBlocking() {
    	result.clear();
        Future<ArrayList<String>> resultFuture = Futures.fold(result, futures, applyFunction, context);
        resultFuture.onComplete(completionFunction);
    }

    public static void main(String[] args) {
        FoldJava example = new FoldJava();
        example.blocking();
        example.nonBlocking();
    }
}