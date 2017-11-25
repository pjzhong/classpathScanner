package com.zjp.beans;

import com.zjp.scanner.ClassFileBinaryParser;
import com.zjp.scanner.ClassRelativePath;
import com.zjp.scanner.InterruptionChecker;
import com.zjp.scanner.ScanSpecification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        for(ClassResource<F> fileResource : classFileMatches) {
            try {
                doParseClassFile(fileResource.getClassFile(), parser, scanSpecification, internMap, builders);
                interruptionChecker.check();
            } catch (Exception e) {
                System.out.println("something wrong when parsing" + fileResource);
                throw new RuntimeException(e);
            }
        }
    }

    public void maskFiles(Set<String> encounteredRelativePath) {
        final Set<String> maskedRelativePaths = new HashSet<>();
        classFileMatches.forEach( classResource -> {
            if(encounteredRelativePath.contains(classResource.getRelativePath())) {
                maskedRelativePaths.add(classResource.getRelativePath());
            } else {
                encounteredRelativePath.add(classResource.getRelativePath());
            }
        });

        List<ClassResource<F>> filteredClassMatches = new ArrayList<>();
        classFileMatches.forEach( c -> {
            if(!maskedRelativePaths.contains(c.getRelativePath())) {
                filteredClassMatches.add(c);
            }
        });

        classFileMatches = filteredClassMatches;
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

    static class ClassResource<F> {
        private final F classFile;
        private final String relativePath;

        public ClassResource(F classFile, String relativePath) {
            this.classFile = classFile;
            this.relativePath = relativePath;
        }

        public F getClassFile() {
            return classFile;
        }

        public String getRelativePath() {
            return relativePath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClassResource<?> that = (ClassResource<?>) o;

            return relativePath != null ? relativePath.equals(that.relativePath) : that.relativePath == null;

        }

        @Override
        public int hashCode() {
            return relativePath != null ? relativePath.hashCode() : 0;
        }
    }

    /** The list of whiteList classFiles found within this classpath resource, if scanFiles is true. */
    protected List<ClassResource<F>> classFileMatches = new ArrayList<>();
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
