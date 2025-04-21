package slapp.editor.derivation;

public class TheoremSet {

    String name;
    boolean sequential;

    TheoremSet(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
