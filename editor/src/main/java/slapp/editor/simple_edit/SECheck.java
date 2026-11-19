
package slapp.editor.simple_edit;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;


//static help stub
public class SECheck {
    SimpleEditExercise exercise;
    SimpleEditView view;
    SECheckSetup checkSetup;
    SimpleEditModel model;

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    SECheck(SimpleEditExercise exercise) {
        this.exercise = exercise;
        this.view = exercise.getExerciseView();
        this.model = exercise.getExerciseModel();

        checkSetup = model.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new SECheckSetup();
            model.setCheckSetup(checkSetup);
        }
        setRightControlBox();
    }

    private void setRightControlBox() {
        if (view == null || view.getStaticHelpButton() == null || checkSetup == null) return;

        view.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

        staticHelpButtonHandler = e -> {
            if (isTornDown || view == null) return;
            try {
                Stage helpStage = view.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else view.showStaticHelp(checkSetup.getStaticHelpDoc());
            } catch (Exception ex) {}
        };
        view.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
    }

    // SCORCHED EARTH RULE 1 & 5: Cleanup cascading and isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try {
            if (view != null && view.getStaticHelpButton() != null) {
                if (staticHelpButtonHandler != null) {
                    view.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                }
                view.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.exercise = null; } catch (Exception e) {}
        try { this.view = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.model = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
    }
}