package com.epiphyllum.aio;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup; 
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AIO异步socket通讯，分成 用于服务端的socekt与用于客户端的socket，当然这两者都是
 * 异步的。两者使用时，都用到了同样的异步通道管理器，异步通道管理器通过线程池管理。
 *    异步通道管理器，可以生成服务端socket与客户端socket。 * 
 *    使用服务端socket或客户端socket都需要一个操作处理器（CompletionHandler）
 * 
 * 当有信息时异步通道管理器会把 相关信息传递给操作作处理器。 * 
 *    操作处理器的方法是同一方法，但方法的参数是泛型，随着调用它的方法不同而改变。
 *    在AIO中，CompletionHandler这个操作处理器方法，是个泛型接口，当回调函数用。
 * 使用CompletionHandler的方法，约定是把该方法前一个参数实例传递给A型参数
 * （attachment），CompletionHandler的另一个参数将是存有该方法的使用情况的实例。
 * 
 */
public class AioTcpServer implements Runnable { 
    private AsynchronousChannelGroup asyncChannelGroup;  
    private AsynchronousServerSocketChannel listener;  
  
    public AioTcpServer(int port) throws Exception { 
        //创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(20); 
        //异步通道管理器
        asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor); 
        //创建 用在服务端的异步Socket.以下简称服务器socket。
        //异步通道管理器，会把服务端所用到的相关参数
        listener = AsynchronousServerSocketChannel.open(asyncChannelGroup).bind(new InetSocketAddress(port)); 
    } 
 
    public void run() { 
        try { 

            AioAcceptHandler acceptHandler = new AioAcceptHandler();
            //为服务端socket指定接收操作对象.accept原型是：
            //accept(A attachment, CompletionHandler<AsynchronousSocketChannel, ? super A> handler)
            //也就是这里的CompletionHandler的A型参数是实际调用accept方法的第一个参数
            //即是listener。另一个参数V，就是原型中的客户端socket
            listener.accept(listener, new AioAcceptHandler());  
            Thread.sleep(400000);   // 400秒后, finally会结束这个线程啊??????
        } catch (Exception e) { 
            e.printStackTrace(); 
        } finally { 
            System.out.println("finished server");
        } 
    } 
 
    public static void main(String... args) throws Exception { 
        AioTcpServer server = new AioTcpServer(9008); 
        new Thread(server).start(); 
    } 
}
