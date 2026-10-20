package shortestpath;

import graph.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public final class Dijkstra {
    public static final long INF = Long.MAX_VALUE;

    private Dijkstra() {
    }

    public record Result(int source, long[] dist, int[] parent) {
        public boolean reachable(int v) {
            return dist[v] != INF;
        }

        public List<Integer> pathTo(int target) {
            if (!reachable(target)) {
                return List.of();
            }
            List<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = parent[v]) {
                path.add(v);
            }
            Collections.reverse(path);
            return path;
        }

        public int reachableCount() {
            int k = 0;
            for (long d : dist) {
                if (d != INF) {
                    k++;
                }
            }
            return k;
        }
    }

    public static Result run(Graph g, int source, Counters c) {
        if (source < 0 || source >= g.n()) {
            throw new IllegalArgumentException("source out of range");
        }

        int n = g.n();
        long[] dist = new long[n];
        int[] parent = new int[n];
        java.util.Arrays.fill(dist, INF);
        java.util.Arrays.fill(parent, -1);
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> {
            c.comparisons++;
            return Long.compare(a[0], b[0]);
        });
        dist[source] = 0;
        pq.add(new long[] {0, source});
        c.pqOps++;

        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            c.pqOps++;
            int v = (int) top[1];
            if (top[0] > dist[v]) {
                continue;
            }
            for (Edge e : g.adj(v)) {
                c.edgeExams++;
                long nd = dist[v] + e.weight();
                c.comparisons++;
                if (nd < dist[e.to()]) {
                    dist[e.to()] = nd;
                    parent[e.to()] = v;
                    c.relaxations++;
                    pq.add(new long[] {nd, e.to()});
                    c.pqOps++;
                }
            }
        }
        return new Result(source, dist, parent);
    }
}