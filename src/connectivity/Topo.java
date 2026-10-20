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

        if (order.size() == n) {
            return new Result(true, order, List.of());
        }
        List<Integer> stuck = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            if (indeg[v] > 0) {
                stuck.add(v);
            }
        }
        return new Result(false, order, stuck);
    }

    public static boolean isValidOrder(Graph g, List<Integer> order) {
        int n = g.n();
        if (order.size() != n) {
            return false;
        }
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || pos[v] != -1) {
                return false;
            }
            pos[v] = i;
        }
        for (Edge e : g.edges()) {
            if (pos[e.from()] >= pos[e.to()]) {
                return false;
            }
        }
        return true;
    }
}