package com.sundy.mainsubreactor;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;

import com.sundy.mainsubreactor.util.CountUtil;

/**
 * 监听客户端连接，使用并发线程池{@link Executors#newSingleThreadExecutor()}实现,用于监听客户端的连接事件{@link SelectionKey#OP_ACCEPT}
 * @author Administrator
 *
 */
public class MainReactor implements Runnable {

	private Selector selector;
	private SubReactorThreadGroup subReactorThreadGroup;
	
	public MainReactor(SelectableChannel channel) {
		try {
			selector = Selector.open();
			channel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		subReactorThreadGroup = new SubReactorThreadGroup(4);
	}

	public void run() {
		System.out.println("main reactor is running。。。。");
		while(!Thread.interrupted()){
			try {
				int count = selector.select(1000);
				if(count==0){
					continue;
				}
				Set<SelectionKey> keys = selector.selectedKeys();
				for(Iterator<SelectionKey> it=keys.iterator();it.hasNext();){
					SelectionKey key = it.next();
					it.remove();
					if(key.isValid() && key.isAcceptable()){
						CountUtil.add();
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
						SocketChannel clientChannel = serverSocketChannel.accept();
						clientChannel.configureBlocking(false);
						subReactorThreadGroup.dispatch(clientChannel);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
