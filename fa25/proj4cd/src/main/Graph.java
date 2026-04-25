package main;

import java.util.*;

/** Use unique int as identifier for nodes */
public class Graph {
    private final Map<Integer, List<Integer>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    public void addEdge(int from, int to) {
        graph.computeIfAbsent(from, _ -> new ArrayList<>()).add(to);
    }

    public Collection<Integer> adj(int from) {
        return graph.getOrDefault(from, Collections.emptyList());
    }
}
