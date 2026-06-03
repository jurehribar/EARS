package org.um.feri.analyse.attractionBasins;
import java.io.*;
import java.util.*;
public class PipelineConfig {
    public final int    resolution;
    public final int    minPlateauSize;
    public final double plateauEpsilon;
    public final String heatmapDir;
    public final String scanlineDir;
    public final String picturesDir;
    public final List<String[]> problems;
    public static PipelineConfig load() throws IOException {
        Properties props = new Properties();
        File configFile = new File("config.properties");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            }
        } else {
            try (InputStream is = PipelineConfig.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                if (is == null)
                    throw new FileNotFoundException("config.properties not found.");
                props.load(is);
            }
        }
        return new PipelineConfig(props);
    }
    private PipelineConfig(Properties p) {
        resolution     = Integer.parseInt(p.getProperty("resolution",     "10000"));
        minPlateauSize = Integer.parseInt(p.getProperty("minPlateauSize", "1"));
        plateauEpsilon = Double.parseDouble(p.getProperty("plateauEpsilon", "0.0"));
        heatmapDir     = p.getProperty("heatmapDir", "C:/Dev/TestEARS/HeatMaps");
        scanlineDir    = p.getProperty("scanlineDir", "C:/Dev/TestEARS/ScanLine");
        picturesDir    = p.getProperty("picturesDir", "C:/Dev/TestEARS/Pictures");
        problems       = parseProblems(p.getProperty("problems", "Easom"));
    }
    private static List<String[]> parseProblems(String raw) {
        List<String[]> list = new ArrayList<>();
        for (String entry : raw.split(",")) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            String[] parts = entry.split(":");
            list.add(new String[]{parts[0].trim(), parts.length > 1 ? parts[1].trim() : "2"});
        }
        return list;
    }
}