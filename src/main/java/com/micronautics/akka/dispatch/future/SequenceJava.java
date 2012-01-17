package com.micronautics.akka.dispatch.future;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.dispatch.MessageDispatcher;
import akka.japi.Function;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;
import com.micronautics.util.HttpGetter;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/** Future.sequence takes the Iterable<Future<String>> and turns it into a Future<Iterable<String>>.
 * We then use map() to work with the Iterable<String> directly, and test the values of the Iterable for possible
 * inclusion in the result. The result is an ArrayList of web pages containing "Simpler Concurrency"
 * @see http://akka.io/docs/akka/snapshot/java/futures.html
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java
 */
public class SequenceJava {
    private final ExecutorService executorService = DaemonExecutors.newFixedThreadPool(10);
    //private MessageDispatcher dispatcher = ExecutionContext.fromExecutorService(executorService);
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };
    private Duration timeout = Duration.create(5, SECONDS);
    
    private ArrayList<Future<String>> futures = new ArrayList<Future<String>>();

    {
        futures.add(Futures.future(new HttpGetter("http://akka.io/"), context));
        futures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), context));
        futures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), context));
    }

    
    void blocking() {
        Future<Iterable<String>> futureListOfPages = Futures.sequence(futures, context);
        // import akka.japi.Function, not scala.Function!
        Future<ArrayList<String>> resultFuture = futureListOfPages.map(new Function<Iterable<String>, ArrayList<String>>() {
            /** Invoked once each future has received a value.
             *  The URL that pointed to the page was discarded by Futures.sequence and is not available now */
            public ArrayList<String> apply(Iterable<String> pages) {
                ArrayList<String> results = new ArrayList<String>();
                for (String page : pages)
                    if (page.indexOf("Simpler Concurrency")>0)
                        results.add(page);
                return results; // how to tell map() it should release its thread now?
            }
        });
        // Await.result() blocks until the Futures all complete
        ArrayList<String> result = (ArrayList<String>) Await.result(resultFuture, timeout);
        System.out.println(result.size() + " results: " + result);
        System.exit(0); // map() keeps spinning forever
    }

    public static void main(String[] args) {
        SequenceJava example = new SequenceJava();
        example.blocking();
    }
}