package com.zjp.scanner;

import com.zjp.utils.ClassScanUtils;
import com.zjp.utils.FastPathResolver;
import com.zjp.utils.ScanPathMatch;
import com.zjp.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by Administrator on 2017/11/3.
 */
public class ClassRelativePath {

    public boolean isValidClasspathElement(ScanSpecification spec) {
        final String path = getResolvedPath();
        if(StringUtils.isEmpty(path) || !exists()) { return false; }

        try {
            if(isFile()) {
                if(!ClassScanUtils.isJar(getCanonicalPath())) {
                    return false;//ignore non-jar file on class path
                }
                ScanPathMatch matchStatus = spec.blockJdk(path);
                switch (matchStatus) {
                    case WITHIN_BLACK_LISTED_PATH:return false;
                }
            }
        } catch (IOException e) {
            //todo log this
            return false;
        }

        return true;
    }

    /** Returns true if path has a .class extension, ignoring case. */
    public static boolean isClassFile(final String path) {
        final int len = path.length();
        return len > 6 && path.regionMatches(true, len - 6, ".class", 0, 6);
    }

    public boolean isClassFile() {
        return isClassFile(getResolvedPath());
    }

    public boolean isJar() {
        return isJar;
    }

    public boolean exists() {
        try {
            return asFile().exists();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isDirectory() {
        try {
            return asFile().isDirectory();
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isFile() {
        try {
            return asFile().isFile();
        } catch (IOException e) {
            return false;
        }
    }

    public String getCanonicalPath() throws IOException{
        if(StringUtils.isEmpty(canonicalPath)) {
            canonicalPath = asFile().getPath();
        }
        return canonicalPath;
    }

    public String getResolvedPath() {
        if(StringUtils.isEmpty(resolvedPath)) {
            resolvedPath = FastPathResolver.resolve(basePath, relativePath);
        }
        return resolvedPath;
    }

    public File asFile() throws IOException {
        if(file == null) {
            final String path = getResolvedPath();
            if (path == null) {
                throw new IOException(
                        "Path " + relativePath + " could not be resolved relative to " + basePath);
            }

            file = new File(path);
            try {
                file = file.getCanonicalFile();
            } catch (SecurityException e) {
                throw new IOException(e);
            }
        }
        return file;
    }

    public ClassRelativePath(String basePath, String relativePath) {
        this.basePath = basePath;
        this.relativePath = relativePath;
        this.isJar = this.relativePath.contains("!") || ClassScanUtils.isJar(this.relativePath);
    }

    @Override
    public String toString() {
        try {
            return getCanonicalPath();
        } catch (IOException e) {
            return getResolvedPath();
        }
    }

   @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassRelativePath that = (ClassRelativePath) o;
        try {
            return Objects.equals(getCanonicalPath(), that.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return Objects.hash(getCanonicalPath());
        } catch (IOException e) {
            return 0;
        }
    }

    private final String basePath;
    private final String relativePath;
    private final boolean isJar;

    private String resolvedPath;
    /** The canonical file for the relative path. */
    private File file;
    private String canonicalPath;
}
