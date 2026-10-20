package dsu;

import graph.Counters;

public final class DSU {
    private final int[] parent;
    private final int[] rank;
    private final boolean compress;
    private final boolean byRank;
    private final Counters c;
    private int components;

    public DSU(int n, Counters c) {
        this(n, true, true, c);
    }

    public DSU(int n, boolean compress, boolean byRank, Counters c) {
        this.parent = new int[n];
        this.rank = new int[n];
        this.compress = compress;
        this.byRank = byRank;
        this.c = c;
        this.components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        c.dsuFind++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
            c.dsuParentSteps++;
        }
        if (compress) {
            while (parent[x] != root) {
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
        }
        return root;
    }

    public boolean union(int a, int b) {
        int ra = find(a);
        int rb = find(b);
        if (ra == rb) {
            return false;
        }
        c.dsuUnion++;
        if (byRank) {
            if (rank[ra] < rank[rb]) {
                int t = ra;
                ra = rb;
                rb = t;
            }
            parent[rb] = ra;
            if (rank[ra] == rank[rb]) {
                rank[ra]++;
            }
        } else {
            parent[rb] = ra;
        }
        components--;
        return true;
    }
}
