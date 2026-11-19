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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.m_parse.m_symbols.MBasicObjTerm;
import slapp.editor.main_window.ControlType;
import slapp.editor.vertical_tree.drag_drop.DragContainer;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.drag_drop.NodeLink;
import slapp.editor.vertical_tree.drag_drop.Point2dSerial;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;


public class ABExpTreeFormulaBox extends AnchorPane {
    private VerticalTreeABExpView verticalTreeABExpView;

    private AnchorPane top_link_handle;
    private AnchorPane bottom_link_handle;
    private Label leftDragLabel;
    private Label closeLabel;

    private EventHandler <MouseEvent> mLinkHandleDragDetected;
    private EventHandler <DragEvent> mLinkHandleDragDropped;
    private EventHandler <DragEvent> mContextLinkDragOver;
    private EventHandler <DragEvent> mContextLinkDragDropped;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;

    private NodeLink mDragLink = null;
    private AnchorPane right_pane = null;
    private List<String> mLinkIds = new ArrayList<>();

    private DragIconType mType = DragIconType.tree_field;

    private Point2D mDragOffset = new Point2D (0.0, 0.0);

    private final ABExpTreeFormulaBox self;

    private BoxedDRTA formulaBox;

    private HBox mainBox;
    private GridPane labelPane;
    private VBox centerBox;
    private VBox middleBox;
    private AnchorPane linesPane = new AnchorPane();

    private String idString;
    private boolean boxed = false;
    private boolean starred = false;
    private boolean annotation = false;
    private boolean annotationChanged = false;
    private BoxedDRTA annotationField;

    Rectangle oval = new Rectangle();
    EventHandler<KeyEvent> circleKeyFilter;
    int circleStage = 0;
    Label[] circleMarkers;
    Double[] circleXAnchors = new Double[2];
    private int[] circleIndexes = new int[2];

    double rtaBoundsHeight;
    double rtaBoundsMinY;
    boolean circled = false;
    EventHandler<KeyEvent> ulineKeyFilter;
    int ulineStage = 0;
    Label[] ulineMarkers;
    Double[] ulineXAnchors = new Double[2];
    private List<Integer[]> ulineIndexes = new ArrayList<>();
    private Integer[] ulineIndexTemp = new Integer[2];
    private boolean ulineInclusion = true;
    double ulineSpace = 3.0;
    List<Integer> baseline = new ArrayList<>();
    private MBasicObjTerm semanticValue;

    private double printWidth;

    // KILL SWITCH: Prevents asynchronous listener execution during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<MouseEvent> topLinkEnteredHandler;
    private EventHandler<DragEvent> topLinkDragOverHandler;
    private EventHandler<MouseEvent> topLinkExitedHandler;
    private EventHandler<DragEvent> topLinkDragExitedHandler;
    private EventHandler<MouseEvent> bottomLinkEnteredHandler;
    private EventHandler<DragEvent> bottomLinkDragOverHandler;
    private EventHandler<MouseEvent> bottomLinkExitedHandler;
    private EventHandler<DragEvent> bottomLinkDragExitedHandler;
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
    private EventHandler<KeyEvent> annRtaKeyFilter;
    private ChangeListener<Boolean> annRtaFocusListener;

