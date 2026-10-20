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
}
