package slapp.editor.abefg_explain;

import javafx.scene.paint.Color;
import javafx.stage.Stage;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.ParseUtilities;
import java.util.Collections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;


public class ABEFGCheck {

    ABEFGexercise exercise;
    ABEFGview view;
    ABEFGCheckSetup checkSetup;
    ABEFGmodel model;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;
    private boolean choiceSuccess;
    boolean choiceASet;
    boolean choiceBSet;
    boolean choiceESet;
    boolean choiceFSet;
    boolean choiceGSet;

    boolean choicesSet;
    boolean choiceAMade;
    boolean choiceBMade;
    boolean choiceEMade;
    boolean choiceFMade;
    boolean choiceGMade;
    boolean choiceABMade;
    boolean choiceEFGMade;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    ABEFGCheck(ABEFGexercise exercise) {
        this.exercise = exercise;
        this.view = exercise.getExerciseView();
        this.model = exercise.getExerciseModel();

        checkSetup = model.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new ABEFGCheckSetup();
            model.setCheckSetup(checkSetup);
        }

        choiceASet = checkSetup.isChoiceA();
        choiceBSet = checkSetup.isChoiceB();
        choiceESet = checkSetup.isChoiceE();
        choiceFSet = checkSetup.isChoiceF();
        choiceGSet = checkSetup.isChoiceG();
        choicesSet = (choiceASet || choiceBSet) && (choiceESet || choiceFSet || choiceGSet);

        checkSuccess = checkSetup.isCheckSuccess();

        view.setCheckColor(Color.ORCHID);
        view.setCheckElementsColor(Color.PURPLE);
        view.setCheckMessage(checkSetup.getCheckMessage());
        if (checkSuccess) view.activateBigCheck();
        else view.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        setChecksCounter();

        setRightControlBox();
    }

    private void setRightControlBox() {

        checkButtonHandler = e -> {
            if (isTornDown || view == null) return;
            view.setCheckColor(Color.ORCHID);
            view.setCheckMessage("Checkbox Choices");
            view.setCheckElementsColor(Color.PURPLE);
            runCheck();
        };
        view.getCheckButton().setOnAction(checkButtonHandler);

        view.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

        staticHelpButtonHandler = e -> {
            if (isTornDown || view == null || checkSetup == null) return;
            Stage helpStage = view.getStaticHelpStage();
            if (helpStage != null && helpStage.isShowing()) helpStage.close();
            else view.showStaticHelp(checkSetup.getStaticHelpDoc());
        };
        view.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
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
        view.jumpToPage0();
        if (!exercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        choiceAMade = exercise.getExerciseView().getCheckBoxA().isSelected();
        choiceBMade = exercise.getExerciseView().getCheckBoxB().isSelected();
        choiceABMade = choiceAMade || choiceBMade;

        choiceEMade = exercise.getExerciseView().getCheckBoxE().isSelected();
        choiceFMade = exercise.getExerciseView().getCheckBoxF().isSelected();
        choiceGMade = exercise.getExerciseView().getCheckBoxG().isSelected();

        choiceEFGMade = choiceEMade || choiceFMade || choiceGMade;

        if (!checkChoiceABMade()) return false;
        if (!checkChoiceEFGMade()) return false;

        if (!checkABChoice()) return false;
        if (!checkEFGChoice()) return false;

        checkSuccess = true;
        view.activateBigCheck();
        return true;
    }


    private boolean checkChoiceABMade() {
        boolean success = true;

        if (!choiceABMade) {
            view.getCheckBoxA().setStyle("-fx-background-color: mistyrose");
            view.getCheckBoxB().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));
            view.getCheckBoxA().setStyle("-fx-background-color: white");
            view.getCheckBoxB().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    private boolean checkChoiceEFGMade() {
        boolean success = true;

        if (!choiceEFGMade) {
            view.getCheckBoxE().setStyle("-fx-background-color: mistyrose");
            view.getCheckBoxF().setStyle("-fx-background-color: mistyrose");
            view.getCheckBoxG().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));
            view.getCheckBoxE().setStyle("-fx-background-color: white");
            view.getCheckBoxF().setStyle("-fx-background-color: white");
            view.getCheckBoxG().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    private boolean checkABChoice() {
        boolean success = true;

        if (choiceAMade && !choiceASet) {
            view.getCheckBoxA().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            view.getCheckBoxA().setStyle("-fx-background-color: white");
            success = false;
        }
        if (choiceBMade && !choiceBSet) {
            view.getCheckBoxB().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            view.getCheckBoxB().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    private boolean checkEFGChoice() {
        boolean success = true;

        if (choiceEMade && !choiceESet) {
            view.getCheckBoxE().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            view.getCheckBoxE().setStyle("-fx-background-color: white");
            success = false;
        }
        if (choiceFMade && !choiceFSet) {
            view.getCheckBoxF().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            view.getCheckBoxF().setStyle("-fx-background-color: white");
            success = false;
        }
        if (choiceGMade && !choiceGSet) {
            view.getCheckBoxG().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            view.getCheckBoxG().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation applied via dozens of try-catch blocks
    public void cleanup() {
        // KILL SWITCH: Engaged!
        isTornDown = true;

        // ---> 4. DISMANTLE THE UI TREE & UNHOOK LISTENERS <---
        try {
            if (view != null && view.getCheckButton() != null) {
                if (checkButtonHandler != null) {
                    view.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                }
                view.getCheckButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (view != null && view.getStaticHelpButton() != null) {
                if (staticHelpButtonHandler != null) {
                    view.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                }
                view.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { this.exercise = null; } catch (Exception e) {}
        try { this.view = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.model = null; } catch (Exception e) {}

        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
    }

    public boolean isCheckFinal() {
        return checkFinal;
    }

    public void setCheckFinal(boolean checkFinal) {
        this.checkFinal = checkFinal;
    }

    public int getCheckMax() {
        return checkMax;
    }

    public void setCheckMax(int checkMax) {
        this.checkMax = checkMax;
    }

    public ABEFGCheckSetup getCheckSetup() {
        return checkSetup;
    }

    public void setCheckSetup(ABEFGCheckSetup checkSetup) {
        this.checkSetup = checkSetup;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public void setCheckTries(int checkTries) {
        this.checkTries = checkTries;
    }

    public boolean isChoiceSuccess() {
        return choiceSuccess;
    }

    public void setChoiceSuccess(boolean choiceSuccess) {
        this.choiceSuccess = choiceSuccess;
    }
}