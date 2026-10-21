import graph.*;
import dsu.DSU;
import mst.MST;
import connectivity.*;
import shortestpath.Dijkstra;

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

        System.out.println("   1. Infrastructure planning: minimum-cost road network (undirected)   ");
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

        System.out.println();

        System.out.println("   2a. Service dependencies (must be DAG)   ");
        Graph deps = new Graph(7, true);

        int[][] d = {{0, 1}, {0, 2}, {1, 4}, {2, 4}, {4, 3}, {4, 6}, {3, 5}};
        for (int[] e : d) {
            deps.addEdge(e[0], e[1], 1);
        }
        Topo.Result t = Topo.sort(deps, new Counters());
        System.out.println("DAG: " + t.isDag() + "; build order: " + path(t.order()));
        deps.addEdge(5, 0, 1);
        Topo.Result bad = Topo.sort(deps, new Counters());
        System.out.println("After adding OldTown -> Downtown: DAG = " + bad.isDag()
                + "; on the cycle or depending on it: " + path(bad.stuck()));

        System.out.println();

        System.out.println("   2b. One-way streets: strongly connected components   ");
        SCC.Result scc = SCC.compute(deps, new Counters());
        System.out.println("Number of SCCs: " + scc.count());
        for (List<Integer> comp : scc.components()) {
            List<String> names = new ArrayList<>();
            for (int v : comp) {
                names.add(name(v));
            }
            System.out.println("  " + names);
        }

        System.out.println();

        System.out.println("   3. Shortest routes from Downtown   ");
        Graph streets = new Graph(7, true);
        int[][] s = {
                {0, 1, 7}, {0, 5, 5}, {5, 1, 3}, {1, 2, 8}, {1, 4, 7}, {2, 4, 5},
                {4, 6, 8}, {5, 6, 11}, {3, 2, 6}
        };
        for (int[] e : s) {
            streets.addEdge(e[0], e[1], e[2]);
        }
        Dijkstra.Result res = Dijkstra.run(streets, 0, new Counters());
        for (int v = 0; v < 7; v++) {
            if (res.reachable(v)) {
                System.out.println("  " + name(v) + ": " + res.dist()[v] + "  via " + path(res.pathTo(v)));
            } else {
                System.out.println("  " + name(v) + ": unreachable from Downtown (no street leads into it)");
            }
        }
    }
}