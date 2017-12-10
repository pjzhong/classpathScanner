package com.zjp.scanner;

import com.zjp.beans.*;
import com.zjp.utils.ReflectionUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 10/15/2017.
 */
public class ClassFileBinaryParser {
    private static final Object placeHolder = new Object();
    private ThreadLocal<List<Object>> localConstantPool = new ThreadLocal<>();

    public ClassInfoBuilder readClassInfoFromClassFileHeader(final InputStream inputStream) throws IOException{
        final DataInputStream classInput = new DataInputStream(new BufferedInputStream(inputStream, 1024));

        //Magic number
        if(classInput.readInt() != 0xCAFEBABE) {
            throw new RuntimeException("Not a valid class File");
        }
        classInput.readUnsignedShort();//Minor version
        classInput.readUnsignedShort();//Major version

        final int constantCount = classInput.readUnsignedShort();//Constant pool count
        List<Object> constantPool = localConstantPool.get();
        if(constantPool == null) {
            localConstantPool.set(constantPool = new ArrayList<>(constantCount));
        }

 /*       final Object[] constantPool = new Object[constantCount];*/
        constantPool.clear();
        for(int i = 1; i < constantCount; i++) {
            final int tag = classInput.readUnsignedByte();
            switch (tag) {
              /*  //Modified UTF8 - String
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
                case 8: {constantPool[i] = classInput.readUnsignedShort();} break;*/
                //Modified UTF8 - String
                case 1:{constantPool.add(classInput.readUTF());} break;
                //byte, boolean, char, short, int are all represented by Constant_INTEGER
                case 3:{constantPool.add(classInput.readInt());} break;
                // float
                case 4:{constantPool.add(classInput.readFloat());} break;
                // long double-slot
                case 5:{constantPool.add(classInput.readLong()); constantPool.add(placeHolder);} break;
                // double double-slot
                case 6:{constantPool.add(classInput.readDouble()) ; constantPool.add(placeHolder);} break;
                // class String
                case 7:
                case 8: {constantPool.add(classInput.readUnsignedShort());} break;
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
        if(isSynthetic) { return null; }//skip class file generate by compiler

        final String className = intern(readRefString(classInput, constantPool).replace('/', '.'));
        if(className.equals("java.lang.Object")) {
            //java.lang.Object doesn't have a superclass to be linked to, can simply return
            return null;
        }

        final String superclassName = intern(readRefString(classInput, constantPool).replace('/', '.'));
        final ClassInfoBuilder infoBuilder = ClassInfo.builder(className, accFlag);
        infoBuilder.addSuperclass(superclassName);

        //Interfaces
        final int interfaceCount = classInput.readUnsignedShort();
        for(int i = 0; i < interfaceCount; i++) {
            infoBuilder.addImplementedInterface(intern(readRefString(classInput, constantPool).replace('/', '.')));
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
                    AnnotationInfo info = readAnnotation(classInput, constantPool);
                    infoBuilder.addAnnotation(info);
                }
            } else {
                classInput.skipBytes(attributeLength);
            }
        }

        return infoBuilder;
    }

