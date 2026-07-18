package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.DoubleProblem;

import java.util.List;

public final class EERunAnalyzer {

    private EERunAnalyzer() {
    }

    public static EEMetrics analyze(DoubleProblem problem, double localSearchStep, List<EELogNode> nodes) {
        EEMetrics metrics = new EEMetrics();
        double basinTolerance = localSearchStep * problem.getNumberOfDimensions() / 10.0;
        AttractorLocalSearch localSearch = new AttractorLocalSearch(localSearchStep);

        for (EELogNode node : nodes) {
            Attractor attractor = localSearch.find(problem, node.getVariables());
            node.setAttractor(attractor);
            metrics.addLocalSearchEvaluations(attractor.getEvaluations());
            metrics.observeFitness(node.getFitness());
        }

        for (EELogNode node : nodes) {
            selectClosestParent(node);
            metrics.observeNode(node.getParent() == null);
            if (node.getParent() == null) {
                continue;
            }

            ExplorationType type = classify(node, node.getParent(), basinTolerance);
            node.setType(type);
            metrics.add(type);
        }

        return metrics;
    }

    private static void selectClosestParent(EELogNode node) {
        EELogNode closest = null;
        double closestDistance = Double.POSITIVE_INFINITY;
        for (EELogNode candidate : node.getCandidateParents()) {
            double candidateDistance = distance(node.getAttractor().getVariables(),
                    candidate.getAttractor().getVariables());
            if (candidateDistance < closestDistance) {
                closest = candidate;
                closestDistance = candidateDistance;
            }
        }
        node.setParent(closest);
    }

    private static ExplorationType classify(EELogNode child, EELogNode parent, double basinTolerance) {
        boolean differentBasin = distance(child.getAttractor().getVariables(), parent.getAttractor().getVariables()) > basinTolerance;
        boolean childBetter = child.getFitness() < parent.getFitness();
        boolean childWorse = child.getFitness() > parent.getFitness();
        boolean childAttractorBetter = child.getAttractor().getFitness() < parent.getAttractor().getFitness();
        boolean childAttractorNotWorse = child.getAttractor().getFitness() <= parent.getAttractor().getFitness();

        if (differentBasin) {
            if (childBetter && childAttractorNotWorse) {
                return ExplorationType.SUCCESSFUL_EXPLORATION;
            }
            if (childBetter) {
                return ExplorationType.DECEPTIVE_EXPLORATION;
            }
            if (childWorse && childAttractorBetter) {
                return ExplorationType.FAILED_EXPLORATION;
            }
            if (childWorse) {
                return ExplorationType.SUCCESSFUL_REJECTION;
            }
            return ExplorationType.INITIAL;
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
