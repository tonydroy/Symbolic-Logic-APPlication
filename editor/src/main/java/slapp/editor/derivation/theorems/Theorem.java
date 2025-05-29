package slapp.editor.derivation.theorems;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.derivation.DCheck;
import slapp.editor.derivation.DCheck;
import slapp.editor.derivation.ViewLine;

import java.util.List;
import java.util.regex.Matcher;

public abstract class Theorem {
    private String name;
    private Matcher matcher;
    private String[] forms;


    public Theorem(String name, String... forms) {
        this.name = name;
        this.forms = forms;
    }


    public boolean matches(String input) {
        matcher.reset(input);
        return matcher.matches();
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public String[] getForms() {
        return forms;
    }

    public String getName() {
        return name;
    }

    public abstract Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs);

}
