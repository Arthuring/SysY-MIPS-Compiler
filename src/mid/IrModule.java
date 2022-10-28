package mid;

import front.FuncEntry;
import front.TableEntry;
import mid.ircode.FuncDef;
import mid.ircode.VarDef;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class IrModule {
    private static final List<String> TARGET_INFORMATION = new ArrayList<String>() {
        {
            add("declare i32 @getint()");
            add("declare void @putint(i32)");
            add("declare void @putch(i32)");
            add("declare void @putstr(i8*)");
        }
    };
    private static final List<TableEntry> GLOBAL_VAR_DEFS = new ArrayList<>();
    private static final List<FuncDef> FUNC_DEFS = new ArrayList<>();

    public static List<FuncDef> getFuncDefs() {
        return FUNC_DEFS;
    }

    public static List<String> getTargetInformation() {
        return TARGET_INFORMATION;
    }

    public static List<TableEntry> getGlobalVarDefs() {
        return GLOBAL_VAR_DEFS;
    }

    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        for (String str : TARGET_INFORMATION) {
            sj.add(str);
        }
        for (TableEntry tableEntry : GLOBAL_VAR_DEFS) {
            sj.add(tableEntry.toGlobalIr());
        }
        for (FuncDef funcDef : FUNC_DEFS) {
            sj.add(funcDef.toIr());
        }
        return sj.toString();
    }
}
