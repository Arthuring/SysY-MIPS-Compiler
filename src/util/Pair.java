package util;

public class Pair<T, U> {
    public static <S, V> Pair<S, V> of(S f, V s) {
        return new Pair<>(f, s);
    }

    public final T o1;
    public final U o2;

    public Pair(T o1, U o2) {
        this.o1 = o1;
        this.o2 = o2;
    }
}
