package org.example;

import java.util.Random;

// Neighborhood Search (steepest descent) pentru KSP (req. #5)
// Vecinătate: toate soluțiile obținute prin flip pe exact 1 bit (N vecini)
// Strategie: la fiecare pas, găsim cel mai bun vecin care îmbunătățește soluția curentă
// Stop: niciun vecin nu mai aduce îmbunătățire (optim local atins)
public class NeighborhoodSearch {
    private final Dataset   ds;
    private final RunLogger logger;
    private final Random    rand;

    public NeighborhoodSearch(Dataset ds) {
        this.ds     = ds;
        this.rand   = new Random();
        this.logger = new RunLogger();
    }

    public Solution solve() {
        Solution current = DataGenerator.randomFeasibleSolution(ds, rand);
        logger.log(current.cost(ds));

        boolean improved = true;
        while (improved) {
            improved = false;
            Solution bestNeighbor = null;

            // Evaluăm TOȚI vecinii (steepest descent)
            for (int i = 0; i < ds.N; i++) {
                Solution neighbor = flip(current, i);
                if (neighbor.isFeasible(ds)) {
                    if (bestNeighbor == null || neighbor.isBetterThan(bestNeighbor, ds)) {
                        bestNeighbor = neighbor;
                    }
                }
            }

            if (bestNeighbor != null && bestNeighbor.isBetterThan(current, ds)) {
                current = bestNeighbor;
                logger.log(current.cost(ds));
                improved = true;
            }
        }

        return current;
    }

    // Creează vecin prin flip pe poziția i (update incremental, fără recompute complet)
    private Solution flip(Solution s, int i) {
        Solution n = new Solution(s);
        n.items[i] = !n.items[i];
        if (n.items[i]) {
            n.totalWeight += ds.g[i];
            n.totalValue += ds.v[i];
        }
        else {
            n.totalWeight -= ds.g[i];
            n.totalValue -= ds.v[i];
        }
        return n;
    }

    public RunLogger getLogger() { return logger; }
}
