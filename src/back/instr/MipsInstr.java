package back.instr;

import mid.ircode.InstructionLinkNode;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class MipsInstr extends InstructionLinkNode {
    private String label = "";
    private final List<String> irDescriptions = new ArrayList<>();
    private String comment = "";

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        if (!label.equals("")) {
            sj.add(label + ":");
        }
        for (String description : irDescriptions) {
            String[] strings = description.split("\n");
            for (String str : strings) {
                sj.add("# " + str);
            }
        }
        return sj.toString();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setIrDescription(List<String> irDescriptions) {
        this.irDescriptions.addAll(irDescriptions);
    }

    public List<String> getIrDescriptions() {
        return irDescriptions;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
}
