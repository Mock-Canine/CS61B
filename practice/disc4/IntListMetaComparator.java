package disc4;

import java.util.Comparator;

public class IntListMetaComparator implements Comparator<IntList> {
    // I think it is a wrapper like spy
    private Comparator<Integer> givenC;
    public IntListMetaComparator(Comparator<Integer> givenC) {
        this.givenC = givenC;
    }

    @Override
    public int compare(IntList o1, IntList o2) {
        // it is comparator's freedom to decide to accept null or not
        if (o1 == null || o2 == null) {
            return 0;
        }
        int compValue = givenC.compare(o1.first, o2.first);
        int offset = Integer.compare(compValue, 0);
        return compare(o1.rest, o2.rest) + offset;
    }
}
