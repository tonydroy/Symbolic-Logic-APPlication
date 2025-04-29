package slapp.editor.derivation.theorems;

public class ThrmSetElement {

    TheoremType type;
    String name;
    String[] forms;


    ThrmSetElement(TheoremType type, String name, String... forms ) {
        this.type = type;
        this.name = name;
        this.forms = forms;
    }


}
