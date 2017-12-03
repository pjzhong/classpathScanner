package com.zjp;

import com.zjp.sterotype.AutoWired;
import com.zjp.sterotype.Controller;
import com.zjp.sterotype.RequestMapping;
import com.zjp.sterotype.RequestMethod;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 10/14/2017.
 */
@Controller("/index")
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


    @AutoWired(required = true)
    TestComponent component;

    public TestController() {
    }

    @RequestMapping(value = {"/index", "/hello"}, methods = RequestMethod.GET)
    public void index(Map<String, Integer> integerMap) {
    }

    @RequestMapping
    public void index2(String test, int b, List<String> list) {
    }

    public boolean index3() {
        return false;
    }

}
