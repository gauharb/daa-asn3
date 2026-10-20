package graph;

import java.util.ArrayList;
import java.util.List;

public final class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj;
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 1) {
            throw new IllegalArgumentException("graph needs at least one vertex");
        }
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int w) {
        if (u < 0 || u >= n || v < 0 || v >= n) {
            throw new IllegalArgumentException("vertex out of range: " + u + "," + v);
        }
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        if (!directed && u != v) {
            adj.get(v).add(new Edge(v, u, w));
        }
    }
}