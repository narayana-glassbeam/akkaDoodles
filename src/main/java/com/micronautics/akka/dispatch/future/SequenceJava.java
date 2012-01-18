package com.micronautics.akka.dispatch.future;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Function;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;
import com.micronautics.util.HttpGetter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.TimeUnit.SECONDS;


/** Future.sequence takes the Iterable<Future<String>> and turns it into a Future<Iterable<String>>.
 * map() tests the Iterable<String> for possible inclusion in the result, which is an ArrayList of web pages containing 
 * "Simpler Concurrency".
 * @see http://akka.io/docs/akka/snapshot/java/futures.html
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java
 * @author Mike Slinn
 */
public class SequenceJava {
    /** executorService creates daemon threads, which shut down when the application exits. */
    private final ExecutorService executorService = DaemonExecutors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    /** Maximum length of time to wait for futures to complete */
    private Duration timeout = Duration.create(5, SECONDS);

    /** Collection of futures, which Futures.sequence will turn into a Future cf a collection */
    private ArrayList<Future<String>> futures = new ArrayList<Future<String>>();

    // import akka.japi.Function, not scala.Function!
    private Function<Iterable<String>, ArrayList<String>> createResult = new Function<Iterable<String>, ArrayList<String>>() {
        /** Invoked once each future has received a value.
         *  The URL that pointed to the page was discarded by Futures.sequence and is no longer available */
        public ArrayList<String> apply(Iterable<String> pages) {
            ArrayList<String> results = new ArrayList<String>();
            for (String page : pages)
                if (page.indexOf("Simpler Concurrency")>0)
                    results.add(page);
            return results; // how to tell map() it should release its thread now?
        }
    };
    

    {   // HttpGetter implements Callable
        futures.add(Futures.future(new HttpGetter("http://akka.io/"), context));
        futures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), context));
        futures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), context));
    }
    

    void blocking() {
        Future<Iterable<String>> futureListOfPages = Futures.sequence(futures, context);
        Future<ArrayList<String>> resultFuture = futureListOfPages.map(createResult);
        // Await.result() blocks until the Futures all complete
        ArrayList<String> result = (ArrayList<String>) Await.result(resultFuture, timeout);
        System.out.println(result.size() + " results: " + result);
    }

    public static void main(String[] args) {
        SequenceJava example = new SequenceJava();
        example.blocking();
    }
}