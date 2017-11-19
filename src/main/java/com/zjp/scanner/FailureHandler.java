package com.zjp.scanner;

/**
 * Created by Administrator on 10/28/2017.
 */
@FunctionalInterface
public interface FailureHandler {

    void onFailure(Throwable throwable);
}
