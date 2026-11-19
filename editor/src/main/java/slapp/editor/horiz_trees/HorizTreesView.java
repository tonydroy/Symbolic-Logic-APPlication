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

package slapp.editor.horiz_trees;

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
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.free_form.ViewElement;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


/**
 * View for the free form exercise
 */


public class HorizTreesView implements ExerciseView<DecoratedRTA> {

    private HorizTreesView self;
    private MainWindowView mainView;
    private DecoratedRTA statementDRTA = new DecoratedRTA();
    private DecoratedRTA commentDRTA = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private DecoratedRTA interpretationDRTA = new DecoratedRTA();
    private RichTextArea interpretationRTA;
    private Node exerciseControlNode = new VBox();
    private String explainPrompt = "";
    private double explainPrefHeight = 0;
    private double interpretationPrefHeight = 0;
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
    private Node freeFormControlNode;
    private List<ViewElement> viewElements = new ArrayList<>();
    private VBox contentBox = new VBox(2);
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> interpretationHeightSpinner;
    private Spinner<Double> interpretationWidthSpinner;
    private Spinner<Double> mainPaneHeightSpinner;
    private Spinner<Double> mainPaneWidthSpinner;
    private Node currentSpinnerNode;
    private Button indentButton;
    private Button outdentButton;
    private Button removeButton;
    private Button restoreButton;
    private Button moveUpButton;
    private Button moveDownButton;

    private Button addHTreeButton;

    private int pointsPossible;
    private TextField pointsEarnedTextField;

