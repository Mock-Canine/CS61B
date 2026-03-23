import jh61b.utils.Reflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/** Performs some basic linked list tests. */
public class LinkedListDeque61BTest {
    // With new methods added, the former tests may should change

     @Test
     /** In this test, we have three different assert statements that verify that addFirst works correctly. */
     public void addFirstTestBasic() {
         // Add from empty, non-empty(from clean state)
         Deque61B<String> lld1 = new LinkedListDeque61B<>();

         lld1.addFirst("back"); // after this call we expect: ["back"]
         assertThat(lld1.toList()).containsExactly("back").inOrder();

         lld1.addFirst("middle"); // after this call we expect: ["middle", "back"]
         assertThat(lld1.toList()).containsExactly("middle", "back").inOrder();

         lld1.addFirst("front"); // after this call we expect: ["front", "middle", "back"]
         assertThat(lld1.toList()).containsExactly("front", "middle", "back").inOrder();

         /* Note: The first two assertThat statements aren't really necessary. For example, it's hard
            to imagine a bug in your code that would lead to ["front"] and ["front", "middle"] failing,
            but not ["front", "middle", "back"].
          */
     }

     @Test
     /** In this test, we use only one assertThat statement. IMO this test is just as good as addFirstTestBasic.
      *  In other words, the tedious work of adding the extra assertThat statements isn't worth it. */
     public void addLastTestBasic() {
         // Add from empty, non-empty(from clean state)
         Deque61B<String> lld1 = new LinkedListDeque61B<>();

         lld1.addLast("back"); // after this call we expect: ["back"]
         assertThat(lld1.toList()).containsExactly("back").inOrder();

         lld1.addLast("middle"); // after this call we expect: ["middle", "back"]
         assertThat(lld1.toList()).containsExactly( "back", "middle").inOrder();

         lld1.addLast("front"); // after this call we expect: ["front", "middle", "back"]
         assertThat(lld1.toList()).containsExactly( "back", "middle", "front").inOrder();

         /* Note: The first two assertThat statements aren't really necessary. For example, it's hard
            to imagine a bug in your code that would lead to ["front"] and ["front", "middle"] failing,
            but not ["front", "middle", "back"].
          */
     }

     @Test
     /** This test performs interspersed addFirst and addLast calls. */
     public void addFirstAndAddLastTest() {
         Deque61B<Integer> lld1 = new LinkedListDeque61B<>();

         /* I've decided to add in comments the state after each call for the convenience of the
            person reading this test. Some programmers might consider this excessively verbose. */
         lld1.addLast(0);   // [0]
         lld1.addLast(1);   // [0, 1]
         lld1.addFirst(-1); // [-1, 0, 1]
         lld1.addLast(2);   // [-1, 0, 1, 2]
         lld1.addFirst(-2); // [-2, -1, 0, 1, 2]

         assertThat(lld1.toList()).containsExactly(-2, -1, 0, 1, 2).inOrder();
     }

     @Test
     public void addAfterRemoveTest() {
         // Add from empty, non-empty(from dirty state)
         Deque61B<Integer> lld1 = new LinkedListDeque61B<>();
         lld1.addLast(0);   // [0]
         lld1.addLast(1);   // [0, 1]
         lld1.addFirst(-1); // [-1, 0, 1]
         lld1.removeFirst();
         lld1.removeFirst();
         lld1.removeLast();

         lld1.addLast(1); // after this call we expect: ["back"]
         assertThat(lld1.toList()).containsExactly(1).inOrder();

         lld1.addLast(3); // after this call we expect: ["middle", "back"]
         assertThat(lld1.toList()).containsExactly(1, 3).inOrder();

         lld1.removeFirst();
         lld1.removeLast();

         lld1.addFirst(1); // after this call we expect: ["back"]
         assertThat(lld1.toList()).containsExactly(1).inOrder();

         lld1.addFirst(3); // after this call we expect: ["middle", "back"]
         assertThat(lld1.toList()).containsExactly(3, 1).inOrder();
     }

     @Test
     public void toListTest() {
         Deque61B<Integer> lld = new LinkedListDeque61B<>();
         // Empty
         assertThat(lld.toList()).isEmpty();

         // Non
         lld.addLast(3);
         lld.addFirst(4);
         assertThat(lld.toList()).containsExactly(4, 3).inOrder();
     }

    // Below, you'll write your own tests for LinkedListDeque61B.
    // Test the tests will fail first
    @Test
    public void isEmptyTest() {
        Deque61B<String> lld1 = new LinkedListDeque61B<>();
        Deque61B<Integer> lld2 = new LinkedListDeque61B<>();

        assertThat(lld1.isEmpty()).isTrue();
        assertThat(lld2.isEmpty()).isTrue();

        lld1.addFirst("hello");
        lld1.addLast("dolo");
        lld2.addLast(0);
        lld2.addLast(10);

        assertThat(lld1.isEmpty()).isFalse();
        assertThat(lld2.isEmpty()).isFalse();
    }

