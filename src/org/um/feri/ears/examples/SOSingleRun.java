package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.cmaes.CMAES;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.de.jade.JADE;
import org.um.feri.ears.algorithms.so.de.lshade.LSHADE;
import org.um.feri.ears.algorithms.so.es.ES1cNsAlgorithm;
import org.um.feri.ears.algorithms.so.es.ES1p1sAlgorithm;
import org.um.feri.ears.algorithms.so.es.ES1pNsAlgorithm;
import org.um.feri.ears.algorithms.so.hc.HillClimbing;
import org.um.feri.ears.algorithms.so.pso.PSO;
import org.um.feri.ears.problems.*;
import org.um.feri.ears.problems.unconstrained.ShiftedCoupledSineBowl;
import org.um.feri.ears.problems.unconstrained.Sphere;

public class SOSingleRun {

    public static void main(String[] args) {

        //RNG.setSeed(100); // set a specific seed for the random generator

        DoubleProblem problem = new ShiftedCoupledSineBowl(10); // problem
        //DoubleProblem problemSphere = new Sphere(5); // problem

        Task problemTask = new Task(problem, StopCriterion.EVALUATIONS, 50000, 0, 0); // set the stopping criterion to max 10000 evaluations

        //NumberAlgorithm alg = new DE(DE.Strategy.JDE_RAND_1_BIN);
        //NumberAlgorithm alg = new CMAES();
        //NumberAlgorithm alg = new ES1pNsAlgorithm();
        NumberAlgorithm alg = new HillClimbing();
        //NumberAlgorithm alg = new LSHADE();
        //NumberAlgorithm alg = new JADE();
        //NumberAlgorithm alg = new PSO();

        NumberSolution<Double> best;
        alg.setDisplayData(true);
        try {
            best = alg.execute(problemTask);
            System.out.println("Best solution found = " + best); // print the best solution found after 10000 evaluations
        } catch (StopCriterionException e) {
            e.printStackTrace();
        }
    }
}
