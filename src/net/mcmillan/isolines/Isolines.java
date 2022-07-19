package net.mcmillan.isolines;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.util.Arrays;

import javax.swing.JFrame;

import net.mcmillan.isolines.util.ColorArrays;

public class Isolines {

	private static JFrame frame;
	private static Canvas canvas = new Canvas();
		
	
	private static boolean drawGridlines = false;
	
	public static void init() {
		if (frame != null) throw new IllegalStateException("Can't init Isolines twice in same application!");
		frame = new JFrame("Isolines");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas.setIgnoreRepaint(true);
		frame.add(canvas);
		canvas.setSize(400,400);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		canvas.setBackground(Color.PINK);
		canvas.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_G:
					drawGridlines = !drawGridlines;
					break;
				case KeyEvent.VK_W:
				case KeyEvent.VK_UP:
					current -= (FieldGen.GRID_WIDTH-1); // Square space grid width
					break;
				case KeyEvent.VK_S:
				case KeyEvent.VK_DOWN:
					current += (FieldGen.GRID_WIDTH-1); // Square width
					break;
				case KeyEvent.VK_A:
				case KeyEvent.VK_LEFT:
					current -= 1;
					break;
				case KeyEvent.VK_D:
				case KeyEvent.VK_RIGHT:
					current += 1;
					break;
				}
			}
		});
		canvas.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				threshold += e.getPreciseWheelRotation()*0.01;
			}
		});
		canvas.createBufferStrategy(2);
	}

	public static void loop() {
		generate();
		draw();
	}
	private static void generate() {
		FieldGen.generate();
	}
	
	private static void draw() {
		BufferStrategy bs = canvas.getBufferStrategy();
		if (bs == null) throw new NullPointerException("BufferStrategy is null!");
		Graphics g = bs.getDrawGraphics();
		impl_draw(g);
		bs.show();
	}
	private static double current = 0;
	private static double threshold = 0.5;
	private static double thspd = 0.0;
	private static boolean thup = false;
	private static int[] getRGBFromValue(double v) {
		return ColorArrays.HSVtoRGB(new double[] { v, 1.0, 1.0 });
	}
	private static void impl_draw(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		update();
//		drawCells(g);
		double step = 1.0/20;
		for (double thresh=step;thresh<1;thresh+=step) {
			long start = System.currentTimeMillis();
			for (int sqx=0;sqx<FieldGen.GRID_WIDTH-1;sqx++) {
				for (int sqy=0;sqy<FieldGen.GRID_LENGTH-1;sqy++) {
					drawMarchedSquare(g,sqx,sqy,thresh,true,false);
				}
			}
			long took = System.currentTimeMillis()-start;
			if (took > 1) System.out.printf("[%.3f] %dms %n", thresh, took);
		}
//		{
//			if (current < 0) current += (FieldGen.GRID_WIDTH-1) * (FieldGen.GRID_HEIGHT-1);
//			if (current >= (FieldGen.GRID_WIDTH-1) * (FieldGen.GRID_HEIGHT-1)) current = 0; // Square coords
//			int curr = (int) (current);
//			final int sqx = curr % (FieldGen.GRID_WIDTH-1), sqy = curr / (FieldGen.GRID_WIDTH-1);
//			drawMarchedSquare(g, sqx, sqy, threshold, false);
//		}
		g.dispose();
	}
	
	private static void update() {
		threshold += thup ? thspd : - thspd;
		if (threshold >= 1) {
			threshold = 1-thspd;
			thup = false;
		} else if (threshold <= 0) {
			threshold = thspd;
			thup = true;
		}
	}
	
	private static void drawCells(Graphics g) {
		final int pw = canvas.getWidth() / FieldGen.GRID_WIDTH,
				ph = canvas.getHeight() / FieldGen.GRID_LENGTH,
				pwh = (int) (pw * 0.5),
				phh = (int) (ph * 0.5),
				pwq = (int) (pw * 0.25),
				phq = (int) (ph * 0.25);
		int px = 0, py = 0;
		double[][] data = FieldGen.data();
		double val = 0;
		for (int x=0;x<FieldGen.GRID_WIDTH;x++) {
			double[] col = data[x];
			py = 0;
			for (int y=0;y<FieldGen.GRID_LENGTH;y++) {
				val = col[y];
//				g.setColor(val>=threshold?Color.WHITE:Color.DARK_GRAY);
				setColor(g, getRGBFromValue(val));
				g.fillRect(px+pwq, py+phq, pwh, phh);
//				g.drawRect(px+pwq, py+phq, pwh, phh);
				py += ph;
			}
			px += pw;
		}
	}
	
