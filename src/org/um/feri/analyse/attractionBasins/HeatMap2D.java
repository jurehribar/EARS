package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.Problem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * This class implements HeatMap for 2 dimensions
 * Author: Mihael Baketaric
 */

public class HeatMap2D implements Serializable{
	private static final long serialVersionUID = 7526472295622776147L;  // unique id - for serialization

	public double[][] evals; // fitness values - first index denotes x1 dimension, second index denotes x2 dimension
	public double[] x1s; // container for values of first independent variable: x1
	public double[] x2s; // container for values of second independent variable: x2
	
	public List<Double> step; // steps for each dimension
	public String problemName;
	public List<Double> lowerBound; // lower bounds for each dimension
	public List<Double> upperBound; // higher bounds for each dimension
	
    public HeatMap2D() 
    { 
    	this.evals = null;
    	this.x1s = null;
    	this.x2s = null;
        this.step = null; 
        this.problemName = null;
        this.lowerBound = null;
        this.upperBound = null;
    }
    
    public HeatMap2D(List<Double> step, DoubleProblem problem)
    { 
    	this.evals = null;
    	this.x1s = null;
    	this.x2s = null;
    	if(problem == null)
    		throw new IllegalArgumentException("Second parameter must not be null!");
    	if(step == null)
    		throw new IllegalArgumentException("First parameter must be greater than zero!");
        this.step = step; 
        this.problemName = problem.getName();
        this.lowerBound = new ArrayList<>(problem.lowerLimit);
        this.upperBound = new ArrayList<>(problem.upperLimit);
        System.out.println("Calculating HeatMap2D started.");
        calculate(problem);
        System.out.println("Calculating HeatMap2D finished.");
    }
    
	private void calculate(DoubleProblem problem) {
		if(problem == null || this.lowerBound == null || this.upperBound == null || this.step == null) {
			System.out.println("Problem is not properly set. Halted.");
			return;
		}
			
		int sizeX1 = (int)((this.upperBound.get(0) - this.lowerBound.get(0)) / this.step.get(0));
		int sizeX2 = (int)((this.upperBound.get(1) - this.lowerBound.get(1)) / this.step.get(1));
		evals = new double[sizeX1][sizeX2];
		x1s = new double[sizeX1];
		x2s = new double[sizeX2];

		int i = 0;
		for (double x1 = this.lowerBound.get(0); x1 <= this.upperBound.get(0) && i < sizeX1; x1 += this.step.get(0)) {
			int j = 0;
			for (double x2 = this.lowerBound.get(1); x2 <= this.upperBound.get(1) && j < sizeX2; x2 += this.step.get(1)) {
				evals[i][j] = problem.eval(new double[] { x1, x2 });
				x1s[i] = x1;
				x2s[j] = x2;
				j++;
			}
			i++;
		}
	}
	
    public void writeEvalsAndScript(String outputDir, String outputFilename) throws IOException {
		if(this.evals == null || this.x1s == null || this.x2s == null || this.lowerBound == null || this.upperBound == null) {
			System.out.println("No adequete data to write.");
			return;
		}
        System.out.println("Writing evals and script started.");
		try (BufferedWriter dataWriter = new BufferedWriter(new FileWriter(outputDir+outputFilename+".data"))) {
			for (int i = 0; i < this.evals.length; i++) {
				for (int j = 0; j < this.evals[i].length; j++) {
					dataWriter.write(x1s[i]+" "+x2s[j] +" "+evals[i][j]+"\n");
				}			
			}
			dataWriter.close();
		} catch (Exception e) {
			throw e;
		}
		try (BufferedWriter gnuplotWriter = new BufferedWriter(new FileWriter(outputDir+outputFilename+".gp"))) {
			gnuplotWriter.write("set xlabel \"x1\"\n");
			gnuplotWriter.write("set ylabel \"x2\"\n");
			gnuplotWriter.write("set term postscript eps enhanced color\n");
			gnuplotWriter.write("\n");
			gnuplotWriter.write("set xrange ["+this.lowerBound.get(0)+" : " + this.upperBound.get(0) + "]\n");
			gnuplotWriter.write("set yrange ["+this.lowerBound.get(1)+" : " + this.upperBound.get(1) + "]\n");
			gnuplotWriter.write("set output \""+outputDir+outputFilename+".eps\"\n");
			gnuplotWriter.write("plot \""+outputDir+outputFilename+".data"+"\" using 1:2:3 with points palette title \"\"\n");
			gnuplotWriter.close();
		} catch (Exception e) {
			throw e;
		}
        System.out.println("Writing evals and script finished.");
    }
    
	public String toString() {
		if(this.step == null || this.lowerBound == null || this.upperBound == null || this.evals == null || this.x1s == null || this.x2s == null) {
			System.out.println("Problem is not properly set. Halted.");
			return "";
		}
		return "HeatMap2D ("+this.problemName+"):\n"
				+ "Lower bound: [" + this.lowerBound.get(0) + "," + this.lowerBound.get(1) + "]\n"
				+ "Upper bound: [" + this.upperBound.get(0) + "," + this.upperBound.get(1) + "]\n"
				+ "Step x1: "+ this.step.get(0) + "\n"
				+ "Step x2: "+ this.step.get(1) + "\n"
				+ "Size x1: " + this.x1s.length +"\n"
				+ "Size x2: " + this.x2s.length +"\n";
	}

	public void writeCompressedThisToFile(String outputPath) throws IOException, FileNotFoundException {
		if(this.step == null ||this.lowerBound == null || this.upperBound == null || this.evals == null || this.x1s == null || this.x2s == null) {
			System.out.println("No adequete data to write.");
			return;
		}
        System.out.println("Writing compressed HeatMap2D started.");
		try {
			GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(outputPath));
			ObjectOutputStream oos = new ObjectOutputStream(gos);
			oos.writeObject(this); 
			gos.close();
			oos.close();
		} catch (Exception e) {
			throw e;
		}
        System.out.println("Writing compressed HeatMap2D finished.");
	}
	
	public HeatMap2D readCompressedFileToObject(String inputPath) throws IOException, FileNotFoundException, ClassNotFoundException {
        System.out.println("Reading compressed HeatMap2D started.");
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(inputPath)));
		HeatMap2D read = (HeatMap2D) ois.readObject();
		ois.close();
        System.out.println("Reading compressed HeatMap2D finished.");
		return read;
	}

}
