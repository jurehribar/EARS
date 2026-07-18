package org.um.feri.ears.experiment.ee;

import org.um.feri.ears.problems.unconstrained.Ackley;
import org.um.feri.ears.problems.unconstrained.Griewank;
import org.um.feri.ears.problems.unconstrained.Rastrigin;
import org.um.feri.ears.problems.unconstrained.cec2017.F10;
import org.um.feri.ears.problems.unconstrained.cec2017.F20;
import org.um.feri.ears.problems.unconstrained.cec2017.F3;

import java.util.Arrays;
import java.util.List;

public final class EEProblemFactory {

    private EEProblemFactory() {
    }

    public static List<EEProblemSpec> all() {
        return Arrays.asList(
                new EEProblemSpec("Rastrigin", 0.01, Rastrigin::new),
                new EEProblemSpec("Ackley", 0.006, Ackley::new),
                new EEProblemSpec("Griewank", 0.12, Griewank::new),
                new EEProblemSpec("F03", 0.02, F3::new),
                new EEProblemSpec("F10", 0.02, F10::new),
                new EEProblemSpec("F20", 0.02, F20::new)
        );
    }
}
