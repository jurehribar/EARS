package org.um.feri.analyse.attractionBasins;

import org.apache.commons.math3.util.Pair;

import java.io.*;
import java.util.List;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Fill2D implements Serializable{
	private static final long serialVersionUID = 7526472295622723149L;  // unique id - for serialization

	public Point[][] map;
	public List<Double> step; // steps for each dimension
	public String problemName;
	public List<Double> lowerBound; // lower bounds for each dimension
	public List<Double> upperBound; // higher bounds for each dimension

	private int minPlateauSize = 1; // N: minimum cells for a region to be considered a plateau


    public Fill2D() 
    { 
    	this.map = null;
        this.step = null; 
        this.problemName = null;
        this.lowerBound = null;
        this.upperBound = null;
    }
    
    public Fill2D(String alg, String inputPathHeatmap, boolean smoothBoundaries) throws FileNotFoundException, ClassNotFoundException, IOException 
    {
    	this(alg, inputPathHeatmap, smoothBoundaries, 5);
    }

    /**
     * @param minPlateauSize  N — minimum number of connected same-f cells to be considered a plateau.
     *                        N=1 → same behaviour as before (region.size() > 1, i.e. at least 2 cells).
     *                        N=5 → only regions with more than 5 cells are marked as plateaus.
     */
    public Fill2D(String alg, String inputPathHeatmap, boolean smoothBoundaries, int minPlateauSize) throws FileNotFoundException, ClassNotFoundException, IOException
    {
    	this.map = null;
        this.step = null; 
        this.problemName = null;
        this.lowerBound = null;
        this.upperBound = null;
        this.minPlateauSize = minPlateauSize;
    	System.out.println("Heatmap reading started.");
    	readCompressedHeatMapObject(inputPathHeatmap);
    	if(this.lowerBound == null || this.upperBound == null)
    		throw new IllegalArgumentException("Lower or upper bounds are null!");
    	if(step == null)
    		throw new IllegalArgumentException("Steps is null!");
    	if(problemName == null)
    		throw new IllegalArgumentException("ProblemName is null!");
    	System.out.println("Heatmap reading finished.");
        calculate(alg);
        System.out.println("Plateau detection started.");
        detectPlateaus();
        System.out.println("Plateau detection finished.");
        if(smoothBoundaries) {
        	System.out.println("Smoothing started.");
        	smooth();
        	System.out.println("Smoothing finished.");
        }
    }
	
	private void readCompressedHeatMapObject(String inputPath) throws FileNotFoundException, ClassNotFoundException, IOException {
		HeatMap2D read = new HeatMap2D();
		HeatMap2D heatMap = read.readCompressedFileToObject(inputPath);
		map = new Point[heatMap.evals.length][heatMap.evals[0].length];
		this.step = heatMap.step;
		this.lowerBound = heatMap.lowerBound;
		this.upperBound = heatMap.upperBound;
		this.problemName = heatMap.problemName;

		for(int i = 0; i < heatMap.evals.length; i++) {
			for(int j = 0; j < heatMap.evals[0].length; j++) {
				Point point = new Point();
				point.x1 = heatMap.x1s[i];
				point.x2 = heatMap.x2s[j];
				point.f = heatMap.evals[i][j];
				point.color = -1;
				point.plateau = 0;
				map[i][j] = point;
			}
		}
	}
	
	public void writeFillMapValues(String outputPath) throws Exception {
		if(this.map == null) {
			System.out.println("No adequete data to write.");
			return;
		}
		System.out.println("Writing fill map values started.");
		try (BufferedWriter dataWriter = new BufferedWriter(new FileWriter(outputPath))) {
			for (int i = 0; i < this.map.length; i++) {
				for (int j = 0; j < this.map[i].length; j++) {
					dataWriter.write(this.map[i][j].x1 + " " + this.map[i][j].x2 + " " + this.map[i][j].color+"\n");
				}	
			}
			dataWriter.close();
		} catch (Exception e) {
			throw e;
		}
		System.out.println("Writing fill map values finished.");
	}

	public void writeCompressedThisToFile(String outputPath) throws IOException, FileNotFoundException {
		if(this.map == null || this.step == null || this.lowerBound == null || this.upperBound == null) {
			System.out.println("No adequete data to write.");
			return;
		}
		System.out.println("Writing compressed Fill2D started.");
		try {
			GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(outputPath));
			ObjectOutputStream oos = new ObjectOutputStream(gos);
			oos.writeObject(this); 
			gos.close();
			oos.close();
		} catch (Exception e) {
			throw e;
		}
		System.out.println("Writing compressed Fill2D finished.");
	}
	
	public Fill2D readCompressedFileToObject(String inputPath) throws IOException, FileNotFoundException, ClassNotFoundException {
		System.out.println("Reading compressed Fill2D started.");
		ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(inputPath)));
		Fill2D read = (Fill2D) ois.readObject();
		ois.close();
		System.out.println("Reading compressed Fill2D finished.");
		return read;
	}
    
	public void makeBoundaries() {
		if(this.map == null)
			return;
		
		for(int i = 0; i < map.length; i++) {
			for(int j = 1; j < map[0].length-1; j+=1) {
				if(map[i][j-1].f < map[i][j].f && map[i][j+1].f < map[i][j].f) {
					map[i][j].color = 0;
				}
			}
		}
		for(int j = 0; j < map[0].length; j++) {
			for(int i = 1; i < map.length-1; i+=1) {
				if(map[i-1][j].f < map[i][j].f && map[i+1][j].f < map[i][j].f) {
					map[i][j].color = 0;
				}
			}
		}
		
		for(int j = 1; j < map[0].length-1; j+=1) {
			for(int i = 1; i < map.length-1; i+=1) {
				//if there is peak on current diagonal
				if(map[i-1][j-1].f < map[i][j].f && map[i+1][j+1].f < map[i][j].f) {
					// color it as a border
					map[i][j].color = 0;
				}
				if(map[i-1][j+1].f < map[i][j].f && map[i+1][j-1].f < map[i][j].f) {
					// color it as a border
					map[i][j].color = 0;
				}
			}
		}
	}

	public void makeBoundariesPlateau() {
		if(this.map == null)
			return;
		/*
		*  a _/
		*  b ‾\
		*  c /‾
		*  d \_
		*  e /\
		* */
		for(int i = 0; i < map.length; i++) {
			for(int j = 1; j < map[0].length-1; j+=1) {
				if((map[i][j-1].f == map[i][j].f && map[i][j].f < map[i][j+1].f) || //a
                   (map[i][j-1].f == map[i][j].f && map[i][j].f > map[i][j+1].f) || //b
                   (map[i][j-1].f < map[i][j].f && map[i][j].f == map[i][j+1].f) || //c
                   (map[i][j-1].f > map[i][j].f && map[i][j].f == map[i][j+1].f) || //d
				   (map[i][j-1].f < map[i][j].f && map[i][j].f > map[i][j+1].f))    //e
				{
					map[i][j].color = 0;
				}
			}
		}
		for(int j = 0; j < map[0].length; j++) {
			for(int i = 1; i < map.length-1; i+=1) {
				if((map[i-1][j].f == map[i][j].f && map[i][j].f < map[i+1][j].f) || //a
                   (map[i-1][j].f == map[i][j].f && map[i][j].f > map[i+1][j].f) || //b
                   (map[i-1][j].f < map[i][j].f && map[i][j].f == map[i+1][j].f) || //c
                   (map[i-1][j].f > map[i][j].f && map[i][j].f == map[i+1][j].f) || //d
                   (map[i-1][j].f < map[i][j].f && map[i][j].f > map[i+1][j].f))    //e
				{
					map[i][j].color = 0;
				}
			}
		}

		for(int j = 1; j < map[0].length-1; j+=1) {
			for(int i = 1; i < map.length-1; i+=1) {
				//if there is peak on current diagonal
				if(/*(map[i-1][j-1].f == map[i][j].f && map[i][j].f < map[i+1][j+1].f) || //a
                   (map[i-1][j-1].f == map[i][j].f && map[i][j].f > map[i+1][j+1].f) || //b
                   (map[i-1][j-1].f < map[i][j].f && map[i][j].f == map[i+1][j+1].f) || //c
                   (map[i-1][j-1].f > map[i][j].f && map[i][j].f == map[i+1][j+1].f) || //d */
                   (map[i-1][j-1].f < map[i][j].f && map[i][j].f > map[i+1][j+1].f))    //e
				{
					map[i][j].color = 0;
				}
				if(/*(map[i-1][j+1].f == map[i][j].f && map[i][j].f < map[i+1][j-1].f) || //a
                   (map[i-1][j+1].f == map[i][j].f && map[i][j].f > map[i+1][j-1].f) || //b
                   (map[i-1][j+1].f < map[i][j].f && map[i][j].f == map[i+1][j-1].f) || //c
                   (map[i-1][j+1].f > map[i][j].f && map[i][j].f == map[i+1][j-1].f) || //d */
                   (map[i-1][j+1].f < map[i][j].f && map[i][j].f > map[i+1][j-1].f))    //e
				{
					map[i][j].color = 0;
				}
			}
		}
	}

	public void calculate(String alg) throws IOException, FileNotFoundException, ClassNotFoundException {
		if(this.map == null)
			return;

    	System.out.println("Make boundaries started.");
		//makeBoundaries();
		makeBoundariesPlateau();
    	System.out.println("Make boundaries finished.");

		// We start from replacementColor 1.
		int replacementColor = 1;
		
		if(alg == "scanline") {
			System.out.println("Scan-line fill algorithm started.");
			// For each point
			for (int i = 0; i < this.map.length; i++) {
				for (int j = 0; j < map[i].length; j++) {
					// If not processed
					if (this.map[i][j].color == -1) {
						scanlinefill(i, j, replacementColor);
						replacementColor++;
					}
				}
			}
			System.out.println("Scan-line fill algorithm finished.");
		}
		else if(alg == "boundary") {
			System.out.println("Boundary fill algorithm started.");
			// For each point
			for (int i = 0; i < this.map.length; i++) {
				for (int j = 0; j < map[i].length; j++) {
					// If not processed
					if (this.map[i][j].color == -1) {
						boundaryfill(i, j, replacementColor);
						replacementColor++;
					}
				}
			}
			System.out.println("Boundary fill algorithm finished.");
		}
		else {
			System.out.println("Wrong argument. Use either: scanfill or floodfill");
			return;
		}
	}

	private void boundaryfill(int i, int j, int replacementColor) {
		int targetColor = this.map[i][j].color;

        Stack<Pair<Integer, Integer>> points = new Stack<Pair<Integer, Integer>>(); 
        points.push(new Pair<Integer, Integer>(new Pair<>(i, j)));

	 
	    while (!points.isEmpty())
	    {
			Pair<Integer, Integer> temp = points.pop();
			int x1 = temp.getFirst();
			int x2 = temp.getSecond();
	        if (x1< this.map.length && x1 >= 0 && x2 < this.map[x1].length && x2 >= 0)
	        {
	 
	            if (this.map[x1][x2].color == targetColor)
	            {
	            	this.map[x1][x2].color = replacementColor;
	            	points.push(new Pair<Integer, Integer>(new Pair<>(x1-1, x2)));
	            	points.push(new Pair<Integer, Integer>(new Pair<>(x1+1, x2)));
	            	points.push(new Pair<Integer, Integer>(new Pair<>(x1, x2-1)));
	                points.push(new Pair<Integer, Integer>(new Pair<>(x1, x2+1)));
	            }
	        }
	    }
	}

	private void scanlinefill(int i, int j, int replacementColor) {
		int targetColor = this.map[i][j].color;
		double currenFVal = map[i][j].f;

        Stack<Pair<Integer, Integer>> points = new Stack<Pair<Integer, Integer>>(); 

        points.push(new Pair<Integer, Integer>(new Pair<>(i, j)));

        while (!points.isEmpty()) {

			Pair<Integer, Integer> temp = points.pop();
			int x = temp.getFirst();
			int y1 = temp.getSecond();
			while(y1 >= 0 && this.map[x][y1].color == targetColor) {
				y1--;
			}
			y1++;
			boolean spanLeft = false;
			boolean spanRight = false;
			while(y1 < this.map[0].length && this.map[x][y1].color == targetColor) {
				this.map[x][y1].color = replacementColor;
				
				if(!spanLeft && x > 0 && this.map[x-1][y1].color == targetColor) {
                    points.push(new Pair<Integer, Integer>(x-1, y1));
                    spanLeft = true;
				}
				else if(spanLeft && x-1 >= 0 && this.map[x-1][y1].color != targetColor) {
                    spanLeft = false;
				}
				if(!spanRight && x < this.map.length-1 && this.map[x+1][y1].color == targetColor) {
                    points.push(new Pair<Integer, Integer>(x+1, y1));
                    spanRight = true;
				}
				else if(spanRight && x < this.map.length-1 && this.map[x+1][y1].color != targetColor) {
					spanRight = false;
				}
				y1++;
			}
		}
	}
	
	public void detectPlateaus() {
		if (this.map == null)
			return;

		int rows = this.map.length;
		int cols = this.map[0].length;
		int plateauCounter = 1;

		// Allocate primitive int arrays ONCE and reuse for every region.
		// At 10 000×10 000 each array is 400 MB — far cheaper than per-region
		// List<Pair> + HashSet<Long> which cost ~9 GB for a large flat region.
		//
		// Encoding: a cell (x, y) is stored as the single int  x * cols + y.
		int[] stackArr  = new int[rows * cols]; // flood-fill stack
		int[] regionArr = new int[rows * cols]; // cells collected in current region

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				// Only seed from unvisited, non-boundary cells
				if (this.map[i][j].plateau == 0 && this.map[i][j].color != 0) {
					double seedF = this.map[i][j].f;
					int stackTop  = 0;
					int regionSize = 0;

					// Pre-mark seed and push — setting plateau = -1 immediately
					// prevents any other neighbour from pushing the same cell again.
					this.map[i][j].plateau = -1;
					stackArr[stackTop++]    = i * cols + j;
					regionArr[regionSize++] = i * cols + j;

					while (stackTop > 0) {
						int cell = stackArr[--stackTop];
						int x    = cell / cols;
						int y    = cell % cols;

						// Helper: push neighbour (nx, ny) if it qualifies and is not yet claimed.
						// Pre-marking (plateau = -1) at push time ensures each cell is pushed at most once,
						// bounding the stack to at most rows*cols entries.
						if (x > 0) {
							int nx = x - 1;
							if (this.map[nx][y].plateau == 0 && this.map[nx][y].color != 0 && this.map[nx][y].f == seedF) {
								this.map[nx][y].plateau  = -1;
								stackArr[stackTop++]     = nx * cols + y;
								regionArr[regionSize++]  = nx * cols + y;
							}
						}
						if (x < rows - 1) {
							int nx = x + 1;
							if (this.map[nx][y].plateau == 0 && this.map[nx][y].color != 0 && this.map[nx][y].f == seedF) {
								this.map[nx][y].plateau  = -1;
								stackArr[stackTop++]     = nx * cols + y;
								regionArr[regionSize++]  = nx * cols + y;
							}
						}
						if (y > 0) {
							int ny = y - 1;
							if (this.map[x][ny].plateau == 0 && this.map[x][ny].color != 0 && this.map[x][ny].f == seedF) {
								this.map[x][ny].plateau  = -1;
								stackArr[stackTop++]     = x * cols + ny;
								regionArr[regionSize++]  = x * cols + ny;
							}
						}
						if (y < cols - 1) {
							int ny = y + 1;
							if (this.map[x][ny].plateau == 0 && this.map[x][ny].color != 0 && this.map[x][ny].f == seedF) {
								this.map[x][ny].plateau  = -1;
								stackArr[stackTop++]     = x * cols + ny;
								regionArr[regionSize++]  = x * cols + ny;
							}
						}
					}

					// Erode: count interior cells — those whose 4 orthogonal neighbours
					// are all in the current region (plateau == -1).
					// Replaces the HashSet<Long> lookup: O(1) direct array access.
					// Cells on the map border or adjacent to a non-region cell are excluded.
					int coreSize = 0;
					for (int k = 0; k < regionSize; k++) {
						int cell = regionArr[k];
						int x    = cell / cols;
						int y    = cell % cols;
						if (x > 0 && x < rows - 1 && y > 0 && y < cols - 1
								&& this.map[x - 1][y].plateau == -1
								&& this.map[x + 1][y].plateau == -1
								&& this.map[x][y - 1].plateau == -1
								&& this.map[x][y + 1].plateau == -1) {
							coreSize++;
						}
					}

				// Assign final plateau ID, or -2 as "visited but not a plateau" sentinel.
				// IMPORTANT: do NOT reset to 0 — that would cause the outer loop to
				// re-flood-fill the same region repeatedly, making the algorithm O(N^2).
				// -2 is skipped by the outer loop (plateau != 0) and by neighbours (plateau != 0).
				// Callers that read plateau values should treat negative values as "no plateau".
				int plateauId = (coreSize > minPlateauSize) ? plateauCounter++ : -2;
				for (int k = 0; k < regionSize; k++) {
					int cell = regionArr[k];
					this.map[cell / cols][cell % cols].plateau = plateauId;
				}
				}
			}
		}
		System.out.println("Plateaus detected: " + (plateauCounter - 1));
	}

	public void smooth() {
		if(this.map == null)
			return;

		for (int i = 1; i < this.map.length-1; i++) {
			for (int j = 1; j < map[i].length-1; j++) {
				if (this.map[i][j].color == 0) {
					
					int ri=1;
					while(i+ri< this.map.length-1 && this.map[i+ri][j].color==0)
						ri++;
					int li=1;
					while(i-li>0 && this.map[i-li][j].color==0)
						li++;
					
					int rj=1;
					while(j+rj< this.map[i].length-1 && this.map[i][j+rj].color==0)
						rj++;
					int lj=1;
					while(j-lj>0 && this.map[i][j-lj].color==0)
						lj++;
					
					int lu=1;
					while(i-lu>0 && j+lu < this.map[i].length-1 && this.map[i-lu][j+lu].color==0)
						lu++;
					int rd=1;
					while(i+rd < this.map.length-1 && j-rd>0 && this.map[i+rd][j-rd].color==0)
						rd++;
					
					int ld=1;
					while(i-ld>0 && j-ld>0 && this.map[i-ld][j-ld].color==0)
						ld++;
					int ru=1;
					while(i+ru<this.map.length-1 && j+ru<this.map[i].length-1 && this.map[i+ru][j+ru].color==0)
						ru++;
					
					// blacks
					
					if (this.map[i-li][j].color == this.map[i+ri][j].color) {
						this.map[i][j].color = this.map[i-li][j].color;
					}
					if (this.map[i][j-lj].color == this.map[i][j+rj].color) {
						this.map[i][j].color = this.map[i][j-lj].color;
					}
					if (this.map[i-lu][j+lu].color == this.map[i+rd][j-rd].color) {
						this.map[i][j].color = this.map[i-lu][j+lu].color;
					}
					if (this.map[i+ru][j+ru].color == this.map[i-ld][j-ld].color) {
						this.map[i][j].color = this.map[i-ld][j-ld].color;
					}
				}
			}
		}
	}

	public String toString() {
		if(this.step == null || this.lowerBound == null || this.upperBound == null) {
			System.out.println("Not properly set. Halted.");
			return "";
		}
		return "Fill2D ("+this.problemName+"):\n"
				+ "Lower bound: [" + this.lowerBound.get(0) + "," + this.lowerBound.get(1) + "]\n"
				+ "Upper bound: [" + this.upperBound.get(0) + "," + this.upperBound.get(1) + "]\n"
				+ "Step x1: "+ this.step.get(0) + "\n"
				+ "Step x2: "+ this.step.get(1) + "\n";
	}
}
