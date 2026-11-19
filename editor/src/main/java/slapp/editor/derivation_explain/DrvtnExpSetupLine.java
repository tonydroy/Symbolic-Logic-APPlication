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
package slapp.editor.derivation_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.main_window.ControlType;

import java.util.ArrayList;
import java.util.List;

/**
 * Adjunct to derivation create, for setup of a single derivation line.
 */


public class DrvtnExpSetupLine {
    private DrvtnExpCreate drvtnExpCreate;
    private BoxedDRTA formulaBoxedDRTA;
    private BoxedDRTA justificationBoxedDRTA;
    private Spinner<Integer> depthSpinner;
    private CheckBox premiseBox;
    private CheckBox conclusionBox;
    private CheckBox addShelfBox;
    private CheckBox addGapBox;
    private HBox spinnerBox;
    private boolean modified = false;
    private double formulaBoxHeight = 27;

    // SCORCHED EARTH RULE 3: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 4: Eradicate Anonymous Lambdas (Fully qualified to satisfy STRICT IMPORTS)
    private javafx.event.EventHandler<KeyEvent> formulaKeyFilter;
    private javafx.beans.value.ChangeListener<Boolean> formulaFocusListener;
    private javafx.event.EventHandler<KeyEvent> justificationKeyFilter;
    private javafx.beans.value.ChangeListener<Boolean> justificationFocusListener;
    private javafx.beans.value.ChangeListener<Integer> depthSpinnerListener;
    private javafx.beans.value.ChangeListener<Boolean> premiseBoxListener;
    private javafx.beans.value.ChangeListener<Boolean> conclusionBoxListener;
    private javafx.beans.value.ChangeListener<Boolean> addShelfBoxListener;
    private javafx.beans.value.ChangeListener<Boolean> addGapBoxListener;

    /**
     * Construct setup line
     * @param drvtnExpCreate the controlling create window
     */
    public DrvtnExpSetupLine(DrvtnExpCreate drvtnExpCreate) {
        this.drvtnExpCreate = drvtnExpCreate;
        formulaBoxedDRTA = new BoxedDRTA();

        if (drvtnExpCreate != null && drvtnExpCreate.getKeyboardSelector() != null && formulaBoxedDRTA.getDRTA() != null && formulaBoxedDRTA.getDRTA().getKeyboardSelector() != null) {
            formulaBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(drvtnExpCreate.getKeyboardSelector());
        }

        RichTextArea formulaRTA = formulaBoxedDRTA.getRTA();
        if (formulaRTA != null) {
            formulaRTA.setMaxHeight(formulaBoxHeight);
            formulaRTA.setMinHeight(formulaBoxHeight);
            formulaRTA.setPrefWidth(400);
            formulaRTA.setContentAreaWidth(500);

            // SCORCHED EARTH RULE 10: Infinite Stylesheet Accumulation Fix
            formulaRTA.getStylesheets().clear();
            formulaRTA.getStylesheets().add("RichTextFieldWide.css");
            formulaRTA.setPromptText("Formula");

            formulaKeyFilter = e -> {
                if (isTornDown) return;
                try {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                        if (justificationBoxedDRTA != null && justificationBoxedDRTA.getRTA() != null) {
                            justificationBoxedDRTA.getRTA().requestFocus();
                            e.consume();
                        }
                    }
                } catch (Exception ex) {}
            };
            formulaRTA.addEventFilter(KeyEvent.KEY_PRESSED, formulaKeyFilter);

            formulaFocusListener = (ob, ov, nv) -> {
                if (isTornDown || this.drvtnExpCreate == null) return;
                try {
                    if (nv) {
                        this.drvtnExpCreate.editorInFocus(formulaBoxedDRTA.getDRTA(), ControlType.FIELD);
                    }
                } catch (Exception ex) {}
            };
            formulaRTA.focusedProperty().addListener(formulaFocusListener);
        }

