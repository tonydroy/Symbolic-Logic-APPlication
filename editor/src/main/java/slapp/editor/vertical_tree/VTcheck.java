package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;
import slapp.editor.vertical_tree.drag_drop.*;
import slapp.editor.vertical_tree.object_models.ObjectControlType;

import javax.sound.midi.Sequence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VTcheck {
    private VerticalTreeExercise vtExercise;
    private VerticalTreeView vtView;
    private VerticalTreeModel vtModel;
    private VTcheckSetup checkSetup;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    List<Node> checkNodeList;
    private List<List<TreeNode>> formulaTree;
    private List<List<String>> lineJustifications;
    private double equalsEpsilon = 10.0;
    private boolean isCheckJustification = true;
    private List<TreeNode> connectedNodes;
    private List<Formula> quantifiedSubformulas;

    private String objLangName;
    private Language objLang;
    private boolean langAllowDP;
    private Expression targetExpression = null;
    private VTCheckType checkType;

    private boolean hasTerms;
    private boolean hasFormulas;
    private double minTermY;
    private double maxTermY;
    private double minFormulaY;
    private double maxFormulaY;
    private TreeNode rootNode;

    private boolean checkBracket;
    private boolean checkDashedLine;
    private boolean checkCircle;
    private boolean checkStar;
    private boolean checkMapping;
    private boolean checkBoxing;
    private boolean checkUnderline;

    private boolean silent;
    VerticalTreeExercise auxExerA;


    VTcheck(VerticalTreeExercise vtExercise) {


        this.vtExercise = vtExercise;
        this.vtView = vtExercise.getExerciseView();
        this.vtModel = vtExercise.getExerciseModel();
        setRightControlBox();

        checkSetup = vtExercise.getExerciseModel().getCheckSetup();
        if (checkSetup == null) { checkSetup = new VTcheckSetup(); }
        objLangName = checkSetup.getObjLangName();
        objLang = Languages.getLanguage(objLangName);
        setLangAllowDP(objLang.isAllowDroppedBrackets());

        Document targetDoc = checkSetup.getFormulaTarget();
        if (targetDoc != null) {
            List<Expression> targetParseList = ParseUtilities.parseDoc(targetDoc, objLang);
            Expression targetExpression = targetParseList.get(0);
        }
        isCheckJustification = checkSetup.isCheckJustifications();
        checkType = checkSetup.getCheckType();



        checkBracket = vtModel.getDragIconList().contains(DragIconType.BRACKET);
        checkDashedLine = vtModel.getDragIconList().contains(DragIconType.DASHED_LINE);
        checkCircle = vtModel.getObjectControlList().contains(ObjectControlType.CIRCLE);
        checkStar = vtModel.getObjectControlList().contains(ObjectControlType.STAR);
        checkMapping = vtModel.getObjectControlList().contains(ObjectControlType.MAPPING);
        checkBoxing = vtModel.getObjectControlList().contains(ObjectControlType.FORMULA_BOX);
        checkUnderline = vtModel.getObjectControlList().contains(ObjectControlType.UNDERLINE);



        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            vtView.setCheckColor(Color.LAWNGREEN);
            vtView.setCheckElementsColor(Color.GREEN);
            vtView.setCheckMessage("Full Tree");
        }
        else {
            vtView.setCheckColor(Color.ORCHID);
            vtView.setCheckElementsColor(Color.PURPLE);
            vtView.setCheckMessage("Nodes: Good");
        }
        if (checkSuccess) vtView.activateBigCheck();
        else vtView.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();
    }



    private void setRightControlBox() {

        vtView.getCheckButton().setOnAction(e -> {
            checkTree();
        });

//        vtView.getCheckProgButton().setOnAction(e -> {
//            System.out.println("check progress");
//        });

        vtView.getstaticHelpButton().setDisable(!vtModel.getCheckSetup().isStaticHelpButton());

        vtView.getstaticHelpButton().setOnAction(e -> {
            System.out.println("static help");
        });
    }

    private void setChecksCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !vtExercise.getMainWindow().isInstructorFunctions()) {
            vtView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        vtView.getCheckTriesLabel().setText(checkString);

    }

    private boolean checkTree() {

        switch (checkType) {
            case FORMULA:
                this.silent = false;
                //check justifications from VTcheckSetup
                return checkFormulaTree(true);

            case UNABB_AUX:
                this.silent = true;
                this.isCheckJustification = false;
                return checkFormulaTree(false);

            case UNABB_EXPAND:
                this.silent = false;
                //check justifications from VTcheckSetup
                return checkFormulaTree(false);

            case UNABB:
                this.silent = false;
                //check justifications from VTcheckSetup
                return checkUnabbTree(false);

            case MAPPING:
                return false;
        }
        return false;
    }

    private boolean checkUnabbTree(boolean checkMarkup) {

        checkSuccess = false;
        vtView.deactivateBigCheck();
        if (!vtExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        formulaTree = new ArrayList<>();
        lineJustifications = new ArrayList<>();

        checkNodeList = vtView.getRootLayout().getMainPane().getChildren();
        if (checkNodeList.isEmpty()) {
            if (!silent) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate.")));
            }
            return false;
        }

        List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
        if (sortedFormulaBoxes.isEmpty()) {
            if (!silent) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
            }
            return false;
        }

        auxExerA = ((VerticalTreeExercise) vtExercise.getAuxExerciseA());  //what about other versions of the Vertical Tree??
        if (auxExerA == null) {
            if (!silent) {
                EditorAlerts.showSimpleTxtListAlert("Auxiliary Tree", Collections.singletonList(ParseUtilities.newRegularText("Missing auxiliary tree.  Cannot check unabbreviation.")));
            }
            return false;
        }

        VTcheck vtCheckA = auxExerA.getVtCheck();
        vtCheckA.setCheckType(VTCheckType.UNABB_AUX);
        if (!vtCheckA.checkTree()) {
            if (!silent) {
                EditorAlerts.showSimpleTxtListAlert("Auxiliary Tree", Collections.singletonList(ParseUtilities.newRegularText("Unabbreviation check requires (successfully) completed auxiliary tree.")));
            }
            return false;
        }

        if (!populateTreeLists(sortedFormulaBoxes)) return false;
        if (!setParentsAndChildren()) return false;
        if (!checkTrueTree()) return false;
        if (!checkFormulaContents()) return false;
        List<List<TreeNode>> auxFormulaTree = auxExerA.getVtCheck().getFormulaTree();

        try {
            checkParallelTrees(formulaTree.get(formulaTree.size() - 1).get(0), auxFormulaTree.get(auxFormulaTree.size() - 1).get(0) );
        }
        catch (TreeNodeException e) {
            if (!silent) {
                e.getTreeNode().getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                EditorAlerts.showSimpleTxtListAlert("Trees Structure", e.getMessageList());
                e.getTreeNode().getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
            }
            return false;
        }



        checkSuccess = true;
        vtView.activateBigCheck();
        return true;

    }


    private void checkParallelTrees(TreeNode mainNode, TreeNode auxNode) throws TreeNodeException {
        mainNode.setMate(auxNode);

        if (mainNode.getParents().size() != auxNode.getParents().size()) {
            throw new TreeNodeException(mainNode, Collections.singletonList(ParseUtilities.newRegularText("Working up from the bottom, this and and the corresponding node of the auxiliary tree not linked to the same number of (parent) nodes.  Trees not parallel.")));
        }
        else {
            for (int i = 0; i < mainNode.getParents().size(); i++) {
                checkParallelTrees(mainNode.getParents().get(i), auxNode.getParents().get(i));
            }
        }
    }

    private boolean checkFormulaTree(boolean checkMarkup) {
        checkSuccess = false;
        vtView.deactivateBigCheck();
        if (!vtExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }


        formulaTree = new ArrayList<>();
        lineJustifications = new ArrayList<>();

        checkNodeList = vtView.getRootLayout().getMainPane().getChildren();
        if (checkNodeList.isEmpty()) {
            if (!silent) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate.")));
            }
            return false;
        }

        List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
        if (sortedFormulaBoxes.isEmpty()) {
            if (!silent) {
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
            }
            return false;
        }

        if (!populateTreeLists(sortedFormulaBoxes)) return false;
        if (!setParentsAndChildren()) return false;
        if (!checkTrueTree()) return false;
        if (!checkContents()) return false;
        if (checkMarkup && !checkMarkup()) return false;


        checkSuccess = true;
        vtView.activateBigCheck();

        return true;
    }

    private boolean checkFormulaContents() {

        rootNode = formulaTree.get(formulaTree.size() - 1).get(0);

        for (int j = 0; j < formulaTree.size(); j++) {
            List<TreeNode> treeList = formulaTree.get(j);
            for (int i = 0; i < treeList.size(); i++) {
                TreeNode treeNode = treeList.get(i);

                RichTextArea rta = treeNode.getMainTreeBox().getFormulaBox().getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document doc = rta.getDocument();
                objLang.setAllowDroppedBrackets(false);
                List<Expression> parseExp = ParseUtilities.parseDoc(doc, objLang);

                if (parseExp.isEmpty()) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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
                            EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                            treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else {
                        if (treeNode != rootNode || !langAllowDP) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
            }
        }
        return true;
    }



    private boolean checkMarkup() {

        if (hasFormulas) {

            List<TreeNode> immediateSubs = rootNode.getParents();

            //check root for circle immediate subs boxed
            TreeFormulaBox rootBox = rootNode.getMainTreeBox();
            RichTextArea rootRTA = rootBox.getFormulaBox().getRTA();
            rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document rootDoc = rootRTA.getDocument();
            objLang.setAllowDroppedBrackets(langAllowDP);
            List<Expression> rootParseList = ParseUtilities.parseDoc(rootDoc, objLang);
            Expression rootNodeExp = rootParseList.get(0);

            //check for circle

            if (rootNodeExp instanceof Formula && !((Formula) rootNodeExp).isAtomic()) {

                if (checkCircle) {
                    List<Expression> circleExpressions = getCircleText(rootNode.getMainTreeBox());
                    if (circleExpressions.size() == 0) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No circled text.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else if (circleExpressions.size() > 1) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not a grammatical expression.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }
                    Expression circleExp = circleExpressions.get(0);
                    if (!(circleExp instanceof Operator)) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not an operator.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }

                    if (!(((Formula) rootNodeExp).getMainOperator().equals(circleExp)) || ((Operator) circleExp).getPosition() != ((Formula) rootNodeExp).getMainOperator().getPosition()) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not the main operator of the root node.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }
                }

                //check for boxes
                if (checkBoxing) {
                    for (TreeNode immediateSub : immediateSubs) {
                        if (!immediateSub.getMainTreeBox().isBoxed()) {
                            if (!silent) {
                                rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Immediate subformula not boxed.")));
                                rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    }
                }
            }

            for (int j = 0; j < formulaTree.size(); j++) {
                List<TreeNode> treeList = formulaTree.get(j);
                for (int i = 0; i < treeList.size(); i++) {
                    TreeNode treeNode = treeList.get(i);
                    TreeFormulaBox  treeFormulaBox = treeNode.getMainTreeBox();

                    RichTextArea rta = treeFormulaBox.getFormulaBox().getRTA();
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    Document doc = rta.getDocument();
                    objLang.setAllowDroppedBrackets(langAllowDP);
                    List<Expression> parseList = ParseUtilities.parseDoc(doc, objLang);
                    Expression treeNodeExp = parseList.get(0);

                    //check no underline on term
                    if (treeNodeExp instanceof Term && !treeFormulaBox.getUlineIndexes().isEmpty()) {
                        if (!silent) {
                            treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No underline on terms.")));
                            treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }

                    if (treeNodeExp instanceof Formula) {
                        Formula treeNodeFormula = (Formula) treeNodeExp;

                        //check star
                        if (checkStar) {
                            if (treeNodeFormula.isAtomic() && !treeFormulaBox.isStarred()) {
                                if (!silent) {
                                    treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Atomic not marked with star.")));
                                    treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                            if (!treeNodeFormula.isAtomic() && treeFormulaBox.isStarred()) {
                                if (!silent) {
                                    treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Star applied to non-atomic formula.")));
                                    treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }

                        //check extra circle
                        if (checkCircle) {
                            if (treeNode != rootNode && treeFormulaBox.isCircled()) {
                                if (!silent) {
                                    treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circle applied node other than root.")));
                                    treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }

                        //check extra boxes
                        if (checkBoxing) {
                            if (treeFormulaBox.isBoxed() && !immediateSubs.contains(treeNode)) {
                                if (!silent) {
                                    treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Box applied to node other than an immediate subformula.")));
                                    treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }

                        //check underlines
                        if (checkUnderline) {
                            if (!treeFormulaBox.isUlineInclusion()) {
                                if (!silent) {
                                    treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline not completely \"included\" in ones below.")));
                                    treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }


                            List<List<Expression>> underlineTexts = getUnderlineTexts(treeFormulaBox);
                            List<Formula> underlineFormulas = new ArrayList<>();

                            //get quantified expressions with underline: underlineFormulas
                            for (List<Expression> underlineExpressions : underlineTexts) {
                                if (underlineExpressions.size() > 1) {
                                    if (!silent) {
                                        treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                        EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline text is not a grammatical expression.")));
                                        treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                                Expression underlineExp = underlineExpressions.get(0);
                                if (!(underlineExp instanceof Formula)) {
                                    if (!silent) {
                                        treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                        EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline text is not a (sub-)formula.")));
                                        treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                                else {
                                    Formula underlineFormula = (Formula) underlineExp;
                                    if (!(underlineFormula.getMainOperator() instanceof UniversalOp) && !(underlineFormula.getMainOperator() instanceof ExistentialOp))   {
                                        if (!silent) {
                                            treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Main operator of underline text not a quantifier.")));
                                            treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                        }
                                        return false;
                                    }
                                    underlineFormulas.add(underlineFormula);
                                }
                            }

                            //check hava all quantified subformulas.
                            quantifiedSubformulas = new ArrayList<>();
                            populateQuantifiedSubformulas(treeNode);

                            boolean[] found = new boolean[quantifiedSubformulas.size()];

                       //     System.out.println("q subs: " + quantifiedSubformulas + " u subs: " + underlineFormulas);

                            for (int a = 0; a < quantifiedSubformulas.size(); a++) {
                                if (!found[a]) {
                                    Formula subformula = quantifiedSubformulas.get(a);

                                    for (int m = 0; m < underlineFormulas.size(); m++) {
                                        Formula ulineFormula = underlineFormulas.get(m);
                                        if (subformula.equals(ulineFormula)) {
                                            found[a] = true;
                                            underlineFormulas.remove(m);
                                            break;
                                        }
                                    }
                                }
                            }
                            boolean good = true;
                            for (int b = 0; b < quantifiedSubformulas.size(); b++) {
                                if (found[b] == false) {
                                    good = false;
                                    break;
                                }
                            }
                            if (!good) {
                                if (!silent) {
                                    treeFormulaBox.getFormulaBox().setVTtreeBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Not every quantified subformula has underline.")));
                                    treeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }
                    }
                }
            }

            if (hasFormulas) {
                List<DashedLine> dashedLines = new ArrayList<>();
                List<VerticalBracket> verticalBrackets = new ArrayList<>();

                for (Node node : checkNodeList) {
                    if (node instanceof DashedLine) dashedLines.add((DashedLine) node);
                    if (node instanceof VerticalBracket) verticalBrackets.add((VerticalBracket) node);
                }



                //dashed line
                if (checkDashedLine && checkType == VTCheckType.FORMULA) {
                    if (!hasTerms) {
                        if (!dashedLines.isEmpty()) {
                            if (!silent) {
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No need for dashed term/formula separator in tree without terms.")));
                            }
                            return false;
                        }
                    } else {
                        if (dashedLines.isEmpty()) {
                            if (!silent) {
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Missing dashed term/formula separator line.")));
                            }
                            return false;
                        }
                        if (dashedLines.size() > 1) {
                            if (!silent) {
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Just one dashed term/formula separator line.")));
                            }
                            return false;
                        }
                        DashedLine dashedLine = dashedLines.get(0);
                        if (dashedLine.getLayoutY() < maxTermY || dashedLine.getLayoutY() > minFormulaY) {
                            if (!silent) {
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Dashed line does not separate terms from formulas.")));
                            }
                            return false;
                        }

                    }
                }


                //vertical bracket
                if (checkBracket && checkType == VTCheckType.FORMULA) {
                    if (verticalBrackets.isEmpty()) {
                        if (!silent) {
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Missing vertical bracket on subformulas.")));
                        }
                        return false;
                    }
                    if (verticalBrackets.size() > 1) {
                        if (!silent) {
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Just one vertical bracket for subformulas.")));
                        }
                        return false;
                    }
                    VerticalBracket bracket = verticalBrackets.get(0);
                    if (hasTerms && (bracket.getLayoutY() + 10) < maxTermY) {
                        if (!silent) {
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Vertical bracket includes more than just subformulas.")));
                        }
                        return false;
                    }
                    if ((bracket.getLayoutY() + 10) > minFormulaY || bracket.getBoundsInParent().getMaxY() < maxFormulaY) {
                        if (!silent) {
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Vertical bracket does not include all subformulas.")));
                        }
                        return false;
                    }
                }

            }
        }
        return true;
    }

    /*
    Check nodes for grammatical term or formula.  Check parents/children and justification.  Check target.
     */
    private boolean checkContents() {
        hasTerms = false;
        hasFormulas = false;
        minTermY = -1;
        maxTermY = -1;
        minFormulaY = -1;
        maxFormulaY = -1;
        rootNode = formulaTree.get(formulaTree.size() - 1).get(0);

        for (int j = 0; j < formulaTree.size(); j++) {
            List<TreeNode> treeList = formulaTree.get(j);
            for (int i = 0; i < treeList.size(); i++) {
                TreeNode treeNode = treeList.get(i);

                RichTextArea rta = treeNode.getMainTreeBox().getFormulaBox().getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document doc = rta.getDocument();
                objLang.setAllowDroppedBrackets(false);
                List<Expression> parseExp = ParseUtilities.parseDoc(doc, objLang);

                if (parseExp.isEmpty()) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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
                            EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                            treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else {
             //           System.out.println("allow dp: " + langAllowDP);
                        if (treeNode != rootNode || !langAllowDP) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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
                        if (treeNode.getParents().size() != 0) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is variable and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (isCheckJustification && !lineJustifications.get(j).get(i).equals("TR(v)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Variable node has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (termType == TermType.CONSTANT) {
                        if (treeNode.getParents().size() != 0) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is constant and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (isCheckJustification && !lineJustifications.get(j).get(i).equals("TR(c)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Constant node has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (termType == TermType.COMPLEX) {
                        FunctionSymbol fnSym = term.getMainFnSymbol();
                        int places = fnSym.getPlaces();
                        String placesString = String.valueOf(places);

                        if (treeNode.getParents().size() != term.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText(placesString + "-place function symbol must be linked to " + placesString + " term(s).")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else {
                            for (int k = 0; k < term.getChildren().size(); k++) {
                                Expression subTerm = term.getChildren().get(k);
                                RichTextArea termRTA = treeNode.getParents().get(k).getMainTreeBox().getFormulaBox().getRTA();
                                termRTA.getActionFactory().saveNow().execute(new ActionEvent());
                                Document termDoc = termRTA.getDocument();
                                objLang.setAllowDroppedBrackets(false);
                                Expression linkTerm = ParseUtilities.parseDoc(termDoc, objLang).get(0);
                                if (!subTerm.equals(linkTerm)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                        List<Text> textList = new ArrayList<>();
                                        textList.add(ParseUtilities.newRegularText("Subterm "));
                                        textList.addAll(subTerm.toTextList());
                                        textList.add(ParseUtilities.newRegularText(" improperly linked."));

                                        EditorAlerts.showSimpleTxtListAlert("TreeContent", textList);
                                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            }
                        }
                        if (isCheckJustification && !lineJustifications.get(j).get(i).equals("TR(f)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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
                        if (treeNode.getParents().size() != 0) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is sentence letter and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (isCheckJustification && !lineJustifications.get(j).get(i).equals("FR(s)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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

                        if (treeNode.getParents().size() != formula.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText(placesString + "-place relation symbol must be linked to " + placesString + " term(s).")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else {
                            for (int k = 0; k < formula.getChildren().size(); k++) {
                                Expression subTerm = formula.getChildren().get(k);
                                RichTextArea termRTA = treeNode.getParents().get(k).getMainTreeBox().getFormulaBox().getRTA();
                                termRTA.getActionFactory().saveNow().execute(new ActionEvent());
                                Document termDoc = termRTA.getDocument();
                                objLang.setAllowDroppedBrackets(false);
                                Expression linkTerm = ParseUtilities.parseDoc(termDoc, objLang).get(0);
                                if (!subTerm.equals(linkTerm)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                        List<Text> textList = new ArrayList<>();
                                        textList.add(ParseUtilities.newRegularText("Subterm "));
                                        textList.addAll(subTerm.toTextList());
                                        textList.add(ParseUtilities.newRegularText(" improperly linked."));
                                        EditorAlerts.showSimpleTxtListAlert("TreeContent", textList);
                                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            }
                        }
                        if (isCheckJustification && !lineJustifications.get(j).get(i).equals("FR(r)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Atomic formula has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else {
                        Operator mainOp = formula.getMainOperator();
                        if (treeNode.getParents().size() != formula.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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
                                RichTextArea termRTA = treeNode.getParents().get(k).getMainTreeBox().getFormulaBox().getRTA();
                                termRTA.getActionFactory().saveNow().execute(new ActionEvent());
                                Document formDoc = termRTA.getDocument();
                                objLang.setAllowDroppedBrackets(false);
                                Expression linkForm = ParseUtilities.parseDoc(formDoc, objLang).get(0);
                                if (!subForm.equals(linkForm)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                                        List<Text> textList = new ArrayList<>();
                                        textList.add(ParseUtilities.newRegularText("Subformula "));
                                        textList.addAll(subForm.toTextList());
                                        textList.add(ParseUtilities.newRegularText(" improperly linked."));
                                        EditorAlerts.showSimpleTxtListAlert("TreeContent", textList);
                                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            }
                        }
                        if (isCheckJustification) {
                            String justificationString ="";
                            if (mainOp instanceof NegationOp || mainOp instanceof ConditionalOp || mainOp instanceof UniversalOp)
                                justificationString = "FR(" + mainOp.getMainSymbol().toString() + ")";
                            else if (mainOp instanceof ConjunctionOp || mainOp instanceof DisjunctionOp || mainOp instanceof BiconditionalOp || mainOp instanceof ExistentialOp)
                                justificationString = "FR\u2032(" + mainOp.getMainSymbol().toString() + ")";

                            if (!lineJustifications.get(j).get(i).equals(justificationString)) {
                                if (!silent) {
                                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
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

        //this needs testing once we have with create to set target
        if (targetExpression != null) {
            RichTextArea rootRTA = rootNode.getMainTreeBox().getFormulaBox().getRTA();
            rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document rootDoc = rootRTA.getDocument();
            objLang.setAllowDroppedBrackets(langAllowDP);
            Expression rootExp = ParseUtilities.parseDoc(rootDoc, objLang).get(0);
            if (!rootExp.equals(targetExpression)) {
                if (!silent) {
                    rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Good tree, but this is not the assigned target expression.")));
                    rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                }
                return false;
            }
        }

        return true;
    }




    /*
    Check that tree has one root node, that nodes have at most one child, that all nodes are connected to the root.
     */
    private boolean checkTrueTree() {

        //check one root
        List<TreeNode> bottomRow = formulaTree.get(formulaTree.size() - 1);
        if (bottomRow.size() > 1 ) {
            if (!silent) {
                for (TreeNode treeNode : bottomRow) {
                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                }
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A tree can have only one bottom (root) node.")));
                for (TreeNode treeNode : bottomRow) {
                    treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                }
            }
            return false;
        }

        //check nodes have at most one child
        for (List<TreeNode> row : formulaTree) {
            for (TreeNode treeNode : row) {
                List<TreeNode> children = treeNode.getChildren();
                if (children.size() > 1) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A node can be linked to at most one child (lower) node.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
            }
        }

        //check all nodes connected to root
        connectedNodes = new ArrayList<>();
        populateConnectedNodes(formulaTree.get(formulaTree.size() - 1).get(0));
        for (List<TreeNode> row : formulaTree) {
            for (TreeNode treeNode : row) {
                if (!connectedNodes.contains(treeNode)) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node is not an an ancestor connected all the way to the root node.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
            }
        }

        return true;
    }

    private void populateConnectedNodes(TreeNode node) {
        connectedNodes.add(node);
        for (TreeNode parent : node.getParents()) {
            populateConnectedNodes(parent);
        }
    }

    private void populateQuantifiedSubformulas(TreeNode node) {
        TreeFormulaBox nodeBox = node.getMainTreeBox();
        RichTextArea rootRTA = nodeBox.getFormulaBox().getRTA();
        rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document nodeDoc = rootRTA.getDocument();
        objLang.setAllowDroppedBrackets(langAllowDP);
        List<Expression> nodeParseList = ParseUtilities.parseDoc(nodeDoc, objLang);
        Expression nodeExp = nodeParseList.get(0);

        if (nodeExp instanceof Formula) {
            Formula formula = (Formula) nodeExp;
            if (formula.getMainOperator() instanceof UniversalOp || formula.getMainOperator() instanceof ExistentialOp) quantifiedSubformulas.add(formula);
        }

        for (TreeNode parent : node.getParents()) {
            populateQuantifiedSubformulas(parent);
        }
    }


    /*
    Set parents and children for tree boxes in the formula tree list.  Sort parents by xPos.
     */
    private boolean setParentsAndChildren() {
        for (List<TreeNode> rowList : formulaTree) {
            for (TreeNode node : rowList) {
                node.setParents(new ArrayList<>());
                node.setChildren(new ArrayList<>());
                TreeFormulaBox mainTBox = node.getMainTreeBox();
                String formulaID = mainTBox.getId();
                String relativeID = "";
                for (Node link : checkNodeList ) {
                    relativeID = "";
                    if (link instanceof ClickableNodeLink) {
                        ClickableNodeLink nodeLink = (ClickableNodeLink) link;
                        if (nodeLink.getSourceId().equals(formulaID)) {
                            relativeID = nodeLink.getTargetId();
                        }
                        if (nodeLink.getTargetId().equals(formulaID)) {
                            relativeID = nodeLink.getSourceId();
                        }
                    }
                    if (!relativeID.equals("")) {
                        for (List<TreeNode> rList : formulaTree) {
                            for (TreeNode tNode : rList) {
                                TreeFormulaBox tBox = tNode.getMainTreeBox();
                                if (tBox.getId().equals(relativeID)) {
                                    if (tBox.getLayoutY() - mainTBox.getLayoutY() > equalsEpsilon)
                                        node.getChildren().add(tNode);
                                    else if (mainTBox.getLayoutY() - tBox.getLayoutY() > equalsEpsilon)
                                        node.getParents().add(tNode);
                                    else {
                                        if (!silent) {
                                            mainTBox.getFormulaBox().setVTtreeBoxHighlight();
                                            tBox.getFormulaBox().setVTtreeBoxHighlight();
                                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Cannot have connected nodes in same row.")));
                                            mainTBox.getFormulaBox().resetVTtreeBoxHighlight();
                                            tBox.getFormulaBox().resetVTtreeBoxHighlight();
                                        }
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (List<TreeNode> rowList : formulaTree) {
            for (TreeNode node : rowList) {
                node.getParents().sort(Comparator.comparingDouble(parent -> parent.getMainTreeBox().getLayoutX()));
            }
        }

        return true;
    }

    /*
    Separate members of sorted formulas into the formulaTree and justificationList by row (sorting formula row by xPos).
    Check that each row has a single justification field / note missing primes / check that justifications are allowable.
    Add list of justifications to lineJustifications.
     */
    private boolean populateTreeLists(List<Node> sortedFormulaBoxes) {
        int j = 0;
        List<TreeNode> treeList = new ArrayList<>();
        List<MapFormulaBox> justificationList = new ArrayList<>();
        lineJustifications = new ArrayList<>();
        for (int i = 0; i < sortedFormulaBoxes.size(); i++) {
            if (sortedFormulaBoxes.get(i) instanceof TreeFormulaBox) treeList.add(new TreeNode((TreeFormulaBox) sortedFormulaBoxes.get(i)));
            if (sortedFormulaBoxes.get(i) instanceof MapFormulaBox) {justificationList.add((MapFormulaBox) sortedFormulaBoxes.get(i));}

            if ((i + 1 < sortedFormulaBoxes.size() && Math.abs(sortedFormulaBoxes.get(i + 1).getLayoutY() - sortedFormulaBoxes.get(i).getLayoutY()) > equalsEpsilon) || i + 1 >= sortedFormulaBoxes.size()) {

                treeList.sort(Comparator.comparingDouble((treeBox -> treeBox.getMainTreeBox().getLayoutX())));
                formulaTree.add(treeList);

                if (isCheckJustification)  {
                    if (justificationList.size() == 0) {
                        if (!silent) {
                            for (TreeNode treeNode : treeList) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                            }
                            List<Text> messageList = new ArrayList<>();
                            messageList.add(ParseUtilities.newRegularText("Missing justification on row."));
                            if (checkType == VTCheckType.UNABB) messageList.add(ParseUtilities.newRegularText("\n\nIf formula results from (nothing or) linked nodes by TR or FR cite that rule.  Otherwise cite the AB rule (including an AB(i) to convert from abbreviating infix forms)."));
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", messageList);
                            for (TreeNode treeNode : treeList) {
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                        }
                        return false;
                    }
                    else if (justificationList.size() > 1) {
                        if (!silent) {
                            for (MapFormulaBox justificationBox : justificationList) {
                                justificationBox.getFormulaBox().setVTmapBoxHighlight();
                            }
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Only one justification field for each row.")));
                            for (MapFormulaBox justificationBox : justificationList) {
                                justificationBox.getFormulaBox().resetVTmapBoxHighlight();
                            }
                        }
                        return false;
                    }
                    else {
                        BoxedDRTA bdrta = justificationList.get(0).getFormulaBox();
                        RichTextArea rta = bdrta.getRTA();
                        rta.getActionFactory().saveNow().execute(new ActionEvent());
                        String boxText = rta.getDocument().getText();

                        if (checkType != VTCheckType.UNABB) {
                            List<String> missingPrimes = getMissingPrime(boxText);
                            if (missingPrimes.size() > 0) {
                                if (!silent) {
                                    bdrta.setVTmapBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Do not forget prime on justification for abbreviating rules, as FR\u2032(\u2227).")));
                                    bdrta.resetVTmapBoxHighlight();
                                }
                                return false;
                            }

                            List<String> justifications = getJustificationStrings(boxText);
                            if (justifications.size() != treeList.size()) {
                                if (!silent) {
                                    bdrta.setVTmapBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one (allowable) justification, as FR(\u223c), for each member of row.")));
                                    bdrta.resetVTmapBoxHighlight();
                                }
                                return false;
                            }
                            lineJustifications.add(justifications);
                        }
                        else {
                            List<String> justifications = getAbbJustificationStrings(boxText);
                            if (justifications.size() != treeList.size()) {
                                if (!silent) {
                                    bdrta.setVTmapBoxHighlight();
                                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one (allowable) justification for each member of row.\n\nIf formula results from (nothing or) linked nodes by TR or FR cite that rule.  Otherwise cite the AB rule (including an AB(i) to convert from abbreviating infix forms).")));
                                    bdrta.resetVTmapBoxHighlight();
                                }
                                return false;
                            }
                            lineJustifications.add(justifications);
                        }
                    }
                }
                j++;
                treeList = new ArrayList<>();
                justificationList = new ArrayList<>();
            }
        }
        return true;
    }

    List<String> getMissingPrime(String inputString) {
        String regexTarget = "FR\\(\\u2227\\)|FR\\(\\u2228\\)|FR\\(\\u2194\\)|FR\\(\\u2203\\)";
        Pattern pattern = Pattern.compile(regexTarget);
        Matcher matcher = pattern.matcher(inputString);

        List<String> justifications = new ArrayList<>();
        while (matcher.find()) {
            justifications.add(matcher.group());
        }
        return justifications;
    }

    List<String> getJustificationStrings(String inputString) {

        String regexTarget = "FR\\(s\\)|FR\\(r\\)|FR\\(\\u223c\\)|FR\\(\\u2192\\)|FR\\u2032\\(\\u2227\\)|FR\\u2032\\(\\u2228\\)|FR\\u2032\\(\\u2194\\)|FR\\(\\u2200\\)|FR\\u2032\\(\\u2203\\)|TR\\(v\\)|TR\\(c\\)|TR\\(f\\)";
        Pattern pattern = Pattern.compile(regexTarget);
        Matcher matcher = pattern.matcher(inputString);

        List<String> justifications = new ArrayList<>();
        while (matcher.find()) {
            justifications.add(matcher.group());
        }
        return justifications;
    }

    List<String> getAbbJustificationStrings(String inputString) {
        String regexTarget = "AB\\(i\\)|AB\\(\\u2228\\)|AB\\(\\u2227\\)|AB\\(\\u2194\\)|AB\\(\\u2203\\)|FR\\(s\\)|FR\\(r\\)|FR\\(\\u223c\\)|FR\\(\\u2192\\)|FR\\(\\u2200\\)|TR\\(v\\)|TR\\(c\\)|TR\\(f\\)";
        Pattern pattern = Pattern.compile(regexTarget);
        Matcher matcher = pattern.matcher(inputString);

        List<String> justifications = new ArrayList<>();
        while (matcher.find()) {
            justifications.add(matcher.group());
        }
        return justifications;
    }


    private List<Node> getSortedFormulaBoxes() {
        List<Node> sortedFormulaBoxes = new ArrayList<>();
        for (Node node : checkNodeList) {
            if (node instanceof TreeFormulaBox || node instanceof MapFormulaBox) sortedFormulaBoxes.add(node);
        }
        sortedFormulaBoxes.sort(Comparator.comparingDouble(node -> node.getLayoutY()));
        return sortedFormulaBoxes;
    }

    private List<Expression> getCircleText(TreeFormulaBox treeFormulaBox) {
        RichTextArea rta = treeFormulaBox.getFormulaBox().getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        String text = doc.getText();

        int[] indexes = treeFormulaBox.getCircleIndexes();
        int startIndex = Math.min(indexes[0], indexes[1]);
        if (0 < startIndex && startIndex + 1 < text.length() && text.codePointCount(startIndex -1, startIndex +1) == 1) startIndex++;

        int endIndex = Math.max(indexes[0], indexes[1]);
        if (0 < endIndex && endIndex + 1 < text.length() && text.codePointCount(endIndex -1, endIndex +1) == 1) endIndex++;

        return ParseUtilities.parseSubDoc(doc, startIndex, endIndex, checkSetup.getObjLangName());
    }

    private List<List<Expression>> getUnderlineTexts(TreeFormulaBox treeFormulaBox) {
        List<List<Expression>> underlineTexts = new ArrayList<>();


        RichTextArea rta = treeFormulaBox.getFormulaBox().getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        String text = doc.getText();

        for (Integer[] index : treeFormulaBox.getUlineIndexes()) {
            int startIndex = index[0];
            int endIndex = index[1];
            if (0 < startIndex && startIndex + 1 < text.length() && text.codePointCount(startIndex -1, startIndex +1) == 1) startIndex++;
            if (0 < endIndex && endIndex + 1 < text.length() && text.codePointCount(endIndex -1, endIndex +1) == 1) endIndex++;
            List<Expression> lineList = ParseUtilities.parseSubDoc(doc, startIndex, endIndex, checkSetup.getObjLangName());
            underlineTexts.add(lineList);
        }
        return underlineTexts;
    }


    private void updateCheckCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !vtExercise.getMainWindow().isInstructorFunctions()) {
            vtView.getCheckButton().setDisable(true);
            vtView.getCheckProgButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        vtView.getCheckTriesLabel().setText(checkString);
    }


    public void setLangAllowDP(boolean langAllowDP) {
        if (langAllowDP) this.langAllowDP = true;
        else this.langAllowDP = false;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckType(VTCheckType checkType) {
        this.checkType = checkType;
    }

    public List<List<TreeNode>> getFormulaTree() {
        return formulaTree;
    }
}
