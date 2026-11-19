package slapp.editor.vert_tree_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.m_parse.MLanguage;
import slapp.editor.m_parse.MParseUtilities;
import slapp.editor.m_parse.MSyntacticalFns;
import slapp.editor.m_parse.m_grammatical_parts.MInterpretation;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.FunctionSymbol;
import slapp.editor.parser.symbols.RelationSymbol;
import slapp.editor.vertical_tree.*;
import slapp.editor.vertical_tree.drag_drop.*;
import slapp.editor.vertical_tree.object_models.ObjectControlType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.event.EventHandler;
import java.util.*;


public class VTExpCheck implements VTAuxCheck {
    private VerticalTreeExpExercise vtExpExercise;
    private VerticalTreeExpView vtExpView;
    private VerticalTreeExpModel vtExpModel;
    private VTcheckSetup checkSetup;
    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    List<Node> checkNodeList;
    private List<List<ExpTreeNode>> formulaTree;
    private List<List<String>> lineJustifications;
    private double equalsEpsilon = 10.0;
    private boolean checkJustification = true;
    private boolean checkValueJustification;
    private boolean checkMarkup = true;
    private List<ExpTreeNode> connectedNodes;
    private List<Formula> quantifiedSubformulas;

    private String objLangName;
    private Language objLang;
    private MLanguage mLang;
    private boolean langAllowDP;
    private Expression targetExpression = null;
    private VTCheckType checkType;

    private boolean hasTerms;
    private boolean hasFormulas;
    private double minTermY;
    private double maxTermY;
    private double minFormulaY;
    private double maxFormulaY;
    private ExpTreeNode rootNode;

    private boolean checkBracket;
    private boolean checkDashedLine;
    private boolean checkCircle;
    private boolean checkStar;
    private boolean checkMapping;
    private boolean checkBoxing;
    private boolean checkUnderline;

    private boolean silent;
    private VTAuxExer auxExerA;
    private VTAuxCheck vtAuxCheckA;
    private MParseUtilities mParseUtilities = new MParseUtilities();

    // SCORCHED EARTH RULE 1: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Named Handlers
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    private void scrollToNode(Node node) {
        if (vtExpExercise != null && vtExpExercise.getMainWindow() != null && vtExpExercise.getMainWindow().getMainView() != null) {
            vtExpExercise.getMainWindow().getMainView().scrollToNode(node);
        }
    }

