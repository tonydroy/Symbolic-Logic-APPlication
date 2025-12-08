package slapp.editor.horizontal_tree;

import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class HTCheck {
    private HorizontalTreeExercise htExercise;
    private HorizontalTreeView htView;
    private HorizontalTreeModel htModel;
    private HTcheckSetup checkSetup;
    private String objLangName;

    private int checkMax;
    private int checkTries;
    private boolean checkSuccess;


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

    public boolean checkTrees() {
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
        htView.getCheckButton().setOnAction(e -> {
           checkTrees();
        });

        htView.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

        htView.getStaticHelpButton().setOnAction(e -> {
            Stage helpStage = htView.getStaticHelpStage();
            if (helpStage != null && helpStage.isShowing()) helpStage.close();
            else htView.showStaticHelp(htModel.getCheckSetup().getStaticHelpDoc());
        });

        htView.getPermitEmptyBranchesCheck().setDisable(!checkSetup.isEmptyBranchesOK());
        htView.getPermitEmptyBranchesCheck().setSelected(checkSetup.isEmptyBranchesSelected());

    }



    private void updateCheckCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !htExercise.getMainWindow().isInstructorFunctions()) {
            htView.getCheckButton().setDisable(true);

        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        htView.getCheckTriesLabel().setText(checkString);
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
