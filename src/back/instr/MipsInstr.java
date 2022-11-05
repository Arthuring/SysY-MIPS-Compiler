package back.instr;

import mid.ircode.InstructionLinkNode;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class MipsInstr extends InstructionLinkNode {
    private List<String> label = new ArrayList<>();
    private final List<String> irDescriptions = new ArrayList<>();
    private String comment = "";

    public String toMips() {
        StringJoiner sj = new StringJoiner("\n");
        if (!label.isEmpty()) {
            for (String labelItem : label) {
                sj.add(labelItem + ":");
            }
        }
        for (String description : irDescriptions) {
            String[] strings = description.split("\n");
            for (String str : strings) {
                sj.add("# " + str);
            }
        }
        return sj.toString();
    }

    public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label.addAll(label);
    }

    public void setLabel(String label) {
        this.label.add(label);
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
