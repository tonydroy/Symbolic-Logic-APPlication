package slapp.editor.derivation;

public class TheoremSet {

    String name;
    boolean cumulative;

    TheoremSet(String name) {
        this.name = name;
    }

    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }

    @Override
    public String toString() {
        return name;
    }

}
