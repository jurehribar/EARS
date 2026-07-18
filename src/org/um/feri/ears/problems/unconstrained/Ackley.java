package org.um.feri.ears.problems.unconstrained;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Collections;

public class Ackley extends DoubleProblem {

    public Ackley(int d) {
        super("Ackley", d, 1, 1, 0);
        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -30.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 30.0));
    }

    @Override
    public double eval(double[] x) {
        double sphereSum = 0.0;
        double cosSum = 0.0;
        for (int i = 0; i < numberOfDimensions; i++) {
            sphereSum += x[i] * x[i];
            cosSum += Math.cos(2.0 * Math.PI * x[i]);
        }
        return -20.0 * Math.exp(-0.2 * Math.sqrt(sphereSum / numberOfDimensions))
                - Math.exp(cosSum / numberOfDimensions) + 20.0 + Math.E;
    }
}
