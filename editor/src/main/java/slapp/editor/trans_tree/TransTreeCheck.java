package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.ParaphraseService;
import slapp.editor.SentenceValidator;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.m_parse.MParseUtilities;
import slapp.editor.main_window.Exercise;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;
import slapp.editor.trans_uline.ULTreeObj;
import slapp.editor.truth_table.TTcheck;
import slapp.editor.vertical_tree.VTCheckType;

import java.util.*;

import static slapp.editor.EditorMain.mainWindow;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;


public class TransTreeCheck {
    private static SentenceValidator validator = SentenceValidator.getInstance();
    private TransTreeExercise transTreeExercise;
    private TransTreeView transTreeView;
    private TransTreeModel transTreeModel;
    private TTreeCheckSetup checkSetup;
    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;
    private String objLangName = "ℒ\ud835\udcc8 (w/abv)";
    private Language objLang = Languages.getLanguage("ℒ\ud835\udcc8 (w/abv)");
    private String mLangName = "Meta Sym";
    private Language mLang = Languages.getLanguage("Meta Sym");
    private static String processInterpLang = "";
    private boolean langAllowDP;
    private VTCheckType checkType;
    private MParseUtilities mParseUtilities = new MParseUtilities();
    private ParaphraseService paraphraseService;
    private AnchorPane parseTreePane = null;
    private AnchorPane formulaTreePane = null;
    private RichTextArea interpFnRTA = null;
    private List<Formula> targetFormulas = new ArrayList<>();
    private List<InterpFnElement> interpFnElements;
    private List<InterpFnElement> keyInterpFnElements;
    private List<ParseTreeBox> parseTreeBoxes;
    private List<Node> parsePaneNodeList;
    private List<Node> formulaPaneNodeList;
    private List<FormulaTreeBox> formulaTreeBoxes;
    private ParseTreeBox parseTreeRoot;
    private FormulaTreeBox formulaTreeRoot;
    private List<String> parseTreeBottoms = new ArrayList<>();
    private double equalsEpsilon = 10.0;
    private List<TTreeBox> connectedNodes;
    private List<SentenceAtomic> sentenceAtomics = new ArrayList<>();
    private Pair<ParseTreeBox, FormulaTreeBox> nonNaturalTreePair;

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Named listeners
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    // SCORCHED EARTH RULE 7: Safe Global State & Null-Poisoning Prevention
    private void scrollToNode(Node node) {
        if (isTornDown || node == null) return;
        try {
            if (transTreeExercise != null && transTreeExercise.getMainWindow() != null && transTreeExercise.getMainWindow().getMainView() != null) {
                transTreeExercise.getMainWindow().getMainView().scrollToNode(node);
            }
        } catch (Exception e) {}
    }

