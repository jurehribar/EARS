package org.um.feri.ears.benchmark;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.problems.constrained.*;
import org.um.feri.ears.problems.unconstrained.*;

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
        //addTask(new Rastrigin(10), stopCriterion, 20000, 0, maxIterations);
        //addTask(new Sphere(10), stopCriterion, 20000, 0, maxIterations);
        //addTask(new Schwefel226(10), stopCriterion, 20000, 0, maxIterations);

        addTask(new RosenbrockDeJong2(10), stopCriterion, 20000, 0, maxIterations);
        addTask(new Powell(10), stopCriterion, 20000, 0, maxIterations);
        addTask(new Zakharov(10), stopCriterion, 20000, 0, maxIterations);
    }
}
