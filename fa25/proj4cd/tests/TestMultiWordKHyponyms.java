import browser.NgordnetQuery;
import browser.NgordnetQueryHandler;
import org.junit.jupiter.api.Test;
import main.AutograderBuddy;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests the case where the list of words is length greater than 1, but k is still zero.
 * The word history and year history files do not matter for the k==0 case, but are provided
 * as input for the constructor of the HyponymsHandler.
 */
public class TestMultiWordKHyponyms {
    private static final String PREFIX = "./data/";

    /** NGrams Files */
    public static final String WORD_HISTORY_EECS_FILE = PREFIX + "word_history_eecs.csv";
    public static final String WORD_HISTORY_SIZE3_FILE = PREFIX + "word_history_size3.csv";
    public static final String WORD_HISTORY_SIZE4_FILE = PREFIX + "word_history_size4.csv";
    public static final String WORD_HISTORY_SIZE1291_FILE = PREFIX + "word_history_size1291.csv";
    public static final String WORD_HISTORY_SIZE14377_FILE = PREFIX + "word_history_size14377.csv";
    public static final String YEAR_HISTORY_FILE = PREFIX + "year_history.csv";

    /** Wordnet Files */
    public static final String SYNSETS_EECS_FILE = PREFIX + "synsets_eecs.txt";
    public static final String HYPONYMS_EECS_FILE = PREFIX + "hyponyms_eecs.txt";
    public static final String SYNSET_SIZE16_FILE = PREFIX + "synsets_size16.txt";
    public static final String HYPONYM_SIZE16_FILE = PREFIX + "hyponyms_size16.txt";
    public static final String HYPONYM_SIZE82191_FILE = PREFIX + "hyponyms_size82191.txt";
    public static final String SYNSET_SIZE82191_FILE = PREFIX + "synsets_size82191.txt";
    public static final String SYNSET_SIZE1000_FILE = PREFIX + "synsets_size1000.txt";
    public static final String HYPONYM_SIZE1000_FILE = PREFIX +  "hyponyms_size1000.txt";


    /** This is an example from the spec.*/
    @Test
    public void testFoodAndCake() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymsHandler(
                WORD_HISTORY_SIZE14377_FILE, YEAR_HISTORY_FILE, SYNSET_SIZE82191_FILE, HYPONYM_SIZE82191_FILE);
        List<String> words = new ArrayList<>();
        words.add("food");
        words.add("cake");

        NgordnetQuery nq = new NgordnetQuery(words, 1950, 1990, 5);
        String actual = studentHandler.handle(nq);
        String expected = "[cake, cookie, kiss, snap, wafer]";
        assertThat(actual).isEqualTo(expected);
    }
}
