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

        if (checkSuccess) htView.activateBigCheck();
        else htView.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();


        setRightControlBox();
    }

    public boolean checkTrees() {
        checkSuccess = false;
        htView.deactivateBigCheck();
        if (!htExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            updateCheckCounter();
        }

        //--------------
        permitEmptyBranches = htExercise.getExerciseView().getPermitEmptyBranchesCheck().isSelected();
        RichTextArea interpRTA = htView.getInterpretationDRTA().getEditor();
        interpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        if (!mSyntacticalFns.setInterpBaseVarAssign(interpRTA.getDocument(), objLangName)) return false;

        List<Exercise> exercises = htExercise.getExerciseList();
        for (int i = 0; i < exercises.size(); i++ ) {
            HorizontalTreeExercise htExer = ((HorizontalTreeExercise) exercises.get(i));

            List<TreePane> panes = htExer.getHorizontalTreeView().getTreePanes();
            for (int j = 0; j < panes.size(); j++ ) {
                TreePane tp = panes.get(j);
                BranchNode root = tp.getRootBranchNode();

                if (!checkTree(root)) return false;
            }
        }

        if (!checkTargets()) return false;


        //------------
        checkSuccess = true;
        htView.activateBigCheck();

        return true;
    }

    private boolean checkTargets() {
        List<Document> targets = checkSetup.getTargetFormulas();
        boolean good = true;


        for (Document doc : targets) {
            Formula target = null;

            List<Expression> expList = mParseUtilities.parseDoc(doc, objLangName);
            if (!expList.isEmpty()) {
                Expression exp = expList.get(0);
                if (exp instanceof Formula) target = (Formula) exp;

                //could insert superscripts on both target and node formulas or (as here) depend on target and node
                //formulas both with or both without.  The former may lead to error messages distinct from actual target.

            //    insertSuperscripts(target);

                if (target != null) {
                    List<Exercise> exercises = htExercise.getExerciseList();

                    boolean found = false;
                    searchLoop:
                    for (int i = 0; i < exercises.size(); i++) {
                        HorizontalTreeExercise htExer = ((HorizontalTreeExercise) exercises.get(i));
                        List<TreePane> panes = htExer.getHorizontalTreeView().getTreePanes();
                        for (int j = 0; j < panes.size(); j++) {
                            TreePane tp = panes.get(j);
                            BranchNode root = tp.getRootBranchNode();

                            BoxedDRTA rootBDRTA = root.getFormulaBoxedDRTA();
                            RichTextArea rootRTA = rootBDRTA.getRTA();
                            rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
                            Document rootDoc = rootRTA.getDocument();
                            List<Expression> rootExps = mParseUtilities.parseDoc(rootDoc, objLangName);
                            MInterpAssignFormula rootIntAss = (MInterpAssignFormula) rootExps.get(0);
                            Formula rootFormula = rootIntAss.getFormula();

                            if (target.equals(rootFormula)) {
                                found = true;
                                break searchLoop;
                            }


                            /*
                            FormulaPacket fp = root.getFormulaPacket();
                            Formula f = fp.getFormula();
                            insertSuperscripts(f);

                            if (target.equals(f)) {
                                found = true;
                                break searchLoop;
                            }
                             */

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
        if (formula == null) return;

        if (formula.isAtomic() && formula instanceof PrefixAtomic) {
            RelationSymbol relationSymbol = formula.getMainRelation();
            if (Languages.getLanguage(objLangName).getXrelationSymbols().contains(relationSymbol.getBaseStr())) relationSymbol.setSuperscriptStr(String.valueOf(relationSymbol.getPlaces()));
        }

        for (Expression child : formula.getChildren()) {
            if (child.getType() == ExpressionType.FORMULA) {
                insertSuperscripts((Formula) child);
            }
        }
    }

    private boolean checkTree(BranchNode root) {
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
        if (node == null || emptyNode(node)) return false;

        for (int i = 0; i < node.getDependents().size(); i++) {
            BranchNode child = node.getDependents().get(i);
            checkBackwardTree(child);
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
                    bdrta.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this constant.")));
                    throw new TextMessageException(null);
                }
                if (!assignedObj.equals(tp.getBasicObject())) {
                    BoxedDRTA annBDRTA = node.getAnnotationField();
                    annBDRTA.setAnnBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                    throw new TextMessageException(null);
                }
            }

            if (term.getTermType() == TermType.VARIABLE) {
                LinkedHashMap<Term, MBasicObjTerm> assignment = tp.getAssignment();
                MBasicObjTerm assignedObj = assignment.get(term);
                if (assignedObj == null) {
                    BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                    bdrta.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Variable assignment sets no value for this variable.")));
                    throw new TextMessageException(null);
                }
                if (!assignedObj.equals(tp.getBasicObject())) {
                    BoxedDRTA annBDRTA = node.getAnnotationField();
                    annBDRTA.setAnnBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                    throw new TextMessageException(null);
                }
            }

            if (term.getTermType() == TermType.COMPLEX) {
                List<MBasicObjTerm> assignmentTuple = new ArrayList<>();
                for (int i = 0; i < node.getDependents().size(); i++) {
                    BranchNode child = node.getDependents().get(i);
                    assignmentTuple.add(child.getTermPacket().getBasicObject());
                }
                MBasicObjTerm assignedObj = null;
                FunctionSymbol fnSym = term.getMainFnSymbol();
                MFunctionCandidate fnCandidate = tp.getInterpretation().getFunctionVal(fnSym);

                if (fnCandidate == null) {
                    BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                    bdrta.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
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
                    bdrta.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation sets no value for this input.")));
                    throw new TextMessageException(null);
                }
                if (!assignedObj.equals(tp.getBasicObject())) {
                    BoxedDRTA annBDRTA = node.getAnnotationField();
                    annBDRTA.setAnnBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                        bdrta.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation sets no value for this letter.")));
                        throw new TextMessageException(null);
                    }
                    else {
                        MSatValue satValue = fp.getSatisfactionVal();
                        if ((satValue instanceof MTheSat && !(truthValue instanceof MTheTrue)) || (satValue instanceof MTheNsat && !(truthValue instanceof MTheFalse))) {
                            BoxedDRTA annBDRTA = node.getAnnotationField();
                            annBDRTA.setAnnBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                    }
                }
                else {
                    List<MBasicObjTerm> assignmentTuple = new ArrayList<>();
                    for (int i = 0; i < node.getDependents().size(); i++) {
                        BranchNode child = node.getDependents().get(i);
                        assignmentTuple.add(child.getTermPacket().getBasicObject());
                    }

                    RelationSymbol relationSymbol = formula.getMainRelation();
                    if (Languages.getLanguage(objLangName).getXrelationSymbols().contains(relationSymbol.getBaseStr())) relationSymbol.setSuperscriptStr(String.valueOf(relationSymbol.getPlaces()));
                    MRelation interpRelation = interpretation.getRelationVal(relationSymbol);

                    if (interpRelation == null) {
                        BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                        bdrta.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
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
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                            annBDRTA.setAnnBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                        if (theNSat.equals(child.getFormulaPacket().getSatisfactionVal()) && !theSat.equals(fp.getSatisfactionVal())) {
                            BoxedDRTA annBDRTA = node.getAnnotationField();
                            annBDRTA.setAnnBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                    }
                    else {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                            annBDRTA.setAnnBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            throw new TextMessageException(null);
                        }
                    }
                    else {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
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
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if ( (emptyNode(child0) && emptyNode(child1)) ||
                            (!emptyNode(child0) && theNSat.equals(child0.getFormulaPacket().getSatisfactionVal()) && emptyNode(child1)) ||
                            (!emptyNode(child1) && theNSat.equals(child1.getFormulaPacket().getSatisfactionVal()) && emptyNode(child0))
                    ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
                else if (mainOperator.getType() == ExpressionType.UNIVERSAL_OP) {
                    boolean someN = false;
                    boolean someE = false;
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
                    if ( (someN && !theNSat.equals(fp.getSatisfactionVal())) || (!someN && !someE && !theSat.equals(fp.getSatisfactionVal())) ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if (!someN && someE) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }

                }
                else if (mainOperator.getType() == ExpressionType.EXISTENTIAL_OP) {
                    boolean someS = false;
                    boolean someE = false;
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
                    if ( (someS && !theSat.equals(fp.getSatisfactionVal())) || (!someS && !someE && !theNSat.equals(fp.getSatisfactionVal())) ) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        throw new TextMessageException(null);
                    }
                    if (!someS && someE) {
                        BoxedDRTA annBDRTA = node.getAnnotationField();
                        annBDRTA.setAnnBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Completed branches (if any) are not sufficient to support this satisfaction value.")));
                        throw new TextMessageException(null);
                    }
                }
            }
        }


        //-------------------

        return true;
    }


    //traverse tree forward
    private boolean setSemanticValues(BranchNode node) throws TextMessageException {
        if (node == null) return false;

        //--------------

        BoxedDRTA annBDRTA = node.getAnnotationField();

        if (node.isFormulaNode()) {
            MSatValue satValue = extractSatValue(annBDRTA);
            if (emptyNode(node) && !(satValue == null || satValue.equals(new MTheEsat("")))) {
                annBDRTA.setAnnBoxHighlight();
                htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Empty node cannot have satisfaction value.")));
                throw new TextMessageException(null);
            }
            else if (!emptyNode(node) && !node.isAnnotation()) {
                node.getFormulaBoxedDRTA().setAnnBoxHighlight();
                htExercise.getMainWindow().getMainView().scrollToNode(node.getFormulaBoxedDRTA().getRTA());
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Missing annotation field.")));
                throw new TextMessageException(null);

            }
            else if (!emptyNode(node) && (new MTheEsat("").equals(satValue))) {
                annBDRTA.setAnnBoxHighlight();
                htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Missing value.")));
                throw new TextMessageException(null);
            }
            else if (!emptyNode(node) && satValue == null) {
                annBDRTA.setAnnBoxHighlight();
                htExercise.getMainWindow().getMainView().scrollToNode(annBDRTA.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Cannot parse to expression of the sort, (\ud835\uddb2) or (\ud835\uddad).")));
                throw new TextMessageException(null);
            }
            else if (!emptyNode(node)) {
                node.getFormulaPacket().setSatisfactionVal(satValue);
            }
        }
        else {   //term node
            if (!emptyNode(node) && !node.isAnnotation()) {
                node.getFormulaBoxedDRTA().setAnnBoxHighlight();
                htExercise.getMainWindow().getMainView().scrollToNode(node.getFormulaBoxedDRTA().getRTA());
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

        for (int i = 0; i < node.getDependents().size(); i++) {
            BranchNode child = node.getDependents().get(i);
            setSemanticValues(child);
        }
        return true;
    }



    private boolean emptyNode(BranchNode node) {
        return node.getFormulaPacket() == null && node.getTermPacket() == null;
    }

    private MBasicObjTerm extractObjValue(BoxedDRTA bdrta) {
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        List<Expression> expressions = mParseUtilities.parseObj(doc, objLangName);

        if (expressions.size() == 0) {
            bdrta.setAnnBoxHighlight();
            htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No empty value for term.")));
            return null;
        }
        else if (expressions.size() == 3 && expressions.get(0) instanceof MOpenSquareBracket && expressions.get(1) instanceof MBasicObjTerm && expressions.get(2) instanceof MCloseSquareBracket) {
            return (MBasicObjTerm) expressions.get(1);
        }
        else {
            bdrta.setAnnBoxHighlight();
            htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Cannot parse to expression of the sort, Open square bracket / Object term / Close square bracket.")));
            return null;
        }
    }

    private MSatValue extractSatValue(BoxedDRTA bdrta) {
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        String str = rta.getDocument().getText();
        if (str.matches("^\\s*\\(" + mLang.getSatisfiedSym() + "\\)\\s*$")) return new MTheSat(mLang.getSatisfiedSym());
        else if (str.matches("^\\s*\\(" + mLang.getNotSatisfiedSym() + "\\)\\s*$")) return new MTheNsat(mLang.getNotSatisfiedSym());
        else if (str.matches("^\\s*")) return new MTheEsat("");


        else return null;
    }



    //traverses tree forward
    private boolean checkForwardTree(BranchNode node) throws TextMessageException {
        if (node == null) return false;

        //-----------
        if (node.getFormulaPacket() == null && node.getTermPacket() == null) {
            if (!node.getDependents().isEmpty()) {
                BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
                bdrta.setVTtreeBoxHighlight();
                htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
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
                RichTextArea rta = connBDRTA.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document doc = rta.getDocument();
                List<Expression> expList = mParseUtilities.parseDoc(doc, objLangName);
                if (expList.size() != 1 || !expList.get(0).equals(formula.getMainOperator())) {
                    connBDRTA.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(connBDRTA.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch for main operator.")));
                    throw new TextMessageException(null);
                }

                //branches ok
                Operator op = formula.getMainOperator();
                BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                List<BranchNode> dependents = node.getDependents();

                //negation
                if (op.getType() == ExpressionType.NEG_OP) {
                    if (dependents.size() != 1) {
                        nodeBDRTA.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires a single branch.")));
                        throw new TextMessageException(null);
                    }
                    boolean branch0Empty = dependents.get(0).getFormulaPacket() == null && dependents.get(0).getTermPacket() == null;
                    if (!permitEmptyBranches && branch0Empty) {
                        nodeBDRTA.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires a single non-empty branch.")));
                        throw new TextMessageException(null);
                    }
                    if (!branch0Empty) {
                        if (dependents.get(0).getFormulaPacket() == null) {
                            nodeBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch.")));
                            throw new TextMessageException(null);
                        }
                        FormulaPacket branchPacket0 = dependents.get(0).getFormulaPacket();
                        if (!branchPacket0.getInterpretation().equals(fp.getInterpretation()) || !branchPacket0.getAssignment().equals(fp.getAssignment()) ||
                                !branchPacket0.getInterpretationVariable().equals(fp.getInterpretationVariable()) || !branchPacket0.getAssignmentVariable().equals(fp.getAssignmentVariable())) {
                            BoxedDRTA branchBDRTA = node.getDependents().get(0).getFormulaBoxedDRTA();
                            branchBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should carry forward interpretation and variable assignment from its root.")));
                            throw new TextMessageException(null);
                        }
                        if (!fp.getFormula().getChildren().get(0).equals(branchPacket0.getFormula())) {
                            BoxedDRTA branchBDRTA = node.getDependents().get(0).getFormulaBoxedDRTA();
                            branchBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should contain immediate subformula from branch's root.")));
                            throw new TextMessageException(null);
                        }
                    }
                }


                //binary
                else if (op.getType() == ExpressionType.COND_OP || op.getType() == ExpressionType.BICOND_OP || op.getType() == ExpressionType.CONJ_OP || op.getType() == ExpressionType.DISJ_OP || op.getType() == ExpressionType.NAND_OP || op.getType() == ExpressionType.NOR_OP) {
                    if (dependents.size() != 2) {
                        nodeBDRTA.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires two branches.")));
                        throw new TextMessageException(null);
                    }
                    boolean branch0Empty = dependents.get(0).getFormulaPacket() == null && dependents.get(0).getTermPacket() == null;
                    boolean branch1Empty = dependents.get(1).getFormulaPacket() == null && dependents.get(1).getTermPacket() == null;
                    if (permitEmptyBranches && (branch0Empty && branch1Empty)) {
                        nodeBDRTA.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires at least one non-empty branch.")));
                        throw new TextMessageException(null);
                    }
                    if (!permitEmptyBranches && (branch0Empty || branch1Empty)) {
                        nodeBDRTA.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires two non-empty branches.")));
                        throw new TextMessageException(null);
                    }
                    if (!branch0Empty) {
                        if (dependents.get(0).getFormulaPacket() == null) {
                            nodeBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch(es).")));
                            throw new TextMessageException(null);
                        }

                        FormulaPacket branchPacket0 = dependents.get(0).getFormulaPacket();
                        if (!branchPacket0.getInterpretation().equals(fp.getInterpretation()) || !branchPacket0.getAssignment().equals(fp.getAssignment()) ||
                                !branchPacket0.getInterpretationVariable().equals(fp.getInterpretationVariable()) || !branchPacket0.getAssignmentVariable().equals(fp.getAssignmentVariable())) {
                            BoxedDRTA branchBDRTA = node.getDependents().get(0).getFormulaBoxedDRTA();
                            branchBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should carry forward interpretation and variable assignment from its root.")));
                            throw new TextMessageException(null);
                        }
                        if (!fp.getFormula().getChildren().get(0).equals(branchPacket0.getFormula())) {
                            BoxedDRTA branchBDRTA = node.getDependents().get(0).getFormulaBoxedDRTA();
                            branchBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Top branch should have leftmost immediate subformula from branch's root.")));
                            throw new TextMessageException(null);
                        }
                    }
                    if (!branch1Empty) {
                        if (dependents.get(1).getFormulaPacket() == null) {
                            nodeBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch(es).")));
                            throw new TextMessageException(null);
                        }
                        FormulaPacket branchPacket1 = dependents.get(1).getFormulaPacket();
                        if (!branchPacket1.getInterpretation().equals(fp.getInterpretation()) || !branchPacket1.getAssignment().equals(fp.getAssignment()) ||
                                !branchPacket1.getInterpretationVariable().equals(fp.getInterpretationVariable()) || !branchPacket1.getAssignmentVariable().equals(fp.getAssignmentVariable())) {
                            BoxedDRTA branchBDRTA = node.getDependents().get(1).getFormulaBoxedDRTA();
                            branchBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should carry forward interpretation and variable assignment from its root.")));
                            throw new TextMessageException(null);
                        }
                        if (!fp.getFormula().getChildren().get(1).equals(branchPacket1.getFormula())) {
                            BoxedDRTA branchBDRTA = node.getDependents().get(1).getFormulaBoxedDRTA();
                            branchBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Bottom branch should have rightmost immediate subformula from branch's root.")));
                            throw new TextMessageException(null);
                        }
                    }
                }

                //quantifier
                else if (op.getType() == ExpressionType.UNIVERSAL_OP || op.getType() == ExpressionType.EXISTENTIAL_OP) {
                    if (dependents.size() != fp.getInterpretation().getmUniverse().getUniverse().size()) {
                        nodeBDRTA.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node requires as many branches as there are members of the universe.")));
                        throw new TextMessageException(null);
                    }

                    //empty branches
                    if (!permitEmptyBranches) {
                        for (BranchNode dependent : dependents) {
                            if (dependent.getFormulaPacket() == null && dependent.getTermPacket() == null) {
                                nodeBDRTA.setVTtreeBoxHighlight();
                                htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
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
                            nodeBDRTA.setVTtreeBoxHighlight();
                            htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node must have at least one nonempty branch.")));
                            throw new TextMessageException(null);
                        }
                    }

                    //formula branches
                    for (BranchNode dependent : dependents) {
                        if (!(dependent.getFormulaPacket() == null && dependent.getTermPacket() == null)) {
                            if (dependent.getFormulaPacket() == null) {
                                BoxedDRTA branchBDRTA = dependent.getFormulaBoxedDRTA();
                                branchBDRTA.setVTtreeBoxHighlight();
                                htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Non-atomic formula requires formula branch(es).")));
                                throw new TextMessageException(null);
                            }
                        }
                    }

                    for (BranchNode dependent : dependents) {
                        if (!(dependent.getFormulaPacket() == null && dependent.getTermPacket() == null)) {
                            if (!fp.getFormula().getChildren().get(0).equals(dependent.getFormulaPacket().getFormula())) {
                                BoxedDRTA branchBDRTA = dependent.getFormulaBoxedDRTA();
                                branchBDRTA.setVTtreeBoxHighlight();
                                htExercise.getMainWindow().getMainView().scrollToNode(branchBDRTA.getRTA());
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
                                nodeBDRTA.setVTtreeBoxHighlight();
                                htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
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
                                        dep.getFormulaBoxedDRTA().setVTtreeBoxHighlight();
                                        htExercise.getMainWindow().getMainView().scrollToNode(dep.getFormulaBoxedDRTA().getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Duplicate assignment.")));
                                        throw new TextMessageException(null);
                                    }

                                }
                            }

                            //branch has assignments from root (excepting variable)
                            for (Map.Entry<Term, MBasicObjTerm> entry : fp.getAssignment().entrySet()) {
                                if (!entry.getKey().equals(variable)) {
                                    if (!branchPacket.getAssignment().entrySet().contains(entry)) {
                                        dependent.getFormulaBoxedDRTA().setVTtreeBoxHighlight();
                                        htExercise.getMainWindow().getMainView().scrollToNode(dependent.getFormulaBoxedDRTA().getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch must carry forward all the assignments from its root.")));
                                        throw new TextMessageException(null);
                                    }
                                }
                            }
                            //root has assignments from branch (excepting variable)
                            for (Map.Entry<Term, MBasicObjTerm> entry : branchPacket.getAssignment().entrySet()) {
                                if (!entry.getKey().equals(variable)) {
                                    if (!fp.getAssignment().entrySet().contains(entry)) {
                                        dependent.getFormulaBoxedDRTA().setVTtreeBoxHighlight();
                                        htExercise.getMainWindow().getMainView().scrollToNode(dependent.getFormulaBoxedDRTA().getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch should have just assignments from its root plus an assignment to the quantified variable.")));
                                        throw new TextMessageException(null);
                                    }
                                }
                            }

                            Pair<Term, MBasicObjTerm> lastAssignment = branchPacket.getLastAssignment();
                            if (!fp.getInterpretation().getmUniverse().getUniverse().contains(lastAssignment.getValue())) {
                                dependent.getFormulaBoxedDRTA().setVTtreeBoxHighlight();
                                htExercise.getMainWindow().getMainView().scrollToNode(dependent.getFormulaBoxedDRTA().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Assignment to quantified variable is not a member of the universe.")));
                                throw new TextMessageException(null);
                            }
                        }
                    }
                }


            }
            else {   //atomic sentence
                if (formula instanceof SentenceAtomic) {
                    if (!node.getDependents().isEmpty()) {
                        BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                        nodeBDRTA.setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Sentence letter does not branch.")));
                        throw new TextMessageException(null);
                    }
                }
                List<Expression> children = formula.getChildren();
                if (children.size() != node.getDependents().size()) {
                    BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                    nodeBDRTA.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node should have one branch for each of its terms.")));
                    throw new TextMessageException(null);
                }
                for (int i = 0; i < children.size(); i++) {
                    BranchNode branch = node.getDependents().get(i);
                    if (!((Term) children.get(i)).equals(branch.getTermPacket().getTerm())) {
                        branch.getFormulaBoxedDRTA().setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(branch.getFormulaBoxedDRTA().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch for terms of root (taken in order).")));
                        throw new TextMessageException(null);
                    }
                }
            }
        }

        //term
        else if (node.getTermPacket() != null) {
            Term term = node.getTermPacket().getTerm();
            if (term.getTermType() == TermType.CONSTANT || term.getTermType() == TermType.VARIABLE) {
                if (!node.getDependents().isEmpty()) {
                    BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                    nodeBDRTA.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Variable or constant sentence does not branch.")));
                    throw new TextMessageException(null);
                }
            }
            else {
                List<Expression> children = term.getChildren();
                if (children.size() != node.getDependents().size()) {
                    BoxedDRTA nodeBDRTA = node.getFormulaBoxedDRTA();
                    nodeBDRTA.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(nodeBDRTA.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node should have one branch for each of its immediate subterms.")));
                    throw new TextMessageException(null);
                }
                for (BranchNode branch : node.getDependents()) {
                    if (branch.getTermPacket() == null) {
                        branch.getFormulaBoxedDRTA().setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(branch.getFormulaBoxedDRTA().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No empty branch on term.")));
                        throw new TextMessageException(null);
                    }
                }


                for (int i = 0; i < children.size(); i++) {
                    BranchNode branch = node.getDependents().get(i);
                    if (!((Term) children.get(i)).equals(branch.getTermPacket().getTerm())) {
                        branch.getFormulaBoxedDRTA().setVTtreeBoxHighlight();
                        htExercise.getMainWindow().getMainView().scrollToNode(branch.getFormulaBoxedDRTA().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Branch for immediate subterms of root (taken in order).")));
                        throw new TextMessageException(null);
                    }
                }
            }
        }




        //----------

        for (int i = 0; i < node.getDependents().size(); i++) {
            BranchNode child = node.getDependents().get(i);
            checkForwardTree(child);
        }
        return true;
    }



    //traverses tree forward
    private boolean setPackets(BranchNode node, SemanticPacket lastPacket) throws TextMessageException {
        if (node == null) return true;

        //-----------
        BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        List<Expression> expList = mParseUtilities.parseDoc(doc, objLangName);
        SemanticPacket currentPacket = null;
        node.setFormulaPacket(null);
        node.setTermPacket(null);

        if (expList.size() == 0) {
            if (!permitEmptyBranches) {
                bdrta.setVTtreeBoxHighlight();
                htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Parse Issue", Collections.singletonList(ParseUtilities.newRegularText("Empty field.")));
                throw new TextMessageException(null);
            }
            else {
                return true;
            }
        }
        else if (expList.size() > 1 || (expList.get(0).getType() != ExpressionType.M_INTERP_ASSIGN_FORMULA && expList.get(0).getType() != ExpressionType.TERM)) {
            bdrta.setVTtreeBoxHighlight();
            htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
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
                    bdrta.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Formula assignment on term branch.")));
                    throw new TextMessageException(null);

                }
                if (mSyntacticalFns.setFormulaPacket(bdrta)) {
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
                    bdrta.setVTtreeBoxHighlight();
                    htExercise.getMainWindow().getMainView().scrollToNode(bdrta.getRTA());
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

        for (int i = 0; i < node.getDependents().size(); i++) {
            BranchNode child = node.getDependents().get(i);
            setPackets(child, currentPacket);
        }
        return true;
    }

    private void resetHighlights(BranchNode node) {
        if (node == null) return;

        BoxedDRTA bdrta = node.getFormulaBoxedDRTA();
        bdrta.resetVTtreeBoxHighlight();
        BoxedDRTA conn = node.getConnectorBoxedDRTA();
        conn.resetVTtreeBoxHighlight();
        BoxedDRTA ann = node.getAnnotationField();
        ann.resetAnnHighlight();

        for (int i = 0; i < node.getDependents().size(); i++) {
            BranchNode child = node.getDependents().get(i);
            resetHighlights(child);
        }
    }


    private void traverseTreeForward(BranchNode node) {
        if (node == null) return;

            if (node.getFormulaPacket() != null) {System.out.println(node.getFormulaPacket().toString());}
            if (node.getTermPacket() != null) {System.out.println(node.getTermPacket().toString());}

        //RichTextArea nodeRTA = node.getFormulaBoxedDRTA().getRTA();
        //nodeRTA.getActionFactory().saveNow().execute(new ActionEvent());
        //System.out.println(nodeRTA.getDocument().getText());

        for (int i = 0; i < node.getDependents().size(); i++) {
            BranchNode child = node.getDependents().get(i);
            traverseTreeForward(child);
        }
    }

    private void traverseTreeBackward(BranchNode node) {
        if (node == null) return;

        for (int i = 0; i < node.getDependents().size(); i++) {
            BranchNode child = node.getDependents().get(i);
            traverseTreeBackward(child);
        }

        RichTextArea nodeRTA = node.getFormulaBoxedDRTA().getRTA();
        nodeRTA.getActionFactory().saveNow().execute(new ActionEvent());
        System.out.println(nodeRTA.getDocument().getText());
    }




    private void setRightControlBox() {
        htView.getCheckButton().setOnAction(e -> {
           checkTrees();
        });

        htView.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());

        htView.getStaticHelpButton().setOnAction(e -> {
            Stage helpStage = htView.getStaticHelpStage();
            if (helpStage != null && helpStage.isShowing()) helpStage.close();
            else htView.showStaticHelp(htModel.getCheckSetup().getStaticHelpDoc());
        });

        htView.getPermitEmptyBranchesCheck().setDisable(!checkSetup.isEmptyBranchesOK());
        htView.getPermitEmptyBranchesCheck().setSelected(checkSetup.isEmptyBranchesSelected());

    }



    private void updateCheckCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !htExercise.getMainWindow().isInstructorFunctions()) {
            htView.getCheckButton().setDisable(true);

        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        htView.getCheckTriesLabel().setText(checkString);
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
