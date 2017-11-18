package zjp.beans;

import com.zjp.scanner.ClassFileBinaryParser;
import com.zjp.scanner.ClassRelativePath;
import com.zjp.scanner.InterruptionChecker;
import com.zjp.scanner.ScanSpecification;
import com.zjp.utils.ScanPathMatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * FastClassPathScanner的ClassElement设计不好，竟然在父类里面提供初始化子类的方法
 * 虽然说不对外公开，但这样读起来真的很容疑惑。
 .
 */
public class ClassPathElementDir extends ClasspathElement<File> {

    public ClassPathElementDir(ClassRelativePath classRelativePath, ScanSpecification spec, InterruptionChecker checker) {
        super(classRelativePath, spec, checker);
        if(spec.isScanFiles()) {
            File dir;
            try {
                dir = classRelativePath.asFile();
            } catch (IOException e) {
                ioExceptionOnOpen = true;
                return;
            }

            classFileMatches = new ArrayList<>();
            final Set<String> scannedCanonicalPaths = new HashSet<>();
            scanDir(classRelativePath, dir, (dir.getPath().length() + 1), scannedCanonicalPaths, 0);
        }
    }

    private void scanDir(ClassRelativePath classRelativePath, File dir, final int ignorePrefixLen,
                         final Set<String> scannedCanonicalPath, int entryIdx) {
        String canonicalPath;
        try {
            canonicalPath = dir.getCanonicalPath();
            if(!scannedCanonicalPath.add(canonicalPath)) {
                System.out.format("Stop recursion at : %s", canonicalPath);
                return;
            }
        } catch (IOException | SecurityException e) {
            //todo log this
            return;
        }

        final String dirPath = dir.getPath();
        final String dirRelatePath = ignorePrefixLen > dirPath.length() ?
                "/" : dirPath.substring(ignorePrefixLen).replace(File.separatorChar, '/') + "/";
        final ScanPathMatch matchStatus = scanSpecification.pathWhiteListMatchStatus(dirRelatePath);
        switch (matchStatus) {
            case NOT_WITHIN_WHITE_LISTED_PATH:
            case WITHIN_BLACK_LISTED_PATH:return;
        }

        final File[] filesInDir = dir.listFiles();
        if(filesInDir == null) { return; }

        for(final File file : filesInDir) {
            if(entryIdx++ % 1024 == 0) {
                if(interruptionChecker.checkAndReturn()) { return; }
            }

            if(file.isDirectory()) {
                scanDir(classRelativePath, file, ignorePrefixLen,  scannedCanonicalPath, entryIdx);
            } else if(file.isFile() && matchStatus == ScanPathMatch.WITHIN_WHITE_LISTED_PATH){
                String fileRelativePath = dirRelatePath + file.getName();
                if(ClassRelativePath.isClassFile(fileRelativePath)) {
                    classFileMatches.add(file);
                }
            }
        }
    }

    protected void doParseClassFile(File file, ClassFileBinaryParser parser, ScanSpecification specification,
                                    ConcurrentMap<String, String> internMap,
                                    ConcurrentLinkedQueue<ClassInfoBuilder> unLinkInfos) throws IOException {
        if(!ioExceptionOnOpen) {
            try (InputStream stream = new FileInputStream(file)){
                ClassInfoBuilder unlinked = parser.readClassInfoFromClassFileHeader(stream, internMap);
                if(unlinked != null) { unLinkInfos.add(unlinked); }
            }
        }
    }


    //do nothing;
    @Override
    public void close() {}
}
