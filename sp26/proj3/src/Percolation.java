import edu.princeton.cs.algs4.WeightedQuickUnionUF;

public class Percolation {
    private final WeightedQuickUnionUF sites;
    private final boolean[] openState, fullState;
    private final int size;

    public Percolation(int N) {
        if (N <= 0) {
            throw new IllegalArgumentException("input size should be positive");
        }
        sites = new WeightedQuickUnionUF(N);
        openState = new boolean[N * N];
        fullState = new boolean[N * N];
        size = N;
    }

    public void open(int row, int col) {
        outOfBound(row, col);
        openState[matrix2Array(row, col)] = true;
    }

    public boolean isOpen(int row, int col) {
        outOfBound(row, col);
        return openState[matrix2Array(row, col)];
    }

    public boolean isFull(int row, int col) {
        outOfBound(row, col);
        return false;
    }

    public int numberOfOpenSites() {
        // TODO: Fill in this method.
        return 0;
    }

    public boolean percolates() {
        // TODO: Fill in this method.
        return false;
    }

    private void outOfBound(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IndexOutOfBoundsException(String.format("(%s, %s) is out of bound size: %s",
                      row, col, this.size));
        }
    }

    private int matrix2Array(int row, int col) {
        return row * size + col;
    }

    private boolean bornFull(int row) {
        return row == 0;
    }

    private boolean rootFull(int row, int col) {
        int idx = matrix2Array(row, col);
        int root = sites.find(idx);
        return fullState[root];
    }

    static void main() {
        Percolation p = new Percolation(5);
    }
}
