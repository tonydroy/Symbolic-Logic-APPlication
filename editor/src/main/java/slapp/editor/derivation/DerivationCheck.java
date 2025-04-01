package slapp.editor.derivation;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerSystems.DerivationRuleset;

import java.util.Collections;
import java.util.List;

public class DerivationCheck {

    private DerivationExercise derivationExercise;
    private DerivationView derivationView;
    private DerivationRuleset derivationRuleset;
    private ViewLine markedLine;
    private List<ViewLine> viewLines;

    private int checkMax;
    private int checkTries;
    private int helpMax;
    private int helpTries;

    DerivationCheck(DerivationExercise derivationExercise, DerivationView derivationView, DerivationRuleset ruleset) {
        this.derivationExercise = derivationExercise;
        this.derivationView = derivationView;
        this.derivationRuleset = ruleset;

        setRightControlBox();
    }

    public void updateCounters() {
        setChecksCounter();
        setHelpCounter();
    }

    private void setRightControlBox() {


        derivationView.getCheckButton().setOnAction(e -> {
           boolean check = runCheck();
           System.out.println(check);
        });


        derivationView.getHelpButton().setOnAction(e -> {

        });
    }

    private boolean runCheck() {
        derivationView.deactivateBigCheck();
        checkTries++;
        setChecksCounter();

        viewLines = derivationView.getViewLines();


        if (!checkFormulas()) return false;
        if (!checkScopeStructure()) return false;

        derivationView.activateBigCheck();
        //set check on model
        return true;
    }

    private void setChecksCounter() {
        if (checkMax != -1 && checkTries >= checkMax) {derivationView.getCheckButton().setDisable(true);}

        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        derivationView.getCheckTriesLabel().setText(checkString);
    }

    private void setHelpCounter() {
        if (helpMax != -1 && helpTries >= helpMax) {derivationView.getHelpButton().setDisable(true);}

        String helpString;
        if (helpMax == -1) helpString = "(unlimited)";
        else if (helpMax == 0) helpString = "(none)";
        else helpString = "(" + String.valueOf(helpTries) + "/" + String.valueOf(helpMax) + ")";

        derivationView.getHelpTriesLabel().setText(helpString);
    }



    private boolean checkScopeStructure() {

        if (viewLines.size() > 0) {
            ViewLine firstLine = viewLines.get(0);
            if (!LineType.isContentLine(firstLine.getLineType())) {
                EditorAlerts.showSimpleTxtListAlert("Derivation Start:", Collections.singletonList(new Text("Top line of derivation cannot be a 'shelf' or 'gap'.")));
                return false;
            }
            if (firstLine.realDepth() > 2) {
                highlightFormula(0);
                EditorAlerts.showSimpleTxtListAlert("Derivation Start:", Collections.singletonList(new Text("Starting scope depth cannot be greater than 2.")));
                resetHighlights();
                return false;
            }
        }
        if (viewLines.size() > 1) {
            ViewLine lastLine = viewLines.get(viewLines.size() - 1);
            if (!LineType.isContentLine(lastLine.getLineType())) {
                EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(new Text("Last line of derivation cannot be a 'shelf' or 'gap'.")));
                return false;
            }
            if (lastLine.realDepth() != 1) {
                highlightFormula(viewLines.size() - 1);
                EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(new Text("Last line of derivation cannot have scope depth greater than 1.")));
                resetHighlights();
                return false;
            }
        }
        int depth = viewLines.get(0).getDepth();
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine currentLine = viewLines.get(i);
            int currentDepth = currentLine.realDepth();

            if (LineType.isGapLine(currentLine.getLineType())) {
                if (currentDepth + 1 != viewLines.get(i - 1).realDepth() ) {
                    highlightFormula(i - 1);
                    EditorAlerts.showSimpleTxtListAlert("Gap Scope:", Collections.singletonList(new Text("Scope depth of line before gap should be one more than that of gap.")));
                    resetHighlights();
                    return false;
                }
                if (currentDepth + 1 != viewLines.get(i + 1).realDepth()) {
                    highlightFormula(i + 1);
                    EditorAlerts.showSimpleTxtListAlert("Gap Scope:", Collections.singletonList(new Text("Scope depth of line after gap should be one more than that of gap.")));
                    resetHighlights();
                    return false;
                }
            }
            else if (LineType.isContentLine(currentLine.getLineType())) {
                if (currentLine.realDepth() > depth + 1 || currentLine.realDepth() < depth - 1) {
                    highlightFormula(i);
                    EditorAlerts.showSimpleTxtListAlert("Scope Jump:", Collections.singletonList(new Text("From one line to the next, scope depth cannot change by more than one.")));
                    resetHighlights();
                    return false;
                }
            }
            depth = currentDepth;
        }
        return true;
    }

    private boolean checkFormulas() {
        for (ViewLine line : viewLines) {
            if (LineType.isContentLine(line.getLineType())) {
                BoxedDRTA bdrta = line.getLineContentBoxedDRTA();
                RichTextArea rta = bdrta.getRTA();
                Document formulaDoc = rta.getDocument();

                //check empty or formula of derivationRuleset.getLanguage();
            }
        }
        return true;
    }


    private void highlightLine(int index) {
        highlightFormula(index);
        highlightJustification(index);
    }

    private void highlightJustification(int index) {
        ViewLine line = viewLines.get(index);
        TextFlow justificationFlow = line.getJustificationFlow();
        justificationFlow.setStyle("-fx-background-color: mistyrose");
        line.setLineHighlight(true);

    }

    private void highlightFormula(int index) {

            ViewLine line = viewLines.get(index);
            BoxedDRTA bdrta = line.getLineContentBoxedDRTA();
            bdrta.setBackgroundColor();
            markedLine = line;
    }

    private void resetHighlights() {
        ViewLine line = markedLine;
        BoxedDRTA bdrta = line.getLineContentBoxedDRTA();
        bdrta.resetBackgroundColor();
        line.getJustificationFlow().setStyle("-fx-background-color: transparent");
        line.setLineHighlight(false);
    }

    private int getCurrentLineNum() {
        int row = -1;
        Node lastFocusedNode = derivationExercise.getMainWindow().getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (derivationExercise.getLastJustificationRTA() == lastFocusedNode) row = derivationExercise.getLastJustificationRow();
        return row;

    }


    public void setCheckMax(int checkMax) {
        this.checkMax = checkMax;
    }

    public void setCheckTries(int checkTries) {
        this.checkTries = checkTries;
    }

    public void setHelpMax(int helpMax) {
        this.helpMax = helpMax;
    }

    public void setHelpTries(int helpTries) {
        this.helpTries = helpTries;
    }
}
