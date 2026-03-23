package disc3;

public class Interweave {
    public static IntList[] interweave(IntList lst, int k) {
        IntList[] array = new IntList[k];
        int index = k - 1;
        // IntList is much easier to addFirst then addLast, so reverse is needed
        IntList L = reverse(lst);
        while (L != null) {
            IntList prevAtIndex = array[index];
            IntList next = L.rest;
            L.rest = prevAtIndex;
            array[index]  = L;
            L = next;
            index--;
            if (index == -1) {
                index = k - 1;
            }
        }
        return array;
    }
    // Because IntList is a naked data structure with recursive attribute, i can do this recursively
    private static IntList reverseHelp(IntList lst, IntList newRest) {
        // Corner case
        if (lst == null) {
            return null;
        }
        IntList oldRest = lst.rest;
        lst.rest = newRest;
        // Terminal case
        if (oldRest == null) {
            return lst;
        }
        // Tail recursion
        return reverseHelp(oldRest, lst);
    }

    public static IntList reverse(IntList lst) {
        return reverseHelp(lst, null);
    }

    static void main() {
        IntList lst = new IntList(6, new IntList(5, new IntList(4,
                      new IntList(3, new IntList(2, new IntList(1, null))))));
//        IntList L = reverse(lst);
        IntList[] arr = interweave(lst, 3);
    }
}
