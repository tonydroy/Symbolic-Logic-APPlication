package slapp.editor.horiz_trees;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.horizontal_tree.BranchNode;
import slapp.editor.horizontal_tree.HorizontalTreeExercise;
import slapp.editor.horizontal_tree.TreePane;
import slapp.editor.m_parse.MLanguage;
import slapp.editor.m_parse.MParseUtilities;
import slapp.editor.m_parse.MSyntacticalFns;
import slapp.editor.m_parse.m_grammatical_parts.FormulaPacket;
import slapp.editor.m_parse.m_grammatical_parts.MInterpretation;
import slapp.editor.m_parse.m_grammatical_parts.SemanticPacket;
import slapp.editor.m_parse.m_grammatical_parts.TermPacket;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.main_window.Exercise;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.FunctionSymbol;
import slapp.editor.parser.symbols.RelationSymbol;
import java.util.*;
import javafx.scene.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.event.EventHandler;


public class HorizTreesCheck {
    private HorizTreesExercise htExercise;
    private HorizTreesView htView;
    private HorizTreesModel htModel;
    private HorizTreesCheckSetup checkSetup;
    private String objLangName;
    private MLanguage mLang;

    private int checkMax;
    private int checkTries;
    private boolean checkSuccess;
    private boolean permitEmptyBranches;
    private MSyntacticalFns mSyntacticalFns;
    private MParseUtilities mParseUtilities = new MParseUtilities();
    MSatValue theSat;
    MSatValue theNSat;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;



    HorizTreesCheck(HorizTreesExercise htExercise) {
        this.htExercise = htExercise;
        this.htView = htExercise.getExerciseView();
        this.htModel = htExercise.getExerciseModel();
        mSyntacticalFns = new MSyntacticalFns(htExercise.getMainWindow());

        checkSetup = htModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new HorizTreesCheckSetup();
            htModel.setCheckSetup(checkSetup);
        }
        objLangName = checkSetup.getObjLangName();
        mLang = new MLanguage();

        theSat = new MTheSat(mLang.getSatisfiedSym());
        theNSat = new MTheNsat(mLang.getNotSatisfiedSym());

        checkSuccess = checkSetup.isCheckSuccess();

        htView.setCheckColor(Color.LAWNGREEN);
        htView.setCheckElementsColor(Color.GREEN);
        htView.setCheckMessage("Tree(s) / Values");

        if (checkSetup.getRootCount() == 0) {
            htView.setCheckColor(Color.ORCHID);
            htView.setCheckElementsColor(Color.PURPLE);
            htView.setCheckMessage("Legitimate Interpretation");
        }


        if (checkSuccess) htView.activateBigCheck();
        else htView.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();

