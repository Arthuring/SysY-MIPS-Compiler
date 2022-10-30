package back.hardware;

public class Reg {
    private int value = 0;
    private final String name;

    public Reg(String name) {
        this.name = name;
    }

    public Reg(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int read() {
        return value;
    }

    public void write(int value) {
        if (!this.name.equals("zero")) {
            this.value = value;
        }
    }
}
