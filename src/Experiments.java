import connectivity.SCC;
import connectivity.Topo;
import graph.Counters;
import graph.Graph;
import graph.GraphGenerator;
import mst.MST;
import shortestpath.Dijkstra;

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
    Arrays.sort(times);

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
    Graph dag = GraphGenerator.directedDag(v, e, SEED, MAX_W);
    Graph cyc = GraphGenerator.directedCyclic(v, e, SEED, MAX_W);

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

    Measurement td = measure(c -> Topo.sort(dag, c));
    Topo.Result tdr = (Topo.Result) td.result();
    record(family, name, v, e, "Topo-DAG", td, "acyclic=" + tdr.isDag() + ";ordered=" + tdr.order().size());
    Measurement tc = measure(c -> Topo.sort(cyc, c));
    Topo.Result tcr = (Topo.Result) tc.result();
    record(family, name, v, e, "Topo-Cyclic", tc, "acyclic=" + tcr.isDag() + ";ordered=" + tcr.order().size());

    Measurement sc = measure(c -> SCC.compute(cyc, c));
    SCC.Result scr = (SCC.Result) sc.result();
    record(family, name, v, e, "SCC-Cyclic", sc, "components=" + scr.count() + ";largest=" + scr.largest());
    Measurement sd = measure(c -> SCC.compute(dag, c));
    SCC.Result sdr = (SCC.Result) sd.result();
    record(family, name, v, e, "SCC-DAG", sd, "components=" + sdr.count() + ";largest=" + sdr.largest());

    Measurement dj = measure(c -> Dijkstra.run(cyc, 0, c));
    Dijkstra.Result djr = (Dijkstra.Result) dj.result();
    record(family, name, v, e, "Dijkstra", dj,
            "source=0;reachable=" + djr.reachableCount() + ";unreachable=" + (v - djr.reachableCount()));
}

static void runAll() {
    int[][] required = {{10, 30}, {100, 500}, {1000, 5000}};
    String[] names = {"Small", "Medium", "Large"};
    for (int i = 0; i < required.length; i++) {
        runConfig("required", names[i], required[i][0], required[i][1]);
    }
    for (int v : new int[]{20, 50, 100, 250, 500, 1000, 2000, 5000}) {
        runConfig("scaleV", "V" + v, v, 5 * v);
    }
    for (int e : new int[]{1000, 2000, 5000, 10000, 20000, 50000}) {
        runConfig("scaleE", "E" + e, 1000, e);
    }
}

void main(String[] args) throws IOException {
    Path outDir = Path.of(args.length > 0 ? args[0] : "results/tables");
    Files.createDirectories(outDir);

    IO.println("warm-up pass (discarded)");
    runAll();
    rows.clear();
    IO.println("measured pass");
    runAll();

    StringBuilder sb = new StringBuilder(
            "family,graph,V,E,algorithm,avg_time_us,median_time_us,edge_exams,comparisons,dsu_find,dsu_union,"
                    + "dsu_parent_steps,pq_ops,relaxations,info\n");
    for (String r : rows) {
        sb.append(r).append('\n');
    }
    Files.writeString(outDir.resolve("results.csv"), sb.toString());
    IO.println("wrote " + outDir.resolve("results.csv"));
}