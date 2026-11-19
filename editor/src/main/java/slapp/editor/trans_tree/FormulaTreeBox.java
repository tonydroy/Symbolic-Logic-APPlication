package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.parser.grammatical_parts.Formula;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class FormulaTreeBox extends TTreeBox {
    private Formula formula;

    // SCORCHED EARTH 2.0: Named listeners
    private ChangeListener<Number> nodesWidthListener;
    private EventHandler<KeyEvent> keyEventFilter;
    private ChangeListener<Boolean> focusListener;

    // Maintain direct references for safe unhooking during cleanup
    private RichTextArea rtaRef;
    private RichTextAreaSkin rtaSkinRef;

    public FormulaTreeBox(TTBoxContainer ttBoxContainer) {
        super(ttBoxContainer);
    }

    @Override
    public void cleanup() {
        // SCORCHED EARTH RULE 7 & 5: NEVER CALL editorInFocus(null, ...) inside local Node teardowns!
        // The parent TransTreeExercise safely handles toolbars. We just isolate unbindings.
        try { if (rtaRef != null) rtaRef.prefWidthProperty().unbind(); } catch (Exception e) {}
        try { if (rtaSkinRef != null && nodesWidthListener != null) rtaSkinRef.nodesWidthProperty().removeListener(nodesWidthListener); } catch (Exception e) {}
        try { if (rtaRef != null && keyEventFilter != null) rtaRef.removeEventFilter(KeyEvent.ANY, keyEventFilter); } catch (Exception e) {}
        try { if (rtaRef != null && focusListener != null) rtaRef.focusedProperty().removeListener(focusListener); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER <---
        try {
            if (rtaRef != null) {
                final RichTextArea fEditor = rtaRef;

                // STAGE 1 (Immediate): Prompt wipe, focus disable, and document injection
                try { fEditor.setPromptText(""); } catch (Exception e) {}
                try { fEditor.setFocusTraversable(false); } catch (Exception e) {}
                try {
                    if (fEditor.getDocument() == null || fEditor.getDocument().getText().isEmpty()) {
                        fEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                    }
                } catch (Exception e) {}

                // STAGE 2 (Delayed Skin Removal): Push skin removal 1500ms into the future
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try {
                        fEditor.setSkin(null);
                    } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Cascade
        try { super.cleanup(); } catch (Exception e) {}

        this.formula = null;
        this.nodesWidthListener = null;
        this.keyEventFilter = null;
        this.focusListener = null;
        this.rtaRef = null;
        this.rtaSkinRef = null;
    }

    @Override
    BoxedDRTA newFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);

        RichTextArea rta = boxedDRTA.getRTA();
        rtaRef = rta;
        rta.setMaxHeight(24);
        rta.setMinHeight(24);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rtaSkinRef = rtaSkin;

        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), 12));

        nodesWidthListener = (observable, oldValue, newValue) -> {
            if (isTornDown) return;
            Platform.runLater(() -> {
                if (isTornDown || rta == null || rta.getParent() == null) return;
                try { rta.getParent().requestLayout(); } catch (Exception e) {}
            });
        };
        rtaSkin.nodesWidthProperty().addListener(nodesWidthListener);

        keyEventFilter = e -> {
            if (isTornDown || ttBoxContainer == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        rta.addEventFilter(KeyEvent.ANY, keyEventFilter);

        rta.getStylesheets().add("blueFormulaBox.css");
        rta.setPromptText("");

        focusListener = (ob, ov, nv) -> {
            if (isTornDown || ttBoxContainer == null) return;
            if (nv) {
                try { ttBoxContainer.editorInFocus(drta, ControlType.FIELD); } catch (Exception e) {}
            }
        };
        rta.focusedProperty().addListener(focusListener);

        return boxedDRTA;
    }

    public Formula getFormula() { return formula; }
    public void setFormula(Formula formula) { this.formula = formula; }
}