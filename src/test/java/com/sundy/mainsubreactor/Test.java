package com.sundy.mainsubreactor;

import java.nio.ByteBuffer;

public class Test {

	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.put("123".getBytes());
		bb.put("爱神的箭".getBytes());
		bb.flip();
		System.out.println(new String(bb.array(),bb.position(),bb.limit()-bb.position()));
	}

}
