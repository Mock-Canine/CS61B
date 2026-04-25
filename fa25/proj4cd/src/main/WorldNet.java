package main;

import edu.princeton.cs.algs4.In;

import java.util.*;

public class WorldNet {
    private final Graph graph;
    /** Map a word to its synsets */
    private final Map<String, List<Integer>> wordSynsets;
    /** synset ID and its words */
    private final List<List<String>> synsets;

    public WorldNet(String synsetFilename, String hyponymsFilename) {
        graph = new Graph();
        wordSynsets = new HashMap<>();
        synsets = new ArrayList<>();
        // Populate graph
        In hyponymsFile = new In(hyponymsFilename);
        while (!hyponymsFile.isEmpty()) {
            String nextLine = hyponymsFile.readLine();
            String[] splitLine = nextLine.split(",");
            String from = splitLine[0];
            for (int i = 1; i < splitLine.length; i++) {
                graph.addEdge(Integer.parseInt(from), Integer.parseInt(splitLine[i]));
            }
        }
        // Populate mutual links between word and synset
        In synsetFile = new In(synsetFilename);
        while (!synsetFile.isEmpty()) {
            String nextLine = synsetFile.readLine();
            String[] splitLine = nextLine.split(",");
            Integer synset = Integer.parseInt(splitLine[0]);
            String[] words = splitLine[1].split(" ");
            for (String word : words) {
                wordSynsets.computeIfAbsent(word, _ -> new ArrayList<>()).add(synset);
            }
            // Assume synset File has no duplicate entry
            synsets.add(synset, List.of(words));
        }
    }

    public Set<String> getHyponyms(String word) {
        Set<String> hyponyms = new HashSet<>();
        Set<Integer> marked = new HashSet<>();
        for (Integer synset : wordSynsets.getOrDefault(word, Collections.emptyList())) {
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(synset);
            while (!queue.isEmpty()) {
                synset = queue.poll();
                if (!marked.add(synset)) {
                    continue;
                }
                hyponyms.addAll(synsets.get(synset));
                queue.addAll(graph.adj(synset));
            }
        }
        return hyponyms;
    }
}
