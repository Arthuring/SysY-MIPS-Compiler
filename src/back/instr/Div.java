package back.instr;

import back.hardware.RF;
import mid.ircode.InstructionLinkNode;

import java.util.StringJoiner;

public class Div extends MipsInstr {
    private final int rs;
    private final int rt;

    public Div(int rs, int rt) {
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
        String sb = "div " + "$" + RF.ID_TO_NAME.get(rs) + ", " +
                "$" + RF.ID_TO_NAME.get(rt);
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
