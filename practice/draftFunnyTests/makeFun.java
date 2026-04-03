package draftFunnyTests;

import com.github.javaparser.quality.NotNull;
import javassist.NotFoundException;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;

public class makeFun {
    private static int dog = 0;
    public static void testException(@NotNull Integer jack) throws NotFoundException {
        if (jack == null) {
            throw new NotFoundException("jack is not null");
        }
    }

    static void main() throws NotFoundException {
        makeFun.dog = 9;
        Integer jack = null;
//        testException(null);
        List<Integer> L = new ArrayList<>();
        L.add(3);
        IO.println(L.hashCode());
        L.add(5);
        IO.println(L.hashCode());
        Map<String, Integer> map = new HashMap<>();
        Set<String> ss = new HashSet<>();
    }
}
