package org.um.feri.ears.algorithms.so.de;

import org.um.feri.ears.algorithms.Algorithm;
import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.Solution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.util.random.RNG;

import java.util.ArrayList;
import java.util.List;

/**
 * Differential Evolution variant that preserves mutation parents for
 * exploration/exploitation ancestry analysis.
 */
public class DELogging extends NumberAlgorithm {

    @AlgorithmParameter(name = "population size")
    private int popSize;
    @AlgorithmParameter
    private DE.Strategy strategy;
    private final boolean targetParentOnly;

    private static final double Finit = 0.5;
    private static final double CRinit = 0.9;
    private static final double Fl = 0.1;
    private static final double Fu = 0.9;
    private static final double CRl = 0.0;
    private static final double CRu = 1.0;
    private static final double tao1 = 0.1;
    private static final double tao2 = 0.1;

    private DESolution[] c;
    private DESolution[] d;
    private DESolution[] pold;
    private DESolution[] pnew;
    private DESolution[] pswap;

    private int i, L, n;
    private int r1, r2, r3, r4, r5;
    private int D;

    private double[] tmp;
    private double tmpF, tmpCR;
    private double F, memF, CR, memCR;
    private DESolution best, bestit, bestI;

    public DELogging() {
        this(DE.Strategy.DE_RAND_1_BIN, 30, Finit, CRinit);
    }

    public DELogging(DE.Strategy strategy) {
        this(strategy, 30, Finit, CRinit);
    }

    public DELogging(DE.Strategy strategy, int popSize) {
        this(strategy, popSize, Finit, CRinit);
    }

    public DELogging(DE.Strategy strategy, int popSize, double F, double CR) {
        this(strategy, popSize, F, CR, false);
    }

    public DELogging(DE.Strategy strategy, int popSize, double F, double CR,
                     boolean targetParentOnly) {
        this.strategy = strategy;
        this.popSize = popSize;
        this.targetParentOnly = targetParentOnly;
        this.memF = F;
        this.memCR = CR;
        this.F = F;
        this.CR = CR;

        assert popSize > 5;

        au = new Author("matej", "matej.crepinsek@um.si");
        ai = new AlgorithmInfo(strategy.label + "Logging", strategy.label + " with ancestry logging",
                "Differential Evolution with mutation-parent logging for exploration/exploitation analysis.");
    }

    private void assignd(int dimension, double[] target, double[] source) {
        System.arraycopy(source, 0, target, 0, dimension);
    }

    public void init() throws StopCriterionException {
        D = task.problem.getNumberOfDimensions();
        F = memF;
        CR = memCR;
        tmp = new double[D];
        c = new DESolution[popSize];
        d = new DESolution[popSize];
        for (i = 0; i < popSize; i++) {
            c[i] = new DESolution(task.generateRandomEvaluatedSolution(), Finit, CRinit);
        }

        bestI = c[0];
        for (i = 0; i < popSize; i++) {
            if (task.problem.isFirstBetter(c[i], bestI)) {
                bestI = c[i];
            }
        }
        best = new DESolution(bestI);
        bestit = new DESolution(bestI);
    }

    @Override
    public NumberSolution<Double> execute(Task<NumberSolution<Double>, DoubleProblem> task) throws StopCriterionException {
        this.task = task;
        init();
        pold = c;
        pnew = d;

        while (!task.isStopCriterion()) {
            for (i = 0; i < popSize; i++) {
                if (task.isStopCriterion()) {
                    break;
                }

                selectRandomIndexes();
                List<Solution> parents = createTrialVector();

                for (int kk = 0; kk < D; kk++) {
                    tmp[kk] = task.problem.makeFeasible(tmp[kk], kk);
                }
                NumberSolution<Double> br = new NumberSolution<>(Util.toDoubleArrayList(tmp));
                br.parents = targetParentOnly ? parents(pold[i]) : parents;
                task.eval(br);

                DESolution trialCost = new DESolution(br, tmpF, tmpCR);
                if (task.problem.isFirstBetter(trialCost, pold[i])) {
                    pnew[i] = trialCost;
                    if (task.problem.isFirstBetter(trialCost, best)) {
                        best = new DESolution(trialCost);
                    }
                } else {
                    pnew[i] = new DESolution(pold[i]);
                }
            }

            bestit = new DESolution(best);
            pswap = pold;
            pold = pnew;
            pnew = pswap;
            if (displayData) {
                System.out.println(task.getNumberOfEvaluations() + " " + best);
            }
            task.incrementNumberOfIterations();
        }
        return best;
    }

