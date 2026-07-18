package org.um.feri.ears.algorithms.so.de.mde;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Gradient Descent local search using numerical central-difference approximation
 * with backtracking line search (Armijo condition).
 *
 * For each call to improve(), the algorithm performs up to maxSteps
 * gradient-descent steps starting from the provided solution.
 * Instead of a fixed learning rate, each step uses backtracking line search:
 *   - Start with initialStepSize as the candidate step length.
 *   - Halve the step (up to maxBacktrackSteps times) until the Armijo
 *     sufficient-decrease condition is satisfied:
 *       f(x - alpha * grad) <= f(x) - c1 * alpha * ||grad||^2
 *   - If no step satisfies the condition, skip the step (gradient is not useful here).
 *
 * Partial derivative for dimension i is estimated as:
 *   df/dx_i = ( f(x + eps*e_i) - f(x - eps*e_i) ) / (2*eps)
 *
 * The optimal eps for central differences in IEEE 754 double precision is:
 *   eps_opt = (machine_epsilon)^(1/3) ≈ (2.2e-16)^(1/3) ≈ 6e-6
 */
public class GradientDescentLocalSearch implements LocalSearch {
    /** Optimal step for central-difference in double precision: cbrt(machine_epsilon). */
    private static final double EPSILON = 6e-6;
    /** Armijo sufficient-decrease constant (standard value). */
    private static final double C1 = 1e-4;
    private final double initialStepSize;
    private final int maxSteps;
    private final int maxBacktrackSteps;
    /**
     * Default: initialStepSize = 1.0, maxSteps = 10, maxBacktrackSteps = 20.
     * Starting step of 1.0 lets the line search find the right scale automatically.
     */
    public GradientDescentLocalSearch() {
        this(1.0, 10, 20);
    }
    /**
     * @param initialStepSize  starting step length for backtracking (halved each trial)
     * @param maxSteps         maximum gradient steps per improve() call
     * @param maxBacktrackSteps maximum number of step halvings per gradient step
     */
    public GradientDescentLocalSearch(double initialStepSize, int maxSteps, int maxBacktrackSteps) {
        this.initialStepSize   = initialStepSize;
        this.maxSteps          = maxSteps;
        this.maxBacktrackSteps = maxBacktrackSteps;
    }
    /**
     * Backwards-compatible constructor: maps the old fixed learningRate to initialStepSize.
     *
     * @param learningRate used as initialStepSize
     * @param maxSteps     maximum gradient steps per improve() call
     */
    public GradientDescentLocalSearch(double learningRate, int maxSteps) {
        this(learningRate, maxSteps, 20);
    }
    @Override
    public NumberSolution<Double> improve(NumberSolution<Double> solution,
                                          Task<NumberSolution<Double>, DoubleProblem> task)
            throws StopCriterionException {
        return improve(solution, task, false);
    }

    @Override
    public NumberSolution<Double> improve(NumberSolution<Double> solution,
                                          Task<NumberSolution<Double>, DoubleProblem> task,
                                          boolean logAncestry)
            throws StopCriterionException {
        NumberSolution<Double> current = solution;
        for (int step = 0; step < maxSteps; step++) {
            if (task.isStopCriterion()) break;
            ArrayList<Double> vars = new ArrayList<>(current.getVariables());
            ArrayList<Double> gradient = computeGradient(vars, current, task, logAncestry);
            if (gradient == null) break;
            // Squared gradient norm: ||grad||^2  (used in Armijo condition)
            double gradNormSq = 0.0;
            for (double g : gradient) gradNormSq += g * g;
            if (gradNormSq == 0.0) break; // already at a flat point, no descent direction
            double currentVal = current.getObjective(0);
            // Backtracking line search (Armijo condition)
            // Find the largest alpha in {initialStepSize, .../2, .../4, ...} such that:
            //   f(x - alpha * grad) <= f(x) - C1 * alpha * ||grad||^2
            double alpha = initialStepSize;
            NumberSolution<Double> candidate = null;
            boolean stepAccepted = false;
            for (int bt = 0; bt < maxBacktrackSteps; bt++) {
                if (task.isStopCriterion()) break;
                ArrayList<Double> newVars = new ArrayList<>(vars.size());
                for (int i = 0; i < vars.size(); i++) {
                    newVars.add(vars.get(i) - alpha * gradient.get(i));
                }
                candidate = new NumberSolution<>(newVars);
                task.problem.makeFeasible(candidate);
                setParent(candidate, current, logAncestry);
                if (task.isStopCriterion()) break;
                task.eval(candidate);
                // Armijo sufficient-decrease condition
                if (candidate.getObjective(0) <= currentVal - C1 * alpha * gradNormSq) {
                    stepAccepted = true;
                    break;
                }
                alpha /= 2.0; // halve the step and retry
            }
            if (stepAccepted && candidate != null
                    && task.problem.isFirstBetter(candidate, current)) {
                current = candidate;
            }
        }
        return current;
    }
    /**
     * Estimate the gradient at vars using central differences.
     * Uses eps = 6e-6, the theoretically optimal step for double precision.
     *
     * @return the gradient vector, or null if the stop criterion was reached
     */
    private ArrayList<Double> computeGradient(ArrayList<Double> vars,
                                              NumberSolution<Double> current,
                                              Task<NumberSolution<Double>, DoubleProblem> task,
                                              boolean logAncestry)
            throws StopCriterionException {
        ArrayList<Double> gradient = new ArrayList<>(vars.size());
        for (int i = 0; i < vars.size(); i++) {
            double original = vars.get(i);
            vars.set(i, original + EPSILON);
            if (task.isStopCriterion()) return null;
            NumberSolution<Double> fwdSol = new NumberSolution<>(new ArrayList<>(vars));
            setParent(fwdSol, current, logAncestry);
            task.eval(fwdSol);
            double fwdVal = fwdSol.getObjective(0);
            vars.set(i, original - EPSILON);
            if (task.isStopCriterion()) return null;
            NumberSolution<Double> bwdSol = new NumberSolution<>(new ArrayList<>(vars));
            setParent(bwdSol, current, logAncestry);
            task.eval(bwdSol);
            double bwdVal = bwdSol.getObjective(0);
            gradient.add((fwdVal - bwdVal) / (2.0 * EPSILON));
            vars.set(i, original);
        }
        return gradient;
    }

    private void setParent(NumberSolution<Double> solution,
                           NumberSolution<Double> parent,
                           boolean logAncestry) {
        if (logAncestry) {
            solution.parents = Collections.singletonList(parent);
        }
    }
    public double getInitialStepSize()   { return initialStepSize;   }
    public int getMaxSteps()             { return maxSteps;           }
    public int getMaxBacktrackSteps()    { return maxBacktrackSteps;  }
    @Override
    public String toString() {
        return "GradientDescentLocalSearch{initialStepSize=" + initialStepSize
                + ", maxSteps=" + maxSteps
                + ", maxBacktrackSteps=" + maxBacktrackSteps + "}";
    }
}
