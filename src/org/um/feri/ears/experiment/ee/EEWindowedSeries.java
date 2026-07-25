package org.um.feri.ears.experiment.ee;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Writes the 100-evaluation window ratios used by the original
 * exploration-and-exploitation article pipeline.
 */
final class EEWindowedSeries {

    static final int WINDOW_SIZE = 100;

    private EEWindowedSeries() {
    }

    static void write(Path seriesDir, String stem, List<EELogNode> nodes) throws IOException {
        List<Window> windows = calculate(nodes, WINDOW_SIZE);
        writeExplorationRatios(seriesDir.resolve(stem + "-window-exploration-ratios.dat"), windows);
        writeExplorationExploitationRatios(
                seriesDir.resolve(stem + "-window-exploration-exploitation-ratios.dat"), windows);
        writeGnuplotScripts(seriesDir, stem);
    }

    static List<Window> calculate(List<EELogNode> nodes, int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }

        List<EELogNode> ordered = new ArrayList<>(nodes);
        ordered.sort(Comparator.comparingLong(EELogNode::getId));
        List<Window> windows = new ArrayList<>();

        int successfulExploration = 0;
        int failedExploration = 0;
        int deceptiveExploration = 0;
        int successfulRejection = 0;
        int successfulExploitation = 0;
        int unsuccessfulExploitation = 0;
        int nodesInWindow = 0;
        long processedNodes = 0;

        for (EELogNode node : ordered) {
            nodesInWindow++;
            processedNodes++;
            switch (node.getType()) {
                case SUCCESSFUL_EXPLORATION:
                    successfulExploration++;
                    break;
                case FAILED_EXPLORATION:
                    failedExploration++;
                    break;
                case DECEPTIVE_EXPLORATION:
                    deceptiveExploration++;
                    break;
                case SUCCESSFUL_REJECTION:
                    successfulRejection++;
                    break;
                case SUCCESSFUL_EXPLOITATION:
                    successfulExploitation++;
                    break;
                case UNSUCCESSFUL_EXPLOITATION:
                    unsuccessfulExploitation++;
                    break;
                default:
                    break;
            }

            if (nodesInWindow == windowSize) {
                windows.add(new Window(processedNodes, nodesInWindow,
                        successfulExploration, failedExploration, deceptiveExploration, successfulRejection,
                        successfulExploitation, unsuccessfulExploitation));
                successfulExploration = 0;
                failedExploration = 0;
                deceptiveExploration = 0;
                successfulRejection = 0;
                successfulExploitation = 0;
                unsuccessfulExploitation = 0;
                nodesInWindow = 0;
            }
        }

