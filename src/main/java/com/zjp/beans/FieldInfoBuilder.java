package com.zjp.beans;

import java.util.ArrayList;
import java.util.List;

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
        if(annotationNames != null) { info.setAnnotationNames(annotationNames);}
        return info;
    }

    public void setConstantValue(Object constantValue) {
        this.constantValue = constantValue;
    }

    public void addAnnotationNames(String annotations) {
        if(annotationNames == null) {
            annotationNames = new ArrayList<>(1);
        }
        annotationNames.add(annotations);
    }

    public void setAnnotationNames(List<String> annotationNames) {
        this.annotationNames = annotationNames;
    }

    private final String className;
    private final String fieldName;
    private final String typeDescriptor;
    private final int modifiers;

    //optional value
    private Object constantValue;
    private List<String> annotationNames;
}