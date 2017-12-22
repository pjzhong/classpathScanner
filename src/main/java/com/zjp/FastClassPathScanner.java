package com.zjp;

import com.zjp.beans.ClassGraph;
import com.zjp.beans.ClassInfo;
import com.zjp.matchprocessor.*;
import com.zjp.scanner.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FastClassPathScanner {

    public void scan() {
        scan(Runtime.getRuntime().availableProcessors());
    }

    public void scan (int numberThreads) {
        ExecutorService executorService = null;
        try {
            executorService = Executors.newFixedThreadPool(numberThreads);
            scan(executorService, numberThreads);
        } finally {
            if(executorService != null) {
                executorService.shutdown();
            }
        }
    }

    public void scan(ExecutorService executorService, int numberThreads) {
        try {
            ClassGraph graph = launchAsyncScan(executorService, specification, numberThreads, null).get();

            if(classMatchers != null) {
                for(ClassMatcher matcher : classMatchers) {
                    matcher.lookForMatches(graph);
                }
            }
        } catch (InterruptedException e) {
            //todo handle this exception
            throw new RuntimeException(e.getCause());
        } catch (ExecutionException e) {
            //todo handle this exception
            throw new RuntimeException(e.getCause());
        }

    }

    private Future<ClassGraph> launchAsyncScan(ExecutorService executorService, ScanSpecification specification,
                                               int numberThread,  FailureHandler handler) {
        return executorService.submit(new Scanner(executorService, numberThread, specification, handler));
    }

    public <T> FastClassPathScanner matchSubClassOf(final Class<T> superClass,
                                                    final SubclassMatchProcessor<T> processor) {
        addClassMatcher(g -> {
            for(ClassInfo subClassing : g.getInfoOfClassSubClassOf(superClass)) {
                try {
                    Class<? extends T> cls = (Class<? extends T>)loadClass(subClassing.getClassName());
                    processor.processMatch(subClassing, cls);
                } catch (Throwable e) {
                    //todo log this
                    System.out.println(e);
                }
            }
        });
        return this;
    }

    /**
     * Match all classes that implemented the specific interface, exclude annotation
     * */
    public <T> FastClassPathScanner matchClassesImplementing(final Class<T> targetInterface,
                                                             final ImplementClassMatchProcessor<T> processor) {
        addClassMatcher(g -> {
            for(ClassInfo classImplementing : g.getInfoOfClassImplementing(targetInterface)) {
                try {
                    Class<? extends T> cls = (Class<? extends T>)loadClass(classImplementing.getClassName());
                    processor.processMatch(classImplementing, cls);
                } catch (Throwable e) {
                    //todo log this
                    System.out.println(e);
                }
            }
        });
        return this;
    }

    public FastClassPathScanner matchClassesWithAnnotation(final Class<?> annotation,
                                           final ClassAnnotationMatchProcessor processor) {
        addClassMatcher( g -> {
            for(ClassInfo classWithAnnotation : g.getInfoOfClassesWithAnnotation(annotation)) {
                try {
                    Class<?> cls = loadClass(classWithAnnotation.getClassName());
                    processor.processMatch(classWithAnnotation, cls);
                } catch (Throwable e) {
                    //todo log this
                    System.out.println(e);
                }
            }
        });
        return this;
    }

    public FastClassPathScanner matchClassesWithMethodAnnotation(final Class<?> annotation,
                                                           final ClassAnnotationMatchProcessor processor) {
        addClassMatcher( g -> {
            for(ClassInfo classWithAnnotation : g.getInfoOfClassesWithMethodAnnotation(annotation)) {
                try {
                    Class<?> cls = loadClass(classWithAnnotation.getClassName());
                    processor.processMatch(classWithAnnotation, cls);
                } catch (Throwable e) {
                    //todo log this
                    System.out.println(e);
                }
            }
        });
        return this;
    }

    private Class<?> loadClass(final String className) {
        try {
            return Class.forName(className, false, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void addClassMatcher(ClassMatcher classMatcher) {
        classMatchers.add(classMatcher);
    }

    private List<ClassMatcher> classMatchers = new ArrayList<>();

    public FastClassPathScanner(String... whiteListed) {
        specification = new ScanSpecification(whiteListed);
    }

    private ScanSpecification specification = null;

    @FunctionalInterface
    private interface ClassMatcher {
        void lookForMatches(ClassGraph result);
    }
}
