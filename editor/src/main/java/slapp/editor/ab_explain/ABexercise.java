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

package slapp.editor.ab_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import slapp.editor.EditorMain;
import javafx.scene.text.Font;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import slapp.editor.DiskUtilities;
import slapp.editor.page_editor.PageContent;

import static slapp.editor.EditorMain.*;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

import javafx.animation.PauseTransition;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.IdentityHashMap;
import java.util.Map;


/**
 * AB exercise has binary choice and explain field
 */

public class ABexercise implements Exercise<ABmodel, ABview> {
    private MainWindow mainWindow;
    private ABmodel abModel;
    private ABview abView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private int lastPageNum = -1;
    private Font labelFont = new Font("Noto Serif Combo", 11);
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    private ABCheck abCheck;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> aCheckBoxListener;
    private ChangeListener<Boolean> bCheckBoxListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private EventHandler<ActionEvent> addPageHandler;
    private EventHandler<ActionEvent> removePageHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;

    // Pagination Listeners Identity Maps (prevents index-shifting CME/memory leaks when removing pages)
    private Map<Object, ChangeListener<Number>> pageHeightListenerMap = new IdentityHashMap<>();
    private Map<Object, ChangeListener<Boolean>> pageModifiedListenerMap = new IdentityHashMap<>();
    private Map<Object, ChangeListener<Boolean>> pageFocusListenerMap = new IdentityHashMap<>();

    /**
     * Construct AB edit exercise
     * @param model an AB edit model
     * @param mainWindow the main window
     */
    public ABexercise(ABmodel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.abModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.abView = new ABview(mainView);

        setEditView();
    }

    /*
     * Set up choices, statement, comment, and pagination view elements
     */
    private void setEditView() {
        abView.setContentPrompt(abModel.getContentPrompt());
        abView.setStatementPrefHeight(abModel.getStatementPrefHeight());
        abView.setCommentPrefHeight(abModel.getCommentPrefHeight());
        abView.setPaginationPrefHeight(abModel.getPaginationPrefHeight());

        abView.setPointsPossible(abModel.getPointsPossible());
        if (abModel.getPointsEarned() >= 0) abView.getPointsEarnedTextField().setText(Integer.toString(abModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            exerciseModified = true;
        };
        abView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //choices
        ABmodelExtra modelFields = abModel.getModelFields();
        abView.getLeaderLabel().setText(modelFields.getLeader());
        CheckBox aCheckBox = abView.getCheckBoxA();
        CheckBox bCheckBox = abView.getCheckBoxB();

        aCheckBox.setText(modelFields.getPromptA());
        aCheckBox.setSelected(modelFields.getValueA());
        aCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || abView == null) return;
            if (Boolean.TRUE.equals(nv)) {
                if (abView.getCheckBoxB() != null) abView.getCheckBoxB().setSelected(false);
            }
            exerciseModified = true;
        };
        aCheckBox.selectedProperty().addListener(aCheckBoxListener);

