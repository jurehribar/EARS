package org.um.feri.ears.benchmark;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.constrained.*;
import org.um.feri.ears.problems.unconstrained.ShiftedCoupledSineBowl;

public class JureHBenchmark extends SOBenchmark<NumberSolution<Double>, NumberSolution<Double>, DoubleProblem, NumberAlgorithm> {
    public JureHBenchmark() {
        super();
        name = "Homework assignment";
        shortName = "JureH";
        info = "Number of tests 2 \n Most dimensions=10\n Compare if difference<=E-10 is tie.";
    }

    @Override
    protected void addTask(DoubleProblem problem, StopCriterion stopCriterion, int maxEvaluations, long time, int maxIterations) {
        tasks.add(new Task<>(problem, stopCriterion, maxEvaluations, time, maxIterations));
    }

    @Override
    public void initAllProblems() {
        addTask(new ShiftedCoupledSineBowl(5), stopCriterion, 200000, 0, maxIterations);
        //addTask(new ShiftedCoupledSineBowl(10), stopCriterion, 200000, 0, maxIterations);
    }
}
