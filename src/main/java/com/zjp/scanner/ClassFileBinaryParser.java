package com.zjp.scanner;

import com.zjp.beans.ClassInfo;
import com.zjp.beans.ClassInfoBuilder;
import com.zjp.beans.FieldInfo;
import com.zjp.beans.MethodInfo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 10/15/2017.
 */
public class ClassFileBinaryParser {

    public ClassInfoBuilder readClassInfoFromClassFileHeader(final InputStream inputStream,
                                                             ConcurrentMap<String, String> interMap) throws IOException{
        final DataInputStream classInput = new DataInputStream(new BufferedInputStream(inputStream, 1024));

        //Magic number
        if(classInput.readInt() != 0xCAFEBABE) {
            throw new RuntimeException("No a valid class File");
        }
        classInput.readUnsignedShort();//Minor version
        classInput.readUnsignedShort();//Major version

        final int constantCount = classInput.readUnsignedShort();//Constant pool count
        final Object[] constantPool = new Object[constantCount];
        for(int i = 1; i < constantCount; i++) {
            final int tag = classInput.readUnsignedByte();
            switch (tag) {
                //Modified UTF8 - String
                case 1:{constantPool[i] = classInput.readUTF();} break;
                //byte, boolean, char, short, int are all represented by Constant_INTEGER
                case 3:{constantPool[i] = classInput.readInt();} break;
                // float
                case 4:{constantPool[i] = classInput.readFloat();} break;
                // long double-slot
                case 5:{constantPool[i] = classInput.readLong(); i++;} break;
                // double double-slot
                case 6:{constantPool[i] = classInput.readDouble(); i++;} break;
                // class String
                case 7:
                case 8: {constantPool[i] = classInput.readUnsignedShort();} break;
                //field-ref, method-ref, interface-ref, name and type all double-slot
                case 9:
                case 10:
                case 11:
                case 12:{ classInput.skipBytes(4); } break;
                //method handler
                case 15:{ classInput.skipBytes(3); } break;
                //method type
                case 16: { classInput.skipBytes(2); } break;
                //invoke dynamic
                case 18: { classInput.skipBytes(4); } break;
            }
        }

        //Access flags
        final int accFlag = classInput.readUnsignedShort();
        final boolean isSynthetic= (accFlag & 0x1000) != 0;
        if(isSynthetic) { return null; }//do not scanned class file generate by compiler

        final String className = readRefString(classInput, constantPool).replace('/', '.');
        if(className.equals("java.lang.Object")) {
            //java.lang.Object doesn't have a superclass to be linked to, can simply return
            return null;
        }

        final String superclassName = readRefString(classInput, constantPool).replace('/', '.');
        final ClassInfoBuilder infoBuilder = ClassInfo.builder(className, accFlag, interMap);
        infoBuilder.addSuperclass(superclassName);

        //Interfaces
        final int interfaceCount = classInput.readUnsignedShort();
        for(int i = 0; i < interfaceCount; i++) {
            infoBuilder.addImplementedInterface(readRefString(classInput, constantPool).replace('/', '.'));
        }

        parseFields(classInput, constantPool, infoBuilder);
        parseMethods(classInput, constantPool, infoBuilder);


        //Attribute (including class annotations)
        final int attributesCount = classInput.readUnsignedShort();
        for(int i = 0; i < attributesCount; i++) {
            final String attributeName = readRefString(classInput, constantPool);
            final int attributeLength = classInput.readInt();
            if("RuntimeVisibleAnnotations".equals(attributeName)) {
                final int annotationCount = classInput.readUnsignedShort();
                for(int j = 0; j < annotationCount; j++) {
                    final String annotationName = readAnnotation(classInput, constantPool);
                    infoBuilder.addAnnotation(annotationName);
                }
            } else {
                classInput.skipBytes(attributeLength);
            }
        }

        return infoBuilder;
    }

