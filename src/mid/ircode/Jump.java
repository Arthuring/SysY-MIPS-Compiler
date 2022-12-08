package mid.ircode;

public class Jump extends InstructionLinkNode {
    private String target;

    public Jump(String target) {
        super();
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public String toIr() {
        return "\tbr label %" + target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
