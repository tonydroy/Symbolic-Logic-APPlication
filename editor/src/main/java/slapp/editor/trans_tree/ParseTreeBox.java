package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.parser.grammatical_parts.Formula;
import javafx.beans.value.ChangeListener;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


public class ParseTreeBox extends TTreeBox {
    private Formula mate;

    // SCORCHED EARTH 2.0: Named listeners
    private ChangeListener<Number> nodesWidthListener;
    private EventHandler<KeyEvent> keyEventFilter;
    private ChangeListener<Boolean> focusListener;

    // Maintain direct references for safe unhooking during cleanup
    private RichTextArea rtaRef;
    private RichTextAreaSkin rtaSkinRef;

    ParseTreeBox(TTBoxContainer ttBoxContainer) {
        super(ttBoxContainer);

        ulineKeyFilter = e -> {
            if (isTornDown || ParseTreeBox.this.ttBoxContainer == null || self == null || formulaBox == null || formulaBox.getRTA() == null) return;
            try {
                RichTextArea rta = formulaBox.getRTA();
                if (rta.getScene() == null || rta.getSkin() == null) return;
                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));

                if (code == KeyCode.F10) {
                    if (ulineStage < 2) {
                        int xPos = (int) Math.round(rta.getCaretRowColumn().getX());
                        ulineIndexTemp[ulineStage] = xPos;

                        Bounds caretBounds = ((RichTextAreaSkin) formulaBox.getRTA().getSkin()).getCaretPosition();
                        Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                        double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 2.0;
                        double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                        Label marker = ulineMarkers[ulineStage];
                        ulineXAnchors[ulineStage] = xAnchor;
                        self.getChildren().add(marker);
                        self.setLeftAnchor(marker, xAnchor);
                        self.setTopAnchor(marker, yAnchor);
                        ulineStage++;
                    } else {
                        if (ulineMarkers != null) self.getChildren().removeAll(ulineMarkers);
                        ulineStage = 0;
                    }
                    e.consume();
                } else if (code == KeyCode.F11) {
                    if (ulineStage == 2) {
                        double minX = Math.min(ulineXAnchors[0], ulineXAnchors[1]);
                        double maxX = Math.max(ulineXAnchors[0], ulineXAnchors[1]);
                        if (ulineMarkers != null) self.getChildren().removeAll(ulineMarkers);
                        double startX = minX - rtaBounds.getMinX();
                        double endX = maxX - rtaBounds.getMinX();

                        if (Math.abs(startX - endX) >= .1) {
                            int lineLevel = setLine(startX, endX);
                            setTreeObj(lineLevel, startX, endX);
                            if (ParseTreeBox.this.ttBoxContainer != null) {
                                ParseTreeBox.this.ttBoxContainer.setExerciseModified(true);
                                ParseTreeBox.this.ttBoxContainer.pushUndoRedo();
                            }
                        }
                        ulineStage = 0;
                    } else {
                        EditorAlerts.fleetingRedPopup("Underline requires two markers.");
                    }
                    e.consume();
                }
            } catch (Exception ex) {}
        };
        self.addEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter);

        underlineClickFilter = event -> {
            if (isTornDown || ParseTreeBox.this.ttBoxContainer == null || self == null) return;
            try {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() != 1) clearLines();
            } catch (Exception ex) {}
        };
        self.addEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter);
    }
    @Override
    public void cleanup() {
        // SCORCHED EARTH RULE 7 & 5: NEVER CALL editorInFocus(null, ...) inside local Node teardowns!
        try { if (self != null && ulineKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter); } catch (Exception e) {}
        try { if (self != null && underlineClickFilter != null) self.removeEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter); } catch (Exception e) {}
        try { if (rtaRef != null) rtaRef.prefWidthProperty().unbind(); } catch (Exception e) {}
        try { if (rtaSkinRef != null && nodesWidthListener != null) rtaSkinRef.nodesWidthProperty().removeListener(nodesWidthListener); } catch (Exception e) {}
        try { if (rtaRef != null && keyEventFilter != null) rtaRef.removeEventFilter(KeyEvent.ANY, keyEventFilter); } catch (Exception e) {}
        try { if (rtaRef != null && focusListener != null) rtaRef.focusedProperty().removeListener(focusListener); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER <---
        try {
            if (rtaRef != null) {
                final RichTextArea pEditor = rtaRef;

                // STAGE 1 (Immediate): Prompt wipe, focus disable, and document injection
                try { pEditor.setPromptText(""); } catch (Exception e) {}
                try { pEditor.setFocusTraversable(false); } catch (Exception e) {}
                try {
                    if (pEditor.getDocument() == null || pEditor.getDocument().getText().isEmpty()) {
                        pEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                    }
                } catch (Exception e) {}

                // STAGE 2 (Delayed Skin Removal): Push skin removal 1500ms into the future
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try {
                        pEditor.setSkin(null);
                    } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        try { super.cleanup(); } catch (Exception e) {}

        this.mate = null;
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
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE);

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

        rta.getStylesheets().add("greenFormulaBox.css");
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

    public Formula getMate() { return mate; }
    public void setMate(Formula mate) { this.mate = mate; }
}