package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * PiecewiseLinear1D - 2D problem where the fitness depends only on x
 *
 * The function is piecewise linear with a flat plateau:
 *
 *   F(x,y) = 10 - x,   0 <= x < 4    (decreasing from 10 to 6)
 *          = 6,         4 <= x <= 5   (flat PLATEAU)
 *          = 11 - x,   5 <  x <= 10  (decreasing from 6 to 1)
 *
 * Note: The function depends only on x, y is irrelevant.
 * The surface is therefore constant along every horizontal (y-axis) line.
 *
 * Domain: x, y ∈ [0, 10]
 * Minimum: F = 1 at x = 10 (right edge)
 * Plateau: F = 6 for x ∈ [4, 5]
 */
public class PiecewiseLinear1D extends DoubleProblem {

    /**
     * Constructor with default domain [0, 10] x [0, 10]
     */
    public PiecewiseLinear1D() {
        super("PiecewiseLinear1D", 2, 1, 1, 0);

        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 0.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 10.0));
    }

    @Override
    public double eval(double[] ds) {
        if (ds.length != 2) {
            throw new IllegalArgumentException("PiecewiseLinear1D is only defined for 2 dimensions");
        }

        double x = ds[0];

        // Region 1: 0 <= x < 4
        if (x < 4.0) {
            return 10.0 - x;
        }

        // Region 2: 4 <= x <= 5 (PLATEAU)
        if (x <= 5.0) {
            return 6.0;
        }

        // Region 3: 5 < x <= 10
        return 11.0 - x;
    }
}

