package com.zjp.beans;

import com.zjp.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 10/28/2017.
 */
public class ClassInfoBuilder {

    ClassInfo addScannedClass(final String className) {
        ClassInfo classInfo;
        if(infoMap.containsKey(className)) {
            classInfo = infoMap.get(className);
        } else {
            infoMap.put(className, classInfo = new ClassInfo(className));
        }

        classInfo.classFileScanned = true;
        classInfo.isInterface |= isInterface();
        classInfo.isAnnotation |= isAnnotation();
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
        final ClassInfo classInfo = addScannedClass(className);

        if(StringUtils.notEmpty(superclassName)) {
            classInfo.addSuperclass(superclassName, this);
        }

        implementedInterfaces.forEach( s -> classInfo.addImplementedInterface(s, this));
        annotations.values().forEach(a -> classInfo.addAnnotation(a, this));
        methodAnnotations.forEach( s -> classInfo.addMethodAnnotation(s, this));
        fieldAnnotations.forEach( s -> classInfo.addFieldAnnotation(s, this));
        classInfo.addFieldInfo(fieldInfoList);
        classInfo.addMethodInfo(methodInfoList);
    }

    public void addSuperclass(final String superclassName) {
        this.superclassName = superclassName;
    }

    public void addImplementedInterface(final String interfaceName) {
        if (implementedInterfaces.isEmpty()) {
            implementedInterfaces = new ArrayList<>();
        }
        implementedInterfaces.add(interfaceName);
    }

    public void addAnnotation(AnnotationInfo annotation) {
        if (annotations.isEmpty()) {
            annotations = new HashMap<>(2);
        }
        annotations.put(annotation.getName(), annotation);
    }

    public void addMethodAnnotation(AnnotationInfo annotation) {
        if (methodAnnotations.isEmpty()) {
            methodAnnotations = new HashSet<>();
        }
        methodAnnotations.add(annotation.getName());
    }

    public void addFieldAnnotation(AnnotationInfo annotation) {
        if (fieldAnnotations.isEmpty()) {
            fieldAnnotations = new HashSet<>();
        }
        fieldAnnotations.add(annotation.getName());
    }

    public void addFieldInfo(final FieldInfo fieldInfo) {
        if (fieldInfoList.isEmpty()) {
            fieldInfoList = new ArrayList<>();
        }
        fieldInfoList.add(fieldInfo);
    }

    public void addMethodInfo(final MethodInfo methodInfo) {
        if (methodInfoList.isEmpty()) {
            methodInfoList = new ArrayList<>();
        }
        methodInfoList.add(methodInfo);
    }

    public String getClassName() {
        return className;
    }

    public boolean isInterface() {
        return !isAnnotation() && ((accessFlag & 0x0200) != 0);
    }

    public boolean isAnnotation() {
        return (accessFlag & 0x2000) != 0;
    }

    public String getSuperclassName() {
        return superclassName;
    }

    ClassInfoBuilder(final String className, final int accessFlag) {
        this.className = className;
        this.accessFlag = accessFlag;
    }

    private final String className;

    private final int accessFlag;
    private String superclassName;    // Superclass (can be null if no superclass, or if superclass is blacklisted)
    private Set<String> methodAnnotations = Collections.EMPTY_SET;
    private Set<String> fieldAnnotations = Collections.EMPTY_SET;
    private List<String> implementedInterfaces = Collections.EMPTY_LIST;
    private Map<String, AnnotationInfo> annotations = Collections.EMPTY_MAP;
    private List<FieldInfo> fieldInfoList = Collections.EMPTY_LIST;
    private List<MethodInfo> methodInfoList = Collections.EMPTY_LIST;

    private Map<String, ClassInfo> infoMap; //intense share by all ClassInfoBuilder instance
}