    private void selectRandomIndexes() {
        do {
            r1 = RNG.nextInt(popSize);
        } while (r1 == i);

        do {
            r2 = RNG.nextInt(popSize);
        } while (r2 == i || r2 == r1);

        do {
            r3 = RNG.nextInt(popSize);
        } while (r3 == i || r3 == r1 || r3 == r2);

        do {
            r4 = RNG.nextInt(popSize);
        } while (r4 == i || r4 == r1 || r4 == r2 || r4 == r3);

        do {
            r5 = RNG.nextInt(popSize);
        } while (r5 == i || r5 == r1 || r5 == r2 || r5 == r3 || r5 == r4);
    }

    private List<Solution> createTrialVector() {
        tmpF = F;
        tmpCR = CR;

        if (strategy == DE.Strategy.DE_BEST_1_EXP) {
            bestOneExp();
            return parents(bestit, pold[r2], pold[r3]);
        } else if (strategy == DE.Strategy.DE_RAND_1_EXP) {
            randOneExp();
            return parents(pold[r1], pold[r2], pold[r3]);
        } else if (strategy == DE.Strategy.DE_RAND_TO_BEST_1_EXP) {
            randToBestOneExp();
            return parents(bestit, pold[r1], pold[r2]);
        } else if (strategy == DE.Strategy.DE_BEST_2_EXP) {
            bestTwoExp();
            return parents(bestit, pold[r1], pold[r2], pold[r3], pold[r4]);
        } else if (strategy == DE.Strategy.DE_RAND_2_EXP) {
            randTwoExp();
            return parents(pold[r5], pold[r1], pold[r2], pold[r3], pold[r4]);
        } else if (strategy == DE.Strategy.DE_BEST_1_BIN) {
            bestOneBin();
            return parents(bestit, pold[r2], pold[r3]);
        } else if (strategy == DE.Strategy.DE_RAND_1_BIN) {
            randOneBin();
            return parents(pold[r1], pold[r2], pold[r3]);
        } else if (strategy == DE.Strategy.JDE_RAND_1_BIN) {
            jdeRandOneBin();
            return parents(pold[r1], pold[r2], pold[r3]);
        } else if (strategy == DE.Strategy.DE_RAND_TO_BEST_1_BIN) {
            randToBestOneBin();
            return parents(bestit, pold[r1], pold[r2]);
        } else if (strategy == DE.Strategy.DE_BEST_2_BIN) {
            bestTwoBin();
            return parents(bestit, pold[r1], pold[r2], pold[r3], pold[r4]);
        }

        randTwoBin();
        return parents(pold[r5], pold[r1], pold[r2], pold[r3], pold[r4]);
    }

