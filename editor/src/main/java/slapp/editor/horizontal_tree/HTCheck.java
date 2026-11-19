package slapp.editor.horizontal_tree;

import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import slapp.editor.parser.MatchUtilities;


public class HTCheck {
    private HorizontalTreeExercise htExercise;
    private HorizontalTreeView htView;
    private HorizontalTreeModel htModel;
    private HTcheckSetup checkSetup;
    private String objLangName;

    private int checkMax;
    private int checkTries;
    private boolean checkSuccess;

    // KILL SWITCH: Prevents asynchronous listener execution during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    HTCheck(HorizontalTreeExercise htExercise) {
        this.htExercise = htExercise;
        this.htView = htExercise.getExerciseView();
        this.htModel = htExercise.getExerciseModel();

        checkSetup = htModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new HTcheckSetup();
            htModel.setCheckSetup(checkSetup);
        }
        objLangName = checkSetup.getObjLangName();

        checkSuccess = checkSetup.isCheckSuccess();

        htView.setCheckColor(Color.LAWNGREEN);
        htView.setCheckElementsColor(Color.GREEN);
        htView.setCheckMessage("Tree(s)");

        if (checkSuccess) htView.activateBigCheck();
        else htView.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();

        setRightControlBox();
    }

    private void scrollToNode(Node node) {
        if (isTornDown || htExercise == null || htExercise.getMainWindow() == null || htExercise.getMainWindow().getMainView() == null) return;
        htExercise.getMainWindow().getMainView().scrollToNode(node);
    }

    public boolean checkTrees() {
        if (isTornDown || htExercise == null || htView == null || htExercise.getMainWindow() == null) return false;

        checkSuccess = false;
        htView.deactivateBigCheck();
        if (!htExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            updateCheckCounter();
        }

        System.out.println("Checking trees");

        checkSuccess = true;
        htView.activateBigCheck();

        return true;
    }

    private void setRightControlBox() {
        if (isTornDown || htView == null || checkSetup == null) return;

        if (htView.getCheckButton() != null) {
            checkButtonHandler = e -> {
                if (isTornDown || htExercise == null || htView == null) return;
                checkTrees();
            };
            htView.getCheckButton().setOnAction(checkButtonHandler);
        }

        if (htView.getStaticHelpButton() != null) {
            htView.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

            staticHelpButtonHandler = e -> {
                if (isTornDown || htExercise == null || htView == null || htModel == null || htModel.getCheckSetup() == null) return;
                Stage helpStage = htView.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else htView.showStaticHelp(htModel.getCheckSetup().getStaticHelpDoc());
            };
            htView.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
        }

        if (htView.getPermitEmptyBranchesCheck() != null) {
            htView.getPermitEmptyBranchesCheck().setDisable(!checkSetup.isEmptyBranchesOK());
            htView.getPermitEmptyBranchesCheck().setSelected(checkSetup.isEmptyBranchesSelected());
        }
    }

    private void updateCheckCounter() {
        if (isTornDown || htExercise == null || htView == null || htExercise.getMainWindow() == null) return;

        if (checkMax != -1 && checkTries >= checkMax && !htExercise.getMainWindow().isInstructorFunctions()) {
            if (htView.getCheckButton() != null) htView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        if (htView.getCheckTriesLabel() != null) {
            htView.getCheckTriesLabel().setText(checkString);
        }
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 1: Engage kill switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Clear global static caches
        try { MatchUtilities.clearMatching(); } catch (Exception | Error e) {}

        try {
            if (htView != null) {
                if (htView.getCheckButton() != null) {
                    if (checkButtonHandler != null) htView.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                    htView.getCheckButton().setOnAction(null);
                }
                if (htView.getStaticHelpButton() != null) {
                    if (staticHelpButtonHandler != null) htView.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                    htView.getStaticHelpButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        // Nullify all fields safely
        try { this.htExercise = null; } catch (Exception e) {}
        try { this.htView = null; } catch (Exception e) {}
        try { this.htModel = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.objLangName = null; } catch (Exception e) {}
        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
    }

    public int getCheckMax() {
        return checkMax;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public HTcheckSetup getCheckSetup() {
        return checkSetup;
    }
}