        justificationBoxedDRTA = new BoxedDRTA();
        if (justificationBoxedDRTA.getDRTA() != null && justificationBoxedDRTA.getDRTA().getKeyboardSelector() != null) {
            justificationBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE);
        }
        RichTextArea justificationRTA = justificationBoxedDRTA.getRTA();

        if (justificationRTA != null) {
            justificationRTA.getActionFactory().open(new Document("")).execute(new ActionEvent());
            justificationRTA.setMaxHeight(formulaBoxHeight);
            justificationRTA.setMinHeight(formulaBoxHeight);
            justificationRTA.setPrefWidth(100);
            justificationRTA.setContentAreaWidth(200);

            // SCORCHED EARTH RULE 10: Infinite Stylesheet Accumulation Fix
            justificationRTA.getStylesheets().clear();
            justificationRTA.getStylesheets().add("RichTextFieldWide.css");
            justificationRTA.setPromptText("Justification");

            justificationKeyFilter = e -> {
                if (isTornDown) return;
                try {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                        if (depthSpinner != null) {
                            depthSpinner.requestFocus();
                            e.consume();
                        }
                    } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                        if (formulaRTA != null) {
                            formulaRTA.requestFocus();
                            e.consume();
                        }
                    }
                } catch (Exception ex) {}
            };
            justificationRTA.addEventFilter(KeyEvent.KEY_PRESSED, justificationKeyFilter);

            justificationFocusListener = (ob, ov, nv) -> {
                if (isTornDown || this.drvtnExpCreate == null) return;
                try {
                    if (nv) {
                        this.drvtnExpCreate.editorInFocus(justificationBoxedDRTA.getDRTA(), ControlType.STATEMENT);
                    }
                } catch (Exception ex) {}
            };
            justificationRTA.focusedProperty().addListener(justificationFocusListener);
        }

        depthSpinner = new Spinner<>(1,19,1);
        depthSpinner.setPrefWidth(55);
        depthSpinnerListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            try {
                modified = true;
            } catch (Exception ex) {}
        };
        depthSpinner.valueProperty().addListener(depthSpinnerListener);

        spinnerBox = new HBox(10, new Label("Depth"), depthSpinner);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);

        premiseBox = new CheckBox("Premise");
        premiseBox.setSelected(false);
        premiseBoxListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            try {
                if (nv != null && nv) {
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
                if (nv != null && nv) {
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
                if (nv != null && nv) {
                    if (addGapBox != null) addGapBox.setSelected(false);
                }
                modified = true;
            } catch (Exception ex) {}
        };
        addShelfBox.selectedProperty().addListener(addShelfBoxListener);

        addGapBox = new CheckBox("Add gap");
        addGapBox.setSelected(false); // Bug fix: original incorrectly called addShelfBox.setSelected(false)
        addGapBoxListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            try {
                if (nv != null && nv) {
                    if (addShelfBox != null) addShelfBox.setSelected(false);
                }
                modified = true;
            } catch (Exception ex) {}
        };
        addGapBox.selectedProperty().addListener(addGapBoxListener);
    }

    /**
     * The main fomula field
     * @return the formula boxed DRTA
     */
    BoxedDRTA getFormulaBoxedDRTA() {
        return formulaBoxedDRTA;
    }

    /**
     * The justification field
     * @return the formula boxed DRTA
     */
    BoxedDRTA getJustificationBoxedDRTA() {
        return justificationBoxedDRTA;
    }

    /**
     * The scope depth spinner
     * @return the spinner
     */
    Spinner<Integer> getDepthSpinner() {
        return depthSpinner;
    }

    /**
     * Box containing depth spinner
     * @return the hbox
     */
    HBox getSpinnerBox() {
        return spinnerBox;
    }

    /**
     * Box containing depth spinner
     * @return the hbox
     */
    CheckBox getPremiseBox() {
        return premiseBox;
    }

    /**
     * Box to identify conclusion line
     * @return the check box
     */
    CheckBox getConclusionBox() {
        return conclusionBox;
    }

    /**
     * Box to add shelf under current line
     * @return the checkbox
     */
    CheckBox getAddShelfBox() {
        return addShelfBox;
    }

    /**
     * Box to add gap under current line
     * @return the check box
     */
    CheckBox getAddGapBox() {
        return addGapBox;
    }

    /**
     * Line is modified if checkbox or one of the RTAs is modified
     * @return true if modified and otherwise false
     */
    boolean isModified() {
        if (isTornDown) return false;
        try {
            boolean formulaMod = (formulaBoxedDRTA != null && formulaBoxedDRTA.getRTA() != null) && formulaBoxedDRTA.getRTA().isModified();
            boolean justMod = (justificationBoxedDRTA != null && justificationBoxedDRTA.getRTA() != null) && justificationBoxedDRTA.getRTA().isModified();
            return (modified || formulaMod || justMod);
        } catch (Exception e) { return modified; }
    }

    /**
     * Line is modified if checkbox or one of the RTAs is modified
     * @param modified true if modified and otherwise false
     */
    void setModified(boolean modified) { this.modified = modified; }

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
        // KILL SWITCH: Engaged!
        isTornDown = true;

        // SCORCHED EARTH RULE 5: Break Down Isolation & Unbind Properties
        try {
            if (formulaBoxedDRTA != null && formulaBoxedDRTA.getRTA() != null) {
                if (formulaKeyFilter != null) formulaBoxedDRTA.getRTA().removeEventFilter(KeyEvent.KEY_PRESSED, formulaKeyFilter);
                if (formulaFocusListener != null) formulaBoxedDRTA.getRTA().focusedProperty().removeListener(formulaFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (justificationBoxedDRTA != null && justificationBoxedDRTA.getRTA() != null) {
                if (justificationKeyFilter != null) justificationBoxedDRTA.getRTA().removeEventFilter(KeyEvent.KEY_PRESSED, justificationKeyFilter);
                if (justificationFocusListener != null) justificationBoxedDRTA.getRTA().focusedProperty().removeListener(justificationFocusListener);
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

        // SCORCHED EARTH RULE 6 & 7: Clean Transient Wrappers and Sever Nodes
        try {
            if (formulaBoxedDRTA != null) {
                severFromParent(formulaBoxedDRTA.getRTA());
                formulaBoxedDRTA.cleanup();
                severFromParent(formulaBoxedDRTA.getBoxedRTA());
            }
        } catch (Exception e) {}

        try {
            if (justificationBoxedDRTA != null) {
                severFromParent(justificationBoxedDRTA.getRTA());
                justificationBoxedDRTA.cleanup();
                severFromParent(justificationBoxedDRTA.getBoxedRTA());
            }
        } catch (Exception e) {}


        try { severFromParent(depthSpinner); } catch (Exception e) {}
        try { severFromParent(premiseBox); } catch (Exception e) {}
        try { severFromParent(conclusionBox); } catch (Exception e) {}
        try { severFromParent(addShelfBox); } catch (Exception e) {}
        try { severFromParent(addGapBox); } catch (Exception e) {}
        try {
            if (spinnerBox != null) {
                spinnerBox.getChildren().clear();
                severFromParent(spinnerBox);
            }
        } catch (Exception e) {}

        // Final Explicit Nullifications
        this.drvtnExpCreate = null;
        this.formulaBoxedDRTA = null;
        this.justificationBoxedDRTA = null;
        this.depthSpinner = null;
        this.premiseBox = null;
        this.conclusionBox = null;
        this.addShelfBox = null;
        this.addGapBox = null;
        this.spinnerBox = null;

        this.formulaKeyFilter = null;
        this.formulaFocusListener = null;
        this.justificationKeyFilter = null;
        this.justificationFocusListener = null;
        this.depthSpinnerListener = null;
        this.premiseBoxListener = null;
        this.conclusionBoxListener = null;
        this.addShelfBoxListener = null;
        this.addGapBoxListener = null;
    }
}