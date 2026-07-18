package org.um.feri.ears.algorithms.so.de.mde;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
/**
 * Interface for local search algorithms plugged into Memetic DE.
 * Implementations must respect the task stop criterion.
 */
public interface LocalSearch {
    /**
     * Improve the given solution using the local search method.
     *
     * @param solution the starting solution to improve
     * @param task     the optimization task
     * @return the best solution found (may be the original if no improvement)
     * @throws StopCriterionException if the stop criterion is met
     */
    NumberSolution<Double> improve(NumberSolution<Double> solution,
                                   Task<NumberSolution<Double>, DoubleProblem> task)
            throws StopCriterionException;

    default NumberSolution<Double> improve(NumberSolution<Double> solution,
                                           Task<NumberSolution<Double>, DoubleProblem> task,
                                           boolean logAncestry)
            throws StopCriterionException {
        return improve(solution, task);
    }
}
