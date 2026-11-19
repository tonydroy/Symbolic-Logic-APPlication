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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
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
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.derivation.theorems.*;
import slapp.editor.main_window.*;
import java.util.ArrayList;
import java.util.List;
import static slapp.editor.EditorMain.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.IdentityHashMap;
import java.util.Map;
import slapp.editor.main_window.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;


/**
 * Controller for the derivation exercise.
 */


public class DerivationExercise implements Exercise<DerivationModel, DerivationView> {
    private MainWindow mainWindow;
    private DerivationModel derivationModel;
    private DerivationView derivationView;
    private MainWindowView mainView;
    private DerivationCheck derivationCheck;
    private DerivationHelp derivationHelp;
    private boolean exerciseModified = false;
    private Font labelFont = new Font("Noto Serif Combo", 11);
    private boolean editJustification;

    // MISSING REFERENCE FIX: Track wrapper globally to clean it up!
    private DecoratedRTA lastJustificationDRTA;
    private RichTextArea lastJustificationRTA;
    private int lastJustificationRow;

    private UndoRedoList<DerivationModel> undoRedoList = new UndoRedoList<>(20);
    private List<Theorem> theorems = new ArrayList<>();
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 2: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Number> splitPaneDividerListener;
    private EventHandler<ActionEvent> insertLineHandler;
    private EventHandler<ActionEvent> deleteLineHandler;
    private EventHandler<ActionEvent> indentHandler;
    private EventHandler<ActionEvent> outdentHandler;
    private EventHandler<ActionEvent> addShelfHandler;
    private EventHandler<ActionEvent> addGapHandler;
    private EventHandler<ActionEvent> insertSubHandler;
    private EventHandler<ActionEvent> insertSubsHandler;
    private EventHandler<ActionEvent> undoHandler;
    private EventHandler<ActionEvent> redoHandler;
    private EventHandler<ActionEvent> showMetaLangHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;

    // SCORCHED EARTH RULE 3: Dynamic trackable listeners for the Grid Lines
    private IdentityHashMap<RichTextArea, EventHandler<KeyEvent>> rtaKeyFilters = new IdentityHashMap<>();
    private IdentityHashMap<RichTextArea, ChangeListener<Boolean>> rtaContentFocusListeners = new IdentityHashMap<>();
    private IdentityHashMap<TextFlow, ChangeListener<Boolean>> flowFocusListeners = new IdentityHashMap<>();
    private IdentityHashMap<TextFlow, EventHandler<MouseEvent>> flowClickHandlers = new IdentityHashMap<>();

    // Active editing listeners
    private ChangeListener<Boolean> rtaFocusListener;
    private EventHandler<MouseEvent> justificationClickFilter;
    private EventHandler<KeyEvent> lastJustificationKeyFilter;

