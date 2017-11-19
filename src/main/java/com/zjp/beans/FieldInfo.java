package com.zjp.beans;

import com.zjp.utils.ReflectionUtils;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 10/28/2017.
 */
@Immutable
public class FieldInfo {

    private final String belongToClass;
    private final String fieldName;
    private final int modifiers;
    private final String typeStr;
    private final List<String> annotationNames;

    public FieldInfo(String className, final String fieldName, final int modifiers, final String typeDescriptor, final List<String> annotationNames) {
        this.belongToClass = className;
        this.fieldName = fieldName;
        this.modifiers = modifiers;

        final List<String> typeNames = ReflectionUtils.parseTypeDescriptor(typeDescriptor);
        if (typeNames.size() != 1) {
            throw new IllegalArgumentException("Invalid type descriptor for field: " + typeDescriptor);
        }
        this.typeStr = typeNames.get(0);

        this.annotationNames = annotationNames.isEmpty() ? Collections.<String> emptyList() : annotationNames;
    }

    /** Get the field modifiers as a string, e.g. "public static final". */
    public String getModifiers() {
        return ReflectionUtils.modifiersToString(modifiers, /* isMethod = */ false);
    }

    /** Returns true if this field is public. */
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    /** Returns true if this field is private. */
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    /** Returns true if this field is protected. */
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    /** Returns true if this field is package-private. */
    public boolean isPackagePrivate() {
        return !isPublic() && !isPrivate() && !isProtected();
    }

    /** Returns true if this field is static. */
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    /** Returns true if this field is final. */
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    /** Returns true if this field is a transient field. */
    public boolean isTransient() {
        return Modifier.isTransient(modifiers);
    }

    /** Returns the name of the field. */
    public String getFieldName() {
        return fieldName;
    }

    /** Returns the access flags of the field. */
    public int getAccessFlags() {
        return modifiers;
    }

    /** Returns the type of the field, in string representation (e.g. "int[][]"). */
    public String getTypeStr() {
        return typeStr;
    }

    /** Returns the names of annotations on the field, or the empty list if none. */
    public List<String> getAnnotationNames() { return Collections.unmodifiableList(annotationNames); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldInfo fieldInfo = (FieldInfo) o;

        if (!belongToClass.equals(fieldInfo.belongToClass)) return false;
        if (!fieldName.equals(fieldInfo.fieldName)) return false;
        return typeStr.equals(fieldInfo.typeStr);
    }

    @Override
    public int hashCode() {
        int result = belongToClass.hashCode();
        result = 31 * result + fieldName.hashCode();
        result = 31 * result + typeStr.hashCode();
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

        if (buf.length() > 0) {
            buf.append(' ');
        }
        buf.append(getTypeStr());

        buf.append(' ');
        buf.append(fieldName);

        return buf.toString();
    }
}
