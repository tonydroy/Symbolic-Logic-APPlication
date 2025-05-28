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
import javafx.stage.Stage;
import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.derivation.theorems.*;
import slapp.editor.main_window.*;

import java.util.ArrayList;
import java.util.List;

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
    private EventHandler justificationClickFilter;
    private RichTextArea lastJustificationRTA;
    private int lastJustificationRow;
    private UndoRedoList<DerivationModel> undoRedoList = new UndoRedoList<>(20);
    private List<Theorem> theorems = new ArrayList<>();


    /**
     * Construct derivation exercise from model
     *
     * @param model      the model
     * @param mainWindow the main window
     */
    public DerivationExercise(DerivationModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.derivationModel = model;
        if (model.getOriginalModel() == null) {
            model.setOriginalModel(model);
        }
        if (derivationModel.getCheckSetup() == null)
            derivationModel.setCheckSetup(new CheckSetup());  //in case SLAPP v2 model
        List<ThrmSetElement> thrmSetElements = derivationModel.getCheckSetup().getThrmSetElements();
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
        this.derivationView = new DerivationView(mainView);

        setDerivationView();
        derivationCheck = new DerivationCheck(this);
        derivationHelp = new DerivationHelp(this);


        //cannot depend on pushUndoRedo because documents can't yet be extracted from view
        DerivationModel deepCopy = (DerivationModel) SerializationUtils.clone(derivationModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }

    /*
     * Set up the derivation view from the model
     */
    private void setDerivationView() {

        derivationView.setLeftmostScopeLine(derivationModel.isLeftmostScopeLine());
        derivationView.setKeyboardSelector(derivationModel.getKeyboardSelector());
        derivationView.setStatementPrefHeight(derivationModel.getStatementPrefHeight());
        derivationView.setCommentPrefHeight(derivationModel.getCommentPrefHeight());
        derivationView.setSplitPanePrefWidth(derivationModel.getSplitPanePrefWidth());
        derivationView.setShowMetaLang(derivationModel.getCheckSetup().isShowMetalanguageButton());

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(derivationModel.getExerciseStatement()).execute(new ActionEvent());

        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        derivationView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            derivationModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.getActionFactory().open(derivationModel.getExerciseComment()).execute(new ActionEvent());

        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        derivationView.setExerciseComment(commentDRTA);

        //buttons
        derivationView.getInsertLineButton().setOnAction(e -> insertLineAction());
        derivationView.getDeleteLineButton().setOnAction(e -> deleteLineAction());
        derivationView.getIndentButton().setOnAction(e -> indentLineAction());
        derivationView.getOutdentButton().setOnAction(e -> outdentLineAction());
        derivationView.getAddShelfButton().setOnAction(e -> addShelfLineAction());
        derivationView.getAddGapButton().setOnAction(e -> addGapLineAction());
        derivationView.getInsertSubButton().setOnAction(e -> insertSubAction());
        derivationView.getInsertSubsButton().setOnAction(e -> insertSubsAction());
        derivationView.getUndoButton().setOnAction(e -> undoAction());
        derivationView.getRedoButton().setOnAction(e -> redoAction());

        //cleanup
        derivationView.initializeViewDetails();
        derivationView.getContentSplitPane().setDividerPosition(0, derivationModel.getGridWidth());
        derivationView.getContentSplitPane().getDividers().get(0).positionProperty().addListener((ob, ov, nv) -> {
            double diff = (double) nv - (double) ov;
            if (Math.abs(diff) >= .07) exerciseModified = true;
        });

        setViewLinesFromModel();
        derivationView.setGridFromViewLines();
        setContentFocusListeners();

        derivationView.setRightControlBox();

        /*
        This is a kludge.  Metalanguage help should work with the regular TextHelpPopup by the first commented line
        below.  But the JavaFX WebView puts an empty box after supplemental unicode characters
        (see https://bugs.openjdk.org/browse/JDK-8343963).  This is ugly.  So using an RTA and Document.

        To edit this document, uncomment the second group of lines below and open metalanguageHelp.sle.  The help Document opens
        into the comment field.  After edit there, press the metalanguage help button so that the Document is sent to
        SLAPPdata.  Close the program, re-comment the lines, and the revised file should open with the help button.
         */
        derivationView.getShowMetaLangButton().setOnAction(e -> {
            //   TextHelpPopup.helpMetalanguage();

/*
            derivationView.getExerciseComment().getEditor().getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = derivationView.getExerciseComment().getEditor().getDocument();
            mainWindow.getSlappData().setMetalanguageHelp(doc);
             */


            derivationView.showMetalanguageHelp(mainWindow.getSlappData().getMetalanguageHelp());
        });


    }

    public void clearStandingPopups() {
        if (derivationView.getStaticHelpStage() != null) derivationView.getStaticHelpStage().close();
        if (derivationView.getMetaLangStage() != null) derivationView.getMetaLangStage().close();
        derivationHelp.closeHelpWindows();
    }


    /*
     * Set the view lines
     */
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
                rta.getActionFactory().open(modelLine.getLineContentDoc()).execute(new ActionEvent());
                rta.getActionFactory().saveNow().execute(new ActionEvent());

                if (LineType.isSetupLine(lineType)) {
                    rta.setEditable(false);
                }

                rta.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    KeyCode code = e.getCode();
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
                        if (contentLineAbove != null)
                            contentLineAbove.getLineContentBoxedDRTA().getRTA().requestFocus();
                        e.consume();
                    } else if (code == KeyCode.DOWN) {
                        ViewLine contentLineBelow = getContentLineBelow(row);
                        if (contentLineBelow != null)
                            contentLineBelow.getLineContentBoxedDRTA().getRTA().requestFocus();
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
        derivationView.setViewLines(viewLines);
    }

    /*
     * Add focus listener to RTA fields
     */
    private void setContentFocusListeners() {
        List<ViewLine> viewLines = derivationView.getViewLines();
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

    /*
     * The content line after row if one exists and otherwise the one at row
     * @param row the row value
     * @return the view line
     */
    public ViewLine getContentLineBelow(int row) {
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

    /*
     * The content line prior to row if one exists, and otherwise the one at row
     * @param row the row value
     * @return the view line
     */
    public ViewLine getContentLineAbove(int row) {
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
                } else {
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
    private TextFlow getStyledJustificationFlow(TextFlow flow) {
        flow.setFocusTraversable(true);
        flow.setMouseTransparent(false);
        flow.setMinWidth(110);
        flow.setMaxHeight(20);
        flow.setPadding(new Insets(0, 0, 0, 5));
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
        int rowIndex = derivationView.getGrid().getRowIndex(flow);
        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea rta = drta.getEditor();
        rta.setContentAreaWidth(200);
        rta.setPrefHeight(20);
        rta.setPrefWidth(100);
        rta.getStylesheets().add("slappDerivation.css");
        rta.getActionFactory().open(new Document(getStringFromJustificationFlow(flow))).execute(new ActionEvent());
        rta.getActionFactory().saveNow().execute(new ActionEvent());

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
                boolean clickOther = !inHierarchy(event.getPickResult().getIntersectedNode(), rta);
                boolean clickParaToolbarBox = inHierarchy(event.getPickResult().getIntersectedNode(), mainWindow.getMainView().getParaToolbarBox());
                boolean clickFontsAndEditBox = inHierarchy(event.getPickResult().getIntersectedNode(), mainWindow.getMainView().getFontsAndEditBox());
                boolean clickKbdSel = inHierarchy(event.getPickResult().getIntersectedNode(), drta.getKeyboardSelector());
                boolean clickUnicode = inHierarchy(event.getPickResult().getIntersectedNode(), drta.getUnicodeField());

                boolean clickRestricted = clickParaToolbarBox || clickFontsAndEditBox || clickKbdSel || clickUnicode;

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
                } else {
                    Node focusOwner = mainView.getMainScene().getFocusOwner();
                    boolean focusUnicode = focusOwner == drta.getUnicodeField();
                    boolean focusKbdDia = focusOwner == drta.getKeyboardDiagramButton();

                    boolean restrictedFocus = focusUnicode || focusKbdDia;

                    if (editJustification && !restrictedFocus) {
                        editJustification = false;
                        saveJustificationRTA(rta, rowIndex);
                    }
                }
            }
        };
        rta.focusedProperty().addListener(focusListener);


        rta.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();

            int row = derivationView.getGrid().getRowIndex(rta);

            if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                derivationView.setGridFromViewLines();
                ViewLine contentLineBelow = getContentLineBelow(row);
                if (contentLineBelow != null) contentLineBelow.getLineContentBoxedDRTA().getRTA().requestFocus();
                e.consume();
            } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                derivationView.setGridFromViewLines();
                ViewLine currentLine = derivationView.getViewLines().get(row);
                currentLine.getLineContentBoxedDRTA().getRTA().requestFocus();
                e.consume();
            } else if (code == KeyCode.UP) {
                derivationView.setGridFromViewLines();
                ViewLine contentLineAbove = getContentLineAbove(row);
                if (contentLineAbove != null) contentLineAbove.getJustificationFlow().requestFocus();
                e.consume();
            } else if (code == KeyCode.DOWN) {
                derivationView.setGridFromViewLines();
                ViewLine contentLineBelow = getContentLineBelow(row);
                if (contentLineBelow != null) contentLineBelow.getJustificationFlow().requestFocus();
                e.consume();
            }

        });

        derivationView.getGrid().getChildren().remove(flow);
        derivationView.getGrid().add(rta, 22, rowIndex);
        rta.requestFocus();
        mainView.editorInFocus(drta, ControlType.JUSTIFICATION);
    }

    /**
     * Determine whether one node is an ancestor of another
     *
     * @param node                      the potentential descendant
     * @param potentialHierarchyElement the potentential ancestor
     * @return true if the second is an ancestor of the first
     */
    public static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
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

        TextFlow justificationFlow = getJustificationFlow(justificationString, derivationView.getViewLines());
        if (derivationView.getViewLines().get(rowIndex).isLineHighlight()) {
            justificationFlow.setStyle("-fx-background-color: mistyrose");
        }
        derivationView.getViewLines().get(rowIndex).setJustificationFlow(justificationFlow);

        //removing the RTA from the grid seems to rewrite the grid, causing focus to jump.  Here we simply
        //blank the RTA (until the grid is next rewritten) so that it does not appear on top of new content
        rta.getActionFactory().newDocumentNow().execute(new ActionEvent());

        derivationView.setJustificationFlowOnGrid(rowIndex);
        //       derivationView.setGridFromViewLines();

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
            int row = derivationView.getGrid().getRowIndex(rta.getParent());

            if (code == KeyCode.ENTER || (code == KeyCode.RIGHT && e.isShortcutDown())) {
                derivationView.getViewLines().get(row).getJustificationFlow().requestFocus();
                e.consume();
            } else if (code == KeyCode.LEFT && e.isShortcutDown()) {
                if (getContentLineAbove(row) != null) getContentLineAbove(row).getJustificationFlow().requestFocus();
                e.consume();
            } else if (code == KeyCode.UP) {
                if (getContentLineAbove(row) != null)
                    getContentLineAbove(row).getLineContentBoxedDRTA().getRTA().requestFocus();
                e.consume();
            } else if (code == KeyCode.DOWN) {
                if (getContentLineBelow(row) != null)
                    getContentLineBelow(row).getLineContentBoxedDRTA().getRTA().requestFocus();
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

        derivationView.getViewLines().add(newRow, viewLine);
    }

    /*
     * Perform undo action
     */
    private void undoAction() {
        DerivationModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            derivationModel = (DerivationModel) SerializationUtils.clone(undoElement);
            setViewLinesFromModel();
            derivationView.setGridFromViewLines();
            updateUndoRedoButtons();
            setContentFocusListeners();

        }
    }

    /*
     * Perform redo action
     */
    private void redoAction() {
        DerivationModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            derivationModel = (DerivationModel) SerializationUtils.clone(redoElement);
            setViewLinesFromModel();
            derivationView.setGridFromViewLines();
            updateUndoRedoButtons();
            setContentFocusListeners();

        }
    }

    /*
     * Enable/Disable undo and redo buttons depending on current state
     */
    private void updateUndoRedoButtons() {
        derivationView.getUndoButton().setDisable(!undoRedoList.canUndo());
        derivationView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    /*
     * Push current state onto undo/redo stack
     */
    private void pushUndoRedo() {
        DerivationModel model = getDerivationModelFromView();
        DerivationModel deepCopy = (DerivationModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }

    /*
     * Insert new line at current position
     */
    private void insertLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;

        if (row >= 0) {
            ViewLine viewLine = derivationView.getViewLines().get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType()) || viewLine.getLineType() == LineType.CONCLUSION_LINE) {
                addEmptyViewContentRow(row, depth);
                derivationView.setGridFromViewLines();
                pushUndoRedo();

                BoxedDRTA bdrta = derivationView.getViewLines().get(row).getLineContentBoxedDRTA();
                DecoratedRTA drta = bdrta.getDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.applyCss();
                rta.layout();
                exerciseModified = true;
            } else {
                EditorAlerts.fleetingRedPopup("Cannot modify setup lines.");
            }
        } else {
            EditorAlerts.fleetingRedPopup("Select derivation row for insert above.");
        }
    }

    /*
     * Delete the line at the current position
     */
    private void deleteLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        List<ViewLine> viewLines = derivationView.getViewLines();
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

    /*
     * Increase scope depth by one
     */
    private void indentLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
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

    /*
     * Decrease scope depth by one
     */
    private void outdentLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
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
                                viewLines.remove(row);
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

    /*
     * Add premise/assumption shelf under current line
     */
    private void addShelfLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        if (row >= 0) {
            List<ViewLine> viewLines = derivationView.getViewLines();
            ViewLine viewLine = viewLines.get(row);
            int depth = viewLine.getDepth();
            if (!LineType.isSetupLine(viewLine.getLineType())) {
                if (++row < viewLines.size()) {
                    if (!(LineType.isShelfLine(viewLines.get(row).getLineType()) || LineType.isGapLine(viewLines.get(row).getLineType()))) {
                        ViewLine shelfLine = new ViewLine(null, depth, LineType.SHELF_LINE, null, null, null);
                        viewLines.add(row, shelfLine);
                        viewLine.getLineContentBoxedDRTA().getRTA().requestFocus();
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

    /*
     * add gap between content lines at current depth
     */
    private void addGapLineAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
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
                            viewLine.getLineContentBoxedDRTA().getRTA().requestFocus();
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

    /*
     * Insert lines for a subderivation above current line
     */
    private void insertSubAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
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

    /*
     * Add lines for a pair of subderivations above current line
     */
    private void insertSubsAction() {
        int row = -1;
        Node lastFocusedNode = mainWindow.getLastFocusOwner();
        if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
            row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
        else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
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
        Node lastFocusedNode = mainWindow.getLastFocusOwner();


        if (lastFocusedNode != null) {
            if (derivationView.getGrid().getChildren().contains(lastFocusedNode.getParent()))
                row = derivationView.getGrid().getRowIndex(lastFocusedNode.getParent());
            else if (lastJustificationRTA == lastFocusedNode) row = lastJustificationRow;
        }
        return row;
    }


    public DerivationHelp getDerivationHelp() {
        return derivationHelp;
    }

    /**
     * The derivation model
     * @return the model
     */
    @Override
    public DerivationModel getExerciseModel() { return derivationModel; }

    /**
     * The derivation view
     * @return the view
     */
    @Override
    public DerivationView getExerciseView() { return derivationView; }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {

        //run later added to allow justification set -- ok???
        Platform.runLater(() -> {
            boolean success = DiskUtilities.saveExercise(saveAs, getDerivationModelFromView());
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
        DerivationExercise printExercise = this;
        DerivationModel printModel = derivationModel;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
        Label exerciseName = new Label(derivationModel.getExerciseName());
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
        if (derivationCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + derivationView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (derivationCheck.isCheckFinal()) {
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
     * Return to the initial (unworked) version of the exercise, retaining the comment and check/help tries only.
     * @return the initial exercise
     */
    @Override
    public Exercise<DerivationModel, DerivationView> resetExercise() {
        RichTextArea commentRTA = derivationView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        DerivationModel originalModel = (DerivationModel) (derivationModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        CheckSetup setup = originalModel.getCheckSetup();
        setup.setHelpTries(derivationCheck.getHelpTries());
        setup.setCheckTries(derivationCheck.getCheckTries());
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        DerivationExercise clearExercise = new DerivationExercise(originalModel, mainWindow);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = derivationView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) {
            exerciseModified = true;
        }
        List<ViewLine> viewLines = derivationView.getViewLines();
        for (ViewLine viewLine : viewLines) {
            if (LineType.isContentLine(viewLine.getLineType())) {
                RichTextArea rta = viewLine.getLineContentBoxedDRTA().getRTA();
                if (rta.isModified()) {
                    exerciseModified = true;
                }
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
     * The view node to be displayed as a component of the free form exercise.  This will be a (possibly modified)
     * portion of the regular exercise view.
     * @return the view node
     */
    @Override
    public Node getFFViewNode() {return derivationView.getExerciseContentNode();}

    @Override
    public Spinner getFFHeightSpinner() {return derivationView.getSplitPaneHeightSpinner();}
    @Override
    public Spinner getFFWidthSpinner() {return derivationView.getSplitPaneWidthSpinner();}

    /**
     * The node to be printed as a component of the free form exercise.
     * @return the print node
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        DerivationExercise printExercise = this;
        DerivationModel printModel = derivationModel;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        GridPane derivationPane = printExercise.getExerciseView().getGrid();
        derivationPane.setPadding(new Insets(15,0,15,0));

        double splitPaneWidth = Math.max(nodeWidth, printModel.getSplitPanePrefWidth());
        double width = printModel.getGridWidth() * splitPaneWidth;

        derivationPane.setMaxWidth(width);
        derivationPane.setMinWidth(width);
        HBox gridBox = new HBox(derivationPane);
        gridBox.setAlignment(Pos.CENTER);

        return gridBox;
    }

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel<DerivationModel> getExerciseModelFromView() {
        return (ExerciseModel) getDerivationModelFromView();
    }

    /*
     * Extract the derivation model from view
     * @return the model
     */
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
        DerivationModel newModel = new DerivationModel(name, started, statementHeight,gridWidth, leftmostScopeLine, defaultShelf, keyboardSelector, statementDocument, commentDocument, modelLines);
        newModel.setOriginalModel(derivationModel.getOriginalModel());
        newModel.setCommentPrefHeight(derivationView.getCommentPrefHeight());
        newModel.setSplitPanePrefWidth(derivationView.getSplitPanePrefWidth());
        newModel.setCommentTextHeight(derivationModel.getCommentTextHeight());
        newModel.setStatementTextHeight(derivationModel.getStatementTextHeight());

        ///
        CheckSetup setup = derivationModel.getCheckSetup();
        setup.setCheckSuccess(derivationCheck.isCheckSuccess());
        setup.setCheckFinal(derivationCheck.isCheckFinal());
        setup.setCheckTries(derivationCheck.getCheckTries());
        setup.setHelpTries(derivationCheck.getHelpTries());
        newModel.setCheckSetup(setup);
       ///

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

    public DerivationCheck getDerivationCheck() {
        return derivationCheck;
    }
}
