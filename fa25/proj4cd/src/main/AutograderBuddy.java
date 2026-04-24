package main;

import browser.NgordnetQueryHandler;


public class AutograderBuddy {
    /** Returns a HyponymHandler */
    public static NgordnetQueryHandler getHyponymsHandler(
            String wordHistoryFile, String yearHistoryFile,
            String synsetFile, String hyponymFile) {

        WorldNet wn = new WorldNet(synsetFile, hyponymFile);
        return new HyponymsHandler(wn);
    }
}
