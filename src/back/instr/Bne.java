package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Bne extends MipsInstr {
    private final int rs;
    private final int rt;
    private final String target;

    public Bne(int rs, int rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.target = label;
    }

    public int getRt() {
        return rt;
    }

    public int getRs() {
        return rs;
    }

    public String getTarget() {
        return target;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "bne " + "$" + RF.ID_TO_NAME.get(rs) + ", " +
                "$" + RF.ID_TO_NAME.get(rt) + ", " + target;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
