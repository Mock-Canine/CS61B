import java.util.ArrayList;
import java.util.List;

public class ArrayDeque61B<T> implements Deque61B<T> {
    private int ARRLEN = 8;
    private final int RFACTOR = 2;
    private final double USERATIO = 0.25;
    private final int LENBOUNDARY = 16;

    // Class is a state machine, and its state is contained in these
    // four boxes, so tests should verify the content of the boxes
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] items;

    public ArrayDeque61B() {
        size = 0;
        // FirstTag is the last element, lastTag is the [size] th element
        nextFirst = ARRLEN - 1;
        nextLast = size;
        items = (T[]) new Object[ARRLEN];
    }

    @Override
    public void addFirst(T x) {
        if (size() == ARRLEN) {
            resize(RFACTOR * ARRLEN);
        }
        items[nextFirst] = x;
        nextFirst = (nextFirst - 1 + ARRLEN) % ARRLEN;
        size++;
    }

    @Override
    public void addLast(T x) {
        if (size() == ARRLEN) {
            resize(RFACTOR * ARRLEN);
        }
        items[nextLast] = x;
        nextLast = (nextLast + 1) % ARRLEN;
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
        return items[(nextFirst + 1) % ARRLEN];
    }

    @Override
    public T getLast() {
        return items[(nextLast - 1 + ARRLEN) % ARRLEN];
    }

    @Override
    public T removeFirst() {
        if (size() == 0) {
            return null;
        }
        boolean enoughElement = size() > (int) (USERATIO * ARRLEN);
        if (ARRLEN >= LENBOUNDARY && !enoughElement) {
            resize((int) (ARRLEN / RFACTOR));
        }
        T first = getFirst();
        nextFirst = (nextFirst + 1) % ARRLEN;
        items[nextFirst] = null;
        size--;
        return first;
    }

    @Override
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        boolean enoughElement = size() > (int) (USERATIO * ARRLEN);
        if (ARRLEN >= LENBOUNDARY && !enoughElement) {
            resize((int) (ARRLEN / RFACTOR));
        }
        T last = getLast();
        nextLast = (nextLast - 1 + ARRLEN) % ARRLEN;
        items[nextLast] = null;
        size--;
        return last;
    }

    @Override
    public T get(int index) {
        if (size == 0 || index < 0 || index >= size) {
            return null;
        }
        return items[(nextFirst + 1 + index) % ARRLEN];
    }

    @Override
    public T getRecursive(int index) {
        throw new UnsupportedOperationException("No need to implement getRecursive for ArrayDeque61B.");
    }

    private void resize(int capacity) {
        T[] newArr = (T[]) new Object[capacity];
        for (int i = 0; i < size(); i++) {
            newArr[i] = get(i);
        }
        nextLast = size();
        nextFirst = capacity - 1;
        ARRLEN = capacity;
        items = newArr;
    }

//    static void main() {
//        Deque61B<Integer> L = new ArrayDeque61B<>();
//        // Verify circular array
//        // Test addFirst
//        L.addFirst(4);
//        L.addFirst(5);
//        L.addFirst(6);
//        L.addFirst(7);
//        L.addFirst(8);
//        L.addFirst(9);
//
//        Deque61B<Integer> LL = new ArrayDeque61B<>();
//        // Verify circular array
//        // Test addLast
//        LL.addLast(4);
//        LL.addLast(5);
//        LL.addLast(6);
//        LL.addLast(7);
//        LL.addLast(8);
//        LL.addLast(9);
//    }
}
