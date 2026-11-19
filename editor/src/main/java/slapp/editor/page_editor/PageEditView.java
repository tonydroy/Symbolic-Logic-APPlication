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

package slapp.editor.page_editor;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;
import java.util.ArrayList;
import java.util.List;

import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * View for the page edit exercise.
 */


public class PageEditView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private List<DecoratedRTA> contentPageList = new ArrayList<>();
    private String contentPrompt = new String();
    private Pagination pagination;
    private Button addPageButton;
    private Button removePageButton;
    private Node exerciseControlNode = new VBox();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double paginationPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> paginationHeightSpinner;
    private Spinner<Double> paginationWidthSpinner;
    private Node currentSpinnerNode;
    private int pointsPossible;
    private TextField pointsEarnedTextField;
    private VBox controlBox;
    private Button staticHelpButton;
    private Stage staticHelpStage;
    private Node rightControlNode;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private javafx.beans.value.ChangeListener<Node> exerciseFocusListener;
    private javafx.beans.value.ChangeListener<Number> localScalePageHeightListener;

    private javafx.beans.value.ChangeListener<Number> paginationIndexListener;
    private javafx.animation.PauseTransition focusPause;

    private java.util.function.UnaryOperator<TextFormatter.Change> pointsEarnedFilter;
    private javafx.util.Callback<Integer, Node> paginationPageFactory;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    /**
     * Construct page edit view
     * @param mainView the main view
     */

    public PageEditView(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;

                if (statementRTA != null && inHierarchy(nv, statementRTA)) {
                    if (currentSpinnerNode != statementRTA) {
                        currentSpinnerNode = statementRTA;
                        if (statementHeightSpinner != null && statementWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
                        }
                    }
                } else if (commentRTA != null && inHierarchy(nv, commentRTA)) {
                    if (currentSpinnerNode != commentRTA) {
                        currentSpinnerNode = commentRTA;
                        if (commentHeightSpinner != null && commentWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
                        }
                    }
                } else if (pagination != null && inHierarchy(nv, pagination)) {
                    if (currentSpinnerNode != pagination) {
                        currentSpinnerNode = pagination;
                        if (paginationHeightSpinner != null && paginationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(paginationHeightSpinner, paginationWidthSpinner);
                        }
                    }
                } else {
                    // THE FIX: Focus moved outside our recognized editors.
                    // Clear the cached state so the next click definitively forces a toolbar update.
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };



        this.pagination = new Pagination();
        pagination.setMaxPageIndicatorCount(5);

        paginationPageFactory = (index) -> {
            if (isTornDown || this.mainView == null || this.contentPageList == null || index < 0 || index >= this.contentPageList.size()) return new Pane();
            try {
                DecoratedRTA drtaPage = contentPageList.get(index);
                if (drtaPage == null || drtaPage.getEditor() == null) return new Pane();
                RichTextArea rtaPage = drtaPage.getEditor();
                if (!rtaPage.getStylesheets().contains("slappTextArea.css")) rtaPage.getStylesheets().add("slappTextArea.css");
                rtaPage.contentAreaWidthProperty().bind(localScalePageWidth);
                return rtaPage;
            } catch (Exception e) {
                return new Pane();
            }
        };

        pagination.setPageFactory(paginationPageFactory);

        paginationIndexListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            focusCurrentPageRTA();
        };
        pagination.currentPageIndexProperty().addListener(paginationIndexListener);

        this.addPageButton = new Button("Insert Page");
        addPageButton.setTooltip(new Tooltip("Add after current page"));
        addPageButton.setPrefWidth(100.0);

        this.removePageButton = new Button("Remove Page");
        removePageButton.setTooltip(new Tooltip("Remove current page"));
        removePageButton.setPrefWidth(100.0);

        controlBox = (VBox) exerciseControlNode;
        controlBox.setSpacing(30.0);
        controlBox.setPadding(new Insets(200,20,0,30));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        controlBox.getChildren().addAll(addPageButton, removePageButton);
        controlBox.setAlignment(Pos.TOP_CENTER);

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));

        pointsEarnedFilter = change -> {
            if (isTornDown || change == null) return null;
            String text = change.getText();
            if (text.matches("[0-9]*")) return change;
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(pointsEarnedFilter);
        pointsEarnedTextField.setTextFormatter(textFormatter);


        pointsEarnedFocusListener = (obs, wasFocused, isNowFocused) -> {
            if (isTornDown || pointsEarnedTextField == null) return;
            if (isNowFocused) {
                Platform.runLater(() -> {
                    if (!isTornDown && pointsEarnedTextField != null) {
                        pointsEarnedTextField.selectAll();
                    }
                });
                if (mainView != null) {
                    mainView.textFieldInFocus();
                }
            } else if (pointsEarnedTextField.getText().isEmpty()) {
                pointsEarnedTextField.setText("0");
            }
        };
        pointsEarnedTextField.focusedProperty().addListener(pointsEarnedFocusListener);

        pointsEarnedClickHandler = event -> {
            if (isTornDown || pointsEarnedTextField == null) return;
            if (pointsEarnedTextField.getSelectedText().isEmpty()) {
                pointsEarnedTextField.selectAll();
            }
        };
        pointsEarnedTextField.setOnMouseClicked(pointsEarnedClickHandler);
    }

    /**
     * Initialize page management for statement, comment, and pagination
     */
    void initializeViewDetails() {
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));
        double pSpinnerMin = (double) ((int) (100 * (45.0 + 63) / localScalePageHeight.get()));

        //statement rta
        RichTextArea statementRTA = exerciseStatement.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.ceil((statementPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        statementHeightSpinner = new Spinner<>(spinnerMin, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(65);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        statementRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //comment rta
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round((commentPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0 );
        commentHeightSpinner = new Spinner<>(spinnerMin, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(65);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        commentRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //pagination
        double paginationInitialHeight = Math.round((paginationPrefHeight - 63)/ localScalePageHeight.get() * 100 );
        paginationHeightSpinner = new Spinner<>(pSpinnerMin, 999.0, paginationInitialHeight, 1.0);
        paginationHeightSpinner.setPrefWidth(65);
        paginationHeightSpinner.setDisable(false);
        paginationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        pagination.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(paginationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))),63));

        pagination.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        paginationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 5.0);
        paginationWidthSpinner.setPrefWidth(65);
        paginationWidthSpinner.setDisable(true);
        paginationWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        if (contentPageList != null && !contentPageList.isEmpty()) {
            contentPageList.get(0).getEditor().setPromptText(contentPrompt);
            pagination.setPageCount(contentPageList.size());
        }

        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                double revSpinnerMin = (double) ((int) (100 * 45.0 / (double) nv));
                double revPSpinnerMin = (double) ((int) (100 * (45.0 + 63) / (double) nv));

                RichTextArea statementRTAl = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                if (statementRTAl != null && statementHeightSpinner != null && statementHeightSpinner.getValueFactory() != null) {
                    statementRTAl.prefHeightProperty().unbind();
                    statementHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    statementRTAl.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                RichTextArea commentRTAl = exerciseComment != null ? exerciseComment.getEditor() : null;
                if (commentRTAl != null && commentHeightSpinner != null && commentHeightSpinner.getValueFactory() != null) {
                    commentRTAl.prefHeightProperty().unbind();
                    commentHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    commentRTAl.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                if (pagination != null && paginationHeightSpinner != null && paginationHeightSpinner.getValueFactory() != null) {
                    pagination.prefHeightProperty().unbind();
                    paginationHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revPSpinnerMin, 999.0, (double) Math.round(paginationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    pagination.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(paginationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))),63));
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        setSizeSpinners();
    }

    public void setRightControlBox() {
        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        VBox rightControlBox = new VBox(40, staticHelpButton);
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(300,20,0,20));
        rightControlNode = rightControlBox;
    }

    public void showStaticHelp(Document doc) {
        if (isTornDown) return;
        if (staticHelpStage == null || !staticHelpStage.isShowing()) {
            RichTextArea hrta = new RichTextArea(EditorMain.mainStage);
            hrta.getActionFactory().open(doc).execute(new javafx.event.ActionEvent());
            hrta.setPadding(new Insets(20, 0, 20, 20));
            hrta.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            hrta.setContentAreaWidth(400);
            hrta.setPrefWidth(400 + RTA_WIDTH_BMP);
            hrta.setPrefHeight(300);
            hrta.setEditable(false);

            Scene scene = new Scene(hrta);
            scene.getStylesheets().add(RichTextArea.class.getClassLoader().getResource("slappEditor.css").toExternalForm());
            hrta.applyCss();
            staticHelpStage = new Stage();
            staticHelpStage.setScene(scene);
            staticHelpStage.setTitle("SLAPP Text Help");
            staticHelpStage.initModality(Modality.NONE);
            staticHelpStage.getIcons().addAll(EditorMain.icons);
            staticHelpStage.initOwner(EditorMain.mainStage);
            Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
            staticHelpStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - (420 + RTA_WIDTH_BMP)));
            staticHelpStage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 320));

            staticHelpStage.show();
        }
    }

    private void setSizeSpinners() {
        if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
        }
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) return true;
        while (node != null) {
            if (node == potentialHierarchyElement) return true;
            node = node.getParent();
        }
        return false;
    }

    void addBlankContentPage(int index, DecoratedRTA drta) {
        if (isTornDown || contentPageList == null || pagination == null) return;
        contentPageList.add(index, drta);
        pagination.setPageCount(contentPageList.size());
        pagination.setCurrentPageIndex(index);
        focusCurrentPageRTA();
    }

    void removeContentPage(int index) {
        if (isTornDown || contentPageList == null || pagination == null) return;
        contentPageList.remove(index);
        int newSize = contentPageList.size();
        pagination.setPageCount(newSize);
        if (newSize >= index) pagination.setCurrentPageIndex(index);
        else pagination.setCurrentPageIndex(Math.max(0, index - 1));
        focusCurrentPageRTA();
    }

    private void focusCurrentPageRTA() {
        if (isTornDown || pagination == null) return;
        if (focusPause != null) focusPause.stop();
        focusPause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(180));
        focusPause.setOnFinished(e -> {
            if (isTornDown || contentPageList == null || pagination == null) return;
            int idx = pagination.getCurrentPageIndex();
            if (idx >= 0 && idx < contentPageList.size()) {
                DecoratedRTA drta = contentPageList.get(idx);
                if (drta != null && drta.getEditor() != null) {
                    drta.getEditor().requestFocus();
                }
            }
        });
        focusPause.playFromStart();
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

    // SCORCHED EARTH RULE 5: Aggressive Try-Catch Isolation in cleanup()
    public void cleanup() {
        // KILL SWITCH: Engaged!
        isTornDown = true;

        try {
            if (pointsEarnedTextField != null) {
                if (pointsEarnedFocusListener != null) {
                    pointsEarnedTextField.focusedProperty().removeListener(pointsEarnedFocusListener);
                }
                if (pointsEarnedClickHandler != null) {
                    pointsEarnedTextField.removeEventHandler(MouseEvent.MOUSE_CLICKED, pointsEarnedClickHandler);
                }
                pointsEarnedTextField.setOnMouseClicked(null);
                pointsEarnedTextField.setTextFormatter(null);
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (localScalePageHeight != null) {
                if (localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener);
                localScalePageHeight.unbind();
            }
        } catch (Exception e) {}

        try {
            if (localScalePageWidth != null) localScalePageWidth.unbind();
        } catch (Exception e) {}

        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().prefHeightProperty().unbind();
                exerciseStatement.getEditor().prefWidthProperty().unbind();
                exerciseStatement.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (exerciseComment != null && exerciseComment.getEditor() != null) {
                exerciseComment.getEditor().prefHeightProperty().unbind();
                exerciseComment.getEditor().prefWidthProperty().unbind();
                exerciseComment.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (pagination != null) {
                pagination.setPageFactory(null); // Synchronously stop factory rendering
                pagination.prefHeightProperty().unbind();
                pagination.prefWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (contentPageList != null) {
                for (DecoratedRTA drta : contentPageList) {
                    if (drta != null && drta.getEditor() != null) {
                        drta.getEditor().prefHeightProperty().unbind();
                        drta.getEditor().prefWidthProperty().unbind();
                        drta.getEditor().contentAreaWidthProperty().unbind();
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (pointsEarnedTextField != null) {
                pointsEarnedTextField.setTextFormatter(null);
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea helpEditor = (staticHelpStage != null && staticHelpStage.getScene() != null
                    && staticHelpStage.getScene().getRoot() instanceof RichTextArea)
                    ? (RichTextArea) staticHelpStage.getScene().getRoot() : null;
            final Pagination pag = pagination;

            final List<RichTextArea> pageEditors = new ArrayList<>();
            if (contentPageList != null) {
                for (var drta : contentPageList) {
                    if (drta != null && drta.getEditor() != null) pageEditors.add(drta.getEditor());
                }
            }

            // STAGE 1 (Immediate)
            if (stEditor != null) {
                try { stEditor.setPromptText(""); } catch (Exception e) {}
                try { stEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (stEditor.getDocument() == null || stEditor.getDocument().getText().isEmpty()) stEditor.getActionFactory().open(new Document(" ")).execute(new javafx.event.ActionEvent()); } catch (Exception e) {}
            }

            if (commEditor != null) {
                try { commEditor.setPromptText(""); } catch (Exception e) {}
                try { commEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (commEditor.getDocument() == null || commEditor.getDocument().getText().isEmpty()) commEditor.getActionFactory().open(new Document(" ")).execute(new javafx.event.ActionEvent()); } catch (Exception e) {}
            }

            if (helpEditor != null) {
                try { helpEditor.setPromptText(""); } catch (Exception e) {}
                try { helpEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (helpEditor.getDocument() == null || helpEditor.getDocument().getText().isEmpty()) helpEditor.getActionFactory().open(new Document(" ")).execute(new javafx.event.ActionEvent()); } catch (Exception e) {}
            }

            for (RichTextArea rta : pageEditors) {
                if (rta != null) {
                    try { rta.setPromptText(""); } catch (Exception e) {}
                    try { rta.setFocusTraversable(false); } catch (Exception e) {}
                    try { if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) rta.getActionFactory().open(new Document(" ")).execute(new javafx.event.ActionEvent()); } catch (Exception e) {}
                }
            }

            // STAGE 2 (Delayed Skin Removal)
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
            pt.setOnFinished(ev -> {
                try { if (pag != null) { if (pag.getSkin() != null) pag.getSkin().dispose(); pag.setSkin(null); } } catch (Exception ex) {}
                try { if (stEditor != null) { stEditor.setSkin(null); } } catch (Exception ex) {}
                try { if (commEditor != null) { commEditor.setSkin(null); } } catch (Exception ex) {}
                try { if (helpEditor != null) { helpEditor.setSkin(null); } } catch (Exception ex) {}
                for (RichTextArea rta : pageEditors) {
                    try { if (rta != null) { rta.setSkin(null); } } catch (Exception ex) {}
                }
            });
            pt.play();
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try { if (pagination != null) severFromParent(pagination); } catch (Exception e) {}
        try {
            if (controlBox != null) {
                severFromParent(controlBox);
                controlBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (exerciseControlNode != null) {
                severFromParent(exerciseControlNode);
                if (exerciseControlNode instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) exerciseControlNode).getChildren().clear();
            }
        } catch (Exception e) {}
        try {
            if (rightControlNode != null) {
                severFromParent(rightControlNode);
                if (rightControlNode instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) rightControlNode).getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (staticHelpStage != null) {
                staticHelpStage.close();
                if (staticHelpStage.getScene() != null) {
                    staticHelpStage.getScene().setRoot(new Pane());
                }
                staticHelpStage.setScene(null);
            }
        } catch (Exception e) {}


        try {
            if (addPageButton != null) addPageButton.setOnAction(null);
            if (removePageButton != null) removePageButton.setOnAction(null);
            if (staticHelpButton != null) staticHelpButton.setOnAction(null);
        } catch(Exception e){}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        // We do not clear Model data. The local contentPageList only points to RTAs, but to be 100% safe against shared references, we just null it.
        try { this.contentPageList = null; } catch(Exception e){}

        try {
            if (focusPause != null) {
                focusPause.stop();
                focusPause.setOnFinished(null);
                focusPause = null;
            }
        } catch (Exception e) {}

        try {
            if (pagination != null && paginationIndexListener != null) {
                pagination.currentPageIndexProperty().removeListener(paginationIndexListener);
            }
            this.paginationIndexListener = null;
        } catch (Exception e) {}


        // Final severed ties
        try { this.mainView = null; } catch(Exception e){}
        try { this.exerciseStatement = null; } catch(Exception e){}
        try { this.exerciseComment = null; } catch(Exception e){}
        try { this.contentPrompt = null; } catch(Exception e){}
        try { this.pagination = null; } catch(Exception e){}
        try { this.addPageButton = null; } catch(Exception e){}
        try { this.removePageButton = null; } catch(Exception e){}
        try { this.exerciseControlNode = null; } catch(Exception e){}
        try { this.currentSpinnerNode = null; } catch(Exception e){}
        try { this.pointsEarnedTextField = null; } catch(Exception e){}
        try { this.controlBox = null; } catch(Exception e){}
        try { this.staticHelpButton = null; } catch(Exception e){}
        try { this.staticHelpStage = null; } catch(Exception e){}
        try { this.rightControlNode = null; } catch(Exception e){}

        try { this.exerciseFocusListener = null; } catch(Exception e){}
        try { this.localScalePageHeightListener = null; } catch(Exception e){}
        try { this.pointsEarnedFilter = null; } catch(Exception e){}
        try { this.paginationPageFactory = null; } catch(Exception e){}

        try { this.statementHeightSpinner = null; } catch(Exception e){}
        try { this.statementWidthSpinner = null; } catch(Exception e){}
        try { this.commentHeightSpinner = null; } catch(Exception e){}
        try { this.commentWidthSpinner = null; } catch(Exception e){}
        try { this.paginationHeightSpinner = null; } catch(Exception e){}
        try { this.paginationWidthSpinner = null; } catch(Exception e){}

        try {this.pointsEarnedFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedClickHandler = null; } catch (Exception e) {}

    }

    public Button getStaticHelpButton() { return staticHelpButton; }
    public Stage getStaticHelpStage() { return staticHelpStage; }
    int getContentPageIndex() { return pagination != null ? pagination.getCurrentPageIndex() : 0;  }
    Button getAddPageButton() { return addPageButton; }
    Button getRemovePageButton() { return removePageButton; }
    List<DecoratedRTA> getContentPageList() { return contentPageList; }
    void setContentPageList(List<DecoratedRTA> contentPageList) { this.contentPageList = contentPageList; }
    void setContentPrompt(String prompt) {   contentPrompt = prompt;  }
    void setCommentPrefHeight(double height) {this.commentPrefHeight = height; }
    void setPaginationPrefHeight(double paginationPrefHeight) { this.paginationPrefHeight = paginationPrefHeight;  }

    double getCommentPrefHeight() {
        if (exerciseComment == null || exerciseComment.getEditor() == null) return 0;
        return exerciseComment.getEditor().getPrefHeight();
    }

    double getPaginationPrefHeight() {
        if (pagination == null) return 0;
        return pagination.getPrefHeight();
    }

    public VBox getControlBox() { return controlBox; }

    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment; }
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) {  this.exerciseStatement = exerciseStatement;  }
    @Override
    public Node getExerciseStatementNode() {
        if (exerciseStatement == null) return null;
        return exerciseStatement.getEditor();
    }

    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
            exerciseStatement.getEditor().setPrefHeight(height);
        }
    }

    @Override
    public Node getExerciseContentNode() { return pagination;  }
    @Override
    public Node getExerciseControl() { return exerciseControlNode; }
    @Override
    public Node getRightControl() { return rightControlNode; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            if (mainView != null && !mainView.isInstructorFunctions() && pointsEarnedTextField != null) {
                pointsEarnedTextField.setDisable(true);
            }
            return pointsEarnedTextField;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) { this.pointsPossible = pointsPossible; }
    @Override
    public int getPointsPossible() { return pointsPossible;  }
    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }
    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }
    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }
    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }
}
