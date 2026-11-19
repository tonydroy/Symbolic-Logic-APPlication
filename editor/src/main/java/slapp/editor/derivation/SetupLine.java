/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.  If not, see
<https://www.gnu.org/licenses/>.
 */

package slapp.editor.derivation;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.main_window.ControlType;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;


/**
 * Adjunct to derivation create, for setup of a single derivation line.
 */
public class SetupLine {
    private DerivationCreate derivationCreate;
    private BoxedDRTA formulaBoxedDRTA;
    private BoxedDRTA justificationBoxedDRTA;
    private Spinner<Integer> depthSpinner;
    private HBox spinnerBox;
    private CheckBox premiseBox;
    private CheckBox conclusionBox;
    private CheckBox addShelfBox;
    private CheckBox addGapBox;
    private boolean modified = false;
    private double formulaBoxHeight = 27;

    // SCORCHED EARTH RULE 2: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<KeyEvent> formulaRtaKeyFilter;
    private ChangeListener<Boolean> formulaRtaFocusListener;
    private EventHandler<KeyEvent> justificationRtaKeyFilter;
    private ChangeListener<Boolean> justificationRtaFocusListener;
    private ChangeListener<Integer> depthSpinnerListener;
    private ChangeListener<Boolean> premiseBoxListener;
    private ChangeListener<Boolean> conclusionBoxListener;
    private ChangeListener<Boolean> addShelfBoxListener;
    private ChangeListener<Boolean> addGapBoxListener;

    /**
     * Construct setup line
     * @param derivationCreate the controlling create window
     */


    public SetupLine(DerivationCreate derivationCreate) {
        this.derivationCreate = derivationCreate;
        formulaBoxedDRTA = new BoxedDRTA();
        if (derivationCreate != null && derivationCreate.getKeyboardSelector() != null) {
            formulaBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(derivationCreate.getKeyboardSelector());
        }
        RichTextArea formulaRTA = formulaBoxedDRTA.getRTA();

        formulaRTA.setMaxHeight(formulaBoxHeight);
        formulaRTA.setMinHeight(formulaBoxHeight);
        formulaRTA.setPrefWidth(400);
        formulaRTA.setContentAreaWidth(500);
        formulaRTA.getStylesheets().add("RichTextFieldWide.css");
        formulaRTA.setPromptText("Formula");

        formulaRtaKeyFilter = e -> {
            if (isTornDown) return;
            KeyCode code = e.getCode();
            try {
                if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                    if (justificationBoxedDRTA != null && justificationBoxedDRTA.getRTA() != null) {
                        justificationBoxedDRTA.getRTA().requestFocus();
                    }
                    e.consume();
                }
            } catch (Exception ex) {}
        };
        formulaRTA.addEventFilter(KeyEvent.KEY_PRESSED, formulaRtaKeyFilter);

        formulaRtaFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.derivationCreate == null || formulaBoxedDRTA == null || formulaBoxedDRTA.getDRTA() == null) return;
            try {
                if (nv) {
                    this.derivationCreate.editorInFocus(formulaBoxedDRTA.getDRTA(), ControlType.FIELD);
                }
            } catch (Exception ex) {}
        };
        formulaRTA.focusedProperty().addListener(formulaRtaFocusListener);

        justificationBoxedDRTA = new BoxedDRTA();
        justificationBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE);
        RichTextArea justificationRTA = justificationBoxedDRTA.getRTA();
        justificationRTA.getActionFactory().open(new Document("")).execute(new ActionEvent());
        justificationRTA.setMaxHeight(formulaBoxHeight);
        justificationRTA.setMinHeight(formulaBoxHeight);
        justificationRTA.setPrefWidth(100);
        justificationRTA.setContentAreaWidth(200);
        justificationRTA.getStylesheets().add("RichTextFieldWide.css");
        justificationRTA.setPromptText("Justification");

        justificationRtaKeyFilter = e -> {
            if (isTornDown) return;
            KeyCode code = e.getCode();
            try {
                if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                    if (depthSpinner != null) {
                        depthSpinner.requestFocus();
                    }
                    e.consume();
                }
                else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                    if (formulaRTA != null) {
                        formulaRTA.requestFocus();
                    }
                    e.consume();
                }
            } catch (Exception ex) {}
        };
        justificationRTA.addEventFilter(KeyEvent.KEY_PRESSED, justificationRtaKeyFilter);

        justificationRtaFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.derivationCreate == null || justificationBoxedDRTA == null || justificationBoxedDRTA.getDRTA() == null) return;
            try {
                if (nv) {
                    this.derivationCreate.editorInFocus(justificationBoxedDRTA.getDRTA(), ControlType.STATEMENT);
                }
            } catch (Exception ex) {}
        };
        justificationRTA.focusedProperty().addListener(justificationRtaFocusListener);

        depthSpinner = new Spinner<>(1,19,1);
        depthSpinner.setPrefWidth(55);
        depthSpinnerListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            modified = true;
        };
        depthSpinner.valueProperty().addListener(depthSpinnerListener);

        spinnerBox = new HBox(10, new Label("Depth"), depthSpinner);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);

        premiseBox = new CheckBox("Premise");
        premiseBox.setSelected(false);
        premiseBoxListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            try {
                if (nv) {
                    if (conclusionBox != null) conclusionBox.setSelected(false);
                    if (addGapBox != null) addGapBox.setSelected(false);
                }
                modified = true;
            } catch (Exception ex) {}
        };
        premiseBox.selectedProperty().addListener(premiseBoxListener);

        conclusionBox = new CheckBox("Conclusion");
        conclusionBox.setSelected(false);
        conclusionBoxListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            try {
                if (nv) {
                    if (premiseBox != null) premiseBox.setSelected(false);
                    if (addShelfBox != null) addShelfBox.setSelected(false);
                    if (addGapBox != null) addGapBox.setSelected(false);
                }
                modified = true;
            } catch (Exception ex) {}
        };
        conclusionBox.selectedProperty().addListener(conclusionBoxListener);

        addShelfBox = new CheckBox("Add shelf");
        addShelfBox.setSelected(false);
        addShelfBoxListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            try {
                if (nv) {
                    if (addGapBox != null) addGapBox.setSelected(false);
                }
                modified = true;
            } catch (Exception ex) {}
        };
        addShelfBox.selectedProperty().addListener(addShelfBoxListener);

        addGapBox = new CheckBox("Add gap");
        addGapBox.setSelected(false);
        addGapBoxListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            try {
                if (nv) {
                    if (addShelfBox != null) addShelfBox.setSelected(false);
                }
                modified = true;
            } catch (Exception ex) {}
        };
        addGapBox.selectedProperty().addListener(addGapBoxListener);
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper
    private void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                else if (node.getParent() instanceof javafx.scene.Group) ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
            }
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 1: Cascading Cleanup
    public void cleanup() {
        // KILL SWITCH: Engaged
        isTornDown = true;

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try {
            if (formulaBoxedDRTA != null && formulaBoxedDRTA.getRTA() != null) {
                if (formulaRtaKeyFilter != null) formulaBoxedDRTA.getRTA().removeEventFilter(KeyEvent.KEY_PRESSED, formulaRtaKeyFilter);
                if (formulaRtaFocusListener != null) formulaBoxedDRTA.getRTA().focusedProperty().removeListener(formulaRtaFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (justificationBoxedDRTA != null && justificationBoxedDRTA.getRTA() != null) {
                if (justificationRtaKeyFilter != null) justificationBoxedDRTA.getRTA().removeEventFilter(KeyEvent.KEY_PRESSED, justificationRtaKeyFilter);
                if (justificationRtaFocusListener != null) justificationBoxedDRTA.getRTA().focusedProperty().removeListener(justificationRtaFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (depthSpinner != null && depthSpinner.valueProperty() != null && depthSpinnerListener != null) {
                depthSpinner.valueProperty().removeListener(depthSpinnerListener);
            }
        } catch (Exception e) {}

        try {
            if (premiseBox != null && premiseBox.selectedProperty() != null && premiseBoxListener != null) {
                premiseBox.selectedProperty().removeListener(premiseBoxListener);
            }
        } catch (Exception e) {}

        try {
            if (conclusionBox != null && conclusionBox.selectedProperty() != null && conclusionBoxListener != null) {
                conclusionBox.selectedProperty().removeListener(conclusionBoxListener);
            }
        } catch (Exception e) {}

        try {
            if (addShelfBox != null && addShelfBox.selectedProperty() != null && addShelfBoxListener != null) {
                addShelfBox.selectedProperty().removeListener(addShelfBoxListener);
            }
        } catch (Exception e) {}

        try {
            if (addGapBox != null && addGapBox.selectedProperty() != null && addGapBoxListener != null) {
                addGapBox.selectedProperty().removeListener(addGapBoxListener);
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea formulaRTA = (formulaBoxedDRTA != null) ? formulaBoxedDRTA.getRTA() : null;
            final RichTextArea justificationRTA = (justificationBoxedDRTA != null) ? justificationBoxedDRTA.getRTA() : null;

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (formulaRTA != null) rtasToClear.add(formulaRTA);
            if (justificationRTA != null) rtasToClear.add(justificationRTA);

            if (!rtasToClear.isEmpty()) {
                // STAGE 1 (Immediate): Prompt wipe, focus disable, and document injection
                for (RichTextArea rta : rtasToClear) {
                    if (rta != null) {
                        try { rta.setPromptText(""); } catch (Exception e) {}
                        try { rta.setFocusTraversable(false); } catch (Exception e) {}
                        try {
                            if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                                rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                            }
                        } catch (Exception e) {}
                    }
                }

                // STAGE 2 (Delayed Skin Removal): Push skin removal 1500ms into the future
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    for (RichTextArea rta : rtasToClear) {
                        try {
                            if (rta != null) {
                                rta.setSkin(null);
                            }
                        } catch (Exception ex) {}
                    }
                });
                pt.play();
            }
        } catch (Exception e) {}

        try {
            if (formulaBoxedDRTA != null) {
                severFromParent(formulaBoxedDRTA.getRTA()); // Explicitly sever inner RTA first
                try { formulaBoxedDRTA.cleanup(); } catch (Exception e) {}
                severFromParent(formulaBoxedDRTA.getBoxedRTA());
            }
        } catch (Exception e) {}

        try {
            if (justificationBoxedDRTA != null) {
                severFromParent(justificationBoxedDRTA.getRTA()); // Explicitly sever inner RTA first
                try { justificationBoxedDRTA.cleanup(); } catch (Exception e) {}
                severFromParent(justificationBoxedDRTA.getBoxedRTA());
            }
        } catch (Exception e) {}

        try { if (depthSpinner != null) severFromParent(depthSpinner); } catch (Exception e) {}
        try { if (spinnerBox != null) { severFromParent(spinnerBox); spinnerBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (premiseBox != null) severFromParent(premiseBox); } catch (Exception e) {}
        try { if (conclusionBox != null) severFromParent(conclusionBox); } catch (Exception e) {}
        try { if (addShelfBox != null) severFromParent(addShelfBox); } catch (Exception e) {}
        try { if (addGapBox != null) severFromParent(addGapBox); } catch (Exception e) {}

        // Final Explicit Class Nullifications
        this.derivationCreate = null;
        this.formulaBoxedDRTA = null;
        this.justificationBoxedDRTA = null;
        this.depthSpinner = null;
        this.spinnerBox = null;
        this.premiseBox = null;
        this.conclusionBox = null;
        this.addShelfBox = null;
        this.addGapBox = null;

        this.formulaRtaKeyFilter = null;
        this.formulaRtaFocusListener = null;
        this.justificationRtaKeyFilter = null;
        this.justificationRtaFocusListener = null;
        this.depthSpinnerListener = null;
        this.premiseBoxListener = null;
        this.conclusionBoxListener = null;
        this.addShelfBoxListener = null;
        this.addGapBoxListener = null;
    }

    BoxedDRTA getFormulaBoxedDRTA() {
        return formulaBoxedDRTA;
    }

    BoxedDRTA getJustificationBoxedDRTA() {
        return justificationBoxedDRTA;
    }

    Spinner<Integer> getDepthSpinner() {
        return depthSpinner;
    }

    HBox getSpinnerBox() {
        return spinnerBox;
    }

    CheckBox getPremiseBox() {
        return premiseBox;
    }

    CheckBox getConclusionBox() {
        return conclusionBox;
    }

    CheckBox getAddShelfBox() {
        return addShelfBox;
    }

    CheckBox getAddGapBox() {
        return addGapBox;
    }

    boolean isModified() {
        if (isTornDown) return false;
        try {
            boolean fMod = false;
            boolean jMod = false;
            if (formulaBoxedDRTA != null && formulaBoxedDRTA.getRTA() != null) fMod = formulaBoxedDRTA.getRTA().isModified();
            if (justificationBoxedDRTA != null && justificationBoxedDRTA.getRTA() != null) jMod = justificationBoxedDRTA.getRTA().isModified();
            return (modified || fMod || jMod);
        } catch (Exception e) {
            return modified;
        }
    }

    void setModified(boolean modified) {
        if (isTornDown) return;
        this.modified = modified;
    }
}