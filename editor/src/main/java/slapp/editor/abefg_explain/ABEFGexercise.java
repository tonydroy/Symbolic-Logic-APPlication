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
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.page_editor.PageContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static slapp.editor.EditorMain.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
 * AB/EFG exercise has binary choice (A/B), triple choice (E/F/G), and explain field
 */


public class ABEFGexercise implements Exercise<ABEFGmodel, ABEFGview> {
    private MainWindow mainWindow;
    private ABEFGmodel abefgModel;
    private ABEFGview abefgView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private int lastPageNum = -1;
    private Font labelFont = new Font("Noto Serif Combo", 11);
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    private ABEFGCheck abefgCheck;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> checkBoxAListener;
    private ChangeListener<Boolean> checkBoxBListener;
    private ChangeListener<Boolean> checkBoxEListener;
    private ChangeListener<Boolean> checkBoxFListener;
    private ChangeListener<Boolean> checkBoxGListener;
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
     * Construct AB/EFG edit exercise
     * @param model an AB/EFG edit model
     * @param mainWindow the main window
     */
    public ABEFGexercise(ABEFGmodel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.abefgModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.abefgView = new ABEFGview(mainView);

        setEditView();
    }

    /*
     * Set up choices, statement, comment, and pagination view elements
     */
    private void setEditView() {
        abefgView.setContentPrompt(abefgModel.getContentPrompt());
        abefgView.setStatementPrefHeight(abefgModel.getStatementPrefHeight());
        abefgView.setCommentPrefHeight(abefgModel.getCommentPrefHeight());
        abefgView.setPaginationPrefHeight(abefgModel.getPaginationPrefHeight());

        abefgView.setPointsPossible(abefgModel.getPointsPossible());
        if (abefgModel.getPointsEarned() >= 0) abefgView.getPointsEarnedTextField().setText(Integer.toString(abefgModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        abefgView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //choices
        ABEFGmodelExtra modelFields = abefgModel.getModelFields();
        abefgView.getLeaderLabelAB().setText(modelFields.getLeaderAB());
        abefgView.getLeaderLabelEFG().setText(modelFields.getLeaderEFG());

        CheckBox checkBoxA = abefgView.getCheckBoxA();
        CheckBox checkBoxB = abefgView.getCheckBoxB();
        CheckBox checkBoxE = abefgView.getCheckBoxE();
        CheckBox checkBoxF = abefgView.getCheckBoxF();
        CheckBox checkBoxG = abefgView.getCheckBoxG();

        checkBoxA.setText(modelFields.getPromptA());
        checkBoxA.setSelected(modelFields.getValueA());
        checkBoxAListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (Boolean.TRUE.equals(nv)) if (checkBoxB != null) checkBoxB.setSelected(false);
            exerciseModified = true;
        };
        checkBoxA.selectedProperty().addListener(checkBoxAListener);

        checkBoxB.setText(modelFields.getPromptB());
        checkBoxB.setSelected(modelFields.getValueB());
        checkBoxBListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (Boolean.TRUE.equals(nv)) if (checkBoxA != null) checkBoxA.setSelected(false);
            exerciseModified = true;
        };
        checkBoxB.selectedProperty().addListener(checkBoxBListener);

        checkBoxE.setText(modelFields.getPromptE());
        checkBoxE.setSelected(modelFields.getValueE());
        checkBoxEListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (Boolean.TRUE.equals(nv)) {
                if (checkBoxF != null) checkBoxF.setSelected(false);
                if (checkBoxG != null) checkBoxG.setSelected(false);
            }
            exerciseModified = true;
        };
        checkBoxE.selectedProperty().addListener(checkBoxEListener);

