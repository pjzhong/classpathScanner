package com.zjp.sterotype;

import com.zjp.scanner.ScanPathMatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 10/14/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    int id() default 1;
    long slary() default 10000L;
    String value() default "zjp";
    ScanPathMatch match() default ScanPathMatch.WITHIN_WHITE_LISTED_PATH;
    Class<String> asdf() default String.class;
    String[] names() default {"zjp", "asdfa", "asdfas", "kstg"};
    RequestMethod[] methods() default {RequestMethod.GET, RequestMethod.POST};

    Controller[] controller() default {};
}
