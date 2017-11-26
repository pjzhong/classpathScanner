package com.zjp.beans;

import com.zjp.scanner.ClassFileBinaryParser;
import com.zjp.scanner.ClassRelativePath;
import com.zjp.scanner.InterruptionChecker;
import com.zjp.scanner.ScanSpecification;

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
                         final ConcurrentMap<String, String> internMap,
                         final ConcurrentLinkedQueue<ClassInfoBuilder> builders) {
        for(F fileResource : classFilesMap.values()) {
            try {
                doParseClassFile(fileResource, parser, scanSpecification, internMap, builders);
                interruptionChecker.check();
            } catch (Exception e) {
                System.out.println("something wrong when parsing:" + fileResource + ", exception:" + e);
                throw new RuntimeException(e);
            }
        }
    }

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
                                    ConcurrentMap<String, String> internMap,
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
