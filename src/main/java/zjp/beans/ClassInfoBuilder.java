package zjp.beans;

import com.zjp.scanner.ScanSpecification;
import com.zjp.utils.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 10/28/2017.
 */
public class ClassInfoBuilder {

    /**
    * Not thread safe
    * */
    public void link(ScanSpecification scanSpec, Map<String, ClassInfo> infoMap) {
        final ClassInfo classInfo = ClassInfo.addScannedClass(className, isInterface,
                isAnnotation, scanSpec, infoMap);

        if(StringUtils.notEmpty(superclassName)) {
            classInfo.addSuperclass(superclassName, infoMap);
        }

        if(implementedInterfaces != null) {
            implementedInterfaces.forEach( s -> classInfo.addImplementedInterface(s, infoMap));
        }

        if(annotations != null) {
            annotations.forEach( s -> classInfo.addAnnotation(s, infoMap));
        }

        if(methodAnnotations != null) {
            methodAnnotations.forEach( s -> classInfo.addMethodAnnotation(s, infoMap));
        }

        if(fieldAnnotations != null) {
            fieldAnnotations.forEach( s -> classInfo.addFieldAnnotation(s, infoMap));
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

    public ClassInfoBuilder(final String className, final boolean isInterface, final boolean isAnnotation,
                     final ConcurrentMap<String, String> stringInternMap) {
        this.stringInternMap = stringInternMap;
        this.className = intern(className);
        this.isInterface = isInterface;
        this.isAnnotation = isAnnotation;
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
    private final ConcurrentMap<String, String> stringInternMap;
}
