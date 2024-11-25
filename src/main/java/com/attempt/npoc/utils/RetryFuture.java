package com.attempt.npoc.utils;

import org.apache.log4j.Logger;

import java.util.concurrent.*;

public class RetryFuture<T> implements Future<T> {
    private Callable<T> task;
    private ExecutorService executor;
    private int maxRetries;
    private int retryCount;
    
    static final Logger logger = Logger.getLogger(RetryFuture.class);
    public RetryFuture(Callable<T> task, ExecutorService executor, int maxRetries) {
        this.task = task;
        this.executor = executor;
        this.maxRetries = maxRetries;
        this.retryCount = 0;
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // 实现取消任务的逻辑
        return false;
    }

    @Override
    public boolean isCancelled() {
        // 判断任务是否被取消
        return false;
    }

    @Override
    public boolean isDone() {
        // 判断任务是否完成
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        boolean stopping = false;
        // 重试逻辑
        while (retryCount < maxRetries) {
            try {
                return executor.submit(task).get();
            }catch (InterruptedException | RejectedExecutionException e ){
                logger.info("Interrupted tasks: "+ e);
                stopping=true;
                break;
            }
            catch (Exception e) {
                retryCount++;
                logger.error("Task failed:"+e+" , retrying..."+retryCount);
                Thread.sleep(1000);
            }
        }
        if (stopping){
            throw new InterruptedException("Task was interrupted");
        }else{
            throw new ExecutionException(new RuntimeException("Task failed after " + maxRetries + " retries"));
        }
        
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // 重试超时的逻辑
        return null;
    }
}