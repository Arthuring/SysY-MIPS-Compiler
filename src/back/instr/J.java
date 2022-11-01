package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class J extends MipsInstr {
    private final String target;

    public J(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "j " + target;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
