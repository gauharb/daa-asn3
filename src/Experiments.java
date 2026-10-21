import graph.*;
import mst.MST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Experiments {
    static final long SEED = 42;
    static final int MAX_W = 100;
    static final int RUNS = 5;
    static final int WARMUP = 20;

    interface Algo {
        Object run(Counters c);
    }

    record Measurement(double avgMicros, double medianMicros, Counters counters, Object result) {
    }

    static Measurement measure(Algo a) {
        System.gc();
        for (int i = 0; i < WARMUP; i++) {
            a.run(new Counters());
        }
        long total = 0;
        long[] times = new long[RUNS];
        Counters last = null;
        Object res = null;
        for (int i = 0; i < RUNS; i++) {
            Counters c = new Counters();
            long t0 = System.nanoTime();
            res = a.run(c);
            long t1 = System.nanoTime();
            times[i] = t1 - t0;
            total += times[i];
            last = c;
        }
        java.util.Arrays.sort(times);

        return new Measurement(total / (double) RUNS / 1000.0, times[RUNS / 2] / 1000.0, last, res);
    }

    static final List<String> rows = new ArrayList<>();

    static void record(String family, String graph, int v, int e, String algo, Measurement m, String info) {
        Counters c = m.counters();
        rows.add(String.format(Locale.ROOT, "%s,%s,%d,%d,%s,%.3f,%.3f,%d,%d,%d,%d,%d,%d,%d,%s",
                family, graph, v, e, algo, m.avgMicros(), m.medianMicros(), c.edgeExams, c.comparisons, c.dsuFind,
                c.dsuUnion, c.dsuParentSteps, c.pqOps, c.relaxations, info));
    }

    static void runConfig(String family, String name, int v, int e) {
        Graph u = GraphGenerator.undirectedConnected(v, e, SEED, MAX_W);

        Measurement kr = measure(c -> MST.kruskal(u, c));
        MST.Result krr = (MST.Result) kr.result();
        record(family, name, v, e, "Kruskal", kr, "mst_weight=" + krr.totalWeight() + ";connected=" + krr.connected());
        Measurement pr = measure(c -> MST.prim(u, c));
        MST.Result prr = (MST.Result) pr.result();
        record(family, name, v, e, "Prim", pr, "mst_weight=" + prr.totalWeight() + ";connected=" + prr.connected());
        if (krr.totalWeight() != prr.totalWeight()) {
            throw new IllegalStateException("Kruskal and Prim disagree on " + name);
        }

        Measurement naive = measure(c -> MST.kruskal(u, c, false, false));
        record(family, name, v, e, "Kruskal-naiveDSU", naive, "dsu=no_compression_no_rank");
        Measurement comp = measure(c -> MST.kruskal(u, c, true, false));
        record(family, name, v, e, "Kruskal-compressOnly", comp, "dsu=compression_only");
        Measurement rank = measure(c -> MST.kruskal(u, c, false, true));
        record(family, name, v, e, "Kruskal-rankOnly", rank, "dsu=rank_only");
    }

    static void runAll() {
        int[][] required = {{10, 30}, {100, 500}, {1000, 5000}};
        String[] names = {"Small", "Medium", "Large"};
        for (int i = 0; i < required.length; i++) {
            runConfig("required", names[i], required[i][0], required[i][1]);
        }
    }

    public static void main(String[] args) throws IOException {
        Path outDir = Path.of(args.length > 0 ? args[0] : "results/tables");
        Files.createDirectories(outDir);

        runAll();

        StringBuilder sb = new StringBuilder(
                "family,graph,V,E,algorithm,avg_time_us,median_time_us,edge_exams,comparisons,dsu_find,dsu_union,"
                        + "dsu_parent_steps,pq_ops,relaxations,info\n");
        for (String r : rows) {
            sb.append(r).append('\n');
        }
        Files.writeString(outDir.resolve("results.csv"), sb.toString());
        System.out.println("wrote " + outDir.resolve("results.csv"));
    }
}