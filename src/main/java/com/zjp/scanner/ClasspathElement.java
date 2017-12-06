package com.zjp.scanner;

import com.zjp.beans.ClassInfoBuilder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;


/** A classpath element (a directory or jarfile on the classpath).
 * leave nestedJar alone first
 *
 * The type Of file, can be File or ZipEntry Object
 *  */
public abstract class ClasspathElement<F>  implements AutoCloseable {

    public void parseClassFiles(ClassFileBinaryParser parser,
                         final ConcurrentLinkedQueue<ClassInfoBuilder> builders) {
        long parseStart = System.nanoTime();
        for(F fileResource : classFilesMap.values()) {
            try {
                doParseClassFile(fileResource, parser, scanSpecification, builders);
                interruptionChecker.check();
            } catch (Exception e) {
                System.out.println("something wrong while parsing:" + fileResource + "\n" + e.getClass());
            }
        }
        StringBuilder builder = new StringBuilder()
                .append(classRelativePath).append(" ")
                .append(Thread.currentThread())
                .append(" parse cost:")
                .append((System.nanoTime() - parseStart));
        System.out.println(builder);
        close();
    }

    /**
     * remove file encountered file from classFilesMap
     * @param encounteredRelativePath the files has encountered in the run-time context
     * */
    public void maskFiles(Set<String> encounteredRelativePath) {
        final Set<String> maskedRelativePaths = new HashSet<>();
        classFilesMap.forEach( (relativePath, classResource) -> {
            if(encounteredRelativePath.contains(relativePath)) {
                maskedRelativePaths.add(relativePath);
            } else {
                encounteredRelativePath.add(relativePath);
            }
        });

        maskedRelativePaths.forEach(classFilesMap::remove);
    }

    protected abstract void doParseClassFile(F file, ClassFileBinaryParser parser, ScanSpecification specification,
                                    ConcurrentLinkedQueue<ClassInfoBuilder> builders) throws IOException;

    public abstract void close();

    ClasspathElement(ClassRelativePath classRelativePath, ScanSpecification spec, InterruptionChecker checker) {
        this.scanSpecification = spec;
        this.interruptionChecker = checker;
        this.classRelativePath = classRelativePath;
    }

    /** The list of whiteList classFiles found within this classpath resource, if scanFiles is true. */
    protected Map<String, F> classFilesMap = new HashMap<>();//relativePath , File
    protected  final ScanSpecification scanSpecification;

    protected boolean ioExceptionOnOpen;
    protected InterruptionChecker interruptionChecker;
    protected ClassRelativePath classRelativePath;

    @Override
    public String toString() {
        return classRelativePath.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClasspathElement<?> that = (ClasspathElement<?>) o;

        return classRelativePath != null ? classRelativePath.equals(that.classRelativePath) : that.classRelativePath == null;

    }

    @Override
    public int hashCode() {
        return classRelativePath != null ? classRelativePath.hashCode() : 0;
    }
}
