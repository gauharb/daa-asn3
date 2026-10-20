package mst;

import graph.*;

import java.util.List;

public final class MST {
    private MST() {
    }

    public record Result(long totalWeight, List<Edge> edges, boolean connected, int components) {
    }

    private static void requireUndirected(Graph g) {
        if (g.directed()) {
            throw new IllegalArgumentException("MST needs an undirected graph");
        }
    }
}
