package back.instr;

public class Sw extends MipsInstr {
    private final int rt;
    private final int base;
    private final int offset;

    public Sw(int rt, int offset, int base) {
        this.rt = rt;
        this.offset = offset;
        this.base = base;
    }

    public Sw(int rt, int offset, int base, String label) {
        super.setLabel(label);
        this.rt = rt;
        this.offset = offset;
        this.base = base;
    }

    public int getRt() {
        return rt;
    }

    public int getBase() {
        return base;
    }

    public int getOffset() {
        return offset;
    }
}
