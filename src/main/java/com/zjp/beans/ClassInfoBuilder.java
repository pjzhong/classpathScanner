package com.zjp.beans;

import com.zjp.scanner.ScanSpecification;
import com.zjp.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 10/28/2017.
 */
public class ClassInfoBuilder {

    ClassInfo addScannedClass(final String className, boolean isInterface, boolean isAnnotation) {
        ClassInfo classInfo;
        if(infoMap.containsKey(className)) {
            classInfo = infoMap.get(className);
        } else {
            infoMap.put(className, classInfo = new ClassInfo(className));
        }

        classInfo.classFileScanned = true;
        classInfo.isInterface |= isInterface;
        classInfo.isAnnotation |= isAnnotation;
        return classInfo;
    }

    ClassInfo getClassInfo(String className) {
        ClassInfo classInfo = infoMap.get(className);
        if(classInfo == null) {
            infoMap.put(className, classInfo = new ClassInfo(className));
        }
        return classInfo;
    }

    /**
    * Not thread safe
     * @param infoMap for cache all classInfo Instances
    * */
    void build(Map<String, ClassInfo> infoMap) {
        this.infoMap = infoMap;
        final ClassInfo classInfo = addScannedClass(className, isInterface, isAnnotation);

        if(StringUtils.notEmpty(superclassName)) {
            classInfo.addSuperclass(superclassName, this);
        }

        if(implementedInterfaces != null) {
            implementedInterfaces.forEach( s -> classInfo.addImplementedInterface(s, this));
        }

        if(annotations != null) {
            annotations.forEach(s -> classInfo.addAnnotation(s, this));
        }

        if(methodAnnotations != null) {
            methodAnnotations.forEach( s -> classInfo.addMethodAnnotation(s, this));
        }

        if(fieldAnnotations != null) {
            fieldAnnotations.forEach( s -> classInfo.addFieldAnnotation(s, this));
        }

        if(fieldInfoList != null) {
            classInfo.addFieldInfo(fieldInfoList);
        }

        if(methodInfoList != null) {
            classInfo.addMethodInfo(methodInfoList);
        }
    }

    private String intern(final String string) {
        if (string == null) {
            return null;
        }
        final String oldValue = stringInternMap.putIfAbsent(string, string);
        return oldValue == null ? string : oldValue;
    }

    public void addSuperclass(final String superclassName) {
        this.superclassName = intern(superclassName);
    }

    public void addImplementedInterface(final String interfaceName) {
        if (implementedInterfaces == null) {
            implementedInterfaces = new ArrayList<>();
        }
        implementedInterfaces.add(intern(interfaceName));
    }

    public void addAnnotation(final String annotationName) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(intern(annotationName));
    }

    public void addMethodAnnotation(final String annotationName) {
        if (methodAnnotations == null) {
            methodAnnotations = new HashSet<>();
        }
        methodAnnotations.add(intern(annotationName));
    }

    public void addFieldAnnotation(final String annotationName) {
        if (fieldAnnotations == null) {
            fieldAnnotations = new HashSet<>();
        }
        fieldAnnotations.add(intern(annotationName));
    }

    public void addFieldInfo(final FieldInfo fieldInfo) {
        if (fieldInfoList == null) {
            fieldInfoList = new ArrayList<>();
        }
        fieldInfoList.add(fieldInfo);
    }

    public void addMethodInfo(final MethodInfo methodInfo) {
        if (methodInfoList == null) {
            methodInfoList = new ArrayList<>();
        }
        methodInfoList.add(methodInfo);
    }

    public String getClassName() {
        return className;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isAnnotation() {
        return isAnnotation;
    }

    public String getSuperclassName() {
        return superclassName;
    }

    ClassInfoBuilder(final String className, final boolean isInterface, final boolean isAnnotation,
                     final ConcurrentMap<String, String> stringInternMap) {
        this.stringInternMap = stringInternMap;
        this.className = intern(className);
        this.isInterface = isInterface;
        this.isAnnotation = isAnnotation;
    }

    private final String className;
    private final boolean isInterface;
    private final boolean isAnnotation;
    // Superclass (can be null if no superclass, or if superclass is blacklisted)
    private String superclassName;
    private List<String> implementedInterfaces;
    private List<String> annotations;
    private Set<String> methodAnnotations;
    private Set<String> fieldAnnotations;
    private List<FieldInfo> fieldInfoList;
    private List<MethodInfo> methodInfoList;
    private Map<String, ClassInfo> infoMap; //intense share by all ClassInfoBuilder instance
    private final ConcurrentMap<String, String> stringInternMap;//复用字符串
}
