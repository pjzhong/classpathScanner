import com.zjp.Abstract;
import com.zjp.FastClassPathScanner;
import com.zjp.beans.FieldInfo;
import com.zjp.sterotype.Component;
import com.zjp.sterotype.Controller;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClassInDefaultPackage {

    @Test
    public void myScannerTest() {
        FastClassPathScanner scanner = new FastClassPathScanner("com.zjp", "jp.spring")
                .matchClassesWithAnnotation(Controller.class,
                        (info, c) -> {
                            System.out.println(c);
                            for(FieldInfo fieldInfo : info.getFieldInfoList()) {
                                System.out.println(fieldInfo.getAnnotationNames());
                            }
                        })
                .matchClassesSubclassOf(Abstract.class, (info, c) -> System.out.println("SubClass of Abstract | " + c) );
        scanner.scan(1);
    }
}
