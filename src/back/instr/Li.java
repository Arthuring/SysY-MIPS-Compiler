package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Li extends MipsInstr {
    private final int rd;
    private final int imm;

    public Li(int rd, int imm) {
        this.rd = rd;
        this.imm = imm;
    }

    public int getRd() {
        return rd;
    }

    public int getImm() {
        return imm;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "li " + "$" + RF.ID_TO_NAME.get(rd) + ", " + imm;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
