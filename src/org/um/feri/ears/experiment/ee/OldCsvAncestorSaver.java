package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.Solution;
import org.um.feri.ears.problems.Task;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes ancestry logs in the legacy exploration-and-exploitation CSV format:
 * id;fitness;[x1, x2, ...];[parentId,...]
 */
public final class OldCsvAncestorSaver {

    private OldCsvAncestorSaver() {
    }

    public static void save(Path outputFile, Task<NumberSolution<Double>, DoubleProblem> task) throws IOException {
        Path parentDir = outputFile.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            ArrayList<Solution> ancestors = task.getAncestors();
            if (ancestors == null) {
                return;
            }

            for (Solution ancestor : ancestors) {
                writer.write(Long.toString(ancestor.getID()));
                writer.write(';');
                writer.write(Double.toString(ancestor.getEval()));
                writer.write(';');
                writeVariables(writer, (NumberSolution<?>) ancestor);
                writer.write(';');
                writeParents(writer, ancestor.parents);
                writer.newLine();
            }
        }
    }

    public static void save(String outputFile, Task<NumberSolution<Double>, DoubleProblem> task) throws IOException {
        save(Path.of(outputFile), task);
    }

    private static void writeVariables(BufferedWriter writer, NumberSolution<?> solution) throws IOException {
        writer.write('[');
        for (int i = 0; i < solution.getVariables().size(); i++) {
            Number value = (Number) solution.getVariables().get(i);
            writer.write(Double.toString(value.doubleValue()));
            if (i + 1 < solution.getVariables().size()) {
                writer.write(", ");
            }
        }
        writer.write(']');
    }

    private static void writeParents(BufferedWriter writer, List<Solution> parents) throws IOException {
        if (parents == null || parents.isEmpty()) {
            return;
        }

        writer.write('[');
        for (int i = 0; i < parents.size(); i++) {
            writer.write(Long.toString(parents.get(i).getID()));
            if (i + 1 < parents.size()) {
                writer.write(',');
            }
        }
        writer.write(']');
    }
}
