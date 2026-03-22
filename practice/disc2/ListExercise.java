package disc2;

import java.util.ArrayList;
import java.util.List;

class ListExercise {
    static List<Integer> common(List<Integer> L1, List<Integer> L2) {
        List<Integer> rList = new ArrayList<>();
        // int is more efficient than Integer
        for (int x : L1) {
            if (L2.contains(x) && !rList.contains(x)) {
                rList.add(x);
            }
        }
        return rList;
    }

    static void capitalize(List<String> L) {
        L.replaceAll(String::toUpperCase);
    }
}
