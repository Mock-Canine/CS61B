import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;
import org.junit.jupiter.api.Test;
import main.AutograderBuddy;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests the most basic case for Hyponyms where the list of words is one word long, and k != 0.
 */
public class TestOneWordKHyponyms {
    private static final String PREFIX = "./data/";

    /** NGrams Files */
    public static final String WORD_HISTORY_EECS_FILE = PREFIX + "word_history_eecs.csv";
    public static final String YEAR_HISTORY_FILE = PREFIX + "year_history.csv";

    /** Wordnet Files */
    public static final String SYNSET_EECS_FILE = PREFIX + "synsets_eecs.txt";
    public static final String HYPONYM_EECS_FILE = PREFIX + "hyponyms_eecs.txt";

    @Test
    public void testActK() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymsHandler(
                WORD_HISTORY_EECS_FILE, YEAR_HISTORY_FILE, SYNSET_EECS_FILE, HYPONYM_EECS_FILE);
        List<String> words = new ArrayList<>();
        words.add("CS61A");

        NgordnetQuery nq = new NgordnetQuery(words, 2010, 2020, 4);
        String actual = studentHandler.handle(nq);
        String expected = "[CS170, CS61A, CS61B, CS61C]";
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    public void testNotInclude() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymsHandler(
                WORD_HISTORY_EECS_FILE, YEAR_HISTORY_FILE, SYNSET_EECS_FILE, HYPONYM_EECS_FILE);
        List<String> words = new ArrayList<>();
        words.add("pollute");

        NgordnetQuery nq = new NgordnetQuery(words, 2010, 2020, 4);
        String actual = studentHandler.handle(nq);
        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testEmptyInput() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymsHandler(
                WORD_HISTORY_EECS_FILE, YEAR_HISTORY_FILE, SYNSET_EECS_FILE, HYPONYM_EECS_FILE);
        List<String> words = new ArrayList<>();

        NgordnetQuery nq = new NgordnetQuery(words, 2010, 2020, 4);
        String actual = studentHandler.handle(nq);
        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }
}
