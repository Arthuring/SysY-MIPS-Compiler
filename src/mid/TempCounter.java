package mid;

import front.TableEntry;
import front.nodes.ExprNode;
import front.nodes.NumberNode;
import mid.ircode.Operand;

import java.util.ArrayList;
import java.util.List;

public class TempCounter {
    private static int counter = -1;

    public static TableEntry getTemp(TableEntry.RefType refType, TableEntry.ValueType valueType) {
        counter += 1;
        return new TableEntry(refType, valueType, "%-t" + counter, null, false, 0, false, true);
    }

    public static TableEntry getTemp() {
        counter += 1;
        TableEntry tableEntry = new TableEntry(TableEntry.RefType.ITEM, TableEntry.ValueType.INT,
                "-t" + counter, 0, false, 0, false, true);
        tableEntry.setDefined(true);
        return tableEntry;
    }

    public static TableEntry getTempPointer(TableEntry base, List<Operand> indexs) {
        counter += 1;
        List<ExprNode> dim = new ArrayList<>();
        dim.add(new NumberNode(0));
        dim.addAll(base.dimension);
        for (int i = 1; i < indexs.size(); i++) {
            dim.remove(1);
        }

        TableEntry tableEntry = new TableEntry(TableEntry.RefType.POINTER, TableEntry.ValueType.INT,
                "-t" + counter, dim);
        tableEntry.setDefined(true);
        return tableEntry;
    }
}
