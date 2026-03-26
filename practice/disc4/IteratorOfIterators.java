package disc4;

import java.util.*;

public class IteratorOfIterators implements Iterator<Integer> {
    private List<Iterator<Integer>> iterators;
    private int curr;
    public IteratorOfIterators(List<Iterator<Integer>> a) {
        // iterator needs initialization, and the inner elements are null by default
        iterators = new LinkedList<>();
        for (Iterator<Integer> iterator : a) {
            // has to ensure iterator is not null
            if (iterator.hasNext()) {
                iterators.add(iterator);
            }
        }
//        for (int i = 0; i < a.size(); i++) {
//            // set does not allow null
//            if (a.get(i) != null) {
//                iterators.set(i, a.get(i));
//            }
//        }
        curr = 0;
    }
    @Override
    public boolean hasNext() {
        return !iterators.isEmpty();
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Iterator<Integer> currIterator = iterators.get(curr);
        int result = currIterator.next();
        if (!currIterator.hasNext()) {
            iterators.remove(curr);
            if (iterators.isEmpty()) {
                curr = -1;
            }
        } else {
            curr = (curr + 1) % iterators.size();
        }
        return result;
    }
}
