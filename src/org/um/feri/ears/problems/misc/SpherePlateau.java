package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;


import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.pow;

/**
 * Sphere with Cross-Shaped Plateau Problem
 *
 * A modified sphere function with a flat cross-shaped plateau defined as:
 * f(x) = 144           if x_i ∈ [10, 17] for any dimension i
 *      = sum(x_i^2)    otherwise
 *
 * The plateau creates a cross-shaped region in 2D (or hyperplane intersections in higher dimensions)
 * where the function value is constant at 144, while the rest follows the standard sphere function.
 *
 * Domain: x ∈ [-100, 100]^d
 * Global optimum: (0, 0, ..., 0) with f = 0
 * Plateau region: Cross-shaped along axes where any coordinate is in [10, 17]
 * Plateau value: f = 144
 */
public class SpherePlateau extends DoubleProblem {
    public SpherePlateau() {
        super("SpherePlateau", 2, 1, 1, 0);
        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -100.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 100.0));
    }

    @Override
    public double eval(double[] x) {
        // Check if ANY dimension is in the plateau range [10, 17]
        // This creates a cross-shaped plateau in 2D
        for (int i = 0; i < numberOfDimensions; i++) {
            if(x[i] >= 10 && x[i] <= 17) {
                return 144.0;  // Flat plateau
            }
        }

        // Outside plateau: use sphere function
        double fitness = 0;
        for (int i = 0; i < numberOfDimensions; i++) {
            fitness += pow(x[i], 2);
        }
        return fitness;
    }
}
