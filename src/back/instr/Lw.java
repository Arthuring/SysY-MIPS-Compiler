package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Lw extends MipsInstr {
    private final int rt;
    private final int base;
    private final int offset;

    public Lw(int rt, int offset, int base) {
        this.rt = rt;
        this.offset = offset;
        this.base = base;
    }

    public Lw(int rt, int offset, int base, String label) {
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

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "lw " + "$" + RF.ID_TO_NAME.get(rt) + ", " +
                offset + "(" + "$" + RF.ID_TO_NAME.get(base) + ")";
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
