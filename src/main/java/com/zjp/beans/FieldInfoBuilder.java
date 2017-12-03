package com.zjp.beans;

import java.util.*;

/**
 * Created by Administrator on 2017/11/23.
 */
public class FieldInfoBuilder {

    FieldInfoBuilder(String className, String fieldName, String typeDescriptor, int modifiers) {
        this.className = className;
        this.fieldName = fieldName;
        this.typeDescriptor = typeDescriptor;
        this.modifiers = modifiers;
    }

    public FieldInfo build() {
        FieldInfo info = new FieldInfo(className, fieldName, typeDescriptor, modifiers);
        if(constantValue != null) { info.setConstantValue(constantValue); }
        info.setAnnotations(annotations);
        return info;
    }

    public void setConstantValue(Object constantValue) {
        this.constantValue = constantValue;
    }

    public void addAnnotationNames(AnnotationInfo annotation) {
        if(annotations.isEmpty()) {
            annotations = new HashMap<>(2);
        }
        annotations.put(annotation.getName(), annotation);
    }

    private final String className;
    private final String fieldName;
    private final String typeDescriptor;
    private final int modifiers;

    //optional value
    private Object constantValue;
    private Map<String, AnnotationInfo> annotations = Collections.EMPTY_MAP;
}