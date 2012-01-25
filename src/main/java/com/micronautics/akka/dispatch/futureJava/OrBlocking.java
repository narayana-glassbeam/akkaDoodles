package com.micronautics.akka.dispatch.futureJava;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.micronautics.concurrent.DaemonExecutors;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Procedure2;
import akka.util.Duration;

class OrBlocking {
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    private final ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
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
    
    private final Duration timeout = Duration.create(1, SECONDS);


    public void doit() {
        Future<Integer> resultException = Futures.future(callableException, context);
        Future<Integer> result5 = Futures.future(callable5, context);
        Future<Integer> resultOr = resultException.or(result5);
        Integer result = (Integer) Await.result(resultOr, timeout);
        System.out.println("Blocking Java Or result: " + result);
    }

    public static void main(String[] args) {
        new OrBlocking().doit();
    }
}