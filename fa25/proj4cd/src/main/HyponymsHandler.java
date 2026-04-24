package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;

import java.util.*;

public class HyponymsHandler extends NgordnetQueryHandler {
    private final WorldNet wn;
    private final NGramMap ngm;

    public HyponymsHandler(WorldNet wn, NGramMap ngm) {
        this.wn = wn;
        this.ngm = ngm;
    }

    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        // this three is handled by the front, should be valid
        int startYear = q.startYear();
        int endYear = q.endYear();
        int k = q.k();
        Set<String> common = commonWords(words);
        List<String> result;
        if (k == 0) {
            result = new ArrayList<>(common);
        } else {
            result = new ArrayList<>();
            Map<String, Double> wordTotalCounts = rmZeroCount(common, startYear, endYear);
            Iterator<String> iter = sort(common, wordTotalCounts);
            for (int i = 0; i < k && iter.hasNext(); i++) {
                result.add(iter.next());
            }
        }
        // Sort is also needed here, sort() use natural order to break tie,
        // two strings with the same counts, smaller string will enter result list,
        // But the results should also output in order, not based on counts
        result.sort(null);
        return result.toString();
    }

    /**
     * Remove the items in the common hyponyms that are not recorded by the Ngrams(zero count).
     * Return a map containing the # of total count for each remaining word.
     */
    private Map<String, Double> rmZeroCount(Set<String> common, int start, int end) {
        Set<String> removed = new HashSet<>();
        Map<String, Double> tracked = new HashMap<>();
        for (String word : common) {
            Double countSum = countSum(word, start, end);
            if (countSum.isNaN()) {
                removed.add(word);
            } else {
                tracked.put(word, countSum);
            }
        }
        common.removeAll(removed);
        return tracked;
    }

    /**
     * Sort the common hyponyms based on # of total count.
     * Return an iterator outputting items from max # of total count.
     * Break tie through natural order
     */
    private Iterator<String> sort(Set<String> common, Map<String, Double> counts) {
        List<String> result = new ArrayList<>(common);
        Comparator<String> countComparator = Comparator.comparing(counts::get);
        // Large item goes first
        result.sort(countComparator.reversed()
                .thenComparing(Comparator.naturalOrder()));
        return result.iterator();
    }

    /**
     * Return the common hyponyms of a list of words
     * Return empty set if no hyponyms have been found
     */
    private Set<String> commonWords(List<String> words) {
        Set<String> commonWords = new HashSet<>();
        // Handle duplicate inputs
        for (String word : new HashSet<>(words)) {
            Set<String> hyponyms = wn.getHyponyms(word);
            if (!commonWords.isEmpty()) {
                hyponyms.retainAll(commonWords);
            }
            commonWords = hyponyms;
        }
        return commonWords;
    }

    // Integer does not have enough precision

    /**
     * Return the # of total count of a word in the NGram, NaN if not in the NGram
     */
    private Double countSum(String word, int startYear, int endYear) {
        TreeMap<Integer, Double> countHistory = ngm.countHistory(word, startYear, endYear);
        if (countHistory.isEmpty()) {
            return Double.NaN;
        }
        Double result = 0.0;
        for (Double v : countHistory.values()) {
            result += v;
        }
        return result;
    }
}
