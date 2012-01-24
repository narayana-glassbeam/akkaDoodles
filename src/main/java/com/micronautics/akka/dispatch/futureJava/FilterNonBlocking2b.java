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
 * This example is similar to FilterNonBlocking2 in that extra method definitions to increase legibility, at the expense of increased verbosity.
 * This example associates urls with their page contents by using the HttpGetterWithUrl helper class. */
public class FilterNonBlocking2b {
    /** executorService creates regular threads, which continue running when the application tries to exit. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /** Akka uses the execution context to manage futures under its control. This ExecutionContext creates regular threads. */
    private final ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable runnable) { executorService.execute(runnable); }
    };

    private final List<HttpGetterWithUrl> httpGetters = new LinkedList<HttpGetterWithUrl> (Arrays.asList(new HttpGetterWithUrl[] {
    	new HttpGetterWithUrl("http://akka.io/"),
    	new HttpGetterWithUrl("http://www.playframework.org/"),
    	new HttpGetterWithUrl("http://nbronson.github.com/scala-stm/")
    }));

    /** Java type checking does not give clues as to the required types for Procedure2 */
    private final Procedure2<Throwable,UrlAndContents> completionFunction = new Procedure2<Throwable,UrlAndContents>() {
    	/** This method is executed asynchronously, probably after the mainline has completed */
        public void apply(Throwable exception, UrlAndContents result) {
            if (result.contents != null)
                System.out.println("Nonblocking Java filter result: " + result.url);
            executorService.shutdown(); // terminates program
        }
    };

    /** Invoked after future completes
     * Java type checking does not give clues as to the required types for Function */
    private final Function<UrlAndContents, Boolean> filterFunction = new Function<UrlAndContents, Boolean>() {
    	public Boolean apply(UrlAndContents urlAndContents) {
            return urlAndContents.contents.indexOf("Simpler Concurrency")>=0;
        }
    };


    private void doit() {
        for (HttpGetterWithUrl httpGetter : httpGetters) {
        	Future<UrlAndContents> resultFuture = Futures.future(httpGetter, context);
            resultFuture.filter(filterFunction);
            resultFuture.onComplete(completionFunction);
        }
    }

    public static void main(String[] args) {
        FilterNonBlocking2b example = new FilterNonBlocking2b();
        example.doit();
    }
}
