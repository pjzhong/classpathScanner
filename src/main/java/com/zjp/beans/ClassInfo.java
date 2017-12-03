package com.zjp.beans;

import com.zjp.utils.MultiSet;
import com.zjp.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 11/6/2017.
 */
public class ClassInfo implements Comparable<ClassInfo> {

    private boolean addRelatedClass(Relation relation , ClassInfo info) {
        return MultiSet.put(relationSet, relation, info);
    }

    private Set<ClassInfo> getDirectlyRelatedClass(Relation relation) {
        Set<ClassInfo> relatedClass = relationSet.get(relation);
        return relatedClass == null ? Collections.EMPTY_SET : relatedClass;
    }

    private Set<ClassInfo> getReachableClasses(Relation relation) {
        final Set<ClassInfo> directlyRelatedCLasses = getDirectlyRelatedClass(relation);
        if(directlyRelatedCLasses.isEmpty()) {
            return directlyRelatedCLasses;
        }

        Set<ClassInfo> reachableClasses = new HashSet<>(directlyRelatedCLasses);
        LinkedList<ClassInfo> queue = new LinkedList<>(directlyRelatedCLasses);
        while(!queue.isEmpty()) {
            ClassInfo head = queue.removeFirst();
            for(final ClassInfo info : head.getDirectlyRelatedClass(relation)) {
                if(reachableClasses.add(info)) {//don't get in cycle
                    queue.add(info);
                }
            }
        }

        return  reachableClasses;
    }

    void addSuperclass(String superclassName, ClassInfoBuilder builder) {
        if(StringUtils.notEmpty(superclassName)) {
            final ClassInfo superClass = builder.getClassInfo(superclassName);
            this.addRelatedClass(Relation.SUPERCLASSES, superClass);
            superClass.addRelatedClass(Relation.SUBCLASSES, this);
        }
    }

    void addImplementedInterface(String interfaceName, ClassInfoBuilder builder) {
        if(StringUtils.notEmpty(interfaceName)) {
            final ClassInfo interfaceClass = builder.getClassInfo(interfaceName);
            interfaceClass.isInterface = true;
            this.addRelatedClass(Relation.IMPLEMENTED_INTERFACES, interfaceClass);
            interfaceClass.addRelatedClass(Relation.CLASSES_IMPLEMENTING, this);
        }
    }

    void addAnnotation(AnnotationInfo annotation, ClassInfoBuilder builder) {
        if(annotation != null) {
            final ClassInfo annotationClass = builder.getClassInfo(annotation.getName());
            annotationClass.isAnnotation = true;
            this.addRelatedClass(Relation.ANNOTATIONS, annotationClass);
            annotationClass.addRelatedClass(Relation.ANNOTATED_CLASSES, this);

            if(annotationInfoList == null) {
                annotationInfoList = new HashMap<>(2);
            }
            annotationInfoList.put(annotation.getName(), annotation);
        }
    }

    void addMethodAnnotation(String annotationName, ClassInfoBuilder builder) {
        if(StringUtils.notEmpty(annotationName)) {
            final ClassInfo annotationClass = builder.getClassInfo(annotationName);
            annotationClass.isAnnotation = true;
            this.addRelatedClass(Relation.METHOD_ANNOTATIONS, annotationClass);
            annotationClass.addRelatedClass(Relation.CLASSES_WITH_METHOD_ANNOTATION, this);
        }
    }

    void addFieldAnnotation(String annotationName, ClassInfoBuilder builder) {
        if(StringUtils.notEmpty(annotationName)) {
            final ClassInfo annotationClass = builder.getClassInfo(annotationName);
            annotationClass.isAnnotation = true;
            this.addRelatedClass(Relation.FIELD_ANNOTATIONS, annotationClass);
            annotationClass.addRelatedClass(Relation.CLASSES_WITH_FIELD_ANNOTATION, this);
        }
    }

    /** Add field info. */
    void addFieldInfo(final List<FieldInfo> fieldInfoList) {
        if (this.fieldInfoList == null) {
            this.fieldInfoList = new ArrayList<>();
        }
        this.fieldInfoList.addAll(fieldInfoList);
    }

    /** Add method info. */
    void addMethodInfo(final List<MethodInfo> methodInfoList) {
        if (this.methodInfoList == null) {
            this.methodInfoList = new ArrayList<>();
        }
        this.methodInfoList.addAll(methodInfoList);
    }

    Set<ClassInfo> getClassesWithFieldAnnotation() {
        return getDirectlyRelatedClass(Relation.CLASSES_WITH_FIELD_ANNOTATION);
    }

