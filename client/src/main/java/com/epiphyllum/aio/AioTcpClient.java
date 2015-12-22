package com.epiphyllum.aio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AioTcpClient {
    public static JTextField jt=new JTextField();
    public static ConcurrentHashMap<String,AsynchronousSocketChannel> sockets =new ConcurrentHashMap<>();

    // 客户端静态对象
    static AioTcpClient me;

    // 异步通道管理器
    private AsynchronousChannelGroup asyncChannelGroup;

	// 构造函数初始化异步通道管理器
    public AioTcpClient() throws Exception {
        //创建线程池 && 异步通道管理器   
        ExecutorService executor = Executors.newFixedThreadPool(20);
        asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(executor);
    }
   
	// GBK解码器 
    private final CharsetDecoder decoder = Charset.forName("GBK").newDecoder();
    
    public void start(final String ip, final int port) throws Exception {
        // 启动20000个并发连接，使用20个线程的池子
        for (int i = 0; i < 2; i++) {
            try {
                //客户端socket.当然它是异步方式的。
                AsynchronousSocketChannel connector = null;
                if (connector == null || !connector.isOpen()) {
                    //从异步通道管理器处得到客户端socket
                    connector = AsynchronousSocketChannel.open(asyncChannelGroup);

					//  放入hashmap, 设置通道参数
                    sockets.putIfAbsent(String.valueOf(i), connector);
                    connector.setOption(StandardSocketOptions.TCP_NODELAY, true);
                    connector.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    connector.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

                    // 发起异步连接。这里的的connect原型是
                    // connect(SocketAddress remote, A attachment, CompletionHandler<Void,? super A> handler)
                    // 也就是它的CompletionHandler的A型参数是由这里的调用方法的第二个参数决定。
					// 即是connector。客户端连接器。
                    // V型为null  ==> Void
					//
                    connector.connect(new InetSocketAddress(ip, port), connector, new AioConnectHandler(i));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void work() throws Exception{
        AioTcpClient client = new AioTcpClient();
        client.start("localhost", 9008);
    }

    public void send() throws UnsupportedEncodingException{

		// 获取0号通道
        AsynchronousSocketChannel socket=sockets.get("0");

		// 组发送buffer
        String sendString=jt.getText();
        ByteBuffer clientBuffer=ByteBuffer.wrap(sendString.getBytes("UTF-8"));        

		// 发起异步写!!!!!
        socket.write(clientBuffer, clientBuffer, new AioSendHandler(socket));
    }
    public   void createPanel() {
        me=this;
        JFrame f = new JFrame("Wallpaper");
        f.getContentPane().setLayout(new BorderLayout());       
        
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));        
        JButton bt=new JButton("点我");
        p.add(bt);
        me=this;
        bt.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
               try {
                    me.send();
                 
                } catch (Exception ex) {
                    Logger.getLogger(AioTcpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
        });
        
        bt=new JButton("结束");
        p.add(bt);
        me=this;
        bt.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {                 
            }
        
        });
 
        f.getContentPane().add(jt,BorderLayout.CENTER);
        f.getContentPane().add(p, BorderLayout.EAST);
        
        f.setSize(450, 300);
        f.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo (null);
        f.setVisible (true);
    }
      
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {     
                AioTcpClient d = null;
                try {
                    d = new AioTcpClient();
                } catch (Exception ex) {
                    Logger.getLogger(AioTcpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                d.createPanel();
                try {
                    d.work();
                } catch (Exception ex) {
                    Logger.getLogger(AioTcpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
               
                 
            }
        });
    } 
}

