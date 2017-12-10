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
    public void runWorkers() throws InterruptedException, ExecutionException {

        while (true) {
            T workUnit = null;
            while(producers.get() > 0 || !workQueue.isEmpty()) {
                interruptionChecker.check();
                workUnit = workQueue.poll();
                if(workUnit != null) { break;}
            }

            if(workUnit == null) { return; }
            try {
                workUnitProcessor.processWorkUnit(workUnit);
            } catch (Exception e) {
                throw interruptionChecker.executionException(e);
            }
        }
    }

    public void runProducer()  throws Exception {
        try {
            workUnitProducer.produceWorkUnit(this);
        } finally {
            producers.decrementAndGet();
        }
    }

    public void start(final ExecutorService executorService, int numWorkers) {
        if(workUnitProducer != null) {
            producers.incrementAndGet();
            workerFutures.add(executorService.submit( () -> {
                try {
                    runProducer();
                } catch (Exception e) {
                    interruptionChecker.executionException(e);
                }
            }));
        }

        for(int i = 1; i < numWorkers; i++) {
            workerFutures.add(executorService.submit( () -> {
                try {
                    runWorkers();
                } catch (Exception e) {
                    interruptionChecker.executionException(e);
                }
            }));
        }
    }

    @Override
    public void close() throws ExecutionException {
        final boolean uncompletedWork = (numWorkUnitsRemaining.get() > 0);
        for(Future<?> future; (future = workerFutures.poll()) != null;) {
            try {
                future.cancel(true);
            } catch (final Exception e) {
                //todo log this exception
                interruptionChecker.executionException(e);
            }
        }
        if (uncompletedWork) {
            throw new RuntimeException("Called close() before completing all work units");
        }
    }

    /** Add a unit of work. May be called by workers to add more work units to the tail of the queue. */
    public void addWorkUnit(final T workUnit) {
        /*numWorkUnitsRemaining.incrementAndGet();*/
        workQueue.add(workUnit);
    }

    /** Add multiple units of work. May be called by workers to add more work units to the tail of the queue. */
    public void addWorkUnits(final Collection<T> workUnits) {
        workUnits.forEach(this::addWorkUnit);
    }

    /** A parallel work queue. */
    public WorkQueue(final Collection<T> initialWorkUnits, final WorkUnitProcessor<T> workUnitProcessor,
                     final InterruptionChecker interruptionChecker) {
        this(workUnitProcessor, interruptionChecker);
        addWorkUnits(initialWorkUnits);
    }

    /** A parallel work queue. */
    public WorkQueue(WorkUnitProducer<T> workUnitProducer, WorkUnitProcessor<T> workUnitProcessor,
                     final InterruptionChecker interruptionChecker) {
        this(workUnitProcessor, interruptionChecker);
        this.workUnitProducer = workUnitProducer;
    }

    /** A parallel work queue. */
    private WorkQueue(final WorkUnitProcessor<T> workUnitProcessor, final InterruptionChecker interruptionChecker) {
        this.workUnitProcessor = workUnitProcessor;
        this.interruptionChecker = interruptionChecker;
    }

    /**
     * The work Unit processor.
     * */
    private WorkUnitProcessor<T> workUnitProcessor;
    private WorkUnitProducer<T> workUnitProducer;

    private final ConcurrentLinkedQueue<T> workQueue = new ConcurrentLinkedQueue<>();

    /**
     * The number of work units remaining. This will always be at least workQueue.size(), but will be higher if work
     * units have been removed from the queue and are currently being processed. Holding this high while work is
     * being done allows us to use this count to safely detect when all work has been completed. This is needed
     * because work units can add new work units to the work queue.
     */
    public final AtomicInteger numWorkUnitsRemaining = new AtomicInteger();
    private final AtomicInteger producers = new AtomicInteger(0);

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

    @FunctionalInterface
    public interface WorkUnitProducer<T> {
        void produceWorkUnit(WorkQueue<T> queue) throws Exception;
    }
}
