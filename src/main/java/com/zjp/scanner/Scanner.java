package com.zjp.scanner;

import com.zjp.beans.ClassGraph;
import com.zjp.beans.ClassInfoBuilder;
import com.zjp.utils.WorkQueue;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 10/28/2017.
 */
public class Scanner implements Callable<ClassGraph>{

    private final ExecutorService executorService;
    private final FailureHandler failureHandler;
    private final ScanSpecification specification;
    private final int workers;
    private final InterruptionChecker interruptionChecker = new InterruptionChecker();

    public Scanner(ExecutorService executorService, int numberThread, ScanSpecification specification, FailureHandler failureHandler) {
        this.workers = numberThread;
        this.specification = specification;
        this.executorService = executorService;
        this.failureHandler = failureHandler;
    }

    @Override
    public ClassGraph call() throws Exception {
        /**
         * Get all classpathElements from the runtime-context, have no idea what these is ?
         * Write a class, and run the code below:
         *      System.getProperty("java.class.path");
         * */
        final List<String> classPathElementStrings = new ClasspathFinder().getRawClassPathStrings();
        final List<ClassRelativePath> rawClassPathElements = new ArrayList<>();
        for(String classElementStr : classPathElementStrings) {
            rawClassPathElements.add(new ClassRelativePath(classElementStr));
        }

        /**
         * split dir and jar file than started to scan them
         * */
        final ClassRelativePathToElementMap elementMap = new ClassRelativePathToElementMap(specification, interruptionChecker);
        WorkQueue<ClassRelativePath> relativePathQueue = null;
        try {
            relativePathQueue = new WorkQueue<>(rawClassPathElements, c -> {
                if(elementMap.get(c) != null) {
                    //duplication
                } else if(c.isValidClasspathElement(specification)) {
                    try {
                        elementMap.createSingleton(c);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }, interruptionChecker);
            relativePathQueue.startWorker(executorService, workers -1 /* in case there only one thread*/);
            relativePathQueue.runWorkLoop();
        } finally {
            if(relativePathQueue != null) {
                relativePathQueue.close();
            }
        }


        /**
         * restore the classpathOrder and filtered the same classes but occurs in difference jar file
         * (remove the second and subsequent)
         * */
        List<ClasspathElement<?>> classpathOrder = restoredClasspathOrder(rawClassPathElements, elementMap);
        Set<String> encounteredClassFile = new HashSet<>();
        for(ClasspathElement element : classpathOrder) {
            element.maskFiles(encounteredClassFile);
        }

        /**
         * start to parse the class files found in the runtime context
         * */
        final ConcurrentLinkedQueue<ClassInfoBuilder> infoBuilders = new ConcurrentLinkedQueue<>();
        ClassFileBinaryParser parser = new ClassFileBinaryParser();
        WorkQueue<ClasspathElement<?>> workQueue = null;
        try {
            workQueue = new WorkQueue<>(classpathOrder, e -> e.parseClassFiles(parser, infoBuilders), interruptionChecker);
            workQueue.startWorker(executorService, workers - 1 /* in case there only one thread*/);
            workQueue.runWorkLoop();
        } finally {
            if(workQueue != null) {
                workQueue.close();
            }
        }



        /**
         * build the classGraph in single-thread
         * */
        long buildStart = System.nanoTime();
        ClassGraph classGraph = ClassGraph.builder(specification, infoBuilders).build();
        System.out.println("buildGraph cost:" + (System.nanoTime() - buildStart));
        return classGraph;
    }

    /**
     * restore the classPath after scanned;
     * */
    private List<ClasspathElement<?>> restoredClasspathOrder(List<ClassRelativePath> rawPaths,
                                                          ClassRelativePathToElementMap elementMap)
            throws InterruptedException {
        final List<ClasspathElement<?>> order = new ArrayList<>();
        for(ClassRelativePath relativePath : rawPaths) {
            ClasspathElement element = elementMap.get(relativePath);
            if(element != null) { order.add(elementMap.get(relativePath)); }
        }
        return order;
    }
}
