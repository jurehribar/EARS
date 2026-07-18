package org.um.feri.ears.experiment.ee;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class MetricAccumulator {

    private final List<EEMetrics> all = new ArrayList<>();

    public void add(EEMetrics metrics) {
        all.add(metrics);
    }

    public int size() {
        return all.size();
    }

    public double mean(ToDoubleFunction<EEMetrics> extractor) {
        if (all.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (EEMetrics metrics : all) {
            sum += extractor.applyAsDouble(metrics);
        }
        return sum / all.size();
    }

    public double stdev(ToDoubleFunction<EEMetrics> extractor) {
        if (all.size() < 2) {
            return 0.0;
        }
        double mean = mean(extractor);
        double sum = 0.0;
        for (EEMetrics metrics : all) {
            double diff = extractor.applyAsDouble(metrics) - mean;
            sum += diff * diff;
        }
        return Math.sqrt(sum / (all.size() - 1));
    }
}
