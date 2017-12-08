import com.zjp.scanner.InterruptionChecker;
import com.zjp.utils.WorkQueue;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/12/7.
 */
public class WorkQueueTest {

    @Test
    public void test() throws Exception {
        InterruptionChecker checker = new InterruptionChecker();
        WorkQueue<Integer> streamWorkQueue = new WorkQueue<Integer>(
                queue -> {
                    for(Integer i = 0 ; i < 1000; i++) {
                        queue.addWorkUnit(i);
                    }
                },
                i -> System.out.println(Thread.currentThread() + "-" + i )
                ,checker
        );

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService fixed = Executors.newFixedThreadPool(threads);
        streamWorkQueue.start(fixed, threads);
        TimeUnit.SECONDS.sleep(2);
    }
}
