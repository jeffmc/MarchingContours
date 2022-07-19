package net.mcmillan.isolines;

import java.util.Random;

import net.mcmillan.isolines.util.OpenSimplexNoise;

public class FieldGen {
	public static final int GRID_WIDTH = 25, GRID_LENGTH = 25;
	public static final OpenSimplexNoise noise = new OpenSimplexNoise(1999);
	
	private static double[][] data = new double[GRID_WIDTH][GRID_LENGTH];
	public static double[][] data() { return data; }
	
	public static double noise_time_speed = 0.0, noise_domain_scale = 2, noise_time = 0;
	public static double[] noise_velocity = new double[] { 0.0, 0.0 };
	public static double[] noise_position = new double[] { 0.0, 0.0 };
	
	public static void generate() {
//		generateRandomNoise();
		generateSimplexNoise();
//		generatePlane();
	}
	private static void generateRandomNoise() {
		Random r = new Random(1280);
		for (int x=0;x<GRID_WIDTH;x++) {
			for (int z=0;z<GRID_LENGTH;z++) {
//				double xx = (double)x/GRID_WIDTH,
//						zz = (double)z/GRID_LENGTH;
				data[x][z] = r.nextDouble();
			}
		}
	}
	private static void generatePlane() {
		for (int x=0;x<GRID_WIDTH;x++) {
			for (int z=0;z<GRID_LENGTH;z++) {
				double xx = (double)x/GRID_WIDTH,
						zz = (double)z/GRID_LENGTH;
				data[x][z] = Math.pow(xx, zz);
			}
		}
	}
	private static void generateSimplexNoise() {
		long start = System.currentTimeMillis();
		for (int d=0;d<2;d++) noise_position[d] += noise_velocity[d];
		noise_time += noise_time_speed;
		for (int x=0;x<GRID_WIDTH;x++) {
			double[] col = data[x];
			for (int y=0;y<GRID_LENGTH;y++) {
				double xx = ((double)x/GRID_WIDTH-0.5)*2, yy = ((double)y/GRID_LENGTH-0.5)*2;
				col[y] = (noise.eval(
						(noise_position[0]+xx) * noise_domain_scale, 
						(noise_position[1]+yy) * noise_domain_scale, 
						noise_time
					) + 1.0)/2;
			}
		}
		long finish = System.currentTimeMillis();
		long cost = finish - start;
		if (cost > 1) System.out.println("[Generate] "+(GRID_WIDTH*GRID_LENGTH)+" cells in "+cost+"ms");
	}
}
