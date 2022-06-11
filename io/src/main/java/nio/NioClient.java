package nio;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


public class NioClient {

    public static void main(String[] args) throws IOException {
        //打开选择器
        Selector selector = Selector.open();
        //打开通道
        SocketChannel socketChannel = SocketChannel.open();
        //配置非阻塞模型
        socketChannel.configureBlocking(false);
        //连接远程主机
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8459));
        //注册事件
        socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ );

        //循环处理
        new Thread(() -> {
            while (true) {
                try {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isConnectable()) {
                            //连接建立或者连接建立不成功
                            SocketChannel channel = (SocketChannel) key.channel();
                            //完成连接的建立
                            if (channel.finishConnect()) {
                                System.out.println("完成连接");
                            }
                        } else if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.clear();
                            channel.read(buffer);
                            System.out.println("客户端收到消息:" + new String(buffer.array()));
                            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                        } else if (key.isWritable()) {
                            System.out.println("客户端向服务端写入数据");
                            // 设置监听事件的集合 这里把写入事件加入
                            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                        }
                        iter.remove();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入要发送的字符串");
            String str = scanner.nextLine();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(str.getBytes());
            buffer.flip();
            socketChannel.write(buffer);
        }
    }
}
