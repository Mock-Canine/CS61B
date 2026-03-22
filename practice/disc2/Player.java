package disc2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
    public double score;
    public Player(double s) {
        score = s;
    }
    public static Map<Player, Integer> rankedAbove(List<Player> players) {
        Map<Player, Integer> results = new HashMap<>();
        for (Player p : players) {
            int rank = 1;
//            for (Player pin : results.keySet()) {
//                if (p.score < pin.score) {
//                    rank++;
//                } else {
//                    results.put(pin, results.get(pin) + 1);
//                }
//            }
            for (Player pn : players) {
                if (pn.score > p.score) {
                    rank++;
                }
            }
            results.put(p, rank);
        }
        return results;
    }

    static void main() {
        List<Player> pL = List.of(new Player(500), new Player(1200), new Player(800), new Player(100));
        Map<Player, Integer> res = Player.rankedAbove(pL);
    }
}
