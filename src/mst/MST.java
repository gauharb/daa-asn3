package mst;

import graph.*;

import java.util.List;

public final class MST {
    private MST() {
    }

    public record Result(long totalWeight, List<Edge> edges, boolean connected, int components) {
    }
}
