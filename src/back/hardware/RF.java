package back.hardware;

import java.util.*;

public class RF {
    private static final int TEXT_BASE = 0x00400000;
    public static final int GP_INIT = 0x10008000;
    public static final int SP_INIT = 0x7fffeffc;
    public static final Map<Integer, String> ID_TO_NAME = new HashMap<Integer, String>() {
        {
            put(0, "zero");
            put(1, "at");
            put(2, "v0");
            put(3, "v1");
            put(4, "a0");
            put(5, "a1");
            put(6, "a2");
            put(7, "a3");
            put(8, "t0");
            put(9, "t1");
            put(10, "t2");
            put(11, "t3");
            put(12, "t4");
            put(13, "t5");
            put(14, "t6");
            put(15, "t7");
            put(16, "s0");
            put(17, "s1");
            put(18, "s2");
            put(19, "s3");
            put(20, "s4");
            put(21, "s5");
            put(22, "s6");
            put(23, "s7");
            put(24, "t8");
            put(25, "t9");
            put(26, "k0");
            put(27, "k1");
            put(28, "gp");
            put(29, "sp");
            put(30, "fp");
            put(31, "ra");
        }
    };

    private final List<Reg> grf = Collections.unmodifiableList(
            Arrays.asList(
                    new Reg("zero"), new Reg("at"), new Reg("v0"), new Reg("v1"),
                    new Reg("a0"), new Reg("a1"), new Reg("a2"), new Reg("a3"),
                    new Reg("t0"), new Reg("t1"), new Reg("t2"), new Reg("t3"),
                    new Reg("t4"), new Reg("t5"), new Reg("t6"), new Reg("t7"),
                    new Reg("s0"), new Reg("s1"), new Reg("s2"), new Reg("s3"),
                    new Reg("s4"), new Reg("s5"), new Reg("s6"), new Reg("s7"),
                    new Reg("t8"), new Reg("t9"), new Reg("k0"), new Reg("k1"),
                    new Reg("gp", GP_INIT), new Reg("sp", SP_INIT), new Reg("fp"), new Reg("ra")
            )
    );

    private final Reg HI = new Reg("hi");
    private final Reg LO = new Reg("lo");
    private final Reg PC = new Reg("pc", TEXT_BASE);

    public static class GPR {
        public static final int ZERO = 0;
        public static final int AT = 1;
        public static final int V0 = 2;
        public static final int V1 = 3;
        public static final int A0 = 4;
        public static final int A1 = 5;
        public static final int A2 = 6;
        public static final int A3 = 7;
        public static final int T0 = 8;
        public static final int T1 = 9;
        public static final int T2 = 10;
        public static final int T3 = 11;
        public static final int T4 = 12;
        public static final int T5 = 13;
        public static final int T6 = 14;
        public static final int T7 = 15;
        public static final int S0 = 16;
        public static final int S1 = 17;
        public static final int S2 = 18;
        public static final int S3 = 19;
        public static final int S4 = 20;
        public static final int S5 = 21;
        public static final int S6 = 22;
        public static final int S7 = 23;
        public static final int T8 = 24;
        public static final int T9 = 25;
        public static final int K0 = 26;
        public static final int K1 = 27;
        public static final int GP = 28;
        public static final int SP = 29;
        public static final int FP = 30;
        public static final int RA = 31;
    }

    public int read(int id) {
        return grf.get(id).read();
    }

    public void write(int id, int value) {
        grf.get(id).write(value);
    }

    public int readHi() {
        return HI.read();
    }

    public int readLo() {
        return LO.read();
    }
}
