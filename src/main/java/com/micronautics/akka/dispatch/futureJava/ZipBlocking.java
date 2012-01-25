package com.micronautics.akka.dispatch.futureJava;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import akka.dispatch.Await;
import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.util.Duration;

import com.micronautics.concurrent.DaemonExecutors;

class ZipBlocking {
    private final ExecutorService daemonExecutorService = DaemonExecutors.newFixedThreadPool(10);

    private final ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { daemonExecutorService.execute(r); }
    };

    private final Callable<Integer> callableInt = new Callable<Integer>() {
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
        Future<Integer> futureException = Futures.future(callableException, context);
        Future<Integer> futureInt = Futures.future(callableInt, context);
        Future<Integer> futureZip = futureException.zip(futureInt);
        Integer result = (Integer) Await.result(futureZip, timeout);
        System.out.println("Blocking Java Zip result: " + result);
    }

    public static void main(String[] args) {
        new ZipBlocking().doit();
    }
}