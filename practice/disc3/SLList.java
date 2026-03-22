package disc3;

public class SLList {
    private class IntNode {
        public int item;
        public IntNode next;
        public IntNode(int item, IntNode next) {
            this.item = item;
            this.next = next;
        }
    }

    private IntNode sentinel;
    public int size;

    public SLList() {
        this.sentinel = new IntNode(0, null);
        sentinel.next = sentinel;
    }

    public void addFirst(int x) {
        sentinel.next = new IntNode(x, sentinel.next);
        size++;
    }

    public void insert(int x, int position) {
        // Handle size == 0 and position out of range cases
        position = Math.max(0, Math.min(position, size));
        IntNode p = sentinel;
//        while (position > 0 && p.next != null)
        while (position != 0) {
            p = p.next;
            position--;
        }
        p.next = new IntNode(x, p.next);
        size++;
    }

    public int[][] gridify(int rows, int cols) {
        int[][] grid = new int[rows][cols];
        gridifyHelper(grid, sentinel.next, 0);
        return grid;
    }

    private void gridifyHelper(int[][] grid, IntNode curr, int numFilled) {
        if (numFilled == grid.length * grid[0].length ||
            curr == sentinel) {
            return;
        }
        int row = numFilled / grid[0].length;
        int col = numFilled % grid[0].length;
        grid[row][col] = curr.item;
        gridifyHelper(grid, curr.next, numFilled + 1);
    }

    static void main() {
        SLList L = new SLList();
        L.addFirst(5);
        L.addFirst(6);
        L.addFirst(7);
        L.addFirst(8);
        L.addFirst(9);
        int[][] arr = L.gridify(2, 3);
    }
}
