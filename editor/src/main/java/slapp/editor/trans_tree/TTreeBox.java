package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.trans_uline.ULTreeObj;
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
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.layout.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;


public class TTreeBox extends AnchorPane {
    TTreeBox self;
    TTBoxContainer ttBoxContainer;
    private AnchorPane top_link_handle;
    private AnchorPane bottom_link_handle;
    private Label leftDragLabel;
    private Label closeLabel;
    private EventHandler<MouseEvent> mLinkHandleDragDetected;
    private EventHandler<DragEvent> mLinkHandleDragDropped;
    private EventHandler<DragEvent> mContextLinkDragOver;
    private EventHandler<DragEvent> mContextLinkDragDropped;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler<DragEvent> mContextDragDropped;
    private NodeLink mDragLink = null;
    private AnchorPane right_pane = null;
    private List<String> mLinkIds = new ArrayList<>();
    private DragIconType mType = DragIconType.tree_field;
    private Point2D mDragOffset = new Point2D(0.0, 0.0);
    BoxedDRTA formulaBox;
    private HBox mainBox;
    private GridPane labelPane;
    private VBox centerBox;
    private VBox middleBox;
    private AnchorPane linesPane = new AnchorPane();
    private String idString;
    double rtaBoundsHeight;
    double rtaBoundsMinY;
    EventHandler<KeyEvent> ulineKeyFilter;
    EventHandler<MouseEvent> underlineClickFilter;
    int ulineStage = 0;
    Label[] ulineMarkers;
    Double[] ulineXAnchors = new Double[2];
    private List<ULTreeObj> treeObjects = new ArrayList<>();
    private List<Integer[]> ulineIndexes = new ArrayList<>();
    int[] ulineIndexTemp = new int[2];
    private double ulineSpace = 3.0;
    private List<Integer> baseline = new ArrayList<>();
    private boolean opened = false;
    private double printWidth;
    private List<TTreeBox> treeChildren;
    private List<TTreeBox> treeParents;
    private boolean gray;

    // SCORCHED EARTH RULE 2: Shared Kill Switch for asynchronous interception in hierarchy
    protected boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Named Event Handlers
    private EventHandler<MouseEvent> topLinkMouseEntered;
    private EventHandler<DragEvent> topLinkDragOver;
    private EventHandler<MouseEvent> topLinkMouseExited;
    private EventHandler<DragEvent> topLinkDragExited;
    private EventHandler<MouseEvent> bottomLinkMouseEntered;
    private EventHandler<DragEvent> bottomLinkDragOver;
    private EventHandler<MouseEvent> bottomLinkMouseExited;
    private EventHandler<DragEvent> bottomLinkDragExited;
    private EventHandler<MouseEvent> leftDragMouseEntered;
    private EventHandler<MouseEvent> leftDragMouseExited;
    private EventHandler<MouseEvent> leftDragDetected;
    private EventHandler<MouseEvent> closeMouseEntered;
    private EventHandler<MouseEvent> closeMouseExited;
    private EventHandler<MouseEvent> closeMouseClicked;
    private ChangeListener<Node> parentPropertyListener;
    protected ChangeListener<Number> textLengthListener;

