package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Smooth Cosine Plateaus Problem
 *
 * Based on g(x,y) = cos(x)*cos(y), with flat plateaus created by
 * clamping the function at threshold c:
 *
 *   F(x,y) = c,              if cos(x)*cos(y) >= c
 *           = cos(x)*cos(y), otherwise
 *
 * Equivalently:  F(x,y) = min(cos(x)*cos(y), c)
 *
 * The plateau boundary follows the level set g(x,y) = c, giving a
 * C1-smooth join between the plateau and the cosine surface (the
 * gradient of g is continuous at the boundary).
 *
 * Domain:   x, y in [-2*pi, 2*pi]  (contains 3x3 = 9 cosine peaks)
 * Global maximum: F = c  (flat plateau regions near each cosine peak)
 * Global minimum: F = -1 (at (pi + 2*pi*m, 2*pi*n) and (2*pi*m, pi + 2*pi*n))
 *
 * Recommended parameter: c = 0.9
 *
 * Tip: vary c to control plateau size:
 *   c = 0.95 -> small tight plateaus
 *   c = 0.80 -> medium plateaus
 *   c = 0.60 -> large plateaus, nearly touching
 */
public class CosinePlateausSmooth extends DoubleProblem {

    private final double c;  // threshold / plateau height

    /**
     * Constructor with default threshold c = 0.9
     */
    public CosinePlateausSmooth() {
        this(0.9);
    }

    /**
     * Constructor with custom threshold.
     *
     * @param c threshold value in (0, 1]; plateau where cos(x)*cos(y) >= c
     */
    public CosinePlateausSmooth(double c) {
        super("CosinePlateausSmooth", 2, 2, 1, 0);
        if (c <= 0.0 || c > 1.0)
            throw new IllegalArgumentException("Threshold c must be in (0, 1], got: " + c);
        this.c = c;

        double bound = 2.0 * Math.PI;
        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -bound));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions,  bound));
    }

    @Override
    public double eval(double[] x) {
        double g = Math.cos(x[0]) * Math.cos(x[1]);
        return Math.min(g, c);
    }

    /**
     * @return the plateau threshold c
     */
    public double getC() {
        return c;
    }
}