    private void parseFields(DataInputStream classInput, Object[] constantPool, ClassInfoBuilder infoBuilder) throws IOException {
        //Fields
        final int fieldCount = classInput.readUnsignedShort();
        for(int i = 0; i < fieldCount; i++) {
            final int accessFlags = classInput.readUnsignedShort();
            final String fieldName = readRefString(classInput, constantPool);
            final  String descriptor = readRefString(classInput, constantPool);

            final int attributeCount = classInput.readUnsignedShort();
            List<String> fieldAnnotationNames = new ArrayList<>(1);
            for(int j = 0; j < attributeCount; j++) {
                final String attributeName = readRefString(classInput, constantPool);
                final int attributeLength = classInput.readInt();
                if(attributeName.equals("RuntimeVisibleAnnotations")) {
                    final int annotationCount = classInput.readUnsignedShort();
                    for(int k = 0; k < annotationCount; k++) {
                        final String annotationName = readAnnotation(classInput, constantPool);
                        infoBuilder.addFieldAnnotation(annotationName);
                        fieldAnnotationNames.add(annotationName);
                    }
                } else {
                    classInput.skipBytes(attributeLength);
                }
            }
            infoBuilder.addFieldInfo(new FieldInfo(infoBuilder.getClassName(), fieldName, accessFlags, descriptor, fieldAnnotationNames));
        }
    }

    private void parseMethods(DataInputStream classInput, Object[] constantPool, ClassInfoBuilder infoBuilder) throws IOException {
        //Methods
        final int methodCount = classInput.readUnsignedShort();
        for(int i = 0; i < methodCount; i++) {
            final int accessFlags = classInput.readUnsignedShort();
            final String methodName = readRefString(classInput, constantPool);
            final String descriptor = readRefString(classInput, constantPool);

            final int attributeCount = classInput.readUnsignedShort();
            List<String> methodAnnotationNames = new ArrayList<>(1);
            for(int j = 0; j < attributeCount; j++) {
                final String attributeName = readRefString(classInput, constantPool);
                final int attributeLength = classInput.readInt();
                if(attributeName.equals("RuntimeVisibleAnnotations")) {
                    final int annotationCount = classInput.readUnsignedShort();
                    for(int k = 0; k < annotationCount; k++) {
                        final String annotationName = readAnnotation(classInput, constantPool);
                        infoBuilder.addMethodAnnotation(annotationName);
                        methodAnnotationNames.add(annotationName);
                    }
                } else {
                    classInput.skipBytes(attributeLength);
                }
            }

            final boolean isConstructor = "<init>".equals(methodName);
            final String className = infoBuilder.getClassName();
            infoBuilder.addMethodInfo(new MethodInfo(className, isConstructor ? className : methodName,
                    accessFlags, descriptor, methodAnnotationNames, isConstructor));
        }
    }

    private String readAnnotation(final DataInputStream input, final Object[] constantPool) throws IOException {
        final String annotationFieldDescriptor = readRefString(input, constantPool);
        String annotationClassName;
        if(annotationFieldDescriptor.charAt(0) == 'L'
                && annotationFieldDescriptor.charAt(annotationFieldDescriptor.length() - 1) == ';' ) {
            //Lcom/xyz/Annotation; -> com.xyz.Annotation
            annotationClassName = annotationFieldDescriptor.substring(1, annotationFieldDescriptor.length() - 1)
                    .replace('/', '.');
        } else {
            //Should not happen, because annotation is an Object
            annotationClassName = annotationFieldDescriptor;
        }

        final int numElementValuePairs = input.readUnsignedShort();
        for(int i = 0; i < numElementValuePairs; i++) {
            final String elementName = readRefString(input, constantPool);//element_name_index
            readAnnotationElementValue(input, constantPool);
        }

        return annotationClassName;
    }

    /**
     * todo try to parse AnnotationElementValue
     * */
    private void readAnnotationElementValue(final DataInputStream input, final Object[] constantPool)
            throws IOException {
        final int tag = input.readUnsignedByte();
        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                //constant_value_index
                input.skipBytes(2);break;
            case 'e'://class_info_index
                input.skipBytes(4);break;
            case '@'://Complex(nested) annotation
                readAnnotation(input, constantPool);break;
            case '['://array_value
                final int count = input.readUnsignedShort();
                for(int i = 0; i < count; i++) {
                    //Nested annotation element value
                    readAnnotationElementValue(input, constantPool);
                }
                break;
        }
    }

    private String readRefString(final DataInputStream input, final Object[] constantPool)
            throws IOException {
        final int index = input.readUnsignedShort();
        if(constantPool[index] instanceof Integer) {//indirect reference, like CONSTANT_Class,CONSTANT_String
            return (String) constantPool[(int)constantPool[index]];
        } else {
            return (String) constantPool[index];
        }
    }

    public ClassFileBinaryParser() {}
}
