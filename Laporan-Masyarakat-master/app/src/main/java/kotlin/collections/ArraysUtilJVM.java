package kotlin.collections;

import java.util.Arrays;
import java.util.List;

/* loaded from: classes.dex */
public class ArraysUtilJVM {
    ArraysUtilJVM() {
    }

    public static <T> List<T> asList(T[] array) {
        return Arrays.asList(array);
    }
}
