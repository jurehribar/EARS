package org.um.feri.ears.problems.unconstrained;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.*;

/**
 * Shifted Coupled Sine Bowl benchmark function
 *
 * For dimension d, x = (x_1,...,x_d) with box bounds x_i in [-5,5]
 * Shift vector: c_i = 0.4*cos(i)
 *
 * f(x) = sum_{i=1}^{d} w_i*(x_i-c_i)^2
 *        + alpha * sum_{i=1}^{d-1} sin^2(beta*[(x_i-c_i) + gamma*(x_{i+1}-c_{i+1})])
 *        + delta * sum_{i=1}^{d-1} [(x_i-c_i) - (x_{i+1}-c_{i+1})]^2
 *
 * Parameters:
 * w_i = 1 + 4*(i-1)/(d-1)
 * alpha = 0.4
 * beta = 3
 * gamma = 0.5
 * delta = 0.15
 */
public class ShiftedCoupledSineBowl extends DoubleProblem {

    private static final double ALPHA = 15.0;//0.4;
    private static final double BETA = 7.0;//3.0;
    private static final double GAMMA = 0.8;//0.5;
    private static final double DELTA = 0.4;//0.15;

    private double[] c; // shift vector
    private double[] w; // weights

    public ShiftedCoupledSineBowl(int d) {
        super("ShiftedCoupledSineBowl", d, 1, 1, 0);
        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -5.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 5.0));

        // Initialize shift vector c_i = 0.4*cos(i)
        c = new double[d];
        for (int i = 0; i < d; i++) {
            c[i] = 0.4 * cos(i + 1); // i+1 because formula uses 1-based indexing
        }

        // Initialize weights w_i = 1 + 4*(i-1)/(d-1)
        w = new double[d];
        if (d == 1) {
            w[0] = 1.0;
        } else {
            for (int i = 0; i < d; i++) {
                w[i] = 1.0 + 4.0 * i / (d - 1); //original formula is 1 + 4*(i-1)/(d-1), but to increase the difficulty we can use 1 + 25*(i-1)/(d-1)
            }
        }
    }

    @Override
    public double eval(double[] x) {
        double fitness = 0.0;

        // First term: sum of weighted squared deviations
        for (int i = 0; i < numberOfDimensions; i++) {
            double deviation = x[i] - c[i];
            fitness += w[i] * deviation * deviation;
        }

        // Second and third terms: coupling terms (only if d > 1)
        if (numberOfDimensions > 1) {
            for (int i = 0; i < numberOfDimensions - 1; i++) {
                double deviation_i = x[i] - c[i];
                double deviation_i_plus_1 = x[i + 1] - c[i + 1];

                // Second term: sine coupling
                double sinArg = BETA * (deviation_i + GAMMA * deviation_i_plus_1);
                fitness += ALPHA * pow(sin(sinArg), 2);

                // Third term: difference penalty
                double diff = deviation_i - deviation_i_plus_1;
                fitness += DELTA * diff * diff;
            }
        }

        return fitness;
    }
}