    public TTreeBox(TTBoxContainer TTBoxContainer) {
        self = this;
        this.ttBoxContainer = TTBoxContainer;
        ulineMarkers = new Label[]{new Label("|"), new Label("|")};

        top_link_handle = new AnchorPane();
        top_link_handle.setPrefHeight(9);
        top_link_handle.setMaxWidth(30);

        topLinkMouseEntered = e -> { if (isTornDown || ttBoxContainer == null) return; top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0"); };
        top_link_handle.setOnMouseEntered(topLinkMouseEntered);

        topLinkDragOver = e -> { if (isTornDown || ttBoxContainer == null) return; top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0"); };
        top_link_handle.setOnDragOver(topLinkDragOver);

        topLinkMouseExited = e -> { if (isTornDown || ttBoxContainer == null) return; top_link_handle.setStyle("-fx-background-color: transparent"); };
        top_link_handle.setOnMouseExited(topLinkMouseExited);

        topLinkDragExited = e -> { if (isTornDown || ttBoxContainer == null) return; top_link_handle.setStyle("-fx-background-color: transparent"); };
        top_link_handle.setOnDragExited(topLinkDragExited);

        bottom_link_handle = new AnchorPane();
        bottom_link_handle.setPrefHeight(9);
        bottom_link_handle.setMaxWidth(30);
        bottom_link_handle.setStyle("-fx-background-radius: 0 0 5 5");

        bottomLinkMouseEntered = e -> { if (isTornDown || ttBoxContainer == null) return; bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5"); };
        bottom_link_handle.setOnMouseEntered(bottomLinkMouseEntered);

        bottomLinkDragOver = e -> { if (isTornDown || ttBoxContainer == null) return; bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5"); };
        bottom_link_handle.setOnDragOver(bottomLinkDragOver);

        bottomLinkMouseExited = e -> { if (isTornDown || ttBoxContainer == null) return; bottom_link_handle.setStyle("-fx-background-color: transparent"); };
        bottom_link_handle.setOnMouseExited(bottomLinkMouseExited);

        bottomLinkDragExited = e -> { if (isTornDown || ttBoxContainer == null) return; bottom_link_handle.setStyle("-fx-background-color: transparent"); };
        bottom_link_handle.setOnDragExited(bottomLinkDragExited);

        leftDragLabel = new Label("");
        leftDragLabel.setMaxWidth(10); leftDragLabel.setMinWidth(10); leftDragLabel.setMaxHeight(10); leftDragLabel.setPadding(new Insets(0));

        closeLabel = new Label();
        closeLabel.setMaxHeight(10); closeLabel.setMaxWidth(10); closeLabel.setPadding(new Insets(0));

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

        leftDragMouseEntered = e -> { if (isTornDown || ttBoxContainer == null) return; leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5;"); closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0;"); };
        leftDragLabel.setOnMouseEntered(leftDragMouseEntered);

        leftDragMouseExited = e -> { if (isTornDown || ttBoxContainer == null) return; leftDragLabel.setStyle("-fx-background-color: transparent"); closeLabel.setStyle("-fx-background-color:transparent"); };
        leftDragLabel.setOnMouseExited(leftDragMouseExited);

        closeMouseEntered = e -> { if (isTornDown || ttBoxContainer == null) return; leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5"); closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0"); };
        closeLabel.setOnMouseEntered(closeMouseEntered);

        closeMouseExited = e -> { if (isTornDown || ttBoxContainer == null) return; leftDragLabel.setStyle("-fx-background-color: transparent"); closeLabel.setStyle("-fx-background-color:transparent"); };
        closeLabel.setOnMouseExited(closeMouseExited);

        formulaBox = newFormulaBoxedDRTA();
        if (formulaBox != null && formulaBox.getBoxedRTA() != null) middleBox = new VBox(formulaBox.getBoxedRTA(), linesPane);
        else middleBox = new VBox(linesPane);

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

        buildNodeDragHandlers();
        buildLinkDragHandlers();

        top_link_handle.setOnDragDetected(mLinkHandleDragDetected);
        bottom_link_handle.setOnDragDetected(mLinkHandleDragDetected);
        top_link_handle.setOnDragDropped(mLinkHandleDragDropped);
        bottom_link_handle.setOnDragDropped(mLinkHandleDragDropped);

        mDragLink = new NodeLink();
        mDragLink.setVisible(false);

        parentPropertyListener = (ob, ov, nv) -> {
            if (isTornDown || self == null) return;
            if (nv instanceof AnchorPane) right_pane = (AnchorPane) nv;
        };
        parentProperty().addListener(parentPropertyListener);
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes Helper
    protected void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                else if (node.getParent() instanceof javafx.scene.Group) ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
            }
        } catch (Exception e) {}
    }

    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 5: Rule of Isolation applied via multiple try-catch blocks
        try { if (formulaBox != null && formulaBox.getRTA() != null && textLengthListener != null) formulaBox.getRTA().textLengthProperty().removeListener(textLengthListener); } catch (Exception e) {}
        try { if (this.parentPropertyListener != null) this.parentProperty().removeListener(parentPropertyListener); } catch (Exception e) {}
        try { this.setOnDragOver(null); } catch (Exception e) {}
        try { this.setOnDragDropped(null); } catch (Exception e) {}
        try { this.setOnDragDetected(null); } catch (Exception e) {}
        try { this.setOnMouseClicked(null); } catch (Exception e) {}

