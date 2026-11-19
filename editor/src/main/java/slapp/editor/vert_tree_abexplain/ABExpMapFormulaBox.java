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

package slapp.editor.vert_tree_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.vertical_tree.drag_drop.DragContainer;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.drag_drop.Point2dSerial;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import javafx.scene.input.*;
import javafx.scene.layout.*;

import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;


public class ABExpMapFormulaBox extends AnchorPane {
    private VerticalTreeABExpView verticalTreeABExpView;

    private Label leftDragLabel;
    private Label closeLabel;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler<DragEvent> mContextDragDropped;

    private AnchorPane right_pane = null;
    private List<String> mLinkIds = new ArrayList<>();

    private DragIconType mType = DragIconType.map_field;

    private Point2D mDragOffset = new Point2D (0.0, 0.0);

    private final ABExpMapFormulaBox self;

    private BoxedDRTA formulaBox;

    private HBox mainBox;
    private GridPane labelPane;
    private VBox centerBox;

    private String idString = "";

    Rectangle oval = new Rectangle();
    EventHandler<KeyEvent> mappingKeyFilter;
    int mapStage = 0;
    Label[] mapMarkers;
    Double[] mapXAnchors = new Double[2];
    private double printWidth;

    // KILL SWITCH: Prevents asynchronous listener execution during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Named Handlers
    private EventHandler<MouseEvent> leftDragEnteredHandler;
    private EventHandler<MouseEvent> leftDragExitedHandler;
    private EventHandler<MouseEvent> closeEnteredHandler;
    private EventHandler<MouseEvent> closeExitedHandler;
    private EventHandler<MouseEvent> closeClickedHandler;
    private EventHandler<MouseEvent> leftDragDetectedHandler;
    private ChangeListener<Object> parentChangeListener;
    private ChangeListener<Number> nodesWidthListener;
    private EventHandler<KeyEvent> rtaKeyFilter;
    private ChangeListener<Boolean> rtaFocusListener;

