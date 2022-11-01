package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

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

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "sw " + "$" + RF.ID_TO_NAME.get(rt) + ", " +
                offset + "(" + "$" + RF.ID_TO_NAME.get(base) + ")";
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
