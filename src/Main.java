import graph.*;
import mst.MST;

import java.util.ArrayList;
import java.util.List;

public final class Main {
    private static final String[] NAMES = {
            "Downtown", "Harbor", "Airport", "University", "TechPark", "OldTown", "Stadium"
    };

    private static String name(int v) {
        return NAMES[v];
    }

    private static String path(List<Integer> p) {
        List<String> parts = new ArrayList<>();
        for (int v : p) {
            parts.add(name(v));
        }
        return String.join(" -> ", parts);
    }

    public static void main(String[] args) {
        System.out.println("=== 1. Infrastructure planning: minimum-cost road network (undirected) ===");
        Graph roads = new Graph(7, false);
        int[][] r = {
                {0, 1, 7}, {0, 5, 5}, {1, 2, 8}, {1, 5, 9}, {1, 4, 7}, {2, 4, 5},
                {3, 4, 15}, {3, 2, 6}, {3, 6, 9}, {4, 6, 8}, {4, 5, 15}, {5, 6, 11}
        };
        for (int[] e : r) {
            roads.addEdge(e[0], e[1], e[2]);
        }
        MST.Result k = MST.kruskal(roads, new Counters());
        MST.Result p = MST.prim(roads, new Counters());
        System.out.println("Connected: " + k.connected());
        System.out.println("Kruskal total cost: " + k.totalWeight() + ", Prim total cost: " + p.totalWeight()
                + (k.totalWeight() == p.totalWeight() ? "  (equal)" : "  (MISMATCH)"));
        for (Edge e : k.edges()) {
            System.out.println("  build " + name(e.from()) + " - " + name(e.to()) + " (cost " + e.weight() + ")");
        }
    }
}