    VTExpCheck(VerticalTreeExpExercise vtExpExercise) {
        this.vtExpExercise = vtExpExercise;
        this.vtExpView = vtExpExercise.getExerciseView();
        this.vtExpModel = vtExpExercise.getExerciseModel();

        checkSetup = vtExpModel.getCheckSetup();
        if (checkSetup == null) { checkSetup = new VTcheckSetup(); vtExpModel.setCheckSetup(checkSetup); }
        mLang = new MLanguage();
        objLangName = checkSetup.getObjLangName();
        objLang = Languages.getLanguage(objLangName);
        setLangAllowDP(objLang.isAllowDroppedBrackets());

        Document targetDoc = checkSetup.getFormulaTarget();
        if (targetDoc != null) {
            objLang.setAllowDroppedBrackets(true);
            List<Expression> targetParseList = ParseUtilities.parseDoc(targetDoc, objLang);
            if (targetParseList.size() > 0) targetExpression = targetParseList.get(0);
        }
        objLang.setAllowDroppedBrackets(langAllowDP);
        checkJustification = checkSetup.isCheckJustifications();
        checkValueJustification = false;
        if (checkJustification) checkValueJustification = true;
        checkMarkup = checkSetup.isCheckMarkup();
        checkType = checkSetup.getCheckType();

        checkBracket = vtExpModel.getDragIconList().contains(DragIconType.bracket);
        checkDashedLine = vtExpModel.getDragIconList().contains(DragIconType.dashed_line);
        checkCircle = vtExpModel.getObjectControlList().contains(ObjectControlType.CIRCLE);
        checkStar = vtExpModel.getObjectControlList().contains(ObjectControlType.STAR);
        checkMapping = vtExpModel.getObjectControlList().contains(ObjectControlType.MAPPING);
        checkBoxing = vtExpModel.getObjectControlList().contains(ObjectControlType.FORMULA_BOX);
        checkUnderline = vtExpModel.getObjectControlList().contains(ObjectControlType.UNDERLINE);

        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            vtExpView.setCheckColor(Color.LAWNGREEN); vtExpView.setCheckElementsColor(Color.GREEN); vtExpView.setCheckMessage("Tree");
        } else {
            vtExpView.setCheckColor(Color.ORCHID); vtExpView.setCheckElementsColor(Color.PURPLE); vtExpView.setCheckMessage("Nodes: Good");
        }
        if (checkSuccess) vtExpView.activateBigCheck();
        else vtExpView.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();
        setRightControlBox();
    }

    private void setRightControlBox() {
        if (vtExpView == null) return;
        if (checkType == VTCheckType.NONE && vtExpView.getCheckButton() != null) vtExpView.getCheckButton().setDisable(true);
        if (vtExpView.getCheckButton() != null) {
            checkButtonHandler = e -> {
                if (isTornDown || vtExpExercise == null || vtExpView == null) return;
                checkTree();
            };
            vtExpView.getCheckButton().setOnAction(checkButtonHandler);
        }
        if (vtExpView.getstaticHelpButton() != null) {
            vtExpView.getstaticHelpButton().setDisable(checkSetup == null || !checkSetup.isStaticHelp());
            staticHelpButtonHandler = e -> {
                if (isTornDown || vtExpExercise == null || vtExpView == null || vtExpModel == null) return;
                Stage helpStage = vtExpView.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else if (vtExpModel.getCheckSetup() != null && vtExpModel.getCheckSetup().getStaticHelpDoc() != null) vtExpView.showStaticHelp(vtExpModel.getCheckSetup().getStaticHelpDoc());
            };
            vtExpView.getstaticHelpButton().setOnAction(staticHelpButtonHandler);
        }
    }

    private void setChecksCounter() {
        if (vtExpView == null || vtExpExercise == null || vtExpExercise.getMainWindow() == null) return;
        if (checkMax != -1 && checkTries >= checkMax && !vtExpExercise.getMainWindow().isInstructorFunctions()) {
            if (vtExpView.getCheckButton() != null) vtExpView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";
        if (vtExpView.getCheckTriesLabel() != null) vtExpView.getCheckTriesLabel().setText(checkString);
    }

    // SCORCHED EARTH RULE 5, 7, & 10: Heavily Isolated Cleanup, Cache Clearing, & Non-Destructive Teardown
    public void cleanup() {
        isTornDown = true;

        try { MatchUtilities.clearMatching(); } catch (Exception e) {}

        try {
            if (vtExpView != null) {
                if (vtExpView.getCheckButton() != null) {
                    if (checkButtonHandler != null) vtExpView.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                    vtExpView.getCheckButton().setOnAction(null);
                }
                if (vtExpView.getstaticHelpButton() != null) {
                    if (staticHelpButtonHandler != null) vtExpView.getstaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                    vtExpView.getstaticHelpButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}

        try { this.formulaTree = null; } catch (Exception e) {}
        try { this.lineJustifications = null; } catch (Exception e) {}
        try { this.connectedNodes = null; } catch (Exception e) {}
        try { this.quantifiedSubformulas = null; } catch (Exception e) {}
        try { this.checkNodeList = null; } catch (Exception e) {}

        try { this.vtExpExercise = null; } catch (Exception e) {}
        try { this.vtExpView = null; } catch (Exception e) {}
        try { this.vtExpModel = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.objLang = null; } catch (Exception e) {}
        try { this.mLang = null; } catch (Exception e) {}
        try { this.targetExpression = null; } catch (Exception e) {}
        try { this.rootNode = null; } catch (Exception e) {}
        try { this.auxExerA = null; } catch (Exception e) {}
        try { this.vtAuxCheckA = null; } catch (Exception e) {}
        try { this.mParseUtilities = null; } catch (Exception e) {}
    }

    public boolean checkTree() {
        if (vtExpExercise == null || vtExpView == null) return false;
        switch (checkType) {
            case FORMULA: vtExpView.setCheckMessage("Tree"); this.silent = false; return checkFormulaTree(checkMarkup);
            case UNABB: vtExpView.setCheckMessage("Tree"); this.silent = false; return checkUnabbTree(false);
            case UNABB_AUX: this.silent = true; this.checkJustification = false; return checkFormulaTree(false);
            case TRUTH_VALUE: vtExpView.setCheckMessage("Tree / Values"); this.silent = false; checkJustification = false; langAllowDP = true; return checkTruthValueTree();
            case TERM_VALUE: vtExpView.setCheckMessage("Tree / Values"); this.silent = false; checkJustification = false; langAllowDP = true; return checkTermValueTree();
            case MAPPING: return false;
        }
        return false;
    }

    private boolean checkFormulaTree(boolean checkMarkup) {
        if (vtExpExercise == null || vtExpView == null) return false; checkSuccess = false; vtExpView.deactivateBigCheck();
        if (vtExpExercise.getMainWindow() != null && !vtExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>(); lineJustifications = new ArrayList<>();
        if (vtExpView.getRootLayout() == null || vtExpView.getRootLayout().getMain_pane() == null) return false;
        checkNodeList = vtExpView.getRootLayout().getMain_pane().getChildren();
        if (checkNodeList.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate."))); return false; }
        List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
        if (sortedFormulaBoxes.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate."))); return false; }
        if (!populateTreeLists(sortedFormulaBoxes)) return false;
        if (!setParentsAndChildren()) return false;
        if (!checkTrueTree()) return false;
        if (!checkContents()) return false;
        if (checkMarkup && !checkMarkup()) return false;
        checkSuccess = true; vtExpView.activateBigCheck(); return true;
    }

    private boolean checkUnabbTree(boolean checkMarkup) {
        if (vtExpExercise == null || vtExpView == null) return false; checkSuccess = false; vtExpView.deactivateBigCheck();
        if (vtExpExercise.getMainWindow() != null && !vtExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>(); lineJustifications = new ArrayList<>();
        if (vtExpView.getRootLayout() == null || vtExpView.getRootLayout().getMain_pane() == null) return false;
        checkNodeList = vtExpView.getRootLayout().getMain_pane().getChildren();
        if (checkNodeList.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate."))); return false; }
        List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
        if (sortedFormulaBoxes.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate."))); return false; }
        auxExerA = vtExpExercise.getVTAuxExer();
        if (auxExerA == null) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Auxiliary Tree", Collections.singletonList(ParseUtilities.newRegularText("Missing auxiliary vertical tree.  Cannot check unabbreviation."))); return false; }
        vtAuxCheckA = auxExerA.getVTAuxCheck();
        if (vtAuxCheckA != null) vtAuxCheckA.setCheckType(VTCheckType.UNABB_AUX);
        if (vtAuxCheckA == null || !vtAuxCheckA.checkTree()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Auxiliary Tree", Collections.singletonList(ParseUtilities.newRegularText("Unabbreviation check requires (successfully) completed auxiliary tree."))); return false; }
        if (!populateTreeLists(sortedFormulaBoxes)) return false;
        if (!setParentsAndChildren()) return false;
        if (!checkTrueTree()) return false;
        if (!checkFormulaContents()) return false;
        List<List<? extends VTAuxTreeNode>> auxFormulaTree = vtAuxCheckA.getAuxFormulaTree();
        try { checkParallelTrees(formulaTree.get(formulaTree.size() - 1).get(0), auxFormulaTree.get(auxFormulaTree.size() - 1).get(0)); } catch (TreeNodeException e) {
            if (!silent) { e.getAuxTreeNode().getMainFormulaBox().setVTtreeBoxHighlight(); scrollToNode(e.getAuxTreeNode().getMainFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Trees Structure", e.getMessageList()); e.getAuxTreeNode().getMainFormulaBox().resetVTtreeBoxHighlight(); } return false;
        }
        if (!checkAbbContent()) return false;
        checkSuccess = true; vtExpView.activateBigCheck(); return true;
    }

    private boolean checkTruthValueTree() {
        if (vtExpExercise == null || vtExpView == null) return false; checkSuccess = false; vtExpView.deactivateBigCheck();
        if (vtExpExercise.getMainWindow() != null && !vtExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>(); lineJustifications = new ArrayList<>();
        if (vtExpView.getRootLayout() == null || vtExpView.getRootLayout().getMain_pane() == null) return false;
        checkNodeList = vtExpView.getRootLayout().getMain_pane().getChildren();
        if (checkNodeList.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate."))); return false; }
        List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
        if (sortedFormulaBoxes.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate."))); return false; }
        if (!populateTreeLists(sortedFormulaBoxes)) return false;
        if (!setParentsAndChildren()) return false;
        if (!checkTrueTree()) return false;
        if (!checkContents()) return false;
        if (!checkTruthValues()) return false;
        checkSuccess = true; vtExpView.activateBigCheck(); return true;
    }

    private boolean checkTermValueTree() {
        if (vtExpExercise == null || vtExpView == null) return false; checkSuccess = false; vtExpView.deactivateBigCheck();
        if (vtExpExercise.getMainWindow() != null && !vtExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>(); lineJustifications = new ArrayList<>();
        if (vtExpView.getRootLayout() == null || vtExpView.getRootLayout().getMain_pane() == null) return false;
        checkNodeList = vtExpView.getRootLayout().getMain_pane().getChildren();
        if (checkNodeList.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate."))); return false; }
        List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
        if (sortedFormulaBoxes.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate."))); return false; }
        if (!populateTreeLists(sortedFormulaBoxes)) return false;
        if (!setParentsAndChildren()) return false;
        if (!checkTrueTree()) return false;
        if (!checkContents()) return false;
        if (!checkTermValues()) return false;
        checkSuccess = true; vtExpView.activateBigCheck(); return true;
    }

    private boolean checkTermValues() {
        if (vtExpExercise == null || vtExpModel == null || vtExpModel.getCheckSetup() == null) return false;
        if (!setTermValues()) return false;
        if (formulaTree == null || formulaTree.isEmpty()) return false;
        MSyntacticalFns syntacticalFns = new MSyntacticalFns(vtExpExercise.getMainWindow());
        if (syntacticalFns.setInterpBaseVarAssign(vtExpModel.getExerciseStatement(), vtExpModel.getCheckSetup().getObjLangName())) {
            List<Expression> expressions = mParseUtilities.parseDoc(vtExpModel.getExerciseStatement(), objLangName);
            MInterpAssignTerm interpAssignTerm = null;
            for (Expression exp : expressions) { if (exp.getType() == ExpressionType.M_INTERP_ASSIGN_TERM) { interpAssignTerm = (MInterpAssignTerm) exp; } }
            if (interpAssignTerm == null) {
                List<Text> textList = new ArrayList<>(); textList.add(ParseUtilities.newRegularText("The exercise statement does not include the expression to be evaluated - as \ud835\udda8")); textList.add(ParseUtilities.newSubscriptText("\ud835\uddbd(\ud835\udccd|\u2134)")); textList.add(ParseUtilities.newRegularText("[\ud835\udcab] for some interpretation, variable assignment, (variable, object), and formula.")); EditorAlerts.showSimpleTxtListAlert("Setup Issue:", textList); return false;
            }
            Term targetTerm = interpAssignTerm.getTerm();
            if (!((Term) targetExpression).equals(targetTerm)) { EditorAlerts.showSimpleTxtListAlert("Setup Issue", Collections.singletonList(ParseUtilities.newRegularText("Statement formula not same as target created for exercise."))); return false; }
            MInterpretationAssignment interpAssignment = interpAssignTerm.getInterpAssign(); MInterpretationVariable interpVariable = interpAssignment.getInterpVariable(); MInterpretation interpretation = syntacticalFns.getInterpretations().getInterpretation(interpVariable);
            MAssignment assWithMods = interpAssignment.getAssignment(); MAssignmentVariable assignmentVariable = assWithMods.getAssignmentVariable(); Set<MAssignmentMod> assignmentMods = assWithMods.getAssignmentMods(); Map<Term, MBasicObjTerm> varAssignment = syntacticalFns.getBaseVarAssign().getAssignment(assignmentVariable);
            for (MAssignmentMod mod : assignmentMods) { varAssignment.put(mod.getVariable(), mod.getObject()); }
            List<ExpTreeNode> topRow = formulaTree.get(0);
            for (int i = 0; i < topRow.size(); i++) {
                ExpTreeNode treeNode = topRow.get(i); BoxedDRTA treeNodeBDRTA = treeNode.getMainTreeBox().getFormulaBox(); RichTextArea treeNodeRTA = treeNodeBDRTA.getRTA(); treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document nodeDoc = treeNodeRTA.getDocument(); List<Expression> nodeParse = ParseUtilities.parseDoc(nodeDoc, objLangName); if (nodeParse.isEmpty()) return false; Expression treeNodeExpr = nodeParse.get(0);
                if (treeNodeExpr.getType() != ExpressionType.TERM) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Expression is not a term of " + objLangName + "."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; } Term treeNodeTerm = (Term) treeNodeExpr;
                if (!(treeNodeTerm.getTermType() == TermType.CONSTANT || treeNodeTerm.getTermType() == TermType.VARIABLE)) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Top row must consist entirely of variabls and constants."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                if (treeNodeTerm.getTermType() == TermType.CONSTANT) {
                    MBasicObjTerm object = interpretation.getConstantVal(treeNodeTerm);
                    if (object == null) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this constant."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                    if (!object.equals(treeNode.getMainTreeBox().getSemanticValue())) { BoxedDRTA annBDRTA = treeNode.getMainTreeBox().getAnnotationField(); annBDRTA.setAnnBoxHighlight(); scrollToNode(annBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value."))); annBDRTA.resetAnnHighlight(); return false; }
                    if (checkValueJustification && !lineJustifications.get(0).get(i).equals("TA(c)")) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect justification for this node."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                }
                if (treeNodeTerm.getTermType() == TermType.VARIABLE) {
                    MBasicObjTerm object = varAssignment.get(treeNodeTerm);
                    if (object == null) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Variable assignment gives no value to this variable."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                    if (!object.equals(treeNode.getMainTreeBox().getSemanticValue())) { BoxedDRTA annBDRTA = treeNode.getMainTreeBox().getAnnotationField(); annBDRTA.setAnnBoxHighlight(); scrollToNode(annBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value."))); annBDRTA.resetAnnHighlight(); return false; }
                    if (checkValueJustification && !lineJustifications.get(0).get(i).equals("TA(v)")) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect justification for this node."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                }
            }
            for (int j = 1; j < formulaTree.size(); j++) {
                List<ExpTreeNode> treeRowList = formulaTree.get(j);
                for (int i = 0; i < treeRowList.size(); i++) {
                    ExpTreeNode treeNode = treeRowList.get(i); BoxedDRTA treeNodeBDRTA = treeNode.getMainTreeBox().getFormulaBox(); RichTextArea treeNodeRTA = treeNodeBDRTA.getRTA(); treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document nodeDoc = treeNodeRTA.getDocument(); List<Expression> nodeParse = ParseUtilities.parseDoc(nodeDoc, objLangName); if (nodeParse.isEmpty()) return false; Expression treeNodeExpr = nodeParse.get(0);
                    if (treeNodeExpr.getType() != ExpressionType.TERM) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Expression is not a term of " + objLangName + "."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; } Term treeNodeTerm = (Term) treeNodeExpr;
                    if (treeNodeTerm.getTermType() == TermType.CONSTANT || treeNodeTerm.getTermType() == TermType.VARIABLE) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Variables and constants should appear in the top row."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                    if (treeNodeTerm.getTermType() == TermType.COMPLEX) {
                        List<MBasicObjTerm> assignmentTuple = new ArrayList<>(); for (int k = 0; k < treeNode.getParents().size(); k++) { ExpTreeNode child = treeNode.getParents().get(k); assignmentTuple.add(child.getMainTreeBox().getSemanticValue()); }
                        MBasicObjTerm assignedObj = null; FunctionSymbol fnSym = treeNodeTerm.getMainFnSymbol(); MFunctionCandidate fnCandidate = interpretation.getFunctionVal(fnSym);
                        if (fnCandidate == null) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this function symbol."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                        Set<MFnPair> fnPairs = fnCandidate.getPairs(); for (MFnPair fnPair : fnPairs) { if (fnPair.getInput().getMembers().equals(assignmentTuple)) { assignedObj = fnPair.getOutput(); break; } }
                        if (assignedObj == null) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation sets no value for this input."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                        if (!assignedObj.equals(treeNode.getMainTreeBox().getSemanticValue())) { BoxedDRTA annBDRTA = treeNode.getMainTreeBox().getAnnotationField(); annBDRTA.setAnnBoxHighlight(); scrollToNode(annBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value."))); annBDRTA.resetAnnHighlight(); return false; }
                        if (checkValueJustification && !lineJustifications.get(j).get(i).equals("TA(f)")) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect justification for this node."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                    }
                }
            }
        } else { return false; }
        return true;
    }

    private boolean setTermValues() {
        if (vtExpExercise == null || formulaTree == null || formulaTree.isEmpty()) return false;
        for (int j = 0; j < formulaTree.size(); j++) {
            List<ExpTreeNode> treeRowList = formulaTree.get(j);
            for (int i = 0; i < treeRowList.size(); i++) {
                ExpTreeNode treeNode = treeRowList.get(i); ExpTreeFormulaBox treeBox = treeNode.getMainTreeBox();
                if (!treeBox.isAnnotation()) { treeBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Missing annotation field."))); treeBox.getFormulaBox().resetVTtreeBoxHighlight(); return false; }
                MBasicObjTerm obj = extractObjValue(treeBox.getAnnotationField()); if (obj == null) { return false; } else { treeBox.setSemanticValue(obj); }
            }
        }
        return true;
    }

    private MBasicObjTerm extractObjValue(BoxedDRTA bdrta) {
        if (bdrta == null || vtExpExercise == null) return null; RichTextArea rta = bdrta.getRTA(); rta.getActionFactory().saveNow().execute(new ActionEvent()); Document doc = rta.getDocument(); List<Expression> expressions = mParseUtilities.parseObj(doc, objLangName);
        if (expressions.size() == 0) { bdrta.setAnnBoxHighlight(); scrollToNode(bdrta.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No empty value for term."))); bdrta.resetAnnHighlight(); return null; }
        else if (expressions.size() == 3 && expressions.get(0) instanceof MOpenSquareBracket && expressions.get(1) instanceof MBasicObjTerm && expressions.get(2) instanceof MCloseSquareBracket) { return (MBasicObjTerm) expressions.get(1); }
        else { bdrta.setAnnBoxHighlight(); scrollToNode(bdrta.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Cannot parse to expression of the sort, Open square bracket / Object term / Close square bracket."))); bdrta.resetAnnHighlight(); return null; }
    }

    private boolean checkTruthValues() {
        if (vtExpExercise == null || vtExpModel == null || vtExpModel.getCheckSetup() == null) return false;
        if (formulaTree == null || formulaTree.isEmpty()) return false;
        MSyntacticalFns syntacticalFns = new MSyntacticalFns(vtExpExercise.getMainWindow()); syntacticalFns.setSententialInterp(true);
        if (syntacticalFns.setInterpBaseVarAssign(vtExpModel.getExerciseStatement(), vtExpModel.getCheckSetup().getObjLangName())) {
            Map<MInterpretationVariable, MInterpretation> interpretations = syntacticalFns.getInterpretations().getMap();
            if (interpretations.size() == 0) { EditorAlerts.showSimpleTxtListAlert("Tree Check", Collections.singletonList(ParseUtilities.newRegularText("Do not find interpretation in exercise statement.  Cannot check truth values."))); return false; }
            else if (interpretations.size() > 1) { EditorAlerts.showSimpleTxtListAlert("Tree Check", Collections.singletonList(ParseUtilities.newRegularText("More than one interpretation found in exercise statement.  Cannot check truth values."))); return false; }
            else {
                Map.Entry<MInterpretationVariable, MInterpretation> entry = interpretations.entrySet().iterator().next(); MInterpretation interpretation = entry.getValue();
                List<ExpTreeNode> topRow = formulaTree.get(0);
                for (ExpTreeNode treeNode: topRow) {
                    RichTextArea treeNodeRTA = treeNode.getMainTreeBox().getFormulaBox().getRTA(); treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document nodeDoc = treeNodeRTA.getDocument(); List<Expression> nodeParse = ParseUtilities.parseDoc(nodeDoc, objLangName); if (nodeParse.isEmpty()) return false; Expression treeNodeExpr = nodeParse.get(0); Formula treeNodeFormula = (Formula) treeNodeExpr;
                    if (!(treeNodeFormula.isAtomic() && SyntacticalFns.sentence(treeNodeFormula, objLangName))) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Top row must consist entirely of basic sentences."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false; }
                    MTruthValue interpretationValue = interpretation.getFormulaVal(treeNodeFormula);
                    if (interpretationValue == null) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); List<Text> textList = new ArrayList<>(); textList.add(new Text("Truth value for this basic sentence not included in interpretation ")); textList.addAll(entry.getKey().toTextList()); textList.add(new Text(".")); EditorAlerts.showSimpleTxtListAlert("Tree Values", textList); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false; }
                    String annotationStr = treeNode.getMainTreeBox().getAnnotationDoc().getText(); MTruthValue annotationValue = extractTruthValue(annotationStr);
                    if (annotationValue == null) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No truth value set for this expression, as \"(" + mLang.getTrueSym() + ")\" or \"(" + mLang.getFalseSym() + ")\" - where the true/false symbol is from the same (sans) font as in exercise statement."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false; }
                    if (!interpretationValue.equals(annotationValue)) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); List<Text> textList = new ArrayList<>(); textList.add(new Text("Not the value set in interpretation ")); textList.addAll(entry.getKey().toTextList()); textList.add(new Text(".")); EditorAlerts.showSimpleTxtListAlert("Tree Values", textList); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false; }
                }
            }
        } else { return false; }

        for (int j = 1; j < formulaTree.size(); j++) {
            List<ExpTreeNode> treeRowList = formulaTree.get(j);
            for (int i = 0; i < treeRowList.size(); i++) {
                ExpTreeNode treeNode = treeRowList.get(i); RichTextArea treeNodeRTA = treeNode.getMainTreeBox().getFormulaBox().getRTA(); BoxedDRTA treeNodeBDRTA = treeNode.getMainTreeBox().getFormulaBox(); treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document nodeDoc = treeNodeRTA.getDocument(); rootNode = formulaTree.get(formulaTree.size() - 1).get(0); objLang.setAllowDroppedBrackets(false); if (treeNode.equals(rootNode)) objLang.setAllowDroppedBrackets(true); List<Expression> nodeParse = ParseUtilities.parseDoc(nodeDoc, objLang); if (nodeParse.isEmpty()) return false; Expression treeNodeExpr = nodeParse.get(0); Formula treeNodeFormula = (Formula) treeNodeExpr;
                if (treeNodeFormula.isAtomic()) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("All basic sentences should appear in top row."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                else {
                    Operator mainOp = treeNodeFormula.getMainOperator(); String formulaAnnotationStr = treeNode.getMainTreeBox().getAnnotationDoc().getText(); MTruthValue formulaValue = extractTruthValue(formulaAnnotationStr);
                    if (formulaValue == null) { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No truth value set for this expression, as \"(" + mLang.getTrueSym() + ")\" or \"(" + mLang.getFalseSym() + ")\" -  where the true/false symbol is from the same (sans) font as in exercise statement."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                    if (mainOp.getType() == ExpressionType.NEG_OP) {
                        List<ExpTreeNode> parents = treeNode.getParents(); ExpTreeNode parentNode = parents.get(0); String parentAnnotationStr = parentNode.getMainTreeBox().getAnnotationDoc().getText(); MTruthValue parentValue = extractTruthValue(parentAnnotationStr);
                        if (parentValue instanceof MTheTrue && formulaValue instanceof MTheFalse) { if (checkValueJustification) { String justificationStr = lineJustifications.get(j).get(i); if (!justificationStr.matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 1\\s*")) { showJustificationError(treeNodeBDRTA); return false; } } }
                        else if (parentValue instanceof MTheFalse && formulaValue instanceof MTheTrue) { if (checkValueJustification) { String justificationStr = lineJustifications.get(j).get(i); if (!justificationStr.matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 2\\s*")) { showJustificationError(treeNodeBDRTA); return false; } } }
                        else { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect truth value."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }
                    } else if (mainOp.getType() == ExpressionType.COND_OP) {
                        List<ExpTreeNode> parents = treeNode.getParents(); ExpTreeNode parentNodeL = parents.get(0); ExpTreeNode parentNodeR = parents.get(1); String parentAnnotationStrL = parentNodeL.getMainTreeBox().getAnnotationDoc().getText(); String parentAnnotationStrR = parentNodeR.getMainTreeBox().getAnnotationDoc().getText(); MTruthValue parentValueL = extractTruthValue(parentAnnotationStrL); MTruthValue parentValueR = extractTruthValue(parentAnnotationStrR);
                        if (parentValueL instanceof MTheTrue && parentValueR instanceof MTheTrue && formulaValue instanceof MTheTrue) { if (checkValueJustification) { String justificationStr = lineJustifications.get(j).get(i); if (!justificationStr.matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 1\\s*")) { showJustificationError(treeNodeBDRTA); return false; } } }
                        else if (parentValueL instanceof MTheTrue && parentValueR instanceof MTheFalse && formulaValue instanceof MTheFalse) { if (checkValueJustification) { String justificationStr = lineJustifications.get(j).get(i); if (!justificationStr.matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 2\\s*")) { showJustificationError(treeNodeBDRTA); return false; } } }
                        else if (parentValueL instanceof MTheFalse && parentValueR instanceof MTheTrue && formulaValue instanceof MTheTrue) { if (checkValueJustification) { String justificationStr = lineJustifications.get(j).get(i); if (!justificationStr.matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 3\\s*")) { showJustificationError(treeNodeBDRTA); return false; } } }
                        else if (parentValueL instanceof MTheFalse && parentValueR instanceof MTheFalse && formulaValue instanceof MTheTrue) { if (checkValueJustification) { String justificationStr = lineJustifications.get(j).get(i); if (!justificationStr.matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 4\\s*")) { showJustificationError(treeNodeBDRTA); return false; } } }
                        else { treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect truth value."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false; }


                    } else if (mainOp.getType() == ExpressionType.CONJ_OP) {
                        MTruthValue pVL = extractTruthValue(treeNode.getParents().get(0).getMainTreeBox().getAnnotationDoc().getText());
                        MTruthValue pVR = extractTruthValue(treeNode.getParents().get(1).getMainTreeBox().getAnnotationDoc().getText());
                        if (pVL instanceof MTheTrue && pVR instanceof MTheTrue && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 1\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheTrue && pVR instanceof MTheFalse && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 2\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheTrue && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 3\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheFalse && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 4\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect truth value."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }
                    } else if (mainOp.getType() == ExpressionType.DISJ_OP) {
                        MTruthValue pVL = extractTruthValue(treeNode.getParents().get(0).getMainTreeBox().getAnnotationDoc().getText());
                        MTruthValue pVR = extractTruthValue(treeNode.getParents().get(1).getMainTreeBox().getAnnotationDoc().getText());
                        if (pVL instanceof MTheTrue && pVR instanceof MTheTrue && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 1\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheTrue && pVR instanceof MTheFalse && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 2\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheTrue && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 3\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheFalse && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 4\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect truth value."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }
                    } else if (mainOp.getType() == ExpressionType.BICOND_OP) {
                        MTruthValue pVL = extractTruthValue(treeNode.getParents().get(0).getMainTreeBox().getAnnotationDoc().getText());
                        MTruthValue pVR = extractTruthValue(treeNode.getParents().get(1).getMainTreeBox().getAnnotationDoc().getText());
                        if (pVL instanceof MTheTrue && pVR instanceof MTheTrue && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 1\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheTrue && pVR instanceof MTheFalse && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 2\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheTrue && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 3\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheFalse && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\ue8bc?\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 4\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect truth value."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void showJustificationError(BoxedDRTA treeNodeBDRTA) {
        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Correct truth value, bad justification."))); treeNodeBDRTA.resetVTtreeBoxHighlight();
    }

    private MTruthValue extractTruthValue(String str) {
        if (mLang == null || str == null) return null;
        if (str.matches("^\\s*\\(" + mLang.getTrueSym() + "\\)\\s*$")) return new MTheTrue(mLang.getTrueSym());
        else if (str.matches("^\\s*\\(" + mLang.getFalseSym() + "\\)\\s*$")) return new MTheFalse(mLang.getFalseSym());
        else return null;
    }

    private boolean checkAbbContent() {
        if (vtExpExercise == null || formulaTree == null || vtAuxCheckA == null) return false;
        for (int j = 0; j < formulaTree.size(); j++) {
            List<ExpTreeNode> treeRowList = formulaTree.get(j);
            for (int i = 0; i < treeRowList.size(); i++) {
                ExpTreeNode treeNode = treeRowList.get(i); RichTextArea unabbRTA = treeNode.getMainTreeBox().getFormulaBox().getRTA(); unabbRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document unabbDoc = unabbRTA.getDocument(); List<Expression> unabbParse = ParseUtilities.parseDoc(unabbDoc, objLangName); if (unabbParse.isEmpty()) return false; Expression unabbExpr = unabbParse.get(0);
                VTAuxTreeNode abbNode = treeNode.getMate(); RichTextArea abbRTA = abbNode.getMainFormulaBox().getRTA(); abbRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document abbDoc = abbRTA.getDocument(); List<Expression> abbParse = ParseUtilities.parseDoc(abbDoc, vtAuxCheckA.getObjLangName()); if (abbParse.isEmpty()) return false; Expression abbExpr = abbParse.get(0);
                if (!unabbExpr.equals(SyntacticalFns.unabbreviate(abbExpr, vtAuxCheckA.getObjLangName()))) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not the unabbreviation of the corresponding node from the auxiliary tree."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                if (checkJustification) {
                    if (abbExpr instanceof Term) {
                        Term abbTerm = (Term) abbExpr; TermType abbTermType = abbTerm.getTermType();
                        if (abbTermType == TermType.VARIABLE) { if (!lineJustifications.get(j).get(i).equals("TR(v)")) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If term results from (nothing or) linked nodes by TR use that justification."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        else if (abbTermType == TermType.CONSTANT) { if (!lineJustifications.get(j).get(i).equals("TR(c)")) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If term results from (nothing or) linked nodes by TR use that justification."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        else if (abbTermType == TermType.COMPLEX) {
                            if (abbTerm instanceof InfixTerm) { if (!lineJustifications.get(j).get(i).equals("AB(i)")) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Unabbreviation of infix term has incorrect justification (use AB(i))."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                            else if (!lineJustifications.get(j).get(i).equals("TR(f)")) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If term results from (nothing or) linked nodes by TR use that justification."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                        }
                    } else if (abbExpr instanceof Formula) {
                        Formula abbFormula = (Formula) abbExpr;
                        if (abbFormula instanceof SentenceAtomic) { if (!lineJustifications.get(j).get(i).equals("FR(s)")) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If formula results from (nothing or) linked nodes by FR use that justification."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        else if (abbFormula instanceof PrefixAtomic) { if (!lineJustifications.get(j).get(i).equals("FR(r)")) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If formula results from (nothing or) linked nodes by FR use that justification."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        else if (abbFormula instanceof InfixAtomic) { if (!lineJustifications.get(j).get(i).equals("AB(i)")) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Unabbreviation of infix atomic has incorrect justification (use AB(i))."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        else {
                            Operator abbOp = abbFormula.getMainOperator(); String justificationString = "";
                            if (abbOp instanceof NegationOp || abbOp instanceof ConditionalOp || abbOp instanceof UniversalOp) { justificationString = "FR(" + abbOp.getMainSymbol().toString() + ")"; if (!lineJustifications.get(j).get(i).equals(justificationString)) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If formula results from (nothing or) linked nodes by FR use that justification."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                            else if (abbOp instanceof ConjunctionOp || abbOp instanceof DisjunctionOp || abbOp instanceof BiconditionalOp || abbOp instanceof ExistentialOp) { justificationString = "AB(" + abbOp.getMainSymbol().toString() + ")"; if (!lineJustifications.get(j).get(i).equals(justificationString)) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); List<Text> messageList = new ArrayList<>(); messageList.add(ParseUtilities.newRegularText("Unabbreviation of formula with main operator ")); messageList.addAll(abbOp.toTextList()); messageList.add(ParseUtilities.newRegularText(" has incorrect justification.")); EditorAlerts.showSimpleTxtListAlert("Tree Content", messageList); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void checkParallelTrees(ExpTreeNode mainNode, VTAuxTreeNode auxNode) throws TreeNodeException {
        if (mainNode == null || auxNode == null) return; mainNode.setMate(auxNode);
        if (mainNode.getParents().size() != auxNode.getParents().size()) { throw new TreeNodeException(mainNode, Collections.singletonList(ParseUtilities.newRegularText("Structures of this and the auxiliary tree are not parallel."))); }
        else { for (int i = 0; i < mainNode.getParents().size(); i++) { checkParallelTrees(mainNode.getParents().get(i), auxNode.getParents().get(i)); } }
    }

    private boolean checkFormulaContents() {
        if (vtExpExercise == null || formulaTree == null || objLang == null || formulaTree.isEmpty()) return false;
        rootNode = formulaTree.get(formulaTree.size() - 1).get(0);
        for (int j = 0; j < formulaTree.size(); j++) {
            List<ExpTreeNode> treeList = formulaTree.get(j);
            for (int i = 0; i < treeList.size(); i++) {
                ExpTreeNode treeNode = treeList.get(i); RichTextArea rta = treeNode.getMainTreeBox().getFormulaBox().getRTA(); rta.getActionFactory().saveNow().execute(new ActionEvent()); Document doc = rta.getDocument(); objLang.setAllowDroppedBrackets(false); List<Expression> parseExp = ParseUtilities.parseDoc(doc, objLang);
                if (parseExp.isEmpty()) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Empty node."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                List<Expression> parseExpDP = new ArrayList<>();
                if (parseExp.size() > 1) {
                    objLang.setAllowDroppedBrackets(true); parseExpDP = ParseUtilities.parseDoc(doc, objLang);
                    if (parseExpDP.size() != 1) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + "."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                    else { if (treeNode != rootNode || !langAllowDP) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression (check outer parentheses)."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                }
                Expression exp = null; if (parseExp.size() == 1) exp = parseExp.get(0); else if (parseExpDP.size() == 1) exp = parseExpDP.get(0);
                if (!(exp instanceof Term) && !(exp instanceof Formula)) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + "."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
            }
        }
        objLang.setAllowDroppedBrackets(langAllowDP);
        return true;
    }

    private boolean checkMarkup() {
        if (vtExpExercise == null || rootNode == null || objLang == null || checkSetup == null) return false;
        if (hasFormulas) {
            List<ExpTreeNode> immediateSubs = rootNode.getParents(); ExpTreeFormulaBox rootBox = rootNode.getMainTreeBox(); RichTextArea rootRTA = rootBox.getFormulaBox().getRTA(); rootRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document rootDoc = rootRTA.getDocument(); objLang.setAllowDroppedBrackets(langAllowDP); List<Expression> rootParseList = ParseUtilities.parseDoc(rootDoc, objLang); if (rootParseList.isEmpty()) return false; Expression rootNodeExp = rootParseList.get(0);
            if (rootNodeExp instanceof Formula && !((Formula) rootNodeExp).isAtomic()) {
                if (checkCircle) {
                    List<Expression> circleExpressions = getCircleText(rootNode.getMainTreeBox());
                    if (circleExpressions.size() == 0) { if (!silent) { rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No circled text."))); rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                    else if (circleExpressions.size() > 1) { if (!silent) { rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not a grammatical expression."))); rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                    Expression circleExp = circleExpressions.get(0);
                    if (!(circleExp instanceof Operator)) { if (!silent) { rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not an operator."))); rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                    if (!(((Formula) rootNodeExp).getMainOperator().equals(circleExp)) || ((Operator) circleExp).getPosition() != ((Formula) rootNodeExp).getMainOperator().getPosition()) { if (!silent) { rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not the main operator of the root node."))); rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                }
                if (checkBoxing) { for (ExpTreeNode immediateSub : immediateSubs) { if (!immediateSub.getMainTreeBox().isBoxed()) { if (!silent) { rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Immediate subformula not boxed."))); rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } } }
            }
            for (int j = 0; j < formulaTree.size(); j++) {
                List<ExpTreeNode> treeList = formulaTree.get(j);
                for (int i = 0; i < treeList.size(); i++) {
                    ExpTreeNode treeNode = treeList.get(i);
                    ExpTreeFormulaBox expTreeFormulaBox = treeNode.getMainTreeBox();
                    RichTextArea rta = expTreeFormulaBox.getFormulaBox().getRTA(); rta.getActionFactory().saveNow().execute(new ActionEvent());
                    Document doc = rta.getDocument();
                    objLang.setAllowDroppedBrackets(langAllowDP); List<Expression> parseList = ParseUtilities.parseDoc(doc, objLang);
                    if (parseList.isEmpty()) return false;
                    Expression treeNodeExp = parseList.get(0);
                    if (treeNodeExp instanceof Term) {
                        if (!expTreeFormulaBox.getUlineIndexes().isEmpty()) {
                            if (!silent) {
                                expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No underline on terms.")));
                                expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (expTreeFormulaBox.isStarred()) {
                            if (!silent) {
                                expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Star applied term (not atomic formula).")));
                                expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (expTreeFormulaBox.isBoxed()) {
                            if (!silent) {
                                expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Box applied term (not formula).")));
                                expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }

                    }
                    if (treeNodeExp instanceof Formula) {
                        Formula treeNodeFormula = (Formula) treeNodeExp;
                        if (checkStar) {
                            if (treeNodeFormula.isAtomic() && !expTreeFormulaBox.isStarred()) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Atomic not marked with star."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                            if (!treeNodeFormula.isAtomic() && expTreeFormulaBox.isStarred()) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Star applied to non-atomic formula."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                        }
                        if (checkCircle) { if (treeNode != rootNode && expTreeFormulaBox.isCircled()) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circle applied node other than root."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        if (checkBoxing) { if (expTreeFormulaBox.isBoxed() && !immediateSubs.contains(treeNode)) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Box applied to node other than an immediate subformula."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; } }
                        if (checkUnderline) {
                            if (!expTreeFormulaBox.isUlineInclusion()) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline not completely \"included\" in ones below."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                            List<List<Expression>> underlineTexts = getUnderlineTexts(expTreeFormulaBox); List<Formula> underlineFormulas = new ArrayList<>();
                            for (List<Expression> underlineExpressions : underlineTexts) {
                                if (underlineExpressions.size() > 1) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline text is not a grammatical expression."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                                Expression underlineExp = underlineExpressions.get(0);
                                if (!(underlineExp instanceof Formula)) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline text is not a (sub-)formula."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                                else { Formula underlineFormula = (Formula) underlineExp; if (!(underlineFormula.getMainOperator() instanceof UniversalOp) && !(underlineFormula.getMainOperator() instanceof ExistentialOp)) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Main operator of underline text not a quantifier."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; } underlineFormulas.add(underlineFormula); }
                            }
                            quantifiedSubformulas = new ArrayList<>(); populateQuantifiedSubformulas(treeNode); boolean[] found = new boolean[quantifiedSubformulas.size()];
                            for (int a = 0; a < quantifiedSubformulas.size(); a++) { if (!found[a]) { Formula subformula = quantifiedSubformulas.get(a); for (int m = 0; m < underlineFormulas.size(); m++) { Formula ulineFormula = underlineFormulas.get(m); if (subformula.equals(ulineFormula)) { found[a] = true; underlineFormulas.remove(m); break; } } } }
                            boolean good = true; for (int b = 0; b < quantifiedSubformulas.size(); b++) { if (found[b] == false) { good = false; break; } }
                            if (!good) { if (!silent) { expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Not every quantified subformula has underline."))); expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                        }
                    }
                }
            }
            if (hasFormulas) {
                List<ExpDashedLine> dashedLines = new ArrayList<>(); List<ExpVerticalBracket> verticalBrackets = new ArrayList<>();
                for (Node node : checkNodeList) { if (node instanceof ExpDashedLine) dashedLines.add((ExpDashedLine) node); if (node instanceof ExpVerticalBracket) verticalBrackets.add((ExpVerticalBracket) node); }
                if (checkDashedLine && checkType == VTCheckType.FORMULA) {
                    if (!hasTerms) { if (!dashedLines.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No need for dashed term/formula separator in tree without terms."))); return false; } }
                    else {
                        if (dashedLines.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Missing dashed term/formula separator line."))); return false; }
                        if (dashedLines.size() > 1) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Just one dashed term/formula separator line."))); return false; }
                        ExpDashedLine dashedLine = dashedLines.get(0);
                        if (dashedLine.getLayoutY() < maxTermY || dashedLine.getLayoutY() > minFormulaY) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Dashed line does not separate terms from formulas."))); return false; }
                    }
                }
                if (checkBracket && checkType == VTCheckType.FORMULA) {
                    if (verticalBrackets.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Missing vertical bracket on subformulas."))); return false; }
                    if (verticalBrackets.size() > 1) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Just one vertical bracket for subformulas."))); return false; }
                    ExpVerticalBracket bracket = verticalBrackets.get(0);
                    if (hasTerms && (bracket.getLayoutY() + 10) < maxTermY) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Vertical bracket includes more than just subformulas."))); return false; }
                    if ((bracket.getLayoutY() + 10) > minFormulaY || bracket.getBoundsInParent().getMaxY() < maxFormulaY) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Vertical bracket does not include all subformulas."))); return false; }
                }
            }
        }
        return true;
    }

    private boolean checkTrueTree() {
        if (vtExpExercise == null || formulaTree == null || formulaTree.isEmpty()) return false;
        List<ExpTreeNode> bottomRow = formulaTree.get(formulaTree.size() - 1);
        if (bottomRow.size() > 1 ) { if (!silent) { scrollToNode(bottomRow.get(0).getMainTreeBox().getFormulaBox().getRTA()); for (ExpTreeNode treeNode : bottomRow) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); } EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A tree can have only one bottom (root) node."))); for (ExpTreeNode treeNode : bottomRow) { treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } } return false; }
        for (List<ExpTreeNode> row : formulaTree) { for (ExpTreeNode treeNode : row) { List<ExpTreeNode> children = treeNode.getChildren(); if (children.size() > 1) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A node can be linked to at most one child (lower) node."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } } }
        connectedNodes = new ArrayList<>(); populateConnectedNodes(formulaTree.get(formulaTree.size() - 1).get(0));
        for (List<ExpTreeNode> row : formulaTree) { for (ExpTreeNode treeNode : row) { if (!connectedNodes.contains(treeNode)) { if (!silent) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node is not an an ancestor connected all the way to the root node."))); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); } return false; } } }
        return true;
    }

    private void populateConnectedNodes(ExpTreeNode node) {
        if (node == null || connectedNodes == null) return; connectedNodes.add(node);
        if (node.getParents() != null) { for (ExpTreeNode parent : node.getParents()) { populateConnectedNodes(parent); } }
    }

    private void populateQuantifiedSubformulas(ExpTreeNode node) {
        if (node == null || objLang == null || quantifiedSubformulas == null) return; ExpTreeFormulaBox nodeBox = node.getMainTreeBox(); if (nodeBox == null || nodeBox.getFormulaBox() == null) return;
        RichTextArea rootRTA = nodeBox.getFormulaBox().getRTA(); if (rootRTA == null) return; rootRTA.getActionFactory().saveNow().execute(new ActionEvent()); Document nodeDoc = rootRTA.getDocument(); objLang.setAllowDroppedBrackets(langAllowDP); List<Expression> nodeParseList = ParseUtilities.parseDoc(nodeDoc, objLang); if (nodeParseList.isEmpty()) return; Expression nodeExp = nodeParseList.get(0);
        if (nodeExp instanceof Formula) { Formula formula = (Formula) nodeExp; if (formula.getMainOperator() instanceof UniversalOp || formula.getMainOperator() instanceof ExistentialOp) quantifiedSubformulas.add(formula); }
        if (node.getParents() != null) { for (ExpTreeNode parent : node.getParents()) { populateQuantifiedSubformulas(parent); } }
    }

    private boolean setParentsAndChildren() {
        if (formulaTree == null || checkNodeList == null) return false;
        for (List<ExpTreeNode> rowList : formulaTree) {
            for (ExpTreeNode node : rowList) {
                node.setParents(new ArrayList<>()); node.setChildren(new ArrayList<>()); ExpTreeFormulaBox mainTBox = node.getMainTreeBox(); String formulaID = mainTBox.getId(); String relativeID = "";
                for (Node link : checkNodeList ) {
                    relativeID = "";
                    if (link instanceof ExpClickableNodeLink) { ExpClickableNodeLink nodeLink = (ExpClickableNodeLink) link; if (nodeLink.getSourceId().equals(formulaID)) { relativeID = nodeLink.getTargetId(); } if (nodeLink.getTargetId().equals(formulaID)) { relativeID = nodeLink.getSourceId(); } }
                    if (!relativeID.equals("")) {
                        for (List<ExpTreeNode> rList : formulaTree) {
                            for (ExpTreeNode tNode : rList) {
                                ExpTreeFormulaBox tBox = tNode.getMainTreeBox();
                                if (tBox.getId().equals(relativeID)) {
                                    if (tBox.getLayoutY() - mainTBox.getLayoutY() > equalsEpsilon) node.getChildren().add(tNode);
                                    else if (mainTBox.getLayoutY() - tBox.getLayoutY() > equalsEpsilon) node.getParents().add(tNode);
                                    else { if (!silent) { mainTBox.getFormulaBox().setVTtreeBoxHighlight(); tBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(mainTBox.getFormulaBox().getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Cannot have connected nodes in same row."))); mainTBox.getFormulaBox().resetVTtreeBoxHighlight(); tBox.getFormulaBox().resetVTtreeBoxHighlight(); } return false; }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (List<ExpTreeNode> rowList : formulaTree) { for (ExpTreeNode node : rowList) { if (node.getParents() != null) { node.getParents().sort(Comparator.comparingDouble(parent -> parent.getMainTreeBox().getLayoutX())); } } }
        return true;
    }

    private boolean populateTreeLists(List<Node> sortedFormulaBoxes) {
        if (sortedFormulaBoxes == null || vtExpExercise == null) return false; int j = 0; List<ExpTreeNode> treeList = new ArrayList<>(); List<ExpMapFormulaBox> justificationList = new ArrayList<>(); lineJustifications = new ArrayList<>();
        for (int i = 0; i < sortedFormulaBoxes.size(); i++) {
            if (sortedFormulaBoxes.get(i) instanceof ExpTreeFormulaBox) treeList.add(new ExpTreeNode((ExpTreeFormulaBox) sortedFormulaBoxes.get(i))); if (sortedFormulaBoxes.get(i) instanceof ExpMapFormulaBox) justificationList.add((ExpMapFormulaBox) sortedFormulaBoxes.get(i));
            if ((i + 1 < sortedFormulaBoxes.size() && Math.abs(sortedFormulaBoxes.get(i + 1).getLayoutY() - sortedFormulaBoxes.get(i).getLayoutY()) > equalsEpsilon) || i + 1 >= sortedFormulaBoxes.size()) {
                if (treeList.size() > 0) { treeList.sort(Comparator.comparingDouble((treeBox -> treeBox.getMainTreeBox().getLayoutX()))); formulaTree.add(treeList); }
                if (treeList.size() == 0 && justificationList.size() != 0) { if (!silent) { BoxedDRTA justificationBDRTA = justificationList.get(0).getFormulaBox(); scrollToNode(justificationBDRTA.getRTA()); justificationBDRTA.setVTtreeBoxHighlight(); List<Text> messageList = new ArrayList<>(); messageList.add(ParseUtilities.newRegularText("Justification without formula field on row.")); EditorAlerts.showSimpleTxtListAlert("Tree Structure", messageList); for (ExpMapFormulaBox mapBox : justificationList) { mapBox.getFormulaBox().resetVTtreeBoxHighlight(); } } return false; }
                if (checkJustification || checkValueJustification) {
                    if (justificationList.size() == 0) {
                        if (!silent) {
                            if (!treeList.isEmpty()) { scrollToNode(treeList.get(0).getMainTreeBox().getFormulaBox().getRTA()); }
                            for (ExpTreeNode treeNode : treeList) { treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); }
                            List<Text> messageList = new ArrayList<>(); messageList.add(ParseUtilities.newRegularText("Missing justification on row."));
                            if (checkType == VTCheckType.UNABB) messageList.add(ParseUtilities.newRegularText("\n\nIf formula results from (nothing or) linked nodes by TR or FR cite that rule.  Otherwise cite the AB rule (including an AB(i) to convert from abbreviating infix forms)."));
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", messageList); for (ExpTreeNode treeNode : treeList) { treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); }
                        } return false;
                    } else if (justificationList.size() > 1) { if (!silent) { BoxedDRTA bdrta = justificationList.get(justificationList.size() - 1).getFormulaBox(); scrollToNode(bdrta.getRTA()); for (ExpMapFormulaBox justificationBox : justificationList) { justificationBox.getFormulaBox().setVTmapBoxHighlight(); } EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Only one justification field for each row."))); for (ExpMapFormulaBox justificationBox : justificationList) { justificationBox.getFormulaBox().resetVTmapBoxHighlight(); } } return false; }
                    else {
                        BoxedDRTA bdrta = justificationList.get(0).getFormulaBox(); RichTextArea rta = bdrta.getRTA(); rta.getActionFactory().saveNow().execute(new ActionEvent()); String boxText = rta.getDocument().getText();
                        if (checkJustification && (checkType == VTCheckType.FORMULA || checkType == VTCheckType.UNABB_AUX)) {
                            List<String> missingPrimes = getMissingPrime(boxText); if (missingPrimes.size() > 0) { if (!silent) { bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Do not forget prime on justification for abbreviating rules, as FR\ue8bc(\u2227)."))); bdrta.resetVTmapBoxHighlight(); } return false; }
                            List<String> justifications = getJustificationStrings(boxText); if (justifications.size() != treeList.size()) { if (!silent) { bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one (allowable) justification, as FR(\u223c), for each member of row."))); bdrta.resetVTmapBoxHighlight(); } return false; }
                            lineJustifications.add(justifications);
                        } else if (checkJustification && checkType == VTCheckType.UNABB) {
                            List<String> justifications = getAbbJustificationStrings(boxText); if (justifications.size() != treeList.size()) { if (!silent) { bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one (allowable) justification for each member of row.\n\nIf formula results from (nothing or) linked nodes by TR or FR cite that rule.  Otherwise cite the AB rule (including an AB(i) to convert from abbreviating infix forms)."))); bdrta.resetVTmapBoxHighlight(); } return false; }
                            lineJustifications.add(justifications);
                        } else if (checkValueJustification && checkType == VTCheckType.TRUTH_VALUE) {
                            if (j == 0) { String[] firstLine = boxText.split(" "); if (firstLine.length != 2 || !(firstLine[0].equals("From") || firstLine[0].equals("from")) || !mLang.getInterpretationVariables().contains(firstLine[1])) { if (!silent) { bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Top row should have single justification, as \"From \ud835\udda8\" - where the interpretation name is in the same (sans) font as in exercise statement."))); bdrta.resetVTmapBoxHighlight(); } return false; } lineJustifications.add(Collections.singletonList(boxText)); }
                            else { List<String> justifications = getTruthValJustificationStrings(boxText);
                                if (justifications.size() != treeList.size()) {
                                    if (!silent) { bdrta.setVTmapBoxHighlight();
                                        scrollToNode(bdrta.getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Rows other than the top should have justifications (as \"T(\u223c) row 1\" or \"T\ue8bc(\u2227) row 2\" for each member.")));
                                        bdrta.resetVTmapBoxHighlight();
                                    }
                                    return false;
                                }
                                lineJustifications.add(justifications);
                            }
                        } else if (checkValueJustification && checkType == VTCheckType.TERM_VALUE) {
                            List<String> justifications = getTermJustificationStrings(boxText); if (justifications.size() != treeList.size()) { if (!silent) { bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one justification for each member of row - as TA(v), TA(c) or TA(f)."))); bdrta.resetVTmapBoxHighlight(); } return false; }
                            lineJustifications.add(justifications);
                        }
                    }
                }
                j++; treeList = new ArrayList<>(); justificationList = new ArrayList<>();
            }
        }
        return true;
    }

    /*
        Check nodes for grammatical term or formula.  Check parents/children and justification.  Check target.
    */
    private boolean checkContents() {
        if (isTornDown || vtExpExercise == null || formulaTree == null || objLang == null || formulaTree.isEmpty()) return false;
        hasTerms = false;
        hasFormulas = false;
        minTermY = -1;
        maxTermY = -1;
        minFormulaY = -1;
        maxFormulaY = -1;

        List<ExpTreeNode> bottomRow = formulaTree.get(formulaTree.size() - 1);
        if (bottomRow == null || bottomRow.isEmpty()) return false;
        rootNode = bottomRow.get(0);

        for (int j = 0; j < formulaTree.size(); j++) {
            List<ExpTreeNode> treeList = formulaTree.get(j);
            for (int i = 0; i < treeList.size(); i++) {
                ExpTreeNode treeNode = treeList.get(i);
                if (treeNode == null || treeNode.getMainTreeBox() == null || treeNode.getMainTreeBox().getFormulaBox() == null) continue;

                RichTextArea rta = treeNode.getMainTreeBox().getFormulaBox().getRTA();
                if (rta == null) return false;

                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document doc = rta.getDocument();
                objLang.setAllowDroppedBrackets(false);
                List<Expression> parseExp = ParseUtilities.parseDoc(doc, objLang);

                if (parseExp.isEmpty()) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                        scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Empty node.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }

                List<Expression> parseExpDP = new ArrayList<>();
                if (parseExp.size() > 1) {
                    objLang.setAllowDroppedBrackets(true);
                    parseExpDP = ParseUtilities.parseDoc(doc, objLang);
                    if (parseExpDP.size() != 1) {
                        if (!silent) {
                            treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                            scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                            treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else {
                        if (treeNode != rootNode || !langAllowDP) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression (check outer parentheses).")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    }
                }

                Expression exp = null;
                if (parseExp.size() == 1) exp = parseExp.get(0);
                else if (parseExpDP.size() == 1) exp = parseExpDP.get(0);

                if (!(exp instanceof Term) && !(exp instanceof Formula)) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                        scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }

                if (exp instanceof Term) {
                    Term term = (Term) exp;
                    TermType termType = term.getTermType();
                    hasTerms = true;
                    double layoutY = treeNode.getMainTreeBox().getLayoutY();
                    double maxLayoutY = treeNode.getMainTreeBox().getBoundsInParent().getMaxY();
                    if (minTermY < 0 || (layoutY + 9) < minTermY) minTermY = (layoutY + 9);
                    if ((maxLayoutY - 9) > maxTermY) maxTermY = (maxLayoutY - 9);

                    if (termType == TermType.VARIABLE) {
                        if (treeNode.getParents() != null && treeNode.getParents().size() != 0) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is variable and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("TR(v)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Variable node has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (termType == TermType.CONSTANT) {
                        if (treeNode.getParents() != null && treeNode.getParents().size() != 0) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is constant and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("TR(c)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Constant node has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (termType == TermType.COMPLEX) {
                        FunctionSymbol fnSym = term.getMainFnSymbol();
                        int places = fnSym.getPlaces();
                        String placesString = String.valueOf(places);

                        if (treeNode.getParents() != null && treeNode.getParents().size() != term.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText(placesString + "-place function symbol must be linked to " + placesString + " term(s).")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else {
                            for (int k = 0; k < term.getChildren().size(); k++) {
                                Expression subTerm = term.getChildren().get(k);
                                if (treeNode.getParents().get(k) == null || treeNode.getParents().get(k).getMainTreeBox() == null) return false;
                                RichTextArea termRTA = treeNode.getParents().get(k).getMainTreeBox().getFormulaBox().getRTA();
                                if (termRTA == null) return false;
                                termRTA.getActionFactory().saveNow().execute(new ActionEvent());
                                Document termDoc = termRTA.getDocument();
                                objLang.setAllowDroppedBrackets(false);
                                List<Expression> linkParseList = ParseUtilities.parseDoc(termDoc, objLang);
                                if (linkParseList.isEmpty()) return false;
                                Expression linkTerm = linkParseList.get(0);
                                if (!subTerm.equals(linkTerm)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                        scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                        List<Text> textList = new ArrayList<>();
                                        textList.add(ParseUtilities.newRegularText("Term requires link to subterm "));
                                        textList.addAll(subTerm.toTextList());
                                        textList.add(ParseUtilities.newRegularText(" above."));

                                        EditorAlerts.showSimpleTxtListAlert("TreeContent", textList);
                                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            }
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("TR(f)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Complex term has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    }
                } else if (exp instanceof Formula) {
                    Formula formula = (Formula) exp;
                    hasFormulas = true;
                    double layoutY = treeNode.getMainTreeBox().getLayoutY();
                    double maxLayoutY = treeNode.getMainTreeBox().getBoundsInParent().getMaxY();
                    if (minFormulaY < 0 || (layoutY + 9) < minFormulaY) minFormulaY = layoutY + 9;
                    if ((maxLayoutY - 9) > maxFormulaY) maxFormulaY = (maxLayoutY - 9);

                    if (formula instanceof SentenceAtomic) {
                        if (treeNode.getParents() != null && treeNode.getParents().size() != 0) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is a sentence letter and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("FR(s)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Sentence letter has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (formula instanceof PrefixAtomic || formula instanceof InfixAtomic) {
                        RelationSymbol relSym = null;
                        if (formula instanceof PrefixAtomic) relSym = ((PrefixAtomic) formula).getMainRelation();
                        else relSym = ((InfixAtomic) formula).getMainRelation();
                        int places = relSym.getPlaces();
                        String placesString = String.valueOf(places);

                        if (treeNode.getParents() != null && treeNode.getParents().size() != formula.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText(placesString + "-place relation symbol must be linked to " + placesString + " term(s).")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else {
                            for (int k = 0; k < formula.getChildren().size(); k++) {
                                Expression subTerm = formula.getChildren().get(k);
                                if (treeNode.getParents().get(k) == null || treeNode.getParents().get(k).getMainTreeBox() == null) return false;
                                RichTextArea termRTA = treeNode.getParents().get(k).getMainTreeBox().getFormulaBox().getRTA();
                                if (termRTA == null) return false;
                                termRTA.getActionFactory().saveNow().execute(new ActionEvent());
                                Document termDoc = termRTA.getDocument();
                                objLang.setAllowDroppedBrackets(false);
                                List<Expression> linkParseList = ParseUtilities.parseDoc(termDoc, objLang);
                                if (linkParseList.isEmpty()) return false;
                                Expression linkTerm = linkParseList.get(0);
                                if (!subTerm.equals(linkTerm)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                        scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                        List<Text> textList = new ArrayList<>();
                                        textList.add(ParseUtilities.newRegularText("Term requires link to subterm "));
                                        textList.addAll(subTerm.toTextList());
                                        textList.add(ParseUtilities.newRegularText(" above."));

                                        EditorAlerts.showSimpleTxtListAlert("TreeContent", textList);
                                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            }
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("FR(r)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Atomic formula has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else {
                        Operator mainOp = formula.getMainOperator();
                        if (treeNode.getParents() != null && treeNode.getParents().size() != formula.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                if (mainOp.isUnary())
                                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Formula with unary main operator must be linked to a single formula.")));
                                else
                                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Formula with binary main operator must be linked to two formulas.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else {
                            for (int k = 0; k < formula.getChildren().size(); k++) {
                                Expression subForm = formula.getChildren().get(k);
                                if (treeNode.getParents().get(k) == null || treeNode.getParents().get(k).getMainTreeBox() == null) return false;
                                RichTextArea termRTA = treeNode.getParents().get(k).getMainTreeBox().getFormulaBox().getRTA();
                                if (termRTA == null) return false;
                                termRTA.getActionFactory().saveNow().execute(new ActionEvent());
                                Document formDoc = termRTA.getDocument();
                                objLang.setAllowDroppedBrackets(false);
                                List<Expression> linkParseList = ParseUtilities.parseDoc(formDoc, objLang);
                                if (linkParseList.isEmpty()) return false;
                                Expression linkForm = linkParseList.get(0);
                                if (!subForm.equals(linkForm)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                        scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                        List<Text> textList = new ArrayList<>();
                                        textList.add(ParseUtilities.newRegularText("Formula requires link to subformula "));
                                        textList.addAll(subForm.toTextList());
                                        textList.add(ParseUtilities.newRegularText(" above."));
                                        EditorAlerts.showSimpleTxtListAlert("TreeContent", textList);
                                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            }
                        }
                        if (checkJustification) {
                            String justificationString = "";
                            if (mainOp instanceof NegationOp || mainOp instanceof ConditionalOp || mainOp instanceof UniversalOp)
                                justificationString = "FR(" + mainOp.getMainSymbol().toString() + ")";
                            else if (mainOp instanceof ConjunctionOp || mainOp instanceof DisjunctionOp || mainOp instanceof BiconditionalOp || mainOp instanceof ExistentialOp)
                                justificationString = "FR\ue8bc(" + mainOp.getMainSymbol().toString() + ")";

                            if (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals(justificationString)) {
                                if (!silent) {
                                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                    scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                    List<Text> messageList = new ArrayList<>();
                                    messageList.add(ParseUtilities.newRegularText("Formula with main operator "));
                                    messageList.addAll(mainOp.toTextList());
                                    messageList.add(ParseUtilities.newRegularText(" has incorrect justification."));
                                    EditorAlerts.showSimpleTxtListAlert("Tree Content", messageList);
                                    treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }

                    }

                }
            }
        }

        //check target
        if (targetExpression != null) {
            if (rootNode == null || rootNode.getMainTreeBox() == null || rootNode.getMainTreeBox().getFormulaBox() == null) return false;
            RichTextArea rootRTA = rootNode.getMainTreeBox().getFormulaBox().getRTA();
            if (rootRTA == null) return false;
            rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document rootDoc = rootRTA.getDocument();
            objLang.setAllowDroppedBrackets(langAllowDP);
            List<Expression> rootParseList = ParseUtilities.parseDoc(rootDoc, objLang);
            if (rootParseList.isEmpty()) return false;
            Expression rootExp = rootParseList.get(0);
            if (!rootExp.equals(targetExpression)) {
                if (!silent) {
                    rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                    scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Good tree, but this is not the expected target expression.")));
                    rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                }
                return false;
            }
        }
        objLang.setAllowDroppedBrackets(langAllowDP);
        return true;
    }



    private List<String> getMissingPrime(String inputString) {
        if (inputString == null) return new ArrayList<>(); String regexTarget = "FR\\(\\u2227\\)|FR\\(\\u2228\\)|FR\\(\\u2194\\)|FR\\(\\u2203\\)"; Pattern pattern = Pattern.compile(regexTarget); Matcher matcher = pattern.matcher(inputString); List<String> justifications = new ArrayList<>(); while (matcher.find()) { justifications.add(matcher.group()); } return justifications;
    }

    private List<String> getJustificationStrings(String inputString) {
        if (inputString == null) return new ArrayList<>(); String regexTarget = "FR\\(s\\)|FR\\(r\\)|FR\\(\\u223c\\)|FR\\(\\u2192\\)|FR\\ue8bc\\(\\u2227\\)|FR\\ue8bc\\(\\u2228\\)|FR\\ue8bc\\(\\u2194\\)|FR\\(\\u2200\\)|FR\\ue8bc\\(\\u2203\\)|TR\\(v\\)|TR\\(c\\)|TR\\(f\\)"; Pattern pattern = Pattern.compile(regexTarget); Matcher matcher = pattern.matcher(inputString); List<String> justifications = new ArrayList<>(); while (matcher.find()) { justifications.add(matcher.group()); } return justifications;
    }

    private List<String> getAbbJustificationStrings(String inputString) {
        if (inputString == null) return new ArrayList<>(); String regexTarget = "AB\\(i\\)|AB\\(\\u2228\\)|AB\\(\\u2227\\)|AB\\(\\u2194\\)|AB\\(\\u2203\\)|FR\\(s\\)|FR\\(r\\)|FR\\(\\u223c\\)|FR\\(\\u2192\\)|FR\\(\\u2200\\)|TR\\(v\\)|TR\\(c\\)|TR\\(f\\)"; Pattern pattern = Pattern.compile(regexTarget); Matcher matcher = pattern.matcher(inputString); List<String> justifications = new ArrayList<>(); while (matcher.find()) { justifications.add(matcher.group()); } return justifications;
    }

    private List<String> getTermJustificationStrings(String inputString) {
        if (inputString == null) return new ArrayList<>(); String regexTarget = "TA\\(v\\)|TA\\(c\\)|TA\\(f\\)"; Pattern pattern = Pattern.compile(regexTarget); Matcher matcher = pattern.matcher(inputString); List<String> justifications = new ArrayList<>(); while (matcher.find()) { justifications.add(matcher.group()); } return justifications;
    }

    private List<String> getTruthValJustificationStrings(String inputString) {
        if (inputString == null || objLang == null) return new ArrayList<>();
        String neg = objLang.getNegation(); String cond = objLang.getConditional();
        String conj = objLang.getConjunction();
        String disj = objLang.getDisjunction();
        String bcnd = objLang.getBiconditional();
        String regexTarget = "(T\\(" + neg + "\\)\\s*,?\\s*row 1)|(T\\(" + neg + "\\)\\s*,?\\s*row 2)|" +
                "(T\\(" + cond + "\\)\\s*,?\\s*row 1)|(T\\(" + cond + "\\)\\s*,?\\s*row 2)|(T\\(" + cond + "\\)\\s*,?\\s*row 3)|(T\\(" + cond + "\\)\\s*,?\\s*row 4)|" +
                "(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 1)|(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 2)|(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 3)|(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 4)|" +
                "(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 1)|(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 2)|(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 3)|(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 4)|" +
                "(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 1)|(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 2)|(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 3)|(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 4)";
        Pattern pattern = Pattern.compile(regexTarget);
        Matcher matcher = pattern.matcher(inputString);
        List<String> justifications = new ArrayList<>();
        while (matcher.find()) {
            justifications.add(matcher.group());
        }
        return justifications;
    }

    private List<Node> getSortedFormulaBoxes() {
        if (checkNodeList == null) return new ArrayList<>(); List<Node> sortedFormulaBoxes = new ArrayList<>(); for (Node node : checkNodeList) { if (node instanceof ExpTreeFormulaBox || node instanceof ExpMapFormulaBox) sortedFormulaBoxes.add(node); } sortedFormulaBoxes.sort(Comparator.comparingDouble(node -> node.getLayoutY())); return sortedFormulaBoxes;
    }

    private List<Expression> getCircleText(ExpTreeFormulaBox expTreeFormulaBox) {
        if (expTreeFormulaBox == null || expTreeFormulaBox.getFormulaBox() == null || checkSetup == null) return new ArrayList<>(); RichTextArea rta = expTreeFormulaBox.getFormulaBox().getRTA(); rta.getActionFactory().saveNow().execute(new ActionEvent()); Document doc = rta.getDocument(); String text = doc.getText(); int[] indexes = expTreeFormulaBox.getCircleIndexes(); int startIndex = Math.min(indexes[0], indexes[1]); if (0 < startIndex && startIndex + 1 < text.length() && text.codePointCount(startIndex -1, startIndex +1) == 1) startIndex++; int endIndex = Math.max(indexes[0], indexes[1]); if (0 < endIndex && endIndex + 1 < text.length() && text.codePointCount(endIndex -1, endIndex +1) == 1) endIndex++; return ParseUtilities.parseSubDoc(doc, startIndex, endIndex, checkSetup.getObjLangName());
    }

    private List<List<Expression>> getUnderlineTexts(ExpTreeFormulaBox treeFormulaBox) {
        if (treeFormulaBox == null || treeFormulaBox.getFormulaBox() == null || checkSetup == null) return new ArrayList<>(); List<List<Expression>> underlineTexts = new ArrayList<>(); RichTextArea rta = treeFormulaBox.getFormulaBox().getRTA(); rta.getActionFactory().saveNow().execute(new ActionEvent()); Document doc = rta.getDocument(); String text = doc.getText();
        for (Integer[] index : treeFormulaBox.getUlineIndexes()) { int startIndex = index[0]; int endIndex = index[1]; if (0 < startIndex && startIndex + 1 < text.length() && text.codePointCount(startIndex -1, startIndex +1) == 1) startIndex++; if (0 < endIndex && endIndex + 1 < text.length() && text.codePointCount(endIndex -1, endIndex +1) == 1) endIndex++; List<Expression> lineList = ParseUtilities.parseSubDoc(doc, startIndex, endIndex, checkSetup.getObjLangName()); underlineTexts.add(lineList); } return underlineTexts;
    }

    private void updateCheckCounter() {
        if (vtExpExercise == null || vtExpView == null || vtExpExercise.getMainWindow() == null) return; if (checkMax != -1 && checkTries >= checkMax && !vtExpExercise.getMainWindow().isInstructorFunctions()) { if (vtExpView.getCheckButton() != null) vtExpView.getCheckButton().setDisable(true); }
        String checkString; if (checkMax == -1) checkString = "(unlimited)"; else if (checkMax == 0) checkString = "(none)"; else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")"; if (vtExpView.getCheckTriesLabel() != null) vtExpView.getCheckTriesLabel().setText(checkString);
    }

    public void setLangAllowDP(boolean langAllowDP) { if (langAllowDP) this.langAllowDP = true; else this.langAllowDP = false; }
    public int getCheckTries() { return checkTries; }
    public boolean isCheckSuccess() { return checkSuccess; }
    public void setCheckType(VTCheckType checkType) { this.checkType = checkType; }
    public List<List<ExpTreeNode>> getFormulaTree() { return formulaTree; }
    public List<List<? extends VTAuxTreeNode>> getAuxFormulaTree() {
        List<List<? extends VTAuxTreeNode>> newFormulaTree = new ArrayList<>();
        if (formulaTree != null) { for (int i = 0; i < formulaTree.size(); i++) { newFormulaTree.add(formulaTree.get(i)); } } return newFormulaTree;
    }
    public String getObjLangName() { return objLangName; }
    public boolean isCheckFinal() { return checkFinal; }
}