        setRightControlBox();
    }

    private void scrollToNode(Node node) {
        if (isTornDown || htExercise == null || htExercise.getMainWindow() == null || htExercise.getMainWindow().getMainView() == null) return;
        htExercise.getMainWindow().getMainView().scrollToNode(node);
    }

    public boolean checkTrees() {
        if (isTornDown || htExercise == null || htView == null || mSyntacticalFns == null || htExercise.getMainWindow() == null) return false;

        checkSuccess = false;
        htView.deactivateBigCheck();
        if (!htExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            updateCheckCounter();
        }
        checkSetup.setRootCount(0);

        //--------------
        if (htView.getPermitEmptyBranchesCheck() != null) {
            permitEmptyBranches = htView.getPermitEmptyBranchesCheck().isSelected();
        }

        if (htView.getInterpretationDRTA() != null && htView.getInterpretationDRTA().getEditor() != null) {
            RichTextArea interpRTA = htView.getInterpretationDRTA().getEditor();
            interpRTA.getActionFactory().saveNow().execute(new ActionEvent());
            if (!mSyntacticalFns.setInterpBaseVarAssign(interpRTA.getDocument(), objLangName)) return false;
        } else {
            return false;
        }


        List<Exercise> exercises = htExercise.getExerciseList();
        if (exercises != null) {
            for (int i = 0; i < exercises.size(); i++ ) {
                if (exercises.get(i) instanceof HorizontalTreeExercise) {
                    HorizontalTreeExercise htExer = ((HorizontalTreeExercise) exercises.get(i));

                    if (htExer != null && htExer.getHorizontalTreeView() != null) {
                        List<TreePane> panes = htExer.getHorizontalTreeView().getTreePanes();
                        if (panes != null) {
                            for (int j = 0; j < panes.size(); j++ ) {
                                TreePane tp = panes.get(j);
                                if (tp != null) {
                                    BranchNode root = tp.getRootBranchNode();
                                    checkSetup.setRootCount(checkSetup.getRootCount() + 1);
                                    if (!checkTree(root)) return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (checkSetup.getRootCount() == 0) {
            htView.setCheckColor(Color.ORCHID);
            htView.setCheckElementsColor(Color.PURPLE);
            htView.setCheckMessage("Legitimate Interpretation");
        }
        else {
            htView.setCheckColor(Color.LAWNGREEN);
            htView.setCheckElementsColor(Color.GREEN);
            htView.setCheckMessage("Tree(s) / Values");
        }

        if (!checkTargets()) return false;

        //------------
        checkSuccess = true;
        htView.activateBigCheck();

        return true;
    }

    private boolean checkTargets() {
        if (isTornDown || checkSetup == null || checkSetup.getTargetFormulas() == null || mParseUtilities == null) return false;
        List<Document> targets = checkSetup.getTargetFormulas();

        for (Document doc : targets) {
            Formula target = null;
            List<Expression> expList = mParseUtilities.parseDoc(doc, objLangName);
            if (!expList.isEmpty()) {
                Expression exp = expList.get(0);
                if (exp instanceof Formula) target = (Formula) exp;

                if (target != null) {
                    List<Exercise> exercises = htExercise.getExerciseList();
                    if (exercises == null) continue;

                    boolean found = false;
                    searchLoop:
                    for (int i = 0; i < exercises.size(); i++) {
                        if (exercises.get(i) instanceof HorizontalTreeExercise) {
                            HorizontalTreeExercise htExer = ((HorizontalTreeExercise) exercises.get(i));
                            if (htExer != null && htExer.getHorizontalTreeView() != null) {
                                List<TreePane> panes = htExer.getHorizontalTreeView().getTreePanes();
                                if (panes != null) {
                                    for (int j = 0; j < panes.size(); j++) {
                                        TreePane tp = panes.get(j);
                                        if (tp != null && tp.getRootBranchNode() != null) {
                                            BranchNode root = tp.getRootBranchNode();

                                            BoxedDRTA rootBDRTA = root.getFormulaBoxedDRTA();
                                            if (rootBDRTA != null && rootBDRTA.getRTA() != null) {
                                                RichTextArea rootRTA = rootBDRTA.getRTA();
                                                rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
                                                Document rootDoc = rootRTA.getDocument();
                                                List<Expression> rootExps = mParseUtilities.parseDoc(rootDoc, objLangName);
                                                if (rootExps != null && !rootExps.isEmpty() && rootExps.get(0) instanceof MInterpAssignFormula) {
                                                    MInterpAssignFormula rootIntAss = (MInterpAssignFormula) rootExps.get(0);
                                                    Formula rootFormula = rootIntAss.getFormula();

                                                    if (target.equals(rootFormula)) {
                                                        found = true;
                                                        break searchLoop;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!found) {
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("Cannot find a tree for target formula "));
                        textList.addAll(target.toTextList());
                        textList.add(ParseUtilities.newRegularText("."));
                        EditorAlerts.showSimpleTxtListAlert("Missing Tree:", textList);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void insertSuperscripts(Formula formula) {
        if (isTornDown || formula == null) return;

        if (formula.isAtomic() && formula instanceof PrefixAtomic) {
            RelationSymbol relationSymbol = formula.getMainRelation();
            if (Languages.getLanguage(objLangName).getXrelationSymbols().contains(relationSymbol.getBaseStr())) relationSymbol.setSuperscriptStr(String.valueOf(relationSymbol.getPlaces()));
        }

        if (formula.getChildren() != null) {
            for (Expression child : formula.getChildren()) {
                if (child.getType() == ExpressionType.FORMULA) {
                    insertSuperscripts((Formula) child);
                }
            }
        }
    }

    private boolean checkTree(BranchNode root) {
        if (isTornDown || root == null) return false;
        boolean success = true;

        //set packets
        try {
            success = setPackets(root, null);
        }
        catch (TextMessageException e) {
            resetHighlights(root);
            return false;
        }
        if (!success) { return false; }

        //check forward tree
        try {
            success = checkForwardTree(root);
        }
        catch (TextMessageException e) {
            resetHighlights(root);
            return false;
        }
        if (!success) { return false; }

        try {
            success = setSemanticValues(root);
        }
        catch (TextMessageException e) {
            resetHighlights(root);
            return false;
        }
        if (!success) { return false; }

        try {
            success = checkBackwardTree(root);
        }
        catch (TextMessageException e) {
            resetHighlights(root);
            return false;
        }
        if (!success) { return false; }

        return true;
    }

    //traverses tree backward
    private boolean checkBackwardTree(BranchNode node) throws TextMessageException {
        if (isTornDown || node == null || emptyNode(node)) return false;

        if (node.getDependents() != null) {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode child = node.getDependents().get(i);
                checkBackwardTree(child);
            }
        }

        //-------------------
        if (node.getTermPacket() != null) {
            TermPacket tp = node.getTermPacket();
            Term term = tp.getTerm();

            if (term.getTermType() == TermType.CONSTANT) {
                MInterpretation interp = tp.getInterpretation();
                MBasicObjTerm assignedObj = interp.getConstantVal(term);
                if (assignedObj  == null) {
                    BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                    if (bdrta != null) {
                        bdrta.setRichTextFieldHighlight();
                        scrollToNode(bdrta.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this constant.")));
                    throw new TextMessageException(null);
                }
                if (!assignedObj.equals(tp.getBasicObject())) {
                    BoxedDRTA annBDRTA = node.getAnnotationField();
                    if (annBDRTA != null) {
                        annBDRTA.setAnnBoxHighlight();
                        scrollToNode(annBDRTA.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                    throw new TextMessageException(null);
                }
            }

            if (term.getTermType() == TermType.VARIABLE) {
                LinkedHashMap<Term, MBasicObjTerm> assignment = tp.getAssignment();
                MBasicObjTerm assignedObj = assignment.get(term);
                if (assignedObj == null) {
                    BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                    if (bdrta != null) {
                        bdrta.setRichTextFieldHighlight();
                        scrollToNode(bdrta.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Variable assignment sets no value for this variable.")));
                    throw new TextMessageException(null);
                }
                if (!assignedObj.equals(tp.getBasicObject())) {
                    BoxedDRTA annBDRTA = node.getAnnotationField();
                    if (annBDRTA != null) {
                        annBDRTA.setAnnBoxHighlight();
                        scrollToNode(annBDRTA.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                    throw new TextMessageException(null);
                }
            }

            if (term.getTermType() == TermType.COMPLEX) {
                List<MBasicObjTerm> assignmentTuple = new ArrayList<>();
                if (node.getDependents() != null) {
                    for (int i = 0; i < node.getDependents().size(); i++) {
                        BranchNode child = node.getDependents().get(i);
                        if (child != null && child.getTermPacket() != null) {
                            assignmentTuple.add(child.getTermPacket().getBasicObject());
                        }
                    }
                }
                MBasicObjTerm assignedObj = null;
                FunctionSymbol fnSym = term.getMainFnSymbol();
                MFunctionCandidate fnCandidate = tp.getInterpretation().getFunctionVal(fnSym);

                if (fnCandidate == null) {
                    BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                    if (bdrta != null) {
                        bdrta.setRichTextFieldHighlight();
                        scrollToNode(bdrta.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this function symbol.")));
                    throw new TextMessageException(null);
                }

                Set<MFnPair> fnPairs = fnCandidate.getPairs();
                for (MFnPair fnPair : fnPairs) {
                    if (fnPair.getInput().getMembers().equals(assignmentTuple)) {
                        assignedObj = fnPair.getOutput();
                        break;
                    }
                }
                if (assignedObj == null) {
                    BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                    if (bdrta != null) {
                        bdrta.setRichTextFieldHighlight();
                        scrollToNode(bdrta.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation sets no value for this input.")));
                    throw new TextMessageException(null);
                }
                if (!assignedObj.equals(tp.getBasicObject())) {
                    BoxedDRTA annBDRTA = node.getAnnotationField();
                    if (annBDRTA != null) {
                        annBDRTA.setAnnBoxHighlight();
                        scrollToNode(annBDRTA.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                    throw new TextMessageException(null);
                }
            }
        }

        else if (node.getFormulaPacket() != null) {
            FormulaPacket fp = node.getFormulaPacket();
            Formula formula = fp.getFormula();
            MInterpretation interpretation = fp.getInterpretation();

            if (formula.isAtomic()) {
                if (formula instanceof SentenceAtomic) {
                    MTruthValue truthValue = interpretation.getFormulaVal(formula);
                    if (truthValue == null) {
                        BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                        if (bdrta != null) {
                            bdrta.setRichTextFieldHighlight();
                            scrollToNode(bdrta.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation sets no value for this letter.")));
                        throw new TextMessageException(null);
                    }
                    else {
                        MSatValue satValue = fp.getSatisfactionVal();
                        if ((satValue instanceof MTheSat && !(truthValue instanceof MTheTrue)) || (satValue instanceof MTheNsat && !(truthValue instanceof MTheFalse))) {
                            BoxedDRTA annBDRTA = node.getAnnotationField();
                            if (annBDRTA != null) {
                                annBDRTA.setAnnBoxHighlight();
                                scrollToNode(annBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                    }
                }
                else {
                    List<MBasicObjTerm> assignmentTuple = new ArrayList<>();
                    if (node.getDependents() != null) {
                        for (int i = 0; i < node.getDependents().size(); i++) {
                            BranchNode child = node.getDependents().get(i);
                            if (child != null && child.getTermPacket() != null) {
                                assignmentTuple.add(child.getTermPacket().getBasicObject());
                            }
                        }
                    }

                    RelationSymbol relationSymbol = formula.getMainRelation();
                    if (Languages.getLanguage(objLangName).getXrelationSymbols().contains(relationSymbol.getBaseStr())) relationSymbol.setSuperscriptStr(String.valueOf(relationSymbol.getPlaces()));
                    MRelation interpRelation = interpretation.getRelationVal(relationSymbol);

                    if (interpRelation == null) {
                        BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                        if (bdrta != null) {
                            bdrta.setRichTextFieldHighlight();
                            scrollToNode(bdrta.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this relation symbol.")));
                        throw new TextMessageException(null);
                    }

                    MSatValue satValue = new MTheNsat(mLang.getNotSatisfiedSym());
                    Set<MRelTuple> relTuples = interpRelation.getTuples();
                    for (MRelTuple relTuple : relTuples) {
                        if (relTuple.getMembers().equals(assignmentTuple)) {
                            satValue = new MTheSat(mLang.getSatisfiedSym());
                            break;
                        }
                    }
                    if (!satValue.equals(fp.getSatisfactionVal())) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                }
            }

            else {   //complex formula
                Operator mainOperator = formula.getMainOperator();

                if (mainOperator.getType() == ExpressionType.NEG_OP) {
                    BranchNode child = node.getDependents().get(0);
                    if (!emptyNode(child)) {
                        if (theSat.equals(child.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal())) {
                            BoxedDRTA annBDRTA = node.getAnnotationField();
                            if (annBDRTA != null) {
                                annBDRTA.setAnnBoxHighlight();
                                scrollToNode(annBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                        if (theNSat.equals(child.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) {
                            BoxedDRTA annBDRTA = node.getAnnotationField();
                            if (annBDRTA != null) {
                                annBDRTA.setAnnBoxHighlight();
                                scrollToNode(annBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                    }
                    else {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Empty branch cannot support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.COND_OP) {
                    BranchNode child0 = node.getDependents().get(0);
                    BranchNode child1 = node.getDependents().get(1);
                    if ( (!emptyNode(child0) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child1) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child0) && !emptyNode(child1) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal()))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.DISJ_OP) {
                    BranchNode child0 = node.getDependents().get(0);
                    BranchNode child1 = node.getDependents().get(1);
                    if ( (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child1) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child0) && !emptyNode(child1) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal()))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.CONJ_OP) {
                    BranchNode child0 = node.getDependents().get(0);
                    BranchNode child1 = node.getDependents().get(1);
                    if ( (!emptyNode(child0) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child0) && !emptyNode(child1) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal()))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.BICOND_OP) {
                    BranchNode child0 = node.getDependents().get(0);
                    BranchNode child1 = node.getDependents().get(1);
                    if (!emptyNode(child0) && !emptyNode(child1)) {
                        if ( (theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                                (theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                                (theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal())) ||
                                (theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal()))
                        ) {
                            BoxedDRTA annBDRTA = node.getAnnotationField();
                            if (annBDRTA != null) {
                                annBDRTA.setAnnBoxHighlight();
                                scrollToNode(annBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                    }
                    else {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.NAND_OP) {
                    BranchNode child0 = node.getDependents().get(0);
                    BranchNode child1 = node.getDependents().get(1);
                    if ( (!emptyNode(child0) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child0) && !emptyNode(child1) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal()))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.NOR_OP) {
                    BranchNode child0 = node.getDependents().get(0);
                    BranchNode child1 = node.getDependents().get(1);
                    if ( (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child1) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theNSat.equals(fp.getSatisfactionVal())) ||
                            (!emptyNode(child0) && !emptyNode(child1) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal()))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.UNIVERSAL_OP) {
                    boolean someN = false;
                    boolean someE = false;
                    if (node.getDependents() != null) {
                        for (BranchNode child : node.getDependents()) {
                            if (emptyNode(child)) {
                                someE = true;
                            } else if (theNSat.equals(child.getFormulaPacket().getSatisfactionVal())) {
                                someN = true;
                            }
                            if (someN && someE) {
                                break;
                            }
                        }
                    }
                    if ( (someN && !theNSat.equals(fp.getSatisfactionVal())) || (!someN && !someE && !theSat.equals(fp.getSatisfactionVal())) ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if (!someN && someE) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }

                }
                else if (mainOperator.getType() == ExpressionType.EXISTENTIAL_OP) {
                    boolean someS = false;
                    boolean someE = false;
                    if (node.getDependents() != null) {
                        for (BranchNode child : node.getDependents()) {
                            if (emptyNode(child)) {
                                someE = true;
                            } else if (theSat.equals(child.getFormulaPacket().getSatisfactionVal())) {
                                someS = true;
                            }
                            if (someS && someE) {
                                break;
                            }
                        }
                    }
                    if ( (someS && !theSat.equals(fp.getSatisfactionVal())) || (!someS && !someE && !theNSat.equals(fp.getSatisfactionVal())) ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if (!someS && someE) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        if (annBDRTA != null) {
                            annBDRTA.setAnnBoxHighlight();
                            scrollToNode(annBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
            }
        }

        //-------------------
        return true;
    }

    // traverses tree forward
    private boolean checkForwardTree(BranchNode node) throws TextMessageException {
        if (isTornDown || node == null) return false;

        //-----------
        if (node.getFormulaPacket() == null && node.getTermPacket() == null) {
            if (node.getDependents() != null && !node.getDependents().isEmpty()) {
                BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                if (bdrta != null) {
                    bdrta.setRichTextFieldHighlight();
                    scrollToNode(bdrta.getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Empty node cannot have branch(es).")));
                throw new TextMessageException(null);
            }
        }
        else if (node.getFormulaPacket() != null) {
            FormulaPacket fp = node.getFormulaPacket();
            Formula formula = fp.getFormula();
            if (!formula.isAtomic()) {

                //connector ok
                BoxedDRTA connBDRTA = node.getConnectorBoxedDRTA();
                if (connBDRTA != null && connBDRTA.getRTA() != null && mParseUtilities != null) {
                    RichTextArea rta = connBDRTA.getRTA();
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    Document doc = rta.getDocument();
                    List<Expression> expList = mParseUtilities.parseDoc(doc, objLangName);
                    if (expList.size() != 1 || !expList.get(0).equals(formula.getMainOperator())) {

                        if (expList.size() == 0) {
                            scrollToNode(node.getFormulaBoxedDRTA().getRTA());
                            node.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                        }
                        else {
                            connBDRTA.setRichTextFieldHighlight();
                            scrollToNode(connBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch for main operator.")));
                        throw new TextMessageException(null);
                    }
                }

                //branches ok
                Operator op = formula.getMainOperator();
                BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                List<BranchNode> dependents = node.getDependents();
                if (dependents == null) dependents = new ArrayList<>();

                //negation
                if (op.getType() == ExpressionType.NEG_OP) {
                    if (dependents.size() != 1) {
                        if (nodeBDRTA != null) {
                            nodeBDRTA.setRichTextFieldHighlight();
                            scrollToNode(nodeBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires a single branch.")));
                        throw new TextMessageException(null);
                    }
                    boolean branch0Empty = dependents.get(0).getFormulaPacket() == null && dependents.get(0).getTermPacket() == null;
                    if (!permitEmptyBranches && branch0Empty) {
                        if (nodeBDRTA != null) {
                            nodeBDRTA.setRichTextFieldHighlight();
                            scrollToNode(nodeBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires a single non-empty branch.")));
                        throw new TextMessageException(null);
                    }
                    if (!branch0Empty) {
                        if (dependents.get(0).getFormulaPacket() == null) {
                            if (nodeBDRTA != null) {
                                nodeBDRTA.setRichTextFieldHighlight();
                                scrollToNode(nodeBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch.")));
                            throw new TextMessageException(null);
                        }
                        FormulaPacket branchPacket0 = dependents.get(0).getFormulaPacket();
                        if (!branchPacket0.getInterpretation().equals(fp.getInterpretation()) || !branchPacket0.getAssignment().equals(fp.getAssignment()) ||
                                !branchPacket0.getInterpretationVariable().equals(fp.getInterpretationVariable()) || !branchPacket0.getAssignmentVariable().equals(fp.getAssignmentVariable())) {
                            BoxedDRTA branchBDRTA = dependents.get(0).getFormulaBoxedDRTA();
                            if (branchBDRTA != null) {
                                branchBDRTA.setRichTextFieldHighlight();
                                scrollToNode(branchBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should carry forward interpretation and variable assignment from its root.")));
                            throw new TextMessageException(null);
                        }
                        if (!fp.getFormula().getChildren().get(0).equals(branchPacket0.getFormula())) {
                            BoxedDRTA branchBDRTA = dependents.get(0).getFormulaBoxedDRTA();
                            if (branchBDRTA != null) {
                                branchBDRTA.setRichTextFieldHighlight();
                                scrollToNode(branchBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should contain immediate subformula from branch's root.")));
                            throw new TextMessageException(null);
                        }
                    }
                }


                //binary
                else if (op.getType() == ExpressionType.COND_OP || op.getType() == ExpressionType.BICOND_OP || op.getType() == ExpressionType.CONJ_OP || op.getType() == ExpressionType.DISJ_OP || op.getType() == ExpressionType.NAND_OP || op.getType() == ExpressionType.NOR_OP) {
                    if (dependents.size() != 2) {
                        if (nodeBDRTA != null) {
                            nodeBDRTA.setRichTextFieldHighlight();
                            scrollToNode(nodeBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires two branches.")));
                        throw new TextMessageException(null);
                    }
                    boolean branch0Empty = dependents.get(0).getFormulaPacket() == null && dependents.get(0).getTermPacket() == null;
                    boolean branch1Empty = dependents.get(1).getFormulaPacket() == null && dependents.get(1).getTermPacket() == null;
                    if (permitEmptyBranches && (branch0Empty && branch1Empty)) {
                        if (nodeBDRTA != null) {
                            nodeBDRTA.setRichTextFieldHighlight();
                            scrollToNode(nodeBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires at least one non-empty branch.")));
                        throw new TextMessageException(null);
                    }
                    if (!permitEmptyBranches && (branch0Empty || branch1Empty)) {
                        if (nodeBDRTA != null) {
                            nodeBDRTA.setRichTextFieldHighlight();
                            scrollToNode(nodeBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires two non-empty branches.")));
                        throw new TextMessageException(null);
                    }
                    if (!branch0Empty) {
                        if (dependents.get(0).getFormulaPacket() == null) {
                            if (nodeBDRTA != null) {
                                nodeBDRTA.setRichTextFieldHighlight();
                                scrollToNode(nodeBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch(es).")));
                            throw new TextMessageException(null);
                        }

                        FormulaPacket branchPacket0 = dependents.get(0).getFormulaPacket();
                        if (!branchPacket0.getInterpretation().equals(fp.getInterpretation()) || !branchPacket0.getAssignment().equals(fp.getAssignment()) ||
                                !branchPacket0.getInterpretationVariable().equals(fp.getInterpretationVariable()) || !branchPacket0.getAssignmentVariable().equals(fp.getAssignmentVariable())) {
                            BoxedDRTA branchBDRTA = dependents.get(0).getFormulaBoxedDRTA();
                            if (branchBDRTA != null) {
                                branchBDRTA.setRichTextFieldHighlight();
                                scrollToNode(branchBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should carry forward interpretation and variable assignment from its root.")));
                            throw new TextMessageException(null);
                        }
                        if (!fp.getFormula().getChildren().get(0).equals(branchPacket0.getFormula())) {
                            BoxedDRTA branchBDRTA = dependents.get(0).getFormulaBoxedDRTA();
                            if (branchBDRTA != null) {
                                branchBDRTA.setRichTextFieldHighlight();
                                scrollToNode(branchBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Top branch should have leftmost immediate subformula from branch's root.")));
                            throw new TextMessageException(null);
                        }
                    }
                    if (!branch1Empty) {
                        if (dependents.get(1).getFormulaPacket() == null) {
                            if (nodeBDRTA != null) {
                                nodeBDRTA.setRichTextFieldHighlight();
                                scrollToNode(nodeBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch(es).")));
                            throw new TextMessageException(null);
                        }
                        FormulaPacket branchPacket1 = dependents.get(1).getFormulaPacket();
                        if (!branchPacket1.getInterpretation().equals(fp.getInterpretation()) || !branchPacket1.getAssignment().equals(fp.getAssignment()) ||
                                !branchPacket1.getInterpretationVariable().equals(fp.getInterpretationVariable()) || !branchPacket1.getAssignmentVariable().equals(fp.getAssignmentVariable())) {
                            BoxedDRTA branchBDRTA = dependents.get(1).getFormulaBoxedDRTA();
                            if (branchBDRTA != null) {
                                branchBDRTA.setRichTextFieldHighlight();
                                scrollToNode(branchBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should carry forward interpretation and variable assignment from its root.")));
                            throw new TextMessageException(null);
                        }
                        if (!fp.getFormula().getChildren().get(1).equals(branchPacket1.getFormula())) {
                            BoxedDRTA branchBDRTA = dependents.get(1).getFormulaBoxedDRTA();
                            if (branchBDRTA != null) {
                                branchBDRTA.setRichTextFieldHighlight();
                                scrollToNode(branchBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Bottom branch should have rightmost immediate subformula from branch's root.")));
                            throw new TextMessageException(null);
                        }
                    }
                }

                //quantifier
                else if (op.getType() == ExpressionType.UNIVERSAL_OP || op.getType() == ExpressionType.EXISTENTIAL_OP) {
                    if (dependents.size() != fp.getInterpretation().getmUniverse().getUniverse().size()) {
                        if (nodeBDRTA != null) {
                            nodeBDRTA.setRichTextFieldHighlight();
                            scrollToNode(nodeBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires as many branches as there are members of the universe.")));
                        throw new TextMessageException(null);
                    }

                    //empty branches
                    if (!permitEmptyBranches) {
                        for (BranchNode dependent : dependents) {
                            if (dependent.getFormulaPacket() == null && dependent.getTermPacket() == null) {
                                if (nodeBDRTA != null) {
                                    nodeBDRTA.setRichTextFieldHighlight();
                                    scrollToNode(nodeBDRTA.getRTA());
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node cannot have empty branch.")));
                                throw new TextMessageException(null);
                            }
                        }
                    }
                    else {
                        boolean empty = true;
                        for (BranchNode dependent : dependents) {
                            if (dependent.getFormulaPacket() != null || dependent.getTermPacket() != null) {
                                empty = false;
                                break;
                            }
                        }
                        if (empty) {
                            if (nodeBDRTA != null) {
                                nodeBDRTA.setRichTextFieldHighlight();
                                scrollToNode(nodeBDRTA.getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node must have at least one nonempty branch.")));
                            throw new TextMessageException(null);
                        }
                    }

                    //formula branches
                    for (BranchNode dependent : dependents) {
                        if (!(dependent.getFormulaPacket() == null && dependent.getTermPacket() == null)) {
                            if (dependent.getFormulaPacket() == null) {
                                BoxedDRTA branchBDRTA = dependent.getFormulaBoxedDRTA();
                                if (branchBDRTA != null) {
                                    branchBDRTA.setRichTextFieldHighlight();
                                    scrollToNode(branchBDRTA.getRTA());
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch(es).")));
                                throw new TextMessageException(null);
                            }
                        }
                    }

                    for (BranchNode dependent : dependents) {
                        if (!(dependent.getFormulaPacket() == null && dependent.getTermPacket() == null)) {
                            if (!fp.getFormula().getChildren().get(0).equals(dependent.getFormulaPacket().getFormula())) {
                                BoxedDRTA branchBDRTA = dependent.getFormulaBoxedDRTA();
                                if (branchBDRTA != null) {
                                    branchBDRTA.setRichTextFieldHighlight();
                                    scrollToNode(branchBDRTA.getRTA());
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should have immediate subformula from branch's root.")));
                                throw new TextMessageException(null);
                            }
                        }
                    }

                    Operator mainOperator = fp.getFormula().getMainOperator();
                    Term variable = null;
                    if (mainOperator instanceof UniversalOp) variable = ((UniversalOp) mainOperator).getVariableTerm();
                    else if (mainOperator instanceof ExistentialOp) variable = mainOperator.getVariableTerm();
                    Set<MBasicObjTerm> universe = fp.getInterpretation().getmUniverse().getUniverse();

                    //assignments for all members of universe
                    if (!permitEmptyBranches) {
                        for (MBasicObjTerm objTerm : universe) {
                            boolean found = false;
                            for (BranchNode dependent : dependents) {
                                Pair<Term, MBasicObjTerm> lastAssignment = dependent.getFormulaPacket().getLastAssignment();
                                if (new Pair(variable, objTerm).equals(lastAssignment)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                if (nodeBDRTA != null) {
                                    nodeBDRTA.setRichTextFieldHighlight();
                                    scrollToNode(nodeBDRTA.getRTA());
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("For quantified variable, assignments must be augmented for quantifed variable with a branch for each member of the universe.")));
                                throw new TextMessageException(null);
                            }
                        }
                    }

                    for (BranchNode dependent : dependents) {
                        if (!(dependent.getFormulaPacket() == null && dependent.getTermPacket() == null)) {
                            FormulaPacket branchPacket = dependent.getFormulaPacket();

                            for (BranchNode dep : dependents) {
                                if (!(dep.getFormulaPacket() == null && dep.getTermPacket() == null)) {
                                    if (!dependent.equals(dep) && branchPacket.getAssignment().equals(dep.getFormulaPacket().getAssignment())) {
                                        if (dep.getFormulaBoxedDRTA() != null) {
                                            dep.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                            scrollToNode(dep.getFormulaBoxedDRTA().getRTA());
                                        }
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Duplicate assignment.")));
                                        throw new TextMessageException(null);
                                    }

                                }
                            }

                            //branch has assignments from root (excepting variable)
                            for (Map.Entry<Term, MBasicObjTerm> entry : fp.getAssignment().entrySet()) {
                                if (!entry.getKey().equals(variable)) {
                                    if (!branchPacket.getAssignment().entrySet().contains(entry)) {
                                        if (dependent.getFormulaBoxedDRTA() != null) {
                                            dependent.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                            scrollToNode(dependent.getFormulaBoxedDRTA().getRTA());
                                        }
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch must carry forward all the assignments from its root.")));
                                        throw new TextMessageException(null);
                                    }
                                }
                            }
                            //root has assignments from branch (excepting variable)
                            for (Map.Entry<Term, MBasicObjTerm> entry : branchPacket.getAssignment().entrySet()) {
                                if (!entry.getKey().equals(variable)) {
                                    if (!fp.getAssignment().entrySet().contains(entry)) {
                                        if (dependent.getFormulaBoxedDRTA() != null) {
                                            dependent.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                            scrollToNode(dependent.getFormulaBoxedDRTA().getRTA());
                                        }
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should have just assignments from its root plus an assignment to the quantified variable.")));
                                        throw new TextMessageException(null);
                                    }
                                }
                            }

                            Pair<Term, MBasicObjTerm> lastAssignment = branchPacket.getLastAssignment();
                            if (!fp.getInterpretation().getmUniverse().getUniverse().contains(lastAssignment.getValue())) {
                                if (dependent.getFormulaBoxedDRTA() != null) {
                                    dependent.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                    scrollToNode(dependent.getFormulaBoxedDRTA().getRTA());
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Assignment to quantified variable is not a member of the universe.")));
                                throw new TextMessageException(null);
                            }
                        }
                    }
                }


            }
            else {   //atomic sentence
                if (formula instanceof SentenceAtomic) {
                    if (node.getDependents() != null && !node.getDependents().isEmpty()) {
                        BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                        if (nodeBDRTA != null) {
                            nodeBDRTA.setRichTextFieldHighlight();
                            scrollToNode(nodeBDRTA.getRTA());
                        }
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Sentence letter does not branch.")));
                        throw new TextMessageException(null);
                    }
                }
                List<Expression> children = formula.getChildren();
                if (node.getDependents() != null && children.size() != node.getDependents().size()) {
                    BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                    if (nodeBDRTA != null) {
                        nodeBDRTA.setRichTextFieldHighlight();
                        scrollToNode(nodeBDRTA.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node should have one branch for each of its terms.")));
                    throw new TextMessageException(null);
                }

                if (node.getDependents() != null) {
                    for (BranchNode branch : node.getDependents()) {
                        if (branch.getTermPacket() == null) {
                            if (branch.getFormulaBoxedDRTA() != null) {
                                branch.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                scrollToNode(branch.getFormulaBoxedDRTA().getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No empty branch on atomic formula.")));
                            throw new TextMessageException(null);
                        }
                    }
                    if (children != null) {
                        for (int i = 0; i < children.size(); i++) {
                            BranchNode branch = node.getDependents().get(i);
                            if (!((Term) children.get(i)).equals(branch.getTermPacket().getTerm())) {
                                if (branch.getFormulaBoxedDRTA() != null) {
                                    branch.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                    scrollToNode(branch.getFormulaBoxedDRTA().getRTA());
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch for terms of root (taken in order).")));
                                throw new TextMessageException(null);
                            }
                        }
                    }
                }

            }
        }

        //term
        else if (node.getTermPacket() != null) {
            Term term = node.getTermPacket().getTerm();
            if (term.getTermType() == TermType.CONSTANT || term.getTermType() == TermType.VARIABLE) {
                if (node.getDependents() != null && !node.getDependents().isEmpty()) {
                    BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                    if (nodeBDRTA != null) {
                        nodeBDRTA.setRichTextFieldHighlight();
                        scrollToNode(nodeBDRTA.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Variable or constant sentence does not branch.")));
                    throw new TextMessageException(null);
                }
            }
            else {
                List<Expression> children = term.getChildren();
                if (node.getDependents() != null && children.size() != node.getDependents().size()) {
                    BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                    if (nodeBDRTA != null) {
                        nodeBDRTA.setRichTextFieldHighlight();
                        scrollToNode(nodeBDRTA.getRTA());
                    }
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node should have one branch for each of its immediate subterms.")));
                    throw new TextMessageException(null);
                }
                if (node.getDependents() != null) {
                    for (BranchNode branch : node.getDependents()) {
                        if (branch.getTermPacket() == null) {
                            if (branch.getFormulaBoxedDRTA() != null) {
                                branch.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                scrollToNode(branch.getFormulaBoxedDRTA().getRTA());
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No empty branch on term.")));
                            throw new TextMessageException(null);
                        }
                    }


                    if (children != null) {
                        for (int i = 0; i < children.size(); i++) {
                            BranchNode branch = node.getDependents().get(i);
                            if (!((Term) children.get(i)).equals(branch.getTermPacket().getTerm())) {
                                if (branch.getFormulaBoxedDRTA() != null) {
                                    branch.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                                    scrollToNode(branch.getFormulaBoxedDRTA().getRTA());
                                }
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch for immediate subterms of root (taken in order).")));
                                throw new TextMessageException(null);
                            }
                        }
                    }
                }
            }
        }

        //----------

        if (node.getDependents() != null) {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode child = node.getDependents().get(i);
                checkForwardTree(child);
            }
        }
        return true;
    }


    private boolean setSemanticValues(BranchNode node) throws TextMessageException {
        if (isTornDown || node == null) return false;

        //--------------

        BoxedDRTA annBDRTA = node.getAnnotationField();

        if (node.isFormulaNode()) {
            MSatValue satValue = extractSatValue(annBDRTA);
            if (emptyNode(node) && !(satValue == null || satValue.equals(new MTheEsat("")))) {
                if (annBDRTA != null) {
                    annBDRTA.setAnnBoxHighlight();
                    scrollToNode(annBDRTA.getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Empty node cannot have satisfaction value.")));
                throw new TextMessageException(null);
            }
            else if (!emptyNode(node) && !node.isAnnotation()) {
                if (node.getFormulaBoxedDRTA() != null) {
                    node.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                    scrollToNode(node.getFormulaBoxedDRTA().getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Missing annotation field.")));
                throw new TextMessageException(null);

            }
            else if (!emptyNode(node) && (new MTheEsat("").equals(satValue))) {
                if (annBDRTA != null) {
                    annBDRTA.setAnnBoxHighlight();
                    scrollToNode(annBDRTA.getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Missing value.")));
                throw new TextMessageException(null);
            }
            else if (!emptyNode(node) && satValue == null) {
                if (annBDRTA != null) {
                    annBDRTA.setAnnBoxHighlight();
                    scrollToNode(annBDRTA.getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Cannot parse to expression of the sort, (\ud835\uddb2) or (\ud835\uddad).")));
                throw new TextMessageException(null);
            }
            else if (!emptyNode(node)) {
                node.getFormulaPacket().setSatisfactionVal(satValue);
            }
        }
        else {   //term node
            if (!emptyNode(node) && !node.isAnnotation()) {
                if (node.getFormulaBoxedDRTA() != null) {
                    node.getFormulaBoxedDRTA().setRichTextFieldHighlight();
                    scrollToNode(node.getFormulaBoxedDRTA().getRTA());
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Missing annotation field.")));
                throw new TextMessageException(null);
            }

            MBasicObjTerm obj = extractObjValue(annBDRTA);
            if (obj == null) {
                throw new TextMessageException(null);
            }
            else {
                node.getTermPacket().setBasicObject(obj);
            }
        }

        //---------

        if (node.getDependents() != null) {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode child = node.getDependents().get(i);
                setSemanticValues(child);
            }
        }
        return true;
    }

    private boolean emptyNode(BranchNode node) {
        if (isTornDown || node == null) return true;
        return node.getFormulaPacket() == null && node.getTermPacket() == null;
    }

    private MBasicObjTerm extractObjValue(BoxedDRTA bdrta) {
        if (isTornDown || bdrta == null || bdrta.getRTA() == null || mParseUtilities == null) return null;
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        List<Expression> expressions = mParseUtilities.parseObj(doc, objLangName);

        if (expressions.size() == 0) {
            bdrta.setAnnBoxHighlight();
            scrollToNode(bdrta.getRTA());
            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No empty value for term.")));
            return null;
        }
        else if (expressions.size() == 3 && expressions.get(0) instanceof MOpenSquareBracket && expressions.get(1) instanceof MBasicObjTerm && expressions.get(2) instanceof MCloseSquareBracket) {
            return (MBasicObjTerm) expressions.get(1);
        }
        else {
            bdrta.setAnnBoxHighlight();
            scrollToNode(bdrta.getRTA());
            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Cannot parse to expression of the sort, Open square bracket / Object term / Close square bracket.")));
            return null;
        }
    }

    private MSatValue extractSatValue(BoxedDRTA bdrta) {
        if (isTornDown || bdrta == null || bdrta.getRTA() == null || mLang == null) return null;
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        String str = rta.getDocument().getText();
        if (str.matches("^\\s*\\(" + mLang.getSatisfiedSym() + "\\)\\s*$")) return new MTheSat(mLang.getSatisfiedSym());
        else if (str.matches("^\\s*\\(" + mLang.getNotSatisfiedSym() + "\\)\\s*$")) return new MTheNsat(mLang.getNotSatisfiedSym());
        else if (str.matches("^\\s*")) return new MTheEsat("");
        else return null;
    }

    private boolean setPackets(BranchNode node, SemanticPacket lastPacket) throws TextMessageException {
        if (isTornDown || node == null) return true;

        //-----------
        BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
        if (bdrta == null || bdrta.getRTA() == null || mParseUtilities == null) return true;
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        List<Expression> expList = mParseUtilities.parseDoc(doc, objLangName);
        SemanticPacket currentPacket = null;
        node.setFormulaPacket(null);
        node.setTermPacket(null);

        if (expList.size() == 0) {
            if (!permitEmptyBranches) {
                bdrta.setRichTextFieldHighlight();
                scrollToNode(bdrta.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Parse Issue", Collections.singletonList(ParseUtilities.newRegularText("Empty field.")));
                throw new TextMessageException(null);
            }
            else {
                return true;
            }
        }
        else if (expList.size() > 1 || (expList.get(0).getType() != ExpressionType.M_INTERP_ASSIGN_FORMULA && expList.get(0).getType() != ExpressionType.TERM)) {
            bdrta.setRichTextFieldHighlight();
            scrollToNode(bdrta.getRTA());
            List<Text> texts = new ArrayList<>();

            texts.add(ParseUtilities.newRegularText("On tree branch expect interpretation / subscript variable assignment / bracketed formula (or just a term).\n\n Found:\n"));
            for (Expression expr : expList) {
                texts.add(new Text(expr.getType() + ": "));
                texts.addAll(expr.toTextList());
                texts.add(new Text("\n"));
            }
            EditorAlerts.showSimpleTxtListAlert("Unexpected Content", texts);
            throw new TextMessageException(null);
        }
        else {
            if (expList.get(0).getType() == ExpressionType.M_INTERP_ASSIGN_FORMULA) {
                if (!node.isFormulaNode()) {
                    bdrta.setRichTextFieldHighlight();
                    scrollToNode(bdrta.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Formula assignment on term branch.")));
                    throw new TextMessageException(null);

                }
                if (mSyntacticalFns != null && mSyntacticalFns.setFormulaPacket(bdrta)) {
                    FormulaPacket formulaPacket = mSyntacticalFns.getFormulaPacket();
                    currentPacket = formulaPacket;
                    node.setFormulaPacket(formulaPacket);
                }
                else {
                    throw new TextMessageException(null);
                }
            }
            else {
                if (node.isFormulaNode()) {
                    bdrta.setRichTextFieldHighlight();
                    scrollToNode(bdrta.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Term on formula branch.")));
                    throw new TextMessageException(null);
                }

                Term term = (Term) expList.get(0);
                TermPacket termPacket = new TermPacket(term, lastPacket.getInterpretation(), lastPacket.getAssignment());
                currentPacket = termPacket;
                node.setTermPacket(termPacket);
            }
        }

        //-------------

        if (node.getDependents() != null) {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode child = node.getDependents().get(i);
                setPackets(child, currentPacket);
            }
        }
        return true;
    }

    private void resetHighlights(BranchNode node) {
        if (isTornDown || node == null) return;

        BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
        if (bdrta != null) bdrta.resetRichTextFieldHighlight();
        BoxedDRTA conn = node.getConnectorBoxedDRTA();
        if (conn != null) conn.resetRichTextFieldHighlight();
        BoxedDRTA ann = node.getAnnotationField();
        if (ann != null) ann.resetAnnHighlight();

        if (node.getDependents() != null) {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode child = node.getDependents().get(i);
                resetHighlights(child);
            }
        }
    }

    private void traverseTreeForward(BranchNode node) {
        if (isTornDown || node == null) return;

        if (node.getFormulaPacket() != null) {System.out.println(node.getFormulaPacket().toString());}
        if (node.getTermPacket() != null) {System.out.println(node.getTermPacket().toString());}

        if (node.getDependents() != null) {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode child = node.getDependents().get(i);
                traverseTreeForward(child);
            }
        }
    }

    private void traverseTreeBackward(BranchNode node) {
        if (isTornDown || node == null) return;

        if (node.getDependents() != null) {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode child = node.getDependents().get(i);
                traverseTreeBackward(child);
            }
        }

        if (node.getFormulaBoxedDRTA() != null && node.getFormulaBoxedDRTA().getRTA() != null) {
            RichTextArea nodeRTA = node.getFormulaBoxedDRTA().getRTA();
            nodeRTA.getActionFactory().saveNow().execute(new ActionEvent());
            System.out.println(nodeRTA.getDocument().getText());
        }
    }

    private void setRightControlBox() {
        if (isTornDown || htView == null || checkSetup == null) return;

        if (htView.getCheckButton() != null) {
            checkButtonHandler = e -> {
                if (isTornDown || htExercise == null || htView == null) return;
                checkTrees();
            };
            htView.getCheckButton().setOnAction(checkButtonHandler);
        }

        if (htView.getStaticHelpButton() != null) {
            htView.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

            staticHelpButtonHandler = e -> {
                if (isTornDown || htExercise == null || htView == null || htModel == null || htModel.getCheckSetup() == null) return;
                Stage helpStage = htView.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else htView.showStaticHelp(htModel.getCheckSetup().getStaticHelpDoc());
            };
            htView.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
        }

        if (htView.getPermitEmptyBranchesCheck() != null) {
            htView.getPermitEmptyBranchesCheck().setDisable(!checkSetup.isEmptyBranchesOK());
            htView.getPermitEmptyBranchesCheck().setSelected(checkSetup.isEmptyBranchesSelected());
        }
    }

    private void updateCheckCounter() {
        if (isTornDown || htExercise == null || htView == null || htExercise.getMainWindow() == null) return;

        if (checkMax != -1 && checkTries >= checkMax && !htExercise.getMainWindow().isInstructorFunctions()) {
            if (htView.getCheckButton() != null) htView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        if (htView.getCheckTriesLabel() != null) {
            htView.getCheckTriesLabel().setText(checkString);
        }
    }

    public void cleanup() {
        // KILL SWITCH: Engaged!
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Clear Global Static Caches
        try { MatchUtilities.clearMatching(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 3: Dozens of isolated try-catch blocks
        try {
            if (htView != null && htView.getCheckButton() != null && checkButtonHandler != null) {
                htView.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                htView.getCheckButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (htView != null && htView.getStaticHelpButton() != null && staticHelpButtonHandler != null) {
                htView.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                htView.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // 8. Final severed ties
        try { this.htExercise = null; } catch (Exception e) {}
        try { this.htView = null; } catch (Exception e) {}
        try { this.htModel = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.objLangName = null; } catch (Exception e) {}
        try { this.mLang = null; } catch (Exception e) {}
        try { this.mSyntacticalFns = null; } catch (Exception e) {}
        try { this.mParseUtilities = null; } catch (Exception e) {}
        try { this.theSat = null; } catch (Exception e) {}
        try { this.theNSat = null; } catch (Exception e) {}
        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
    }

    public int getCheckMax() {
        return checkMax;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public HorizTreesCheckSetup getCheckSetup() {
        return checkSetup;
    }
}