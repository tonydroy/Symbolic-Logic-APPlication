package slapp.editor.truth_table_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
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
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.SyntacticalFns;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.truth_table.TTcheckSetup;
import slapp.editor.truth_table.TruthTableExercise;
import slapp.editor.truth_table.TruthTableModel;
import slapp.editor.truth_table.TruthTableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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


    TTExpCheck(TruthTableExpExercise ttExpExercise) {

        this.ttExercise = ttExpExercise;
        this.ttView = ttExercise.getExerciseView();
        this.ttModel = ttExercise.getExerciseModel();

        boxBackground = ttView.getaCheckBox().getBackground();
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

    private void setRightControlBox() {
        ttView.getCheckButton().setOnAction(e -> {
            checkTable();
        });

        ttView.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

        ttView.getStaticHelpButton().setOnAction(e -> {
            Stage helpStage = ttView.getStaticHelpStage();
            if (helpStage != null && helpStage.isShowing()) helpStage.close();
            else ttView.showStaticHelp(ttModel.getCheckSetup().getStaticHelpDoc());
        });

        ttView.getShortTableCheck().setDisable(!checkSetup.isPermitShortTable());
        ttView.getShortTableCheck().setSelected(checkSetup.isShortTable());

    }

    public boolean checkTable() {
        boolean checkGood = true;
        basicDocs = ttModel.getBasicFormulas();
        tableColumns = ttExercise.getTableColumns();
        tableRows = (int) Math.pow(2, basicDocs.size());
        checkSuccess = false;
        ttView.deactivateBigCheck();

        choiceAMade = ttExercise.getExerciseView().getaCheckBox().isSelected();
        choiceBMade = ttExercise.getExerciseView().getbCheckBox().isSelected();
        choiceMade = choiceAMade || choiceBMade;

        if (!ttExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        //check table
        if (!ttView.getShortTableCheck().isSelected()) {
            if (!checkFullTable()) checkGood = false;
        }
        else {
            if (!checkShortTable()) checkGood = false;
        }

        checkSuccess = false;
        //check choice
        if (checkGood) {
            if (checkSetup.isAutoCheckValidity()) {
                if (!checkChoiceMade()) checkGood = false;
                else if (!autoCheckValidity()) checkGood = false;

                if (checkGood) {
                    checkSuccess = true;
                    ttView.setCheckMessage("Table / Choice");
                }
            }
            else {
                if ((checkChoices || ttExercise.getMainWindow().isInstructorFunctions())  && choiceSet) {
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
        TextField[][] tableFields = ttView.getTableFields();
        boolean goodChoice = true;
        if (choiceAMade) {   //validity check
            if (ttView.getShortTableCheck().isSelected()) {
                ttView.getaCheckBox().setBackground(highlightBackground);
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("A short table is not sufficient to demonstrate validity.")));
                ttView.getaCheckBox().setBackground(boxBackground);

                goodChoice = false;
            }
            else {


                boolean badRow = false;
                for (int r = 0; r < tableRows; r++) {
                    if (premisesTrueConclusionFalse(r)) {
                        badRow = true;
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            tableFields[mainFormulas.get(j).getTtColumn()][r].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");
                        }
                    }
                }
                if (badRow) {
                    Background boxBackground = ttView.getaCheckBox().getBackground();
                    ttView.getaCheckBox().setBackground(highlightBackground);
                    EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Some row(s) have all the premises T and conclusion F.")));
                    ttView.getaCheckBox().setBackground(boxBackground);
                    for (int r = 0; r < tableRows; r++) {
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            int mfCol = mainFormulas.get(j).getTtColumn();
                            if (ttView.getHighlightButtons()[mfCol].isSelected()) {
                                tableFields[mfCol][r].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
                            } else {
                                tableFields[mfCol][r].setStyle("-fx-background-radius: 2; -fxBbackground-color: white; ");
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
            for (int r = 0; r < rowCommentsArray.length; r++) {
                RichTextArea rowRTA = rowCommentsArray[r].getRTA();
                if (rowRTA.isModified()) ttExercise.setExerciseModified(true);
                rowRTA.getActionFactory().saveNow().execute(new ActionEvent());
                String commentStr = rowRTA.getDocument().getText().strip();
                if (commentStr.startsWith("*") || commentStr.startsWith("\u2217") || commentStr.startsWith("\u22c6") || commentStr.startsWith("\u27f8") || commentStr.startsWith("\u21d0")) {
                    markedRows.add(r);
                }
            }
            if (markedRows.size() == 0) {
                ttView.getbCheckBox().setBackground(highlightBackground);
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Invalidity requires a marked row with the premises true and conclusion false (mark with asterisk, star, or a double back arrow).")));
                ttView.getbCheckBox().setBackground(boxBackground);
                goodChoice = false;
            }
            else {
                boolean badRow = false;
                for (int r = 0; r < markedRows.size(); r++) {
                    if (!premisesTrueConclusionFalse(markedRows.get(r))) {
                        badRow = true;
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            tableFields[mainFormulas.get(j).getTtColumn()][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");
                        }
                    }
                }
                if (badRow) {
                    ttView.getbCheckBox().setBackground(highlightBackground);
                    EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Some marked row(s) do not have all the premises T and conclusion F.")));
                    ttView.getbCheckBox().setBackground(boxBackground);
                    for (int r = 0; r < markedRows.size(); r++) {
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            int mfCol = mainFormulas.get(j).getTtColumn();
                            if (ttView.getHighlightButtons()[mfCol].isSelected()) {
                                tableFields[mfCol][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
                            } else {
                                tableFields[mfCol][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fxBbackground-color: white; ");
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
        TextField[][] tableFields = ttView.getTableFields();

        boolean conclusionF = tableFields[mainFormulas.get(mainFormulas.size() - 1).getTtColumn()][r].getText().equals("F");
        boolean premisesT = true;
        for (int j = 0; j + 1 < mainFormulas.size(); j++) {
            if (tableFields[mainFormulas.get(j).getTtColumn()][r].getText().equals("F")) {
                premisesT = false;
                break;
            }
        }
        return premisesT && conclusionF;
    }


    public boolean checkShortTable() {
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

    private void setChecksCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !ttExercise.getMainWindow().isInstructorFunctions()) {
            ttView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        ttView.getCheckTriesLabel().setText(checkString);

    }

    private boolean checkStaticChoice() {
        boolean success = true;

        if (choiceAMade && !choiceASet) {
            ttView.getaCheckBox().setBackground(highlightBackground);
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            ttView.getaCheckBox().setBackground(boxBackground);
            success = false;
        }
        if (choiceBMade && !choiceBSet) {
            ttView.getbCheckBox().setBackground(highlightBackground);
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            ttView.getbCheckBox().setBackground(boxBackground);
            success = false;
        }
        return success;
    }

    private boolean checkChoiceMade() {
        boolean success = true;

        if (!choiceMade) {
            ttView.getaCheckBox().setBackground(highlightBackground);
            ttView.getbCheckBox().setBackground(highlightBackground);
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));
            ttView.getaCheckBox().setBackground(boxBackground);
            ttView.getbCheckBox().setBackground(boxBackground);
            success = false;

        }
        return success;
    }

    private boolean checkHighlights() {
        ToggleButton[] highlightButtons = ttView.getHighlightButtons();
        boolean[] highlightValues = new boolean[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            if (highlightButtons[i] != null) {
                highlightValues[i] = highlightButtons[i].isSelected();
            }
        }
        TextField[][] tableFields = ttView.getTableFields();

        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        for (int i = 0; i < baseFormulaCols.size(); i++) {
            int baseCol = baseFormulaCols.get(i);

            if (highlightValues[baseFormulaCols.get(i)]) {
                if (highlightValues[baseCol]) {
                    for (int k = 0; k < ttExercise.getTableRows(); k++) {
                        tableFields[baseCol][k].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");  //mistyrose
                    }
                    EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight only columns in main table.")));
                    for (int k = 0; k < ttExercise.getTableRows(); k++) {
                        tableFields[baseCol][k].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
                    }
                    return false;
                }
            }
        }

        for (int i = 0; i < mainFormulaSubLists.size(); i++) {

            List<Expression> sublist = mainFormulaSubLists.get(i);
            for (int j = 0; j < sublist.size(); j++) {
                Formula sub = (Formula) sublist.get(j);
                if (highlightValues[sub.getTtColumn()] == true && !isMainFormula(sub)) {
                    for (int k = 0; k < ttExercise.getTableRows(); k++) {
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");  //mistyrose
                    }
                    EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight only column for value of the whole formula.")));
                    for (int k = 0; k < ttExercise.getTableRows(); k++) {
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
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
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");  //mistyrose
                    }
                    EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight column for value of the whole formula.")));
                    for (int k = 0; k < ttExercise.getTableRows(); k++) {
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-Bbackground-color: white");
                    }
                    return false;
                }
            }
        }

        return true;
    }

    private boolean checkShortTableValues() {
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
                for (int b = 0; b < baseFormulaCols.size(); b++) {
                    if (!tableFields[baseFormulaCols.get(b)][i].getText().equals("")) {
                        tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");
                    }
                }
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (!tableFields[sub.getTtColumn()][i].getText().equals("")) {
                            tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");
                        }
                    }
                }
                EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText("Problems in row: not all these values are included in any row of a completed table.")));
                for (int b = 0; b < baseFormulaCols.size(); b++) {
                    if (ttView.getHighlightButtons()[baseFormulaCols.get(b)].isSelected()) {
                        tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
                    } else {
                        tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fxBbackground-color: white; ");
                    }
                }
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (ttView.getHighlightButtons()[sub.getTtColumn()].isSelected()) {
                            tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
                        }
                        else {
                            tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fxBbackground-color: white; ");
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
            for (int b = 0; b < baseFormulaCols.size(); b++) {
                if (tableFields[baseFormulaCols.get(b)][i].getText().equals("")) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) {
                outer:
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (tableFields[sub.getTtColumn()][i].getText().equals("")) {
                            if (!checkSetup.isSkipBasicsOK() || !isBasic(sub) || isMainFormula(sub) ) {
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
        TextField[][] tableFields = ttView.getTableFields();
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        boolean empty = true;
        for (int i = 0; i < baseFormulaCols.size(); i++) {
            if (!tableFields[baseFormulaCols.get(i)][r].getText().equals("")) {
                empty = false;
                break;
            }
        }
        if (empty) {
            mainLoop:
            for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                List<Expression> sublist = mainFormulaSubLists.get(j);
                for (int k = 0; k < sublist.size(); k++) {
                    Formula sub = (Formula) sublist.get(k);
                    if (!tableFields[sub.getTtColumn()][r].getText().equals("")) {
                        empty = false;
                        break mainLoop;
                    }
                }
            }
        }
        return empty;
    }

    private boolean tableRowGood(int r) {
        TextField[][] tableFields = ttView.getTableFields();
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();

        for (int i = 0; i < tableRows; i++) {
            boolean rowGood = true;
            for (int j = 0; j < baseFormulaCols.size(); j++) {
                int baseCol = baseFormulaCols.get(j);
                if (!tableFields[baseCol][r].getText().equals("") && !tableFields[baseCol][r].getText().equals(tableKey[baseCol][i])) {
                    rowGood = false;
                    break;
                }
            }
            if (rowGood) {
                mainLoop:
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        int subCol = sub.getTtColumn();
                        if (!tableFields[subCol][r].getText().equals("") && !tableFields[subCol][r].getText().equals(tableKey[subCol][i])) {
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
        TextField[][] tableFields = ttView.getTableFields();

        //check base
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        for (int j = 0; j < baseFormulaCols.size(); j++) {
            boolean goodColumn = true;
            for (int i = 0; i < interpretations.size(); i++) {
                if (!tableKey[baseFormulaCols.get(j)][i].equalsIgnoreCase(tableFields[baseFormulaCols.get(j)][i].getText())) {
                    tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");  //mistyrose
                    goodColumn = false;
                }
            }
            if (!goodColumn) {
                EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText("Problem(s) in column.")));
                for (int i = 0 ; i < interpretations.size(); i++) {
                    if (ttView.getHighlightButtons()[baseFormulaCols.get(j)].isSelected()) {
                        tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
                    }
                    else
                        tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fxBbackground-color: white; ");
                }
                return false;
            }
        }

        //check main formulas
        for (int i = 0; i < mainFormulaSubLists.size(); i++) {       //formula lists
            List<Expression> subList = mainFormulaSubLists.get(i);
            for (int j = 0; j < subList.size(); j++) {                // list for formula
                Formula sub = (Formula) subList.get(j);
                boolean goodColumn = true;
                for (int k = 0 ; k < interpretations.size(); k++) {   //row
                    if (checkSetup.isSkipBasicsOK() && isBasic(sub) && !isMainFormula(sub) && tableFields[sub.getTtColumn()][k].getText().equals("") ) {
                        continue;
                    }
                    else {
                        if (!tableKey[sub.getTtColumn()][k].equalsIgnoreCase(tableFields[sub.getTtColumn()][k].getText())) {
                            tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-background-color: mistyrose; ");
                            goodColumn = false;
                        }
                    }
                }
                if (!goodColumn) {
                    String message = "Problem(s) in column.";
                    if (!isMainFormula(sub)) message = "Problems(s) in column -- where values in this column may matter for ones that depend upon it.";
                    EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText(message)));
                    for (int m = 0 ; m < interpretations.size(); m++) {
                        if (ttView.getHighlightButtons()[sub.getTtColumn()].isSelected()) {
                            tableFields[sub.getTtColumn()][m].setStyle("-fx-background-radius: 2; -fx-background-color: lightblue");
                        }
                        else
                            tableFields[sub.getTtColumn()][m].setStyle("-fx-background-radius: 2; -fxBbackground-color: white; ");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isMainFormula(Formula formula) {
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
        boolean good = true;
        if (tableRows != ttExercise.getTableRows()) {
            Background spinnerBackground = ttView.getRowsSpinner().getEditor().getBackground();
            ttView.getRowsSpinner().getEditor().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(0), new Insets(0))));
            Background buttonBackground = ttView.getSetupTableButton().getBackground();
            ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
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
        //base columns
        List<Integer> baseFormulaCols = ttExercise.getBasicFormulaCols();
        for (int i = 0; i < baseFormulaCols.size(); i++) {
            int columnNum = baseFormulaCols.get(i);
            for (int j = 0; j < interpretations.size(); j++) {
                tableKey[columnNum][j] = interpretations.get(j)[i].getValue();
            }
        }

        //populate main formula sub lists
        mainFormulaSubLists = new ArrayList<>();
        mainFormulas = new ArrayList<>();
        for (int i = 0; i < ttModel.getMainFormulas().size(); i++) {
            subformulaColCounter = ttExercise.getMainFormulaCols().get(i);
            mainFormulaSubLists.add(new ArrayList<>());
            Document doc = ttModel.getMainFormulas().get(i);
            Formula formula = (Formula) ParseUtilities.parseDoc(doc, objLangName).get(0);
            mainFormulas.add(formula);
            populateFormulaSubList(formula, i);
            Collections.sort(mainFormulaSubLists.get(i), new SyntacticalFns.SortByLevel());
        }

        //check all basic formulas included in setup
        for (int j = 0; j < mainFormulaSubLists.size(); j++) {
            List<Expression> sublist = mainFormulaSubLists.get(j);
            for (int m = 0; m < sublist.size(); m++) {
                Formula formula = (Formula) sublist.get(m);
                if (isBasic(formula)) {
                    boolean included = false;
                    for (int n = 0; n < basicFormulas.length; n++) {
                        if (formula.equals(basicFormulas[n])) {
                            included = true;
                            break;
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

/*
         //print main formula sub lists
        for (int i = 0; i < mainFormulaSubLists.size(); i++) {
            for (int j = 0; j < mainFormulaSubLists.get(i).size(); j++) {
                Formula form = (Formula) mainFormulaSubLists.get(i).get(j);

                System.out.print(form.getTtColumn() + ": " + mainFormulaSubLists.get(i).get(j) + ", ");
            }
            System.out.println();
        }
 */
/*
        //print table
        for (int i = 0; i < tableRows; i++) {
            for (int j = 0; j < tableColumns; j++) {
                System.out.print(tableKey[j][i] + " ");
            }
            System.out.println();
        }
 */

        return true;
    }

    private String truthFuncionValue(Operator tfOperator, String... values) {
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
            else if (values[0].equals("T") && values[1].equals("F")) result = "T";
            else if (values[0].equals("F") && values[1].equals("T")) result = "T";
            else if (values[0].equals("F") && values[1].equals("F")) result = "T";
        }
        else System.out.println("operator not recognized");
        return result;
    }

    private boolean isBasic(Expression exp) {
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

        if (exp == null || !SyntacticalFns.sentence(exp, objLangName)) return;

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
                    ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                    Background buttonBackground = ttView.getSetupTableButton().getBackground();
                    ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                    EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Setup of basic expression not a sentence.")));
                    ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                    ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                    ttView.getSetupTableButton().setBackground(buttonBackground);
                    break;
                }
            }
            else {
                good = false;
                ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
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
        AlphanumStringComparator comparator = new AlphanumStringComparator();
        boolean good = true;
        for (int i = 0; i + 1 < basicDocs.size(); i++) {
            if (comparator.compare(basicDocs.get(i).getText(), basicDocs.get(i + 1).getText()) >= 0) {
                good = false;
                ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                ttView.getBasicFormulasBoxedDRTAs().get(i + 1).setVTtreeBoxHighlight();
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
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
        boolean good = true;

        for (int i = 0; i < basicDocs.size(); i++) {
            Document basicDoc = basicDocs.get(i);
            if (basicDoc.getText().equals("")) {
                good = false;
                ttView.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                Border buttonBorder = ttView.getSetupTableButton().getBorder();
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                EditorAlerts.showSimpleTxtListAlert("Table Setup: ", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with no empty basic formula.")));
                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                ttView.getSetupTableButton().setBackground(buttonBackground);
                break;
            }
        }
        return good;
    }

    private boolean checkTableRows() {
        boolean good = ttModel.getTableRows() > 0;
        if (!good) {
            Background spinnerBackground = ttView.getRowsSpinner().getEditor().getBackground();
            ttView.getRowsSpinner().getEditor().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(0), new Insets(0))));
            Background buttonBackground = ttView.getSetupTableButton().getBackground();
            ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
            EditorAlerts.showSimpleTxtListAlert("Table Setup: ", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with more than 0 rows.")));
            ttView.getRowsSpinner().getEditor().setBackground(spinnerBackground);
            ttView.getSetupTableButton().setBackground(buttonBackground);
        }
        return good;
    }

    private void updateCheckCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !ttExercise.getMainWindow().isInstructorFunctions()) {
            ttView.getCheckButton().setDisable(true);
            ttView.getCheckProgButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        ttView.getCheckTriesLabel().setText(checkString);
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