        // The article pipeline emits complete 100-evaluation blocks only.
        return windows;
    }

    private static void writeExplorationRatios(Path file, List<Window> windows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("evaluation_end failed_exploration successful_exploration deceptive_exploration successful_rejection");
            writer.newLine();
            for (Window window : windows) {
                writer.write(String.format(Locale.US, "%d %.12f %.12f %.12f %.12f",
                        window.evaluationEnd(), window.failedExplorationRatio(),
                        window.successfulExplorationRatio(), window.deceptiveExplorationRatio(),
                        window.successfulRejectionRatio()));
                writer.newLine();
            }
        }
    }

    private static void writeExplorationExploitationRatios(Path file, List<Window> windows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("evaluation_end exploration exploitation successful_exploitation unsuccessful_exploitation");
            writer.newLine();
            for (Window window : windows) {
                writer.write(String.format(Locale.US, "%d %.12f %.12f %.12f %.12f",
                        window.evaluationEnd(), window.explorationRatio(), window.exploitationRatio(),
                        window.successfulExploitationRatio(), window.unsuccessfulExploitationRatio()));
                writer.newLine();
            }
        }
    }

    private static void writeGnuplotScripts(Path seriesDir, String stem) throws IOException {
        String explorationData = stem + "-window-exploration-ratios.dat";
        String overviewData = stem + "-window-exploration-exploitation-ratios.dat";

        try (BufferedWriter writer = Files.newBufferedWriter(seriesDir.resolve(stem + "-window-exploration-types.plt"))) {
            writePlotHeader(writer, stem + "-window-exploration-types.png");
            writer.write("plot '" + explorationData + "' using 1:2 with lines linewidth 2 lc rgb 'blue' title 'FailedExpl', \\\n");
            writer.write("     '" + explorationData + "' using 1:3 with lines linewidth 2 lc rgb 'red' title 'SuccesExpl', \\\n");
            writer.write("     '" + explorationData + "' using 1:4 with lines linewidth 2 lc rgb 'green' title 'DeceptiveExpl', \\\n");
            writer.write("     '" + explorationData + "' using 1:5 with lines linewidth 2 lc rgb 'orange' title 'SuccesfullReject'\n");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(seriesDir.resolve(stem + "-window-xpl-xpt.plt"))) {
            writePlotHeader(writer, stem + "-window-xpl-xpt.png");
            writer.write("plot '" + overviewData + "' using 1:2 with lines linewidth 2 lc rgb 'red' title 'Exploration', \\\n");
            writer.write("     '" + overviewData + "' using 1:3 with lines linewidth 2 lc rgb 'blue' title 'Exploitation'\n");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(seriesDir.resolve(stem + "-window-exploitation-types.plt"))) {
            writePlotHeader(writer, stem + "-window-exploitation-types.png");
            writer.write("plot '" + overviewData + "' using 1:4 with lines linewidth 2 lc rgb 'red' title 'SuccesfullExploit', \\\n");
            writer.write("     '" + overviewData + "' using 1:5 with lines linewidth 2 lc rgb 'blue' title 'UnSuccesfullExploit'\n");
        }
    }

    private static void writePlotHeader(BufferedWriter writer, String outputFile) throws IOException {
        writer.write("set term pngcairo size 1200,800\n");
        writer.write("set output '" + outputFile + "'\n");
        writer.write("set xlabel 'Evaluation index (100-evaluation windows)'\n");
        writer.write("set ylabel 'Ratio'\n");
        writer.write("set yrange [0:1]\n");
    }

    static final class Window {
        private final long evaluationEnd;
        private final int nodeCount;
        private final int successfulExploration;
        private final int failedExploration;
        private final int deceptiveExploration;
        private final int successfulRejection;
        private final int successfulExploitation;
        private final int unsuccessfulExploitation;

        Window(long evaluationEnd, int nodeCount, int successfulExploration, int failedExploration,
               int deceptiveExploration, int successfulRejection, int successfulExploitation,
               int unsuccessfulExploitation) {
            this.evaluationEnd = evaluationEnd;
            this.nodeCount = nodeCount;
            this.successfulExploration = successfulExploration;
            this.failedExploration = failedExploration;
            this.deceptiveExploration = deceptiveExploration;
            this.successfulRejection = successfulRejection;
            this.successfulExploitation = successfulExploitation;
            this.unsuccessfulExploitation = unsuccessfulExploitation;
        }

        long evaluationEnd() { return evaluationEnd; }

        double explorationRatio() { return ratio(explorationCount(), nodeCount); }

        double exploitationRatio() { return 1.0 - explorationRatio(); }

        double successfulExplorationRatio() { return ratio(successfulExploration, explorationCount()); }

        double failedExplorationRatio() { return ratio(failedExploration, explorationCount()); }

        double deceptiveExplorationRatio() { return ratio(deceptiveExploration, explorationCount()); }

        double successfulRejectionRatio() { return ratio(successfulRejection, explorationCount()); }

        double successfulExploitationRatio() { return ratioOrZero(successfulExploitation, exploitationCount()); }

        double unsuccessfulExploitationRatio() { return ratioOrZero(unsuccessfulExploitation, exploitationCount()); }

        private int explorationCount() {
            return successfulExploration + failedExploration + deceptiveExploration + successfulRejection;
        }

        private int exploitationCount() {
            return successfulExploitation + unsuccessfulExploitation;
        }

        private static double ratio(int numerator, int denominator) {
            return (double) numerator / denominator;
        }

        private static double ratioOrZero(int numerator, int denominator) {
            return denominator == 0 ? 0.0 : ratio(numerator, denominator);
        }
    }
}
