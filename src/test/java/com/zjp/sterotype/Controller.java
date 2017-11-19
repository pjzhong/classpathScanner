package com.zjp.sterotype;

import java.lang.annotation.*;

/**
 * Created by Administrator on 10/14/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@FalseController
@Inherited
public @interface Controller {

    String value() default "";
}
