package org.example;

public class Main {

    // SA parameter sets
    private static final double[] SA_T0 = {1500, 15000, 500};
    private static final int[] SA_NREP = {500, 1500, 200};
    private static final double[] SA_ALPHA = {0.90, 0.99, 0.85};

    // GA parameter sets
    private static final int[] GA_POP = {1000, 50, 200};
    private static final int[] GA_ITER = {50, 1000, 200};
    private static final double[] GA_CX = {0.80, 0.60, 0.70};
    private static final double[] GA_MUT = {0.15, 0.30, 0.20};
    private static final double[] GA_CLONE = {0.05, 0.10, 0.10};

    private static final String[] LABELS = {"DS8", "DS10", "DS50", "DS100"};

    // Pune true doar dacă vrei grafice ASCII în consolă.
    // Recomand false pentru output curat.
    private static final boolean PRINT_CHARTS = false;

    public static void main(String[] args) {

        section("1. Dataset generation");
        Dataset[] datasets = DataGenerator.generateAll();

        for (int i = 0; i < datasets.length; i++) {
            System.out.println("[" + LABELS[i] + "]");
            System.out.println(datasets[i]);
            System.out.println();
        }

        section("2. Backtracking");
        Solution[] btSolutions = new Solution[4];
        long[] btTimes = new long[4];

        for (int i = 0; i < 2; i++) {
            Backtracking solver = new Backtracking(datasets[i]);

            long start = System.nanoTime();
            btSolutions[i] = solver.solve();
            btTimes[i] = System.nanoTime() - start;

            printSolution(LABELS[i], btSolutions[i], btTimes[i]);
        }

        printRatio("TB-10 / TB-8", btTimes[1], btTimes[0]);
        System.out.println("Observation: backtracking grows exponentially, so it is not feasible for DS50 and DS100.");

        section("3. Neighborhood Search");
        Solution[] nsSolutions = new Solution[4];
        long[] nsTimes = new long[4];

        for (int i = 0; i < datasets.length; i++) {
            NeighborhoodSearch solver = new NeighborhoodSearch(datasets[i]);

            long start = System.nanoTime();
            nsSolutions[i] = solver.solve();
            nsTimes[i] = System.nanoTime() - start;

            printSolution(LABELS[i], nsSolutions[i], nsTimes[i]);
        }

        printRatio("TNS-10 / TNS-8", nsTimes[1], nsTimes[0]);
        printRatio("TB-8 / TNS-8", btTimes[0], nsTimes[0]);
        printRatio("TB-10 / TNS-10", btTimes[1], nsTimes[1]);

        section("4. Simulated Annealing");
        Solution[][] saSolutions = new Solution[4][3];
        long[][] saTimes = new long[4][3];

        for (int d = 0; d < datasets.length; d++) {
            System.out.println("\n" + LABELS[d]);

            for (int p = 0; p < 3; p++) {
                SimulatedAnnealing solver = new SimulatedAnnealing(
                        datasets[d],
                        SA_T0[p],
                        SA_NREP[p],
                        SA_ALPHA[p]
                );

                long start = System.nanoTime();
                saSolutions[d][p] = solver.solve();
                saTimes[d][p] = System.nanoTime() - start;

                String label = "SA Set " + p + " " + solver;
                printSolution(label, saSolutions[d][p], saTimes[d][p]);

                if (PRINT_CHARTS && d >= 2) {
                    solver.getLogger().printChart("SA Set " + p + " " + LABELS[d], 40);
                }
            }
        }

        section("5. Genetic Algorithm");
        Solution[][] gaSolutions = new Solution[4][3];
        long[][] gaTimes = new long[4][3];

        for (int d = 0; d < datasets.length; d++) {
            System.out.println("\n" + LABELS[d]);

            for (int p = 0; p < 3; p++) {
                GeneticAlgorithm solver = new GeneticAlgorithm(
                        datasets[d],
                        GA_POP[p],
                        GA_ITER[p],
                        GA_CX[p],
                        GA_MUT[p],
                        GA_CLONE[p]
                );

                long start = System.nanoTime();
                gaSolutions[d][p] = solver.solve();
                gaTimes[d][p] = System.nanoTime() - start;

                String label = "GA Set " + p + " " + solver;
                printSolution(label, gaSolutions[d][p], gaTimes[d][p]);

                if (PRINT_CHARTS && d >= 2) {
                    solver.getLogger().printChart("GA Set " + p + " " + LABELS[d], 40);
                }
            }
        }

        section("6. Solution quality comparison");
        printQualityTable(btSolutions, nsSolutions, saSolutions, gaSolutions);

        section("7. Time comparison");
        printTimeComparison(btTimes, nsTimes, saTimes, gaTimes);

        section("8. Parameter influence");
        printSAParameterInfluence(saSolutions);
        printGAParameterInfluence(gaSolutions);

        section("9. Conclusions");

        System.out.println("Neighborhood Search vs Backtracking:");
        System.out.println("- Backtracking guarantees the optimal solution for DS8 and DS10, but it explores an exponential search space.");
        System.out.println("- Neighborhood Search is much faster and can be applied to DS50 and DS100.");
        System.out.println("- However, Neighborhood Search may stop in a local optimum, as shown by the lower values obtained on DS50 and DS100.");

        System.out.println();

        System.out.println("SA and GA vs NS and Backtracking:");
        System.out.println("- Simulated Annealing improves local search by accepting worse moves with a probability controlled by temperature.");
        System.out.println("- This allows SA to escape local optima and obtain better solutions than NS on large datasets.");
        System.out.println("- Genetic Algorithm works with a population of solutions and uses selection, crossover, mutation and cloning.");
        System.out.println("- On DS50 and DS100, GA obtained the best or near-best results, but required more time than NS.");
        System.out.println("- Backtracking remains the only exact method, but it is feasible only for small datasets.");
        System.out.println("- For large datasets, SA and GA are more practical because they trade guaranteed optimality for good solutions in reasonable time.");

        section("Done");
    }

