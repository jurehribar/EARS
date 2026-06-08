package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Combined attraction basin pipeline — runs all three stages (HeatMap, Fill, Measure)
 * entirely in memory for a single problem. No intermediate files are written to disk.
 * The only output is the two PNG pictures:
 *   [picturesDir]/[ProblemName].png
 *   [picturesDir]/[ProblemName]_plateaus.png
 *
 * Usage (from Gradle):
 *   ./gradlew runCombinedTest_Rastrigin
 *   ./gradlew runAllCombinedTests
 *
 * Same config.properties parameters as the full pipeline:
 *   resolution, minPlateauSize, plateauEpsilon, picturesDir, problems
 */
public class CombinedTest {

    public static void main(String[] args) throws Exception {
        PipelineConfig config = PipelineConfig.load();

        String problemName = System.getProperty("problemName");
        int dims = Integer.parseInt(System.getProperty("problemDims", "2"));

        if (problemName == null || problemName.isEmpty())
            throw new IllegalArgumentException(
                "System property -DproblemName is required. " +
                "Run via Gradle: gradlew runCombinedTest_<ProblemName>");

        DoubleProblem problem = ProblemFactory.create(problemName, dims);

        // Ensure output directory exists
        File directory = new File(config.picturesDir);
        if (!directory.exists()) directory.mkdirs();

        runInMemory(problem, config);
    }

    /**
     * Run the full pipeline for a single problem entirely in memory.
     * No heatmap or scanline files are written to disk.
     */
    public static void runInMemory(DoubleProblem problem, PipelineConfig config) throws Exception {
        System.out.println("=== CombinedTest: " + problem.getName() + " ===");
        System.out.println("  resolution     = " + config.resolution);
        System.out.println("  minPlateauSize = " + config.minPlateauSize);
        System.out.println("  plateauEpsilon = " + config.plateauEpsilon);

        // ── Stage 1: HeatMap (in memory) ─────────────────────────────────
        List<Double> step = new ArrayList<>();
        step.add((problem.upperLimit.get(0) - problem.lowerLimit.get(0)) / (double) config.resolution);
        step.add((problem.upperLimit.get(1) - problem.lowerLimit.get(1)) / (double) config.resolution);

        HeatMap2D heatMap = new HeatMap2D(step, problem);
        System.out.println(heatMap.toString());

        // ── Stage 2: Fill2D (in memory, from HeatMap2D directly) ─────────
        Fill2D fill2D = new Fill2D(
                "scanline",
                heatMap,
                true,                       // smoothBoundaries
                config.minPlateauSize,
                config.plateauEpsilon);
        System.out.println(fill2D.toString());

        // ── Stage 3: Render images (only disk output) ─────────────────────
        Measure.drawAttractionBasins(
                new Fill2D[]{fill2D},
                new DoubleProblem[]{problem},
                config);

        System.out.println("=== CombinedTest finished: " + problem.getName() + " ===");
    }
}

