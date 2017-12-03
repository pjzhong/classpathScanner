package com.zjp.scanner;

import com.zjp.utils.FastPathResolver;
import com.zjp.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 2017/10/31.
 */
public class ScanSpecification {

    public ScanPathMatch pathWhiteListMatchStatus(final String relatePath) {
        for(final String blackListed : blackListPathPrefixes) {
            if(relatePath.startsWith(blackListed)) {
                return ScanPathMatch.WITHIN_BLACK_LISTED_PATH;
            }
        }

        for (final String whiteList : whiteListPathPrefixes) {
            if(relatePath.equals(whiteList) || relatePath.startsWith(whiteList)) {
                return ScanPathMatch.WITHIN_WHITE_LISTED_PATH;
            } else if(whiteList.startsWith(relatePath) || "/".equals(relatePath)){
                return ScanPathMatch.ANCESTOR_OF_WHITE_LISTED_PATH;
            }
        }

        return ScanPathMatch.NOT_WITHIN_WHITE_LISTED_PATH;
    }

    public ScanPathMatch blockJdk(final String relativePath) {
        for(String jrePath : jrePaths) {
            if(relativePath.startsWith(jrePath)) {
                return ScanPathMatch.WITHIN_BLACK_LISTED_PATH;
            }
        }

        return ScanPathMatch.NOT_WITHIN_WHITE_LISTED_PATH;
    }

    public ScanSpecification(final String... specifications) {
        final Set<String> uniqueWhiteListPathPrefixes = new HashSet<>();
        final Set<String> uniqueBlackListedPathPrefixes = new HashSet<>();
        for(final String specification : specifications) {
            String spec = specification;
            final boolean blankListed = spec.startsWith("-");
            if(blankListed) { spec = spec.substring(1); }

            String specPath = spec.replace('.', '/');
            if(!specPath.equals("/")) { specPath += "/"; }// '/' mean scan all
            if(blankListed) {
                uniqueBlackListedPathPrefixes.add(specPath);
            } else {
                uniqueWhiteListPathPrefixes.add(specPath);
            }
        }

        //process blackListed - always block SystemPackage 2017-11-6
        if(blackSystemPackages) {
            //java package prefix
            uniqueBlackListedPathPrefixes.add("java/");
            uniqueBlackListedPathPrefixes.add("javax/");
            uniqueBlackListedPathPrefixes.add("sun/");
        }
        blackListPathPrefixes.addAll(uniqueBlackListedPathPrefixes);
        blackListPathPrefixes.forEach( s -> blackListPackagePrefixes.add(s.replace('/', '.')) );

        //block jrePath
        jrePaths = getJrePaths();

        //process whiteListed
        uniqueWhiteListPathPrefixes.removeAll(uniqueBlackListedPathPrefixes);
        whiteListPathPrefixes.addAll(uniqueWhiteListPathPrefixes);
    }

    private Set<String> getJrePaths() {
        Set<File> jrePathFile = new HashSet<>();
        String javaHome = System.getProperty("java.home");
        if(StringUtils.notEmpty(javaHome)) {
            File javaHomeFile = new File(javaHome);
            jrePathFile.add(new File(javaHome, "lib"));
            jrePathFile.add(new File(javaHome, "ext"));
            if (javaHomeFile.getName().equals("jre")) {  // Handle jre/../lib/tools.jar
                final File parent = javaHomeFile.getParentFile();
                if (parent != null) {
                    jrePathFile.add(new File(parent, "lib"));
                }
            }
        }
        String javaExtDirs = System.getProperty("java.ext.dirs");
        if(StringUtils.notEmpty(javaExtDirs)) {
            for(String javaExtDir : javaExtDirs.split(File.pathSeparator)) {
                if(StringUtils.notEmpty(javaExtDir)) {
                    jrePathFile.add(new File(javaExtDir));
                }
            }
        }

        Set<String> jrePathStr = new HashSet<>();
        for(File file : jrePathFile) {
            if(file.canRead() && file.isDirectory()) {
                String path = file.getPath();
                if(!path.endsWith(File.separator)) { path += File.separator; }

                final String jrePath = FastPathResolver.resolve("", path);
                if(StringUtils.notEmpty(jrePath)) { jrePathStr.add(jrePath); }
                try {
                    String canonicalPath = file.getCanonicalPath();
                    if(!canonicalPath.endsWith(File.separator)) {
                        canonicalPath += File.separator;
                    }
                    String jreCanonicalPath = FastPathResolver.resolve("", canonicalPath);
                    if(!jreCanonicalPath.equals(canonicalPath) && StringUtils.notEmpty(jreCanonicalPath)) {
                        jrePathStr.add(jreCanonicalPath);
                    }
                } catch (IOException | SecurityException e) {
                    //do nothing == ignore;
                }
            }
        }

        return jrePathStr;
    }

    public boolean isScanFiles() {
        return scanFiles;
    }

    private List<String> whiteListPathPrefixes = new ArrayList<>();
    private List<String> blackListPathPrefixes = new ArrayList<>();
    private List<String> blackListPackagePrefixes = new ArrayList<>();
    private Set<String> jrePaths;

    private boolean scanFiles = true;
    private boolean blackSystemPackages = true;
    private boolean blankSystemJars = true;
}
