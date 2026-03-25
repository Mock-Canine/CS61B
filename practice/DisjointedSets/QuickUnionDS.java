package DisjointedSets;

public class QuickUnionDS implements DisjointSets {
    private int[] parent;
    private int size;

    QuickUnionDS(int s) {
        size = s;
        parent = new int[size];
        for (int i = 0; i < size; i++) {
            // no parent, is the root
            parent[i] = -1;
        }
    }

    private int root(int e) {
        if (parent[e] == -1) {
            return e;
        }
        return root(parent[e]);
    }

    @Override
    // Worst case -> Theta N
    public boolean isConnected(int p, int q) {
        return root(p) == root(q);
    }

    @Override
    // Worst case -> Theta N
    public void connect(int p, int q) {
        parent[root(p)] = root(q);
    }
}
