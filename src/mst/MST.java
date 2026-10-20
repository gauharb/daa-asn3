package mst;

import graph.*;
import dsu.DSU;

import java.util.List;
import java.util.ArrayList;

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

    private static void requireUndirected(Graph g) {
        if (g.directed()) {
            throw new IllegalArgumentException("MST needs an undirected graph");
        }
    }
}
