import java.util.ArrayList;
import java.util.List;

public class ArrayDeque61B<T> implements Deque61B<T> {
    private final int initSize = 8;
    // Class is a state machine, and its state is contained in these
    // four boxes, so tests should verify the content of the boxes
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] items;

    public ArrayDeque61B() {
        size = 0;
        nextFirst = 3;
        nextLast = 4;
        items = (T[]) new Object[initSize];
    }

    @Override
    public void addFirst(T x) {
        items[nextFirst] = x;
        nextFirst -= 1;
        size++;
    }

    @Override
    public void addLast(T x) {
        items[nextLast] = x;
        nextLast += 1;
        size++;
    }

    @Override
    public List<T> toList() {
        List<T> returnList = new ArrayList<>();
        // empty
        for (int i = 0; i < size(); i++) {
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
        return items[nextFirst + 1];
    }

    @Override
    public T getLast() {
        return items[nextLast - 1];
    }

    @Override
    public T removeFirst() {
        if (size() == 0) {
            return null;
        }
        T first = getFirst();
        items[++nextFirst] = null;
        size--;
        return first;
    }

    @Override
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        T last = getLast();
        items[--nextLast] = null;
        size--;
        return last;
    }

    @Override
    public T get(int index) {
        if (size == 0 || index < 0 || index >= size) {
            return null;
        }
        return items[nextFirst + 1 + index];
    }

    @Override
    public T getRecursive(int index) {
        throw new UnsupportedOperationException("No need to implement getRecursive for ArrayDeque61B.");
    }
}
