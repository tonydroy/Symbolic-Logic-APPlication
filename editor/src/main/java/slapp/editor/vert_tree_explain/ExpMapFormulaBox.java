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

package slapp.editor.vert_tree_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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

import com.gluonhq.richtextarea.model.Document;
import javafx.scene.input.*;
import javafx.scene.layout.*;


public class ExpMapFormulaBox extends AnchorPane {
    private VerticalTreeExpView verticalTreeExpView;

    private Label leftDragLabel;
    private Label closeLabel;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;

    private AnchorPane right_pane = null;
    private List<String> mLinkIds = new ArrayList<>();

    private DragIconType mType = DragIconType.map_field;

    private Point2D mDragOffset = new Point2D (0.0, 0.0);

    private final ExpMapFormulaBox self;

    private BoxedDRTA formulaBox;

    private HBox mainBox;
    private GridPane labelPane;
    private VBox centerBox;
    private String idString = "";

    private Rectangle oval = new Rectangle();
    private EventHandler<KeyEvent> mappingKeyFilter;
    private int mapStage = 0;
    private Label[] mapMarkers;
    private Double[] mapXAnchors = new Double[2];

    private double printWidth;

    // SCORCHED EARTH RULE 1: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Named Handlers
    private EventHandler<MouseEvent> leftDragEnterHandler;
    private EventHandler<MouseEvent> leftDragExitHandler;
    private EventHandler<MouseEvent> closeLabelEnterHandler;
    private EventHandler<MouseEvent> closeLabelExitHandler;
    private ChangeListener<Object> parentListener;
    private EventHandler<MouseEvent> closeLabelClickHandler;
    private EventHandler<MouseEvent> leftDragDetectedHandler;
    private ChangeListener<Number> rtaNodesWidthListener;
    private EventHandler<KeyEvent> rtaKeyFilter;
    private ChangeListener<Boolean> rtaFocusListener;

