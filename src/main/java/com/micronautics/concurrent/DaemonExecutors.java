package com.micronautics.concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** @author Mike Slinn */
public class DaemonExecutors {
    public static final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.AbortPolicy();


    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new DaemonThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    static class DaemonThreadPoolExecutor extends ThreadPoolExecutor {

        public DaemonThreadPoolExecutor(int corePoolSize,
                                        int maximumPoolSize,
                                        long keepAliveTime,
                                        TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                    new DefaultThreadFactory(), defaultHandler);
        }

        static class DefaultThreadFactory implements ThreadFactory {
            static final AtomicInteger poolNumber = new AtomicInteger(1);
            final ThreadGroup group;
            final AtomicInteger threadNumber = new AtomicInteger(1);
            final String namePrefix;

            DefaultThreadFactory() {
                SecurityManager s = System.getSecurityManager();
                group = (s != null) ? s.getThreadGroup() :
                        Thread.currentThread().getThreadGroup();
                namePrefix = "pool-" +
                        poolNumber.getAndIncrement() +
                        "-thread-";
            }

            public Thread newThread(Runnable r) {
                Thread t = new Thread(group, r,
                        namePrefix + threadNumber.getAndIncrement(),
                        0);
                if (!t.isDaemon())
                    t.setDaemon(true);
                if (t.getPriority() != Thread.NORM_PRIORITY)
                    t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        }
    }
}
