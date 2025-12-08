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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;

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

    /**
     * Construct setup line
     * @param drvtnExpCreate the controlling create window
     */
    public DrvtnExpSetupLine(DrvtnExpCreate drvtnExpCreate) {
        this.drvtnExpCreate = drvtnExpCreate;
        formulaBoxedDRTA = new BoxedDRTA();
        formulaBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(drvtnExpCreate.getKeyboardSelector());
        RichTextArea formulaRTA = formulaBoxedDRTA.getRTA();

        formulaRTA.setMaxHeight(formulaBoxHeight);
        formulaRTA.setMinHeight(formulaBoxHeight);
        formulaRTA.setPrefWidth(400);
        formulaRTA.setContentAreaWidth(500);
        formulaRTA.getStylesheets().add("RichTextFieldWide.css");
        formulaRTA.setPromptText("Formula");

        formulaRTA.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                justificationBoxedDRTA.getRTA().requestFocus();
                e.consume();
            }
        });

        formulaRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
               drvtnExpCreate.editorInFocus(formulaBoxedDRTA.getDRTA(), ControlType.FIELD);
            }
        });

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

        justificationRTA.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                depthSpinner.requestFocus();
                e.consume();
            }
            else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                formulaRTA.requestFocus();
                e.consume();
            }
        });

        justificationRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                drvtnExpCreate.editorInFocus(justificationBoxedDRTA.getDRTA(), ControlType.STATEMENT);
            }
        });

        depthSpinner = new Spinner<>(1,19,1);
        depthSpinner.setPrefWidth(55);
        depthSpinner.valueProperty().addListener((ob, ov, nv) -> modified = true);
        spinnerBox = new HBox(10, new Label("Depth"), depthSpinner);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);

        premiseBox = new CheckBox("Premise");
        premiseBox.setSelected(false);
        premiseBox.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv == true) {
                conclusionBox.setSelected(false);
                addGapBox.setSelected(false);
            }
            modified = true;
        });

        conclusionBox = new CheckBox("Conclusion");
        conclusionBox.setSelected(false);
        conclusionBox.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv == true) {
                premiseBox.setSelected(false);
                addShelfBox.setSelected(false);
                addGapBox.setSelected(false);
            }
            modified = true;
        });

        addShelfBox = new CheckBox("Add shelf");
        addShelfBox.setSelected(false);
        addShelfBox.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv == true) addGapBox.setSelected(false);
            modified = true;
        });

        addGapBox = new CheckBox("Add gap");
        addShelfBox.setSelected(false);
        addGapBox.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv == true) addShelfBox.setSelected(false);
            modified = true;
        });
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
    Spinner getDepthSpinner() {
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
        return (modified || formulaBoxedDRTA.getRTA().isModified() || justificationBoxedDRTA.getRTA().isModified());
    }

    /**
     * Line is modified if checkbox or one of the RTAs is modified
     * @param modified true if modified and otherwise false
     */
    void setModified(boolean modified) { this.modified = modified; }

}
