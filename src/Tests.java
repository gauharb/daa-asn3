import graph.*;
import dsu.DSU;

public final class Tests {
    private static int passed;
    private static int failed;

    private static void check(boolean ok, String name) {
        if (ok) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    private static Counters cn() {
        return new Counters();
    }

    private static Graph undirected(int n, int[][] es) {
        Graph g = new Graph(n, false);
        for (int[] e : es) {
            g.addEdge(e[0], e[1], e[2]);
        }
        return g;
    }

    private static Graph directed(int n, int[][] es) {
        Graph g = new Graph(n, true);
        for (int[] e : es) {
            g.addEdge(e[0], e[1], e.length > 2 ? e[2] : 1);
        }
        return g;
    }

    public static void main(String[] args) {
        dsuTests();
        System.out.println();
        System.out.println("Passed: " + passed + "  Failed: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void dsuTests() {
        System.out.println("[DSU]");
        for (boolean compress : new boolean[] {false, true}) {
            for (boolean rank : new boolean[] {false, true}) {
                DSU d = new DSU(6, compress, rank, cn());
                boolean a = d.union(0, 1);
                boolean b = d.union(1, 2);
                boolean again = d.union(0, 2);
                d.union(3, 4);
                String tag = " (compress=" + compress + ", rank=" + rank + ")";
                check(a && b && !again, "union true for new merge, false inside same set" + tag);
                check(d.connected(0, 2) && !d.connected(0, 3) && d.components() == 3,
                        "connectivity and component count" + tag);
            }
        }

        Counters naive = cn();
        DSU slow = new DSU(1000, false, false, naive);
        for (int i = 1; i < 1000; i++) {
            slow.union(i, i - 1);
        }
        Counters fast = cn();
        DSU quick = new DSU(1000, true, true, fast);
        for (int i = 1; i < 1000; i++) {
            quick.union(i, i - 1);
        }
        check(fast.dsuParentSteps < naive.dsuParentSteps || naive.dsuParentSteps == 0,
                "optimised DSU follows no more parent pointers than naive DSU");
    }
}
