package net.mcmillan.test;

import java.nio.ByteBuffer;

import net.mcmillan.isolines.Isolines;

public class Test {

	public static void main(String args[]) {
		
		int[] ints = new int[] { 123, 6, 8, 9 };
		ByteBuffer byteBuf = ByteBuffer.allocate(Integer.BYTES * ints.length);
		for (int i=0;i<ints.length;i++) byteBuf.putInt(ints[i]);
		
		Isolines.init();
		
		final long FRAME_TIME = 16;
		long now = System.currentTimeMillis(), last = now - FRAME_TIME, d = 0;
		while (true) {
			now = System.currentTimeMillis();
			d = now - last;
			if (d >= FRAME_TIME) {
				last = now;
				Isolines.loop();
			}
		}
	}
}
