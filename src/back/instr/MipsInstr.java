package back.instr;

import mid.ircode.InstructionLinkNode;

public class MipsInstr extends InstructionLinkNode {
    private  String label = "";

    public String toMips() {
        return null;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
