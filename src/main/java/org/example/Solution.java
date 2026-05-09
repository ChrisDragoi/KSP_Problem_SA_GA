package org.example;

import java.util.Arrays;

public class Solution {
    public boolean[] items;
    public int totalValue;
    public int totalWeight;

    public Solution(int n) {
        this.items = new boolean[n];
        this.totalValue  = 0;
        this.totalWeight = 0;
    }

    // Deep copy
    public Solution(Solution other) {
        this.items       = Arrays.copyOf(other.items, other.items.length);
        this.totalValue  = other.totalValue;
        this.totalWeight = other.totalWeight;
    }

    // Recalculează totalValue și totalWeight din array-ul items
    public void compute(Dataset ds) {
        totalValue  = 0;
        totalWeight = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i]) {
                totalValue  += ds.v[i];
                totalWeight += ds.g[i];
            }
        }
    }

    public boolean isFeasible(Dataset ds) {
        return totalWeight <= ds.G;
    }

    // Cost pentru SA (minimizare):
    //   feasible   -> -totalValue   (valoare mai mare = cost mai mic = mai bun)
    //   infeasible -> penalizare mare
    public double cost(Dataset ds) {
        if (isFeasible(ds)) return -totalValue;
        return 1_000_000.0 + (totalWeight - ds.G);
    }

    // True dacă THIS e strict mai bun decât OTHER
    // Criteriu: valoare mai mare; la egalitate, greutate mai mică
    public boolean isBetterThan(Solution other, Dataset ds) {
        boolean thisFeasible  = this.isFeasible(ds);
        boolean otherFeasible = other.isFeasible(ds);

        if (thisFeasible && !otherFeasible) return true;
        if (!thisFeasible) return false;

        if (this.totalValue != other.totalValue)
            return this.totalValue > other.totalValue;
        return this.totalWeight < other.totalWeight;
    }

    @Override
    public String toString() {
        return String.format("Value=%d, Weight=%d, Items=%s",
                totalValue, totalWeight, Arrays.toString(items));
    }
}