//	private static void drawCurrent(Graphics g, int cx, int cy) {
//		final int[] fv = fieldSpaceToCanvasSpace(cx, cy);
//		g.setColor(Color.WHITE);
//		g.fillRect(fv[0] - 4, fv[1] - 4, 8, 8);
//		final int[] sqv = squareSpaceToCanvasSpace(cx, cy);
//		g.setColor(Color.CYAN);
//		g.fillRect(sqv[0] - 4, sqv[1] - 4, 8, 8);
//		final int[] hv = vertexSpaceToCanvasSpace(cx, cy, false);
//		final int[] vv = vertexSpaceToCanvasSpace(cx, cy, true);
//		g.setColor(Color.RED);
//		g.fillRect(hv[0] - 3, hv[1] - 3, 6, 6);
//		g.setColor(Color.GREEN);
//		g.fillRect(vv[0] - 3, vv[1] - 3, 6, 6);
//	}
	
	private static int[] fieldSpaceToCanvasSpace(int x, int y) {
		final int pw = canvas.getWidth() / FieldGen.GRID_WIDTH,
				ph = canvas.getHeight() / FieldGen.GRID_LENGTH,
				pwh = (int) (pw * 0.5),
				phh = (int) (ph * 0.5);
		return new int[] {x * pw + pwh, y * ph + phh };
	}
	private static int[] squareSpaceToCanvasSpace(int x, int y) {
		final int pw = canvas.getWidth() / FieldGen.GRID_WIDTH,
				ph = canvas.getHeight() / FieldGen.GRID_LENGTH;
		return new int[] {(x+1) * pw, (y+1) * ph };
	}
	private static int[] vertexSpaceToCanvasSpace(int x, int y, boolean vertical) { // equivalent of lerp(0.5,...) due to lack of threshold.
		final int pw = canvas.getWidth() / FieldGen.GRID_WIDTH,
				ph = canvas.getHeight() / FieldGen.GRID_LENGTH,
				phalf = (int) ((vertical?pw:ph) * 0.5);
		return new int[] {
				(x+1)*pw - (vertical ? phalf : 0 ), 
				(y+1)*ph - (vertical ? 0 : phalf )
			};
	}
	private static void setColor(Graphics g, int[] rgb) {
//		System.out.printf("R%dG%dB%d\n", rgb[0], rgb[1], rgb[2]);
		g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
	}
	
	// TopLeftBottomRight,HorVerHorVer
	private static final int[][] indiceLUT = new int[][] { // CLOCKWISE
		null, // 0: empty
		new int[] { 1,2 }, // 1: L-B
		new int[] { 2,3 }, // 2: B-R
		new int[] { 1,3 }, // 3: L-R (bottom filled)
		new int[] { 3,0 }, // 4: R-T
		new int[] { 1,0, 3,2 }, // 5: L-T, R-B
		new int[] { 2,0 }, // 6: B-T (right filled)
		new int[] { 1,0 }, // 7: L-T
		new int[] { 0,1 }, // 8: T-L
		new int[] { 0,2 }, // 9: T-B
		new int[] { 0,3, 2,1 }, // 10: T-R, B-L
		new int[] { 0,3 }, // 11: T-R
		new int[] { 3,1 }, // 12: R-L (top filled)
		new int[] { 3,2 }, // 13: R-B
		new int[] { 2,1 }, // 14: B-L
		null, // 15: solid
	};
	
	private static void drawMarchedSquare(Graphics g, int sqx, int sqy, double thresh, boolean lerped, boolean outline) {
		if (outline) {
			final int pw = canvas.getWidth() / FieldGen.GRID_WIDTH,
					ph = canvas.getHeight() / FieldGen.GRID_LENGTH;
			// Draw bounds
			final int[] fv = fieldSpaceToCanvasSpace(sqx, sqy);
			g.setColor(Color.CYAN);
			g.drawRect(fv[0], fv[1], pw, ph);
		}
		// Draw edges
		int[][][] edges = getEdges(sqx,sqy,thresh,lerped);
		if (edges != null) {
//			System.out.println("Vertices: "+Arrays.deepToString(edges));
			setColor(g, getRGBFromValue(thresh));
			for (int[][] e : edges) {
				g.drawLine(
						e[0][0], e[0][1], 
						e[1][0], e[1][1]);
			}
		}
	}
	private static int[][][] getEdges(int sqx, int sqy, double thresh, boolean lerped) {
		double[][] data = FieldGen.data();
		int state = 0;
		if (data[sqx][sqy+1] >= thresh) state += 1;
		if (data[sqx+1][sqy+1] >= thresh) state += 2;
		if (data[sqx+1][sqy] >= thresh) state += 4;
		if (data[sqx][sqy] >= thresh) state += 8;
//		System.out.println("State: " + state);
		int[] indices = indiceLUT[state];
		if (indices == null) return null;
		int[][] verts = new int[][] { null,null,null,null};
		boolean[] vertReq = new boolean[] { false,false,false,false };
		for (int i:indices) vertReq[i] = true;
		for (int v=0;v<4;v++) if (vertReq[v]) {
			try {
				verts[v]=lerped?getMiddleLerped(data, sqx, sqy, v, thresh):getMiddle(sqx,sqy,v);
			} catch (Exception e) {
				throw new IllegalArgumentException("State: " + state, e);
			}
		}
//		System.out.println("Indices: "+Arrays.toString(indices));
		int[][][] edges = new int[][][] { null, null }; // edges, verts, x/y
		int e = 0;
		for (int i=0;i<indices.length;i+=2) {
			edges[e++] = new int[][] {
				verts[indices[i]],
				verts[indices[i+1]],
			};
		}
		int [][][] trimmed = new int[e][][];
		System.arraycopy(edges, 0, trimmed, 0, e);
		return trimmed;
	}
	private static int[][] eoffs = new int[][] {
		new int[] { 0,0,1,0 }, // top
		new int[] { 0,0,0,1 }, // left
		new int[] { 0,1,1,1 }, // bottom
		new int[] { 1,0,1,1 }, // right
	};
	private static int[] getMiddle(int sqx, int sqy, int edge) {
		final int[] asqv = fieldSpaceToCanvasSpace(sqx+eoffs[edge][0], sqy+eoffs[edge][1]),
				bsqv = fieldSpaceToCanvasSpace(sqx+eoffs[edge][2], sqy+eoffs[edge][3]);
		return new int[] {
				(int) ((asqv[0]+bsqv[0])*0.5),
				(int) ((asqv[1]+bsqv[1])*0.5)
		};
	}
	private static int[] getMiddleLerped(double[][] data, int sqx, int sqy, int edge, double thresh) {
		final int[] av = new int[] { sqx+eoffs[edge][0], sqy+eoffs[edge][1] },
				    bv = new int[] { sqx+eoffs[edge][2], sqy+eoffs[edge][3] };
		final double adata = data[av[0]][av[1]], bdata = data[bv[0]][bv[1]];
		final int[] apxv = fieldSpaceToCanvasSpace(av[0], av[1]), 
				bpxv = fieldSpaceToCanvasSpace(bv[0], bv[1]);
		try {
			return fieldLerp(thresh,adata,bdata,apxv,bpxv);
		} catch (Exception e) {
			throw new IllegalArgumentException("Edge: " + edge + " | AV:" + Arrays.toString(av) + " | BV:" + Arrays.toString(bv), e);
		}
	}
	public static double getTVal(double thresh, double a, double b) {
		double t = (a-thresh) / (a-b);
		if (t > 1 || t < 0) throw new IllegalArgumentException(String.format("T:%f A:%f B:%f Got:%f", thresh, a, b, t));
		return t;
	}
	public static int[] fieldLerp(double thresh, double ad, double bd, int[] av, int[] bv) {
		return arrLerp(getTVal(thresh, ad, bd), av, bv);
	}
	public static int[] arrLerp(double t, int[] a, int[] b) {
		if (t > 1 || t < 0) throw new IllegalArgumentException(Double.toString(t));
		final double invt = 1-t;
		return new int[] {
				(int) (invt*a[0] + t * b[0]),
				(int) (invt*a[1] + t * b[1])
		};
	}
	
}
