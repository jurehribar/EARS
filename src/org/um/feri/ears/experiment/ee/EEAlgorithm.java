package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.abc.ABCLogging;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.de.DELogging;

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
    };

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
