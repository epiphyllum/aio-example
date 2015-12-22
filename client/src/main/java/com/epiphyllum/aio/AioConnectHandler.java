package com.epiphyllum.aio;

import java.util.concurrent.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

// 异步连接返回值为Void
// attachment为连接好的通道
public class AioConnectHandler implements CompletionHandler<Void,AsynchronousSocketChannel>
{
    private Integer content = 0;
    
    public AioConnectHandler(Integer value){
        this.content = value;
    }
 
    @Override
    public void completed(Void attachment, AsynchronousSocketChannel connector) { 
        try {  
			// 向连接号的通道发起异步写????????????????????
            connector.write(
				ByteBuffer.wrap(String.valueOf(content).getBytes())
			).get();

			// 向连接好的通道发起异步读
            startRead(connector); 
        } catch (ExecutionException e) { 
            e.printStackTrace(); 
        } catch (InterruptedException ep) { 
            ep.printStackTrace(); 
        } 
    } 
 
    @Override
    public void failed(Throwable exc, AsynchronousSocketChannel attachment) { 
        exc.printStackTrace(); 
    } 
    
    public void startRead(AsynchronousSocketChannel socket) { 
        //read的原型是
        //read(ByteBuffer dst, A attachment,
        //    CompletionHandler<Integer,? super A> handler) 
        //即它的操作处理器，的A型，是实际调用read的第二个参数，即clientBuffer。
        // V型是存有read的连接情况的参数
        ByteBuffer clientBuffer = ByteBuffer.allocate(1024); 
        socket.read(clientBuffer, clientBuffer, new AioReadHandler(socket)); 
        try { 
            
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
    }
 
}

