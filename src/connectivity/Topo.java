package connectivity;

import graph.*;

import java.util.List;

public final class Topo {
    private Topo() {
    }

    public record Result(boolean isDag, List<Integer> order, List<Integer> stuck) {
    }
}