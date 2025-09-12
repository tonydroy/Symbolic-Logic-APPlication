package slapp.editor.truth_table;

import com.gluonhq.richtextarea.model.Document;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import slapp.editor.AlphanumStringComparator;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.SyntacticalFns;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.vertical_tree.VTcheckSetup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TTcheck {
    private TruthTableExercise ttExercise;
    private TruthTableView ttView;
    private TruthTableModel ttModel;
    private TTcheckSetup checkSetup;

    private String objLangName;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    private List<Document> basicDocs;
    List<Pair<Formula, Character>[]> interpretations;
    Formula[] basicFormulas;


    TTcheck(TruthTableExercise ttExercise) {
        this.ttExercise = ttExercise;

        this.ttExercise = ttExercise;
        this.ttView = ttExercise.getExerciseView();
        this.ttModel = ttExercise.getExerciseModel();


        checkSetup = ttModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new TTcheckSetup();
            ttModel.setCheckSetup(checkSetup);
        }

        objLangName = checkSetup.getObjLangName();


        setRightControlBox();
    }

    private void setRightControlBox() {
        ttView.getCheckButton().setOnAction(e -> {
            checkTable();
        });
    }

    public boolean checkTable() {
        basicDocs = ttModel.getBasicFormulas();

        boolean checkGood = true;

        if (!checkBasicDocs()) checkGood = false;
        else if (!populateBasicFormulas()) checkGood = false;
        else if (!checkBasicOrder()) checkGood = false;
        else if (!checkTableRows()) checkGood = false;
        else if (!checkGood) return false;

        populateInterpretations();

        System.out.println("check good: " + checkGood);
        return checkGood;
    }


    private void populateInterpretations() {
        int n = basicDocs.size();
        interpretations = new ArrayList<>();

        for (int i = 0; i < Math.pow(2, n); i++) {
            Pair<Formula, Character>[] assignment = new Pair[n];

            for (int j = 0; j < n; j++) {
                int mask = (int) Math.pow(2, j);
                if ((i & mask) == 0) {
                    assignment[n - j - 1] = new Pair(basicFormulas[n - j - 1], new Character('T'));
                }
                else assignment[n - j - 1] = new Pair(basicFormulas[n - j - 1], new Character('F'));
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
                    ttView.getBasicFormulasBoxedDRTAList().get(i).setVTtreeBoxHighlight();
                    Background buttonBackground = ttView.getSetupTableButton().getBackground();
                    ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                    EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Setup of basic expression not a sentence.")));
                    ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().clear();
                    ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                    ttView.getSetupTableButton().setBackground(buttonBackground);
                    break;
                }
            }
            else {
                good = false;
                ttView.getBasicFormulasBoxedDRTAList().get(i).setVTtreeBoxHighlight();
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Setup of basic expression not a formula.")));
                ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
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
                ttView.getBasicFormulasBoxedDRTAList().get(i).setVTtreeBoxHighlight();
                ttView.getBasicFormulasBoxedDRTAList().get(i + 1).setVTtreeBoxHighlight();
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                EditorAlerts.showSimpleTxtListAlert("Table Setup:", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with basic formulas in alphanumeric order.")));
                ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
                ttView.getBasicFormulasBoxedDRTAList().get(i+1).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAList().get(i+1).getRTA().getStylesheets().add("RichTextFieldWide.css");
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
                ttView.getBasicFormulasBoxedDRTAList().get(i).setVTtreeBoxHighlight();
                Background buttonBackground = ttView.getSetupTableButton().getBackground();
                Border buttonBorder = ttView.getSetupTableButton().getBorder();
                ttView.getSetupTableButton().setBackground(new Background(new BackgroundFill(Color.MISTYROSE, new CornerRadii(3.0), new Insets(0))));
                EditorAlerts.showSimpleTxtListAlert("Table Setup: ", Collections.singletonList(ParseUtilities.newRegularText("Table should be set up with no empty basic formula.")));
                ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().clear();
                ttView.getBasicFormulasBoxedDRTAList().get(i).getRTA().getStylesheets().add("RichTextFieldWide.css");
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
