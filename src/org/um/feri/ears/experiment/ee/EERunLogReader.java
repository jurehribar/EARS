package org.um.feri.ears.experiment.ee;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EERunLogReader {

    private EERunLogReader() {
    }

    public static List<EELogNode> read(Path file) throws IOException {
        List<EELogNode> nodes = new ArrayList<>();
        Map<Long, EELogNode> byId = new HashMap<>();

        for (String line : Files.readAllLines(file)) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] parts = line.split(";", -1);
            if (parts.length < 3) {
                continue;
            }

            long id = Long.parseLong(parts[0].trim());
            double fitness = Double.parseDouble(parts[1].trim());
            double[] variables = parseVariables(parts[2]);
            List<Long> parentIds = parts.length > 3 ? parseParentIds(parts[3]) : new ArrayList<>();
            EELogNode node = new EELogNode(id, fitness, variables, parentIds);

            for (Long parentId : parentIds) {
                EELogNode parent = byId.get(parentId);
                if (parent != null) {
                    node.addCandidateParent(parent);
                }
            }

            nodes.add(node);
            byId.put(id, node);
        }

        return nodes;
    }

    private static double[] parseVariables(String raw) {
        String cleaned = raw.trim().replace("[", "").replace("]", "");
        if (cleaned.isEmpty()) {
            return new double[0];
        }
        String[] values = cleaned.split(",");
        double[] variables = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            variables[i] = Double.parseDouble(values[i].trim());
        }
        return variables;
    }

    private static List<Long> parseParentIds(String raw) {
        List<Long> ids = new ArrayList<>();
        String cleaned = raw.trim().replace("[", "").replace("]", "");
        if (cleaned.isEmpty()) {
            return ids;
        }
        for (String id : cleaned.split(",")) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty()) {
                ids.add(Long.parseLong(trimmed));
            }
        }
        return ids;
    }
}
