package com.epiphyllum.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer; 
import java.nio.channels.AsynchronousSocketChannel; 
import java.nio.channels.CompletionHandler; 
import java.nio.charset.CharacterCodingException; 
import java.nio.charset.Charset; 
import java.nio.charset.CharsetDecoder; 
import java.util.logging.Level;
import java.util.logging.Logger;

// 这里的参数型号，受调用它的函数决定。这里是受客户端socket.read调用
// Integer代表读了多少个字节
// ByteBuffer代表attachment
public class AioReadHandler implements CompletionHandler<Integer,ByteBuffer> {
    private AsynchronousSocketChannel socket; 
    private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();  
    public  String msg;

    // 构造函数, 将通道注入
    public AioReadHandler(AsynchronousSocketChannel socket) { 
        this.socket = socket; 
    }     
    
    @Override
    public void completed(Integer i, ByteBuffer buf) { 
	    // 读到的字节数 > 0 
        if (i > 0) { 
            buf.flip();   // 进入读走模式
            try { 
				// 将buf数据转成字符串
                msg = decoder.decode(buf).toString();
                System.out.println("收到" + socket.getRemoteAddress().toString() + "的消息:" + msg); 
                buf.compact();  // compact buf  --- 挑战ByteBuffer
            } catch (CharacterCodingException e) { 
                e.printStackTrace(); 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 

			// 关键:  尽快发起下一次异步读
            // 第一个参数为读到哪里
            // 第二个参数为attachment
            // 第三个参数为CompletionHandler, 就是自己
            socket.read(buf, buf, this); 

            // 将读到的数据echo回客户端
            try {
        		String sendString="服务器回应,你输出的是:"+msg;
		        ByteBuffer clientBuffer=ByteBuffer.wrap(sendString.getBytes("UTF-8"));        
				// 发起异步写
                // 第一个参数为写的buffer
                // 第二个参数为attachment
                // 第三个参数为CompletionHandler,
       		    socket.write(clientBuffer, clientBuffer, new AioWriteHandler(socket));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AioReadHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        // 读失败, 就是客户端断开了
        else if (i == -1) { 
            try { 
                System.out.println("客户端断线:" + socket.getRemoteAddress().toString()); 
                buf = null; 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 
        // 读到0字节， 就不再发起异步读了!!!!
    } 

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
         System.out.println("cancelled"); 
    }
 
}

