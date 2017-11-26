import com.zjp.beans.ClassInfoBuilder;
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
        ClassFileBinaryParser parser = new ClassFileBinaryParser();
        System.err.println();

        ClassInfoBuilder builder = parser.readClassInfoFromClassFileHeader(new FileInputStream(new File("C:\\Users\\Administrator\\Desktop\\TypeInfoSetImpl.class")),
                new ConcurrentHashMap<>());
        System.out.println(System.getProperty("java.class.path"));

        System.out.println((char)99);
    }
}
