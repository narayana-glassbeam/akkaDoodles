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

import com.micronautics.util.HttpGetter;

/** '''Future<A> filter<A>(Function<A, Boolean>);'''
 * This example minimizes method definitions, like the Scala version, but unlike the Scala version is not very readable.
 * Another difference from the Scala version is that the input (urls) are not associated with the resulting web pages; this
 * is due to a difference in how scopes are managed between Scala and Java. FilterNonBlocking1b and FilterNonBlocking2b correct this problem. */
public class FilterNonBlocking1a {
    /** executorService creates regular threads, which continue running when the application tries to exit. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates regular threads. */
    private ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable runnable) { executorService.execute(runnable); }
    };
    
    private List<HttpGetter> httpGetters = new LinkedList<HttpGetter> (Arrays.asList(new HttpGetter[] {
    	new HttpGetter("http://akka.io/"),
    	new HttpGetter("http://www.playframework.org/"),
    	new HttpGetter("http://nbronson.github.com/scala-stm/")
    })); 

    
    private void doit() {
        for (HttpGetter httpGetter : httpGetters) {
        	Future<String> resultFuture = Futures.future(httpGetter, context);
            // Java type checking does not give clues as to the required types for Function:
            resultFuture.filter(new Function<String, Boolean>() {
            	public Boolean apply(String urlStr) {
                    return urlStr.indexOf("Simpler Concurrency")>=0;
                }
            }); // urlStr is out of scope, so it cannot be associated with result in the next block
            // Java type checking does not give clues as to the required types for Procedure2:
            resultFuture.onComplete(new Procedure2<Throwable, String>() { 
            	/** This method is executed asynchronously, probably after the mainline has completed */
                public void apply(Throwable exception, String result) {
                    if (result != null) {
                        System.out.println("Nonblocking Java filter result: " + result.substring(0, 20) + "...");
                    } else {
                        System.out.println("Exception: " + exception);
                    }
                    executorService.shutdown(); // terminates program
                }
            });
        }
    }
    
    public static void main(String[] args) {
        FilterNonBlocking1a example = new FilterNonBlocking1a();
        example.doit();
    }
}
