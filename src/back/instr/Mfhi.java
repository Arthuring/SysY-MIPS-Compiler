package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Mfhi extends MipsInstr {
    private final int rd;

    public Mfhi(int rd) {
        this.rd = rd;
    }

    public int getRd() {
        return rd;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "mfhi " + "$" + RF.ID_TO_NAME.get(rd);
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
