import graph.*;
import dsu.DSU;
import mst.MST;
import connectivity.*;
import shortestpath.Dijkstra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Tests {
    private static int passed;
    private static int failed;

    private static void check(boolean ok, String name) {
        if (ok) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    private static Counters cn() {
        return new Counters();
    }

    private static Graph undirected(int n, int[][] es) {
        Graph g = new Graph(n, false);
        for (int[] e : es) {
            g.addEdge(e[0], e[1], e[2]);
        }
        return g;
    }

    private static Graph directed(int n, int[][] es) {
        Graph g = new Graph(n, true);
        for (int[] e : es) {
            g.addEdge(e[0], e[1], e.length > 2 ? e[2] : 1);
        }
        return g;
    }

    public static void main(String[] args) {
        dsuTests();
        mstTests();
        topoTests();
        sccTests();
        dijkstraTests();
        System.out.println();
        System.out.println("Passed: " + passed + "  Failed: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void dsuTests() {
        System.out.println("[DSU]");
        for (boolean compress : new boolean[] {false, true}) {
            for (boolean rank : new boolean[] {false, true}) {
                DSU d = new DSU(6, compress, rank, cn());
                boolean a = d.union(0, 1);
                boolean b = d.union(1, 2);
                boolean again = d.union(0, 2);
                d.union(3, 4);
                String tag = " (compress=" + compress + ", rank=" + rank + ")";
                check(a && b && !again, "union true for new merge, false inside same set" + tag);
                check(d.connected(0, 2) && !d.connected(0, 3) && d.components() == 3,
                        "connectivity and component count" + tag);
            }
        }

        Counters naive = cn();
        DSU slow = new DSU(1000, false, false, naive);
        for (int i = 1; i < 1000; i++) {
            slow.union(i, i - 1);
        }
        Counters fast = cn();
        DSU quick = new DSU(1000, true, true, fast);
        for (int i = 1; i < 1000; i++) {
            quick.union(i, i - 1);
        }
        check(fast.dsuParentSteps < naive.dsuParentSteps || naive.dsuParentSteps == 0,
                "optimised DSU follows no more parent pointers than naive DSU");
    }

    private static long brute(Graph g) {
        List<Edge> es = g.edges();
        int n = g.n();
        int m = es.size();
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << m); mask++) {
            if (Integer.bitCount(mask) != n - 1) {
                continue;
            }
            DSU d = new DSU(n, cn());
            long w = 0;
            boolean ok = true;
            for (int i = 0; i < m && ok; i++) {
                if ((mask >> i & 1) == 1) {
                    ok = d.union(es.get(i).from(), es.get(i).to());
                    w += es.get(i).weight();
                }
            }
            if (ok) {
                best = Math.min(best, w);
            }
        }
        return best;
    }

    private static boolean treeIsSpanning(Graph g, MST.Result r) {
        if (r.edges().size() != g.n() - 1) {
            return false;
        }
        DSU d = new DSU(g.n(), cn());
        for (Edge e : r.edges()) {
            if (!d.union(e.from(), e.to())) {
                return false;
            }
        }
        return d.components() == 1;
    }

    private static void mstTests() {
        System.out.println("[MST]");
        Graph small = undirected(4, new int[][] {{0, 1, 1}, {1, 2, 2}, {0, 2, 3}, {2, 3, 4}});
        MST.Result k = MST.kruskal(small, cn());
        MST.Result p = MST.prim(small, cn());
        check(k.totalWeight() == 7 && p.totalWeight() == 7, "small graph: MST weight 7 for both");
        check(k.connected() && p.connected(), "small graph reported connected");
        check(treeIsSpanning(small, k) && treeIsSpanning(small, p), "small graph: results are spanning trees");

        Graph disc = undirected(5, new int[][] {{0, 1, 1}, {1, 2, 2}, {3, 4, 5}});
        MST.Result dk = MST.kruskal(disc, cn());
        MST.Result dp = MST.prim(disc, cn());
        check(!dk.connected() && !dp.connected(), "disconnected graph detected by Kruskal and Prim");
        check(dk.components() == 2 && dp.components() == 2, "disconnected graph: 2 components");
        check(dk.totalWeight() == 8 && dp.totalWeight() == 8, "disconnected graph: forest weight 8 for both");

        Graph tri = undirected(3, new int[][] {{0, 1, 2}, {1, 2, 2}, {0, 2, 2}});
        Graph sq = undirected(4, new int[][] {{0, 1, 1}, {1, 2, 1}, {2, 3, 1}, {3, 0, 1}});
        check(MST.kruskal(tri, cn()).totalWeight() == 4 && MST.prim(tri, cn()).totalWeight() == 4,
                "multiple valid MSTs (triangle, equal weights): same weight 4");
        check(MST.kruskal(sq, cn()).totalWeight() == 3 && MST.prim(sq, cn()).totalWeight() == 3,
                "multiple valid MSTs (square, equal weights): same weight 3");

        Graph cyc = undirected(4, new int[][] {{0, 1, 5}, {1, 2, 5}, {2, 3, 5}, {3, 0, 5}, {0, 2, 1}});
        check(MST.kruskal(cyc, cn()).totalWeight() == 11 && MST.prim(cyc, cn()).totalWeight() == 11,
                "graph with cycles: weight 11 for both");

        boolean allMatch = true;
        boolean allBrute = true;
        boolean allSpan = true;
        boolean variantsMatch = true;
        for (int t = 0; t < 300; t++) {
            int n = 2 + t % 5;
            int maxM = Math.min(n * (n - 1) / 2, 10);
            int m = (n - 1) + (t / 5) % (maxM - (n - 1) + 1);
            Graph g = GraphGenerator.undirectedConnected(n, m, 1000 + t, 4);
            MST.Result a = MST.kruskal(g, cn());
            MST.Result b = MST.prim(g, cn());
            allMatch &= a.totalWeight() == b.totalWeight();
            allBrute &= a.totalWeight() == brute(g) && b.totalWeight() == brute(g);
            allSpan &= treeIsSpanning(g, a) && treeIsSpanning(g, b);
            variantsMatch &= MST.kruskal(g, cn(), false, false).totalWeight() == a.totalWeight()
                    && MST.kruskal(g, cn(), true, false).totalWeight() == a.totalWeight()
                    && MST.kruskal(g, cn(), false, true).totalWeight() == a.totalWeight();
        }
        check(allMatch, "300 random graphs with duplicate weights: Kruskal weight == Prim weight");
        check(allBrute, "300 random graphs: both equal brute-force optimum");
        check(allSpan, "300 random graphs: both return acyclic spanning trees");
        check(variantsMatch, "all four DSU variants give the same MST weight");

        boolean bigMatch = true;
        for (int t = 0; t < 10; t++) {
            Graph g = GraphGenerator.undirectedConnected(500, 2500, 7 + t, 100);
            bigMatch &= MST.kruskal(g, cn()).totalWeight() == MST.prim(g, cn()).totalWeight();
        }
        check(bigMatch, "10 graphs with V=500, E=2500: Kruskal weight == Prim weight");
    }

    private static void topoTests() {
        System.out.println("[Topological ordering]");
        Graph dag = directed(6, new int[][] {{5, 2}, {5, 0}, {4, 0}, {4, 1}, {2, 3}, {3, 1}});
        Topo.Result r = Topo.sort(dag, cn());
        check(r.isDag() && Topo.isValidOrder(dag, r.order()), "known DAG: valid order");

        Graph tri = directed(3, new int[][] {{0, 1}, {1, 2}, {2, 0}});
        Topo.Result rc = Topo.sort(tri, cn());
        check(!rc.isDag() && rc.stuck().size() == 3, "3-cycle: topological order impossible, 3 stuck vertices");

        Graph self = directed(2, new int[][] {{0, 0}, {0, 1}});
        check(!Topo.sort(self, cn()).isDag(), "self loop is a cycle");

        Graph partial = directed(5, new int[][] {{0, 1}, {1, 2}, {2, 1}, {2, 3}, {3, 4}});
        Topo.Result rp = Topo.sort(partial, cn());
        check(!rp.isDag() && rp.order().equals(List.of(0)), "cycle in a subgraph: only vertex 0 can be ordered");

        boolean allValid = true;
        boolean allDagFlag = true;
        boolean cyclicDetected = true;
        for (int t = 0; t < 200; t++) {
            int n = 2 + t % 30;
            int maxM = n * (n - 1) / 2;
            int m = Math.min(maxM, (t * 7) % (3 * n));
            Graph g = GraphGenerator.directedDag(n, m, 500 + t, 10);
            Topo.Result res = Topo.sort(g, cn());
            allDagFlag &= res.isDag();
            allValid &= res.isDag() && Topo.isValidOrder(g, res.order());
            if (m >= 1) {

                Edge e = g.edges().get(0);
                g.addEdge(e.to(), e.from(), 1);
                cyclicDetected &= !Topo.sort(g, cn()).isDag();
            }
        }
        check(allDagFlag, "200 random DAGs all reported acyclic");
        check(allValid, "200 random DAGs: order valid for every edge, every vertex exactly once");
        check(cyclicDetected, "200 random DAGs + one back edge: cycle detected every time");

        Graph big = GraphGenerator.directedDag(1000, 5000, 42, 100);
        check(Topo.isValidOrder(big, Topo.sort(big, cn()).order()), "V=1000, E=5000 DAG: valid order");
        Graph cyclic = GraphGenerator.directedCyclic(1000, 5000, 42, 100);
        check(!Topo.sort(cyclic, cn()).isDag(), "V=1000, E=5000 cyclic graph: detected");
    }

    private static void sccTests() {
        System.out.println("[SCC]");
        Graph g = directed(8, new int[][] {{0, 1}, {1, 2}, {2, 0}, {2, 3}, {3, 4}, {4, 5}, {5, 3}, {6, 5}, {6, 7}, {7, 6}});
        SCC.Result r = SCC.compute(g, cn());
        Set<Set<Integer>> got = new HashSet<>();
        for (List<Integer> c : r.components()) {
            got.add(new HashSet<>(c));
        }
        Set<Set<Integer>> want = Set.of(Set.of(0, 1, 2), Set.of(3, 4, 5), Set.of(6, 7));
        check(r.count() == 3 && got.equals(want), "known graph: components {0,1,2} {3,4,5} {6,7}");

        Graph dag = directed(5, new int[][] {{0, 1}, {1, 2}, {0, 3}, {3, 4}});
        check(SCC.compute(dag, cn()).count() == 5, "DAG: every vertex is its own SCC");
        Graph ring = directed(6, new int[][] {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 0}});
        check(SCC.compute(ring, cn()).count() == 1, "ring: one SCC");

        boolean exactlyOne = true;
        boolean matchesReach = true;
        for (int t = 0; t < 300; t++) {
            int n = 1 + t % 8;
            int m = n == 1 ? 0 : (t * 3) % Math.min(n * (n - 1), 14);
            Graph rg = GraphGenerator.directedRandom(n, m, 9000 + t, 5);
            SCC.Result res = SCC.compute(rg, cn());
            int[] seen = new int[n];
            int total = 0;
            for (List<Integer> comp : res.components()) {
                total += comp.size();
                for (int v : comp) {
                    seen[v]++;
                }
            }
            exactlyOne &= total == n;
            for (int v = 0; v < n; v++) {
                exactlyOne &= seen[v] == 1 && res.componentOf()[v] >= 0;
            }
            boolean[][] reach = new boolean[n][n];
            for (int i = 0; i < n; i++) {
                reach[i][i] = true;
            }
            for (Edge e : rg.edges()) {
                reach[e.from()][e.to()] = true;
            }
            for (int k = 0; k < n; k++) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        reach[i][j] |= reach[i][k] && reach[k][j];
                    }
                }
            }
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    boolean same = res.componentOf()[u] == res.componentOf()[v];
                    matchesReach &= same == (reach[u][v] && reach[v][u]);
                }
            }
        }
        check(exactlyOne, "300 random graphs: every vertex belongs to exactly one SCC");
        check(matchesReach, "300 random graphs: same SCC <=> mutually reachable (Floyd-Warshall)");

        Graph big = GraphGenerator.directedCyclic(1000, 5000, 42, 100);
        SCC.Result br = SCC.compute(big, cn());
        int sum = 0;
        for (List<Integer> comp : br.components()) {
            sum += comp.size();
        }
        check(sum == 1000, "V=1000, E=5000: component sizes add up to V");

        Graph chain = new Graph(200_000, true);
        for (int i = 0; i + 1 < 200_000; i++) {
            chain.addEdge(i, i + 1, 1);
        }
        check(SCC.compute(chain, cn()).count() == 200_000, "200000-vertex chain: no stack overflow, all singletons");
    }

    private static long[] bellmanFord(Graph g, int s) {
        long[] d = new long[g.n()];
        Arrays.fill(d, Dijkstra.INF);
        d[s] = 0;
        for (int i = 0; i < g.n(); i++) {
            for (Edge e : g.edges()) {
                if (d[e.from()] != Dijkstra.INF && d[e.from()] + e.weight() < d[e.to()]) {
                    d[e.to()] = d[e.from()] + e.weight();
                }
                if (!g.directed() && d[e.to()] != Dijkstra.INF && d[e.to()] + e.weight() < d[e.from()]) {
                    d[e.from()] = d[e.to()] + e.weight();
                }
            }
        }
        return d;
    }

    private static boolean pathCostMatches(Graph g, Dijkstra.Result r, int t) {
        List<Integer> p = r.pathTo(t);
        if (!r.reachable(t)) {
            return p.isEmpty();
        }
        if (p.get(0) != r.source() || p.get(p.size() - 1) != t) {
            return false;
        }
        long cost = 0;
        for (int i = 0; i + 1 < p.size(); i++) {
            long best = Long.MAX_VALUE;
            for (Edge e : g.adj(p.get(i))) {
                if (e.to() == p.get(i + 1)) {
                    best = Math.min(best, e.weight());
                }
            }
            if (best == Long.MAX_VALUE) {
                return false;
            }
            cost += best;
        }
        return cost == r.dist()[t];
    }

    private static void dijkstraTests() {
        System.out.println("[Dijkstra]");
        Graph g = directed(5, new int[][] {{0, 1, 4}, {0, 2, 1}, {2, 1, 2}, {1, 3, 1}, {2, 3, 5}});
        Dijkstra.Result r = Dijkstra.run(g, 0, cn());
        check(r.dist()[0] == 0 && r.dist()[1] == 3 && r.dist()[2] == 1 && r.dist()[3] == 4,
                "known graph: distances 0,3,1,4");
        check(r.pathTo(3).equals(List.of(0, 2, 1, 3)), "known graph: path to 3 is 0-2-1-3");
        check(!r.reachable(4) && r.dist()[4] == Dijkstra.INF && r.pathTo(4).isEmpty(),
                "unreachable vertex: INF distance and empty path");
        check(r.reachableCount() == 4, "reachable count is 4");

        Graph zero = directed(3, new int[][] {{0, 1, 0}, {1, 2, 0}});
        check(Dijkstra.run(zero, 0, cn()).dist()[2] == 0, "zero-weight edges handled");

        Graph dup = directed(4, new int[][] {{0, 1, 2}, {0, 2, 2}, {1, 3, 2}, {2, 3, 2}});
        Dijkstra.Result dr = Dijkstra.run(dup, 0, cn());
        check(dr.dist()[3] == 4 && pathCostMatches(dup, dr, 3), "duplicate weights (two equal shortest paths)");

        Graph neg = directed(3, new int[][] {{0, 1, 2}, {0, 2, 5}, {2, 1, -4}});
        boolean threw = false;
        try {
            Dijkstra.run(neg, 0, cn());
        } catch (IllegalArgumentException ex) {
            threw = true;
        }
        check(threw, "negative edge weight rejected");
        check(bellmanFord(neg, 0)[1] == 1, "(for the report) true distance with the negative edge is 1, not 2");

        boolean allMatch = true;
        boolean allPaths = true;
        for (int t = 0; t < 300; t++) {
            int n = 1 + t % 40;
            int m = n == 1 ? 0 : (t * 5) % Math.min(n * (n - 1), 4 * n);
            Graph rg = GraphGenerator.directedRandom(n, m, 3000 + t, 6);
            Dijkstra.Result res = Dijkstra.run(rg, t % n, cn());
            long[] ref = bellmanFord(rg, t % n);
            allMatch &= Arrays.equals(res.dist(), ref);
            for (int v = 0; v < n; v++) {
                allPaths &= pathCostMatches(rg, res, v);
            }
        }
        check(allMatch, "300 random directed graphs: distances equal Bellman-Ford");
        check(allPaths, "300 random directed graphs: reconstructed path cost equals distance");

        boolean undirMatch = true;
        for (int t = 0; t < 50; t++) {
            Graph ug = GraphGenerator.undirectedConnected(30, 80, 77 + t, 20);
            undirMatch &= Arrays.equals(Dijkstra.run(ug, 0, cn()).dist(), bellmanFord(ug, 0));
        }
        check(undirMatch, "50 undirected connected graphs: distances equal Bellman-Ford");
    }
}