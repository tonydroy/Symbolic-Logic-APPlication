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

package slapp.editor.derivation_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.derivation.*;
import slapp.editor.derivation.theorems.*;
import slapp.editor.main_window.*;
import slapp.editor.vertical_tree.VerticalTreeExercise;
import slapp.editor.vertical_tree.VerticalTreeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Derivation Explain Exercise
 */
public class DrvtnExpExercise implements Exercise<DrvtnExpModel, DrvtnExpView> {
    private MainWindow mainWindow;
    private DrvtnExpModel drvtnExpModel;
    private DrvtnExpView drvtnExpView;
    private MainWindowView mainView;
    private DrvtnExpCheck drvtnExpCheck;
    private DrvtnExpHelp drvtnExpHelp;
    private boolean exerciseModified = false;
    private Font labelFont = new Font("Noto Serif Combo", 11);
    private boolean editJustification;
    private EventHandler justificationClickFilter;
    private RichTextArea lastJustificationRTA;
    private int lastJustificationRow;
    private UndoRedoList<DrvtnExpModel> undoRedoList = new UndoRedoList<>(20);
    private List<Theorem> theorems = new ArrayList<>();

    /**
     * Construct derivation explain exercise from model
     * @param model the model
     * @param mainWindow the main window
     */
    public DrvtnExpExercise(DrvtnExpModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.drvtnExpModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        if (drvtnExpModel.getCheckSetup() == null)
            drvtnExpModel.setCheckSetup(new CheckSetup());  //in case SLAPP v2 model
        List<ThrmSetElement> thrmSetElements = drvtnExpModel.getCheckSetup().getThrmSetElements();
        for (ThrmSetElement thrmSetElement : thrmSetElements) {
            switch (thrmSetElement.getType()) {
                case ZERO_INPUT:
                    theorems.add(new ZeroInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms()));
                    break;
                case ZERO_INPUT2:
                    theorems.add(new ZeroInputTheorem2(thrmSetElement.getName(), thrmSetElement.getForms()));
                    break;
                case SINGLE_INPUT:
                    theorems.add(new SingleInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms()));
                    break;
                case DOUBLE_INPUT:
                    theorems.add(new DoubleInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms()));
                    break;
                case TRIPLE_INPUT:
                    theorems.add(new TripleInputTheorem(thrmSetElement.getName(), thrmSetElement.getForms()));
                    break;
                case ADQ_A4:
                    theorems.add(new ADq_A4(thrmSetElement.getName(), ""));
                    break;
                case ADQ_A5:
                    theorems.add(new ADq_A5(thrmSetElement.getName(), ""));
                    break;
                case ADQT3_28:
                    theorems.add(new ADqT3_28(thrmSetElement.getName(), ""));
                    break;
                case ADQT3_29:
                    theorems.add(new ADqT3_29(thrmSetElement.getName(), ""));
                    break;
                case ADQT3_30:
                    theorems.add(new ADqT3_30(thrmSetElement.getName(), ""));
                    break;
                case ADQT3_31:
                    theorems.add(new ADqT3_31(thrmSetElement.getName(), ""));
                    break;
                case ADQT3_32:
                    theorems.add(new QND_T51(thrmSetElement.getName(), ""));
                    break;
                case AD_A7:
                    theorems.add(new AD_A7(thrmSetElement.getName(), ""));
                    break;
                case AD_A8:
                    theorems.add(new AD_A8(thrmSetElement.getName(), ""));
                    break;
                case ADT3_37:
                    theorems.add(new ADT3_37(thrmSetElement.getName(), ""));
                    break;
                case ADT3_38:
                    theorems.add(new ADT3_38(thrmSetElement.getName(), ""));
                    break;
                case QND_T51:
                    theorems.add(new QND_T51(thrmSetElement.getName(), ""));
                    break;
                case PANDT13_11i:
                    theorems.add(new PANDT13_11i(thrmSetElement.getName(), ""));
            }
        }

        this.mainView = mainWindow.getMainView();
        this.drvtnExpView = new DrvtnExpView(mainView);
        setDrvtnExpView();
        drvtnExpCheck = new DrvtnExpCheck(this);
        drvtnExpHelp = new DrvtnExpHelp(this);


        //cannot depend on pushUndoRedo because documents can't yet be extracted from view
        DrvtnExpModel deepCopy = (DrvtnExpModel) SerializationUtils.clone(drvtnExpModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
 //       pushUndoRedo();
    }

    /*
     * Set up the derivation view from the model
     */
    private void setDrvtnExpView() {

        drvtnExpView.setContentPrompt(drvtnExpModel.getContentPrompt());
        drvtnExpView.setLeftmostScopeLine(drvtnExpModel.isLeftmostScopeLine());
        drvtnExpView.setKeyboardSelector(drvtnExpModel.getKeyboardSelector());
        drvtnExpView.setStatementPrefHeight(drvtnExpModel.getStatementPrefHeight());
        drvtnExpView.setCommentPrefHeight(drvtnExpModel.getCommentPrefHeight());
        drvtnExpView.setExplanationPrefHeight(drvtnExpModel.getExplanationPrefHeight());
        drvtnExpView.setSplitPanePrefWidth(drvtnExpModel.getSplitPanePrefWidth());
        drvtnExpView.setShowMetaLang(drvtnExpModel.getCheckSetup().isShowMetalanguageButton());

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(drvtnExpModel.getExerciseStatement()).execute(new ActionEvent());

        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        drvtnExpView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(drvtnExpModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            drvtnExpModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        drvtnExpView.setExerciseComment(commentDRTA);

        //explain
        DecoratedRTA explanationDRTA = new DecoratedRTA();
        RichTextArea explanationEditor = explanationDRTA.getEditor();
        explanationEditor.getActionFactory().open(drvtnExpModel.getExplanationDocument()).execute(new ActionEvent());

        explanationEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double explanationTextHeight = mainView.getRTATextHeight(explanationEditor);
            drvtnExpModel.setExplanationTextHeight(explanationTextHeight);
        });

        explanationEditor.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(explanationDRTA, ControlType.AREA);
            }
        });
        drvtnExpView.setExplanationDRTA(explanationDRTA);

        //buttons
        drvtnExpView.getInsertLineButton().setOnAction(e -> insertLineAction());
        drvtnExpView.getDeleteLineButton().setOnAction(e -> deleteLineAction());
        drvtnExpView.getIndentButton().setOnAction(e -> indentLineAction());
        drvtnExpView.getOutdentButton().setOnAction(e -> outdentLineAction());
        drvtnExpView.getAddShelfButton().setOnAction(e -> addShelfLineAction());
        drvtnExpView.getAddGapButton().setOnAction(e -> addGapLineAction());
        drvtnExpView.getInsertSubButton().setOnAction(e -> insertSubAction());
        drvtnExpView.getInsertSubsButton().setOnAction(e -> insertSubsAction());
        drvtnExpView.getUndoButton().setOnAction(e -> undoAction());
        drvtnExpView.getRedoButton().setOnAction(e -> redoAction());


        //cleanup
        drvtnExpView.initializeViewDetails();
        drvtnExpView.getSplitPane().setDividerPosition(0, drvtnExpModel.getGridWidth());
        drvtnExpView.getSplitPane().getDividers().get(0).positionProperty().addListener((ob, ov, nv) -> {
            double diff = (double) nv - (double) ov;
            if (Math.abs(diff) >= .07) exerciseModified = true;
        });

        setViewLinesFromModel();
        drvtnExpView.setGridFromViewLines();
        setContentFocusListeners();
        drvtnExpView.setRightControlBox();

        //see comment in DerivationExericise to edit metalanguage help
        drvtnExpView.getShowMetaLangButton().setOnAction(e -> {
            drvtnExpView.showMetalanguageHelp(mainWindow.getSlappData().getMetalanguageHelp());
        });
    }

    public void clearStandingPopups() {
        if (drvtnExpView.getStaticHelpStage() != null) drvtnExpView.getStaticHelpStage().close();
        if (drvtnExpView.getMetaLangStage() != null) drvtnExpView.getMetaLangStage().close();
        drvtnExpHelp.closeHelpWindows();
    }

    /*
     * Set the view lines
     */
    private void setViewLinesFromModel() {

        List<ModelLine> modelLines = drvtnExpModel.getDerivationLines();
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
                rta.getActionFactory().open(modelLine.getLineContentDoc()).execute(new ActionEvent());
                rta.getActionFactory().saveNow().execute(new ActionEvent());


                if (LineType.isSetupLine(lineType)) {
                    rta.setEditable(false);
                }

                rta.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    KeyCode code = e.getCode();
                    int row = drvtnExpView.getGrid().getRowIndex(rta.getParent());

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
                });
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
        drvtnExpView.setViewLines(viewLines);
    }

    /*
     * Add focus listener to RTA fields
     */
    private void setContentFocusListeners() {
        List<ViewLine> viewLines = drvtnExpView.getViewLines();
        for (ViewLine viewLine : viewLines) {
            if (LineType.isContentLine(viewLine.getLineType())) {
                BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                DecoratedRTA drta = bdrta.getDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                mainView.editorInFocus(drta, ControlType.FIELD);
                rta.focusedProperty().addListener((o, ov, nv) -> {
                    if (nv) {
                        mainView.editorInFocus(drta, ControlType.FIELD);
                    } else {
                        if (rta.isModified()) {
                            pushUndoRedo();
                            exerciseModified = true;
                        }
                    }
                });
            }
        }
    }

    //line below, or current if row is the last
    public ViewLine getContentLineBelow(int row) {
        ViewLine line = drvtnExpView.getViewLines().get(row);
//        ViewLine line = null;
        row++;
        for (int i = row; i < drvtnExpView.getViewLines().size(); i++) {
            ViewLine temp = drvtnExpView.getViewLines().get(i);
            if (LineType.isContentLine(temp.getLineType())) {
                line = temp;
                break;
            }
        }
        return line;
    }

    //line above, or current if row is the first
    public ViewLine getContentLineAbove(int row) {
        ViewLine line = drvtnExpView.getViewLines().get(row);
//        ViewLine line = null;
        row--;
        for (int i = row; i >= 0; i--) {
            ViewLine temp = drvtnExpView.getViewLines().get(i);
            if (LineType.isContentLine(temp.getLineType())) {
                line = temp;
                break;
            }
        }
        return line;
    }

    /*
     * Get a text flow from justification string.  The flow binds number values to numbered lines
     * @param justificationString the justification string
     * @param viewLines the collection of view lines
     * @return the text flow
     */
    private TextFlow getJustificationFlow(String justificationString, List<ViewLine> viewLines) {
        justificationString = justificationString.trim();
        TextFlow flow = getStyledJustificationFlow(new TextFlow());

        if (!justificationString.isEmpty()) {

            //get List of alternating digit and non-digit sequences
            List<String> splitList = new ArrayList();
            boolean startsDigit = false;
            if (charIsDigit(justificationString.charAt(0))) startsDigit = true;
            StringBuilder builder = new StringBuilder();
            boolean buildingDigit = startsDigit;

            int j = 0;
            while (j < justificationString.length()) {
                if (charIsDigit(justificationString.charAt(j)) == buildingDigit) {
                    builder.append(justificationString.charAt(j));
                    j++;
                }
                else {
                    splitList.add(builder.toString());
                    builder.delete(0, builder.length());
                    buildingDigit = charIsDigit(justificationString.charAt(j));
                }
            }
            splitList.add(builder.toString());

            //get flow of labels and texts with labels bound to line numbers
            boolean buildingNum = startsDigit;
            for (int i = 0; i < splitList.size(); i++) {
                if (buildingNum) {
                    Label label = new Label(splitList.get(i));
                    label.setFont(labelFont);

                    for (int k = 0; k < viewLines.size(); k++) {
                        ViewLine line = viewLines.get(k);
                        if (line.getLineNumberLabel() != null) {
                            String lineLabel = line.getLineNumberLabel().getText();
                            if ((!lineLabel.isEmpty()) && (lineLabel.equals(label.getText()))) {
                                label.textProperty().bind(line.getLineNumberLabel().textProperty());
                                line.getClientLabels().add(label);
                                break;
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

    /*
     * Set features for a justification text flow
     * @param flow the input text flow
     * @return the resultant text flow
     */
    TextFlow getStyledJustificationFlow(TextFlow flow) {
        flow.setFocusTraversable(true);
        flow.setMouseTransparent(false);
        flow.setMinWidth(110);
//        flow.setMaxWidth(100);
        flow.setMaxHeight(20);
        flow.setPadding(new Insets(0,0,0,5));
        flow.setOnMouseClicked(e -> flow.requestFocus());

        flow.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                editJustificationField(flow);
            }
        });
        return flow;
    }

    /*
     * Convert text flow to RTA to enable editing the field
     * @param flow the text flow
     */
    private void editJustificationField(TextFlow flow) {
        int rowIndex = drvtnExpView.getGrid().getRowIndex(flow);
        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea rta = drta.getEditor();
        rta.setContentAreaWidth(200);
        rta.setPrefHeight(20);
        rta.setPrefWidth(100);
        rta.getStylesheets().add("slappDerivation.css");
        rta.getActionFactory().open(new Document(getStringFromJustificationFlow(flow))).execute(new ActionEvent());
        rta.getActionFactory().saveNow().execute(new ActionEvent());

        if (drvtnExpView.getViewLines().get(rowIndex).getLineType() == LineType.PREMISE_LINE) rta.setEditable(false);
        lastJustificationRTA = rta;
        lastJustificationRow = rowIndex;
        rta.applyCss();
        rta.layout();

        justificationClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean clickOther = !inHierarchy(event.getPickResult().getIntersectedNode(), rta);
                boolean clickParaToolbarBox = inHierarchy(event.getPickResult().getIntersectedNode(), mainWindow.getMainView().getParaToolbarBox());
                boolean clickFontsAndEditBox = inHierarchy(event.getPickResult().getIntersectedNode(), mainWindow.getMainView().getFontsAndEditBox());
                boolean clickKbdSel = inHierarchy(event.getPickResult().getIntersectedNode(), drta.getKeyboardSelector());
                boolean clickUnicode = inHierarchy(event.getPickResult().getIntersectedNode(), drta.getUnicodeField());

                boolean clickRestricted = clickParaToolbarBox || clickFontsAndEditBox || clickKbdSel || clickUnicode ;

                if (clickOther && !clickRestricted) {
                    if (editJustification) {
                        editJustification = false;
                        saveJustificationRTA(rta, rowIndex);
                    }
                }
            }
        };

        ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv) {
                    editJustification = true;
                    mainView.getMainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, justificationClickFilter);
                }
                else {
                    Node focusOwner = mainView.getMainScene().getFocusOwner();
                    boolean focusUnicode = focusOwner == drta.getUnicodeField();
                    boolean focusKbdDia = focusOwner == drta.getKeyboardDiagramButton();

                    boolean restrictedFocus = focusUnicode || focusKbdDia ;

                    if (editJustification && !restrictedFocus ) {
                        editJustification = false;
                        saveJustificationRTA(rta, rowIndex);
                    }
                }
            }
        };
        rta.focusedProperty().addListener(focusListener);

        rta.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();

            int row = drvtnExpView.getGrid().getRowIndex(rta);

            if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                drvtnExpView.setGridFromViewLines();
                ViewLine contentLineBelow = getContentLineBelow(row);
                if (contentLineBelow != null) contentLineBelow.getLineContentBoxedDRTA().getRTA().requestFocus();
                e.consume();
            } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                drvtnExpView.setGridFromViewLines();
                ViewLine currentLine = drvtnExpView.getViewLines().get(row);
                currentLine.getLineContentBoxedDRTA().getRTA().requestFocus();
                e.consume();
            } else if (code == KeyCode.UP ) {
                drvtnExpView.setGridFromViewLines();
                ViewLine contentLineAbove = getContentLineAbove(row);
                if (contentLineAbove != null) contentLineAbove.getJustificationFlow().requestFocus();
                e.consume();
            } else if (code == KeyCode.DOWN ) {
                drvtnExpView.setGridFromViewLines();
                ViewLine contentLineBelow = getContentLineBelow(row);
                if (contentLineBelow != null)  contentLineBelow.getJustificationFlow().requestFocus();
                e.consume();
            }

        });

        drvtnExpView.getGrid().getChildren().remove(flow);
        drvtnExpView.getGrid().add(rta, 22, rowIndex);
        rta.requestFocus();
        mainView.editorInFocus(drta, ControlType.JUSTIFICATION);
    }

    /*
     * Determine whether one node is an ancestor of another
     * @param node the potentential descendant
     * @param potentialHierarchyElement the potentential ancestor
     * @return true if the second is an ancestor of the first
     */
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

    /*
     * Save text from justification RTA into text flow, and place on grid
     * @param rta the justification RTA
     * @param rowIndex the row index of the justification field
     */
    private void saveJustificationRTA(RichTextArea rta, int rowIndex) {

        mainView.getMainScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, justificationClickFilter);

        rta.setEditable(true);
        boolean modified = rta.isModified();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        String justificationString = rta.getDocument().getText();

        TextFlow justificationFlow = getJustificationFlow(justificationString, drvtnExpView.getViewLines());
        drvtnExpView.getViewLines().get(rowIndex).setJustificationFlow(justificationFlow);

        rta.getActionFactory().newDocumentNow().execute(new ActionEvent());
        drvtnExpView.setJustificationFlowOnGrid(rowIndex);
 //       drvtnExpView.setGridFromViewLines();

        if (modified) {
            pushUndoRedo();
            exerciseModified = true;
        }
    }

    /*
     * Extract text string from flow consisting of labels and texts
     * @param flow the text flow
     * @return the extracted string
     */
    public String getStringFromJustificationFlow(TextFlow flow) {
        StringBuilder sb = new StringBuilder();
        ObservableList<Node> list = flow.getChildren();
        for (Node node : flow.getChildren()) {
            if (node instanceof Label) sb.append(((Label) node).getText());
            else if (node instanceof Text) sb.append(((Text) node).getText());
        }
        return sb.toString();
    }

    public List<String> getLineLabelsFromJustificationFlow(TextFlow flow) {
        List labelList = new ArrayList();
        ObservableList<Node> list = flow.getChildren();
        for (Node node : flow.getChildren()) {
            if (node instanceof Label) labelList.add(((Label) node).getText());
        }
        return labelList;
    }

    /*
     * Identify digit (or point) character
     * @param character the character
     * @return true if the character is a digit and otherwise false
     */
    private boolean charIsDigit(char character) {
        boolean result = false;
        if (('0' <= character && character <= '9') || character == '.') result = true;
        return result;
    }

    /*
     * Create and add empty view line at supplied row position, pushing others down
     * @param newRow the new row position
     * @param depth scope depth of the line
     */
    private void addEmptyViewContentRow(int newRow, int depth) {
        Label numLabel = new Label();
        numLabel.setFont(labelFont);

        BoxedDRTA bdrta = new BoxedDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.getStylesheets().add("slappDerivation.css");

        rta.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            int row = drvtnExpView.getGrid().getRowIndex(rta.getParent());

            if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                drvtnExpView.getViewLines().get(row).getJustificationFlow().requestFocus();
                e.consume();
            } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                if (getContentLineAbove(row) != null) getContentLineAbove(row).getJustificationFlow().requestFocus();
                e.consume();
            } else if (code == KeyCode.UP ) {
                if (getContentLineAbove(row) != null) getContentLineAbove(row).getLineContentBoxedDRTA().getRTA().requestFocus();
                e.consume();
            } else if (code == KeyCode.DOWN) {
                if (getContentLineBelow(row) != null) getContentLineBelow(row).getLineContentBoxedDRTA().getRTA().requestFocus();
                e.consume();
            }
        });

        DecoratedRTA drta = bdrta.getDRTA();
        mainView.editorInFocus(drta, ControlType.FIELD);
        rta.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(drta, ControlType.FIELD);
            } else {
                if (rta.isModified()) {
                    pushUndoRedo();
                    exerciseModified = true;
                }
            }
        });

        TextFlow flow = new TextFlow();
        TextFlow justificationFlow = getStyledJustificationFlow(flow);
        ViewLine viewLine = new ViewLine(numLabel, depth, LineType.MAIN_CONTENT_LINE, bdrta, justificationFlow, new ArrayList<Label>());

        drvtnExpView.getViewLines().add(newRow, viewLine);
    }

    /*
     * Perform undo action
     */
    private void undoAction() {
        DrvtnExpModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            drvtnExpModel = (DrvtnExpModel) SerializationUtils.clone(undoElement);
            setViewLinesFromModel();
            drvtnExpView.setGridFromViewLines();
            updateUndoRedoButtons();
            setContentFocusListeners();

        }
    }

    /*
     * Perform redo action
     */
    private void redoAction() {
        DrvtnExpModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            drvtnExpModel = (DrvtnExpModel) SerializationUtils.clone(redoElement);
            setViewLinesFromModel();
            drvtnExpView.setGridFromViewLines();
            updateUndoRedoButtons();
            setContentFocusListeners();

        }
    }

    /*
     * Enable/Disable undo and redo buttons depending on current state
     */
    private void updateUndoRedoButtons() {
        drvtnExpView.getUndoButton().setDisable(!undoRedoList.canUndo());
        drvtnExpView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    /*
     * Push current state onto undo/redo stack
     */
    private void pushUndoRedo() {
        DrvtnExpModel model = getDrvtnExpModelFromView();
        DrvtnExpModel deepCopy = (DrvtnExpModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }

    /*
     * Insert new line at current position
     */
    private void insertLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;

        if (row >= 0) {
            ViewLine viewLine = drvtnExpView.getViewLines().get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType()) || viewLine.getLineType() == LineType.CONCLUSION_LINE) {
                addEmptyViewContentRow(row, depth);
                drvtnExpView.setGridFromViewLines();
                pushUndoRedo();

                BoxedDRTA bdrta = drvtnExpView.getViewLines().get(row).getLineContentBoxedDRTA();
                DecoratedRTA drta = bdrta.getDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.applyCss();
                rta.layout();
                exerciseModified = true;
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup lines.");
            }
        }
        else {
            EditorAlerts.fleetingRedPopup("Select derivation row for insert above.");
        }
    }

    /*
     * Delete the line at the current position
     */
    private void deleteLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        List<ViewLine> viewLines = drvtnExpView.getViewLines();
        if (row >= 0 && row < viewLines.size()) {
            if (viewLines.size() > 1) {
                if (!LineType.isSetupLine(viewLines.get(row).getLineType())) {
                    List<Label> clients = viewLines.get(row).getClientLabels();
                    for (Label label : clients) {
                        label.textProperty().unbind();
                        label.setText("??");
                    }
                    viewLines.remove(row);
                    exerciseModified = true;

                    if (row < viewLines.size()) {
                        LineType type = viewLines.get(row).getLineType();
                        if (LineType.isShelfLine(type) || LineType.isGapLine(type)) viewLines.remove(row);
                    }

                    drvtnExpView.setGridFromViewLines();

                    pushUndoRedo();

                    for (int i = row; i < viewLines.size(); i++) {
                        if (viewLines.get(i).getLineContentBoxedDRTA() != null) {
                            viewLines.get(i).getLineContentBoxedDRTA().getRTA().requestFocus();
                            break;
                        }
                    }
                }
                else {
                    EditorAlerts.fleetingRedPopup("Cannot modify setup line.");
                }
            }
            else {
                EditorAlerts.fleetingRedPopup("A derivation must contain at least one line.");
            }
        }
        else {
            EditorAlerts.fleetingRedPopup("Select derivation line to delete.");
        }
    }

    /*
     * Increase scope depth by one
     */
    private void indentLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        if (row >= 0) {
            List<ViewLine> viewLines = drvtnExpView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                int depth = viewLine.getDepth();
                if (depth < 19) {
                    viewLine.setDepth(++depth);
                    viewLines.set(row, viewLine);
                    if (++row < viewLines.size()) {
                        ViewLine nextLine = viewLines.get(row);
                        if (LineType.isShelfLine(nextLine.getLineType())  || LineType.isGapLine(nextLine.getLineType())) {
                            nextLine.setDepth(depth);
                            viewLines.set(row, nextLine);
                        }
                    }
                    drvtnExpView.setGridFromViewLines();
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

    /*
     * Decrease scope depth by one
     */
    private void outdentLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        if (row >= 0) {
            List<ViewLine> viewLines = drvtnExpView.getViewLines();
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
                                viewLines.remove(row);
                            }
                        }
                    }
                    drvtnExpView.setGridFromViewLines();
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

    /*
     * Add premise/assumption shelf under current line
     */
    private void addShelfLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        if (row >= 0) {
            List<ViewLine> viewLines = drvtnExpView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                if (++row < viewLines.size()) {
                    if (!(LineType.isShelfLine(viewLines.get(row).getLineType()) || LineType.isGapLine(viewLines.get(row).getLineType()))) {
                        ViewLine shelfLine = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                        viewLines.add(row, shelfLine);
                        viewLine.getLineContentBoxedDRTA().getRTA().requestFocus();
                        drvtnExpView.setGridFromViewLines();
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

    /*
     * add gap between content lines at current depth
     */
    private void addGapLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        if (row >= 0) {
            List<ViewLine> viewLines = drvtnExpView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                if (depth > 1) {
                    if (++row < viewLines.size()) {
                        if (!(LineType.isShelfLine(viewLines.get(row).getLineType())|| LineType.isGapLine(viewLines.get(row).getLineType()))) {
                            ViewLine gapLine = new ViewLine(null, depth, LineType.GAP_LINE, null, null, null);
                            viewLines.add(row, gapLine);
                            viewLine.getLineContentBoxedDRTA().getRTA().requestFocus();
                            drvtnExpView.setGridFromViewLines();
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

    /*
     * Insert lines for a subderivation above current line
     */
    private void insertSubAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        if (row >= 0) {
            ViewLine viewLine = drvtnExpView.getViewLines().get(row);
            int depth = viewLine.getDepth();
            depth++;
            if (!LineType.isSetupLine(viewLine.getLineType()) || viewLine.getLineType() == LineType.CONCLUSION_LINE) {
                addEmptyViewContentRow(row, depth);
                addEmptyViewContentRow(row, depth);
                if (drvtnExpModel.isDefaultShelf()) {
                    ViewLine shelfLine = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                    drvtnExpView.getViewLines().add(row, shelfLine);
                }
                addEmptyViewContentRow(row, depth);

                drvtnExpView.setGridFromViewLines();
                pushUndoRedo();
                exerciseModified = true;


            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup lines.");
            }
        }
        else {
            EditorAlerts.fleetingRedPopup("Select derivation row for insert above.");
        }
    }

    /*
     * Add lines for a pair of subderivations above current line
     */
    private void insertSubsAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent())) row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        if (row >= 0) {
            ViewLine viewLine = drvtnExpView.getViewLines().get(row);
            int depth = viewLine.getDepth();
            depth++;
            if (!LineType.isSetupLine(viewLine.getLineType()) || viewLine.getLineType() == LineType.CONCLUSION_LINE) {
                addEmptyViewContentRow(row, depth);
                addEmptyViewContentRow(row, depth);
                if (drvtnExpModel.isDefaultShelf()) {
                    ViewLine shelfLine1 = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                    drvtnExpView.getViewLines().add(row, shelfLine1);
                }
                addEmptyViewContentRow(row, depth);

                ViewLine gapLine = new ViewLine(null, depth, LineType.GAP_LINE,  null, null, null);
                drvtnExpView.getViewLines().add(row, gapLine);

                addEmptyViewContentRow(row, depth);
                addEmptyViewContentRow(row, depth);
                if (drvtnExpModel.isDefaultShelf()) {
                    ViewLine shelfLine2 = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                    drvtnExpView.getViewLines().add(row, shelfLine2);
                }
                addEmptyViewContentRow(row, depth);
                drvtnExpView.setGridFromViewLines();
                pushUndoRedo();
                exerciseModified = true;


            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup lines.");
            }
        }
        else {
            EditorAlerts.fleetingRedPopup("Select derivation row for insert above.");
        }
    }

    public int currentRow() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();

        if (lastFocusedNode != null) {
            if (drvtnExpView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
                row = drvtnExpView.getGrid().getRowIndex(lastFocusedNode.getParent());
            else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        }
        return row;
    }

    public DrvtnExpHelp getDrvtnExpHelp() {
        return drvtnExpHelp;
    }



    /**
     * The derivation model
     * @return the model
     */
    @Override
    public DrvtnExpModel getExerciseModel() { return drvtnExpModel; }

    /**
     * The derivation view
     * @return the view
     */
    @Override
    public DrvtnExpView getExerciseView() { return drvtnExpView; }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        Platform.runLater(() -> {
            boolean success = DiskUtilities.saveExercise(saveAs, getDrvtnExpModelFromView());
            if (success) exerciseModified = false;
        });
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        DrvtnExpExercise printExercise = this;
        DrvtnExpModel printModel = drvtnExpModel;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
        Label exerciseName = new Label(drvtnExpModel.getExerciseName());
        exerciseName.setStyle("-fx-font-weight: bold;");
        Region spacer = new Region();

        HBox hbox = new HBox(exerciseName, spacer, printCheckNode());
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
        double statementHeight = printModel.getStatementTextHeight();
        statementRTA.setPrefHeight(statementHeight + 35.0);
        statementRTA.setContentAreaWidth(nodeWidth);
        statementRTA.setMinWidth(nodeWidth);
        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        //content node
        GridPane derivationPane = printExercise.getExerciseView().getGrid();
        derivationPane.setPadding(new Insets(15,0,15,0));

        double splitPaneWidth = Math.max(nodeWidth, printModel.getSplitPanePrefWidth());
        double width = printModel.getGridWidth() * splitPaneWidth;
        derivationPane.setMaxWidth(width);
        derivationPane.setMinWidth(width);
        HBox gridBox = new HBox(derivationPane);
        gridBox.setAlignment(Pos.CENTER);
        nodeList.add(gridBox);

        RichTextArea explanationRTA = printExercise.getExerciseView().getExplanationDRTA().getEditor();
        explanationRTA.prefHeightProperty().unbind();
        explanationRTA.minWidthProperty().unbind();
        double explanationHeight = printModel.getExplanationTextHeight();
        explanationRTA.setPrefHeight(explanationHeight + 35.0);
        explanationRTA.setContentAreaWidth(nodeWidth);
        explanationRTA.setMinWidth(nodeWidth);
        explanationRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(explanationRTA);

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
        commentRTA.setPrefHeight(printModel.getCommentTextHeight() + 35.0);
        commentRTA.setContentAreaWidth(nodeWidth);
        commentRTA.setMinWidth(nodeWidth);
        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(commentRTA);

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (drvtnExpCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + drvtnExpView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (drvtnExpCheck.isCheckFinal()) {
                bigCheck.setFill(Color.LAWNGREEN);
                message.setFill(Color.GREEN);
            }
            else {
                bigCheck.setFill(Color.ORCHID);
                message.setFill(Color.PURPLE);
            }
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }


    /**
     * Return to the initial (unworked) version of the exercise, retaining the comment only.
     * @return the initial exercise
     */
    @Override
    public Exercise<DrvtnExpModel, DrvtnExpView> resetExercise() {
        RichTextArea commentRTA = drvtnExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        DrvtnExpModel originalModel = (DrvtnExpModel) (drvtnExpModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);

        CheckSetup setup = originalModel.getCheckSetup();
        setup.setHelpTries(drvtnExpCheck.getHelpTries());
        setup.setCheckTries(drvtnExpCheck.getCheckTries());
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        DrvtnExpExercise clearExercise = new DrvtnExpExercise(originalModel, mainWindow);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = drvtnExpView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) exerciseModified = true;

        RichTextArea explanationEditor = drvtnExpView.getExplanationDRTA().getEditor();
        if (explanationEditor.isModified()) exerciseModified = true;

        List<ViewLine> viewLines = drvtnExpView.getViewLines();
        for (ViewLine viewLine : viewLines) {
            if (LineType.isContentLine(viewLine.getLineType())) {
                RichTextArea rta = viewLine.getLineContentBoxedDRTA().getRTA();
                if (rta.isModified()) exerciseModified = true;
            }
        }
        return exerciseModified;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @param modified true if exercise is modified, and otherwise false
     */
    @Override
    public void setExerciseModified(boolean modified) { this.exerciseModified = modified; }

    /**
     * There is no Derivation Explain free form option
     * @return null
     */
    @Override
    public Node getFFViewNode() {return null;}

    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}

    /**
     * There is no Derivation Explain free form option
     * @return null
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel<DrvtnExpModel> getExerciseModelFromView() {
        return (ExerciseModel) getDrvtnExpModelFromView();
    }

    /*
     * Extract the derivation explain model from view
     * @return the model
     */
    private DrvtnExpModel getDrvtnExpModelFromView() {
        String name = drvtnExpModel.getExerciseName();
        String prompt = drvtnExpView.getContentPrompt();
        Boolean started = (drvtnExpModel.isStarted() || exerciseModified);

        Document statementDocument = drvtnExpModel.getExerciseStatement();
        double statementHeight = drvtnExpView.getExerciseStatement().getEditor().getPrefHeight();

        RichTextArea commentRTA = drvtnExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        RichTextArea explanationRTA = drvtnExpView.getExplanationDRTA().getEditor();
        explanationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document explanationDocument = explanationRTA.getDocument();

        double gridWidth = drvtnExpView.getSplitPane().getDividerPositions()[0];
        boolean leftmostScopeLine = drvtnExpModel.isLeftmostScopeLine();
        boolean defaultShelf = drvtnExpModel.isDefaultShelf();
        RichTextAreaSkin.KeyMapValue keyboardSelector = drvtnExpModel.getKeyboardSelector();

        List<ModelLine> modelLines = new ArrayList<>();
        List<ViewLine> viewLines = drvtnExpView.getViewLines();
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            int depth = viewLine.getDepth();

            LineType lineType = viewLine.getLineType();
            Document lineContentDocument = null;
            String justification = "";
            if (LineType.isContentLine(lineType)) {

                RichTextArea lineContentRTA = viewLine.getLineContentBoxedDRTA().getRTA();

                boolean editable = lineContentRTA.isEditable();
                lineContentRTA.setEditable(true);

                lineContentRTA.setDisable(true);
                lineContentRTA.getActionFactory().saveNow().execute(new ActionEvent());
                lineContentRTA.setDisable(false);

                lineContentDocument = lineContentRTA.getDocument();
                lineContentRTA.setEditable(editable);

                justification = getStringFromJustificationFlow(viewLine.getJustificationFlow());
            }

            ModelLine modelLine = new ModelLine(depth, lineContentDocument, justification, lineType);
            modelLines.add(modelLine);
        }


        DrvtnExpModel newModel = new DrvtnExpModel(name, started, statementHeight, gridWidth, prompt, leftmostScopeLine, defaultShelf, keyboardSelector, statementDocument, commentDocument, explanationDocument, modelLines);
        newModel.setOriginalModel(drvtnExpModel.getOriginalModel());
        newModel.setCommentPrefHeight(drvtnExpView.getCommentPrefHeight());
        newModel.setExplanationPrefHeight(drvtnExpView.getExplanationPrefHeight());
        newModel.setSplitPanePrefWidth(drvtnExpView.getSplitPanePrefWidth());
        newModel.setCommentTextHeight(drvtnExpModel.getCommentTextHeight());
        newModel.setStatementTextHeight(drvtnExpModel.getStatementTextHeight());
        newModel.setExplanationTextHeight(drvtnExpModel.getExplanationTextHeight());

        CheckSetup setup = drvtnExpModel.getCheckSetup();
        setup.setCheckSuccess(drvtnExpCheck.isCheckSuccess());
        setup.setCheckFinal(drvtnExpCheck.isCheckFinal());
        setup.setCheckTries(drvtnExpCheck.getCheckTries());
        setup.setHelpTries(drvtnExpCheck.getHelpTries());
        newModel.setCheckSetup(setup);

        return newModel;
    }

    MainWindow getMainWindow() {
        return mainWindow;
    }

    public RichTextArea getLastJustificationRTA() {
        return lastJustificationRTA;
    }

    public int getLastJustificationRow() {
        return lastJustificationRow;
    }

    public List<Theorem> getTheorems() {
        return theorems;
    }

    public DrvtnExpCheck getDrvtnExpCheck() {
        return drvtnExpCheck;
    }


}
