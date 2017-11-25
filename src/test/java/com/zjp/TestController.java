package com.zjp;

import com.zjp.sterotype.AutoWired;
import com.zjp.sterotype.Controller;
import com.zjp.sterotype.RequestMapping;

/**
 * Created by Administrator on 10/14/2017.
 */
@Controller
public class TestController {

    private static int num = 1;
    private static final String str = "12312312";
    private static long log = 1231231231L;
    private static final float fl = 123.0f;
    private static final char qwe = 'a';
    private static final byte aaa = 1;
    private static final Integer integer = 123;
    private static final boolean IamTrue = true;
    private static final Boolean IamFalse = false;


    @AutoWired
    TestComponent component;

    public TestController() {
    }

    @RequestMapping({"/index", "/hello"})
    public void index() {
    }

    @RequestMapping()
    public void index2() {
    }

    public boolean index3() {
        return false;
    }

}
