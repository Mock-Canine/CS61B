package main;

import edu.princeton.cs.algs4.In;

import java.util.Collection;
import java.util.HashMap;

/**
 * An object that provides utility methods for making queries on the
 * Google NGrams dataset (or a subset thereof).
 * An NGramMap stores pertinent data from a "words file" and a "counts
 * file". It is not a map in the strict sense, but it does provide additional
 * functionality.
 *
 * @author Josh Hug
 */
public class NGramMap {
    private final HashMap<String, TimeSeries> wordHistories;
    private final TimeSeries yearHistory;

    /**
     * Constructs an NGramMap from WORDHISTORYFILENAME and YEARHISTORYFILENAME.
     */
    public NGramMap(String wordHistoryFilename, String yearHistoryFilename) {
        wordHistories = new HashMap<>();
        yearHistory = new TimeSeries();
        In wordFile = new In(wordHistoryFilename);
        while (!wordFile.isEmpty()) {
            String nextLine = wordFile.readLine();
            String[] splitLine = nextLine.split("\t");
            String word = splitLine[0];
            if (!wordHistories.containsKey(word)) {
                wordHistories.put(word, new TimeSeries());
            }
            // Format demo : airport	2007	175702	32788
            wordHistories.get(word).put(Integer.parseInt(splitLine[1]), Double.parseDouble(splitLine[2]));
        }
        In yearFile = new In(yearHistoryFilename);
        while (!yearFile.isEmpty()) {
            String nextLine = yearFile.readLine();
            String[] splitLine = nextLine.split(",");
            // Format demo : 1470,984,10,1
            yearHistory.put(Integer.parseInt(splitLine[0]), Double.parseDouble(splitLine[1]));
        }
    }

    /**
     * Provides the history of WORD between STARTYEAR and ENDYEAR, inclusive of both ends. The
     * returned TimeSeries should be a copy, not a link to this NGramMap's TimeSeries. In other
     * words, changes made to the object returned by this function should not also affect the
     * NGramMap. This is also known as a "defensive copy". If the word is not in the data files,
     * returns an empty TimeSeries.
     */
    public TimeSeries countHistory(String word, int startYear, int endYear) {
        TimeSeries wordHistory = wordHistories.getOrDefault(word, new TimeSeries());
        return new TimeSeries(wordHistory, startYear, endYear);
    }

    /**
     * Provides the history of WORD. The returned TimeSeries should be a copy, not a link to this
     * NGramMap's TimeSeries. In other words, changes made to the object returned by this function
     * should not also affect the NGramMap. This is also known as a "defensive copy". If the word
     * is not in the data files, returns an empty TimeSeries.
     */
    public TimeSeries countHistory(String word) {
        return countHistory(word, TimeSeries.MIN_YEAR, TimeSeries.MAX_YEAR);
    }

    /**
     * Returns a defensive copy of the total number of words recorded per year in all volumes.
     */
    public TimeSeries totalCountHistory() {
        return new TimeSeries(yearHistory, TimeSeries.MIN_YEAR, TimeSeries.MAX_YEAR);
    }

    /**
     * Provides a TimeSeries containing the relative frequency per year of WORD between STARTYEAR
     * and ENDYEAR, inclusive of both ends. If the word is not in the data files, returns an empty
     * TimeSeries.
     */
    public TimeSeries weightHistory(String word, int startYear, int endYear) {
        return countHistory(word, startYear, endYear).dividedBy(yearHistory);
    }

    /**
     * Provides a TimeSeries containing the relative frequency per year of WORD compared to all
     * words recorded in that year. If the word is not in the data files, returns an empty
     * TimeSeries.
     */
    public TimeSeries weightHistory(String word) {
        return countHistory(word).dividedBy(yearHistory);
    }

    /**
     * Provides the summed relative frequency per year of all words in WORDS between STARTYEAR and
     * ENDYEAR, inclusive of both ends. If a word does not exist in this time frame, ignore it
     * rather than throwing an exception.
     */
    public TimeSeries summedWeightHistory(Collection<String> words,
                                          int startYear, int endYear) {
        TimeSeries wordSum = new TimeSeries();
        for (String word : words) {
            TimeSeries wordHistory = countHistory(word, startYear, endYear);
            wordSum = wordSum.plus(wordHistory);
        }
        return wordSum.dividedBy(yearHistory);
    }

    /**
     * Returns the summed relative frequency per year of all words in WORDS. If a word does not
     * exist in this time frame, ignore it rather than throwing an exception.
     */
    public TimeSeries summedWeightHistory(Collection<String> words) {
        return summedWeightHistory(words, TimeSeries.MIN_YEAR, TimeSeries.MAX_YEAR);
    }
}
