package com.sundy.mainsubreactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import com.sundy.mainsubreactor.util.CountUtil;

/**
 * nio 线程 负责处理nio read write
 * @author Administrator
 *
 */
public class SubReactorThread extends Thread {

	private Selector selector;
	private ExecutorService businessExecutorPool;
	private List<NioTask> taskList = new ArrayList<NioTask>();
	private ReentrantLock taskMainLock = new ReentrantLock();
	
	
	public SubReactorThread(ExecutorService businessExecutorPool) {
		try {
			this.selector = Selector.open();
			this.businessExecutorPool = businessExecutorPool;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void register(NioTask task) {
		if(task!=null){
			try {
				taskMainLock.lock();
				taskList.add(task);
			} finally{
				taskMainLock.unlock();
			}
		}
	}

	public void run() {
		while(!Thread.interrupted()){
			try {
				int count = selector.select(1000);
				if(count!=0){
					Set<SelectionKey> keys = selector.selectedKeys();
					for(Iterator<SelectionKey> it = keys.iterator();it.hasNext();){
						SelectionKey key = it.next();
						it.remove();
						SocketChannel clientChannel = null;
						try {
							if(key.isValid() && key.isWritable()){
								clientChannel = (SocketChannel) key.channel();
								ByteBuffer buf = (ByteBuffer) key.attachment();
								buf.flip();
								clientChannel.write(buf);
								clientChannel.register(selector, SelectionKey.OP_READ);
							}else if(key.isValid() && key.isReadable()){
								clientChannel = (SocketChannel) key.channel();
								ByteBuffer buf = ByteBuffer.allocate(1024);
								clientChannel.read(buf);
								dispatch(clientChannel,buf);
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("客户端主动断开连接。。。。。。。");
							clientChannel.close();
							CountUtil.derease();
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			if(!taskList.isEmpty()){
				try {
					taskMainLock.lock();
					for(Iterator<NioTask> it = taskList.iterator();it.hasNext();){
						NioTask task = it.next();
						it.remove();
						try {
							SocketChannel sc = task.getSc();
							if(task.getData()!=null){
								sc.register(selector, task.getOp(),task.getData());
							}else {
								sc.register(selector, task.getOp());
							}
						} catch (ClosedChannelException e) {
							e.printStackTrace();
						}
					}
				} finally{
					taskMainLock.unlock();
				}
			}
		}
	}

	private void dispatch(SocketChannel channel, ByteBuffer reqBuf) {
		businessExecutorPool.submit(new Handler(channel, reqBuf, this));
	}

	

}
