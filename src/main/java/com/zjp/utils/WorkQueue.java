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

    public void runWorkers() throws InterruptedException, ExecutionException {
        T workUnit;
        while (true) {
            workUnit = null;
            while(producers.get() > 0 || !workQueue.isEmpty()) {
                interruptionChecker.check();
                workUnit = workQueue.poll(500, TimeUnit.MILLISECONDS);
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
                    long start = System.nanoTime();
                    runProducer();
                    System.out.println("producer done, cost:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
                } catch (Exception e) {
                    interruptionChecker.executionException(e);
                }
            }));
        }

        for(int i = producers.get(); i < numWorkers; i++) {
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
       final boolean uncompletedWork = (!workQueue.isEmpty() || producers.get() > 0);
        for(Future<?> future; (future = workerFutures.poll()) != null;) {
            try {
                future.cancel(true);
            } catch (final Exception e) {
                //todo log this exception
                interruptionChecker.executionException(e);
            }
        }
        if (uncompletedWork) {
            throw interruptionChecker.getExecutionException();
        }
    }

    /** Add a unit of work. May be called by workers to add more work units to the tail of the queue. */
    public void addWorkUnit(final T workUnit) {
        try {
            workQueue.put(workUnit);
        } catch (InterruptedException e) {
            System.out.println("add workUnit:" + workUnit + " was interrupted");
        }

    }

    /** Add multiple units of work. May be called by workers to add more work units to the tail of the queue. */
    public void addWorkUnits(final Collection<T> workUnits) {
        workUnits.forEach(this::addWorkUnit);
    }

    /** A parallel work queue. */
    public WorkQueue(final Collection<T> initialWorkUnits, final WorkUnitProcessor<T> workUnitProcessor,
                     final InterruptionChecker interruptionChecker) {
        this.workUnitProcessor = workUnitProcessor;
        this.interruptionChecker = interruptionChecker;
        this.workQueue = new LinkedBlockingQueue<>();
        addWorkUnits(initialWorkUnits);
    }

    /** A parallel work queue. */
    public WorkQueue(WorkUnitProducer<T> workUnitProducer, WorkUnitProcessor<T> workUnitProcessor,
                     final InterruptionChecker interruptionChecker) {
        this.workUnitProcessor = workUnitProcessor;
        this.interruptionChecker = interruptionChecker;
        this.workUnitProducer = workUnitProducer;
        this.workQueue = new LinkedBlockingQueue<>(workUnitProducer != null ? (Runtime.getRuntime().availableProcessors() * 50) : Integer.MAX_VALUE);
    }


    /**
     * The work Unit processor.
     * */
    private WorkUnitProcessor<T> workUnitProcessor;
    private WorkUnitProducer<T> workUnitProducer;

    private final BlockingQueue<T> workQueue;
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
