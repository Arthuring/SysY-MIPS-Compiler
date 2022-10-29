package mid;

import front.TableEntry;

public class TempCounter {
    private static int counter = -1;

    public static TableEntry getTemp(TableEntry.RefType refType, TableEntry.ValueType valueType) {
        counter += 1;
        return new TableEntry(refType, valueType, "%t" + counter, null, false, 0,false);
    }

    public static TableEntry getTemp() {
        counter += 1;
        return new TableEntry(TableEntry.RefType.ITEM, TableEntry.ValueType.INT,
                "t" + counter, 0, false, 0, false);
    }
}
