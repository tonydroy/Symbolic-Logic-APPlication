package slapp.editor.derivation.theorems;

import slapp.editor.AlphanumStringComparator;

import java.io.Serializable;

public class ThrmSetElement  implements Serializable {

    TheoremType type;
    String name;
    String[] forms;


    ThrmSetElement(TheoremType type, String name, String... forms ) {
        this.type = type;
        this.name = name;
        this.forms = forms;
    }

    public boolean isPriorTo(String name) {
        boolean result = false;
        AlphanumStringComparator cmp = new AlphanumStringComparator();
        int compare = cmp.compare(this.name, name);
        if (compare < 0) result = true;
        return result;
    }

    public String getName() {
        return name;
    }
}
