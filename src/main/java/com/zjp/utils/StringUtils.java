package com.zjp.utils;

/**
 * Created by Administrator on 10/14/2017.
 */
public class StringUtils {

    public static boolean isEmpty(String str) {
        return (str == null || str.isEmpty());
    }

    public static  boolean notEmpty(String str) {
        return !isEmpty(str);
    }
}
