package com.zjp.beans;

import com.zjp.scanner.ScanSpecification;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/22.
 */
public class ClassGraphBuilder {

    public ClassGraph build() {
        Map<String, ClassInfo> infoMap = new HashMap<String, ClassInfo>((int)(builders.size() * 1.2), 0.9F);
        for(ClassInfoBuilder builder : builders) {
            builder.build(infoMap);
        }

        return new ClassGraph(specification, infoMap);
    }

    ClassGraphBuilder(ScanSpecification specification, Collection<ClassInfoBuilder> builders) {
        this.specification = specification;
        this.builders = builders;
    }

    private final ScanSpecification specification;
    private final Collection<ClassInfoBuilder> builders;
}
