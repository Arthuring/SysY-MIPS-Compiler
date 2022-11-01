package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class Addiu extends MipsInstr {
    private final int rd;
    private final int rs;
    private final int imm;

    public Addiu(int rd, int rs, int imm) {
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    public Addiu(String label, int rd, int rs, int imm) {
        super.setLabel(label);
        this.rd = rd;
        this.rs = rs;
        this.imm = imm;
    }

    public int getImm() {
        return imm;
    }

    public int getRd() {
        return rd;
    }

    public int getRs() {
        return rs;
    }

    @Override
    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "addiu " + "$" + RF.ID_TO_NAME.get(rd) + ", " +
                "$" + RF.ID_TO_NAME.get(rs) + ", " + imm;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
