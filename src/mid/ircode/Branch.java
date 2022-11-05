package mid.ircode;

public class Branch extends InstructionLinkNode {
    private final Operand cond;
    private final String labelTrue;
    private final String labelFalse;
    private final BrOp brOp;

    /**
     * BEQ: Jump when cond == 0, means jump when cond false, target: labelFalse
     * BNE: Jump when cond != 0, means jump when cond true, target: labelTrue
     */
    public enum BrOp {
        BEQ, BNE
    }

    public Branch(Operand cond, String labelTrue, String labelFalse, BrOp brOp) {
        this.cond = cond;
        this.labelFalse = labelFalse;
        this.labelTrue = labelTrue;
        this.brOp = brOp;
    }

    public Operand getCond() {
        return cond;
    }

    public String getLabelFalse() {
        return labelFalse;
    }

    public String getLabelTrue() {
        return labelTrue;
    }

    public BrOp getBrOp() {
        return brOp;
    }

    public String toIr() {
        return "\tbr " + cond.toNameIr() + " label %" + labelTrue + " label %" + labelFalse;
    }
}
