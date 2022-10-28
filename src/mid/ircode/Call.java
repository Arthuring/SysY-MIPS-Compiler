package mid.ircode;

import front.FuncEntry;
import front.TableEntry;

import java.util.List;

public class Call extends InstructionLinkNode {
    private final FuncEntry funcEntry;
    private final List<Operand> args;
    private final TableEntry returnDst;

    public Call(FuncEntry funcEntry, List<Operand> args, TableEntry returnDst) {
        this.funcEntry = funcEntry;
        this.args = args;
        this.returnDst = returnDst;
    }

    public Call(FuncEntry funcEntry, List<Operand> args) {
        this.funcEntry = funcEntry;
        this.args = args;
        this.returnDst = null;
    }

    public FuncEntry getFuncEntry() {
        return funcEntry;
    }

    public List<Operand> getArgs() {
        return args;
    }

    public TableEntry getReturnDst() {
        return returnDst;
    }
}
