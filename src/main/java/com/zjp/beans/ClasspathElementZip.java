package com.zjp.beans;

import com.zjp.scanner.ClassFileBinaryParser;
import com.zjp.scanner.ClassRelativePath;
import com.zjp.scanner.InterruptionChecker;
import com.zjp.scanner.ScanSpecification;
import com.zjp.utils.ScanPathMatch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Administrator on 11/5/2017.
 */
public class ClasspathElementZip extends ClasspathElement<ZipEntry> {

    public ClasspathElementZip(ClassRelativePath classRelativePath, ScanSpecification spec,
                        InterruptionChecker checker) {
        super(classRelativePath, spec, checker);
        final File classpathFile;
        try {
            classpathFile = classRelativePath.asFile();
        } catch (IOException e) {
            ioExceptionOnOpen = true;
            return;
        }

        if(classpathFile == null || !classpathFile.canRead()) {
            ioExceptionOnOpen = true;
            return;
        }

        try {
            zipFile = new ZipFile(classpathFile);
            classFileMatches = new ArrayList<>(zipFile.size());
             scanZipFile(classRelativePath, zipFile);
        } catch (IOException e) {
            ioExceptionOnOpen = true;
            return;
        }
    }

    private void scanZipFile(ClassRelativePath classRelativePath, ZipFile zipFile) {
        String prevParentRelativePath = null;
        ScanPathMatch prevMatchStatus = null;
        int entryIdx = 0;
        for(Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
            if( (entryIdx % 1024) ==0 && interruptionChecker.checkAndReturn()) {
                return;
            }

            final ZipEntry zipEntry = entries.nextElement();
            if(zipEntry.isDirectory()) { continue; } // Ignore directory entries, they are not used

            String relativePath = zipEntry.getName();
            final int lastSlashIdx = relativePath.lastIndexOf("/");
            final String parentRelativePath = lastSlashIdx < 0 ? "/" : relativePath.substring(0, lastSlashIdx + 1);
            final boolean prevParentPathChange = !parentRelativePath.equals(prevParentRelativePath);
            final  ScanPathMatch matchStatus =
                    (prevParentRelativePath == null || prevParentPathChange)
                            ? scanSpecification.pathWhiteListMatchStatus(parentRelativePath)
                            : prevMatchStatus;
            prevParentRelativePath = parentRelativePath;
            prevMatchStatus = matchStatus;

            switch (matchStatus) {
                case WITHIN_BLACK_LISTED_PATH:
                case NOT_WITHIN_WHITE_LISTED_PATH:continue;
                case WITHIN_WHITE_LISTED_PATH: {
                    if(ClassRelativePath.isClassFile(relativePath)) {
                        classFileMatches.add(zipEntry);
                    }
                }
            }
        }
    }

    @Override
    protected void doParseClassFile(ZipEntry zipEntry, ClassFileBinaryParser parser, ScanSpecification specification,
                                    ConcurrentMap<String, String> internMap,
                                    ConcurrentLinkedQueue<ClassInfoBuilder> unLinkInfos)
            throws IOException {
        if(!ioExceptionOnOpen) {
            try (InputStream stream = zipFile.getInputStream(zipEntry)){
                ClassInfoBuilder infoUnlinked = parser.readClassInfoFromClassFileHeader(stream, internMap);
                if(infoUnlinked != null) {
                    unLinkInfos.add(infoUnlinked);
                }
            }
        }
    }

    public void close() {
        try {
            if(zipFile != null) {
                zipFile.close();
            }
        } catch (IOException e) {
            //todo log this
            throw new RuntimeException(e);
        }
    }

    private ZipFile zipFile;
}
