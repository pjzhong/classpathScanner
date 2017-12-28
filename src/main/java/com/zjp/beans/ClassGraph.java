package com.zjp.beans;

import com.zjp.scanner.ScanSpecification;
import com.zjp.utils.ClassScanUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 11/12/2017.
 */
public class ClassGraph {

    public List<ClassInfo> getAllInterfaces() {
        return  classBeans.values()
                .parallelStream()
                .filter(c -> c.isInterface() && c.isClassFileScanned())
                .collect(Collectors.toList());
    }

    public List<ClassInfo> getAllAnnotations() {
        return classBeans.values()
                .parallelStream()
                .filter(c -> c.isAnnotation() && c.isClassFileScanned())
                .collect(Collectors.toList());
    }

    public List<ClassInfo> getAllStandardClass() {
        return classBeans.values()
                .parallelStream()
                .filter(c -> c.isStandardClass() && c.isClassFileScanned())
                .collect(Collectors.toList());
    }

    public List<ClassInfo> getInfoOfClassSuperClassOf(Class<?> target) {
        final ClassInfo info = classBeans.get(target.getName());
        return (info == null) ? Collections.EMPTY_LIST : new ArrayList<>(info.getSuperClasses());
    }

    public List<ClassInfo> getInfoOfClassSubClassOf(Class<?> target) {
        final ClassInfo info = classBeans.get(target.getName());
        return (info == null) ? Collections.EMPTY_LIST : new ArrayList<>(info.getSubClasses());
    }

    /**
     * return all standard class(include abstract) that implementing the specific interface,exclude interface and annotation
     * */
    public List<ClassInfo> getInfoOfClassImplementing(Class<?> targetInterfaces) {
        final ClassInfo info = classBeans.get(ClassScanUtils.interfaceName(targetInterfaces));
        if(info == null) {
            return Collections.EMPTY_LIST;
        } else {
            Set<ClassInfo> reachableClasses = filterClassInfo(info.getClassesImplementing(), ClassType.STANDARD_CLASS);
            return new ArrayList<>(reachableClasses);
        }
    }

    /***
     * return All class that annotated by the specific annotation , exclude annotation
     */
    public List<ClassInfo> getInfoOfClassesWithMethodAnnotation(Class<?> targetAnnotation) {
        final ClassInfo info = classBeans.get(ClassScanUtils.annotationName(targetAnnotation));
        if(info == null) {
            return Collections.EMPTY_LIST;
        } else {
            Set<ClassInfo> classWithAnnotation = filterClassInfo(info.getClassesWithMethodAnnotation(),
                    ClassType.STANDARD_CLASS, ClassType.INTERFACES);

            return new ArrayList<>(classWithAnnotation);
        }
    }

    public List<ClassInfo> getInfoOfClassesWithFieldAnnotation(Class<?> targetAnnotation) {
        final ClassInfo info = classBeans.get(ClassScanUtils.annotationName(targetAnnotation));
        return (info == null) ? Collections.EMPTY_LIST : new ArrayList<>(info.getClassesWithFieldAnnotation());
    }

    public List<ClassInfo> getInfoOfClassesWithAnnotation(Class<?> targetAnnotation) {
        final ClassInfo info = classBeans.get(ClassScanUtils.annotationName(targetAnnotation));
        return (info == null) ? Collections.EMPTY_LIST : new ArrayList<>(info.getClassesWithAnnotation());
    }

    private Set<ClassInfo> filterClassInfo(Set<ClassInfo> infoSet, ClassType ... classTypes) {
        if(infoSet == null) { return Collections.EMPTY_SET; }

        boolean includeAllTypes = (classTypes.length == 0);
        boolean includeStandardClasses = false;
        boolean includeInterfaces = false;
        boolean includeAnnotations = false;

        for(ClassType classType : classTypes) {
            if(includeAllTypes) { break; }
            switch (classType) {
                case ALL:includeAllTypes = true;break;
                case STANDARD_CLASS:includeStandardClasses = true;break;
                case INTERFACES:includeInterfaces = true;break;
                case ANNOTATION:includeAnnotations = true;break;
                case INTERFACE_OR_ANNOTATION:includeInterfaces = includeAnnotations = true;break;
            }
        }

        if((includeStandardClasses && includeAnnotations && includeInterfaces) || includeAllTypes) {
            return infoSet;
        }

        Set<ClassInfo> infoAfterFiltered = new HashSet<>(infoSet.size());
        for(ClassInfo info : infoSet) {
            if((includeStandardClasses && info.isStandardClass())
                    || (includeInterfaces && info.isInterface())
                    || (includeAnnotations && info.isAnnotation())
                    ) {
                if(info.isClassFileScanned()) {
                    infoAfterFiltered.add(info);
                }
            }
        }

        return infoAfterFiltered;
    }

    public static ClassGraphBuilder builder(ScanSpecification specification, Collection<ClassInfoBuilder> builders) {
        return new ClassGraphBuilder(specification, builders);
    }

    ClassGraph(ScanSpecification specification, Map<String, ClassInfo> infoMap) {
        this.specification = specification;
        this.classBeans = infoMap;
    }

    public final Map<String, ClassInfo> classBeans;
    private final ScanSpecification specification;
    private enum ClassType {
        ALL,
        STANDARD_CLASS,
        INTERFACES,
        ANNOTATION,
        INTERFACE_OR_ANNOTATION
    }
}
