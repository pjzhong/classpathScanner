package com.zjp.scanner;

import com.zjp.beans.ClassInfoBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/** A classpath element (a directory or jarfile on the classpath).
 * leave nestedJar alone first
 *
 * The type Of file, can be File or ZipEntry Object
 *
 * Iterator can iterate through the input of file found in this ClasspathElement
 *  */
public abstract class ClasspathElement<F>  implements AutoCloseable, Iterable<InputStream> {

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

    /**
     * iterate through all the inputStreams(open from the file founded in this ClasspathElement)
     * @return an Iterator.
     */
    public abstract Iterator<InputStream> iterator();

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
