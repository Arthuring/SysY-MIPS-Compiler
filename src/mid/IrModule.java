package mid;

import front.FuncEntry;
import front.TableEntry;
import mid.ircode.FuncDef;
import mid.ircode.VarDef;

import java.util.ArrayList;
import java.util.List;

public class IrModule {
    private static final List<String> TARGET_INFORMATION = new ArrayList<>();
    public static final List<TableEntry> GLOBAL_VAR_DEFS = new ArrayList<>();
    private static final List<FuncDef> FUNC_DEFS = new ArrayList<>();

}
