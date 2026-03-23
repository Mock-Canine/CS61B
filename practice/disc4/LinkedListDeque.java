package disc4;

public class LinkedListDeque<T> {
    private Node sentinel;
    private int size;
    public LinkedListDeque() {}
    private class Node {
        private T value;
        private Node next, prev;
    }

    public void rotateLeft(int x) {
        if (size <= 1) {
            return;
        }
        // x % size
        for (int i = 0; i < x; i++) {
            // removeFirst() + addLast()
            Node oldLast = sentinel.prev;
            Node oldFirst = sentinel.next;
            Node oldSecond = oldFirst.next;

            sentinel.next = oldSecond;
            oldSecond.prev = sentinel;

            oldFirst.prev = oldLast;
            oldFirst.next = sentinel;
            oldLast.next = oldFirst;
            sentinel.prev = oldFirst;
        }
    }
}
