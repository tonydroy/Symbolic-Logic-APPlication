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

package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class TransTreeView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private TransTreeExercise transTreeExercise;
    private BorderPane root;
    private TTreeRootLayout rootLayout;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA interpFnDRTA = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double interpFnPrefHeight = 0;
    private double mainPanePrefHeight = 0;
    private double mainPanePrefWidth = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> interpFnHeightSpinner;
    private Spinner<Double> interpFnWidthSpinner;
    private Spinner<Double> mainPaneHeightSpinner;
    private Spinner<Double> mainPaneWidthSpinner;
    private Node currentSpinnerNode;

    private VBox controlBox = new VBox(25);
    private Button undoButton;
    private Button redoButton;
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();

    SplitPane mainPane;
    private int pointsPossible;
    private TextField pointsEarnedTextField;

    private Node rightControlNode;
    private Button checkButton;
    private Text bigCheck;
    private String checkMessage;
    private Text checkedElements;
    private Label checkTriesLabel;
    private Color checkColor;
    private Color checkElementsColor;
    private boolean checkShowing = false;
    private Button staticHelpButton;
    private Stage  staticHelpStage;
    private String interpFieldPrompt = "Interpretation Function";
    private CheckBox omitTreesCheckBox;
    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private ChangeListener<Number> mainPaneHeightListener;
    private ChangeListener<Number> mainPaneWidthListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private ChangeListener<Number> localScalePageWidthListener;
    private EventHandler<MouseEvent> mainPaneMouseClickedFilter;
    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    TransTreeView(MainWindowView mainView, TransTreeExercise transTreeExercise) {
        this.mainView = mainView;
        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea interpFnRTA = interpFnDRTA != null ? interpFnDRTA.getEditor() : null;

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
                } else if (interpFnRTA != null && inHierarchy(nv, interpFnRTA)) {
                    if (currentSpinnerNode != interpFnRTA) {
                        currentSpinnerNode = interpFnRTA;
                        if (interpFnHeightSpinner != null && interpFnWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(interpFnHeightSpinner, interpFnWidthSpinner);
                        }
                    }
                } else if (mainPane != null && inHierarchy(nv, mainPane)) {
                    if (currentSpinnerNode != mainPane) {
                        currentSpinnerNode = mainPane;
                        if (mainPaneHeightSpinner != null && mainPaneWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                        }
                    }
                } else {
                    // THE FIX: Focus moved outside our recognized editors (e.g., to another exercise).
                    // Clear the cached state so the next click definitively forces a toolbar update!
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        this.transTreeExercise = transTreeExercise;
        root = new BorderPane();
        rootLayout = new TTreeRootLayout(this);

        VBox contentBox = new VBox(3, rootLayout, interpFnDRTA.getEditor());
        root.setCenter(contentBox);

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");
        undoButton.setPrefWidth(64);
        redoButton.setPrefWidth(64);
        undoButton.setPrefHeight(28);
        redoButton.setPrefHeight(28);

        controlBox.getChildren().addAll(undoButton, redoButton);
        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBox.setPadding(new Insets(40,20,100,20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);

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

    void initializeViewDetails() {
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

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

        statementRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //comment
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

        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));


        //interp function
        RichTextArea interpFnRTA = interpFnDRTA.getEditor();
        interpFnRTA.getStylesheets().add("slappTextArea.css");
        interpFnRTA.setPromptText(interpFieldPrompt);

        double interpFnInitialHeight = Math.round((interpFnPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0 );
        interpFnHeightSpinner = new Spinner<>(spinnerMin, 999.0, interpFnInitialHeight, 1.0);
        interpFnHeightSpinner.setPrefWidth(65);
        interpFnHeightSpinner.setDisable(false);
        interpFnHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        interpFnRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(interpFnHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));


        interpFnRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpFnRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpFnRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        interpFnWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        interpFnWidthSpinner.setPrefWidth(65);
        interpFnWidthSpinner.setDisable(true);
        interpFnWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));


        //main pane
        AnchorPane mainPane1 = rootLayout.getMain_pane();
        mainPane = rootLayout.getBase_pane();
        mainPane.setStyle("-fx-focus-color: gainsboro;");
        mainPane1.setMinHeight(250.0);
        mainPane1.setMinWidth(localScalePageWidth.get() + RTA_WIDTH_BMP);

        mainPaneHeightSpinner = new Spinner<>(0, 999.0, 0, 1.0);
        mainPaneHeightSpinner.setPrefWidth(65);
        mainPaneHeightSpinner.setDisable(true);
        mainPaneHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        mainPaneHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPaneHeightSpinner == null) return;
            try {
                mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / localScalePageHeight.get() * 100));
            } catch (Exception e) {}
        };
        mainPane1.heightProperty().addListener(mainPaneHeightListener);

        mainPaneWidthSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        mainPaneWidthSpinner.setPrefWidth(65);
        mainPaneWidthSpinner.setDisable(true);
        mainPaneWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        mainPaneWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPaneWidthSpinner == null) return;
            try {
                mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane1.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100));
            } catch (Exception e) {}
        };
        mainPane1.widthProperty().addListener(mainPaneWidthListener);

        //page size listeners
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                double revSpinnerMin = (double) ((int) (100 * 45.0 / (double) nv));

                if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                    RichTextArea stRTA = exerciseStatement.getEditor();
                    stRTA.prefHeightProperty().unbind();
                    if (statementHeightSpinner != null && statementHeightSpinner.getValueFactory() != null) {
                        statementHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                        stRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                    }
                }

                if (exerciseComment != null && exerciseComment.getEditor() != null) {
                    RichTextArea cmtRTA = exerciseComment.getEditor();
                    cmtRTA.prefHeightProperty().unbind();
                    if (commentHeightSpinner != null && commentHeightSpinner.getValueFactory() != null) {
                        commentHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                        cmtRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                    }
                }

                if (interpFnDRTA != null && interpFnDRTA.getEditor() != null) {
                    RichTextArea ifnRTA = interpFnDRTA.getEditor();
                    ifnRTA.prefHeightProperty().unbind();
                    if (interpFnHeightSpinner != null && interpFnHeightSpinner.getValueFactory() != null) {
                        interpFnHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(interpFnHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                        ifnRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpFnHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                    }
                }

                if (mainPaneHeightSpinner != null && mainPaneHeightSpinner.getValueFactory() != null && mainPane1 != null) {
                    mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / localScalePageHeight.get() * 100.0));
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        localScalePageWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null) return;
            try {
                mainPane.prefWidthProperty().unbind();
                if (mainPaneWidthSpinner != null && mainPaneWidthSpinner.getValueFactory() != null && mainPane1 != null) {
                    mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane1.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100));
                }
            } catch (Exception e) {}
        };
        localScalePageWidth.addListener(localScalePageWidthListener);

        setSizeSpinners();
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

        checkButton = new Button("Check Tree");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check exercise for correctness."));
        checkTriesLabel = new Label();

        VBox checksBox = new VBox(10, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        omitTreesCheckBox = new CheckBox("Trees Omitted");


        VBox rightControlBox = new VBox(40, bigCheckBox, checksBox, staticHelpButton, omitTreesCheckBox );
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(60,20,0,20));
        rightControlNode = rightControlBox;

        deactivateBigCheck();
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
            }
            else {
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
        try {
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
        } catch (Exception e) {}
    }

    private void setSizeSpinners() {

        mainPaneMouseClickedFilter = e -> {
            if (isTornDown || this.mainView == null) return;
            try {
                if (mainPane != null && currentSpinnerNode != mainPane) {
                    currentSpinnerNode = mainPane;
                    if (mainPaneHeightSpinner != null && mainPaneWidthSpinner != null) {
                        mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                    }
                    mainPane.requestFocus();
                }
            } catch (Exception ex) {}
        };

        /*
        mainPaneMouseClickedFilter = e -> {
            if (isTornDown || this.mainView == null ) return;
            try {
                if (mainPane != null && currentSpinnerNode != mainPane) {
                    mainPane.requestFocus();
                }
            } catch (Exception ex) {}
        };

         */

        try {
            if (mainPane != null && mainPaneMouseClickedFilter != null) {
                mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneMouseClickedFilter);
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
            }
        } catch (Exception e) {}
    }



    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) return true;
        while (node != null) {
            if (node == potentialHierarchyElement) return true;
            node = node.getParent();
        }
        return false;
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes Helper
    private void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                else if (node.getParent() instanceof javafx.scene.Group) ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
            }
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation applied heavily below via multiple try-catch blocks
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
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
            if (mainPane != null && mainPaneMouseClickedFilter != null) {
                mainPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneMouseClickedFilter);
            }
        } catch (Exception e) {}

        try {
            if (rootLayout != null && rootLayout.getMain_pane() != null) {
                if (mainPaneHeightListener != null) rootLayout.getMain_pane().heightProperty().removeListener(mainPaneHeightListener);
                if (mainPaneWidthListener != null) rootLayout.getMain_pane().widthProperty().removeListener(mainPaneWidthListener);
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
            if (undoRedoFlag != null) {
                undoRedoFlag.unbind();
            }
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

        try { if (checkButton != null) checkButton.setOnAction(null); } catch (Exception e) {}
        try { if (undoButton != null) undoButton.setOnAction(null); } catch (Exception e) {}
        try { if (redoButton != null) redoButton.setOnAction(null); } catch (Exception e) {}
        try { if (staticHelpButton != null) staticHelpButton.setOnAction(null); } catch (Exception e) {}
        try { if (omitTreesCheckBox != null) omitTreesCheckBox.setOnAction(null); } catch (Exception e) {}

        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().prefHeightProperty().unbind();
                exerciseStatement.getEditor().maxWidthProperty().unbind();
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
            if (interpFnDRTA != null && interpFnDRTA.getEditor() != null) {
                interpFnDRTA.getEditor().prefHeightProperty().unbind();
                interpFnDRTA.getEditor().prefWidthProperty().unbind();
                interpFnDRTA.getEditor().maxWidthProperty().unbind();
                interpFnDRTA.getEditor().minWidthProperty().unbind();
                interpFnDRTA.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea interpEditor = (interpFnDRTA != null) ? interpFnDRTA.getEditor() : null;

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (stEditor != null) rtasToClear.add(stEditor);
            if (commEditor != null) rtasToClear.add(commEditor);
            if (interpEditor != null) rtasToClear.add(interpEditor);

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

        // SCORCHED EARTH RULE 1: Cascade Cleanup
        try {
            if (rootLayout != null) {
                try { rootLayout.getClass().getMethod("cleanup").invoke(rootLayout); } catch (Exception ex) {}
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 6 & 8: Dismantle UI Tree & Sever Shared Nodes
        try {
            if (rootLayout != null) {
                if (rootLayout.getMain_pane() != null) {
                    severFromParent(rootLayout.getMain_pane());
                    rootLayout.getMain_pane().getChildren().clear();
                }
                if (rootLayout.getBase_pane() != null) {
                    rootLayout.getBase_pane().prefWidthProperty().unbind();
                }
                severFromParent(rootLayout);
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
                if (rightControlNode instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) rightControlNode).getChildren().clear();
                }
            }
        } catch (Exception e) {}

        try {
            if (root != null) {
                severFromParent(root);
                root.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (staticHelpStage != null) {
                staticHelpStage.close();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        this.mainView = null;
        this.transTreeExercise = null;
        this.root = null;
        this.rootLayout = null;
        this.exerciseComment = null;
        this.exerciseStatement = null;
        this.interpFnDRTA = null;

        this.statementHeightSpinner = null;
        this.statementWidthSpinner = null;
        this.commentHeightSpinner = null;
        this.commentWidthSpinner = null;
        this.interpFnHeightSpinner = null;
        this.interpFnWidthSpinner = null;
        this.mainPaneHeightSpinner = null;
        this.mainPaneWidthSpinner = null;
        this.currentSpinnerNode = null;

        this.controlBox = null;
        this.undoButton = null;
        this.redoButton = null;
        this.undoRedoFlag = null;

        this.mainPane = null;
        this.pointsEarnedTextField = null;

        this.rightControlNode = null;
        this.checkButton = null;
        this.bigCheck = null;
        this.checkMessage = null;
        this.checkedElements = null;
        this.checkTriesLabel = null;
        this.checkColor = null;
        this.checkElementsColor = null;
        this.staticHelpButton = null;
        this.omitTreesCheckBox = null;
        this.staticHelpStage = null;
        this.interpFieldPrompt = null;

        this.exerciseFocusListener = null;
        this.mainPaneHeightListener = null;
        this.mainPaneWidthListener = null;
        this.localScalePageHeightListener = null;
        this.localScalePageWidthListener = null;
        this.mainPaneMouseClickedFilter = null;
        this.pointsEarnedFilter = null;

        this.pointsEarnedFocusListener = null;
        this.pointsEarnedClickHandler = null;
    }

    public MainWindowView getMainView() {
        return mainView;
    }

    public TTreeRootLayout getRootLayout() {return rootLayout;}

    public VBox getControlBox() {  return controlBox;  }

    public Button getUndoButton() { return undoButton;   }

    public Button getRedoButton() { return redoButton;   }

    public boolean isUndoRedoFlag() {
        if (undoRedoFlag == null) return false;
        return undoRedoFlag.get();
    }

    public BooleanProperty undoRedoFlagProperty() {    return undoRedoFlag;    }

    public void setUndoRedoFlag(boolean undoRedoFlag) {
        if (this.undoRedoFlag != null) this.undoRedoFlag.set(undoRedoFlag);
    }

    public DecoratedRTA getInterpFnDRTA() { return interpFnDRTA; }

    public double getCommentPrefHeight() {
        if (exerciseComment == null || exerciseComment.getEditor() == null) return commentPrefHeight;
        return exerciseComment.getEditor().getPrefHeight();
    }

    public void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }

    public double getInterpFnPrefHeight() {
        if (interpFnDRTA == null || interpFnDRTA.getEditor() == null) return interpFnPrefHeight;
        return interpFnDRTA.getEditor().getPrefHeight();
    }

    public void setInterpFnPrefHeight(double interpFnPrefHeight) {   this.interpFnPrefHeight = interpFnPrefHeight;  }

    public double getMainPanePrefHeight() {
        if (rootLayout == null || rootLayout.getMain_pane() == null) return mainPanePrefHeight;
        return rootLayout.getMain_pane().getPrefHeight();
    }

    public void setMainPanePrefHeight(double mainPanePrefHeight) { this.mainPanePrefHeight = mainPanePrefHeight;  }

    public double getMainPanePrefWidth() {
        if (rootLayout == null || rootLayout.getMain_pane() == null) return mainPanePrefWidth;
        return rootLayout.getMain_pane().getPrefWidth();
    }
    public void setMainPanePrefWidth(double mainPanePrefWidth) {   this.mainPanePrefWidth = mainPanePrefWidth;  }

    public Button getCheckButton() {
        return checkButton;
    }

    public Button getstaticHelpButton() {
        return staticHelpButton;
    }

    public Label getCheckTriesLabel() {
        return checkTriesLabel;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public void setCheckColor(Color checkColor) {
        this.checkColor = checkColor;
    }

    public void setCheckElementsColor(Color checkElementsColor) {
        this.checkElementsColor = checkElementsColor;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public Stage getStaticHelpStage() { return staticHelpStage; }

    public void setInterpFieldPrompt(String interpFieldPrompt) {
        this.interpFieldPrompt = interpFieldPrompt;
    }

    public Spinner<Double> getInterpFnHeightSpinner() {
        return interpFnHeightSpinner;
    }

    public CheckBox getOmitTreesCheckBox() {
        return omitTreesCheckBox;
    }

    public void setOmitTreesCheckBox(CheckBox omitTreesCheckBox) {
        this.omitTreesCheckBox = omitTreesCheckBox;
    }

    @Override
    public DecoratedRTA getExerciseComment() {
        return exerciseComment;
    }

    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment;  }

    @Override
    public DecoratedRTA getExerciseStatement() {
        return exerciseStatement;
    }

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
        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().setPrefHeight(height);
            }
        } catch (Exception e) {}
    }

    @Override
    public Node getExerciseContentNode() {
        if (root == null) return new VBox();
        return new VBox(root);
    }

    @Override
    public Node getExerciseControl() {
        return controlBox;
    }

    @Override
    public Node getRightControl() { return rightControlNode; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            try {
                if (mainView != null && !mainView.isInstructorFunctions() && pointsEarnedTextField != null) {
                    pointsEarnedTextField.setDisable(true);
                }
            } catch (Exception e) {}
            return pointsEarnedTextField;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    @Override
    public int getPointsPossible() { return pointsPossible;  }

    public TextField getPointsEarnedTextField() {
        return pointsEarnedTextField;
    }

    public TransTreeExercise getTransTreeExercise() {
        return transTreeExercise;
    }

    public DoubleProperty localScalePageHeightProperty() {
        return localScalePageHeight;
    }

    public DoubleProperty localScalePageWidthProperty() {
        return localScalePageWidth;
    }

    public ChangeListener<Node> getExerciseFocusListener() {
        return exerciseFocusListener;
    }
}