package main;

import java.util.*;

/** Use unique int as identifier for nodes */
public class Graph {
    private final Map<Integer, List<Integer>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    public void addEdge(Integer from, Integer to) {
        graph.computeIfAbsent(from, _ -> new ArrayList<>()).add(to);
    }

    public Collection<Integer> adj(Integer from) {
        return graph.getOrDefault(from, Collections.emptyList());
    }
}
