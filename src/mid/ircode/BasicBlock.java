package mid.ircode;

import java.util.StringJoiner;

public class BasicBlock extends InstructionLinkNode {
    private final String beginLabel;
    private final InstructionLinkNode end = new InstructionLinkNode();
    private String endLabel;

    public BasicBlock(String label) {
        this.beginLabel = label;
        this.setNext(this.end);
        this.end.setPrev(this);
        this.endLabel = null;
    }

    public String getLabel() {
        return beginLabel;
    }

    public InstructionLinkNode getFirstInstruction() {
        return this.next();
    }

    public InstructionLinkNode getLastInstruction() {
        return end.prev();
    }

    public void addAfter(InstructionLinkNode instruct) {
        InstructionLinkNode last = getLastInstruction();
        last.setNext(instruct);
        instruct.setPrev(last);
        InstructionLinkNode ptr = instruct;
        while (ptr.hasNext()) {
            ptr = ptr.next();
        }
        end.setPrev(ptr);
        ptr.setNext(end);
    }

    public String toIr() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add(beginLabel + ":");
        InstructionLinkNode ptr = this.next();
        while (ptr != this.end) {
            sj.add(ptr.toIr());
            ptr = ptr.next();
        }
        if (endLabel != null) {
            sj.add(endLabel + ":");
        }
        return sj.toString();
    }

    public InstructionLinkNode getEnd() {
        return end;
    }

    public String getEndLabel() {
        return endLabel;
    }

    public String getBeginLabel() {
        return beginLabel;
    }

    public void setEndLabel(String endLabel) {
        this.endLabel = endLabel;
    }
}
