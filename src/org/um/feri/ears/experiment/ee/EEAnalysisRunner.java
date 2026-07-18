package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.DoubleProblem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

public final class EEAnalysisRunner {

    private static final int[] DIMENSIONS = {5, 10, 30};
    private static final int[] POPULATION_SIZES = {24, 50, 100};
    private static final int[] EVALUATIONS = {50_000, 100_000, 250_000};
    private static final int REPETITIONS = 10;

    private EEAnalysisRunner() {
    }

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);
        Path historyDir = args.length > 0 ? Path.of(args[0]) : Path.of("History");
        Path outputDir = args.length > 1 ? Path.of(args[1]) : Path.of("EEAnalysis");
        Files.createDirectories(outputDir);

        if (args.length >= 7) {
            EEProblemSpec spec = findProblem(args[2]);
            int dimension = Integer.parseInt(args[3]);
            int populationSize = Integer.parseInt(args[4]);
            int maxEvaluations = Integer.parseInt(args[5]);
            LimitSetting limitSetting = findLimit(args[6]);
            MetricAccumulator accumulator = analyzeCombination(historyDir, outputDir, spec, dimension, populationSize, maxEvaluations, limitSetting);
            try (BufferedWriter summary = Files.newBufferedWriter(outputDir.resolve("abc-summary.csv"));
                 BufferedWriter ratios = Files.newBufferedWriter(outputDir.resolve("abc-ratios.csv"));
                 BufferedWriter table = Files.newBufferedWriter(outputDir.resolve("abc-table.tex"))) {
                summary.write("problem,dimension,population,evaluations,limit,runs,exploration_mean,exploration_stdev,exploitation_mean,exploitation_stdev,best_fitness_mean,best_fitness_stdev");
                summary.newLine();
                ratios.write("problem,dimension,population,evaluations,limit,runs,se_mean,se_stdev,fe_mean,fe_stdev,de_mean,de_stdev,sr_mean,sr_stdev,sx_mean,sx_stdev,ux_mean,ux_stdev");
                ratios.newLine();
                writeTableHeader(table);
                if (accumulator.size() > 0) {
                    writeSummary(summary, spec, dimension, populationSize, maxEvaluations, limitSetting, accumulator);
                    writeRatios(ratios, spec, dimension, populationSize, maxEvaluations, limitSetting, accumulator);
                    writeTableRow(table, spec, dimension, populationSize, maxEvaluations, limitSetting, accumulator);
                }
                writeTableFooter(table);
            }
            return;
        }

        try (BufferedWriter summary = Files.newBufferedWriter(outputDir.resolve("abc-summary.csv"));
             BufferedWriter ratios = Files.newBufferedWriter(outputDir.resolve("abc-ratios.csv"));
             BufferedWriter table = Files.newBufferedWriter(outputDir.resolve("abc-table.tex"))) {
            summary.write("problem,dimension,population,evaluations,limit,runs,exploration_mean,exploration_stdev,exploitation_mean,exploitation_stdev,best_fitness_mean,best_fitness_stdev");
            summary.newLine();
            ratios.write("problem,dimension,population,evaluations,limit,runs,se_mean,se_stdev,fe_mean,fe_stdev,de_mean,de_stdev,sr_mean,sr_stdev,sx_mean,sx_stdev,ux_mean,ux_stdev");
            ratios.newLine();
            writeTableHeader(table);

            for (int dimension : DIMENSIONS) {
                for (int populationSize : POPULATION_SIZES) {
                    for (int maxEvaluations : EVALUATIONS) {
                        for (LimitSetting limitSetting : LimitSetting.values()) {
                            for (EEProblemSpec spec : EEProblemFactory.all()) {
                                MetricAccumulator accumulator = analyzeCombination(historyDir, outputDir, spec, dimension, populationSize, maxEvaluations, limitSetting);
                                if (accumulator.size() > 0) {
                                    writeSummary(summary, spec, dimension, populationSize, maxEvaluations, limitSetting, accumulator);
                                    writeRatios(ratios, spec, dimension, populationSize, maxEvaluations, limitSetting, accumulator);
                                    writeTableRow(table, spec, dimension, populationSize, maxEvaluations, limitSetting, accumulator);
                                }
                            }
                        }
                    }
                }
            }
            writeTableFooter(table);
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

    private static MetricAccumulator analyzeCombination(Path historyDir, Path outputDir, EEProblemSpec spec, int dimension,
                                                        int populationSize, int maxEvaluations, LimitSetting limitSetting) throws IOException {
        MetricAccumulator accumulator = new MetricAccumulator();
        for (int run = 0; run < REPETITIONS; run++) {
            Path file = historyDir.resolve(EEExperimentRunner.fileStem(spec, dimension, populationSize, maxEvaluations, limitSetting, run) + ".csv");
            if (!Files.exists(file)) {
                continue;
            }

            DoubleProblem problem = spec.createProblem(dimension);
            List<EELogNode> nodes = EERunLogReader.read(file);
            EEMetrics metrics = EERunAnalyzer.analyze(problem, spec.getLocalSearchStep(), nodes);
            accumulator.add(metrics);
            writeRunSeries(outputDir, file.getFileName().toString().replace(".csv", ""), nodes);
        }
        if (accumulator.size() != REPETITIONS) {
            throw new IOException("Expected " + REPETITIONS + " history files for "
                    + EEExperimentRunner.fileStem(spec, dimension, populationSize, maxEvaluations, limitSetting, 0)
                    + " through run " + (REPETITIONS - 1) + ", but found " + accumulator.size());
        }
        return accumulator;
    }

    private static void writeRunSeries(Path outputDir, String stem, List<EELogNode> nodes) throws IOException {
        Path seriesDir = outputDir.resolve("series");
        Files.createDirectories(seriesDir);
        Path dataFile = seriesDir.resolve(stem + "-types.dat");
        try (BufferedWriter writer = Files.newBufferedWriter(dataFile)) {
            writer.write("index exploration exploitation successful_exploration failed_exploration deceptive_exploration successful_rejection successful_exploitation unsuccessful_exploitation");
            writer.newLine();
            int exploration = 0;
            int exploitation = 0;
            int se = 0;
            int fe = 0;
            int de = 0;
            int sr = 0;
            int sx = 0;
            int ux = 0;
            int index = 0;
            for (EELogNode node : nodes) {
                switch (node.getType()) {
                    case SUCCESSFUL_EXPLORATION: exploration++; se++; break;
                    case FAILED_EXPLORATION: exploration++; fe++; break;
                    case DECEPTIVE_EXPLORATION: exploration++; de++; break;
                    case SUCCESSFUL_REJECTION: exploration++; sr++; break;
                    case SUCCESSFUL_EXPLOITATION: exploitation++; sx++; break;
                    case UNSUCCESSFUL_EXPLOITATION: exploitation++; ux++; break;
                    default: break;
                }
                writer.write(String.format(Locale.US, "%d %d %d %d %d %d %d %d %d", index++, exploration, exploitation, se, fe, de, sr, sx, ux));
                writer.newLine();
            }
        }

        writeGnuplotScripts(seriesDir, stem);
    }

    private static void writeGnuplotScripts(Path seriesDir, String stem) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(seriesDir.resolve(stem + "-xpl-xpt.plt"))) {
            writer.write("set term pngcairo size 1200,800\n");
            writer.write("set output '" + stem + "-xpl-xpt.png'\n");
            writer.write("set xlabel 'Evaluation index'\n");
            writer.write("set ylabel 'Cumulative events'\n");
            writer.write("plot '" + stem + "-types.dat' using 1:2 with lines title 'XPL', \\\n");
            writer.write("     '" + stem + "-types.dat' using 1:3 with lines title 'XPT'\n");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(seriesDir.resolve(stem + "-types.plt"))) {
            writer.write("set term pngcairo size 1200,800\n");
            writer.write("set output '" + stem + "-types.png'\n");
            writer.write("set xlabel 'Evaluation index'\n");
            writer.write("set ylabel 'Cumulative events'\n");
            writer.write("plot '" + stem + "-types.dat' using 1:4 with lines title 'SE', \\\n");
            writer.write("     '" + stem + "-types.dat' using 1:5 with lines title 'FE', \\\n");
            writer.write("     '" + stem + "-types.dat' using 1:6 with lines title 'DE', \\\n");
            writer.write("     '" + stem + "-types.dat' using 1:7 with lines title 'SR', \\\n");
            writer.write("     '" + stem + "-types.dat' using 1:8 with lines title 'SX', \\\n");
            writer.write("     '" + stem + "-types.dat' using 1:9 with lines title 'UX'\n");
        }
    }

    private static void writeSummary(BufferedWriter writer, EEProblemSpec spec, int dimension, int populationSize,
                                     int maxEvaluations, LimitSetting limitSetting, MetricAccumulator accumulator) throws IOException {
        writer.write(String.format(Locale.US, "%s,%d,%d,%d,%s,%d,%.8f,%.8f,%.8f,%.8f,%.12f,%.12f",
                spec.getName(), dimension, populationSize, maxEvaluations, limitSetting.getLabel(), accumulator.size(),
                accumulator.mean(EEMetrics::getExplorationRatio), accumulator.stdev(EEMetrics::getExplorationRatio),
                accumulator.mean(EEMetrics::getExploitationRatio), accumulator.stdev(EEMetrics::getExploitationRatio),
                accumulator.mean(EEMetrics::getBestFitness), accumulator.stdev(EEMetrics::getBestFitness)));
        writer.newLine();
    }

    private static void writeRatios(BufferedWriter writer, EEProblemSpec spec, int dimension, int populationSize,
                                    int maxEvaluations, LimitSetting limitSetting, MetricAccumulator accumulator) throws IOException {
        writer.write(String.format(Locale.US, "%s,%d,%d,%d,%s,%d,%s,%s,%s,%s,%s,%s",
                spec.getName(), dimension, populationSize, maxEvaluations, limitSetting.getLabel(), accumulator.size(),
                pair(accumulator, EEMetrics::getSuccessfulExplorationRatio),
                pair(accumulator, EEMetrics::getFailedExplorationRatio),
                pair(accumulator, EEMetrics::getDeceptiveExplorationRatio),
                pair(accumulator, EEMetrics::getSuccessfulRejectionRatio),
                pair(accumulator, EEMetrics::getSuccessfulExploitationRatio),
                pair(accumulator, EEMetrics::getUnsuccessfulExploitationRatio)));
        writer.newLine();
    }

    private static String pair(MetricAccumulator accumulator, ToDoubleFunction<EEMetrics> extractor) {
        return String.format(Locale.US, "%.8f,%.8f", accumulator.mean(extractor), accumulator.stdev(extractor));
    }

    private static void writeTableHeader(BufferedWriter writer) throws IOException {
        writer.write("\\begin{longtable}{lrrrrrrrrrrrrr}\n");
        writer.write("Problem & D & Pop. & FEs & Limit & SE & FE & DE & SR & SX & UX & XPL & XPT & Best \\\\ \\hline\n");
        writer.write("\\endfirsthead\n");
        writer.write("Problem & D & Pop. & FEs & Limit & SE & FE & DE & SR & SX & UX & XPL & XPT & Best \\\\ \\hline\n");
        writer.write("\\endhead\n");
    }

    private static void writeTableRow(BufferedWriter writer, EEProblemSpec spec, int dimension, int populationSize,
                                      int maxEvaluations, LimitSetting limitSetting,
                                      MetricAccumulator accumulator) throws IOException {
        writer.write(String.format(Locale.US,
                "%s & %d & %d & %d & %s & %.4f $\\pm$ %.4f & %.4f $\\pm$ %.4f & %.4f $\\pm$ %.4f & %.4f $\\pm$ %.4f & %.4f $\\pm$ %.4f & %.4f $\\pm$ %.4f & %.4f $\\pm$ %.4f & %.4f $\\pm$ %.4f & %.6g $\\pm$ %.6g \\\\%n",
                spec.getName(), dimension, populationSize, maxEvaluations, limitSetting.getLabel(),
                accumulator.mean(EEMetrics::getSuccessfulExplorationRatio), accumulator.stdev(EEMetrics::getSuccessfulExplorationRatio),
                accumulator.mean(EEMetrics::getFailedExplorationRatio), accumulator.stdev(EEMetrics::getFailedExplorationRatio),
                accumulator.mean(EEMetrics::getDeceptiveExplorationRatio), accumulator.stdev(EEMetrics::getDeceptiveExplorationRatio),
                accumulator.mean(EEMetrics::getSuccessfulRejectionRatio), accumulator.stdev(EEMetrics::getSuccessfulRejectionRatio),
                accumulator.mean(EEMetrics::getSuccessfulExploitationRatio), accumulator.stdev(EEMetrics::getSuccessfulExploitationRatio),
                accumulator.mean(EEMetrics::getUnsuccessfulExploitationRatio), accumulator.stdev(EEMetrics::getUnsuccessfulExploitationRatio),
                accumulator.mean(EEMetrics::getExplorationRatio), accumulator.stdev(EEMetrics::getExplorationRatio),
                accumulator.mean(EEMetrics::getExploitationRatio), accumulator.stdev(EEMetrics::getExploitationRatio),
                accumulator.mean(EEMetrics::getBestFitness), accumulator.stdev(EEMetrics::getBestFitness)));
    }

    private static void writeTableFooter(BufferedWriter writer) throws IOException {
        writer.write("\\end{longtable}\n");
    }
}
