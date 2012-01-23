package com.micronautics.akka.dispatch.futureJava;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.dispatch.ExecutionContext;
import akka.dispatch.Futures;
import akka.dispatch.japi.Future;
import akka.japi.Function;
import akka.japi.Procedure2;

import com.micronautics.util.HttpGetterWithUrl;
import com.micronautics.util.UrlAndContents;

/** '''Future<A> filter<A>(Function<A, Boolean>);'''
 * This example minimizes method definitions, like the Scala version, but unlike the Scala version is not very readable.
 * This example associates urls with their page contents by using the HttpGetterWithUrl helper class. */
public class FilterNonBlocking1b {
    /** executorService creates regular threads, which continue running when the application tries to exit. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates regular threads. */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable runnable) { executorService.execute(runnable); }
    };
    
    private List<HttpGetterWithUrl> httpGettersWithUrl = new LinkedList<HttpGetterWithUrl> (Arrays.asList(new HttpGetterWithUrl[] {
    	new HttpGetterWithUrl("http://akka.io/"),
    	new HttpGetterWithUrl("http://www.playframework.org/"),
    	new HttpGetterWithUrl("http://nbronson.github.com/scala-stm/")
    })); 

    
    private void doit() {
        for (HttpGetterWithUrl httpGetterWithUrl : httpGettersWithUrl) {
        	Future<UrlAndContents> resultFuture = Futures.future(httpGetterWithUrl, context);
            // Java type checking does not give clues as to the required types for Function:
            resultFuture.filter(new Function<UrlAndContents, Boolean>() {
            	public Boolean apply(UrlAndContents urlAndContents) {
                    return urlAndContents.contents.indexOf("Simpler Concurrency")>=0;
                }
            }); // urlStr is out of scope, so it cannot be associated with result in the next block
            // Java type checking does not give clues as to the required types for Procedure2:
            resultFuture.onComplete(new Procedure2<Throwable, UrlAndContents>() { 
            	/** This method is executed asynchronously, probably after the mainline has completed */
                public void apply(Throwable exception, UrlAndContents urlAndContents) {
                    if (urlAndContents.contents != null) {
                        System.out.println("Nonblocking Java filter result: " + urlAndContents.url);
                    } else {
                        System.out.println("Exception: " + exception);
                    }
                    executorService.shutdown(); // terminates program
                }
            });
        }
    }
    
    public static void main(String[] args) {
        FilterNonBlocking1b example = new FilterNonBlocking1b();
        example.doit();
    }
}
