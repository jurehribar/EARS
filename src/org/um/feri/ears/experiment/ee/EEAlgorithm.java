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

    static final double DEFAULT_MDE_F = 0.5;
    static final double DEFAULT_MDE_CR = 0.9;
    static final int DEFAULT_MDE_ELITE_SIZE = 1;
    static final int DEFAULT_MDE_LOCAL_SEARCH_FREQUENCY = 1;

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

    public boolean isMde() {
        return this == MDE_RAND_1_BIN || this == MDE_BEST_1_BIN;
    }

    public NumberAlgorithm create(int populationSize, LimitSetting limitSetting, int dimension,
                                  int eliteSize, int localSearchFrequency) {
        return create(populationSize, limitSetting, dimension, eliteSize, localSearchFrequency,
                DEFAULT_MDE_F, DEFAULT_MDE_CR);
    }

    public NumberAlgorithm create(int populationSize, LimitSetting limitSetting, int dimension,
                                  int eliteSize, int localSearchFrequency, double f, double cr) {
        if (!isMde()) {
            return create(populationSize, limitSetting, dimension);
        }
        if (eliteSize < 0) {
            throw new IllegalArgumentException("MDE elite size must be non-negative");
        }
        if (eliteSize > populationSize) {
            throw new IllegalArgumentException("MDE elite size must not exceed the population size");
        }
        if (localSearchFrequency < 1) {
            throw new IllegalArgumentException("MDE local-search frequency must be positive");
        }
        DE.Strategy strategy = this == MDE_BEST_1_BIN
                ? DE.Strategy.DE_BEST_1_BIN
                : DE.Strategy.DE_RAND_1_BIN;
        return createMde(strategy, populationSize, eliteSize, localSearchFrequency, f, cr);
    }

    public String resultLabel(int eliteSize, int localSearchFrequency) {
        return resultLabel(eliteSize, localSearchFrequency, DEFAULT_MDE_F, DEFAULT_MDE_CR);
    }

    public String resultLabel(int eliteSize, int localSearchFrequency, double f, double cr) {
        if (!isMde()) {
            return getLabel();
        }
        return getLabel() + "-elite" + eliteSize + "-freq" + localSearchFrequency
                + "-F" + Double.toString(f) + "-CR" + Double.toString(cr);
    }

    private static NumberAlgorithm createMde(DE.Strategy strategy, int populationSize) {
        return createMde(strategy, populationSize, DEFAULT_MDE_ELITE_SIZE,
                DEFAULT_MDE_LOCAL_SEARCH_FREQUENCY, DEFAULT_MDE_F, DEFAULT_MDE_CR);
    }

    private static NumberAlgorithm createMde(DE.Strategy strategy, int populationSize,
                                             int eliteSize, int localSearchFrequency, double f, double cr) {
        return new MDELogging(strategy, populationSize, f, cr,
                eliteSize, localSearchFrequency, new GradientDescentLocalSearch());
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
