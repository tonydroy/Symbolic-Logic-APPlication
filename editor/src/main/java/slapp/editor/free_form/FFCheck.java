
package slapp.editor.free_form;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

public class FFCheck {

    FreeFormExercise exercise;
    FreeFormView view;
    FFCheckSetup checkSetup;
    FreeFormModel model;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    FFCheck(FreeFormExercise exercise) {
        this.exercise = exercise;
        if (exercise != null) {
            this.view = exercise.getExerciseView();
            this.model = exercise.getExerciseModel();
        }

        if (model != null) {
            checkSetup = model.getCheckSetup();
            if (checkSetup == null) {
                checkSetup = new FFCheckSetup();
                model.setCheckSetup(checkSetup);
            }
        }
        setRightControlBox();
    }

    private void setRightControlBox() {
        if (isTornDown || view == null || checkSetup == null) return;

        if (view.getStaticHelpButton() != null) {
            view.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

            staticHelpButtonHandler = e -> {
                if (isTornDown || view == null || checkSetup == null) return;
                Stage helpStage = view.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) {
                    helpStage.close();
                } else {
                    view.showStaticHelp(checkSetup.getStaticHelpDoc());
                }
            };

            view.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
        }
    }

    public void cleanup() {
        // KILL SWITCH: Engaged!
        isTornDown = true;

        // SCORCHED EARTH RULE 3 & 5: Isolated try-catch blocks for removing listeners
        try {
            if (view != null && view.getStaticHelpButton() != null) {
                if (staticHelpButtonHandler != null) {
                    view.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                }
                view.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final nullifications
        try { this.exercise = null; } catch (Exception e) {}
        try { this.view = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.model = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
    }
}