package connectivity;

import graph.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class Topo {
    private Topo() {
    }

    public record Result(boolean isDag, List<Integer> order, List<Integer> stuck) {
    }

    public static Result sort(Graph g, Counters c) {
        if (!g.directed()) {
            throw new IllegalArgumentException("topological ordering needs a directed graph");
        }
        int n = g.n();

        int[] indeg = new int[n];
        for (Edge e : g.edges()) {
            c.edgeExams++;
            indeg[e.to()]++;
        }

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                queue.add(v);
            }
        }

        List<Integer> order = new ArrayList<>(n);
        while (!queue.isEmpty()) {
            int v = queue.poll();
            order.add(v);
            for (Edge e : g.adj(v)) {
                c.edgeExams++;
                c.comparisons++;
                if (--indeg[e.to()] == 0) {
                    queue.add(e.to());
                }
            }
        }

        return new Result(order.size() == n, order, List.of());
    }
}