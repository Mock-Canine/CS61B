package DisjointedSets;

public class WeightedDSArray implements DisjointSets {
    private int[] parent;
    private int[] weight;
    private final int size;

    WeightedDSArray(int s) {
        size = s;
        parent = new int[size];
        weight = new int[size];
        for (int i = 0; i < size; i++) {
            // no parent, is the root
            parent[i] = -1;
            // Elements are alone at first
            weight[i] = 1;
        }
    }

    private int root(int e) {
        if (parent[e] == -1) {
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
        // Verify p and q are in my sets, verify p and q are not connected(find the root of each first)
        // connect -> verify the weight of each tree(weight[root]), pay attention to the size equal case
        // change the weight of the root of the large tree, the parent of the small tree
        if (outOfBound(p) || outOfBound(q)) {
            throw new RuntimeException("idx out of bound");
        }
        int rootP = root(p);
        int rootQ = root(q);
        if (rootP == rootQ) {
            return;
        }

        boolean isPLarge = weight[rootP] > weight[rootQ];
        int largeRoot = isPLarge ? rootP : rootQ;
        int smallRoot = isPLarge ? rootQ : rootP;

        weight[largeRoot] += weight[smallRoot];
        parent[smallRoot] = largeRoot;
    }

    static void main() {
        DisjointSets ds = new WeightedDSArray(7);
        ds.connect(0, 1);
        ds.connect(2, 3);
        ds.connect(0, 3);
        ds.connect(3, 5);
        boolean j = ds.isConnected(2, 4);
        boolean k = ds.isConnected(3, 0);
        ds.connect(4, 2);
        ds.connect(4, 6);
        ds.connect(3, 6);
        boolean q = ds.isConnected(3, 0);
    }
}
