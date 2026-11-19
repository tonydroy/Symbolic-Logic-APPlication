package slapp.editor.truth_table_generate;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
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
import slapp.editor.ParaphraseService;
import slapp.editor.SentenceValidator;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.m_parse.MParseUtilities;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.MSentenceLetter;
import slapp.editor.parser.symbols.SentenceLetter;
import slapp.editor.trans_tree.InterpFnElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static slapp.editor.trans_tree.TransTreeCheck.*;
import static slapp.editor.truth_table_explain.TTExpCheck.truthFuncionValue;
import javafx.scene.Node;
import javafx.event.EventHandler;


public class TTGCheck {

    private static SentenceValidator validator = SentenceValidator.getInstance();
    TruthTableGenExercise exercise;
    TruthTableGenView view;
    TTGCheckSetup checkSetup;
    TruthTableGenModel model;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;
    private boolean choiceSuccess;

    private List<Document> basicDocs;
    private List<Pair<Formula, String>[]> interpretations;
    private Formula[] basicFormulas;
    private int tableColumns;
    private int tableRows;
    private String[][] tableKey;
    private List<List<Expression>> mainFormulaSubLists;
    private int subformulaColCounter;

    boolean choiceASet;
    boolean choiceBSet;
    boolean choiceSet;
    boolean choiceAMade;
    boolean choiceBMade;
    boolean choiceMade;
    Background boxBackground;
    Background highlightBackground;

    private String objLangName = "\u2112\ud835\udcc8 (w/abv)";
    private Language objLang = Languages.getLanguage("\u2112\ud835\udcc8 (w/abv)");
    private String mLangName = "Meta Sym";
    private Language mLang = Languages.getLanguage("Meta Sym");
    private static String processInterpLang = "";
    private MParseUtilities mParseUtilities = new MParseUtilities();
    private ParaphraseService paraphraseService;

    private RichTextArea interpFnRTA = null;
    private List<Formula> targetFormulas = new ArrayList<>();

    private List<InterpFnElement> interpFnElements;
    private List<InterpFnElement> keyInterpFnElements;
    private List<SentenceAtomic> sentenceAtomics = new ArrayList<>();
    private List<DecoratedRTA> mainFormulaDRTAs = new ArrayList<>();
    private List<Formula> mainFormulas = new ArrayList<>();

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    TTGCheck(TruthTableGenExercise exercise) {
        this.exercise = exercise;
        this.view = exercise.getExerciseView();
        this.model = exercise.getExerciseModel();

        checkSetup = model.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new TTGCheckSetup();
            model.setCheckSetup(checkSetup);
        }

        choiceASet = checkSetup.isChoiceA();
        choiceBSet = checkSetup.isChoiceB();
        choiceSet = choiceASet || choiceBSet;

        checkSuccess = checkSetup.isCheckSuccess();

        boxBackground = view.getaCheckBox().getBackground();
        highlightBackground = new Background(new BackgroundFill(Color.MISTYROSE, CornerRadii.EMPTY, Insets.EMPTY));

        view.setCheckColor(Color.LAWNGREEN);
        view.setCheckElementsColor(Color.GREEN);
        view.setCheckMessage(checkSetup.getCheckMessage());
        if (checkSuccess) view.activateBigCheck();
        else view.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();

