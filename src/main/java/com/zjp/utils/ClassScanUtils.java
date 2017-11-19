package com.zjp.utils;

/**
 * Created by Administrator on 10/15/2017.
 */
public class ClassScanUtils {

    public static String interfaceName(final Class<?> obj) {
        if(!obj.isInterface()) {
            throw new IllegalArgumentException("Class" + obj.getName() + "is not an interface");
        }
        return obj.getName();
    }

    public static String annotationName(final Class<?> obj) {
        if(!obj.isAnnotation()) {
            throw new IllegalArgumentException("Class" + obj.getName() + "is not an annotation");
        }
        return obj.getName();
    }

    public static boolean isJar(final String path) {
        final int len = path.length();
        return path.regionMatches(true, len - 4, ".jar", 0, 4) //
                || path.regionMatches(true, len - 4, ".zip", 0, 4) //
                || path.regionMatches(true, len - 4, ".war", 0, 4) //
                || path.regionMatches(true, len - 4, ".car", 0, 4) //
                || path.regionMatches(true, len - 6, ".wsjar", 0, 6);
    }
}
