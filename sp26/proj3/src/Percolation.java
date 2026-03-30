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
    // doubleChainSites used for percolate(), topChainSites used for isFull()
    private final WeightedQuickUnionUF doubleChainSites;
    private final WeightedQuickUnionUF topChainSites;
    private final int[] topNode;
    private final int[] bottomNode;
    // Special elements: top and bottom line
    private final boolean[][] openState;
    // The number of elements in the matrix being opened
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
        doubleChainSites = new WeightedQuickUnionUF(N * N + 2);
        topChainSites = new WeightedQuickUnionUF(N * N + 1);
        // Pretend to be in the matrix
        topNode = new int[]{N - 1, N};
        bottomNode = new int[]{N - 1, N + 1};
        openState = new boolean[N][N];
        openNum = 0;
        size = N;
    }

    /**
     * Open the element at (row, col) in the percolation matrix
     * @param row row of the element
     * @param col column of the element
     */
    public void open(int row, int col) {
        outOfBound(row, col);
        if (openState[row][col]) {
            return;
        }
        openState[row][col] = true;
        openNum++;
        // Virtual nodes should only be chained when a node is opened,
        // not when the system initialize
        chainVirtual(row, col);
        // Simulate percolate process
        // Find its opened neighbors, connect together
        List<int[]> neighbors = findNeighbors(row, col);
        for (int[] n : neighbors) {
            union(row, col, n[0], n[1], doubleChainSites);
            union(row, col, n[0], n[1], topChainSites);
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
        if (row == 0) {
            return true;
        }
        return isConnected(row, col, topNode[0], topNode[1], topChainSites);
    }

    public int numberOfOpenSites() {
        return openNum;
    }

    public boolean percolates() {
        return isConnected(bottomNode[0], bottomNode[1], topNode[0], topNode[1], doubleChainSites);
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
     * Chain element to virtual node if it is in the top or bottom line
     * the element should be in the percolation matrix and opened
     * @param row row of the element
     * @param col column of the element
     */
    private void chainVirtual(int row, int col) {
        if (row == 0) {
            union(row, col, topNode[0], topNode[1], doubleChainSites);
            union(row, col, topNode[0], topNode[1], topChainSites);
        }
        if (row == size - 1) {
            union(row, col, bottomNode[0], bottomNode[1], doubleChainSites);
        }
    }

    /**
     * Check if two elements is connected in the QuickUnion,
     * the elements should be in the percolation matrix and opened
     * @param pX row of p
     * @param pY column of p
     * @param qX row of q
     * @param qY column of q
     * @param uf which QuickUnion to find
     * @return is connected or not
     */
    private boolean isConnected(int pX, int pY, int qX, int qY, WeightedQuickUnionUF uf) {
        int idxP = matrix2Array(pX, pY);
        int idxQ = matrix2Array(qX, qY);
        return uf.connected(idxP, idxQ);
    }

    /**
     * Connect two elements in the QuickUnion,
     * the elements should be in the percolation matrix and opened
     * @param pX row of p
     * @param pY column of p
     * @param qX row of q
     * @param qY column of q
     * @param uf which QuickUnion to connect
     */
    private void union(int pX, int pY, int qX, int qY, WeightedQuickUnionUF uf) {
        int idxP = matrix2Array(pX, pY);
        int idxQ = matrix2Array(qX, qY);
        uf.union(idxP, idxQ);
    }
}
