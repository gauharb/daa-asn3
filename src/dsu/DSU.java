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
}