    @Test
    public void sizeTest() {
         // For size(), it will face four cases
        Deque61B<Integer> lld1 = new LinkedListDeque61B<>();

        assertThat(lld1.size()).isEqualTo(0);

        lld1.addLast(0);   // [0]
        lld1.addLast(1);   // [0, 1]
        lld1.addFirst(-1); // [-1, 0, 1]
        assertThat(lld1.size()).isEqualTo(3);

        // Remove to empty
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();
        assertThat(lld1.size()).isEqualTo(0);

        // Remove from empty
        lld1.removeFirst();
        assertThat(lld1.size()).isEqualTo(0);
    }


    @Test
    public void getFirstTest() {
        Deque61B<Integer> lld1 = new LinkedListDeque61B<>();

        assertThat(lld1.getFirst()).isEqualTo(null);

        lld1.addLast(0);   // [0]
        assertThat(lld1.getFirst()).isEqualTo(0);

        lld1.addLast(1);   // [0, 1]
        lld1.addFirst(-1); // [-1, 0, 1]
        assertThat(lld1.getFirst()).isEqualTo(-1);
    }

    @Test
    public void getLastTest() {
        Deque61B<Integer> lld1 = new LinkedListDeque61B<>();

        assertThat(lld1.getLast()).isEqualTo(null);

        lld1.addFirst(0);   // [0]
        assertThat(lld1.getLast()).isEqualTo(0);

        lld1.addFirst(1);   // [1, 0]
        lld1.addLast(-1); // [1, 0, -1]
        assertThat(lld1.getLast()).isEqualTo(-1);
    }

    @Test
    public void getTest() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();

        assertThat(lld.get(0)).isEqualTo(null);
        assertThat(lld.get(-1)).isEqualTo(null);
        assertThat(lld.get(1)).isEqualTo(null);

        lld.addLast(0);   // [0]
        lld.addLast(1);   // [0, 1]
        lld.addFirst(-1); // [-1, 0, 1]
        lld.addLast(2);   // [-1, 0, 1, 2]
        lld.addFirst(-2); // [-2, -1, 0, 1, 2]

        assertThat(lld.get(0)).isEqualTo(-2);
        assertThat(lld.get(4)).isEqualTo(2);
        assertThat(lld.get(7)).isEqualTo(null);
        assertThat(lld.get(-4)).isEqualTo(null);

    }

    @Test
    public void getRecursiveTest() {
        Deque61B<Integer> lld = new LinkedListDeque61B<>();

        assertThat(lld.getRecursive(0)).isEqualTo(null);
        assertThat(lld.getRecursive(-1)).isEqualTo(null);
        assertThat(lld.getRecursive(1)).isEqualTo(null);

        lld.addLast(0);   // [0]
        lld.addLast(1);   // [0, 1]
        lld.addFirst(-1); // [-1, 0, 1]
        lld.addLast(2);   // [-1, 0, 1, 2]
        lld.addFirst(-2); // [-2, -1, 0, 1, 2]

        assertThat(lld.getRecursive(0)).isEqualTo(-2);
        assertThat(lld.getRecursive(4)).isEqualTo(2);
        assertThat(lld.getRecursive(7)).isEqualTo(null);
        assertThat(lld.getRecursive(-4)).isEqualTo(null);

    }
    @Test
    public void removeFirstTestBasic() {
        // Remove from empty, remove from one, remove from two
        Deque61B<String> lld1 = new LinkedListDeque61B<>();
        assertThat(lld1.removeFirst()).isEqualTo(null);

        lld1.addFirst("front"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.removeFirst()).isEqualTo("front");
        assertThat(lld1.toList()).isEmpty();

        // To test when first and last is the same one
        lld1.addFirst("pig"); // after this call we expect: ["front", "middle", "back"]
        lld1.addLast("dog"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.removeFirst()).isEqualTo("pig");
        assertThat(lld1.toList()).containsExactly( "dog").inOrder();
    }

    @Test
    public void removeLastTestBasic() {
        // Remove from empty, remove from one, remove from two
        Deque61B<String> lld1 = new LinkedListDeque61B<>();
        assertThat(lld1.removeLast()).isEqualTo(null);

        lld1.addFirst("front"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.removeLast()).isEqualTo("front");
        assertThat(lld1.toList()).isEmpty();

        // To test when first and last is the same one
        lld1.addFirst("pig"); // after this call we expect: ["front", "middle", "back"]
        lld1.addLast("dog"); // after this call we expect: ["front", "middle", "back"]
        assertThat(lld1.removeLast()).isEqualTo("dog");
        assertThat(lld1.toList()).containsExactly( "pig").inOrder();
    }
}