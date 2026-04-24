package main;

import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;

import java.util.List;

public class HyponymsHandler extends NgordnetQueryHandler {
    private final WorldNet wn;

    public HyponymsHandler(WorldNet wn) {
        this.wn = wn;
    }

    // TODO: Consider the front input edge case
    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        return wn.getHyponyms(words.getFirst()).toString();
    }
}
