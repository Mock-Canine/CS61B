package disc4;

import java.util.ArrayList;
import java.util.List;

public class Spy implements UnaryFunction {
    private UnaryFunction sq;
    private List<String> L;

    Spy(UnaryFunction sq) {
        L = new ArrayList<>();
        this.sq = sq;
    }

    @Override
    public Integer apply(Integer x) {
        L.add(x.toString());
        return sq.apply(x);
    }

    public void printArgumentHistory() {
        IO.println(String.join(" ", L));
    }

    static void main() {
        UnaryFunction sq = new SquareFunction();
        Spy spy = new Spy(sq);
        spy.apply(2);
        spy.apply(3);
        spy.printArgumentHistory();
    }
}
