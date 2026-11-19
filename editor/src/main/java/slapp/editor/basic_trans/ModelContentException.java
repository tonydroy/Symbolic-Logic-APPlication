package slapp.editor.basic_trans;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.parser.symbols.LanguageElement;

import java.util.List;

public class ModelContentException extends RuntimeException {
    boolean profTrue;
    List<Text> universe;
    List<Pair<LanguageElement, List<Text>>> assignmentList;

    public ModelContentException(boolean profTrue, List<Text> universe, List<Pair<LanguageElement, List<Text>>> assignmentList) {
        super();
        this.profTrue = profTrue;
        this.universe = universe;
        this.assignmentList = assignmentList;
    }

    public List<Text> getUniverse() {
        return universe;
    }

    public List<Pair<LanguageElement, List<Text>>> getAssignmentList() {
        return assignmentList;
    }

    public boolean isProfTrue() {
        return profTrue;
    }
}
