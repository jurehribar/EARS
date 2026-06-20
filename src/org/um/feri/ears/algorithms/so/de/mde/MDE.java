package org.um.feri.ears.algorithms.so.de.mde;
import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.Algorithm;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.util.Util;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.util.random.RNG;
import java.util.ArrayList;
import java.util.List;
/**
 * Memetic Differential Evolution (MDE) - DE/rand/1/bin + pluggable local search.
 *
 * The DE component is a faithful port of the DE/rand/1/bin strategy from DE.java,
 * using the same generational (double-buffer pold/pnew) update and the same
 * binomial crossover traversal.  Passing null as localSearch makes the algorithm
 * behave identically to DE.java with Strategy.DE_RAND_1_BIN.
 *
 * Algorithm outline:
 *
 *   Initialize population into pold
 *
 *   while stop criterion not met:
 *
 *     // DE phase (generational - all mutations read from pold)
 *     for each individual x_i in pold:
 *       pick 3 distinct random indices r1, r2, r3 (all != i)
 *       mutation:  v_i = pold[r1] + F * (pold[r2] - pold[r3])
 *       crossover: starting at random n, wrap around D dimensions,
 *                  copy from v_i if rand()<CR or last dimension, else from pold[i]
 *       selection: if f(trial) <= f(pold[i]): pnew[i] = trial, else pnew[i] = pold[i]
 *     swap pold <-> pnew
 *
 *     // Local search phase (every localSearchFrequency generations)
 *     select top eliteSize individuals from pold
 *     for each elite: improved = localSearch.improve(elite, task)
 *                     if improved is better, replace in pold and update best
 */
public class MDE extends NumberAlgorithm {
    @AlgorithmParameter(name = "population size")
    private final int popSize;
    /** Mutation (differential weight) factor F in (0, 2]. */
    @AlgorithmParameter(name = "F")
    private final double F;
    /** Crossover probability CR in [0, 1]. */
    @AlgorithmParameter(name = "CR")
    private final double CR;
    /**
     * How many of the best individuals receive local-search refinement per
     * local-search phase.  Set to 0 to disable local search entirely.
     */
    @AlgorithmParameter(name = "elite size")
    private final int eliteSize;
    /**
     * Local search is applied every localSearchFrequency generations.
     * 1 = every generation; 5 = every 5th generation.
     */
    @AlgorithmParameter(name = "local search frequency")
    private final int localSearchFrequency;
    /** Pluggable local search strategy. null = no local search (pure DE/rand/1/bin). */
    private final LocalSearch localSearch;
    // Internal state
    private NumberSolution<Double>[] pold;  // current generation
    private NumberSolution<Double>[] pnew;  // next generation (buffer)
    private NumberSolution<Double>[] pswap; // for pointer swap
    private NumberSolution<Double> bestSolution;
    private int D;
    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------
    /**
     * Default: pop=30, F=0.5, CR=0.9, elite=1, local search every generation,
     * GradientDescentLocalSearch(0.01, 10).
     */
    public MDE() {
        this(30, 0.5, 0.9, 1, 1, new GradientDescentLocalSearch());
    }

    public MDE(int eliteSize, int localSearchFrequency) {
        this(30, 0.5, 0.9, eliteSize, localSearchFrequency, new GradientDescentLocalSearch());
    }

