package com.adtsw.jcommons.execution;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

/**
 * Blocks current task execution if there is not enough resources for it.
 * Maximum task count usage controlled by maxTaskCount property.
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger logger = LogManager.getLogger(BlockingThreadPoolExecutor.class);

    private final Semaphore semaphore;
    private final String poolName;

    public BlockingThreadPoolExecutor(String poolName, int threadPoolSize, int workQueueSize) {
        super(
            threadPoolSize, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, 
            new SynchronousQueue<>(), 
            new ThreadFactoryBuilder()
                .setNameFormat("Thread-pool-" + poolName + "-%d")
                .setUncaughtExceptionHandler(new TerminateExceptionHandler()).build()
        );
        this.poolName = poolName;
        semaphore = new Semaphore(workQueueSize);
    }

    public void executeButBlockIfFull(final Runnable task) throws InterruptedException{
        semaphore.acquire();
        try {
            execute(task);
        } catch (RejectedExecutionException re) {
            logger.warn(poolName + " rejected " + this + " \n" + re.getMessage());
            throw re;
        }
    }

    public boolean executeButRejectIfFull(final Runnable task) {
        boolean acquired = semaphore.tryAcquire();
        if(!acquired) {
            return false;
        }
        try {
            execute(task);
        } catch (RejectedExecutionException re) {
            logger.warn(poolName + " rejected " + this + " \n" + re.getMessage());
            throw re;
        }
        return true;
    }

    public boolean executeButBlockWithTimeoutIfFull(final Runnable task, long timeout, TimeUnit unit) 
        throws InterruptedException {
        boolean acquired = semaphore.tryAcquire(timeout, unit);
        if(!acquired) {
            return false;
        }
        try {
            execute(task);
        } catch (RejectedExecutionException re) {
            logger.warn(poolName + " rejected " + this + " \n" + re.getMessage());
            throw re;
        }
        return true;
    }

    /**Submits task to execution pool, but blocks while number of running threads 
     * has reached the bound limit
     */
    public <T> Future<T> submitButBlockIfFull(final Callable<T> task) throws InterruptedException{
        semaphore.acquire();
        return submit(task);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        semaphore.release();
    }

    static class TerminateExceptionHandler implements Thread.UncaughtExceptionHandler {

        /***
         * Handle thread termination behaviour which could be plain reporting or also may be respawning the thread
         * back again based on usecase
         * @param thread Handle to the thread
         * @param te Uncaught exception
         */
        @Override
        public void uncaughtException(Thread thread, Throwable te) {
            logger.warn("Following error encountered while computing ", te);
        }
    }

    public String toString() {
        return "sem queue length " + semaphore.getQueueLength() + " " +
            " sem per avail " + semaphore.availablePermits() + " " +
            " que psize " + getPoolSize() + " " +
            " que size " + getQueue().size() + " " +
            " que cap " + getQueue().remainingCapacity();
    }

    public ThreadPoolStats getStats() {
        return new ThreadPoolStats(
            semaphore.getQueueLength(), semaphore.availablePermits(),
            getPoolSize(), getQueue().size(), getQueue().remainingCapacity()
        );
    }
}