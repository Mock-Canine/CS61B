import edu.princeton.cs.algs4.WeightedQuickUnionUF;

import java.util.ArrayList;
import java.util.List;

public class Percolation {
    // Only simulate the open nodes, not the block ones
    private final WeightedQuickUnionUF sites;
    private final boolean[][] openState, fullState;
    private final int size;

    public Percolation(int N) {
        if (N <= 0) {
            throw new IllegalArgumentException("input size should be positive");
        }
        sites = new WeightedQuickUnionUF(N);
        openState = new boolean[N][N];
        fullState = new boolean[N][N];
        size = N;
    }

    public void open(int row, int col) {
        outOfBound(row, col);
        openState[row][col] = true;

        // Separate into unionNeighbors()
//        List<Integer> roots = neighborsRoot(row, col);

    }

    public boolean isOpen(int row, int col) {
        outOfBound(row, col);
        return openState[row][col];
    }

    public boolean isFull(int row, int col) {
        if (!isOpen(row, col)) {
            return false;
        }
        int root = findRoot(row, col);
        int[] rootXY = array2Matrix(root);
        return fullState[rootXY[0]][rootXY[1]];
//        return bornFull(row) || isRootFull(row, col);
        // Optimize latter
    }

    public int numberOfOpenSites() {
        // TODO: Fill in this method.
        return 0;
    }

    public boolean percolates() {
        // TODO: Fill in this method.
        return false;
    }

    /* All helper methods will not make boundary check */
    private void outOfBound(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IndexOutOfBoundsException(String.format("(%s, %s) is out of bound size: %s",
                      row, col, this.size));
        }
    }

    private int matrix2Array(int row, int col) {
        return row * size + col;
    }

    private int[] array2Matrix(int idx) {
        return new int[]{idx / size, idx % size};
    }

    /** Returns a list, contains the index of the root of neighbors(four directions, should be open), in the
       WeightedQuickUnion */
    private List<Integer> neighborsRoot(int row, int col) {
        List<Integer> result = new ArrayList<>();
        // Maybe add null if no root will be found
        result.add(findRoot(row - 1, col));
        result.add(findRoot(row + 1, col));
        result.add(findRoot(row, col - 1));
        result.add(findRoot(row, col + 1));
        return result;
    }

    /** Assume taking in (row, col) which is open and in the bound */
    private int findRoot(int row, int col) {
        int idx = matrix2Array(row, col);
        return sites.find(idx);
    }
}
