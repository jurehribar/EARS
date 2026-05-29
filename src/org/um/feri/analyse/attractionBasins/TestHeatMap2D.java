package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestHeatMap2D {

    public static void main(String[] args) throws Exception {
        PipelineConfig config = PipelineConfig.load();

        // Problem is supplied via Gradle system property -DproblemName=X -DproblemDims=2
        String problemName = System.getProperty("problemName");
        int dims = Integer.parseInt(System.getProperty("problemDims", "2"));

        if (problemName == null || problemName.isEmpty())
            throw new IllegalArgumentException(
                "System property -DproblemName is required. " +
                "Run via Gradle: gradlew runHeatMap_<ProblemName>");

        DoubleProblem problem = ProblemFactory.create(problemName, dims);

        File directory = new File(config.heatmapDir);
        if (!directory.exists()) directory.mkdirs();

        System.out.println("Generating heatmap for: " + problem.getName()
                + "  resolution=" + config.resolution);

        List<Double> step = new ArrayList<>();
        step.add((problem.upperLimit.get(0) - problem.lowerLimit.get(0)) / (double) config.resolution);
        step.add((problem.upperLimit.get(1) - problem.lowerLimit.get(1)) / (double) config.resolution);

        HeatMap2D heatMap2D = new HeatMap2D(step, problem);
        heatMap2D.writeCompressedThisToFile(config.heatmapDir + "/" + problem.getName() + "Compressed.object");
        heatMap2D.writeEvalsAndScript(config.heatmapDir + "/", problem.getName());
        System.out.println(heatMap2D.toString());
        System.out.println("Heatmap done: " + problem.getName());
    }
}
