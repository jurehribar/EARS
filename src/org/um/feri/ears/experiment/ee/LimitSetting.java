package org.um.feri.ears.experiment.ee;

public enum LimitSetting {
    ZERO("0"),
    K("K"),
    L100("100"),
    L250("250"),
    L750("750"),
    INFINITY("INF");

    private final String label;

    LimitSetting(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public int resolve(int populationSize, int dimension) {
        switch (this) {
            case ZERO:
                return 0;
            case K:
                return (populationSize * dimension) / 2;
            case L100:
                return 100;
            case L250:
                return 250;
            case L750:
                return 750;
            case INFINITY:
                return Integer.MAX_VALUE;
            default:
                throw new IllegalStateException("Unknown limit setting: " + this);
        }
    }
}
