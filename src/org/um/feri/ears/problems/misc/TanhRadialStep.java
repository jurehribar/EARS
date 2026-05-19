package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Tanh Radial Step Problem
 *
 * A 2D smooth radial valley function defined as:
 * f(x,y) = -h * (1/2) * (1 - tanh(k * (sqrt(x^2 + y^2) - r)))
 *
 * This creates a smooth radial depression/valley that transitions to zero
 * using a hyperbolic tangent function.
 *
 * Domain: x,y ∈ [-10, 10]
 * Global optimum: (0, 0) with f ≈ -h (approaches -h as distance → 0)
 *
 * Parameters:
 * - h: Height parameter (depth of valley), default = 10
 * - k: Steepness parameter (larger = sharper transition), default = 2
 * - r: Transition radius (where f = -h/2), default = 3
 */
public class TanhRadialStep extends DoubleProblem {

    private final double h;  // height/depth parameter
    private final double k;  // steepness parameter
    private final double r;  // transition radius

    /**
     * Constructor with default parameters: h=10, k=2, r=3
     */
    public TanhRadialStep() {
        this(10.0, 2.0, 3.0);
    }

    /**
     * Constructor with custom parameters
     *
     * @param h the height parameter (depth of valley, default 10.0)
     * @param k the steepness parameter (larger = sharper edge, default 2.0)
     * @param r the transition radius (default 3.0)
     */
    public TanhRadialStep(double h, double k, double r) {
        super("TanhRadialStep", 2, 2, 1, 0);
        this.h = h;
        this.k = k;
        this.r = r;

        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -10.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 10.0));
    }

    @Override
    public double eval(double[] x) {
        double xCoord = x[0];
        double yCoord = x[1];

        // Calculate distance from origin
        double distance = Math.sqrt(xCoord * xCoord + yCoord * yCoord);

        // Apply the inverted tanh radial step function
        // f(x,y) = -h * (1/2) * (1 - tanh(k * (distance - r)))
        double tanhValue = Math.tanh(k * (distance - r));
        return -h * 0.5 * (1.0 - tanhValue);
    }

    /**
     * @return the height/depth parameter h
     */
    public double getH() {
        return h;
    }

    /**
     * @return the steepness parameter k
     */
    public double getK() {
        return k;
    }

    /**
     * @return the transition radius r
     */
    public double getR() {
        return r;
    }
}


