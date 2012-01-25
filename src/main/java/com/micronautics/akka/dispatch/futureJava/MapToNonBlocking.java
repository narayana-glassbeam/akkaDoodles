package com.micronautics.akka.dispatch.futureJava;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.dispatch.ExecutionContext;
import akka.dispatch.Future;
import akka.dispatch.Futures;
import akka.japi.Procedure2;

class MapToNonBlocking {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final ExecutionContext context = new ExecutionContext() {
        public void execute(Runnable r) { executorService.execute(r); }
    };

    private final Procedure2<Throwable,CharSequence> completionFunction = new Procedure2<Throwable,CharSequence>() {
        
        public void apply(Throwable exception, CharSequence result) {
            if (result != null) {
                System.out.println("Nonblocking Java mapTo result: " + result);
            } else {
                System.out.println("Nonblocking Java mapTo exception: " + exception);
            }
            executorService.shutdown(); // terminates program if no other threads are running
        }
    };

    private final Callable<String> callableString = new Callable<String>() {
        public String call() {
            return "asdf" + "qwer";
        }
    };


    public void doit() {
        Future<CharSequence> future = Futures.future(callableString, context).mapTo(CharSequence.class);
        future.onComplete(completionFunction);
    }

    public static void main(String[] args) {
        new MapToNonBlocking().doit();
    }
}