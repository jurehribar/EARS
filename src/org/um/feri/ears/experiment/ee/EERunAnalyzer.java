package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.List;

public final class EERunAnalyzer {

    private EERunAnalyzer() {
    }

    public static EEMetrics analyze(DoubleProblem problem, double localSearchStep, List<EELogNode> nodes) {
        EEMetrics metrics = new EEMetrics();
        double basinTolerance = localSearchStep * problem.getNumberOfDimensions() / 10.0;

        for (EELogNode node : nodes) {
            Attractor attractor = AttractorLocalSearch.find(problem, node.getVariables(), localSearchStep);
            node.setAttractor(attractor);
            metrics.addLocalSearchEvaluations(attractor.getEvaluations());
            metrics.observeFitness(node.getFitness());
        }

        for (EELogNode node : nodes) {
            if (node.getParent() == null) {
                continue;
            }

            ExplorationType type = classify(node, node.getParent(), basinTolerance);
            node.setType(type);
            metrics.add(type);
        }

        return metrics;
    }

    private static ExplorationType classify(EELogNode child, EELogNode parent, double basinTolerance) {
        boolean differentBasin = distance(child.getAttractor().getVariables(), parent.getAttractor().getVariables()) > basinTolerance;
        boolean childBetter = child.getFitness() < parent.getFitness();
        boolean childAttractorBetter = child.getAttractor().getFitness() < parent.getAttractor().getFitness();

        if (differentBasin) {
            if (childBetter && childAttractorBetter) {
                return ExplorationType.SUCCESSFUL_EXPLORATION;
            }
            if (childBetter) {
                return ExplorationType.DECEPTIVE_EXPLORATION;
            }
            if (childAttractorBetter) {
                return ExplorationType.FAILED_EXPLORATION;
            }
            return ExplorationType.SUCCESSFUL_REJECTION;
        }

        return childBetter ? ExplorationType.SUCCESSFUL_EXPLOITATION : ExplorationType.UNSUCCESSFUL_EXPLOITATION;
    }

    private static double distance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}
