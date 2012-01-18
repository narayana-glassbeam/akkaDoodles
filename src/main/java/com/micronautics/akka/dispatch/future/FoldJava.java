package com.micronautics.akka.dispatch.future;

import akka.dispatch.Futures;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.ExecutorService;

import scala.Either;
import scala.Option;
import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.japi.Function;
import akka.japi.Function2;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;
import com.micronautics.util.HttpGetter;

import java.util.ArrayList;

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
    /** executorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService executorService = DaemonExecutors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    /** Maximum length of time to wait for futures to complete */
    private Duration timeout = Duration.create(10, SECONDS);

    /** Collection of futures, which Futures.sequence will turn into a Future of a collection */
    private ArrayList<Future<String>> futures = new ArrayList<Future<String>>();

    protected ArrayList<String> result = new ArrayList<String>();

    private Function2<ArrayList<String>, String, ArrayList<String>> applyFunction = new Function2<ArrayList<String>, String, ArrayList<String>>() {
        public ArrayList<String> apply(ArrayList<String> result, String contents) {
            if (contents.indexOf("Simpler Concurrency")>0)
                result.add(contents);
            return result;
        }
    };

    private Function<Either<Throwable,ArrayList<String>>,ArrayList<String>> completionFunction = new Function<Either<Throwable,ArrayList<String>>,ArrayList<String>>() {
        /** This method is executed asynchronously */
        public void apply(Either<Throwable,ArrayList<String>,> either) {
            System.out.println("Result: " + either);
        }
    };


    {   /* HttpGetter implements Callable */
        futures.add(Futures.future(new HttpGetter("http://akka.io/"), context));
        futures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), context));
        futures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), context));
    }


    void blocking() {
        Future<ArrayList<String>> resultFuture = Futures.fold(result, futures, applyFunction, context);
        // Await.result() blocks until the Future completes
        ArrayList<String> result = (ArrayList<String>) Await.result(resultFuture, timeout);
        System.out.println(result.size() + " web pages contained 'Simpler Concurrency'");
    }

    void nonBlocking() {
        Future<ArrayList<String>> resultFuture = Futures.fold(result, futures, applyFunction, context);
        resultFuture.onComplete(completionFunction);
    }

    public static void main(String[] args) {
        FoldJava example = new FoldJava();
        example.blocking();
        example.nonBlocking();
    }
}