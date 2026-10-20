package graph;

import java.util.Random;

public final class GraphGenerator {
    private GraphGenerator() {
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
