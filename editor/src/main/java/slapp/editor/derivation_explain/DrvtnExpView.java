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

package slapp.editor.derivation_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.derivation.LineType;
import slapp.editor.derivation.ViewLine;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

/**
 * View for the derivation explain exercise
 */

public class DrvtnExpView implements ExerciseView<DecoratedRTA> {
    MainWindowView mainView;
    private RichTextAreaSkin.KeyMapValue keyboardSelector;

    // CRITICAL LEAK FIX: Do not initialize here! Controller injects these.
    // Initializing them here created "phantom" DecoratedRTAs hooked permanently to global toolbars.
    private DecoratedRTA exerciseStatement;
    private DecoratedRTA exerciseComment;
    private DecoratedRTA explanationDRTA;

    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double explanationPrefHeight = 0;
    private double splitPanePrefWidth = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> explanationHeightSpinner;
    private Spinner<Double> explanationWidthSpinner;
    private Spinner<Double> splitPaneHeightSpinner;
    private Spinner<Double> splitPaneWidthSpinner;
    private Node currentSpinnerNode;

    private String contentPrompt = new String("");
    private boolean isLeftmostScopeLine = true;
    private Node exerciseControlNode = new VBox();
    private SplitPane contentSplitPane = new SplitPane();
    private GridPane grid = new GridPane();
    private VBox contentBox = new VBox();
    private List<ViewLine> viewLines = new ArrayList<>();
    private Button insertLineButton;
    private Button deleteLineButton;
    private Button indentButton;
    private Button outdentButton;
    private Button addShelfButton;
    private Button addGapButton;
    private Button insertSubButton;
    private Button insertSubsButton;
    private Button undoButton;
    private Button redoButton;
    private double contentRowHeight = 19.0;
    private double shelfRowHeight = 5.0;
    private double gapRowHeight = 7.0;

    //-------------
    private Node rightControlNode;
    private Button checkButton;
    private Button checkProgButton;
    private Button helpButton;
    private Text bigCheck;
    private String checkMessage;
    private Text checkedElements;
    private Label helpTriesLabel;
    private Label checkTriesLabel;
    private boolean checkSuccess;
    private Color checkColor;
    private Color checkElementsColor;
    private boolean checkShowing = false;
    private Button staticHelpButton;

    private Button showMetaLangButton;
    private boolean showMetaLang;
    private Stage staticHelpStage;

    // Popup RTAs to track and hammer

    private RichTextArea staticHelpRTA;

    private ParallelTransition checkTransition;

    private int pointsPossible;
    private TextField pointsEarnedTextField;
    private VBox controlBox;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 2: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private EventHandler<MouseEvent> contentSplitPaneClickHandler;
    private ChangeListener<Number> splitPaneHeightListener;
    private ChangeListener<Double> splitPaneWidthSpinnerListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private ChangeListener<Number> localScalePageWidthListener;
    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;

