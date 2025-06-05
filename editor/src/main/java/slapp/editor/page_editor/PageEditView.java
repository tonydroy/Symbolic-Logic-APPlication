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
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;
import java.util.ArrayList;
import java.util.List;

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


    /**
     * Construct page edit view
     * @param mainView the main view
     */
    public PageEditView(MainWindowView mainView) {
        this.mainView = mainView;
        this.pagination = new Pagination();

        pagination.setMaxPageIndicatorCount(5);
        pagination.setPageFactory((index) -> {
            DecoratedRTA drtaPage = contentPageList.get(index);
            RichTextArea rtaPage = drtaPage.getEditor();
            rtaPage.getStylesheets().add("slappTextArea.css");
            return rtaPage;
        });

        this.addPageButton = new Button("Insert Page");
        addPageButton.setTooltip(new Tooltip("Add after current page"));
        addPageButton.setPrefWidth(100.0);

        this.removePageButton = new Button("Remove Page");
        removePageButton.setTooltip(new Tooltip("Remove current page"));
        removePageButton.setPrefWidth(100.0);

        VBox controlBox = (VBox) exerciseControlNode;
        controlBox.setSpacing(30.0);
        controlBox.setPadding(new Insets(200,20,0,30));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        controlBox.getChildren().addAll(addPageButton, removePageButton);
    }

    /**
     * Initialize page management for statement, comment, and pagination
     */
    void initializeViewDetails() {

        //statement rta
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


       //comment rta
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
        double paginationInitialHeight = Math.round(paginationPrefHeight / mainView.getScalePageHeight() * 20.0 ) * 5;
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
        paginationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 5.0);
        paginationWidthSpinner.setPrefWidth(60);
        paginationWidthSpinner.setDisable(true);
        paginationWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));


        if (!contentPageList.isEmpty()) {
            contentPageList.get(0).getEditor().setPromptText(contentPrompt);
        }
        pagination.setPageCount(contentPageList.size());


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

        });

        setSizeSpinners();

    }

    private void setSizeSpinners() {

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
        contentPageList.remove(index);
        int newSize = contentPageList.size();
        pagination.setPageCount(newSize);
        if (newSize >= index) {
            pagination.setCurrentPageIndex(index);
        }
        else {
            pagination.setCurrentPageIndex(Math.max(0, index - 1));
        }
        pagination.requestFocus();
    }

    /**
     * The index for the pagination's current page
     * @return the page index
     */
    int getContentPageIndex() { return pagination.getCurrentPageIndex();  }

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
    List<DecoratedRTA> getContentPageList() { return contentPageList; }

    /**
     * The list of DecoratedRTA corresponding to to pagination's pages
     * @param contentPageList
     */
    void setContentPageList(List<DecoratedRTA> contentPageList) { this.contentPageList = contentPageList; }

    /**
     * The prompt to appear on a first (blank) page of the pagination
     * @param prompt the string prompt
     */
    void setContentPrompt(String prompt) {   contentPrompt = prompt;  }

    /**
     * The preferred height of the comment window
     * @param height the height value
     */
    void setCommentPrefHeight(double height) {this.commentPrefHeight = height; }

    /**
     * The preferred height of the pagination window
     * @param paginationPrefHeight the height value
     */
    void setPaginationPrefHeight(double paginationPrefHeight) { this.paginationPrefHeight = paginationPrefHeight;  }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight(); }

    /**
     * The preferred height of the pagination window
     * @return the height value
     */
    double getPaginationPrefHeight() { return pagination.getPrefHeight();    }

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
     * The exercise DecoratedRTA prompt
     * @return the statement
     */
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }

    /**
     * The exercise prompt DecoratedRTA
     * @param exerciseStatement the statement
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) {  this.exerciseStatement = exerciseStatement;  }

    /**
     * The exercise prompt RTA node
     * @return the statement node
     */
    @Override
    public Node getExerciseStatementNode() {  return exerciseStatement.getEditor();  }

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
     * The content just is the pagination
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
     * @return the control node
     */
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

