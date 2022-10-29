package mid.ircode;

import java.util.StringJoiner;

public class PrintStr extends InstructionLinkNode {
    private final String label;
    private final String content;
    private final boolean singleChar = true;

    public PrintStr(String label, String content) {
        this.label = label;
        this.content = content.replace("\\" + "n", "\n");
    }

    public String getLabel() {
        return label;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toIr() {
        if (singleChar) {
            StringJoiner sj = new StringJoiner("\n");
            for (int i = 0; i < content.length(); i++) {
                sj.add("\t" + "call void @putch(i32 " + (int) content.charAt(i) + " )    " +
                        "; '" + (content.charAt(i) == '\n' ? "\\n" : content.charAt(i)) + "'");
            }
            return sj.toString();
        } else {
            //TODO
            return null;
        }
    }
}
