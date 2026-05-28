package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.Problem;
import org.um.feri.ears.problems.misc.*;
import org.um.feri.ears.problems.unconstrained.*;
import org.um.feri.ears.problems.unconstrained.cec2005.F10;
import org.um.feri.ears.problems.unconstrained.cec2005.F16;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TestHeatMap2D{
    
	public static String OUTPUT_DIR = "/Users/jurehribar/Dev/TestEARS/HeatMaps/";
	public static String[] RESOLUTIONS = {"10000"};

	public static void main(String[] args) throws Exception{
		test1(); // Calculates heat maps for each resolution in RESOLUTIONS and makes a new directories within OUTPUT_DIR - Takes some time ~30mins
	}
	
	public static void test1() throws FileNotFoundException, IOException {
		int dimm = 2;
		DoubleProblem[] problems = {
				//new Rastrigin(dimm),
				//new Sphere(dimm),
				//new ShiftedCoupledSineBowl(dimm),
				//new InvertedHemispheres()
				//new TanhRadialStep()
				//new SpherePlateau()
				//new RastriginPlateau()
				//new PiecewiseLinearPlateau()
				new PiecewiseLinear1D_Y()
		};
		
		for(int j = 0; j < RESOLUTIONS.length; j++) {
		    File directory = new File(OUTPUT_DIR);
		    if (!directory.exists()){
		        directory.mkdir();
		    }
			for(int i = 0; i < problems.length; i++) {
				System.out.println(problems[i].getName() + " (" + (i+1) + "/" + problems.length + ")");
				List<Double> step =  new ArrayList<Double>();
				step.add((problems[i].upperLimit.get(0) - problems[i].lowerLimit.get(0)) / Float.parseFloat(RESOLUTIONS[j]));
				step.add((problems[i].upperLimit.get(1) - problems[i].lowerLimit.get(1)) / Float.parseFloat(RESOLUTIONS[j]));
				HeatMap2D heatMap2D = new HeatMap2D(step, problems[i]);
				heatMap2D.writeCompressedThisToFile(OUTPUT_DIR+problems[i].getName()+"Compressed.object");
				heatMap2D.writeEvalsAndScript(OUTPUT_DIR, problems[i].getName());
				System.out.println(heatMap2D.toString());
			}
		}
	}
		
}
