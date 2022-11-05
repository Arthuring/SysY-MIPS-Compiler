package mid;

public class LabelCounter {
    private static int labelCounter = -1;

    public static String getLabel() {
        labelCounter += 1;
        return "label_" + labelCounter;
    }

    public static String getLabel(String tag) {
        labelCounter += 1;
        return tag + "_label_" + labelCounter;
    }
}
