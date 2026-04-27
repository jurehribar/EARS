package org.um.feri.ears.examples;

import org.um.feri.ears.algorithms.NumberAlgorithm;
import org.um.feri.ears.algorithms.so.abc.ABC;
import org.um.feri.ears.algorithms.so.de.DE;
import org.um.feri.ears.algorithms.so.de.jade.JADE;
import org.um.feri.ears.algorithms.so.de.lshade.LSHADE;
import org.um.feri.ears.algorithms.so.gwo.GWO;
import org.um.feri.ears.algorithms.so.pso.PSO;
import org.um.feri.ears.algorithms.so.random.RandomSearch;
import org.um.feri.ears.algorithms.so.tlbo.TLBO;
import org.um.feri.ears.benchmark.Benchmark;
import org.um.feri.ears.benchmark.JureHBenchmark;
import org.um.feri.ears.benchmark.RPUOed30Benchmark;

import java.util.ArrayList;

public class SOBenchmarkJureH {

    public static void main(String[] args) {
        Benchmark.printInfo = false; //prints one on one results
        //add algorithms to a list

        ArrayList<NumberAlgorithm> algorithms = new ArrayList<NumberAlgorithm>();
        //algorithms.add(new JADE(60,0.7,0.10)); //Default parameters are popSize=30, p=0.05, c=0.1
        algorithms.add(new JADE()); //Default parameters are popSize=30, p=0.05, c=0.1
        algorithms.add(new LSHADE());
        algorithms.add(new DE(DE.Strategy.JDE_RAND_1_BIN));
        algorithms.add(new PSO(50,0.5,2.0,2.0)); //Default parameters are popSize=30, omega=0.7, c1=2, c2=2
        //algorithms.add(new PSO());
        algorithms.add(new RandomSearch());

        JureHBenchmark bench = new JureHBenchmark(); // benchmark with prepared tasks and settings

        bench.addAlgorithms(algorithms);  // register the algorithms in the benchmark

        bench.run(10); //start the tournament with 10 runs/repetitions
    }
}
