package disc2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapExercise {
    static Map<Integer, List<Integer>> buildLessThanMap(List<Integer> L) {
        Map<Integer, List<Integer>> result = new HashMap<>();
        for (int x : L) {
            if (!result.containsKey(x)) {
                result.put(x, new ArrayList<>());
            }

            for (int y : L) {
                if (y < x && !result.get(x).contains(y)) {
                   result.get(x).add(y);
                }
            }
        }
        return result;
    }
}
