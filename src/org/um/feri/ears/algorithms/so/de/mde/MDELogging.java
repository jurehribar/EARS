package org.um.feri.ears.algorithms.so.de.mde;

import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.Solution;

import java.util.ArrayList;
import java.util.List;

/**
 * MDE variant that preserves DE and local-search ancestry for
 * exploration/exploitation analysis.
 */
public class MDELogging extends MDE {

    public MDELogging() {
        super();
        initLoggingInfo();
    }

    public MDELogging(DE.Strategy strategy) {
        super(strategy);
        initLoggingInfo();
    }

    public MDELogging(int eliteSize, int localSearchFrequency) {
        super(eliteSize, localSearchFrequency);
        initLoggingInfo();
    }

    public MDELogging(int popSize, double F, double CR,
                      int eliteSize, int localSearchFrequency) {
        super(popSize, F, CR, eliteSize, localSearchFrequency);
        initLoggingInfo();
    }

    public MDELogging(int popSize, double F, double CR,
                      int eliteSize, int localSearchFrequency,
                      LocalSearch localSearch) {
        super(popSize, F, CR, eliteSize, localSearchFrequency, localSearch);
        initLoggingInfo();
    }

    public MDELogging(DE.Strategy strategy, int popSize, double F, double CR,
                      int eliteSize, int localSearchFrequency,
                      LocalSearch localSearch) {
        super(strategy, popSize, F, CR, eliteSize, localSearchFrequency, localSearch);
        initLoggingInfo();
    }

    private void initLoggingInfo() {
        au = new Author("mde", "mde@ears");
        ai = new AlgorithmInfo("MDE-" + getStrategy().label + "Logging",
                "Memetic Differential Evolution (" + getStrategy().label + ") with ancestry logging",
                "MDE with DE target-parent and local-search ancestry logging.");
    }

    @Override
    protected void assignTrialParents(NumberSolution<Double> trial,
                                      NumberSolution<Double> target) {
        trial.parents = parents(target);
    }

    @Override
    protected boolean logLocalSearchAncestry() {
        return true;
    }

    @Override
    protected int captureLocalSearchAncestryStart() {
        ArrayList<Solution> ancestors = task.getAncestors();
        return ancestors == null ? -1 : ancestors.size();
    }

    @Override
    protected void assignFallbackLocalSearchParents(int ancestryStart, NumberSolution<Double> source) {
        ArrayList<Solution> ancestors = task.getAncestors();
        if (ancestryStart < 0 || ancestors == null) {
            return;
        }
        for (int i = ancestryStart; i < ancestors.size(); i++) {
            Solution solution = ancestors.get(i);
            if (solution.parents == null || solution.parents.isEmpty()) {
                solution.parents = parents(source);
            }
        }
    }

    private List<Solution> parents(Solution... solutions) {
        List<Solution> parents = new ArrayList<>(solutions.length);
        for (Solution solution : solutions) {
            parents.add(solution);
        }
        return parents;
    }
}
