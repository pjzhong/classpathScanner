
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;

public class SocketTest {

    public static void main(String[] args) {
        int port = 9001;
        ServerSocket server = null;
        try {
            // 创建ServerSocket服务
            server = new ServerSocket(port);
            System.out.println("ServerSocket服务启动成功，端口:"+port);
        } catch (IOException e) {
            System.out.println("ServerSocket服务启动失败，端口:"+port+" Error:"+e);
        }
        Socket socket = null;

        try {
            // 监听端口是否有连接（阻塞状态）
            socket = server.accept();
            System.out.println("端口:"+port+" 连接成功");

            // 获取client发送过来的数据
            BufferedReader cliIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("接收到client的数据:" + cliIn.readLine());

            // 构建printWriter对象, 用来返回数据给client
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            // 从控制台获取数据
            BufferedReader sytIn = new BufferedReader(new InputStreamReader(System.in));

            String line = sytIn.readLine();
            // 如果没有从控制台获取到end，则一直循环
            while(!"end".equals(line)){
                // 刷新缓存把数据返回给client
                writer.println(line);
                writer.flush();
                System.out.println("server返回数据:"+line);

                // 等待client发送数据
                System.out.println("接收到client的数据:"+cliIn.readLine());

                // 从控制台接收数据
                sytIn.readLine();
            }

            server.close();
            socket.close();
            cliIn.close();
            writer.close();
            sytIn.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
