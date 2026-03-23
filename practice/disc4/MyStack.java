package disc4;

import java.util.ArrayList;
import java.util.List;

public interface MyStack<E> {
    void push(E element);
    E pop();
    boolean isEmpty();
    int size();

    private void insertAtBottom(E item) {
        // passing params never change!!
        if (isEmpty()) {
            push(item);
            return;
        }

        E topElem = pop();
        insertAtBottom(item);
        push(topElem);
    }

    default void flip() {
        // The params are not the sign of whether a problem is recursive,
        // It is recursive when the size of the problem is shrinking, hitting
        // the base case, every function invoke has the same pattern
        // To invoke has the format of stack
        if (isEmpty()) {
            return;
        }
        E topElem = pop();
        flip();
        insertAtBottom(topElem);
    }
}
