package org.um.feri.ears.algorithms.so.abc;

import org.um.feri.ears.algorithms.AlgorithmInfo;
import org.um.feri.ears.algorithms.Author;
import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.problems.DoubleProblem;
import org.um.feri.ears.problems.NumberSolution;
import org.um.feri.ears.problems.Solution;
import org.um.feri.ears.problems.StopCriterionException;
import org.um.feri.ears.problems.Task;
import org.um.feri.ears.util.annotation.AlgorithmParameter;
import org.um.feri.ears.util.random.RNG;

import java.util.ArrayList;
import java.util.List;

/**
 * Artificial Bee Colony variant that preserves parent-offspring ancestry for
 * exploration/exploitation analysis. The search behavior follows ABC, while
 * every generated candidate keeps a reference to the food source it came from.
 */
public class ABCLogging extends NumberAlgorithm {

    @AlgorithmParameter(name = "population size")
    private int popSize;
    private int foodNumber;

    @AlgorithmParameter(description = "maximum number of unsuccessful trials before abandonment")
    private int limit;

    private final boolean useDefaultLimit;
    private ABCSolution best;
    protected ArrayList<ABCSolution> population;

    public ABCLogging() {
        this(60);
    }

    public ABCLogging(int popSize) {
        this(popSize, 0, true);
    }

    public ABCLogging(int popSize, int limit) {
        this(popSize, limit, false);
    }

    private ABCLogging(int popSize, int limit, boolean useDefaultLimit) {
        this.popSize = popSize;
        this.foodNumber = popSize / 2;
        this.limit = limit;
        this.useDefaultLimit = useDefaultLimit;

        au = new Author("miha", "miha.ravber@um.si");
        ai = new AlgorithmInfo("ABCLogging", "Artificial Bee Colony with ancestry logging",
                "@article{karaboga2007powerful,"
                        + "title={A powerful and efficient algorithm for numerical function optimization: artificial bee colony (ABC) algorithm},"
                        + "author={Karaboga, Dervis and Basturk, Bahriye},"
                        + "journal={Journal of global optimization},"
                        + "volume={39},"
                        + "number={3},"
                        + "pages={459--471},"
                        + "year={2007}"
                        + "}");
    }

    @Override
    public NumberSolution<Double> execute(Task<NumberSolution<Double>, DoubleProblem> task) throws StopCriterionException {
        this.task = task;
        if (useDefaultLimit) {
            limit = (popSize * task.problem.getNumberOfDimensions()) / 2;
        }

        initPopulation();

        while (!task.isStopCriterion()) {
            sendEmployedBees();
            calculateProbabilities();
            sendOnlookerBees();
            memorizeBestSource();
            sendScoutBees();

            task.incrementNumberOfIterations();
        }

        return best;
    }

    private void sendScoutBees() throws StopCriterionException {
        int maxTrialIndex = 0;
        for (int i = 1; i < foodNumber; i++) {
            if (population.get(i).trials > population.get(maxTrialIndex).trials) {
                maxTrialIndex = i;
            }
        }

        if (population.get(maxTrialIndex).trials >= limit) {
            if (task.isStopCriterion()) {
                return;
            }

            ABCSolution parent = population.get(maxTrialIndex);
            ABCSolution newBee = new ABCSolution(task.problem.generateRandomSolution());
            newBee.parents = singleParent(parent);
            task.eval(newBee);
            population.set(maxTrialIndex, newBee);
        }
    }

    private void memorizeBestSource() {
        for (ABCSolution bee : population) {
            if (task.problem.isFirstBetter(bee, best)) {
                best = new ABCSolution(bee);
            }
        }
    }

    private void sendOnlookerBees() throws StopCriterionException {
        int t = 0;
        int i = 0;

        while (t < foodNumber) {
            if (RNG.nextDouble() < population.get(i).getProb()) {
                t++;
                ABCSolution newBee = createNeighbourSolution(i);

                if (task.isStopCriterion()) {
                    return;
                }

                task.eval(newBee);
                greedyReplace(i, newBee);
            }

            i++;
            if (i == foodNumber) {
                i = 0;
            }
        }
    }

    private void calculateProbabilities() {
        double maxFit = population.get(0).getABCEval();
        for (int i = 1; i < foodNumber; i++) {
            if (maxFit < population.get(i).getABCEval()) {
                maxFit = population.get(i).getABCEval();
            }
        }

        for (ABCSolution bee : population) {
            bee.setProb((0.9 * (bee.getABCEval() / maxFit)) + 0.1);
        }
    }

    private void sendEmployedBees() throws StopCriterionException {
        for (int i = 0; i < foodNumber; i++) {
            ABCSolution newBee = createNeighbourSolution(i);

            if (task.isStopCriterion()) {
                return;
            }

            task.eval(newBee);
            greedyReplace(i, newBee);
        }
    }

    private ABCSolution createNeighbourSolution(int index) {
        int neighbour = RNG.nextInt(foodNumber);
        while (neighbour == index) {
            neighbour = RNG.nextInt(foodNumber);
        }

        int paramToChange = RNG.nextInt(task.problem.getNumberOfDimensions());
        double phi = RNG.nextDouble(-1, 1);
        double newValue = population.get(index).getValue(paramToChange)
                + (population.get(index).getValue(paramToChange) - population.get(neighbour).getValue(paramToChange)) * phi;
        newValue = task.problem.makeFeasible(newValue, paramToChange);

        ABCSolution newBee = new ABCSolution(new NumberSolution<>(population.get(index).getVariables()));
        newBee.setValue(paramToChange, newValue);
        newBee.parents = singleParent(population.get(index));
        task.problem.makeFeasible(newBee);
        return newBee;
    }

    private void greedyReplace(int index, ABCSolution newBee) {
        if (newBee.getABCEval() > population.get(index).getABCEval()) {
            newBee.trials = 0;
            population.set(index, newBee);
        } else {
            population.get(index).trials++;
        }
    }

    private void initPopulation() throws StopCriterionException {
        population = new ArrayList<>();
        ABCSolution bee = new ABCSolution(task.generateRandomEvaluatedSolution());
        population.add(bee);
        best = new ABCSolution(bee);

        for (int i = 0; i < foodNumber - 1; i++) {
            ABCSolution newBee = new ABCSolution(task.generateRandomEvaluatedSolution());
            population.add(newBee);
            if (task.problem.isFirstBetter(newBee, best)) {
                best = new ABCSolution(newBee);
            }
            if (task.isStopCriterion()) {
                break;
            }
        }
    }

    private List<Solution> singleParent(Solution parent) {
        List<Solution> parents = new ArrayList<>(1);
        parents.add(parent);
        return parents;
    }

    @Override
    public void resetToDefaultsBeforeNewRun() {
    }
}