    //----------------
    private Node rightControlNode;
    private VBox rightControlBox;
    private Button checkButton;
    private Button checkProgButton;
    private Text bigCheck;
    private String checkMessage;
    private Text checkedElements;
    private Label checkTriesLabel;
    private Color checkColor;
    private Color checkElementsColor;
    private boolean checkShowing = false;
    private Button staticHelpButton;
    private Stage staticHelpStage;
    private CheckBox permitEmptyBranchesCheck;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private UnaryOperator<TextFormatter.Change> pointsFilter;
    private EventHandler<MouseEvent> interpretationClickHandler;
    private EventHandler<MouseEvent> statementClickHandler;
    private EventHandler<MouseEvent> commentClickHandler;
    private EventHandler<MouseEvent> explainClickHandler;
    private ChangeListener<Number> localScalePageHeightListener;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    public HorizTreesView(MainWindowView mainView) {
        this.self = this;
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                RichTextArea statementRTA = statementDRTA != null ? statementDRTA.getEditor() : null;
                RichTextArea commentEditor = commentDRTA != null ? commentDRTA.getEditor() : null;
                RichTextArea explainRTA = explainDRTA != null ? explainDRTA.getEditor() : null;
                RichTextArea interpEditor = interpretationDRTA != null ? interpretationDRTA.getEditor() : null;

                if (statementRTA != null && inHierarchy(nv, statementRTA)) {
                    if (currentSpinnerNode != statementRTA) {
                        currentSpinnerNode = statementRTA;
                        if (statementHeightSpinner != null && statementWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
                        }
                    }
                } else if (commentEditor != null && inHierarchy(nv, commentEditor)) {
                    if (currentSpinnerNode != commentEditor) {
                        currentSpinnerNode = commentEditor;
                        if (commentHeightSpinner != null && commentWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
                        }
                    }
                } else if (explainRTA != null && inHierarchy(nv, explainRTA)) {
                    if (currentSpinnerNode != explainRTA) {
                        currentSpinnerNode = explainRTA;
                        if (explainHeightSpinner != null && explainWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(explainHeightSpinner, explainWidthSpinner);
                        }
                    }
                } else if (interpEditor != null && inHierarchy(nv, interpEditor)) {
                    if (currentSpinnerNode != interpEditor) {
                        currentSpinnerNode = interpEditor;
                        if (interpretationHeightSpinner != null && interpretationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);
                        }
                    }
                } else {
                    // THE FIX: Check if focus is in a child view element
                    boolean foundChildNode = false;
                    if (viewElements != null) {
                        for (int i = 0; i < viewElements.size(); i++) {
                            if (viewElements.get(i) != null && viewElements.get(i).getNode() != null) {
                                Node viewNode = viewElements.get(i).getNode();
                                if (inHierarchy(nv, viewNode)) {
                                    if (currentSpinnerNode != viewNode) {
                                        currentSpinnerNode = viewNode;
                                    }
                                    foundChildNode = true;
                                    break;
                                }
                            }
                        }
                    }
                    // THE FIX: Reachable cache clear!
                    if (!foundChildNode) {
                        currentSpinnerNode = null;
                    }
                }
            } catch (Exception e) {}
        };

        if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
        }

        indentButton = newControlsButton("Indent", "Indent current window.");
        outdentButton = newControlsButton("Outdent", "Outdent current window.");
        removeButton = newControlsButton("Remove", "Remove current window.");
        restoreButton = newControlsButton ("Restore", "Restore last removed item.");
        moveUpButton = newControlsButton("Move Up", "Place above previous item.");
        moveDownButton = newControlsButton("Move Down", "Place below following item.");
        addHTreeButton = newControlsButton("Add Window", "Insert new tree pane.");

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));

        pointsFilter = change -> {
            if (isTornDown || change == null) return null;
            String text = change.getText();
            if (text.matches("[0-9]*")) return change;
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(pointsFilter);
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


        VBox controlBox = new VBox();
        controlBox.setMinWidth(150);
        controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) return true;
        while (node != null) {
            if (node == potentialHierarchyElement) return true;
            node = node.getParent();
        }
        return false;
    }

    void initalizeInterpretationRTA() {
        if (isTornDown || this.mainView == null || interpretationRTA == null || interpretationDRTA == null) return;

        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPromptText("Interpretation");
        if (interpretationDRTA.getKeyboardSelector() != null) {
            interpretationDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.SANS_AND_ITALIC);
        }

        double interpretationInitialHeight = Math.round((interpretationPrefHeight - RTA_SCREEN_BMP)/ localScalePageHeight.get() * 100 );

        interpretationHeightSpinner = new Spinner<>(spinnerMin, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(65);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        interpretationRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        interpretationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        interpretationWidthSpinner.setPrefWidth(65);
        interpretationWidthSpinner.setDisable(true);
        interpretationWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        /*
        interpretationClickHandler = e -> {
            if (isTornDown || this.mainView == null || currentSpinnerNode == null) return;
            if (currentSpinnerNode != interpretationRTA) {
                currentSpinnerNode = interpretationRTA;
                if (interpretationHeightSpinner != null && interpretationWidthSpinner != null) {
                    mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);
                }
            }
        };
        interpretationRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, interpretationClickHandler);
         */
    }

    private Button newControlsButton(String name, String tip) {
        Button button = new Button(name);
        button.setPrefWidth(100);
        button.setPrefHeight(28);
        button.setTooltip(new Tooltip(tip));
        return button;
    }

    void initializeViewDetails() {
        if (isTornDown || this.mainView == null || statementDRTA == null || commentDRTA == null || explainDRTA == null) return;
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

        //statement
        RichTextArea statementRTA = statementDRTA.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.ceil((statementPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        statementHeightSpinner = new Spinner<>(spinnerMin, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(65);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        statementRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        /*
        statementClickHandler = e -> {
            if (isTornDown || this.mainView == null) return;
            if (currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                if (statementHeightSpinner != null && statementWidthSpinner != null) {
                    mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
                }
            }
        };
        statementRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, statementClickHandler);
         */

        //comment rta
        RichTextArea commentRTA = commentDRTA.getEditor();
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
        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        /*
        commentClickHandler = e -> {
            if (isTornDown || this.mainView == null) return;
            if (currentSpinnerNode != commentRTA) {
                currentSpinnerNode = commentRTA;
                if (commentHeightSpinner != null && commentWidthSpinner != null) {
                    mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
                }
            }
        };
        commentRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, commentClickHandler);
         */

        //explain
        RichTextArea explainRTA = explainDRTA.getEditor();
        explainRTA.getStylesheets().add("slappTextArea.css");
        explainRTA.setPromptText(explainPrompt);

        double explainInitialHeight = Math.round((explainPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0 );
        explainHeightSpinner = new Spinner<>(spinnerMin, 999.0, explainInitialHeight, 1.0);
        explainHeightSpinner.setPrefWidth(65);
        explainHeightSpinner.setDisable(false);
        explainHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        explainRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        explainRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        explainRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        explainRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        explainWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explainWidthSpinner.setPrefWidth(65);
        explainWidthSpinner.setDisable(true);
        explainWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        /*
        explainClickHandler = e -> {
            if (isTornDown || this.mainView == null) return;
            if (currentSpinnerNode != explainRTA) {
                currentSpinnerNode = explainRTA;
                if (explainHeightSpinner != null && explainWidthSpinner != null) {
                    mainView.updateSizeSpinners(explainHeightSpinner, explainWidthSpinner);
                }
            }
        };
        explainRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, explainClickHandler);
         */


        //page height listener
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || statementRTA == null || commentRTA == null || explainRTA == null || interpretationRTA == null) return;
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

            explainRTA.prefHeightProperty().unbind();
            if (explainHeightSpinner != null && explainHeightSpinner.getValueFactory() != null) {
                explainHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(explainHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                explainRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
            }

            interpretationRTA.prefHeightProperty().unbind();
            if (interpretationHeightSpinner != null && interpretationHeightSpinner.getValueFactory() != null) {
                interpretationHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
            }
        };
        localScalePageHeight.addListener(localScalePageHeightListener);
    }

    public void setRightControlBox() {
        bigCheck = new Text("\ue89a");
        bigCheck.setFont(Font.font("Noto Serif Combo", 72));

        checkedElements = new Text(checkMessage);
        checkedElements.setFont(Font.font("Noto Serif Combo", 11));
        TextFlow checkedElementsFlow = new TextFlow(checkedElements);
        checkedElementsFlow.setMaxWidth(150);

        VBox bigCheckBox = new VBox(0, bigCheck, checkedElementsFlow);
        bigCheckBox.setAlignment(Pos.CENTER);
        checkedElementsFlow.setTextAlignment(TextAlignment.CENTER);
        bigCheckBox.setMargin(checkedElementsFlow, new Insets(-15, 0, 0, 0));

        checkButton = new Button("Check Tree(s)");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check tree or trees for correctness."));
        checkTriesLabel = new Label();

        checkProgButton = new Button();

        VBox checksBox = new VBox(5, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        permitEmptyBranchesCheck = new CheckBox("Empty Branches");

        rightControlBox = new VBox(20, bigCheckBox, checksBox, staticHelpButton, permitEmptyBranchesCheck );
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(20,20,0,20));
        rightControlNode = rightControlBox;

        deactivateBigCheck();
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

            ParallelTransition pt = new ParallelTransition(t1, t2);
            pt.play();
        }
        else {
            bigCheck.setOpacity(1.0);
            checkedElements.setOpacity(1.0);
        }
    }

    public void deactivateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
        if (bigCheck.getOpacity() > .5) checkShowing = true;
        else checkShowing = false;
        bigCheck.setOpacity(0.0);
        checkedElements.setOpacity(0.0);
    }

    public void showStaticHelp(Document doc) {
        if (isTornDown || this.mainView == null) return;

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

    public Stage getStaticHelpStage() { return staticHelpStage; }

    void updateContentFromViewElements() {
        if (isTornDown || this.mainView == null || contentBox == null || viewElements == null) return;

        // SCORCHED EARTH RULE 8: Safely sever to prevent ConcurrentModificationException
        if (contentBox.getChildren() != null) {
            List<Node> childrenCopy = new ArrayList<>(contentBox.getChildren());
            for (Node node : childrenCopy) {
                severFromParent(node);
            }
            contentBox.getChildren().clear();
        }

        if (interpretationRTA != null) contentBox.getChildren().add(interpretationRTA);
        for (ViewElement element : viewElements) {
            if (element != null && element.getNode() != null) {
                Node node = element.getNode();
                HBox windowBox = new HBox(node);
                windowBox.setPrefWidth(HBox.USE_COMPUTED_SIZE);
                windowBox.setMinWidth(HBox.USE_PREF_SIZE);
                windowBox.setMaxWidth(HBox.USE_PREF_SIZE);

                if (node instanceof AnchorPane) {
                    AnchorPane pane = (AnchorPane) node;
                    pane.setPadding(new Insets(0, RTA_WIDTH_BMP, 0, 0));
                }

                contentBox.getChildren().add(windowBox);
            }
        }
        if (explainDRTA != null && explainDRTA.getEditor() != null) {
            contentBox.getChildren().add(explainDRTA.getEditor());
        }
    }

    void setBorders(int i) {
        if (isTornDown || this.mainView == null || viewElements == null) return;
        for (ViewElement element : viewElements) {
            if (element != null && element.getNode() != null) {
                element.getNode().getStyleClass().remove("-fx-border-color");
                element.getNode().setStyle("-fx-border-color: gainsboro");
            }
        }
        if (i >= 0 && i < viewElements.size() && viewElements.get(i) != null && viewElements.get(i).getNode() != null) {
            ViewElement currentElement = viewElements.get(i);
            currentElement.getNode().getStyleClass().remove("-fx-border-color");
            currentElement.getNode().setStyle("-fx-border-color: lightblue");
        }
    }

    void setContentSpinners(int index, Spinner heightSpinner, Spinner widthSpinner) {
        if (isTornDown || this.mainView == null || viewElements == null || index < 0 || index >= viewElements.size() || viewElements.get(index) == null || viewElements.get(index).getNode() == null) return;
        if (currentSpinnerNode != viewElements.get(index).getNode()) {
            currentSpinnerNode = viewElements.get(index).getNode();
            mainView.updateSizeSpinners(heightSpinner, widthSpinner);
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


    public void cleanup() {
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
            if (mainView != null) {
                mainView.textFieldInFocus();
            }
        } catch (Exception e) {}

        try {
            if (localScalePageHeight != null) {
                if (localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener);
                localScalePageHeight.unbind();
            }
        } catch (Exception e) {}
        try { if (localScalePageWidth != null) localScalePageWidth.unbind(); } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        try { if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null); } catch (Exception e) {}
        try { if (interpretationRTA != null && interpretationClickHandler != null) interpretationRTA.removeEventFilter(MouseEvent.MOUSE_CLICKED, interpretationClickHandler); } catch (Exception e) {}
        try { if (statementDRTA != null && statementDRTA.getEditor() != null && statementClickHandler != null) statementDRTA.getEditor().removeEventFilter(MouseEvent.MOUSE_CLICKED, statementClickHandler); } catch (Exception e) {}
        try { if (commentDRTA != null && commentDRTA.getEditor() != null && commentClickHandler != null) commentDRTA.getEditor().removeEventFilter(MouseEvent.MOUSE_CLICKED, commentClickHandler); } catch (Exception e) {}
        try { if (explainDRTA != null && explainDRTA.getEditor() != null && explainClickHandler != null) explainDRTA.getEditor().removeEventFilter(MouseEvent.MOUSE_CLICKED, explainClickHandler); } catch (Exception e) {}

        try { if (staticHelpStage != null) staticHelpStage.close(); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (Async Separation) <---
        try {
            final RichTextArea stEditor = (statementDRTA != null) ? statementDRTA.getEditor() : null;
            final RichTextArea commEditor = (commentDRTA != null) ? commentDRTA.getEditor() : null;
            final RichTextArea expEditor = (explainDRTA != null) ? explainDRTA.getEditor() : null;
            final RichTextArea interpEditor1 = (interpretationDRTA != null) ? interpretationDRTA.getEditor() : null;
            final RichTextArea interpEditor2 = interpretationRTA;

            // Unbind size properties synchronously
            if (stEditor != null) { stEditor.prefHeightProperty().unbind(); stEditor.prefWidthProperty().unbind(); stEditor.maxWidthProperty().unbind(); stEditor.minWidthProperty().unbind(); stEditor.contentAreaWidthProperty().unbind(); }
            if (commEditor != null) { commEditor.prefHeightProperty().unbind(); commEditor.prefWidthProperty().unbind(); commEditor.maxWidthProperty().unbind(); commEditor.minWidthProperty().unbind(); commEditor.contentAreaWidthProperty().unbind(); }
            if (expEditor != null) { expEditor.prefHeightProperty().unbind(); expEditor.prefWidthProperty().unbind(); expEditor.maxWidthProperty().unbind(); expEditor.minWidthProperty().unbind(); expEditor.contentAreaWidthProperty().unbind(); }
            if (interpEditor1 != null) { interpEditor1.prefHeightProperty().unbind(); interpEditor1.prefWidthProperty().unbind(); interpEditor1.maxWidthProperty().unbind(); interpEditor1.minWidthProperty().unbind(); interpEditor1.contentAreaWidthProperty().unbind(); }
            if (interpEditor2 != null) { interpEditor2.prefHeightProperty().unbind(); interpEditor2.prefWidthProperty().unbind(); interpEditor2.maxWidthProperty().unbind(); interpEditor2.minWidthProperty().unbind(); interpEditor2.contentAreaWidthProperty().unbind(); }

            // STAGE 1: Execute async document injection IMMEDIATELY and synchronously clear prompt texts to disarm the NPE listener
            try { if (stEditor != null) { stEditor.setPromptText(""); stEditor.setFocusTraversable(false); if (stEditor.getDocument() == null || stEditor.getDocument().getText().isEmpty()) stEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (commEditor != null) { commEditor.setPromptText(""); commEditor.setFocusTraversable(false); if (commEditor.getDocument() == null || commEditor.getDocument().getText().isEmpty()) commEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (expEditor != null) { expEditor.setPromptText(""); expEditor.setFocusTraversable(false); if (expEditor.getDocument() == null || expEditor.getDocument().getText().isEmpty()) expEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (interpEditor1 != null) { interpEditor1.setPromptText(""); interpEditor1.setFocusTraversable(false); if (interpEditor1.getDocument() == null || interpEditor1.getDocument().getText().isEmpty()) interpEditor1.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (interpEditor2 != null) { interpEditor2.setPromptText(""); interpEditor2.setFocusTraversable(false); if (interpEditor2 != interpEditor1 && (interpEditor2.getDocument() == null || interpEditor2.getDocument().getText().isEmpty())) interpEditor2.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}

            // STAGE 2: Wait 1.5s for ALL background tasks and internal Gluon layout listeners to complete BEFORE safely nulling the skin.
            if (stEditor != null || commEditor != null || expEditor != null || interpEditor1 != null || interpEditor2 != null) {
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try { if (stEditor != null) stEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (commEditor != null) commEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (expEditor != null) expEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (interpEditor1 != null) interpEditor1.setSkin(null); } catch (Exception ex) {}
                    try { if (interpEditor2 != null && interpEditor2 != interpEditor1) interpEditor2.setSkin(null); } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (contentBox != null) {
                if (contentBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(contentBox.getChildren());
                    for (Node child : childrenCopy) {
                        severFromParent(child);
                    }
                }
                contentBox.getChildren().clear();
                severFromParent(contentBox);
            }
        } catch (Exception e) {}

        try {
            if (rightControlBox != null) {
                if (rightControlBox.getChildren() != null) rightControlBox.getChildren().clear();
                severFromParent(rightControlBox);
            }
        } catch (Exception e) {}

        try { if (exerciseControlNode != null) severFromParent(exerciseControlNode); } catch (Exception e) {}
        try { if (freeFormControlNode != null) severFromParent(freeFormControlNode); } catch (Exception e) {}
        try { if (rightControlNode != null) severFromParent(rightControlNode); } catch (Exception e) {}

        try { if (indentButton != null) indentButton.setOnAction(null); } catch (Exception e) {}
        try { if (outdentButton != null) outdentButton.setOnAction(null); } catch (Exception e) {}
        try { if (removeButton != null) removeButton.setOnAction(null); } catch (Exception e) {}
        try { if (restoreButton != null) restoreButton.setOnAction(null); } catch (Exception e) {}
        try { if (moveUpButton != null) moveUpButton.setOnAction(null); } catch (Exception e) {}
        try { if (moveDownButton != null) moveDownButton.setOnAction(null); } catch (Exception e) {}
        try { if (addHTreeButton != null) addHTreeButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkButton != null) checkButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkProgButton != null) checkProgButton.setOnAction(null); } catch (Exception e) {}
        try { if (staticHelpButton != null) staticHelpButton.setOnAction(null); } catch (Exception e) {}

        try { if (viewElements != null) viewElements = null; } catch (Exception e) {}

        // 6. Complete nullification
        try { this.self = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.statementDRTA = null; } catch (Exception e) {}
        try { this.commentDRTA = null; } catch (Exception e) {}
        try { this.explainDRTA = null; } catch (Exception e) {}
        try { this.interpretationDRTA = null; } catch (Exception e) {}
        try { this.interpretationRTA = null; } catch (Exception e) {}
        try { this.exerciseControlNode = null; } catch (Exception e) {}
        try { this.freeFormControlNode = null; } catch (Exception e) {}
        try { this.contentBox = null; } catch (Exception e) {}
        try { this.explainPrompt = null; } catch (Exception e) {}
        try { this.explainHeightSpinner = null; } catch (Exception e) {}
        try { this.explainWidthSpinner = null; } catch (Exception e) {}
        try { this.statementHeightSpinner = null; } catch (Exception e) {}
        try { this.statementWidthSpinner = null; } catch (Exception e) {}
        try { this.commentHeightSpinner = null; } catch (Exception e) {}
        try { this.commentWidthSpinner = null; } catch (Exception e) {}
        try { this.interpretationHeightSpinner = null; } catch (Exception e) {}
        try { this.interpretationWidthSpinner = null; } catch (Exception e) {}
        try { this.mainPaneHeightSpinner = null; } catch (Exception e) {}
        try { this.mainPaneWidthSpinner = null; } catch (Exception e) {}
        try { this.currentSpinnerNode = null; } catch (Exception e) {}
        try { this.indentButton = null; } catch (Exception e) {}
        try { this.outdentButton = null; } catch (Exception e) {}
        try { this.removeButton = null; } catch (Exception e) {}
        try { this.restoreButton = null; } catch (Exception e) {}
        try { this.moveUpButton = null; } catch (Exception e) {}
        try { this.moveDownButton = null; } catch (Exception e) {}
        try { this.addHTreeButton = null; } catch (Exception e) {}
        try { this.pointsEarnedTextField = null; } catch (Exception e) {}
        try { this.rightControlNode = null; } catch (Exception e) {}
        try { this.rightControlBox = null; } catch (Exception e) {}
        try { this.checkButton = null; } catch (Exception e) {}
        try { this.checkProgButton = null; } catch (Exception e) {}
        try { this.bigCheck = null; } catch (Exception e) {}
        try { this.checkMessage = null; } catch (Exception e) {}
        try { this.checkedElements = null; } catch (Exception e) {}
        try { this.checkTriesLabel = null; } catch (Exception e) {}
        try { this.checkColor = null; } catch (Exception e) {}
        try { this.checkElementsColor = null; } catch (Exception e) {}
        try { this.staticHelpButton = null; } catch (Exception e) {}
        try { this.staticHelpStage = null; } catch (Exception e) {}
        try { this.permitEmptyBranchesCheck = null; } catch (Exception e) {}
        try { this.exerciseFocusListener = null; } catch (Exception e) {}
        try { this.pointsFilter = null; } catch (Exception e) {}
        try { this.interpretationClickHandler = null; } catch (Exception e) {}
        try { this.statementClickHandler = null; } catch (Exception e) {}
        try { this.commentClickHandler = null; } catch (Exception e) {}
        try { this.explainClickHandler = null; } catch (Exception e) {}
        try { this.localScalePageHeightListener = null; } catch (Exception e) {}

        try {this.pointsEarnedFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedClickHandler = null; } catch (Exception e) {}
    }


    List<ViewElement> getViewElements() {      return viewElements;  }
    Button getIndentButton() {     return indentButton;  }
    Button getOutdentButton() {     return outdentButton;   }
    Button getRemoveButton() {     return removeButton;   }
    Button getRestoreButton() {     return restoreButton;  }
    Button getMoveUpButton() { return moveUpButton; }
    Button getMoveDownButton() { return moveDownButton; }
    Button getAddHTreeButton() {     return addHTreeButton;  }

    double getCommentPrefHeight() {
        if (commentDRTA == null || commentDRTA.getEditor() == null) return 0;
        return commentDRTA.getEditor().getPrefHeight();
    }
    void setCommentPrefHeight(double commentPrefHeight) {     this.commentPrefHeight = commentPrefHeight;   }
    void setExerciseControlNode(Node exerciseControlNode) {   this.exerciseControlNode = exerciseControlNode;  }
    public VBox getRightControlBox() { return rightControlBox; }

    @Override
    public DecoratedRTA getExerciseComment() { return commentDRTA;  }
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { commentDRTA = exerciseComment;   }
    @Override
    public DecoratedRTA getExerciseStatement() {  return statementDRTA;   }
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { statementDRTA = exerciseStatement;   }
    @Override
    public Node getExerciseStatementNode() {
        if (statementDRTA == null) return new AnchorPane();
        return statementDRTA.getEditor();
    }

    @Override
    public void setStatementPrefHeight(double height) {
        if (statementDRTA == null || statementDRTA.getEditor() == null) return;
        statementPrefHeight = height;
        statementDRTA.getEditor().setPrefHeight(height);
    }
    @Override
    public Node getExerciseContentNode() {   return contentBox;   }
    @Override
    public Node getExerciseControl() {  return exerciseControlNode;   }
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
    public void setPointsPossible(int pointsPossible) { this.pointsPossible = pointsPossible; }
    @Override
    public int getPointsPossible() { return pointsPossible;  }
    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }

    public DecoratedRTA getInterpretationDRTA() { return interpretationDRTA; }
    public void setInterpretationRTA(RichTextArea interpretationRTA) { this.interpretationRTA = interpretationRTA; }
    public double getInterpretationPrefHeight() {
        if (interpretationRTA == null) return 0;
        return interpretationRTA.getPrefHeight();
    }
    public void setInterpretationPrefHeight(double interpretationPrefHeight) { this.interpretationPrefHeight = interpretationPrefHeight; }
    public void setInterpretationDRTA(DecoratedRTA interpretationDRTA) { this.interpretationDRTA = interpretationDRTA; }
    double getExplainPrefHeight() {
        if (explainDRTA == null || explainDRTA.getEditor() == null) return 0;
        return explainDRTA.getEditor().getPrefHeight();
    }
    void setExplainPrefHeight(double explainPrefHeight) {   this.explainPrefHeight = explainPrefHeight;  }
    DecoratedRTA getExplainDRTA() { return explainDRTA; }
    void setExplainPrompt(String explainPrompt) {  this.explainPrompt = explainPrompt; }

    public String getCheckMessage() { return checkMessage; }
    public Button getStaticHelpButton() { return staticHelpButton; }
    public Button getCheckButton() { return checkButton; }
    public CheckBox getPermitEmptyBranchesCheck() { return permitEmptyBranchesCheck; }
    public void setCheckColor(Color checkColor) { this.checkColor = checkColor; }
    public void setCheckElementsColor(Color checkElementsColor) { this.checkElementsColor = checkElementsColor; }
    public void setCheckMessage(String checkMessage) { this.checkMessage = checkMessage; }
    public Label getCheckTriesLabel() { return checkTriesLabel; }
    public VBox getContentBox() { return contentBox; }
    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }
    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }
}