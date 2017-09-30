package com.sundy.mainsubreactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 维护{@link ServerSocketChannel}类，绑定服务端监听端口。然后将该通道注册到MainReactor中
 * @author Administrator
 *
 */
public class Acceptor implements Runnable {

	private static ExecutorService mainReactor = Executors.newSingleThreadExecutor();
	private final int port;
	
	public Acceptor(int port){
		this.port = port;
	}
	
	public void run() {
		ServerSocketChannel ssc = null;
		try {
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.bind(new InetSocketAddress(port));
			dispatch(ssc);
			System.out.println("服务端启动成功.......");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void dispatch(ServerSocketChannel ssc) {
		mainReactor.submit(new MainReactor(ssc));
	}

}
