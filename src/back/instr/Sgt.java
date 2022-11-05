package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Sgt extends MipsInstr {
    private final int rd;
    private final int rs;
    private final int rt;

    public Sgt(int rd, int rs, int rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    public int getRs() {
        return rs;
    }

    public int getRt() {
        return rt;
    }

    public int getRd() {
        return rd;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "sgt " + "$" + RF.ID_TO_NAME.get(rd) + ", " +
                "$" + RF.ID_TO_NAME.get(rs) + ", " + "$" + RF.ID_TO_NAME.get(rt);
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
