package connectivity;

import graph.*;

import java.util.List;

public final class SCC {
    private SCC() {
    }

    public record Result(int count, int[] componentOf, List<List<Integer>> components) {
        public int largest() {
            int best = 0;
            for (List<Integer> comp : components) {
                best = Math.max(best, comp.size());
            }
            return best;
        }
    }
}
