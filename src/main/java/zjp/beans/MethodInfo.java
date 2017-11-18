package zjp.beans;

import com.zjp.utils.ReflectionUtils;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 10/28/2017.
 */
@Immutable
public class MethodInfo {
    private final String belongToClass;
    private final String methodName;
    private final int modifiers;
    private final List<String> annotationNames;
    private final List<String> parameterTypeStrings;
    private final String returnTypeStr;
    private final boolean isConstructor;

    public MethodInfo(String className, String methodName, int modifiers, String typeDescriptor,
                      List<String> annotationNames, boolean isConstructor) {
        this.belongToClass = className;
        this.methodName = methodName;
        this.modifiers = modifiers;

        final List<String> typeNames = ReflectionUtils.parseTypeDescriptor(typeDescriptor);
        if (typeNames.size() < 1) {
            throw new IllegalArgumentException("Invalid type descriptor for method: " + typeDescriptor);
        }
        this.parameterTypeStrings = typeNames.subList(0, typeNames.size() - 1);
        this.returnTypeStr = typeNames.get(typeNames.size() - 1);

        this.annotationNames = annotationNames.isEmpty() ? Collections.<String> emptyList() : annotationNames;
        this.isConstructor = isConstructor;
    }

    /** Get the method modifiers as a string, e.g. "public static final". */
    public String getModifiers() {
        return ReflectionUtils.modifiersToString(modifiers, /* isMethod = */ true);
    }

    /** Returns true if this method is a constructor. */
    public boolean isConstructor() {
        return isConstructor;
    }

    /** Returns the name of the method. */
    public String getMethodName() {
        return methodName;
    }

    /** Returns the access flags of the method. */
    public int getAccessFlags() {
        return modifiers;
    }

    /**
     * Returns the return type for the method in string representation, e.g. "char[]". If this is a constructor, the
     * returned type will be "void".
     */
    public String getReturnTypeStr() {
        return returnTypeStr;
    }

    /** Returns the parameter types for the method in string representation, e.g. ["int", "List", "com.abc.XYZ"]. */
    public List<String> getParameterTypeStrings() {
        return Collections.unmodifiableList(parameterTypeStrings);
    }

    /** Returns true if this method is public. */
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    /** Returns true if this method is private. */
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    /** Returns true if this method is protected. */
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    /** Returns true if this method is package-private. */
    public boolean isPackagePrivate() {
        return !isPublic() && !isPrivate() && !isProtected();
    }

    /** Returns true if this method is static. */
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    /** Returns true if this method is final. */
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    /** Returns true if this method is synchronized. */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(modifiers);
    }

    /** Returns true if this method is a bridge method. */
    public boolean isBridge() {
        // From:
        // http://anonsvn.jboss.org/repos/javassist/trunk/src/main/javassist/bytecode/AccessFlag.java
        return (modifiers & 0x0040) != 0;
    }

    /** Returns true if this method is a varargs method. */
    public boolean isVarArgs() {
        // From:
        // http://anonsvn.jboss.org/repos/javassist/trunk/src/main/javassist/bytecode/AccessFlag.java
        return (modifiers & 0x0080) != 0;
    }

    /** Returns true if this method is a native method. */
    public boolean isNative() { return Modifier.isNative(modifiers);
    }

    /** Returns the names of annotations on the method, or the empty list if none. */
    public List<String> getAnnotationNames() {
        return Collections.unmodifiableList(annotationNames);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodInfo that = (MethodInfo) o;

        if (!belongToClass.equals(that.belongToClass)) return false;
        if (!methodName.equals(that.methodName)) return false;
        return parameterTypeStrings.equals(that.parameterTypeStrings);

    }

    @Override
    public int hashCode() {
        int result = belongToClass.hashCode();
        result = 31 * result + methodName.hashCode();
        result = 31 * result + parameterTypeStrings.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();

        if (!annotationNames.isEmpty()) {
            for (final String annotationName : annotationNames) {
                buf.append("@").append(annotationName).append("\n");
            }
        }

        buf.append(getModifiers());

        if (!isConstructor) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append(getReturnTypeStr());
        }

        if (buf.length() > 0) {
            buf.append(' ');
        }
        buf.append(methodName);

        buf.append('(');
        final List<String> paramTypes = getParameterTypeStrings();
        final boolean isVarargs = isVarArgs();
        for (int i = 0; i < paramTypes.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            final String paramType = paramTypes.get(i);
            if (isVarargs && (i == paramTypes.size() - 1)) {
                // Show varargs params correctly
                if (!paramType.endsWith("[]")) {
                    throw new IllegalArgumentException(
                            "Got non-array type for last parameter of varargs method " + methodName);
                }
                buf.append(paramType.substring(0, paramType.length() - 2));
                buf.append("...");
            } else {
                buf.append(paramType);
            }
        }
        buf.append(')');

        return buf.toString();
    }
}
