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

/** '''Future<A> filter<A>(Function<A, Boolean>);''' */
public class FilterNonBlocking2 {
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

    /** Java type checking does not give clues as to the required types of Procedure2 */
    private Procedure2<Throwable,String> completionFunction = new Procedure2<Throwable,String>() {
    	/** This method is executed asynchronously, probably after the mainline has completed.
    	 * Cannot associate the url with the resulting page contents */
        public void apply(Throwable exception, String result) {
            if (result != null) {
                System.out.println("Nonblocking Java filter result: " + result.substring(0, 20) + "...");
            } else {
                System.out.println("Exception: " + exception);
            }
            executorService.shutdown(); // terminates program
        }
    };
      
    /** Invoked after future completes 
     * Java type checking does not give clues as to the required types of Function */
    private Function<String, Boolean> filterFunction = new Function<String, Boolean>() {
    	public Boolean apply(String urlStr) {
            return urlStr.indexOf("Simpler Concurrency")>=0;
        }
    };

    
    private void doit() {
        for (HttpGetter httpGetter : httpGetters) {
        	Future<String> resultFuture = Futures.future(httpGetter, context);
            resultFuture.filter(filterFunction);
            resultFuture.onComplete(completionFunction);
        }
    }
    
    public static void main(String[] args) {
        FilterNonBlocking2 example = new FilterNonBlocking2();
        example.doit();
    }
}