    Set<ClassInfo> getClassesWithMethodAnnotation() {
        return getDirectlyRelatedClass(Relation.CLASSES_WITH_METHOD_ANNOTATION);
    }

    Set<ClassInfo> getClassesWithAnnotation() {
        if(!isAnnotation()) { return Collections.EMPTY_SET; }

        Set<ClassInfo> classWithAnnotation = getReachableClasses(Relation.ANNOTATED_CLASSES);

        boolean isInherited = false;
        for(ClassInfo metaAnnotation : getDirectlyRelatedClass(Relation.ANNOTATIONS)) {
            if(metaAnnotation.className.equals("java.lang.annotation.Inherited")) {
                isInherited = true;
                break;
            }
        }

        if(isInherited) {
            final Set<ClassInfo> classesWithAnnotationAndTheirSubclasses = new HashSet<>(classWithAnnotation);
            for(ClassInfo info : classWithAnnotation) {
                classesWithAnnotationAndTheirSubclasses.addAll(info.getSubClasses());
            }
            return classesWithAnnotationAndTheirSubclasses;
        }else {
            return classWithAnnotation;
        }
    }

    public Set<ClassInfo> getClassesImplementing() {
        if(!isInterface()) { return Collections.EMPTY_SET; }

        Set<ClassInfo> reachableClasses = getReachableClasses(Relation.CLASSES_IMPLEMENTING);

        final Set<ClassInfo> allImplementingClasses = new HashSet<>();
        for(ClassInfo implementingClass : reachableClasses) {
            allImplementingClasses.add(implementingClass);
            allImplementingClasses.addAll(implementingClass.getReachableClasses(Relation.SUBCLASSES));
        }
        return allImplementingClasses;
    }

    public Set<ClassInfo> getSuperClasses() {
        return getReachableClasses(Relation.SUBCLASSES);
    }

    public Set<ClassInfo> getSubClasses() {
        return getReachableClasses(Relation.SUBCLASSES);
    }

    boolean isClassFileScanned() {
        return classFileScanned;
    }

    public String getClassName() {
        return className;
    }

    public boolean isInterface() {
        return isInterface && !isAnnotation;
    }

    public boolean isAnnotation() {
        return isAnnotation;
    }

    public boolean isStandardClass() {
        return !(isAnnotation ||isInterface);
    }

    public List<FieldInfo> getFieldInfoList() {
        return fieldInfoList == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(fieldInfoList);
    }

    public List<MethodInfo> getMethodInfoList() {
        return methodInfoList == null ? Collections.EMPTY_LIST: Collections.unmodifiableList(methodInfoList);
    }

    ClassInfo(String className) {
        this.className = className;
    }

    public static ClassInfoBuilder builder(String className, int accessFlag, ConcurrentMap<String, String> internMap) {
        return new ClassInfoBuilder(className, accessFlag, internMap);
    }

    @Override
    public int compareTo(ClassInfo o) { return this.className.compareTo(o.className); }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) return false;

        ClassInfo classInfo = (ClassInfo) o;

        return className.equals(classInfo.className);
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public String toString() {
        return (isStandardClass() ? "class "  : isInterface() ? "interface " : "annotation ")  + className;
    }

    private final Map<Relation, Set<ClassInfo>> relationSet = new HashMap<>();
    private final String className;

    boolean isInterface;
    boolean isAnnotation;
    /**
     * True when a class has been scanned(i.e its classFile contents read), as opposed to only being
     * referenced by another class' classFile as a superclass/superInterface/annotation. If classFileScanned is true,
     * then this also must be a whiteListed (and non-blacklisted) class in a whiteListed(and non-blackListed) package
     * */
    boolean classFileScanned;

    private List<FieldInfo> fieldInfoList;
    private List<MethodInfo> methodInfoList;
    private Map<String, AnnotationInfo> annotationInfoList;

    private enum Relation {
        SUPERCLASSES,
        SUBCLASSES,

        /**
         * Interfaces that this class implements, if this is a regular class, or superinterfaces, if this is an
         * interface.
         *
         * (May also include annotations, since annotations are interfaces, so you can implement an annotation.)
         */
        IMPLEMENTED_INTERFACES,

        /** Classes that implement this interface (including sub-interfaces), if this is an interface. */
        CLASSES_IMPLEMENTING,

        ANNOTATIONS,

        ANNOTATED_CLASSES,

        /*Annotations on one ore more methos of this class. */
        METHOD_ANNOTATIONS,

        /*Classes that have one or more method annotated with this annotation, if this is an annotation*/
        CLASSES_WITH_METHOD_ANNOTATION,

        /* Annotations on one or more fields of this class*/
        FIELD_ANNOTATIONS,

        CLASSES_WITH_FIELD_ANNOTATION,
    }
}
