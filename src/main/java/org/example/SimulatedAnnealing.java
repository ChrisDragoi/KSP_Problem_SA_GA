package org.example;

import java.util.Random;

// Simulated Annealing pentru KSP (req. #8)
// Implementează EXACT algoritmul din Lecture 2.2, slide 15:
//
//   Select initial solution s0
//   Select initial temperature t0 > 0
//   Repeat
//     repeat (nrep ori)
//       randomly select s in N(s0)
//       delta = f(s) - f(s0)
//       if delta < 0 then s0 = s
//       else if random(0,1) < exp(-delta/t) then s0 = s
//     until iteration_count = nrep
//     t = alpha * t
//   Until t < MIN_TEMP
public class SimulatedAnnealing {
    private static final double MIN_TEMP = 0.01;

    private final Dataset ds;
    private final RunLogger logger;
    private final Random rand = new Random();

    private final double initialTemp;   // t0
    private final int tempLength;    // nrep
    private final double coolingRatio;  // alpha

    public SimulatedAnnealing(Dataset ds, double initialTemp, int tempLength, double coolingRatio) {
        this.ds = ds;
        this.initialTemp = initialTemp;
        this.tempLength = tempLength;
        this.coolingRatio = coolingRatio;
        this.logger = new RunLogger();
    }

    public Solution solve() {
        Solution s0 = DataGenerator.randomFeasibleSolution(ds, rand);
        Solution best = new Solution(s0);
        double t = initialTemp;

        while (t > MIN_TEMP) {

            for (int i = 0; i < tempLength; i++) {
                Solution s = randomNeighbor(s0);
                double delta = s.cost(ds) - s0.cost(ds);

                if (delta < 0) {
                    s0 = s;
                } else {
                    if (rand.nextDouble() < Math.exp(-delta/t)) {
                        s0 = s;
                    }
                }

                if (s0.isFeasible(ds) && s0.isBetterThan(best, ds)) {
                    best = new Solution(s0);
                }
            }

            logger.log(best.cost(ds));

            t *= coolingRatio;
        }

        return best;
    }

    private Solution randomNeighbor(Solution s) {
        Solution n = new Solution(s);
        int i = rand.nextInt(ds.N);
        n.items[i] = !n.items[i];
        if (n.items[i]) { n.totalWeight += ds.g[i]; n.totalValue += ds.v[i]; }
        else { n.totalWeight -= ds.g[i]; n.totalValue -= ds.v[i]; }
        return n;
    }

    public RunLogger getLogger() { return logger; }

    @Override
    public String toString() {
        return String.format("SA{T0=%.0f, nrep=%d, alpha=%.2f}",
                initialTemp, tempLength, coolingRatio);
    }
}
