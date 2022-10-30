package mid;

import front.TableEntry;
import mid.ircode.FuncDef;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class IrModule {
    private final List<String> targetInformation = new ArrayList<String>() {
        {
            add("declare i32 @getint()");
            add("declare void @putint(i32)");
            add("declare void @putch(i32)");
            add("declare void @putstr(i8*)");
        }
    };
    private final List<TableEntry> globalVarDefs = new ArrayList<>();
    private final List<FuncDef> funcDefs = new ArrayList<>();

    public IrModule(){

    }
    public List<FuncDef> getFuncDefs() {
        return funcDefs;
    }

    public List<String> getTargetInformation() {
        return targetInformation;
    }

    public List<TableEntry> getGlobalVarDefs() {
        return globalVarDefs;
    }

    public String toIr() {
        StringJoiner sj = new StringJoiner("\n");
        for (String str : targetInformation) {
            sj.add(str);
        }
        for (TableEntry tableEntry : globalVarDefs) {
            sj.add(tableEntry.toGlobalIr());
        }
        for (FuncDef funcDef : funcDefs) {
            sj.add(funcDef.toIr());
        }
        return sj.toString();
    }
}
