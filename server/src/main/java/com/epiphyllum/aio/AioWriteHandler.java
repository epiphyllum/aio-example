package com.epiphyllum.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

// 写CompletionHandler的
// Integer:  写了多少个字节
// ByteBuffer:  Attachment
//
public class AioWriteHandler implements CompletionHandler<Integer,ByteBuffer>
{ 
    private AsynchronousSocketChannel socket; 

	// 构造函数， 注入socket 
    public AioWriteHandler(AsynchronousSocketChannel socket) { 
        this.socket = socket; 
    } 

    @Override
    public void completed(Integer i, ByteBuffer buf) {
		// 写入的字节数 > 0
        if (i > 0) { 
			// 重新发起异步写!!!!!
            socket.write(buf, buf, this); 
        } 
	    // 写失败
        else if (i == -1) { 
            try { 
                System.out.println("对端断线:" + socket.getRemoteAddress().toString()); 
                buf = null; 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 

		// 写如数据为0， 就没有发起异步写了, 关键!!!!!!!
        
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        System.out.println("cancelled"); 
    }
    
}

