package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
}