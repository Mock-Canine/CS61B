import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class PercolationTest {

    /**
     * Enum to represent the state of a cell in the grid. Use this enum to help you write tests.
     * <p>
     * (0) CLOSED: isOpen() returns false, isFull() return false
     * <p>
     * (1) OPEN: isOpen() returns true, isFull() returns false
     * <p>
     * (2) INVALID: isOpen() returns false, isFull() returns true
     *              (This should not happen! Only open cells should be full.)
     * <p>
     * (3) FULL: isOpen() returns true, isFull() returns true
     * <p>
     */
    private enum Cell {
        CLOSED, OPEN, INVALID, FULL
    }

    /**
     * Creates a Cell[][] based off of what Percolation p returns.
     * Use this method in your tests to see if isOpen and isFull are returning the
     * correct things.
     */
    private static Cell[][] getState(int N, Percolation p) {
        Cell[][] state = new Cell[N][N];
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                int open = p.isOpen(r, c) ? 1 : 0;
                int full = p.isFull(r, c) ? 2 : 0;
                state[r][c] = Cell.values()[open + full];
            }
        }
        return state;
    }

    @Test
    public void basicTest() {
        int N = 5;
        Percolation p = new Percolation(N);
        // open sites at (r, c) = (0, 1), (2, 0), (3, 1), etc. (0, 0) is top-left
        int[][] openSites = {
                {0, 1},
                {2, 0},
                {3, 1},
                {4, 1},
                {1, 0},
                {1, 1}
        };
        Cell[][] expectedState = {
                {Cell.CLOSED, Cell.FULL, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED},
                {Cell.FULL, Cell.FULL, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED},
                {Cell.FULL, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED},
                {Cell.CLOSED, Cell.OPEN, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED},
                {Cell.CLOSED, Cell.OPEN, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED}
        };
        for (int[] site : openSites) {
            p.open(site[0], site[1]);
        }
        assertThat(getState(N, p)).isEqualTo(expectedState);
        assertThat(p.percolates()).isFalse();
    }

    /*
     * Testing strategy for each operation of Percolation:
     *
     * all methods:
     *      partition on matrix size: 1, 2, >2
     * open():
     *      partition on (row, col):
     *          block -> open
     *          block -> full
     *          open  -> open
     *          full  -> full
     *      partition on one neighbor of (row, col):
     *          block -> block
     *          open  -> open
     *          open  -> full
     *          full  -> full
     *          no neighbor
     * isOpen(), isFull(), open():
     *      partition on row=0, 0<row<size-1, row=size-1
     *      partition on col=0, 0<col<size-1, col=size-1
     * isOpen(), isFull():
     *      partition on (row, col): block, open, full
     * isOpen(), isFull(), percolates():
     *      partition on return value: true, false
     * numberOfOpenSites():
     *      partition on return value: 0, 1, >1
     *      partition on state of site: open, full
     *      partition on elements of the site: 1, >1
     */


    @Test
    public void size1Test() {
        int N = 1;
        Percolation p = new Percolation(N);
        p.open(0, 0);
        Cell[][] expectedState = {
                {Cell.FULL}
        };
        assertThat(getState(N, p)).isEqualTo(expectedState);
        assertThat(p.percolates()).isTrue();
        assertThat(p.numberOfOpenSites()).isEqualTo(1);
    }

    @Test
    public void size2Test() {
        int N = 2;
        Percolation p = new Percolation(N);
        int[][] openSites = {
                {0, 0},
                {1, 0},
        };
        Cell[][] expectedState = {
                {Cell.FULL, Cell.CLOSED},
                {Cell.FULL, Cell.CLOSED},
        };
        for (int[] site : openSites) {
            p.open(site[0], site[1]);
        }
        assertThat(getState(N, p)).isEqualTo(expectedState);
        assertThat(p.percolates()).isTrue();
        assertThat(p.numberOfOpenSites()).isEqualTo(1);
    }

    /**
     * Cover special cases:
     *      number of sites is larger than 1,
     *      the elements in the sites is larger than 1,
     *      sites are open or full
     */
    @Test
    public void repetitiveTest() {
        int N = 4;
        Percolation p = new Percolation(N);
        int[][] openSites = {
                {0, 2},
                {0, 2},
                {2, 2},
                {2, 2},
                {3, 0},
                {3, 2}
        };
        Cell[][] expectedState = {
                {Cell.CLOSED, Cell.CLOSED, Cell.FULL, Cell.CLOSED},
                {Cell.CLOSED, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED},
                {Cell.CLOSED, Cell.CLOSED, Cell.OPEN, Cell.CLOSED},
                {Cell.OPEN, Cell.CLOSED, Cell.OPEN, Cell.CLOSED},
        };
        for (int[] site : openSites) {
            p.open(site[0], site[1]);
        }
        assertThat(getState(N, p)).isEqualTo(expectedState);
        assertThat(p.percolates()).isFalse();
        assertThat(p.numberOfOpenSites()).isEqualTo(3);
    }

    @Test
    public void emptyTest() {
        int N = 4;
        Percolation p = new Percolation(N);
        assertThat(p.isOpen(2, 2)).isFalse();
        assertThat(p.isFull(2, 2)).isFalse();
        assertThat(p.percolates()).isFalse();
        assertThat(p.numberOfOpenSites()).isEqualTo(0);
    }

    /**
     * Cover special cases:
     *      percolate when large size,
     *      top line element open first -> other elements are percolated to full
     *      non-top line element open first -> when connected to top line, being full
     */
    @Test
    public void percolateTest() {
        int N = 5;
        Percolation p = new Percolation(N);
        int[][] openSites = {
                // top line first
                {0, 0},
                {1, 0},
                {1, 1},
                {2, 1},
                // top line last
                {4, 4},
                {3, 4},
                {2, 4},
                {2, 3},
                {1, 4},
                {0, 4},
                // additional sites
                {4, 2},
                {4, 1},
        };
        for (int[] site : openSites) {
            p.open(site[0], site[1]);
        }
        Cell[][] expectedState = {
                {Cell.FULL, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED, Cell.FULL},
                {Cell.FULL, Cell.FULL, Cell.CLOSED, Cell.CLOSED, Cell.FULL},
                {Cell.CLOSED, Cell.FULL, Cell.CLOSED, Cell.FULL, Cell.FULL},
                {Cell.CLOSED, Cell.CLOSED, Cell.CLOSED, Cell.CLOSED, Cell.FULL},
                {Cell.CLOSED, Cell.OPEN, Cell.OPEN, Cell.CLOSED, Cell.FULL}
        };
        assertThat(getState(N, p)).isEqualTo(expectedState);
        assertThat(p.percolates()).isTrue();
        assertThat(p.numberOfOpenSites()).isEqualTo(3);
    }

    /**
     * Cover special cases:
     *      open and full sites connect by a newly opened element,
     */
    @Test
    public void percolate2Test() {
        int N = 5;
        Percolation p = new Percolation(N);
        int[][] openSites = {
                {0, 2},
                {1, 2},
                {1, 3},

                {4, 2},
                {3, 2},

                {2, 2},
        };
        for (int[] site : openSites) {
            p.open(site[0], site[1]);
        }
        Cell[][] expectedState = {
                {Cell.CLOSED, Cell.CLOSED, Cell.FULL, Cell.CLOSED, Cell.CLOSED},
                {Cell.CLOSED, Cell.CLOSED, Cell.FULL, Cell.FULL, Cell.CLOSED},
                {Cell.CLOSED, Cell.CLOSED, Cell.FULL, Cell.CLOSED, Cell.CLOSED},
                {Cell.CLOSED, Cell.CLOSED, Cell.FULL, Cell.CLOSED, Cell.CLOSED},
                {Cell.CLOSED, Cell.CLOSED, Cell.FULL, Cell.CLOSED, Cell.CLOSED}
        };
        assertThat(getState(N, p)).isEqualTo(expectedState);
        assertThat(p.percolates()).isTrue();
        assertThat(p.numberOfOpenSites()).isEqualTo(1);
    }
}
