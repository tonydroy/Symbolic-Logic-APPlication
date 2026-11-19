package slapp.editor.trans_uline;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;


public class SimpleULineBox extends AnchorPane {
    SimpleULineBox self;
    ULBoxContainer ulBoxContainer;
    BoxedDRTA formulaBox;
    AnchorPane linesPane = new AnchorPane();
    VBox middleBox;

    // SCORCHED EARTH RULE 3: Named listeners so they can be explicitly removed in cleanup!
    EventHandler<KeyEvent> ulineKeyFilter;
    EventHandler<MouseEvent> underlineClickFilter;
    ChangeListener<Number> textLengthListener;
    ChangeListener<Number> nodesWidthListener;
    EventHandler<KeyEvent> rtaKeyFilter;
    ChangeListener<Boolean> rtaFocusListener;

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    int ulineStage = 0;
    Label[] ulineMarkers;
    Double[] ulineXAnchors = new Double[2];
    private List<ULTreeObj> treeObjects = new ArrayList<>();
    private int[] ulineIndexTemp = new int[2];
    double ulineSpace = 3.0;
    List<Integer> baseline = new ArrayList<>();
    boolean opened = false;
    private boolean isExtracting = false;

    public SimpleULineBox(ULBoxContainer ulBoxContainer) {
        self = this;
        this.ulBoxContainer = ulBoxContainer;
        ulineMarkers = new Label[]{new Label("|"), new Label("|")};

        formulaBox = newFormulaBoxedDRTA();
        middleBox = new VBox(formulaBox.getBoxedRTA(), linesPane);
        self.getChildren().addAll(middleBox);

        textLengthListener = (ob, ov, nv) -> {
            if (isTornDown || self == null || isExtracting) return;
            try {
                if (opened) {
                    clearLines();
                }
                opened = true;
            } catch (Exception e) {}
        };

        ulineKeyFilter = e -> {
            if (isTornDown || self == null || ulBoxContainer == null || formulaBox == null || formulaBox.getRTA() == null) return;
            try {
                RichTextArea rta = formulaBox.getRTA();
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
                        AnchorPane.setLeftAnchor(marker, xAnchor);
                        AnchorPane.setTopAnchor(marker, yAnchor);
                        ulineStage++;
                    } else {
                        if (ulineMarkers != null) self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]);
                        ulineStage = 0;
                    }
                    e.consume();
                }
                else if (code == KeyCode.F11) {
                    if (ulineStage == 2) {
                        double minX = Math.min(ulineXAnchors[0], ulineXAnchors[1]);
                        double maxX = Math.max(ulineXAnchors[0], ulineXAnchors[1]);
                        if (ulineMarkers != null) self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]);
                        double startX = minX - rtaBounds.getMinX();
                        double endX = maxX - rtaBounds.getMinX();

                        if (Math.abs(startX - endX) >= .1) {
                            int lineLevel = setLine(startX, endX);
                            setTreeObj(lineLevel, startX, endX);
                            if (ulBoxContainer != null) {
                                ulBoxContainer.setExerciseModified(true);
                                ulBoxContainer.pushUndoRedo();
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
            if (isTornDown || self == null || ulBoxContainer == null) return;
            try {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() != 1) {
                    clearLines();
                }
            } catch (Exception e) {}
        };
        self.addEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter);
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper
    private void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                } else if (node.getParent() instanceof javafx.scene.Group) {
                    ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
                }
            }
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation applied heavily inside cleanup()
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 6: Forcefully detach from parent VBox/Pane
        try { severFromParent(this); } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Isolate Event Filter Removal
        try {
            if (self != null) {
                if (ulineKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter);
                if (underlineClickFilter != null) self.removeEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Isolate Property Unbinding and Listener Removal
        try {
            if (formulaBox != null && formulaBox.getRTA() != null) {
                final RichTextArea rta = formulaBox.getRTA();
                try { rta.prefWidthProperty().unbind(); } catch(Exception e) {}

                try {
                    if (textLengthListener != null) rta.textLengthProperty().removeListener(textLengthListener);
                } catch(Exception e) {}

                try {
                    if (rtaKeyFilter != null) rta.removeEventFilter(KeyEvent.ANY, rtaKeyFilter);
                } catch(Exception e) {}

                try {
                    if (rtaFocusListener != null) rta.focusedProperty().removeListener(rtaFocusListener);
                } catch(Exception e) {}

                try {
                    if (nodesWidthListener != null && rta.getSkin() != null) {
                        ((RichTextAreaSkin) rta.getSkin()).nodesWidthProperty().removeListener(nodesWidthListener);
                    }
                } catch(Exception e) {}
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 6: Sever internal UI nodes
        try { if (ulineMarkers != null) { for (Label marker : ulineMarkers) severFromParent(marker); } } catch (Exception e) {}
        try { if (linesPane != null) severFromParent(linesPane); } catch (Exception e) {}
        try { if (formulaBox != null && formulaBox.getBoxedRTA() != null) severFromParent(formulaBox.getBoxedRTA()); } catch (Exception e) {}
        try { if (middleBox != null) severFromParent(middleBox); } catch (Exception e) {}

        // SCORCHED EARTH RULE 8 & 5: Isolate physical UI destruction
        try {
            if (linesPane != null) {
                List<Node> linesCopy = new ArrayList<>(linesPane.getChildren());
                for (Node n : linesCopy) severFromParent(n);
                linesPane.getChildren().clear();
            }
        } catch (Exception e) {}
        try {
            if (middleBox != null) {
                List<Node> mbCopy = new ArrayList<>(middleBox.getChildren());
                for (Node n : mbCopy) severFromParent(n);
                middleBox.getChildren().clear();
            }
        } catch (Exception e) {}
        try { if (self != null) self.getChildren().clear(); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea rta = (formulaBox != null) ? formulaBox.getRTA() : null;
            if (rta != null) {
                // STAGE 1 (Immediate): Prompt wipe, focus disable, and document injection
                try { rta.setPromptText(""); } catch (Exception e) {}
                try { rta.setFocusTraversable(false); } catch (Exception e) {}
                try {
                    if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                        rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                    }
                } catch (Exception e) {}

                // STAGE 2 (Delayed Skin Removal): Push skin removal 1500ms into the future
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try {
                        rta.setSkin(null);
                    } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive Data Teardown (only clear primitive tracking data, NOT models)
        try { if (baseline != null) baseline.clear(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.ulineKeyFilter = null; } catch (Exception e) {}
        try { this.underlineClickFilter = null; } catch (Exception e) {}
        try { this.textLengthListener = null; } catch (Exception e) {}
        try { this.nodesWidthListener = null; } catch (Exception e) {}
        try { this.rtaKeyFilter = null; } catch (Exception e) {}
        try { this.rtaFocusListener = null; } catch (Exception e) {}

        try { this.self = null; } catch (Exception e) {}
        try { this.ulBoxContainer = null; } catch (Exception e) {}
        try { this.formulaBox = null; } catch (Exception e) {}
        try { this.linesPane = null; } catch (Exception e) {}
        try { this.middleBox = null; } catch (Exception e) {}
        try { this.ulineMarkers = null; } catch (Exception e) {}
        try { this.ulineXAnchors = null; } catch (Exception e) {}
        try { this.ulineIndexTemp = null; } catch (Exception e) {}

        // CRITICAL FIX: Do not call .clear() on treeObjects as it points to the Model!
        try { this.treeObjects = null; } catch (Exception e) {}
        try { this.baseline = null; } catch (Exception e) {}
    }

    public void clearLines() {
        if (isTornDown || self == null || linesPane == null || ulBoxContainer == null) return;
        try {
            List<Node> linesCopy = new ArrayList<>(linesPane.getChildren());
            for (Node n : linesCopy) severFromParent(n);
            linesPane.getChildren().clear();
        } catch (Exception e) {}

        // --- ADDED FIX: Remove F10 markers and reset stage ---
        try {
            if (ulineMarkers != null) {
                self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]);
            }
        } catch (Exception e) {}
        ulineStage = 0;
        // -----------------------------------------------------


        if (baseline != null) baseline.clear();
        if (treeObjects != null) treeObjects.clear();
        ulBoxContainer.setExerciseModified(true);
        ulBoxContainer.pushUndoRedo();
    }

    public void resetLines() {
        if (isTornDown || self == null || linesPane == null) return;
        try {
            List<Node> linesCopy = new ArrayList<>(linesPane.getChildren());
            for (Node n : linesCopy) severFromParent(n);
            linesPane.getChildren().clear();
        } catch (Exception e) {}

        // --- ADDED FIX: Remove F10 markers and reset stage ---
        try {
            if (ulineMarkers != null) {
                self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]);
            }
        } catch (Exception e) {}
        ulineStage = 0;
        // -----------------------------------------------------

        if (baseline != null) baseline.clear();
        setupBox(treeObjects);
    }

    private void setTreeObj(int level, double lineStartX, double lineEndX) {
        if (isTornDown || self == null || ulineIndexTemp == null || treeObjects == null) return;
        int minX = Math.min(ulineIndexTemp[0], ulineIndexTemp[1]);
        int maxX = Math.max(ulineIndexTemp[0], ulineIndexTemp[1]);
        treeObjects.add(new ULTreeObj(lineStartX, lineEndX, minX, maxX, level));
    }

    private int setLine(double startX, double endX) {
        if (isTornDown || self == null || baseline == null || ulBoxContainer == null) return 0;

        int intStartX = (int) Math.round(startX);
        int intEndX = (int) Math.round(endX);
        for (int i = baseline.size(); i <= intEndX; i++) {
            baseline.add(-1);
        }

        int maxBase = -1;
        for (int i = intStartX; i <= intEndX; i++) {
            if (baseline.get(i) > maxBase) {
                maxBase = baseline.get(i);
            }
        }

        int yPos = maxBase + 1;
        for (int i = intStartX; i <= intEndX; i++) {
            baseline.set(i, yPos);
        }

        addLineToPane(startX, endX - startX, yPos * ulineSpace);
        if (ulBoxContainer != null) {
            ulBoxContainer.setExerciseModified(true);
        }
        return yPos;
    }

    public void addLineToPane(double startX, double length, double yPos) {
        if (isTornDown || self == null || linesPane == null) return;
        if (startX >= 0) {
            Line line = new Line(0, 0, length, 0);
            linesPane.getChildren().add(line);
            AnchorPane.setLeftAnchor(line, startX);
            AnchorPane.setBottomAnchor(line, yPos);
        }
    }

    private BoxedDRTA newFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE);
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(24);
        rta.setMinHeight(24);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        if (ulBoxContainer != null && ulBoxContainer.getScalePageWidth() != null) {
            rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), ulBoxContainer.getScalePageWidth()));
        }

        nodesWidthListener = (observable, oldValue, newValue) -> {
            if (isTornDown || self == null) return;
            Platform.runLater(() -> {
                if (isTornDown || self == null || rta == null || rta.getParent() == null) return;
                try {
                    rta.getParent().requestLayout();
                } catch (Exception e) {}
            });
        };
        rtaSkin.nodesWidthProperty().addListener(nodesWidthListener);

        rtaKeyFilter = e -> {
            if (isTornDown || self == null) return;
            try {
                if (e.getCode() == KeyCode.ENTER) e.consume();
            } catch (Exception ex) {}
        };
        rta.addEventFilter(KeyEvent.ANY, rtaKeyFilter);

        rta.getStylesheets().add("greenFormulaBox.css");
        rta.setPromptText("");

        rtaFocusListener = (ob, ov, nv) -> {
            if (isTornDown || self == null || ulBoxContainer == null) return;
            try {
                if (nv) {
                    ulBoxContainer.editorInFocus(drta, ControlType.FIELD);
                }
            } catch (Exception e) {}
        };
        rta.focusedProperty().addListener(rtaFocusListener);

        return boxedDRTA;
    }

    public BoxedDRTA getFormulaBox() {
        return formulaBox;
    }

    public List<ULTreeObj> getTreeObjects() {
        if (isTornDown || self == null || treeObjects == null) return new ArrayList<>();
        if (treeObjects.size() > 0 && treeObjects.get(0).isRoot()) {
            treeObjects.remove(0);
        }

        if (formulaBox != null && formulaBox.getRTA() != null) {
            RichTextArea rta = formulaBox.getRTA();

            isExtracting = true;
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            isExtracting = false;

            String text = rta.getDocument().getText();
            ULTreeObj rootObj = new ULTreeObj(-1.0, -1.0, 0, text.length(), -1);
            rootObj.setText(text);
            rootObj.setRoot(true);
            treeObjects.add(0, rootObj);
        }

        return treeObjects;
    }

    public void setupBox(List<ULTreeObj> treeObjs) {
        if (isTornDown || self == null) return;

        // Force valid initialization even if empty!
        this.treeObjects = (treeObjs != null) ? treeObjs : new ArrayList<>();

        if (formulaBox != null && formulaBox.getRTA() != null && textLengthListener != null) {
            formulaBox.getRTA().textLengthProperty().removeListener(textLengthListener);
        }

        opened = false;

        // 1. ALWAYS initialize the Document so JavaFX registers it correctly
        String text = "";
        if (!this.treeObjects.isEmpty()) {
            text = this.treeObjects.get(0).getText();
        }

        if (formulaBox != null && formulaBox.getRTA() != null) {
            formulaBox.getRTA().getActionFactory().open(new Document(text)).execute(new ActionEvent());
            formulaBox.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
        }

        // 2. If it's an empty "B" exercise, stop drawing lines, but DO attach the listener!
        if (this.treeObjects.isEmpty()) {
            Platform.runLater(() -> {
                if (isTornDown || self == null || formulaBox == null || formulaBox.getRTA() == null || textLengthListener == null) return;
                try {
                    formulaBox.getRTA().textLengthProperty().removeListener(textLengthListener);
                    opened = false;
                    formulaBox.getRTA().textLengthProperty().addListener(textLengthListener);
                } catch (Exception e) {}
            });
            return;
        }

        // 3. (Only happens for "A") - baseline size and line drawing
        int maxX = 0;
        for (ULTreeObj treeObj : this.treeObjects) {
            if ((int) Math.round(treeObj.getLineEndX()) > maxX) {
                maxX = (int) Math.round(treeObj.getLineEndX());
            }
        }

        //reset baseline
        if (baseline != null) {
            for (int i = 0; i <= maxX; i++) {
                int newBase = -1;
                for (ULTreeObj treeObj : this.treeObjects) {
                    if ((int) Math.round(treeObj.getLineStartX()) <= i && i <= (int) Math.round(treeObj.getLineEndX())) {
                        if (treeObj.getLevel() > newBase) {
                            newBase = treeObj.getLevel();
                        }
                    }
                }
                baseline.add(i, newBase);
            }
        }

        //draw lines
        for (ULTreeObj treeObj : this.treeObjects) {
            double startX = treeObj.getLineStartX();
            double endX = treeObj.getLineEndX();
            addLineToPane(startX, endX - startX, treeObj.getLevel() * ulineSpace);
        }

        Platform.runLater(() -> {
            if (isTornDown || self == null || formulaBox == null || formulaBox.getRTA() == null || textLengthListener == null) return;
            try {
                formulaBox.getRTA().textLengthProperty().removeListener(textLengthListener);
                opened = false;
                formulaBox.getRTA().textLengthProperty().addListener(textLengthListener);
            } catch (Exception e) {}
        });
    }

    public void setTreeObjects(List<ULTreeObj> treeObjects) {
        this.treeObjects = treeObjects;
    }
}