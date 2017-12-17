package com.zjp.beans;

import com.zjp.utils.MultiMap;

import java.util.*;

/**
 * for more details, see:https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.16.1
 */
public class AnnotationInfo {
    private final String annotationName;
    private Map<String, List<Object>> values = Collections.EMPTY_MAP;

    public AnnotationInfo(String annotationName) {
        this.annotationName = annotationName;
    }

    public String getName() {
        return annotationName;
    }

    public void addValue(String name, List<Object> value) {
        if(values == Collections.EMPTY_MAP) { values = new HashMap<>(4); }
        MultiMap.putAll(values, name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationInfo that = (AnnotationInfo) o;

        return annotationName != null ? annotationName.equals(that.annotationName) : that.annotationName == null;

    }

    @Override
    public int hashCode() {
        return annotationName != null ? annotationName.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('@').append(annotationName);
        if(!values.isEmpty()) {
            sb.append('(');
            Iterator<Map.Entry<String, List<Object>>> entryIterator = values.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry<String, List<Object>> entry = entryIterator.next();
                List<Object> values = entry.getValue();
                if(values != null && !values.isEmpty()) {
                    sb.append(entry.getKey()).append('=').append(values);
                    if(entryIterator.hasNext()) {
                        sb.append(", ");
                    }
                }
            }
            sb.append(')');
        }

        return sb.toString();
    }
}
