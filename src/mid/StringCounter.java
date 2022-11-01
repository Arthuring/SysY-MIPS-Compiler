package mid;

import java.util.HashMap;
import java.util.Map;

public class StringCounter {
    private static final Map<String, String> STR_2_LABEL = new HashMap<>();
    private static int labelCounter = 0;

    public static String findString(String str) {
        if (!STR_2_LABEL.containsKey(str)) {
            STR_2_LABEL.put(str, "str" + labelCounter);
            labelCounter += 1;
            return STR_2_LABEL.get(str);
        } else {
            return STR_2_LABEL.get(str);
        }
    }

    public static int getLabelCounter() {
        return labelCounter;
    }

    public static Map<String, String> getStr2Label() {
        return STR_2_LABEL;
    }
}
