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

        int[] comp = new int[n];
        java.util.Arrays.fill(comp, -1);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            int s = finish[i];
            if (comp[s] != -1) {
                continue;
            }
            int id = components.size();
            List<Integer> members = new ArrayList<>();
            int sp = 0;
            stack[sp++] = s;
            comp[s] = id;
            while (sp > 0) {
                int v = stack[--sp];
                members.add(v);
                for (int u : rev.get(v)) {
                    c.edgeExams++;
                    if (comp[u] == -1) {
                        comp[u] = id;
                        stack[sp++] = u;
                    }
                }
            }
            components.add(members);
        }
        return new Result(components.size(), comp, components);
    }
}
