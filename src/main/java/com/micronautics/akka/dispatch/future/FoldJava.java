package com.micronautics.akka.dispatch.future;

import static akka.dispatch.Futures.future;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.Callable;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import scala.Either;
import scala.Option;
import akka.dispatch.Futures;
import akka.actor.ActorSystem;
import akka.dispatch.Await;
import akka.dispatch.Future;
import akka.dispatch.MessageDispatcher;
import akka.japi.Function2;
import akka.util.Duration;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/** Invoke Future as a non-blocking function call, executed on another thread.
 *  This example uses map to print URLs of web pages that contain the string {{{Simpler Concurrency}}}.
 *  Non-blocking fold is executed on the thread of the last Future to be completed.
 * If this Future is completed with an exception then the new Future will also contain this exception.
 * @see https://github.com/jboner/akka/blob/releasing-2.0-M2/akka-docs/java/code/akka/docs/future/FutureDocTestBase.java */
class FoldJava {
    private ActorSystem system = ActorSystem.create();
    private MessageDispatcher dispatcher = system.dispatcher();
    private DefaultHttpClient httpclient = new DefaultHttpClient();
    private Duration timeout = Duration.create(1, SECONDS);
    ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
    
    {
        futures.add(Futures.successful(httpGet("http://akka.io/"),                       dispatcher));
        futures.add(Futures.successful(httpGet("http://www.playframework.org/"),         dispatcher));
        futures.add(Futures.successful(httpGet("http://nbronson.github.com/scala-stm/"), dispatcher));
    }
    
    
    void blocking() {
        Future<String> resultFuture = Futures.fold("", futures, new Function2<String, String, String>() {
            public String apply(String url, String contents) {
                return contents.indexOf("Simpler Concurrency")>0 ? url : null;
              }
            }, dispatcher);
        // Await.result() blocks until the Future completes
        String result = (String) Await.result(resultFuture, timeout);
        System.out.println("Result: " + result);
    }

    void nonBlocking() {
        Future<String> resultFuture = Futures.fold("", futures, new Function2<String, String, String>() {
            public String apply(String url, String contents) {
                return contents.indexOf("Simpler Concurrency")>0 ? url : null;
              }
            }, dispatcher);
        /*resultFuture.onComplete(new Function<Future<String>>() { 
            public void apply(Future<String> future) {
                // This block is executed asynchronously
                Option<Either<Throwable,String>> result = future.value();
                System.out.println("Result: " + result.right);
                System.exit(0);
            }
        });*/
    }
    
    String httpGet(String urlStr) {
        HttpGet httpget = new HttpGet(urlStr);
        BasicResponseHandler brh = new BasicResponseHandler();
        try {
            return httpclient.execute(httpget, brh);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        FoldJava example = new FoldJava();
        example.blocking();
        example.nonBlocking();
    }
}