        try {
            if (top_link_handle != null) {
                if (topLinkMouseEntered != null) top_link_handle.removeEventHandler(MouseEvent.MOUSE_ENTERED, topLinkMouseEntered);
                if (topLinkMouseExited != null) top_link_handle.removeEventHandler(MouseEvent.MOUSE_EXITED, topLinkMouseExited);
                if (topLinkDragOver != null) top_link_handle.removeEventHandler(DragEvent.DRAG_OVER, topLinkDragOver);
                if (topLinkDragExited != null) top_link_handle.removeEventHandler(DragEvent.DRAG_EXITED, topLinkDragExited);
                if (mLinkHandleDragDetected != null) top_link_handle.removeEventHandler(MouseEvent.DRAG_DETECTED, mLinkHandleDragDetected);
                if (mLinkHandleDragDropped != null) top_link_handle.removeEventHandler(DragEvent.DRAG_DROPPED, mLinkHandleDragDropped);
                top_link_handle.setOnMouseEntered(null); top_link_handle.setOnMouseExited(null); top_link_handle.setOnDragDetected(null);
                top_link_handle.setOnDragDropped(null); top_link_handle.setOnDragOver(null); top_link_handle.setOnDragExited(null);
                severFromParent(top_link_handle);
            }
        } catch (Exception e) {}

        try {
            if (bottom_link_handle != null) {
                if (bottomLinkMouseEntered != null) bottom_link_handle.removeEventHandler(MouseEvent.MOUSE_ENTERED, bottomLinkMouseEntered);
                if (bottomLinkMouseExited != null) bottom_link_handle.removeEventHandler(MouseEvent.MOUSE_EXITED, bottomLinkMouseExited);
                if (bottomLinkDragOver != null) bottom_link_handle.removeEventHandler(DragEvent.DRAG_OVER, bottomLinkDragOver);
                if (bottomLinkDragExited != null) bottom_link_handle.removeEventHandler(DragEvent.DRAG_EXITED, bottomLinkDragExited);
                if (mLinkHandleDragDetected != null) bottom_link_handle.removeEventHandler(MouseEvent.DRAG_DETECTED, mLinkHandleDragDetected);
                if (mLinkHandleDragDropped != null) bottom_link_handle.removeEventHandler(DragEvent.DRAG_DROPPED, mLinkHandleDragDropped);
                bottom_link_handle.setOnMouseEntered(null); bottom_link_handle.setOnMouseExited(null); bottom_link_handle.setOnDragDetected(null);
                bottom_link_handle.setOnDragDropped(null); bottom_link_handle.setOnDragOver(null); bottom_link_handle.setOnDragExited(null);
                severFromParent(bottom_link_handle);
            }
        } catch (Exception e) {}

        try {
            if (leftDragLabel != null) {
                if (leftDragMouseEntered != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, leftDragMouseEntered);
                if (leftDragMouseExited != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, leftDragMouseExited);
                if (leftDragDetected != null) leftDragLabel.removeEventHandler(MouseEvent.DRAG_DETECTED, leftDragDetected);
                leftDragLabel.setOnMouseEntered(null); leftDragLabel.setOnMouseExited(null); leftDragLabel.setOnDragDetected(null);
                severFromParent(leftDragLabel);
            }
        } catch (Exception e) {}

