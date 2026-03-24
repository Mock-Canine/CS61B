import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class ArrayDeque61B<T> implements Deque61B<T> {
    private class ArrayDequeIterator implements Iterator<T> {
        private int idx;
        public ArrayDequeIterator() {
            idx = 0;
        }

        @Override
        public boolean hasNext() {
            return idx < size;
        }

        @Override
        public T next() {
            if(!hasNext()) {
                return null;
            }
            return get(idx++);
        }
    }

    private int ARLEN = 8;
    private final int FACTOR = 2;
    private final double USERATIO = 0.25;
    private final int BOUNDARY = 16;

    // Class is a state machine, and its state is contained in these
    // four boxes, so tests should verify the content of the boxes
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] items;

    public ArrayDeque61B() {
        size = 0;
        // FirstTag is the last element, lastTag is the [size] th element
        nextFirst = ARLEN - 1;
        nextLast = size;
        items = (T[]) new Object[ARLEN];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private boolean contains(T item) {
        for (T i : this) {
            if (i.equals(item)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayDeque61B other) {
            if (this.size == other.size) {
                for (T item : this) {
                    if (!other.contains(item)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("[");
        for (T item : this) {
            str.append(item);
            str.append(", ");
        }
        str.append("]");
        return str.toString();
    }

    @Override
    public void addFirst(T x) {
        if (size == ARLEN) {
            resize(FACTOR * ARLEN);
        }
        items[nextFirst] = x;
        nextFirst = (nextFirst - 1 + ARLEN) % ARLEN;
        size++;
    }

    @Override
    public void addLast(T x) {
        if (size == ARLEN) {
            resize(FACTOR * ARLEN);
        }
        items[nextLast] = x;
        nextLast = (nextLast + 1) % ARLEN;
        size++;
    }

    @Override
    public List<T> toList() {
        List<T> returnList = new ArrayList<>();
        // empty
        for (int i = 0; i < size; i++) {
            returnList.add(get(i));
        }
        return returnList;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getFirst() {
        return items[(nextFirst + 1) % ARLEN];
    }

    @Override
    public T getLast() {
        return items[(nextLast - 1 + ARLEN) % ARLEN];
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        boolean enoughElement = size > (int) (USERATIO * ARLEN);
        if (ARLEN >= BOUNDARY && !enoughElement) {
            resize(ARLEN / FACTOR);
        }
        T first = getFirst();
        nextFirst = (nextFirst + 1) % ARLEN;
        items[nextFirst] = null;
        size--;
        return first;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        boolean enoughElement = size > (int) (USERATIO * ARLEN);
        if (ARLEN >= BOUNDARY && !enoughElement) {
            resize(ARLEN / FACTOR);
        }
        T last = getLast();
        nextLast = (nextLast - 1 + ARLEN) % ARLEN;
        items[nextLast] = null;
        size--;
        return last;
    }

    @Override
    public T get(int index) {
        if (size == 0 || index < 0 || index >= size) {
            return null;
        }
        return items[(nextFirst + 1 + index) % ARLEN];
    }

    @Override
    public T getRecursive(int index) {
        throw new UnsupportedOperationException("No need to implement getRecursive for ArrayDeque61B.");
    }

    private void resize(int capacity) {
        T[] newArr = (T[]) new Object[capacity];
        for (int i = 0; i < size; i++) {
            newArr[i] = get(i);
        }
        nextLast = size;
        nextFirst = capacity - 1;
        ARLEN = capacity;
        items = newArr;
    }
}