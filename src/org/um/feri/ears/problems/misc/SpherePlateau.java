package org.um.feri.ears.problems.misc;

import org.um.feri.ears.problems.DoubleProblem;


import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.pow;

public class SpherePlateau extends DoubleProblem {
    public SpherePlateau() {
        super("SpherePlateau", 2, 1, 1, 0);
        lowerLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, -100.0));
        upperLimit = new ArrayList<>(Collections.nCopies(numberOfDimensions, 100.0));
    }

    @Override
    public double eval(double[] x) {
        double fitness = 0;
        for (int i = 0; i < numberOfDimensions; i++) {
            if(x[i] >= 10 && x[i] <= 17)
                fitness = 144;
            else
                fitness += +pow(x[i], 2);
        }
        return fitness;
    }
}
