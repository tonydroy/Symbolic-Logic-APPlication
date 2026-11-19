/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.
If not, see <https://www.gnu.org/licenses/>.
 */

package slapp.editor.abefg_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;


public class ABEFGview implements ExerciseView<DecoratedRTA> {
    private MainWindowView mainView;
    private Label leaderLabelAB = new Label("");
    private Label leaderLabelEFG = new Label("");
    private CheckBox checkBoxA = new CheckBox("");
    private CheckBox checkBoxB = new CheckBox("");
    private CheckBox checkBoxE = new CheckBox("");
    private CheckBox checkBoxF = new CheckBox("");
    private CheckBox checkBoxG = new CheckBox("");
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private List<DecoratedRTA> contentPageList = new ArrayList<>();
    private String contentPrompt = new String();
    private Pagination pagination;
    private Button addPageButton;
    private Button removePageButton;
    private Node exerciseControlNode = new VBox();
    private HBox abBox = new HBox();
    private HBox efgBox = new HBox();
    private VBox checksBox = new VBox();
    private Font labelFont = new Font("Noto Serif Combo", 11);
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double paginationPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> choicesHeightSpinner;
    private Spinner<Double> choicesWidthSpinner;
    private Spinner<Double> paginationHeightSpinner;

    private Spinner<Double> page0HeightSpinner;
    private boolean syncingSpinners = false;
    private ChangeListener<Number> checksBoxHeightListener;
    private ChangeListener<Double> page0SpinnerListener;
    private ChangeListener<Double> paginationSpinnerListener;

    private Spinner<Double> paginationWidthSpinner;
    private Node currentSpinnerNode;
    private int pointsPossible;
    private TextField pointsEarnedTextField;
    private VBox controlBox;

    private Button staticHelpButton;
    private Stage staticHelpStage;
    private Node rightControlNode;

