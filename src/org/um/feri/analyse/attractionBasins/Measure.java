package org.um.feri.analyse.attractionBasins;

import org.um.feri.ears.problems.DoubleProblem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

public class Measure {

    static public void main(String[] args) throws Exception {
        PipelineConfig config = PipelineConfig.load();

        String problemName = System.getProperty("problemName");
        int dims = Integer.parseInt(System.getProperty("problemDims", "2"));

        if (problemName == null || problemName.isEmpty())
            throw new IllegalArgumentException(
                "System property -DproblemName is required. " +
                "Run via Gradle: gradlew runMeasure_<ProblemName>");

        DoubleProblem problem = ProblemFactory.create(problemName, dims);

        File directory = new File(config.picturesDir);
        if (!directory.exists()) directory.mkdirs();

        drawAttractionBasins(new DoubleProblem[]{problem}, config);
    }

    public static void drawAttractionBasins(DoubleProblem[] problems, PipelineConfig config) throws Exception {
        for (int i = 0; i < problems.length; i++) {
            System.out.println(problems[i].getName() + " (" + (i + 1) + "/" + problems.length + ")");
            Fill2D tmp = new Fill2D();
            Fill2D fill2D = tmp.readCompressedFileToObject(
                    config.scanlineDir + "/" + problems[i].getName() + "Compressed.object");
            int width  = fill2D.map.length;
            int height = fill2D.map[0].length;

            Random gen = new Random(42);
            int fontSize = 14 * width / 500;
            Font myFont = new Font("Times", Font.BOLD, fontSize);

            HashMap<Integer, Color> basinColorMap   = new HashMap<>();
            HashMap<Integer, Color> plateauColorMap = new HashMap<>();
            basinColorMap.put(0, Color.BLACK);

            float plateauHue = 0f;
            final float goldenRatio = 0.618033988f;

            // ── IMAGE 1: Basin coloring ────────────────────────────────────────
            try {
                BufferedImage bi = new BufferedImage(width + 200, height + 200, BufferedImage.TYPE_INT_ARGB);
                Graphics2D ig2 = bi.createGraphics();
                int x = 0, y = 0;
                ig2.setFont(myFont);
                ig2.setColor(Color.WHITE);
                ig2.fillRect(0, 0, bi.getWidth(), bi.getHeight());

                int prevVal = 0;
                for (x = 0; x < width; x++) {
                    for (y = 0; y < height; y++) {
                        int val = fill2D.map[x][y].color;
                        if (prevVal != val) {
                            prevVal = val;
                            if (!basinColorMap.containsKey(val))
                                basinColorMap.put(val, new Color(gen.nextInt(256), gen.nextInt(256), gen.nextInt(256)));
                            ig2.setColor(basinColorMap.get(val));
                        }
                        ig2.drawLine(x + 180, height - y, x + 180, height - y);
                    }
                }
                ig2.setColor(Color.BLACK);
                ig2.drawString(problems[i].upperLimit.get(0).toString(), 0, fontSize);
                ig2.drawString("0", 0, y / 2 + fontSize);
                ig2.drawString(problems[i].lowerLimit.get(0).toString(), 0, y);
                ig2.drawString(problems[i].lowerLimit.get(0).toString(), fontSize, y + fontSize);
                ig2.drawString("0", x / 2, y + fontSize);
                ig2.drawString(problems[i].upperLimit.get(0).toString(), x - fontSize, y + fontSize);
                ImageIO.write(bi, "PNG", new File(config.picturesDir + "/" + fill2D.problemName + ".png"));
                System.out.println("Basin image saved: " + fill2D.problemName + ".png");
            } catch (IOException ie) {
                ie.printStackTrace();
            }

            // ── IMAGE 2: Plateau coloring ──────────────────────────────────────
            try {
                BufferedImage bi2 = new BufferedImage(width + 200, height + 200, BufferedImage.TYPE_INT_ARGB);
                Graphics2D ig2 = bi2.createGraphics();
                int x = 0, y = 0;
                ig2.setFont(myFont);
                ig2.setColor(Color.WHITE);
                ig2.fillRect(0, 0, bi2.getWidth(), bi2.getHeight());

                for (x = 0; x < width; x++) {
                    for (y = 0; y < height; y++) {
                        int plateauId = fill2D.map[x][y].plateau;
                        int basinId   = fill2D.map[x][y].color;
                        Color c;
                        if (basinId == 0) {
                            c = Color.BLACK;
                        } else if (plateauId > 0) {
                            if (!plateauColorMap.containsKey(plateauId)) {
                                plateauHue = (plateauHue + goldenRatio) % 1.0f;
                                plateauColorMap.put(plateauId, Color.getHSBColor(plateauHue, 0.9f, 0.95f));
                            }
                            c = plateauColorMap.get(plateauId);
                        } else {
                            c = Color.LIGHT_GRAY;
                        }
                        ig2.setColor(c);
                        ig2.drawLine(x + 180, height - y, x + 180, height - y);
                    }
                }
                ig2.setColor(Color.BLACK);
                ig2.drawString(problems[i].upperLimit.get(0).toString(), 0, fontSize);
                ig2.drawString("0", 0, y / 2 + fontSize);
                ig2.drawString(problems[i].lowerLimit.get(0).toString(), 0, y);
                ig2.drawString(problems[i].lowerLimit.get(0).toString(), fontSize, y + fontSize);
                ig2.drawString("0", x / 2, y + fontSize);
                ig2.drawString(problems[i].upperLimit.get(0).toString(), x - fontSize, y + fontSize);

                int legendX = width + 185;
                int legendY = fontSize * 2;
                ig2.drawString("Plateaus:", legendX, legendY);
                for (java.util.Map.Entry<Integer, Color> entry : plateauColorMap.entrySet()) {
                    legendY += fontSize + 4;
                    ig2.setColor(entry.getValue());
                    ig2.fillRect(legendX, legendY - fontSize, fontSize, fontSize);
                    ig2.setColor(Color.BLACK);
                    ig2.drawRect(legendX, legendY - fontSize, fontSize, fontSize);
                    ig2.drawString("P" + entry.getKey(), legendX + fontSize + 4, legendY);
                }
                if (plateauColorMap.isEmpty()) {
                    legendY += fontSize + 4;
                    ig2.drawString("(none)", legendX, legendY);
                }
                ImageIO.write(bi2, "PNG", new File(config.picturesDir + "/" + fill2D.problemName + "_plateaus.png"));
                System.out.println("Plateau image saved: " + fill2D.problemName + "_plateaus.png");
                System.out.println("Distinct plateaus found: " + plateauColorMap.size());
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            System.out.println("Finished: " + problems[i].getName());
        }
    }

    public static String strround(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(value + 1e-6);
    }
}