        checkBoxF.setText(modelFields.getPromptF());
        checkBoxF.setSelected(modelFields.getValueF());
        checkBoxFListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (Boolean.TRUE.equals(nv)) {
                if (checkBoxE != null) checkBoxE.setSelected(false);
                if (checkBoxG != null) checkBoxG.setSelected(false);
            }
            exerciseModified = true;
        };
        checkBoxF.selectedProperty().addListener(checkBoxFListener);

        checkBoxG.setText(modelFields.getPromptG());
        checkBoxG.setSelected(modelFields.getValueG());
        checkBoxGListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (Boolean.TRUE.equals(nv)) {
                if (checkBoxE != null) checkBoxE.setSelected(false);
                if (checkBoxF != null) checkBoxF.setSelected(false);
            }
            exerciseModified = true;
        };
        checkBoxG.selectedProperty().addListener(checkBoxGListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(abefgModel.getExerciseStatement()).execute(new ActionEvent());

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
        abefgView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(abefgModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};

        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || abefgModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            abefgModel.setCommentTextHeight(height);
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
        abefgView.setExerciseComment(commentDRTA);

        //pagination
        List<DecoratedRTA> contentList = new ArrayList<>();
        List<PageContent> pageContents = abefgModel.getPageContents();

        for (int i = 0; i < pageContents.size(); i++) {
            PageContent pageContent = pageContents.get(i);
            Document doc = pageContent.getPageDoc();
            DecoratedRTA drta = new DecoratedRTA();
            RichTextArea pageEditor = drta.getEditor();

            pageEditor.getActionFactory().open(doc).execute(new ActionEvent());
            pageEditor.getActionFactory().saveNow().execute(new ActionEvent());


            final boolean[] pagePrimed = {false};

            ChangeListener<Number> pHeightListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null || abefgModel == null) return;
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
                if (isTornDown || mainView == null) return;
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
        abefgView.setContentPageList(contentList);

        //cleanup
        abefgView.initializeViewDetails();
        abefgView.setRightControlBox();
        abefgCheck = new ABEFGCheck(this);

        addPageHandler = e -> {
            if (isTornDown || mainView == null) return;
            addPageAction();
        };
        abefgView.getAddPageButton().setOnAction(addPageHandler);

        removePageHandler = e -> {
            if (isTornDown || mainView == null) return;
            removePageAction();
        };
        abefgView.getRemovePageButton().setOnAction(removePageHandler);

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (abefgView != null && abefgView.getControlBox() != null) {
                    abefgView.getControlBox().getChildren().add(thumbPane);
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

             //       tStage.initOwner(EditorMain.mainStage);
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
        if (isTornDown || mainView == null || abefgModel == null || abefgView == null) return;

        int newPageIndex = abefgView.getContentPageIndex() + 1;

        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea editor = drta.getEditor();
        editor.getActionFactory().saveNow().execute(new ActionEvent());

        abefgView.addBlankContentPage(newPageIndex, drta);
        abefgModel.addBlankContentPage(newPageIndex);
        PageContent newPageContent = abefgModel.getPageContents().get(newPageIndex);

        ChangeListener<Number> pHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || abefgModel == null) return;
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            newPageContent.setTextHeight(height);
        };


        pageHeightListenerMap.put(editor, pHeightListener);
        editor.fullHeightProperty().addListener(pHeightListener);

        ChangeListener<Boolean> pModListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
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
        if (isTornDown || mainView == null || abefgModel == null || abefgView == null) return;
        int currentPageIndex = abefgView.getContentPageIndex();

        if (abefgModel.getPageContents().size() <= 1) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Your response must include at least one page.");
        }
        else {
            if (currentPageIndex == 0) {
                EditorAlerts.showSimpleAlert("Cannot Remove", "Cannot remove top page with selection boxes.");
            }
            else {
                boolean okContinue = true;
                if (abefgView.getContentPageList().get(currentPageIndex).getEditor().isModified()) {
                    Alert confirm = EditorAlerts.confirmationAlert("Confirm Remove", "This page appears to have unsaved changes.  Continue to remove?");
                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.get() != ButtonType.OK) okContinue = false;
                }
                if (okContinue) {
                    // SCORCHED EARTH RULE 9: The UI Deletion Fix (Clean up dynamic listeners on removed node)
                    try {
                        DecoratedRTA deletedDRTA = abefgView.getContentPageList().get(currentPageIndex);
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

                    abefgView.removeContentPage(currentPageIndex);
                    abefgModel.getPageContents().remove(currentPageIndex);
                    exerciseModified = true;
                }
            }
        }
    }

    @Override
    public ABEFGmodel getExerciseModel() { return abefgModel; }

    @Override
    public ABEFGview getExerciseView() { return abefgView; }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getABEFGmodelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        ABEFGmodel printModel = (ABEFGmodel) originalModel;
        ABEFGexercise printExercise = this;
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
        statementRTA.prefWidthProperty().unbind();
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

        ABEFGmodelExtra fields = printModel.getModelFields();
        Label leaderLabelAB = new Label(fields.getLeaderAB());
        CheckBox boxA = new CheckBox(fields.getPromptA());
        boxA.setSelected(fields.getValueA());
        CheckBox boxB = new CheckBox(fields.getPromptB());
        boxB.setSelected(fields.getValueB());
        leaderLabelAB.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);
        HBox abBox = new HBox(20);
        abBox.getChildren().addAll(leaderLabelAB, boxA, boxB);

        Label leaderLabelEFG = new Label(fields.getLeaderEFG());
        CheckBox boxE = new CheckBox(fields.getPromptE());
        boxE.setSelected(fields.getValueE());
        CheckBox boxF = new CheckBox(fields.getPromptF());
        boxF.setSelected(fields.getValueF());
        CheckBox boxG = new CheckBox(fields.getPromptG());
        boxG.setSelected(fields.getValueG());
        leaderLabelEFG.setFont(labelFont); boxE.setFont(labelFont); boxF.setFont(labelFont); boxG.setFont(labelFont);
        HBox efgBox = new HBox(20);
        efgBox.getChildren().addAll(leaderLabelEFG, boxE, boxF, boxG);

        VBox checksBox = new VBox(10, abBox, efgBox);
        checksBox.setPadding(new Insets(10,0,20,0));
        nodeList.add(checksBox);

        List<DecoratedRTA> pageList = printExercise.getExerciseView().getContentPageList();
        List<PageContent> pageContents = printModel.getPageContents();
        for (int i = 0; i < pageList.size(); i++) {
            DecoratedRTA drta = pageList.get(i);
            RichTextArea pageRTA = drta.getEditor();
            pageRTA.prefWidthProperty().bind(abefgView.localScalePageWidthProperty());
            pageRTA.contentAreaWidthProperty().bind(abefgView.localScalePageWidthProperty());
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

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (abefgCheck != null && abefgCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + abefgView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));
            bigCheck.setFill(Color.ORCHID);
            message.setFill(Color.PURPLE);
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }

    @Override
    public ABEFGexercise resetExercise() {
        RichTextArea commentRTA = abefgView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();


        int pointsEarned = -1;
        TextField pointsField = abefgView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || abefgModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        ABEFGmodel originalModel = (ABEFGmodel) (abefgModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);

        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : abefgModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);

        ABEFGCheckSetup setup = originalModel.getCheckSetup();
        if (abefgCheck != null) setup.setCheckTries(abefgCheck.getCheckTries());
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        ABEFGexercise clearExercise = new ABEFGexercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (abefgView == null) return false;
        if (abefgView.getExerciseComment() != null) {
            RichTextArea commentEditor = abefgView.getExerciseComment().getEditor();
            if (commentEditor != null && commentEditor.isModified()) exerciseModified = true;
        }

        List<DecoratedRTA> exerciseContent = abefgView.getContentPageList();
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
        return (ExerciseModel) getABEFGmodelFromView();
    }

    private ABEFGmodel getABEFGmodelFromView() {
        RichTextArea commentRTA = abefgView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        List<DecoratedRTA> exerciseContent = abefgView.getContentPageList();
        List<PageContent> currentContents = abefgModel.getPageContents();
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

        String name = abefgModel.getExerciseName();
        String prompt = abefgModel.getContentPrompt();

        ABEFGmodelExtra fields = abefgModel.getModelFields();
        String leaderAB = fields.getLeaderAB();
        String promptA = fields.getPromptA();
        boolean valueA = abefgView.getCheckBoxA().isSelected();
        String promptB = fields.getPromptB();
        boolean valueB = abefgView.getCheckBoxB().isSelected();

        String leaderEFG = fields.getLeaderEFG();
        String promptE = fields.getPromptE();
        boolean valueE = abefgView.getCheckBoxE().isSelected();
        String promptF = fields.getPromptF();
        boolean valueF = abefgView.getCheckBoxF().isSelected();
        String promptG = fields.getPromptG();
        boolean valueG = abefgView.getCheckBoxG().isSelected();

        ABEFGmodelExtra extra = new ABEFGmodelExtra(leaderAB, promptA, valueA, promptB, valueB, leaderEFG, promptE, valueE, promptF, valueF, promptG, valueG);

        boolean started = (abefgModel.isStarted() || exerciseModified);
        abefgModel.setStarted(started);

        ABEFGCheckSetup checkSetup = abefgModel.getCheckSetup();
        if (abefgCheck != null) {
            checkSetup.setCheckTries(abefgCheck.getCheckTries());
            checkSetup.setCheckSuccess(abefgCheck.isCheckSuccess());
            checkSetup.setCheckMessage(abefgView.getCheckMessage());
        }
        abefgModel.setCheckSetup(checkSetup);

        Document statementDocument = abefgModel.getExerciseStatement();
        double statementHeight = abefgView.getExerciseStatement().getEditor().getPrefHeight();

        ABEFGmodel newModel = new ABEFGmodel(name, extra, started, prompt, statementHeight, statementDocument, commentDocument, contentList);
        newModel.setOriginalModel(abefgModel.getOriginalModel());
        newModel.setCommentPrefHeight(abefgView.getCommentPrefHeight());
        newModel.setPaginationPrefHeight(abefgView.getPaginationPrefHeight());

        double commentTextHeight = commentRTA.getFullHeight();
        newModel.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : abefgModel.getCommentTextHeight());

        newModel.setStatementTextHeight(abefgModel.getStatementTextHeight());
        newModel.setPointsPossible(abefgModel.getPointsPossible());
        newModel.setAuxExerName(abefgModel.getAuxExerName());

        newModel.setCheckSetup(checkSetup);

        TextField pointsField = abefgView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || abefgModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) newModel.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else newModel.setPointsEarned(-1);

        return newModel;
    }

    @Override
    public Node getAuxExerNode() {
        if (abefgModel == null || abefgView == null) return new Pane();
        List<PageContent> modelContents = abefgModel.getPageContents();
        VBox auxBox = new VBox(5);
        try { auxBox.getChildren().add(abefgView.getChecksBox()); } catch(Exception e){}
        List<DecoratedRTA> contentPageList = abefgView.getContentPageList();
        for (int i = 0; i < contentPageList.size(); i++) {
            DecoratedRTA drta = contentPageList.get(i);
            RichTextArea rta = drta.getEditor();

            rta.setMaxHeight(Math.max(100, modelContents.get(i).getTextHeight() + RTA_SCREEN_BMP));
            rta.setMinHeight(Math.max(100, modelContents.get(i).getTextHeight() + RTA_SCREEN_BMP));

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
                mainView.textFieldInFocus(); // Replaces top toolbars safely
            }
        } catch (Exception e) {}

        // 1. Unbind global scaling properties & static listeners defensively
        try { if (abefgView != null && abefgView.localScalePageHeightProperty() != null) abefgView.localScalePageHeightProperty().unbind(); } catch(Exception e){}
        try { if (abefgView != null && abefgView.localScalePageWidthProperty() != null) abefgView.localScalePageWidthProperty().unbind(); } catch(Exception e){}
        try {
            if (mainView != null && mainView.getMainScene() != null && abefgView != null && abefgView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(abefgView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        // Remove Single Listeners
        try { if (abefgView != null && abefgView.getPointsEarnedTextField() != null && pointsEarnedListener != null) abefgView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch(Exception e){}
        try { if (abefgView != null && abefgView.getCheckBoxA() != null && checkBoxAListener != null) abefgView.getCheckBoxA().selectedProperty().removeListener(checkBoxAListener); } catch(Exception e){}
        try { if (abefgView != null && abefgView.getCheckBoxB() != null && checkBoxBListener != null) abefgView.getCheckBoxB().selectedProperty().removeListener(checkBoxBListener); } catch(Exception e){}
        try { if (abefgView != null && abefgView.getCheckBoxE() != null && checkBoxEListener != null) abefgView.getCheckBoxE().selectedProperty().removeListener(checkBoxEListener); } catch(Exception e){}
        try { if (abefgView != null && abefgView.getCheckBoxF() != null && checkBoxFListener != null) abefgView.getCheckBoxF().selectedProperty().removeListener(checkBoxFListener); } catch(Exception e){}
        try { if (abefgView != null && abefgView.getCheckBoxG() != null && checkBoxGListener != null) abefgView.getCheckBoxG().selectedProperty().removeListener(checkBoxGListener); } catch(Exception e){}
        try { if (abefgView != null && abefgView.getExerciseStatement() != null && abefgView.getExerciseStatement().getEditor() != null && statementFocusListener != null) abefgView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch(Exception e){}

        try {
            if (abefgView != null && abefgView.getExerciseComment() != null && abefgView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) abefgView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) abefgView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) abefgView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
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
            if (abefgView != null) {
                final javafx.scene.control.Pagination pag = (abefgView.getExerciseContentNode() instanceof javafx.scene.control.Pagination)
                        ? (javafx.scene.control.Pagination) abefgView.getExerciseContentNode() : null;
                if (pag != null) {
                    pag.setPageFactory(null); // Stop generation immediately
                }
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (abefgView != null) {
                if (abefgView.getChecksBox() != null) {
                    severFromParent(abefgView.getChecksBox());
                    abefgView.getChecksBox().getChildren().clear();
                    abefgView.getChecksBox().setOnMouseClicked(null);
                }
                if (abefgView.getControlBox() != null) {
                    severFromParent(abefgView.getControlBox());
                    abefgView.getControlBox().getChildren().clear();
                }

                if (abefgView.getAddPageButton() != null) {
                    if (addPageHandler != null) abefgView.getAddPageButton().removeEventHandler(ActionEvent.ACTION, addPageHandler);
                    abefgView.getAddPageButton().setOnAction(null);
                }
                if (abefgView.getRemovePageButton() != null) {
                    if (removePageHandler != null) abefgView.getRemovePageButton().removeEventHandler(ActionEvent.ACTION, removePageHandler);
                    abefgView.getRemovePageButton().setOnAction(null);
                }
                if (abefgView.getCheckButton() != null) abefgView.getCheckButton().setOnAction(null);
                if (abefgView.getStaticHelpButton() != null) abefgView.getStaticHelpButton().setOnAction(null);
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

        try { if (abefgCheck != null) abefgCheck.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (abefgView != null) abefgView.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { if (pageHeightListenerMap != null) pageHeightListenerMap.clear(); } catch(Exception e){}
        try { if (pageModifiedListenerMap != null) pageModifiedListenerMap.clear(); } catch(Exception e){}
        try { if (pageFocusListenerMap != null) pageFocusListenerMap.clear(); } catch(Exception e){}
        try { this.pageHeightListenerMap = null; } catch(Exception e){}
        try { this.pageModifiedListenerMap = null; } catch(Exception e){}
        try { this.pageFocusListenerMap = null; } catch(Exception e){}

        // Final severed ties
        try { this.abefgModel = null; } catch(Exception e){}
        try { this.mainWindow = null; } catch(Exception e){}
        try { this.mainView = null; } catch(Exception e){}
        try { this.abefgCheck = null; } catch(Exception e){}
        try { this.auxExerciseA = null; } catch(Exception e){}
        try { this.auxExerciseB = null; } catch(Exception e){}
        try { this.abefgView = null; } catch(Exception e){}
        try { this.tScene = null; } catch(Exception e){}
        try { this.tStage = null; } catch(Exception e){}
        try { this.thumbPane = null; } catch(Exception e){}
        try { this.labelFont = null; } catch(Exception e){}

        try { this.pointsEarnedListener = null; } catch(Exception e){}
        try { this.checkBoxAListener = null; } catch(Exception e){}
        try { this.checkBoxBListener = null; } catch(Exception e){}
        try { this.checkBoxEListener = null; } catch(Exception e){}
        try { this.checkBoxFListener = null; } catch(Exception e){}
        try { this.checkBoxGListener = null; } catch(Exception e){}
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
        if (abefgModel == null) return "";
        return abefgModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) { auxExerciseA = exer; }
    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}