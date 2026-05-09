package org.example;

public class Backtracking {
    private final Dataset ds;
    private Solution best;
    private final RunLogger logger;

    public Backtracking(Dataset ds) {
        this.ds = ds;
        this.logger = new RunLogger();
    }

    public Solution solve() {
        best = new Solution(ds.N);
        Solution current = new Solution(ds.N);

        backtracking(0, current);

        return best;
    }

    private void backtracking(int k, Solution current) {
        if (k == ds.N) {
            if (current.isBetterThan(best, ds)) {
                best = new Solution(current);
                logger.log(best.cost(ds));
            }
            return;
        }

        // 1. Branch: do not take item k
        current.items[k] = false;
        backtracking(k + 1, current);

        // 2. Branch: take item k, only if it does not exceed capacitye
        if (current.totalWeight + ds.g[k] <= ds.G) {
            current.items[k] = true;
            current.totalWeight += ds.g[k];
            current.totalValue += ds.v[k];

            backtracking(k + 1, current);

            // undo choice
            current.items[k] = false;
            current.totalWeight -= ds.g[k];
            current.totalValue -= ds.v[k];
        }
    }

    public RunLogger getLogger() {
        return logger;
    }
}