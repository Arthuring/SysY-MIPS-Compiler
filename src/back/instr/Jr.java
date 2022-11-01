package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Jr extends MipsInstr {
    private final int rd;

    public Jr(int rd) {
        this.rd = rd;
    }

    public int getRd() {
        return rd;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "jr " + "$" + RF.ID_TO_NAME.get(rd);
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
