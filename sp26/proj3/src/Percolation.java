import edu.princeton.cs.algs4.WeightedQuickUnionUF;

import java.util.ArrayList;
import java.util.List;

/**
 * Create an N*N matrix to simulate percolation from top to bottom
 * states for an element in the matrix:
 *        open: an element being opened;
 *        full: elements in the top line of the matrix is full once opened,
 *              opened elements can be percolated to become full when its site
 *              is full
 *        block: initial state of the elements
 * states for a site(a set of connected and opened elements in the matrix):
 *        open: initial state
 *        full: once the site connects to a full element
 * states for the whole system:
 *        percolate: any element in the bottom line of the matrix is full
 *        not percolate: vice visa
 */
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

    /**
     * Open the element at (row, col) in the percolation matrix
     * @param row row
     * @param col
     */
    public void open(int row, int col) {
/*        outOfBound(row, col);
        openState[row][col] = true;
        // Separate into unionNeighbors() latter
        List<int[]> targets = new ArrayList<>();
        targets.add(new int[]{row, col});

        int idx = matrix2Array(row, col);
        boolean isFull = isFull(row, col);
        List<Integer> roots = neighborsRoot(row, col);
        for (Integer root : roots) {
            if (root != null) {
                int[] rootXY = array2Matrix(root);
                targets.add(rootXY);
                if (isFull(rootXY[0], rootXY[1])) {
                    isFull = true;
                }
                sites.union(idx, root);
            }
        }
        if (isFull) {
            for (int[] target : targets) {
                fullState[target[0]][target[1]] = true;
            }
        }*/
    }

    /**
     * Check the open state of element at (row, col) in the percolation matrix
     * @param row row of the element
     * @param col column of the element
     * @return if the element has been opened
     */
    public boolean isOpen(int row, int col) {
//        outOfBound(row, col);
//        return openState[row][col];
        return false;
    }

    /**
     * Check the full state of element at (row, col) in the percolation matrix
     * @param row row of the element
     * @param col column of the element
     * @return if the element is full
     */
    public boolean isFull(int row, int col) {
/*        if (!isOpen(row, col)) {
            return false;
        }
        int root = findRoot(row, col);
        int[] rootXY = array2Matrix(root);
        return row == 0 || fullState[rootXY[0]][rootXY[1]];
//        return bornFull(row) || isRootFull(row, col);
        // Optimize latter*/
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

    /**
     * Check if the index is out of the boundary of percolation matrix
     * @param row row of the element
     * @param col column of the element
     * @return true if index out of bound
     */
    private boolean outOfBound(int row, int col) {
//        if (row < 0 || row >= size || col < 0 || col >= size) {
//            throw new IndexOutOfBoundsException(String.format("(%s, %s) is out of bound size: %s",
//                      row, col, this.size));
//        }
    }

    /**
     * Map an element in the percolation matrix to its index in the QuickUnion
     * the element should be opened
     * @param row row of the element
     * @param col column of the element
     * @return index of the element in the QuickUnion
     */
    private int matrix2Array(int row, int col) {
        return row * size + col;
    }

    /**
     * Map the element in the QuickUnion to its position in the percolation matrix
     * @param idx valid index in the QuickUnion
     * @return (row, col) binary array of the element
     */
    private int[] array2Matrix(int idx) {
        return new int[]{idx / size, idx % size};
    }

    /**
     * Find the opened neighbors(up, down, left, right) of an element in the percolation matrix
     * the element should be opened
     * @param row row of the element
     * @param col column of the element
     * @return a list containing the (row, col) binary array of opened neighbors,
     *         null if not found
     */
    private List<int[]> neighborsRoot(int row, int col) {
//        List<Integer> result = new ArrayList<>();
//        result.add(isOpenHelper(row - 1, col) ? findRoot(row - 1, col) : null);
//        result.add(isOpenHelper(row + 1, col) ? findRoot(row + 1, col) : null);
//        result.add(isOpenHelper(row, col - 1) ? findRoot(row, col - 1) : null);
//        result.add(isOpenHelper(row, col + 1) ? findRoot(row, col + 1) : null);
//        return result;
    }

    /**
     * Find the root of an element in the QuickUnion,
     * the element should be in the percolation matrix and opened
     * @param row row of the element
     * @param col column of the element
     * @return index of the root in the QuickUnion
     */
    private int findRoot(int row, int col) {
        int idx = matrix2Array(row, col);
        return sites.find(idx);
    }
}
