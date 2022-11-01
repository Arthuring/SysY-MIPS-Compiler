package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Mult extends MipsInstr {
    private final int rs;
    private final int rt;

    public Mult(int rs, int rt) {
        this.rs = rs;
        this.rt = rt;
    }

    public int getRt() {
        return rt;
    }

    public int getRs() {
        return rs;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "mult " + "$" + RF.ID_TO_NAME.get(rs) + ", " +
                "$" + RF.ID_TO_NAME.get(rt);
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