    private static void printSolution(String label, Solution solution, long timeNs) {
        System.out.printf(
                "%-35s | value = %-5d | weight = %-5d | time = %.3f ms%n",
                label,
                solution.totalValue,
                solution.totalWeight,
                timeNs / 1_000_000.0
        );
    }

    private static void printQualityTable(
            Solution[] bt,
            Solution[] ns,
            Solution[][] sa,
            Solution[][] ga
    ) {
        System.out.printf(
                "%-6s | %-8s | %-8s | %-8s %-8s %-8s | %-8s %-8s %-8s%n",
                "DS", "BT", "NS", "SA[0]", "SA[1]", "SA[2]", "GA[0]", "GA[1]", "GA[2]"
        );

        System.out.println("-".repeat(90));

        for (int d = 0; d < 4; d++) {
            String btValue = d < 2 ? String.valueOf(bt[d].totalValue) : "N/A";

            System.out.printf(
                    "%-6s | %-8s | %-8d | %-8d %-8d %-8d | %-8d %-8d %-8d%n",
                    LABELS[d],
                    btValue,
                    ns[d].totalValue,
                    sa[d][0].totalValue,
                    sa[d][1].totalValue,
                    sa[d][2].totalValue,
                    ga[d][0].totalValue,
                    ga[d][1].totalValue,
                    ga[d][2].totalValue
            );
        }
    }

