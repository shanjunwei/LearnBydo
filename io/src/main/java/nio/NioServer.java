package nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
/**
 * NIO服务端
 */
public class NioServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        // 打开Channel服务端并绑定监听一个端口
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(8459));
        ssc.configureBlocking(false);

        // 打开多路复用器 并注册到 ServerSocketChannel 并监听连接事件
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务器已启动...");
        while (true) {
            // 如果没有事件发生 则select() 处于阻塞状态
            selector.select();
            // 发生事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            // 处理事件
            while (iterator.hasNext()) {
                // 拿到具体事件
                SelectionKey selectionKey = iterator.next();

                // 判断事件的类型
                if (selectionKey.isAcceptable()) {
                    System.out.println("客户端连接事件");
                    SocketChannel channel = ssc.accept();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                    if (channel.finishConnect()) {
                        System.out.println("完成连接");
                    }
                } else if (selectionKey.isReadable()) {
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int read = sc.read(buffer);
                    System.out.println("收到的消息:" + new String(buffer.array(), 0, read));

                    // 响应客户端  这里可有可无
                    buffer.clear();
                    buffer.put("已收到消息".getBytes());
                    // 将缓冲区各标志复位，因为向里面put了数据标志被改变要想从中读取数据发向服务器，就要复位
                    buffer.flip();
                    sc.write(buffer);
                    // 设置监听事件的集合 这里把写入事件加入
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    System.out.println("服务器向客户端发送已确认消息");

                } else if (selectionKey.isWritable()) {
                    System.out.println("触发往客户端写入数据事件");
                    // 发送完了就取消监听写事件，否则会无限循环触发写事件
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                }

                // 事件完成后，将其移除
                iterator.remove();
            }
        }
    }
}

