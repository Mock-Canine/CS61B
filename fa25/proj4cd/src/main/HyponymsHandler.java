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

    // TODO: Consider the front input edge case
    // TODO: Prompt AI for data structure choosing improvement
    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        int startYear = q.startYear();
        int endYear = q.endYear();
        int k = q.k();
        Set<String> commonWords = new HashSet<>();
        // Handle duplicate inputs
        for (String word : new HashSet<>(words)) {
            if (commonWords.isEmpty()) {
                commonWords = wn.getHyponyms(word);
            } else {
                commonWords.retainAll(wn.getHyponyms(word));
            }
        }
        List<String> result = new ArrayList<>(commonWords);
        result.sort(null);
        return result.toString();
    }

    // TODO: may attention that break tie by the alphabet

}
