package org.example;

import java.util.ArrayList;
import java.util.List;

// Stochează istoricul costurilor pe durata unui run (req. #16)
public class RunLogger {
    private final List<Double> costHistory = new ArrayList<>();
    private final List<Double> bestHistory = new ArrayList<>();
    private double runningBest = Double.MAX_VALUE;

    public void log(double cost) {
        costHistory.add(cost);
        if (cost < runningBest) runningBest = cost;
        bestHistory.add(runningBest);
    }

    public List<Double> getCostHistory() { return costHistory; }
    public List<Double> getBestHistory()  { return bestHistory; }
    public double getBestCost()           { return runningBest; }
    public int    getStepCount()          { return costHistory.size(); }

    // Graf ASCII simplu al best-so-far (req. #16)
    public void printChart(String label, int maxPoints) {
        System.out.println("\n=== Convergence: " + label + " ===");
        if (bestHistory.isEmpty()) { System.out.println("(no data)"); return; }

        int steps = bestHistory.size();
        int step  = Math.max(1, steps / maxPoints);

        List<Double> sampled = new ArrayList<>();
        for (int i = 0; i < steps; i += step)
            sampled.add(-bestHistory.get(i)); // negăm: afișăm valoarea, nu costul

        double min = sampled.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = sampled.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        if (max == min) max = min + 1;

        int height = 8;
        char[][] grid = new char[height][sampled.size()];
        for (char[] row : grid) java.util.Arrays.fill(row, ' ');

        for (int x = 0; x < sampled.size(); x++) {
            int y = (int) Math.round((sampled.get(x) - min) / (max - min) * (height - 1));
            grid[height - 1 - y][x] = '*';
        }

        System.out.printf("Best value (max=%.0f)%n", max);
        for (char[] row : grid) {
            System.out.print("|");
            for (char c : row) System.out.print(c);
            System.out.println();
        }
        System.out.println("+" + "-".repeat(sampled.size()));
        System.out.printf("Steps (1 tick ~ %d steps) | min=%.0f%n", step, min);
    }
}