    public ABExpMapFormulaBox(VerticalTreeABExpView verticalTreeABExpView) {
        this.verticalTreeABExpView = verticalTreeABExpView;
        self = this;
        mapMarkers = new Label[]{new Label("|"), new Label("|")};

        leftDragLabel = new Label("");
        leftDragLabel.setMaxWidth(10);
        leftDragLabel.setMinWidth(10);
        leftDragLabel.setMaxHeight(10);
        leftDragLabel.setPadding(new Insets(0));

        closeLabel = new Label();
        closeLabel.setMaxHeight(10);
        closeLabel.setMaxWidth(10);
        closeLabel.setPadding(new Insets(0));

        labelPane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints(10);
        columnConstraints.setHgrow(Priority.NEVER);
        labelPane.getColumnConstraints().add(columnConstraints);
        RowConstraints closeRowConstraints = new RowConstraints(10);
        closeRowConstraints.setVgrow(Priority.NEVER);
        RowConstraints moveRowConstraints = new RowConstraints(10);
        moveRowConstraints.setVgrow(Priority.NEVER);
        labelPane.getRowConstraints().addAll(closeRowConstraints, moveRowConstraints);
        labelPane.add(closeLabel, 0, 0); labelPane.add(leftDragLabel, 0, 1);

        leftDragEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null) return;
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5;");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0;");
        };
        leftDragLabel.setOnMouseEntered(leftDragEnteredHandler);

        leftDragExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null) return;
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        };
        leftDragLabel.setOnMouseExited(leftDragExitedHandler);

        closeEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null) return;
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0");
        };
        closeLabel.setOnMouseEntered(closeEnteredHandler);

        closeExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null) return;
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        };
        closeLabel.setOnMouseExited(closeExitedHandler);

        formulaBox = newFormulaBoxedDRTA();

        centerBox = new VBox(formulaBox.getBoxedRTA());
        centerBox.setAlignment(Pos.CENTER);

        mainBox = new HBox(labelPane, centerBox);
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setMargin(labelPane, new Insets(2, 0, 0, 0));

        self.getChildren().addAll(mainBox);
        self.setLeftAnchor(mainBox, 0.0);
        self.setTopAnchor(mainBox, 0.0);

        setId(UUID.randomUUID().toString());
        idString = getId();
        initialize();
    }

    private void initialize() {
        buildNodeDragHandlers();

        parentChangeListener = (ob, ov, nv) -> {
            if (isTornDown || self == null) return;
            if (getParent() instanceof AnchorPane) right_pane = (AnchorPane) getParent();
        };
        parentProperty().addListener(parentChangeListener);

        mappingKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (isTornDown || verticalTreeABExpView == null || self == null || formulaBox == null || formulaBox.getRTA() == null) return;
                RichTextArea rta = formulaBox.getRTA();

                // Prevent layout NPEs during the teardown event storm
                if (rta.getScene() == null || rta.getSkin() == null) return;

                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                if (rtaBounds == null) return;

                if (code == KeyCode.F10) {
                    if (mapStage < 2) {
                        Bounds caretBounds = ((RichTextAreaSkin) rta.getSkin()).getCaretPosition();
                        if (caretBounds == null) return;

                        Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                        double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 1.0;
                        double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                        if (mapMarkers != null && mapMarkers.length > mapStage) {
                            Label marker = mapMarkers[mapStage];
                            mapXAnchors[mapStage] = xAnchor;
                            self.getChildren().add(marker);
                            self.setLeftAnchor(marker, xAnchor);
                            self.setTopAnchor(marker, yAnchor);
                        }
                        mapStage++;
                    } else {
                        if (mapMarkers != null && mapMarkers.length >= 2) {
                            try { self.getChildren().removeAll(mapMarkers[0], mapMarkers[1]); } catch(Exception ex) {}
                        }
                        mapStage = 0;
                    }
                    e.consume();
                }
            }
        };

    }

    public void registerLink(String linkId) {
        if (mLinkIds != null) mLinkIds.add(linkId);
    }

    public void setType (DragIconType type) {
        mType = type;
    }

    public void buildNodeDragHandlers() {

        mContextDragOver = new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeABExpView == null || getParent() == null) return;
                event.acceptTransferModes(TransferMode.ANY);
                relocateToPoint(new Point2dSerial( event.getSceneX(), event.getSceneY()));
                event.consume();
            }
        };

        mContextDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeABExpView == null || getParent() == null || self == null) return;
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                event.setDropCompleted(true);

                relocateToGridPoint2( new Point2D(event.getSceneX(), event.getSceneY()) );
                self.setCursor(Cursor.DEFAULT);
                pushUndoRedo();
            }
        };

        // SCORCHED EARTH RULE 9: The UI Deletion Fix
        closeClickedHandler = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeABExpView == null || self == null || self.getParent() == null) return;
                self.requestFocus();
                AnchorPane parent  = (AnchorPane) self.getParent();
                parent.getChildren().remove(self);

                if (mLinkIds != null) {
                    // SCORCHED EARTH RULE 8: Iterate over a copy to avoid CME
                    List<Node> parentChildrenCopy = new ArrayList<>(parent.getChildren());
                    for (ListIterator<String> iterId = mLinkIds.listIterator(); iterId.hasNext();) {
                        String id = iterId.next();
                        for (Node node : parentChildrenCopy) {
                            if (node.getId() == null) continue;
                            if (node.getId().equals(id)) {
                                parent.getChildren().remove(node);
                            }
                        }
                        iterId.remove();
                    }
                }
                pushUndoRedo();
                parent.requestFocus();
                self.cleanup(); // Force explicit teardown so node does not float in memory
            }
        };
        closeLabel.setOnMouseClicked(closeClickedHandler);

        leftDragDetectedHandler = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeABExpView == null || getParent() == null) return;
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                getParent().setOnDragOver (mContextDragOver);
                getParent().setOnDragDropped (mContextDragDropped);

                mDragOffset = new Point2D(event.getX(), event.getY());
                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();
                if (mType != null) container.addData ("type", mType.toString());
                content.put(DragContainer.AddNode, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);

                if (formulaBox != null && formulaBox.getRTA() != null) {
                    formulaBox.getRTA().requestFocus();
                }
                event.consume();
            }
        };
        leftDragLabel.setOnDragDetected(leftDragDetectedHandler);
    }

    public void relocateToGridPoint (Point2D p) {
        if (getParent() == null || self == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0;
        relocate ((int) localCoords.getX() - 44,  (int) (localY ) );
        self.requestFocus();
    }

    public void relocateToGridPoint2 (Point2D p) {
        if (getParent() == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0 ;
        relocate (  (int) localCoords.getX(), (int) (localY )    );
    }

    public void relocateToPoint (Point2D p) {
        if (getParent() == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        relocate (
                (int) localCoords.getX(),
                (int) ((localCoords.getY() - (getBoundsInLocal().getHeight()) / 2 ))
        );
    }

    private BoxedDRTA newFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        if (verticalTreeABExpView != null && verticalTreeABExpView.getDefaultMapKeyboard() != null) {
            drta.getKeyboardSelector().valueProperty().setValue(verticalTreeABExpView.getDefaultMapKeyboard());
        }
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(24);
        rta.setMinHeight(24);
        rta.setPrefWidth(36);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), 12));

        nodesWidthListener = (observable, oldValue, newValue) -> {
            if (isTornDown) return;
            Platform.runLater(() -> {
                if (isTornDown || verticalTreeABExpView == null || rta == null || rta.getParent() == null) return;
                rta.getParent().requestLayout();
            });
        };
        rtaSkin.nodesWidthProperty().addListener(nodesWidthListener);

        rtaKeyFilter = e -> {
            if (isTornDown || verticalTreeABExpView == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        rta.addEventFilter(KeyEvent.ANY, rtaKeyFilter);

        rta.getStylesheets().add("greenFormulaBox.css");
        rta.setPromptText("");

        rtaFocusListener = (ob, ov, nv) -> {
            if (isTornDown || verticalTreeABExpView == null || verticalTreeABExpView.getMainView() == null) return;
            if (nv) {
                verticalTreeABExpView.getMainView().editorInFocus(drta, ControlType.FIELD);
            }
        };
        rta.focusedProperty().addListener(rtaFocusListener);
        return boxedDRTA;
    }

    public void processMappingRequest(boolean add) {
        if (add) {
            if (formulaBox != null && formulaBox.getRTA() != null) {
                RichTextArea rta = formulaBox.getRTA();
                rta.requestFocus();
                if (self != null && mappingKeyFilter != null) {
                    self.addEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
                }
            }
        } else {
            undoMappingRequest();
        }
    }

    public void undoMappingRequest() {
        if (self != null && mappingKeyFilter != null) {
            try { self.removeEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter); } catch (Exception e) {}
        }
        if (self != null && mapMarkers != null && mapMarkers.length >= 2) {
            try { self.getChildren().removeAll(mapMarkers[0], mapMarkers[1]); } catch (Exception e) {}
        }
        mapStage = 0;
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

    // SCORCHED EARTH RULE 5: Rule of Isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 1: Kill Switch
        isTornDown = true;

        try {
            if (this.getParent() != null) {
                if (this.getParent().getOnDragOver() == mContextDragOver) this.getParent().setOnDragOver(null);
                if (this.getParent().getOnDragDropped() == mContextDragDropped) this.getParent().setOnDragDropped(null);
            }
        } catch (Exception e) {}

        // 1. Unhook explicit event filters/listeners on node
        try { if (self != null && mappingKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter); } catch (Exception e) {}
        try { if (parentChangeListener != null) parentProperty().removeListener(parentChangeListener); } catch (Exception e) {}

        // 2. Unhook explicitly named labels/events
        try {
            if (leftDragLabel != null) {
                if (leftDragEnteredHandler != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, leftDragEnteredHandler);
                if (leftDragExitedHandler != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, leftDragExitedHandler);
                if (leftDragDetectedHandler != null) leftDragLabel.removeEventHandler(MouseEvent.DRAG_DETECTED, leftDragDetectedHandler);
                leftDragLabel.setOnMouseEntered(null);
                leftDragLabel.setOnMouseExited(null);
                leftDragLabel.setOnDragDetected(null);
                severFromParent(leftDragLabel);
            }
        } catch (Exception e) {}

        try {
            if (closeLabel != null) {
                if (closeEnteredHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, closeEnteredHandler);
                if (closeExitedHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, closeExitedHandler);
                if (closeClickedHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, closeClickedHandler);
                closeLabel.setOnMouseEntered(null);
                closeLabel.setOnMouseExited(null);
                closeLabel.setOnMouseClicked(null);
                severFromParent(closeLabel);
            }
        } catch (Exception e) {}

        try { this.setOnDragOver(null); } catch (Exception e) {}
        try { this.setOnDragDropped(null); } catch (Exception e) {}
        try { this.setOnDragDetected(null); } catch (Exception e) {}
        try { this.setOnMouseClicked(null); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) <---
        try {
            if (formulaBox != null && formulaBox.getRTA() != null) {
                final RichTextArea rta = formulaBox.getRTA();
                if (rtaFocusListener != null) rta.focusedProperty().removeListener(rtaFocusListener);
                if (rtaKeyFilter != null) rta.removeEventFilter(KeyEvent.ANY, rtaKeyFilter);
                if (rta.getSkin() != null && nodesWidthListener != null) {
                    ((RichTextAreaSkin) rta.getSkin()).nodesWidthProperty().removeListener(nodesWidthListener);
                }
                rta.prefWidthProperty().unbind();

                // STAGE 1 (Immediate)
                try { rta.setPromptText(""); } catch (Exception e) {}
                try { rta.setFocusTraversable(false); } catch (Exception e) {}
                try { if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}

                // STAGE 2 (Delayed Skin Removal)
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try { rta.setSkin(null); } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // 4. Clear Layouts
        try { if (mainBox != null) { severFromParent(mainBox); mainBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (labelPane != null) { severFromParent(labelPane); labelPane.getChildren().clear(); } } catch (Exception e) {}
        try { if (centerBox != null) { severFromParent(centerBox); centerBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (oval != null) { severFromParent(oval); } } catch (Exception e) {}
        try { this.getChildren().clear(); } catch (Exception e) {}
        try { severFromParent(self); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive Data Teardown
        try { this.mLinkIds = null; } catch (Exception e) {}

        // 5. Final severed pointers
        try { this.verticalTreeABExpView = null; } catch (Exception e) {}
        try { this.formulaBox = null; } catch (Exception e) {}
        try { this.right_pane = null; } catch (Exception e) {}
        try { this.mainBox = null; } catch (Exception e) {}
        try { this.labelPane = null; } catch (Exception e) {}
        try { this.centerBox = null; } catch (Exception e) {}
        try { this.leftDragLabel = null; } catch (Exception e) {}
        try { this.closeLabel = null; } catch (Exception e) {}
        try { this.mContextDragOver = null; } catch (Exception e) {}
        try { this.mContextDragDropped = null; } catch (Exception e) {}
        try { this.mappingKeyFilter = null; } catch (Exception e) {}

        try {
            if (mapMarkers != null) {
                for (int i = 0; i < mapMarkers.length; i++) {
                    if (mapMarkers[i] != null) severFromParent(mapMarkers[i]);
                    mapMarkers[i] = null;
                }
            }
            this.mapMarkers = null;
        } catch (Exception e) {}

        try { this.mapXAnchors = null; } catch (Exception e) {}
        try { this.oval = null; } catch (Exception e) {}
        try { this.mType = null; } catch (Exception e) {}
        try { this.mDragOffset = null; } catch (Exception e) {}

        try { this.leftDragEnteredHandler = null; } catch (Exception e) {}
        try { this.leftDragExitedHandler = null; } catch (Exception e) {}
        try { this.closeEnteredHandler = null; } catch (Exception e) {}
        try { this.closeExitedHandler = null; } catch (Exception e) {}
        try { this.closeClickedHandler = null; } catch (Exception e) {}
        try { this.leftDragDetectedHandler = null; } catch (Exception e) {}
        try { this.parentChangeListener = null; } catch (Exception e) {}
        try { this.nodesWidthListener = null; } catch (Exception e) {}
        try { this.rtaKeyFilter = null; } catch (Exception e) {}
        try { this.rtaFocusListener = null; } catch (Exception e) {}
    }

    public VBox getCenterBox() {
        return centerBox;
    }

    public Double[] getMapXAnchors() {
        return mapXAnchors;
    }

    public void setMapXAnchors(Double[] mapXAnchors) {
        this.mapXAnchors = mapXAnchors;
    }

    public int getMapStage() {
        return mapStage;
    }

    public void setMapStage(int mapStage) {
        this.mapStage = mapStage;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
        setId(idString);
    }

    public List<String> getmLinkIds() {
        return mLinkIds;
    }

    public void setmLinkIds(List<String> mLinkIds) {
        this.mLinkIds = mLinkIds;
    }

    public BoxedDRTA getFormulaBox() {
        return formulaBox;
    }

    public double getPrintWidth() {
        return printWidth;
    }

    public void setPrintWidth(double printWidth) {
        this.printWidth = printWidth;
    }

    private void pushUndoRedo() {
        if (isTornDown) return;
        if (verticalTreeABExpView != null && verticalTreeABExpView.getVtABExpExercise() != null) {
            verticalTreeABExpView.getVtABExpExercise().pushUndoRedo();
        }
    }
}