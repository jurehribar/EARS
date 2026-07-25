package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.abc.ABCLogging;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.de.DELogging;
import org.um.feri.ears.algorithms.so.de.mde.GradientDescentLocalSearch;
import org.um.feri.ears.algorithms.so.de.mde.MDELogging;

public enum EEAlgorithm {
    ABC("ABC", true) {
        @Override
        public NumberAlgorithm create(int populationSize, LimitSetting limitSetting, int dimension) {
            return new ABCLogging(populationSize, limitSetting.resolve(populationSize, dimension));
        }

        @Override
        public String filePrefix(LimitSetting limitSetting) {
            return "ABClim" + limitSetting.getLabel();
        }
    },
    DE_RAND_1_BIN("DE-rand-1-bin", false) {
        @Override
        public NumberAlgorithm create(int populationSize, LimitSetting limitSetting, int dimension) {
            return new DELogging(DE.Strategy.DE_RAND_1_BIN, populationSize);
        }

        @Override
        public String filePrefix(LimitSetting limitSetting) {
            return getLabel();
        }
    },
    MDE_RAND_1_BIN("MDE-rand-1-bin", false) {
        @Override
        public NumberAlgorithm create(int populationSize, LimitSetting limitSetting, int dimension) {
            return createMde(DE.Strategy.DE_RAND_1_BIN, populationSize);
        }

        @Override
        public String filePrefix(LimitSetting limitSetting) {
            return getLabel();
        }
    },
    MDE_BEST_1_BIN("MDE-best-1-bin", false) {
        @Override
        public NumberAlgorithm create(int populationSize, LimitSetting limitSetting, int dimension) {
            return createMde(DE.Strategy.DE_BEST_1_BIN, populationSize);
        }

        @Override
        public String filePrefix(LimitSetting limitSetting) {
            return getLabel();
        }
    };

    private static final double MDE_F = 0.5;
    private static final double MDE_CR = 0.9;
    private static final int MDE_ELITE_SIZE = 1;
    private static final int MDE_LOCAL_SEARCH_FREQUENCY = 1;

    private final String label;
    private final boolean usesLimit;

    EEAlgorithm(String label, boolean usesLimit) {
        this.label = label;
        this.usesLimit = usesLimit;
    }

    public abstract NumberAlgorithm create(int populationSize, LimitSetting limitSetting, int dimension);

    public abstract String filePrefix(LimitSetting limitSetting);

    public String getLabel() {
        return label;
    }

    public boolean usesLimit() {
        return usesLimit;
    }

    private static NumberAlgorithm createMde(DE.Strategy strategy, int populationSize) {
        return new MDELogging(strategy, populationSize, MDE_F, MDE_CR,
                MDE_ELITE_SIZE, MDE_LOCAL_SEARCH_FREQUENCY, new GradientDescentLocalSearch());
    }

    public static EEAlgorithm fromLabel(String label) {
        for (EEAlgorithm algorithm : values()) {
            if (algorithm.label.equalsIgnoreCase(label) || algorithm.name().equalsIgnoreCase(label)
                    || algorithm.label.replace("-", "_").equalsIgnoreCase(label)) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Unknown EE algorithm: " + label);
    }
}