    private void parseFields(DataInputStream classInput, List<Object> constantPool, ClassInfoBuilder infoBuilder) throws IOException {
        //Fields
        final int fieldCount = classInput.readUnsignedShort();
        for(int i = 0; i < fieldCount; i++) {
            final int accessFlags = classInput.readUnsignedShort();
            final String fieldName = readRefString(classInput, constantPool);
            final  String descriptor = readRefString(classInput, constantPool);

            FieldInfoBuilder fieldBuilder = FieldInfo.builder(infoBuilder.getClassName(), fieldName, descriptor, accessFlags);

            final int attributeCount = classInput.readUnsignedShort();
            for(int j = 0; j < attributeCount; j++) {
                final String attributeName = readRefString(classInput, constantPool);
                final int attributeLength = classInput.readInt();
                switch (attributeName) {
                    case "RuntimeVisibleAnnotations":{
                        final int annotationCount = classInput.readUnsignedShort();
                        for(int k = 0; k < annotationCount; k++) {
                            AnnotationInfo info = readAnnotation(classInput, constantPool);
                            infoBuilder.addFieldAnnotation(info);
                            fieldBuilder.addAnnotationNames(info);
                        }
                    } break;
                    case "ConstantValue": {
                        final int valueIndex = classInput.readUnsignedShort();
                        Object constantValue;
                        final char firstChar = descriptor.charAt(0);
                        switch (firstChar) {
                            case 'B':constantValue = ((Integer)constantPool.get(valueIndex)).byteValue();break;
                            case 'C':constantValue = ((char)((Integer)constantPool.get(valueIndex)).intValue());break;
                            case 'S':constantValue = ((Integer)constantPool.get(valueIndex)).shortValue();break;
                            case 'Z':constantValue =  ((Integer)constantPool.get(valueIndex)) != 0;break;
                            case 'I':
                            case 'J':
                            case 'F'://Integer, Long, Float, Double already in correct type
                            case 'D':constantValue = constantPool.get(valueIndex);break;
                            default: {
                                if(descriptor.equals("Ljava/lang/String;")) {
                                    constantValue = constantPool.get((int)constantPool.get(valueIndex));
                                } else {
                                    throw new RuntimeException("unknown Constant type:" + descriptor);
                                }
                            } break;
                        }
                        fieldBuilder.setConstantValue(constantValue);
                    } break;
                    default:classInput.skipBytes(attributeLength);break;
                }
            }

            infoBuilder.addFieldInfo(fieldBuilder.build());
        }
    }

    private void parseMethods(DataInputStream classInput, List<Object> constantPool, ClassInfoBuilder infoBuilder) throws IOException {
        //Methods
        final int methodCount = classInput.readUnsignedShort();
        for(int i = 0; i < methodCount; i++) {
            final int accessFlags = classInput.readUnsignedShort();
            final String methodName = readRefString(classInput, constantPool);
            final String descriptor = readRefString(classInput, constantPool);

            MethodInfoBuilder methodInfoBuilder = MethodInfo.builder(infoBuilder.getClassName(), methodName, descriptor, accessFlags);

            final int attributeCount = classInput.readUnsignedShort();
            for(int j = 0; j < attributeCount; j++) {
                final String attributeName = readRefString(classInput, constantPool);
                final int attributeLength = classInput.readInt();
                switch (attributeName) {
                    case "RuntimeVisibleAnnotations":{
                        final int annotationCount = classInput.readUnsignedShort();
                        for(int k = 0; k < annotationCount; k++) {
                            AnnotationInfo info = readAnnotation(classInput, constantPool);
                            infoBuilder.addMethodAnnotation(info);
                            methodInfoBuilder.addAnnotationName(info);
                        }
                    } break;
                    case "AnnotationDefault": {

                        List<Object> defaultValue = parseElementValue(classInput, constantPool);
                        methodInfoBuilder.setDefaultValue(defaultValue);
                    } break;
                    default:classInput.skipBytes(attributeLength);
                }
            }

            infoBuilder.addMethodInfo(methodInfoBuilder.build());
        }
    }

    /**
     * try to read a annotation and it's value  from this class, method or field, but ignore nested annotations
     * */
    private AnnotationInfo readAnnotation(final DataInputStream input, List<Object> constantPool) throws IOException {
        final String annotationFieldDescriptor = readRefString(input, constantPool);
        String annotationClassName;
        if(annotationFieldDescriptor.charAt(0) == 'L'
                && annotationFieldDescriptor.charAt(annotationFieldDescriptor.length() - 1) == ';' ) {
            //Lcom/xyz/Annotation; -> com.xyz.Annotation
            annotationClassName = annotationFieldDescriptor.substring(1, annotationFieldDescriptor.length() - 1).replace('/', '.');
            annotationClassName = intern(annotationClassName);
        } else {
            //Should not happen, because annotation is an Object
            annotationClassName = annotationFieldDescriptor;
        }
        AnnotationInfo info = new AnnotationInfo(annotationClassName);
        final int numElementValuePairs = input.readUnsignedShort();
        for(int i = 0; i < numElementValuePairs; i++) {
            final String elementName = readRefString(input, constantPool);//element_name_index
            List<Object> value = parseElementValue(input, constantPool);
            info.addValue(elementName, value);
        }

        return info;
    }


