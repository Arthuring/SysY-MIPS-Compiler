package mid.ircode;

public class PrintStr extends InstructionLinkNode {
    private final String  label;
    private final String content;

    public PrintStr(String label, String content) {
        this.label = label;
        this.content = content;
    }

    public String getLabel() {
        return label;
    }

    public String getContent() {
        return content;
    }
}
