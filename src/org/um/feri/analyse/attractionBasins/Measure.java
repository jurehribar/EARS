package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.Problem;
import org.um.feri.ears.problems.unconstrained.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

public class Measure {
	public static String[] RESOLUTIONS = {"10000"};
	public static String INPUT_DIR = "/Users/jurehribar/Dev/TestEARS/ScanLine/";
	public static String OUTPUT_DIR = "/Users/jurehribar/Dev/TestEARS/Pictures/";
	//public static final String OUTPUT_DIR = "E:/Mihael magistrska/eksperiment_v2/AttractionBasins/2D/";
	//public static final String INPUT_DIR = "E:/Mihael magistrska/eksperiment_v2/FloodFills/2D/";

	static public void main(String[] args) throws Exception {
		DoubleProblem[] problems = {
				//new Rastrigin(2),
	            //new Sphere(2)
				new ShiftedCoupledSineBowl(2),
		};
		drawAttractionBasins(problems);
	}
	
	public static void drawAttractionBasins(DoubleProblem[] problems) throws Exception {
		for(int j = 0; j < RESOLUTIONS.length; j++) {
			for(int i = 0; i < problems.length; i++) {
				System.out.println(problems[i].getName() + " (" + (i+1) + "/" + problems.length + ")");
				Fill2D tmp = new Fill2D();
				Fill2D fill2D = tmp.readCompressedFileToObject(INPUT_DIR+"/"+problems[i].getName()+"Compressed.object");
				int width = fill2D.map.length;
				int height = fill2D.map[0].length;
				
				// Count unique basins
				HashMap<Integer, Integer> basinCount = new HashMap<>();
				for(int x = 0; x < width; x++) {
					for(int y = 0; y < height; y++) {
						int basinId = fill2D.map[x][y].color;
						basinCount.put(basinId, basinCount.getOrDefault(basinId, 0) + 1);
					}
				}
				int numBasins = basinCount.size();
				System.out.println("Number of unique basins: " + numBasins);
				if(numBasins > 1000) {
					System.out.println("WARNING: Too many basins (" + numBasins + ") - colors will be hard to distinguish!");
				}

				HashMap<Integer, Color> colorMap = new HashMap<Integer, Color>();
				// Pre-assign boundary color
				colorMap.put(0, Color.BLACK);

				Random gen = new Random();
				// Use golden ratio for better color distribution
				float goldenRatio = 0.618033988749895f;
				float hue = gen.nextFloat();

				int fontSize = 14*width/500;
				Font myFont = new Font ("Times", Font.BOLD, fontSize);
				try {
					BufferedImage bi = new BufferedImage(width+200, height+200, BufferedImage.TYPE_INT_ARGB);
					Graphics2D ig2 = bi.createGraphics();
					
					int prevVal = 0;
					int x = 0;
					int y = 0;
					ig2.setFont(myFont);
					ig2.setColor(Color.WHITE);
					ig2.fillRect(0, 0, bi.getHeight(), bi.getWidth());
					for (x = 0; x < width; x++) {
						for (y = 0; y < height; y++) {
							int val = fill2D.map[x][y].color;
							
							if (prevVal != val) {
								prevVal = val;
								Color color;
								if (colorMap.containsKey(val)) {
									color = colorMap.get(val);
								}
								else {
									// Use HSV color space with golden ratio for better distribution
									hue += goldenRatio;
									hue = hue % 1.0f;
									// High saturation and value for vivid colors
									color = Color.getHSBColor(hue, 0.7f + gen.nextFloat() * 0.3f, 0.7f + gen.nextFloat() * 0.3f);
									colorMap.put(val, color);
								}
								ig2.setColor(color);
							}
							ig2.drawLine(x+180, height-y, x+180, height-y);
						}
					}
					ig2.setColor(Color.BLACK);
					ig2.drawString(problems[i].upperLimit.get(0).toString(), 0, fontSize);
					ig2.drawString("0", 0, y/2 + fontSize);
					ig2.drawString(problems[i].lowerLimit.get(0).toString(), 0, y);
					ig2.drawString(problems[i].lowerLimit.get(0).toString(), fontSize, y+fontSize);
					ig2.drawString("0", x/2, y+fontSize);
					ig2.drawString(problems[i].upperLimit.get(0).toString(), x-fontSize, y+fontSize);
					ImageIO.write(bi, "PNG", new File(OUTPUT_DIR+fill2D.problemName+".png"));
			
				} catch (IOException ie) {
					ie.printStackTrace();
				}
				System.out.println("Finished.");
			}
		}
	}
	
	public static String strround(double value) {
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		Double d = value + 1e-6;
	    return df.format(d);
	}
}
