package com.zjp.matchprocessor;

import com.zjp.beans.ClassInfo;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 11/4/2017.
 */
@FunctionalInterface
public interface MethodAnnotationMatchProcessor {
    void processMatch(ClassInfo info, Class<?> matchingClass, Method matchingMethod);
}
