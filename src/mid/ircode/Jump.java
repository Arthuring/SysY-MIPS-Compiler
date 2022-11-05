package mid.ircode;

public class Jump extends InstructionLinkNode {
    private final String target;

    public Jump(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public String toIr() {
        return "\tbr label %" + target;
    }
}
