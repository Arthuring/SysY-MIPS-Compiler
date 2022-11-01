package back.instr;

import back.hardware.RF;

import java.util.StringJoiner;

public class La extends MipsInstr {
    private final int dst;
    private final String addressLabel;

    public La(int dst, String label) {
        this.dst = dst;
        this.addressLabel = label;
    }

    public String getAddressLabel() {
        return addressLabel;
    }

    public int getDst() {
        return dst;
    }

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(super.toMips());
        String sb = "la " + "$" + RF.ID_TO_NAME.get(dst) + ", " + addressLabel;
        StringBuilder stringBuilder = new StringBuilder(sb);
        if (!super.getComment().equals("")) {
            stringBuilder.append("\t# ").append(super.getComment());
        }
        sj.add(stringBuilder.toString());
        return sj.toString();
    }
}
