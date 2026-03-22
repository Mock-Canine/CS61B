package disc2;

import java.util.ArrayList;
import java.util.List;

public class PositiveFilter {
    static int[] filterPositive(List<Integer> L) {
        List<Integer> pos = new ArrayList<>();
        for (int x : L) {
           if (x > 0) {
               pos.add(x);
           }
        }

        int[] result = new int[pos.size()];
        for (int i = 0; i < pos.size(); i++) {
            result[i] = pos.get(i);
        }
        return result;
    }
}