    private void bestOneExp() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        L = 0;
        do {
            tmp[n] = bestit.getValue(n) + F * (pold[r2].getValue(n) - pold[r3].getValue(n));
            n = (n + 1) % D;
            L++;
        } while (RNG.nextDouble() < CR && L < D);
    }

    private void randOneExp() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        L = 0;
        do {
            tmp[n] = pold[r1].getValue(n) + F * (pold[r2].getValue(n) - pold[r3].getValue(n));
            n = (n + 1) % D;
            L++;
        } while (RNG.nextDouble() < CR && L < D);
    }

    private void randToBestOneExp() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        L = 0;
        do {
            tmp[n] = tmp[n] + F * (bestit.getValue(n) - tmp[n]) + F * (pold[r1].getValue(n) - pold[r2].getValue(n));
            n = (n + 1) % D;
            L++;
        } while (RNG.nextDouble() < CR && L < D);
    }

    private void bestTwoExp() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        L = 0;
        do {
            tmp[n] = bestit.getValue(n) + (pold[r1].getValue(n) + pold[r2].getValue(n) - pold[r3].getValue(n) - pold[r4].getValue(n)) * F;
            n = (n + 1) % D;
            L++;
        } while (RNG.nextDouble() < CR && L < D);
    }

    private void randTwoExp() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        L = 0;
        do {
            tmp[n] = pold[r5].getValue(n) + (pold[r1].getValue(n) + pold[r2].getValue(n) - pold[r3].getValue(n) - pold[r4].getValue(n)) * F;
            n = (n + 1) % D;
            L++;
        } while (RNG.nextDouble() < CR && L < D);
    }

    private void bestOneBin() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        for (L = 0; L < D; L++) {
            if (RNG.nextDouble() < CR || L == D - 1) {
                tmp[n] = bestit.getValue(n) + F * (pold[r2].getValue(n) - pold[r3].getValue(n));
            }
            n = (n + 1) % D;
        }
    }

    private void randOneBin() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        for (L = 0; L < D; L++) {
            if (RNG.nextDouble() < CR || L == D - 1) {
                tmp[n] = pold[r1].getValue(n) + F * (pold[r2].getValue(n) - pold[r3].getValue(n));
            }
            n = (n + 1) % D;
        }
    }

    private void jdeRandOneBin() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        tmpF = pold[i].getF();
        tmpCR = pold[i].getCR();
        n = RNG.nextInt(D);
        if (RNG.nextDouble() < tao1) {
            F = Fl + RNG.nextDouble() * Fu;
            tmpF = F;
        } else {
            F = tmpF;
        }
        if (RNG.nextDouble() < tao2) {
            CR = CRl + RNG.nextDouble() * CRu;
            tmpCR = CR;
        } else {
            CR = tmpCR;
        }

        for (L = 0; L < D; L++) {
            if (RNG.nextDouble() < CR || L == D - 1) {
                tmp[n] = pold[r1].getValue(n) + F * (pold[r2].getValue(n) - pold[r3].getValue(n));
            }
            n = (n + 1) % D;
        }
    }

    private void randToBestOneBin() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        for (L = 0; L < D; L++) {
            if (RNG.nextDouble() < CR || L == D - 1) {
                tmp[n] = tmp[n] + F * (bestit.getValue(n) - tmp[n]) + F * (pold[r1].getValue(n) - pold[r2].getValue(n));
            }
            n = (n + 1) % D;
        }
    }

    private void bestTwoBin() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        for (L = 0; L < D; L++) {
            if (RNG.nextDouble() < CR || L == D - 1) {
                tmp[n] = bestit.getValue(n) + (pold[r1].getValue(n) + pold[r2].getValue(n) - pold[r3].getValue(n) - pold[r4].getValue(n)) * F;
            }
            n = (n + 1) % D;
        }
    }

    private void randTwoBin() {
        assignd(D, tmp, Util.toDoubleArray(pold[i].getVariables()));
        n = RNG.nextInt(D);
        for (L = 0; L < D; L++) {
            if (RNG.nextDouble() < CR || L == D - 1) {
                tmp[n] = pold[r5].getValue(n) + (pold[r1].getValue(n) + pold[r2].getValue(n) - pold[r3].getValue(n) - pold[r4].getValue(n)) * F;
            }
            n = (n + 1) % D;
        }
    }

    private List<Solution> parents(Solution... solutions) {
        List<Solution> parents = new ArrayList<>(solutions.length);
        for (Solution solution : solutions) {
            parents.add(solution);
        }
        return parents;
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
        F = memF;
        CR = memCR;
    }

    @Override
    public List<Algorithm> getAlgorithmParameterTest(int dimension, int maxCombinations) {
        List<Algorithm> alternative = new ArrayList<>();
        if (maxCombinations == 1) {
            alternative.add(this);
        } else {
            int counter = 0;
            if (strategy == DE.Strategy.JDE_RAND_1_BIN) {
                int[] paramCombinations = {25, 50, 15, 75, 100, 10, 30, 40};
                for (int k = 0; k < paramCombinations.length && counter < maxCombinations; k++) {
                    alternative.add(new DELogging(strategy, paramCombinations[k]));
                    counter++;
                }
            } else {
                double[][] paramCombinations = {
                        {10 * dimension, 0.5, 0.9}, {10 * dimension, 0.5, 0.85}, {25, 0.5, 0.9}, {50, 0.5, 0.9},
                        {10, 0.5, 0.9}, {50, 0.5, 0.9}, {25, 0.5, 0.8}, {25, 0.5, 0.9}};
                for (int k = 0; k < paramCombinations.length && counter < maxCombinations; k++) {
                    alternative.add(new DELogging(strategy, (int) paramCombinations[k][0], paramCombinations[k][1], paramCombinations[k][2]));
                    counter++;
                }
            }
        }
        return alternative;
    }
}
