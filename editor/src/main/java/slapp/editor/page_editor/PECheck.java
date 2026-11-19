package slapp.editor.page_editor;

import javafx.stage.Stage;




public class PECheck {

    PageEditExercise exercise;
    PageEditView view;
    PECheckSetup checkSetup;
    PageEditModel model;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private javafx.event.EventHandler<javafx.event.ActionEvent> staticHelpButtonHandler;

    PECheck(PageEditExercise exercise) {
        this.exercise = exercise;
        this.view = exercise.getExerciseView();
        this.model = exercise.getExerciseModel();

        checkSetup = model.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new PECheckSetup();
            model.setCheckSetup(checkSetup);
        }
        setRightControlBox();
    }

    private void setRightControlBox() {
        if (view == null || view.getStaticHelpButton() == null) return;

        view.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

        staticHelpButtonHandler = e -> {
            if (isTornDown || view == null || checkSetup == null) return;
            Stage helpStage = view.getStaticHelpStage();
            if (helpStage != null && helpStage.isShowing()) helpStage.close();
            else view.showStaticHelp(checkSetup.getStaticHelpDoc());
        };
        view.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation applied via dozens of try-catch blocks
    public void cleanup() {
        // KILL SWITCH: Engaged!
        isTornDown = true;

        // ---> 4. DISMANTLE THE UI TREE & UNHOOK LISTENERS <---
        try {
            if (view != null && view.getStaticHelpButton() != null) {
                if (staticHelpButtonHandler != null) {
                    view.getStaticHelpButton().removeEventHandler(javafx.event.ActionEvent.ACTION, staticHelpButtonHandler);
                }
                view.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { this.exercise = null; } catch (Exception e) {}
        try { this.view = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.model = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
    }
}