    public ABExpTreeFormulaBox(VerticalTreeABExpView verticalTreeABExpView) {
        this.verticalTreeABExpView = verticalTreeABExpView;
        self = this;
        circleMarkers = new Label[]{new Label("|"), new Label("|")};
        ulineMarkers = new Label[]{new Label("|"), new Label("|")};

        top_link_handle = new AnchorPane();
        top_link_handle.setPrefHeight(9);
        top_link_handle.setMaxWidth(30);

        topLinkEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || top_link_handle == null) return;
            top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0");
        };
        top_link_handle.setOnMouseEntered(topLinkEnteredHandler);

        topLinkDragOverHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || top_link_handle == null) return;
            top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0");
        };
        top_link_handle.setOnDragOver(topLinkDragOverHandler);

        topLinkExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || top_link_handle == null) return;
            top_link_handle.setStyle("-fx-background-color: transparent");
        };
        top_link_handle.setOnMouseExited(topLinkExitedHandler);

        topLinkDragExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || top_link_handle == null) return;
            top_link_handle.setStyle("-fx-background-color: transparent");
        };
        top_link_handle.setOnDragExited(topLinkDragExitedHandler);

        bottom_link_handle = new AnchorPane();
        bottom_link_handle.setPrefHeight(9);
        bottom_link_handle.setMaxWidth(30);
        bottom_link_handle.setStyle("-fx-background-radius: 0 0 5 5");

        bottomLinkEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || bottom_link_handle == null) return;
            bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5");
        };
        bottom_link_handle.setOnMouseEntered(bottomLinkEnteredHandler);

        bottomLinkDragOverHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || bottom_link_handle == null) return;
            bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5");
        };
        bottom_link_handle.setOnDragOver(bottomLinkDragOverHandler);

        bottomLinkExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || bottom_link_handle == null) return;
            bottom_link_handle.setStyle("-fx-background-color: transparent");
        };
        bottom_link_handle.setOnMouseExited(bottomLinkExitedHandler);

        bottomLinkDragExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null || bottom_link_handle == null) return;
            bottom_link_handle.setStyle("-fx-background-color: transparent");
        };
        bottom_link_handle.setOnDragExited(bottomLinkDragExitedHandler);

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
            if (leftDragLabel != null) leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5;");
            if (closeLabel != null) closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0;");
        };
        leftDragLabel.setOnMouseEntered(leftDragEnteredHandler);

        leftDragExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null) return;
            if (leftDragLabel != null) leftDragLabel.setStyle("-fx-background-color: transparent");
            if (closeLabel != null) closeLabel.setStyle("-fx-background-color:transparent");
        };
        leftDragLabel.setOnMouseExited(leftDragExitedHandler);

        closeEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null) return;
            if (leftDragLabel != null) leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5");
            if (closeLabel != null) closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0");
        };
        closeLabel.setOnMouseEntered(closeEnteredHandler);

        closeExitedHandler = e -> {
            if (isTornDown || this.verticalTreeABExpView == null) return;
            if (leftDragLabel != null) leftDragLabel.setStyle("-fx-background-color: transparent");
            if (closeLabel != null) closeLabel.setStyle("-fx-background-color:transparent");
        };
        closeLabel.setOnMouseExited(closeExitedHandler);

        formulaBox = newFormulaBoxedDRTA();

        middleBox = new VBox(formulaBox.getBoxedRTA(), linesPane);

        centerBox = new VBox(top_link_handle, middleBox, bottom_link_handle);
        centerBox.setAlignment(Pos.CENTER);

        mainBox = new HBox(labelPane, centerBox);
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setMargin(labelPane, new Insets(10, 0, 0, 0));

        self.getChildren().addAll(mainBox);
        self.setLeftAnchor(mainBox, 0.0);
        self.setTopAnchor(mainBox, 0.0);

        setId(UUID.randomUUID().toString());
        idString = getId();
        initialize();
    }

    private void initialize() {
        buildNodeDragHandlers();
        buildLinkDragHandlers();

        top_link_handle.setOnDragDetected(mLinkHandleDragDetected);
        bottom_link_handle.setOnDragDetected(mLinkHandleDragDetected);

        top_link_handle.setOnDragDropped(mLinkHandleDragDropped);
        bottom_link_handle.setOnDragDropped(mLinkHandleDragDropped);

        mDragLink = new NodeLink();
        mDragLink.setVisible(false);

        parentChangeListener = (ob, ov, nv) -> {
            if (isTornDown || self == null) return;
            if (getParent() instanceof AnchorPane) right_pane = (AnchorPane) getParent();
        };
        parentProperty().addListener(parentChangeListener);

        circleKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (isTornDown || verticalTreeABExpView == null || verticalTreeABExpView.getMainView() == null || self == null || formulaBox == null || formulaBox.getRTA() == null) return;
                RichTextArea rta = formulaBox.getRTA();
                if (rta.getScene() == null || rta.getSkin() == null) return;

                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                if (rtaBounds == null) return;

                rtaBoundsHeight = rtaBounds.getHeight();
                rtaBoundsMinY = rtaBounds.getMinY();

                if (code == KeyCode.F10) {
                    if (circleStage < 2) {
                        if (!circled) {
                            int xPos = (int) Math.round(rta.getCaretRowColumn().getX());
                            circleIndexes[circleStage] = xPos;

                            Bounds caretBounds = ((RichTextAreaSkin) rta.getSkin()).getCaretPosition();
                            if (caretBounds == null) return;
                            Bounds newCaretBounds = rta.sceneToLocal(caretBounds);

                            double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 1.0;
                            double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                            if (circleMarkers != null && circleStage < circleMarkers.length) {
                                Label marker = circleMarkers[circleStage];
                                circleXAnchors[circleStage] = xAnchor;
                                self.getChildren().add(marker);
                                self.setLeftAnchor(marker, xAnchor);
                                self.setTopAnchor(marker, yAnchor);
                            }
                            circleStage++;
                        }
                        else EditorAlerts.fleetingRedPopup("Text field has at most one circle annotation.");
                    } else {
                        if (circleMarkers != null && circleMarkers.length >= 2) {
                            try { self.getChildren().removeAll(circleMarkers[0], circleMarkers[1]); } catch(Exception ex) {}
                        }
                        circleStage = 0;
                    }
                    e.consume();
                } else if (code == KeyCode.F11) {
                    if (circleStage < 2) {
                        EditorAlerts.fleetingRedPopup("Circle requires two markers.");
                    }
                    else if (circleStage == 2) {
                        setCircle();
                        circleStage++;
                        circled = true;
                        pushUndoRedo();
                    } else {
                        EditorAlerts.fleetingRedPopup("Text field has at most one circle annotation.");
                    }
                    e.consume();
                }
            }
        };

        ulineKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (isTornDown || verticalTreeABExpView == null || self == null || formulaBox == null || formulaBox.getRTA() == null) return;
                RichTextArea rta = formulaBox.getRTA();
                if (rta.getScene() == null || rta.getSkin() == null) return;

                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                if (rtaBounds == null) return;

                if (code == KeyCode.F10) {
                    if (ulineStage < 2) {

                        int xPos = (int) Math.round(rta.getCaretRowColumn().getX());
                        ulineIndexTemp[ulineStage] = xPos;

                        Bounds caretBounds = ((RichTextAreaSkin) rta.getSkin()).getCaretPosition();
                        if (caretBounds == null) return;
                        Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                        double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 2.0;
                        double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                        if (ulineMarkers != null && ulineStage < ulineMarkers.length) {
                            Label marker = ulineMarkers[ulineStage];
                            ulineXAnchors[ulineStage] = xAnchor;
                            self.getChildren().add(marker);
                            self.setLeftAnchor(marker, xAnchor);
                            self.setTopAnchor(marker, yAnchor);
                        }
                        ulineStage++;
                    } else {
                        if (ulineMarkers != null && ulineMarkers.length >= 2) {
                            try { self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]); } catch(Exception ex) {}
                        }
                        ulineStage = 0;
                    }
                    e.consume();
                }
                else if (code == KeyCode.F11) {
                    if (ulineStage == 2) {
                        double minX = Math.min(ulineXAnchors[0], ulineXAnchors[1]);
                        double maxX = Math.max(ulineXAnchors[0], ulineXAnchors[1]);
                        if (ulineMarkers != null && ulineMarkers.length >= 2) {
                            try { self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]); } catch(Exception ex) {}
                        }
                        setLine(minX - rtaBounds.getMinX(), maxX - rtaBounds.getMinX());
                        setLineIndexes();
                        ulineStage = 0;
                        pushUndoRedo();
                    } else {
                        EditorAlerts.fleetingRedPopup("Underline requires two markers.");
                    }
                    e.consume();
                }
            }
        };
    }

    private void setLineIndexes() {
        if (ulineIndexTemp == null || ulineIndexes == null) return;
        int minX = Math.min(ulineIndexTemp[0], ulineIndexTemp[1]);
        int maxX = Math.max(ulineIndexTemp[0], ulineIndexTemp[1]);

        if (minX != maxX) {
            for (Integer[] index : ulineIndexes) {
                if (index[0] > minX && index[0] < maxX) ulineInclusion = false;
                if (index[1] > minX && index[1] < maxX) ulineInclusion = false;
            }
            if (ulineInclusion) {
                ulineIndexes.add(new Integer[]{minX, maxX});
            }
        }
    }

    public void setCircle() {
        if (circleXAnchors == null || circleXAnchors.length < 2 || self == null || oval == null) return;
        double minX = Math.min(circleXAnchors[0], circleXAnchors[1]);
        double maxX = Math.max(circleXAnchors[0], circleXAnchors[1]);
        if (circleMarkers != null && circleMarkers.length >= 2) {
            try { self.getChildren().removeAll(circleMarkers[0], circleMarkers[1]); } catch(Exception ex) {}
        }
        circleStage = 0;
        self.getChildren().add(oval);
        oval.setWidth(maxX - minX);
        oval.setMouseTransparent(true);

        oval.setHeight(rtaBoundsHeight - 6.0);
        oval.setStyle("-fx-fill: transparent; -fx-stroke: black; -fx-stroke-width: 1;");
        oval.setArcHeight(rtaBoundsHeight - 6.0);
        oval.setArcWidth((maxX - minX));
        self.setLeftAnchor(oval, minX);
        self.setTopAnchor(oval, rtaBoundsMinY + 2.0);
    }

    private void setLine(double startX, double endX) {
        if (baseline == null) return;
        //make sure there is a baseline for new line by extending baseline to endX
        int intStartX = (int) Math.round(startX);
        int intEndX = (int) Math.round(endX);
        for (int i = baseline.size(); i <= intEndX; i++) {
            baseline.add(-((int) ulineSpace));
        }

        //find base for new line
        int maxBase = 0;
        for (int i = intStartX; i <= intEndX; i++) {
            if (baseline.get(i) > maxBase) {
                maxBase = baseline.get(i);
            }
        }

        //get yPosition of new line and update baseline
        double yPos = (double) maxBase + ulineSpace;
        for (int i = intStartX; i <= intEndX; i++) {
            baseline.set(i, (int) Math.round(yPos));
        }

        //add line to linesPane
        addLineToPane(startX, endX - startX, yPos);
    }

    public void addLineToPane(double startX, double length, double yPos) {
        if (linesPane != null) {
            Line line = new Line(0, 0, length, 0);
            linesPane.getChildren().add(line);
            linesPane.setLeftAnchor(line, startX);
            linesPane.setBottomAnchor(line, yPos);
        }
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
                relocateToGridPoint2( new Point2D(event.getSceneX(), event.getSceneY())  );
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
                relocateToPoint( new Point2D(event.getSceneX(), event.getSceneY())  );

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                if (mType != null) container.addData ("type", mType.toString());
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

    private void buildLinkDragHandlers() {
        mLinkHandleDragDetected = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeABExpView == null || getParent() == null || right_pane == null || middleBox == null || mDragLink == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                getParent().setOnDragOver(mContextLinkDragOver);
                getParent().setOnDragDropped(mContextLinkDragDropped);

                right_pane.getChildren().add(0,mDragLink);
                mDragLink.setVisible(false);

                Point2D p = new Point2D(
                        getLayoutX() + (getWidth() / 2.0),
                        getLayoutY() + (middleBox.getHeight()/2) + 9
                );
                mDragLink.setStart(p);

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();
                container.addData("source", getId());
                content.put(DragContainer.AddLink, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);

                event.consume();
            }
        };

        mLinkHandleDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeABExpView == null || getParent() == null || right_pane == null || mDragLink == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);
                if (container == null)  return;

                mDragLink.setVisible(false);
                if (!right_pane.getChildren().isEmpty()) right_pane.getChildren().remove(0);

                AnchorPane link_handle = (AnchorPane) event.getSource();
                ClipboardContent content = new ClipboardContent();
                container.addData("target", getId());
                content.put(DragContainer.AddLink, container);

                event.getDragboard().setContent(content);
                event.setDropCompleted(true);
                event.consume();
            }
        };

        mContextLinkDragOver = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeABExpView == null || mDragLink == null) return;
                event.acceptTransferModes(TransferMode.ANY);

                if (!mDragLink.isVisible())  mDragLink.setVisible(true);
                mDragLink.setEnd(new Point2D(event.getX(), event.getY()));

                event.consume();
            }
        };

        mContextLinkDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeABExpView == null || getParent() == null || right_pane == null || mDragLink == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                mDragLink.setVisible(false);
                if (!right_pane.getChildren().isEmpty()) right_pane.getChildren().remove(0);

                event.setDropCompleted(true);
                event.consume();
            }
        };
    }

    public void relocateToGridPoint (Point2D p) {
        if (getParent() == null || self == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0;
        relocate (
                (int) localCoords.getX() - 36,
                (int) (localY - 7 )
        );
        self.requestFocus();
    }

    public void relocateToGridPoint2 (Point2D p) {
        if (getParent() == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0 ;
        relocate (
                (int) localCoords.getX(),
                (int) (localY - 7)
        );
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
        if (verticalTreeABExpView != null && verticalTreeABExpView.getDefaultKeyboard() != null) {
            drta.getKeyboardSelector().valueProperty().setValue(verticalTreeABExpView.getDefaultKeyboard());
        }
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(24);
        rta.setMinHeight(24);

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

        rta.getStylesheets().add("blueFormulaBox.css");
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

    private BoxedDRTA newAnnotationBoxedDRTA() {
        BoxedDRTA annBDRTA = new BoxedDRTA();
        RichTextArea annRTA = annBDRTA.getRTA();
        annRTA.setPrefWidth(42);
        annBDRTA.getBoxedRTA().setMaxHeight(20);
        annBDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);

        annRtaKeyFilter = e -> {
            if (isTornDown || verticalTreeABExpView == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        annRTA.addEventFilter(KeyEvent.ANY, annRtaKeyFilter);

        annRTA.getStylesheets().add("slappAnnotation.css");
        annRTA.setPromptText("");

        annRtaFocusListener = (ob, ov, nv) -> {
            if (isTornDown || verticalTreeABExpView == null || verticalTreeABExpView.getMainView() == null) return;
            if (nv)  verticalTreeABExpView.getMainView().editorInFocus(annBDRTA.getDRTA(), ControlType.FIELD);
            else {
                if (annRTA.isModified()) {
                    pushUndoRedo();
                    annRTA.getActionFactory().saveNow().execute(new ActionEvent());
                }
            }
        };
        annRTA.focusedProperty().addListener(annRtaFocusListener);

        annBDRTA.getBoxedRTA().getTransforms().add(new Scale(.75,.75));
        return annBDRTA;
    }

    void processBoxRequest(boolean add) {
        if (add) {
            if (!boxed) {
                if (middleBox != null) middleBox.setStyle("-fx-border-color: black; -fx-border-width: 1.2 1.2 1.2 1.2");
                boxed = true;
                pushUndoRedo();
            }
        } else {
            if (boxed) {
                if (middleBox != null) middleBox.setStyle("-fx-border-width: 0 0 0 0");
                boxed = false;
                pushUndoRedo();
            }
        }
    }

    public void addBox() {
        if (middleBox != null) {
            middleBox.setStyle("-fx-border-color: black; -fx-border-width: 1 1 1 1");
            boxed = true;
        }
    }

    public void processStarRequest(boolean add) {
        if (add) {
            if (!starred) {
                addStar();
                pushUndoRedo();
            }
        } else {
            if (starred) {
                if (mainBox != null && labelPane != null && centerBox != null) {
                    mainBox.getChildren().clear();
                    mainBox.getChildren().addAll(labelPane, centerBox);
                }
                starred = false;
                pushUndoRedo();
            }
        }
    }

    public void addStar() {
        if (mainBox == null || labelPane == null || centerBox == null) return;
        Label star = new Label("\u2605");
        star.setStyle("-fx-font-size: 8");
        mainBox.getChildren().clear();
        annotation = false;
        mainBox.getChildren().addAll(labelPane, centerBox, star);
        mainBox.setMargin(star, new Insets(6,0, 0, 0));
        starred = true;
    }

    public void processAnnotationRequest(boolean add) {
        if (add) {
            if (!annotation) {
                addAnnotation();
                pushUndoRedo();
            }
        } else {
            if (annotation) {
                if (mainBox != null && labelPane != null && centerBox != null) {
                    mainBox.getChildren().clear();
                    mainBox.getChildren().addAll(labelPane, centerBox);
                }
                annotation = false;
                pushUndoRedo();
            }
        }
    }

    public void addAnnotation() {
        if (mainBox == null || labelPane == null || centerBox == null) return;
        if (annotationField == null) {
            annotationField = newAnnotationBoxedDRTA();
        }
        if (annotationField.getRTA() != null) {
            annotationField.getRTA().getActionFactory().newDocumentNow().execute(new ActionEvent());
        }
        mainBox.getChildren().clear();
        starred = false;
        mainBox.getChildren().addAll(labelPane, centerBox, annotationField.getBoxedRTA());
        mainBox.setMargin(annotationField.getBoxedRTA(), new Insets(8, 0, 0, 0));
        annotationField.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.SANS_AND_ITALIC);
        annotation = true;
    }



    public void setAnnotationDoc(Document doc) {
        if (annotationField != null && annotationField.getRTA() != null) {
            annotationField.getRTA().getActionFactory().open(doc).execute(new ActionEvent());
        }
    }

    public Document getAnnotationDoc() {
        if (annotationField != null && annotationField.getRTA() != null) {
            annotationField.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            return annotationField.getRTA().getDocument();
        } else {
            return new Document();
        }
    }

    public void processCircleRequest(boolean add) {
        if (add) {
            if (formulaBox != null && formulaBox.getRTA() != null) {
                RichTextArea rta = formulaBox.getRTA();
                rta.requestFocus();
                if (self != null && circleKeyFilter != null) {
                    self.addEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter);
                }
            }
        } else {
            undoCircleRequest();
            if (self != null && oval != null) self.getChildren().remove(oval);
            circled = false;
            circleIndexes = new int[2];
            pushUndoRedo();
        }
    }

    public void undoCircleRequest() {
        if (self != null && circleKeyFilter != null) {
            try { self.removeEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter); } catch (Exception e) {}
        }
        if (self != null && circleMarkers != null && circleMarkers.length >= 2) {
            try { self.getChildren().removeAll(circleMarkers[0], circleMarkers[1]); } catch (Exception e) {}
        }
        circleStage = 0;
    }

    public void processUnderlineRequest(boolean add) {
        if (add) {
            if (formulaBox != null && formulaBox.getRTA() != null) {
                RichTextArea rta = formulaBox.getRTA();
                rta.requestFocus();
                if (self != null && ulineKeyFilter != null) {
                    self.addEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter);
                }
            }
        } else {
            undoUnderlineRequest();
            if (linesPane != null) linesPane.getChildren().clear();
            if (baseline != null) baseline.clear();
            ulineIndexes = new ArrayList<>();
            ulineInclusion = true;
            pushUndoRedo();
        }
    }

    public void undoUnderlineRequest() {
        if (self != null && ulineKeyFilter != null) {
            try { self.removeEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter); } catch (Exception e) {}
        }
        if (self != null && ulineMarkers != null && ulineMarkers.length >= 2) {
            try { self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]); } catch (Exception e) {}
        }
        ulineStage = 0;
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
                if (this.getParent().getOnDragOver() == mContextLinkDragOver) this.getParent().setOnDragOver(null);
                if (this.getParent().getOnDragDropped() == mContextLinkDragDropped) this.getParent().setOnDragDropped(null);
            }
        } catch (Exception e) {}

        // 1. Unhook local filters
        try { if (self != null && circleKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter); } catch(Exception e) {}
        try { if (self != null && ulineKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter); } catch(Exception e) {}
        try { if (parentChangeListener != null) parentProperty().removeListener(parentChangeListener); } catch (Exception e) {}

        // 2. Unhook Explicit Handlers from Labels and Panes
        try {
            if (top_link_handle != null) {
                if (topLinkEnteredHandler != null) top_link_handle.removeEventHandler(MouseEvent.MOUSE_ENTERED, topLinkEnteredHandler);
                if (topLinkExitedHandler != null) top_link_handle.removeEventHandler(MouseEvent.MOUSE_EXITED, topLinkExitedHandler);
                if (topLinkDragOverHandler != null) top_link_handle.removeEventHandler(DragEvent.DRAG_OVER, topLinkDragOverHandler);
                if (topLinkDragExitedHandler != null) top_link_handle.removeEventHandler(DragEvent.DRAG_EXITED, topLinkDragExitedHandler);
                if (mLinkHandleDragDetected != null) top_link_handle.removeEventHandler(MouseEvent.DRAG_DETECTED, mLinkHandleDragDetected);
                if (mLinkHandleDragDropped != null) top_link_handle.removeEventHandler(DragEvent.DRAG_DROPPED, mLinkHandleDragDropped);

                top_link_handle.setOnMouseEntered(null);
                top_link_handle.setOnMouseExited(null);
                top_link_handle.setOnDragDetected(null);
                top_link_handle.setOnDragDropped(null);
                top_link_handle.setOnDragOver(null);
                top_link_handle.setOnDragExited(null);
                severFromParent(top_link_handle);
            }
        } catch (Exception e) {}

        try {
            if (bottom_link_handle != null) {
                if (bottomLinkEnteredHandler != null) bottom_link_handle.removeEventHandler(MouseEvent.MOUSE_ENTERED, bottomLinkEnteredHandler);
                if (bottomLinkExitedHandler != null) bottom_link_handle.removeEventHandler(MouseEvent.MOUSE_EXITED, bottomLinkExitedHandler);
                if (bottomLinkDragOverHandler != null) bottom_link_handle.removeEventHandler(DragEvent.DRAG_OVER, bottomLinkDragOverHandler);
                if (bottomLinkDragExitedHandler != null) bottom_link_handle.removeEventHandler(DragEvent.DRAG_EXITED, bottomLinkDragExitedHandler);
                if (mLinkHandleDragDetected != null) bottom_link_handle.removeEventHandler(MouseEvent.DRAG_DETECTED, mLinkHandleDragDetected);
                if (mLinkHandleDragDropped != null) bottom_link_handle.removeEventHandler(DragEvent.DRAG_DROPPED, mLinkHandleDragDropped);

                bottom_link_handle.setOnMouseEntered(null);
                bottom_link_handle.setOnMouseExited(null);
                bottom_link_handle.setOnDragDetected(null);
                bottom_link_handle.setOnDragDropped(null);
                bottom_link_handle.setOnDragOver(null);
                bottom_link_handle.setOnDragExited(null);
                severFromParent(bottom_link_handle);
            }
        } catch (Exception e) {}

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

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) - BLOCK 1 <---
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

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) - BLOCK 2 <---
        try {
            if (annotationField != null && annotationField.getRTA() != null) {
                final RichTextArea annRta = annotationField.getRTA();
                if (annRtaFocusListener != null) annRta.focusedProperty().removeListener(annRtaFocusListener);
                if (annRtaKeyFilter != null) annRta.removeEventFilter(KeyEvent.ANY, annRtaKeyFilter);
                annRta.prefWidthProperty().unbind();

                // STAGE 1 (Immediate)
                try { annRta.setPromptText(""); } catch (Exception e) {}
                try { annRta.setFocusTraversable(false); } catch (Exception e) {}
                try { if (annRta.getDocument() == null || annRta.getDocument().getText().isEmpty()) annRta.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}

                // STAGE 2 (Delayed Skin Removal)
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try { annRta.setSkin(null); } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // 4. Clean Layouts and Panels
        try { if (mainBox != null) { severFromParent(mainBox); mainBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (centerBox != null) { severFromParent(centerBox); centerBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (middleBox != null) { severFromParent(middleBox); middleBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (labelPane != null) { severFromParent(labelPane); labelPane.getChildren().clear(); } } catch (Exception e) {}
        try { if (linesPane != null) { severFromParent(linesPane); linesPane.getChildren().clear(); } } catch (Exception e) {}
        try { if (oval != null) { severFromParent(oval); } } catch (Exception e) {}
        try { if (mDragLink != null) { severFromParent(mDragLink); } } catch (Exception e) {}
        try { this.getChildren().clear(); } catch (Exception e) {}
        try { severFromParent(self); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive Data Teardown
        try { this.mLinkIds = null; } catch (Exception e) {}
        try { this.baseline = null; } catch (Exception e) {}
        try { this.ulineIndexes = null; } catch (Exception e) {}
        try { this.ulineIndexTemp = null; } catch (Exception e) {}

        // 5. Final severed pointers
        try { this.verticalTreeABExpView = null; } catch (Exception e) {}
        try { this.formulaBox = null; } catch (Exception e) {}
        try { this.annotationField = null; } catch (Exception e) {}
        try { this.right_pane = null; } catch (Exception e) {}
        try { this.mainBox = null; } catch (Exception e) {}
        try { this.labelPane = null; } catch (Exception e) {}
        try { this.centerBox = null; } catch (Exception e) {}
        try { this.middleBox = null; } catch (Exception e) {}
        try { this.linesPane = null; } catch (Exception e) {}
        try { this.top_link_handle = null; } catch (Exception e) {}
        try { this.bottom_link_handle = null; } catch (Exception e) {}
        try { this.leftDragLabel = null; } catch (Exception e) {}
        try { this.closeLabel = null; } catch (Exception e) {}
        try { this.mContextDragOver = null; } catch (Exception e) {}
        try { this.mContextDragDropped = null; } catch (Exception e) {}
        try { this.mLinkHandleDragDetected = null; } catch (Exception e) {}
        try { this.mLinkHandleDragDropped = null; } catch (Exception e) {}
        try { this.mContextLinkDragOver = null; } catch (Exception e) {}
        try { this.mContextLinkDragDropped = null; } catch (Exception e) {}
        try { this.mDragLink = null; } catch (Exception e) {}
        try { this.circleKeyFilter = null; } catch (Exception e) {}
        try { this.ulineKeyFilter = null; } catch (Exception e) {}

        try {
            if (circleMarkers != null) {
                for(int i=0; i<circleMarkers.length; i++) {
                    if (circleMarkers[i] != null) severFromParent(circleMarkers[i]);
                    circleMarkers[i] = null;
                }
            }
            this.circleMarkers = null;
        } catch (Exception e) {}

        try {
            if (ulineMarkers != null) {
                for(int i=0; i<ulineMarkers.length; i++) {
                    if (ulineMarkers[i] != null) severFromParent(ulineMarkers[i]);
                    ulineMarkers[i] = null;
                }
            }
            this.ulineMarkers = null;
        } catch (Exception e) {}

        try { this.circleXAnchors = null; } catch (Exception e) {}
        try { this.circleIndexes = null; } catch (Exception e) {}
        try { this.ulineXAnchors = null; } catch (Exception e) {}
        try { this.oval = null; } catch (Exception e) {}
        try { this.semanticValue = null; } catch (Exception e) {}
        try { this.mType = null; } catch (Exception e) {}
        try { this.mDragOffset = null; } catch (Exception e) {}

        try { this.topLinkEnteredHandler = null; } catch (Exception e) {}
        try { this.topLinkDragOverHandler = null; } catch (Exception e) {}
        try { this.topLinkExitedHandler = null; } catch (Exception e) {}
        try { this.topLinkDragExitedHandler = null; } catch (Exception e) {}
        try { this.bottomLinkEnteredHandler = null; } catch (Exception e) {}
        try { this.bottomLinkDragOverHandler = null; } catch (Exception e) {}
        try { this.bottomLinkExitedHandler = null; } catch (Exception e) {}
        try { this.bottomLinkDragExitedHandler = null; } catch (Exception e) {}
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
        try { this.annRtaKeyFilter = null; } catch (Exception e) {}
        try { this.annRtaFocusListener = null; } catch (Exception e) {}
    }

    public VBox getMiddleBox() {
        return middleBox;
    }

    public List<String> getmLinkIds() {
        return mLinkIds;
    }

    public void setmLinkIds(List<String> mLinkIds) {
        this.mLinkIds = mLinkIds;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
        setId(idString);
    }

    public BoxedDRTA getFormulaBox() {
        return formulaBox;
    }

    public VBox getCenterBox() {return centerBox;}

    public boolean isBoxed() {
        return boxed;
    }

    public boolean isStarred() {
        return starred;
    }

    public boolean isAnnotation() {
        return annotation;
    }

    public BoxedDRTA getAnnotationField() {return annotationField; }

    public boolean isCircled() {
        return circled;
    }

    public void setCircled(boolean circled) {
        this.circled = circled;
    }

    public Double[] getCircleXAnchors() {
        return circleXAnchors;
    }

    public void setCircleXAnchors(Double[] circleXAnchors) {
        this.circleXAnchors = circleXAnchors;
    }

    public int[] getCircleIndexes() {
        return circleIndexes;
    }

    public void setCircleIndexes(int[] circleIndexes) {
        this.circleIndexes = circleIndexes;
    }


    public double getRtaBoundsHeight() {
        return rtaBoundsHeight;
    }

    public void setRtaBoundsHeight(double rtaBoundsHeight) {
        this.rtaBoundsHeight = rtaBoundsHeight;
    }
    public double getRtaBoundsMinY() {
        return rtaBoundsMinY;
    }

    public void setRtaBoundsMinY(double rtaBoundsMinY) {
        this.rtaBoundsMinY = rtaBoundsMinY;
    }

    public AnchorPane getLinesPane() {
        return linesPane;
    }

    public List<Integer> getBaseline() {
        return baseline;
    }

    public void setBaseline(List<Integer> baseline) {
        this.baseline = baseline;
    }

    public double getPrintWidth() {
        return printWidth;
    }

    public void setPrintWidth(double printWidth) {
        this.printWidth = printWidth;
    }

    public List<Integer[]> getUlineIndexes() {
        return ulineIndexes;
    }

    public void setUlineIndexes(List<Integer[]> ulineIndexes) {
        this.ulineIndexes = ulineIndexes;
    }

    public boolean isUlineInclusion() {
        return ulineInclusion;
    }

    public void setUlineInclusion(boolean ulineInclusion) {
        this.ulineInclusion = ulineInclusion;
    }

    private void pushUndoRedo() {
        if (isTornDown) return;
        if (verticalTreeABExpView != null) {
            verticalTreeABExpView.setUndoRedoFlag(true);
            verticalTreeABExpView.setUndoRedoFlag(false);
        }
    }

    public MBasicObjTerm getSemanticValue() {
        return semanticValue;
    }

    public void setSemanticValue(MBasicObjTerm semanticValue) {
        this.semanticValue = semanticValue;
    }
}