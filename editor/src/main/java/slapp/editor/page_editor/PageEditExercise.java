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
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import slapp.editor.EditorMain;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import slapp.editor.DiskUtilities;
import static slapp.editor.EditorMain.*;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

import javafx.scene.control.*;
import javafx.scene.layout.*;


/**
 * Basic page edit exsericise
 */


public class PageEditExercise implements Exercise<PageEditModel, PageEditView> {
    private MainWindow mainWindow;
    private PageEditModel editModel;
    private PageEditView editView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private int lastPageNum = -1;
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());
    private PECheck peCheck;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private javafx.beans.value.ChangeListener<String> pointsEarnedListener;
    private javafx.beans.value.ChangeListener<Boolean> statementFocusListener;
    private javafx.beans.value.ChangeListener<Number> commentFullHeightListener;
    private javafx.beans.value.ChangeListener<Boolean> commentModifiedListener;
    private javafx.beans.value.ChangeListener<Boolean> commentFocusListener;
    private javafx.event.EventHandler<javafx.event.ActionEvent> addPageHandler;
    private javafx.event.EventHandler<javafx.event.ActionEvent> removePageHandler;
    private javafx.event.EventHandler<javafx.scene.input.MouseEvent> thumbPaneClickHandler;

    // Pagination Listeners Identity Maps (prevents index-shifting CME/memory leaks when removing pages)
    private java.util.Map<Object, javafx.beans.value.ChangeListener<Number>> pageHeightListenerMap = new java.util.IdentityHashMap<>();
    private java.util.Map<Object, javafx.beans.value.ChangeListener<Boolean>> pageModifiedListenerMap = new java.util.IdentityHashMap<>();
    private java.util.Map<Object, javafx.beans.value.ChangeListener<Boolean>> pageFocusListenerMap = new java.util.IdentityHashMap<>();

    /**
     * Construct page edit exercise
     * @param model a page edit model
     * @param mainWindow the main window
     */
    public PageEditExercise(PageEditModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.editModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.editView = new PageEditView(mainView);
        setEditView();
    }

    /*
     * Set up statement, comment and pagination view elements.
     */
    private void setEditView() {
        editView.setContentPrompt(editModel.getContentPrompt());
        editView.setStatementPrefHeight(editModel.getStatementPrefHeight());
        editView.setCommentPrefHeight(editModel.getCommentPrefHeight());
        editView.setPaginationPrefHeight(editModel.getPaginationPrefHeight());

        editView.setPointsPossible(editModel.getPointsPossible());
        if (editModel.getPointsEarned() >= 0) editView.getPointsEarnedTextField().setText(Integer.toString(editModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        editView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(editModel.getExerciseStatement()).execute(new javafx.event.ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);

        statementFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                    mainView.setRTATextHeight(statementEditor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        statementEditor.focusedProperty().addListener(statementFocusListener);
        editView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.getActionFactory().open(editModel.getExerciseComment()).execute(new javafx.event.ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new javafx.event.ActionEvent());

        final boolean[] commentPrimed = {false};

        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || editModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            editModel.setCommentTextHeight(height);
        };
        commentEditor.fullHeightProperty().addListener(commentFullHeightListener);

        commentModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        commentEditor.modifiedProperty().addListener(commentModifiedListener);

        mainView.editorInFocus(commentDRTA, ControlType.AREA);

        commentFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(commentDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(commentEditor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        commentEditor.focusedProperty().addListener(commentFocusListener);
        editView.setExerciseComment(commentDRTA);

        //pagination
        List<DecoratedRTA> contentList = new ArrayList<>();
        List<PageContent> pageContents = editModel.getPageContents();
        for (int i = 0; i < pageContents.size(); i++) {
            PageContent pageContent = pageContents.get(i);
            Document doc = pageContent.getPageDoc();
            DecoratedRTA drta = new DecoratedRTA();
            RichTextArea pageEditor = drta.getEditor();

            pageEditor.getActionFactory().open(doc).execute(new javafx.event.ActionEvent());
            pageEditor.getActionFactory().saveNow().execute(new javafx.event.ActionEvent());

            final boolean[] pagePrimed = {false};

            javafx.beans.value.ChangeListener<Number> pHeightListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null || editModel == null) return;
                if (!pagePrimed[0]) {
                    pagePrimed[0] = true;
                    if (!pageEditor.isFocused()) return;
                }
                double height = (double) nv;
                if (pageEditor.isFocused()) mainView.setRTATextHeight(height);
                pageContent.setTextHeight(height);
            };

            pageHeightListenerMap.put(pageEditor, pHeightListener);
            pageEditor.fullHeightProperty().addListener(pHeightListener);

            javafx.beans.value.ChangeListener<Boolean> pModifiedListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                if (nv) exerciseModified = true;
            };
            pageModifiedListenerMap.put(pageEditor, pModifiedListener);
            pageEditor.modifiedProperty().addListener(pModifiedListener);

            mainView.editorInFocus(drta, ControlType.AREA);

            javafx.beans.value.ChangeListener<Boolean> pFocusListener = (o, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                try {
                    if (nv) {
                        mainView.editorInFocus(drta, ControlType.AREA);
                        mainView.setRTATextHeight(pageEditor.getFullHeight());
                    } else mainView.undoRTATextHeight();
                } catch (Exception e) {}
            };
            pageFocusListenerMap.put(pageEditor, pFocusListener);
            pageEditor.focusedProperty().addListener(pFocusListener);

            contentList.add(drta);
        }
        editView.setContentPageList(contentList);

        //cleanup
        editView.initializeViewDetails();
        editView.setRightControlBox();
        peCheck = new PECheck(this);

        addPageHandler = e -> {
            if (isTornDown || mainView == null) return;
            addPageAction();
        };
        editView.getAddPageButton().setOnAction(addPageHandler);

        removePageHandler = e -> {
            if (isTornDown || mainView == null) return;
            removePageAction();
        };
        editView.getRemovePageButton().setOnAction(removePageHandler);

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (editView != null && editView.getControlBox() != null) {
                    editView.getControlBox().getChildren().add(thumbPane);
                }
            }
        });
    }

    private Pane auxImage() {
        Pane thumbPane = new Pane();
        if (auxExerciseA != null && auxExerciseA.getAuxExerNode() != null)  {
            thumbPane.setStyle("-fx-background-color: FAFAFA;");
            Node auxExerNode = auxExerciseA.getAuxExerNode();
            auxExerNode.setMouseTransparent(true);
            auxExerNode.setFocusTraversable(false);
            thumbPane.getChildren().add(auxExerNode);

            Group thumbRoot = new Group();
            Scene scene = new Scene(thumbRoot);
            thumbRoot.getChildren().add(thumbPane);
            thumbRoot.applyCss();
            thumbRoot.layout();
            double thumbWidth = thumbPane.getLayoutBounds().getWidth();
            double scale = thumbWidth > 0 ? 85.0 / thumbWidth : .16;

            Rectangle clip = new Rectangle();
            clip.setWidth(800);
            clip.setHeight(400);
            thumbPane.setClip(clip);

            thumbPane.getTransforms().clear();
            thumbPane.getTransforms().add(new Scale(scale, scale));

            thumbPaneClickHandler = event -> {
                if (isTornDown || mainView == null || auxExerciseB == null) return;
                if (tStage == null || !tStage.isShowing()) {
                    Pane pane1 = new Pane(auxExerciseB.getAuxExerNode());
                    pane1.setDisable(true);

                    pane1.setId("aux-pane");
                    String css = "#aux-pane { -fx-opacity: 0.85; } \n" +
                            "#aux-pane * { -fx-opacity: 1.0; } \n" +
                            "#aux-pane .caret { -fx-opacity: 0.0; }";
                    String cssUri = "data:text/css;base64," +
                            java.util.Base64.getEncoder().encodeToString(css.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    pane1.getStylesheets().add(cssUri);

                    Pane spacerPane = new Pane();
                    spacerPane.prefHeightProperty().bind(pane1.heightProperty());
                    spacerPane.prefWidthProperty().bind(pane1.widthProperty());
                    Group group = new Group(spacerPane);
                    AnchorPane comboPane = new AnchorPane(group, pane1);
                    ScrollPane tPane = new ScrollPane(comboPane);
                    tPane.setFitToHeight(false);
                    tPane.setFitToWidth(false);

                    tScene.setRoot(tPane);
                    tPane.applyCss();
                    tPane.layout();

                    tStage = new Stage();
                    tStage.setScene(tScene);
                    tStage.setTitle(((ExerciseModel) auxExerciseB.getExerciseModel()).getExerciseName());
                    tStage.initStyle(StageStyle.UTILITY);

                    //tStage.initOwner(EditorMain.mainStage);
                    tStage.initModality(Modality.NONE);

                    Stage mainStage = EditorMain.mainStage;
                    tStage.setX(mainStage.getX() + mainStage.getWidth()  - 500);
                    tStage.setY(mainStage.getY() + mainStage.getHeight()  - 300);

                    tStage.setWidth(pane1.getWidth() + RTA_WIDTH_BMP);
                    tStage.setHeight(Math.min(mainStage.getHeight() - 100, pane1.getHeight() + RTA_SCREEN_BMP + 25));

                    tStage.show();

                    Platform.runLater(() -> {
                        if (isTornDown || mainView == null || tStage == null) return;


                        double prefX = mainStage.getX() + mainStage.getWidth() - tStage.getWidth() + 50;
                        double prefY = mainStage.getY() + mainStage.getHeight() - tStage.getHeight()+ 50;
                        Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();

                        tStage.setX(Math.min(prefX, bounds.getMaxX() - tStage.getWidth()));
                        tStage.setY(Math.min(prefY, bounds.getMaxY() - tStage.getHeight()));

                    });

                } else {
                    tStage.close();
                }
            };
            thumbPane.setOnMouseClicked(thumbPaneClickHandler);
        }
        return thumbPane;
    }

    public void clearStandingPopups() {
        if (tStage != null) tStage.close();
    }

    /*
     * Add page after the current page
     */
    private void addPageAction() {
        if (isTornDown || mainView == null || editModel == null || editView == null) return;

        int newPageIndex = editView.getContentPageIndex() + 1;



        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea editor = drta.getEditor();
        editor.getActionFactory().saveNow().execute(new javafx.event.ActionEvent());

        editView.addBlankContentPage(newPageIndex, drta);
        editModel.addBlankContentPage(newPageIndex);
        PageContent newPageContent = editModel.getPageContents().get(newPageIndex);

        javafx.beans.value.ChangeListener<Number> pHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || editModel == null) return;
            mainView.setRTATextHeight((double) nv);
            newPageContent.setTextHeight((double) nv);
        };


        pageHeightListenerMap.put(editor, pHeightListener);
        editor.fullHeightProperty().addListener(pHeightListener);

        javafx.beans.value.ChangeListener<Boolean> pModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        pageModifiedListenerMap.put(editor, pModifiedListener);
        editor.modifiedProperty().addListener(pModifiedListener);

        mainView.editorInFocus(drta, ControlType.AREA);

        javafx.beans.value.ChangeListener<Boolean> pFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(drta, ControlType.AREA);
                    mainView.setRTATextHeight(editor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        pageFocusListenerMap.put(editor, pFocusListener);
        editor.focusedProperty().addListener(pFocusListener);


    }

    /*
     * Remove the current page
     */
    private void removePageAction() {
        if (isTornDown || mainView == null || editModel == null || editView == null) return;
        int currentPageIndex = editView.getContentPageIndex();

        if (editModel.getPageContents().size() <= 1) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Your response must include at least one page.");
        }
        else  {
            boolean okContinue = true;
            if (editView.getContentPageList().get(currentPageIndex).getEditor().isModified()) {
                Alert confirm = EditorAlerts.confirmationAlert("Confirm Remove", "This page appears to have unsaved changes.  Continue to remove?");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() != ButtonType.OK) okContinue = false;
            }
            if (okContinue) {
                // SCORCHED EARTH RULE 9: The UI Deletion Fix (Clean up dynamic listeners on removed node)
                try {
                    DecoratedRTA deletedDRTA = editView.getContentPageList().get(currentPageIndex);
                    if (deletedDRTA != null && deletedDRTA.getEditor() != null) {
                        final RichTextArea rta = deletedDRTA.getEditor();
                        try { rta.fullHeightProperty().removeListener(pageHeightListenerMap.remove(rta)); } catch (Exception e) {}
                        try { rta.modifiedProperty().removeListener(pageModifiedListenerMap.remove(rta)); } catch (Exception e) {}
                        try { rta.focusedProperty().removeListener(pageFocusListenerMap.remove(rta)); } catch (Exception e) {}
                        try { rta.prefWidthProperty().unbind(); } catch (Exception e) {}
                        try { rta.contentAreaWidthProperty().unbind(); } catch (Exception e) {}
                        try {
                            rta.setPromptText("");
                            rta.setFocusTraversable(false);
                            if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                                rta.getActionFactory().open(new Document(" ")).execute(new javafx.event.ActionEvent());
                            }
                            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                            pt.setOnFinished(ev -> {
                                try { rta.setSkin(null); } catch (Exception ex) {}
                            });
                            pt.play();
                        } catch (Exception e) {}
                        severFromParent(rta);
                    }
                } catch (Exception e) {}

                editModel.getPageContents().remove(currentPageIndex);
                editView.removeContentPage(currentPageIndex);
                exerciseModified = true;
            }

        }
    }

    @Override
    public PageEditModel getExerciseModel() { return editModel; }

    @Override
    public PageEditView getExerciseView() { return editView; }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getPageEditModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        PageEditModel printModel = (PageEditModel) originalModel;
        PageEditExercise printExercise = this;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        Label exerciseName = new Label(printModel.getExerciseName());
        exerciseName.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        Label pointsLabel = new Label("");
        if (printModel.getPointsPossible() > 0)
            pointsLabel.setText("   " + Integer.toString(printModel.getPointsEarned()) + "/" + Integer.toString(printModel.getPointsPossible()));

        HBox hbox = new HBox(exerciseName, spacer, pointsLabel);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setHgrow(spacer, Priority.ALWAYS);
        hbox.setMinWidth(nodeWidth);
        hbox.setPadding(new Insets(0,0,10,0));

        Group headerRoot = new Group();
        Scene headerScene = new Scene(headerRoot);
        headerRoot.getChildren().add(hbox);
        headerRoot.applyCss();
        headerRoot.layout();
        double boxHeight = hbox.getHeight();
        hbox.setPrefHeight(boxHeight);
        nodeList.add(hbox);
        Separator headerSeparator = new Separator(Orientation.HORIZONTAL);
        headerSeparator.setPrefWidth(nodeWidth);
        nodeList.add(headerSeparator);

        RichTextArea statementRTA = printExercise.getExerciseView().getExerciseStatement().getEditor();
        statementRTA.prefHeightProperty().unbind();
        statementRTA.minWidthProperty().unbind();
        statementRTA.setPrefHeight(printModel.getStatementTextHeight() + RTA_PRINT_BMP);
        statementRTA.setMaxWidth(nodeWidth);
        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        List<DecoratedRTA> pageList = printExercise.getExerciseView().getContentPageList();
        List<PageContent> pageContents = printModel.getPageContents();
        for (int i = 0; i < pageList.size(); i++) {
            DecoratedRTA drta = pageList.get(i);
            RichTextArea pageRTA = drta.getEditor();
            pageRTA.prefWidthProperty().bind(editView.localScalePageWidthProperty());
            pageRTA.contentAreaWidthProperty().bind(editView.localScalePageWidthProperty());
            pageRTA.getStylesheets().clear(); pageRTA.getStylesheets().add("richTextAreaPrinter.css");
            double responseHeight = Math.max(40, pageContents.get(i).getTextHeight() + RTA_PRINT_BMP);
            pageRTA.setMinHeight(responseHeight);
            pageRTA.setMaxHeight(responseHeight);
            nodeList.add(pageRTA);
        }

        Separator contentSeparator = new Separator(Orientation.HORIZONTAL);
        contentSeparator.setStyle("-fx-stroke-dash-array:0.1 5.0");
        contentSeparator.setPrefWidth(100);
        HBox contentSepBox = new HBox(contentSeparator);
        contentSepBox.setMinWidth(nodeWidth);
        contentSepBox.setAlignment(Pos.CENTER);
        nodeList.add(contentSepBox);

        RichTextArea commentRTA = printExercise.getExerciseView().getExerciseComment().getEditor();
        commentRTA.prefHeightProperty().unbind();
        commentRTA.minWidthProperty().unbind();
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");


        nodeList.add(commentRTA);

        return nodeList;
    }

    @Override
    public PageEditExercise resetExercise() {
        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new javafx.event.ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = editView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || editModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        PageEditModel originalModel = (PageEditModel) (editModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);

        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : editModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);
        PageEditExercise clearExercise = new PageEditExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (editView == null) return false;
        if (editView.getExerciseComment() != null) {
            RichTextArea commentEditor = editView.getExerciseComment().getEditor();
            if (commentEditor != null && commentEditor.isModified()) exerciseModified = true;
        }

        List<DecoratedRTA> exerciseContent = editView.getContentPageList();
        if (exerciseContent != null) {
            for (DecoratedRTA drta : exerciseContent) {
                if (drta != null && drta.getEditor() != null) {
                    RichTextArea editor = drta.getEditor();
                    if (editor.isModified()) exerciseModified = true;
                }
            }
        }
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) { this.exerciseModified = modified; }
    @Override
    public Node getFFViewNode() {return null;}
    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    @Override
    public ExerciseModel getExerciseModelFromView() {
        return (ExerciseModel) getPageEditModelFromView();
    }

    private PageEditModel getPageEditModelFromView() {
        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new javafx.event.ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        List<DecoratedRTA> exerciseContent = editView.getContentPageList();
        List<PageContent> currentContents = editModel.getPageContents();
        List<PageContent> contentList = new ArrayList<>();

        for (int i = 0; i < exerciseContent.size(); i++) {
            DecoratedRTA drta = exerciseContent.get(i);
            RichTextArea editor = drta.getEditor();
            if (editor.isModified()) exerciseModified = true;
            editor.getActionFactory().saveNow().execute(new javafx.event.ActionEvent());
            Document doc = editor.getDocument();

            double liveHeight = editor.getFullHeight();
            double textHeight = liveHeight > 0 ? liveHeight : currentContents.get(i).getTextHeight();

            PageContent pageContent = new PageContent(doc, textHeight);
            contentList.add(pageContent);
        }

        String name = editModel.getExerciseName();
        String prompt = editModel.getContentPrompt();

        boolean started = (editModel.isStarted() || exerciseModified);
        editModel.setStarted(started);

        Document statementDocument = editModel.getExerciseStatement();
        double statementHeight = editView.getExerciseStatement().getEditor().getPrefHeight();

        PageEditModel newModel = new PageEditModel(name, started, prompt, statementHeight, statementDocument, commentDocument, contentList);
        newModel.setOriginalModel(editModel.getOriginalModel());
        newModel.setCommentPrefHeight(editView.getCommentPrefHeight());
        newModel.setPaginationPrefHeight(editView.getPaginationPrefHeight());

        double commentTextHeight = commentRTA.getFullHeight();
        newModel.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : editModel.getCommentTextHeight());

        newModel.setStatementTextHeight(editModel.getStatementTextHeight());
        newModel.setAuxExerName(editModel.getAuxExerName());
        newModel.setCheckSetup(editModel.getCheckSetup());

        newModel.setPointsPossible(editModel.getPointsPossible());
        TextField pointsField = editView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || editModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) newModel.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else newModel.setPointsEarned(-1);

        return newModel;
    }

    @Override
    public Node getAuxExerNode() {
        if (editModel == null || editView == null) return new Pane();
        List<PageContent> modelContents = editModel.getPageContents();
        VBox auxBox = new VBox(5);
        List<DecoratedRTA> contentPageList = editView.getContentPageList();
        for (int i = 0; i < contentPageList.size(); i++) {
            DecoratedRTA drta = contentPageList.get(i);
            RichTextArea rta = drta.getEditor();

            rta.setMaxHeight(Math.max(200, modelContents.get(i).getTextHeight() + RTA_SCREEN_BMP));
            rta.setMinHeight(Math.max(200, modelContents.get(i).getTextHeight() + RTA_SCREEN_BMP));

            rta.setMinWidth(PrintUtilities.getPageWidth() / mainWindow.getBaseScale() + RTA_WIDTH_BMP);
            rta.setMaxWidth(PrintUtilities.getPageWidth() / mainWindow.getBaseScale() + RTA_WIDTH_BMP);
            rta.setPadding(new Insets(10));
            auxBox.getChildren().add(rta);
        }
        return auxBox;
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

    // SCORCHED EARTH RULE 5: Rule of Isolation applied via dozens of try-catch blocks
    @Override
    public void cleanup() {
        // KILL SWITCH: Engaged! Prevent any focus listeners from doing UI operations mid-teardown
        isTornDown = true;

        // SCORCHED EARTH RULE 2: No Global Null-Poisoning
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Replaces top toolbars with a dummy, releasing the old RTAs safely
            }
        } catch (Exception e) {}

        // 1. Unbind global scaling properties defensively
        try { if (editView != null && editView.localScalePageHeightProperty() != null) editView.localScalePageHeightProperty().unbind(); } catch (Exception e) {}
        try { if (editView != null && editView.localScalePageWidthProperty() != null) editView.localScalePageWidthProperty().unbind(); } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && editView != null && editView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(editView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        // Remove single property listeners
        try { if (editView != null && editView.getPointsEarnedTextField() != null && pointsEarnedListener != null) editView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}
        try { if (editView != null && editView.getExerciseStatement() != null && editView.getExerciseStatement().getEditor() != null && statementFocusListener != null) editView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (editView != null && editView.getExerciseComment() != null && editView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) editView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) editView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) editView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        // Remove dynamic pagination listeners safely via tracking maps
        try {
            if (pageHeightListenerMap != null) {
                for (java.util.Map.Entry<Object, javafx.beans.value.ChangeListener<Number>> entry : pageHeightListenerMap.entrySet()) {
                    try { ((RichTextArea) entry.getKey()).fullHeightProperty().removeListener(entry.getValue()); } catch(Exception e){}
                }
            }
        } catch(Exception e){}

        try {
            if (pageModifiedListenerMap != null) {
                for (java.util.Map.Entry<Object, javafx.beans.value.ChangeListener<Boolean>> entry : pageModifiedListenerMap.entrySet()) {
                    try { ((RichTextArea) entry.getKey()).modifiedProperty().removeListener(entry.getValue()); } catch(Exception e){}
                }
            }
        } catch(Exception e){}

        try {
            if (pageFocusListenerMap != null) {
                for (java.util.Map.Entry<Object, javafx.beans.value.ChangeListener<Boolean>> entry : pageFocusListenerMap.entrySet()) {
                    try { ((RichTextArea) entry.getKey()).focusedProperty().removeListener(entry.getValue()); } catch(Exception e){}
                }
            }
        } catch(Exception e){}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DEFERRED TO VIEW) <---
        try {
            if (editView != null) {
                final javafx.scene.control.Pagination pag = (editView.getExerciseContentNode() instanceof javafx.scene.control.Pagination)
                        ? (javafx.scene.control.Pagination) editView.getExerciseContentNode() : null;
                if (pag != null) {
                    pag.setPageFactory(null); // Stop pagination generation immediately
                }
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (editView != null) {
                if (editView.getControlBox() != null) {
                    severFromParent(editView.getControlBox());
                    editView.getControlBox().getChildren().clear();
                }
                if (editView.getAddPageButton() != null) {
                    if (addPageHandler != null) editView.getAddPageButton().removeEventHandler(javafx.event.ActionEvent.ACTION, addPageHandler);
                    editView.getAddPageButton().setOnAction(null);
                }
                if (editView.getRemovePageButton() != null) {
                    if (removePageHandler != null) editView.getRemovePageButton().removeEventHandler(javafx.event.ActionEvent.ACTION, removePageHandler);
                    editView.getRemovePageButton().setOnAction(null);
                }
                if (editView.getStaticHelpButton() != null) editView.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (thumbPane != null) {
                if (thumbPaneClickHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler);
                thumbPane.setOnMouseClicked(null);
                thumbPane.getChildren().clear();
                severFromParent(thumbPane);
            }
        } catch (Exception e) {}

        try { clearStandingPopups(); } catch (Exception e) {}


        try { if (peCheck != null) peCheck.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (editView != null) editView.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { if (pageHeightListenerMap != null) pageHeightListenerMap.clear(); } catch(Exception e){}
        try { if (pageModifiedListenerMap != null) pageModifiedListenerMap.clear(); } catch(Exception e){}
        try { if (pageFocusListenerMap != null) pageFocusListenerMap.clear(); } catch(Exception e){}
        try { this.pageHeightListenerMap = null; } catch(Exception e){}
        try { this.pageModifiedListenerMap = null; } catch(Exception e){}
        try { this.pageFocusListenerMap = null; } catch(Exception e){}

        // Final severed ties
        try { this.editModel = null; } catch(Exception e){}
        try { this.mainWindow = null; } catch(Exception e){}
        try { this.mainView = null; } catch(Exception e){}
        try { this.peCheck = null; } catch(Exception e){}
        try { this.auxExerciseA = null; } catch(Exception e){}
        try { this.auxExerciseB = null; } catch(Exception e){}
        try { this.editView = null; } catch(Exception e){}
        try { this.tScene = null; } catch(Exception e){}
        try { this.tStage = null; } catch(Exception e){}
        try { this.thumbPane = null; } catch(Exception e){}

        try { this.pointsEarnedListener = null; } catch(Exception e){}
        try { this.statementFocusListener = null; } catch(Exception e){}
        try { this.commentFullHeightListener = null; } catch(Exception e){}
        try { this.commentModifiedListener = null; } catch(Exception e){}
        try { this.commentFocusListener = null; } catch(Exception e){}
        try { this.addPageHandler = null; } catch(Exception e){}
        try { this.removePageHandler = null; } catch(Exception e){}
        try { this.thumbPaneClickHandler = null; } catch(Exception e){}
    }

    @Override
    public String getAuxExerName() {
        if (editModel == null) return "";
        return editModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) { auxExerciseA = exer; }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}