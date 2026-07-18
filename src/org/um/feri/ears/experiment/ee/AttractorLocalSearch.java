package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.Arrays;

public final class AttractorLocalSearch {

    private static final int MAX_STEP_REDUCTIONS = 2;
    private static final int MAX_EVALUATIONS = 100_000;
    // Intentionally retained between find calls to match the original per-run local-search instance.
    private double step;

    public AttractorLocalSearch(double initialStep) {
        this.step = initialStep;
    }

    public Attractor find(DoubleProblem problem, double[] start) {
        double[] best = Arrays.copyOf(start, start.length);
        double bestFitness = problem.eval(best);
        int evaluations = 1;
        int reductions = 0;

        while (evaluations < MAX_EVALUATIONS) {
            boolean improved = false;
            for (int i = 0; i < best.length; i++) {
                double[] plus = Arrays.copyOf(best, best.length);
                plus[i] += step;
                double plusFitness = problem.eval(plus);
                evaluations++;
                if (plusFitness < bestFitness) {
                    best = plus;
                    bestFitness = plusFitness;
                    improved = true;
                    break;
                }
                if (evaluations >= MAX_EVALUATIONS) {
                    break;
                }

                double[] minus = Arrays.copyOf(best, best.length);
                minus[i] -= step;
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
