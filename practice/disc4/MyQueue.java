package disc4;

public interface MyQueue<E> {
    void enqueue(E element);
    E dequeue();
    boolean isEmpty();
    int size();
    default void clear() {
        while (!isEmpty()) {
            dequeue();
        }
    }

    default void remove(E item) {
        for (int i = size(); i > 0; i--) {
            E first = dequeue();
            if (!first.equals(item)) {
                enqueue(first);
            }
        }
    }

    default void appendAll(MyQueue<E> otherQueue) {
        for (int i = otherQueue.size(); i > 0; i--) {
            E first = otherQueue.dequeue();
            enqueue(first);
            otherQueue.enqueue(first);
        }
    }
}