    public TransTreeCheck(TransTreeExercise transTreeExercise) {
        this.transTreeExercise = transTreeExercise;
        this.transTreeView = transTreeExercise.getExerciseView();
        this.transTreeModel = transTreeExercise.getExerciseModel();
        checkSetup = transTreeModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new TTreeCheckSetup();
            transTreeModel.setCheckSetup(checkSetup);
        }
        setRightControlBox();
        if (checkSetup.isCheckSuccess()) {
            checkSuccess = true;
            transTreeView.setCheckMessage(checkSetup.getCheckMessage());
            if (checkSetup.isCheckGreen()) {
                transTreeView.setCheckColor(Color.LAWNGREEN);
                transTreeView.setCheckElementsColor(Color.GREEN);
            } else {
                transTreeView.setCheckColor(Color.ORCHID);
                transTreeView.setCheckElementsColor(Color.PURPLE);
            }
            transTreeView.activateBigCheck();
        }
        checkTries = checkSetup.getCheckTries();
        checkMax = checkSetup.getCheckMax();
        updateCounter();
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation applied heavily below via multiple try-catch blocks
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Clear static match registry! (If global matches are left alive, nodes float forever)
        try {
            MatchUtilities.clearMatching();
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getCheckButton() != null) {
                if (checkButtonHandler != null) transTreeView.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                transTreeView.getCheckButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getstaticHelpButton() != null) {
                if (staticHelpButtonHandler != null) transTreeView.getstaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                transTreeView.getstaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown (Never call .clear() on lists, only nullify!)
        try { this.targetFormulas = null; } catch (Exception e) {}
        try { this.interpFnElements = null; } catch (Exception e) {}
        try { this.keyInterpFnElements = null; } catch (Exception e) {}
        try { this.parseTreeBoxes = null; } catch (Exception e) {}
        try { this.parsePaneNodeList = null; } catch (Exception e) {}
        try { this.formulaPaneNodeList = null; } catch (Exception e) {}
        try { this.formulaTreeBoxes = null; } catch (Exception e) {}
        try { this.parseTreeBottoms = null; } catch (Exception e) {}
        try { this.connectedNodes = null; } catch (Exception e) {}
        try { this.sentenceAtomics = null; } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.transTreeExercise = null; } catch (Exception e) {}
        try { this.transTreeView = null; } catch (Exception e) {}
        try { this.transTreeModel = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.objLangName = null; } catch (Exception e) {}
        try { this.objLang = null; } catch (Exception e) {}
        try { this.mLangName = null; } catch (Exception e) {}
        try { this.mLang = null; } catch (Exception e) {}
        try { this.checkType = null; } catch (Exception e) {}
        try { this.mParseUtilities = null; } catch (Exception e) {}
        try { this.paraphraseService = null; } catch (Exception e) {}
        try { this.parseTreePane = null; } catch (Exception e) {}
        try { this.formulaTreePane = null; } catch (Exception e) {}
        try { this.interpFnRTA = null; } catch (Exception e) {}
        try { this.parseTreeRoot = null; } catch (Exception e) {}
        try { this.formulaTreeRoot = null; } catch (Exception e) {}
        try { this.nonNaturalTreePair = null; } catch (Exception e) {}
        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
    }

    private void setRightControlBox() {
        if (isTornDown || transTreeView == null || transTreeView.getCheckButton() == null) return;
        Button checkButton = transTreeView.getCheckButton();
        checkButton.setDisable(true);
        if (checkSetup.getCheckMax() == -1 || checkSetup.getCheckTries() < checkSetup.getCheckMax() || (transTreeExercise != null && transTreeExercise.getMainWindow() != null && transTreeExercise.getMainWindow().isInstructorFunctions())) {
            checkButton.setText("Loading Check");
            ParaphraseService.getInstance().thenAccept(service -> {
                Platform.runLater(() -> {
                    // SCORCHED EARTH RULE 2: Intercept asynchronous returns
                    if (isTornDown || transTreeView == null || transTreeView.getCheckButton() == null) return;
                    try {
                        transTreeView.getCheckButton().setDisable(false);
                        transTreeView.getCheckButton().setText("Check");
                    } catch (Exception e) {}
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    // SCORCHED EARTH RULE 2: Intercept asynchronous returns
                    if (isTornDown || transTreeView == null || transTreeView.getCheckButton() == null) return;
                    try {
                        transTreeView.getCheckButton().setText("AI Error!");
                    } catch (Exception e) {}
                });
                return null;
            });

            checkButtonHandler = e -> {
                if (isTornDown || transTreeExercise == null || transTreeView == null) return;
                checkExer();
            };
            transTreeView.getCheckButton().setOnAction(checkButtonHandler);
        }
        if (transTreeView.getstaticHelpButton() != null) {
            transTreeView.getstaticHelpButton().setDisable(!checkSetup.isStaticHelp());
            staticHelpButtonHandler = e -> {
                if (isTornDown || transTreeView == null || checkSetup == null) return;
                try {
                    Stage helpStage = transTreeView.getStaticHelpStage();
                    if (helpStage != null && helpStage.isShowing()) helpStage.close();
                    else transTreeView.showStaticHelp(checkSetup.getStaticHelpDoc());
                } catch (Exception ex) {}
            };
            transTreeView.getstaticHelpButton().setOnAction(staticHelpButtonHandler);
        }
    }

    private void setChecksCounter() {
        if (isTornDown || transTreeView == null || transTreeExercise == null || transTreeExercise.getMainWindow() == null) return;
        try {
            if (checkMax != -1 && checkTries >= checkMax && !transTreeExercise.getMainWindow().isInstructorFunctions()) {
                if (transTreeView.getCheckButton() != null) transTreeView.getCheckButton().setDisable(true);
            }
            String checkString;
            if (checkMax == -1) checkString = "(unlimited)";
            else if (checkMax == 0) checkString = "(none)";
            else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";
            if (transTreeView.getCheckTriesLabel() != null) transTreeView.getCheckTriesLabel().setText(checkString);
        } catch (Exception e) {}
    }

    private boolean checkExer() {
        if (isTornDown || transTreeView == null || transTreeExercise == null || checkSetup == null) return false;
        checkSuccess = false;
        transTreeView.deactivateBigCheck();
        if (transTreeExercise.getMainWindow() != null && !transTreeExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }
        if (checkSetup.isTreesOmitted()) {
            if (transTreeView.getRootLayout() == null) return false;
            parseTreePane = transTreeView.getRootLayout().getMain_pane();
            if (transTreeView.getInterpFnDRTA() != null) interpFnRTA = transTreeView.getInterpFnDRTA().getEditor();
            formulaTreePane = transTreeView.getRootLayout().getMain_pane();
            parseTreeBoxes = getParseTreeBoxes();
            formulaTreeBoxes = getFormulaTreeBoxes();
            if (formulaTreeBoxes.size() != 1) {
                EditorAlerts.showSimpleTxtListAlert("Omit Trees: ", Collections.singletonList(ParseUtilities.newRegularText("If trees are to be omitted, you still require an interpretation function, and the translation in a single (blue) formula field (parse tree boxes, if any, are ignored).")));
                return false;
            }
            if (interpFnRTA != null) {
                interpFnRTA.getActionFactory().saveNow().execute(new ActionEvent());
                Document interpFnDoc = interpFnRTA.getDocument();
                interpFnElements = processInterpFn(interpFnDoc, objLang);
            }
            if (interpFnElements == null) return false;
            ParaphraseService service = ParaphraseService.getInstance().join();
            if (!checkInterpFnForDuplicates(service)) return false;


            formulaTreeRoot = formulaTreeBoxes.get(0);
            objLang.setAllowDroppedBrackets(true);
            try {
                checkForFormula(formulaTreeRoot);
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Translation Expression:", e.getMessageList());
                if (formulaTreeRoot != null && formulaTreeRoot.getFormulaBox() != null) formulaTreeRoot.getFormulaBox().resetVTtreeBoxHighlight();
                return false;
            }
            if (formulaTreeRoot != null && formulaTreeRoot.getFormulaBox() != null && formulaTreeRoot.getFormulaBox().getRTA() != null) {
                Document translationDoc = formulaTreeRoot.getFormulaBox().getRTA().getDocument();
                if (!verifyInterpFnAdequacy(interpFnElements, translationDoc, objLang, false)) return false;
            }
            if (interpFnRTA != null && interpFnRTA.isEditable()) {
                if (!interpFnInKey(service)) return false;
            }
            if (!setTargetFormulas()) return false;



            if (!setTargetFormulas()) return false;
            try {
                objLang.setAllowDroppedBrackets(true);
                checkForFormula(formulaTreeRoot);
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Translation Expression:", e.getMessageList());
                if (formulaTreeRoot != null && formulaTreeRoot.getFormulaBox() != null) formulaTreeRoot.getFormulaBox().resetVTtreeBoxHighlight();
                return false;
            }
            if (!checkTarget()) return false;
            checkSuccess = true;
            checkSetup.setCheckGreen(true);
            transTreeView.setCheckColor(Color.LAWNGREEN);
            transTreeView.setCheckElementsColor(Color.GREEN);
            transTreeView.setCheckMessage("Interpretation function, translation: Good");
            if (checkSetup.isCheckNatural()) {
                boolean natural = false;
                for (Formula formula : targetFormulas) {
                    if (formula.equals(formulaTreeRoot.getFormula())) {
                        natural = true;
                        break;
                    }
                }
                if (natural) transTreeView.setCheckMessage("Interpretation function, translation: Good / Natural");
                else {
                    checkSetup.setCheckGreen(false);
                    transTreeView.setCheckColor(Color.ORCHID);
                    transTreeView.setCheckElementsColor(Color.PURPLE);
                    transTreeView.setCheckMessage("Interpretation function, translation: Good");
                }
            }
            if (checkSuccess) {
                transTreeView.activateBigCheck();
                return true;
            } else return false;
        }

        if (!setParsePaneAndInterpFn()) return false;
        parseTreeBoxes = getParseTreeBoxes();
        if (!setParentsAndChildren(parseTreeBoxes, parsePaneNodeList)) return false;
        if (!checkTrueTree(parseTreeBoxes)) return false;
        if (!processULStructures()) return false;
        for (ParseTreeBox parseTreeBox : parseTreeBoxes) {
            try {
                checkULstructure(parseTreeBox);
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure:", e.getMessageList());
                clearHighlights(parseTreeRoot, parseTreeBoxes);
                return false;
            }
        }
        if (interpFnRTA != null) {
            interpFnRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document interpFnDoc = interpFnRTA.getDocument();
            interpFnElements = processInterpFn(interpFnDoc, objLang);
        }
        if (interpFnElements == null) return false;
        ParaphraseService service = ParaphraseService.getInstance().join();
        if (!checkInterpFnForDuplicates(service)) return false;
        if (parseTreeBottoms != null) {
            parseTreeBottoms.clear();
        } else {
            parseTreeBottoms = new ArrayList<>();
        }
        try {
            checkTreeBottomsInInterpFn(parseTreeRoot);
        } catch (TextMessageException e) {
            EditorAlerts.showSimpleTxtListAlert("Tree Structure:", e.getMessageList());
            clearHighlights(parseTreeRoot, parseTreeBoxes);
            return false;
        }
        if (parseTreeBottoms.isEmpty()) {
            EditorAlerts.showSimpleTxtListAlert("Tree Structure:", Collections.singletonList(ParseUtilities.newRegularText("No parse tree to evaluate.")));
            return false;
        }
        if (!bottomNodesInInterpFn()) return false;
        if (interpFnRTA != null && interpFnRTA.isEditable()) {
            if (!interpFnInKey(service)) return false;
        }
        if (!setTargetFormulas()) return false;

        if (!checkSetup.isExcludeFormalTree()) {
            if (transTreeView.getRootLayout() != null) formulaTreePane = transTreeView.getRootLayout().getMain_pane();
            formulaTreeBoxes = getFormulaTreeBoxes();
            if (formulaTreeBoxes.isEmpty()) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure: ", Collections.singletonList(ParseUtilities.newRegularText("No formula tree to evaluate.")));
                return false;
            }
            if (!setParentsAndChildren(formulaTreeBoxes, formulaPaneNodeList)) return false;
            if (!checkTrueTree(formulaTreeBoxes)) return false;
            try {
                checkParallelStructure(parseTreeRoot, formulaTreeRoot);
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure:", e.getMessageList());
                clearHighlights(parseTreeRoot, parseTreeBoxes);
                clearHighlights(formulaTreeRoot, formulaTreeBoxes);
                return false;
            }
            try {
                checkGoodFormulas(formulaTreeRoot);
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Formula Tree:", e.getMessageList());
                clearHighlights(formulaTreeRoot, formulaTreeBoxes);
                return false;
            }
            clearHighlights(parseTreeRoot, parseTreeBoxes);
            clearHighlights(formulaTreeRoot, formulaTreeBoxes);
            try {
                nonNaturalTreePair = null;
                checkNodeMatch(parseTreeRoot, formulaTreeRoot);
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure:", e.getMessageList());
                clearHighlights(parseTreeRoot, parseTreeBoxes);
                clearHighlights(formulaTreeRoot, formulaTreeBoxes);
                return false;
            }
            if (!checkTarget()) return false;
            checkSuccess = true;
            checkSetup.setCheckGreen(true);
            transTreeView.setCheckColor(Color.LAWNGREEN);
            transTreeView.setCheckElementsColor(Color.GREEN);
            transTreeView.setCheckMessage("Trees, interp func: Good");
            if (checkSetup.isCheckNatural()) {
                boolean natural = false;
                for (Formula formula : targetFormulas) {
                    if (nonNaturalTreePair == null && formula.equals(formulaTreeRoot.getFormula())) {
                        natural = true;
                        break;
                    }
                }
                if (natural) transTreeView.setCheckMessage("Trees, interp func: Good / Natural");
                else {
                    if (nonNaturalTreePair != null) {
                        if (nonNaturalTreePair.getKey() != null && nonNaturalTreePair.getKey().getFormulaBox() != null) {
                            nonNaturalTreePair.getKey().getFormulaBox().setVTtreeBoxMutedHighlight();
                            nonNaturalTreePair.getKey().setGray(true);
                            scrollToNode(nonNaturalTreePair.getKey().getFormulaBox().getRTA());
                        }
                        if (nonNaturalTreePair.getValue() != null && nonNaturalTreePair.getValue().getFormulaBox() != null) {
                            nonNaturalTreePair.getValue().getFormulaBox().setVTtreeBoxMutedHighlight();
                            nonNaturalTreePair.getValue().setGray(true);
                            scrollToNode(nonNaturalTreePair.getValue().getFormulaBox().getRTA());
                        }
                    }
                    checkSetup.setCheckGreen(false);
                    transTreeView.setCheckColor(Color.ORCHID);
                    transTreeView.setCheckElementsColor(Color.PURPLE);
                    transTreeView.setCheckMessage("Trees, interp func: Good");
                }
            }
        } else {
            try {
                projectParallelTree(parseTreeRoot);
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure:", e.getMessageList());
                clearHighlights(parseTreeRoot, parseTreeBoxes);
                return false;
            }
            if (!checkParseTreeResult()) return false;
            checkSuccess = true;
            checkSetup.setCheckGreen(true);
            transTreeView.setCheckColor(Color.LAWNGREEN);
            transTreeView.setCheckElementsColor(Color.GREEN);
            transTreeView.setCheckMessage("Tree, interpretation function: Good");
        }
        if (checkSuccess) {
            transTreeView.activateBigCheck();
            return true;
        } else return false;
    }

    private void projectParallelTree(ParseTreeBox pBox) throws TextMessageException {
        if (isTornDown || pBox == null || interpFnElements == null) return;
        for (int i = 0; i < pBox.getTreeChildren().size(); i++) {
            ParseTreeBox pChild = (ParseTreeBox) pBox.getTreeChildren().get(i);
            projectParallelTree(pChild);
        }
        if (pBox.getTreeChildren().size() == 0) {
            if (pBox.getTreeObjects() == null || pBox.getTreeObjects().isEmpty()) return;
            ULTreeObj pBoxTreeObj = pBox.getTreeObjects().get(0);
            String pBoxTxt = pBoxTreeObj.getText();
            String pBoxText = cleanString(pBoxTxt);
            boolean found = false;
            for (InterpFnElement interpElement : interpFnElements) {
                if (pBoxText.equals(cleanString(interpElement.getAssignmentString()))) {
                    found = true;
                    LanguageElement interpSymbol = interpElement.getSymbol();
                    Formula sentenceAtomic = new SentenceAtomic((SentenceLetter) interpSymbol);
                    pBox.setMate(sentenceAtomic);
                    return;
                }
            }
            if (!found) {
                if (pBox.getFormulaBox() != null) {
                    pBox.getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(pBox.getFormulaBox().getRTA());
                }
                throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("The interpretation function assigns no value to this sentence.")));
            }
        } else {
            MatchUtilities.clearMatching();
            List<Pair<String, List<Formula>>> tfOpList = TFOperatorList.getTfOperatorList();
            if (pBox.getTreeObjects() == null || pBox.getTreeObjects().isEmpty()) return;
            ULTreeObj treeObj = pBox.getTreeObjects().get(0);
            String mainOpString = treeObj.getMainOperator();
            for (Pair opPair : tfOpList) {
                if (opPair.getKey().equals(mainOpString)) {
                    for (int i = 0; i < pBox.getTreeChildren().size(); i++) {
                        Formula childFormula = ((ParseTreeBox) pBox.getTreeChildren().get(i)).getMate();
                        String baseStr = "";
                        if (i == 0) baseStr = "\ud835\udc9c";
                        else if (i == 1) baseStr = "\u212c";
                        else if (i == 2) baseStr = "\ud835\udc9e";
                        else baseStr = "\ud835\udc9f";
                        MFormulaSym metaSym = new MFormulaSym(baseStr, "");
                        MFormula metaForm = MFormula.getInstance(metaSym);
                        try {
                            metaForm.setMatch(childFormula);
                        } catch (TextMessageException e) {
                            System.out.println("matching error");
                        }
                    }
                    Formula branchTarget = ((List<Formula>) opPair.getValue()).get(0).getMatch();
                    pBox.setMate(branchTarget);
                    break;
                }
            }
        }
    }

    private boolean checkParseTreeResult() {
        if (isTornDown || targetFormulas == null || targetFormulas.isEmpty() || parseTreeRoot == null) return false;
        setSentenceAtomics(targetFormulas.get(0), parseTreeRoot.getMate());
        if (!checkFormulaEquivalence(targetFormulas.get(0), parseTreeRoot.getMate())) {
            if (parseTreeRoot.getFormulaBox() != null) {
                parseTreeRoot.getFormulaBox().setVTtreeBoxHighlight();
                scrollToNode(parseTreeRoot.getFormulaBox().getRTA());
            }
            EditorAlerts.showSimpleTxtListAlert("Final Result:", Collections.singletonList(ParseUtilities.newRegularText("Parse tree and interpretation function are internally consistent.  But this is not a good tree!  Check initial underline structure.")));
            if (parseTreeRoot.getFormulaBox() != null) parseTreeRoot.getFormulaBox().resetVTtreeBoxHighlight();
            return false;
        }
        return true;
    }

    private boolean checkTarget() {
        if (isTornDown || targetFormulas == null || targetFormulas.isEmpty() || formulaTreeRoot == null) return false;
        setSentenceAtomics(targetFormulas.get(0), formulaTreeRoot.getFormula());
        if (!checkFormulaEquivalence(targetFormulas.get(0), formulaTreeRoot.getFormula())) {
            if (formulaTreeRoot.getFormulaBox() != null) {
                formulaTreeRoot.getFormulaBox().setVTtreeBoxHighlight();
                scrollToNode(formulaTreeRoot.getFormulaBox().getRTA());
            }
            if (checkSetup != null && checkSetup.isTreesOmitted()) EditorAlerts.showSimpleTxtListAlert("Final Result:", Collections.singletonList(ParseUtilities.newRegularText("The interpretation function is fine, but this is not a good translation.")));
            else EditorAlerts.showSimpleTxtListAlert("Final Result:", Collections.singletonList(ParseUtilities.newRegularText("Parse tree, interpretation function, and formal tree are internally consistent.  But this is not a good translation!  Check initial underline structure.")));
            if (formulaTreeRoot.getFormulaBox() != null) formulaTreeRoot.getFormulaBox().resetVTtreeBoxHighlight();
            return false;
        }
        return true;
    }

    private void checkNodeMatch(ParseTreeBox pBox, FormulaTreeBox fBox) throws TextMessageException {
        if (isTornDown || pBox == null || fBox == null || interpFnElements == null) return;

        for (int i = 0; i < pBox.getTreeChildren().size(); i++) {
            ParseTreeBox pChild = (ParseTreeBox) pBox.getTreeChildren().get(i);
            FormulaTreeBox fChild = (FormulaTreeBox) fBox.getTreeChildren().get(i);
            checkNodeMatch(pChild, fChild);
        }
        if (pBox.getTreeChildren().size() == 0) {
            if (pBox.getTreeObjects() == null || pBox.getTreeObjects().isEmpty()) return;
            ULTreeObj pBoxTreeObj = pBox.getTreeObjects().get(0);
            String pBoxTxt = pBoxTreeObj.getText();
            String pBoxText = cleanString(pBoxTxt);
            boolean found = false;
            for (InterpFnElement interpElement : interpFnElements) {
                if (pBoxText.equals(cleanString(interpElement.getAssignmentString()))) {
                    found = true;
                    LanguageElement interpSymbol = interpElement.getSymbol();
                    Formula sentenceAtomic = new SentenceAtomic((SentenceLetter) interpSymbol);
                    if (sentenceAtomic.equals(fBox.getFormula())) {
                        return;
                    } else {
                        if (pBox.getFormulaBox() != null) {
                            pBox.getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(pBox.getFormulaBox().getRTA());
                        }
                        if (fBox.getFormulaBox() != null) {
                            fBox.getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(fBox.getFormulaBox().getRTA());
                        }
                        throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("The interpretation function assigns a different expression to this sentence.")));
                    }
                }
            }
            if (!found) {
                if (pBox.getFormulaBox() != null) {
                    pBox.getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(pBox.getFormulaBox().getRTA());
                }
                throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("The interpretation function assigns no value to this sentence.")));
            }
        } else {
            MatchUtilities.clearMatching();
            List<Pair<String, List<Formula>>> tfOpList = TFOperatorList.getTfOperatorList();
            if (pBox.getTreeObjects() == null || pBox.getTreeObjects().isEmpty()) return;
            ULTreeObj treeObj = pBox.getTreeObjects().get(0);
            String mainOpString = treeObj.getMainOperator();
            for (Pair opPair : tfOpList) {
                if (opPair.getKey().equals(mainOpString)) {
                    for (int i = 0; i < fBox.getTreeChildren().size(); i++) {
                        Formula childFormula = ((FormulaTreeBox) fBox.getTreeChildren().get(i)).getFormula();
                        String baseStr = "";
                        if (i == 0) baseStr = "\ud835\udc9c";
                        else if (i == 1) baseStr = "\u212c";
                        else if (i == 2) baseStr = "\ud835\udc9e";
                        else baseStr = "\ud835\udc9f";
                        MFormulaSym metaSym = new MFormulaSym(baseStr, "");
                        MFormula metaForm = MFormula.getInstance(metaSym);
                        try {
                            metaForm.setMatch(childFormula);
                        } catch (TextMessageException e) {
                            System.out.println("matching error");
                        }
                    }
                    Formula branchTarget = (((List<Formula>) opPair.getValue()).get(0)).getMatch();
                    setSentenceAtomics(branchTarget, fBox.getFormula());
                    if (!checkFormulaEquivalence(branchTarget, fBox.getFormula())) {
                        if (pBox.getFormulaBox() != null) {
                            pBox.getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(pBox.getFormulaBox().getRTA());
                        }
                        if (fBox.getFormulaBox() != null) {
                            fBox.getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(fBox.getFormulaBox().getRTA());
                        }
                        throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Main operator '" + treeObj.getMainOperator() + "' not matched to equivalent formal expression.")));
                    }
                    if (nonNaturalTreePair == null) {
                        List<Formula> targets = (List<Formula>) opPair.getValue();
                        Formula formula = fBox.getFormula();
                        boolean natural = false;
                        for (Formula target : targets) {
                            Formula targetMatch = target.getMatch();
                            if (targetMatch.equals(formula)) {
                                natural = true;
                                break;
                            }
                        }
                        if (!natural) {
                            nonNaturalTreePair = new Pair(pBox, fBox);
                        }
                    }
                }
            }
        }
    }

    private void setSentenceAtomics(Formula formula1, Formula formula2) {
        if (isTornDown || sentenceAtomics == null) return;
        sentenceAtomics.clear();
        sentenceAtomicsHelper(formula1);
        sentenceAtomicsHelper(formula2);
    }

    private boolean checkFormulaEquivalence(Formula formula1, Formula formula2) {
        if (isTornDown || sentenceAtomics == null) return false;
        int numAtomics = sentenceAtomics.size();
        int numInterp = (int) Math.pow(2, numAtomics);
        boolean equivalent = true;
        for (int i = 0; i < numInterp; i++) {
            for (int j = 0; j < numAtomics; j++) {
                int mask = (int) Math.pow(2, j);
                if ((i & mask) == 0) {
                    sentenceAtomics.get(j).setSemanticValue("T");
                } else sentenceAtomics.get(j).setSemanticValue("F");
            }
            setFormulaValue(formula1, sentenceAtomics);
            setFormulaValue(formula2, sentenceAtomics);
            if (formula1.getSemanticValue() == null || formula2.getSemanticValue() == null) return false;
            if (!formula1.getSemanticValue().equals(formula2.getSemanticValue())) {
                equivalent = false;
                break;
            }
        }
        return equivalent;
    }

    private void sentenceAtomicsHelper(Formula formula) {
        if (isTornDown || formula == null || sentenceAtomics == null) return;
        if (formula.isAtomic()) {
            if (!sentenceAtomics.contains(formula)) sentenceAtomics.add((SentenceAtomic) formula);
            return;
        }
        if (formula.getChildren() != null) {
            for (Expression childExp : formula.getChildren()) {
                if (!(childExp instanceof Formula)) return;
                else {
                    Formula childForm = (Formula) childExp;
                    sentenceAtomicsHelper(childForm);
                }
            }
        }
    }

    private void checkParallelStructure(TTreeBox pBox, TTreeBox fBox) throws TextMessageException {
        if (isTornDown || pBox == null || fBox == null) return;
        if (pBox.getTreeChildren().size() == fBox.getTreeChildren().size()) {
            for (int i = 0; i < pBox.getTreeChildren().size(); i++) {
                checkParallelStructure(pBox.getTreeChildren().get(i), fBox.getTreeChildren().get(i));
            }
        } else {
            if (pBox.getFormulaBox() != null) {
                pBox.getFormulaBox().setVTtreeBoxHighlight();
                scrollToNode(pBox.getFormulaBox().getRTA());
            }
            if (fBox.getFormulaBox() != null) {
                fBox.getFormulaBox().setVTtreeBoxHighlight();
                scrollToNode(fBox.getFormulaBox().getRTA());
            }
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Parse and formula trees not structurally parallel: highlighted nodes have different number of children.")));
        }
    }

    private void checkGoodFormulas(FormulaTreeBox box) throws TextMessageException {
        if (isTornDown || box == null || objLang == null) return;
        objLang.setAllowDroppedBrackets(false);
        for (TTreeBox cBox : box.getTreeChildren()) {
            FormulaTreeBox childBox = (FormulaTreeBox) cBox;
            checkGoodFormulasHelper(childBox);
        }
        objLang.setAllowDroppedBrackets(true);
        checkForFormula(box);
    }

    private void checkGoodFormulasHelper(FormulaTreeBox box) throws TextMessageException {
        if (isTornDown || box == null) return;
        if (box.getTreeChildren().isEmpty()) {
            checkForFormula(box);
            return;
        }
        for (TTreeBox cBox : box.getTreeChildren()) {
            FormulaTreeBox childBox = (FormulaTreeBox) cBox;
            checkGoodFormulasHelper(childBox);
        }
        checkForFormula(box);
    }

    private void checkForFormula(FormulaTreeBox box) throws TextMessageException {
        if (isTornDown || box == null || objLang == null || box.getFormulaBox() == null) return;
        RichTextArea rta = box.getFormulaBox().getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        List<Expression> parseList = ParseUtilities.parseDoc(doc, objLang);
        if (parseList.isEmpty()) {
            box.getFormulaBox().setVTtreeBoxHighlight();
            scrollToNode(box.getFormulaBox().getRTA());
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Empty node.")));
        } else if (parseList.size() > 1 || !(parseList.get(0) instanceof Formula)) {
            box.getFormulaBox().setVTtreeBoxHighlight();
            scrollToNode(box.getFormulaBox().getRTA());
            if (checkSetup != null && checkSetup.isTreesOmitted()) throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Not a formula of " + objLangName)));
            else throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Not a formula of " + objLangName + " -- where dropped parentheses are permitted only on root (top) node.")));
        } else box.setFormula((Formula) parseList.get(0));
    }

    private boolean setTargetFormulas() {
        if (isTornDown || targetFormulas == null || checkSetup == null || interpFnRTA == null || objLang == null || mLang == null) return false;
        targetFormulas.clear();
        List<Document> targetDocs = checkSetup.getTargetDocs();
        if (targetDocs == null) return false;
        for (Document targetDoc : targetDocs) {
            if (!interpFnRTA.isEditable()) {
                List<Expression> parseList = ParseUtilities.parseDoc(targetDoc, objLang);
                if (parseList.isEmpty()) return false;
                Formula targetFormula = (Formula) parseList.get(0);
                targetFormulas.add(targetFormula);
            } else {
                List<Expression> parseList = ParseUtilities.parseDoc(targetDoc, mLang);
                if (parseList.isEmpty()) return false;
                Formula targetFormula = (Formula) parseList.get(0);
                if (sentenceAtomics != null) {
                    sentenceAtomics.clear();
                    sentenceAtomicsHelper(targetFormula);
                    for (SentenceAtomic atomic : sentenceAtomics) {
                        if (atomic.getMainLetter().getMatch() == null) {
                            EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("The interpretation function does not assign a value to all the sentences required for this exercise.")));
                            return false;
                        }
                    }
                }
                targetFormulas.add(targetFormula.getMatch());
            }
        }
        return true;
    }

    private boolean interpFnInKey(ParaphraseService service) {
        if (isTornDown || checkSetup == null || mLang == null || interpFnElements == null) return false;
        Document keyInterpFnDoc = checkSetup.getKeyInterpFnDoc();
        keyInterpFnElements = processInterpFn(keyInterpFnDoc, mLang);
        MatchUtilities.clearMatching();
        for (InterpFnElement objElement : interpFnElements) {
            boolean found = false;
            String objCleanStr = cleanString(objElement.getAssignmentString());
            if (keyInterpFnElements != null) {
                for (InterpFnElement keyElement : keyInterpFnElements) {
                    String keyCleanStr = cleanString(keyElement.getAssignmentString());
                    if (service.getSimilarityScore(objCleanStr, keyCleanStr) > .6) {
                        found = true;
                        MSentenceLetter keyLetter = (MSentenceLetter) keyElement.getSymbol();
                        SentenceLetter objLetter = (SentenceLetter) objElement.getSymbol();
                        try {
                            keyLetter.setMatch(objLetter);
                        } catch (TextMessageException e) {
                            System.out.println("matching error");
                        }
                        break;
                    }
                }
            }
            if (!found) {
                EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("I do not recognize '" + objElement.getAssignmentString() + "' as a proper member of the interpretation function for this exercise. \n\nYour interpretation function can have 'extra' elements - but each sentence should appear in the proper interpretation function for some member of the exercise set.")));
                return false;
            }
        }
        return true;
    }

    private boolean bottomNodesInInterpFn() {
        if (isTornDown || parseTreeBottoms == null || interpFnElements == null) return false;
        for (String bottomStr : parseTreeBottoms) {
            boolean found = false;
            for (InterpFnElement element : interpFnElements) {
                String cleanElementStr = cleanString(element.getAssignmentString());
                if (bottomStr.equals(cleanElementStr)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("Parse tree bottom node '" + bottomStr + "' not represented in the interpretation function.")));
                return false;
            }
        }
        return true;
    }

    private void checkTreeBottomsInInterpFn(TTreeBox box) throws TextMessageException {
        if (isTornDown || box == null || parseTreeBottoms == null || interpFnElements == null) return;
        if (box.getTreeChildren().size() == 0) {
            if (box.getTreeObjects() != null && !box.getTreeObjects().isEmpty()) {
                String boxText = box.getTreeObjects().get(0).getText();
                String cleanText = cleanString(boxText);
                if (!parseTreeBottoms.contains(cleanText)) {
                    parseTreeBottoms.add(cleanText);
                    boolean found = false;
                    for (InterpFnElement element : interpFnElements) {
                        String cleanElementStr = cleanString(element.getAssignmentString());
                        if (cleanText.equals(cleanElementStr)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        if (box.getFormulaBox() != null) {
                            box.getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(box.getFormulaBox().getRTA());
                        }
                        throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Parse tree bottom node not represented in interpretation function.")));
                    }
                }
            }
        }
        if (box.getTreeChildren() != null) {
            for (TTreeBox child : box.getTreeChildren()) {
                checkTreeBottomsInInterpFn(child);
            }
        }
    }

    public boolean checkInterpFnForDuplicates(ParaphraseService service) {
        if (isTornDown || interpFnElements == null) return false;
        for (int i = 0; i < interpFnElements.size(); i++) {
            InterpFnElement mainElement = interpFnElements.get(i);
            for (int j = i + 1; j < interpFnElements.size(); j++) {
                InterpFnElement testElement = interpFnElements.get(j);
                if (service.getSimilarityScore(mainElement.getAssignmentString(), testElement.getAssignmentString()) > .6) {
                    EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("Sentence '" + mainElement.getAssignmentString() + "' has duplicate (or near duplicate) entries in the interpretation function.")));
                    return false;
                }
            }
        }
        return true;
    }

    private void checkULstructure(ParseTreeBox treeBox) throws TextMessageException {
        if (isTornDown || treeBox == null || treeBox.getTreeObjects() == null || treeBox.getTreeObjects().isEmpty()) return;
        ULTreeObj boxTreeObj = treeBox.getTreeObjects().get(0);
        if (boxTreeObj.isTfOperator() && treeBox.getTreeChildren().size() == 0) {
            if (treeBox.getFormulaBox() != null) {
                treeBox.getFormulaBox().setVTtreeBoxHighlight();
                scrollToNode(treeBox.getFormulaBox().getRTA());
            }
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("'" + boxTreeObj.getMainOperator() + "' is a truth functional operator and so should have branch(es).")));
        }
        if (!boxTreeObj.isTfOperator() && treeBox.getTreeChildren().size() > 0) {
            if (boxTreeObj.getMainOperator().equals("")) {
                if (treeBox.getFormulaBox() != null) {
                    treeBox.getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(treeBox.getFormulaBox().getRTA());
                }
                throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Branch and (missing) underline structures do not match.")));
            } else {
                if (treeBox.getFormulaBox() != null) {
                    treeBox.getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(treeBox.getFormulaBox().getRTA());
                }
                throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("I do not recognize '" + boxTreeObj.getMainOperator() + "' as a truth functional operator.  If not truth functional, do not branch, otherwise edit to (standard) truth functional form.")));
            }
        }
        if (boxTreeObj.isTfOperator() && boxTreeObj.getChildren().size() != treeBox.getTreeChildren().size()) {
            if (treeBox.getFormulaBox() != null) {
                treeBox.getFormulaBox().setVTtreeBoxHighlight();
                scrollToNode(treeBox.getFormulaBox().getRTA());
            }
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Branch and underline structures do not match.")));
        }
        for (int i = 0; i < treeBox.getTreeChildren().size(); i++) {
            ULTreeObj uTreeChild = boxTreeObj.getChildren().get(i);
            ParseTreeBox childTreeBox = (ParseTreeBox) treeBox.getTreeChildren().get(i);
            if (childTreeBox != null && childTreeBox.getTreeObjects() != null && !childTreeBox.getTreeObjects().isEmpty()) {
                ULTreeObj pTreeChild = childTreeBox.getTreeObjects().get(0);
                try {
                    checkLineMatch(uTreeChild, pTreeChild);
                } catch (TextMessageException e) {
                    if (treeBox.getFormulaBox() != null) treeBox.getFormulaBox().setVTtreeBoxHighlight();
                    if (childTreeBox.getFormulaBox() != null) {
                        childTreeBox.getFormulaBox().setVTtreeBoxHighlight();
                        scrollToNode(childTreeBox.getFormulaBox().getRTA());
                    }
                    throw e;
                }
            }
        }
    }

    private void checkLineMatch(ULTreeObj uTreeObj, ULTreeObj pTreeObj) throws TextMessageException {
        if (isTornDown || uTreeObj == null || pTreeObj == null) return;
        String cleanUString = cleanString(uTreeObj.getText());
        String cleanPString = cleanString(pTreeObj.getText());
        if (!cleanUString.equals(cleanPString)) {
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Child text not same as underlined in parent.")));
        }
        if (uTreeObj.getChildren().size() != pTreeObj.getChildren().size()) {
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newRegularText("Branch does not preserve underline structure.")));
        }
        for (int i = 0; i < uTreeObj.getChildren().size(); i++) {
            ULTreeObj uchildObj = uTreeObj.getChildren().get(i);
            ULTreeObj pchildObj = pTreeObj.getChildren().get(i);
            checkLineMatch(uchildObj, pchildObj);
        }
    }

    private void clearHighlights(TTreeBox root, List<? extends TTreeBox> boxes) {
        if (isTornDown || root == null || boxes == null) return;
        if (root.getFormulaBox() != null) root.getFormulaBox().resetVTtreeBoxHighlight();
        root.setGray(false);
        if (root.getTreeChildren() != null) {
            for (TTreeBox child : root.getTreeChildren()) {
                clearHighlights(child, boxes);
            }
        }
    }

    private boolean checkTrueTree(List<? extends TTreeBox> boxes) {
        if (isTornDown || boxes == null) return false;
        TTreeBox root;
        List<TTreeBox> rootCandidates = new ArrayList<>();
        for (TTreeBox box : boxes) {
            if (box.getTreeParents().size() == 0) {
                rootCandidates.add(box);
            }
        }
        if (rootCandidates.size() == 1) {
            if (rootCandidates.get(0) instanceof ParseTreeBox) parseTreeRoot = (ParseTreeBox) rootCandidates.get(0);
            if (rootCandidates.get(0) instanceof FormulaTreeBox) formulaTreeRoot = (FormulaTreeBox) rootCandidates.get(0);
            root = rootCandidates.get(0);
        } else {
            for (TTreeBox rootCandidate : rootCandidates) {
                if (rootCandidate.getFormulaBox() != null) {
                    rootCandidate.getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(rootCandidate.getFormulaBox().getRTA());
                }
            }
            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A tree can have only one root node (node without parents).")));
            for (TTreeBox rootCandidate : rootCandidates) {
                if (rootCandidate.getFormulaBox() != null) rootCandidate.getFormulaBox().resetVTtreeBoxHighlight();
            }
            return false;
        }

        for (TTreeBox box : boxes) {
            List<TTreeBox> parents = box.getTreeParents();
            if (parents != null && parents.size() > 1) {
                if (box.getFormulaBox() != null) {
                    box.getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(box.getFormulaBox().getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A node can be linked to at most one parent (upper) node.")));
                if (box.getFormulaBox() != null) box.getFormulaBox().resetVTtreeBoxHighlight();
                return false;
            }
        }
        return true;
    }

    private void populateConnectedNodes(TTreeBox box) {
        if (isTornDown || box == null || connectedNodes == null) return;
        connectedNodes.add(box);
        if (box.getTreeChildren() != null) {
            for (TTreeBox child : box.getTreeChildren()) {
                populateConnectedNodes(child);
            }
        }
    }

    private boolean setParentsAndChildren(List<? extends TTreeBox> boxes, List<Node> allNodes) {
        if (isTornDown || boxes == null || allNodes == null) return false;
        for (TTreeBox box : boxes) {
            box.setTreeParents(new ArrayList<>());
            box.setTreeChildren(new ArrayList<>());
            String formulaID = box.getId();
            for (Node link : allNodes) {
                String relativeID = "";
                if (link instanceof TTClickableNodeLink) {
                    TTClickableNodeLink nodeLink = (TTClickableNodeLink) link;
                    if (nodeLink.getSourceId() != null && nodeLink.getSourceId().equals(formulaID)) {
                        relativeID = nodeLink.getTargetId();
                    }
                    if (nodeLink.getTargetId() != null && nodeLink.getTargetId().equals(formulaID)) {
                        relativeID = nodeLink.getSourceId();
                    }
                }
                if (!relativeID.equals("")) {
                    for (TTreeBox tBox : boxes) {
                        if (tBox.getId().equals(relativeID)) {
                            if (tBox.getLayoutY() - box.getLayoutY() > equalsEpsilon) {
                                box.getTreeChildren().add(tBox);
                            } else if (box.getLayoutY() - tBox.getLayoutY() > equalsEpsilon) {
                                box.getTreeParents().add(tBox);
                            } else {
                                if (box.getFormulaBox() != null) {
                                    box.getFormulaBox().setVTtreeBoxHighlight();
                                    scrollToNode(box.getFormulaBox().getRTA());
                                }
                                if (tBox.getFormulaBox() != null) {
                                    tBox.getFormulaBox().setVTtreeBoxHighlight();
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Cannot have connected nodes in same row.")));
                                if (box.getFormulaBox() != null) box.getFormulaBox().resetVTtreeBoxHighlight();
                                if (tBox.getFormulaBox() != null) tBox.getFormulaBox().resetVTtreeBoxHighlight();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        for (TTreeBox box : boxes) {
            if (box.getTreeChildren() != null) {
                box.getTreeChildren().sort(Comparator.comparingDouble((parent -> parent.getLayoutX())));
            }
        }
        return true;
    }

    private boolean processULStructures() {
        if (isTornDown || parseTreeBoxes == null) return false;
        for (ParseTreeBox box : parseTreeBoxes) {
            List<ULTreeObj> objList = box.getTreeObjects();
            if (objList == null || objList.isEmpty()) continue;
            String rootString = objList.get(0).getText();
            for (ULTreeObj treeObj : objList) {
                int startIndex = treeObj.getIndexMin();
                int endIndex = treeObj.getIndexMax();
                if (0 < startIndex && startIndex + 1 < rootString.length() && rootString.codePointCount(startIndex - 1, startIndex + 1) == 1)
                    startIndex++;
                if (0 < endIndex && endIndex + 1 < rootString.length() && rootString.codePointCount(endIndex - 1, endIndex + 1) == 1)
                    endIndex++;
                String objString = rootString.substring(startIndex, endIndex);
                treeObj.setText(objString);
            }
            for (int i = 1; i < objList.size(); i++) {
                ULTreeObj candidate = objList.get(i);
                boolean included = false;
                for (ULTreeObj base : objList) {
                    if (base.getLevel() + 1 == candidate.getLevel()) {
                        if (!base.getChildren().contains(candidate)) {
                            if ((candidate.getIndexMin() >= base.getIndexMin() && candidate.getIndexMax() < base.getIndexMax()) ||
                                    (candidate.getIndexMin() > base.getIndexMin() && candidate.getIndexMax() <= base.getIndexMax())) {
                                included = true;
                                int index = 0;
                                List<ULTreeObj> children = base.getChildren();
                                for (int j = 0; j < children.size(); j++) {
                                    ULTreeObj child = children.get(j);
                                    if (child.getIndexMin() > candidate.getIndexMin()) {
                                        index = j;
                                        break;
                                    }
                                    index = j + 1;
                                }
                                children.add(index, candidate);
                            }
                        } else included = true;
                    }
                }
                if (!included) {
                    if (box.getFormulaBox() != null) {
                        box.getFormulaBox().setVTtreeBoxHighlight();
                        scrollToNode(box.getFormulaBox().getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Underline Structure", Collections.singletonList(ParseUtilities.newRegularText("One or more lines not completely \"included\" in ones below.")));
                    if (box.getFormulaBox() != null) box.getFormulaBox().resetVTtreeBoxHighlight();
                    return false;
                }
            }
            ULTreeObj rObj = objList.get(0);
            if (rObj.getText().equals("")) {
                if (box.getFormulaBox() != null) {
                    box.getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(box.getFormulaBox().getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Expression Content:", Collections.singletonList(ParseUtilities.newRegularText("Cannot process an empty expression.")));
                if (box.getFormulaBox() != null) box.getFormulaBox().resetVTtreeBoxHighlight();
                return false;
            } else {
                if (validator != null) {
                    if (!validator.isCompleteSentence(rObj.getText())) {
                        if (box.getFormulaBox() != null) {
                            box.getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(box.getFormulaBox().getRTA());
                        }
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("Cannot identify the boxed expression\n\n    "));
                        textList.add(ParseUtilities.newRegularText(rObj.getText()));
                        textList.add(ParseUtilities.newRegularText("\n\nas a complete sentence."));
                        EditorAlerts.showSimpleTxtListAlert("Expression Content:", textList);
                        if (box.getFormulaBox() != null) box.getFormulaBox().resetVTtreeBoxHighlight();
                        return false;
                    }
                    try {
                        traverseTreeForSentences(rObj);
                    } catch (TextMessageException e) {
                        if (box.getFormulaBox() != null) {
                            box.getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(box.getFormulaBox().getRTA());
                        }
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("Cannot identify the underlined expression\n\n    "));
                        textList.addAll(e.getMessageList());
                        textList.add(ParseUtilities.newRegularText("\n\nas a complete sentence."));
                        EditorAlerts.showSimpleTxtListAlert("Expression Content", textList);
                        if (box.getFormulaBox() != null) box.getFormulaBox().resetVTtreeBoxHighlight();
                        return false;
                    }
                } else {
                    EditorAlerts.fleetingRedPopup("No sentence validator");
                }
            }
            updateTreeOperators(rObj);
        }
        return true;
    }

    private static void updateTreeOperators(ULTreeObj obj) {
        if (obj == null) return;
        String cleanParent = cleanString(obj.getText());
        for (int i = 0; i < obj.getChildren().size(); i++) {
            ULTreeObj childObj = obj.getChildren().get(i);
            String cleanChild = cleanString(childObj.getText());
            cleanParent = cleanParent.replaceFirst(cleanChild, "__");
        }
        if (obj.getChildren().size() > 0) {
            obj.setMainOperator(cleanParent);
            List<Pair<String, List<Formula>>> tfOperatorList = TFOperatorList.getTfOperatorList();
            for (Pair<String, List<Formula>> opPair : tfOperatorList) {
                if (cleanParent.equals(opPair.getKey())) {
                    obj.setTfOperator(true);
                    break;
                }
            }
        }
        for (ULTreeObj child : obj.getChildren()) {
            updateTreeOperators(child);
        }
    }

    public static String cleanString(String string) {
        if (string == null) return "";
        String lowercaseString = string.toLowerCase();
        String noPunctString = lowercaseString.replaceAll("[\\p{Punct}]", "");
        String noDSpaceString = noPunctString.replaceAll("\\s+", " ").trim();
        return noDSpaceString;
    }

    private static String cleanString2(String string) {
        if (string == null) return "";
        String lowercaseString = string.toLowerCase();
        String noDSpaceString = lowercaseString.replaceAll("\\s+", " ").trim();
        return noDSpaceString;
    }

    private static void traverseTreeForSentences(ULTreeObj obj) throws TextMessageException {
        if (obj == null || validator == null) return;
        if (!validator.isCompleteSentence(obj.getText())) {
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newUnderlineText(obj.getText())));
        }
        if (obj.getChildren() != null) {
            for (ULTreeObj child : obj.getChildren()) {
                traverseTreeForSentences(child);
            }
        }
    }

    private List<ParseTreeBox> getParseTreeBoxes() {
        if (isTornDown || parseTreePane == null) return new ArrayList<>();
        List<ParseTreeBox> parseBoxes = new ArrayList<>();
        parsePaneNodeList = parseTreePane.getChildren();
        for (Node node : parsePaneNodeList) {
            if (node instanceof ParseTreeBox) parseBoxes.add((ParseTreeBox) node);
        }
        return parseBoxes;
    }

    private List<FormulaTreeBox> getFormulaTreeBoxes() {
        if (isTornDown || formulaTreePane == null) return new ArrayList<>();
        List<FormulaTreeBox> formulaBoxes = new ArrayList<>();
        formulaPaneNodeList = formulaTreePane.getChildren();
        for (Node node : formulaPaneNodeList) {
            if (node instanceof FormulaTreeBox) formulaBoxes.add((FormulaTreeBox) node);
        }
        return formulaBoxes;
    }

    private boolean setParsePaneAndInterpFn() {
        if (isTornDown || checkSetup == null || transTreeExercise == null || transTreeView == null || transTreeView.getRootLayout() == null) return false;
        if(!checkSetup.isParseTreeFromAux()) {
            parseTreePane = transTreeView.getRootLayout().getMain_pane();
            if (transTreeView.getInterpFnDRTA() != null) {
                interpFnRTA = transTreeView.getInterpFnDRTA().getEditor();
            }
        } else {
            Exercise auxExer = transTreeExercise.getAuxExerciseB();
            if (auxExer != null && auxExer instanceof TransTreeExercise) {
                TransTreeExercise transAuxExer = (TransTreeExercise) auxExer;
                if (transAuxExer.getExerciseView() != null && transAuxExer.getExerciseView().getRootLayout() != null) {
                    parseTreePane = transAuxExer.getExerciseView().getRootLayout().getMain_pane();
                    if (transAuxExer.getExerciseView().getInterpFnDRTA() != null) {
                        interpFnRTA = transAuxExer.getExerciseView().getInterpFnDRTA().getEditor();
                    }
                }
            } else {
                EditorAlerts.showSimpleTxtListAlert("Auxiliary Exercise:", Collections.singletonList(ParseUtilities.newRegularText("Auxiliary exercise not available (or not right exercise type).  Cannot check.")));
                return false;
            }
        }
        return true;
    }

    public static boolean checkEquivalence(Document doc1, Document doc2, String objLangName) {
        if (doc1 == null || doc2 == null || objLangName == null) return false;
        Language language = Languages.getLanguage(objLangName);
        List<SentenceAtomic> sentenceAtomics = new ArrayList<>();
        List<OriginalElement> elementsDoc1 = ParseUtilities.getElements(doc1);
        List<OriginalElement> elementsDoc2 = ParseUtilities.getElements(doc2);
        List<Expression> targetExpressions = new ArrayList<>();
        if (elementsDoc1 != null) {
            for (OriginalElement element : elementsDoc1) {
                targetExpressions.add(element);
            }
        }
        if (elementsDoc2 != null) {
            for (OriginalElement element : elementsDoc2) {
                targetExpressions.add(element);
            }
        }
        processLanguageElements(targetExpressions, language);
        for (Expression exp : targetExpressions) {
            if (exp instanceof LanguageElement && exp instanceof SentenceLetter) {
                SentenceLetter sentenceLetter = (SentenceLetter) exp;
                SentenceAtomic atomic = new SentenceAtomic(sentenceLetter);
                atomic.setLevel(1);
                atomic.setAtomic(true);
                atomic.setCombines(true);
                if (!sentenceAtomics.contains(atomic)) sentenceAtomics.add(atomic);
            }
        }
        int numAtomics = sentenceAtomics.size();
        int numInterp = (int) Math.pow(2, numAtomics);
        boolean equivalent = true;
        for (int i = 0; i < numInterp; i++) {
            for (int j = 0; j < numAtomics; j++) {
                int mask = (int) Math.pow(2, j);
                if ((i & mask) == 0) {
                    sentenceAtomics.get(j).setSemanticValue("T");
                } else sentenceAtomics.get(j).setSemanticValue("F");
            }
            List<Expression> parseListDoc1 = ParseUtilities.parseDoc(doc1, objLangName);
            List<Expression> parseListDoc2 = ParseUtilities.parseDoc(doc2, objLangName);
            if (parseListDoc1 == null || parseListDoc1.isEmpty() || parseListDoc2 == null || parseListDoc2.isEmpty()) return false;
            Formula formula1 = (Formula) parseListDoc1.get(0);
            Formula formula2 = (Formula) parseListDoc2.get(0);
            setFormulaValue(formula1, sentenceAtomics);
            setFormulaValue(formula2, sentenceAtomics);
            if (formula1.getSemanticValue() == null || formula2.getSemanticValue() == null) return false;
            if (!formula1.getSemanticValue().equals(formula2.getSemanticValue())) {
                equivalent = false;
                break;
            }
        }
        return equivalent;
    }

    public static void setFormulaValue(Formula formula, List<SentenceAtomic> atomics) {
        if (formula == null || atomics == null) return;
        if (formula.getChildren() != null) {
            for (Expression child : formula.getChildren()) {
                if (child instanceof Formula) {
                    Formula childFormula = (Formula) child;
                    setFormulaValue(childFormula, atomics);
                }
            }
        }
        if (formula.isAtomic()) {
            for (SentenceAtomic atomic : atomics) {
                if (atomic.equals(formula)) formula.setSemanticValue(atomic.getSemanticValue());
            }
            return;
        }
        Operator mainOp = formula.getMainOperator();
        if (mainOp != null && formula.getChildren() != null && !formula.getChildren().isEmpty()) {
            if (mainOp.getType() == ExpressionType.NEG_OP) {
                formula.setSemanticValue(TTcheck.truthFunctionValue(mainOp, ((Formula) formula.getChildren().get(0)).getSemanticValue()));
            } else if (formula.getChildren().size() > 1) {
                formula.setSemanticValue(TTcheck.truthFunctionValue(mainOp, ((Formula) formula.getChildren().get(0)).getSemanticValue(), ((Formula) formula.getChildren().get(1)).getSemanticValue()));
            }
        }
    }

    public static List<InterpFnElement> processInterpFn(Document interpDoc, Language language) {
        if (interpDoc == null || language == null) return new ArrayList<>();
        processInterpLang = language.getNameString();
        List<OriginalElement> originalElements = getElements(interpDoc);
        if (originalElements == null) return new ArrayList<>();
        List<Expression> expressions = new ArrayList<>();
        for (OriginalElement element : originalElements) {
            expressions.add(element);
        }
        processLanguageElements(expressions, language);
        List<InterpFnElement> interpFnElements = processInterpFnElements(expressions, language);
        return interpFnElements;
    }

    public static boolean verifyInterpFnAdequacy(List<InterpFnElement> interpFnElements, Document targetDoc, Language language, boolean requireUniv) {
        if (interpFnElements == null || targetDoc == null || language == null) return false;
        List<Expression> parseList = ParseUtilities.parseDoc(targetDoc, language);
        if (parseList != null && !parseList.isEmpty()) {
            Formula targetFormula = (Formula) parseList.get(0);
            return verifyInterpFnAdequacy(interpFnElements, targetFormula, language, requireUniv);
        }
        return false;
    }


    public static boolean verifyInterpFnAdequacy(List<InterpFnElement> interpFnElements, Formula targetFormula, Language language, boolean requireUniv) {
        if (interpFnElements == null || targetFormula == null || language == null) return false;

        List<LanguageElement> targetElementList = new ArrayList<>(getElementSet(targetFormula));
        if (requireUniv) {
            boolean hasU = false;
            for (InterpFnElement element : interpFnElements) {
                LanguageElement langElement = element.getSymbol();
                if (language.getNameString().equals("Meta Sym") && langElement instanceof MetUnivSym) {
                    hasU = true;
                    break;
                } else if (langElement instanceof ObjUnivSym) {
                    hasU = true;
                    break;
                }
            }
            String varType = "";
            varType = language.getNameString().equals("Meta Sym") ? "(uppercase sans italic)" : "(uppercase italic)";
            if (!hasU) {
                EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", Collections.singletonList(ParseUtilities.newRegularText("This interpretation function lacks specification of a universe, of the sort " + varType + " universe symbol / colon / assignment.")));
                return false;
            }
        }
        for (LanguageElement targetElement : targetElementList) {
            boolean found = false;
            for (InterpFnElement interpFnElement : interpFnElements) {
                LanguageElement interpElement = interpFnElement.getSymbol();
                if (targetElement.getBaseStr().equals(interpElement.getBaseStr()) &&
                        targetElement.getSubscriptStr().equals(interpElement.getSubscriptStr()) &&
                        targetElement.getPlaces() == interpElement.getPlaces()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                List<Text> textList = new ArrayList<>();
                String symbolType = "";
                if (targetElement instanceof SentenceLetter) symbolType = "sentence letter";
                else if (targetElement instanceof RelationSymbol) symbolType = "relation symbol";
                else if (targetElement instanceof FunctionSymbol) symbolType = "function symbol";
                else if (targetElement instanceof ConstantSym) symbolType = "constant symbol";
                textList.add(ParseUtilities.newRegularText("The interpretation function does not include an assignment for " + symbolType + " "));
                textList.addAll(((LanguageElement) targetElement).toTextList());
                textList.add(ParseUtilities.newRegularText("."));
                EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                return false;
            }
        }

        return true;
    }

    private static Set<LanguageElement> getElementSet(Formula formula) {
        Set<LanguageElement> elementSet = new HashSet<>();
        elementSetHelper(formula, elementSet);
        return elementSet;
    }

    private static void elementSetHelper(Expression exp, Set<LanguageElement> elementSet) {
        if (exp == null || elementSet == null) return;
        RelationSymbol eqSym = new RelationSymbol("\ue8ac", "", "", 2);
        if (exp instanceof InfixAtomic) {
            RelationSymbol relSym = ((InfixAtomic) exp).getMainRelation();
            if (!relSym.equals(eqSym)) elementSet.add(relSym);
        } else if (exp instanceof PrefixAtomic) {
            RelationSymbol relSym = ((PrefixAtomic) exp).getMainRelation();
            if (!relSym.equals(eqSym)) elementSet.add(relSym);
        } else if (exp instanceof SentenceAtomic) {
            elementSet.add(((SentenceAtomic) exp).getMainLetter());
            return;
        } else if (exp instanceof Term) {
            Term term = (Term) exp;
            if (term.getTermType() == TermType.COMPLEX) elementSet.add(term.getMainFnSymbol());
            else if (term.getTermType() == TermType.CONSTANT) {
                if (term.getChildren() != null && !term.getChildren().isEmpty()) {
                    elementSet.add(((ConstantSym) term.getChildren().get(0)));
                }
                return;
            } else return;
        }
        if (exp.getChildren() != null) {
            for (Expression childExp : exp.getChildren()) {
                elementSetHelper(childExp, elementSet);
            }
        }
    }

    private static List<Text> metaSymbolMessage(LanguageElement element) {
        if (element == null) return new ArrayList<>();
        List<Text> txtList = new ArrayList<>();
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("Expect meta-symbol in place of ")));
        txtList.addAll(element.toTextList());
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(".")));
        return txtList;
    }

    private static List<Text> objSymbolMessage(LanguageElement element) {
        if (element == null) return new ArrayList<>();
        List<Text> txtList = new ArrayList<>();
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("Expect object-symbol in place of ")));
        txtList.addAll(element.toTextList());
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(".")));
        return txtList;
    }

    private static List<InterpFnElement> processInterpFnElements(List<Expression> expressions, Language language) {
        if (expressions == null || language == null) return new ArrayList<>();
        List<InterpFnElement> interpFnElements = new ArrayList<>();
        Language metaSupp = Languages.getLanguage("Meta Supp");
        Integer i = 0;
        while (i < expressions.size()) {
            Expression exp = expressions.get(i);
            if (exp instanceof LanguageElement) {
                LanguageElement languageElement = (LanguageElement) exp;
                boolean isNew = true;
                for (InterpFnElement element : interpFnElements) {
                    if (element.getSymbol().equals(exp)) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    boolean add = false;
                    while (i + 1 < expressions.size()) {
                        i++;
                        Pair<Boolean, Integer> findPair = findColonNext(expressions, i);
                        if (findPair.getKey()) {
                            i = findPair.getValue();
                            i = i + 1;
                            add = true;
                            break;
                        } else {
                            List<Text> textList = new ArrayList<>();
                            textList.add(ParseUtilities.newRegularText("For symbol '"));
                            textList.addAll(languageElement.toTextList());
                            textList.add(ParseUtilities.newRegularText("' of language "));
                            textList.addAll(language.getNameTextList());
                            textList.add(ParseUtilities.newRegularText(" do not find, symbol / colon / assignment."));
                            EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                            return null;
                        }
                    }
                    if (!add) {
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("For symbol '"));
                        textList.addAll(languageElement.toTextList());
                        textList.add(ParseUtilities.newRegularText("' of language "));
                        textList.addAll(language.getNameTextList());
                        textList.add(ParseUtilities.newRegularText(" do not find, symbol / colon / assignment."));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                        return null;
                    } else {
                        List<Expression> assignmentExpList = new ArrayList<>();
                        while (i < expressions.size() && !(expressions.get(i) instanceof LanguageElement)) {
                            assignmentExpList.add(expressions.get(i));
                            i++;
                        }
                        trimExpressionList(assignmentExpList);
                        try {
                            InterpFnElement interpFnElement = new InterpFnElement(languageElement, assignmentExpList);
                            interpFnElements.add(interpFnElement);
                        } catch (TextMessageException e) {
                            EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", e.getMessageList());
                            return null;
                        }
                    }
                } else {
                    List<Text> textList = new ArrayList<>();
                    textList.add(ParseUtilities.newRegularText("This interpretation function contains multiple entries for symbol "));
                    textList.addAll(exp.toTextList());
                    textList.add(ParseUtilities.newRegularText("."));
                    EditorAlerts.showSimpleTxtListAlert("Interpretation Function: ", textList);
                    return null;
                }
            } else i++;
        }
        if (interpFnElements.size() == 0) {
            List<Text> texts = new ArrayList<>();
            texts.add(ParseUtilities.newRegularText("For language, "));
            texts.addAll(language.getNameTextList());
            texts.add(ParseUtilities.newRegularText(" do not find interpretation with elements of the sort, symbol / colon / assignment."));
            EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", texts);
            return null;
        }
        int count = 0;
        for (InterpFnElement element : interpFnElements) {
            if (element.getSymbol() instanceof UnivSym) count++;
        }
        if (count > 1) {
            EditorAlerts.showSimpleTxtListAlert("Interpretaton Function:", Collections.singletonList(ParseUtilities.newRegularText("This interpretation function includes assignments to more than one universe variable.")));
            return null;
        }
        if (verifyInterpFnElements(interpFnElements)) return interpFnElements;
        else return null;
    }

    private static boolean verifyInterpFnElements(List<InterpFnElement> interpFnElements) {
        if (interpFnElements == null) return false;
        UnivSym universeSym = null;
        Language metaSupp = Languages.getLanguage("Meta Supp");
        for (InterpFnElement element : interpFnElements) {
            if (element.getSymbol() instanceof UnivSym) {
                if (universeSym == null) universeSym = (UnivSym) (element.getSymbol());
                else {
                    List<Text> textList = new ArrayList<>();
                    textList.add(ParseUtilities.newRegularText("This interpretation function includes multiple universe assignments."));
                    EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                    return false;
                }
            }
        }
        if (validator != null) {
            for (InterpFnElement element : interpFnElements) {
                LanguageElement symbol = element.getSymbol();
                if (symbol instanceof SentenceLetter) {
                    if (!validator.isCompleteSentence((element.getAssignmentString()))) {
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("Cannot understand the expression '" + element.getAssignmentString() + "' assigned to sentence letter "));
                        textList.addAll(element.getSymbol().toTextList());
                        textList.add(ParseUtilities.newRegularText(" as a complete sentence."));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                        return false;
                    }
                } else if (symbol instanceof ConstantSym) {
                    String assignmentString = element.getAssignmentString();
                    String cleanString = cleanString2(assignmentString);
                    if (cleanString.isEmpty()) {
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("Constant symbol '"));
                        textList.addAll(symbol.toTextList());
                        textList.add(ParseUtilities.newRegularText("' must be assigned to some member of the universe."));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                        return false;
                    } else {
                        boolean good = cleanString.codePoints()
                                .allMatch(cp -> {
                                    String s = Character.toString(cp);
                                    return metaSupp.getNameAlphabet().contains(s) || s.equals(" ");
                                });
                        if (!good) {
                            List<Text> textList = new ArrayList<>();
                            textList.add(ParseUtilities.newRegularText("Expect the assignment to Constant symbol '"));
                            textList.addAll(symbol.toTextList());
                            textList.add(ParseUtilities.newRegularText("' to identify some member of the universe (all alphanumeric characters in the base font)."));
                            EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                            return false;
                        }
                    }
                }
                if (symbol instanceof RelationSymbol || symbol instanceof FunctionSymbol) {
                    if (universeSym == null) {
                        List<Text> textList = new ArrayList<>();
                        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText("This interpretation requires a specification for the universe.")));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                        return false;
                    }
                    if (element.getUnivSym() != null && !element.getUnivSym().equals(universeSym)) {
                        List<Text> textList = new ArrayList<>();
                        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText("For symbol ")));
                        textList.addAll(symbol.toTextList());
                        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText(", the stated universe ")));
                        textList.addAll(element.getUnivSym().toTextList());
                        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" is not the same as the universe ")));
                        if (universeSym != null) textList.addAll(universeSym.toTextList());
                        textList.add(ParseUtilities.newRegularText(" from the interpretation itself."));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                        return false;
                    }
                }
                if (symbol instanceof RelationSymbol || symbol instanceof FunctionSymbol || symbol instanceof UnivSym) {
                    if (element.buildCapNameCondition(element.getSetCondition()) != null && !validator.isCompleteSentence(element.buildCapNameCondition(element.getSetCondition()).trim())) {
                        List<Text> textList = new ArrayList<>();
                        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText("For symbol ")));
                        textList.addAll(symbol.toTextList());
                        textList.add(ParseUtilities.newRegularText(", cannot understand the set condition '" + element.buildSetConditionString().trim() + "' as a complete sentence."));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Function:", textList);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static Pair<Boolean, Integer> findColonNext(List<Expression> expressions, Integer index) {
        if (expressions == null || index == null) return new Pair<>(false, 0);
        while (index < expressions.size()) {
            if (expressions.get(index).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                String elementStr = ((OriginalElement) expressions.get(index)).getElementStr();
                if (elementStr.equals(" ")) { }
                else if (elementStr.equals(":")) return new Pair(true, index);
                else return new Pair(false, 0);
            }
            else return new Pair(false, 0);
            index = index + 1;
        }
        return new Pair<>(false, 0);
    }

    private static boolean nextCharColon(List<Expression> expressions, Integer index) {
        if (expressions == null || index == null) return false;
        Integer i = new Integer(index);
        while (i + 1 < expressions.size()) {
            i++;
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                String elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (elementStr.equals(" ")) { }
                else if (elementStr.equals(":")) return true;
                else return false;
            }
            else return false;
        }
        return false;
    }

    private static void trimExpressionList(List<Expression> list) {
        if (list == null) return;
        String regex = "[\\p{Punct}&&[^(){}\\[\\]]]";
        while (list.size() > 0 && list.get(0).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) list.get(0)).getElementStr().matches("[\\p{P}\\p{Z}&&[^{}]]")) {
            list.remove(0);
        }
        while (list.size() > 0 && list.get(list.size() - 1).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) list.get(list.size() - 1)).getElementStr().matches("[\\p{P}\\p{Z}&&[^{}]]")) {
            list.remove(list.size() - 1);
        }
    }

    private static void processLanguageElements(List<Expression> expressions, Language language) {
        if (expressions == null || language == null) return;
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            String supString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getOnePlaceFunctionSymbols() != null && language.getOnePlaceFunctionSymbols().contains(elementStr)) {
                    FunctionSymbol functionSymbol = new FunctionSymbol(elementStr, "", "", 1, false);
                    expressions.set(i, functionSymbol);
                    continue;
                }
                if (language.getTwoPlaceFunctionSymbols() != null && language.getTwoPlaceFunctionSymbols().contains(elementStr)) {
                    FunctionSymbol functionSymbol = new FunctionSymbol(elementStr, "", "", 2, language.isAllowBinaryInfixFunctions());
                    expressions.set(i, functionSymbol);
                    continue;
                }
                if (language.getXfunctionSymbols() != null && language.getXfunctionSymbols().contains(elementStr)) {
                    int j = i + 1;
                    if (ParseUtilities.isFollowingSuperscript(j, expressions)) {
                        if (language.isXfunctionSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            subString = ParseUtilities.getSubString(j, expressions);
                            if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                supString = ParseUtilities.getSupString(j, expressions);
                        } else if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            supString = ParseUtilities.getSupString(j, expressions);
                            if (language.isXfunctionSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                subString = ParseUtilities.getSubString(j, expressions);
                        }
                        FunctionSymbol functionSymbol;
                        if (!language.isMetalanguage())
                            functionSymbol = new FunctionSymbol(elementStr, subString, supString, Integer.parseInt(supString), false);
                        else
                            functionSymbol = MFunctionSymbol.getInstance(elementStr, subString, supString, Integer.parseInt(supString), false);
                        expressions.set(i, functionSymbol);
                    }
                }
            }
        }
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getConstants() != null && language.getConstants().contains(elementStr)) {
                    int j = i + 1;
                    if (language.isConstantSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = ParseUtilities.getSubString(j, expressions);
                    ConstantSym constant;
                    if (!language.isMetalanguage()) constant = new ConstantSym(elementStr, subString);
                    else constant = MConstant.getInstance(elementStr, subString);
                    expressions.set(i, constant);
                }
            }
        }
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                Language metaSupp = Languages.getLanguage("Meta Supp");
                UnivSym univSym = null;
                if (language.getNameString().equals("Meta Sym") && metaSupp.getMetUnivSymbols().contains(elementStr)) {
                    univSym = MetUnivSym.getInstance(elementStr);
                    if (nextCharColon(expressions, i)) expressions.set(i, univSym);
                }
                else if (!language.getNameString().equals("Meta Sym") && metaSupp.getObjUnivSymbols().contains(elementStr)) {
                    univSym = new ObjUnivSym(elementStr);
                    if (nextCharColon(expressions, i)) expressions.set(i, univSym);
                }
            }
        }
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            String supString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getOnePlaceRelSymbols() != null && language.getOnePlaceRelSymbols().contains(elementStr)) {
                    expressions.set(i, new RelationSymbol(elementStr, "", "", 1));
                    continue;
                }
                if (language.getInfixRelations() != null) {
                    if (language.getInfixRelations().containsKey(elementStr)) {
                        RelationSymbol relationSymbol = new RelationSymbol(elementStr, "", "", 2);
                        relationSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());
                        expressions.set(i, relationSymbol);
                        continue;
                    }
                }
                if (language.getXrelationSymbols() != null && language.getXrelationSymbols().contains(elementStr) ) {
                    int j = i + 1;
                    int places = ParseUtilities.areFollowingTerms(j, expressions);
                    if (ParseUtilities.isFollowingSuperscript(j, expressions) || (!language.isXrelationSymbolsRequireSuper() && places > 0)) {
                        if (language.isXrelationSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            subString = ParseUtilities.getSubString(j, expressions);
                            if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                supString = ParseUtilities.getSupString(j, expressions);
                        } else if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            supString = ParseUtilities.getSupString(j, expressions);
                            if (language.isXrelationSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                subString = ParseUtilities.getSubString(j, expressions);
                        }
                        if (!supString.isEmpty()) places = Integer.parseInt(supString);
                        RelationSymbol relationSymbol;
                        if (!language.isMetalanguage())
                            relationSymbol = new RelationSymbol(elementStr, subString, supString, places);
                        else relationSymbol = MRelationSymbol.getInstance(elementStr, subString, supString, places);
                        expressions.set(i, relationSymbol);
                    }
                }
            }
        }
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getSentenceLetters() != null && language.getSentenceLetters().contains(elementStr)) {
                    int j = i + 1;
                    if (language.isSentenceLetterSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = ParseUtilities.getSubString(j, expressions);
                    SentenceLetter letter;
                    if (!language.isMetalanguage()) letter = new SentenceLetter(elementStr, subString);
                    else letter = MSentenceLetter.getInstance(elementStr, subString);
                    expressions.set(i, letter);
                }
            }
        }
    }

    public static List<OriginalElement> getElements(Document doc) {
        if (doc == null) return new ArrayList<>();
        List<OriginalElement> elements = new ArrayList<OriginalElement>();
        String text = doc.getText();
        if (text == null) return elements;
        String s;
        int i = 0;
        while (i < text.length()) {
            int len = 1;
            if (i + 2 <= text.length() && text.codePointCount(i, i + 2) == 1) {
                len = 2;
            }
            s = text.substring(i, i + len);
            TextDecoration dec = null;
            if (doc.getDecorations() != null) {
                dec = (TextDecoration) (ParseUtilities.getDecorationAtIndex(i, doc.getDecorations()).getDecoration());
            }
            OriginalElement element = new OriginalElement(s, dec, i);
            elements.add(element);
            i = i + len;
        }
        i = 0;
        while (i < elements.size()) {
            String elementStr = elements.get(i).getElementStr();
            if (elementStr != null && !elementStr.isEmpty()) {
                char ch = elementStr.charAt(0);
                boolean bad = elementStr.length() == 1 && 0xD800 <= (int) ch && (int) ch <= 0xDFFF;
                if (Character.isISOControl(ch) || bad) {
                    elements.remove(i);
                    continue;
                }
            }
            i = i + 1;
        }
        return elements;
    }

    public void updateCounter() {
        if (isTornDown || transTreeExercise == null || transTreeView == null || transTreeExercise.getMainWindow() == null) return;
        try {
            if (checkMax != -1 && checkTries >= checkMax && !transTreeExercise.getMainWindow().isInstructorFunctions()) {
                if (transTreeView.getCheckButton() != null) transTreeView.getCheckButton().setDisable(true);
            }
            String checkString;
            if (checkMax == -1) checkString = "(unlimited)";
            else if (checkMax == 0) checkString = "(none)";
            else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";
            if (transTreeView.getCheckTriesLabel() != null) transTreeView.getCheckTriesLabel().setText(checkString);
        } catch (Exception e) {}
    }

    public int getCheckTries() { return checkTries; }
    public void setCheckTries(int checkTries) { this.checkTries = checkTries; }
    public boolean isCheckFinal() { return checkFinal; }
    public void setCheckFinal(boolean checkFinal) { this.checkFinal = checkFinal; }
    public boolean isCheckSuccess() { return checkSuccess; }
    public void setCheckSuccess(boolean checkSuccess) { this.checkSuccess = checkSuccess; }
    public AnchorPane getParseTreePane() { return parseTreePane; }
    public static String getProcessInterpLang() { return processInterpLang; }
}