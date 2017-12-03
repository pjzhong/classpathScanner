import com.zjp.FastClassPathScanner;

import com.zjp.scanner.ScanPathMatch;
import com.zjp.sterotype.Component;
import org.junit.Test;

import java.util.*;

public class ClassInDefaultPackage {

    @Test
    public void myScannerTest() {

/*        FastClassPathScanner scanner = new FastClassPathScanner("com")
                .matchSubClassOf(Object.class, (info, c) -> {
                    System.out.println(c);
                });
        scanner.scan(1);*/

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

        List<String> lists = new ArrayList<String>();
        lists.add("fasdfasd");
        lists.add("23");
            lists.add("er");
        lists.add("fasdasdffasd");
        lists.add("45345345");

        List<String> list = lists.subList(2, 4);

        lists.set(2, "zjp");
        lists.set(3, "zxcvzxcvzxcv");

        list.set(1, "pjz");

        System.out.println(list);
        System.out.println(lists);

    }
}
