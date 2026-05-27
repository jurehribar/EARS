package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.Problem;
import org.um.feri.ears.problems.misc.InvertedHemispheres;
import org.um.feri.ears.problems.misc.RastriginPlateau;
import org.um.feri.ears.problems.misc.SpherePlateau;
import org.um.feri.ears.problems.misc.TanhRadialStep;
import org.um.feri.ears.problems.unconstrained.*;
import org.um.feri.ears.problems.unconstrained.cec2005.F10;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestFill2D{
    
	public static String SCANLINE_DIR = "/Users/jurehribar/Dev/TestEARS/ScanLine/";
	public static String HEATMAP_DIR = "/Users/jurehribar/Dev/TestEARS/HeatMaps/";
	public static String[] RESOLUTIONS = {"10000"};

	public static void main(String args[]) throws Exception{
		//test1();
		//test1("boundary");
		test1("scanline");
	}
	
	public static void test1(String algorithm) throws Exception {
		int dimm = 2;
		DoubleProblem[] problems = {
				//new Rastrigin(dimm),
				//new Sphere(dimm),
				//new ShiftedCoupledSineBowl(dimm),
				//new InvertedHemispheres()
				//new TanhRadialStep()
				//new SpherePlateau()
				new RastriginPlateau()
		};

		File directory = new File(SCANLINE_DIR + "/");
		if (!directory.exists()) {
			directory.mkdir();
		}
		for (int i = 0; i < problems.length; i++) {
			System.out.println(problems[i].getName() + " (" + (i + 1) + "/" + problems.length + ")");
			Fill2D fill2D = new Fill2D(algorithm, HEATMAP_DIR + "/" + problems[i].getName() + "Compressed.object", true);
			fill2D.writeCompressedThisToFile(SCANLINE_DIR + "/" + problems[i].getName() + "Compressed.object");
			fill2D.writeFillMapValues(SCANLINE_DIR + "/" + problems[i].getName() + ".data");
			System.out.println(fill2D.toString());
		}

	}
	
	public static void test1() throws FileNotFoundException, IOException {
		int dimm = 2;
		DoubleProblem[] problems = {
				new Rastrigin(dimm),
				new Sphere(dimm),
		};
		
		for(int j = 0; j < RESOLUTIONS.length; j++) {
		    File directory = new File(HEATMAP_DIR);
		    if (!directory.exists()){
		        directory.mkdir();
		    }
			for(int i = 0; i < problems.length; i++) {
				System.out.println(problems[i].getName() + " (" + (i+1) + "/" + problems.length + ")");
				List<Double> step =  new ArrayList<Double>();
				step.add((problems[i].upperLimit.get(0) - problems[i].lowerLimit.get(0)) / Float.parseFloat(RESOLUTIONS[j]));
				step.add((problems[i].upperLimit.get(1) - problems[i].lowerLimit.get(1)) / Float.parseFloat(RESOLUTIONS[j]));
				HeatMap2D heatMap2D = new HeatMap2D(step, problems[i]);
				heatMap2D.writeCompressedThisToFile(HEATMAP_DIR+problems[i].getName()+"Compressed.object");
				heatMap2D.writeEvalsAndScript(HEATMAP_DIR, problems[i].getName());
				System.out.println(heatMap2D.toString());
			}
		}
	}
		
}
