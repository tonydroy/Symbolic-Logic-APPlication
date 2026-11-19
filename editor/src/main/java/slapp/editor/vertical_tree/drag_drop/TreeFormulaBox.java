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

package slapp.editor.vertical_tree.drag_drop;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.m_parse.m_symbols.MBasicObjTerm;
import slapp.editor.main_window.ControlType;
import slapp.editor.vertical_tree.VerticalTreeView;

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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;


public class TreeFormulaBox extends AnchorPane {
    private VerticalTreeView verticalTreeView;
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
    private final TreeFormulaBox self;
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
    private Rectangle oval = new Rectangle();
    private EventHandler<KeyEvent> circleKeyFilter;
    private int circleStage = 0;
    private Label[] circleMarkers;
    private Double[] circleXAnchors = new Double[2];
    private int[] circleIndexes = new int[2];
    private double rtaBoundsHeight;
    private double rtaBoundsMinY;
    private boolean circled = false;
    private EventHandler<KeyEvent> ulineKeyFilter;
    private int ulineStage = 0;
    private Label[] ulineMarkers;
    private Double[] ulineXAnchors = new Double[2];
    private List<Integer[]> ulineIndexes = new ArrayList<>();
    private Integer[] ulineIndexTemp = new Integer[2];
    private boolean ulineInclusion = true;
    private double ulineSpace = 3.0;
    private List<Integer> baseline = new ArrayList<>();
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
    private EventHandler<MouseEvent> closeLabelEnteredHandler;
    private EventHandler<MouseEvent> closeLabelExitedHandler;
    private ChangeListener<Object> parentPropertyListener;
    private EventHandler<MouseEvent> closeLabelClickedHandler;
    private EventHandler<MouseEvent> leftDragDetectedHandler;
    private ChangeListener<Number> nodesWidthListener;
    private EventHandler<KeyEvent> formulaRtaKeyFilter;
    private ChangeListener<Boolean> formulaRtaFocusListener;
    private EventHandler<KeyEvent> annKeyFilter;
    private ChangeListener<Boolean> annFocusListener;

    public TreeFormulaBox(VerticalTreeView verticalTreeView) {
        this.verticalTreeView = verticalTreeView;
        self = this;
        circleMarkers = new Label[]{new Label("|"), new Label("|")};
        ulineMarkers = new Label[]{new Label("|"), new Label("|")};

        top_link_handle = new AnchorPane();
        top_link_handle.setPrefHeight(9);
        top_link_handle.setMaxWidth(30);

        topLinkEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0");
        };
        top_link_handle.setOnMouseEntered(topLinkEnteredHandler);

        topLinkDragOverHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0");
        };
        top_link_handle.setOnDragOver(topLinkDragOverHandler);

        topLinkExitedHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            top_link_handle.setStyle("-fx-background-color: transparent");
        };
        top_link_handle.setOnMouseExited(topLinkExitedHandler);

        topLinkDragExitedHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            top_link_handle.setStyle("-fx-background-color: transparent");
        };
        top_link_handle.setOnDragExited(topLinkDragExitedHandler);

        bottom_link_handle = new AnchorPane();
        bottom_link_handle.setPrefHeight(9);
        bottom_link_handle.setMaxWidth(30);
        bottom_link_handle.setStyle("-fx-background-radius: 0 0 5 5");

        bottomLinkEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5");
        };
        bottom_link_handle.setOnMouseEntered(bottomLinkEnteredHandler);

        bottomLinkDragOverHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5");
        };
        bottom_link_handle.setOnDragOver(bottomLinkDragOverHandler);

        bottomLinkExitedHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            bottom_link_handle.setStyle("-fx-background-color: transparent");
        };
        bottom_link_handle.setOnMouseExited(bottomLinkExitedHandler);

        bottomLinkDragExitedHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
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
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5;");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0;");
        };
        leftDragLabel.setOnMouseEntered(leftDragEnteredHandler);

        leftDragExitedHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        };
        leftDragLabel.setOnMouseExited(leftDragExitedHandler);

        closeLabelEnteredHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0");
        };
        closeLabel.setOnMouseEntered(closeLabelEnteredHandler);

        closeLabelExitedHandler = e -> {
            if (isTornDown || this.verticalTreeView == null || this.verticalTreeView.getMainView() == null) return;
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        };
        closeLabel.setOnMouseExited(closeLabelExitedHandler);

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

    /*
     * Set up handlers
     */
    private void initialize() {
        buildNodeDragHandlers();
        buildLinkDragHandlers();

        top_link_handle.setOnDragDetected(mLinkHandleDragDetected);
        bottom_link_handle.setOnDragDetected(mLinkHandleDragDetected);

        top_link_handle.setOnDragDropped(mLinkHandleDragDropped);
        bottom_link_handle.setOnDragDropped(mLinkHandleDragDropped);

        mDragLink = new NodeLink();
        mDragLink.setVisible(false);

        parentPropertyListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                if (isTornDown || self == null) return;
                if (getParent() instanceof AnchorPane) right_pane = (AnchorPane) getParent();
            }
        };
        parentProperty().addListener(parentPropertyListener);

        circleKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if (isTornDown || verticalTreeView == null || verticalTreeView.getMainView() == null || formulaBox == null || formulaBox.getRTA() == null) return;
                RichTextArea rta = formulaBox.getRTA();
                if (rta.getSkin() == null) return;
                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                rtaBoundsHeight = rtaBounds.getHeight();
                rtaBoundsMinY = rtaBounds.getMinY();

                if (code == KeyCode.F10) {

                    if (circleStage < 2) {
                        if (!circled) {
                            int xPos = (int) Math.round(rta.getCaretRowColumn().getX());
                            circleIndexes[circleStage] = xPos;

                            Bounds caretBounds = ((RichTextAreaSkin) formulaBox.getRTA().getSkin()).getCaretPosition();
                            Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                            double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 1.0;
                            double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                            Label marker = circleMarkers[circleStage];
                            circleXAnchors[circleStage] = xAnchor;
                            self.getChildren().add(marker);
                            self.setLeftAnchor(marker, xAnchor);
                            self.setTopAnchor(marker, yAnchor);
                            circleStage++;
                        }
                        else EditorAlerts.fleetingRedPopup("Text field has at most one circle annotation.");
                    } else {
                        self.getChildren().removeAll(circleMarkers);
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
                if (isTornDown || verticalTreeView == null || verticalTreeView.getMainView() == null || formulaBox == null || formulaBox.getRTA() == null) return;
                RichTextArea rta = formulaBox.getRTA();
                if (rta.getSkin() == null) return;
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
                        self.getChildren().removeAll(ulineMarkers);
                        ulineStage = 0;
                    }
                    e.consume();
                }
                else if (code == KeyCode.F11) {
                    if (ulineStage == 2) {
                        double minX = Math.min(ulineXAnchors[0], ulineXAnchors[1]);
                        double maxX = Math.max(ulineXAnchors[0], ulineXAnchors[1]);
                        self.getChildren().removeAll(ulineMarkers);
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

        closeLabelClickedHandler = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeView == null || self.getParent() == null) return;
                self.requestFocus();
                AnchorPane parent  = (AnchorPane) self.getParent();
                parent.getChildren().remove(self);

                for (ListIterator<String> iterId = mLinkIds.listIterator(); iterId.hasNext();) {
                    String id = iterId.next();
                    for (ListIterator <Node> iterNode = parent.getChildren().listIterator(); iterNode.hasNext();) {
                        Node node = iterNode.next();
                        if (node.getId() == null) continue;
                        if (node.getId().equals(id)) iterNode.remove();
                    }
                    iterId.remove();
                }
                pushUndoRedo();
                parent.requestFocus();

                // SCORCHED EARTH RULE 9: Dynamic UI Deletion Fix
                self.cleanup();
            }
        };
        closeLabel.setOnMouseClicked(closeLabelClickedHandler);
    }

    private void setLineIndexes() {
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
        double minX = Math.min(circleXAnchors[0], circleXAnchors[1]);
        double maxX = Math.max(circleXAnchors[0], circleXAnchors[1]);
        self.getChildren().removeAll(circleMarkers);
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
        int intStartX = (int) Math.round(startX);
        int intEndX = (int) Math.round(endX);
        for (int i = baseline.size(); i <= intEndX; i++) {
            baseline.add(-((int) ulineSpace));
        }

        int maxBase = 0;
        for (int i = intStartX; i <= intEndX; i++) {
            if (baseline.get(i) > maxBase) {
                maxBase = baseline.get(i);
            }
        }

        double yPos = (double) maxBase + ulineSpace;
        for (int i = intStartX; i <= intEndX; i++) {
            baseline.set(i, (int) Math.round(yPos));
        }

        addLineToPane(startX, endX - startX, yPos);
    }

    public void addLineToPane(double startX, double length, double yPos) {
        Line line = new Line(0, 0, length, 0);
        linesPane.getChildren().add(line);
        linesPane.setLeftAnchor(line, startX);
        linesPane.setBottomAnchor(line, yPos);
    }

    public void registerLink(String linkId) {mLinkIds.add(linkId); }

    public void buildNodeDragHandlers() {
        mContextDragOver = new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeView == null || getParent() == null) return;
                event.acceptTransferModes(TransferMode.ANY);
                relocateToPoint(new Point2dSerial( event.getSceneX(), event.getSceneY()));
                event.consume();
            }
        };

        mContextDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeView == null || getParent() == null) return;
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                event.setDropCompleted(true);

                relocateToGridPoint2( new Point2D(event.getSceneX(), event.getSceneY())  );
                self.setCursor(Cursor.DEFAULT);
                pushUndoRedo();
            }
        };

        leftDragDetectedHandler = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeView == null || getParent() == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver (mContextDragOver);
                getParent().setOnDragDropped (mContextDragDropped);

                mDragOffset = new Point2D(event.getX(), event.getY());
                relocateToPoint( new Point2D(event.getSceneX(), event.getSceneY())  );

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
        leftDragLabel.setOnDragDetected (leftDragDetectedHandler);
    }

    private void buildLinkDragHandlers() {
        mLinkHandleDragDetected = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || verticalTreeView == null || getParent() == null || right_pane == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver(mContextLinkDragOver);
                getParent().setOnDragDropped(mContextLinkDragDropped);

                right_pane.getChildren().add(0,mDragLink);
                mDragLink.setVisible(false);

                Point2D p = new Point2D(getLayoutX() + (getWidth() / 2.0), getLayoutY() + (middleBox.getHeight()/2) + 9  );
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
                if (isTornDown || verticalTreeView == null || getParent() == null || right_pane == null) return;

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
                if (isTornDown || verticalTreeView == null || mDragLink == null) return;
                event.acceptTransferModes(TransferMode.ANY);

                if (!mDragLink.isVisible())  mDragLink.setVisible(true);
                mDragLink.setEnd(new Point2D(event.getX(), event.getY()));

                event.consume();
            }
        };

        mContextLinkDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || verticalTreeView == null || getParent() == null || right_pane == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                if (mDragLink != null) mDragLink.setVisible(false);
                if (!right_pane.getChildren().isEmpty()) right_pane.getChildren().remove(0);

                event.setDropCompleted(true);
                event.consume();
            }
        };
    }

    public void relocateToGridPoint (Point2D p) {
        if (getParent() == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0;
        relocate ((int) localCoords.getX() - 36, (int) (localY - 7 )   );
        self.requestFocus();
    }

    public void relocateToGridPoint2 (Point2D p) {
        if (getParent() == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0 ;
        relocate ( (int) localCoords.getX(), (int) (localY - 7)  );
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
        if (verticalTreeView != null && verticalTreeView.getDefaultKeyboard() != null) {
            drta.getKeyboardSelector().valueProperty().setValue(verticalTreeView.getDefaultKeyboard());
        }
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(24);
        rta.setMinHeight(24);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), 12));  //6,12

        nodesWidthListener = (observable, oldValue, newValue) -> {
            if (isTornDown) return;
            Platform.runLater(() -> {
                if (isTornDown || verticalTreeView == null || rta == null || rta.getParent() == null) return;
                rta.getParent().requestLayout();
            });
        };
        rtaSkin.nodesWidthProperty().addListener(nodesWidthListener);

        HBox box = boxedDRTA.getBoxedRTA();

        formulaRtaKeyFilter = e -> {
            if (isTornDown || verticalTreeView == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        rta.addEventFilter(KeyEvent.ANY, formulaRtaKeyFilter);

        rta.getStylesheets().add("blueFormulaBox.css");
        rta.setPromptText("");

        formulaRtaFocusListener = (ob, ov, nv) -> {
            try {
                if (isTornDown || verticalTreeView == null || verticalTreeView.getMainView() == null) return;
                if (nv) {
                    verticalTreeView.getMainView().editorInFocus(drta, ControlType.FIELD);
                }
            } catch (Exception e) {}
        };
        rta.focusedProperty().addListener(formulaRtaFocusListener);
        return boxedDRTA;
    }

    private BoxedDRTA newAnnotationBoxedDRTA() {

        BoxedDRTA annBDRTA = new BoxedDRTA();
        RichTextArea annRTA = annBDRTA.getRTA();
        annRTA.setPrefWidth(42);
        annBDRTA.getBoxedRTA().setMaxHeight(20);
        annBDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);

        annKeyFilter = e -> {
            if (isTornDown || verticalTreeView == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        annRTA.addEventFilter(KeyEvent.ANY, annKeyFilter);

        annRTA.getStylesheets().add("slappAnnotation.css");
        annRTA.setPromptText("");

        annFocusListener = (ob, ov, nv) -> {
            try {
                if (isTornDown || verticalTreeView == null || verticalTreeView.getMainView() == null) return;
                if (nv)  verticalTreeView.getMainView().editorInFocus(annBDRTA.getDRTA(), ControlType.FIELD);
                else {
                    if (annRTA.isModified()) {
                        pushUndoRedo();
                        annRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    }
                }
            } catch (Exception e) {}
        };
        annRTA.focusedProperty().addListener(annFocusListener);

        annBDRTA.getBoxedRTA().getTransforms().add(new Scale(.75,.75));

        return annBDRTA;
    }

    void processBoxRequest(boolean add) {
        if (add) {
            if (!boxed) {
                if (middleBox != null) middleBox.setStyle("-fx-border-color: black; -fx-border-width: 1 1 1 1");
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
                if (mainBox != null) {
                    mainBox.getChildren().clear();
                    mainBox.getChildren().addAll(labelPane, centerBox);
                }
                starred = false;
                pushUndoRedo();
            }
        }
    }

    public void addStar() {
        if (mainBox == null) return;
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
                if (mainBox != null) {
                    mainBox.getChildren().clear();
                    mainBox.getChildren().addAll(labelPane, centerBox);
                }
                annotation = false;
                pushUndoRedo();
            }
        }
    }
    public void addAnnotation() {
        if (mainBox == null) return;

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
            RichTextArea rta = formulaBox.getRTA();
            if (rta != null) rta.requestFocus();
            self.addEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter);
        } else {
            undoCircleRequest();
            self.getChildren().remove(oval);
            circled = false;
            circleIndexes = new int[2];
            pushUndoRedo();
        }
    }

    public void undoCircleRequest() {
        if (circleKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter);
        self.getChildren().removeAll(circleMarkers[0], circleMarkers[1]);
        circleStage = 0;
    }

    public void processUnderlineRequest(boolean add) {
        if (add) {
            RichTextArea rta = formulaBox.getRTA();
            if (rta != null) rta.requestFocus();
            self.addEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter);
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
        if (ulineKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter);
        self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]);
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


    // SCORCHED EARTH RULE 5: Heavily Isolated cleanup
    public void cleanup() {
        // KILL SWITCH: Engaged! Prevent asynchronous JavaFX ghost events mid-teardown
        isTornDown = true;

        // SCORCHED EARTH RULE 9: Unhook Drag Listeners if destroyed dynamically while dragging
        try {
            if (this.getParent() != null) {
                if (this.getParent().getOnDragOver() == mContextDragOver) this.getParent().setOnDragOver(null);
                if (this.getParent().getOnDragDropped() == mContextDragDropped) this.getParent().setOnDragDropped(null);
                if (this.getParent().getOnDragOver() == mContextLinkDragOver) this.getParent().setOnDragOver(null);
                if (this.getParent().getOnDragDropped() == mContextLinkDragDropped) this.getParent().setOnDragDropped(null);
            }
        } catch (Exception e) {}

        try { if (circleKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter); } catch (Exception e) {}
        try { if (ulineKeyFilter != null) self.removeEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter); } catch (Exception e) {}
        try { if (parentPropertyListener != null) parentProperty().removeListener(parentPropertyListener); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) - BLOCK 1 <---
        try {
            if (formulaBox != null && formulaBox.getRTA() != null) {
                final RichTextArea rta = formulaBox.getRTA();

                try { rta.prefWidthProperty().unbind(); } catch (Exception e) {}
                try { if (nodesWidthListener != null && rta.getSkin() != null) ((RichTextAreaSkin) rta.getSkin()).nodesWidthProperty().removeListener(nodesWidthListener); } catch (Exception e) {}
                try { if (formulaRtaKeyFilter != null) rta.removeEventFilter(KeyEvent.ANY, formulaRtaKeyFilter); } catch (Exception e) {}
                try { if (formulaRtaFocusListener != null) rta.focusedProperty().removeListener(formulaRtaFocusListener); } catch (Exception e) {}

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

                try { annRta.prefWidthProperty().unbind(); } catch (Exception e) {}
                try { if (annKeyFilter != null) annRta.removeEventFilter(KeyEvent.ANY, annKeyFilter); } catch (Exception e) {}
                try { if (annFocusListener != null) annRta.focusedProperty().removeListener(annFocusListener); } catch (Exception e) {}

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

        // SCORCHED EARTH RULE 8: The CME Fix for dynamically removing inner children
        try {
            if (linesPane != null) {
                if (linesPane.getChildren() != null) {
                    List<Node> linesCopy = new ArrayList<>(linesPane.getChildren());
                    for (Node node : linesCopy) severFromParent(node);
                }
                linesPane.getChildren().clear();
            }
        } catch (Exception e) {}

        try { if (mainBox != null) mainBox.getChildren().clear(); } catch (Exception e) {}
        try { if (centerBox != null) centerBox.getChildren().clear(); } catch (Exception e) {}
        try { if (middleBox != null) middleBox.getChildren().clear(); } catch (Exception e) {}
        try { if (labelPane != null) labelPane.getChildren().clear(); } catch (Exception e) {}

        try {
            this.setOnDragOver(null);
            this.setOnDragDropped(null);
            this.setOnDragDetected(null);
            this.setOnMouseClicked(null);
        } catch (Exception e) {}

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
            }
        } catch (Exception e) {}

        try {
            if (closeLabel != null) {
                if (closeLabelEnteredHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, closeLabelEnteredHandler);
                if (closeLabelExitedHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, closeLabelExitedHandler);
                if (closeLabelClickedHandler != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, closeLabelClickedHandler);

                closeLabel.setOnMouseEntered(null);
                closeLabel.setOnMouseExited(null);
                closeLabel.setOnMouseClicked(null);
            }
        } catch (Exception e) {}

        try {
            if (mDragLink != null) {
                severFromParent(mDragLink);
            }
        } catch (Exception e) {}

        try {
            if (circleMarkers != null) {
                if (circleMarkers[0] != null) severFromParent(circleMarkers[0]);
                if (circleMarkers[1] != null) severFromParent(circleMarkers[1]);
            }
        } catch (Exception e) {}

        try {
            if (ulineMarkers != null) {
                if (ulineMarkers[0] != null) severFromParent(ulineMarkers[0]);
                if (ulineMarkers[1] != null) severFromParent(ulineMarkers[1]);
            }
        } catch (Exception e) {}

        try {
            if (oval != null) {
                severFromParent(oval);
            }
        } catch (Exception e) {}

        try { this.getChildren().clear(); } catch (Exception e) {}
        try { severFromParent(self); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { this.mLinkIds = null; } catch (Exception e) {}
        try { this.baseline = null; } catch (Exception e) {}
        try { this.ulineIndexes = null; } catch (Exception e) {}
        try { this.ulineIndexTemp = null; } catch (Exception e) {}

        // 1. Final Nullification Sweep
        try { this.verticalTreeView = null; } catch (Exception e) {}
        try { this.formulaBox = null; } catch (Exception e) {}
        try { this.annotationField = null; } catch (Exception e) {}
        try { this.mainBox = null; } catch (Exception e) {}
        try { this.centerBox = null; } catch (Exception e) {}
        try { this.middleBox = null; } catch (Exception e) {}
        try { this.labelPane = null; } catch (Exception e) {}
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
        try { this.right_pane = null; } catch (Exception e) {}
        try { this.circleMarkers = null; } catch (Exception e) {}
        try { this.ulineMarkers = null; } catch (Exception e) {}
        try { this.circleXAnchors = null; } catch (Exception e) {}
        try { this.circleIndexes = null; } catch (Exception e) {}
        try { this.ulineXAnchors = null; } catch (Exception e) {}
        try { this.oval = null; } catch (Exception e) {}
        try { this.semanticValue = null; } catch (Exception e) {}
        try { this.idString = null; } catch (Exception e) {}
        try { this.mDragOffset = null; } catch (Exception e) {}
        try { this.mType = null; } catch (Exception e) {}

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
        try { this.closeLabelEnteredHandler = null; } catch (Exception e) {}
        try { this.closeLabelExitedHandler = null; } catch (Exception e) {}
        try { this.closeLabelClickedHandler = null; } catch (Exception e) {}
        try { this.parentPropertyListener = null; } catch (Exception e) {}
        try { this.leftDragDetectedHandler = null; } catch (Exception e) {}
        try { this.nodesWidthListener = null; } catch (Exception e) {}
        try { this.formulaRtaKeyFilter = null; } catch (Exception e) {}
        try { this.formulaRtaFocusListener = null; } catch (Exception e) {}
        try { this.annKeyFilter = null; } catch (Exception e) {}
        try { this.annFocusListener = null; } catch (Exception e) {}
    }


    public VBox getMiddleBox() {     return middleBox;   }

    public List<String> getmLinkIds() {    return mLinkIds;  }

    public void setmLinkIds(List<String> mLinkIds) {     this.mLinkIds = mLinkIds;  }

    public String getIdString() {      return idString;  }

    public void setIdString(String idString) {
        this.idString = idString;
        setId(idString);
    }

    public BoxedDRTA getFormulaBox() {      return formulaBox;  }

    public VBox getCenterBox() {return centerBox;}

    public boolean isBoxed() {      return boxed;   }

    public boolean isStarred() {      return starred;   }

    public boolean isAnnotation() {     return annotation;  }

    public BoxedDRTA getAnnotationField() {return annotationField; }

    public boolean isCircled() {      return circled;   }

    public void setCircled(boolean circled) {      this.circled = circled;   }

    public Double[] getCircleXAnchors() {     return circleXAnchors;   }

    public void setCircleXAnchors(Double[] circleXAnchors) {     this.circleXAnchors = circleXAnchors;   }

    public int[] getCircleIndexes() {
        return circleIndexes;
    }

    public void setCircleIndexes(int[] circleIndexes) {
        this.circleIndexes = circleIndexes;
    }

    public double getRtaBoundsHeight() {    return rtaBoundsHeight;   }

    public void setRtaBoundsHeight(double rtaBoundsHeight) {    this.rtaBoundsHeight = rtaBoundsHeight;   }

    public double getRtaBoundsMinY() {     return rtaBoundsMinY;  }

    public void setRtaBoundsMinY(double rtaBoundsMinY) {     this.rtaBoundsMinY = rtaBoundsMinY;   }

    public AnchorPane getLinesPane() {     return linesPane;   }

    public List<Integer> getBaseline() {      return baseline;   }

    public void setBaseline(List<Integer> baseline) {     this.baseline = baseline;   }

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
        if (verticalTreeView != null) {
       //     verticalTreeView.getVTExercise().pushUndoRedo();
            verticalTreeView.setUndoRedoFlag(true);
            verticalTreeView.setUndoRedoFlag(false);
        }
    }

    public MBasicObjTerm getSemanticValue() {
        return semanticValue;
    }

    public void setSemanticValue(MBasicObjTerm semanticValue) {
        this.semanticValue = semanticValue;
    }
}