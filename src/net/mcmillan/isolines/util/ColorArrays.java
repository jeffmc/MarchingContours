package net.mcmillan.isolines.util;

// Any double/float array values clamped from 0-1,
// Any int array values clamped 0-255(360 for hue)

public class ColorArrays {

	public static final int RGB_MIN = 0, RGB_MAX = 255, HUE_MIN = 0, HUE_MAX = 360,
			SAT_MIN = 0, SAT_MAX = 255, VAL_MIN = 0, VAL_MAX = 255;
	
	public static final double doubleSixth = 1.0/6.0;
	// https://www.rapidtables.com/convert/color/hsv-to-rgb.html
	public static int[] HSVtoRGB(final double[] hsv) {
		final double hueDiv = hsv[0]/doubleSixth, C = hsv[2] * hsv[1], m = hsv[2] - C,
				X = C * (1 - Math.abs(hueDiv%2-1));
		final int hueBin = (int) hueDiv;
		final double[] rgbp;
		switch (hueBin%6) {
		case 0: // 0-60
			rgbp = new double[] { C,X,0 };
			break;
		case 1: // 60-120
			rgbp = new double[] { X,C,0 };
			break;
		case 2: // 120-180
			rgbp = new double[] { 0,C,X };
			break;
		case 3: // 180-240
			rgbp = new double[] { 0,X,C };
			break;
		case 4: // 240-300
			rgbp = new double[] { X,0,C };
			break;
		case 5: // 300-360
			rgbp = new double[] { C,0,X };
			break;
		default:
			throw new IllegalStateException("Broken maths!");
		}
		final int[] rgb = new int[3];
		for (int i=0;i<3;i++) 
			rgb[i] = (int) ((rgbp[i]+m)*255);
		return rgb;
	}
	public static final int intSixth = 360/6;
	public static int[] HSVtoRGB(final int[] hsv) {
		return HSVtoRGB(new double[] {
				((double)hsv[0] / HUE_MAX),
				((double)hsv[1] / SAT_MAX),
				((double)hsv[2] / VAL_MAX),
		});
	}
	public static double[] RGBtoHSV(final int[] rgb) {
		final double[] rgbp = new double[3];
		for (int i=0;i<3;i++) rgbp[i] = (double)rgb[i]/RGB_MAX;
		final double Cmax = Math.max(Math.max(rgbp[0], rgbp[1]), rgbp[2]),
				Cmin = Math.min(Math.min(rgbp[0], rgbp[1]), rgbp[2]),
				delta = Cmax - Cmin;
		if (delta == 0) return new double[] { 0, 0, Cmax };
		double h = doubleSixth;
		if (Cmax == rgbp[0]) {
			h *= ((rgbp[1]-rgbp[2])/delta)%6;
		} else if (Cmax == rgbp[1]) {
			h *= ((rgbp[2]-rgbp[0])/delta)+2;
		} else if (Cmax == rgbp[2]) {
			h *= ((rgbp[0]-rgbp[1])/delta)+4;
		}
		return new double[] { h, Cmax==0?0:delta/Cmax, Cmax };
	}
	
	public static int[] HSVtoInt(final double[] hsv) {
		return new int[] {
				(int) (hsv[0] * HUE_MAX),
				(int) (hsv[1] * SAT_MAX),
				(int) (hsv[2] * VAL_MAX),
		};
	}
	public static double[] RGBtoDouble(final int[] rgb) {
		return new double[] {
				(double)rgb[0]/RGB_MAX,
				(double)rgb[1]/RGB_MAX,
				(double)rgb[2]/RGB_MAX,
		};
	}
	
}
