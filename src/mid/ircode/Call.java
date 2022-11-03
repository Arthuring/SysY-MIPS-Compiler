package mid.ircode;

import front.FuncEntry;
import front.TableEntry;

import java.util.List;
import java.util.StringJoiner;

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

    @Override
    public String toIr() {
        StringBuilder sb = new StringBuilder();
        if (returnDst != null) {
            sb.append("\t").append(returnDst.toNameIr()).append(" = call ");
        } else {
            sb.append("\t" + "call ");
        }

        sb.append(TableEntry.TO_IR.get(funcEntry.returnType()));
        sb.append(" ");
        sb.append("@").append(funcEntry.name()).append("(");
        StringJoiner sj = new StringJoiner(",");
        for (Operand operand : args) {
            sj.add(operand.toParamIr());
        }
        sb.append(sj);
        sb.append(")");
        return sb.toString();
    }
}