    public DerivationExercise(DerivationModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.derivationModel = model;
        if (model.getOriginalModel() == null) {
            model.setOriginalModel(model);
        }
        if (derivationModel.getCheckSetup() == null) derivationModel.setCheckSetup(new CheckSetup());

        List<ThrmSetElement> thrmSetElements = derivationModel.getCheckSetup().getThrmSetElements();
        for (ThrmSetElement thrmSetElement : thrmSetElements) {
            switch (thrmSetElement.getType()) {
                case ZERO_INPUT: theorems.add(new ZeroInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms())); break;
                case ZERO_INPUT2: theorems.add(new ZeroInputTheorem2(thrmSetElement.getName(), thrmSetElement.getForms())); break;
                case SINGLE_INPUT: theorems.add(new SingleInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms())); break;
                case DOUBLE_INPUT: theorems.add(new DoubleInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms())); break;
                case TRIPLE_INPUT: theorems.add(new TripleInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms())); break;
                case ADQ_A4: theorems.add(new ADq_A4(thrmSetElement.getName(), "")); break;
                case ADQ_A5: theorems.add(new ADq_A5(thrmSetElement.getName(), "")); break;
                case ADQT3_28: theorems.add(new ADqT3_28(thrmSetElement.getName(), "")); break;
                case ADQT3_29: theorems.add(new ADqT3_29(thrmSetElement.getName(), "")); break;
                case ADQT3_30: theorems.add(new ADqT3_30(thrmSetElement.getName(), "")); break;
                case ADQT3_31: theorems.add(new ADqT3_31(thrmSetElement.getName(), "")); break;
                case ADQT3_32: theorems.add(new ADqT3_32(thrmSetElement.getName(), "")); break;
                case AD_A7: theorems.add(new AD_A7(thrmSetElement.getName(), "")); break;
                case AD_A8: theorems.add(new AD_A8(thrmSetElement.getName(), "")); break;
                case ADT3_37: theorems.add(new ADT3_37(thrmSetElement.getName(), "")); break;
                case ADT3_38: theorems.add(new ADT3_38(thrmSetElement.getName(), "")); break;
                case QND_T51: theorems.add(new QND_T51(thrmSetElement.getName(), "")); break;
                case PANDT13_11i: theorems.add(new PANDT13_11i(thrmSetElement.getName(), ""));
            }
        }

        this.mainView = mainWindow.getMainView();
        this.derivationView = new DerivationView(mainView);

        setDerivationView();
        derivationCheck = new DerivationCheck(this);
        derivationHelp = new DerivationHelp(this);

        DerivationModel deepCopy = (DerivationModel) SerializationUtils.clone(derivationModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }

    private void setDerivationView() {
        derivationView.setLeftmostScopeLine(derivationModel.isLeftmostScopeLine());
        derivationView.setKeyboardSelector(derivationModel.getKeyboardSelector());
        derivationView.setStatementPrefHeight(derivationModel.getStatementPrefHeight());
        derivationView.setCommentPrefHeight(derivationModel.getCommentPrefHeight());
        derivationView.setSplitPanePrefWidth(derivationModel.getSplitPanePrefWidth());
        derivationView.setShowMetaLang(derivationModel.getCheckSetup().isShowMetalanguageButton());

        derivationView.setPointsPossible(derivationModel.getPointsPossible());
        if (derivationModel.getPointsEarned() >= 0) derivationView.getPointsEarnedTextField().setText(Integer.toString(derivationModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        derivationView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        // Statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(derivationModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                    mainView.setRTATextHeight(statementEditor.getFullHeight());
                }
            } catch (Exception e) {}
        };
        statementEditor.focusedProperty().addListener(statementFocusListener);
        derivationView.setExerciseStatement(statementDRTA);

        // Comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(derivationModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || derivationModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            try {
                double height = (double) nv;
                mainView.setRTATextHeight(height);
                derivationModel.setCommentTextHeight(height);
            } catch (Exception e) {}
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
                } else {
                    mainView.undoRTATextHeight();
                }
            } catch (Exception e) {}
        };
        commentEditor.focusedProperty().addListener(commentFocusListener);
        derivationView.setExerciseComment(commentDRTA);

        // Buttons
        insertLineHandler = e -> { if (isTornDown || mainView == null) return; insertLineAction(); };
        deleteLineHandler = e -> { if (isTornDown || mainView == null) return; deleteLineAction(); };
        indentHandler = e -> { if (isTornDown || mainView == null) return; indentLineAction(); };
        outdentHandler = e -> { if (isTornDown || mainView == null) return; outdentLineAction(); };
        addShelfHandler = e -> { if (isTornDown || mainView == null) return; addShelfLineAction(); };
        addGapHandler = e -> { if (isTornDown || mainView == null) return; addGapLineAction(); };
        insertSubHandler = e -> { if (isTornDown || mainView == null) return; insertSubAction(); };
        insertSubsHandler = e -> { if (isTornDown || mainView == null) return; insertSubsAction(); };
        undoHandler = e -> { if (isTornDown || mainView == null) return; undoAction(); };
        redoHandler = e -> { if (isTornDown || mainView == null) return; redoAction(); };

        derivationView.getInsertLineButton().setOnAction(insertLineHandler);
        derivationView.getDeleteLineButton().setOnAction(deleteLineHandler);
        derivationView.getIndentButton().setOnAction(indentHandler);
        derivationView.getOutdentButton().setOnAction(outdentHandler);
        derivationView.getAddShelfButton().setOnAction(addShelfHandler);
        derivationView.getAddGapButton().setOnAction(addGapHandler);
        derivationView.getInsertSubButton().setOnAction(insertSubHandler);
        derivationView.getInsertSubsButton().setOnAction(insertSubsHandler);
        derivationView.getUndoButton().setOnAction(undoHandler);
        derivationView.getRedoButton().setOnAction(redoHandler);

        derivationView.initializeViewDetails();
        derivationView.getContentSplitPane().setDividerPosition(0, derivationModel.getGridWidth());

        splitPaneDividerListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            double diff = (double) nv - (double) ov;
            if (Math.abs(diff) >= .07) exerciseModified = true;
        };
        derivationView.getContentSplitPane().getDividers().get(0).positionProperty().addListener(splitPaneDividerListener);

        setViewLinesFromModel();
        derivationView.setGridFromViewLines();
        setContentFocusListeners();
        derivationView.setRightControlBox();

        showMetaLangHandler = e -> {
            if (isTornDown || mainView == null) return;
            TextHelpPopup.helpMetalanguage();
        };
        derivationView.getShowMetaLangButton().setOnAction(showMetaLangHandler);

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (derivationView != null && derivationView.getControlBox() != null) {
                    derivationView.getControlBox().getChildren().add(thumbPane);
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
               //     tStage.initOwner(EditorMain.mainStage);
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
        if (derivationView != null && derivationView.getStaticHelpStage() != null) derivationView.getStaticHelpStage().close();
        if (tStage != null) tStage.close();
        if (derivationHelp != null) derivationHelp.closeHelpWindows();
    }

    private void setViewLinesFromModel() {
        List<ModelLine> modelLines = derivationModel.getDerivationLines();
        List<ViewLine> viewLines = new ArrayList<>();
        int lineNumber = 1;
        for (int rowIndex = 0; rowIndex < modelLines.size(); rowIndex++) {
            ModelLine modelLine = modelLines.get(rowIndex);
            ViewLine viewLine = new ViewLine();

            viewLine.setDepth(modelLine.getDepth());
            LineType lineType = modelLine.getLineType();
            viewLine.setLineType(lineType);

            if (LineType.isContentLine(lineType)) {
                Label numLabel = new Label();
                numLabel.setText(Integer.toString(lineNumber++));
                numLabel.setFont(labelFont);
                viewLine.setLineNumberLabel(numLabel);

                BoxedDRTA bdrta = new BoxedDRTA();
                RichTextArea rta = bdrta.getRTA();
                bdrta.getBoxedRTA().setPadding(new Insets(0,0,0,3));

                rta.getActionFactory().open(modelLine.getLineContentDoc()).execute(new ActionEvent());
                rta.getActionFactory().saveNow().execute(new ActionEvent());

                if (LineType.isSetupLine(lineType)) {
                    rta.setEditable(false);
                }

                EventHandler<KeyEvent> rtaKeyFilter = e -> {
                    if (isTornDown || mainView == null || derivationView == null || derivationView.getGrid() == null) return;
                    KeyCode code = e.getCode();
                    try {
                        int row = derivationView.getGrid().getRowIndex(rta.getParent());
                        Node targetNode = null;

                        if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                            targetNode = viewLine.getJustificationFlow();
                        } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                            ViewLine contentLineAbove = getContentLineAbove(row);
                            if (contentLineAbove != null) targetNode = contentLineAbove.getJustificationFlow();
                        } else if (code == KeyCode.UP) {
                            ViewLine contentLineAbove = getContentLineAbove(row);
                            if (contentLineAbove != null) targetNode = contentLineAbove.getLineContentBoxedDRTA().getRTA();
                        } else if (code == KeyCode.DOWN) {
                            ViewLine contentLineBelow = getContentLineBelow(row);
                            if (contentLineBelow != null) targetNode = contentLineBelow.getLineContentBoxedDRTA().getRTA();
                        }

                        if (targetNode != null) {
                            // PREEMPTIVE SAVE: Prevent the background save from aborting the focus transfer!
                            if (rta.isModified()) {
                                pushUndoRedo();
                                exerciseModified = true;
                            }

                            targetNode.requestFocus();
                            e.consume();
                        }
                    } catch (Exception ex) {}
                };

                /*
                EventHandler<KeyEvent> rtaKeyFilter = e -> {
                    if (isTornDown || mainView == null || derivationView == null || derivationView.getGrid() == null) return;
                    KeyCode code = e.getCode();
                    try {
                        int row = derivationView.getGrid().getRowIndex(rta.getParent());

                        if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                            viewLine.getJustificationFlow().requestFocus();
                            e.consume();
                        } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                            ViewLine contentLineAbove = getContentLineAbove(row);
                            if (contentLineAbove != null) contentLineAbove.getJustificationFlow().requestFocus();
                            e.consume();
                        } else if (code == KeyCode.UP) {
                            ViewLine contentLineAbove = getContentLineAbove(row);
                            if (contentLineAbove != null) contentLineAbove.getLineContentBoxedDRTA().getRTA().requestFocus();
                            e.consume();
                        } else if (code == KeyCode.DOWN) {
                            ViewLine contentLineBelow = getContentLineBelow(row);
                            if (contentLineBelow != null) contentLineBelow.getLineContentBoxedDRTA().getRTA().requestFocus();
                            e.consume();
                        }
                    } catch (Exception ex) {}
                };

                 */
                rta.addEventFilter(KeyEvent.KEY_PRESSED, rtaKeyFilter);
                rtaKeyFilters.put(rta, rtaKeyFilter);

                viewLine.setLineContentBoxedDRTA(bdrta);

                TextFlow justificationFlow = getJustificationFlow(modelLine.getJustification(), viewLines);
                viewLine.setJustificationFlow(justificationFlow);

            } else {
                viewLine.setLineNumberLabel(null);
                viewLine.setLineContentBoxedDRTA(null);
                viewLine.setJustificationFlow(null);
            }
            viewLines.add(viewLine);
        }
        derivationView.setViewLines(viewLines);
    }

    private void setContentFocusListeners() {
        if (derivationView == null || derivationView.getViewLines() == null) return;
        List<ViewLine> viewLines = derivationView.getViewLines();
        for (ViewLine viewLine : viewLines) {
            if (LineType.isContentLine(viewLine.getLineType())) {
                BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                DecoratedRTA drta = bdrta.getDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                try { mainView.editorInFocus(drta, ControlType.FIELD); } catch (Exception e) {}

                ChangeListener<Boolean> rtaFocusDynamic = (o, ov, nv) -> {
                    if (isTornDown || mainView == null) return;
                    if (nv) {
                        try { mainView.editorInFocus(drta, ControlType.FIELD); } catch (Exception e) {}
                    } else {
                        if (rta.isModified()) {
                            pushUndoRedo();
                            exerciseModified = true;
                        }
                    }
                };
                rta.focusedProperty().addListener(rtaFocusDynamic);
                rtaContentFocusListeners.put(rta, rtaFocusDynamic);
            }
        }
    }

    public ViewLine getContentLineBelow(int row) {
        if (derivationView == null || derivationView.getViewLines() == null || row < 0 || row >= derivationView.getViewLines().size()) return null;
        ViewLine line = derivationView.getViewLines().get(row);
        row++;
        for (int i = row; i < derivationView.getViewLines().size(); i++) {
            ViewLine temp = derivationView.getViewLines().get(i);
            if (LineType.isContentLine(temp.getLineType())) {
                line = temp;
                break;
            }
        }
        return line;
    }

    public ViewLine getContentLineAbove(int row) {
        if (derivationView == null || derivationView.getViewLines() == null || row < 0 || row >= derivationView.getViewLines().size()) return null;
        ViewLine line = derivationView.getViewLines().get(row);
        row--;
        for (int i = row; i >= 0; i--) {
            ViewLine temp = derivationView.getViewLines().get(i);
            if (LineType.isContentLine(temp.getLineType())) {
                line = temp;
                break;
            }
        }
        return line;
    }

    private TextFlow getJustificationFlow(String justificationString, List<ViewLine> viewLines) {
        justificationString = justificationString.trim();
        TextFlow flow = getStyledJustificationFlow(new TextFlow());

        if (!justificationString.isEmpty()) {
            List<String> splitList = new ArrayList<>();
            boolean startsDigit = false;
            if (charIsDigit(justificationString.charAt(0))) startsDigit = true;
            StringBuilder builder = new StringBuilder();
            boolean buildingDigit = startsDigit;

            int j = 0;
            while (j < justificationString.length()) {
                if (charIsDigit(justificationString.charAt(j)) == buildingDigit) {
                    builder.append(justificationString.charAt(j));
                    j++;
                } else {
                    splitList.add(builder.toString());
                    builder.delete(0, builder.length());
                    buildingDigit = charIsDigit(justificationString.charAt(j));
                }
            }
            splitList.add(builder.toString());

            boolean buildingNum = startsDigit;
            for (int i = 0; i < splitList.size(); i++) {
                if (buildingNum) {
                    Label label = new Label(splitList.get(i));
                    label.setFont(labelFont);

                    if (viewLines != null) {
                        for (int k = 0; k < viewLines.size(); k++) {
                            ViewLine line = viewLines.get(k);
                            if (line.getLineNumberLabel() != null) {
                                String lineLabel = line.getLineNumberLabel().getText();
                                if ((!lineLabel.isEmpty()) && (lineLabel.equals(label.getText()))) {
                                    label.textProperty().bind(line.getLineNumberLabel().textProperty());
                                    if (line.getClientLabels() != null) line.getClientLabels().add(label);
                                    break;
                                }
                            }
                        }
                    }
                    flow.getChildren().add(label);
                } else {
                    Text text = new Text(splitList.get(i));
                    text.setFont(Font.font("Noto Serif Combo", 11));
                    flow.getChildren().add(text);
                }
                buildingNum = !buildingNum;
            }
        }
        return flow;
    }


    private TextFlow getStyledJustificationFlow(TextFlow flow) {
        flow.setFocusTraversable(true);
        flow.setMouseTransparent(false);
        flow.setPrefWidth(Region.USE_COMPUTED_SIZE);
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.setMinWidth(110);
        flow.setMaxHeight(40);
        flow.setPadding(new Insets(0, 0, 0, 0));

        ChangeListener<Boolean> localFlowFocusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ob, Boolean ov, Boolean nv) {
                if (isTornDown || mainView == null) return;
                if (nv) {
                    // Safely ignore the focus event exactly once when parked
                    if (Boolean.TRUE.equals(flow.getProperties().get("suppressFocus"))) {
                        flow.getProperties().put("suppressFocus", false);
                        return;
                    }
                    try {
                        flow.focusedProperty().removeListener(this);
                        flowFocusListeners.remove(flow);
                        editJustificationField(flow);
                    } catch (Exception e) {}
                }
            }
        };

        EventHandler<MouseEvent> clickHandler = e -> {
            if (isTornDown || mainView == null) return;
            // If focus was parked here seamlessly, the listener won't fire on click. Fire manually!
            if (flow.isFocused()) {
                try {
                    flow.focusedProperty().removeListener(localFlowFocusListener);
                    flowFocusListeners.remove(flow);
                    editJustificationField(flow);
                } catch (Exception ex) {}
            } else {
                flow.requestFocus();
            }
        };

        // CRITICAL FIX: To be able to remove this later safely, we MUST use setOnMouseClicked
        flow.setOnMouseClicked(clickHandler);
        flowClickHandlers.put(flow, clickHandler);

        flow.focusedProperty().addListener(localFlowFocusListener);
        flowFocusListeners.put(flow, localFlowFocusListener);

        return flow;
    }



    private void editJustificationField(TextFlow flow) {
        if (isTornDown || derivationView == null || derivationView.getGrid() == null || mainView == null) return;

        // Ensure absolutely no orphaned justification fields stack up in memory
        if (lastJustificationRTA != null) {
            try { if (rtaFocusListener != null) lastJustificationRTA.focusedProperty().removeListener(rtaFocusListener); } catch (Exception e) {}
            try { if (lastJustificationKeyFilter != null) lastJustificationRTA.removeEventFilter(KeyEvent.KEY_PRESSED, lastJustificationKeyFilter); } catch (Exception e) {}
            try { derivationView.getGrid().getChildren().remove(lastJustificationRTA); } catch (Exception e) {}

            hammerRTA(lastJustificationRTA); // Apply Skin Hammer to the discarded field
        }
        if (lastJustificationDRTA != null) {
            try { lastJustificationDRTA.cleanup(); } catch (Exception e) {}
        }
        if (justificationClickFilter != null && mainView.getMainScene() != null) {
            try { mainView.getMainScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, justificationClickFilter); } catch (Exception e) {}
        }

        int rowIndex = derivationView.getGrid().getRowIndex(flow);
        derivationView.getGrid().getChildren().remove(flow);

        String flowText = getStringFromJustificationFlow(flow);

        // CRITICAL LEAK FIX: Destroy the old TextFlow before overwriting it dynamically!
        destroyJustificationFlow(flow);

        // Store DRTA globally so its toolbars/listeners don't float alive in memory after editing
        lastJustificationDRTA = new DecoratedRTA();
        RichTextArea rta = lastJustificationDRTA.getEditor();
        rta.setContentAreaWidth(200);
        rta.setPrefHeight(20);
        rta.setPrefWidth(100);
        rta.getStylesheets().add("slappDerivation.css");
        rta.getActionFactory().open(new Document(flowText)).execute(new ActionEvent());

        if (derivationView.getViewLines().get(rowIndex).getLineType() == LineType.PREMISE_LINE) rta.setEditable(false);
        lastJustificationRTA = rta;
        lastJustificationRow = rowIndex;
        rta.applyCss();
        rta.layout();

        if (derivationView.getViewLines().get(rowIndex).isLineHighlight()) {
            rta.getStylesheets().clear();
            rta.getStylesheets().add("slappDerivationHighlight.css");
        }

        justificationClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || mainWindow == null || lastJustificationDRTA == null) return;
                try {
                    boolean clickOther = !inHierarchy(event.getPickResult().getIntersectedNode(), rta);
                    boolean clickParaToolbarBox = inHierarchy(event.getPickResult().getIntersectedNode(), mainWindow.getMainView().getParaToolbarBox());
                    boolean clickFontsAndEditBox = inHierarchy(event.getPickResult().getIntersectedNode(), mainWindow.getMainView().getFontsAndEditBox());
                    boolean clickKbdSel = inHierarchy(event.getPickResult().getIntersectedNode(), lastJustificationDRTA.getKeyboardSelector());
                    boolean clickUnicode = inHierarchy(event.getPickResult().getIntersectedNode(), lastJustificationDRTA.getUnicodeField());

                    boolean clickRestricted = clickParaToolbarBox || clickFontsAndEditBox || clickKbdSel || clickUnicode;

                    if (clickOther && !clickRestricted) {
                        if (editJustification) {
                            editJustification = false;
                            rta.getActionFactory().saveNow().execute(new ActionEvent());
                            saveJustificationRTA(rta, rowIndex);
                        }
                    }
                } catch (Exception ex) {}
            }
        };

        rtaFocusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ob, Boolean ov, Boolean nv) {
                if (isTornDown || mainView == null || mainView.getMainScene() == null || lastJustificationDRTA == null) return;
                try {
                    if (nv) {
                        editJustification = true;
                        mainView.getMainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, justificationClickFilter);
                    } else {
                        Node focusOwner = mainView.getMainScene().getFocusOwner();
                        boolean focusUnicode = focusOwner == lastJustificationDRTA.getUnicodeField();
                        boolean focusKbdDia = focusOwner == lastJustificationDRTA.getKeyboardDiagramButton();

                        boolean restrictedFocus = focusUnicode || focusKbdDia;

                        if (editJustification && !restrictedFocus) {
                            editJustification = false;
                            rta.getActionFactory().saveNow().execute(new ActionEvent());
                            saveJustificationRTA(rta, rowIndex);
                        }
                    }
                } catch (Exception ex) {}
            }
        };
        rta.focusedProperty().addListener(rtaFocusListener);
        lastJustificationKeyFilter = e -> {
            if (isTornDown || mainView == null || derivationView == null || derivationView.getGrid() == null) return;
            KeyCode code = e.getCode();
            try {
                int row = derivationView.getGrid().getRowIndex(rta);

                if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                    if (editJustification) {
                        editJustification = false;
                        rta.getActionFactory().saveNow().execute(new ActionEvent());
                        saveJustificationRTA(rta, row);
                    }
                    ViewLine contentLineBelow = getContentLineBelow(row);
                    if (contentLineBelow != null) contentLineBelow.getLineContentBoxedDRTA().getRTA().requestFocus();
                    e.consume();
                } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                    if (editJustification) {
                        editJustification = false;
                        rta.getActionFactory().saveNow().execute(new ActionEvent());
                        saveJustificationRTA(rta, row);
                    }
                    ViewLine currentLine = derivationView.getViewLines().get(row);
                    if (currentLine != null) currentLine.getLineContentBoxedDRTA().getRTA().requestFocus();
                    e.consume();
                } else if (code == KeyCode.UP) {
                    if (editJustification) {
                        editJustification = false;
                        rta.getActionFactory().saveNow().execute(new ActionEvent());
                        saveJustificationRTA(rta, row);
                    }
                    ViewLine contentLineAbove = getContentLineAbove(row);
                    if (contentLineAbove != null) contentLineAbove.getJustificationFlow().requestFocus();
                    e.consume();
                } else if (code == KeyCode.DOWN) {
                    if (editJustification) {
                        editJustification = false;
                        rta.getActionFactory().saveNow().execute(new ActionEvent());
                        saveJustificationRTA(rta, row);
                    }
                    ViewLine contentLineBelow = getContentLineBelow(row);
                    if (contentLineBelow != null) contentLineBelow.getJustificationFlow().requestFocus();
                    e.consume();
                }
            } catch (Exception ex) {}
        };
        rta.addEventFilter(KeyEvent.KEY_PRESSED, lastJustificationKeyFilter);

        derivationView.getGrid().add(rta, 22, rowIndex);
        rta.requestFocus();
        try { mainView.editorInFocus(lastJustificationDRTA, ControlType.JUSTIFICATION); } catch (Exception e) {}
    }

    public static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) return true;
        while (node != null) {
            if (node == potentialHierarchyElement) return true;
            node = node.getParent();
        }
        return false;
    }

    // SCORCHED EARTH RULE 8 & 9: Dynamic Node Sever Helper to prevent floating TextFlows and bound labels!
    private void destroyJustificationFlow(TextFlow oldFlow) {
        if (oldFlow == null) return;
        try {
            oldFlow.setOnMouseClicked(null);
            if (flowClickHandlers.containsKey(oldFlow)) {
                flowClickHandlers.remove(oldFlow);
            }
            if (flowFocusListeners.containsKey(oldFlow)) {
                oldFlow.focusedProperty().removeListener(flowFocusListeners.remove(oldFlow));
            }
            if (oldFlow.getChildren() != null) {
                List<Node> children = new ArrayList<>(oldFlow.getChildren());
                for (Node n : children) {
                    if (n instanceof Label) {
                        Label lbl = (Label) n;
                        try { if (lbl.textProperty() != null) lbl.textProperty().unbind(); } catch (Exception ex) {}
                        if (derivationView != null && derivationView.getViewLines() != null) {
                            for (ViewLine vl : derivationView.getViewLines()) {
                                if (vl != null && vl.getClientLabels() != null) {
                                    vl.getClientLabels().remove(lbl);
                                }
                            }
                        }
                    }
                    severFromParent(n);
                }
                oldFlow.getChildren().clear();
            }
            severFromParent(oldFlow);
        } catch (Exception e) {}
    }

    private void saveJustificationRTA(RichTextArea rta, int rowIndex) {
        try {
            if (justificationClickFilter != null && mainView != null && mainView.getMainScene() != null) {
                mainView.getMainScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, justificationClickFilter);
            }
        } catch (Exception e) {}
        try { if (rtaFocusListener != null) rta.focusedProperty().removeListener(rtaFocusListener); } catch (Exception e) {}
        try { if (lastJustificationKeyFilter != null) rta.removeEventFilter(KeyEvent.KEY_PRESSED, lastJustificationKeyFilter); } catch (Exception e) {}

        rta.setEditable(true);
        boolean modified = rta.isModified();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        String justificationString = rta.getDocument().getText();

        if (derivationView != null && derivationView.getViewLines() != null && derivationView.getViewLines().size() > rowIndex) {
            ViewLine targetLine = derivationView.getViewLines().get(rowIndex);

            destroyJustificationFlow(targetLine.getJustificationFlow());

            TextFlow justificationFlow = getJustificationFlow(justificationString, derivationView.getViewLines());
            if (targetLine.isLineHighlight()) {
                justificationFlow.setStyle("-fx-background-color: mistyrose");
            }
            targetLine.setJustificationFlow(justificationFlow);
            derivationView.setJustificationFlowOnGrid(rowIndex);

            // THE SEAMLESS FOCUS PARK
            // By parking focus on the inert TextFlow BEFORE removing the RTA, we prevent
            // JavaFX from panicking and auto-transferring focus to the left formula field.
            // This completely stops the "flash" and prevents the toolbars from reacting,
            // allowing components like the MenuBar to open smoothly!
            boolean isFocusOwner = rta.isFocused();
            try {
                if (rta.getScene() != null && rta.getScene().getFocusOwner() == rta) isFocusOwner = true;
            } catch (Exception e) {}

            if (isFocusOwner) {
                justificationFlow.getProperties().put("suppressFocus", true);
                justificationFlow.requestFocus();
                justificationFlow.getProperties().put("suppressFocus", false);
            }
        }

        // Remove from scene BEFORE hammering so the caret collapse doesn't affect the ScrollPane
        try {
            if (derivationView != null && derivationView.getGrid() != null) {
                derivationView.getGrid().getChildren().remove(rta);
            }
            severFromParent(rta);
        } catch (Exception e) {}

        try {
            if (lastJustificationDRTA != null) {
                lastJustificationDRTA.cleanup();
                lastJustificationDRTA = null;
            }
        } catch (Exception e) {}

        hammerRTA(rta);
        if (lastJustificationRTA == rta) {
            lastJustificationRTA = null;
        }



        if (modified) {
            pushUndoRedo();
            exerciseModified = true;
        }
    }



    public String getStringFromJustificationFlow(TextFlow flow) {
        StringBuilder sb = new StringBuilder();
        if (flow != null && flow.getChildren() != null) {
            for (Node node : flow.getChildren()) {
                if (node instanceof Label) sb.append(((Label) node).getText());
                else if (node instanceof Text) sb.append(((Text) node).getText());
            }
        }
        return sb.toString();
    }

    public List<String> getLineLabelsFromJustificationFlow(TextFlow flow) {
        List<String> labelList = new ArrayList<>();
        if (flow != null && flow.getChildren() != null) {
            for (Node node : flow.getChildren()) {
                if (node instanceof Label) labelList.add(((Label) node).getText());
            }
        }
        return labelList;
    }

    private boolean charIsDigit(char character) {
        return ('0' <= character && character <= '9') || character == '.';
    }

    private void addEmptyViewContentRow(int newRow, int depth) {
        Label numLabel = new Label();
        numLabel.setFont(labelFont);

        BoxedDRTA bdrta = new BoxedDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.getStylesheets().add("slappDerivation.css");
        bdrta.getBoxedRTA().setPadding(new Insets(0,0,0,3));

        EventHandler<KeyEvent> rtaKeyFilter = e -> {
            if (isTornDown || mainView == null || derivationView == null || derivationView.getGrid() == null) return;
            KeyCode code = e.getCode();
            try {
                int row = derivationView.getGrid().getRowIndex(rta.getParent());
                Node targetNode = null;

                if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                    targetNode = derivationView.getViewLines().get(row).getJustificationFlow();
                } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                    if (getContentLineAbove(row) != null) targetNode = getContentLineAbove(row).getJustificationFlow();
                } else if (code == KeyCode.UP) {
                    if (getContentLineAbove(row) != null) targetNode = getContentLineAbove(row).getLineContentBoxedDRTA().getRTA();
                } else if (code == KeyCode.DOWN) {
                    if (getContentLineBelow(row) != null) targetNode = getContentLineBelow(row).getLineContentBoxedDRTA().getRTA();
                }

                if (targetNode != null) {
                    // PREEMPTIVE SAVE: Prevent the background save from aborting the focus transfer!
                    if (rta.isModified()) {
                        pushUndoRedo();
                        exerciseModified = true;
                    }

                    targetNode.requestFocus();
                    e.consume();
                }
            } catch (Exception ex) {}
        };

        /*
        EventHandler<KeyEvent> rtaKeyFilter = e -> {
            if (isTornDown || mainView == null || derivationView == null || derivationView.getGrid() == null) return;
            KeyCode code = e.getCode();
            try {
                int row = derivationView.getGrid().getRowIndex(rta.getParent());

                if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                    derivationView.getViewLines().get(row).getJustificationFlow().requestFocus();
                    e.consume();
                } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                    if (getContentLineAbove(row) != null) getContentLineAbove(row).getJustificationFlow().requestFocus();
                    e.consume();
                } else if (code == KeyCode.UP) {
                    if (getContentLineAbove(row) != null) getContentLineAbove(row).getLineContentBoxedDRTA().getRTA().requestFocus();
                    e.consume();
                } else if (code == KeyCode.DOWN) {
                    if (getContentLineBelow(row) != null) getContentLineBelow(row).getLineContentBoxedDRTA().getRTA().requestFocus();
                    e.consume();
                }
            } catch (Exception ex) {}
        };

         */
        rta.addEventFilter(KeyEvent.KEY_PRESSED, rtaKeyFilter);
        rtaKeyFilters.put(rta, rtaKeyFilter);

        DecoratedRTA drta = bdrta.getDRTA();
        try { mainView.editorInFocus(drta, ControlType.FIELD); } catch (Exception e) {}

        ChangeListener<Boolean> rtaFocusDynamic = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) {
                try { mainView.editorInFocus(drta, ControlType.FIELD); } catch (Exception e) {}
            } else {
                if (rta.isModified()) {
                    pushUndoRedo();
                    exerciseModified = true;
                }
            }
        };
        rta.focusedProperty().addListener(rtaFocusDynamic);
        rtaContentFocusListeners.put(rta, rtaFocusDynamic);

        TextFlow flow = new TextFlow();
        TextFlow justificationFlow = getStyledJustificationFlow(flow);
        ViewLine viewLine = new ViewLine(numLabel, depth, LineType.MAIN_CONTENT_LINE, bdrta, justificationFlow, new ArrayList<Label>());

        derivationView.getViewLines().add(newRow, viewLine);
    }

    // SCORCHED EARTH RULE 4: Dynamic Skin Hammer helper for severed rows
    private void hammerRTA(RichTextArea rta) {
        if (rta == null) return;
        try {
            rta.setPromptText("");
            rta.setFocusTraversable(false);
            rta.setMouseTransparent(true);
            if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
            }
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
            pt.setOnFinished(ev -> {
                try {
                    rta.setSkin(null);
                } catch (Exception ex) {}
            });
            pt.play();
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 8 & 9: Dynamic Node Sever Helper for UI Deletions & Rebuilds
    private void severViewLine(ViewLine line) {
        if (line == null) return;
        try {
            if (line.getClientLabels() != null) {
                for (Label label : new java.util.ArrayList<>(line.getClientLabels())) {
                    try { if (label.textProperty() != null) label.textProperty().unbind(); label.setText("??"); } catch (Exception ex) {}
                }
                line.getClientLabels().clear();
            }
            if (line.getLineContentBoxedDRTA() != null && line.getLineContentBoxedDRTA().getRTA() != null) {
                RichTextArea rta = line.getLineContentBoxedDRTA().getRTA();
                if (rtaKeyFilters.containsKey(rta)) {
                    rta.removeEventFilter(KeyEvent.KEY_PRESSED, rtaKeyFilters.remove(rta));
                }
                if (rtaContentFocusListeners.containsKey(rta)) {
                    rta.focusedProperty().removeListener(rtaContentFocusListeners.remove(rta));
                }

                // THE MISSING HAMMER: Safely dismantle orphaned dynamic line RTAs during Undo/Redo/Delete
                hammerRTA(rta);

                try { line.getLineContentBoxedDRTA().cleanup(); } catch (Exception e) {}
                severFromParent(line.getLineContentBoxedDRTA().getBoxedRTA());
            }
            if (line.getJustificationFlow() != null) {
                destroyJustificationFlow(line.getJustificationFlow());
            }
            try { line.cleanup(); } catch (Exception e) {} // Allow ViewLine to fully clear out its own local properties
        } catch (Exception e) {}
    }

    private void severAllViewLines() {
        try {
            if (derivationView != null && derivationView.getViewLines() != null) {
                List<ViewLine> copyLines = new ArrayList<>(derivationView.getViewLines());
                for (ViewLine vl : copyLines) {
                    severViewLine(vl);
                }
                derivationView.getViewLines().clear();
            }
        } catch (Exception e) {}
    }

    private void undoAction() {
        if (undoRedoList == null) return;
        DerivationModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            severAllViewLines();
            derivationModel = (DerivationModel) SerializationUtils.clone(undoElement);
            setViewLinesFromModel();
            derivationView.setGridFromViewLines();
            updateUndoRedoButtons();
            setContentFocusListeners();
        }
    }

    private void redoAction() {
        if (undoRedoList == null) return;
        DerivationModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            severAllViewLines();
            derivationModel = (DerivationModel) SerializationUtils.clone(redoElement);
            setViewLinesFromModel();
            derivationView.setGridFromViewLines();
            updateUndoRedoButtons();
            setContentFocusListeners();
        }
    }

    private void updateUndoRedoButtons() {
        if (derivationView == null || undoRedoList == null) return;
        if (derivationView.getUndoButton() != null) derivationView.getUndoButton().setDisable(!undoRedoList.canUndo());
        if (derivationView.getRedoButton() != null) derivationView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    private void pushUndoRedo() {
        if (undoRedoList == null || derivationModel == null) return;
        DerivationModel model = getDerivationModelFromView();
        DerivationModel deepCopy = (DerivationModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }

    private void insertLineAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;

        row = currentRow();

        if (row >= 0) {
            ViewLine viewLine = derivationView.getViewLines().get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType()) || viewLine.getLineType() == LineType.CONCLUSION_LINE) {
                addEmptyViewContentRow(row, depth);
                derivationView.setGridFromViewLines();
                pushUndoRedo();

                BoxedDRTA bdrta = derivationView.getViewLines().get(row).getLineContentBoxedDRTA();
                if (bdrta != null && bdrta.getRTA() != null) {
                    RichTextArea rta = bdrta.getRTA();
                    rta.applyCss();
                    rta.layout();
                }
                exerciseModified = true;
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup lines.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select derivation row for insert above.");
        }
    }

    private void deleteLineAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;

        row = currentRow();

        List<ViewLine> viewLines = derivationView.getViewLines();
        if (row >= 0 && row < viewLines.size()) {
            if (viewLines.size() > 1) {
                if (!LineType.isSetupLine(viewLines.get(row).getLineType())) {

                    ViewLine removedLine = viewLines.remove(row);
                    severViewLine(removedLine); // SEVER HANDLES UNBINDING & SKIN HAMMERING

                    exerciseModified = true;

                    if (row < viewLines.size()) {
                        LineType type = viewLines.get(row).getLineType();
                        if (LineType.isShelfLine(type) || LineType.isGapLine(type)) {
                            ViewLine removedShelf = viewLines.remove(row);
                            severViewLine(removedShelf);
                        }
                    }

                    derivationView.setGridFromViewLines();
                    pushUndoRedo();

                    for (int i = row; i < viewLines.size(); i++) {
                        if (viewLines.get(i).getLineContentBoxedDRTA() != null) {
                            viewLines.get(i).getLineContentBoxedDRTA().getRTA().requestFocus();
                            break;
                        }
                    }
                } else {
                    EditorAlerts.fleetingRedPopup("Cannot modify setup line.");
                }
            } else {
                EditorAlerts.fleetingRedPopup("A derivation must contain at least one line.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select derivation line to delete.");
        }
    }

    private void indentLineAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;

        row = currentRow();

        if (row >= 0) {
            List<ViewLine> viewLines = derivationView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                int depth = viewLine.getDepth();
                if (depth < 19) {
                    viewLine.setDepth(++depth);
                    viewLines.set(row, viewLine);
                    if (++row < viewLines.size()) {
                        ViewLine nextLine = viewLines.get(row);
                        if (LineType.isShelfLine(nextLine.getLineType()) || LineType.isGapLine(nextLine.getLineType())) {
                            nextLine.setDepth(depth);
                            viewLines.set(row, nextLine);
                        }
                    }
                    derivationView.setGridFromViewLines();
                    pushUndoRedo();
                    exerciseModified = true;
                } else {
                    EditorAlerts.fleetingRedPopup("19 is the maximum scope depth.");
                }
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup line.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select derivation row to indent.");
        }
    }

    private void outdentLineAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;


        row = currentRow();

        if (row >= 0) {
            List<ViewLine> viewLines = derivationView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                int depth = viewLine.getDepth();
                if (depth > 1) {
                    viewLine.setDepth(--depth);
                    viewLines.set(row, viewLine);
                    if (++row < viewLines.size()) {
                        ViewLine nextLine = viewLines.get(row);
                        if (LineType.isShelfLine(nextLine.getLineType()) || LineType.isGapLine(nextLine.getLineType())) {
                            if (depth > 1) {
                                nextLine.setDepth(depth);
                                viewLines.set(row, nextLine);
                            } else {
                                ViewLine removedShelf = viewLines.remove(row);
                                severViewLine(removedShelf);
                            }
                        }
                    }
                    derivationView.setGridFromViewLines();
                    pushUndoRedo();
                    exerciseModified = true;
                } else {
                    EditorAlerts.fleetingRedPopup("1 is the mininum scope depth.");
                }
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup line.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select derivation row to outdent.");
        }
    }

    private void addShelfLineAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;

        row = currentRow();

        if (row >= 0) {
            List<ViewLine> viewLines = derivationView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                if (++row < viewLines.size()) {
                    if (!(LineType.isShelfLine(viewLines.get(row).getLineType()) || LineType.isGapLine(viewLines.get(row).getLineType()))) {
                        ViewLine shelfLine = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                        viewLines.add(row, shelfLine);
                        if (viewLine.getLineContentBoxedDRTA() != null && viewLine.getLineContentBoxedDRTA().getRTA() != null) viewLine.getLineContentBoxedDRTA().getRTA().requestFocus();
                        derivationView.setGridFromViewLines();
                        pushUndoRedo();
                        exerciseModified = true;
                    } else {
                        EditorAlerts.fleetingRedPopup("No shelf on top of shelf or gap.");
                    }
                } else {
                    EditorAlerts.fleetingRedPopup("No shelf under last line.");
                }
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup line.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select row to have shelf.");
        }
    }

    private void addGapLineAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;

        row = currentRow();

        if (row >= 0) {
            List<ViewLine> viewLines = derivationView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                if (depth > 1) {
                    if (++row < viewLines.size()) {
                        if (!(LineType.isShelfLine(viewLines.get(row).getLineType()) || LineType.isGapLine(viewLines.get(row).getLineType()))) {
                            ViewLine gapLine = new ViewLine(null, depth, LineType.GAP_LINE, null, null, null);
                            viewLines.add(row, gapLine);
                            if (viewLine.getLineContentBoxedDRTA() != null && viewLine.getLineContentBoxedDRTA().getRTA() != null) viewLine.getLineContentBoxedDRTA().getRTA().requestFocus();
                            derivationView.setGridFromViewLines();
                            pushUndoRedo();
                            exerciseModified = true;
                        } else {
                            EditorAlerts.fleetingRedPopup("No gap on top of shelf or gap.");
                        }
                    } else {
                        EditorAlerts.fleetingRedPopup("No gap under last line.");
                    }
                } else {
                    EditorAlerts.fleetingRedPopup("Cannot modify at leftmost scope depth.");
                }
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup line.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select row to have gap.");
        }
    }

    private void insertSubAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;

        row = currentRow();

        if (row >= 0) {
            ViewLine viewLine = derivationView.getViewLines().get(row);
            int depth = viewLine.getDepth();
            depth++;
            if (!LineType.isSetupLine(viewLine.getLineType()) || viewLine.getLineType() == LineType.CONCLUSION_LINE) {
                addEmptyViewContentRow(row, depth);
                addEmptyViewContentRow(row, depth);
                if (derivationModel.isDefaultShelf()) {
                    ViewLine shelfLine = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                    derivationView.getViewLines().add(row, shelfLine);
                }
                addEmptyViewContentRow(row, depth);

                derivationView.setGridFromViewLines();
                pushUndoRedo();
                exerciseModified = true;
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup lines.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select derivation row for insert above.");
        }
    }

    private void insertSubsAction() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return;


        row = currentRow();

        if (row >= 0) {
            ViewLine viewLine = derivationView.getViewLines().get(row);
            int depth = viewLine.getDepth();
            depth++;
            if (!LineType.isSetupLine(viewLine.getLineType()) || viewLine.getLineType() == LineType.CONCLUSION_LINE) {
                addEmptyViewContentRow(row, depth);
                addEmptyViewContentRow(row, depth);
                if (derivationModel.isDefaultShelf()) {
                    ViewLine shelfLine1 = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                    derivationView.getViewLines().add(row, shelfLine1);
                }
                addEmptyViewContentRow(row, depth);

                ViewLine gapLine = new ViewLine(null, depth, LineType.GAP_LINE, null, null, null);
                derivationView.getViewLines().add(row, gapLine);

                addEmptyViewContentRow(row, depth);
                addEmptyViewContentRow(row, depth);
                if (derivationModel.isDefaultShelf()) {
                    ViewLine shelfLine2 = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                    derivationView.getViewLines().add(row, shelfLine2);
                }
                addEmptyViewContentRow(row, depth);
                derivationView.setGridFromViewLines();
                pushUndoRedo();
                exerciseModified = true;
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup lines.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select derivation row for insert above.");
        }
    }

    public int currentRow() {
        int row = -1;
        if (mainWindow == null || derivationView == null || derivationView.getGrid() == null) return row;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (lastFocusedNode != null) {
            if (derivationView.getGrid().getChildren().contains(lastFocusedNode))
                row = derivationView.getGrid().getRowIndex(lastFocusedNode);
            else if (lastFocusedNode.getParent() != null && derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
                row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
            else if (lastJustificationRTA != null && lastJustificationRTA == lastFocusedNode)
                row = lastJustificationRow;
        }
        return row;
    }

    public DerivationHelp getDerivationHelp() {
        return derivationHelp;
    }

    @Override
    public DerivationModel getExerciseModel() { return derivationModel; }

    @Override
    public DerivationView getExerciseView() { return derivationView; }

    @Override
    public void saveExercise(boolean saveAs) {
        Platform.runLater(() -> {
            if (isTornDown) return;
            boolean success = DiskUtilities.saveExercise(saveAs, getDerivationModelFromView());
            if (success) exerciseModified = false;
        });
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        DerivationExercise printExercise = this;
        DerivationModel printModel = derivationModel;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        Label exerciseName = new Label(derivationModel.getExerciseName());
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

        GridPane derivationPane = printExercise.getExerciseView().getGrid();
        derivationPane.setPadding(new Insets(15,0,15,0));

        double splitPaneWidth = Math.max(nodeWidth, printModel.getSplitPanePrefWidth() - RTA_WIDTH_BMP);
        double width = printModel.getGridWidth() * splitPaneWidth;
        derivationPane.setMaxWidth(width);
        derivationPane.setMinWidth(width);
        HBox gridBox = new HBox(derivationPane);
        gridBox.setAlignment(Pos.CENTER);
        nodeList.add(gridBox);

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
        commentRTA.maxWidthProperty().unbind();
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
        commentRTA.setMinWidth(nodeWidth);
        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");

        /*
        Node commentNode;
        if (printModel.getPointsPossible() > 0) {
            Label pointsLabel = new Label(Integer.toString(printModel.getPointsEarned()) + "/" + Integer.toString(printModel.getPointsPossible()));
            AnchorPane anchorPane = new AnchorPane(commentRTA, pointsLabel);
            anchorPane.setTopAnchor(commentRTA, 0.0);
            anchorPane.setLeftAnchor(commentRTA, 0.0);
            anchorPane.setBottomAnchor(pointsLabel, 3.0);
            anchorPane.setRightAnchor(pointsLabel, 3.0);
            commentNode = anchorPane;
        } else {
            commentNode = commentRTA;
        }
        nodeList.add(commentNode);

         */

        nodeList.add(commentRTA);

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (derivationCheck != null && derivationCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + derivationView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (derivationCheck.isCheckFinal()) {
                bigCheck.setFill(Color.LAWNGREEN);
                message.setFill(Color.GREEN);
            } else {
                bigCheck.setFill(Color.ORCHID);
                message.setFill(Color.PURPLE);
            }
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }

    @Override
    public Exercise<DerivationModel, DerivationView> resetExercise() {
        RichTextArea commentRTA = derivationView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = derivationView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || derivationModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        DerivationModel originalModel = (DerivationModel) (derivationModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : derivationModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);
        CheckSetup setup = originalModel.getCheckSetup();

        if (derivationCheck != null) {
            setup.setHelpTries(derivationCheck.getHelpTries());
            setup.setCheckTries(derivationCheck.getCheckTries());
        }
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        DerivationExercise clearExercise = new DerivationExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (derivationView == null) return exerciseModified;
        RichTextArea commentEditor = derivationView.getExerciseComment().getEditor();
        if (commentEditor != null && commentEditor.isModified()) {
            exerciseModified = true;
        }
        if (derivationView.getViewLines() != null) {
            List<ViewLine> viewLines = derivationView.getViewLines();
            for (ViewLine viewLine : viewLines) {
                if (LineType.isContentLine(viewLine.getLineType()) && viewLine.getLineContentBoxedDRTA() != null) {
                    RichTextArea rta = viewLine.getLineContentBoxedDRTA().getRTA();
                    if (rta != null && rta.isModified()) {
                        exerciseModified = true;
                    }
                }
            }
        }
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) { this.exerciseModified = modified; }

    @Override
    public Node getFFViewNode() { return derivationView.getExerciseContentNode(); }

    @Override
    public Spinner getFFHeightSpinner() { return derivationView.getSplitPaneHeightSpinner(); }

    @Override
    public Spinner getFFWidthSpinner() { return derivationView.getSplitPaneWidthSpinner(); }

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        DerivationExercise printExercise = this;
        DerivationModel printModel = derivationModel;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        GridPane derivationPane = printExercise.getExerciseView().getGrid();
        derivationPane.setPadding(new Insets(15,0,15,0));

        double splitPaneWidth = Math.max(nodeWidth, printModel.getSplitPanePrefWidth() - RTA_WIDTH_BMP);
        double width = printModel.getGridWidth() * splitPaneWidth;

        derivationPane.setMaxWidth(width);
        derivationPane.setMinWidth(width);
        HBox gridBox = new HBox(derivationPane);
        gridBox.setAlignment(Pos.CENTER);

        return gridBox;
    }

    @Override
    public ExerciseModel<DerivationModel> getExerciseModelFromView() {
        return (ExerciseModel) getDerivationModelFromView();
    }




    private DerivationModel getDerivationModelFromView() {
        String name = derivationModel.getExerciseName();
        Boolean started = (derivationModel.isStarted() || exerciseModified);

        Document statementDocument = derivationModel.getExerciseStatement();
        double statementHeight = derivationView.getExerciseStatement().getEditor().getPrefHeight();

        RichTextArea commentRTA = derivationView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        double gridWidth = derivationView.getContentSplitPane().getDividerPositions()[0];
        boolean leftmostScopeLine = derivationModel.isLeftmostScopeLine();
        boolean defaultShelf = derivationModel.isDefaultShelf();
        RichTextAreaSkin.KeyMapValue keyboardSelector = derivationModel.getKeyboardSelector();

        List<ModelLine> modelLines = new ArrayList<>();
        List<ViewLine> viewLines = derivationView.getViewLines();
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            int depth = viewLine.getDepth();

            LineType lineType = viewLine.getLineType();
            Document lineContentDocument = null;
            String justification = "";
            if (LineType.isContentLine(lineType)) {
                if (viewLine.getLineContentBoxedDRTA() != null) {
                    RichTextArea lineContentRTA = viewLine.getLineContentBoxedDRTA().getRTA();
                    boolean editable = lineContentRTA.isEditable();
                    lineContentRTA.setEditable(true);
              //      lineContentRTA.setDisable(true);
                    lineContentRTA.getActionFactory().saveNow().execute(new ActionEvent());
               //     lineContentRTA.setDisable(false);
                    lineContentDocument = lineContentRTA.getDocument();
                    lineContentRTA.setEditable(editable);
                }
                justification = getStringFromJustificationFlow(viewLine.getJustificationFlow());
            }

            ModelLine modelLine = new ModelLine(depth, lineContentDocument, justification, lineType);
            modelLines.add(modelLine);
        }

        DerivationModel newModel = new DerivationModel(name, started, statementHeight,gridWidth, leftmostScopeLine, defaultShelf, keyboardSelector, statementDocument, commentDocument, modelLines);
        newModel.setOriginalModel(derivationModel.getOriginalModel());
        newModel.setCommentPrefHeight(derivationView.getCommentPrefHeight());
        newModel.setSplitPanePrefWidth(derivationView.getSplitPanePrefWidth());

        double commentTextHeight = commentRTA.getFullHeight();
        newModel.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : derivationModel.getCommentTextHeight());

        newModel.setStatementTextHeight(derivationModel.getStatementTextHeight());
        newModel.setPointsPossible(derivationModel.getPointsPossible());
        newModel.setAuxExerName(derivationModel.getAuxExerName());

        newModel.setPointsPossible(derivationModel.getPointsPossible());
        TextField pointsField = derivationView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || derivationModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) newModel.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else newModel.setPointsEarned(-1);

        CheckSetup setup = derivationModel.getCheckSetup();
        if (derivationCheck != null) {
            setup.setCheckSuccess(derivationCheck.isCheckSuccess());
            setup.setCheckFinal(derivationCheck.isCheckFinal());
            setup.setCheckTries(derivationCheck.getCheckTries());
            setup.setHelpTries(derivationCheck.getHelpTries());
        }
        newModel.setCheckSetup(setup);
        return newModel;
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

    // SCORCHED EARTH RULE 1: Cascading Cleanup
    @Override
    public void cleanup() {
        // KILL SWITCH: Engaged! Prevent any focus listeners from doing UI operations mid-teardown
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Clear global MainView references safely
        try {
            if (mainView != null) {
                mainView.textFieldInFocus();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try { if (derivationView != null && derivationView.localScalePageHeightProperty() != null) derivationView.localScalePageHeightProperty().unbind(); } catch (Exception e) {}
        try { if (derivationView != null && derivationView.localScalePageWidthProperty() != null) derivationView.localScalePageWidthProperty().unbind(); } catch (Exception e) {}

        try { if (mainView != null && mainView.getMainScene() != null && derivationView != null && derivationView.getExerciseFocusListener() != null) mainView.getMainScene().focusOwnerProperty().removeListener(derivationView.getExerciseFocusListener()); } catch (Exception e) {}
        try { if (mainView != null && mainView.getMainScene() != null && justificationClickFilter != null) mainView.getMainScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, justificationClickFilter); } catch (Exception e) {}

        try { if (derivationView != null && derivationView.getPointsEarnedTextField() != null && pointsEarnedListener != null) derivationView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}
        try { if (derivationView != null && derivationView.getExerciseStatement() != null && derivationView.getExerciseStatement().getEditor() != null && statementFocusListener != null) derivationView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (derivationView != null && derivationView.getExerciseComment() != null && derivationView.getExerciseComment().getEditor() != null) {
                RichTextArea commentEditor = derivationView.getExerciseComment().getEditor();
                if (commentFullHeightListener != null) commentEditor.fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) commentEditor.modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) commentEditor.focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (derivationView != null && derivationView.getContentSplitPane() != null && !derivationView.getContentSplitPane().getDividers().isEmpty() && splitPaneDividerListener != null) {
                derivationView.getContentSplitPane().getDividers().get(0).positionProperty().removeListener(splitPaneDividerListener);
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

        try {
            if (derivationView != null) {
                if (derivationView.getInsertLineButton() != null) { derivationView.getInsertLineButton().removeEventHandler(ActionEvent.ACTION, insertLineHandler); derivationView.getInsertLineButton().setOnAction(null); }
                if (derivationView.getDeleteLineButton() != null) { derivationView.getDeleteLineButton().removeEventHandler(ActionEvent.ACTION, deleteLineHandler); derivationView.getDeleteLineButton().setOnAction(null); }
                if (derivationView.getIndentButton() != null) { derivationView.getIndentButton().removeEventHandler(ActionEvent.ACTION, indentHandler); derivationView.getIndentButton().setOnAction(null); }
                if (derivationView.getOutdentButton() != null) { derivationView.getOutdentButton().removeEventHandler(ActionEvent.ACTION, outdentHandler); derivationView.getOutdentButton().setOnAction(null); }
                if (derivationView.getAddShelfButton() != null) { derivationView.getAddShelfButton().removeEventHandler(ActionEvent.ACTION, addShelfHandler); derivationView.getAddShelfButton().setOnAction(null); }
                if (derivationView.getAddGapButton() != null) { derivationView.getAddGapButton().removeEventHandler(ActionEvent.ACTION, addGapHandler); derivationView.getAddGapButton().setOnAction(null); }
                if (derivationView.getInsertSubButton() != null) { derivationView.getInsertSubButton().removeEventHandler(ActionEvent.ACTION, insertSubHandler); derivationView.getInsertSubButton().setOnAction(null); }
                if (derivationView.getInsertSubsButton() != null) { derivationView.getInsertSubsButton().removeEventHandler(ActionEvent.ACTION, insertSubsHandler); derivationView.getInsertSubsButton().setOnAction(null); }
                if (derivationView.getUndoButton() != null) { derivationView.getUndoButton().removeEventHandler(ActionEvent.ACTION, undoHandler); derivationView.getUndoButton().setOnAction(null); }
                if (derivationView.getRedoButton() != null) { derivationView.getRedoButton().removeEventHandler(ActionEvent.ACTION, redoHandler); derivationView.getRedoButton().setOnAction(null); }
                if (derivationView.getShowMetaLangButton() != null) { derivationView.getShowMetaLangButton().removeEventHandler(ActionEvent.ACTION, showMetaLangHandler); derivationView.getShowMetaLangButton().setOnAction(null); }
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 8 & 9: Dismantle lines cleanly and safely skin hammer them all
        try { severAllViewLines(); } catch (Exception e) {}

        try {
            if (lastJustificationRTA != null) {
                if (rtaFocusListener != null) lastJustificationRTA.focusedProperty().removeListener(rtaFocusListener);
                if (lastJustificationKeyFilter != null) lastJustificationRTA.removeEventFilter(KeyEvent.KEY_PRESSED, lastJustificationKeyFilter);
                severFromParent(lastJustificationRTA);
                hammerRTA(lastJustificationRTA);
            }
        } catch (Exception e) {}

        // Unhook Dynamic Listeners securely
        try {
            if (rtaKeyFilters != null) {
                for (Map.Entry<RichTextArea, EventHandler<KeyEvent>> entry : rtaKeyFilters.entrySet()) {
                    entry.getKey().removeEventFilter(KeyEvent.KEY_PRESSED, entry.getValue());
                }
                rtaKeyFilters.clear();
            }
        } catch (Exception e) {}

        try {
            if (rtaContentFocusListeners != null) {
                for (Map.Entry<RichTextArea, ChangeListener<Boolean>> entry : rtaContentFocusListeners.entrySet()) {
                    entry.getKey().focusedProperty().removeListener(entry.getValue());
                }
                rtaContentFocusListeners.clear();
            }
        } catch (Exception e) {}

        try {
            if (flowFocusListeners != null) {
                for (Map.Entry<TextFlow, ChangeListener<Boolean>> entry : flowFocusListeners.entrySet()) {
                    entry.getKey().focusedProperty().removeListener(entry.getValue());
                }
                flowFocusListeners.clear();
            }
        } catch (Exception e) {}

        try {
            if (flowClickHandlers != null) {
                for (Map.Entry<TextFlow, EventHandler<MouseEvent>> entry : flowClickHandlers.entrySet()) {
                    entry.getKey().setOnMouseClicked(null);
                }
                flowClickHandlers.clear();
            }
        } catch (Exception e) {}

        // CRITICAL FIX: Clean up the temporary wrapper DRTA to prevent toolbar leaks!
        try {
            if (lastJustificationDRTA != null) {
                lastJustificationDRTA.cleanup();
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (for static RTAs) <---
        // (Completely removed from Controller per Rule 1. Document injection and skin removal deferred entirely to View.)
        // Note: Dynamic RTAs in viewLines were ALREADY skin hammered during severAllViewLines() calling their view cleanup.

        // Clear OS-level popups
        try { clearStandingPopups(); } catch (Exception e) {}

        // Cascade to sub-components
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (derivationView != null) derivationView.cleanup(); } catch (Exception e) {}
        try { if (derivationCheck != null) derivationCheck.cleanup(); } catch (Exception e) {}
        try { if (derivationHelp != null) derivationHelp.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-Destructive Data Nullification
        try { this.undoRedoList = null; } catch (Exception e) {}
        try { this.theorems = null; } catch (Exception e) {}

        // Rule 1: Final Class Nullifications
        this.derivationModel = null;
        this.mainWindow = null;
        this.mainView = null;
        this.derivationView = null;
        this.derivationCheck = null;
        this.derivationHelp = null;
        this.lastJustificationDRTA = null;
        this.lastJustificationRTA = null;
        this.auxExerciseA = null;
        this.auxExerciseB = null;
        this.tStage = null;
        this.thumbPane = null;
        this.tScene = null;
        this.labelFont = null;

        this.rtaKeyFilters = null;
        this.rtaContentFocusListeners = null;
        this.flowFocusListeners = null;
        this.flowClickHandlers = null;

        this.pointsEarnedListener = null;
        this.statementFocusListener = null;
        this.commentFullHeightListener = null;
        this.commentModifiedListener = null;
        this.commentFocusListener = null;
        this.splitPaneDividerListener = null;
        this.insertLineHandler = null;
        this.deleteLineHandler = null;
        this.indentHandler = null;
        this.outdentHandler = null;
        this.addShelfHandler = null;
        this.addGapHandler = null;
        this.insertSubHandler = null;
        this.insertSubsHandler = null;
        this.undoHandler = null;
        this.redoHandler = null;
        this.showMetaLangHandler = null;
        this.thumbPaneClickHandler = null;
        this.justificationClickFilter = null;
        this.rtaFocusListener = null;
        this.lastJustificationKeyFilter = null;
    }



    MainWindow getMainWindow() { return mainWindow; }

    public RichTextArea getLastJustificationRTA() { return lastJustificationRTA; }

    public int getLastJustificationRow() { return lastJustificationRow; }

    public List<Theorem> getTheorems() { return theorems; }

    public DerivationCheck getDerivationCheck() { return derivationCheck; }

    @Override
    public Node getAuxExerNode() {
        if (derivationView == null) return new Pane();
        return derivationView.getContentSplitPane();
    }

    @Override
    public String getAuxExerName() {
        if (derivationModel == null) return "";
        return derivationModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) { auxExerciseA = exer; }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}