package org.um.feri.ears.algorithms.so.de.mde;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import java.util.ArrayList;
/**
 * Gradient Descent local search using numerical central-difference approximation.
 *
 * For each call to improve(), the algorithm performs up to maxSteps
 * gradient-descent steps starting from the provided solution.
 * The step size is controlled by learningRate.
 * The best solution encountered during the descent is returned.
 *
 * Partial derivative for dimension i is estimated as:
 *   df/dx_i = ( f(x + eps*e_i) - f(x - eps*e_i) ) / (2*eps)
 */
public class GradientDescentLocalSearch implements LocalSearch {
    private static final double EPSILON = 1e-6;
    private final double learningRate;
    private final int maxSteps;
    /** Default: learningRate = 0.01, maxSteps = 10. */
    public GradientDescentLocalSearch() {
        this(0.01, 10);
    }
    /**
     * @param learningRate step-size multiplier (alpha in  x = x - alpha * grad_f(x))
     * @param maxSteps     maximum gradient steps per improve() call
     */
    public GradientDescentLocalSearch(double learningRate, int maxSteps) {
        this.learningRate = learningRate;
        this.maxSteps = maxSteps;
    }
    @Override
    public NumberSolution<Double> improve(NumberSolution<Double> solution,
                                          Task<NumberSolution<Double>, DoubleProblem> task)
            throws StopCriterionException {
        NumberSolution<Double> current = solution;
        for (int step = 0; step < maxSteps; step++) {
            if (task.isStopCriterion()) break;
            ArrayList<Double> vars = new ArrayList<>(current.getVariables());
            ArrayList<Double> gradient = computeGradient(vars, task);
            if (gradient == null) break;
            // x = x - learningRate * grad_f(x)
            ArrayList<Double> newVars = new ArrayList<>(vars.size());
            for (int i = 0; i < vars.size(); i++) {
                newVars.add(vars.get(i) - learningRate * gradient.get(i));
            }
            NumberSolution<Double> candidate = new NumberSolution<>(newVars);
            task.problem.makeFeasible(candidate);
            if (task.isStopCriterion()) break;
            task.eval(candidate);
            if (task.problem.isFirstBetter(candidate, current)) {
                current = candidate;
            }
        }
        return current;
    }
    /**
     * Estimate the gradient at vars using central differences.
     *
     * @return the gradient vector, or null if stop criterion was reached
     */
    private ArrayList<Double> computeGradient(ArrayList<Double> vars,
                                              Task<NumberSolution<Double>, DoubleProblem> task)
            throws StopCriterionException {
        ArrayList<Double> gradient = new ArrayList<>(vars.size());
        for (int i = 0; i < vars.size(); i++) {
            double original = vars.get(i);
            vars.set(i, original + EPSILON);
            if (task.isStopCriterion()) return null;
            NumberSolution<Double> fwdSol = new NumberSolution<>(new ArrayList<>(vars));
            task.eval(fwdSol);
            double fwdVal = fwdSol.getObjective(0);
            vars.set(i, original - EPSILON);
            if (task.isStopCriterion()) return null;
            NumberSolution<Double> bwdSol = new NumberSolution<>(new ArrayList<>(vars));
            task.eval(bwdSol);
            double bwdVal = bwdSol.getObjective(0);
            gradient.add((fwdVal - bwdVal) / (2.0 * EPSILON));
            vars.set(i, original);
        }
        return gradient;
    }
    public double getLearningRate() { return learningRate; }
    public int getMaxSteps()        { return maxSteps;     }
    @Override
    public String toString() {
        return "GradientDescentLocalSearch{learningRate=" + learningRate
                + ", maxSteps=" + maxSteps + "}";
    }
}