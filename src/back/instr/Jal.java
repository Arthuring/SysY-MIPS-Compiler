package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Jal extends MipsInstr {
    private final String tar;

    public Jal(String tar) {
        this.tar = tar;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "jal " + tar;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