        setChecksCounter();
        setRightControlBox();
    }

    private void scrollToNode(Node node) {
        if (isTornDown) return;
        try {
            if (exercise != null && exercise.getMainWindow() != null && exercise.getMainWindow().getMainView() != null) {
                exercise.getMainWindow().getMainView().scrollToNode(node);
            }
        } catch (Exception e) {}
    }

    private void setRightControlBox() {
        Button checkButton = view.getCheckButton();
        checkButton.setDisable(true);

        if (checkSetup.getCheckMax() == -1 || checkSetup.getCheckTries() < checkSetup.getCheckMax() || exercise.getMainWindow().isInstructorFunctions()) {


            if (!model.isCheckTrans()) {
                checkButton.setText("Check/Choice");
                checkButton.setDisable(false);

                checkButtonHandler = e -> {
                    if (isTornDown || exercise == null || view == null) return;
                    try {
                        view.setCheckColor(Color.LAWNGREEN);
                        view.setCheckElementsColor(Color.GREEN);
                        runCheck();
                    } catch (Exception ex) { }
                };
                view.getCheckButton().setOnAction(checkButtonHandler);
            }


            else {

                checkButton.setText("Loading Check");

                ParaphraseService.getInstance().thenAccept(service -> {
                    Platform.runLater(() -> {
                        // KILL SWITCH: Prevents crash if teardown fires before thread completes
                        if (isTornDown || exercise == null || view == null) return;
                        try {
                            checkButton.setDisable(false);
                            checkButton.setText("Check Exercise");
                        } catch (Exception ex) {
                        }
                    });
                }).exceptionally(ex -> {
                    Platform.runLater(() -> {
                        // KILL SWITCH: Prevents crash if teardown fires before thread completes
                        if (isTornDown || exercise == null || view == null) return;
                        try {
                            checkButton.setText("AI Error!");
                        } catch (Exception ex2) {
                        }
                    });
                    return null;
                });

                checkButtonHandler = e -> {
                    if (isTornDown || exercise == null || view == null) return;
                    try {
                        view.setCheckColor(Color.LAWNGREEN);
                        view.setCheckElementsColor(Color.GREEN);
                        runCheck();
                    } catch (Exception ex) {
                    }
                };
                view.getCheckButton().setOnAction(checkButtonHandler);
            }
        }

        view.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());
        staticHelpButtonHandler = e -> {
            if (isTornDown || exercise == null || view == null || checkSetup == null) return;
            try {
                Stage helpStage = view.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else view.showStaticHelp(checkSetup.getStaticHelpDoc());
            } catch (Exception ex) {}
        };
        view.getStaticHelpButton().setOnAction(staticHelpButtonHandler);

        view.getShortTableCheck().setDisable(!checkSetup.isPermitShortTable());
        view.getShortTableCheck().setSelected(checkSetup.isShortTable());
    }

    private void setChecksCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !exercise.getMainWindow().isInstructorFunctions()) {
            view.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        view.getCheckTriesLabel().setText(checkString);
    }

    public boolean runCheck() {
        checkSuccess = false;
        view.deactivateBigCheck();
        if (!exercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        if (model.isCheckTrans()) {
            //check translation
            mainFormulas.clear();

            for (BoxedDRTA bdrta : view.getMainFormulasBoxedDRTAs()) {
                mainFormulaDRTAs.add(bdrta.getDRTA());
            }

            interpFnRTA = view.getInterpretationDRTA().getEditor();
            interpFnRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document interpFnDoc = interpFnRTA.getDocument();
            interpFnElements = processInterpFn(interpFnDoc, objLang);
            if (interpFnElements == null) return false;

            ParaphraseService service = ParaphraseService.getInstance().join();
            if (!checkInterpFnForDuplicates(service)) return false;

            if (interpFnRTA.isEditable()) {
                if (!interpFnInKey(service)) return false;
            }

            if (!setTargetFormulas()) return false;

            for (BoxedDRTA bdrta : view.getMainFormulasBoxedDRTAs()) {
                try {
                    checkForFormula(bdrta);
                } catch (TextMessageException e) {
                    EditorAlerts.showSimpleTxtListAlert("Translation Expression:", e.getMessageList());
                    bdrta.resetVTtreeBoxHighlight();
                    return false;
                }
            }

            for (DecoratedRTA drta : mainFormulaDRTAs) {
                drta.getEditor().getActionFactory().saveNow().execute(new ActionEvent());
                Document formulaDoc = drta.getEditor().getDocument();
                if (!verifyInterpFnAdequacy(interpFnElements, formulaDoc, objLang, false)) return false;
            }

            if (targetFormulas.size() != mainFormulas.size()) {
                EditorAlerts.showSimpleTxtListAlert("Translation Issue:", Collections.singletonList(ParseUtilities.newRegularText("It doesn't look like you have exactly one formal translation for each sentence of the exercise.")));
            }

            for (int i = 0; i < targetFormulas.size(); i++) {
                if (!checkTarget(targetFormulas.get(i), mainFormulas.get(i), i)) return false;
            }
        }

        //check table

        basicDocs = model.getBasicFormulas();
        tableColumns = exercise.getTableColumns();
        tableRows = (int) Math.pow(2, basicDocs.size());

        if (!view.getShortTableCheck().isSelected()) {
            if (!checkFullTable()) return false;
        }
        else {
            if (!checkShortTable()) return false;
        }

        //choice

        choiceAMade = exercise.getExerciseView().getaCheckBox().isSelected();
        choiceBMade = exercise.getExerciseView().getbCheckBox().isSelected();
        choiceMade = choiceAMade || choiceBMade;

        if (checkSetup.isAutoCheckValidity()) {
            if (!checkChoiceMade()) return false;
            if (!autoCheckValidity()) return false;
            view.setCheckMessage("Table / Choice");
        }
        else if ((checkSetup.isCheckChoices() || exercise.getMainWindow().isInstructorFunctions()) && choiceSet) {
            if (!checkChoiceMade()) return false;
            if (!checkChoice()) return false;
            view.setCheckMessage("Table / Choice");
        }
        else {
            view.setCheckMessage("Table");
        }

        checkSuccess = true;
        view.activateBigCheck();
        return true;
    }

    public boolean checkShortTable() {
        if (!checkBasicDocs()) return false;
        if (!populateBasicFormulas()) return false;
        if (!checkBasicOrder()) return false;
        if (!checkTableRows()) return false;

        populateInterpretations();
        if (!populateTableKey()) return false;

        if (!checkShortTableValues()) return false;
        if (!checkHighlights()) return false;

        return true;
    }

    public boolean checkFullTable() {
        if (!checkBasicDocs()) return false;
        if (!populateBasicFormulas()) return false;
        if (!checkBasicOrder()) return false;
        if (!checkTableRows()) return false;

        populateInterpretations();  //initializes tableKey
        if (!populateTableKey()) return false;

        if (!checkRows()) return false;     //won't apply to short tables
        if (!checkTableValues()) return false;
        if (!checkHighlights()) return false;

        return true;
    }

    public boolean autoCheckValidity() {
        TextField[][] tableFields = view.getTableFields();
        boolean goodChoice = true;
        if (choiceAMade) {   //validity check
            if (view.getShortTableCheck().isSelected()) {
                view.getaCheckBox().setBackground(highlightBackground);
                scrollToNode(view.getaCheckBox());
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("A short table is not sufficient to demonstrate validity.")));
                view.getaCheckBox().setBackground(boxBackground);

                goodChoice = false;
            }
            else {
                boolean badRow = false;
                for (int r = 0; r < tableRows; r++) {
                    if (premisesTrueConclusionFalse(r)) {
                        badRow = true;
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            tableFields[mainFormulas.get(j).getTtColumn()][r].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                            scrollToNode(tableFields[mainFormulas.get(j).getTtColumn()][r]);
                        }
                    }
                }
                if (badRow) {
                    Background boxBackground = view.getaCheckBox().getBackground();
                    view.getaCheckBox().setBackground(highlightBackground);
                    scrollToNode(view.getaCheckBox());
                    EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Some row(s) have all the premises T and conclusion F.")));
                    view.getaCheckBox().setBackground(boxBackground);
                    for (int r = 0; r < tableRows; r++) {
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            int mfCol = mainFormulas.get(j).getTtColumn();
                            if (view.getHighlightButtons()[mfCol].isSelected()) {
                                tableFields[mfCol][r].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                            } else {
                                tableFields[mfCol][r].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                            }
                        }
                    }
                    goodChoice = false;
                }
            }
        }
        else {  //invalidity
            BoxedDRTA[] rowCommentsArray = view.getRowCommentsArray();
            List<Integer> markedRows = new ArrayList<>();
            for (int r = 0; r < rowCommentsArray.length; r++) {
                RichTextArea rowRTA = rowCommentsArray[r].getRTA();
                if (rowRTA.isModified()) exercise.setExerciseModified(true);
                rowRTA.getActionFactory().saveNow().execute(new ActionEvent());
                String commentStr = rowRTA.getDocument().getText().strip();
                if (commentStr.startsWith("*") || commentStr.startsWith("\u2217") || commentStr.startsWith("\u22c6") || commentStr.startsWith("\u27f8") || commentStr.startsWith("\u21d0")) {
                    markedRows.add(r);
                }
            }
            if (markedRows.size() == 0) {
                view.getbCheckBox().setBackground(highlightBackground);
                scrollToNode(view.getbCheckBox());
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Invalidity requires a marked row with the premises true and conclusion false (mark with asterisk, star, or a double back arrow).")));
                view.getbCheckBox().setBackground(boxBackground);
                goodChoice = false;
            }
            else {
                boolean badRow = false;
                for (int r = 0; r < markedRows.size(); r++) {
                    if (!premisesTrueConclusionFalse(markedRows.get(r))) {
                        badRow = true;
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            tableFields[mainFormulas.get(j).getTtColumn()][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                            scrollToNode(tableFields[mainFormulas.get(j).getTtColumn()][markedRows.get(r)]);
                        }
                    }
                }
                if (badRow) {
                    view.getbCheckBox().setBackground(highlightBackground);
                    scrollToNode(view.getbCheckBox());
                    EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Some marked row(s) do not have all the premises T and conclusion F.")));
                    view.getbCheckBox().setBackground(boxBackground);
                    for (int r = 0; r < markedRows.size(); r++) {
                        for (int j = 0; j < mainFormulas.size(); j++) {
                            int mfCol = mainFormulas.get(j).getTtColumn();
                            if (view.getHighlightButtons()[mfCol].isSelected()) {
                                tableFields[mfCol][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                            } else {
                                tableFields[mfCol][markedRows.get(r)].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
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
        TextField[][] tableFields = view.getTableFields();

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

    private boolean checkTableValues() {
        TextField[][] tableFields = view.getTableFields();

        //check base
        List<Integer> baseFormulaCols = exercise.getBasicFormulaCols();
        for (int j = 0; j < baseFormulaCols.size(); j++) {
            boolean goodColumn = true;
            for (int i = 0; i < interpretations.size(); i++) {
                TextField field = tableFields[baseFormulaCols.get(j)][i];
                if (!tableKey[baseFormulaCols.get(j)][i].equalsIgnoreCase(field.getText())) {
                    field.setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                    scrollToNode(field);
                    goodColumn = false;
                }
            }
            if (!goodColumn) {
                EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText("Problem(s) in column.")));
                for (int i = 0 ; i < interpretations.size(); i++) {
                    if (view.getHighlightButtons()[baseFormulaCols.get(j)].isSelected()) {
                        tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                    }
                    else
                        tableFields[baseFormulaCols.get(j)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
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
                        TextField field = tableFields[sub.getTtColumn()][k];
                        if (!tableKey[sub.getTtColumn()][k].equalsIgnoreCase(field.getText())) {
                            field.setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                            scrollToNode(field);
                            goodColumn = false;
                        }
                    }
                }
                if (!goodColumn) {
                    String message = "Problem(s) in column.";
                    if (!isMainFormula(sub)) message = "Problems(s) in column -- where values in this column may matter for ones that depend upon it.";
                    EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText(message)));
                    for (int m = 0 ; m < interpretations.size(); m++) {
                        if (view.getHighlightButtons()[sub.getTtColumn()].isSelected()) {
                            tableFields[sub.getTtColumn()][m].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        }
                        else
                            tableFields[sub.getTtColumn()][m].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkRows() {
        boolean good = true;
        if (tableRows != exercise.getTableRows()) {
            Background spinnerBackground = view.getRowsSpinner().getEditor().getBackground();
            view.getRowsSpinner().getEditor().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(0), new Insets(0))));
            Background buttonBackground = view.getSetupTableButton().getBackground();
            view.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
            scrollToNode(view.getSetupTableButton());

            List<Text> textList = new ArrayList<>();
            textList.add(ParseUtilities.newRegularText("Full table with \ud835\udc5b basic sentences should have 2"));
            textList.add(ParseUtilities.newSuperscriptText("\ud835\udc5b"));
            textList.add(ParseUtilities.newRegularText(" rows."));
            EditorAlerts.showSimpleTxtListAlert("Table Setup: ", textList);

            view.getRowsSpinner().getEditor().setBackground(spinnerBackground);
            view.getSetupTableButton().setBackground(buttonBackground);
            good = false;
        }
        return good;
    }

    private boolean checkHighlights() {
        ToggleButton[] highlightButtons = view.getHighlightButtons();
        boolean[] highlightValues = new boolean[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            if (highlightButtons[i] != null) {
                highlightValues[i] = highlightButtons[i].isSelected();
            }
        }
        TextField[][] tableFields = view.getTableFields();

        List<Integer> baseFormulaCols = exercise.getBasicFormulaCols();
        for (int i = 0; i < baseFormulaCols.size(); i++) {
            int baseCol = baseFormulaCols.get(i);

            if (highlightValues[baseFormulaCols.get(i)]) {
                if (highlightValues[baseCol]) {
                    for (int k = 0; k < exercise.getTableRows(); k++) {
                        tableFields[baseCol][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                        scrollToNode(tableFields[baseCol][k]);
                    }
                    EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight only columns in main table.")));
                    for (int k = 0; k < exercise.getTableRows(); k++) {
                        tableFields[baseCol][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
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
                    for (int k = 0; k < exercise.getTableRows(); k++) {
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                        scrollToNode(tableFields[sub.getTtColumn()][k]);
                    }
                    EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight only column for value of the whole formula.")));
                    for (int k = 0; k < exercise.getTableRows(); k++) {
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
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
                    for (int k = 0; k < exercise.getTableRows(); k++) {
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");  //mistyrose
                        scrollToNode(tableFields[sub.getTtColumn()][k]);
                    }
                    EditorAlerts.showSimpleTxtListAlert("Highlight:", Collections.singletonList(ParseUtilities.newRegularText("In completed table, highlight column for value of whole formula(s).")));
                    for (int k = 0; k < exercise.getTableRows(); k++) {
                        tableFields[sub.getTtColumn()][k].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white");
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

    private boolean checkShortTableValues() {
        TextField[][] tableFields = view.getTableFields();
        List<Integer> baseFormulaCols = exercise.getBasicFormulaCols();
        int rows = exercise.getTableRows();

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
                        tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                        scrollToNode(tableFields[baseFormulaCols.get(b)][i]);
                    }
                }
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (!tableFields[sub.getTtColumn()][i].getText().equals("")) {
                            tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: mistyrose; ");
                            scrollToNode(tableFields[sub.getTtColumn()][i]);
                        }
                    }
                }
                EditorAlerts.showSimpleTxtListAlert("Table Values:", Collections.singletonList(ParseUtilities.newRegularText("Problems in row: not all these values are included in any row of a completed table.")));
                for (int b = 0; b < baseFormulaCols.size(); b++) {
                    if (view.getHighlightButtons()[baseFormulaCols.get(b)].isSelected()) {
                        tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                    } else {
                        tableFields[baseFormulaCols.get(b)][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
                    }
                }
                for (int j = 0; j < mainFormulaSubLists.size(); j++) {
                    List<Expression> sublist = mainFormulaSubLists.get(j);
                    for (int k = 0; k < sublist.size(); k++) {
                        Formula sub = (Formula) sublist.get(k);
                        if (view.getHighlightButtons()[sub.getTtColumn()].isSelected()) {
                            tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                        }
                        else {
                            tableFields[sub.getTtColumn()][i].setStyle("-fx-background-radius: 2; -fx-control-inner-background: white; ");
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

    private boolean tableRowGood(int r) {
        TextField[][] tableFields = view.getTableFields();
        List<Integer> baseFormulaCols = exercise.getBasicFormulaCols();

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

    private boolean emptyRow(int r) {
        TextField[][] tableFields = view.getTableFields();
        List<Integer> baseFormulaCols = exercise.getBasicFormulaCols();
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

    private boolean populateTableKey() {
        //base columns
        List<Integer> baseFormulaCols = exercise.getBasicFormulaCols();
        for (int i = 0; i < baseFormulaCols.size(); i++) {
            int columnNum = baseFormulaCols.get(i);
            for (int j = 0; j < interpretations.size(); j++) {
                tableKey[columnNum][j] = interpretations.get(j)[i].getValue();
            }
        }

        //populate main formula sub lists
        mainFormulaSubLists = new ArrayList<>();
        mainFormulas = new ArrayList<>();
        for (int i = 0; i < model.getMainFormulas().size(); i++) {
            subformulaColCounter = exercise.getMainFormulaCols().get(i);
            mainFormulaSubLists.add(new ArrayList<>());
            Document doc = model.getMainFormulas().get(i);
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

        return true;
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

    private boolean checkTableRows() {
        boolean good = model.getTableRows() > 0;
        if (!good) {
            Background spinnerBackground = view.getRowsSpinner().getEditor().getBackground();
            view.getRowsSpinner().getEditor().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(0), new Insets(0))));
            Background buttonBackground = view.getSetupTableButton().getBackground();
            view.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
            scrollToNode(view.getSetupTableButton());
            EditorAlerts.showSimpleTxtListAlert("Table Setup: ", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with more than 0 rows.")));
            view.getRowsSpinner().getEditor().setBackground(spinnerBackground);
            view.getSetupTableButton().setBackground(buttonBackground);
        }
        return good;
    }

    private boolean checkBasicOrder() {
        AlphanumStringComparator comparator = new AlphanumStringComparator();
        boolean good = true;
        for (int i = 0; i + 1 < basicDocs.size(); i++) {
            if (comparator.compare(basicDocs.get(i).getText(), basicDocs.get(i + 1).getText()) >= 0) {
                good = false;
                view.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                view.getBasicFormulasBoxedDRTAs().get(i + 1).setVTtreeBoxHighlight();
                scrollToNode(view.getBasicFormulasBoxedDRTAs().get(i + 1).getRTA());
                Background buttonBackground = view.getSetupTableButton().getBackground();
                view.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                scrollToNode(view.getSetupTableButton());
                EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with basic formulas in alphanumeric order.")));
                view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                view.getBasicFormulasBoxedDRTAs().get(i+1).getRTA().getStylesheets().clear();
                view.getBasicFormulasBoxedDRTAs().get(i+1).getRTA().getStylesheets().add("RichTextFieldWide.css");
                view.getSetupTableButton().setBackground(buttonBackground);
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
                view.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                scrollToNode(view.getBasicFormulasBoxedDRTAs().get(i).getRTA());
                Background buttonBackground = view.getSetupTableButton().getBackground();
                Border buttonBorder = view.getSetupTableButton().getBorder();
                view.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                scrollToNode(view.getSetupTableButton());
                EditorAlerts.showSimpleTxtListAlert("Table Setup: ", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up and have no empty basic formula(s).")));
                view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                view.getSetupTableButton().setBackground(buttonBackground);
                break;
            }
        }
        return good;
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
                    view.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                    scrollToNode(view.getBasicFormulasBoxedDRTAs().get(i).getRTA());
                    Background buttonBackground = view.getSetupTableButton().getBackground();
                    view.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                    scrollToNode(view.getSetupTableButton());
                    EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Setup of basic expression not a sentence.")));
                    view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                    view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                    view.getSetupTableButton().setBackground(buttonBackground);
                    break;
                }
            }
            else {
                good = false;
                view.getBasicFormulasBoxedDRTAs().get(i).setVTtreeBoxHighlight();
                scrollToNode(view.getBasicFormulasBoxedDRTAs().get(i).getRTA());
                Background buttonBackground = view.getSetupTableButton().getBackground();
                view.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                scrollToNode(view.getSetupTableButton());
                EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Setup of basic expression not a formula.")));
                view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().clear();
                view.getBasicFormulasBoxedDRTAs().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                view.getSetupTableButton().setBackground(buttonBackground);
                break;
            }
        }
        return good;
    }

    private boolean checkTarget(Formula keyFormula, Formula transFormula, int index) {
        setSentenceAtomics(keyFormula, transFormula);
        if (!checkFormulaEquivalence(keyFormula, transFormula)) {
            BoxedDRTA bdrta = view.getMainFormulasBoxedDRTAs().get(index);
            bdrta.setVTtreeBoxHighlight();
            EditorAlerts.showSimpleTxtListAlert("Translation Issue:", Collections.singletonList(ParseUtilities.newRegularText("Not a good translation of sentence " + (index + 1) + ".")));
            bdrta.resetVTtreeBoxHighlight();
            return false;
        }
        return true;
    }

    private void setSentenceAtomics(Formula formula1, Formula formula2) {
        sentenceAtomics.clear();
        sentenceAtomicsHelper(formula1);
        sentenceAtomicsHelper(formula2);
    }

    private boolean checkFormulaEquivalence(Formula formula1, Formula formula2) {
        int numAtomics = sentenceAtomics.size();
        int numInterp = (int) Math.pow(2, numAtomics);

        //iterate through interpretations
        boolean equivalent = true;
        for (int i = 0; i < numInterp; i++) {

            //set interpretation i
            for (int j = 0; j < numAtomics; j++) {
                int mask = (int) Math.pow(2, j);
                if ((i & mask) == 0) {
                    sentenceAtomics.get(j).setSemanticValue("T");
                }
                else sentenceAtomics.get(j).setSemanticValue("F");
            }

            setFormulaValue(formula1, sentenceAtomics);
            setFormulaValue(formula2, sentenceAtomics);

            if (!formula1.getSemanticValue().equals(formula2.getSemanticValue())) {
                equivalent = false;
                break;
            }
        }
        return equivalent;
    }

    private void checkForFormula(BoxedDRTA bdrta) throws TextMessageException {
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        List<Expression> parseList = ParseUtilities.parseDoc(doc, objLang);

        if (parseList.isEmpty()) {
            bdrta.setVTtreeBoxHighlight();
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Empty node.")));
        }
        else if (parseList.size() > 1 || !(parseList.get(0) instanceof Formula)) {
            bdrta.setVTtreeBoxHighlight();
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Not a formula of " + objLangName)));
        }
        else mainFormulas.add((Formula) parseList.get(0));
    }

    private boolean setTargetFormulas() {
        targetFormulas.clear();
        List<Document> targetDocs = checkSetup.getKeyFormulas();
        for (Document targetDoc : targetDocs) {

            if (!interpFnRTA.isEditable()) {
                List<Expression> parseList = ParseUtilities.parseDoc(targetDoc, objLang);
                Formula targetFormula = (Formula) parseList.get(0);
                targetFormulas.add(targetFormula);
            }
            else {
                List<Expression> parseList = ParseUtilities.parseDoc(targetDoc, mLang);
                Formula targetFormula = (Formula) parseList.get(0);

                sentenceAtomics.clear();
                sentenceAtomicsHelper(targetFormula);
                for (SentenceAtomic atomic : sentenceAtomics) {
                    if (atomic.getMainLetter().getMatch() == null) {
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("The interpretation function does not assign a value to all the sentences required for this exercise.")));
                        return false;
                    }
                }
                targetFormulas.add(targetFormula.getMatch());
            }
        }
        return true;
    }

    private void sentenceAtomicsHelper(Formula formula) {
        if (formula.isAtomic()) {
            if (!sentenceAtomics.contains(formula)) sentenceAtomics.add((SentenceAtomic) formula);
            return;
        }
        for (Expression childExp : formula.getChildren()) {
            if (!(childExp instanceof Formula)) return;
            else {
                Formula childForm = (Formula) childExp;
                sentenceAtomicsHelper(childForm);
            }
        }
    }

    private boolean interpFnInKey(ParaphraseService service) {
        Document keyInterpFnDoc = checkSetup.getKeyInterpFnDoc();
        keyInterpFnElements = processInterpFn(keyInterpFnDoc, mLang);

        MatchUtilities.clearMatching();
        for (InterpFnElement objElement : interpFnElements ) {
            boolean found = false;
            String objCleanStr = cleanString(objElement.getAssignmentString());
            for (InterpFnElement keyElement : keyInterpFnElements) {
                String keyCleanStr = cleanString(keyElement.getAssignmentString());

                if (service.getSimilarityScore(objCleanStr, keyCleanStr) > .6) {
                    found = true;

                    //set matching
                    MSentenceLetter keyLetter = (MSentenceLetter) keyElement.getSymbol();
                    SentenceLetter objLetter = (SentenceLetter) objElement.getSymbol();
                    try {
                        keyLetter.setMatch(objLetter);
                    }
                    catch (TextMessageException e) {
                        System.out.println("matching error");
                    }
                    break;
                }
            }
            if (!found) {
                EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("I do not recognize '" + objElement.getAssignmentString() + "' like it) as a proper member of the interpretation function for this exercise.")));
                return false;
            }
        }
        return true;
    }

    public boolean checkInterpFnForDuplicates(ParaphraseService service) {
        for (int i = 0; i < interpFnElements.size(); i++ ) {
            InterpFnElement mainElement = interpFnElements.get(i);
            for (int j = i + 1; j < interpFnElements.size(); j++) {
                InterpFnElement testElement = interpFnElements.get(j);

                if (service.getSimilarityScore(mainElement.getAssignmentString(), testElement.getAssignmentString()) > .6) {
                    EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("Sentence '" + mainElement.getAssignmentString() + "' has duplicate (or near duplicate) entries in the interpretation function.")));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkChoiceMade() {
        boolean success = true;

        if (!choiceMade) {
            view.getaCheckBox().setBackground(highlightBackground);
            view.getbCheckBox().setBackground(highlightBackground);
            scrollToNode(view.getaCheckBox());
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));
            view.getaCheckBox().setBackground(boxBackground);
            view.getbCheckBox().setBackground(boxBackground);
            success = false;
        }
        return success;
    }

    private boolean checkChoice() {
        boolean success = true;

        if (choiceAMade && !choiceASet) {
            view.getaCheckBox().setBackground(highlightBackground);
            scrollToNode(view.getaCheckBox());
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            view.getaCheckBox().setBackground(boxBackground);
            success = false;
        }
        if (choiceBMade && !choiceBSet) {
            view.getbCheckBox().setBackground(highlightBackground);
            scrollToNode(view.getbCheckBox());
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            view.getbCheckBox().setBackground(boxBackground);
            success = false;
        }
        return success;
    }

    public void cleanup() {
        // KILL SWITCH
        isTornDown = true;

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try {
            if (view != null) {
                if (view.getCheckButton() != null && checkButtonHandler != null) {
                    view.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                    view.getCheckButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        try {
            if (view != null) {
                if (view.getStaticHelpButton() != null && staticHelpButtonHandler != null) {
                    view.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                    view.getStaticHelpButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 7: Clear Global Static Caches
        try {
            MatchUtilities.clearMatching();
        } catch (Exception e) {}

        try {
            if (interpretations != null) {
                for (int i = 0; i < interpretations.size(); i++) {
                    Pair<Formula, String>[] array = interpretations.get(i);
                    if (array != null) {
                        for (int j = 0; j < array.length; j++) array[j] = null;
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (basicFormulas != null) {
                for (int i = 0; i < basicFormulas.length; i++) basicFormulas[i] = null;
            }
        } catch (Exception e) {}

        try {
            if (tableKey != null) {
                for (int i = 0; i < tableKey.length; i++) {
                    if (tableKey[i] != null) {
                        for (int j = 0; j < tableKey[i].length; j++) tableKey[i][j] = null;
                    }
                    tableKey[i] = null;
                }
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown.
        // DO NOT call .clear() on lists, especially model data like basicDocs.
        try { this.basicDocs = null; } catch (Exception e) {}
        try { this.interpretations = null; } catch (Exception e) {}
        try { this.basicFormulas = null; } catch (Exception e) {}
        try { this.tableKey = null; } catch (Exception e) {}
        try { this.mainFormulaSubLists = null; } catch (Exception e) {}
        try { this.targetFormulas = null; } catch (Exception e) {}
        try { this.interpFnElements = null; } catch (Exception e) {}
        try { this.keyInterpFnElements = null; } catch (Exception e) {}
        try { this.sentenceAtomics = null; } catch (Exception e) {}
        try { this.mainFormulaDRTAs = null; } catch (Exception e) {}
        try { this.mainFormulas = null; } catch (Exception e) {}

        this.exercise = null;
        this.view = null;
        this.checkSetup = null;
        this.model = null;
        this.boxBackground = null;
        this.highlightBackground = null;
        this.objLang = null;
        this.mLang = null;
        this.mParseUtilities = null;
        this.paraphraseService = null;
        this.interpFnRTA = null;
        this.checkButtonHandler = null;
        this.staticHelpButtonHandler = null;
    }

    public boolean isCheckFinal() { return checkFinal; }

    public void setCheckFinal(boolean checkFinal) { this.checkFinal = checkFinal; }

    public int getCheckMax() { return checkMax; }

    public void setCheckMax(int checkMax) { this.checkMax = checkMax; }

    public TTGCheckSetup getCheckSetup() { return checkSetup; }

    public void setCheckSetup(TTGCheckSetup checkSetup) { this.checkSetup = checkSetup; }

    public boolean isCheckSuccess() { return checkSuccess; }

    public void setCheckSuccess(boolean checkSuccess) { this.checkSuccess = checkSuccess; }

    public int getCheckTries() { return checkTries; }

    public void setCheckTries(int checkTries) { this.checkTries = checkTries; }

    public boolean isChoiceSuccess() { return choiceSuccess; }

    public void setChoiceSuccess(boolean choiceSuccess) { this.choiceSuccess = choiceSuccess; }
}