package com.zjp.matchprocessor;

import com.zjp.beans.ClassInfo;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 11/4/2017.
 */
@FunctionalInterface
public interface FieldAnnotationMatchProcessor {
    public void processMatch(ClassInfo info, Class<?> matchingClass, Field matchingField);
}