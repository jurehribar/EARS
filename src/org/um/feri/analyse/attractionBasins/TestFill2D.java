package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;

import java.io.File;

public class TestFill2D {

    public static void main(String[] args) throws Exception {
        PipelineConfig config = PipelineConfig.load();

        String problemName = System.getProperty("problemName");
        int dims = Integer.parseInt(System.getProperty("problemDims", "2"));

        if (problemName == null || problemName.isEmpty())
            throw new IllegalArgumentException(
                "System property -DproblemName is required. " +
                "Run via Gradle: gradlew runFill_<ProblemName>");

        DoubleProblem problem = ProblemFactory.create(problemName, dims);

        File directory = new File(config.scanlineDir);
        if (!directory.exists()) directory.mkdirs();

        System.out.println("Running scanline fill for: " + problem.getName()
                + "  minPlateauSize=" + config.minPlateauSize
                + "  plateauEpsilon=" + config.plateauEpsilon);

        Fill2D fill2D = new Fill2D(
                "scanline",
                config.heatmapDir + "/" + problem.getName() + "Compressed.object",
                true,
                config.minPlateauSize,
                config.plateauEpsilon);

        fill2D.writeCompressedThisToFile(config.scanlineDir + "/" + problem.getName() + "Compressed.object");
        fill2D.writeFillMapValues(config.scanlineDir + "/" + problem.getName() + ".data");
        System.out.println(fill2D.toString());
        System.out.println("Fill done: " + problem.getName());
    }
}