    private static void printTimeComparison(
            long[] btTimes,
            long[] nsTimes,
            long[][] saTimes,
            long[][] gaTimes
    ) {
        System.out.println("Backtracking / Neighborhood Search:");
        printRatio("TB-10 / TB-8", btTimes[1], btTimes[0]);
        printRatio("TNS-10 / TNS-8", nsTimes[1], nsTimes[0]);
        printRatio("TB-8 / TNS-8", btTimes[0], nsTimes[0]);
        printRatio("TB-10 / TNS-10", btTimes[1], nsTimes[1]);

        System.out.println("\nSimulated Annealing:");
        for (int p = 0; p < 3; p++) {
            printRatio("TSA[" + p + "]-10 / TSA[" + p + "]-8", saTimes[1][p], saTimes[0][p]);
            printRatio("TB-8 / TSA[" + p + "]-8", btTimes[0], saTimes[0][p]);
            printRatio("TB-10 / TSA[" + p + "]-10", btTimes[1], saTimes[1][p]);
        }

        System.out.println("\nGenetic Algorithm:");
        for (int p = 0; p < 3; p++) {
            printRatio("TGA[" + p + "]-10 / TGA[" + p + "]-8", gaTimes[1][p], gaTimes[0][p]);
            printRatio("TB-8 / TGA[" + p + "]-8", btTimes[0], gaTimes[0][p]);
            printRatio("TB-10 / TGA[" + p + "]-10", btTimes[1], gaTimes[1][p]);
        }

        System.out.println("\nLarge datasets: SA / GA / NS");
        for (int d = 2; d < 4; d++) {
            System.out.println(LABELS[d] + ":");

            for (int p = 0; p < 3; p++) {
                System.out.printf(
                        "  SA[%d] = %.3f ms, GA[%d] = %.3f ms, NS = %.3f ms%n",
                        p,
                        saTimes[d][p] / 1_000_000.0,
                        p,
                        gaTimes[d][p] / 1_000_000.0,
                        nsTimes[d] / 1_000_000.0
                );
            }
        }
    }

    private static void printSAParameterInfluence(Solution[][] saSolutions) {
        System.out.println("Simulated Annealing parameter sets:");
        System.out.println("Set 0: T0=1500,  nrep=500,  alpha=0.90");
        System.out.println("Set 1: T0=15000, nrep=1500, alpha=0.99");
        System.out.println("Set 2: T0=500,   nrep=200,  alpha=0.85");
        System.out.println();

        for (int d = 0; d < 4; d++) {
            System.out.printf(
                    "%-6s | Set0=%-5d | Set1=%-5d | Set2=%-5d%n",
                    LABELS[d],
                    saSolutions[d][0].totalValue,
                    saSolutions[d][1].totalValue,
                    saSolutions[d][2].totalValue
            );
        }

        System.out.println("\nGeneral interpretation:");
        System.out.println("- higher initial temperature allows more exploration");
        System.out.println("- larger temperature length gives more trials at each temperature");
        System.out.println("- cooling ratio closer to 1 means slower cooling and usually better exploration");
    }

    private static void printGAParameterInfluence(Solution[][] gaSolutions) {
        System.out.println("\nGenetic Algorithm parameter sets:");
        System.out.println("Set 0: population=1000, iterations=50,   crossover=80%, mutation=15%, cloning=5%");
        System.out.println("Set 1: population=50,   iterations=1000, crossover=60%, mutation=30%, cloning=10%");
        System.out.println("Set 2: population=200,  iterations=200,  crossover=70%, mutation=20%, cloning=10%");
        System.out.println();

        for (int d = 0; d < 4; d++) {
            System.out.printf(
                    "%-6s | Set0=%-5d | Set1=%-5d | Set2=%-5d%n",
                    LABELS[d],
                    gaSolutions[d][0].totalValue,
                    gaSolutions[d][1].totalValue,
                    gaSolutions[d][2].totalValue
            );
        }

        System.out.println("\nGeneral interpretation:");
        System.out.println("- large population with few iterations explores many individuals early");
        System.out.println("- small population with many iterations evolves longer but may lose diversity");
        System.out.println("- crossover mainly exploits existing good structure");
        System.out.println("- mutation adds exploration and prevents premature convergence");
        System.out.println("- cloning preserves good individuals between generations");
    }

    private static void printRatio(String label, long numerator, long denominator) {
        if (denominator == 0) {
            System.out.println(label + " = undefined");
            return;
        }

        System.out.printf("%-30s = %.2f%n", label, (double) numerator / denominator);
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println(title);
        System.out.println("=".repeat(80));
    }
}