package Sort;

public class Sort {
    public static int findSmallest(String[] input, int start) {
        int smallIdx = start;
        for (int i = start; i < input.length; i++) {
            if (input[i].compareTo(input[smallIdx]) < 0) {
                smallIdx = i;
            }
        }
        return smallIdx;
    }

    public static void swap(String[] input, int i, int i1) {
        String tmp = input[i];
        input[i] = input[i1];
        input[i1] = tmp;
    }

    public static void sort(String[] input) {
        sort(input, 0);
    }

    private static void sort(String[] input, int start) {
        if (start == input.length)
            return;
        int smallIdx = findSmallest(input, start);
        swap(input, smallIdx, start);
        sort(input, start + 1);
    }
}
