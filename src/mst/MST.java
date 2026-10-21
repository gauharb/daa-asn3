package mst;

import graph.*;
import dsu.DSU;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class MST {
    private MST() {
    }

    public record Result(long totalWeight, List<Edge> edges, boolean connected, int components) {
    }

    public static Result kruskal(Graph g, Counters c) {
        return kruskal(g, c, true, true);
    }

    public static Result kruskal(Graph g, Counters c, boolean compress, boolean byRank) {
        requireUndirected(g);

        List<Edge> sorted = new ArrayList<>(g.edges());
        sorted.sort((a, b) -> {
            c.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });

        DSU dsu = new DSU(g.n(), compress, byRank, c);
        List<Edge> tree = new ArrayList<>();
        long total = 0;
        for (Edge e : sorted) {
            c.edgeExams++;
            if (dsu.union(e.from(), e.to())) {
                tree.add(e);
                total += e.weight();
                if (tree.size() == g.n() - 1) {
                    break;
                }
            }
        }
        return new Result(total, tree, dsu.components() == 1, dsu.components());
    }

    public static Result prim(Graph g, Counters c) {
        requireUndirected(g);
        int n = g.n();

        boolean[] inTree = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            c.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> tree = new ArrayList<>();
        long total = 0;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            pushEdges(g, start, inTree, pq, c);
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                c.pqOps++;
                if (inTree[e.to()]) {
                    continue;
                }
                inTree[e.to()] = true;
                tree.add(e);
                total += e.weight();
                pushEdges(g, e.to(), inTree, pq, c);
            }
        }
        return new Result(total, tree, components == 1, components);
    }

    // ---- helpers ----
    private static void pushEdges(Graph g, int v, boolean[] inTree, PriorityQueue<Edge> pq, Counters c) {
        for (Edge e : g.adj(v)) {
            c.edgeExams++;
            if (!inTree[e.to()]) {
                pq.add(e);
                c.pqOps++;
            }
        }
    }

    private static void requireUndirected(Graph g) {
        if (g.directed()) {
            throw new IllegalArgumentException("MST needs an undirected graph");
        }
    }
}