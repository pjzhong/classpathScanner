import com.zjp.FastClassPathScanner;

import com.zjp.sterotype.Component;
import org.junit.Test;

public class ClassInDefaultPackage {

    @Test
    public void myScannerTest() {

        FastClassPathScanner scanner = new FastClassPathScanner("com.sun.xml.bind.v2.model.impl")
                .matchClassesWithAnnotation(Component.class, (info, c) -> {
                    System.out.println(c);
                });
        scanner.scan(1);

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
    }
}
