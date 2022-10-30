package back.instr;

public class Addu {
    private final int rd;
    private final int rt;
    private final int rs;

    public Addu(int rd, int rs, int rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    public int getRs() {
        return rs;
    }

    public int getRd() {
        return rd;
    }

    public int getRt() {
        return rt;
    }
}
