import com.zjp.FastClassPathScanner;

import com.zjp.beans.MethodInfo;
import org.junit.Test;

import java.lang.reflect.Method;


public class ClassInDefaultPackage {

    @Test
    public void myScannerTest() {
      /*  FastClassPathScanner scanner = new FastClassPathScanner("com.zjp", "jp.spring", "fai");
              *//*  .matchSubClassOf(Server.class, (info, c) -> System.out.println("server | " + c ))
                .matchSubClassOf(Client.class, (info, c) -> System.out.println("client | " + c ));*//*
        scanner.scan();*/

       /* for(int i = 1000; i < 10000;i ++) {
            System.out.println( (i * -1 ) & 16 );
        }*/

       /* Integer some_object = null;

        Map<String, String> hashMap = null;

        int index = some_object.hashCode() & ( hashMap.size() - 1);*/
        System.out.println(Integer.MAX_VALUE);

        FastClassPathScanner scanner = new FastClassPathScanner("com.example")
                .matchClassesWithMethodAnnotation(Deprecated.class, (info, c) -> {
                    System.out.println("----" + c + "------");
                    try {
                        Object ob  = c.newInstance();
                        for(Method method : ob.getClass().getMethods()) {
                           if(method.getDeclaringClass().equals(ob.getClass())) {
                               System.out.println(method);
                           }
                        }
                        System.out.println("\n-----------ClassInfo---------------");
                        for(MethodInfo methodInfo : info.getMethodInfoList()) {
                            System.out.println(methodInfo);
                        }
                    } catch (Exception e) {

                    }
                });
        scanner.scan();
    }
}
