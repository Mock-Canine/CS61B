package DisjointedSets;

public class WeightedQuickUnionDS implements DisjointSets {
    private int[] parent;
    private int size;

    WeightedQuickUnionDS(int s) {
        size = s;
        parent = new int[size];
        for (int i = 0; i < size; i++) {
            // no parent, is the root
            parent[i] = -1;
        }
    }

    private int root(int e) {
        if (parent[e] < 0) {
            return e;
        }
        return root(parent[e]);
    }

    private boolean outOfBound(int e) {
        return e < 0 || e >= size;
    }

    @Override
    public boolean isConnected(int p, int q) {
        if (outOfBound(p) || outOfBound(q)) {
            throw new RuntimeException("idx out of bound");
        }
        return root(p) == root(q);
    }

    @Override
    public void connect(int p, int q) {
        int rp = root(p);
        int rq = root(q);
        // Connected
        if (rp == rq) {
            return;
        }
        int sizePTree = Math.abs(parent[rp]);
        int sizeQTree = Math.abs(parent[rq]);

        // rp != rq, so can use value to distinguish them
        int largeRoot = sizePTree > sizeQTree ? rp : rq;
        int smallRoot = largeRoot == rp ? rq : rp;
        parent[largeRoot] += parent[smallRoot];
        parent[smallRoot] = largeRoot;
    }

    static void main() {
        DisjointSets S = new WeightedQuickUnionDS(8);
        for (int i = 0; i < 7; i++) {
            S.connect(i, i + 1);
        }
        boolean c = S.isConnected(4, 7);
        S.connect(4, 6);
    }
}
