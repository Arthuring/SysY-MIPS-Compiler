package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Slti extends MipsInstr {
    private final int rd;
    private final int rs;
    private final int imm;

    public Slti(int rd, int rs, int imm) {
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    public int getRs() {
        return rs;
    }

    public int getRt() {
        return imm;
    }

    public int getRd() {
        return rd;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "slti " + "$" + RF.ID_TO_NAME.get(rd) + ", " +
                "$" + RF.ID_TO_NAME.get(rs) + ", " + imm;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