        bCheckBox.setText(modelFields.getPromptB());
        bCheckBox.setSelected(modelFields.getValueB());
        bCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || abView == null) return;
            if (Boolean.TRUE.equals(nv)) {
                if (abView.getCheckBoxA() != null) abView.getCheckBoxA().setSelected(false);
            }
            exerciseModified = true;
        };
        bCheckBox.selectedProperty().addListener(bCheckBoxListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(abModel.getExerciseStatement()).execute(new ActionEvent());

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
        abView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.getActionFactory().open(abModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};

        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || abModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            abModel.setCommentTextHeight(height);
        };
        commentEditor.fullHeightProperty().addListener(commentFullHeightListener);

        commentModifiedListener = (ob, ov, nv) -> {
            if (isTornDown) return;
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
        abView.setExerciseComment(commentDRTA);

        //pagination
        ArrayList<DecoratedRTA> contentList = new ArrayList<>();
        List<PageContent> pageContents = abModel.getPageContents();
        for (int i = 0; i < pageContents.size(); i++) {
            PageContent pageContent = pageContents.get(i);
            Document doc = pageContent.getPageDoc();
            DecoratedRTA drta = new DecoratedRTA();
            RichTextArea pageEditor = drta.getEditor();

            pageEditor.getActionFactory().open(doc).execute(new ActionEvent());
            pageEditor.getActionFactory().saveNow().execute(new ActionEvent());

            final int[] index = {i};
            final boolean[] pagePrimed = {false};

            ChangeListener<Number> pHeightListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null || abModel == null) return;
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

            ChangeListener<Boolean> pModListener = (ob, ov, nv) -> {
                if (isTornDown) return;
                if (nv) exerciseModified = true;
            };
            pageModifiedListenerMap.put(pageEditor, pModListener);
            pageEditor.modifiedProperty().addListener(pModListener);

            mainView.editorInFocus(drta, ControlType.AREA);

            ChangeListener<Boolean> pFocusListener = (o, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                try {
                    if (nv) {
                        mainView.editorInFocus(drta, ControlType.AREA);
                        double height = pageEditor.getFullHeight();
                        mainView.setRTATextHeight(height);
                    } else mainView.undoRTATextHeight();
                } catch (Exception e) {}
            };
            pageFocusListenerMap.put(pageEditor, pFocusListener);
            pageEditor.focusedProperty().addListener(pFocusListener);

            contentList.add(drta);
        }
        abView.setContentPageList(contentList);

        //cleanup
        abView.initializeViewDetails();
        abView.setRightControlBox();
        abCheck = new ABCheck(this);

        addPageHandler = e -> {
            if (isTornDown) return;
            addPageAction();
        };
        abView.getAddPageButton().setOnAction(addPageHandler);

        removePageHandler = e -> {
            if (isTornDown) return;
            removePageAction();
        };
        abView.getRemovePageButton().setOnAction(removePageHandler);

        Platform.runLater(() -> {
            if (isTornDown) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (abView != null && abView.getControlBox() != null) {
                    abView.getControlBox().getChildren().add(thumbPane);
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


       //     double scale = 85.0 / thumbPane.getLayoutBounds().getWidth();

            Rectangle clip = new Rectangle();
            clip.setWidth(800);
            clip.setHeight(400);
            thumbPane.setClip(clip);

            thumbPane.getTransforms().clear();
            thumbPane.getTransforms().add(new Scale(scale, scale));

            thumbPaneClickHandler = event -> {
                if (isTornDown || auxExerciseB == null) return;
                if (tStage == null || !tStage.isShowing()) {

                    Pane pane1 = new Pane(auxExerciseB.getAuxExerNode());


             //       VBox pane1 = new VBox(auxExerciseB.getAuxExerNode());
             //       pane1.setPadding(new Insets(10));
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

                 //   tStage.initOwner(EditorMain.mainStage);
                    tStage.initModality(Modality.NONE);
                    Stage mainStage = EditorMain.mainStage;
                    tStage.setX(mainStage.getX() + mainStage.getWidth()  - 500);
                    tStage.setY(mainStage.getY() + mainStage.getHeight()  - 300);

                    tStage.setWidth(pane1.getWidth() + RTA_WIDTH_BMP);
                    tStage.setHeight(Math.min(mainStage.getHeight() - 100, pane1.getHeight() + RTA_SCREEN_BMP + 25));

                    tStage.show();

                    Platform.runLater(() -> {
                        if (isTornDown || tStage == null) return;

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
        if (isTornDown || mainView == null || abModel == null || abView == null) return;

        int newPageIndex = abView.getContentPageIndex() + 1;


        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea editor = drta.getEditor();
        editor.getActionFactory().saveNow().execute(new ActionEvent());

        abView.addBlankContentPage(newPageIndex, drta);
        abModel.addBlankContentPage(newPageIndex);
        PageContent newPageContent = abModel.getPageContents().get(newPageIndex);

        ChangeListener<Number> pHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || abModel == null) return;
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            newPageContent.setTextHeight(height);
        };


        pageHeightListenerMap.put(editor, pHeightListener);
        editor.fullHeightProperty().addListener(pHeightListener);

        ChangeListener<Boolean> pModListener = (ob, ov, nv) -> {
            if (isTornDown) return;
            if (nv) exerciseModified = true;
        };
        pageModifiedListenerMap.put(editor, pModListener);
        editor.modifiedProperty().addListener(pModListener);

        mainView.editorInFocus(drta, ControlType.AREA);

        ChangeListener<Boolean> pFocusListener = (o, ov, nv) -> {
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
        if (isTornDown || mainView == null || abModel == null || abView == null) return;
        int currentPageIndex = abView.getContentPageIndex();

        if (abModel.getPageContents().size() <= 1) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Your response must include at least one page.");
        }
        else {
            if (currentPageIndex == 0) {
                EditorAlerts.showSimpleAlert("Cannot Remove", "Cannot remove top page with selection boxes.");
            }
            else {
                boolean okContinue = true;
                if (abView.getContentPageList().get(currentPageIndex).getEditor().isModified()) {
                    Alert confirm = EditorAlerts.confirmationAlert("Confirm Remove", "This page appears to have unsaved changes.  Continue to remove?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.get() != ButtonType.OK) okContinue = false;
                }
                if (okContinue) {
// SCORCHED EARTH RULE 9: The UI Deletion Fix (Clean up dynamic listeners on removed node)
                    try {
                        DecoratedRTA deletedDRTA = abView.getContentPageList().get(currentPageIndex);
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
                                    rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                                }
                                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(Duration.millis(1500));
                                pt.setOnFinished(ev -> {
                                    try { rta.setSkin(null); } catch (Exception ex) {}
                                });
                                pt.play();
                            } catch (Exception e) {}
                            severFromParent(rta);
                        }
                    } catch (Exception e) {}

                    abModel.getPageContents().remove(currentPageIndex);
                    abView.removeContentPage(currentPageIndex);
                    exerciseModified = true;
                }
            }
        }
    }

    @Override
    public ABmodel getExerciseModel() { return abModel; }

    @Override
    public ABview getExerciseView() { return abView; }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getABmodelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();

        ABexercise printExercise = this;
        ABmodel printModel = (ABmodel) originalModel;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();
        Label exerciseName = new Label(printModel.getExerciseName());
        exerciseName.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        Label pointsLabel = new Label("");
        if (printModel.getPointsPossible() > 0)
            pointsLabel.setText("   " + Integer.toString(printModel.getPointsEarned()) + "/" + Integer.toString(printModel.getPointsPossible()));

        HBox hbox = new HBox(exerciseName, spacer, printCheckNode(), pointsLabel);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setHgrow(spacer, Priority.ALWAYS);
        hbox.setPadding(new Insets(0,0,10,0));
        hbox.setMinWidth(nodeWidth);

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

        ABmodelExtra fields = printModel.getModelFields();
        Label leaderLabel = new Label(fields.getLeader());
        CheckBox boxA = new CheckBox(fields.getPromptA());
        boxA.setSelected(fields.getValueA());
        CheckBox boxB = new CheckBox(fields.getPromptB());
        boxB.setSelected(fields.getValueB());
        leaderLabel.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);

        HBox abBox = new HBox(20);
        abBox.setPadding(new Insets(10,10,10,0));
        abBox.getChildren().addAll(leaderLabel, boxA, boxB);
        nodeList.add(abBox);

        List<DecoratedRTA> pageList = printExercise.getExerciseView().getContentPageList();
        List<PageContent> pageContents = printModel.getPageContents();
        for (int i = 0; i < pageList.size(); i++) {
            DecoratedRTA drta = pageList.get(i);
            RichTextArea pageRTA = drta.getEditor();
            pageRTA.prefWidthProperty().bind(abView.localScalePageWidthProperty());
            pageRTA.contentAreaWidthProperty().bind(abView.localScalePageWidthProperty());
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
        commentRTA.getStylesheets().clear();
        commentRTA.getStylesheets().add("richTextAreaPrinter.css");


        nodeList.add(commentRTA);

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (abCheck != null && abCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + abView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));
            bigCheck.setFill(Color.ORCHID);
            message.setFill(Color.PURPLE);
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }

    @Override
    public ABexercise resetExercise() {
        RichTextArea commentRTA = abView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = abView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || abModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());



        ABmodel originalModel = (ABmodel) (abModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);

        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : abModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);

        ABCheckSetup setup = originalModel.getCheckSetup();
        if (abCheck != null) setup.setCheckTries(abCheck.getCheckTries());
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        ABexercise clearExercise = new ABexercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (abView == null) return false;
        if (abView.getExerciseComment() != null) {
            RichTextArea commentEditor = abView.getExerciseComment().getEditor();
            if (commentEditor != null && commentEditor.isModified()) exerciseModified = true;
        }

        List<DecoratedRTA> exerciseContent = abView.getContentPageList();
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
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}
    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}

    @Override
    public ExerciseModel getExerciseModelFromView() {
        return (ExerciseModel) getABmodelFromView();
    }

    private ABmodel getABmodelFromView() {
        RichTextArea commentRTA = abView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        List<DecoratedRTA> exerciseContent = abView.getContentPageList();
        List<PageContent> currentContents = abModel.getPageContents();
        List<PageContent> contentList = new ArrayList<>();

        for (int i = 0; i < exerciseContent.size(); i++) {
            DecoratedRTA drta = exerciseContent.get(i);
            RichTextArea editor = drta.getEditor();
            if (editor.isModified()) exerciseModified = true;
            editor.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = editor.getDocument();

            double liveHeight = editor.getFullHeight();
            double textHeight = liveHeight > 0 ? liveHeight : currentContents.get(i).getTextHeight();

            PageContent pageContent = new PageContent(doc, textHeight);
            contentList.add(pageContent);
        }

        String name = abModel.getExerciseName();
        String prompt = abModel.getContentPrompt();

        String leader = abModel.getModelFields().getLeader();
        String promptA = abModel.getModelFields().getPromptA();
        boolean valueA = abView.getCheckBoxA().isSelected();
        String promptB = abModel.getModelFields().getPromptB();
        boolean valueB = abView.getCheckBoxB().isSelected();
        ABmodelExtra extra = new ABmodelExtra(leader, promptA, valueA, promptB, valueB);

        boolean started = (abModel.isStarted() || exerciseModified);
        abModel.setStarted(started);

        ABCheckSetup checkSetup = abModel.getCheckSetup();
        if (abCheck != null) {
            checkSetup.setCheckTries(abCheck.getCheckTries());
            checkSetup.setCheckSuccess(abCheck.isCheckSuccess());
            checkSetup.setCheckMessage(abView.getCheckMessage());
        }
        abModel.setCheckSetup(checkSetup);

        double statementHeight = abView.getExerciseStatement().getEditor().getPrefHeight();
        Document statementDocument = abModel.getExerciseStatement();
        ABmodel newModel = new ABmodel(name, extra, started, prompt, statementHeight, statementDocument, commentDocument, contentList);
        newModel.setOriginalModel(abModel.getOriginalModel());
        newModel.setCommentPrefHeight(abView.getCommentPrefHeight());
        newModel.setPaginationPrefHeight(abView.getPaginationPrefHeight());

        double commentTextHeight = commentRTA.getFullHeight();
        newModel.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : abModel.getCommentTextHeight());

        newModel.setStatementTextHeight(abModel.getStatementTextHeight());
        newModel.setAuxExerName(abModel.getAuxExerName());

        newModel.setCheckSetup(checkSetup);

        newModel.setPointsPossible(abModel.getPointsPossible());
        TextField pointsField = abView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || abModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) newModel.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else newModel.setPointsEarned(-1);

        return newModel;
    }

    @Override
    public Node getAuxExerNode() {
        if (abModel == null || abView == null) return new Pane();
        List<PageContent> modelContents = abModel.getPageContents();
        VBox auxBox = new VBox(5);
        try { auxBox.getChildren().add(abView.getAbBox()); } catch (Exception e){}
        List<DecoratedRTA> contentPageList = abView.getContentPageList();
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

        // 1. Unbind global scaling properties & static listeners defensively
        try { if (abView != null && abView.localScalePageHeightProperty() != null) abView.localScalePageHeightProperty().unbind(); } catch (Exception e) {}
        try { if (abView != null && abView.localScalePageWidthProperty() != null) abView.localScalePageWidthProperty().unbind(); } catch (Exception e) {}
        try {
            if (mainView != null && mainView.getMainScene() != null && abView != null && abView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(abView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        // Remove Single Listeners
        try { if (abView != null && abView.getPointsEarnedTextField() != null && pointsEarnedListener != null) abView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch(Exception e){}
        try { if (abView != null && abView.getCheckBoxA() != null && aCheckBoxListener != null) abView.getCheckBoxA().selectedProperty().removeListener(aCheckBoxListener); } catch(Exception e){}
        try { if (abView != null && abView.getCheckBoxB() != null && bCheckBoxListener != null) abView.getCheckBoxB().selectedProperty().removeListener(bCheckBoxListener); } catch(Exception e){}
        try { if (abView != null && abView.getExerciseStatement() != null && abView.getExerciseStatement().getEditor() != null && statementFocusListener != null) abView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch(Exception e){}

        try {
            if (abView != null && abView.getExerciseComment() != null && abView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) abView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) abView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) abView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch(Exception e){}

        // Remove dynamic pagination listeners safely via tracking maps
        try {
            if (pageHeightListenerMap != null) {
                for (Map.Entry<Object, ChangeListener<Number>> entry : pageHeightListenerMap.entrySet()) {
                    try { ((RichTextArea) entry.getKey()).fullHeightProperty().removeListener(entry.getValue()); } catch(Exception e){}
                }
            }
        } catch(Exception e){}

        try {
            if (pageModifiedListenerMap != null) {
                for (Map.Entry<Object, ChangeListener<Boolean>> entry : pageModifiedListenerMap.entrySet()) {
                    try { ((RichTextArea) entry.getKey()).modifiedProperty().removeListener(entry.getValue()); } catch(Exception e){}
                }
            }
        } catch(Exception e){}

        try {
            if (pageFocusListenerMap != null) {
                for (Map.Entry<Object, ChangeListener<Boolean>> entry : pageFocusListenerMap.entrySet()) {
                    try { ((RichTextArea) entry.getKey()).focusedProperty().removeListener(entry.getValue()); } catch(Exception e){}
                }
            }
        } catch(Exception e){}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DEFERRED TO VIEW) <---
        try {
            if (abView != null) {
                final javafx.scene.control.Pagination pag = (abView.getExerciseContentNode() instanceof javafx.scene.control.Pagination)
                        ? (javafx.scene.control.Pagination) abView.getExerciseContentNode() : null;
                if (pag != null) {
                    pag.setPageFactory(null); // Stop generation immediately
                }
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (abView != null) {
                if (abView.getAbBox() != null) {
                    severFromParent(abView.getAbBox());
                    abView.getAbBox().getChildren().clear();
                }
                if (abView.getControlBox() != null) {
                    severFromParent(abView.getControlBox());
                    abView.getControlBox().getChildren().clear();
                }
                if (abView.getAddPageButton() != null) {
                    if (addPageHandler != null) abView.getAddPageButton().removeEventHandler(ActionEvent.ACTION, addPageHandler);
                    abView.getAddPageButton().setOnAction(null);
                }
                if (abView.getRemovePageButton() != null) {
                    if (removePageHandler != null) abView.getRemovePageButton().removeEventHandler(ActionEvent.ACTION, removePageHandler);
                    abView.getRemovePageButton().setOnAction(null);
                }
                if (abView.getCheckButton() != null) abView.getCheckButton().setOnAction(null);
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

        try { if (abCheck != null) abCheck.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (abView != null) abView.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { if (pageHeightListenerMap != null) pageHeightListenerMap.clear(); } catch(Exception e){}
        try { if (pageModifiedListenerMap != null) pageModifiedListenerMap.clear(); } catch(Exception e){}
        try { if (pageFocusListenerMap != null) pageFocusListenerMap.clear(); } catch(Exception e){}
        try { this.pageHeightListenerMap = null; } catch(Exception e){}
        try { this.pageModifiedListenerMap = null; } catch(Exception e){}
        try { this.pageFocusListenerMap = null; } catch(Exception e){}

        // Final severed ties
        try { this.abModel = null; } catch(Exception e){}
        try { this.mainWindow = null; } catch(Exception e){}
        try { this.mainView = null; } catch(Exception e){}
        try { this.abCheck = null; } catch(Exception e){}
        try { this.auxExerciseA = null; } catch(Exception e){}
        try { this.auxExerciseB = null; } catch(Exception e){}
        try { this.abView = null; } catch(Exception e){}
        try { this.tScene = null; } catch(Exception e){}
        try { this.tStage = null; } catch(Exception e){}
        try { this.thumbPane = null; } catch(Exception e){}
        try { this.labelFont = null; } catch(Exception e){}

        try { this.pointsEarnedListener = null; } catch(Exception e){}
        try { this.aCheckBoxListener = null; } catch(Exception e){}
        try { this.bCheckBoxListener = null; } catch(Exception e){}
        try { this.statementFocusListener = null; } catch(Exception e){}
        try { this.commentFullHeightListener = null; } catch(Exception e){}
        try { this.commentModifiedListener = null; } catch(Exception e){}
        try { this.commentFocusListener = null; } catch(Exception e){}
        try { this.addPageHandler = null; } catch(Exception e){}
        try { this.removePageHandler = null; } catch(Exception e){}
        try { this.thumbPaneClickHandler = null; } catch(Exception e){}
    }

    public MainWindow getMainWindow() { return mainWindow; }
    @Override
    public String getAuxExerName() {
        if (abModel == null) return "";
        return abModel.getAuxExerName();
    }
    @Override
    public void setAuxExerA(Exercise exer) { auxExerciseA = exer; }
    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}