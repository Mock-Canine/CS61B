package List;

import edu.princeton.cs.algs4.In;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// A wrapper to IntNode that can contain metadata like size,
// and provide isolation of the IntNode
public class SLList {
    private static class IntNode {
        int item;
        IntNode next;

        IntNode(int i, IntNode n) {
            item = i;
            next = n;
        }
    }

    // Avoid the special case of null list
    // Thought -> avoid the special case by modifying the data structure
    // and make invariants more
    private final IntNode sentinel;
    private int size;

    SLList() {
        sentinel = new IntNode(67, null);
    }

    SLList(int i) {
        sentinel = new IntNode(67, null);
        sentinel.next = new IntNode(i, null);
        size++;
    }

    public int getFirst() {
        return sentinel.next.item;
    }

    public void addFirst(int i) {
        size++;
        sentinel.next = new IntNode(i, sentinel.next);
    }

//    public int size() {
//        IntNode n = first;
//        int len = 1;
//        while (n.next != null) {
//            n = n.next;
//            len++;
//        }
//        return len;
//    }
    // no need to use length to iterate the list, null is enough
//    private static int size (IntNode p) {
//        if (p.next == null) {
//            return 1;
//        }
//        return size(p.next) + 1;
//    }
//
//    // Hide the recursive structure underneath the hood
//    public int size() {
//        return size(sentinel.next);
//    }

    public int size() {
        return size;
    }

    public void addLast(int i) {
        size++;
        IntNode n = sentinel;
//        ugly special case
//        if (n == null) {
//            first = new IntNode(i, null);
//            return;
//        }
        while (n.next != null) {
            n = n.next;
        }
        n.next = new IntNode(i, null);
    }

    static void main() {
        SLList L = new SLList();
        L.addLast(8);
        L.addFirst(10);
        L.addFirst(12);
        IO.println(L.getFirst());
        IO.println(L.size());
    }
}
