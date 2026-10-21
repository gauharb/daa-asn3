import graph.*;

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
        System.out.println();
        System.out.println("Passed: " + passed + "  Failed: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
