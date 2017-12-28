package com.zjp.matchprocessor;

import com.zjp.beans.ClassInfo;

/**
 * Created by Administrator on 2017/12/28.
 */
@FunctionalInterface
public interface MatchProcessor<T> {

    void processMatch(ClassInfo classInfo, Class<? extends T> target);
}
