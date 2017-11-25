import com.zjp.scanner.ClassFileBinaryParser;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 11/19/2017.
 */
public class BinaryParserTest {

    @Test
    public void parseRuntimeVisibleAnnotation() throws Exception {
        /*ClassFileBinaryParser parser = new ClassFileBinaryParser();
        System.err.println();

        parser.readClassInfoFromClassFileHeader(new FileInputStream(new File("D:\\faisco\\fast-classpath-scanner-zjp\\target\\test-classes\\com\\zjp\\TestController.class")),
                new ConcurrentHashMap<>());*/
        System.out.println(System.getProperty("java.class.path"));
    }
}
