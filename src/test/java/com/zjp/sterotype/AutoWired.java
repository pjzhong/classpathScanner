package com.zjp.sterotype;

import com.zjp.Abstract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 10/14/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AutoWired {
    boolean required() default true;
    Class<? extends Abstract> target();
}
