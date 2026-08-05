package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.de.mde.GradientDescentLocalSearch;
import org.um.feri.ears.algorithms.so.de.mde.MDE;
import org.um.feri.ears.benchmark.Benchmark;
import org.um.feri.ears.benchmark.JureHBenchmark;

import java.util.ArrayList;
import java.util.Locale;

public class SOBenchmarkJureH {

    public static void main(String[] args) {
        DE.Strategy strategy = parseStrategy(args.length > 0 ? args[0] : "rand");
        int eliteSize = parseNonNegativeInt(args, 1, 5, "elite size");
        int localSearchFrequency = parsePositiveInt(args, 2, 1, "local-search frequency");
        int localSearchStartEvaluations = parseNonNegativeInt(args, 3, 0,
                "local-search start evaluations");
        if (args.length > 4) {
            throw new IllegalArgumentException(
                    "Usage: SOBenchmarkJureH [rand|best] [eliteSize>=0] "
                            + "[localSearchFrequency>=1] [localSearchStartEvaluations>=0]");
        }

        Benchmark.printInfo = false; //prints one on one results
        System.out.printf("Running JureH benchmark: strategy=%s, eliteSize=%d, "
                        + "localSearchFrequency=%d, localSearchStartEvaluations=%d%n",
                strategy.label, eliteSize, localSearchFrequency, localSearchStartEvaluations);

        ArrayList<NumberAlgorithm> algorithms = new ArrayList<>();
        algorithms.add(new MDE(strategy, 100, 0.5, 0.9, eliteSize, localSearchFrequency,
                localSearchStartEvaluations,
                new GradientDescentLocalSearch()));
        //algorithms.add(new MDE(DE.Strategy.DE_BEST_1_BIN, 100, 0.5, 0.9, eliteSize, localSearchFrequency,
        //        new GradientDescentLocalSearch()));
        algorithms.add(new DE(strategy, 100, 0.5, 0.9));
        //algorithms.add(new DE(DE.Strategy.DE_BEST_1_BIN, 100, 0.5, 0.9));
        JureHBenchmark bench = new JureHBenchmark(); // benchmark with prepared tasks and settings

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
}
