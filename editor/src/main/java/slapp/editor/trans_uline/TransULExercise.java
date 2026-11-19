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

package slapp.editor.trans_uline;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.EditorMain.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class TransULExercise implements Exercise<TransULModel, TransULView>, ULBoxContainer {

    private MainWindow mainWindow;
    private MainWindowView mainView;
    private TransULView transULView;
    private TransULModel transULModel;
    private TransULCheck transULCheck;
    private boolean exerciseModified = false;
    private UndoRedoList<TransULModel> undoRedoList;
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    private Stage tStage;
    private Pane thumbPane = new Pane();
    private Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<String> pointsListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentHeightListener;
    private ChangeListener<Boolean> commentModListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Boolean> aCheckListener;
    private ChangeListener<Boolean> bCheckListener;
    private EventHandler<KeyEvent> ulineFilter;
    private EventHandler<KeyEvent> ulineHandler;
    private ChangeListener<Boolean> ulineFocusListener;
    private EventHandler<KeyEvent> mainOpHandler;
    private ChangeListener<Boolean> mainOpFocusListener;
    private EventHandler<ActionEvent> undoHandler;
    private EventHandler<ActionEvent> redoHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;


    public TransULExercise(TransULModel transULModel, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.transULModel = transULModel;

        if (this.transULModel.getOriginalModel() == null) {
            TransULModel deepCopy = (TransULModel) SerializationUtils.clone(transULModel);
            this.transULModel.setOriginalModel(deepCopy);
        }
        this.mainView = mainWindow.getMainView();
        this.transULView = new TransULView(mainView, this);
        setTransULView();
        transULCheck = new TransULCheck(this);
        undoRedoList = new UndoRedoList<>(50);
        TransULModel deepModelCopy = (TransULModel) SerializationUtils.clone(transULModel);
        undoRedoList.push(deepModelCopy);
        updateUndoRedoButtons();
        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = false;
        });
    }

    private void setTransULView() {
        transULView.setStatementPrefHeight(transULModel.getStatementPrefHeight());
        transULView.setCommentPrefHeight(transULModel.getCommentPrefHeight());
        transULView.setPointsPossible(transULModel.getPointsPossible());
        if (transULModel.getPointsEarned() >= 0) transULView.getPointsEarnedTextField().setText(Integer.toString(transULModel.getPointsEarned()));

        pointsListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        transULView.getPointsEarnedTextField().textProperty().addListener(pointsListener);

        //Statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(transULModel.getExerciseStatement()).execute(new ActionEvent());

        statementFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                    mainView.setRTATextHeight(statementEditor.getFullHeight());
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        statementEditor.focusedProperty().addListener(statementFocusListener);
        transULView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(transULModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || transULModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            transULModel.setCommentTextHeight(height);
        };
        commentEditor.fullHeightProperty().addListener(commentHeightListener);

        commentModListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        commentEditor.modifiedProperty().addListener(commentModListener);

        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(commentDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(commentEditor.getFullHeight());
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        commentEditor.focusedProperty().addListener(commentFocusListener);
        transULView.setExerciseComment(commentDRTA);

        //choices
        CheckBox aCheckBox = transULView.getaCheckBox();
        CheckBox bCheckBox = transULView.getbCheckBox();
        aCheckBox.setSelected(transULModel.isaSelected());

        aCheckListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (Boolean.TRUE.equals(nv)) bCheckBox.setSelected(false);
            exerciseModified = true;
        };
        aCheckBox.selectedProperty().addListener(aCheckListener);

        bCheckBox.setSelected(transULModel.isbSelected());
        bCheckListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (Boolean.TRUE.equals(nv)) aCheckBox.setSelected(false);
            exerciseModified = true;
        };
        bCheckBox.selectedProperty().addListener(bCheckListener);

        //uline
        SimpleULineBox ulineBox = transULView.getUlineBox();
        ulineBox.setupBox(transULModel.getMainModel());

        RichTextArea ulineRTA = ulineBox.getFormulaBox().getRTA();

        ulineFilter = event -> {
            if (isTornDown || mainView == null) return;
            KeyCode code = event.getCode();
            if (code != KeyCode.LEFT && code != KeyCode.RIGHT)
                event.consume();
        };
        ulineRTA.addEventFilter(KeyEvent.ANY, ulineFilter);

        mainView.editorInFocus(ulineBox.getFormulaBox().getDRTA(), ControlType.AREA);

        ulineHandler = e -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        ulineRTA.addEventHandler(KeyEvent.KEY_RELEASED, ulineHandler);

        ulineFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(ulineBox.getFormulaBox().getDRTA(), ControlType.FIELD);
                }
            } catch (Exception e) {}
        };
        ulineRTA.focusedProperty().addListener(ulineFocusListener);

        //mainOperator
        BoxedDRTA mainOpBDRTA = transULView.getMainOperatorBDRTA();
        RichTextArea mainOpRTA = mainOpBDRTA.getRTA();

        String mainOperator = transULModel.getMainOperator();
        mainOpRTA.getActionFactory().open(new Document(mainOperator)).execute(new ActionEvent());
        mainOpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(mainOpBDRTA.getDRTA(), ControlType.AREA);

        mainOpHandler = e -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        mainOpRTA.addEventHandler(KeyEvent.KEY_RELEASED, mainOpHandler);

        mainOpFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(mainOpBDRTA.getDRTA(), ControlType.FIELD);
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        mainOpRTA.focusedProperty().addListener(mainOpFocusListener);

        //cleanup actions
        undoHandler = e -> {
            if (isTornDown || mainView == null) return;
            undoAction();
        };
        transULView.getUndoButton().setOnAction(undoHandler);

        redoHandler = e -> {
            if (isTornDown || mainView == null) return;
            redoAction();
        };
        transULView.getRedoButton().setOnAction(redoHandler);

        // Execute View setup BEFORE populating control box!
        transULView.initializeViewDetails();
        transULView.setRightControlBox();
        populateControlBox();
    }

    private void populateControlBox() {
        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (transULView != null && transULView.getControlBox() != null) {
                    transULView.getControlBox().getChildren().add(thumbPane);
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

            //        tStage.initOwner(EditorMain.mainStage);
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


    private void undoAction() {
        if (isTornDown || undoRedoList == null || transULView == null) return;
        TransULModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            transULModel = (TransULModel) SerializationUtils.clone(undoElement);
            SimpleULineBox ulineBox = transULView.getUlineBox();
            if (ulineBox != null) {
                ulineBox.setTreeObjects(transULModel.getMainModel());
                ulineBox.resetLines();
            }
            updateUndoRedoButtons();
        }
    }

    private void redoAction() {
        if (isTornDown || undoRedoList == null || transULView == null) return;
        TransULModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            transULModel = (TransULModel) SerializationUtils.clone(redoElement);
            SimpleULineBox ulineBox = transULView.getUlineBox();
            if (ulineBox != null) {
                ulineBox.setTreeObjects(transULModel.getMainModel());
                ulineBox.resetLines();
            }
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        if (isTornDown || transULView == null || undoRedoList == null) return;
        if (transULView.getUndoButton() != null) transULView.getUndoButton().setDisable(!undoRedoList.canUndo());
        if (transULView.getRedoButton() != null) transULView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    public void pushUndoRedo() {
        if (isTornDown || undoRedoList == null || transULModel == null) return;
        TransULModel model = getTransULModelFromView();
        TransULModel deepCopy = (TransULModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
        exerciseModified = true;
    }

    @Override
    public TransULModel getExerciseModel() {  return transULModel;    }

    @Override
    public TransULView getExerciseView() {  return transULView;    }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getTransULModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        TransULModel printModel = transULModel;
        TransULExercise printExercise = this;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
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

        //statement node
        RichTextArea statementRTA = printExercise.getExerciseView().getExerciseStatement().getEditor();
        statementRTA.prefHeightProperty().unbind();
        statementRTA.minWidthProperty().unbind();
        statementRTA.maxWidthProperty().unbind();
        statementRTA.setPrefHeight(printModel.getStatementTextHeight() + RTA_PRINT_BMP);
        statementRTA.setMaxWidth(nodeWidth);
        statementRTA.setMinWidth(nodeWidth);

        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        //content
        Label leaderLabel = new Label("The following sentence is ");
        CheckBox boxA = new CheckBox("simple");
        boxA.setSelected(transULModel.isaSelected());
        CheckBox boxB = new CheckBox("compound");
        boxB.setSelected(transULModel.isbSelected());
        Font labelFont2 = new Font("Noto Serif Combo", 11);
        leaderLabel.setFont(labelFont2); boxA.setFont(labelFont2); boxB.setFont(labelFont2);
        HBox abBox = new HBox(20, leaderLabel, boxA, boxB);
        abBox.setStyle("-fx-border-width: 0; -fx-border-color: transparent;");
        abBox.setPadding(new Insets(20, 0, 20, 0));
        nodeList.add(abBox);


        SimpleULineBox ulineBox = new SimpleULineBox(printExercise);
        ulineBox.setupBox(transULModel.getMainModel());
        ulineBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
        ulineBox.getFormulaBox().getRTA().setMinWidth(transULModel.getUlBoxPrintWidth());


        ulineBox.setPadding(new Insets(20,0,0,0));

        nodeList.add(ulineBox);


        RichTextArea moRTA = new RichTextArea(EditorMain.mainStage);
        moRTA.getActionFactory().open(new Document(transULModel.getMainOperator())).execute(new ActionEvent());
        moRTA.getActionFactory().saveNow().execute(new ActionEvent());
        moRTA.setStyle("-fx-border-width: 0; -fx-border-color: transparent");
        moRTA.setMaxWidth(nodeWidth - 200);
        moRTA.setMaxHeight(27);
        moRTA.setMinHeight(27);
        Label moLabel = new Label("Main Operator:");
        moLabel.setFont(labelFont2);
        HBox moHBox = new HBox(10, moLabel, moRTA);
        moHBox.setMaxWidth(nodeWidth);

        moHBox.setStyle("-fx-border-width: 0; -fx-border-color: transparent;");
        moHBox.setPadding(new Insets(10,0,20,0));

        nodeList.add(moHBox);



        Separator contentSeparator = new Separator(Orientation.HORIZONTAL);
        contentSeparator.setStyle("-fx-stroke-dash-array:0.1 5.0");
        contentSeparator.setPrefWidth(100);
        HBox contentSepBox = new HBox(contentSeparator);
        contentSepBox.setMinWidth(nodeWidth);
        contentSepBox.setAlignment(Pos.CENTER);
        nodeList.add(contentSepBox);

        //comment node
        RichTextArea commentRTA = printExercise.getExerciseView().getExerciseComment().getEditor();
        commentRTA.prefHeightProperty().unbind();
        commentRTA.minWidthProperty().unbind();
        commentRTA.maxWidthProperty().unbind();
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
        commentRTA.setMinWidth(nodeWidth);

        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");


        nodeList.add(commentRTA);

        return nodeList;


    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (transULCheck != null && transULCheck.isCheckSuccess() && transULView != null) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + transULView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            bigCheck.setFill(Color.LAWNGREEN);
            message.setFill(Color.GREEN);
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }



    @Override
    public Exercise<TransULModel, TransULView> resetExercise() {
        RichTextArea commentRTA = transULView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = transULView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || transULModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        TransULModel originalModel = (TransULModel) (transULModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : transULModel.getCommentTextHeight());


        originalModel.setPointsEarned(pointsEarned);

        ULcheckSetup setup = originalModel.getCheckSetup();
        if (transULCheck != null) {
            setup.setCheckTries(transULCheck.getCheckTries());
        }
        setup.setCheckSuccess(false);
        setup.setChoiceSuccess(false);
        originalModel.setCheckSetup(setup);

        TransULExercise clearExercise = new TransULExercise(originalModel, mainWindow);
        return clearExercise;
    }



    @Override
    public boolean isExerciseModified() {
        if (transULView != null) {
            if (transULView.getExerciseComment() != null && transULView.getExerciseComment().getEditor() != null && transULView.getExerciseComment().getEditor().isModified()) {
                exerciseModified = true;
            }
            if (transULView.getMainOperatorBDRTA() != null && transULView.getMainOperatorBDRTA().getRTA() != null && transULView.getMainOperatorBDRTA().getRTA().isModified()) {
                exerciseModified = true;
            }
        }
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) { exerciseModified = modified; }

    @Override
    public Node getFFViewNode() {return null;}

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    @Override
    public Spinner getFFHeightSpinner() {return null;}

    @Override
    public Spinner getFFWidthSpinner() {return null;}

    @Override
    public ExerciseModel<TransULModel> getExerciseModelFromView() {
        return (ExerciseModel) getTransULModelFromView();
    }

    private TransULModel getTransULModelFromView() {
        TransULModel model = new TransULModel();
        if (transULModel == null || transULView == null) return model;

        model.setExerciseName(transULModel.getExerciseName());

        model.setAuxExerName(transULModel.getAuxExerName());
        model.setOriginalModel(transULModel.getOriginalModel());
        model.setStarted(transULModel.isStarted() || exerciseModified);
        model.setExerciseStatement(transULModel.getExerciseStatement());
        model.setStatementPrefHeight(transULView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight((transULModel.getStatementTextHeight()));

        RichTextArea commentRTA = transULView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        model.setCommentPrefHeight(transULView.getCommentPrefHeight());
        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : transULModel.getCommentTextHeight());


        model.setaSelected(transULView.getaCheckBox().isSelected());
        model.setbSelected(transULView.getbCheckBox().isSelected());

        model.setPointsPossible(transULModel.getPointsPossible());
        TextField pointsField = transULView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || transULModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);


        ULcheckSetup checkSetup = transULModel.getCheckSetup();
        if (transULCheck != null) {
            checkSetup.setCheckSuccess(transULCheck.isCheckSuccess());
            checkSetup.setCheckTries(transULCheck.getCheckTries());
            checkSetup.setCheckMessage(transULView.getCheckMessage());
        }
        model.setCheckSetup(checkSetup);

        if (transULView.getUlineBox() != null) {
            model.setMainModel(transULView.getUlineBox().getTreeObjects());
        }
        model.setUlBoxPrintWidth(transULModel.getUlBoxPrintWidth());


        if (transULView.getMainOperatorBDRTA() != null) {
            RichTextArea mainOpRTA = transULView.getMainOperatorBDRTA().getRTA();
            mainOpRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setMainOperator(mainOpRTA.getDocument().getText());
        }

        return model;
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

    @Override
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Safe Global State & Null-Poisoning Prevention
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Safely clear toolbars
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try { if (transULView != null && transULView.localScalePageHeightProperty() != null) transULView.localScalePageHeightProperty().unbind(); } catch (Exception e) {}
        try { if (transULView != null && transULView.localScalePageWidthProperty() != null) transULView.localScalePageWidthProperty().unbind(); } catch (Exception e) {}

        try { if (transULView != null && transULView.getPointsEarnedTextField() != null && pointsListener != null) transULView.getPointsEarnedTextField().textProperty().removeListener(pointsListener); } catch (Exception e) {}
        try { if (transULView != null && transULView.getaCheckBox() != null && aCheckListener != null) transULView.getaCheckBox().selectedProperty().removeListener(aCheckListener); } catch (Exception e) {}
        try { if (transULView != null && transULView.getbCheckBox() != null && bCheckListener != null) transULView.getbCheckBox().selectedProperty().removeListener(bCheckListener); } catch (Exception e) {}

        try { if (transULView != null && transULView.getUndoButton() != null && undoHandler != null) transULView.getUndoButton().setOnAction(null); } catch (Exception e) {}
        try { if (transULView != null && transULView.getRedoButton() != null && redoHandler != null) transULView.getRedoButton().setOnAction(null); } catch (Exception e) {}

        try { if (transULView != null && transULView.getExerciseStatement() != null && statementFocusListener != null) transULView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (transULView != null && transULView.getExerciseComment() != null) {
                if (commentHeightListener != null) transULView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentHeightListener);
                if (commentModListener != null) transULView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModListener);
                if (commentFocusListener != null) transULView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (transULView != null && transULView.getUlineBox() != null && transULView.getUlineBox().getFormulaBox() != null) {
                RichTextArea ulEditor = transULView.getUlineBox().getFormulaBox().getRTA();
                if (ulEditor != null) {
                    if (ulineFilter != null) ulEditor.removeEventFilter(KeyEvent.ANY, ulineFilter);
                    if (ulineHandler != null) ulEditor.removeEventHandler(KeyEvent.KEY_RELEASED, ulineHandler);
                    if (ulineFocusListener != null) ulEditor.focusedProperty().removeListener(ulineFocusListener);
                }
            }
        } catch (Exception e) {}

        try {
            if (transULView != null && transULView.getMainOperatorBDRTA() != null) {
                RichTextArea moEditor = transULView.getMainOperatorBDRTA().getRTA();
                if (moEditor != null) {
                    if (mainOpHandler != null) moEditor.removeEventHandler(KeyEvent.KEY_RELEASED, mainOpHandler);
                    if (mainOpFocusListener != null) moEditor.focusedProperty().removeListener(mainOpFocusListener);
                }
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 6: Sever Shared Nodes
        try {
            if (thumbPane != null) {
                if (thumbPaneClickHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler);
                thumbPane.setOnMouseClicked(null);
                thumbPane.getChildren().clear();
                severFromParent(thumbPane);
            }
        } catch (Exception e) {}

        try { clearStandingPopups(); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER <---
        // (Completely removed from Controller per Rule 1. Document injection and skin removal deferred entirely to View.)
        // Note: There were no synchronous unbindings in this block, so it was deleted safely.

        // SCORCHED EARTH RULE 1: Cascade Cleanup
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (transULView != null) transULView.cleanup(); } catch (Exception e) {}
        try { if (transULCheck != null) transULCheck.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.pointsListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentHeightListener = null; } catch (Exception e) {}
        try { this.commentModListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.aCheckListener = null; } catch (Exception e) {}
        try { this.bCheckListener = null; } catch (Exception e) {}
        try { this.ulineFilter = null; } catch (Exception e) {}
        try { this.ulineHandler = null; } catch (Exception e) {}
        try { this.ulineFocusListener = null; } catch (Exception e) {}
        try { this.mainOpHandler = null; } catch (Exception e) {}
        try { this.mainOpFocusListener = null; } catch (Exception e) {}
        try { this.undoHandler = null; } catch (Exception e) {}
        try { this.redoHandler = null; } catch (Exception e) {}
        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}

        try { this.transULModel = null; } catch (Exception e) {}
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.transULCheck = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.transULView = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive List Teardown (only clear pointers)
        try { this.undoRedoList = null; } catch (Exception e) {}
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    @Override

    public String getAuxExerName() {
        if (transULModel == null ) return "";
        return getExerciseModel().getAuxExerName();
    }


    @Override
    public Node getAuxExerNode() {
        if (transULView == null) return new Pane();
        return transULView.getContentBox();
    }

    @Override
    public void setAuxExerA(Exercise exercise) {
        this.auxExerciseA = exercise;
    }

    @Override
    public void setAuxExerB(Exercise exercise) {
        this.auxExerciseB = exercise;
    }


    @Override
    public void clearStandingPopups() {
        try {
            if (transULView != null && transULView.getStaticHelpStage() != null) transULView.getStaticHelpStage().close();
            if (tStage != null) tStage.close();
        } catch (Exception e) {}
    }

    @Override
    public void editorInFocus(DecoratedRTA decoratedRTA, ControlType control) {
        if (mainView != null) {
            mainView.editorInFocus(decoratedRTA, control);
        }
    }

    @Override
    public DoubleProperty getScalePageWidth() {
        if (mainView != null) {
            return mainView.scalePageWidthProperty();
        }
        return null;
    }
}