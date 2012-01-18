package com.micronautics.akka.dispatch.future;

import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.ExecutorService;

import scala.Either;
import scala.Option;
import akka.dispatch.Futures;
import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.japi.Function2;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;
import com.micronautics.util.HttpGetter;

import java.util.ArrayList;


/** Invoke Future as a non-blocking function call, executed on another thread.
 *  This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
 *  Map creates a new Future by applying a function to a successful Future result. 
 * If this Future is completed with an exception then the new Future will also contain this exception.
 * Map produces simpler code than using Future.filter.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class MapJava {
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

    private Function2<String, String, String[]> createResult = new Function2<String, String, String[]>() {
        public String[] apply(String url, String contents) {
            if (contents.indexOf("Simpler Concurrency")>0)
            	result.add(url);
            return result.toArray(new String[3]);
        }
    };


    {   // HttpGetter implements Callable
        futures.add(Futures.future(new HttpGetter("http://akka.io/"), context));
        futures.add(Futures.future(new HttpGetter("http://www.playframework.org/"), context));
        futures.add(Futures.future(new HttpGetter("http://nbronson.github.com/scala-stm/"), context));
    }

    
    void blocking() {
        Future<String[]> resultFuture = Futures.map(futures, context);// .toArray(new Future<String[]>());
        // Await.result() blocks until the Future completes
        String[] result = (String[]) Await.result(resultFuture, timeout);
        System.out.println("Result: " + result);
    }

    /** Futures.map() is not defined. Is this deliberate or an oversight? 
     * The test cases use the Scala Future.map() method instead of the Java Futures class. 
     * When should the Java Futures methods be used, and when should Java code use the Scala Future methods? */
    void nonBlocking() {
        /*Future<Integer> resultFuture = futures.map(new Function2<String, String, String>() {
            public String apply(String url, String contents) {
              return contents.indexOf("Simpler Concurrency")>0 ? url : null;
            }
          }, dispatcher);*/
        /*resultFuture.onComplete(new Function<Future<Integer>>() { 
            public void apply(Future<Integer> future) {
                // This block is executed asynchronously
                Option<Either<Throwable,String>> result = future.value();
                System.out.println("Result: " + result.right);
                System.exit(0);
            }
        });*/
    }
    
    public static void main(String[] args) {
        MapJava example = new MapJava();
        example.blocking();
        example.nonBlocking();
    }
}