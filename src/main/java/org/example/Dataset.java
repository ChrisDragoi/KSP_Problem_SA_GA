package org.example;

import java.util.Arrays;

public class Dataset {
    public final int G;
    public final int N;
    public final int[] g;
    public final int[] v;

    public Dataset(int G, int N, int[] g, int[] v) {
        this.G = G;
        this.N = N;
        this.g = Arrays.copyOf(g, N);
        this.v = Arrays.copyOf(v, N);
    }

    @Override
    public String toString() {
        return String.format("Dataset{ N=%d, G=%d\n  weights=%s\n  values =%s }",
                N, G, Arrays.toString(g), Arrays.toString(v));
    }
}
