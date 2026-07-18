package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.Arrays;

public final class AttractorLocalSearch {

    private static final int MAX_STEP_REDUCTIONS = 2;

    private AttractorLocalSearch() {
    }

    public static Attractor find(DoubleProblem problem, double[] start, double initialStep) {
        double step = initialStep;
        double[] best = Arrays.copyOf(start, start.length);
        problem.makeFeasible(best);
        double bestFitness = problem.eval(best);
        int evaluations = 1;
        int reductions = 0;

        while (true) {
            boolean improved = false;
            for (int i = 0; i < best.length; i++) {
                double[] plus = Arrays.copyOf(best, best.length);
                plus[i] += step;
                problem.makeFeasible(plus);
                double plusFitness = problem.eval(plus);
                evaluations++;
                if (plusFitness < bestFitness) {
                    best = plus;
                    bestFitness = plusFitness;
                    improved = true;
                    break;
                }

                double[] minus = Arrays.copyOf(best, best.length);
                minus[i] -= step;
                problem.makeFeasible(minus);
                double minusFitness = problem.eval(minus);
                evaluations++;
                if (minusFitness < bestFitness) {
                    best = minus;
                    bestFitness = minusFitness;
                    improved = true;
                    break;
                }
            }

            if (!improved) {
                if (reductions >= MAX_STEP_REDUCTIONS) {
                    break;
                }
                reductions++;
                step /= 2.0;
            }
        }

        return new Attractor(best, bestFitness, evaluations);
    }
}
