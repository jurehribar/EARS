package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.de.mde.GradientDescentLocalSearch;
import org.um.feri.ears.algorithms.so.de.mde.MDE;
import org.um.feri.ears.benchmark.Benchmark;
import org.um.feri.ears.benchmark.JureHBenchmark;
import org.um.feri.ears.experiment.ee.EEProblemFactory;
import org.um.feri.ears.experiment.ee.EEProblemSpec;
import org.um.feri.ears.problems.DoubleProblem;

import java.util.ArrayList;
import java.util.Locale;

public class SOBenchmarkJureH {

    public static void main(String[] args) {
        DE.Strategy strategy = parseStrategy(args.length > 0 ? args[0] : "rand");
        int eliteSize = parseNonNegativeInt(args, 1, 5, "elite size");
        int localSearchFrequency = parsePositiveInt(args, 2, 1, "local-search frequency");
        int localSearchStartEvaluations = parseNonNegativeInt(args, 3, 0,
                "local-search start evaluations");
        String problemName = args.length > 4 ? args[4] : "Rastrigin";
        int dimension = parsePositiveInt(args, 5, 10, "dimension");
        int maxEvaluations = parsePositiveInt(args, 6, 20000, "maximum evaluations");
        int populationSize = parsePositiveInt(args, 7, 30, "population size");
        double f = parseDouble(args, 8, 0.5, "F");
        double cr = parseDouble(args, 9, 0.9, "CR");
        if (f <= 0.0 || f > 2.0) {
            throw new IllegalArgumentException("F must be in (0, 2]");
        }
        if (cr < 0.0 || cr > 1.0) {
            throw new IllegalArgumentException("CR must be in [0, 1]");
        }
        if (args.length > 10) {
            throw new IllegalArgumentException(
                    "Usage: SOBenchmarkJureH [rand|best] [eliteSize>=0] "
                            + "[localSearchFrequency>=1] [localSearchStartEvaluations>=0] "
                            + "[problem] [dimension>=1] [maxEvaluations>=1] [populationSize>=1] "
                            + "[F in (0,2]] [CR in [0,1]]");
        }

        DoubleProblem problem = createProblem(problemName, dimension);

        Benchmark.printInfo = false; //prints one on one results
        System.out.printf("Running JureH benchmark: strategy=%s, problem=%s, dimension=%d, "
                        + "maxEvaluations=%d, populationSize=%d, F=%s, CR=%s, eliteSize=%d, "
                        + "localSearchFrequency=%d, localSearchStartEvaluations=%d%n",
                strategy.label, problem.getName(), dimension, maxEvaluations, populationSize,
                f, cr, eliteSize, localSearchFrequency, localSearchStartEvaluations);

        ArrayList<NumberAlgorithm> algorithms = new ArrayList<>();

        algorithms.add(new MDE(strategy, populationSize, f, cr, eliteSize, localSearchFrequency,
                 localSearchStartEvaluations,
                new GradientDescentLocalSearch()));
        //----
        algorithms.add(new MDE(DE.Strategy.DE_RAND_1_BIN, populationSize, f, cr, eliteSize, localSearchFrequency,
                localSearchStartEvaluations, new GradientDescentLocalSearch()));

        algorithms.add(new DE(strategy, populationSize, f, cr));
        //---
        algorithms.add(new DE(DE.Strategy.DE_RAND_1_BIN, populationSize, f, cr));

        JureHBenchmark bench = new JureHBenchmark(problem, maxEvaluations);

        bench.addAlgorithms(algorithms);  // register the algorithms in the benchmark

        bench.run(10); //start the tournament with 10 runs/repetitions
    }

    private static DE.Strategy parseStrategy(String value) {
        switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "rand":
            case "de-rand-1-bin":
            case "mde-rand-1-bin":
                return DE.Strategy.DE_RAND_1_BIN;
            case "best":
            case "de-best-1-bin":
            case "mde-best-1-bin":
                return DE.Strategy.DE_BEST_1_BIN;
            default:
                throw new IllegalArgumentException("Unsupported strategy '" + value
                        + "'. Use rand or best.");
        }
    }

    private static int parseNonNegativeInt(String[] args, int index, int defaultValue, String name) {
        int value = parseInt(args, index, defaultValue, name);
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be at least 0");
        }
        return value;
    }

    private static int parsePositiveInt(String[] args, int index, int defaultValue, String name) {
        int value = parseInt(args, index, defaultValue, name);
        if (value < 1) {
            throw new IllegalArgumentException(name + " must be at least 1");
        }
        return value;
    }

    private static int parseInt(String[] args, int index, int defaultValue, String name) {
        if (args.length <= index) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer: " + args[index], e);
        }
    }

    private static double parseDouble(String[] args, int index, double defaultValue, String name) {
        if (args.length <= index) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(args[index]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a number: " + args[index], e);
        }
    }

    private static DoubleProblem createProblem(String name, int dimension) {
        for (EEProblemSpec spec : EEProblemFactory.all()) {
            if (spec.getName().equalsIgnoreCase(name)) {
                return spec.createProblem(dimension);
            }
        }
        throw new IllegalArgumentException("Unsupported problem '" + name
                + "'. Supported problems: Rastrigin, Ackley, Griewank, F03, F10, F20.");
    }
}