    public ExpMapFormulaBox(VerticalTreeExpView verticalTreeExpView) {
        this.verticalTreeExpView = verticalTreeExpView;
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

        leftDragEnterHandler = e -> {
            if (isTornDown || this.verticalTreeExpView == null || this.verticalTreeExpView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5;");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0;");
        };
        leftDragLabel.setOnMouseEntered(leftDragEnterHandler);

        leftDragExitHandler = e -> {
            if (isTornDown || this.verticalTreeExpView == null || this.verticalTreeExpView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        };
        leftDragLabel.setOnMouseExited(leftDragExitHandler);

        closeLabelEnterHandler = e -> {
            if (isTornDown || this.verticalTreeExpView == null || this.verticalTreeExpView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0");
        };
        closeLabel.setOnMouseEntered(closeLabelEnterHandler);

        closeLabelExitHandler = e -> {
            if (isTornDown || this.verticalTreeExpView == null || this.verticalTreeExpView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        };
        closeLabel.setOnMouseExited(closeLabelExitHandler);

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
        parentListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                if (isTornDown || self == null) return;
                if (getParent() instanceof AnchorPane) right_pane = (AnchorPane) getParent();
            }
        };
        parentProperty().addListener(parentListener);

        mappingKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (isTornDown || verticalTreeExpView == null || formulaBox == null || formulaBox.getRTA() == null) return;
                RichTextArea rta = formulaBox.getRTA();
                if (rta.getSkin() == null) return;
                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                if (code == KeyCode.F10) {
                    if (mapStage < 2) {
                        Bounds caretBounds = ((RichTextAreaSkin) formulaBox.getRTA().getSkin()).getCaretPosition();
                        Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                        double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 1.0;
                        double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                        Label marker = mapMarkers[mapStage];
                        mapXAnchors[mapStage] = xAnchor;
                        self.getChildren().add(marker);
                        self.setLeftAnchor(marker, xAnchor);
                        self.setTopAnchor(marker, yAnchor);
                        mapStage++;
                    } else {
                        self.getChildren().removeAll(mapMarkers);
                        mapStage = 0;
                    }
                    e.consume();
                }
            }
        };
    }

    public void registerLink(String linkId) {mLinkIds.add(linkId); }

    public void setType (DragIconType type) {
        mType = type;
    }

    public void buildNodeDragHandlers() {
        mContextDragOver = new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeExpView == null || getParent() == null) return;
                event.acceptTransferModes(TransferMode.ANY);
                relocateToPoint(new Point2dSerial( event.getSceneX(), event.getSceneY()));
                event.consume();
            }
        };

        mContextDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeExpView == null || getParent() == null) return;
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                event.setDropCompleted(true);

                relocateToGridPoint2( new Point2D(event.getSceneX(), event.getSceneY()) );
                self.setCursor(Cursor.DEFAULT);
                pushUndoRedo();
            }
        };

        closeLabelClickHandler = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeExpView == null || self.getParent() == null) return;
                self.requestFocus();
                AnchorPane parent  = (AnchorPane) self.getParent();
                parent.getChildren().remove(self);

                if (mLinkIds != null) {
                    for (ListIterator<String> iterId = mLinkIds.listIterator(); iterId.hasNext();) {
                        String id = iterId.next();
                        for (ListIterator <Node> iterNode = parent.getChildren().listIterator(); iterNode.hasNext();) {
                            Node node = iterNode.next();
                            if (node.getId() == null) continue;
                            if (node.getId().equals(id)) iterNode.remove();
                        }
                        iterId.remove();
                    }
                }
                pushUndoRedo();
                parent.requestFocus();

                // SCORCHED EARTH RULE 9: The UI Deletion Fix
                self.cleanup();
            }
        };
        closeLabel.setOnMouseClicked(closeLabelClickHandler);

        leftDragDetectedHandler = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeExpView == null || getParent() == null) return;
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver (mContextDragOver);
                getParent().setOnDragDropped (mContextDragDropped);

                mDragOffset = new Point2D(event.getX(), event.getY());
                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                container.addData ("type", mType.toString());
                content.put(DragContainer.AddNode, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);
                if (formulaBox != null && formulaBox.getRTA() != null) formulaBox.getRTA().requestFocus();
                event.consume();
            }
        };
        leftDragLabel.setOnDragDetected(leftDragDetectedHandler);
    }

    public void relocateToGridPoint (Point2D p) {
        if (getParent() == null) return;
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
        if (verticalTreeExpView != null && verticalTreeExpView.getDefaultMapKeyboard() != null) {
            drta.getKeyboardSelector().valueProperty().setValue(verticalTreeExpView.getDefaultMapKeyboard());
        }
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(24);
        rta.setMinHeight(24);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), 12));

        rtaNodesWidthListener = (observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (isTornDown || verticalTreeExpView == null || rta == null || rta.getParent() == null) return;
                rta.getParent().requestLayout();
            });
        };
        rtaSkin.nodesWidthProperty().addListener(rtaNodesWidthListener);

        rtaKeyFilter = e -> {
            if (isTornDown || verticalTreeExpView == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        rta.addEventFilter(KeyEvent.ANY, rtaKeyFilter);

        rta.getStylesheets().add("greenFormulaBox.css");
        rta.setPromptText("");

        rtaFocusListener = (ob, ov, nv) -> {
            if (isTornDown || verticalTreeExpView == null || verticalTreeExpView.getMainView() == null) return;
            if (nv) {
                verticalTreeExpView.getMainView().editorInFocus(drta, ControlType.FIELD);
            }
        };
        rta.focusedProperty().addListener(rtaFocusListener);
        return boxedDRTA;
    }

    public void processMappingRequest(boolean add) {
        if (add) {
            RichTextArea rta = formulaBox.getRTA();
            if (rta != null) rta.requestFocus();
            self.addEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
        } else {
            undoMappingRequest();
        }
    }

    public void undoMappingRequest() {
        if (mappingKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
        self.getChildren().removeAll(mapMarkers[0], mapMarkers[1]);
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

    // SCORCHED EARTH RULE 5: Heavily Isolated cleanup
    public void cleanup() {
        // KILL SWITCH: Engaged
        isTornDown = true;

        try {
            if (this.getParent() != null) {
                if (this.getParent().getOnDragOver() == mContextDragOver) this.getParent().setOnDragOver(null);
                if (this.getParent().getOnDragDropped() == mContextDragDropped) this.getParent().setOnDragDropped(null);
            }
        } catch (Exception e) {}

        try {
            if (mappingKeyFilter != null) {
                self.removeEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
            }
        } catch (Exception e) {}

        try {
            if (parentListener != null) {
                parentProperty().removeListener(parentListener);
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) <---
        try {
            if (formulaBox != null && formulaBox.getRTA() != null) {
                final RichTextArea rta = formulaBox.getRTA();
                try { if (rtaFocusListener != null) rta.focusedProperty().removeListener(rtaFocusListener); } catch(Exception e){}
                try { if (rtaKeyFilter != null) rta.removeEventFilter(KeyEvent.ANY, rtaKeyFilter); } catch(Exception e){}
                try {
                    if (rta.getSkin() != null && rtaNodesWidthListener != null) {
                        ((RichTextAreaSkin) rta.getSkin()).nodesWidthProperty().removeListener(rtaNodesWidthListener);
                    }
                } catch(Exception e){}

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

        try { if (mainBox != null) { severFromParent(mainBox); mainBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (labelPane != null) { severFromParent(labelPane); labelPane.getChildren().clear(); } } catch (Exception e) {}
        try { if (centerBox != null) { severFromParent(centerBox); centerBox.getChildren().clear(); } } catch (Exception e) {}

        try {
            this.setOnDragOver(null);
            this.setOnDragDropped(null);
            this.setOnDragDetected(null);
            this.setOnMouseClicked(null);
        } catch (Exception e) {}

        try {
            if (leftDragLabel != null) {
                if (leftDragEnterHandler != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, leftDragEnterHandler);
                if (leftDragExitHandler != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, leftDragExitHandler);
                if (leftDragDetectedHandler != null) leftDragLabel.removeEventHandler(MouseEvent.DRAG_DETECTED, leftDragDetectedHandler);
                leftDragLabel.setOnMouseEntered(null);
                leftDragLabel.setOnMouseExited(null);
                leftDragLabel.setOnDragDetected(null);
            }
        } catch (Exception e) {}

        try {
            if (closeLabel != null) {
                if (closeLabelEnterHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, closeLabelEnterHandler);
                if (closeLabelExitHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, closeLabelExitHandler);
                if (closeLabelClickHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, closeLabelClickHandler);
                closeLabel.setOnMouseEntered(null);
                closeLabel.setOnMouseExited(null);
                closeLabel.setOnMouseClicked(null);
            }
        } catch (Exception e) {}

        try { this.getChildren().clear(); } catch (Exception e) {}
        try { severFromParent(self); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { this.mLinkIds = null; } catch (Exception e) {}

        this.verticalTreeExpView = null;
        this.formulaBox = null;
        this.right_pane = null;
        this.mainBox = null;
        this.labelPane = null;
        this.centerBox = null;
        this.leftDragLabel = null;
        this.closeLabel = null;
        this.mContextDragOver = null;
        this.mContextDragDropped = null;
        this.mappingKeyFilter = null;
        this.mapMarkers = null;
        this.mapXAnchors = null;
        this.oval = null;
        this.leftDragEnterHandler = null;
        this.leftDragExitHandler = null;
        this.closeLabelEnterHandler = null;
        this.closeLabelExitHandler = null;
        this.parentListener = null;
        this.closeLabelClickHandler = null;
        this.leftDragDetectedHandler = null;
        this.rtaNodesWidthListener = null;
        this.rtaKeyFilter = null;
        this.rtaFocusListener = null;
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
        if (verticalTreeExpView != null && verticalTreeExpView.getVtExpExercise() != null) {
            verticalTreeExpView.getVtExpExercise().pushUndoRedo();
        }
    }
}