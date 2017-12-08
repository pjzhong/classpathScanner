package com.zjp.scanner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 10/28/2017.
 */
public class InterruptionChecker {
    private final AtomicBoolean interrupted = new AtomicBoolean(false);
    private ExecutionException executionException;

    public void interrupt() {
        interrupted.set(true);
    }

    public boolean checkAndReturn() {
        if(Thread.currentThread().isInterrupted()) {
            interrupt();
        }
        return interrupted.get() || executionException != null;
    }

    public void check() throws InterruptedException, ExecutionException {
        if(checkAndReturn()) {
            if(executionException != null) {
                throw executionException;
            } else {
                throw new InterruptedException();
            }
        }
    }

    /**
     * Stop all threads that share this InterruptionChecker due to an exception being thrown in one of them.
     * */
    public ExecutionException executionException(final Exception e) {
        final ExecutionException newExecutionException = e instanceof ExecutionException ? (ExecutionException) e
                : new ExecutionException(e);
        executionException = newExecutionException;
        return executionException;
    }

    public ExecutionException getExecutionException() {
        return executionException;
    }
}
