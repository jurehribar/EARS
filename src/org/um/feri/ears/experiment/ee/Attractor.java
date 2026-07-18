package org.um.feri.ears.experiment.ee;

import java.util.Arrays;

public final class Attractor {

    private final double[] variables;
    private final double fitness;
    private final int evaluations;

    public Attractor(double[] variables, double fitness, int evaluations) {
        this.variables = Arrays.copyOf(variables, variables.length);
        this.fitness = fitness;
        this.evaluations = evaluations;
    }

    public double[] getVariables() {
        return Arrays.copyOf(variables, variables.length);
    }

    public double getFitness() {
        return fitness;
    }

    public int getEvaluations() {
        return evaluations;
    }
}
