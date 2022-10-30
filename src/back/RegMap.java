package back;

import front.TableEntry;

import java.util.*;

public class RegMap {
    private static final Map<Integer, TableEntry> BUSY_REG_TO_VAR = new HashMap<>();
    private static final Map<TableEntry, Integer> VAR_TO_BUST_REG = new HashMap<>();

    private static final Collection<Integer> availableReg = Collections.unmodifiableList(Arrays.asList(
            5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25
    ));
    private static final Set<Integer> freeRegList = new HashSet<>(availableReg);



}
