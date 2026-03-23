import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ArrayDeque61BTest {
    @Test
    public void addFirstTest() {
        // zero to one, one to more
        Deque61B<Integer> L = new ArrayDeque61B<>();
        L.addFirst(4);
        assertThat(L.toList()).containsExactly(4).inOrder();
        L.addFirst(5);
        L.addFirst(7);
        assertThat(L.toList()).containsExactly(7, 5, 4).inOrder();
    }

    @Test
    public void removeAndAddFirstTest() {
        // Change after remove operations
        // removeFirst
        Deque61B<Integer> L = new ArrayDeque61B<>();
        L.addFirst(4);
        L.addFirst(5);
        L.removeFirst();
        L.removeFirst();
        L.addFirst(6);
        L.addFirst(7);
        assertThat(L.toList()).containsExactly(7, 6).inOrder();

        // removeLast
        Deque61B<Integer> LL = new ArrayDeque61B<>();
        LL.addFirst(4);
        LL.addFirst(5);
        LL.removeLast();
        LL.removeLast();
        LL.addFirst(6);
        LL.addFirst(7);
        assertThat(LL.toList()).containsExactly(7, 6).inOrder();
    }

    @Test
    public void addLastTest() {
        // zero to one, one to more
        Deque61B<Integer> L = new ArrayDeque61B<>();
        L.addLast(4);
        assertThat(L.toList()).containsExactly(4).inOrder();
        L.addLast(5);
        L.addLast(7);
        assertThat(L.toList()).containsExactly(4, 5, 7).inOrder();
    }

    @Test
    public void removeAndAddLastTest() {
        // Change after remove operations
        // removeFirst
        Deque61B<Integer> L = new ArrayDeque61B<>();
        L.addLast(4);
        L.addLast(5);
        L.removeFirst();
        L.removeFirst();
        L.addLast(6);
        L.addLast(7);
        assertThat(L.toList()).containsExactly(6, 7).inOrder();

        // removeLast
        Deque61B<Integer> LL = new ArrayDeque61B<>();
        LL.addLast(4);
        LL.addLast(5);
        LL.removeLast();
        LL.removeLast();
        LL.addLast(6);
        LL.addLast(7);
        assertThat(LL.toList()).containsExactly(6, 7).inOrder();
    }

    @Test
    public void getFirstTest() {
        // get from empty, addFirst -> getFirst, addLast -> getFirst
        Deque61B<Integer> L = new ArrayDeque61B<>();
        Deque61B<Integer> L1 = new ArrayDeque61B<>();
        assertThat(L.getFirst()).isEqualTo(null);
        L.addFirst(4);
        assertThat(L.getFirst()).isEqualTo(4);
        L1.addLast(4);
        assertThat(L1.getFirst()).isEqualTo(4);
    }

    @Test
    public void getLastTest() {
        // get from empty, addFirst -> getLast, addLast -> getLast
        Deque61B<Integer> L = new ArrayDeque61B<>();
        Deque61B<Integer> L1 = new ArrayDeque61B<>();
        assertThat(L.getLast()).isEqualTo(null);
        L.addFirst(4);
        assertThat(L.getLast()).isEqualTo(4);
        L1.addLast(4);
        assertThat(L1.getLast()).isEqualTo(4);
    }

    @Test
    public void getTest() {
        // get from empty, get idx out of boundary, normal
        Deque61B<Integer> L = new ArrayDeque61B<>();
        assertThat(L.get(0)).isEqualTo(null);

        L.addFirst(3);
        L.addLast(1);
        L.addLast(5);
        L.addFirst(7); // [7, 3, 1, 5]

        assertThat(L.get(-100)).isEqualTo(null);
        assertThat(L.get(-1)).isEqualTo(null);
        assertThat(L.get(0)).isEqualTo(7);
        assertThat(L.get(3)).isEqualTo(5);
        assertThat(L.get(4)).isEqualTo(null);
        assertThat(L.get(200)).isEqualTo(null);
    }

    @Test
    public void getTestCircular() {
        // get from empty, get idx out of boundary, normal
        // For firstTag = 3, lastTag = 4
        Deque61B<Integer> L = new ArrayDeque61B<>();

        L.addFirst(3);
        L.addFirst(1);
        L.addFirst(5);
        L.addFirst(7);
        L.addFirst(8); // at the end of the array
        assertThat(L.get(0)).isEqualTo(8);

        Deque61B<Integer> LL = new ArrayDeque61B<>();

        LL.addLast(3);
        LL.addLast(1);
        LL.addLast(5);
        LL.addLast(7);
        LL.addLast(8); // at the front of the array
        assertThat(LL.get(4)).isEqualTo(8);
    }

    @Test
    public void isEmptyTest() {
        // empty, not empty
        Deque61B<Integer> L = new ArrayDeque61B<>();
        Deque61B<Integer> L1 = new ArrayDeque61B<>();
        assertThat(L.isEmpty()).isTrue();

        L.addFirst(3);
        assertThat(L.isEmpty()).isFalse();
        L1.addLast(1);
        assertThat(L1.isEmpty()).isFalse();

        L.addLast(5);
        L.addFirst(7); // [7, 3, 5]
        assertThat(L.isEmpty()).isFalse();
    }

    @Test
    public void sizeTest() {
        // empty, not empty(grow)
        Deque61B<Integer> L = new ArrayDeque61B<>();
        Deque61B<Integer> L1 = new ArrayDeque61B<>();
        assertThat(L.size()).isEqualTo(0);

        L.addFirst(3);
        assertThat(L.size()).isEqualTo(1);
        L1.addLast(1);
        assertThat(L1.size()).isEqualTo(1);

        L.addLast(5);
        L.addFirst(7); // [7, 3, 5]
        assertThat(L.size()).isEqualTo(3);
    }

    @Test
    public void toListTest() {
        // empty, not empty(grow)
        Deque61B<Integer> L = new ArrayDeque61B<>();
        Deque61B<Integer> LL = new ArrayDeque61B<>();
        assertThat(L.toList()).isEmpty();

        L.addFirst(3);
        assertThat(L.toList()).containsExactly(3).inOrder();

        LL.addLast(3);
        assertThat(LL.toList()).containsExactly(3).inOrder();

        L.addLast(5);
        L.addFirst(7); // [7, 3, 5]
        assertThat(L.toList()).containsExactly(7, 3, 5).inOrder();
        LL.addLast(5);
        LL.addFirst(7); // [7, 3, 5]
        assertThat(LL.toList()).containsExactly(7, 3, 5).inOrder();
    }

    @Test
    public void toListTestCircular() {
        Deque61B<Integer> L = new ArrayDeque61B<>();
        L.addFirst(3);
        L.addFirst(4);
        L.addFirst(5);
        L.addFirst(6);
        L.addFirst(7);
        assertThat(L.toList()).containsExactly(7, 6, 5, 4, 3).inOrder();
    }

    @Test
    public void removeFirstTest() {
        // remove from empty, addFirst -> removeFirst to zero
        // addLast -> removeFirst to zero
        Deque61B<Integer> L = new ArrayDeque61B<>();
        assertThat(L.removeFirst()).isEqualTo(null);
        assertThat(L.toList()).isEmpty();

        L.addFirst(4);
        assertThat(L.removeFirst()).isEqualTo(4);
        assertThat(L.toList()).isEmpty();

        L.addLast(4);
        assertThat(L.removeFirst()).isEqualTo(4);
        assertThat(L.toList()).isEmpty();
    }

    @Test
    public void removeLastTest() {
        // remove from empty, addFirst -> removeFirst to zero
        // addLast -> removeFirst to zero
        Deque61B<Integer> L = new ArrayDeque61B<>();
        assertThat(L.removeLast()).isEqualTo(null);
        assertThat(L.toList()).isEmpty();

        L.addFirst(4);
        assertThat(L.removeLast()).isEqualTo(4);
        assertThat(L.toList()).isEmpty();

        L.addLast(4);
        assertThat(L.removeLast()).isEqualTo(4);
        assertThat(L.toList()).isEmpty();
    }

    @Test
    public void resizeUpTest() {
        Deque61B<Integer> L = new ArrayDeque61B<>();
        Deque61B<Integer> L1 = new ArrayDeque61B<>();
        // Verify triggered by addFirst
        int count = 10;
        for (int i = 0; i <= count; i++) {
            L.addFirst(i);
        }
        L.addLast(20);
        assertThat(L.toList()).containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 20);
        // Verify triggered by addLast
        for (int i = 0; i <= count; i++) {
            L1.addLast(i);
        }
        L1.addFirst(20);
        assertThat(L1.toList()).containsExactly(20, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void resizeDownTest() {
        Deque61B<Integer> L = new ArrayDeque61B<>();
        // Verify triggered by removeFirst
        int count = 8;
        for (int i = 0; i < count; i++) {
            L.addFirst(i);
        } // 7-0, len 16
        for (int i = 0; i < count / 2; i++) {
            L.removeFirst();
        } // 3-0, len16
        L.removeFirst(); // 2-0, len8
        assertThat(L.toList()).containsExactly( 2, 1, 0);

        Deque61B<Integer> L1 = new ArrayDeque61B<>();
        // Verify triggered by removeLast
        for (int i = 0; i < count; i++) {
            L1.addFirst(i);
        } // 7-0, len 16
        for (int i = 0; i < count / 2; i++) {
            L1.removeLast();
        } // 7-4, len 16
        L1.removeLast(); // 7-5, len8
        assertThat(L1.toList()).containsExactly(7, 6, 5);
    }
}
