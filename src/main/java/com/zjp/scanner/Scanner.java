package com.zjp.scanner;

import com.zjp.beans.ClassGraph;
import com.zjp.beans.ClassInfo;
import com.zjp.beans.ClassInfoBuilder;
import com.zjp.beans.ClasspathElement;
import com.zjp.utils.FastPathResolver;
import com.zjp.utils.WorkQueue;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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
         String currDirPathStr = "";
         try {
             Path currDirPath = Paths.get("").toAbsolutePath();
             currDirPath = currDirPath.normalize();
             currDirPath = currDirPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
             currDirPathStr = FastPathResolver.resolve(currDirPath.toString());
         } catch (IOException e) {
             throw new RuntimeException("Could not resolve current directory: " + currDirPathStr, e);
         }

         final List<String> classPathElementStrings = new ClasspathFinder().getRawClassPathStrings();
         final List<ClassRelativePath> rawClassPathElements = new ArrayList<>();
         for(String classElementStr : classPathElementStrings) {
             rawClassPathElements.add(new ClassRelativePath(currDirPathStr, classElementStr));
         }

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

        final ConcurrentLinkedQueue<ClassInfoBuilder> infoBuilders = new ConcurrentLinkedQueue<>();
        final ConcurrentMap<String, String> stringInternMap = new ConcurrentHashMap<>();
        ClassFileBinaryParser parser = new ClassFileBinaryParser();
        WorkQueue<ClasspathElement<?>> workQueue = null;
        try {
            workQueue = new WorkQueue<>(elementMap.values(),
                    e -> e.parseClassFiles(parser, stringInternMap, infoBuilders), interruptionChecker);
            workQueue.startWorker(executorService, workers - 1 /* in case there only one thread*/);
            workQueue.runWorkLoop();
        } finally {
            if(workQueue != null) {
                workQueue.close();
            }
        }

        Map<String, ClassInfo> infoMap = new HashMap<>();
        for(ClassInfoBuilder builder : infoBuilders) {
            builder.link(specification, infoMap);
        }

        ClassGraph classGraph = new ClassGraph(specification, infoMap);
        return classGraph;
    }
}
