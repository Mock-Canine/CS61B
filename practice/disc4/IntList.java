package disc4;

// Naked list
public class IntList {
    public int first;
    public IntList rest;

    public IntList(int f, IntList r) {
        first = f;
        rest = r;
    }

    public static void replace(IntList L, int a, int b) {
        IntList p = L;
        while (p != null) {
            if (p.first == a) {
                p.first = b;
            }
            p = p.rest;
        }
    }

    public static IntList replaces(IntList L, int a, int b) {
        if (L == null) {
            return null;
        }
        if (L.first == a) {
            return new IntList(b, replaces(L.rest, a, b));
        } else {
            return new IntList(L.first, replaces(L.rest, a, b));
        }
    }

    public static void replacess(IntList L, int a, int b) {
        if (L == null) {
            return;
        }
        if (L.first == a) {
            L.first = b;
        }
        replacess(L.rest, a, b);
    }

    public static void removeDuplicates(IntList p) {
        // modify
        if (p == null) {
            return;
        }
        IntList curr = p.rest;
        IntList prev = p;
        // Two tasks
        // 1. tweak the chain if duplicate
        // 2. move the prev and curr if necessary
        while (curr != null) {
//            IntList newPrev, newCurr;
            if (curr.first == prev.first) {
                prev.rest = curr.rest;
//                 newPrev = curr.rest;
//                 newCurr = newPrev.rest;
//                 prev.rest = newPrev;
            } else {
                prev = curr;
//                 newPrev = curr;
//                 newCurr = curr.rest;
            }
//            prev = newPrev;
//            curr = newCurr;
            curr = curr.rest;
        }
    }

    public void skippify() {
        IntList p = this;
        int n = 1;
        while (p != null) {
            IntList next = p.rest;
            for (int i = 0; i < n; i++) {
                if (next == null) {
                    return;
                }
                next = next.rest;
            }
            p.rest = next;
            p = next;
            n++;
        }
    }

    static void main() {
        IntList lst = new IntList(6, new IntList(5, new IntList(4,
                      new IntList(3, new IntList(2, new IntList(1, null))))));
        lst.skippify();
    }
}
