package com.micronautics.akka.dispatch.futureJava;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;

class OrBlocking {
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    private final ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
    };

    private final Callable<Integer> callableInt = new Callable<Integer>() {
        public Integer call() {
            return 2 + 3;
        }
    };

    private final Callable<Integer> callableInt2 = new Callable<Integer>() {
        public Integer call() {
        	System.out.println("Evaluating callableInt2");
            return 2 + 3;
        }
    };

    private final Callable<String> callableString = new Callable<String>() {
        public String call() {
            return "asdf" + "qwer";
        }
    };

    private final Callable<Integer> callableException = new Callable<Integer>() {
        public Integer call() {
            return 6 / 0;
        }
    };
    
    private final Duration timeout = Duration.create(1, SECONDS);


    public void doit() {
        Future<Integer> futureException = Futures.future(callableException, context);
        Future<String>  futureString    = Futures.future(callableString,    context);
        Future<Integer> futureInt       = Futures.future(callableInt,       context);
        Future<Integer> futureInt2      = Futures.future(callableInt2,      context);
        // Not allowed; all Or'd futures must be of same type in Java
        //Future<Integer> futureOr = futureException.or(futureString.or(futureInt));
        Future<Integer> futureOr = futureException.or(futureInt.or(futureInt2));
        Integer result = (Integer) Await.result(futureOr, timeout);
        System.out.println("Blocking Java Or result: " + result);
    }

    public static void main(String[] args) {
        new OrBlocking().doit();
    }
}