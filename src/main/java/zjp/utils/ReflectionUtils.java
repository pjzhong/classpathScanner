package zjp.utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 10/28/2017.
 */
public class ReflectionUtils {

    /** Convert field or method modifiers into a string representation, e.g. "public static final". */
    public static String modifiersToString(final int modifiers, final boolean isMethod) {
        final StringBuilder buf = new StringBuilder();
        if ((modifiers & Modifier.PUBLIC) != 0) {
            buf.append("public");
        } else if ((modifiers & Modifier.PROTECTED) != 0) {
            buf.append("protected");
        } else if ((modifiers & Modifier.PRIVATE) != 0) {
            buf.append("private");
        }
        if ((modifiers & Modifier.STATIC) != 0) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("static");
        }
        if ((modifiers & Modifier.ABSTRACT) != 0) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("abstract");
        }
        if ((modifiers & Modifier.SYNCHRONIZED) != 0) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("synchronized");
        }
        if (!isMethod && (modifiers & Modifier.TRANSIENT) != 0) {
            // TRANSIENT has the same value as VARARGS, since they are mutually exclusive
            // (TRANSIENT applies only to fields, VARARGS applies only to methods)
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("transient");
        } else if ((modifiers & Modifier.VOLATILE) != 0) {
            // VOLATILE has the same value as BRIDGE, since they are mutually exclusive
            // (VOLATILE applies only to fields, BRIDGE applies only to methods)
            if (buf.length() > 0) {
                buf.append(' ');
            }
            if (!isMethod) {
                buf.append("volatile");
            } else {
                buf.append("bridge");
            }
        }
        if ((modifiers & Modifier.FINAL) != 0) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("final");
        }
        if ((modifiers & Modifier.NATIVE) != 0) {
            if (buf.length() > 0) {
                buf.append(' ');
            }
            buf.append("native");
        }
        return buf.toString();
    }

    /**
     * Parse a type descriptor into a type or list of types. For a single type (for a field), returns a list with
     * one item. For a method, returns a list of types, with the first N-1 items corresponding to the argument
     * types, and the last item corresponding to the method return type.
     */
    public static List<String> parseTypeDescriptor(final String typeDescriptor) {
        final List<String> types = new ArrayList<>();
        final StringBuilder builder = new StringBuilder();
        for(int i = 0; i < typeDescriptor.length(); i++) {
            int numDimensions = 0;
            char c = typeDescriptor.charAt(i);
            if(c == '(' || c == ')') { //Beginning or end of (maybe method)arg list, ignore
                continue;
            } else if(c == '[') {  //Beginning of array, count the number of dimensions
                numDimensions += 1;
                for(i++; i < typeDescriptor.length(); i++) {
                    c = typeDescriptor.charAt(i);
                    if (c == '[') { numDimensions++; }
                    else {  break; }
                }
            }
            switch (c) {
                case 'B':builder.append("byte");break;
                case 'C':builder.append("char");break;
                case 'D':builder.append("double");break;
                case 'F':builder.append("float");break;
                case 'I':builder.append("int");break;
                case 'J':builder.append("long");break;
                case 'S':builder.append("short");break;
                case 'Z':builder.append("boolean");break;
                case 'V':builder.append("void");break;
                case 'L': {
                    final int semicolonIdx = typeDescriptor.indexOf(';', i + 1);
                    if(semicolonIdx < 0) {//Missing ';' after class name
                        throw new RuntimeException("Invalid type descriptor: " + typeDescriptor);
                    }
                    final String className =  typeDescriptor.substring(i + 1 , semicolonIdx).replace('/', '.');
                    if(className.isEmpty()) {
                        throw new RuntimeException("Invalid type descriptor: " + typeDescriptor);
                    }
                    builder.append(className);
                    i = semicolonIdx;
                } break;
            }
            for(int j = 0; j < numDimensions; j++) {
                builder.append("[]");
            }
            types.add(builder.toString());
            builder.setLength(0);
        }
        return types;
    }
}
