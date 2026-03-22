package Sort;

import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;

public class TestDemo {
    @Test
    public void testFindSmallest() {
        String[] input = {"string", "apple", "hello", "wobbly"};
        int expected = 1;

        int actual = Sort.findSmallest(input, 0);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testSwap() {
        String[] input = {"string", "apple", "hello", "wobbly"};
        String[] expected = {"string", "hello", "apple", "wobbly"};

        Sort.swap(input, 1, 2);
        assertThat(input).isEqualTo(expected);
    }

    @Test
    public void testSort() {
        String[] input = {"string", "apple", "hello", "wobbly"};
        String[] expected = { "apple", "hello", "string", "wobbly"};

        Sort.sort(input);
        assertThat(input).isEqualTo(expected);
    }
}
