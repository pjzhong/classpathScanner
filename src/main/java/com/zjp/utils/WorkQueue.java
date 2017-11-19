package com.zjp.utils;

import com.zjp.scanner.InterruptionChecker;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 10/29/2017.
 *
 * producer - consumer pattern ?
 */
public class WorkQueue<T> implements AutoCloseable {

    /**
     * why using double check here, I am a bit of confusing
     * */
    public void runWorkLoop() throws InterruptedException, ExecutionException {
        while (numWorkUnitsRemaining.get() > 0) {
            T workUnit = null;
            while(numWorkUnitsRemaining.get() > 0) {
                interruptionChecker.check();
                workUnit = workQueue.poll();
                if(workUnit != null) {
                   break;
                }
            }
            if(workUnit == null) { return; } // No work units remaining;
            try {
                workUnitProcessor.processWorkUnit(workUnit);
            } catch (InterruptedException e) {
                interruptionChecker.interrupt();
                throw e;
            } catch (Exception e) {
                //todo say something about what is going on this thread , maybe ?
                throw interruptionChecker.executionException(e);
            } finally {
                numWorkUnitsRemaining.decrementAndGet();
            }
        }
    }

    public void startWorker(final ExecutorService executorService, int numWorkers) {
        for(int i = 0; i < numWorkers; i++) {
            workerFutures.add(executorService.submit( () -> {
                try {
                    runWorkLoop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }

    @Override
    public void close() throws ExecutionException {
        final boolean uncompletedWork = (numWorkUnitsRemaining.get() > 0);
        for(Future<?> future; (future = workerFutures.poll()) != null;) {
            try {
                if(uncompletedWork) {
                    future.cancel(true);
                }
                future.get();
            } catch ( CancellationException | InterruptedException e) {
                // Ignore
            } catch (final ExecutionException e) {
                //todo log this exception
                interruptionChecker.executionException(e);
            }
        }
        if (uncompletedWork) {
            throw new RuntimeException("Called close() before completing all work units");
        }
    }

    /** Add a unit of work. May be called by workers to add more work units to the tail of the queue. */
    private void addWorkUnit(final T workUnit) {
        numWorkUnitsRemaining.incrementAndGet();
        workQueue.add(workUnit);
    }

    /** Add multiple units of work. May be called by workers to add more work units to the tail of the queue. */
    public void addWorkUnits(final Collection<T> workUnits) {
        workUnits.forEach(u -> addWorkUnit(u));
    }

    /** A parallel work queue. */
    public WorkQueue(final Collection<T> initialWorkUnits, final WorkUnitProcessor<T> workUnitProcessor,
                     final InterruptionChecker interruptionChecker) {
        this(workUnitProcessor, interruptionChecker);
        addWorkUnits(initialWorkUnits);
    }

    /** A parallel work queue. */
    private WorkQueue(final WorkUnitProcessor<T> workUnitProcesor, final InterruptionChecker interruptionChecker) {
        this.workUnitProcessor = workUnitProcesor;
        this.interruptionChecker = interruptionChecker;
    }

    /**
     * The work Unit processor.
     * */
    private final WorkUnitProcessor<T> workUnitProcessor;

    private final ConcurrentLinkedQueue<T> workQueue = new ConcurrentLinkedQueue<>();

    /**
     * The number of work units remaining. This will always be at least workQueue.size(), but will be higher if work
     * units have been removed from the queue and are currently being processed. Holding this high while work is
     * being done allows us to use this count to safely detect when all work has been completed. This is needed
     * because work units can add new work units to the work queue.
     */
    private final AtomicInteger numWorkUnitsRemaining = new AtomicInteger();

    /** The Future object added for each worker, used to detect worker completion. */
    private final ConcurrentLinkedQueue<Future<?>> workerFutures = new ConcurrentLinkedQueue<>();

    /**
     * The shared InterruptionChecker, used to detect thread interruption and execution exceptions, and to shut down
     * all threads if either of these occurs.
     */
    private final InterruptionChecker interruptionChecker;

    @FunctionalInterface
    public interface WorkUnitProcessor<T>  {
        void processWorkUnit(T workUnit) throws Exception;
    }
}
