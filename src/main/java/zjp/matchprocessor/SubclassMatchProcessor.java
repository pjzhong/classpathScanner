package zjp.matchprocessor;

import com.zjp.beans.ClassInfo;

/**
 * Created by Administrator on 11/4/2017.
 */
@FunctionalInterface
public interface SubclassMatchProcessor<T> {
    public void processMatch(ClassInfo info, Class<? extends T> subclass);
}
