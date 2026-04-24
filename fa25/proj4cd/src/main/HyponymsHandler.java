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
        if (k == 0) {
            List<String> result = new ArrayList<>(common);
            result.sort(null);
            return result.toString();
        } else {
            List<String> selectK = new ArrayList<>();
            Map<String, Integer> wordTotalCounts = rmZeroCount(common, startYear, endYear);
            Iterator<String> iter = sort(common, wordTotalCounts);
            for (int i = 0; i < k && iter.hasNext(); i++) {
                selectK.add(iter.next());
            }
            return selectK.toString();
        }
    }

    /**
     * Remove the items in the common hyponyms that are not recorded by the Ngrams(zero count).
     * Return a map containing the # of total count for each remaining word.
     */
    private Map<String, Integer> rmZeroCount(Set<String> common, int start, int end) {
        Set<String> removed = new HashSet<>();
        Map<String, Integer> tracked = new HashMap<>();
        for (String word : common) {
            int countSum = countSum(word, start, end);
            if (countSum == 0) {
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
    private Iterator<String> sort(Set<String> common, Map<String, Integer> counts) {
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
            hyponyms.retainAll(commonWords);
            commonWords = hyponyms;
        }
        return commonWords;
    }

    private int countSum(String word, int startYear, int endYear) {
        TreeMap<Integer, Double> countHistory = ngm.countHistory(word, startYear, endYear);
        double result = 0.0;
        for (Double v : countHistory.values()) {
            result += v;
        }
        return (int) result;
    }
}
