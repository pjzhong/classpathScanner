package com.zjp;

import com.zjp.sterotype.AutoWired;
import com.zjp.sterotype.Controller;
import com.zjp.sterotype.RequestMapping;

/**
 * Created by Administrator on 10/14/2017.
 */
@Controller
public class TestController {

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
