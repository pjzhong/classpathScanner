import com.zjp.FastClassPathScanner;
import fai.comm.netkit.Client;
import fai.comm.netkit.Server;
import org.junit.Test;

public class ClassInDefaultPackage {

    @Test
    public void myScannerTest() {
        FastClassPathScanner scanner = new FastClassPathScanner("com.zjp", "jp.spring", "fai", "sbin")
                .matchSubClassOf(Server.class, (info, c) -> System.out.println("server | " + c ))
                .matchSubClassOf(Client.class, (info, c) -> System.out.println("client | " + c ));
        scanner.scan();
    }
}
