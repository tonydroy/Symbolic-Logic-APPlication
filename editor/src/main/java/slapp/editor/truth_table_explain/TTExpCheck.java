package slapp.editor.truth_table_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.AlphanumStringComparator;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.parser.Expression;
import slapp.editor.parser.MatchUtilities;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.SyntacticalFns;
import slapp.editor.parser.grammatical_parts.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.Node;


public class TTExpCheck {
    private TruthTableExpExercise ttExercise;
    private TruthTableExpView ttView;
    private TruthTableExpModel ttModel;
    private TTExpCheckSetup checkSetup;

    private String objLangName;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    private List<Document> basicDocs;
    private List<Pair<Formula, String>[]> interpretations;
    private Formula[] basicFormulas;
    private List<Formula> mainFormulas;
    private int tableColumns;
    private int tableRows;
    private String[][] tableKey;
    private List<List<Expression>> mainFormulaSubLists;
    private int subformulaColCounter;

    private boolean choiceAMade;
    private boolean choiceBMade;
    private boolean choiceMade;
    private boolean choiceASet;
    private boolean choiceBSet;
    private boolean choiceSet;
    private boolean checkChoices;
    Background boxBackground;
    Background highlightBackground;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    TTExpCheck(TruthTableExpExercise ttExpExercise) {

        this.ttExercise = ttExpExercise;
        this.ttView = ttExercise.getExerciseView();
        this.ttModel = ttExercise.getExerciseModel();

        if (ttView.getaCheckBox() != null) {
            boxBackground = ttView.getaCheckBox().getBackground();
        }
        highlightBackground = new Background(new BackgroundFill(Color.MISTYROSE, CornerRadii.EMPTY, Insets.EMPTY));

        checkSetup = ttModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new TTExpCheckSetup();
            ttModel.setCheckSetup(checkSetup);
        }
        objLangName = checkSetup.getObjLangName();

        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            ttView.setCheckColor(Color.LAWNGREEN);
            ttView.setCheckElementsColor(Color.GREEN);
        }
        else {
            ttView.setCheckColor(Color.ORCHID);
            ttView.setCheckElementsColor(Color.PURPLE);
        }
        ttView.setCheckMessage(checkSetup.getCheckMessage());
        if (checkSuccess) ttView.activateBigCheck();
        else ttView.deactivateBigCheck();

        checkChoices = checkSetup.isCheckChoices();
        choiceASet = checkSetup.isChoiceA();
        choiceBSet = checkSetup.isChoiceB();
        choiceSet = choiceASet || choiceBSet;

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();

        setRightControlBox();
    }

    private void scrollToNode(Node node) {
        if (isTornDown) return;
        if (ttExercise != null && ttExercise.getMainWindow() != null && ttExercise.getMainWindow().getMainView() != null) {
            ttExercise.getMainWindow().getMainView().scrollToNode(node);
        }
    }

    private void setRightControlBox() {
        if (isTornDown || ttView == null) return;

        if (ttView.getCheckButton() != null) {
            checkButtonHandler = e -> {
                if (isTornDown || ttExercise == null || ttView == null || ttModel == null) return;
                checkTable();
            };
            ttView.getCheckButton().setOnAction(checkButtonHandler);
        }

        if (ttView.getStaticHelpButton() != null && checkSetup != null) {
            ttView.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

            staticHelpButtonHandler = e -> {
                if (isTornDown || ttExercise == null || ttView == null || ttModel == null) return;
                Stage helpStage = ttView.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else if (ttModel.getCheckSetup() != null) ttView.showStaticHelp(ttModel.getCheckSetup().getStaticHelpDoc());
            };
            ttView.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
        }

        if (ttView.getShortTableCheck() != null && checkSetup != null) {
            ttView.getShortTableCheck().setDisable(!checkSetup.isPermitShortTable());
            ttView.getShortTableCheck().setSelected(checkSetup.isShortTable());
        }
    }

    public void cleanup() {
        // KILL SWITCH: Engaged!
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Clear Global Static Caches
        try { MatchUtilities.clearMatching(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation via tiny isolated try-catch blocks
        try {
            if (ttView != null && ttView.getCheckButton() != null && checkButtonHandler != null) {
                ttView.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                ttView.getCheckButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (ttView != null && ttView.getStaticHelpButton() != null && staticHelpButtonHandler != null) {
                ttView.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                ttView.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // Clear dynamic checks gracefully
        try { if (interpretations != null) interpretations.clear(); } catch (Exception e) {}
        try { if (mainFormulas != null) mainFormulas.clear(); } catch (Exception e) {}
        try {
            if (mainFormulaSubLists != null) {
                for (List<Expression> list : mainFormulaSubLists) {
                    if (list != null) list.clear();
                }
                mainFormulaSubLists.clear();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE DATA TEARDOWN
        try { this.basicDocs = null; } catch (Exception e) {}
        try { this.interpretations = null; } catch (Exception e) {}
        try { this.basicFormulas = null; } catch (Exception e) {}
        try { this.mainFormulas = null; } catch (Exception e) {}
        try { this.tableKey = null; } catch (Exception e) {}
        try { this.mainFormulaSubLists = null; } catch (Exception e) {}

        try { this.boxBackground = null; } catch (Exception e) {}
        try { this.highlightBackground = null; } catch (Exception e) {}
        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}

        try { this.ttExercise = null; } catch (Exception e) {}
        try { this.ttView = null; } catch (Exception e) {}
        try { this.ttModel = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.objLangName = null; } catch (Exception e) {}
    }

    public boolean checkTable() {
        if (isTornDown || ttExercise == null || ttModel == null || ttView == null) return false;

        boolean checkGood = true;
        basicDocs = ttModel.getBasicFormulas();
        if (basicDocs == null) return false;

        tableColumns = ttExercise.getTableColumns();
        tableRows = (int) Math.pow(2, basicDocs.size());
        checkSuccess = false;
        ttView.deactivateBigCheck();

        if (ttView.getaCheckBox() != null) choiceAMade = ttView.getaCheckBox().isSelected();
        if (ttView.getbCheckBox() != null) choiceBMade = ttView.getbCheckBox().isSelected();
        choiceMade = choiceAMade || choiceBMade;

        if (ttExercise.getMainWindow() != null && !ttExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        //check table
        if (ttView.getShortTableCheck() != null && !ttView.getShortTableCheck().isSelected()) {
            if (!checkFullTable()) checkGood = false;
        }
        else {
            if (!checkShortTable()) checkGood = false;
        }

        checkSuccess = false;
        //check choice
        if (checkGood) {
            if (checkSetup != null && checkSetup.isAutoCheckValidity()) {
                if (!checkChoiceMade()) checkGood = false;
                else if (!autoCheckValidity()) checkGood = false;

                if (checkGood) {
                    checkSuccess = true;
                    ttView.setCheckMessage("Table / Choice");
                }
            }
            else {
                if ((checkChoices || (ttExercise.getMainWindow() != null && ttExercise.getMainWindow().isInstructorFunctions()))  && choiceSet) {
                    if (!checkChoiceMade()) checkGood = false;
                    else if (!checkStaticChoice()) checkGood = false;

                    if (checkGood) {
                        checkSuccess = true;
                        ttView.setCheckMessage("Table / Choice");
                    }
                }
                else {
                    checkSuccess = true;
                    ttView.setCheckMessage("Table");
                }
            }
        }
        if (checkSuccess) ttView.activateBigCheck();
        return checkSuccess;
    }

    public boolean autoCheckValidity() {
        if (isTornDown || ttView == null || ttExercise == null) return false;

        TextField[][] tableFields = ttView.getTableFields();
        boolean goodChoice = true;
        if (choiceAMade) {   //validity check
            if (ttView.getShortTableCheck() != null && ttView.getShortTableCheck().isSelected()) {
                if (ttView.getaCheckBox() != null) {
                    ttView.getaCheckBox().setBackground(highlightBackground);
                    scrollToNode(ttView.getaCheckBox());
                    EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("A short table is not sufficient to demonstrate validity.")));
                    ttView.getaCheckBox().setBackground(boxBackground);
                }
                goodChoice = false;
            }
            else {
                boolean badRow = false;
                for (int r = 0; r < tableRows; r++) {
                    if (premisesTrueConclusionFalse(r)) {
                        badRow = true;
                        if (mainFormulas != null) {
                            for (int j = 0; j < mainFormulas.size(); j++) {
                                if (tableFields[mainFormulas.get(j).getTtColumn()][r] != null) {
                                    tableFields[mainFormulas.get(j).getTtColumn()][r].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                                    scrollToNode(tableFields[mainFormulas.get(j).getTtColumn()][r]);
                                }
                            }
                        }
                    }
                }
                if (badRow) {
                    if (ttView.getaCheckBox() != null) {
                        Background prevBackground = ttView.getaCheckBox().getBackground();
                        ttView.getaCheckBox().setBackground(highlightBackground);
                        scrollToNode(ttView.getaCheckBox());
                        EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Some row(s) have all the premises T and conclusion F.")));
                        ttView.getaCheckBox().setBackground(prevBackground);
                    } else {
                        EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Some row(s) have all the premises T and conclusion F.")));
                    }
                    for (int r = 0; r < tableRows; r++) {
                        if (mainFormulas != null) {
                            for (int j = 0; j < mainFormulas.size(); j++) {
                                int mfCol = mainFormulas.get(j).getTtColumn();
                                if (ttView.getHighlightButtons() != null && ttView.getHighlightButtons()[mfCol] != null && ttView.getHighlightButtons()[mfCol].isSelected()) {
                                    if (tableFields[mfCol][r] != null) tableFields[mfCol][r].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                                } else {
                                    if (tableFields[mfCol][r] != null) tableFields[mfCol][r].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                                }
                            }
                        }
                    }
                    goodChoice = false;
                }
            }
        }
        else {  //invalidity
            BoxedDRTA[] rowCommentsArray = ttView.getRowCommentsArray();
            List<Integer> markedRows = new ArrayList<>();
            if (rowCommentsArray != null) {
                for (int r = 0; r < rowCommentsArray.length; r++) {
                    if (rowCommentsArray[r] == null || rowCommentsArray[r].getRTA() == null) continue;
                    RichTextArea rowRTA = rowCommentsArray[r].getRTA();
                    if (rowRTA.isModified() && ttExercise != null) ttExercise.setExerciseModified(true);
                    rowRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    if (rowRTA.getDocument() != null && rowRTA.getDocument().getText() != null) {
                        String commentStr = rowRTA.getDocument().getText().strip();
                        if (commentStr.startsWith("*") || commentStr.startsWith("\u2217") || commentStr.startsWith("\u22c6") || commentStr.startsWith("\u27f8") || commentStr.startsWith("\u21d0")) {
                            markedRows.add(r);
                        }
                    }
                }
            }
            if (markedRows.size() == 0) {
                if (ttView.getbCheckBox() != null) {
                    ttView.getbCheckBox().setBackground(highlightBackground);
                    scrollToNode(ttView.getbCheckBox());
                    EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Invalidity requires a marked row with the premises true and conclusion false (mark with asterisk, star, or a double back arrow).")));
                    ttView.getbCheckBox().setBackground(boxBackground);
                }
                goodChoice = false;
            }
            else {
                boolean badRow = false;
                for (int r = 0; r < markedRows.size(); r++) {
                    if (!premisesTrueConclusionFalse(markedRows.get(r))) {
                        badRow = true;
                        if (mainFormulas != null) {
                            for (int j = 0; j < mainFormulas.size(); j++) {
                                if (tableFields[mainFormulas.get(j).getTtColumn()][markedRows.get(r)] != null) {
                                    tableFields[mainFormulas.get(j).getTtColumn()][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                                    scrollToNode(tableFields[mainFormulas.get(j).getTtColumn()][markedRows.get(r)]);
                                }
                            }
                        }
                    }
                }
                if (badRow) {
                    if (ttView.getbCheckBox() != null) {
                        ttView.getbCheckBox().setBackground(highlightBackground);
                        scrollToNode(ttView.getbCheckBox());
                        EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Some marked row(s) do not have all the premises T and conclusion F.")));
                        ttView.getbCheckBox().setBackground(boxBackground);
                    }
                    for (int r = 0; r < markedRows.size(); r++) {
                        if (mainFormulas != null) {
                            for (int j = 0; j < mainFormulas.size(); j++) {
                                int mfCol = mainFormulas.get(j).getTtColumn();
                                if (ttView.getHighlightButtons() != null && ttView.getHighlightButtons()[mfCol] != null && ttView.getHighlightButtons()[mfCol].isSelected()) {
                                    if (tableFields[mfCol][markedRows.get(r)] != null) tableFields[mfCol][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                                } else {
                                    if (tableFields[mfCol][markedRows.get(r)] != null) tableFields[mfCol][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                                }
                            }
                        }
                    }
                    goodChoice = false;
                }
            }
        }
        return goodChoice;
    }

    public boolean premisesTrueConclusionFalse(int r) {
        if (isTornDown || ttView == null || mainFormulas == null || mainFormulas.isEmpty()) return false;

        TextField[][] tableFields = ttView.getTableFields();

        boolean conclusionF = false;
        if (tableFields[mainFormulas.get(mainFormulas.size() - 1).getTtColumn()][r] != null) {
            conclusionF = tableFields[mainFormulas.get(mainFormulas.size() - 1).getTtColumn()][r].getText().equals("F");
        }

        boolean premisesT = true;
        for (int j = 0; j + 1 < mainFormulas.size(); j++) {
            if (tableFields[mainFormulas.get(j).getTtColumn()][r] != null && tableFields[mainFormulas.get(j).getTtColumn()][r].getText().equals("F")) {
                premisesT = false;
                break;
            }
        }
        return premisesT && conclusionF;
    }


    public boolean checkShortTable() {
        if (isTornDown || ttExercise == null || ttModel == null || ttView == null) return false;
        boolean checkGood = true;

        if (!checkBasicDocs()) checkGood = false;
        else if (!populateBasicFormulas()) checkGood = false;
        else if (!checkBasicOrder()) checkGood = false;
        else if (!checkTableRows()) checkGood = false;
        if (!checkGood) return false;

        populateInterpretations();  //initializes tableKey
        if (!populateTableKey()) checkGood = false;

        else if (!checkShortTableValues()) checkGood = false;
        else if (!checkHighlights()) checkGood = false;

        return checkGood;
    }


    public boolean checkFullTable() {
        if (isTornDown || ttExercise == null || ttModel == null || ttView == null) return false;
        boolean checkGood = true;

        if (!checkBasicDocs()) checkGood = false;
        else if (!populateBasicFormulas()) checkGood = false;
        else if (!checkBasicOrder()) checkGood = false;
        else if (!checkTableRows()) checkGood = false;
        if (!checkGood) return false;

        populateInterpretations();  //initializes tableKey
        if (!populateTableKey()) checkGood = false;

        else if (!checkRows()) checkGood = false;     //won't apply to short tables
        else if (!checkTableValues()) checkGood = false;
        else if (!checkHighlights()) checkGood = false;

        return checkGood;
    }

    private void updateCheckCounter() {
        if (isTornDown || ttExercise == null || ttView == null || ttExercise.getMainWindow() == null) return;

        if (checkMax != -1 && checkTries >= checkMax && !ttExercise.getMainWindow().isInstructorFunctions()) {
            if (ttView.getCheckButton() != null) ttView.getCheckButton().setDisable(true);
            if (ttView.getCheckProgButton() != null) ttView.getCheckProgButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        if (ttView.getCheckTriesLabel() != null) {
            ttView.getCheckTriesLabel().setText(checkString);
        }
    }

    private void setChecksCounter() {
        if (isTornDown || ttExercise == null || ttView == null || ttExercise.getMainWindow() == null) return;

        if (checkMax != -1 && checkTries >= checkMax && !ttExercise.getMainWindow().isInstructorFunctions()) {
            if (ttView.getCheckButton() != null) ttView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        if (ttView.getCheckTriesLabel() != null) {
            ttView.getCheckTriesLabel().setText(checkString);
        }
    }

    private boolean checkStaticChoice() {
        if (isTornDown || ttView == null) return false;
        boolean success = true;

        if (choiceAMade && !choiceASet) {
            if (ttView.getaCheckBox() != null) {
                ttView.getaCheckBox().setBackground(highlightBackground);
                scrollToNode(ttView.getaCheckBox());
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
                ttView.getaCheckBox().setBackground(boxBackground);
            }
            success = false;
        }
        if (choiceBMade && !choiceBSet) {
            if (ttView.getbCheckBox() != null) {
                ttView.getbCheckBox().setBackground(highlightBackground);
                scrollToNode(ttView.getbCheckBox());
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
                ttView.getbCheckBox().setBackground(boxBackground);
            }
            success = false;
        }
        return success;
    }

    private boolean checkChoiceMade() {
        if (isTornDown || ttView == null) return false;
        boolean success = true;

        if (!choiceMade) {
            if (ttView.getaCheckBox() != null) ttView.getaCheckBox().setBackground(highlightBackground);
            if (ttView.getbCheckBox() != null) ttView.getbCheckBox().setBackground(highlightBackground);
            scrollToNode(ttView.getaCheckBox());
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));
            if (ttView.getaCheckBox() != null) ttView.getaCheckBox().setBackground(boxBackground);
            if (ttView.getbCheckBox() != null) ttView.getbCheckBox().setBackground(boxBackground);
            success = false;
        }
        return success;
    }

    private boolean checkHighlights() {
        if (isTornDown || ttView == null || ttExercise == null) return false;

        ToggleButton[] highlightButtons = ttView.getHighlightButtons();
        boolean[] highlightValues = new boolean[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            if (highlightButtons != null && highlightButtons[i] != null) {
                highlightValues[i] = highlightButtons[i].isSelected();
            }
        }
        TextField[][] tableFields = ttView.getTableFields();

        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        if (baseFormulaCols != null) {
            for (int i = 0; i < baseFormulaCols.size(); i++) {
                int baseCol = baseFormulaCols.get(i);

                if (highlightValues[baseFormulaCols.get(i)]) {
                    if (highlightValues[baseCol]) {
                        for (int k = 0; k < ttExercise.getTableRows(); k++) {
                            if (tableFields[baseCol][k] != null) {
                                tableFields[baseCol][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                                scrollToNode(tableFields[baseCol][k]);
                            }
                        }
                        EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight only columns in main table.")));
                        for (int k = 0; k < ttExercise.getTableRows(); k++) {
                            if (tableFields[baseCol][k] != null) tableFields[baseCol][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        }
                        return false;
                    }
                }
            }
        }

        if (mainFormulaSubLists != null) {
            for (int i = 0; i < mainFormulaSubLists.size(); i++) {
                List<Expression> sublist = mainFormulaSubLists.get(i);
                for (int j = 0; j < sublist.size(); j++) {
                    Formula sub = (Formula) sublist.get(j);
                    if (highlightValues[sub.getTtColumn()] == true && !isMainFormula(sub)) {
                        for (int k = 0; k < ttExercise.getTableRows(); k++) {
                            if (tableFields[sub.getTtColumn()][k] != null) {
                                tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                                scrollToNode(tableFields[sub.getTtColumn()][k]);
                            }
                        }
                        EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight only column for value of the whole formula.")));
                        for (int k = 0; k < ttExercise.getTableRows(); k++) {
                            if (tableFields[sub.getTtColumn()][k] != null) tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        }
                        return false;
                    }
                }
            }

            for (int i = 0; i < mainFormulaSubLists.size(); i++) {
                List<Expression> sublist = mainFormulaSubLists.get(i);
                for (int j = 0; j < sublist.size(); j++) {
                    Formula sub = (Formula) sublist.get(j);

                    if (isMainFormula(sub) && highlightValues[sub.getTtColumn()] != true) {
                        for (int k = 0; k < ttExercise.getTableRows(); k++) {
                            if (tableFields[sub.getTtColumn()][k] != null) {
                                tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                                scrollToNode(tableFields[sub.getTtColumn()][k]);
                            }
                        }
                        EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight column for value of the whole formula.")));
                        for (int k = 0; k < ttExercise.getTableRows(); k++) {
                            if (tableFields[sub.getTtColumn()][k] != null) tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white");
                        }
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkShortTableValues() {
        if (isTornDown || ttExercise == null || ttView == null || mainFormulaSubLists == null) return false;

        TextField[][] tableFields = ttView.getTableFields();
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        int rows = ttExercise.getTableRows();

        //check empty table
        boolean empty = true;
        for (int i = 0; i < rows; i++) {
            if (!emptyRow(i)) {
                empty = false;
                break;
            }
        }
        if (empty) {
            EditorAlerts.showSimpleTxtListAlert("Table:", Collections.singletonList(ParseUtilities.newRegularText("Cannot evaluate empty table.")));
            return false;
        }

        //check rows included in rows of key table
        for (int i = 0; i < rows; i++) {
            if (!tableRowGood(i)) {
                if (baseFormulaCols != null) {
                    for (int b = 0; b < baseFormulaCols.size(); b++) {
                        if (tableFields[baseFormulaCols.get(b)][i] != null && !tableFields[baseFormulaCols.get(b)][i].getText().equals("")) {
                            tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                            scrollToNode(tableFields[baseFormulaCols.get(b)][i]);
                        }
                    }
                }
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (tableFields[sub.getTtColumn()][i] != null && !tableFields[sub.getTtColumn()][i].getText().equals("")) {
                            tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                            scrollToNode(tableFields[sub.getTtColumn()][i]);
                        }
                    }
                }
                EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText("Problems in row: not all these values are included in any row of a completed table.")));
                if (baseFormulaCols != null) {
                    for (int b = 0; b < baseFormulaCols.size(); b++) {
                        if (ttView.getHighlightButtons() != null && ttView.getHighlightButtons()[baseFormulaCols.get(b)] != null && ttView.getHighlightButtons()[baseFormulaCols.get(b)].isSelected()) {
                            if (tableFields[baseFormulaCols.get(b)][i] != null) tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        } else {
                            if (tableFields[baseFormulaCols.get(b)][i] != null) tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                        }
                    }
                }
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (ttView.getHighlightButtons() != null && ttView.getHighlightButtons()[sub.getTtColumn()] != null && ttView.getHighlightButtons()[sub.getTtColumn()].isSelected()) {
                            if (tableFields[sub.getTtColumn()][i] != null) tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        }
                        else {
                            if (tableFields[sub.getTtColumn()][i] != null) tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                        }
                    }
                }
                return false;
            }
        }

        //check is at least on completed row.
        boolean isGoodRow = false;
        for (int i = 0; i < rows; i++) {
            boolean rowComplete = true;
            if (baseFormulaCols != null) {
                for (int b = 0; b < baseFormulaCols.size(); b++) {
                    if (tableFields[baseFormulaCols.get(b)][i] == null || tableFields[baseFormulaCols.get(b)][i].getText().equals("")) {
                        rowComplete = false;
                        break;
                    }
                }
            }
            if (rowComplete) {
                outer:
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (tableFields[sub.getTtColumn()][i] == null || tableFields[sub.getTtColumn()][i].getText().equals("")) {
                            if (checkSetup == null || !checkSetup.isSkipBasicsOK() || !isBasic(sub) || isMainFormula(sub) ) {
                                rowComplete = false;
                                break outer;
                            }
                        }
                    }
                }
            }
            if (rowComplete) {
                isGoodRow = true;
                break;
            }
        }
        if (!isGoodRow) {
            EditorAlerts.showSimpleTxtListAlert("Table:", Collections.singletonList(ParseUtilities.newRegularText("Short table requires at least one completed row.")));
            return false;
        }

        return true;
    }

    private boolean emptyRow(int r) {
        if (isTornDown || ttView == null || ttExercise == null) return true;

        TextField[][] tableFields = ttView.getTableFields();
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        boolean empty = true;
        if (baseFormulaCols != null) {
            for (int i = 0; i < baseFormulaCols.size(); i++) {
                if (tableFields[baseFormulaCols.get(i)][r] != null && !tableFields[baseFormulaCols.get(i)][r].getText().equals("")) {
                    empty = false;
                    break;
                }
            }
        }
        if (empty && mainFormulaSubLists != null) {
            mainLoop:
            for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                List<Expression> sublist = mainFormulaSubLists.get(j);
                for (int k = 0; k < sublist.size(); k++) {
                    Formula sub = (Formula) sublist.get(k);
                    if (tableFields[sub.getTtColumn()][r] != null && !tableFields[sub.getTtColumn()][r].getText().equals("")) {
                        empty = false;
                        break mainLoop;
                    }
                }
            }
        }
        return empty;
    }

    private boolean tableRowGood(int r) {
        if (isTornDown || ttView == null || ttExercise == null || tableKey == null) return false;

        TextField[][] tableFields = ttView.getTableFields();
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();

        for (int i = 0; i < tableRows; i++) {
            boolean rowGood = true;
            if (baseFormulaCols != null) {
                for (int j = 0; j < baseFormulaCols.size(); j++) {
                    int baseCol = baseFormulaCols.get(j);
                    if (tableFields[baseCol][r] != null && !tableFields[baseCol][r].getText().equals("") && !tableFields[baseCol][r].getText().equals(tableKey[baseCol][i])) {
                        rowGood = false;
                        break;
                    }
                }
            }
            if (rowGood && mainFormulaSubLists != null) {
                mainLoop:
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        int subCol = sub.getTtColumn();
                        if (tableFields[subCol][r] != null && !tableFields[subCol][r].getText().equals("") && !tableFields[subCol][r].getText().equals(tableKey[subCol][i])) {
                            rowGood = false;
                            break mainLoop;
                        }
                    }
                }
            }
            if (rowGood) return true;
        }
        return false;
    }

    private boolean checkTableValues() {
        if (isTornDown || ttExercise == null || ttView == null || tableKey == null || interpretations == null || mainFormulaSubLists == null) return false;

        TextField[][] tableFields = ttView.getTableFields();

        //check base
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        if (baseFormulaCols != null) {
            for (int j = 0; j < baseFormulaCols.size(); j++) {
                boolean goodColumn = true;
                for (int i = 0; i < interpretations.size(); i++) {
                    if (tableFields[baseFormulaCols.get(j)][i] != null && !tableKey[baseFormulaCols.get(j)][i].equalsIgnoreCase(tableFields[baseFormulaCols.get(j)][i].getText())) {
                        tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                        scrollToNode(tableFields[baseFormulaCols.get(j)][i]);
                        goodColumn = false;
                    }
                }
                if (!goodColumn) {
                    EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText("Problem(s) in column.")));
                    for (int i = 0 ; i < interpretations.size(); i++) {
                        if (ttView.getHighlightButtons() != null && ttView.getHighlightButtons()[baseFormulaCols.get(j)] != null && ttView.getHighlightButtons()[baseFormulaCols.get(j)].isSelected()) {
                            if (tableFields[baseFormulaCols.get(j)][i] != null)
                                tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        }
                        else
                        if (tableFields[baseFormulaCols.get(j)][i] != null)
                            tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                    }
                    return false;
                }
            }
        }

        //check main formulas
        for (int i = 0; i < mainFormulaSubLists.size(); i++) {       //formula lists
            List<Expression> subList = mainFormulaSubLists.get(i);
            for (int j = 0; j < subList.size(); j++) {                // list for formula
                Formula sub = (Formula) subList.get(j);
                boolean goodColumn = true;
                for (int k = 0 ; k < interpretations.size(); k++) {   //row
                    if (checkSetup != null && checkSetup.isSkipBasicsOK() && isBasic(sub) && !isMainFormula(sub) && tableFields[sub.getTtColumn()][k] != null && tableFields[sub.getTtColumn()][k].getText().equals("") ) {
                        continue;
                    }
                    else {
                        if (tableFields[sub.getTtColumn()][k] != null && !tableKey[sub.getTtColumn()][k].equalsIgnoreCase(tableFields[sub.getTtColumn()][k].getText())) {
                            tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                            scrollToNode(tableFields[sub.getTtColumn()][k]);
                            goodColumn = false;
                        }
                    }
                }
                if (!goodColumn) {
                    String message = "Problem(s) in column.";
                    if (!isMainFormula(sub)) message = "Problems(s) in column -- where values in this column may matter for ones that depend upon it.";
                    EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText(message)));
                    for (int m = 0 ; m < interpretations.size(); m++) {
                        if (ttView.getHighlightButtons() != null && ttView.getHighlightButtons()[sub.getTtColumn()] != null && ttView.getHighlightButtons()[sub.getTtColumn()].isSelected()) {
                            if (tableFields[sub.getTtColumn()][m] != null) tableFields[sub.getTtColumn()][m].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        }
                        else
                        if (tableFields[sub.getTtColumn()][m] != null) tableFields[sub.getTtColumn()][m].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isMainFormula(Formula formula) {
        if (isTornDown || mainFormulas == null) return false;
        boolean isMain = false;
        for (Formula mf : mainFormulas) {
            if (formula == mf) {
                isMain = true;
                break;
            }
        }
        return isMain;
    }

    private boolean equalsBasicFormula(Formula formula) {
        if (isTornDown || basicFormulas == null) return false;
        boolean equalsBasic = false;
        for (Formula bf : basicFormulas) {
            if (bf.equals(formula)) {
                equalsBasic = true;
                break;
            }
        }
        return equalsBasic;
    }

    private boolean checkRows() {
        if (isTornDown || ttExercise == null || ttView == null) return false;

        boolean good = true;
        if (tableRows != ttExercise.getTableRows()) {
            Background spinnerBackground = ttView.getRowsSpinner().getEditor().getBackground();
            Background buttonBackground = ttView.getSetupTableButton().getBackground();
            if (ttView.getRowsSpinner() != null && ttView.getRowsSpinner().getEditor() != null) {
                ttView.getRowsSpinner().getEditor().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(0), new Insets(0))));
            }
            if (ttView.getSetupTableButton() != null) {
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                scrollToNode(ttView.getSetupTableButton());
            }
            List<Text> textList = new ArrayList<>();
            textList.add(ParseUtilities.newRegularText("Full table with \ud835\udc5b basic sentences should have 2"));
            textList.add(ParseUtilities.newSuperscriptText("\ud835\udc5b"));
            textList.add(ParseUtilities.newRegularText(" rows."));
            EditorAlerts.showSimpleTxtListAlert("Table Setup: ", textList);

            ttView.getRowsSpinner().getEditor().setBackground(spinnerBackground);
            ttView.getSetupTableButton().setBackground(buttonBackground);

            good = false;
        }
        return good;
    }

    private boolean populateTableKey() {
        if (isTornDown || ttExercise == null || ttModel == null || interpretations == null) return false;

        //base columns
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        if (baseFormulaCols != null) {
            for (int i = 0; i < baseFormulaCols.size(); i++) {
                int columnNum = baseFormulaCols.get(i);
                for (int j = 0; j < interpretations.size(); j++) {
                    tableKey[columnNum][j] = interpretations.get(j)[i].getValue();
                }
            }
        }

        //populate main formula sub lists
        mainFormulaSubLists = new ArrayList<>();
        mainFormulas = new ArrayList<>();
        if (ttModel.getMainFormulas() != null) {
            for (int i = 0; i < ttModel.getMainFormulas().size(); i++) {
                subformulaColCounter = ttExercise.getMainFormulaCols().get(i);
                mainFormulaSubLists.add(new ArrayList<>());
                Document doc = ttModel.getMainFormulas().get(i);
                Formula formula = (Formula) ParseUtilities.parseDoc(doc, objLangName).get(0);
                mainFormulas.add(formula);
                populateFormulaSubList(formula, i);
                Collections.sort(mainFormulaSubLists.get(i), new SyntacticalFns.SortByLevel());
            }
        }

        //check all basic formulas included in setup
        for (int j = 0; j < mainFormulaSubLists.size(); j++) {
            List<Expression> sublist = mainFormulaSubLists.get(j);
            for (int m = 0; m < sublist.size(); m++) {
                Formula formula = (Formula) sublist.get(m);
                if (isBasic(formula)) {
                    boolean included = false;
                    if (basicFormulas != null) {
                        for (int n = 0; n < basicFormulas.length; n++) {
                            if (formula.equals(basicFormulas[n])) {
                                included = true;
                                break;
                            }
                        }
                    }
                    if (!included) {
                        List<Text> textList = new ArrayList<>();
                        textList.addAll(formula.toTextList());
                        textList.add(ParseUtilities.newRegularText(" is basic in the main table, but not included among base sentences in table setup."));
                        EditorAlerts.showSimpleTxtListAlert("Table Setup:", textList);
                        return false;
                    }
                }
            }
        }

        //set main table
        for (int i = 0; i < interpretations.size(); i++) {
            Pair<Formula, String>[] interpretation = interpretations.get(i);

            //process basic formulas
            for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                List<Expression> subList = mainFormulaSubLists.get(j);
                for (int m = 0; m < subList.size(); m++) {
                    Formula formula = (Formula) subList.get(m);
                    if (isBasic(formula)) {
                        for (int n = 0; n < interpretation.length; n++) {
                            if (formula.equals(interpretation[n].getKey())) {
                                formula.setSemanticValue(interpretation[n].getValue());
                                tableKey[formula.getTtColumn()][i] = interpretation[n].getValue();
                                break;
                            }
                        }
                    }
                }
            }

            //process non-basic formulas
            for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                List<Expression> subList = mainFormulaSubLists.get(j);
                for (int m = 0; m < subList.size(); m++) {
                    Formula formula = (Formula) subList.get(m);
                    if (!isBasic(formula)) {
                        Operator mainOp = formula.getMainOperator();
                        if (mainOp instanceof NegationOp) {
                            String value = truthFuncionValue(mainOp, ((Formula) formula.getChildren().get(0)).getSemanticValue());
                            formula.setSemanticValue(value);
                            tableKey[formula.getTtColumn()][i] = value;
                        }
                        else {
                            String value = truthFuncionValue(mainOp, ((Formula) formula.getChildren().get(0)).getSemanticValue(), ((Formula) formula.getChildren().get(1)).getSemanticValue());
                            formula.setSemanticValue(value);
                            tableKey[formula.getTtColumn()][i] = value;
                        }
                    }
                }
            }
        }

        return true;
    }

    public static String truthFuncionValue(Operator tfOperator, String... values) {
        String result = "XX";

        if (tfOperator instanceof NegationOp) {
            if (values[0].equals("T")) result = "F";
            else if (values[0].equals("F")) result = "T";
        }
        else if (tfOperator instanceof ConditionalOp) {
            if (values[0].equals("T") && values[1].equals("T")) result = "T";
            else if (values[0].equals("T") && values[1].equals("F")) result = "F";
            else if (values[0].equals("F") && values[1].equals("T")) result = "T";
            else if (values[0].equals("F") && values[1].equals("F")) result = "T";
        }
        else if (tfOperator instanceof ConjunctionOp) {
            if (values[0].equals("T") && values[1].equals("T")) result = "T";
            else if (values[0].equals("T") && values[1].equals("F")) result = "F";
            else if (values[0].equals("F") && values[1].equals("T")) result = "F";
            else if (values[0].equals("F") && values[1].equals("F")) result = "F";
        }
        else if (tfOperator instanceof DisjunctionOp) {
            if (values[0].equals("T") && values[1].equals("T")) result = "T";
            else if (values[0].equals("T") && values[1].equals("F")) result = "T";
            else if (values[0].equals("F") && values[1].equals("T")) result = "T";
            else if (values[0].equals("F") && values[1].equals("F")) result = "F";
        }
        else if (tfOperator instanceof BiconditionalOp) {
            if (values[0].equals("T") && values[1].equals("T")) result = "T";
            else if (values[0].equals("T") && values[1].equals("F")) result = "F";
            else if (values[0].equals("F") && values[1].equals("T")) result = "F";
            else if (values[0].equals("F") && values[1].equals("F")) result = "T";
        }
        else if (tfOperator instanceof NandOp) {
            if (values[0].equals("T") && values[1].equals("T")) result = "F";
            else if (values[0].equals("T") && values[1].equals("F")) result = "T";
            else if (values[0].equals("F") && values[1].equals("T")) result = "T";
            else if (values[0].equals("F") && values[1].equals("F")) result = "T";
        }
        else if (tfOperator instanceof NorOp) {
            if (values[0].equals("T") && values[1].equals("T")) result = "F";
            else if (values[0].equals("T") && values[1].equals("F")) result = "F";
            else if (values[0].equals("F") && values[1].equals("T")) result = "F";
            else if (values[0].equals("F") && values[1].equals("F")) result = "T";
        }
        else System.out.println("operator not recognized");
        return result;
    }

    private boolean isBasic(Expression exp) {
        if (isTornDown || exp == null) return false;
        boolean isBasic = false;
        if (exp.getChildren() == null || exp.getChildren().size() == 0) isBasic = true;
        else for (int i = 0; i < exp.getChildren().size(); i++) {
            if (!SyntacticalFns.sentence(exp.getChildren().get(i), objLangName)) {
                isBasic = true;
                break;
            }
        }
        return isBasic;
    }

    private void populateFormulaSubList(Expression exp, int index) {

        if (isTornDown || exp == null || !SyntacticalFns.sentence(exp, objLangName)) return;

        Expression childExp1 = null;
        if (exp.getChildren().size() >= 2) childExp1 = exp.getChildren().get(0);
        populateFormulaSubList(childExp1, index);

        ((Formula) exp).setTTColumn(subformulaColCounter);
        subformulaColCounter++;
        mainFormulaSubLists.get(index).add(exp);

        Expression childExp2 = null;
        if (exp.getChildren().size() == 1) childExp2 = exp.getChildren().get(0);
        if (exp.getChildren().size() >= 2) childExp2 = exp.getChildren().get(1);
        populateFormulaSubList(childExp2, index);
    }


    private void populateInterpretations() {
        if (isTornDown || basicDocs == null) return;
        int n = basicDocs.size();
        int keyRows = (int) Math.pow(2,n);
        tableKey = new String[tableColumns][keyRows];
        interpretations = new ArrayList<>();

        for (int i = 0; i < keyRows; i++) {
            Pair<Formula, String>[] assignment = new Pair[n];

            for (int j = 0; j < n; j++) {
                int mask = (int) Math.pow(2, j);
                if ((i & mask) == 0) {
                    assignment[n - j - 1] = new Pair(basicFormulas[n - j - 1], "T");
                }
                else assignment[n - j - 1] = new Pair(basicFormulas[n - j - 1], "F");
            }
            interpretations.add(assignment);
        }
    }



    private boolean populateBasicFormulas() {
        if (isTornDown || ttView == null || basicDocs == null) return false;

        boolean good = true;
        basicFormulas = new Formula[basicDocs.size()];
        for (int i = 0; i < basicFormulas.length; i++) {
            List<Expression> basicExps = ParseUtilities.parseDoc(basicDocs.get(i), objLangName);
            if (basicExps.size() ==  1 && basicExps.get(0) instanceof Formula) {
                Expression exp = (Expression) basicExps.get(0);
                if (SyntacticalFns.sentence(exp, objLangName)) {
                    basicFormulas[i] = (Formula) basicExps.get(0);
                }
                else {
                    good = false;
                    Background buttonBackground = ttView.getSetupTableButton().getBackground();
                    if (ttView.getBasicFormulasBoxedDRTAs() != null && ttView.getBasicFormulasBoxedDRTAs().size() > i) {
                        ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                        scrollToNode(ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA());
                    }
                    if (ttView.getSetupTableButton() != null) {
                        ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                        scrollToNode(ttView.getSetupTableButton());
                    }

                    EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Setup of basic expression not a sentence.")));

                    ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                    ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                    ttView.getSetupTableButton().setBackground(buttonBackground);

                    break;
                }
            }
            else {
                good = false;
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                if (ttView.getBasicFormulasBoxedDRTAs() != null && ttView.getBasicFormulasBoxedDRTAs().size() > i) {
                    ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                    scrollToNode(ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA());
                }
                if (ttView.getSetupTableButton() != null) {
                    ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                    scrollToNode(ttView.getSetupTableButton());
                }
                EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Setup of basic expression not a formula.")));

                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                ttView.getSetupTableButton().setBackground(buttonBackground);

                break;
            }
        }
        return good;
    }


    private boolean checkBasicOrder() {
        if (isTornDown || ttView == null || basicDocs == null) return false;

        AlphanumStringComparator comparator = new AlphanumStringComparator();
        boolean good = true;
        for (int i = 0; i + 1 < basicDocs.size(); i++) {
            if (comparator.compare(basicDocs.get(i).getText(), basicDocs.get(i + 1).getText()) >= 0) {
                good = false;
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                if (ttView.getBasicFormulasBoxedDRTAs() != null && ttView.getBasicFormulasBoxedDRTAs().size() > i + 1) {
                    ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                    ttView.getBasicFormulasBoxedDRTAs().get(i + 1).setVTtreeBoxHighlight();
                    scrollToNode(ttView.getBasicFormulasBoxedDRTAs().get(i + 1).getRTA());
                }
                if (ttView.getSetupTableButton() != null) {
                    ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                    scrollToNode(ttView.getSetupTableButton());
                }
                EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with basic formulas in alphanumeric order.")));

                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                ttView.getBasicFormulasBoxedDRTAs().get(i+1).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAs().get(i+1).getRTA().getStylesheets().add("RichTextFieldWide.css");
                ttView.getSetupTableButton().setBackground(buttonBackground);

                break;
            }
        }
        return good;
    }

    private boolean checkBasicDocs() {
        if (isTornDown || ttView == null || basicDocs == null) return false;

        boolean good = true;

        for (int i = 0; i < basicDocs.size(); i++) {

            Document basicDoc = basicDocs.get(i);
            if (basicDoc.getText().equals("")) {
                good = false;
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
           //     Border buttonBorder = ttView.getSetupTableButton().getBorder();
                if (ttView.getBasicFormulasBoxedDRTAs() != null && ttView.getBasicFormulasBoxedDRTAs().size() > i) {
                    ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                    scrollToNode(ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA());
                }
                if (ttView.getSetupTableButton() != null) {
                    ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                    scrollToNode(ttView.getSetupTableButton());
                }
                EditorAlerts.showSimpleTxtListAlert("Table Setup: ", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up and have no empty basic formula(s).")));

                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                ttView.getSetupTableButton().setBackground(buttonBackground);



                break;
            }
        }
        return good;
    }

    private boolean checkTableRows() {
        if (isTornDown || ttModel == null || ttView == null) return false;

        boolean good = ttModel.getTableRows() > 0;
        if (!good) {
            Background spinnerBackground = ttView.getRowsSpinner().getEditor().getBackground();
            Background buttonBackground = ttView.getSetupTableButton().getBackground();
            if (ttView.getRowsSpinner() != null && ttView.getRowsSpinner().getEditor() != null) {
                ttView.getRowsSpinner().getEditor().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(0), new Insets(0))));
            }
            if (ttView.getSetupTableButton() != null) {
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                scrollToNode(ttView.getSetupTableButton());
            }
            EditorAlerts.showSimpleTxtListAlert("Table Setup: ", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with more than 0 rows.")));
            ttView.getRowsSpinner().getEditor().setBackground(spinnerBackground);
            ttView.getSetupTableButton().setBackground(buttonBackground);

        }
        return good;
    }


    public int getCheckMax() {
        return checkMax;
    }

    public void setCheckMax(int checkMax) {
        this.checkMax = checkMax;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public void setCheckTries(int checkTries) {
        this.checkTries = checkTries;
    }

    public boolean isCheckFinal() {
        return checkFinal;
    }

    public void setCheckFinal(boolean checkFinal) {
        this.checkFinal = checkFinal;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        this.checkSuccess = checkSuccess;
    }
}