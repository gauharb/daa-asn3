package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public final class GraphGenerator {
    private GraphGenerator() {
    }

    public static Graph undirectedConnected(int n, int m, long seed, int maxW) {
        if (m < n - 1 || m > (long) n * (n - 1) / 2) {
            throw new IllegalArgumentException("need n-1 <= m <= n(n-1)/2");
        }
        Random r = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();
        int[] perm = shuffled(n, r);
        for (int i = 1; i < n; i++) {
            int u = perm[i];
            int v = perm[r.nextInt(i)];
            used.add(key(u, v, n, false));
            g.addEdge(u, v, 1 + r.nextInt(maxW));
        }
        while (g.edgeCount() < m) {
            int u = r.nextInt(n);
            int v = r.nextInt(n);
            if (u != v && used.add(key(u, v, n, false))) {
                g.addEdge(u, v, 1 + r.nextInt(maxW));
            }
        }
        return g;
    }

    private static int[] shuffled(int n, Random r) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            p[i] = i;
        }
        for (int i = n - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = p[i];
            p[i] = p[j];
            p[j] = t;
        }
        return p;
    }

    private static long key(int u, int v, int n, boolean directed) {
        if (!directed && u > v) {
            int t = u;
            u = v;
            v = t;
        }
        return (long) u * n + v;
    }
}
