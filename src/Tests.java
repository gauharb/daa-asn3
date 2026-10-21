import graph.*;
import dsu.DSU;
import mst.MST;
import connectivity.*;

import java.util.List;

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
}