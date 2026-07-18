package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.algorithms.so.abc.ABCLogging;
import org.um.feri.ears.experiment.ee.OldCsvAncestorSaver;
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
        if (args.length >= 7) {
            Path outputDir = Path.of(args[0]);
            EEProblemSpec spec = findProblem(args[1]);
            int dimension = Integer.parseInt(args[2]);
            int populationSize = Integer.parseInt(args[3]);
            int maxEvaluations = Integer.parseInt(args[4]);
            LimitSetting limitSetting = findLimit(args[5]);
            int run = Integer.parseInt(args[6]);
            runOne(outputDir, spec, dimension, populationSize, maxEvaluations, limitSetting, run);
            return;
        }

        Path outputDir = args.length > 0 ? Path.of(args[0]) : Path.of("History");

        for (int dimension : DIMENSIONS) {
            for (int populationSize : POPULATION_SIZES) {
                for (int maxEvaluations : EVALUATIONS) {
                    for (LimitSetting limitSetting : LimitSetting.values()) {
                        for (EEProblemSpec spec : EEProblemFactory.all()) {
                            for (int run = 0; run < REPETITIONS; run++) {
                                runOne(outputDir, spec, dimension, populationSize, maxEvaluations, limitSetting, run);
                            }
                        }
                    }
                }
            }
        }
    }

    private static EEProblemSpec findProblem(String name) {
        for (EEProblemSpec spec : EEProblemFactory.all()) {
            if (spec.getName().equalsIgnoreCase(name)) {
                return spec;
            }
        }
        throw new IllegalArgumentException("Unknown paper problem: " + name);
    }

    private static LimitSetting findLimit(String label) {
        for (LimitSetting setting : LimitSetting.values()) {
            if (setting.getLabel().equalsIgnoreCase(label) || setting.name().equalsIgnoreCase(label)) {
                return setting;
            }
        }
        throw new IllegalArgumentException("Unknown limit setting: " + label);
    }

    private static void runOne(Path outputDir, EEProblemSpec spec, int dimension, int populationSize,
                               int maxEvaluations, LimitSetting limitSetting, int run) throws IOException {
        DoubleProblem problem = spec.createProblem(dimension);
        Task<NumberSolution<Double>, DoubleProblem> task = new Task<>(
                problem, StopCriterion.EVALUATIONS, maxEvaluations, 0, 0, 0.001);
        task.enableAncestorLogging();

        ABCLogging abc = new ABCLogging(populationSize, limitSetting.resolve(populationSize, dimension));
        try {
            abc.execute(task);
        } catch (StopCriterionException e) {
            // Reaching the evaluation budget is the normal termination route for some EARS algorithms.
            if (!task.isStopCriterion()) {
                throw new IllegalStateException("ABCLogging stopped before the configured criterion", e);
            }
        }

        OldCsvAncestorSaver.save(outputDir.resolve(fileStem(spec, dimension, populationSize, maxEvaluations, limitSetting, run) + ".csv"), task);
    }

    static String fileStem(EEProblemSpec spec, int dimension, int populationSize, int maxEvaluations,
                           LimitSetting limitSetting, int run) {
        return "ABClim" + limitSetting.getLabel() + "_" + spec.getName() + "D" + dimension
                + "R" + run + "P" + populationSize + "F" + maxEvaluations;
    }
}
