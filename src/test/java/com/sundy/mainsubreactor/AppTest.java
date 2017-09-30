package com.sundy.mainsubreactor;

import java.util.concurrent.Executors;

import com.sundy.mainsubreactor.util.CountUtil;

public class AppTest 
{
	public static void main(String[] args) {
		new Thread(new Acceptor(8090)).start();
		
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			public void run() {
				while(true){
					System.out.println("当前在线人数: "+CountUtil.get());
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
	}
}
