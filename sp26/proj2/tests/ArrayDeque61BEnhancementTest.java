import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayDeque61BEnhancementTest {
    @Test
    public void uglyLoopTest() {
        // Can not be declared Deque61B now, interface has no method called
        // iterator() now
        ArrayDeque61B<String> L = new ArrayDeque61B<>();
        // Test empty
        Iterator<String> iter = L.iterator();
        assertThat(iter.hasNext()).isFalse();
        assertThat(iter.next()).isEqualTo(null);

        // Test non
        L.addFirst("jack");
        L.addFirst("moya");
        L.addFirst("senta");
        List<String> myList = new ArrayList<>();
        while (iter.hasNext()) {
            myList.add(iter.next());
        }
        assertThat(myList).containsExactly("senta", "moya", "jack").inOrder();
        assertThat(iter.next()).isEqualTo(null);
    }

    @Test
    public void enhancedLoopTest() {
        Deque61B<String> L = new ArrayDeque61B<>();
        List<String> myList = new ArrayList<>();
        // Test empty
        for (String str : L) {
            myList.add(str);
        }
        assertThat(myList).isEmpty();
        // Test non
        L.addFirst("jack");
        L.addFirst("moya");
        L.addFirst("senta");
        for (String str : L) {
            myList.add(str);
        }
        assertThat(myList).containsExactly("senta", "moya", "jack").inOrder();
        assertThat(L).containsExactly("senta", "moya", "jack").inOrder();
    }

    @Test
    public void equalTest() {
        Deque61B<Integer> L = new ArrayDeque61B<>();
        Deque61B<Integer> compare = new ArrayDeque61B<>();

        // empty
        assertThat(L.equals(compare)).isTrue();
        // non-empty
        // different size
        L.addFirst(3);
        L.addFirst(4); // [4, 3]
        compare.addLast(3); // [3]
        assertThat(L.equals(compare)).isFalse();
        // same size, different elements
        compare.addLast(5); // [3, 5]
        assertThat(L.equals(compare)).isFalse();
        // the same
        compare.removeLast();
        compare.addFirst(4); // [4, 3]
        assertThat(compare).containsExactly(4, 3).inOrder();
        assertThat(L.equals(compare)).isTrue();
    }

    @Test
    public void equalTestEnhanced() {
        Deque61B<String> L = new ArrayDeque61B<>();
        Deque61B<String> compare = new ArrayDeque61B<>();
        String jack = "jack";
        String mary = "mary";
        L.addFirst(jack);
        L.addFirst(mary);
        compare.addFirst(mary);
        compare.addFirst(jack);
        assertThat(L.equals(compare)).isFalse();
    }

    @Test
    public void toStringTest() {
        Deque61B<Integer> L = new ArrayDeque61B<>();
        IO.println(L); // []
        L.addFirst(3);
        L.addLast(4);
        IO.println(L); // [3, 4, ]
        L.removeLast();
        L.removeLast();
        IO.println(L); // []
    }
}
