package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.function.IntFunction;

public final class EEProblemSpec {

    private final String name;
    private final double localSearchStep;
    private final IntFunction<DoubleProblem> factory;

    public EEProblemSpec(String name, double localSearchStep, IntFunction<DoubleProblem> factory) {
        this.name = name;
        this.localSearchStep = localSearchStep;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }

    public double getLocalSearchStep() {
        return localSearchStep;
    }

    public DoubleProblem createProblem(int dimension) {
        return factory.apply(dimension);
    }
}
