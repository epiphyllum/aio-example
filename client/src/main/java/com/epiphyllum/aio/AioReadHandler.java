package com.epiphyllum.aio;

import java.io.IOException;
import java.nio.ByteBuffer; 
import java.nio.CharBuffer; 
import java.nio.channels.AsynchronousSocketChannel; 
import java.nio.channels.CompletionHandler; 
import java.nio.charset.CharacterCodingException; 
import java.nio.charset.Charset; 
import java.nio.charset.CharsetDecoder; 

// 读到的字节数 + attachment 
public class AioReadHandler implements CompletionHandler<Integer,ByteBuffer>
{ 
    private AsynchronousSocketChannel socket; 
    private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder(); 
 
    public AioReadHandler(AsynchronousSocketChannel socket) { 
        this.socket = socket; 
    } 

	// 
    public void cancelled(ByteBuffer attachment) { 
        System.out.println("cancelled"); 
    } 
 
 
    @Override
    public void completed(Integer i, ByteBuffer buf) { 
		// 读到数据, 则重新发起异步读
        if (i > 0) { 
            buf.flip(); // 切换模式
            try { 
				CharBuffer readMessage = decoder.decode(buf); // 从buf中decode字节到CharBuffer
                System.out.println("收到"+socket.getRemoteAddress().toString()+"的消息:" + readMessage);
                buf.compact(); // 
            } catch (CharacterCodingException e) { 
                e.printStackTrace(); 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
            // 重新发起异步读
            socket.read(buf, buf, this); 
        } 
        // 读失败
		else if (i == -1) { 
            try { 
                System.out.println("对端断线:" + socket.getRemoteAddress().toString()); 
                buf = null; 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 

		// 读到0个字节, 不再重新发起异步读了!!!!!!!!!!!!!!
    } 
 
    @Override
    public void failed(Throwable exc, ByteBuffer buf) { 
        System.out.println(exc); 
    } 

     
}

