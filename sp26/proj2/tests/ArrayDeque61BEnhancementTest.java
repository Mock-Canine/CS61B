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
    }
}
