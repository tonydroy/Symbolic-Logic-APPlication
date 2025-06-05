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

package slapp.editor.vert_tree_abefexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.vert_tree_abefexplain.ABEFExpRootLayout;
import slapp.editor.vert_tree_explain.ExpRootLayout;

public class VerticalTreeABEFExpView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private RichTextAreaSkin.KeyMapValue defaultKeyboard;
    private String explainPrompt = "";
    private Label abChoiceLeadLabel = new Label();
    private CheckBox aCheckBox = new CheckBox();
    private CheckBox bCheckBox = new CheckBox();
    private HBox choiceBox1;
    private Label efChoiceLeadLabel = new Label();
    private CheckBox eCheckBox = new CheckBox();
    private CheckBox fCheckBox = new CheckBox();
    private HBox choiceBox2;


    private BorderPane root;
    private ABEFExpRootLayout rootLayout;
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
    VBox choicesBox;


    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private VBox controlBox = new VBox(25);
    private Button undoButton;
    private Button redoButton;
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    Node exerciseControlNode;
    SplitPane mainPane;

    private int pointsPossible;
    private TextField pointsEarnedTextField;

    VerticalTreeABEFExpView(MainWindowView mainView) {
        this.mainView = mainView;

//        aCheckBox.setFocusTraversable(false); bCheckBox.setFocusTraversable(false); eCheckBox.setFocusTraversable(false); fCheckBox.setFocusTraversable(false);
        Font labelFont = new Font("Noto Serif Combo", 11);
        abChoiceLeadLabel.setFont(labelFont); aCheckBox.setFont(labelFont); bCheckBox.setFont(labelFont);
        choiceBox1 = new HBox(20, abChoiceLeadLabel, aCheckBox, bCheckBox);
        choiceBox1.setPadding(new Insets(7,7,7,10));
        choiceBox1.setStyle("-fx-border-color: gainsboro; -fx-background-color: white;");
        efChoiceLeadLabel.setFont(labelFont); eCheckBox.setFont(labelFont); fCheckBox.setFont(labelFont);
        choiceBox2 = new HBox(20, efChoiceLeadLabel, eCheckBox, fCheckBox);
        choiceBox2.setPadding(new Insets(7,7,7,10));
        choiceBox2.setStyle("-fx-border-color: gainsboro; -fx-background-color: white;");
        choicesBox = new VBox(choiceBox1, choiceBox2);

        root = new BorderPane();
        rootLayout = new ABEFExpRootLayout(this);

        VBox contentBox = new VBox(3, rootLayout, choicesBox, explainDRTA.getEditor());
        root.setCenter(contentBox);

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");
        undoButton.setPrefWidth(64);
        redoButton.setPrefWidth(64);
        undoButton.setPrefHeight(28);
        redoButton.setPrefHeight(28);

        controlBox.getChildren().addAll(undoButton, redoButton);
        controlBox.setAlignment(Pos.BASELINE_RIGHT);
        controlBox.setPadding(new Insets(100,20,0,40));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;
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

       //choices (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(60);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));

        choicesBox.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        choicesWidthSpinner = new Spinner<>(0.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(60);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        //main pane
        AnchorPane mainPane1 = rootLayout.getMain_pane();
        mainPane = rootLayout.getBase_pane();
        mainPane.setStyle("-fx-focus-color: gainsboro;");
        mainPane1.setMinHeight(150.0);
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

            double choicesHeightValue = Math.round(choicesBox.getHeight() / mainView.getScalePageHeight() * 100);
            choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
        });

        mainView.scalePageWidthProperty().addListener((ob, ov, nv) -> {
            mainPane.prefWidthProperty().unbind();
            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round(mainPaneWidthSpinner.getValue() * ov.doubleValue() / nv.doubleValue() / 5.0) * 5.0);
            mainPane.prefWidthProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(mainPaneWidthSpinner.getValueFactory().valueProperty()).divide(100.0))));
        });

        setSizeSpinners();

    }

    private void setSizeSpinners() {

        mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != mainPane) {
                mainPane.requestFocus();
            }
        });
        choicesBox.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != choicesBox) {
                choicesBox.requestFocus();
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
            if (inHierarchy(nv, choicesBox) && currentSpinnerNode != choicesBox) {
                currentSpinnerNode = choicesBox;
                double choicesHeightValue = Math.round(choicesBox.getHeight() / mainView.getScalePageHeight() * 100);
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

    public ABEFExpRootLayout getRootLayout() {return rootLayout;}

    public VBox getControlBox() {  return controlBox;  }

    public Button getUndoButton() { return undoButton;   }

    public Button getRedoButton() { return redoButton;   }

    public boolean isUndoRedoFlag() {    return undoRedoFlag.get();    }

    public BooleanProperty undoRedoFlagProperty() {    return undoRedoFlag;    }

    public void setUndoRedoFlag(boolean undoRedoFlag) {    this.undoRedoFlag.set(undoRedoFlag);    }

    public DecoratedRTA getExplainDRTA() { return explainDRTA; }

    public Label getEFChoiceLeadLabel() { return efChoiceLeadLabel;  }
    public CheckBox geteCheckBox() {  return eCheckBox;  }
    public CheckBox getfCheckBox() {  return fCheckBox;  }
    public Label getABChoiceLeadLabel() { return abChoiceLeadLabel;  }
    public CheckBox getaCheckBox() {  return aCheckBox;  }
    public CheckBox getbCheckBox() {  return bCheckBox;  }

    public RichTextAreaSkin.KeyMapValue getDefaultKeyboard() {    return defaultKeyboard;   }

    public void setDefaultKeyboard(RichTextAreaSkin.KeyMapValue defaultKeyboard) {     this.defaultKeyboard = defaultKeyboard;  }

    public void setExplainPrompt(String explainPrompt) {    this.explainPrompt = explainPrompt; }

    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();   }

    public void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }

    public double getExplainPrefHeight() { return explainDRTA.getEditor().getPrefHeight();    }

    public void setExplainPrefHeight(double explainPrefHeight) {   this.explainPrefHeight = explainPrefHeight;  }

    public double getMainPanePrefHeight() {    return rootLayout.getMain_pane().getPrefHeight();    }

    public void setMainPanePrefHeight(double mainPanePrefHeight) { this.mainPanePrefHeight = mainPanePrefHeight;  }

    public double getMainPanePrefWidth() { return rootLayout.getMain_pane().getPrefWidth();   }

    public void setMainPanePrefWidth(double mainPanePrefWidth) {   this.mainPanePrefWidth = mainPanePrefWidth;  }



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
    public Node getRightControl() { return null; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            Label pointsPossibleLabel = new Label(" / " + pointsPossible);
            HBox pointBox = new HBox(pointsEarnedTextField, pointsPossibleLabel);
            return pointBox;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }
    public TextField getPointsEarnedTextField() {
        return pointsEarnedTextField;
    }


}
