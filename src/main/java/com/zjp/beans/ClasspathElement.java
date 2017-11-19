package com.zjp.beans;

import com.zjp.scanner.ClassFileBinaryParser;
import com.zjp.scanner.ClassRelativePath;
import com.zjp.scanner.InterruptionChecker;
import com.zjp.scanner.ScanSpecification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
                         final ConcurrentLinkedQueue<ClassInfoBuilder> unLinkedInfos) {
        for(F file : classFileMatches) {
            try {
                doParseClassFile(file, parser, scanSpecification, internMap, unLinkedInfos);
                interruptionChecker.check();
            } catch (Exception e) {
                System.out.println("something wrong when parseing" + file);
                throw new RuntimeException(e);
            }
        }
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
    protected List<F> classFileMatches = new ArrayList<>();
    protected  final ScanSpecification scanSpecification;

    protected boolean ioExceptionOnOpen;
    protected InterruptionChecker interruptionChecker;
    protected ClassRelativePath classRelativePath;

    @Override
    public String toString() {
        return classRelativePath.toString();
    }
}
