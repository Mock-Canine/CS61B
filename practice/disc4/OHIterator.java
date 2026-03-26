package disc4;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class OHIterator implements Iterator<OHRequest> {
    private OHRequest curr;

    public OHIterator(OHRequest request) {
        curr = request;
    }

    public static boolean isGood(String description) {
        return description.length() >= 5;
    }

    @Override
    public boolean hasNext() {
        while (curr != null && !isGood(curr.description)) {
            curr = curr.next;
        }
        // the return value can be a condition, not only
        // pure false or true!!(because we exit the loop in different
        // situations
        return curr != null;
    }

    @Override
    public OHRequest next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        OHRequest temp = curr;
        curr = curr.next;
        if (temp.description.contains("thank u")) {
            hasNext();
        }
        return temp;
    }
}
