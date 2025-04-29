package slapp.editor.derivation.theorems;

import java.util.ArrayList;
import java.util.List;

public class TheoremSet {

    private String setName;
    private List<ThrmSetElement> elements = new ArrayList();


    TheoremSet(String name) {
        this.setName = name;
    }

    public List<ThrmSetElement> getElements() {
        return elements;
    }

    public String getSetName() {
        return setName;
    }

    @Override
    public String toString() {
        return setName;
    }

}
