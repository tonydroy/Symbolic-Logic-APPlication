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

package slapp.editor.map_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import slapp.editor.vert_tree_abexplain.ABExpRootLayout;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpExercise;

import java.util.function.UnaryOperator;

public class MapABExpView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private MapABExpExercise mapABExpExercise;
    private RichTextAreaSkin.KeyMapValue defaultKeyboard;
    private RichTextAreaSkin.KeyMapValue defaultMapKeyboard;
    private String explainPrompt = "";
    private Label choiceLeadLabel = new Label();
    private CheckBox aCheckBox = new CheckBox();
    private CheckBox bCheckBox = new CheckBox();
    private HBox choiceBox;
    private BorderPane root;
    private MapABExpRootLayout rootLayout;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private double statementPrefHeight = 0;

    private double commentPrefHeight = 0;
    private double explainPrefHeight = 0;
    private double mainPanePrefHeight = 0;
    private double mainPanePrefWidth = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
    private Spinner<Double> mainPaneHeightSpinner;
    private Spinner<Double> mainPaneWidthSpinner;
    private Spinner<Double> choicesHeightSpinner;
    private Spinner<Double> choicesWidthSpinner;
    private Node currentSpinnerNode;

    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private VBox controlBox = new VBox(25);
    private Button undoButton;
    private Button redoButton;
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    Node exerciseControlNode;
    SplitPane mainPane;

    private int pointsPossible;
    private TextField pointsEarnedTextField;

    private Node rightControlNode;
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
    private Stage  staticHelpStage;;

    MapABExpView(MainWindowView mainView, MapABExpExercise mapABExpExercise) {
        this.mainView = mainView;
        this.mapABExpExercise = mapABExpExercise;
        Font labelFont = new Font("Noto Serif Combo", 11);
        choiceLeadLabel.setFont(labelFont); aCheckBox.setFont(labelFont); bCheckBox.setFont(labelFont);
        choiceBox = new HBox(20, choiceLeadLabel, aCheckBox, bCheckBox);
        choiceBox.setPadding(new Insets(7,7,7,10));
        choiceBox.setStyle("-fx-border-color: gainsboro; -fx-background-color: white;");

        root = new BorderPane();
        rootLayout = new MapABExpRootLayout(this);

        VBox contentBox = new VBox(3, rootLayout, choiceBox, explainDRTA.getEditor());
        root.setCenter(contentBox);

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");
        undoButton.setPrefWidth(64);
        redoButton.setPrefWidth(64);
        undoButton.setPrefHeight(28);
        redoButton.setPrefHeight(28);

        controlBox.getChildren().addAll(undoButton, redoButton);
        controlBox.setAlignment(Pos.BASELINE_RIGHT);
        controlBox.setPadding(new Insets(50,20,0,20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        pointsEarnedTextField.setTextFormatter(textFormatter);
    }

    void initializeViewDetails() {
        //statement
        RichTextArea statementRTA = exerciseStatement.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.round(statementPrefHeight / mainView.getScalePageHeight() * 100.0 );
        statementHeightSpinner = new Spinner<>(0.0, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(60);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        statementHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = statementHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = statementHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        statementRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(60);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

       //comment
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round(commentPrefHeight / mainView.getScalePageHeight() * 100.0 );
        commentHeightSpinner = new Spinner<>(0.0, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(60);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        commentHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = commentHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = commentHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        commentRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        commentRTA.minWidthProperty().bind(mainView.scalePageWidthProperty());
        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(60);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        //explain
        RichTextArea explainRTA = explainDRTA.getEditor();
        explainRTA.getStylesheets().add("slappTextArea.css");
        explainRTA.setPromptText(explainPrompt);

        double explainInitialHeight = Math.round(explainPrefHeight / mainView.getScalePageHeight() * 100.0 );
        explainHeightSpinner = new Spinner<>(0.0, 999.0, explainInitialHeight, 1.0);
        explainHeightSpinner.setPrefWidth(60);
        explainHeightSpinner.setDisable(false);
        explainHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        explainRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        explainHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = explainHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = explainHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        explainRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        explainRTA.minWidthProperty().bind(mainView.scalePageWidthProperty());
        explainWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explainWidthSpinner.setPrefWidth(60);
        explainWidthSpinner.setDisable(true);
        explainWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        //choice (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(60);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));

        choiceBox.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        choicesWidthSpinner = new Spinner<>(0.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(60);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        //main pane
        AnchorPane mainPane1 = rootLayout.getMain_pane();
        mainPane = rootLayout.getBase_pane();
        mainPane.setStyle("-fx-focus-color: gainsboro;");

        mainPane1.setMinHeight(250.0);
        mainPane1.setMinWidth(mainView.getScalePageWidth());


        mainPaneHeightSpinner = new Spinner<>(5, 999.0, 0, 1.0);
        mainPaneHeightSpinner.setPrefWidth(60);
        mainPaneHeightSpinner.setDisable(true);
        mainPaneHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        mainPane1.heightProperty().addListener((ob, ov, nv) -> {
            mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / mainView.getScalePageHeight() * 100));
        });

        mainPaneWidthSpinner = new Spinner<>(100.0, 999.0, 0, 1.0);
        mainPaneWidthSpinner.setPrefWidth(60);
        mainPaneWidthSpinner.setDisable(true);
        mainPaneWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));
        mainPane1.widthProperty().addListener((ob, ov, nv) -> {
            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getWidth() /mainView.getScalePageWidth() * 100));
        });


        //page size listeners
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            explainRTA.prefHeightProperty().unbind();
            explainHeightSpinner.getValueFactory().setValue((double) Math.round(explainHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            explainRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / mainView.getScalePageHeight() * 100.0));

            double choicesHeightValue = Math.round(choiceBox.getHeight() / mainView.getScalePageHeight() * 100);
            choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
        });

        mainView.scalePageWidthProperty().addListener((ob, ov, nv) -> {
            mainPane.prefWidthProperty().unbind();
            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round(mainPaneWidthSpinner.getValue() * ov.doubleValue() / nv.doubleValue() / 5.0) * 5.0);
            mainPane.prefWidthProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(mainPaneWidthSpinner.getValueFactory().valueProperty()).divide(100.0))));
        });

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

        checkButton = new Button("Check Map");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check map for correctness."));
        checkTriesLabel = new Label();

        checkProgButton = new Button("Check Progress");
        checkProgButton.setPrefWidth(105);
        checkProgButton.setTooltip(new Tooltip("Check map progress so far."));
        VBox checksBox = new VBox(10, checkProgButton, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);
        checksBox.setMargin(checkProgButton, new Insets(0,0,10, 0));

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));


        VBox rightControlBox = new VBox(40, bigCheckBox, checksBox, staticHelpButton );
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(60,20,0,20));
        rightControlNode = rightControlBox;

        deactivateBigCheck();

    }

    public void activateBigCheck() {
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

    //record status, and deactivate
    public void deactivateBigCheck() {
        if (bigCheck.getOpacity() > .5) checkShowing = true;
        else checkShowing = false;
        bigCheck.setOpacity(0.0);
        checkedElements.setOpacity(0.0);
    }

    public void showStaticHelp(Document doc) {

        if (staticHelpStage == null || !staticHelpStage.isShowing()) {
            RichTextArea hrta = new RichTextArea(EditorMain.mainStage);
            hrta.getActionFactory().open(doc).execute(new ActionEvent());
            hrta.setPadding(new Insets(20, 0, 20, 20));
            hrta.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            hrta.setPrefWidth(400);
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
            staticHelpStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 420));
            staticHelpStage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 320));

            staticHelpStage.show();
        }
    }

    public Stage getStaticHelpStage() { return staticHelpStage; }


    private void setSizeSpinners() {

        mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != mainPane) {
                mainPane.requestFocus();
            }
        });
        choiceBox.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != choiceBox) {
                choiceBox.requestFocus();
            }
        });

        mainView.getMainScene().focusOwnerProperty().addListener((ob, ov, nv) -> {

            RichTextArea statementRTA = exerciseStatement.getEditor();
            if (inHierarchy(nv, statementRTA) && currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
                return;
            }
            RichTextArea commentRTA = exerciseComment.getEditor();
            if (inHierarchy(nv, commentRTA) && currentSpinnerNode != commentRTA) {
                currentSpinnerNode = commentRTA;
                mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
                return;
            }
            RichTextArea explainRTA = explainDRTA.getEditor();
            if (inHierarchy(nv, explainRTA) && currentSpinnerNode != explainRTA) {
                currentSpinnerNode = explainRTA;
                mainView.updateSizeSpinners(explainHeightSpinner, explainWidthSpinner);
                return;
            }
            if (inHierarchy(nv, choiceBox) && currentSpinnerNode != choiceBox) {
                currentSpinnerNode = choiceBox;
                double choicesHeightValue = Math.round(choiceBox.getHeight() / mainView.getScalePageHeight() * 100);
                choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                mainView.updateSizeSpinners(choicesHeightSpinner, choicesWidthSpinner);
                return;
            }
            if (inHierarchy(nv, mainPane) && currentSpinnerNode != mainPane) {
                currentSpinnerNode = mainPane;
                mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                return;
            }

        });
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) {
            return true;
        }
        while (node != null) {
            if (node == potentialHierarchyElement) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    public MainWindowView getMainView() {
        return mainView;
    }

    public MapABExpRootLayout getRootLayout() {return rootLayout;}

    public VBox getControlBox() {  return controlBox;  }

    public Button getUndoButton() { return undoButton;   }

    public Button getRedoButton() { return redoButton;   }

    public boolean isUndoRedoFlag() {    return undoRedoFlag.get();    }

    public BooleanProperty undoRedoFlagProperty() {    return undoRedoFlag;    }

    public void setUndoRedoFlag(boolean undoRedoFlag) {    this.undoRedoFlag.set(undoRedoFlag);    }

    public DecoratedRTA getExplainDRTA() { return explainDRTA; }

    public Label getChoiceLeadLabel() { return choiceLeadLabel;  }

    public CheckBox getaCheckBox() {  return aCheckBox;  }

    public CheckBox getbCheckBox() {  return bCheckBox;  }

    public void setExplainPrompt(String explainPrompt) {    this.explainPrompt = explainPrompt;  }

    public RichTextAreaSkin.KeyMapValue getDefaultKeyboard() {     return defaultKeyboard;   }

    public void setDefaultKeyboard(RichTextAreaSkin.KeyMapValue defaultKeyboard) {    this.defaultKeyboard = defaultKeyboard;  }

    public RichTextAreaSkin.KeyMapValue getDefaultMapKeyboard() {
        return defaultMapKeyboard;
    }

    public void setDefaultMapKeyboard(RichTextAreaSkin.KeyMapValue defaultMapKeyboard) {
        this.defaultMapKeyboard = defaultMapKeyboard;
    }

    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();   }

    public void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }

    public double getExplainPrefHeight() { return explainDRTA.getEditor().getPrefHeight();    }

    public void setExplainPrefHeight(double explainPrefHeight) {   this.explainPrefHeight = explainPrefHeight;  }

    public double getMainPanePrefHeight() {    return rootLayout.getMain_pane().getPrefHeight();    }

    public void setMainPanePrefHeight(double mainPanePrefHeight) { this.mainPanePrefHeight = mainPanePrefHeight;  }

    public double getMainPanePrefWidth() { return rootLayout.getPrefWidth();   }

    public void setMainPanePrefWidth(double mainPanePrefWidth) {   this.mainPanePrefWidth = mainPanePrefWidth;  }



    public Button getCheckButton() {
        return checkButton;
    }

    public Button getCheckProgButton() {
        return checkProgButton;
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
    public Node getExerciseStatementNode() { return exerciseStatement.getEditor();   }

    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        exerciseStatement.getEditor().setPrefHeight(height);
    }

    @Override
    public Node getExerciseContentNode() {
        return new VBox(root);
    }
    @Override
    public Node getExerciseControl() {
        return exerciseControlNode;
    }
    @Override
    public Node getRightControl() { return rightControlNode; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            if (!mainView.isInstructorFunctions()) pointsEarnedTextField.setDisable(true);
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

    public MapABExpExercise getMapABExpExercise() {
        return mapABExpExercise;
    }




}
