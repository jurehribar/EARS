package org.um.feri.ears.experiment.ee;

public final class EEMetrics {

    private int successfulExploration;
    private int successfulRejection;
    private int deceptiveExploration;
    private int failedExploration;
    private int successfulExploitation;
    private int unsuccessfulExploitation;
    private int localSearchEvaluations;
    private double bestFitness = Double.POSITIVE_INFINITY;

    public void add(ExplorationType type) {
        switch (type) {
            case SUCCESSFUL_EXPLORATION:
                successfulExploration++;
                break;
            case SUCCESSFUL_REJECTION:
                successfulRejection++;
                break;
            case DECEPTIVE_EXPLORATION:
                deceptiveExploration++;
                break;
            case FAILED_EXPLORATION:
                failedExploration++;
                break;
            case SUCCESSFUL_EXPLOITATION:
                successfulExploitation++;
                break;
            case UNSUCCESSFUL_EXPLOITATION:
                unsuccessfulExploitation++;
                break;
            default:
                break;
        }
    }

    public void addLocalSearchEvaluations(int evaluations) { localSearchEvaluations += evaluations; }
    public void observeFitness(double fitness) { bestFitness = Math.min(bestFitness, fitness); }

    public int getExploration() { return successfulExploration + successfulRejection + deceptiveExploration + failedExploration; }
    public int getExploitation() { return successfulExploitation + unsuccessfulExploitation; }
    public int getClassifiedEvents() { return getExploration() + getExploitation(); }

    public double ratio(int count, int denominator) { return denominator == 0 ? 0.0 : (double) count / denominator; }
    public double getExplorationRatio() { return ratio(getExploration(), getClassifiedEvents()); }
    public double getExploitationRatio() { return ratio(getExploitation(), getClassifiedEvents()); }
    public double getSuccessfulExplorationRatio() { return ratio(successfulExploration, getExploration()); }
    public double getSuccessfulRejectionRatio() { return ratio(successfulRejection, getExploration()); }
    public double getDeceptiveExplorationRatio() { return ratio(deceptiveExploration, getExploration()); }
    public double getFailedExplorationRatio() { return ratio(failedExploration, getExploration()); }
    public double getSuccessfulExploitationRatio() { return ratio(successfulExploitation, getExploitation()); }
    public double getUnsuccessfulExploitationRatio() { return ratio(unsuccessfulExploitation, getExploitation()); }

    public int getSuccessfulExploration() { return successfulExploration; }
    public int getSuccessfulRejection() { return successfulRejection; }
    public int getDeceptiveExploration() { return deceptiveExploration; }
    public int getFailedExploration() { return failedExploration; }
    public int getSuccessfulExploitation() { return successfulExploitation; }
    public int getUnsuccessfulExploitation() { return unsuccessfulExploitation; }
    public int getLocalSearchEvaluations() { return localSearchEvaluations; }
    public double getBestFitness() { return bestFitness; }
}
