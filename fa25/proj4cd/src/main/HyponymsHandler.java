package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;

import java.util.*;

public class HyponymsHandler extends NgordnetQueryHandler {
    private final WorldNet wn;

    public HyponymsHandler(WorldNet wn) {
        this.wn = wn;
    }

    // TODO: Consider the front input edge case
    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        SortedSet<String> commonWords = new TreeSet<>();
        // Handle duplicate inputs
        for (String word : new HashSet<>(words)) {
            if (commonWords.isEmpty()) {
                commonWords = wn.getHyponyms(word);
            } else {
                commonWords.retainAll(wn.getHyponyms(word));
            }
        }
        return commonWords.toString();
    }
}
