package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.*;

/**
 * RastriginPlateau - 2D Rastrigin function with square plateaus at extrema
 *
 * This function is based on the standard Rastrigin function but with small
 * square plateaus (0.1 x 0.1 by default) placed around:
 * - Local minima at integer coordinates (0,0), (1,0), (-1,0), etc.
 * - Local maxima at half-integer coordinates (0.5,0.5), (1.5,0.5), etc.
 *
 * Domain: x, y in [-5.12, 5.12]
 * Global minimum: f(0, 0) = 0
 *
 * Base function: f(x,y) = 20 + x² + y² - 10*cos(2πx) - 10*cos(2πy)
 */
public class RastriginPlateau extends DoubleProblem {

    private double plateauSize;
    private double halfWidth;

    /**
     * Constructor with default plateau size of 0.1
     */
    public RastriginPlateau() {
        this(0.1);
    }

    /**
     * Constructor with custom plateau size
     * @param plateauSize Width of the square plateau around each extremum
     */
    public RastriginPlateau(double plateauSize) {
        super("RastriginPlateau", 2, 1, 1, 0);
        this.plateauSize = plateauSize;
        this.halfWidth = plateauSize / 2.0;

        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -5.12));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 5.12));
    }

    /**
     * Evaluates the standard 2D Rastrigin function
     */
    private double rastrigin2d(double x, double y) {
        return 20.0 + x * x + y * y
               - 10.0 * cos(2.0 * PI * x)
               - 10.0 * cos(2.0 * PI * y);
    }

    @Override
    public double eval(double[] ds) {
        if (ds.length != 2) {
            throw new IllegalArgumentException("RastriginPlateau is only defined for 2 dimensions");
        }

        double x = ds[0];
        double y = ds[1];

        // Calculate base Rastrigin value
        double fitness = rastrigin2d(x, y);

        // Check for plateau at local minima (integer coordinates)
        double minX = round(x);
        double minY = round(y);
        boolean onMinPlateau = abs(x - minX) <= halfWidth && abs(y - minY) <= halfWidth;

        if (onMinPlateau) {
            // On a minimum plateau - use the value at the exact minimum point
            return rastrigin2d(minX, minY);
        }

        // Check for plateau at local maxima (half-integer coordinates)
        double maxX = floor(x) + 0.5;
        double maxY = floor(y) + 0.5;
        boolean onMaxPlateau = abs(x - maxX) <= halfWidth && abs(y - maxY) <= halfWidth;

        if (onMaxPlateau) {
            // On a maximum plateau - use the value at the exact maximum point
            return rastrigin2d(maxX, maxY);
        }

        // Not on any plateau - return normal Rastrigin value
        return fitness;
    }

    /**
     * Get the plateau size
     * @return plateau size (width of square plateau)
     */
    public double getPlateauSize() {
        return plateauSize;
    }
}

