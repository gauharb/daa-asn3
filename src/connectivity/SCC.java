package connectivity;

import graph.*;

import java.util.ArrayList;
import java.util.List;

public final class SCC {
    private SCC() {
    }

    public record Result(int count, int[] componentOf, List<List<Integer>> components) {
        public int largest() {
            int best = 0;
            for (List<Integer> comp : components) {
                best = Math.max(best, comp.size());
            }
            return best;
        }
    }

    public static Result compute(Graph g, Counters c) {
        if (!g.directed()) {
            throw new IllegalArgumentException("SCC needs a directed graph");
        }
        int n = g.n();

        boolean[] visited = new boolean[n];
        int[] next = new int[n];
        int[] finish = new int[n];
        int finished = 0;
        int[] stack = new int[n];
        for (int s = 0; s < n; s++) {
            if (visited[s]) {
                continue;
            }
            int sp = 0;
            stack[sp++] = s;
            visited[s] = true;
            while (sp > 0) {
                int v = stack[sp - 1];
                List<Edge> out = g.adj(v);
                if (next[v] < out.size()) {
                    Edge e = out.get(next[v]++);
                    c.edgeExams++;
                    if (!visited[e.to()]) {
                        visited[e.to()] = true;
                        stack[sp++] = e.to();
                    }
                } else {
                    sp--;
                    finish[finished++] = v;
                }
            }
        }

        List<List<Integer>> rev = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            rev.add(new ArrayList<>());
        }
        for (Edge e : g.edges()) {
            c.edgeExams++;
            rev.get(e.to()).add(e.from());
        }

        return new Result(0, new int[n], new ArrayList<>());
    }
}
