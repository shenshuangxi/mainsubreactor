package com.sundy.mainsubreactor;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * nio 线程组
 * @author Administrator
 *
 */
public class SubReactorThreadGroup {

	private static final AtomicInteger requestCount = new AtomicInteger(); //请求计数器
	private final int nioThreadCount; //线程池IO线程数量
	private static final int DEFAULT_NIO_THREAD_COUNT = 4;
	private SubReactorThread[] nioThreads;	//sub reactor 线程组
	private ExecutorService businessExecutorPool; //业务线程池
	
	public SubReactorThreadGroup(){
		this(DEFAULT_NIO_THREAD_COUNT);
	}
	
	public SubReactorThreadGroup(int threadCount) {
		if(threadCount < 1){
			threadCount = DEFAULT_NIO_THREAD_COUNT;
		}
		this.nioThreadCount = threadCount;
		businessExecutorPool = Executors.newFixedThreadPool(threadCount);
		this.nioThreads = new SubReactorThread[threadCount];
		for(int i=0;i<threadCount;i++){
			this.nioThreads[i] = new SubReactorThread(businessExecutorPool);
			this.nioThreads[i].start();
		}
		System.out.println("sub reactor nio 线程数为: "+threadCount);
	}

	public void dispatch(SocketChannel socketChannel) {
		if(socketChannel!=null){
			next().register(new NioTask(socketChannel, SelectionKey.OP_READ));
		}
		
	}

	private SubReactorThread next() {
		return nioThreads[requestCount.getAndIncrement() % nioThreadCount];
	}

}
