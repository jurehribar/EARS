package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterion;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;

import java.io.IOException;
import java.nio.file.Path;

public final class EEExperimentRunner {

    private static final int[] DIMENSIONS = {5, 10, 30};
    private static final int[] POPULATION_SIZES = {24, 50, 100};
    private static final int[] EVALUATIONS = {50_000, 100_000, 250_000};
    private static final int REPETITIONS = 10;

    private EEExperimentRunner() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length >= 8) {
            EEAlgorithm algorithm = EEAlgorithm.fromLabel(args[0]);
            Path outputDir = Path.of(args[1]);
            EEProblemSpec spec = findProblem(args[2]);
            int dimension = Integer.parseInt(args[3]);
            int populationSize = Integer.parseInt(args[4]);
            int maxEvaluations = Integer.parseInt(args[5]);
            LimitSetting limitSetting = algorithm.usesLimit() ? findLimit(args[6]) : LimitSetting.K;
            int eliteSize = optionalMdeParameter(args, 8, EEAlgorithm.DEFAULT_MDE_ELITE_SIZE,
                    "elite size", algorithm);
            int localSearchFrequency = optionalMdeParameter(args, 9,
                    EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_FREQUENCY, "local-search frequency", algorithm);
            double f = optionalMdeDoubleParameter(args, 10, EEAlgorithm.DEFAULT_MDE_F, "F", algorithm);
            double cr = optionalMdeDoubleParameter(args, 11, EEAlgorithm.DEFAULT_MDE_CR, "CR", algorithm);
            int localSearchStartEvaluations = optionalMdeStartParameter(args, 12,
                    EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_START_FE, algorithm);
            validateMdeParameterPair(args, algorithm);
            if (args[7].equalsIgnoreCase("all")) {
                for (int run = 0; run < REPETITIONS; run++) {
                    runOne(algorithm, outputDir, spec, dimension, populationSize, maxEvaluations,
                            limitSetting, run, eliteSize, localSearchFrequency, f, cr,
                            localSearchStartEvaluations);
                }
            } else {
                int run = Integer.parseInt(args[7]);
                runOne(algorithm, outputDir, spec, dimension, populationSize, maxEvaluations,
                        limitSetting, run, eliteSize, localSearchFrequency, f, cr,
                        localSearchStartEvaluations);
            }
            return;
        }

        if (args.length == 7) {
            Path outputDir = Path.of(args[0]);
            EEProblemSpec spec = findProblem(args[1]);
            int dimension = Integer.parseInt(args[2]);
            int populationSize = Integer.parseInt(args[3]);
            int maxEvaluations = Integer.parseInt(args[4]);
            LimitSetting limitSetting = findLimit(args[5]);
            int run = Integer.parseInt(args[6]);
            runOne(EEAlgorithm.ABC, outputDir, spec, dimension, populationSize, maxEvaluations, limitSetting, run);
            return;
        }

        Path outputDir = args.length > 0 ? Path.of(args[0]) : Path.of("History");

        for (EEAlgorithm algorithm : EEAlgorithm.values()) {
            for (int dimension : DIMENSIONS) {
                for (int populationSize : POPULATION_SIZES) {
                    for (int maxEvaluations : EVALUATIONS) {
                        if (algorithm.usesLimit()) {
                            for (LimitSetting limitSetting : LimitSetting.values()) {
                                runAllProblems(outputDir, algorithm, dimension, populationSize, maxEvaluations, limitSetting);
                            }
                        } else {
                            runAllProblems(outputDir, algorithm, dimension, populationSize, maxEvaluations, LimitSetting.K);
                        }
                    }
                }
            }
        }
    }

    private static void runAllProblems(Path outputDir, EEAlgorithm algorithm, int dimension, int populationSize,
                                       int maxEvaluations, LimitSetting limitSetting) throws IOException {
        for (EEProblemSpec spec : EEProblemFactory.all()) {
            for (int run = 0; run < REPETITIONS; run++) {
                runOne(algorithm, outputDir, spec, dimension, populationSize, maxEvaluations, limitSetting, run);
            }
        }
    }

    private static EEProblemSpec findProblem(String name) {
        for (EEProblemSpec spec : EEProblemFactory.all()) {
            if (spec.getName().equalsIgnoreCase(name)) {
                return spec;
            }
        }
        throw new IllegalArgumentException("Unknown EE problem: " + name);
    }

    private static LimitSetting findLimit(String label) {
        for (LimitSetting setting : LimitSetting.values()) {
            if (setting.getLabel().equalsIgnoreCase(label) || setting.name().equalsIgnoreCase(label)) {
                return setting;
            }
        }
        throw new IllegalArgumentException("Unknown limit setting: " + label);
    }

    private static int optionalMdeParameter(String[] args, int index, int defaultValue, String name,
                                            EEAlgorithm algorithm) {
        if (args.length <= index) {
            return defaultValue;
        }
        if (!algorithm.isMde() && !algorithm.isDeOldNew()) {
            throw new IllegalArgumentException(name + " is supported only for MDE and OldNew DE algorithms");
        }
        return Integer.parseInt(args[index]);
    }

    private static double optionalMdeDoubleParameter(String[] args, int index, double defaultValue, String name,
                                                     EEAlgorithm algorithm) {
        if (args.length <= index) {
            return defaultValue;
        }
        if (!algorithm.isMde() && !algorithm.isDeOldNew()) {
            throw new IllegalArgumentException(name + " is supported only for MDE and OldNew DE algorithms");
        }
        return Double.parseDouble(args[index]);
    }

    private static int optionalMdeStartParameter(String[] args, int index, int defaultValue,
                                                 EEAlgorithm algorithm) {
        if (args.length <= index) {
            return defaultValue;
        }
        if (!algorithm.isMde()) {
            throw new IllegalArgumentException("local-search start evaluations are supported only for MDE algorithms");
        }
        return Integer.parseInt(args[index]);
    }

    private static void validateMdeParameterPair(String[] args, EEAlgorithm algorithm) {
        if ((algorithm.isMde() || algorithm.isDeOldNew()) && (args.length == 9 || args.length == 11)) {
            throw new IllegalArgumentException(
                    "Provide experiment parameters as complete pairs: elite size/frequency and F/CR");
        }
        if (args.length > 13) {
            throw new IllegalArgumentException("Too many experiment arguments");
        }
    }

    private static void runOne(EEAlgorithm algorithm, Path outputDir, EEProblemSpec spec, int dimension,
                               int populationSize, int maxEvaluations, LimitSetting limitSetting, int run) throws IOException {
        runOne(algorithm, outputDir, spec, dimension, populationSize, maxEvaluations, limitSetting, run,
                EEAlgorithm.DEFAULT_MDE_ELITE_SIZE, EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_FREQUENCY,
                EEAlgorithm.DEFAULT_MDE_F, EEAlgorithm.DEFAULT_MDE_CR,
                EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_START_FE);
    }

    private static void runOne(EEAlgorithm algorithm, Path outputDir, EEProblemSpec spec, int dimension,
                               int populationSize, int maxEvaluations, LimitSetting limitSetting, int run,
                               int eliteSize, int localSearchFrequency, double f, double cr,
                               int localSearchStartEvaluations) throws IOException {
        DoubleProblem problem = spec.createProblem(dimension);
        Task<NumberSolution<Double>, DoubleProblem> task = new Task<>(
                problem, StopCriterion.EVALUATIONS, maxEvaluations, 0, 0, 0.001);
        task.enableAncestorLogging();

        NumberAlgorithm eeAlgorithm = algorithm.create(populationSize, limitSetting, dimension,
                eliteSize, localSearchFrequency, f, cr, localSearchStartEvaluations);
        try {
            eeAlgorithm.execute(task);
        } catch (StopCriterionException e) {
            if (!task.isStopCriterion()) {
                throw new IllegalStateException(algorithm.getLabel() + " stopped before the configured criterion", e);
            }
        }

        OldCsvAncestorSaver.save(outputDir.resolve(fileStem(algorithm, spec, dimension, populationSize,
                maxEvaluations, limitSetting, run, eliteSize, localSearchFrequency, f, cr,
                localSearchStartEvaluations) + ".csv"), task);
    }

    static String fileStem(EEAlgorithm algorithm, EEProblemSpec spec, int dimension, int populationSize, int maxEvaluations,
                           LimitSetting limitSetting, int run) {
        return fileStem(algorithm, spec, dimension, populationSize, maxEvaluations, limitSetting, run,
                EEAlgorithm.DEFAULT_MDE_ELITE_SIZE, EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_FREQUENCY,
                EEAlgorithm.DEFAULT_MDE_F, EEAlgorithm.DEFAULT_MDE_CR,
                EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_START_FE);
    }

    static String fileStem(EEAlgorithm algorithm, EEProblemSpec spec, int dimension, int populationSize,
                           int maxEvaluations, LimitSetting limitSetting, int run,
                           int eliteSize, int localSearchFrequency) {
        return fileStem(algorithm, spec, dimension, populationSize, maxEvaluations, limitSetting, run,
                eliteSize, localSearchFrequency, EEAlgorithm.DEFAULT_MDE_F, EEAlgorithm.DEFAULT_MDE_CR,
                EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_START_FE);
    }

    static String fileStem(EEAlgorithm algorithm, EEProblemSpec spec, int dimension, int populationSize,
                           int maxEvaluations, LimitSetting limitSetting, int run,
                           int eliteSize, int localSearchFrequency, double f, double cr) {
        return fileStem(algorithm, spec, dimension, populationSize, maxEvaluations, limitSetting, run,
                eliteSize, localSearchFrequency, f, cr, EEAlgorithm.DEFAULT_MDE_LOCAL_SEARCH_START_FE);
    }

    static String fileStem(EEAlgorithm algorithm, EEProblemSpec spec, int dimension, int populationSize,
                           int maxEvaluations, LimitSetting limitSetting, int run,
                           int eliteSize, int localSearchFrequency, double f, double cr,
                           int localSearchStartEvaluations) {
        String prefix = algorithm.isMde()
                ? algorithm.resultLabel(eliteSize, localSearchFrequency, f, cr, localSearchStartEvaluations)
                : algorithm.filePrefix(limitSetting);
        return prefix + "_" + spec.getName() + "D" + dimension
                + "R" + run + "P" + populationSize + "F" + maxEvaluations;
    }
}
