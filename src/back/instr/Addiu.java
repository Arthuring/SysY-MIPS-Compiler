package back.instr;

public class Addiu extends MipsInstr {
    private final int rd;
    private final int rs;
    private final int imm;

    public Addiu(int rd, int rs, int imm) {
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    public Addiu(String label, int rd, int rs, int imm) {
        super.setLabel(label);
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    public int getImm() {
        return imm;
    }

    public int getRd() {
        return rd;
    }

    public int getRs() {
        return rs;
    }

}