    /**
     * @param input the bytes of stream of a classFile
     * @param constantPool as the name means
     * */
    private List<Object> parseElementValue(final DataInputStream input, List<Object> constantPool)
            throws IOException {
        final int tag = input.readUnsignedByte();
        switch (tag) {
            case '@'://Complex(nested) annotation
                readAnnotation(input, constantPool);//ignore nested annotations
                return Collections.EMPTY_LIST;
            case '['://array_value
                final int count = input.readUnsignedShort();
                List<Object> arrayValues = new ArrayList<>();
                for(int i = 0; i < count; i++) {
                    //Nested annotation element value
                    arrayValues.addAll(parseElementValue(input, constantPool));
                }
                return arrayValues;
            default:return Arrays.asList(parseElementValue(tag, input, constantPool));
        }
    }

    private Object parseElementValue(int tag, DataInputStream input, List<Object> constantPool) throws IOException {
        switch (tag) {
            case 'B':return ((Integer) constantPool.get(input.readUnsignedShort())).byteValue();
            case 'C':return (char) ((Integer) constantPool.get(input.readUnsignedShort())).intValue();
            case 'S':return ((Integer) constantPool.get(input.readUnsignedShort())).shortValue();
            case 'Z':return ((Integer) constantPool.get(input.readUnsignedShort())) != 0;
            case 'I'://int
            case 'J'://long
            case 'D'://double
            case 'F'://float
            case 's'://string
                return constantPool.get(input.readUnsignedShort());//Already in correct type;
            case 'c': { //class_info_index
                String typeDescriptor = (String) constantPool.get(input.readUnsignedShort());
                List<String> classInfo = ReflectionUtils.parseTypeDescriptor(typeDescriptor);
                if (classInfo.isEmpty() || classInfo.size() > 1) {
                    throw new RuntimeException("Illegal element_value class_info_index: " + typeDescriptor);
                }
                return classInfo.get(0);
            }
            case 'e': {//enum_constant_index
                final String typeDescriptor = (String) constantPool.get(input.readUnsignedShort());
                List<String> type = ReflectionUtils.parseTypeDescriptor(typeDescriptor);
                if (type.isEmpty() || type.size() > 1) {
                    throw new RuntimeException("Illegal element_value enum_constant_index: " + typeDescriptor);
                }
                final String constantName = (String) constantPool.get(input.readUnsignedShort());
                String enumObj = type.get(0) + "." + constantName;
                return enumObj;
            }
            default:return null;
        }
    }

    private String readRefString(final DataInputStream input, final List<Object> constantPool)
            throws IOException {
        final int index = input.readUnsignedShort();
        if(constantPool.get(index) instanceof Integer) {//indirect reference, like CONSTANT_Class,CONSTANT_String
            int temp = (int)constantPool.get(index);
            return (String) constantPool.get(temp);
        } else {
            return (String) constantPool.get(index);
        }
    }

    /**
     * 复用String对象
     * */
    private String intern(final String string) {
        if (string == null) {
            return null;
        }
        final String oldValue = internStringMap.putIfAbsent(string, string);
        return oldValue == null ? string : oldValue;
    }

    public ClassFileBinaryParser() {
        internStringMap = new ConcurrentHashMap<String, String>(128);
    }

    //缓存重复字符串对象
    private ConcurrentMap<String, String> internStringMap;
}
