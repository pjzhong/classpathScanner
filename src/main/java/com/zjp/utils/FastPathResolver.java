package com.zjp.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/11/2.
 */
public class FastPathResolver {
    private static final Pattern percentMatcher = Pattern.compile("([%][0-9a-fA-F][0-9a-fA-F])+");
    private static final boolean WINDOWS = File.separatorChar == '\\';

    /** Translate backslashes to forward slashes, optionally removing trailing separator. */
    private static void translateSeparator(String path, int startIdx, int endIdx,
                                           boolean stripFinalSeparator, StringBuilder buf) {
        for(int i = startIdx; i < endIdx; i++) {
            final char c = path.charAt(i);
            if(c == '\\' || c == '/') {
                if( i < endIdx - 1 || !stripFinalSeparator) {
                    char prevChar = buf.length() == 0 ? '\0' : buf.charAt(buf.length() - 1);
                    if(prevChar != '/') { buf.append('/'); }
                }
            } else {
                buf.append(c);
            }
        }
    }

    private static void unescapePercentEncoding(String path, final int startIdx, int endIdx, StringBuilder buf) {
        if(endIdx - startIdx == 3
                && path.charAt(startIdx + 1) == '2' && path.charAt(startIdx + 2) == '0') {
            buf.append(' ');
        } else {
            final byte[] bytes = new byte[(endIdx - startIdx) / 3];
            //a simple url decode, google it for more details about URLEncode work if you want
            // or use URLDecoder to replace this method
            for(int i = startIdx, j = 0; i < endIdx; i+=3, j++) {
                final char c1 = path.charAt(i + 1);
                final char c2 = path.charAt(i + 2);
                final int digit1 = (c1 >= '0' && c1 <= '9') ? (c1 - '0')
                        : (c1 >= 'a' && c1 <= 'f') ? (c1 - 'a' + 10) : (c1 - 'A' + 10);
                final int digit2 = (c2 >= '0' && c2 <= '9') ? (c2 - '0')
                        : (c2 >= 'a' && c2 <= 'f') ? (c2 - 'a' + 10) : (c2 - 'A' + 10);
                bytes[j] = (byte) ((digit1 << 4) | digit2);
            }
            String str = new String(bytes, StandardCharsets.UTF_8);
            translateSeparator(str, 0, str.length(), false, buf);
        }
    }

    public static String normalizePath(final String path) {
        final boolean hasPercent = (path.indexOf('%') >= 0);
        if(!hasPercent && (path.indexOf('\\') < 0) && !path.endsWith("/")) {
            return path;
        } else {
            final StringBuilder builder = new StringBuilder(path.length());
            if(hasPercent) {
                int prevEndMatchIdx = 0;
                final Matcher matcher = percentMatcher.matcher(path);
                while (matcher.find()) {
                    translateSeparator(path, prevEndMatchIdx, matcher.start(), false, builder);
                    unescapePercentEncoding(path, matcher.start(), matcher.end(), builder);
                    prevEndMatchIdx = matcher.end();
                }

                translateSeparator(path, prevEndMatchIdx, path.length(), true, builder);
            } else {
                translateSeparator(path, 0, path.length(), true, builder);
            }
            return builder.toString();
        }
    }

    public static String resolve(final String resolveBasePath, final String relativePathStr) {
        if(StringUtils.isEmpty(relativePathStr)) {
            return resolveBasePath;
        }

        //We don't fetch remote classpath entries, although they are theoretically valid if
        // using a URLClassLoader
        if(relativePathStr.startsWith("http:") || relativePathStr.startsWith("https:")) {
            return "";
        }

        int startIdx = 0;
        if(relativePathStr.startsWith("jar:", startIdx)) { startIdx += 4; }

        boolean isAbsolutePath = false;
        String prefix = "";
        if(relativePathStr.startsWith("file:", startIdx)) {
            startIdx += 5;
            if(WINDOWS) {
                if(relativePathStr.startsWith("\\\\\\\\", startIdx)
                        || relativePathStr.startsWith("////", startIdx)) {
                    ///Windows UNC URL
                    startIdx += 4;
                    prefix = "//";
                    isAbsolutePath = true;
                } else if(relativePathStr.startsWith("\\\\", startIdx)) {
                    startIdx += 2;
                }
            }
            if(relativePathStr.startsWith("//", startIdx)) {
                startIdx += 2;
            }
        } else if(WINDOWS && (relativePathStr.startsWith("//") || relativePathStr.startsWith("\\")) ) {
            startIdx += 2;
            prefix = "//";
            isAbsolutePath = true;
        }

        //Handle Windows path starting with a drive designation as an absolute path
        if(WINDOWS) {
            if(relativePathStr.length() - startIdx > 2
                    && Character.isLetter(relativePathStr.charAt(startIdx))
                    && relativePathStr.charAt(startIdx + 1) == ':') {
                isAbsolutePath = true;
            } else if(relativePathStr.length() - startIdx > 3
                    && (relativePathStr.charAt(startIdx) == '/' || relativePathStr.charAt(startIdx) == '\\' )
                    && Character.isLetter(relativePathStr.charAt(startIdx + 1))
                    ) {
                isAbsolutePath = true;
                startIdx++;
            }
        }

        // Catch-all for paths starting with separator
        if (relativePathStr.length() - startIdx > 1
                && (relativePathStr.charAt(startIdx) == '/' || relativePathStr.charAt(startIdx) == '\\')) {
            isAbsolutePath = true;
        }

        String pathStr = normalizePath(startIdx == 0 ? relativePathStr : relativePathStr.substring(startIdx));
        if(!prefix.isEmpty()) {
            pathStr = prefix + pathStr;
        }

        if(resolveBasePath == null || isAbsolutePath) {
            return pathStr;
        } else {
            return resolveBasePath + "/" + pathStr;
        }
    }

    public static String resolve(final String pathStr) {
        return resolve(null, pathStr);
    }
}
