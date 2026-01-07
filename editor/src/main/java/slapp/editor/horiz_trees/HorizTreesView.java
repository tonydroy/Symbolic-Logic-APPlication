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
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
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

/**
 * View for the free form exercise
 */
public class HorizTreesView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private DecoratedRTA statementDRTA = new DecoratedRTA();
    private DecoratedRTA commentDRTA = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private DecoratedRTA interpretationDRTA = new DecoratedRTA();
    private RichTextArea interpretationRTA;
    private double explainPrefHeight = 0;
    private double interpretationPrefHeight = 0;
    private String explainPrompt = "";
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
    private Node exerciseControlNode;
    private Node freeFormControlNode;
    private List<ViewElement> viewElements = new ArrayList<>();
    private VBox contentBox = new VBox(2);
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> interpretationHeightSpinner;
    private Spinner<Double> interpretationWidthSpinner;
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



    /**
     * Construct the free form view
     * @param mainView the main view
     */
    public HorizTreesView(MainWindowView mainView) {
        this.mainView = mainView;

        //all the buttons from which the exercise includes some
        indentButton = newControlsButton("Indent", "Indent current window.");
        outdentButton = newControlsButton("Outdent", "Outdent current window.");
        removeButton = newControlsButton("Remove", "Remove current window.");
        restoreButton = newControlsButton ("Restore", "Restore last removed item.");
        moveUpButton = newControlsButton("Move Up", "Place above previous item.");
        moveDownButton = newControlsButton("Move Down", "Place below following item.");

        addHTreeButton = newControlsButton("Add Window", "Insert new tree pane.");


  //      VBox rightControlBox = new VBox(20);
  //      rightControlBox.setAlignment(Pos.BASELINE_LEFT);
  //      rightControlBox.setPadding(new Insets(40,20,0,20));
  //      freeFormControlNode = rightControlBox;

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

    void initalizeInterpretationRTA() {

        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPromptText("Interpretation");
        interpretationDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);

        double interpretationInitialHeight = Math.round((interpretationPrefHeight - RTA_SCREEN_BMP)/ mainView.getScalePageHeight() * 100 );

        interpretationHeightSpinner = new Spinner<>(0.0, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(60);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        interpretationHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = interpretationHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = interpretationHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        interpretationRTA.maxWidthProperty().bind(Bindings.add(mainView.scalePageWidthProperty(), RTA_WIDTH_BMP));
        interpretationRTA.minWidthProperty().bind(Bindings.add(mainView.scalePageWidthProperty(), RTA_WIDTH_BMP));
        interpretationRTA.contentAreaWidthProperty().bind(mainView.scalePageWidthProperty());
        interpretationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        interpretationWidthSpinner.setPrefWidth(60);
        interpretationWidthSpinner.setDisable(true);
        interpretationWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        interpretationRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != interpretationRTA) {
                currentSpinnerNode = interpretationRTA;
                mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);
            }
        });

    }

    /*
     * Construct new button
     * @param name Button name
     * @param tip Button tooltip
     * @return the button
     */
    private Button newControlsButton(String name, String tip) {
        Button button = new Button(name);
        button.setPrefWidth(100);
        button.setPrefHeight(28);
        button.setTooltip(new Tooltip(tip));
        return button;
    }

    /**
     * initialize statement and comment fields
     */
    void initializeViewDetails() {

        //statement
        RichTextArea statementRTA = statementDRTA.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.round((statementPrefHeight - RTA_SCREEN_BMP) / mainView.getScalePageHeight() * 100.0);
        statementHeightSpinner = new Spinner<>(0.0, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(60);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        statementHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = statementHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = statementHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        statementRTA.maxWidthProperty().bind(Bindings.add(mainView.scalePageWidthProperty(), RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(mainView.scalePageWidthProperty());
        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(60);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        statementRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
            }
        });

        //comment rta
        RichTextArea commentRTA = commentDRTA.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round((commentPrefHeight - RTA_SCREEN_BMP) / mainView.getScalePageHeight() * 100.0);
        commentHeightSpinner = new Spinner<>(0.0, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(60);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        commentHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = commentHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = commentHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        commentRTA.maxWidthProperty().bind(Bindings.add(mainView.scalePageWidthProperty(), RTA_WIDTH_BMP));
        commentRTA.minWidthProperty().bind(Bindings.add(mainView.scalePageWidthProperty(), RTA_WIDTH_BMP));
        commentRTA.contentAreaWidthProperty().bind(mainView.scalePageWidthProperty());
        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(60);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        commentRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != commentRTA) {
                currentSpinnerNode = commentRTA;
                mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
            }
        });

        //explain
        RichTextArea explainRTA = explainDRTA.getEditor();
        explainRTA.getStylesheets().add("slappTextArea.css");
        explainRTA.setPromptText(explainPrompt);

        double explainInitialHeight = Math.round((explainPrefHeight - RTA_SCREEN_BMP) / mainView.getScalePageHeight() * 100.0 );
        explainHeightSpinner = new Spinner<>(0.0, 999.0, explainInitialHeight, 1.0);
        explainHeightSpinner.setPrefWidth(60);
        explainHeightSpinner.setDisable(false);
        explainHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        explainRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        explainHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = explainHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = explainHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        explainRTA.maxWidthProperty().bind(Bindings.add(mainView.scalePageWidthProperty(), RTA_WIDTH_BMP));
        explainRTA.minWidthProperty().bind(Bindings.add(mainView.scalePageWidthProperty(), RTA_WIDTH_BMP));
        explainRTA.contentAreaWidthProperty().bind(mainView.scalePageWidthProperty());

        explainWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explainWidthSpinner.setPrefWidth(60);
        explainWidthSpinner.setDisable(true);
        explainWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        explainRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != explainRTA) {
                currentSpinnerNode = explainRTA;
                mainView.updateSizeSpinners(explainHeightSpinner, explainWidthSpinner);
            }
        });


        //page height listener
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

            explainRTA.prefHeightProperty().unbind();
            explainHeightSpinner.getValueFactory().setValue((double) Math.round(explainHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            explainRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

            interpretationRTA.prefHeightProperty().unbind();
            interpretationHeightSpinner.getValueFactory().setValue((double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        });
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

        //     checkProgButton = new Button("Check Progress");
        //     checkProgButton.setPrefWidth(105);
        //     checkProgButton.setTooltip(new Tooltip("Check tree progress so far."));
        VBox checksBox = new VBox(5, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);
//        checksBox.setMargin(checkProgButton, new Insets(0,0,10, 0));

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


    /**
     * Update the content VBox from the view elements
     */
    void updateContentFromViewElements() {
        contentBox.getChildren().clear();
        contentBox.getChildren().add(interpretationRTA);
        for (ViewElement element : viewElements) {
            Node node = element.getNode();
            HBox windowBox = new HBox(node);

            windowBox.setPrefWidth(HBox.USE_COMPUTED_SIZE);
            windowBox.setMinWidth(HBox.USE_PREF_SIZE);
            windowBox.setMaxWidth(HBox.USE_PREF_SIZE);

            AnchorPane pane = (AnchorPane) node;
            pane.setPadding(new Insets(0, RTA_WIDTH_BMP, 0, 0));

            contentBox.getChildren().add(windowBox);
        }
        contentBox.getChildren().add(explainDRTA.getEditor());
    }

    void setBorders(int i) {
        for (ViewElement element : viewElements) {
            element.getNode().getStyleClass().remove("fx-border-color");
            element.getNode().setStyle("-fx-border-color: gainsboro");
      //      System.out.println("other: " + element.getNode().getStyle());
        }
        ViewElement currentElement = viewElements.get(i);
        currentElement.getNode().getStyleClass().remove("fx-border-color");
        currentElement.getNode().setStyle("-fx-border-color: lightblue");
    //    System.out.println("current: " + currentElement.getNode().getStyle());


    }

    void setContentSpinners(int index, Spinner heightSpinner, Spinner widthSpinner) {
        if (currentSpinnerNode != viewElements.get(index).getNode()) {
            currentSpinnerNode = viewElements.get(index).getNode();
            mainView.updateSizeSpinners(heightSpinner, widthSpinner);
        }
    }

    /**
     * The list of view elements
     * @return the list
     */
    List<ViewElement> getViewElements() {      return viewElements;  }

    /**
     * The indent button
     * @return the button
     */
    Button getIndentButton() {     return indentButton;  }

    /**
     * The outdent button
     * @return the button
     */
    Button getOutdentButton() {     return outdentButton;   }

    /**
     * The button to remove a current element
     * @return the button
     */
    Button getRemoveButton() {     return removeButton;   }

    /**
     * Button to restore last removed element
     * @return the button
     */
    Button getRestoreButton() {     return restoreButton;  }

    /**
     * Button to move current element up
     * @return the button
     */
    Button getMoveUpButton() { return moveUpButton; }

    /**
     * Button to move current element down
     * @return the button
     */
    Button getMoveDownButton() { return moveDownButton; }



    /**
     * Button to add horizontal tree element
     * @return the button
     */
    Button getAddHTreeButton() {     return addHTreeButton;  }


    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() {    return commentDRTA.getEditor().getPrefHeight(); }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {     this.commentPrefHeight = commentPrefHeight;   }

    /**
     * The (left) control node for the current free form element
     * @param exerciseControlNode the control node
     */
    void setExerciseControlNode(Node exerciseControlNode) {   this.exerciseControlNode = exerciseControlNode;  }

    public VBox getRightControlBox() {
        return rightControlBox;
    }

    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    @Override
    public DecoratedRTA getExerciseComment() { return commentDRTA;  }

    /**
     * The comment decoratedRTA
     * @param exerciseComment DecoratedRTA
     */
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { commentDRTA = exerciseComment;   }

    /**
     * The exercise prompt
     * @return the statement decorated RTA (T)
     */
    @Override
    public DecoratedRTA getExerciseStatement() {  return statementDRTA;   }

    /**
     * The exercise prompt
     * @param exerciseStatement the statement decorated RTA (T)
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { statementDRTA = exerciseStatement;   }

    /**
     * The exercise prompt node (RTA)
     * @return the statement node
     */
    @Override
    public Node getExerciseStatementNode() { return statementDRTA.getEditor();  }

    /**
     * The preferred height of the statement window
     * @param height the preferred height
     */
    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        statementDRTA.getEditor().setPrefHeight(height);
    }

    /**
     * The node which includes content of this exercise
     * @return the content node
     */
    @Override
    public Node getExerciseContentNode() {   return contentBox;   }

    /**
     * The left control node for this exercise
     * @return the control node
     */
    @Override
    public Node getExerciseControl() {  return exerciseControlNode;   }

    /**
     * The right control node with controls for the exercise as a whole
     * @return
     */
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

    public DecoratedRTA getInterpretationDRTA() {
        return interpretationDRTA;
    }

    public void setInterpretationRTA(RichTextArea interpretationRTA) {
        this.interpretationRTA = interpretationRTA;
    }

    public double getInterpretationPrefHeight() {
        return interpretationRTA.getPrefHeight();
    }

    public void setInterpretationPrefHeight(double interpretationPrefHeight) {
        this.interpretationPrefHeight = interpretationPrefHeight;
    }

    public void setInterpretationDRTA(DecoratedRTA interpretationDRTA) {
        this.interpretationDRTA = interpretationDRTA;
    }

    double getExplainPrefHeight() { return explainDRTA.getEditor().getPrefHeight();    }

    /**
     * The preferred height of the explanation field
     * @param explainPrefHeight the height value
     */
    void setExplainPrefHeight(double explainPrefHeight) {   this.explainPrefHeight = explainPrefHeight;  }

    DecoratedRTA getExplainDRTA() { return explainDRTA; }

    /**
     * The prompt for the explain field
     * @param explainPrompt the String prompt
     */
    void setExplainPrompt(String explainPrompt) {  this.explainPrompt = explainPrompt; }

    public String getCheckMessage() {
        return checkMessage;
    }





    public Button getStaticHelpButton() {
        return staticHelpButton;
    }

    public Button getCheckButton() {
        return checkButton;
    }

    public CheckBox getPermitEmptyBranchesCheck() {
        return permitEmptyBranchesCheck;
    }





    public void setCheckColor(Color checkColor) {
        this.checkColor = checkColor;
    }

    public void setCheckElementsColor(Color checkElementsColor) {
        this.checkElementsColor = checkElementsColor;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public Label getCheckTriesLabel() {
        return checkTriesLabel;
    }

    public VBox getContentBox() {
        return contentBox;
    }




}
