package com.zjp.scanner;

import com.zjp.classloaderhandler.ClassLoaderHandler;
import com.zjp.classloaderhandler.URLClassLoaderHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/10/31.
 * This is class is a toy, refactor it when you know more about classLoaders
 */
public class ClasspathFinder {

     ClasspathFinder() {
        //for convenient, only handler sun.misc.Launcher$AppClassLoader
        List<ClassLoaderHandler> classLoaderHandlers = Arrays.asList(new URLClassLoaderHandler());

        for(ClassLoader loader : findAllClassLoaders()) {
            for(ClassLoaderHandler handler : classLoaderHandlers) {
                try {
                    handler.handle(loader, this);
                } catch (Exception e) {
                    //todo say something about what happened;
                }
            }
        }
    }

    List<String> getRawClassPathStrings() {
        return rawClassPathStrings;
    }

    /**
     * Add a classpath element relative to a base file. May be called by a ClassLoaderHandler to add classpath
     * elements that it knows about.
     *
     * @return true (and add the classpath element) if pathElement is not null or empty, otherwise return false.
     */
    public boolean addClasspathElement(final String pathElement) {
        if (pathElement != null && !pathElement.isEmpty()) {
            rawClassPathStrings.add(pathElement);
            return true;
        }
        return false;
    }

    /**
     * Add classpath elements, separated by the system path separator character. May be called by a
     * ClassLoaderHandler to add a path string that it knows about.
     *
     * @return true (and add the classpath element) if pathElement is not null or empty, otherwise return false.
     */
    public boolean addClasspathElements(final String pathStr) {
        if (pathStr != null && !pathStr.isEmpty()) {
            for (final String pathElement : pathStr.split(File.pathSeparator)) {
                addClasspathElement(pathElement);
            }
            return true;
        }
        return false;
    }

    public List<ClassLoader> findAllClassLoaders() {
        return Arrays.asList(ClassLoader.getSystemClassLoader());
    }

    private final List<String> rawClassPathStrings = new ArrayList<>();
}
