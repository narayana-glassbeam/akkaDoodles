package com.micronautics.akka.dispatch.futureJava;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Procedure2;

class OrNonBlocking {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    private final Procedure2<Throwable,Integer> completionFunction = new Procedure2<Throwable,Integer>() {
        
        public void apply(Throwable exception, Integer result) {
            if (result != null) {
                System.out.println("Nonblocking Java Or result: " + result);
            } else {
                System.out.println("Nonblocking Java Or exception: " + exception);
            }
            executorService.shutdown(); // terminates program if no other threads are running
        }
    };

    private final Callable<Integer> callable5 = new Callable<Integer>() {
        public Integer call() {
            return 2 + 3;
        }
    };

    private final Callable<Integer> callableException = new Callable<Integer>() {
        public Integer call() {
            return 6 / 0;
        }
    };


    public void doit() {
        Future<Integer> resultException = Futures.future(callableException, context);
        Future<Integer> result5 = Futures.future(callable5, context);
        Future<Integer> resultOr = resultException.or(result5);
        resultOr.onComplete(completionFunction);
    }

    public static void main(String[] args) {
        new OrNonBlocking().doit();
    }
}