package zjp.matchprocessor;

import com.zjp.beans.ClassInfo;

/**
 * Created by Administrator on 11/4/2017.
 */
@FunctionalInterface
public interface ImplementClassMatchProcessor<T> {
    void processMatch(ClassInfo info, Class<? extends T> implementingCLass);
}
