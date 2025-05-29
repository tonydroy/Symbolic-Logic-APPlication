package slapp.editor.derivation;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.derivation.der_systems.DerivationRuleset;
import slapp.editor.main_window.Exercise;

import java.util.List;

public interface DCheck {

    String getContradictionSymbolString();

    void updateCounters();

    void setHelpCounter();

    boolean backgroundProgressCheck();

    Pair<ViewLine, List<Text>> getLineFromLabel(String label);

    Pair<Boolean, List<Text>> lineIsAccessibleTo(ViewLine justifier, ViewLine candidate);

    ViewLine lastLineAtScope(ViewLine startLine);

    Pair<Boolean, List<Text>> isSubderivation(String label1, String label2);

    Pair<Boolean, List<Text>> isAccessibleSubderivationFor(ViewLine line, String label1, String label2);

    void setCheckMax(int checkMax);
    void setCheckTries(int checkTries);
    void setHelpMax(int helpMax);
    void setHelpTries(int helpTries);
    List<ViewLine> getViewLines();
    DerivationRuleset getDerivationRuleset();
    Exercise getExercise();
    int getCheckMax();
    int getCheckTries();
    int getHelpMax();
    int getHelpTries();
    boolean isCheckFinal();
    boolean isCheckSuccess();











}

