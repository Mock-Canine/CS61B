package disc4;

public class DLList {
    Node sentinel;
    public DLList() {}
    public class Node{int item; Node prev; Node next; }
    public void removeDuplicates() {
        // begin at first and check if the latter items are the same as me
        Node ref = sentinel.next;
        Node checker;
        while (ref != sentinel) {
            checker = ref.next;
            while (checker != sentinel) {
                if (ref.item == checker.item) {
                    Node checkerPrev = checker.prev;
                    Node checherNext = checker.next;
                    checkerPrev.next = checherNext;
                    checherNext.prev = checkerPrev;
                }
                checker = checker.next;
            }
            ref = ref.next;
        }
    }
}
