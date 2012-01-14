package com.micronautics.akka.dispatch.future;

import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.ArrayList;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import akka.actor.ActorSystem;
import akka.dispatch.Await;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.dispatch.MessageDispatcher;
import akka.japi.Function;
import akka.util.Duration;


/** Future.sequence takes the Iterable<Future<String>> and turns it into a Future<Iterable<String>>.
 * We then use map() to work with the Iterable<String> directly, and test the values of the Iterable for possible
 * inclusion in the result. The result is an ArrayList of web pages containing "Simpler Concurrency"
 * @see http://akka.io/docs/akka/snapshot/java/futures.html
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java
 */
class SequenceJava {
    private DefaultHttpClient httpClient = new DefaultHttpClient();
    private BasicResponseHandler brh = new BasicResponseHandler();

    private ActorSystem system = ActorSystem.create();
    private MessageDispatcher dispatcher = system.dispatcher();
    private Duration timeout = Duration.create(1, SECONDS);
    
    private ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
    {
        futures.add(Futures.successful(httpGet("http://akka.io/"),                       dispatcher));
        futures.add(Futures.successful(httpGet("http://www.playframework.org/"),         dispatcher));
        futures.add(Futures.successful(httpGet("http://nbronson.github.com/scala-stm/"), dispatcher));
    }

    
    void blocking() {
        Future<Iterable<String>> futureListOfPages = Futures.sequence(futures, dispatcher);
        // import akka.japi.Function, not scala.Function!
        Future<ArrayList<String>> resultFuture = futureListOfPages.map(new Function<Iterable<String>, ArrayList<String>>() {
            /** Invoked once each future has received a value.
             *  The URL that pointed to the page was discarded by Futures.sequence and is not available now */
            public ArrayList<String> apply(Iterable<String> pages) {
                ArrayList<String> results = new ArrayList<String>();
                for (String page : pages)
                    if (page.indexOf("Simpler Concurrency")>0)
                        results.add(page);
                return results;
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

    private String httpGet(String urlStr) {
        HttpGet httpget = new HttpGet(urlStr);
        try {
            return httpClient.execute(httpget, brh);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}