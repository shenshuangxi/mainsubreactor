package com.sundy.mainsubreactor.util;

import java.util.concurrent.atomic.AtomicInteger;

public class CountUtil {

	private static AtomicInteger count = new AtomicInteger();
	
	public static int add(){
		return count.incrementAndGet();
	}
	
	public static int derease(){
		return count.decrementAndGet();
	}
	
	public static int get(){
		return count.get();
	}
	
}
