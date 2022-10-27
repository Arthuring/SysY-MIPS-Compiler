package mid;

import java.util.HashMap;
import java.util.Map;

public class StringCounter {
    private static Map<String, String> str2label = new HashMap<>();
    private static int labelCounter = 0;

    public String findString(String str) {
        if (!str2label.containsKey(str)) {
            str2label.put(str, "str" + labelCounter);
            labelCounter += 1;
            return str2label.get(str);
        } else {
            return str2label.get(str);
        }
    }

}
