package com.micronautics.akka.dispatch.future;

import akka.actor.ActorSystem;
import akka.dispatch.Await;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import static akka.dispatch.Futures.future;
import akka.dispatch.MessageDispatcher;
import akka.util.Duration;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import akka.japi.Function;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;


/** Future.sequence takes the Iterable<Future<String>> and turns it into a Future<Iterable<String>>.
 * We then use map() to work with the Iterable<String> directly, and test the values of the Iterable for possible
 * inclusion in the result. The result is an ArrayList of web pages containing "Simpler Concurrency"
 * @see http://akka.io/docs/akka/snapshot/java/futures.html
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java
 */
class SequenceJava {
    private ActorSystem system = ActorSystem.create();
    private MessageDispatcher dispatcher = system.dispatcher();
    private DefaultHttpClient httpClient = new DefaultHttpClient();
    private Duration timeout = Duration.create(1, SECONDS);
    ArrayList<Future<String>> futures = new ArrayList<Future<String>>();

    {
        futures.add(Futures.successful(httpGet("http://akka.io/"),                       dispatcher));
        futures.add(Futures.successful(httpGet("http://www.playframework.org/"),         dispatcher));
        futures.add(Futures.successful(httpGet("http://nbronson.github.com/scala-stm/"), dispatcher));
    }

    
    void blocking() {
        Future<Iterable<String>> futureListOfPages = Futures.sequence(futures, dispatcher);
        // import akka.japi.Function, not scala.Function!
        Future<ArrayList<String>> resultFuture = futureListOfPages.map(new Function<Iterable<String>, ArrayList<String>>() {
            /** Invoked once each future has received a value */
            public ArrayList<String> apply(Iterable<String> pages) {
                ArrayList<String> results = new ArrayList<String>();
                for (String page : pages)
                    if (page.indexOf("Simpler Concurrency")>0)
                        results.add(page); // the URL pointing to the page is long gone
                return results;
            }
        });
        // Await.result() blocks until the Future completes
        ArrayList<String> result = (ArrayList<String>) Await.result(resultFuture, timeout);
        System.out.println(result.size() + " results: " + result);
        System.exit(0);
    }

    /** Invoke Future as a non-blocking function call, executed on another thread. */
    void nonBlocking() {
        Future<Integer> resultFuture = future(new Callable<Integer>() {
          public Integer call() {
            return 2 + 3;
          }
        }, dispatcher);
        /*resultFuture.onComplete(new Function<Future<Integer>>() { 
            public void apply(Future<Integer> future) {
                // This block is executed asynchronously
                Option<Either<Throwable,Integer>> result = future.value();
                System.out.println("Result: " + result);
                System.exit(0);
            }
        });*/
    }

    public static void main(String[] args) {
        SequenceJava example = new SequenceJava();
        example.blocking();
        example.nonBlocking();
    }

    private String httpGet(String urlStr) {
        HttpGet httpget = new HttpGet(urlStr);
        BasicResponseHandler brh = new BasicResponseHandler();
        try {
            return httpClient.execute(httpget, brh);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}