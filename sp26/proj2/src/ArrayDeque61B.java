import java.util.ArrayList;
import java.util.List;

public class ArrayDeque61B<T> implements Deque61B<T> {
    private int SIZE = 8;
    private final int FIRSTTAG = 0;
    private final int LASTTAG = 1;
    private final int RFACTOR = 2;
    private final double USERATIO = 0.25;

    // Class is a state machine, and its state is contained in these
    // four boxes, so tests should verify the content of the boxes
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] items;

    public ArrayDeque61B() {
        size = 0;
        nextFirst = FIRSTTAG;
        nextLast = LASTTAG;
        items = (T[]) new Object[SIZE];
    }

    @Override
    public void addFirst(T x) {
        items[nextFirst] = x;
        nextFirst = (nextFirst - 1 + SIZE) % SIZE;
        size++;
    }

    @Override
    public void addLast(T x) {
        items[nextLast] = x;
        nextLast = (nextLast + 1) % SIZE;
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
        return items[(nextFirst + 1) % SIZE];
    }

    @Override
    public T getLast() {
        return items[(nextLast - 1 + SIZE) % SIZE];
    }

    @Override
    public T removeFirst() {
        if (size() == 0) {
            return null;
        }
        T first = getFirst();
        nextFirst = (nextFirst + 1) % SIZE;
        items[nextFirst] = null;
        size--;
        return first;
    }

    @Override
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        T last = getLast();
        nextLast = (nextLast - 1 + SIZE) % SIZE;
        items[nextLast] = null;
        size--;
        return last;
    }

    @Override
    public T get(int index) {
        if (size == 0 || index < 0 || index >= size) {
            return null;
        }
        return items[(nextFirst + 1 + index) % SIZE];
    }

    @Override
    public T getRecursive(int index) {
        throw new UnsupportedOperationException("No need to implement getRecursive for ArrayDeque61B.");
    }

    public void resize(int capacity) {

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
