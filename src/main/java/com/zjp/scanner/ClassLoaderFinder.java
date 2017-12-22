package com.zjp.scanner;

import com.zjp.FastClassPathScanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * This file is part of FastClasspathScanner.
 *
 * Author: Luke Hutchison
 *
 * Hosted at: https://github.com/lukehutch/fast-classpath-scanner
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Luke Hutchison
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class ClassLoaderFinder {

    public static ClassLoader[] findEvnClassLoader() {
        Set<ClassLoader> uniqueClassLoaders = new HashSet<>();
        List<ClassLoader> orderOfClassLoaders = new ArrayList<>();

        final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if(systemClassLoader != null) {
            if(uniqueClassLoaders.add(systemClassLoader)) {
                orderOfClassLoaders.add(systemClassLoader);
            }
        }

        //get callerClassLoader
        if(CALLER_RESOLVER != null) {
            final Class<?>[] callStack = CALLER_RESOLVER.getClassContext();
            final String fcsPkgPrefix = FastClassPathScanner.class.getPackage().getName() + ".";
            int fcsIdx;
            for(fcsIdx = callStack.length - 1; fcsIdx >= 0; --fcsIdx) {
                if(callStack[fcsIdx].getName().startsWith(fcsPkgPrefix)) {break;}
            }

            final ClassLoader callStackClassLoader = callStack[fcsIdx + 1].getClassLoader();
            if(callStackClassLoader != null) {
                if(uniqueClassLoaders.add(callStackClassLoader)) {
                    orderOfClassLoaders.add(callStackClassLoader);
                }
            }
        }

        //Get context classloader
        final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        if(threadClassLoader != null) {
            if(uniqueClassLoaders.add(threadClassLoader)) {
                orderOfClassLoaders.add(threadClassLoader);
            }
        }

        final Set<ClassLoader> ancestralClassLoaders = new HashSet<>(uniqueClassLoaders.size());
        for(ClassLoader classLoader : uniqueClassLoaders) {
            for(ClassLoader cl = classLoader.getParent(); cl != null; cl = cl.getParent()) {
                ancestralClassLoaders.add(cl);
            }
        }

        uniqueClassLoaders.removeAll(ancestralClassLoaders);
        return uniqueClassLoaders.toArray(new ClassLoader[uniqueClassLoaders.size()]);
    }

    private static CallerResolver CALLER_RESOLVER = new CallerResolver();

    private static final class CallerResolver extends SecurityManager {
        @Override protected Class<?>[] getClassContext() { return super.getClassContext(); }
    }
}
