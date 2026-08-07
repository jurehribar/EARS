package org.um.feri.ears.benchmark;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.Task;

public class JureHBenchmark extends SOBenchmark<NumberSolution<Double>, NumberSolution<Double>, DoubleProblem, NumberAlgorithm> {
    private final DoubleProblem problem;

    public JureHBenchmark(DoubleProblem problem, int maxEvaluations) {
        super();
        if (problem == null) {
            throw new IllegalArgumentException("Problem must not be null");
        }
        if (maxEvaluations < 1) {
            throw new IllegalArgumentException("Maximum evaluations must be positive");
        }
        this.problem = problem;
        this.maxEvaluations = maxEvaluations;
        name = "Homework assignment";
        shortName = "JureH";
        info = "Problem=" + problem.getName()
                + ", dimensions=" + problem.getNumberOfDimensions()
                + ", maximum evaluations=" + maxEvaluations;
    }

    @Override
    protected void addTask(DoubleProblem problem, StopCriterion stopCriterion, int maxEvaluations, long time, int maxIterations) {
        tasks.add(new Task<>(problem, stopCriterion, maxEvaluations, time, maxIterations));
    }

    @Override
    public void initAllProblems() {
        addTask(problem, stopCriterion, maxEvaluations, 0, maxIterations);
        //addTask(new Schwefel226(10), stopCriterion, 200000, 0, maxIterations);
        //addTask(new Griewank(10), stopCriterion, 200000, 0, maxIterations);

        //addTask(new RosenbrockDeJong2(10), stopCriterion, 200000, 0, maxIterations);
        //addTask(new Powell(10), stopCriterion, 200000, 0, maxIterations);
        //addTask(new Zakharov(10), stopCriterion, 200000, 0, maxIterations);
    }
}
