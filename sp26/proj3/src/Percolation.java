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
    // Only simulate the open elements, not the blocked ones
    private final WeightedQuickUnionUF sites;
    // openState[row][col] is valid for each (row, col)
    // fullState[row][col] is only valid for elements that are the root in the union
    // and for non-root elements, the result is garbage
    private final boolean[][] openState, fullState;
    private int openNum;
    private final int size;

    /**
     * Initialize percolation matrix
     * @param N dimension of the matrix
     */
    public Percolation(int N) {
        if (N <= 0) {
            throw new IllegalArgumentException("input size should be positive");
        }
        sites = new WeightedQuickUnionUF(N * N);
        openState = new boolean[N][N];
        fullState = new boolean[N][N];
        openNum = 0;
        size = N;
    }

    /**
     * Open the element at (row, col) in the percolation matrix
     * @param row row of the element
     * @param col column of the element
     */
    public void open(int row, int col) {
        // TODO: modify the findRoot algorithm for top line element, no need to find their root
        outOfBound(row, col);
        if (openState[row][col]) {
            return;
        }
        openState[row][col] = true;
        openNum++;
        // Simulate percolate process
        // Find the root of its opened neighbors, if any one or me is full, change
        // all the roots(possible root for the whole set) to full, and connect me
        // with the roots
        List<int[]> neighbors = findNeighbors(row, col);
        List<int[]> roots = new ArrayList<>();
        // Full state of me
        boolean isFull = row == 0;
        for (int[] n : neighbors) {
            int[] root = findRoot(n[0], n[1]);
            roots.add(root);
            isFull = isFull || fullState[root[0]][root[1]];
        }
        fullState[row][col] = isFull;
        for (int[] root : roots) {
            int rowR = root[0];
            int colR = root[1];
            fullState[rowR][colR] = isFull;
            // Mutate the state of sites
            union(row, col, rowR, colR);
        }
    }

    /**
     * Check the open state of element at (row, col) in the percolation matrix
     * @param row row of the element
     * @param col column of the element
     * @return if the element has been opened
     */
    public boolean isOpen(int row, int col) {
        outOfBound(row, col);
        return openState[row][col];
    }

    /**
     * Check the full state of element at (row, col) in the percolation matrix
     * @param row row of the element
     * @param col column of the element
     * @return if the element is full
     */
    public boolean isFull(int row, int col) {
        if (!isOpen(row, col)) {
            return false;
        }
        int[] root = findRoot(row, col);
        return fullState[root[0]][root[1]];
        // TODO: Optimize later for opened top line elements
    }

    public int numberOfOpenSites() {
        // sites.count(): block + openSites
        int i = sites.count();
        return openNum + sites.count() - size * size;
    }

    public boolean percolates() {
        // TODO: Modify this implementation
        for (int i = 0; i < size; i++) {
            if (isFull(size - 1, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the index is out of the boundary of percolation matrix
     * @param row row of the element
     * @param col column of the element
     */
    private void outOfBound(int row, int col) {
        if (outOfBoundCon(row, col)) {
            throw new IndexOutOfBoundsException(String.format("(%s, %s) is out of bound size: %s",
                      row, col, this.size));
        }
    }

    private boolean outOfBoundCon(int row, int col) {
        return row < 0 || row >= size || col < 0 || col >= size;
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
     */
    private List<int[]> findNeighbors(int row, int col) {
        List<int[]> result = new ArrayList<>();
        appendOpenNode(row - 1, col, result);
        appendOpenNode(row + 1, col, result);
        appendOpenNode(row, col - 1, result);
        appendOpenNode(row, col + 1, result);
        return result;
    }

    /**
     * Append (row, col) to input list if it is open
     * no change if out of bound or block
     * @param row row of the element
     * @param col column of the element
     */
    private void appendOpenNode(int row, int col, List<int[]> dst) {
        if (!outOfBoundCon(row, col) && openState[row][col]) {
            dst.add(new int[]{row, col});
        }
    }

    /**
     * Find the root of an element in the QuickUnion,
     * the element should be in the percolation matrix and opened
     * @param row row of the element
     * @param col column of the element
     * @return binary array containing position of the root in the matrix
     */
    private int[] findRoot(int row, int col) {
        int idx = matrix2Array(row, col);
        return array2Matrix(sites.find(idx));
    }

    /**
     * Connect two elements in the QuickUnion,
     * the elements should be in the percolation matrix and opened
     * @param pX row of p
     * @param pY column of p
     * @param qX row of q
     * @param qY column of q
     */
    private void union(int pX, int pY, int qX, int qY) {
        int idxP = matrix2Array(pX, pY);
        int idxQ = matrix2Array(qX, qY);
        sites.union(idxP, idxQ);
    }
}
