package draftFunnyTests;

import com.github.javaparser.quality.NotNull;
import javassist.NotFoundException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class makeFun {
    private static int dog = 0;
    public static void testException(@NotNull Integer jack) throws NotFoundException {
        if (jack == 7) {
            throw new NotFoundException("jack is not null");
        }
    }

    static void main() throws NotFoundException {
        makeFun.dog = 9;
        Integer jack = null;
        testException(null);
        List<Integer> L = List.of(4, 5, 8, 1);
    }
}