    private Button checkButton;
    private Text bigCheck;
    private String checkMessage;
    private Text checkedElements;
    private Label checkTriesLabel;
    private Color checkColor;
    private Color checkElementsColor;
    private boolean checkShowing = false;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private ChangeListener<Number> paginationIndexListener;
    private PauseTransition focusPause;

    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;
    private Callback<Integer, Node> paginationPageFactory;
    private EventHandler<MouseEvent> checksBoxMouseFilter;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    /**
     * Construct AB/EFG page edit view
     * @param mainView the main view
     */
    public ABEFGview(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea page0RTA = (contentPageList != null && !contentPageList.isEmpty() && contentPageList.get(0) != null)
                        ? contentPageList.get(0).getEditor() : null;

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
                } else if (checksBox != null && inHierarchy(nv, checksBox)) {
                    if (currentSpinnerNode != checksBox) {
                        currentSpinnerNode = checksBox;
                        if (choicesHeightSpinner != null && choicesWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(choicesHeightSpinner, choicesWidthSpinner);
                        }
                    }
                } else if (page0RTA != null && inHierarchy(nv, page0RTA)) {
                    if (currentSpinnerNode != page0RTA) {
                        currentSpinnerNode = page0RTA;
                        if (page0HeightSpinner != null && paginationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(page0HeightSpinner, paginationWidthSpinner);
                        }
                    }
                } else if (pagination != null && pagination.getCurrentPageIndex() > 0 && inHierarchy(nv, pagination)) {
                    if (currentSpinnerNode != pagination) {
                        currentSpinnerNode = pagination;
                        if (paginationHeightSpinner != null && paginationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(paginationHeightSpinner, paginationWidthSpinner);
                        }
                    }
                } else {
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        leaderLabelAB.setFont(labelFont); checkBoxA.setFont(labelFont); checkBoxB.setFont(labelFont);
        abBox.getChildren().addAll(leaderLabelAB, checkBoxA, checkBoxB);
        abBox.setSpacing(20);

        leaderLabelEFG.setFont(labelFont); checkBoxE.setFont(labelFont); checkBoxF.setFont(labelFont); checkBoxG.setFont(labelFont);
        efgBox.getChildren().addAll(leaderLabelEFG, checkBoxE, checkBoxF, checkBoxG);
        efgBox.setSpacing(20);

        checksBox.getChildren().addAll(abBox, efgBox);
        checksBox.setStyle("-fx-border-color: gainsboro; -fx-border-width: 2.2; -fx-background-color: white");
        checksBox.setSpacing(10);
        checksBox.setPadding(new Insets(10));
        checksBox.setFocusTraversable(true);

        this.pagination = new Pagination();
        pagination.setMaxPageIndicatorCount(5);

        paginationPageFactory = (index) -> {
            if (isTornDown || this.mainView == null || this.contentPageList == null || index < 0 || index >= this.contentPageList.size()) return new Pane();
            try {
                Node page;
                if (index == 0) {
                    DecoratedRTA drtaPage0 = contentPageList.get(index);
                    if (drtaPage0 == null || drtaPage0.getEditor() == null) return new Pane();
                    RichTextArea rtaPage0 = drtaPage0.getEditor();
                    if (!rtaPage0.getStylesheets().contains("slappTextArea.css")) rtaPage0.getStylesheets().add("slappTextArea.css");
                    rtaPage0.contentAreaWidthProperty().bind(localScalePageWidth);
                    VBox topContentPage = new VBox(3, checksBox, rtaPage0);
                    VBox.setVgrow(rtaPage0, Priority.ALWAYS);
                    VBox.setMargin(checksBox, new Insets(5,0,0,0));
                    page = topContentPage;
                } else {
                    DecoratedRTA drtaPage = contentPageList.get(index);
                    if (drtaPage == null || drtaPage.getEditor() == null) return new Pane();
                    RichTextArea rtaPage = drtaPage.getEditor();
                    if (!rtaPage.getStylesheets().contains("slappTextArea.css")) rtaPage.getStylesheets().add("slappTextArea.css");
                    rtaPage.contentAreaWidthProperty().bind(localScalePageWidth);
                    page = rtaPage;
                }

                return page;

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
     * Initialize page management for statement, comment, pagination, and choices box
     */
    void initializeViewDetails() {
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));
        double pSpinnerMin = (double) ((int) (100 * (45.0 + 63) / localScalePageHeight.get()));

        //statement
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

        //comment
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
        double paginationInitialHeight = Math.round((paginationPrefHeight - 63)/ localScalePageHeight.get() * 100);
        paginationHeightSpinner = new Spinner<>(pSpinnerMin, 999.0, paginationInitialHeight, 1.0);
        paginationHeightSpinner.setPrefWidth(65);
        paginationHeightSpinner.setDisable(false);
        paginationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        pagination.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(paginationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))),63));

        pagination.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        paginationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        paginationWidthSpinner.setPrefWidth(65);
        paginationWidthSpinner.setDisable(true);
        paginationWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        if (contentPageList != null && !contentPageList.isEmpty()) {
            contentPageList.get(0).getEditor().setPromptText(contentPrompt);
            pagination.setPageCount(contentPageList.size());
        }

        //choices (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(65);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        choicesWidthSpinner = new Spinner<>(0.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(65);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        // page 0 RTA spinner (total pagination height minus choicesBox height)
        double initialChoicesPct = Math.round((checksBox.getHeight() > 0 ? checksBox.getHeight() + 8.0 : 75.0) / localScalePageHeight.get() * 100.0);
        double page0InitialHeight = Math.max(spinnerMin, paginationInitialHeight - initialChoicesPct);
        page0HeightSpinner = new Spinner<>(spinnerMin, 999.0, page0InitialHeight, 1.0);
        page0HeightSpinner.setPrefWidth(65);
        page0HeightSpinner.setDisable(false);
        page0HeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        page0SpinnerListener = (ob, ov, nv) -> {
            if (isTornDown || syncingSpinners || nv == null || paginationHeightSpinner == null || choicesHeightSpinner == null) return;
            syncingSpinners = true;
            try {
                double choicesPct = choicesHeightSpinner.getValue() != null ? choicesHeightSpinner.getValue() : initialChoicesPct;
                paginationHeightSpinner.getValueFactory().setValue(nv + choicesPct);
            } finally {
                syncingSpinners = false;
            }
        };
        page0HeightSpinner.valueProperty().addListener(page0SpinnerListener);

        paginationSpinnerListener = (ob, ov, nv) -> {
            if (isTornDown || syncingSpinners || nv == null || page0HeightSpinner == null || choicesHeightSpinner == null) return;
            syncingSpinners = true;
            try {
                double choicesPct = choicesHeightSpinner.getValue() != null ? choicesHeightSpinner.getValue() : initialChoicesPct;
                page0HeightSpinner.getValueFactory().setValue(Math.max(spinnerMin, nv - choicesPct));
            } finally {
                syncingSpinners = false;
            }
        };
        paginationHeightSpinner.valueProperty().addListener(paginationSpinnerListener);

        checksBoxHeightListener = (ob, ov, nv) -> {
            if (isTornDown || nv == null || nv.doubleValue() <= 0 || localScalePageHeight.get() <= 0) return;
            syncingSpinners = true;
            try {
                double choicesPct = Math.round((nv.doubleValue() + 8.0) / localScalePageHeight.get() * 100.0);
                if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null) {
                    choicesHeightSpinner.getValueFactory().setValue(choicesPct);
                }
                if (page0HeightSpinner != null && page0HeightSpinner.getValueFactory() != null && paginationHeightSpinner != null) {
                    double curMin = ((SpinnerValueFactory.DoubleSpinnerValueFactory) page0HeightSpinner.getValueFactory()).getMin();
                    page0HeightSpinner.getValueFactory().setValue(Math.max(curMin, paginationHeightSpinner.getValue() - choicesPct));
                }
            } finally {
                syncingSpinners = false;
            }
        };
        checksBox.heightProperty().addListener(checksBoxHeightListener);



        //page height listener
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

                if (checksBox != null && choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null) {
                    double cbH = checksBox.getHeight() > 0 ? checksBox.getHeight() + 8.0 : 75.0;
                    double choicesHeightValue = Math.round(cbH / nv.doubleValue() * 100.0);
                    syncingSpinners = true;
                    try {
                        choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                        if (page0HeightSpinner != null && paginationHeightSpinner != null) {
                            double page0Val = Math.max(revSpinnerMin, paginationHeightSpinner.getValue() - choicesHeightValue);
                            page0HeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, page0Val, 1.0));
                        }
                    } finally {
                        syncingSpinners = false;
                    }
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        setSizeSpinners();
    }

    public void setRightControlBox() {
        bigCheck = new Text("\ue89a");
        bigCheck.setFont(Font.font("Noto Serif Combo", 72));

        checkedElements = new Text("Choices Only");
        checkedElements.setFont(Font.font("Noto Serif Combo", 11));
        TextFlow checkedElementsFlow = new TextFlow(checkedElements);
        checkedElementsFlow.setMaxWidth(150);

        VBox bigCheckBox = new VBox(0, bigCheck, checkedElementsFlow);
        bigCheckBox.setAlignment(Pos.CENTER);
        checkedElementsFlow.setTextAlignment(TextAlignment.CENTER);

        checkButton = new Button("Check Choices");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check choices for correctness."));
        checkTriesLabel = new Label();

        VBox checksBoxRight = new VBox(10, checkButton, checkTriesLabel);
        checksBoxRight.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        VBox rightControlBox = new VBox(40, bigCheckBox, checksBoxRight, staticHelpButton);
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(40,20,0,20));
        rightControlNode = rightControlBox;
    }

    public void jumpToPage0() {
        if (isTornDown || pagination == null) return;
        if (pagination.getCurrentPageIndex() != 0) {
            pagination.setCurrentPageIndex(0);
        } else {
            focusCurrentPageRTA();
        }
    }

    public void activateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
        try {
            bigCheck.setFill(checkColor);
            checkedElements.setText(checkMessage);
            checkedElements.setFill(checkElementsColor);

            if (checkShowing) {
                FadeTransition t1 = new FadeTransition(new Duration(250), checkedElements);
                t1.setToValue(1.0);
                t1.setInterpolator(Interpolator.DISCRETE);

                FadeTransition t2 = new FadeTransition(new Duration(250), bigCheck);
                t2.setToValue(1.0);
                t2.setInterpolator(Interpolator.DISCRETE);

                ParallelTransition pt = new ParallelTransition(t1, t2);
                pt.play();
            } else {
                bigCheck.setOpacity(1.0);
                checkedElements.setOpacity(1.0);
            }
        } catch (Exception e) {}
    }

    public void deactivateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
        try {
            if (bigCheck.getOpacity() > .5) checkShowing = true;
            else checkShowing = false;
            bigCheck.setOpacity(0.0);
            checkedElements.setOpacity(0.0);
        } catch (Exception e) {}
    }

    public void showStaticHelp(Document doc) {
        if (isTornDown) return;
        if (staticHelpStage == null || !staticHelpStage.isShowing()) {
            RichTextArea hrta = new RichTextArea(EditorMain.mainStage);
            hrta.getActionFactory().open(doc).execute(new ActionEvent());
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
        if (checksBox != null) {
            checksBoxMouseFilter = e -> {
                if (isTornDown || mainView == null) return;
                if (currentSpinnerNode != checksBox) {
                    checksBox.requestFocus();
                }
            };
            checksBox.addEventFilter(MouseEvent.MOUSE_CLICKED, checksBoxMouseFilter);
        }
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

    private void focusCurrentPageRTA() {
        if (isTornDown || pagination == null) return;
        if (focusPause != null) focusPause.stop();
        focusPause = new PauseTransition(Duration.millis(180));
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


    void addBlankContentPage(int index, DecoratedRTA drta) {
        if (isTornDown || contentPageList == null || pagination == null) return;
        contentPageList.add(index, drta);
        pagination.setPageCount(contentPageList.size());
        pagination.setCurrentPageIndex(index);
        focusCurrentPageRTA();
    }

    void removeContentPage(int index) {
        if (isTornDown || contentPageList == null || pagination == null) return;
        if (index == 0) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Cannot remove top page with selection boxes.");
        } else {
            contentPageList.remove(index);
            int newSize = contentPageList.size();
            pagination.setPageCount(newSize);
            if (newSize > index) {
                pagination.setCurrentPageIndex(index);
            } else {
                pagination.setCurrentPageIndex(Math.max(0, index - 1));
            }
        }
        focusCurrentPageRTA();
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
        // KILL SWITCH: Engaged! Prevent any focus listeners from doing UI operations mid-teardown
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

        try { if (checksBox != null && checksBoxHeightListener != null) checksBox.heightProperty().removeListener(checksBoxHeightListener); } catch (Exception e) {}
        try { if (page0HeightSpinner != null && page0SpinnerListener != null) page0HeightSpinner.valueProperty().removeListener(page0SpinnerListener); } catch (Exception e) {}
        try { if (paginationHeightSpinner != null && paginationSpinnerListener != null) paginationHeightSpinner.valueProperty().removeListener(paginationSpinnerListener); } catch (Exception e) {}

        try {
            if (checksBox != null && checksBoxMouseFilter != null) checksBox.removeEventFilter(MouseEvent.MOUSE_CLICKED, checksBoxMouseFilter);
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
            if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null);
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
                try { if (stEditor.getDocument() == null || stEditor.getDocument().getText().isEmpty()) stEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}
            }

            if (commEditor != null) {
                try { commEditor.setPromptText(""); } catch (Exception e) {}
                try { commEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (commEditor.getDocument() == null || commEditor.getDocument().getText().isEmpty()) commEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}
            }

            if (helpEditor != null) {
                try { helpEditor.setPromptText(""); } catch (Exception e) {}
                try { helpEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (helpEditor.getDocument() == null || helpEditor.getDocument().getText().isEmpty()) helpEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}
            }

            for (RichTextArea rta : pageEditors) {
                if (rta != null) {
                    try { rta.setPromptText(""); } catch (Exception e) {}
                    try { rta.setFocusTraversable(false); } catch (Exception e) {}
                    try { if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}
                }
            }

            // STAGE 2 (Delayed Skin Removal)
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(Duration.millis(1500));
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
            if (abBox != null) {
                severFromParent(abBox);
                abBox.getChildren().clear();
            }
        } catch (Exception e) {}
        try {
            if (efgBox != null) {
                severFromParent(efgBox);
                efgBox.getChildren().clear();
            }
        } catch (Exception e) {}
        try {
            if (checksBox != null) {
                severFromParent(checksBox);
                checksBox.getChildren().clear();
                checksBox.setOnMouseClicked(null);
            }
        } catch (Exception e) {}
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
            if (checkButton != null) checkButton.setOnAction(null);
            if (staticHelpButton != null) staticHelpButton.setOnAction(null);
        } catch(Exception e){}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
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

        try { this.leaderLabelAB = null; } catch(Exception e){}
        try { this.leaderLabelEFG = null; } catch(Exception e){}
        try { this.checkBoxA = null; } catch(Exception e){}
        try { this.checkBoxB = null; } catch(Exception e){}
        try { this.checkBoxE = null; } catch(Exception e){}
        try { this.checkBoxF = null; } catch(Exception e){}
        try { this.checkBoxG = null; } catch(Exception e){}
        try { this.abBox = null; } catch(Exception e){}
        try { this.efgBox = null; } catch(Exception e){}
        try { this.checksBox = null; } catch(Exception e){}

        try { this.currentSpinnerNode = null; } catch(Exception e){}
        try { this.pointsEarnedTextField = null; } catch(Exception e){}
        try { this.controlBox = null; } catch(Exception e){}
        try { this.staticHelpButton = null; } catch(Exception e){}
        try { this.staticHelpStage = null; } catch(Exception e){}
        try { this.rightControlNode = null; } catch(Exception e){}
        try { this.checkButton = null; } catch(Exception e){}
        try { this.bigCheck = null; } catch(Exception e){}
        try { this.checkedElements = null; } catch(Exception e){}
        try { this.checkTriesLabel = null; } catch(Exception e){}

        try { this.exerciseFocusListener = null; } catch(Exception e){}
        try { this.localScalePageHeightListener = null; } catch(Exception e){}
        try { this.pointsEarnedFilter = null; } catch(Exception e){}
        try { this.paginationPageFactory = null; } catch(Exception e){}
        try { this.checksBoxMouseFilter = null; } catch(Exception e){}

        try { this.statementHeightSpinner = null; } catch(Exception e){}
        try { this.statementWidthSpinner = null; } catch(Exception e){}
        try { this.commentHeightSpinner = null; } catch(Exception e){}
        try { this.commentWidthSpinner = null; } catch(Exception e){}
        try { this.choicesHeightSpinner = null; } catch(Exception e){}
        try { this.choicesWidthSpinner = null; } catch(Exception e){}
        try { this.paginationHeightSpinner = null; } catch(Exception e){}
        try { this.paginationWidthSpinner = null; } catch(Exception e){}
        try { this.checksBoxHeightListener = null; } catch(Exception e){}
        try { this.page0SpinnerListener = null; } catch(Exception e){}
        try { this.paginationSpinnerListener = null; } catch(Exception e){}
        try { this.page0HeightSpinner = null; } catch(Exception e){}

        try {this.pointsEarnedFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedClickHandler = null; } catch (Exception e) {}
    }

    public Button getCheckButton() { return checkButton; }
    public Label getCheckTriesLabel() { return checkTriesLabel; }
    public void setCheckMessage(String checkMessage) { this.checkMessage = checkMessage; }
    public void setCheckColor(Color checkColor) { this.checkColor = checkColor; }
    public void setCheckElementsColor(Color checkElementsColor) { this.checkElementsColor = checkElementsColor; }
    public String getCheckMessage() { return checkMessage; }
    public Button getStaticHelpButton() { return staticHelpButton; }
    public Stage getStaticHelpStage() { return staticHelpStage; }

    Label getLeaderLabelAB() { return leaderLabelAB; }
    Label getLeaderLabelEFG() { return leaderLabelEFG; }
    CheckBox getCheckBoxA() { return checkBoxA; }
    CheckBox getCheckBoxB() { return checkBoxB; }
    CheckBox getCheckBoxE() { return checkBoxE; }
    CheckBox getCheckBoxF() { return checkBoxF; }
    CheckBox getCheckBoxG() { return checkBoxG; }

    int getContentPageIndex() { return pagination != null ? pagination.getCurrentPageIndex() : 0; }
    Button getAddPageButton() { return addPageButton; }
    Button getRemovePageButton() { return removePageButton; }
    List<DecoratedRTA> getContentPageList() { return contentPageList; }
    void setContentPageList(List<DecoratedRTA> contentPageList) { this.contentPageList = contentPageList; }
    void setContentPrompt(String prompt) { contentPrompt = prompt; }

    double getCommentPrefHeight() {
        if (exerciseComment == null || exerciseComment.getEditor() == null) return 0;
        return exerciseComment.getEditor().getPrefHeight();
    }
    void setCommentPrefHeight(double commentPrefHeight) { this.commentPrefHeight = commentPrefHeight; }

    double getPaginationPrefHeight() {
        if (pagination == null) return 0;
        return pagination.getPrefHeight();
    }
    void setPaginationPrefHeight(double paginationPrefHeight) { this.paginationPrefHeight = paginationPrefHeight; }

    public VBox getControlBox() { return controlBox; }
    public VBox getChecksBox() { return checksBox; }

    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment; }
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement; }

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
    public Node getExerciseContentNode() { return pagination; }
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

    @Override
    public int getPointsPossible() { return pointsPossible;  }
    public void setPointsPossible(int pointsPossible) { this.pointsPossible = pointsPossible; }
    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }
    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }
    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }
    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }
}