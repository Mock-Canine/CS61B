package DisjointedSets;

public interface DisjointSets {
    public boolean isConnected(int p, int q);
    public void connect(int p, int q);
}
