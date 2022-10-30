package back.instr;

public class Move extends MipsInstr {
    private final int dst;
    private final int src;

    public Move(int dst, int src) {
        this.dst = dst;
        this.src = src;
    }

    public int getDst() {
        return dst;
    }

    public int getSrc() {
        return src;
    }
}
