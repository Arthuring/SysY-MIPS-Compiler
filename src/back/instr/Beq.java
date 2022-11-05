package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Beq extends MipsInstr {
    private final int rs;
    private final int rt;
    private final String target;

    public Beq(int rs, int rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.target = label;
    }

    public String getTarget() {
        return target;
    }

    public int getRs() {
        return rs;
    }

    public int getRt() {
        return rt;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "beq " + "$" + RF.ID_TO_NAME.get(rs) + ", " +
                "$" + RF.ID_TO_NAME.get(rt) + ", " + target;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
