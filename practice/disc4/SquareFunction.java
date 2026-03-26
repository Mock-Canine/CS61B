package disc4;

public class SquareFunction implements UnaryFunction {

    SquareFunction() {}
    @Override
    public Integer apply(Integer x) {
        return Math.powExact(x, 2);
    }
}
