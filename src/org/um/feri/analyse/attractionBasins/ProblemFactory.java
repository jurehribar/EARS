package org.um.feri.analyse.attractionBasins;
import org.um.feri.ears.problems.DoubleProblem;
import java.lang.reflect.Constructor;
public class ProblemFactory {
    private static final String[] PACKAGES = {
        "org.um.feri.ears.problems.unconstrained",
        "org.um.feri.ears.problems.misc",
        "org.um.feri.ears.problems.unconstrained.cec2005"
    };
    public static DoubleProblem create(String name, int dims) throws Exception {
        for (String pkg : PACKAGES) {
            try {
                Class<?> clazz = Class.forName(pkg + "." + name);
                try {
                    Constructor<?> c = clazz.getConstructor(int.class);
                    return (DoubleProblem) c.newInstance(dims);
                } catch (NoSuchMethodException ignored) {}
                Constructor<?> c = clazz.getConstructor();
                return (DoubleProblem) c.newInstance();
            } catch (ClassNotFoundException ignored) {}
        }
        throw new IllegalArgumentException(
            "Problem class '" + name + "' not found. Add its package to ProblemFactory.PACKAGES if needed.");
    }
}