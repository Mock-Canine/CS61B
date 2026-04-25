package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;

import java.util.*;

public class HyponymsHandler extends NgordnetQueryHandler {
    private final WorldNet wn;
    private final NGramMap ngm;

    /**
     * Helper class for sort
     */
    private static class WordCount {
        private final String word;
        private final double count;
        public WordCount(String word, double count) {
            this.word = word;
            this.count = count;
        }
    }

    public HyponymsHandler(WorldNet wn, NGramMap ngm) {
        this.wn = wn;
        this.ngm = ngm;
    }

    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        int k = q.k();
        Set<String> common = commonWords(words);
        List<String> result;
        if (k == 0) {
            result = new ArrayList<>(common);
        } else {
            // q.xx() is handled by the front, should be valid
            result = selectK(common, q.startYear(), q.endYear(), k);
        }
        // Sort is also needed here, two strings with the same counts, smaller string will enter result list,
        // But the results should also output in natural order, not based on counts
        result.sort(null);
        return result.toString();
    }

    /**
     * Return the k items with the largest count, break tie using natural order
     */
    private List<String> selectK(Set<String> common, int startYear, int endYear, int k) {
        /* Least count, larger string goes first */
        Comparator<WordCount> worstFirst = (a, b) -> {
            int count = Double.compare(a.count, b.count);
            if (count != 0) {
                return count;
            }
            return b.word.compareTo(a.word);
        };
        Queue<WordCount> pq = new PriorityQueue<>(worstFirst);
        for (String word : common) {
            double count = countSum(word, startYear, endYear);
            /* Avoid zero items */
            if (count <= 0) {
                continue;
            }
            WordCount wordCount = new WordCount(word, count);
            if (pq.size() < k) {
                pq.add(wordCount);
            } else if (worstFirst.compare(wordCount, pq.peek()) > 0) {
                pq.poll();
                pq.add(wordCount);
            }
        }
        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().word);
        }
        return result;
    }

    /**
     * Return the common hyponyms of a list of words
     * Return empty set if no hyponyms have been found
     */
    private Set<String> commonWords(List<String> words) {
        Set<String> commonWords = new HashSet<>();
        boolean firstWord = true;
        // Handle duplicate inputs
        for (String word : new HashSet<>(words)) {
            Set<String> hyponyms = wn.getHyponyms(word);
            if (firstWord) {
                commonWords = hyponyms;
                firstWord = false;
            } else {
                commonWords.retainAll(hyponyms);
            }
            // commonWords is empty when firstWord or two words do not have intersection
            if (commonWords.isEmpty()) {
                break;
            }
        }
        return commonWords;
    }

    /**
     * Return the # of total count of a word in the NGram
     */
    private Double countSum(String word, int startYear, int endYear) {
        TreeMap<Integer, Double> countHistory = ngm.countHistory(word, startYear, endYear);
        Double result = 0.0;
        for (Double v : countHistory.values()) {
            result += v;
        }
        return result;
    }
}