    // Focus Shield Listeners for Scorched Earth Teardown
    private EventHandler<MouseEvent> spinnerMousePressedFilter;
    private EventHandler<MouseEvent> spinnerMouseReleasedFilter;
    private EventHandler<KeyEvent> spinnerKeyPressedFilter;
    private EventHandler<KeyEvent> spinnerKeyReleasedFilter;
    private ChangeListener<Boolean> spinnerFocusListener;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    /**
     * Construct the derivation view
     * @param mainView the main view
     */
    public DrvtnExpView(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null ) return;
            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea explainRTA = explanationDRTA != null ? explanationDRTA.getEditor() : null;

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
                } else if (explainRTA != null && inHierarchy(nv, explainRTA)) {
                    if (currentSpinnerNode != explainRTA) {
                        currentSpinnerNode = explainRTA;
                        if (explanationHeightSpinner != null && explanationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(explanationHeightSpinner, explanationWidthSpinner);
                        }
                    }
                } else if (contentSplitPane != null && inHierarchy(nv, contentSplitPane)) {
                    if (currentSpinnerNode != contentSplitPane ) {
                        currentSpinnerNode = contentSplitPane;
                        if (splitPaneHeightSpinner != null && splitPaneWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(splitPaneHeightSpinner, splitPaneWidthSpinner);
                        }
                    }
                } else {
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        Pane blankPane = new Pane();
        blankPane.setMinWidth(RTA_WIDTH_BMP);
        blankPane.setMaxWidth(1000);
        blankPane.setStyle("-fx-background-color: white;");
        contentSplitPane.getItems().addAll(grid, blankPane);
        contentSplitPane.setOrientation(Orientation.HORIZONTAL);
        contentSplitPane.setMinHeight(10.0);
   //     contentSplitPane.setStyle("-fx-focus-color: lightblue; -fx-faint-focus-color: lightblue");

        contentSplitPane.setStyle(
                "-fx-focus-color: -fx-box-border; " +
                        "-fx-faint-focus-color: transparent; " +
                        "-fx-background-color: -fx-box-border, -fx-control-inner-background; " +
                        "-fx-background-insets: 0, 1; " +
                        "-fx-disabled-opacity: 1.0;"
        );

        ColumnConstraints fixedCol = new ColumnConstraints();
        fixedCol.setMinWidth(10);
        ColumnConstraints numCol = new ColumnConstraints();
        numCol.setMinWidth(20);
        ColumnConstraints justCol = new ColumnConstraints();
        justCol.setMinWidth(100);
        ColumnConstraints growCol = new ColumnConstraints();
        growCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(numCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, fixedCol, growCol, fixedCol, justCol);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: white;");
        grid.setMinWidth(350);
        grid.setMinHeight(10);

        insertLineButton = new Button("Insert Line");
        deleteLineButton = new Button("Delete Line");
        indentButton = new Button("Indent Line");
        outdentButton = new Button("Outdent Line");
        addShelfButton = new Button("Add Shelf");
        addGapButton = new Button("Add Gap");
        insertSubButton = new Button("Insert Subder");
        insertSubsButton = new Button("Insert Subders");
        undoButton = new Button("Undo");
        redoButton = new Button("Redo");

        insertLineButton.setPrefWidth(100); insertLineButton.setTooltip(new Tooltip("Insert blank line above current one"));
        deleteLineButton.setPrefWidth(100); deleteLineButton.setTooltip(new Tooltip("Delete current line"));
        indentButton.setPrefWidth(100); indentButton.setTooltip(new Tooltip("Increase depth of current line"));
        outdentButton.setPrefWidth(100); outdentButton.setTooltip(new Tooltip("Decrease depth of current line"));
        addShelfButton.setPrefWidth(100); addShelfButton.setTooltip(new Tooltip("Add shelf beneath current line"));
        addGapButton.setPrefWidth(100); addGapButton.setTooltip(new Tooltip("Add gap beneath current line"));
        insertSubButton.setPrefWidth(100); insertSubButton.setTooltip(new Tooltip("Insert subderivation above current line"));
        insertSubsButton.setPrefWidth(100); insertSubsButton.setTooltip(new Tooltip("Insert subderivation pair above current line"));
        undoButton.setPrefWidth(100); undoButton.setTooltip(new Tooltip("Undo last button action"));
        redoButton.setPrefWidth(100); redoButton.setTooltip(new Tooltip("Redo button action"));

        controlBox = new VBox(20, undoButton, redoButton, insertLineButton, deleteLineButton, insertSubButton, insertSubsButton, indentButton, outdentButton, addShelfButton, addGapButton);
        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBox.setMargin(insertLineButton, new Insets(20,0,0, 0));
        controlBox.setPadding(new Insets(40,20,0,20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;

        showMetaLangButton = new Button("\u2133");
        showMetaLangButton.setPrefWidth(25);
        showMetaLangButton.setMaxHeight(40);
        showMetaLangButton.setPadding(new Insets(2));
        showMetaLangButton.setTooltip(new Tooltip("Show metalanguage information."));
        showMetaLangButton.setStyle("-fx-background-color: rgb(" + 0xF0 + "," + 0xF0 + "," + 0xF0 + "); ");

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
     * Initialize statement, comment, explain field, and split pane
     */
    public void initializeViewDetails() {
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

        //statement
        if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
            RichTextArea statementRTA = exerciseStatement.getEditor();
            statementRTA.getStylesheets().add("slappTextArea.css");
            statementRTA.setEditable(false);

            double statementInitialHeight = Math.ceil((statementPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
            statementHeightSpinner = new Spinner<>(spinnerMin, 999.0, statementInitialHeight, 1.0);
            statementHeightSpinner.setPrefWidth(65);
            statementHeightSpinner.setDisable(false);
            statementHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
            statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

            if (!showMetaLang) statementRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
            else statementRTA.minWidthProperty().bind(Bindings.add(localScalePageWidthProperty(), RTA_WIDTH_BMP));
            statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        }

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //comment
        if (exerciseComment != null && exerciseComment.getEditor() != null) {
            RichTextArea commentRTA = exerciseComment.getEditor();
            commentRTA.getStylesheets().add("slappTextArea.css");
            commentRTA.setPromptText("Comment:");

            double commentInitialHeight = Math.round((commentPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
            commentHeightSpinner = new Spinner<>(spinnerMin, 999.0, commentInitialHeight, 1.0);
            commentHeightSpinner.setPrefWidth(65);
            commentHeightSpinner.setDisable(false);
            commentHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
            commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

            commentRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
            commentRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
            commentRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        }

        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //explanation
        if (explanationDRTA != null && explanationDRTA.getEditor() != null) {
            RichTextArea explanationRTA = explanationDRTA.getEditor();
            explanationRTA.getStylesheets().add("slappTextArea.css");
            explanationRTA.setPromptText(contentPrompt);
            contentBox.getChildren().addAll(contentSplitPane, explanationRTA);

            double explanationInitialHeight = Math.round((explanationPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0 );
            explanationHeightSpinner = new Spinner<>(spinnerMin, 999.0, explanationInitialHeight, 1.0);
            explanationHeightSpinner.setPrefWidth(65);
            explanationHeightSpinner.setDisable(false);
            explanationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
            explanationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(explanationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

            explanationRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
            explanationRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
            explanationRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        }

        explanationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explanationWidthSpinner.setPrefWidth(65);
        explanationWidthSpinner.setDisable(true);
        explanationWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //split pane
        double splitPaneInitialWidth = Math.round((splitPanePrefWidth - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100) ;
        splitPaneWidthSpinner = new Spinner<>(100.0, 999.0, splitPaneInitialWidth, 1.0);
        splitPaneWidthSpinner.setPrefWidth(65);
        splitPaneWidthSpinner.setDisable(false);
        splitPaneWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));
        contentSplitPane.prefWidthProperty().bind(Bindings.add(Bindings.multiply(localScalePageWidth, DoubleProperty.doubleProperty(splitPaneWidthSpinner.getValueFactory().valueProperty()).divide(100.0)), RTA_WIDTH_BMP));

        // 1. CSS Trick: Prevent visual dimming when disabled
 //       try {
  //          if (contentSplitPane != null) contentSplitPane.setStyle("-fx-disabled-opacity: 1.0;");
  //      } catch (Exception e) {}

        // 2. Define Named Mouse Filters
        spinnerMousePressedFilter = e -> {
            if (isTornDown || contentSplitPane == null) return;
            try {
                contentSplitPane.setDisable(true);
            } catch (Exception ex) {}
        };

        spinnerMouseReleasedFilter = e -> {
            if (isTornDown || contentSplitPane == null) return;
            try {
                contentSplitPane.setDisable(false);
            } catch (Exception ex) {}
        };

        // 3. Define Named Keyboard Filters
        spinnerKeyPressedFilter = e -> {
            if (isTornDown || contentSplitPane == null) return;
            try {
                if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                    contentSplitPane.setDisable(true);
                }
            } catch (Exception ex) {}
        };

        spinnerKeyReleasedFilter = e -> {
            if (isTornDown || contentSplitPane == null) return;
            try {
                if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                    contentSplitPane.setDisable(false);
                }
            } catch (Exception ex) {}
        };

        // 4. Define Named Focus Safeguard
        spinnerFocusListener = (obs, oldVal, newVal) -> {
            if (isTornDown || contentSplitPane == null) return;
            try {
                if (Boolean.FALSE.equals(newVal)) {
                    contentSplitPane.setDisable(false);
                }
            } catch (Exception ex) {}
        };

        // 5. Attach them safely to the Spinner
        try {
            if (splitPaneWidthSpinner != null) {
                splitPaneWidthSpinner.addEventFilter(MouseEvent.MOUSE_PRESSED, spinnerMousePressedFilter);
                splitPaneWidthSpinner.addEventFilter(MouseEvent.MOUSE_RELEASED, spinnerMouseReleasedFilter);
                splitPaneWidthSpinner.addEventFilter(KeyEvent.KEY_PRESSED, spinnerKeyPressedFilter);
                splitPaneWidthSpinner.addEventFilter(KeyEvent.KEY_RELEASED, spinnerKeyReleasedFilter);
                splitPaneWidthSpinner.focusedProperty().addListener(spinnerFocusListener);
            }
        } catch (Exception e) {}

        contentSplitPane.setMinHeight(200);
        splitPaneHeightSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        splitPaneHeightSpinner.setPrefWidth(65);
        splitPaneHeightSpinner.setDisable(true);
        splitPaneHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        splitPaneHeightListener = (ob, ov, nv) -> {
            if (isTornDown || splitPaneHeightSpinner == null || contentSplitPane == null) return;
            try {
                splitPaneHeightSpinner.getValueFactory().setValue((double) Math.round(contentSplitPane.getHeight() / localScalePageHeight.get() * 100));
            } catch (Exception e) {}
        };
        contentSplitPane.heightProperty().addListener(splitPaneHeightListener);


        //page size listeners
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || exerciseStatement == null || exerciseStatement.getEditor() == null || exerciseComment == null || exerciseComment.getEditor() == null || explanationDRTA == null || explanationDRTA.getEditor() == null) return;
            try {
                RichTextArea statementRTA = exerciseStatement.getEditor();
                RichTextArea commentRTA = exerciseComment.getEditor();
                RichTextArea explanationRTA = explanationDRTA.getEditor();

                double revSpinnerMin = (double) ((int) (100 * 45.0 / (double) nv));

                statementRTA.prefHeightProperty().unbind();
                if (statementHeightSpinner != null && statementHeightSpinner.getValueFactory() != null) {
                    statementHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                commentRTA.prefHeightProperty().unbind();
                if (commentHeightSpinner != null && commentHeightSpinner.getValueFactory() != null) {
                    commentHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                explanationRTA.prefHeightProperty().unbind();
                if (explanationHeightSpinner != null && explanationHeightSpinner.getValueFactory() != null) {
                    explanationHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(explanationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    explanationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explanationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                if (splitPaneHeightSpinner != null && splitPaneHeightSpinner.getValueFactory() != null && contentSplitPane != null) {
                    splitPaneHeightSpinner.getValueFactory().setValue((double) Math.round(contentSplitPane.getHeight() / localScalePageHeight.get() * 100.0));
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        localScalePageWidthListener = (ob, ov, nv) -> {
            if (isTornDown || contentSplitPane == null || splitPaneWidthSpinner == null) return;
            try {
                contentSplitPane.prefWidthProperty().unbind();
                if (splitPaneWidthSpinner.getValueFactory() != null) {
                    splitPaneWidthSpinner.getValueFactory().setValue((double) Math.round(splitPaneWidthSpinner.getValue() * ov.doubleValue() / nv.doubleValue() / 5.0) * 5.0);
                    contentSplitPane.prefWidthProperty().bind(Bindings.add(Bindings.multiply(localScalePageWidth, DoubleProperty.doubleProperty(splitPaneWidthSpinner.getValueFactory().valueProperty()).divide(100.0)), RTA_WIDTH_BMP));
                }
            } catch (Exception e) {}
        };
        localScalePageWidth.addListener(localScalePageWidthListener);

        setSizeSpinners();
    }

    public void setRightControlBox() {
        //right control box
        bigCheck = new Text("\ue89a");
        bigCheck.setFont(Font.font("Noto Serif Combo", 72));

        checkedElements = new Text(checkMessage);
        checkedElements.setFont(Font.font("Noto Serif Combo", 11));
        TextFlow checkedElementsFlow = new TextFlow(checkedElements);
        checkedElementsFlow.setMaxWidth(150);

        VBox bigCheckBox = new VBox(0, bigCheck, checkedElementsFlow);
        bigCheckBox.setAlignment(Pos.CENTER);
        checkedElementsFlow.setTextAlignment(TextAlignment.CENTER);

        checkButton = new Button("Check Final");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check complete derivation for correctness."));
        checkTriesLabel = new Label();

        checkProgButton = new Button("Check Progress");
        checkProgButton.setPrefWidth(105);
        checkProgButton.setTooltip(new Tooltip("Check derivation progress so far."));
        VBox checksBox = new VBox(10, checkProgButton, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);
        checksBox.setMargin(checkProgButton, new Insets(0,0,10, 0));

        helpButton = new Button("Contextual Help");
        helpButton.setPrefWidth(105);
        helpButton.setTooltip(new Tooltip("Get help for selected goal"));
        helpTriesLabel = new Label();

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        VBox helpBox = new VBox(10, helpButton, helpTriesLabel, staticHelpButton );
        helpBox.setAlignment(Pos.CENTER);
        helpTriesLabel.setAlignment(Pos.CENTER);
        helpBox.setMargin(staticHelpButton, new Insets(10,0,0,0));

        VBox rightControlBox = new VBox(40, bigCheckBox, checksBox, helpBox );
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(60,20,0,20));
        rightControlNode = rightControlBox;

        deactivateBigCheck();
    }
    private void setSizeSpinners() {
        contentSplitPaneClickHandler = e -> {
            if (isTornDown || contentSplitPane == null) return;
            try {
                // 1. Unconditionally force the UI to update just in case it was swallowed
                if (mainView != null) {
                    mainView.updateSizeSpinners(splitPaneHeightSpinner, splitPaneWidthSpinner);
                }

                // 2. Safely grab focus if we aren't already registered here.
                // If the user clicked a RichTextArea, the event will continue downwards
                // and correctly give the text area focus right after this.
                if (currentSpinnerNode != contentSplitPane) {
                    currentSpinnerNode = contentSplitPane;
                    contentSplitPane.requestFocus();
                }
            } catch (Exception ex) {}
        };

        // CRITICAL FIX: Use MOUSE_PRESSED! This fires instantly before text areas can consume the event.
        contentSplitPane.addEventFilter(MouseEvent.MOUSE_PRESSED, contentSplitPaneClickHandler);

        if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);

            // JUMPSTART INITIAL FOCUS (Highly Recommended)
            // This ensures the spinners sync up immediately when the view opens.
            javafx.application.Platform.runLater(() -> {
                try {
                    if (!isTornDown && mainView != null && mainView.getMainScene() != null) {
                        Node currentFocus = mainView.getMainScene().getFocusOwner();
                        if (inHierarchy(currentFocus, contentSplitPane)) {
                            currentSpinnerNode = contentSplitPane;
                            mainView.updateSizeSpinners(splitPaneHeightSpinner, splitPaneWidthSpinner);
                        }
                    }
                } catch (Exception ex) {}
            });
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

    /**
     * Update visible grid from view lines list
     */
    public void setGridFromViewLines() {
        if (isTornDown || grid == null || viewLines == null) return;

        // SCORCHED EARTH RULE 8: Safely sever dynamically generated UI spacers to prevent leaks
        try {
            if (grid.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(grid.getChildren());
                for (Node node : childrenCopy) {
                    severFromParent(node);
                }
            }
            grid.getChildren().clear();
            ObservableList<RowConstraints> gridRowConstraints = grid.getRowConstraints();
            gridRowConstraints.clear();

            int lineNumber = 1;
            for (int index = 0; index < viewLines.size(); index++) {
                ViewLine viewLine = viewLines.get(index);
                LineType lineType = viewLine.getLineType();

                RowConstraints constraint = new RowConstraints();
                if (LineType.isContentLine(lineType)) { constraint.setMinHeight(contentRowHeight); constraint.setMaxHeight(contentRowHeight); }
                if (LineType.isGapLine(lineType)) {constraint.setMinHeight(gapRowHeight); constraint.setMaxHeight(gapRowHeight); }
                if (LineType.isShelfLine(lineType)) {constraint.setMinHeight(shelfRowHeight); constraint.setMaxHeight(shelfRowHeight); }

                if (index >= gridRowConstraints.size()) gridRowConstraints.add(index, constraint);
                else gridRowConstraints.set(index, constraint);

                Label lineNumberLabel = viewLine.getLineNumberLabel();
                if (lineNumberLabel != null) {
                    lineNumberLabel.setText(Integer.toString(lineNumber++));
                    HBox numBox = new HBox(lineNumberLabel, new Label(". "));
                    numBox.setAlignment(Pos.BASELINE_RIGHT);
                    grid.add(numBox, 0, index, 1, 1);
                }

                int depth = viewLine.getDepth();
                HBox contentBoxItem = new HBox();
                if (viewLine.getLineContentBoxedDRTA() != null) {
                    BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                    DecoratedRTA drta = bdrta.getDRTA();
                    if (drta != null && drta.getKeyboardSelector() != null && drta.getKeyboardSelector().valueProperty() != null) drta.getKeyboardSelector().valueProperty().setValue(keyboardSelector);
                    RichTextArea rta = bdrta.getRTA();
                    if (rta != null) {
                        rta.setMaxHeight(contentRowHeight);
                        rta.setMinHeight(contentRowHeight);
                        rta.setPrefWidth(100);

                        // CRITICAL LEAK FIX: Prevent infinite Stylesheet String Accumulation on Grid Rebuild
                        rta.getStylesheets().clear();

                        if (viewLine.isLineHighlight()) {
                            rta.getStylesheets().add("slappDerivationHighlight.css");
                        } else {
                            rta.getStylesheets().add("slappDerivation.css");
                        }
                    }
                    contentBoxItem = bdrta.getBoxedRTA();
                    if (contentBoxItem != null) contentBoxItem.setHgrow(bdrta.getRTA(), Priority.ALWAYS);
                }

                if (depth > 1) {
                    Pane spacer1 = new Pane();
                    if (isLeftmostScopeLine) spacer1.setStyle("-fx-border-color: white white white black; -fx-border-width: 0 0 0 1;");
                    else spacer1.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0;");
                    grid.add(spacer1, 1, index, 1, 1);

                    for (int i = 2; i < depth; i++) {
                        Pane spacer = new Pane();
                        spacer.setStyle("-fx-border-color: white white white black; -fx-border-width: 0 0 0 1;");
                        grid.add(spacer, i, index, 1, 1);
                    }
                }
                if (LineType.isContentLine(lineType)) {
                    if (isLeftmostScopeLine || depth > 1)
                        contentBoxItem.setStyle("-fx-border-color: white white white black; -fx-border-width: 0 0 0 1;");
                    else contentBoxItem.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0;");

                    grid.add(contentBoxItem, depth, index, 21 - depth, 1);

                } else {
                    Pane endSpacer = new Pane();
                    if (LineType.isGapLine(lineType)) endSpacer.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0;");
                    if (LineType.isShelfLine(lineType) && (depth > 1 || isLeftmostScopeLine))  endSpacer.setStyle("-fx-border-color: black white white black; -fx-border-width: 1 0 0 1;");
                    grid.add(endSpacer, depth, index, 1, 1);
                }

                if (viewLine.getJustificationFlow() != null) grid.add(viewLine.getJustificationFlow(), 22, index);
            }
        } catch (Exception e) {}
    }

    public void activateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
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

            if (checkTransition != null) checkTransition.stop();
            checkTransition = new ParallelTransition(t1, t2);
            checkTransition.play();
        }
        else {
            bigCheck.setOpacity(1.0);
            checkedElements.setOpacity(1.0);
        }
    }

    //record status, and deactivate
    public void deactivateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
        if (bigCheck.getOpacity() > .5) checkShowing = true;
        else checkShowing = false;
        bigCheck.setOpacity(0.0);
        checkedElements.setOpacity(0.0);
    }


    public void showStaticHelp(Document doc) {
        if (isTornDown) return;
        if (staticHelpStage == null || !staticHelpStage.isShowing()) {
            staticHelpRTA = new RichTextArea(EditorMain.mainStage);
            staticHelpRTA.getActionFactory().open(doc).execute(new ActionEvent());
            staticHelpRTA.setPadding(new Insets(20, 0, 20, 20));
            staticHelpRTA.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            staticHelpRTA.setContentAreaWidth(400);
            staticHelpRTA.setPrefWidth(400 + RTA_WIDTH_BMP);
            staticHelpRTA.setPrefHeight(300);
            staticHelpRTA.setEditable(false);

            Scene scene = new Scene(staticHelpRTA);
            scene.getStylesheets().add(RichTextArea.class.getClassLoader().getResource("slappEditor.css").toExternalForm());
            staticHelpRTA.applyCss();
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

            // CRITICAL FIX: Hammer the popup RTAs when closed to prevent dynamic stage leaks!

            staticHelpStage.setOnHidden(e -> {
                try {
                    final RichTextArea rtaToClear = staticHelpRTA;
                    staticHelpRTA = null;
                    staticHelpStage = null;
                    if (rtaToClear != null) {
                        rtaToClear.setPromptText("");
                        rtaToClear.setFocusTraversable(false);
                        if (rtaToClear.getDocument() == null || rtaToClear.getDocument().getText().isEmpty()) {
                            rtaToClear.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                        }
                        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(Duration.millis(1500));
                        pt.setOnFinished(ev -> {
                            try { rtaToClear.setSkin(null); } catch (Exception ex) {}
                        });
                        pt.play();
                    }
                } catch (Exception ex) {}
            });
        }
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

    // SCORCHED EARTH RULE 1: Cascading Cleanup
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

        try { if (checkTransition != null) checkTransition.stop(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation Break Down
        try {
            if (contentSplitPane != null && contentSplitPaneClickHandler != null) {
                contentSplitPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, contentSplitPaneClickHandler);
            }
        } catch (Exception e) {}

        try {
            if (contentSplitPane != null && splitPaneHeightListener != null) {
                contentSplitPane.heightProperty().removeListener(splitPaneHeightListener);
            }
        } catch (Exception e) {}

        try {
            if (splitPaneWidthSpinner != null && splitPaneWidthSpinner.valueProperty() != null && splitPaneWidthSpinnerListener != null) {
                splitPaneWidthSpinner.valueProperty().removeListener(splitPaneWidthSpinnerListener);
            }
        } catch (Exception e) {}

        try {
            if (splitPaneWidthSpinner != null && spinnerMousePressedFilter != null) {
                splitPaneWidthSpinner.removeEventFilter(MouseEvent.MOUSE_PRESSED, spinnerMousePressedFilter);
            }
        } catch (Exception e) {}

        try {
            if (splitPaneWidthSpinner != null && spinnerMouseReleasedFilter != null) {
                splitPaneWidthSpinner.removeEventFilter(MouseEvent.MOUSE_RELEASED, spinnerMouseReleasedFilter);
            }
        } catch (Exception e) {}

        try {
            if (splitPaneWidthSpinner != null && spinnerKeyPressedFilter != null) {
                splitPaneWidthSpinner.removeEventFilter(KeyEvent.KEY_PRESSED, spinnerKeyPressedFilter);
            }
        } catch (Exception e) {}

        try {
            if (splitPaneWidthSpinner != null && spinnerKeyReleasedFilter != null) {
                splitPaneWidthSpinner.removeEventFilter(KeyEvent.KEY_RELEASED, spinnerKeyReleasedFilter);
            }
        } catch (Exception e) {}

        try {
            if (splitPaneWidthSpinner != null && splitPaneWidthSpinner.focusedProperty() != null && spinnerFocusListener != null) {
                splitPaneWidthSpinner.focusedProperty().removeListener(spinnerFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (localScalePageHeight != null) {
                if (localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener);
                localScalePageHeight.unbind();
            }
        } catch (Exception e) {}

        try {
            if (localScalePageWidth != null) {
                if (localScalePageWidthListener != null) localScalePageWidth.removeListener(localScalePageWidthListener);
                localScalePageWidth.unbind();
            }
        } catch (Exception e) {}

        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().prefHeightProperty().unbind();
                exerciseStatement.getEditor().maxWidthProperty().unbind();
                exerciseStatement.getEditor().minWidthProperty().unbind();
                exerciseStatement.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (exerciseComment != null && exerciseComment.getEditor() != null) {
                exerciseComment.getEditor().prefHeightProperty().unbind();
                exerciseComment.getEditor().prefWidthProperty().unbind();
                exerciseComment.getEditor().maxWidthProperty().unbind();
                exerciseComment.getEditor().minWidthProperty().unbind();
                exerciseComment.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (explanationDRTA != null && explanationDRTA.getEditor() != null) {
                explanationDRTA.getEditor().prefHeightProperty().unbind();
                explanationDRTA.getEditor().prefWidthProperty().unbind();
                explanationDRTA.getEditor().maxWidthProperty().unbind();
                explanationDRTA.getEditor().minWidthProperty().unbind();
                explanationDRTA.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea expEditor = (explanationDRTA != null) ? explanationDRTA.getEditor() : null;

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (stEditor != null) rtasToClear.add(stEditor);
            if (commEditor != null) rtasToClear.add(commEditor);
            if (expEditor != null) rtasToClear.add(expEditor);

            if (staticHelpRTA != null) rtasToClear.add(staticHelpRTA);

            if (!rtasToClear.isEmpty()) {
                // STAGE 1 (Immediate): Prompt wipe, focus disable, and document injection
                for (RichTextArea rta : rtasToClear) {
                    if (rta != null) {
                        try { rta.setPromptText(""); } catch (Exception e) {}
                        try { rta.setFocusTraversable(false); } catch (Exception e) {}
                        try {
                            if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                                rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                            }
                        } catch (Exception e) {}
                    }
                }

                // STAGE 2 (Delayed Skin Removal): Push skin removal 1500ms into the future
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    for (RichTextArea rta : rtasToClear) {
                        try {
                            if (rta != null) {
                                rta.setSkin(null);
                            }
                        } catch (Exception ex) {}
                    }
                });
                pt.play();
            }
        } catch (Exception e) {}

        // Ensure DecoratedRTAs run their own cleanup logic
        try { if (exerciseStatement != null) exerciseStatement.cleanup(); } catch (Exception e) {}
        try { if (exerciseComment != null) exerciseComment.cleanup(); } catch (Exception e) {}
        try { if (explanationDRTA != null) explanationDRTA.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 8: Safely sever Grid Nodes to prevent CME
        try {
            if (grid != null) {
                if (grid.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(grid.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                grid.getChildren().clear();
                grid.getRowConstraints().clear();
                grid.getColumnConstraints().clear();
                severFromParent(grid);
            }
        } catch (Exception e) {}

        try {
            if (contentSplitPane != null) {
                contentSplitPane.prefWidthProperty().unbind();
                if (contentSplitPane.getItems() != null) {
                    List<Node> itemsCopy = new ArrayList<>(contentSplitPane.getItems());
                    for (Node node : itemsCopy) severFromParent(node);
                }
                contentSplitPane.getItems().clear();
                severFromParent(contentSplitPane);
            }
        } catch (Exception e) {}

        try {
            if (contentBox != null) {
                if (contentBox.getChildren() != null) {
                    List<Node> itemsCopy = new ArrayList<>(contentBox.getChildren());
                    for (Node node : itemsCopy) severFromParent(node);
                }
                contentBox.getChildren().clear();
                severFromParent(contentBox);
            }
        } catch (Exception e) {}

        // DISMANTLE UI ACTIONS
        try {
            if (insertLineButton != null) insertLineButton.setOnAction(null);
            if (deleteLineButton != null) deleteLineButton.setOnAction(null);
            if (indentButton != null) indentButton.setOnAction(null);
            if (outdentButton != null) outdentButton.setOnAction(null);
            if (addShelfButton != null) addShelfButton.setOnAction(null);
            if (addGapButton != null) addGapButton.setOnAction(null);
            if (insertSubButton != null) insertSubButton.setOnAction(null);
            if (insertSubsButton != null) insertSubsButton.setOnAction(null);
            if (undoButton != null) undoButton.setOnAction(null);
            if (redoButton != null) redoButton.setOnAction(null);
            if (checkButton != null) checkButton.setOnAction(null);
            if (checkProgButton != null) checkProgButton.setOnAction(null);
            if (helpButton != null) helpButton.setOnAction(null);
            if (staticHelpButton != null) staticHelpButton.setOnAction(null);
            if (showMetaLangButton != null) showMetaLangButton.setOnAction(null);
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (pointsEarnedTextField != null) {
                pointsEarnedTextField.setTextFormatter(null);
            }
        } catch (Exception e) {}

        try {
            if (exerciseControlNode != null) {
                severFromParent(exerciseControlNode);
            }
        } catch (Exception e) {}

        try {
            if (controlBox != null) {
                severFromParent(controlBox);
                controlBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (rightControlNode != null) {
                severFromParent(rightControlNode);
            }
        } catch (Exception e) {}

        try {
            if (staticHelpStage != null) {
                staticHelpStage.close();
            }
        } catch (Exception e) {}



        // SCORCHED EARTH RULE 10: Non-destructive Data Teardown
        try { if (viewLines != null) viewLines = null; } catch (Exception e) {}

        // Final Explicit Nullifications
        this.mainView = null;
        this.keyboardSelector = null;
        this.exerciseStatement = null;
        this.exerciseComment = null;
        this.explanationDRTA = null;
        this.statementHeightSpinner = null;
        this.statementWidthSpinner = null;
        this.commentHeightSpinner = null;
        this.commentWidthSpinner = null;
        this.explanationHeightSpinner = null;
        this.explanationWidthSpinner = null;
        this.splitPaneHeightSpinner = null;
        this.splitPaneWidthSpinner = null;
        this.currentSpinnerNode = null;

        this.contentPrompt = null;
        this.exerciseControlNode = null;
        this.contentSplitPane = null;
        this.grid = null;
        this.contentBox = null;

        this.insertLineButton = null;
        this.deleteLineButton = null;
        this.indentButton = null;
        this.outdentButton = null;
        this.addShelfButton = null;
        this.addGapButton = null;
        this.insertSubButton = null;
        this.insertSubsButton = null;
        this.undoButton = null;
        this.redoButton = null;
        this.rightControlNode = null;
        this.checkButton = null;
        this.checkProgButton = null;
        this.helpButton = null;
        this.bigCheck = null;
        this.checkedElements = null;
        this.helpTriesLabel = null;
        this.checkTriesLabel = null;
        this.staticHelpButton = null;
        this.showMetaLangButton = null;
        this.staticHelpStage = null;
        this.staticHelpRTA = null;
        this.pointsEarnedTextField = null;
        this.controlBox = null;
        this.checkTransition = null;

        this.exerciseFocusListener = null;
        this.contentSplitPaneClickHandler = null;
        this.splitPaneHeightListener = null;
        this.splitPaneWidthSpinnerListener = null;
        this.localScalePageHeightListener = null;
        this.localScalePageWidthListener = null;
        this.pointsEarnedFilter = null;
        this.pointsEarnedFocusListener = null;
        this.pointsEarnedClickHandler = null;

        try {
            this.spinnerMousePressedFilter = null;
            this.spinnerMouseReleasedFilter = null;
            this.spinnerKeyPressedFilter = null;
            this.spinnerKeyReleasedFilter = null;
            this.spinnerFocusListener = null;
        } catch (Exception e) {}
    }



    public Stage getStaticHelpStage() {
        return staticHelpStage;
    }

    /**
     * Set justification flow from a single view line on grid
     * @param index the line index
     */
    void setJustificationFlowOnGrid(int index) {
        if (isTornDown || viewLines == null || index < 0 || index >= viewLines.size() || grid == null) return;
        try {
            ViewLine line = viewLines.get(index);
            if (line != null && line.getJustificationFlow() != null) grid.add(line.getJustificationFlow(), 22, index);
        } catch (Exception e) {}
    }

    /**
     * The main grid for the derivation
     * @return the grid pane
     */
    GridPane getGrid() { return grid; }

    /**
     * Switch to to say whether the derivation should have a leftmost scope line
     * @param leftmostScopeLine true if there is a leftmost line and otherwise false
     */
    void setLeftmostScopeLine(boolean leftmostScopeLine) { isLeftmostScopeLine = leftmostScopeLine;  }

    /**
     * The default formula keyboard for the derivation
     * @param keyboardSelector {@link com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue}
     */
    void setKeyboardSelector(RichTextAreaSkin.KeyMapValue keyboardSelector) {this.keyboardSelector = keyboardSelector;}

    /**
     * The list from which derivation grid is populated
     * @return the list of {@link slapp.editor.derivation.ViewLine}
     */
    List<ViewLine> getViewLines() { return viewLines; }

    /**
     * The list from which the derivation grid is populated
     * @param viewLines the list of {@link slapp.editor.derivation.ViewLine}
     */
    void setViewLines(List<ViewLine> viewLines) {this.viewLines = viewLines; }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;    }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() { return exerciseComment != null && exerciseComment.getEditor() != null ? exerciseComment.getEditor().getPrefHeight() : 0.0; }

    /**
     * The preferred height of the explanation window
     * @param explanationPrefHeight the height value
     */
    void setExplanationPrefHeight(double explanationPrefHeight) {    this.explanationPrefHeight = explanationPrefHeight;    }

    /**
     * The preferred height of the explanation window
     * @return the height value
     */
    double getExplanationPrefHeight() {return explanationDRTA != null && explanationDRTA.getEditor() != null ? explanationDRTA.getEditor().getPrefHeight() : 0.0; }

    /**
     * The preferred width of the split pane window
     * @param splitPanePrefWidth the width value
     */
    void setSplitPanePrefWidth(double splitPanePrefWidth) {     this.splitPanePrefWidth = splitPanePrefWidth;    }

    /**
     * The preferred width of the split pane window
     * @return the width value
     */
    double getSplitPanePrefWidth() {return contentSplitPane != null ? contentSplitPane.getPrefWidth() : 0.0; }

    /**
     * Button to insert line above current line
     * @return the button
     */
    Button getInsertLineButton() { return insertLineButton;  }

    /**
     * Button to remove current line
     * @return the button
     */
    Button getDeleteLineButton() { return deleteLineButton; }

    /**
     * Button to increase scope depth by one
     * @return the button
     */
    Button getIndentButton() { return indentButton; }

    /**
     * Button to decrease scope depth by one
     * @return
     */
    Button getOutdentButton() { return outdentButton; }

    /**
     * Button to add small shelf under current line
     * @retu the button
     */
    Button getAddShelfButton() { return addShelfButton; }

    /**
     * Button to add small gap under current line
     * @return the button
     */
    Button getAddGapButton() { return addGapButton; }

    /**
     * Button to insert lines for a subderivation above current line
     * @return the button
     */
    Button getInsertSubButton() { return insertSubButton;  }

    /**
     * Button to insert lines for a pair of subderivations above current line
     * @return the button
     */
    Button getInsertSubsButton() { return insertSubsButton; }

    /**
     * Button to undo last main action
     * @return the button
     */
    Button getUndoButton() { return undoButton;  }

    /**
     * Button to redo last undo
     * @return the button
     */
    Button getRedoButton() {  return redoButton; }

    /**
     * The pain containing the derivation (in its left half)
     * @return the split pane
     */
    SplitPane getSplitPane() {return contentSplitPane; }

    /**
     * The Decorated Rich Text Area for the explanation field
     * @return the explain DRTA
     */
    DecoratedRTA getExplanationDRTA() { return explanationDRTA;  }

    /**
     * The Decorated Rich Text Area for the explanation field
     * @param explanationDRTA the explain DRTA
     */
    void setExplanationDRTA(DecoratedRTA explanationDRTA) { this.explanationDRTA = explanationDRTA;  }

    /**
     * The string prompt for the explain field
     * @return the prompt
     */
    String getContentPrompt() {      return contentPrompt;   }

    /**
     * The string prompt for the explain field
     * @param prompt the prompt
     */
    public void setContentPrompt(String prompt) { contentPrompt = prompt; }

    public Button getCheckButton() {
        return checkButton;
    }

    public Button getCheckProgButton() {
        return checkProgButton;
    }

    public Button getHelpButton() {
        return helpButton;
    }

    public Text getBigCheck() {
        return bigCheck;
    }

    public Label getHelpTriesLabel() {
        return helpTriesLabel;
    }

    public Label getCheckTriesLabel() {
        return checkTriesLabel;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public void setCheckColor(Color checkColor) {
        this.checkColor = checkColor;
    }

    public void setCheckElementsColor(Color checkElementsColor) {
        this.checkElementsColor = checkElementsColor;
    }

    public void setCheckShowing(boolean checkShowing) {
        this.checkShowing = checkShowing;
    }

    public Color getCheckColor() {
        return checkColor;
    }

    public Color getCheckElementsColor() {
        return checkElementsColor;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setShowMetaLang(boolean showMetaLang) {
        this.showMetaLang = showMetaLang;
    }

    public Button getShowMetaLangButton() {
        return showMetaLangButton;
    }

    public Button getStaticHelpButton() {
        return staticHelpButton;
    }

    public VBox getControlBox() {
        return controlBox;
    }

    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }

    /**
     * The comment decoratedRTA
     * @param exerciseComment DecoratedRTA
     */
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment; }

    /**
     * The exercise prompt
     * @return the statement decorated RTA (T)
     */
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }

    /**
     * The exercise prompt
     * @param exerciseStatement the statement decorated RTA (T)
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement; }

    /**
     * The exercise prompt node (RTA)
     * @return the statement node
     */
    @Override
    public Node getExerciseStatementNode() {
        if (exerciseStatement == null || exerciseStatement.getEditor() == null) return new Pane();
        Node statementNode;
        if (!showMetaLang) {
            statementNode = exerciseStatement.getEditor();
        } else {
            AnchorPane anchorPane = new AnchorPane(exerciseStatement.getEditor(), showMetaLangButton);
            anchorPane.setTopAnchor(exerciseStatement.getEditor(), 0.0);
            anchorPane.setLeftAnchor(exerciseStatement.getEditor(), 0.0);
            anchorPane.setRightAnchor(showMetaLangButton, 3.0);
            anchorPane.setBottomAnchor(showMetaLangButton, 3.0);
            try { anchorPane.maxWidthProperty().bind(localScalePageWidth); } catch (Exception e) {}
            statementNode = anchorPane;
        }
        return statementNode;
    }

    /**
     * The preferred height of the statement window
     * @param height the preferred height
     */
    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        if (exerciseStatement != null && exerciseStatement.getEditor() != null) exerciseStatement.getEditor().setPrefHeight(height);
    }

    /**
     * The node which includes content of this exercise
     * @return the content node
     */
    @Override
    public Node getExerciseContentNode() { return contentBox; }

    /**
     * The left control node for this exercise
     * @return the control node
     */
    @Override
    public Node getExerciseControl() { return exerciseControlNode; }

    /**
     * The (null) right control node for this exercise
     * @return the control node
     */
    @Override
    public Node getRightControl() { return rightControlNode; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            if (mainView != null && !mainView.isInstructorFunctions() && pointsEarnedTextField != null) pointsEarnedTextField.setDisable(true);
            return pointsEarnedTextField;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    @Override
    public int getPointsPossible() { return pointsPossible;  }

    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }

    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }

    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }

    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }
}