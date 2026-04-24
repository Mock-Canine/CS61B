package main;

import java.util.*;

/** Use unique string as identifier for nodes */
public class Graph {
    private final Map<String, List<String>> graph;

    public Graph() {
        graph = new HashMap<>();
    }

    public void addEdge(String from, String to) {
        graph.computeIfAbsent(from, _ -> new ArrayList<>()).add(to);
    }

    public Collection<String> adj(String from) {
        return graph.getOrDefault(from, new ArrayList<>());
    }
}
