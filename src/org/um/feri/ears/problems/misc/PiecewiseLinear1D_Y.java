package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * PiecewiseLinear1D_Y - 2D problem where the fitness depends only on y
 *
 * This is the y-flipped version of PiecewiseLinear1D.
 * The function is piecewise linear with a flat plateau:
 *
 *   F(x,y) = 10 - y,   0 <= y < 4    (decreasing from 10 to 6)
 *          = 6,         4 <= y <= 5   (flat PLATEAU)
 *          = 11 - y,   5 <  y <= 10  (decreasing from 6 to 1)
 *
 * Note: The function depends only on y, x is irrelevant.
 * The surface is therefore constant along every vertical (x-axis) line.
 *
 * Domain: x, y ∈ [0, 10]
 * Minimum: F = 1 at y = 10 (top edge)
 * Plateau: F = 6 for y ∈ [4, 5]
 */
public class PiecewiseLinear1D_Y extends DoubleProblem {

    /**
     * Constructor with default domain [0, 10] x [0, 10]
     */
    public PiecewiseLinear1D_Y() {
        super("PiecewiseLinear1D_Y", 2, 1, 1, 0);

        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 0.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 10.0));
    }

    @Override
    public double eval(double[] ds) {
        if (ds.length != 2) {
            throw new IllegalArgumentException("PiecewiseLinear1D_Y is only defined for 2 dimensions");
        }

        double y = ds[1];

        // Region 1: 0 <= y < 4
        if (y < 4.0) {
            return 10.0 - y;
        }

        // Region 2: 4 <= y <= 5 (PLATEAU)
        if (y <= 5.0) {
            return 6.0;
        }

        // Region 3: 5 < y <= 10
        return 11.0 - y;
    }
}

