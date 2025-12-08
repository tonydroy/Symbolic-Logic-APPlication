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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * View for the AB/EFG page edit exercise
 */
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
    private Spinner<Double> paginationWidthSpinner;
    private Node currentSpinnerNode;
    private int pointsPossible;
    private TextField pointsEarnedTextField;


    /**
     * Construct AB/EFG page edit view
     * @param mainView the main view
     */
    public ABEFGview(MainWindowView mainView) {
        this.mainView = mainView;
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

        this.pagination = new Pagination();
        pagination.setMaxPageIndicatorCount(5);
        pagination.setPageFactory((index) -> {
            Node page;
            if (index == 0) {
                DecoratedRTA drtaPage0 = contentPageList.get(index);
                RichTextArea rtaPage0 = drtaPage0.getEditor();
                rtaPage0.getStylesheets().add("slappTextArea.css");
                VBox topContentPage = new VBox(3, checksBox, rtaPage0);
                topContentPage.setVgrow(rtaPage0, Priority.ALWAYS);
                topContentPage.setMargin(checksBox, new Insets(5,0,0,0));
                page = topContentPage;
            } else {
                DecoratedRTA drtaPage = contentPageList.get(index);
                RichTextArea rtaPage = drtaPage.getEditor();
                rtaPage.getStylesheets().add("slappTextArea.css");
                page = rtaPage;
            }
            return page;
        });

        this.addPageButton = new Button("Insert Page");
        addPageButton.setTooltip(new Tooltip("Add after current page"));
        addPageButton.setPrefWidth(100.0);

        this.removePageButton = new Button("Remove Page");
        removePageButton.setTooltip(new Tooltip("Remove current page"));
        removePageButton.setPrefWidth(100.0);

        VBox controlBox = (VBox) exerciseControlNode;        ;
        controlBox.setSpacing(30.0);
        controlBox.setPadding(new Insets(200,20,0,30));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        controlBox.getChildren().addAll(addPageButton, removePageButton);

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

    /**
     * Initialize page management for statement, comment, pagination, and choices box
     */
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

        statementRTA.prefWidthProperty().bind(mainView.scalePageWidthProperty());
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

        commentRTA.prefWidthProperty().bind(mainView.scalePageWidthProperty());
        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(60);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));


        //pagination
        double paginationInitialHeight = Math.round(paginationPrefHeight / mainView.getScalePageHeight() * 20.0 ) * 5.0 ;
        paginationHeightSpinner = new Spinner<>(0.0, 999.0, paginationInitialHeight, 5.0);
        paginationHeightSpinner.setPrefWidth(60);
        paginationHeightSpinner.setDisable(false);
        paginationHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        pagination.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(paginationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        paginationHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = paginationHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = paginationHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        pagination.prefWidthProperty().bind(mainView.scalePageWidthProperty());
        paginationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        paginationWidthSpinner.setPrefWidth(60);
        paginationWidthSpinner.setDisable(true);
        paginationWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));


        if (!contentPageList.isEmpty()) {
            contentPageList.get(0).getEditor().setPromptText(contentPrompt);
        }
        pagination.setPageCount(contentPageList.size());

        //choices (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(60);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));

        choicesWidthSpinner = new Spinner<>(0.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(60);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));


        //page height listener
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            pagination.prefHeightProperty().unbind();
            paginationHeightSpinner.getValueFactory().setValue((double) Math.round(paginationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue() / 5.0) * 5.0);
            pagination.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(paginationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            double choicesHeightValue = Math.round(checksBox.getHeight() / mainView.getScalePageHeight() * 100);
            choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);

        });

        setSizeSpinners();
    }

    private void setSizeSpinners() {

        checksBox.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != checksBox) {
                checksBox.requestFocus();
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

            if (inHierarchy(nv, pagination) && currentSpinnerNode != pagination) {
                currentSpinnerNode = pagination;
                mainView.updateSizeSpinners(paginationHeightSpinner, paginationWidthSpinner);
                return;
            }

            if (inHierarchy(nv, checksBox) && currentSpinnerNode != checksBox) {
                currentSpinnerNode = checksBox;
                double choicesHeightValue = Math.round(checksBox.getHeight() / mainView.getScalePageHeight() * 100);
                choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                mainView.updateSizeSpinners(choicesHeightSpinner, choicesWidthSpinner);
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
     * Add DRTA to list at index position; the pagination page factory extracts the RTA.
     * @param index index at which to insert the page
     * @param drta the DecoratedRTA to insert
     */
    void addBlankContentPage(int index, DecoratedRTA drta) {
        contentPageList.add(index, drta);
        pagination.setPageCount(contentPageList.size());
        pagination.setCurrentPageIndex(index);
        pagination.requestFocus();
    }

    /**
     * Remove DRTA at index position from list.
     * @param index the index of the page to remove.
     */
    void removeContentPage(int index) {
        if (index == 0) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Cannot remove top page with selection boxes.");
        } else {
            contentPageList.remove(index);
            int newSize = contentPageList.size();
            pagination.setPageCount(newSize);
            if (newSize >= index) {
                pagination.setCurrentPageIndex(index);
            } else {
                pagination.setCurrentPageIndex(Math.max(0, index - 1));
            }
        }
        pagination.requestFocus();
    }

    /**
     * The AB leader label appears before the AB choice check boxes
     * @return the label
     */
    Label getLeaderLabelAB() {
        return leaderLabelAB;
    }

    /**
     * The EFG leader label appears before the EFG choice check boxes
     * @return the label
     */
    Label getLeaderLabelEFG() { return leaderLabelEFG; }

    /**
     * The A choice check box
     * @return the check box
     */
    CheckBox getCheckBoxA() {
        return checkBoxA;
    }

    /**
     * The B choice check box
     * @return the check box
     */
    CheckBox getCheckBoxB() {
        return checkBoxB;
    }

    /**
     * The E choice check box
     * @return the check box
     */
    CheckBox getCheckBoxE() { return checkBoxE; }

    /**
     * The F choice check box
     * @return the check box
     */
    CheckBox getCheckBoxF() { return checkBoxF; }

    /**
     * The G choice check box
     * @return the check box
     */
    CheckBox getCheckBoxG() { return checkBoxG; }

    /**
     * The index for the pagination's current page
     * @return the page index
     */
    int getContentPageIndex() {return pagination.getCurrentPageIndex();  }

    /**
     * The add page button is used to add a page after the current page
     * @return the button
     */
    Button getAddPageButton() { return addPageButton; }

    /**
     * The remove page button is used to remove the current page
     * @return the button
     */
    Button getRemovePageButton() { return removePageButton; }

    /**
     * The list of DecoratedRTA corresponding to the pagination's pages
     * @return the list
     */
    List<DecoratedRTA> getContentPageList() {
        return contentPageList;
    }
    /**
     * The list of DecoratedRTA corresponding to to pagination's pages
     * @param contentPageList
     */
    void setContentPageList(List<DecoratedRTA> contentPageList) { this.contentPageList = contentPageList; }

    /**
     * The prompt to appear on a first (blank) page of the pagination
     * @param prompt the string prompt
     */
    void setContentPrompt(String prompt) {    contentPrompt = prompt;   }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();  }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) { this.commentPrefHeight = commentPrefHeight; }

    /**
     * The preferred height of the pagination window
     * @return the height value
     */
    double getPaginationPrefHeight() {    return pagination.getPrefHeight(); }

    /**
     * The preferred height of the pagination window
     * @param paginationPrefHeight the height value
     */
    void setPaginationPrefHeight(double paginationPrefHeight) { this.paginationPrefHeight = paginationPrefHeight;   }

    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment;  }

    /**
     * The comment decoratedRTA
     * @param exerciseComment DecoratedRTA
     */
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment; }

    /**
     * The exercise prompt DecoratedRTA
     * @return the statement
     */
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }

    /**
     * The exercise prompt DecoratedRTA
     * @param exerciseStatement the statement
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement; }

    /**
     * The exercise prompt RTA node
     * @return the statement node
     */
    @Override
    public Node getExerciseStatementNode() {return exerciseStatement.getEditor();    }

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
     * The content just is the pagination (which includes the choice boxes on its first page)
     * @return the content node
     */
    @Override
    public Node getExerciseContentNode() {     return pagination;  }

    /**
     * The left control node for this exercise
     * @return the control node
     */
    @Override
    public Node getExerciseControl() { return exerciseControlNode; }

    /**
     * The right control node for this exercise
     * @return the null right control node
     */
    @Override
    public Node getRightControl() { return null; }

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


}


