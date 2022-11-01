package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Move extends MipsInstr {
    private final int dst;
    private final int src;

    public Move(int dst, int src) {
        this.dst = dst;
        this.src = src;
    }

    public int getDst() {
        return dst;
    }

    public int getSrc() {
        return src;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "move " + "$" + RF.ID_TO_NAME.get(dst) + ", " +
                "$" + RF.ID_TO_NAME.get(src);
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
