package DisjointedSets;

public class QuickFindDS implements DisjointSets {
    private int[] id;

    @Override
    public boolean isConnected(int p, int q) {
        // Theta constant
        // Map the index of id to the set,
        // constant time to find
        return id[p] == id[q];
    }

    @Override
    public void connect(int p, int q) {
        // Theta N
        int pid = id[p];
        int qid = id[q];
        for (int i = 0; i < id.length; i++) {
            if (id[i] == pid) {
                id[i] = qid;
            }
        }
    }
}
