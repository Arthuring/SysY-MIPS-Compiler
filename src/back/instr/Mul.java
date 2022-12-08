package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Mul extends MipsInstr {
    private final int rd;
    private final int rt;
    private final int rs;

    public Mul(int rd, int rs, int rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    public int getRs() {
        return rs;
    }

    public int getRd() {
        return rd;
    }

    public int getRt() {
        return rt;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "mul " + "$" + RF.ID_TO_NAME.get(rd) + ", " +
                "$" + RF.ID_TO_NAME.get(rs) + ", " + "$" + RF.ID_TO_NAME.get(rt);
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