    /**
     * Uses GradientDescentLocalSearch as the default local search.
     */
    public MDE(int popSize, double F, double CR, int eliteSize, int localSearchFrequency) {
        this(popSize, F, CR, eliteSize, localSearchFrequency, new GradientDescentLocalSearch());
    }
    /**
     * Full constructor.
     *
     * @param popSize              population size (must be > 3)
     * @param F                    mutation factor in (0, 2]
     * @param CR                   crossover probability in [0, 1]
     * @param eliteSize            number of elite individuals refined per local-search phase
     * @param localSearchFrequency apply local search every N generations
     * @param localSearch          local search implementation; null = pure DE/rand/1/bin
     */
    public MDE(int popSize, double F, double CR,
               int eliteSize, int localSearchFrequency,
               LocalSearch localSearch) {
        if (popSize < 4)
            throw new IllegalArgumentException("Population size must be at least 4.");
        if (F <= 0 || F > 2)
            throw new IllegalArgumentException("Mutation factor F must be in (0, 2].");
        if (CR < 0 || CR > 1)
            throw new IllegalArgumentException("Crossover rate CR must be in [0, 1].");
        this.popSize              = popSize;
        this.F                    = F;
        this.CR                   = CR;
        this.eliteSize            = Math.max(0, eliteSize);
        this.localSearchFrequency = Math.max(1, localSearchFrequency);
        this.localSearch          = localSearch;
        au = new Author("mde", "mde@ears");
        ai = new AlgorithmInfo(
                "MDE",
                "Memetic Differential Evolution (DE/rand/1/bin + local search)",
                "DE/rand/1/bin from: R. Storn & K. Price, Differential Evolution - "
                + "A Simple and Efficient Heuristic for Global Optimization over "
                + "Continuous Spaces, Journal of Global Optimization, 11(4):341-359, 1997."
        );
    }
    // -----------------------------------------------------------------------
    // Initialisation
    // -----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void initPopulation() throws StopCriterionException {
        D = task.problem.getNumberOfDimensions();
        pold  = new NumberSolution[popSize];
        pnew  = new NumberSolution[popSize];
        for (int i = 0; i < popSize; i++) {
            pold[i] = task.generateRandomEvaluatedSolution();
        }
        bestSolution = pold[0];
        for (int i = 1; i < popSize; i++) {
            if (task.problem.isFirstBetter(pold[i], bestSolution)) {
                bestSolution = pold[i];
            }
        }
    }
    // -----------------------------------------------------------------------
    // Main loop
    // -----------------------------------------------------------------------
    @Override
    public NumberSolution<Double> execute(Task<NumberSolution<Double>, DoubleProblem> task)
            throws StopCriterionException {
        this.task = task;
        initPopulation();
        int generation = 0;
        while (!task.isStopCriterion()) {
            // =================================================================
            // DE phase: DE/rand/1/bin  (generational, identical to DE.java)
            // =================================================================
            for (int i = 0; i < popSize; i++) {
                if (task.isStopCriterion()) break;
                // Pick 3 distinct random indices r1, r2, r3 (all != i)
                int r1, r2, r3;
                do { r1 = RNG.nextInt(popSize); } while (r1 == i);
                do { r2 = RNG.nextInt(popSize); } while (r2 == i || r2 == r1);
                do { r3 = RNG.nextInt(popSize); } while (r3 == i || r3 == r1 || r3 == r2);
                // Build trial vector using DE/rand/1/bin crossover
                // Start at random dimension n, wrap around D times.
                // The last dimension (L == D-1) is always taken from the mutant
                // to guarantee at least one dimension changes -- identical to DE.java.
                double[] tmp = Util.toDoubleArray(pold[i].getVariables());
                int n = RNG.nextInt(D);
                for (int L = 0; L < D; L++) {
                    if (RNG.nextDouble() < CR || L == (D - 1)) {
                        // mutation: v_n = pold[r1][n] + F*(pold[r2][n] - pold[r3][n])
                        tmp[n] = pold[r1].getValue(n)
                               + F * (pold[r2].getValue(n) - pold[r3].getValue(n));
                    }
                    // else: tmp[n] keeps pold[i][n] (already copied above)
                    n = (n + 1) % D;
                }
                // Feasibility repair
                for (int j = 0; j < D; j++) {
                    tmp[j] = task.problem.makeFeasible(tmp[j], j);
                }
                // Evaluate trial vector
                NumberSolution<Double> trial = new NumberSolution<>(Util.toDoubleArrayList(tmp));
                task.eval(trial);
                // Greedy selection into pnew (generational - reads from pold, writes to pnew)
                if (task.problem.isFirstBetter(trial, pold[i])) {
                    pnew[i] = trial;
                    if (task.problem.isFirstBetter(trial, bestSolution)) {
                        bestSolution = trial;
                    }
                } else {
                    pnew[i] = pold[i];
                }
            }
            // Swap buffers: pnew becomes the current generation
            pswap = pold;
            pold  = pnew;
            pnew  = pswap;
            generation++;
            // =================================================================
            // Local search phase (applied to pold, i.e. the freshly swapped-in generation)
            // =================================================================
            if (localSearch != null && eliteSize > 0
                    && generation % localSearchFrequency == 0
                    && !task.isStopCriterion()) {
                applyLocalSearch();
            }
            if (displayData) {
                System.out.println(task.getNumberOfEvaluations() + " " + bestSolution);
            }
            task.incrementNumberOfIterations();
        }
        return bestSolution;
    }
    // -----------------------------------------------------------------------
    // Local search helpers
    // -----------------------------------------------------------------------
    /**
     * Apply local search to the top eliteSize individuals in pold.
     * If an improved solution is found it replaces the individual in pold
     * and updates bestSolution if necessary.
     */
    private void applyLocalSearch() throws StopCriterionException {
        int actualElite = Math.min(eliteSize, popSize);
        int[] eliteIndices = getEliteIndices(actualElite);
        for (int idx : eliteIndices) {
            if (task.isStopCriterion()) break;
            NumberSolution<Double> improved = localSearch.improve(pold[idx], task);
            if (task.problem.isFirstBetter(improved, pold[idx])) {
                pold[idx] = improved;
                if (task.problem.isFirstBetter(improved, bestSolution)) {
                    bestSolution = improved;
                }
            }
        }
    }
    /**
     * Return the indices of the k best individuals in pold (no sort, O(k*N)).
     */
    private int[] getEliteIndices(int k) {
        boolean[] selected = new boolean[popSize];
        int[] result = new int[k];
        for (int rank = 0; rank < k; rank++) {
            int bestIdx = -1;
            for (int i = 0; i < popSize; i++) {
                if (!selected[i]) {
                    if (bestIdx == -1 || task.problem.isFirstBetter(pold[i], pold[bestIdx])) {
                        bestIdx = i;
                    }
                }
            }
            selected[bestIdx] = true;
            result[rank] = bestIdx;
        }
        return result;
    }
    // -----------------------------------------------------------------------
    // Reset
    // -----------------------------------------------------------------------
    @Override
    public void resetToDefaultsBeforeNewRun() {
        // F and CR are final; nothing to reset
    }
    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------
    public int getPopSize()              { return popSize; }
    public double getMutationFactor()    { return F; }
    public double getCrossoverRate()     { return CR; }
    public int getEliteSize()            { return eliteSize; }
    public int getLocalSearchFrequency() { return localSearchFrequency; }
    public LocalSearch getLocalSearch()  { return localSearch; }
    @Override
    public List<Algorithm> getAlgorithmParameterTest(int dimension, int maxCombinations) {
        List<Algorithm> alternatives = new ArrayList<>();
        if (maxCombinations == 1) {
            alternatives.add(this);
        } else {
            // Representative parameter combinations: {popSize, eliteSize, localSearchFrequency}
            int[][] combos = {
                {10 * dimension, 1, 1},
                {50, 1, 1},
                {30, 1, 1},
                {50, 3, 1},
                {50, 1, 5},
                {30, 3, 5},
                {100, 1, 1},
                {25, 1, 1}
            };
            for (int i = 0; i < combos.length && i < maxCombinations; i++) {
                alternatives.add(new MDE(combos[i][0], F, CR, combos[i][1], combos[i][2],
                        new GradientDescentLocalSearch()));
            }
        }
        return alternatives;
    }
}