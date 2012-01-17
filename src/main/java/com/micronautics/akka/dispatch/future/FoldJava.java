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

    /** Collection of futures, which Futures.sequence will turn into a Future cf a collection */
    private ArrayList<Future<String>> futures = new ArrayList<Future<String>>();

    {   // HttpGetter implements Callable
        futures.add(Futures.future(new HttpGetter("http://akka.io/"), context));
        futures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), context));
        futures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), context));
    }

    protected ArrayList<String> result = new ArrayList<String>();


    void blocking() {
        Function2<String, String, ArrayList<String>> function2 = new Function2<String, String, ArrayList<String>>() {
            public ArrayList<String> apply(String url, String contents) {
                if (contents.indexOf("Simpler Concurrency")>0)
                    result.add(url);
                return result;
            }
        };
        Future<ArrayList<String>> resultFuture = Futures.fold(result, futures, function2, context);
        // Await.result() blocks until the Future completes
        ArrayList<String> result = (ArrayList<String>) Await.result(resultFuture, timeout);
        System.out.println(result.size() + " web pages contained 'Simpler Concurrency'");
    }

    void nonBlocking() {
        Future<String> resultFuture = Futures.fold("", futures, new Function2<String, String, String>() {
            public String apply(String url, String contents) {
                return contents.indexOf("Simpler Concurrency")>0 ? url : null;
              }
            }, context);
        /*resultFuture.onComplete(new Function<Future<String>>() {
            public void apply(Future<String> future) {
                // This block is executed asynchronously
                Option<Either<Throwable,String>> result = future.value();
                System.out.println("Result: " + result.right);
            }
        });*/
    }

    public static void main(String[] args) {
        FoldJava example = new FoldJava();
        example.blocking();
        example.nonBlocking();
    }
}