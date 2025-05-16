package slapp.editor.derivation;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.der_systems.DerivationRule;
import slapp.editor.derivation.der_systems.DerivationRuleset;
import slapp.editor.derivation.der_systems.DerivationRulesets;
import slapp.editor.derivation.theorems.Theorem;
import slapp.editor.parser.Expression;
import slapp.editor.parser.Languages;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.grammatical_parts.Formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DerivationCheck {

    private DerivationExercise derivationExercise;
    private DerivationView derivationView;
    private DerivationModel derivationModel;
    private DerivationRuleset derivationRuleset;
    private ViewLine markedLine;
    private List<ViewLine> viewLines;

    private int checkMax;
    private int checkTries;
    private int helpMax;
    private int helpTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    DerivationCheck(DerivationExercise derivationExercise) {
        this.derivationExercise = derivationExercise;
        this.derivationView = derivationExercise.getExerciseView();
        this.derivationModel = derivationExercise.getExerciseModel();
        setRightControlBox();

        CheckSetup checkSetup = derivationExercise.getExerciseModel().getCheckSetup();
        if (checkSetup == null) {checkSetup = new CheckSetup();}


        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            derivationView.setCheckColor(Color.LAWNGREEN);
            derivationView.setCheckElementsColor(Color.GREEN);
            derivationView.setCheckMessage("Derivation");
        }
        else {
            derivationView.setCheckColor(Color.ORCHID);
            derivationView.setCheckElementsColor(Color.PURPLE);
            derivationView.setCheckMessage("Checked Lines: Good");
        }
        if (checkSuccess) derivationView.activateBigCheck();
        else derivationView.deactivateBigCheck();

        derivationRuleset = DerivationRulesets.getRuleset(checkSetup.getRulesetName());
        derivationRuleset.setMetaLanguage(Languages.getLanguage(checkSetup.getMetLangName()));
        derivationRuleset.setObjectLanguage(Languages.getLanguage(checkSetup.getObjLangName()));

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        helpMax = checkSetup.getHelpMax();
        helpTries = checkSetup.getHelpTries();
        updateCounters();

    }

    public String getContradictionSymbolString() {
        return derivationRuleset.getObjectLanguage().getContradictionSymbol().toString();
    }

    public void updateCounters() {
        setChecksCounter();
        setHelpCounter();
    }

    private void setChecksCounter() {
        if (checkMax != -1 && checkTries >= checkMax) {
            derivationView.getCheckButton().setDisable(true);
            derivationView.getCheckProgButton().setDisable(true);
        }
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

    private void setRightControlBox() {

        derivationView.getCheckButton().setOnAction(e -> {
            checkFinal = true;
            derivationView.setCheckColor(Color.LAWNGREEN);
            derivationView.setCheckMessage("Derivation");
            derivationView.setCheckElementsColor(Color.GREEN);
            boolean check = runCheck();
        });

        derivationView.getCheckProgButton().setOnAction(e -> {
            checkFinal = false;
            derivationView.setCheckColor(Color.ORCHID);
            derivationView.setCheckMessage("Checked Lines: Good");
            derivationView.setCheckElementsColor(Color.PURPLE);

            boolean check = runCheck();
        });

        derivationView.getHelpButton().setOnAction(e -> {
        });

        derivationView.getStaticHelpButton().setDisable(!derivationModel.getCheckSetup().isStaticHelpButton());
        derivationView.getStaticHelpButton().setOnAction(e -> {
            derivationView.showStaticHelp(derivationModel.getCheckSetup().getStaticHelpDoc());
        });

    }

    private boolean runCheck() {
        checkSuccess = false;
        derivationView.deactivateBigCheck();
        checkTries++;
        setChecksCounter();

        viewLines = derivationView.getViewLines();

        if (!checkFormulas()) return false;
        if (!checkScopeStructure()) return false;
 //       if (!checkFormulas()) return false;
        if (!checkAssumptionScopes()) return false;
        if (!checkJustificationForms()) return false;
        setScopeLists();
        if (!checkJustifications()) return false;

        checkSuccess = true;
        derivationView.activateBigCheck();
        //set check on model
        return true;
    }

    private boolean checkJustifications() {
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (LineType.isContentLine(viewLine.getLineType())) {
                TextFlow justificationFlow = viewLine.getJustificationFlow();
                String justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);

                for (DerivationRule rule : derivationRuleset.getRules()) {
                    if (rule.matches(justificationString)) {
                        List<String> lineLabels = derivationExercise.getLineLabelsFromJustificationFlow(justificationFlow);
                        String[] labelArray = new String[lineLabels.size()];
                        lineLabels.toArray(labelArray);
                        Pair<Boolean, List<Text>> resultPair = rule.applies(this, viewLines.get(i), labelArray);
                        if (!resultPair.getKey()) {
                            highlightLine(i);
                            EditorAlerts.showSimpleTxtListAlert("Justification Issue:", resultPair.getValue());
                            resetHighlights();
                            return false;
                        }
                        break;
                    }
                }
                for (Theorem theorem : derivationExercise.getTheorems()) {
                    if (theorem.matches(justificationString)) {
                        List<String> lineLabels = derivationExercise.getLineLabelsFromJustificationFlow(justificationFlow);
                        String[] labelArray = new String[lineLabels.size()];
                        lineLabels.toArray(labelArray);
                        Pair<Boolean, List<Text>> resultPair = theorem.applies(this, viewLines.get(i), labelArray);
                        if (!resultPair.getKey()) {
                            highlightLine(i);
                            EditorAlerts.showSimpleTxtListAlert("Justification Issue:", resultPair.getValue());
                            resetHighlights();
                            return false;
                        }
                        break;
                    }
                }
            }
        }

        return true;
    }

    public Pair<ViewLine, List<Text>>getLineFromLabel(String label) {
        ViewLine line = null;

        for (ViewLine viewLine : viewLines) {
            if (LineType.isContentLine(viewLine.getLineType()) && viewLine.getLineNumberLabel().getText().equals(label)) {
                line = viewLine;
                break;
            }
        }
        List texts = new ArrayList();
        if (line == null) texts.add(ParseUtilities.newRegularText("Line (" + label + ") not found in this derivation."));
        return new Pair(line, texts);
    }

    //requires justifier and candidate are content lines
    public Pair<Boolean, List<Text>> lineIsAccessibleTo(ViewLine justifier, ViewLine candidate) {
        String justifierLabel = justifier.getLineNumberLabel().getText();
        String candidateLabel = candidate.getLineNumberLabel().getText();
        if (viewLines.indexOf(candidate) <= viewLines.indexOf(justifier)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Line " + justifierLabel + " (does not come before and) is not available for justification of line " + candidateLabel + ".")));
        }
        List<String> justifierAssps = justifier.getAssumptionList();
        List<String> candidateAssps = candidate.getAssumptionList();
        for (String labelStr : justifierAssps) {
            if (!candidateAssps.contains(labelStr)) {
                return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Line " + justifierLabel + " is not accessible for justification of line " + candidateLabel + ".")));
            }
        }
        return new Pair(true, null);
    }

    //requires start line is content line
    public ViewLine lastLineAtScope(ViewLine startLine) {
        int index = viewLines.indexOf(startLine);
        int depth = startLine.realDepth();
        ViewLine lastCandidate = startLine;
        for (int i = index; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (viewLine.realDepth() >= depth) {
                if (LineType.isContentLine(viewLine.getLineType())) lastCandidate = viewLine;
            }
            else break;
        }
        return lastCandidate;
    }


    public Pair<Boolean, List<Text>> isSubderivation(String label1, String label2) {

        ViewLine startLine;
        Pair<ViewLine, List<Text>> startLinePair = getLineFromLabel(label1);
        if (startLinePair.getKey() != null) startLine = startLinePair.getKey();
        else return new Pair<>(false, startLinePair.getValue());

        TextFlow startJustificationFlow = startLine.getJustificationFlow();
        String startJustificationString = derivationExercise.getStringFromJustificationFlow(startJustificationFlow);
        Matcher matcher = derivationRuleset.getGenericAssumption().matcher(startJustificationString);
        if (!matcher.matches()) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("A subderivation " + label1 + "-" + label2 + " must start with an assumption.")));
        }
        ViewLine lastLine = lastLineAtScope(startLine);
        if (!lastLine.getLineNumberLabel().getText().equals(label2)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText(label2 + " does not identify the last line of the subderivation beginning at " + label1 + ".")));
        }
        return new Pair<>(true, null);
    }

    public Pair<Boolean, List<Text>> isAccessibleSubderivationFor(ViewLine line, String label1, String label2) {
        Pair<Boolean, List<Text>> subderResult = isSubderivation(label1, label2);
        if (!subderResult.getKey()) {
            return subderResult;
        }


        ViewLine firstLine;
        Pair<ViewLine, List<Text>> firstLinePair = getLineFromLabel(label1);
        if (firstLinePair.getKey() != null) firstLine = firstLinePair.getKey();
        else return new Pair(false, firstLinePair.getValue());

        ViewLine lastLine;
        Pair<ViewLine, List<Text>> lastLinePair = getLineFromLabel(label2);
        if (lastLinePair.getKey() != null) lastLine = lastLinePair.getKey();
        else return new Pair(false, lastLinePair.getValue());

        String candidateLabel = line.getLineNumberLabel().getText();
        if (viewLines.indexOf(line) <= viewLines.indexOf(firstLine)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Subderivation must be complete (and assumption discharged) before any appeal to it.")));
        }

        if (viewLines.indexOf(line) <= viewLines.indexOf(lastLine)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Assumption (" + label1 + ") is not discharged at (" + candidateLabel + ") and so (" + label1 + ")-(" + label2 +") is not accessible to (" + candidateLabel + ").")));
        }

        List<String> lastLineAssps = lastLine.getAssumptionList();
        List<String> candidateAssps = line.getAssumptionList();
        for (int i = 0; i < lastLineAssps.size() - 1; i++) {
            String labelStr = lastLineAssps.get(i);
            if (!candidateAssps.contains(labelStr)) {
                return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Subderivation " + label1 + "-" + label2 + " is not accessible for the justification of (" + line.getLineNumberLabel().getText() + ").")));
            }
        }
        return new Pair<>(true, null);
    }


    private void setScopeLists() {
        ArrayList<String> premiseList = new ArrayList();
        ArrayList<String> assumptionList = new ArrayList<>();
        int currentDepth = 1;

        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (viewLine.realDepth() == currentDepth) {
                if (LineType.isContentLine(viewLine.getLineType())) {
                    TextFlow justificationFlow = viewLine.getJustificationFlow();
                    String justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);
                    if (derivationRuleset.getPremiseRule().matches(justificationString)) {
                        premiseList.add(viewLine.getLineNumberLabel().getText());
                    }
                }
            }
            else {
                if (LineType.isContentLine(viewLine.getLineType()) && viewLine.realDepth() == currentDepth + 1) {
                    assumptionList.add(viewLine.getLineNumberLabel().getText());
                    currentDepth++;
                }
                else if (viewLine.realDepth() == currentDepth - 1) {
                    assumptionList.remove(assumptionList.size() - 1);
                    currentDepth--;
                }
            }
            ArrayList newList = new ArrayList(premiseList);
            newList.addAll(assumptionList);
            viewLine.setAssumptionList(newList);
        }
    }

    private boolean checkAssumptionScopes() {
        int currentDepth = 1;
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            String justificationString = "";
            if (LineType.isContentLine(viewLine.getLineType())) {
                TextFlow justificationFlow = viewLine.getJustificationFlow();
                justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);
            }

            Matcher matcher = derivationRuleset.getGenericAssumption().matcher(justificationString);
            if (viewLine.realDepth() == currentDepth + 1 && !matcher.matches()) {
                highlightLine(i);
                EditorAlerts.showSimpleTxtListAlert("Scope Increase:", Collections.singletonList(ParseUtilities.newRegularText("An increase in scope should be associated with a justification by assumption.")));
                resetHighlights();
                return false;
            }
            else if (viewLine.realDepth() != currentDepth + 1 && matcher.matches()) {
                highlightLine(i);
                EditorAlerts.showSimpleTxtListAlert("Assumption Scope:", Collections.singletonList(ParseUtilities.newRegularText("A justification by assumption should be associated with a scope increase.")));
                resetHighlights();
                return false;
            }
            else{
                currentDepth = viewLine.realDepth();
            }
        }
        return true;
    }

    private boolean checkJustificationForms() {
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (LineType.isContentLine(viewLine.getLineType())) {
                BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document lineDoc = rta.getDocument();
                TextFlow justificationFlow = viewLine.getJustificationFlow();
                String justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);

                if (lineDoc.getText().equals("") && !justificationString.equals("")) {
                    highlightJustification(i);
                    EditorAlerts.showSimpleTxtListAlert("Missing Formula", Collections.singletonList(ParseUtilities.newRegularText("No formula to which justification applies.")));
                    resetHighlights();
                    return false;
                }

                if (!lineDoc.getText().equals("")) {
                    boolean blankOK = false;

                    ViewLine priorLine = derivationExercise.getContentLineAbove(i);
                    if (!priorLine.equals(viewLine)) {
                        TextFlow priorJustificationFlow = priorLine.getJustificationFlow();
                        String priorJustificationString = derivationExercise.getStringFromJustificationFlow(priorJustificationFlow);
                        if ((derivationRuleset.getAsspRestrictedExisExploitCRule() != null && derivationRuleset.getAsspRestrictedExisExploitCRule().matches(priorJustificationString))
                                || (derivationRuleset.getAsspRestrictedExisExploitGRule() != null) && derivationRuleset.getAsspRestrictedExisExploitGRule().matches(priorJustificationString)) {
                            blankOK = true;
                        }
                    }

                    if (justificationString.equals("")) {
                        if (blankOK) break;
                        if (checkFinal) {
                            highlightJustification(i);
                            EditorAlerts.showSimpleTxtListAlert("Missing Justification:", Collections.singletonList(ParseUtilities.newRegularText("Line requires justification.")));
                            resetHighlights();
                            return false;
                        }
                    } else {
                        boolean ok = false;
                        for (DerivationRule rule : derivationRuleset.getRules()) {
                            if (rule.matches(justificationString)) {
                                ok = true;
                                break;
                            }
                        }

                        for (Theorem theorem : derivationExercise.getTheorems()) {
                            if (theorem.matches(justificationString)) {
                                ok = true;
                                break;
                            }
                        }
                        if (!ok) {
                            for (Pair<Pattern, String> dummyPair : derivationRuleset.getDummyRules()) {
                                Matcher matcher = dummyPair.getKey().matcher(justificationString);
                                if (matcher.matches()) {
                                    highlightJustification(i);
                                    EditorAlerts.showSimpleTxtListAlert("Justification Error:", Collections.singletonList(ParseUtilities.newRegularText(dummyPair.getValue())));
                                    resetHighlights();
                                    return false;
                                }
                            }
                            String message = "I do not recognize this as a(n) ";
                            if (!derivationExercise.getTheorems().isEmpty()) message = "I do not recognize this as an (available) ";
                            highlightJustification(i);
                            List<Text> texts = new ArrayList<>();
                  //          texts.add(new Text("I do not recognize this as a(n) "));
                            texts.add(new Text(message));
                            texts.addAll(derivationRuleset.getTextName());
                            texts.add(new Text(" justification."));
                            EditorAlerts.showSimpleTxtListAlert("Justification Issue:", texts);
                            resetHighlights();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean checkFormulas() {
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (LineType.isContentLine(viewLine.getLineType())) {
                BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document lineDoc = rta.getDocument();
                if (!lineDoc.getText().equals("")) {

                    List<Expression> parseExpressions = ParseUtilities.parseDoc(lineDoc, derivationRuleset.getObjectLanguage().getNameString());
                    if (parseExpressions.size() == 1 && parseExpressions.get(0) instanceof Formula) {
                        if (!ParseUtilities.getAlternateMessage().isEmpty()) {
                            highlightFormula(i);
                            EditorAlerts.showSimpleTxtListAlert("Problem in bounded quantifier:", ParseUtilities.getAlternateMessage());
                            resetHighlights();
                            return false;
                        }
                        continue;
                    }
                    else {
                        List<Text> texts = new ArrayList<>();
                        Text leadText = new Text("Parse results:\n");
                        leadText.setFont(Font.font("Noto Serif Combo", FontPosture.ITALIC, 11));
                        texts.add(leadText);

                        for (Expression expr : parseExpressions) {
                            texts.add(new Text("    "));
                            texts.add(new Text(expr.getType() + ": "));
                            texts.addAll(expr.toTextList());
                            texts.add(new Text("\n"));
                        }
                        highlightFormula(i);
                        EditorAlerts.showSimpleTxtListAlert("Not a formula of " + derivationRuleset.getObjectLanguage().getNameString() + ":", texts);
                        resetHighlights();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkScopeStructure() {

        if (viewLines.size() > 0) {
            ViewLine firstLine = viewLines.get(0);
            if (!LineType.isContentLine(firstLine.getLineType())) {
                EditorAlerts.showSimpleTxtListAlert("Derivation Start:", Collections.singletonList(ParseUtilities.newRegularText("Top line of derivation cannot be a 'shelf' or 'gap'.")));
                return false;
            }
            if (firstLine.realDepth() > 2) {
                highlightFormula(0);
                EditorAlerts.showSimpleTxtListAlert("Derivation Start:", Collections.singletonList(ParseUtilities.newRegularText("Starting scope depth cannot be greater than 2.")));
                resetHighlights();
                return false;
            }
        }
        if (viewLines.size() > 1) {
            ViewLine lastLine = viewLines.get(viewLines.size() - 1);
            if (!LineType.isContentLine(lastLine.getLineType())) {
                EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(ParseUtilities.newRegularText("Last line of derivation cannot be a 'shelf' or 'gap'.")));
                return false;
            }
            if (lastLine.realDepth() != 1) {
                highlightFormula(viewLines.size() - 1);
                EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(ParseUtilities.newRegularText("Last line of derivation cannot have scope depth greater than 1.")));
                resetHighlights();
                return false;
            }

            BoxedDRTA bottomDRTA = lastLine.getLineContentBoxedDRTA();
            RichTextArea bottomRTA = bottomDRTA.getRTA();
            bottomRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document bottomLineDoc = bottomRTA.getDocument();

            if (bottomLineDoc.getText().equals("")) {
                highlightFormula(viewLines.size() - 1);
                EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(ParseUtilities.newRegularText("Last line of derivation should be populated with a formula.")));
                resetHighlights();
                return false;

            }
        }
        int depth = viewLines.get(0).getDepth();
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine currentLine = viewLines.get(i);
            int currentDepth = currentLine.realDepth();

            if (currentDepth != 1 && !derivationRuleset.isPermitSubderivations()) {
                highlightLine(i);
                EditorAlerts.showSimpleTxtListAlert("Scope Increase:", Collections.singletonList(ParseUtilities.newRegularText("Basic " + derivationRuleset.getName() + " does not permit subderivations.")));
                resetHighlights();
                return false;
            }

            if (LineType.isGapLine(currentLine.getLineType())) {
                if (currentDepth + 1 != viewLines.get(i - 1).realDepth() ) {
                    highlightFormula(i - 1);
                    EditorAlerts.showSimpleTxtListAlert("Gap Scope:", Collections.singletonList(ParseUtilities.newRegularText("Scope depth of line before gap should be one more than that of gap.")));
                    resetHighlights();
                    return false;
                }
                if (currentDepth + 1 != viewLines.get(i + 1).realDepth()) {
                    highlightFormula(i + 1);
                    EditorAlerts.showSimpleTxtListAlert("Gap Scope:", Collections.singletonList(ParseUtilities.newRegularText("Scope depth of line after gap should be one more than that of gap.")));
                    resetHighlights();
                    return false;
                }
            }
            else if (LineType.isContentLine(currentLine.getLineType())) {
                if (currentLine.realDepth() > depth + 1 || currentLine.realDepth() < depth - 1) {
                    highlightFormula(i);
                    EditorAlerts.showSimpleTxtListAlert("Scope Jump:", Collections.singletonList(ParseUtilities.newRegularText("From one line to the next, scope depth cannot change by more than one.")));
                    resetHighlights();
                    return false;
                }
            }
            depth = currentDepth;
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
        markedLine = line;
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

    public List<ViewLine> getViewLines() {
        return viewLines;
    }


    public DerivationRuleset getDerivationRuleset() {
        return derivationRuleset;
    }

    public DerivationExercise getDerivationExercise() {
        return derivationExercise;
    }

    public int getCheckMax() {
        return checkMax;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public int getHelpMax() {
        return helpMax;
    }

    public int getHelpTries() {
        return helpTries;
    }

    public boolean isCheckFinal() {
        return checkFinal;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }


}
