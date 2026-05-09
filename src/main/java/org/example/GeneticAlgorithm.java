package org.example;

import java.util.*;

// Genetic Algorithm pentru KSP (req. #9)
// Implementează meta-algoritmul din Lecture 4.1, slide 12:
//
//   Produce initial population
//   Evaluate fitness
//   While termination not met:
//     Select fitter individuals   (tournament selection)
//     Recombine                   (single-point crossover)
//     Mutate                      (bit-flip)
//     Evaluate fitness
//     Generate new population     (cloning + crossover + mutation + random fill)
//
// Infeasible offspring sunt reparați: scoatem obiecte cu cel mai slab raport v/g
// până soluția devine feasible.
public class GeneticAlgorithm {
    private static final int TOURNAMENT_K = 3;

    private final Dataset   ds;
    private final RunLogger logger;
    private final Random    rand = new Random();

    private final int    populationSize;
    private final int    iterations;
    private final double crossoverRate;
    private final double mutationRate;
    private final double cloningRate;

    public GeneticAlgorithm(Dataset ds, int populationSize, int iterations,
                            double crossoverRate, double mutationRate, double cloningRate) {
        this.ds             = ds;
        this.populationSize = populationSize;
        this.iterations     = iterations;
        this.crossoverRate  = crossoverRate;
        this.mutationRate   = mutationRate;
        this.cloningRate    = cloningRate;
        this.logger         = new RunLogger();
    }

    public Solution solve() {
        List<Solution> population = initialPopulation();
        sortByFitness(population);
        Solution bestEver = new Solution(population.get(0));

        for (int iter = 0; iter < iterations; iter++) {
            List<Solution> next = new ArrayList<>(populationSize);

            // 1. Cloning (elitism): cei mai buni supraviețuiesc nemodificați
            int nClone = (int) Math.round(populationSize * cloningRate);
            for (int i = 0; i < nClone && i < population.size(); i++) {
                next.add(new Solution(population.get(i)));
            }

            // 2. Crossover: producem copii din părinți selectați prin turneu
            int nCross = (int) Math.round(populationSize * crossoverRate);
            while (next.size() < nClone + nCross) {
                Solution p1 = tournamentSelect(population);
                Solution p2 = tournamentSelect(population);
                Solution[] children = crossover(p1, p2);
                for (Solution child : children) {
                    next.add(repair(child));
                    if (next.size() >= nClone + nCross) break;
                }
            }

            // 3. Mutation: mutăm indivizi selectați prin turneu
            int nMut = (int) Math.round(populationSize * mutationRate);
            for (int i = 0; i < nMut; i++) {
                next.add(repair(mutate(tournamentSelect(population))));
            }

            // 4. Completăm cu soluții random feasible dacă mai e nevoie
            while (next.size() < populationSize) {
                next.add(DataGenerator.randomFeasibleSolution(ds, rand));
            }
            while (next.size() > populationSize) {
                next.remove(next.size() - 1);
            }

            population = next;
            sortByFitness(population);

            // Logăm cel mai bun individ din generația curentă (req. #16)
            Solution iterBest = population.get(0);
            logger.log(iterBest.cost(ds));
            if (iterBest.isBetterThan(bestEver, ds)) {
                bestEver = new Solution(iterBest);
            }
        }

        return bestEver;
    }

    // ---------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------

    private List<Solution> initialPopulation() {
        List<Solution> pop = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            pop.add(DataGenerator.randomFeasibleSolution(ds, rand));
        }
        return pop;
    }

    // Descrescător după valoare; la egalitate, crescător după greutate
    private void sortByFitness(List<Solution> pop) {
        pop.sort((a, b) -> {
            if (b.totalValue != a.totalValue) return b.totalValue - a.totalValue;
            return a.totalWeight - b.totalWeight;
        });
    }

    // Turneu: alege K indivizi random, returnează cel mai bun
    private Solution tournamentSelect(List<Solution> pop) {
        Solution best = null;
        for (int i = 0; i < TOURNAMENT_K; i++) {
            Solution c = pop.get(rand.nextInt(pop.size()));
            if (best == null || c.isBetterThan(best, ds)) best = c;
        }
        return best;
    }

    // Crossover single-point: produce 2 copii
    private Solution[] crossover(Solution p1, Solution p2) {
        int point = 1 + rand.nextInt(ds.N - 1);
        Solution c1 = new Solution(ds.N);
        Solution c2 = new Solution(ds.N);
        for (int i = 0; i < ds.N; i++) {
            c1.items[i] = i < point ? p1.items[i] : p2.items[i];
            c2.items[i] = i < point ? p2.items[i] : p1.items[i];
        }
        c1.compute(ds);
        c2.compute(ds);
        return new Solution[]{c1, c2};
    }

    // Mutație: flip pe un bit ales random
    private Solution mutate(Solution s) {
        Solution m = new Solution(s);
        int i = rand.nextInt(ds.N);
        m.items[i] = !m.items[i];
        if (m.items[i]) { m.totalWeight += ds.g[i]; m.totalValue += ds.v[i]; }
        else            { m.totalWeight -= ds.g[i]; m.totalValue -= ds.v[i]; }
        return m;
    }

    // Repair: scoate obiecte cu cel mai slab raport v/g până soluția e feasible
    private Solution repair(Solution s) {
        if (s.isFeasible(ds)) return s;

        List<Integer> included = new ArrayList<>();
        for (int i = 0; i < ds.N; i++) {
            if (s.items[i]) included.add(i);
        }
        // Sortăm crescător după v/g (cel mai slab raport = primul scos)
        included.sort(Comparator.comparingDouble(i -> (double) ds.v[i] / ds.g[i]));

        Solution r = new Solution(s);
        for (int idx : included) {
            if (r.isFeasible(ds)) break;
            r.items[idx]   = false;
            r.totalWeight -= ds.g[idx];
            r.totalValue  -= ds.v[idx];
        }
        return r;
    }

    public RunLogger getLogger() { return logger; }

    @Override
    public String toString() {
        return String.format("GA{pop=%d, iter=%d, cx=%.0f%%, mut=%.0f%%, clone=%.0f%%}",
                populationSize, iterations,
                crossoverRate * 100, mutationRate * 100, cloningRate * 100);
    }
}