        try {
            if (closeLabel != null) {
                if (closeMouseEntered != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, closeMouseEntered);
                if (closeMouseExited != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, closeMouseExited);
                if (closeMouseClicked != null) closeLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, closeMouseClicked);
                closeLabel.setOnMouseEntered(null); closeLabel.setOnMouseExited(null); closeLabel.setOnMouseClicked(null);
                severFromParent(closeLabel);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 6: Sever Shared Nodes Application
        try { if (mainBox != null) { severFromParent(mainBox); mainBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (centerBox != null) { severFromParent(centerBox); centerBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (middleBox != null) { severFromParent(middleBox); middleBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (labelPane != null) { severFromParent(labelPane); labelPane.getChildren().clear(); } } catch (Exception e) {}
        try { if (linesPane != null) { severFromParent(linesPane); linesPane.getChildren().clear(); } } catch (Exception e) {}
        try {
            if (mDragLink != null) {
                severFromParent(mDragLink);
                try { mDragLink.getClass().getMethod("cleanup").invoke(mDragLink); } catch (Exception ex) {}
            }
        } catch (Exception e) {}

        try {
            if (ulineMarkers != null) {
                for (Label marker : ulineMarkers) if (marker != null) severFromParent(marker);
            }
        } catch (Exception e) {}

        try {
            if (this.getParent() != null) {
                if (this.getParent().getOnDragOver() == mContextDragOver) this.getParent().setOnDragOver(null);
                if (this.getParent().getOnDragDropped() == mContextDragDropped) this.getParent().setOnDragDropped(null);
                if (this.getParent().getOnDragOver() == mContextLinkDragOver) this.getParent().setOnDragOver(null);
                if (this.getParent().getOnDragDropped() == mContextLinkDragDropped) this.getParent().setOnDragDropped(null);
            }
        } catch (Exception e) {}

        try { severFromParent(self); } catch (Exception e) {}
        try { this.getChildren().clear(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown.
        // 🚨 CRITICAL FIX: DO NOT CLEAR THESE LISTS. They belong to the Model!
        // Just drop the local UI references so GC can handle the UI Box.
        this.mLinkIds = null;
        this.treeObjects = null;
        this.ulineIndexes = null;
        this.baseline = null;
        this.treeChildren = null;
        this.treeParents = null;

        // SCORCHED EARTH RULE 1: Final Nullification
        this.ttBoxContainer = null;
        this.top_link_handle = null;
        this.bottom_link_handle = null;
        this.leftDragLabel = null;
        this.closeLabel = null;
        this.mLinkHandleDragDetected = null;
        this.mLinkHandleDragDropped = null;
        this.mContextLinkDragOver = null;
        this.mContextLinkDragDropped = null;
        this.mContextDragOver = null;
        this.mContextDragDropped = null;
        this.mDragLink = null;
        this.right_pane = null;
        this.mType = null;
        this.mDragOffset = null;
        this.formulaBox = null;
        this.mainBox = null;
        this.labelPane = null;
        this.centerBox = null;
        this.middleBox = null;
        this.linesPane = null;
        this.idString = null;
        this.ulineKeyFilter = null;
        this.underlineClickFilter = null;
        this.ulineMarkers = null;
        this.ulineXAnchors = null;
        this.ulineIndexTemp = null;
        this.topLinkMouseEntered = null;
        this.topLinkDragOver = null;
        this.topLinkMouseExited = null;
        this.topLinkDragExited = null;
        this.bottomLinkMouseEntered = null;
        this.bottomLinkDragOver = null;
        this.bottomLinkMouseExited = null;
        this.bottomLinkDragExited = null;
        this.leftDragMouseEntered = null;
        this.leftDragMouseExited = null;
        this.leftDragDetected = null;
        this.closeMouseEntered = null;
        this.closeMouseExited = null;
        this.closeMouseClicked = null;
        this.parentPropertyListener = null;
        this.textLengthListener = null;

        this.self = null;
    }

    public void clearLines() {
        if (isTornDown || ttBoxContainer == null) return;
        try {
            if (linesPane != null) linesPane.getChildren().clear();
            if (baseline != null) baseline.clear();
            if (treeObjects != null) treeObjects.clear();
            ttBoxContainer.setExerciseModified(true);
            ttBoxContainer.pushUndoRedo();
        } catch (Exception e) {}
    }

    public void resetLines() {
        if (isTornDown) return;
        try {
            if (linesPane != null) linesPane.getChildren().clear();
            if (baseline != null) baseline.clear();
            setupBox(treeObjects);
        } catch (Exception e) {}
    }

    void setTreeObj(int level, double lineStartX, double lineEndX) {
        if (isTornDown || ulineIndexTemp == null || treeObjects == null) return;
        try {
            int minX = Math.min(ulineIndexTemp[0], ulineIndexTemp[1]);
            int maxX = Math.max(ulineIndexTemp[0], ulineIndexTemp[1]);
            treeObjects.add(new ULTreeObj(lineStartX, lineEndX, minX, maxX, level));
        } catch (Exception e) {}
    }

    int setLine(double startX, double endX) {
        if (isTornDown || baseline == null) return -1;
        try {
            int intStartX = (int) Math.round(startX);
            int intEndX = (int) Math.round(endX);
            for (int i = baseline.size(); i <= intEndX; i++) {
                baseline.add(-1);
            }

            int maxBase = -1;
            for (int i = intStartX; i <= intEndX; i++) {
                if (baseline.get(i) > maxBase) maxBase = baseline.get(i);
            }

            int yPos = maxBase + 1;
            for (int i = intStartX; i <= intEndX; i++) baseline.set(i, yPos);

            addLineToPane(startX, endX - startX, yPos * ulineSpace);
            if (ttBoxContainer != null) ttBoxContainer.setExerciseModified(true);
            return yPos;
        } catch (Exception e) { return -1; }
    }

    public void addLineToPane(double startX, double length, double yPos) {
        if (isTornDown) return;
        try {
            if (startX >= 0 && linesPane != null) {
                Line line = new Line(0, 0, length, 0);
                linesPane.getChildren().add(line);
                linesPane.setLeftAnchor(line, startX);
                linesPane.setBottomAnchor(line, yPos);
            }
        } catch (Exception e) {}
    }

    public void registerLink(String linkId) {
        if (isTornDown || mLinkIds == null) return;
        mLinkIds.add(linkId);
    }

    public void setType(DragIconType type) { mType = type; }

    public void buildNodeDragHandlers() {
        mContextDragOver = event -> {
            if (isTornDown || ttBoxContainer == null || getParent() == null) return;
            try {
                event.acceptTransferModes(TransferMode.ANY);
                relocateToPoint(new Point2dSerial(event.getSceneX(), event.getSceneY()));
                event.consume();
            } catch (Exception e) {}
        };

        mContextDragDropped = event -> {
            if (isTornDown || ttBoxContainer == null || getParent() == null) return;
            try {
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                event.setDropCompleted(true);
                relocateToGridPoint2(new Point2D(event.getSceneX(), event.getSceneY()));
                if (self != null) self.setCursor(Cursor.DEFAULT);
                ttBoxContainer.pushUndoRedo();
            } catch (Exception e) {}
        };

        closeMouseClicked = event -> {
            if (isTornDown || ttBoxContainer == null || self == null || self.getParent() == null) return;
            try {
                self.requestFocus();
                AnchorPane parent = (AnchorPane) self.getParent();

                // SCORCHED EARTH RULE 8 & 9: Safe removal without CME (Dynamic Deletion Fix)
                List<Node> childrenCopy = new ArrayList<>(parent.getChildren());
                parent.getChildren().remove(self);

                if (mLinkIds != null) {
                    for (int i = mLinkIds.size() - 1; i >= 0; i--) {
                        String id = mLinkIds.get(i);
                        for (Node node : childrenCopy) {
                            if (node.getId() != null && node.getId().equals(id)) {
                                parent.getChildren().remove(node);
                                severFromParent(node);
                            }
                        }
                        mLinkIds.remove(i);
                    }
                }

                ttBoxContainer.pushUndoRedo();
                parent.requestFocus();

                // 🚨 SCORCHED EARTH PATCH: Seppuku! Destroy self when dynamically deleted to stop memory leak.
                self.cleanup();
            } catch (Exception e) {}
        };
        closeLabel.setOnMouseClicked(closeMouseClicked);

        leftDragDetected = event -> {
            if (isTornDown || ttBoxContainer == null || getParent() == null) return;
            try {
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                getParent().setOnDragOver(mContextDragOver);
                getParent().setOnDragDropped(mContextDragDropped);

                mDragOffset = new Point2D(event.getX(), event.getY());
                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();
                if (mType != null) container.addData("type", mType.toString());
                content.put(DragContainer.AddNode, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);

                if (formulaBox != null && formulaBox.getRTA() != null) formulaBox.getRTA().requestFocus();
                event.consume();
            } catch (Exception e) {}
        };
        leftDragLabel.setOnDragDetected(leftDragDetected);
    }

    private void buildLinkDragHandlers() {
        mLinkHandleDragDetected = event -> {
            if (isTornDown || ttBoxContainer == null || getParent() == null || right_pane == null || middleBox == null || mDragLink == null) return;
            try {
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                getParent().setOnDragOver(mContextLinkDragOver);
                getParent().setOnDragDropped(mContextLinkDragDropped);

                right_pane.getChildren().add(0, mDragLink);
                mDragLink.setVisible(false);

                Point2D p = new Point2D(getLayoutX() + (getWidth() / 2.0), getLayoutY() + (middleBox.getHeight() / 2) + 9);
                mDragLink.setStart(p);

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();
                container.addData("source", getId());
                content.put(DragContainer.AddLink, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);

                event.consume();
            } catch (Exception e) {}
        };

        mLinkHandleDragDropped = event -> {
            if (isTornDown || ttBoxContainer == null || getParent() == null || right_pane == null || mDragLink == null) return;
            try {
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                DragContainer container = (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);
                if (container == null) return;

                mDragLink.setVisible(false);
                if (!right_pane.getChildren().isEmpty()) right_pane.getChildren().remove(0);

                ClipboardContent content = new ClipboardContent();
                container.addData("target", getId());
                content.put(DragContainer.AddLink, container);

                event.getDragboard().setContent(content);
                event.setDropCompleted(true);
                event.consume();
            } catch (Exception e) {}
        };

        mContextLinkDragOver = event -> {
            if (isTornDown || ttBoxContainer == null || mDragLink == null) return;
            try {
                event.acceptTransferModes(TransferMode.ANY);
                if (!mDragLink.isVisible()) mDragLink.setVisible(true);
                mDragLink.setEnd(new Point2D(event.getX(), event.getY()));
                event.consume();
            } catch (Exception e) {}
        };

        mContextLinkDragDropped = event -> {
            if (isTornDown || ttBoxContainer == null || getParent() == null || right_pane == null || mDragLink == null) return;
            try {
                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                mDragLink.setVisible(false);
                if (!right_pane.getChildren().isEmpty()) right_pane.getChildren().remove(0);

                event.setDropCompleted(true);
                event.consume();
            } catch (Exception e) {}
        };
    }

    public void relocateToGridPoint(Point2D p) {
        if (isTornDown || getParent() == null) return;
        try {
            Point2D localCoords = getParent().sceneToLocal(p);
            double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0;
            relocate((int) localCoords.getX() - 36, (int) (localY - 7));
            if (self != null) self.requestFocus();
        } catch (Exception e) {}
    }

    public void relocateToGridPoint2(Point2D p) {
        if (isTornDown || getParent() == null) return;
        try {
            Point2D localCoords = getParent().sceneToLocal(p);
            double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0;
            relocate((int) localCoords.getX(), (int) (localY - 7));
        } catch (Exception e) {}
    }

    public void relocateToPoint(Point2D p) {
        if (isTornDown || getParent() == null) return;
        try {
            Point2D localCoords = getParent().sceneToLocal(p);
            relocate((int) localCoords.getX(), (int) ((localCoords.getY() - (getBoundsInLocal().getHeight()) / 2)));
        } catch (Exception e) {}
    }

    BoxedDRTA newFormulaBoxedDRTA() { return null; }
    public BoxedDRTA getFormulaBox() { return formulaBox; }

    public List<ULTreeObj> getTreeObjects() {
        if (isTornDown || treeObjects == null || formulaBox == null || formulaBox.getRTA() == null) return new ArrayList<>();
        try {
            if (treeObjects.size() == 0 || !treeObjects.get(0).isRoot()) {
                ULTreeObj rootObj = new ULTreeObj(-1.0, -1.0, 0, 0, -1);
                rootObj.setRoot(true);
                treeObjects.add(0, rootObj);
            }

            RichTextArea rta = formulaBox.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            String text = rta.getDocument().getText();

            ULTreeObj rootObj = treeObjects.get(0);
            rootObj.setText(text);
            rootObj.setIndexMax(text.length());
        } catch (Exception e) {}
        return treeObjects;
    }

    public void setupBox(List<ULTreeObj> treeObjs) {
        if (isTornDown || treeObjs == null || treeObjs.isEmpty() || formulaBox == null || formulaBox.getRTA() == null || baseline == null) return;
        try {
            this.treeObjects = treeObjs;

            ULTreeObj rootObj = treeObjs.get(0);
            String text = rootObj.getText();
            formulaBox.getRTA().getActionFactory().open(new Document(text)).execute(new ActionEvent());
            formulaBox.getRTA().getActionFactory().saveNow().execute(new ActionEvent());

            int maxX = 0;
            for (ULTreeObj treeObj : treeObjs) {
                if ((int) Math.round(treeObj.getLineEndX()) > maxX) maxX = (int) Math.round(treeObj.getLineEndX());
            }
            for (int i = 0; i <= maxX; i++) {
                int newBase = -1;
                for (ULTreeObj treeObj : treeObjs) {
                    if ((int) Math.round(treeObj.getLineStartX()) <= i && i <= (int) Math.round(treeObj.getLineEndX())) {
                        if (treeObj.getLevel() > newBase) newBase = treeObj.getLevel();
                    }
                }
                baseline.add(i, newBase);
            }

            for (ULTreeObj treeObj : treeObjs) {
                double startX = treeObj.getLineStartX();
                double endX = treeObj.getLineEndX();
                addLineToPane(startX, endX - startX, treeObj.getLevel() * ulineSpace);
            }

            Platform.runLater(() -> {
                if (isTornDown || formulaBox == null || formulaBox.getRTA() == null) return;
                try {
                    textLengthListener = (ob, ov, nv) -> {
                        if (isTornDown || ttBoxContainer == null) return;
                        if (opened) clearLines();
                        opened = true;
                    };
                    formulaBox.getRTA().textLengthProperty().addListener(textLengthListener);
                } catch (Exception e) {}
            });
        } catch (Exception e) {}
    }

    public VBox getMiddleBox() { return middleBox; }
    public List<String> getmLinkIds() { return mLinkIds; }
    public void setmLinkIds(List<String> mLinkIds) { this.mLinkIds = mLinkIds; }
    public String getIdString() { return idString; }
    public void setIdString(String idString) { this.idString = idString; setId(idString); }
    public VBox getCenterBox() { return centerBox; }
    public void setUlineIndexes(List<Integer[]> ulineIndexes) { this.ulineIndexes = ulineIndexes; }
    public void setTreeObjects(List<ULTreeObj> treeObjects) { this.treeObjects = treeObjects; }
    public double getPrintWidth() { return printWidth; }
    public void setPrintWidth(double printWidth) { this.printWidth = printWidth; }
    public void setBaseline(List<Integer> baseline) { this.baseline = baseline; }
    public double getRtaBoundsHeight() { return rtaBoundsHeight; }
    public void setRtaBoundsHeight(double rtaBoundsHeight) { this.rtaBoundsHeight = rtaBoundsHeight; }
    public double getRtaBoundsMinY() { return rtaBoundsMinY; }
    public void setRtaBoundsMinY(double rtaBoundsMinY) { this.rtaBoundsMinY = rtaBoundsMinY; }
    public List<Integer[]> getUlineIndexes() { return ulineIndexes; }
    public AnchorPane getLinesPane() { return linesPane; }
    public List<Integer> getBaseline() { return baseline; }
    public List<TTreeBox> getTreeChildren() { return treeChildren; }
    public void setTreeChildren(List<TTreeBox> treeChildren) { this.treeChildren = treeChildren; }
    public List<TTreeBox> getTreeParents() { return treeParents; }
    public void setTreeParents(List<TTreeBox> treeParents) { this.treeParents = treeParents; }
    public boolean isOpened() { return opened; }
    public void setOpened(boolean opened) { this.opened = opened; }
    public boolean isGray() { return gray; }
    public void setGray(boolean gray) { this.gray = gray; }
}