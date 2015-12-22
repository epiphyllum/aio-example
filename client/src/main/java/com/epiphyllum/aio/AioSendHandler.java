package com.epiphyllum.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;


public class AioSendHandler implements CompletionHandler<Integer,ByteBuffer>
{ 
    private AsynchronousSocketChannel socket; 

	// 构造函数, 注入通道
    public AioSendHandler(AsynchronousSocketChannel socket) { 
        this.socket = socket; 
    } 

    @Override
    public void completed(Integer i, ByteBuffer buf) {
		// 本次异步写, 写了>0个字节, 则再次发起异步写, 知道写入量为0
        if (i > 0) { 
            socket.write(buf, buf, this); 
        } 
		// 本次异步写失败(对方断线)
		else if (i == -1) { 
            try { 
                System.out.println("对端断线:" + socket.getRemoteAddress().toString()); 
                buf = null; 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 
        // 本次异步写没有写入字节 
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        System.out.println("cancelled"); 
    }
    
}

