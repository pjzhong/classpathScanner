package com.zjp.scanner;

import com.zjp.beans.ClassPathElementDir;
import com.zjp.beans.ClasspathElement;
import com.zjp.beans.ClasspathElementZip;
import com.zjp.utils.SingleTonMap;

/**
 * Created by Administrator on 11/5/2017.
 * Just my personal opinion, this class has some overDesign, it make the reader confuse what this class for in the
 * first time read this code , like me.
 * todo find a better way to replace this class
 */
class ClassRelativePathToElementMap extends SingleTonMap<ClassRelativePath, ClasspathElement<?>>  implements AutoCloseable {

    @Override
    protected ClasspathElement newInstance(ClassRelativePath relativePath) {
        if(relativePath.isDirectory()) {
            return new ClassPathElementDir(relativePath, spec, interruptionChecker);
        } else {
            return  new ClasspathElementZip(relativePath, spec, interruptionChecker);
        }

    }

    public void close() throws Exception {
        for (final ClasspathElement classpathElt : values()) {
            classpathElt.close();
        }
    }

    ClassRelativePathToElementMap(ScanSpecification spec, InterruptionChecker checker) {
        this.spec = spec;
        this.interruptionChecker = checker;
    }

    private final ScanSpecification spec;
    private final InterruptionChecker interruptionChecker;
}
