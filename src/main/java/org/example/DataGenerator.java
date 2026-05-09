package org.example;

import java.util.Random;

public class DataGenerator {

    private static final Random RAND = new Random(1234L);

    public static Dataset generate(int N) {
        int[] g = new int[N];
        int[] v = new int[N];

        int totalWeight = 0;

        for (int i = 0; i < N; i++) {
            g[i] = 10 + RAND.nextInt(41);  // weights in [10, 50]
            v[i] = 10 + RAND.nextInt(91);  // values in [10, 100]
            totalWeight += g[i];
        }

        // Capacitatea este controlată: nu încap toate obiectele,
        // dar nici nu este prea mică.
        int G = (int) (totalWeight * 0.50);

        return new Dataset(G, N, g, v);
    }

    public static Dataset[] generateAll() {
        return new Dataset[]{
                generate(8),
                generate(10),
                generate(50),
                generate(100)
        };
    }

    public static Solution randomFeasibleSolution(Dataset ds, Random rand) {
        Solution s = new Solution(ds.N);

        Integer[] order = new Integer[ds.N];
        for (int i = 0; i < ds.N; i++) order[i] = i;

        for (int i = ds.N - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }

        int w = 0;
        for (int idx : order) {
            if (w + ds.g[idx] <= ds.G) {
                s.items[idx] = true;
                w += ds.g[idx];
            }
        }

        s.compute(ds);
        return s;
    }
}