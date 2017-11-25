import com.zjp.FastClassPathScanner;

import org.junit.Test;

import java.util.Map;

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
    }
}
