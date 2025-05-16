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

package slapp.editor.derivation;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

import java.util.ArrayList;
import java.util.List;

/**
 * View for the derivation exercise.
 */
public class DerivationView implements ExerciseView<DecoratedRTA> {
    MainWindowView mainView;
    private RichTextAreaSkin.KeyMapValue keyboardSelector;
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double splitPanePrefWidth = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> splitPaneHeightSpinner;
    private Spinner<Double> splitPaneWidthSpinner;
    private Node currentSpinnerNode;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private boolean isLeftmostScopeLine = true;
    private Node exerciseControlNode = new VBox();
    private SplitPane contentSplitPane = new SplitPane();
    private GridPane grid = new GridPane();
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
    private Stage metaLangStage;
    private Stage staticHelpStage;





    /**
     * Construct the derivation view
     * @param mainView the main view
     */
    DerivationView(MainWindowView mainView) {
        this.mainView = mainView;
        Pane blankPane = new Pane();
        blankPane.setMinWidth(0);
        blankPane.setMaxWidth(1000);
        blankPane.setStyle("-fx-background-color: white;");
        contentSplitPane.getItems().addAll(grid, blankPane);
        contentSplitPane.setOrientation(Orientation.HORIZONTAL);
        contentSplitPane.setMinHeight(10.0);
        contentSplitPane.setMinWidth(10.0);

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

        insertLineButton.setPrefWidth(100);
        insertLineButton.setTooltip(new Tooltip("Insert blank line above current one"));
        deleteLineButton.setPrefWidth(100);
        deleteLineButton.setTooltip(new Tooltip("Delete current line"));
        indentButton.setPrefWidth(100);
        indentButton.setTooltip(new Tooltip("Increase depth of current line"));
        outdentButton.setPrefWidth(100);
        outdentButton.setTooltip(new Tooltip("Decrease depth of current line"));
        addShelfButton.setPrefWidth(100);
        addShelfButton.setTooltip(new Tooltip("Add shelf beneath current line"));
        addGapButton.setPrefWidth(100);
        addGapButton.setTooltip(new Tooltip("Add gap beneath current line"));
        insertSubButton.setPrefWidth(100);
        insertSubButton.setTooltip(new Tooltip("Insert subderivation above current line"));
        insertSubsButton.setPrefWidth(100);
        insertSubsButton.setTooltip(new Tooltip("Insert subderivation pair above current line"));
        undoButton.setPrefWidth(100);
        undoButton.setTooltip(new Tooltip("Undo last button action"));
        redoButton.setPrefWidth(100);
        redoButton.setTooltip(new Tooltip("Redo button action"));

        VBox controlBox = new VBox(20, undoButton, redoButton, insertLineButton, deleteLineButton, insertSubButton, insertSubsButton, indentButton, outdentButton, addShelfButton, addGapButton);
        controlBox.setAlignment(Pos.BASELINE_RIGHT);
        controlBox.setMargin(insertLineButton, new Insets(0,0,20, 0));
        controlBox.setPadding(new Insets(40,20,0,20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;

        showMetaLangButton = new Button("\u2133");
        showMetaLangButton.setPrefWidth(25);
        showMetaLangButton.setMaxHeight(40);
        showMetaLangButton.setPadding(new Insets(2));
        showMetaLangButton.setTooltip(new Tooltip("Show metalanguage information."));
        showMetaLangButton.setStyle("-fx-background-color: rgb(" + 0xF0 + "," + 0xF0 + "," + 0xF0 + "); ");


        showMetaLangButton.setOnAction(e -> {
    //        TextHelpPopup.helpMetalanguage();


        });


    }

    /**
     * Initialize statement, comment and split pane
     */
    void initializeViewDetails() {

        //statement
        RichTextArea statementRTA = exerciseStatement.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.round(statementPrefHeight / mainView.getScalePageHeight() * 100.0 );
        statementHeightSpinner = new Spinner<>(0.0, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(65);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        statementHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = statementHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = statementHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        if (!showMetaLang) statementRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        else statementRTA.minWidthProperty().bind(mainView.scalePageWidthProperty());
        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));




        //comment
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round(commentPrefHeight / mainView.getScalePageHeight() * 100.0 );
        commentHeightSpinner = new Spinner<>(0.0, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(65);
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
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));



        //split pane
        double splitPaneInitialWidth = Math.round(splitPanePrefWidth / mainView.getScalePageWidth() * 20.0) * 5.0;
        splitPaneWidthSpinner = new Spinner<>(70.0, 999.0, splitPaneInitialWidth, 5.0);
        splitPaneWidthSpinner.setPrefWidth(65);
        splitPaneWidthSpinner.setDisable(false);
        splitPaneWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));
        contentSplitPane.prefWidthProperty().bind(Bindings.multiply(mainView.scalePageWidthProperty(), DoubleProperty.doubleProperty(splitPaneWidthSpinner.getValueFactory().valueProperty()).divide(100.0)));

        splitPaneWidthSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = splitPaneWidthSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = splitPaneWidthSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        contentSplitPane.setMinHeight(200);
        splitPaneHeightSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        splitPaneHeightSpinner.setPrefWidth(65);
        splitPaneHeightSpinner.setDisable(true);
        splitPaneHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));

        contentSplitPane.heightProperty().addListener((ob, ov, nv) -> {
            splitPaneHeightSpinner.getValueFactory().setValue((double) Math.round(contentSplitPane.getHeight() / mainView.getScalePageHeight() * 100));
        });



        //page size listeners
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            splitPaneHeightSpinner.getValueFactory().setValue((double) Math.round(contentSplitPane.getHeight() / mainView.getScalePageHeight() * 100.0));
        });

        mainView.scalePageWidthProperty().addListener((ob, ov, nv) -> {
            contentSplitPane.prefWidthProperty().unbind();
            splitPaneWidthSpinner.getValueFactory().setValue((double) Math.round(splitPaneWidthSpinner.getValue() * ov.doubleValue() / nv.doubleValue() / 5.0) * 5.0);
            contentSplitPane.prefWidthProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(splitPaneWidthSpinner.getValueFactory().valueProperty()).divide(100.0))));
        });

        setSizeSpinners();


    }

    public void setRightControlBox() {
        //right control box
        bigCheck = new Text("\ue89a");
        bigCheck.setFont(Font.font("Noto Serif Combo", 72));
     //   bigCheck.setFill(checkColor);

        checkedElements = new Text(checkMessage);
        checkedElements.setFont(Font.font("Noto Serif Combo", 11));
     //   checkedElements.setFill(checkElementsColor);
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
   //    checkTriesLabel = new Label();
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
        contentSplitPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != contentSplitPane) {
                contentSplitPane.requestFocus();
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
            if (inHierarchy(nv, contentSplitPane) && currentSpinnerNode != contentSplitPane) {
                currentSpinnerNode = contentSplitPane;
                mainView.updateSizeSpinners(splitPaneHeightSpinner, splitPaneWidthSpinner);
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

    /**
     * Update visible grid from view lines list
     */
    void setGridFromViewLines() {

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
            HBox contentBox = new HBox();
            if (viewLine.getLineContentBoxedDRTA() != null) {
                BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                DecoratedRTA drta = bdrta.getDRTA();
                drta.getKeyboardSelector().valueProperty().setValue(keyboardSelector);
                RichTextArea rta = bdrta.getRTA();
                rta.setMaxHeight(contentRowHeight);
                rta.setMinHeight(contentRowHeight);
                rta.setPrefWidth(100);
                rta.getStylesheets().add("slappDerivation.css");

                if (viewLine.isLineHighlight()) {
                    rta.getStylesheets().clear();
                    rta.getStylesheets().add("slappDerivationHighlight.css");
                }

                contentBox = bdrta.getBoxedRTA();
                contentBox.setHgrow(bdrta.getRTA(), Priority.ALWAYS);
            }
            if (depth > 1) {
                Pane spacer1 = new Pane();
                if (isLeftmostScopeLine) spacer1.setStyle("-fx-border-color: white white white black; -fx-border-width: 0 0 0 1");
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
                    contentBox.setStyle("-fx-border-color: white, white, white, black; -fx-border-width: 0 0 0 1;");
                else contentBox.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0;");

                grid.add(contentBox, depth, index, 21 - depth, 1);

            } else {
                Pane endSpacer = new Pane();
                if (LineType.isGapLine(lineType)) endSpacer.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0;");
                if (LineType.isShelfLine(lineType) && (depth > 1 || isLeftmostScopeLine))  endSpacer.setStyle("-fx-border-color: black white white black; -fx-border-width: 1 0 0 1;");
                grid.add(endSpacer, depth, index, 1, 1);
            }

            if (viewLine.getJustificationFlow() != null) grid.add(viewLine.getJustificationFlow(), 22, index);
        }
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

    public void showMetalanguageHelp(Document doc) {

        if (metaLangStage == null || !metaLangStage.isShowing()) {
            RichTextArea mrta = new RichTextArea(EditorMain.mainStage);
            mrta.getActionFactory().open(doc).execute(new ActionEvent());
            mrta.setPadding(new Insets(20, 0, 20, 20));
            mrta.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            mrta.setPrefWidth(800);
            mrta.setPrefHeight(700);
            mrta.setEditable(false);


            Scene scene = new Scene(mrta);
            scene.getStylesheets().add(RichTextArea.class.getClassLoader().getResource("slappEditor.css").toExternalForm());
            mrta.applyCss();
            metaLangStage = new Stage();
            metaLangStage.setScene(scene);
            metaLangStage.setTitle("SLAPP Text Help");
            metaLangStage.initModality(Modality.NONE);
            metaLangStage.getIcons().addAll(EditorMain.icons);
            metaLangStage.initOwner(EditorMain.mainStage);
            Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
            metaLangStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 820));
            metaLangStage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 720));

            metaLangStage.show();
        }
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

    public void clearStandingPopups() {
        if (staticHelpStage != null) staticHelpStage.close();
        if (metaLangStage != null) metaLangStage.close();
    }







    /**
     * Set justification flow from a single view line on grid
     * @param index the line index
     */
    void setJustificationFlowOnGrid(int index) {
        ViewLine line = viewLines.get(index);
        if (line.getJustificationFlow() != null) grid.add(line.getJustificationFlow(), 22, index);
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
     * The split pane with derivation grid in its left half
     * @return the split pane
     */
    SplitPane getContentSplitPane() { return contentSplitPane; }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight(); }

    /**
     * The preferred width of the split pane window
     * @return the width value
     */
    double getSplitPanePrefWidth() {return contentSplitPane.getPrefWidth(); }

    /**
     * The preferred width of the split pane window
     * @param splitPanePrefWidth the width value
     */
    void setSplitPanePrefWidth(double splitPanePrefWidth) { this.splitPanePrefWidth = splitPanePrefWidth; }

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

    Spinner getSplitPaneHeightSpinner() { return splitPaneHeightSpinner; }

    Spinner getSplitPaneWidthSpinner() { return splitPaneWidthSpinner; }

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
        Node statementNode;
        if (!showMetaLang) {
            statementNode =exerciseStatement.getEditor();
        }
        else {
            AnchorPane anchorPane = new AnchorPane(exerciseStatement.getEditor(), showMetaLangButton);
            anchorPane.setTopAnchor(exerciseStatement.getEditor(), 0.0);
            anchorPane.setLeftAnchor(exerciseStatement.getEditor(), 0.0);
            anchorPane.setRightAnchor(showMetaLangButton, 3.0);
            anchorPane.setBottomAnchor(showMetaLangButton, 3.0);
            anchorPane.maxWidthProperty().bind(mainView.scalePageWidthProperty());
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
        exerciseStatement.getEditor().setPrefHeight(height);
    }

    /**
     * The node which includes content of this exercise
     * @return the content node
     */
    @Override
    public Node getExerciseContentNode() { return new VBox(contentSplitPane); }


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


}
