package slapp.editor.vert_tree_abexplain;

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


public class VTABExpCheck implements VTAuxCheck {
    private VerticalTreeABExpExercise vtABExpExercise;
    private VerticalTreeABExpView vtABExpView;
    private VerticalTreeABExpModel vtABExpModel;
    private VTcheckSetup checkSetup;
    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;
    List<Node> checkNodeList;
    private List<List<ABExpTreeNode>> formulaTree;
    private List<List<String>> lineJustifications;
    private double equalsEpsilon = 10.0;
    private boolean checkJustification = true;
    private boolean checkValueJustification;
    private boolean checkMarkup = true;
    private List<ABExpTreeNode> connectedNodes;
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
    private ABExpTreeNode rootNode;
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
    boolean checkChoices;
    boolean choiceASet;
    boolean choiceBSet;
    boolean choiceSet;
    boolean choiceAMade;
    boolean choiceBMade;
    boolean choiceMade;
    String message;

    // SCORCHED EARTH RULE 1: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    private void scrollToNode(Node node) {
        try {
            if (vtABExpExercise != null && vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().getMainView() != null) {
                vtABExpExercise.getMainWindow().getMainView().scrollToNode(node);
            }
        } catch (Exception e) {}
    }

    VTABExpCheck(VerticalTreeABExpExercise vtABExpExercise) {
        this.vtABExpExercise = vtABExpExercise;
        this.vtABExpView = vtABExpExercise.getExerciseView();
        this.vtABExpModel = vtABExpExercise.getExerciseModel();
        checkSetup = vtABExpModel.getCheckSetup();
        if (checkSetup == null) { checkSetup = new VTcheckSetup(); vtABExpModel.setCheckSetup(checkSetup); }
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
        checkBracket = vtABExpModel.getDragIconList().contains(DragIconType.bracket);
        checkDashedLine = vtABExpModel.getDragIconList().contains(DragIconType.dashed_line);
        checkCircle = vtABExpModel.getObjectControlList().contains(ObjectControlType.CIRCLE);
        checkStar = vtABExpModel.getObjectControlList().contains(ObjectControlType.STAR);
        checkMapping = vtABExpModel.getObjectControlList().contains(ObjectControlType.MAPPING);
        checkBoxing = vtABExpModel.getObjectControlList().contains(ObjectControlType.FORMULA_BOX);
        checkUnderline = vtABExpModel.getObjectControlList().contains(ObjectControlType.UNDERLINE);
        checkChoices = checkSetup.isCheckChoices();
        choiceASet = checkSetup.isChoiceA();
        choiceBSet = checkSetup.isChoiceB();
        choiceSet = choiceASet || choiceBSet;
        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            vtABExpView.setCheckColor(Color.LAWNGREEN);
            vtABExpView.setCheckElementsColor(Color.GREEN);
            vtABExpView.setCheckMessage(checkSetup.getCheckMessage());
        } else {
            vtABExpView.setCheckColor(Color.ORCHID);
            vtABExpView.setCheckElementsColor(Color.PURPLE);
            vtABExpView.setCheckMessage("Nodes: Good");
        }
        if (checkSuccess) vtABExpView.activateBigCheck();
        else vtABExpView.deactivateBigCheck();
        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();
        setRightControlBox();
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 1: Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Clear Global Static Caches
        try { MatchUtilities.clearMatching(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
        try {
            if (vtABExpView != null) {
                if (vtABExpView.getCheckButton() != null) {
                    if (checkButtonHandler != null) vtABExpView.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                    vtABExpView.getCheckButton().setOnAction(null);
                }
                if (vtABExpView.getstaticHelpButton() != null) {
                    if (staticHelpButtonHandler != null) vtABExpView.getstaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                    vtABExpView.getstaticHelpButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-Destructive Data Teardown
        try { this.formulaTree = null; } catch (Exception e) {}
        try { this.lineJustifications = null; } catch (Exception e) {}
        try { this.connectedNodes = null; } catch (Exception e) {}
        try { this.quantifiedSubformulas = null; } catch (Exception e) {}
        try { this.checkNodeList = null; } catch (Exception e) {}

        // Sever all local pointers safely
        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
        try { this.vtABExpExercise = null; } catch (Exception e) {}
        try { this.vtABExpView = null; } catch (Exception e) {}
        try { this.vtABExpModel = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.objLang = null; } catch (Exception e) {}
        try { this.mLang = null; } catch (Exception e) {}
        try { this.targetExpression = null; } catch (Exception e) {}
        try { this.rootNode = null; } catch (Exception e) {}
        try { this.auxExerA = null; } catch (Exception e) {}
        try { this.vtAuxCheckA = null; } catch (Exception e) {}
        try { this.mParseUtilities = null; } catch (Exception e) {}
        try { this.message = null; } catch (Exception e) {}
    }

    private void setRightControlBox() {
        if (vtABExpView == null) return;
        if (checkType == VTCheckType.NONE && vtABExpView.getCheckButton() != null) vtABExpView.getCheckButton().setDisable(true);

        checkButtonHandler = e -> {
            if (isTornDown || vtABExpExercise == null || vtABExpView == null) return;
            checkTree();
        };
        if (vtABExpView.getCheckButton() != null) vtABExpView.getCheckButton().setOnAction(checkButtonHandler);

        staticHelpButtonHandler = e -> {
            if (isTornDown || vtABExpExercise == null || vtABExpView == null || checkSetup == null) return;
            Stage helpStage = vtABExpView.getStaticHelpStage();
            if (helpStage != null && helpStage.isShowing()) helpStage.close();
            else if (checkSetup.getStaticHelpDoc() != null) vtABExpView.showStaticHelp(checkSetup.getStaticHelpDoc());
        };
        if (vtABExpView.getstaticHelpButton() != null) {
            vtABExpView.getstaticHelpButton().setDisable(checkSetup == null || !checkSetup.isStaticHelp());
            vtABExpView.getstaticHelpButton().setOnAction(staticHelpButtonHandler);
        }
    }

    private void setChecksCounter() {
        if (vtABExpView == null || vtABExpExercise == null || vtABExpExercise.getMainWindow() == null) return;
        if (checkMax != -1 && checkTries >= checkMax && !vtABExpExercise.getMainWindow().isInstructorFunctions()) {
            if (vtABExpView.getCheckButton() != null) vtABExpView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";
        if (vtABExpView.getCheckTriesLabel() != null) vtABExpView.getCheckTriesLabel().setText(checkString);
    }

    public boolean checkTree() {
        if (vtABExpExercise == null || vtABExpView == null) return false;
        switch (checkType) {
            case FORMULA: vtABExpView.setCheckMessage("Tree"); this.silent = false; return checkFormulaTree(checkMarkup);
            case UNABB: vtABExpView.setCheckMessage("Tree"); this.silent = false; return checkUnabbTree(false);
            case TRUTH_VALUE: vtABExpView.setCheckMessage("Tree / Values"); this.silent = false; checkJustification = false; langAllowDP = true; return checkTruthValueTree();
            case UNABB_AUX: this.silent = true; this.checkJustification = false; return checkFormulaTree(false);
            case TERM_VALUE: vtABExpView.setCheckMessage("Tree / Values"); this.silent = false; checkJustification = false; langAllowDP = true; return checkTermValueTree();
            case MAPPING: return false;
        }
        return false;
    }

    private boolean checkFormulaTree(boolean checkMarkup) {
        if (vtABExpExercise == null || vtABExpView == null) return false;
        boolean goodTree = true;
        vtABExpView.deactivateBigCheck();
        if (vtABExpExercise.getMainWindow() != null && !vtABExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>();
        lineJustifications = new ArrayList<>();
        if (vtABExpView.getRootLayout() == null || vtABExpView.getRootLayout().getMain_pane() == null) return false;
        // SCORCHED EARTH RULE 8: Safely read UI children without CME risk
        checkNodeList = new ArrayList<>(vtABExpView.getRootLayout().getMain_pane().getChildren());
        if (vtABExpExercise.getExerciseView() != null) {
            if (vtABExpExercise.getExerciseView().getaCheckBox() != null) choiceAMade = vtABExpExercise.getExerciseView().getaCheckBox().isSelected();
            if (vtABExpExercise.getExerciseView().getbCheckBox() != null) choiceBMade = vtABExpExercise.getExerciseView().getbCheckBox().isSelected();
        }
        choiceMade = choiceAMade || choiceBMade;
        if (checkNodeList.isEmpty()) {
            if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
            goodTree = false;
        } else {
            List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
            if (sortedFormulaBoxes.isEmpty()) {
                if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
                goodTree = false;
            } else if (!populateTreeLists(sortedFormulaBoxes)) goodTree = false;
            else if (!setParentsAndChildren()) goodTree = false;
            else if (!checkTrueTree()) goodTree = false;
            else if (!checkContents()) goodTree = false;
            else if (checkMarkup && !checkMarkup()) goodTree = false;
        }
        checkSuccess = false;
        if (goodTree) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || (vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().isInstructorFunctions()))) {
                if (checkChoice()) { checkSuccess = true; vtABExpView.setCheckMessage("Tree / Choice"); }
            } else { checkSuccess = true; vtABExpView.setCheckMessage("Tree"); }
        } else if (targetExpression == null) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || (vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().isInstructorFunctions()))) {
                if (checkChoice()) { checkSuccess = true; vtABExpView.setCheckMessage("Choice"); }
            }
        }
        if (checkSuccess) { vtABExpView.activateBigCheck(); return true; } else return false;
    }

    private boolean checkUnabbTree(boolean checkMarkup) {
        if (vtABExpExercise == null || vtABExpView == null) return false;
        checkSuccess = false;
        vtABExpView.deactivateBigCheck();
        if (vtABExpExercise.getMainWindow() != null && !vtABExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>();
        lineJustifications = new ArrayList<>();
        if (vtABExpView.getRootLayout() == null || vtABExpView.getRootLayout().getMain_pane() == null) return false;
        checkNodeList = new ArrayList<>(vtABExpView.getRootLayout().getMain_pane().getChildren());
        if (vtABExpExercise.getExerciseView() != null) {
            if (vtABExpExercise.getExerciseView().getaCheckBox() != null) choiceAMade = vtABExpExercise.getExerciseView().getaCheckBox().isSelected();
            if (vtABExpExercise.getExerciseView().getbCheckBox() != null) choiceBMade = vtABExpExercise.getExerciseView().getbCheckBox().isSelected();
        }
        choiceMade = choiceAMade || choiceBMade;
        boolean goodTree = true;
        if (checkNodeList.isEmpty()) {
            if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
            goodTree = false;
        } else {
            List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
            if (sortedFormulaBoxes.isEmpty()) {
                if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
                goodTree = false;
            } else {
                auxExerA = vtABExpExercise.getVTAuxExer();
                if (auxExerA == null) {
                    if (!silent) EditorAlerts.showSimpleTxtListAlert("Auxiliary Tree", Collections.singletonList(ParseUtilities.newRegularText("Missing auxiliary vertical tree.  Cannot check unabbreviation.")));
                    goodTree = false;
                } else {
                    vtAuxCheckA = auxExerA.getVTAuxCheck();
                    if (vtAuxCheckA != null) vtAuxCheckA.setCheckType(VTCheckType.UNABB_AUX);
                    if (vtAuxCheckA == null || !vtAuxCheckA.checkTree()) {
                        if (!silent) EditorAlerts.showSimpleTxtListAlert("Auxiliary Tree", Collections.singletonList(ParseUtilities.newRegularText("Unabbreviation check requires (successfully) completed auxiliary tree.")));
                        goodTree = false;
                    } else if (!populateTreeLists(sortedFormulaBoxes)) goodTree = false;
                    else if (!setParentsAndChildren()) goodTree = false;
                    else if (!checkTrueTree()) goodTree = false;
                    else if (!checkFormulaContents()) goodTree = false;
                    else {
                        List<List<? extends VTAuxTreeNode>> auxFormulaTree = vtAuxCheckA.getAuxFormulaTree();
                        try { checkParallelTrees(formulaTree.get(formulaTree.size() - 1).get(0), auxFormulaTree.get(auxFormulaTree.size() - 1).get(0));
                        } catch (TreeNodeException e) {
                            if (!silent) {
                                e.getAuxTreeNode().getMainFormulaBox().setVTtreeBoxHighlight();
                                scrollToNode(e.getAuxTreeNode().getMainFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Trees Structure", e.getMessageList());
                                e.getAuxTreeNode().getMainFormulaBox().resetVTtreeBoxHighlight();
                            }
                            goodTree = false;
                        }
                    }
                    if (goodTree) { if (!checkAbbContent()) goodTree = false; }
                }
            }
        }
        if (goodTree) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || (vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().isInstructorFunctions()))) {
                if (checkChoice()) { checkSuccess = true; vtABExpView.setCheckMessage("Tree / Choice"); }
            } else { checkSuccess = true; vtABExpView.setCheckMessage("Tree"); }
        }
        if (checkSuccess) { vtABExpView.activateBigCheck(); return true; } else return false;
    }

    private boolean checkTruthValueTree() {
        if (vtABExpExercise == null || vtABExpView == null) return false;
        boolean goodTree = true;
        checkSuccess = false;
        vtABExpView.deactivateBigCheck();
        if (vtABExpExercise.getMainWindow() != null && !vtABExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>();
        lineJustifications = new ArrayList<>();
        if (vtABExpView.getRootLayout() == null || vtABExpView.getRootLayout().getMain_pane() == null) return false;
        checkNodeList = new ArrayList<>(vtABExpView.getRootLayout().getMain_pane().getChildren());
        if (vtABExpExercise.getExerciseView() != null) {
            if (vtABExpExercise.getExerciseView().getaCheckBox() != null) choiceAMade = vtABExpExercise.getExerciseView().getaCheckBox().isSelected();
            if (vtABExpExercise.getExerciseView().getbCheckBox() != null) choiceBMade = vtABExpExercise.getExerciseView().getbCheckBox().isSelected();
        }
        choiceMade = choiceAMade || choiceBMade;
        if (checkNodeList.isEmpty()) {
            if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate.")));
            goodTree = false;
        } else {
            List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
            if (sortedFormulaBoxes.isEmpty()) {
                if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
                goodTree = false;
            } else if (!populateTreeLists(sortedFormulaBoxes)) goodTree = false;
            else if (!setParentsAndChildren()) goodTree = false;
            else if (!checkTrueTree()) goodTree = false;
            else if (!checkContents()) goodTree = false;
            else if (!checkTruthValues()) goodTree = false;
        }
        if (goodTree) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || (vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().isInstructorFunctions()))) {
                if (checkChoice()) {
                    checkSuccess = true;
                    vtABExpView.setCheckMessage("Tree / Values / Choice");
                }
            } else {
                checkSuccess = true; vtABExpView.setCheckMessage("Tree / Values");
            }
        } else if (targetExpression == null) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || (vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().isInstructorFunctions()))) {
                if (checkChoice()) {
                    checkSuccess = true;
                    vtABExpView.setCheckMessage("Choice");
                }
            }
        }
        if (checkSuccess) { vtABExpView.activateBigCheck(); return true; } else return false;
    }

    private boolean checkTermValueTree() {
        if (vtABExpExercise == null || vtABExpView == null) return false;
        boolean goodTree = true;
        checkSuccess = false;
        vtABExpView.deactivateBigCheck();
        if (vtABExpExercise.getMainWindow() != null && !vtABExpExercise.getMainWindow().isInstructorFunctions()) { checkTries++; setChecksCounter(); }
        formulaTree = new ArrayList<>();
        lineJustifications = new ArrayList<>();
        if (vtABExpView.getRootLayout() == null || vtABExpView.getRootLayout().getMain_pane() == null) return false;
        checkNodeList = new ArrayList<>(vtABExpView.getRootLayout().getMain_pane().getChildren());
        if (vtABExpExercise.getExerciseView() != null) {
            if (vtABExpExercise.getExerciseView().getaCheckBox() != null) choiceAMade = vtABExpExercise.getExerciseView().getaCheckBox().isSelected();
            if (vtABExpExercise.getExerciseView().getbCheckBox() != null) choiceBMade = vtABExpExercise.getExerciseView().getbCheckBox().isSelected();
        }
        choiceMade = choiceAMade || choiceBMade;
        if (checkNodeList.isEmpty()) {
            if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Nothing to evaluate.")));
            goodTree = false;
        } else {
            List<Node> sortedFormulaBoxes = getSortedFormulaBoxes();
            if (sortedFormulaBoxes.isEmpty()) {
                if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("No tree to evaluate.")));
                goodTree = false;
            } else if (!populateTreeLists(sortedFormulaBoxes)) goodTree = false;
            else if (!setParentsAndChildren()) goodTree = false;
            else if (!checkTrueTree()) goodTree = false;
            else if (!checkContents()) goodTree = false;
            else if (!checkTermValues()) goodTree = false;
        }
        if (goodTree) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || (vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().isInstructorFunctions()))) {
                if (checkChoice()) { checkSuccess = true; vtABExpView.setCheckMessage("Tree / Values / Choice"); }
            } else { checkSuccess = true; vtABExpView.setCheckMessage("Tree / Values"); }
        } else if (targetExpression == null) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || (vtABExpExercise.getMainWindow() != null && vtABExpExercise.getMainWindow().isInstructorFunctions()))) {
                if (checkChoice()) { checkSuccess = true; vtABExpView.setCheckMessage("Choice"); }
            }
        }
        if (checkSuccess) { vtABExpView.activateBigCheck(); return true; } else return false;
    }

    private boolean checkTermValues() {
        if (vtABExpExercise == null || vtABExpModel == null || vtABExpModel.getCheckSetup() == null) return false;
        if (!setTermValues()) return false;
        if (formulaTree == null || formulaTree.isEmpty()) return false;
        MSyntacticalFns syntacticalFns = new MSyntacticalFns(vtABExpExercise.getMainWindow());
        if (syntacticalFns.setInterpBaseVarAssign(vtABExpModel.getExerciseStatement(), vtABExpModel.getCheckSetup().getObjLangName())) {
            List<Expression> expressions = mParseUtilities.parseDoc(vtABExpModel.getExerciseStatement(), objLangName);
            MInterpAssignTerm interpAssignTerm = null;
            for (Expression exp : expressions) {
                if (exp.getType() == ExpressionType.M_INTERP_ASSIGN_TERM) interpAssignTerm = (MInterpAssignTerm) exp;
            }
            if (interpAssignTerm == null) {
                List<Text> textList = new ArrayList<>();
                textList.add(ParseUtilities.newRegularText("The exercise statement does not include the expression to be evaluated - as \ud835\udda8"));
                textList.add(ParseUtilities.newSubscriptText("\ud835\uddbd(\ud835\udccd|\u2134)"));
                textList.add(ParseUtilities.newRegularText("[\ud835\udcab] for some interpretation, variable assignment, (variable, object), and formula."));
                EditorAlerts.showSimpleTxtListAlert("Setup Issue:", textList);
                return false;
            }
            Term targetTerm = interpAssignTerm.getTerm();
            if (!((Term) targetExpression).equals(targetTerm)) { EditorAlerts.showSimpleTxtListAlert("Setup Issue", Collections.singletonList(ParseUtilities.newRegularText("Statement formula not same as target created for exercise."))); return false; }
            MInterpretationAssignment interpAssignment = interpAssignTerm.getInterpAssign();
            MInterpretationVariable interpVariable = interpAssignment.getInterpVariable();
            MInterpretation interpretation = syntacticalFns.getInterpretations().getInterpretation(interpVariable);
            MAssignment assWithMods = interpAssignment.getAssignment();
            MAssignmentVariable assignmentVariable = assWithMods.getAssignmentVariable();
            Set<MAssignmentMod> assignmentMods = assWithMods.getAssignmentMods();
            Map<Term, MBasicObjTerm> varAssignment = syntacticalFns.getBaseVarAssign().getAssignment(assignmentVariable);
            for (MAssignmentMod mod : assignmentMods) { varAssignment.put(mod.getVariable(), mod.getObject()); }
            List<ABExpTreeNode> topRow = formulaTree.get(0);
            for (int i = 0; i < topRow.size(); i++) {
                ABExpTreeNode treeNode = topRow.get(i);
                BoxedDRTA treeNodeBDRTA = treeNode.getMainTreeBox().getFormulaBox();
                RichTextArea treeNodeRTA = treeNodeBDRTA.getRTA();
                treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                List<Expression> nodeParse = ParseUtilities.parseDoc(treeNodeRTA.getDocument(), objLangName);
                if (nodeParse.isEmpty()) return false;
                Expression treeNodeExpr = nodeParse.get(0);
                if (treeNodeExpr.getType() != ExpressionType.TERM) {
                    treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Expression is not a term of " + objLangName + ".")));
                    treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                }
                Term treeNodeTerm = (Term) treeNodeExpr;
                if (!(treeNodeTerm.getTermType() == TermType.CONSTANT || treeNodeTerm.getTermType() == TermType.VARIABLE)) {
                    treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Top row must consist entirely of variabls and constants.")));
                    treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                }
                if (treeNodeTerm.getTermType() == TermType.CONSTANT) {
                    MBasicObjTerm object = interpretation.getConstantVal(treeNodeTerm);
                    if (object == null) {
                        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this constant.")));
                        treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                    }
                    if (!object.equals(treeNode.getMainTreeBox().getSemanticValue())) {
                        BoxedDRTA annBDRTA = treeNode.getMainTreeBox().getAnnotationField();
                        annBDRTA.setAnnBoxHighlight(); scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        annBDRTA.resetAnnHighlight(); return false;
                    }
                    if (checkValueJustification && !lineJustifications.get(0).get(i).equals("TA(c)")) {
                        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect justification for this node.")));
                        treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                    }
                }
                if (treeNodeTerm.getTermType() == TermType.VARIABLE) {
                    MBasicObjTerm object = varAssignment.get(treeNodeTerm);
                    if (object == null) {
                        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Variable assignment gives no value to this variable.")));
                        treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                    }
                    if (!object.equals(treeNode.getMainTreeBox().getSemanticValue())) {
                        BoxedDRTA annBDRTA = treeNode.getMainTreeBox().getAnnotationField();
                        annBDRTA.setAnnBoxHighlight(); scrollToNode(annBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                        annBDRTA.resetAnnHighlight(); return false;
                    }
                    if (checkValueJustification && !lineJustifications.get(0).get(i).equals("TA(v)")) {
                        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect justification for this node.")));
                        treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                    }
                }
            }
            for (int j = 1; j < formulaTree.size(); j++) {
                List<ABExpTreeNode> treeRowList = formulaTree.get(j);
                for (int i = 0; i < treeRowList.size(); i++) {
                    ABExpTreeNode treeNode = treeRowList.get(i);
                    BoxedDRTA treeNodeBDRTA = treeNode.getMainTreeBox().getFormulaBox();
                    RichTextArea treeNodeRTA = treeNodeBDRTA.getRTA();
                    treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    List<Expression> nodeParse = ParseUtilities.parseDoc(treeNodeRTA.getDocument(), objLangName);
                    if (nodeParse.isEmpty()) return false;
                    Expression treeNodeExpr = nodeParse.get(0);
                    if (treeNodeExpr.getType() != ExpressionType.TERM) {
                        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Expression is not a term of " + objLangName + ".")));
                        treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                    }
                    Term treeNodeTerm = (Term) treeNodeExpr;
                    if (treeNodeTerm.getTermType() == TermType.CONSTANT || treeNodeTerm.getTermType() == TermType.VARIABLE) {
                        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Variables and constants should appear in the top row.")));
                        treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                    }
                    if (treeNodeTerm.getTermType() == TermType.COMPLEX) {
                        List<MBasicObjTerm> assignmentTuple = new ArrayList<>();
                        for (int k = 0; k < treeNode.getParents().size(); k++) { assignmentTuple.add(treeNode.getParents().get(k).getMainTreeBox().getSemanticValue()); }
                        MBasicObjTerm assignedObj = null;
                        MFunctionCandidate fnCandidate = interpretation.getFunctionVal(treeNodeTerm.getMainFnSymbol());
                        if (fnCandidate == null) {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation assigns no value to this function symbol.")));
                            treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }
                        for (MFnPair fnPair : fnCandidate.getPairs()) {
                            if (fnPair.getInput().getMembers().equals(assignmentTuple)) { assignedObj = fnPair.getOutput(); break; }
                        }
                        if (assignedObj == null) {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Interpretation sets no value for this input.")));
                            treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }
                        if (!assignedObj.equals(treeNode.getMainTreeBox().getSemanticValue())) {
                            BoxedDRTA annBDRTA = treeNode.getMainTreeBox().getAnnotationField();
                            annBDRTA.setAnnBoxHighlight(); scrollToNode(annBDRTA.getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect value.")));
                            annBDRTA.resetAnnHighlight(); return false;
                        }
                        if (checkValueJustification && !lineJustifications.get(j).get(i).equals("TA(f)")) {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeRTA);
                            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect justification for this node.")));
                            treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }
                    }
                }
            }
        } else { return false; }
        return true;
    }

    private boolean setTermValues() {
        if (vtABExpExercise == null || formulaTree == null || formulaTree.isEmpty()) return false;
        for (int j = 0; j < formulaTree.size(); j++) {
            List<ABExpTreeNode> treeRowList = formulaTree.get(j);
            for (int i = 0; i < treeRowList.size(); i++) {
                ABExpTreeNode treeNode = treeRowList.get(i);
                ABExpTreeFormulaBox treeBox = treeNode.getMainTreeBox();
                if (!treeBox.isAnnotation()) {
                    treeBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeBox.getFormulaBox().getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Missing annotation field.")));
                    treeBox.getFormulaBox().resetVTtreeBoxHighlight(); return false;
                }
                MBasicObjTerm obj = extractObjValue(treeBox.getAnnotationField());
                if (obj == null) return false; else treeBox.setSemanticValue(obj);
            }
        }
        return true;
    }

    private MBasicObjTerm extractObjValue(BoxedDRTA bdrta) {
        if (bdrta == null || vtABExpExercise == null) return null;
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        List<Expression> expressions = mParseUtilities.parseObj(rta.getDocument(), objLangName);
        if (expressions.size() == 0) {
            bdrta.setAnnBoxHighlight(); scrollToNode(bdrta.getRTA());
            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No empty value for term.")));
            bdrta.resetAnnHighlight(); return null;
        } else if (expressions.size() == 3 && expressions.get(0) instanceof MOpenSquareBracket && expressions.get(1) instanceof MBasicObjTerm && expressions.get(2) instanceof MCloseSquareBracket) {
            return (MBasicObjTerm) expressions.get(1);
        } else {
            bdrta.setAnnBoxHighlight(); scrollToNode(bdrta.getRTA());
            EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Cannot parse to expression of the sort, Open square bracket / Object term / Close square bracket.")));
            bdrta.resetAnnHighlight(); return null;
        }
    }

    private boolean checkTruthValues() {
        if (vtABExpExercise == null || vtABExpModel == null || vtABExpModel.getCheckSetup() == null) return false;
        if (formulaTree == null || formulaTree.isEmpty()) return false;
        MSyntacticalFns syntacticalFns = new MSyntacticalFns(vtABExpExercise.getMainWindow());
        syntacticalFns.setSententialInterp(true);
        if (syntacticalFns.setInterpBaseVarAssign(vtABExpModel.getExerciseStatement(), vtABExpModel.getCheckSetup().getObjLangName())) {
            Map<MInterpretationVariable, MInterpretation> interpretations = syntacticalFns.getInterpretations().getMap();
            if (interpretations.size() == 0) {
                EditorAlerts.showSimpleTxtListAlert("Tree Check", Collections.singletonList(ParseUtilities.newRegularText("Do not find interpretation in exercise statement.  Cannot check truth values.")));
                return false;
            } else if (interpretations.size() > 1) {
                EditorAlerts.showSimpleTxtListAlert("Tree Check", Collections.singletonList(ParseUtilities.newRegularText("More than one interpretation found in exercise statement.  Cannot check truth values.")));
                return false;
            } else {
                Map.Entry<MInterpretationVariable, MInterpretation> entry = interpretations.entrySet().iterator().next();
                MInterpretation interpretation = entry.getValue();
                for (ABExpTreeNode treeNode: formulaTree.get(0)) {
                    RichTextArea treeNodeRTA = treeNode.getMainTreeBox().getFormulaBox().getRTA();
                    treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    List<Expression> nodeParse = ParseUtilities.parseDoc(treeNodeRTA.getDocument(), objLangName);
                    if (nodeParse.isEmpty()) return false;
                    Formula treeNodeFormula = (Formula) nodeParse.get(0);
                    if (!(treeNodeFormula.isAtomic() && SyntacticalFns.sentence(treeNodeFormula, objLangName))) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Top row must consist entirely of basic sentences.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false;
                    }
                    MTruthValue interpretationValue = interpretation.getFormulaVal(treeNodeFormula);
                    if (interpretationValue == null) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        List<Text> textList = new ArrayList<>(); textList.add(new Text("Truth value for this basic sentence not included in interpretation ")); textList.addAll(entry.getKey().toTextList()); textList.add(new Text("."));
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", textList); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false;
                    }
                    MTruthValue annotationValue = extractTruthValue(treeNode.getMainTreeBox().getAnnotationDoc().getText());
                    if (annotationValue == null) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No truth value set for this expression, as \"(" + mLang.getTrueSym() + ")\" or \"(" + mLang.getFalseSym() + ")\" - where the true/false symbol is from the same (sans) font as in exercise statement.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false;
                    }
                    if (!interpretationValue.equals(annotationValue)) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        List<Text> textList = new ArrayList<>(); textList.add(new Text("Not the value set in interpretation ")); textList.addAll(entry.getKey().toTextList()); textList.add(new Text("."));
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", textList); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight(); return false;
                    }
                }
            }
        } else return false;

        for (int j = 1; j < formulaTree.size(); j++) {
            List<ABExpTreeNode> treeRowList = formulaTree.get(j);
            for (int i = 0; i < treeRowList.size(); i++) {
                ABExpTreeNode treeNode = treeRowList.get(i);
                BoxedDRTA treeNodeBDRTA = treeNode.getMainTreeBox().getFormulaBox();
                RichTextArea treeNodeRTA = treeNodeBDRTA.getRTA();
                treeNodeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                rootNode = formulaTree.get(formulaTree.size() - 1).get(0);
                objLang.setAllowDroppedBrackets(treeNode.equals(rootNode));
                List<Expression> nodeParse = ParseUtilities.parseDoc(treeNodeRTA.getDocument(), objLang);
                if (nodeParse.isEmpty()) return false;
                Formula treeNodeFormula = (Formula) nodeParse.get(0);
                if (treeNodeFormula.isAtomic()) {
                    treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("All basic sentences should appear in top row.")));
                    treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                } else {
                    Operator mainOp = treeNodeFormula.getMainOperator();
                    MTruthValue formulaValue = extractTruthValue(treeNode.getMainTreeBox().getAnnotationDoc().getText());
                    if (formulaValue == null) {
                        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("No truth value set for this expression, as \"(" + mLang.getTrueSym() + ")\" or \"(" + mLang.getFalseSym() + ")\" -  where the true/false symbol is from the same (sans) font as in exercise statement.")));
                        treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                    }
                    if (mainOp.getType() == ExpressionType.NEG_OP) {
                        MTruthValue parentValue = extractTruthValue(treeNode.getParents().get(0).getMainTreeBox().getAnnotationDoc().getText());
                        if (parentValue instanceof MTheTrue && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 1\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (parentValue instanceof MTheFalse && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 2\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect truth value."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }
                    } else if (mainOp.getType() == ExpressionType.COND_OP) {
                        MTruthValue pVL = extractTruthValue(treeNode.getParents().get(0).getMainTreeBox().getAnnotationDoc().getText());
                        MTruthValue pVR = extractTruthValue(treeNode.getParents().get(1).getMainTreeBox().getAnnotationDoc().getText());
                        if (pVL instanceof MTheTrue && pVR instanceof MTheTrue && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 1\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheTrue && pVR instanceof MTheFalse && formulaValue instanceof MTheFalse) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 2\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheTrue && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 3\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else if (pVL instanceof MTheFalse && pVR instanceof MTheFalse && formulaValue instanceof MTheTrue) {
                            if (checkValueJustification && !lineJustifications.get(j).get(i).matches("\\s*T\\(" + mainOp.getMainSymbol() + "\\)\\s*,?\\s*row 4\\s*")) { showJustificationError(treeNodeBDRTA); return false; }
                        } else {
                            treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA()); EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Incorrect truth value."))); treeNodeBDRTA.resetVTtreeBoxHighlight(); return false;
                        }

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
        if (vtABExpExercise == null || vtABExpExercise.getMainWindow() == null || vtABExpExercise.getMainWindow().getMainView() == null) return;
        treeNodeBDRTA.setVTtreeBoxHighlight(); scrollToNode(treeNodeBDRTA.getRTA());
        EditorAlerts.showSimpleTxtListAlert("Tree Values", Collections.singletonList(ParseUtilities.newRegularText("Correct truth value, bad justification.")));
        treeNodeBDRTA.resetVTtreeBoxHighlight();
    }

    private MTruthValue extractTruthValue(String str) {
        if (mLang == null || str == null) return null;
        if (str.matches("^\\s*\\(" + mLang.getTrueSym() + "\\)\\s*$")) return new MTheTrue(mLang.getTrueSym());
        else if (str.matches("^\\s*\\(" + mLang.getFalseSym() + "\\)\\s*$")) return new MTheFalse(mLang.getFalseSym());
        else return null;
    }

    private boolean checkChoiceMade() {
        if (vtABExpView == null || vtABExpView.getaCheckBox() == null || vtABExpView.getbCheckBox() == null) return false;
        boolean success = true;
        if (!choiceMade ) {
            if (!silent) {
                vtABExpView.getaCheckBox().setStyle("-fx-background-color: mistyrose");
                vtABExpView.getbCheckBox().setStyle("-fx-background-color: mistyrose");
                scrollToNode(vtABExpView.getaCheckBox());
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));
                vtABExpView.getaCheckBox().setStyle("-fx-background-color: white");
                vtABExpView.getbCheckBox().setStyle("-fx-background-color: white");
            }
            success = false;
        }
        return success;
    }

    private boolean checkChoice() {
        if (vtABExpView == null || vtABExpView.getaCheckBox() == null || vtABExpView.getbCheckBox() == null) return false;
        boolean success = true;
        if (choiceAMade && !choiceASet) {
            if (!silent) {
                vtABExpView.getaCheckBox().setStyle("-fx-background-color: mistyrose");
                scrollToNode(vtABExpView.getaCheckBox());
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
                vtABExpView.getaCheckBox().setStyle("-fx-background-color: white");
            }
            success = false;
        }
        if (choiceBMade && !choiceBSet) {
            if (!silent) {
                vtABExpView.getbCheckBox().setStyle("-fx-background-color: mistyrose");
                scrollToNode(vtABExpView.getbCheckBox());
                EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
                vtABExpView.getbCheckBox().setStyle("-fx-background-color: white");
            }
            success = false;
        }
        return success;
    }

    private boolean checkAbbContent() {
        if (vtABExpExercise == null || formulaTree == null || vtAuxCheckA == null) return false;
        for (int j = 0; j < formulaTree.size(); j++) {
            List<ABExpTreeNode> treeRowList = formulaTree.get(j);
            for (int i = 0; i < treeRowList.size(); i++) {
                ABExpTreeNode treeNode = treeRowList.get(i);
                RichTextArea unabbRTA = treeNode.getMainTreeBox().getFormulaBox().getRTA();
                unabbRTA.getActionFactory().saveNow().execute(new ActionEvent());
                List<Expression> unabbParse = ParseUtilities.parseDoc(unabbRTA.getDocument(), objLangName);
                if (unabbParse.isEmpty()) return false;
                Expression unabbExpr = unabbParse.get(0);
                VTAuxTreeNode abbNode = treeNode.getMate();
                RichTextArea abbRTA = abbNode.getMainFormulaBox().getRTA();
                abbRTA.getActionFactory().saveNow().execute(new ActionEvent());
                List<Expression> abbParse = ParseUtilities.parseDoc(abbRTA.getDocument(), vtAuxCheckA.getObjLangName());
                if (abbParse.isEmpty()) return false;
                Expression abbExpr = abbParse.get(0);
                if (!unabbExpr.equals(SyntacticalFns.unabbreviate(abbExpr, vtAuxCheckA.getObjLangName()))) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not the unabbreviation of the corresponding node from the auxiliary tree.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
                if (checkJustification) {
                    if (abbExpr instanceof Term) {
                        TermType abbTermType = ((Term) abbExpr).getTermType();
                        if (abbTermType == TermType.VARIABLE && !lineJustifications.get(j).get(i).equals("TR(v)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If term results from (nothing or) linked nodes by TR use that justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else if (abbTermType == TermType.CONSTANT && !lineJustifications.get(j).get(i).equals("TR(c)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If term results from (nothing or) linked nodes by TR use that justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else if (abbTermType == TermType.COMPLEX) {
                            if (abbExpr instanceof InfixTerm && !lineJustifications.get(j).get(i).equals("AB(i)")) {
                                if (!silent) {
                                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Unabbreviation of infix term has incorrect justification (use AB(i)).")));
                                    treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            } else if (!(abbExpr instanceof InfixTerm) && !lineJustifications.get(j).get(i).equals("TR(f)")) {
                                if (!silent) {
                                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If term results from (nothing or) linked nodes by TR use that justification.")));
                                    treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }
                    } else if (abbExpr instanceof Formula) {
                        Formula abbFormula = (Formula) abbExpr;
                        if (abbFormula instanceof SentenceAtomic && !lineJustifications.get(j).get(i).equals("FR(s)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If formula results from (nothing or) linked nodes by FR use that justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else if (abbFormula instanceof PrefixAtomic && !lineJustifications.get(j).get(i).equals("FR(r)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If formula results from (nothing or) linked nodes by FR use that justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else if (abbFormula instanceof InfixAtomic && !lineJustifications.get(j).get(i).equals("AB(i)")) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Unabbreviation of infix atomic has incorrect justification (use AB(i)).")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        } else if (!(abbFormula instanceof SentenceAtomic) && !(abbFormula instanceof PrefixAtomic) && !(abbFormula instanceof InfixAtomic)) {
                            Operator abbOp = abbFormula.getMainOperator();
                            String justificationString = "";
                            if (abbOp instanceof NegationOp || abbOp instanceof ConditionalOp || abbOp instanceof UniversalOp) {
                                justificationString = "FR(" + abbOp.getMainSymbol().toString() + ")";
                                if (!lineJustifications.get(j).get(i).equals(justificationString)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("If formula results from (nothing or) linked nodes by FR use that justification.")));
                                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            } else if (abbOp instanceof ConjunctionOp || abbOp instanceof DisjunctionOp || abbOp instanceof BiconditionalOp || abbOp instanceof ExistentialOp) {
                                justificationString = "AB(" + abbOp.getMainSymbol().toString() + ")";
                                if (!lineJustifications.get(j).get(i).equals(justificationString)) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                        List<Text> messageList = new ArrayList<>(); messageList.add(ParseUtilities.newRegularText("Unabbreviation of formula with main operator ")); messageList.addAll(abbOp.toTextList()); messageList.add(ParseUtilities.newRegularText(" has incorrect justification."));
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
        }
        return true;
    }

    private void checkParallelTrees(ABExpTreeNode mainNode, VTAuxTreeNode auxNode) throws TreeNodeException {
        if (mainNode == null || auxNode == null) return;
        mainNode.setMate(auxNode);
        if (mainNode.getParents().size() != auxNode.getParents().size()) {
            throw new TreeNodeException(mainNode, Collections.singletonList(ParseUtilities.newRegularText("Structures of this and the auxiliary tree are not parallel.")));
        } else {
            for (int i = 0; i < mainNode.getParents().size(); i++) checkParallelTrees(mainNode.getParents().get(i), auxNode.getParents().get(i));
        }
    }

    private boolean checkFormulaContents() {
        if (vtABExpExercise == null || formulaTree == null || objLang == null || formulaTree.isEmpty()) return false;
        rootNode = formulaTree.get(formulaTree.size() - 1).get(0);
        for (int j = 0; j < formulaTree.size(); j++) {
            List<ABExpTreeNode> treeList = formulaTree.get(j);
            for (int i = 0; i < treeList.size(); i++) {
                ABExpTreeNode treeNode = treeList.get(i);
                RichTextArea rta = treeNode.getMainTreeBox().getFormulaBox().getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                objLang.setAllowDroppedBrackets(false);
                List<Expression> parseExp = ParseUtilities.parseDoc(rta.getDocument(), objLang);
                if (parseExp.isEmpty()) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Empty node.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
                List<Expression> parseExpDP = new ArrayList<>();
                if (parseExp.size() > 1) {
                    objLang.setAllowDroppedBrackets(true);
                    parseExpDP = ParseUtilities.parseDoc(rta.getDocument(), objLang);
                    if (parseExpDP.size() != 1) {
                        if (!silent) {
                            treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                            treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else {
                        if (treeNode != rootNode || !langAllowDP) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + " - check outer parentheses.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    }
                }
                Expression exp = null;
                if (parseExp.size() == 1) exp = parseExp.get(0); else if (parseExpDP.size() == 1) exp = parseExpDP.get(0);
                if (!(exp instanceof Term) && !(exp instanceof Formula)) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
            }
        }
        objLang.setAllowDroppedBrackets(langAllowDP);
        return true;
    }

    private boolean checkMarkup() {
        if (vtABExpExercise == null || rootNode == null || objLang == null || checkSetup == null) return false;
        if (hasFormulas) {
            List<ABExpTreeNode> immediateSubs = rootNode.getParents();
            ABExpTreeFormulaBox rootBox = rootNode.getMainTreeBox();
            RichTextArea rootRTA = rootBox.getFormulaBox().getRTA();
            rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
            objLang.setAllowDroppedBrackets(langAllowDP);
            List<Expression> rootParseList = ParseUtilities.parseDoc(rootRTA.getDocument(), objLang);
            if (rootParseList.isEmpty()) return false;
            Expression rootNodeExp = rootParseList.get(0);
            if (rootNodeExp instanceof Formula && !((Formula) rootNodeExp).isAtomic()) {
                if (checkCircle) {
                    List<Expression> circleExpressions = getCircleText(rootNode.getMainTreeBox());
                    if (circleExpressions.size() == 0) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No circled text.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else if (circleExpressions.size() > 1) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not a grammatical expression.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }
                    Expression circleExp = circleExpressions.get(0);
                    if (!(circleExp instanceof Operator)) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not an operator.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }
                    if (!(((Formula) rootNodeExp).getMainOperator().equals(circleExp)) || ((Operator) circleExp).getPosition() != ((Formula) rootNodeExp).getMainOperator().getPosition()) {
                        if (!silent) {
                            rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circled text is not the main operator of the root node.")));
                            rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    }
                }
                if (checkBoxing) {
                    for (ABExpTreeNode immediateSub : immediateSubs) {
                        if (!immediateSub.getMainTreeBox().isBoxed()) {
                            if (!silent) {
                                rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Immediate subformula not boxed.")));
                                rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    }
                }
            }
            for (int j = 0; j < formulaTree.size(); j++) {
                List<ABExpTreeNode> treeList = formulaTree.get(j);
                for (int i = 0; i < treeList.size(); i++) {
                    ABExpTreeNode treeNode = treeList.get(i);
                    ABExpTreeFormulaBox expTreeFormulaBox = treeNode.getMainTreeBox();
                    RichTextArea rta = expTreeFormulaBox.getFormulaBox().getRTA();
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    objLang.setAllowDroppedBrackets(langAllowDP);
                    List<Expression> parseList = ParseUtilities.parseDoc(rta.getDocument(), objLang);
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
                                EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Box applied term (not immediate subformula).")));
                                expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }

                    }
                    if (treeNodeExp instanceof Formula) {
                        Formula treeNodeFormula = (Formula) treeNodeExp;
                        if (checkStar) {
                            if (treeNodeFormula.isAtomic() && !expTreeFormulaBox.isStarred()) {
                                if (!silent) {
                                    expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Atomic not marked with star.")));
                                    expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                            if (!treeNodeFormula.isAtomic() && expTreeFormulaBox.isStarred()) {
                                if (!silent) {
                                    expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Star applied to non-atomic formula.")));
                                    expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }
                        if (checkCircle) {
                            if (treeNode != rootNode && expTreeFormulaBox.isCircled()) {
                                if (!silent) {
                                    expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Circle applied node other than root.")));
                                    expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }
                        if (checkBoxing) {
                            if (expTreeFormulaBox.isBoxed() && !immediateSubs.contains(treeNode)) {
                                if (!silent) {
                                    expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Box applied to node other than an immediate subformula.")));
                                    expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }
                        if (checkUnderline) {
                            if (!expTreeFormulaBox.isUlineInclusion()) {
                                if (!silent) {
                                    expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline not completely \"included\" in ones below.")));
                                    expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                            List<List<Expression>> underlineTexts = getUnderlineTexts(expTreeFormulaBox);
                            List<Formula> underlineFormulas = new ArrayList<>();
                            for (List<Expression> underlineExpressions : underlineTexts) {
                                if (underlineExpressions.size() > 1) {
                                    if (!silent) {
                                        expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline text is not a grammatical expression.")));
                                        expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                                Expression underlineExp = underlineExpressions.get(0);
                                if (!(underlineExp instanceof Formula)) {
                                    if (!silent) {
                                        expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Underline text is not a (sub-)formula.")));
                                        expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                } else {
                                    Formula underlineFormula = (Formula) underlineExp;
                                    if (!(underlineFormula.getMainOperator() instanceof UniversalOp) && !(underlineFormula.getMainOperator() instanceof ExistentialOp)) {
                                        if (!silent) {
                                            expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                            EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Main operator of underline text not a quantifier.")));
                                            expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                        }
                                        return false;
                                    }
                                    underlineFormulas.add(underlineFormula);
                                }
                            }
                            quantifiedSubformulas = new ArrayList<>();
                            populateQuantifiedSubformulas(treeNode);
                            boolean[] found = new boolean[quantifiedSubformulas.size()];
                            for (int a = 0; a < quantifiedSubformulas.size(); a++) {
                                if (!found[a]) {
                                    Formula subformula = quantifiedSubformulas.get(a);
                                    for (int m = 0; m < underlineFormulas.size(); m++) {
                                        if (subformula.equals(underlineFormulas.get(m))) { found[a] = true; underlineFormulas.remove(m); break; }
                                    }
                                }
                            }
                            boolean good = true;
                            for (int b = 0; b < quantifiedSubformulas.size(); b++) { if (found[b] == false) { good = false; break; } }
                            if (!good) {
                                if (!silent) {
                                    expTreeFormulaBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(expTreeFormulaBox.getFormulaBox().getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Not every quantified subformula has underline.")));
                                    expTreeFormulaBox.getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }
                    }
                }
            }
            if (hasFormulas) {
                List<ABExpDashedLine> dashedLines = new ArrayList<>();
                List<ABExpVerticalBracket> verticalBrackets = new ArrayList<>();
                for (Node node : checkNodeList) {
                    if (node instanceof ABExpDashedLine) dashedLines.add((ABExpDashedLine) node);
                    if (node instanceof ABExpVerticalBracket) verticalBrackets.add((ABExpVerticalBracket) node);
                }
                if (checkDashedLine && checkType == VTCheckType.FORMULA) {
                    if (!hasTerms) {
                        if (!dashedLines.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("No need for dashed term/formula separator in tree without terms."))); return false; }
                    } else {
                        if (dashedLines.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Missing dashed term/formula separator line."))); return false; }
                        if (dashedLines.size() > 1) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Just one dashed term/formula separator line."))); return false; }
                        if (dashedLines.get(0).getLayoutY() < maxTermY || dashedLines.get(0).getLayoutY() > minFormulaY) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Dashed line does not separate terms from formulas."))); return false; }
                    }
                }
                if (checkBracket && checkType == VTCheckType.FORMULA) {
                    if (verticalBrackets.isEmpty()) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Missing vertical bracket on subformulas."))); return false; }
                    if (verticalBrackets.size() > 1) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Just one vertical bracket for subformulas."))); return false; }
                    ABExpVerticalBracket bracket = verticalBrackets.get(0);
                    if (hasTerms && (bracket.getLayoutY() + 10) < maxTermY) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Vertical bracket includes more than just subformulas."))); return false; }
                    if ((bracket.getLayoutY() + 10) > minFormulaY || bracket.getBoundsInParent().getMaxY() < maxFormulaY) { if (!silent) EditorAlerts.showSimpleTxtListAlert("Tree Markup", Collections.singletonList(ParseUtilities.newRegularText("Vertical bracket does not include all subformulas."))); return false; }
                }
            }
        }
        return true;
    }

    private boolean checkContents() {
        if (vtABExpExercise == null || formulaTree == null || objLang == null || formulaTree.isEmpty()) return false;
        hasTerms = false; hasFormulas = false;
        minTermY = -1; maxTermY = -1; minFormulaY = -1; maxFormulaY = -1;
        List<ABExpTreeNode> bottomRow = formulaTree.get(formulaTree.size() - 1);
        if (bottomRow == null || bottomRow.isEmpty()) return false;
        rootNode = bottomRow.get(0);
        for (int j = 0; j < formulaTree.size(); j++) {
            List<ABExpTreeNode> treeList = formulaTree.get(j);
            for (int i = 0; i < treeList.size(); i++) {
                ABExpTreeNode treeNode = treeList.get(i);
                if (treeNode == null || treeNode.getMainTreeBox() == null || treeNode.getMainTreeBox().getFormulaBox() == null) continue;
                RichTextArea rta = treeNode.getMainTreeBox().getFormulaBox().getRTA();
                if (rta == null) return false;
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                objLang.setAllowDroppedBrackets(false);
                List<Expression> parseExp = ParseUtilities.parseDoc(rta.getDocument(), objLang);
                if (parseExp.isEmpty()) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Empty node.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
                List<Expression> parseExpDP = new ArrayList<>();
                if (parseExp.size() > 1) {
                    objLang.setAllowDroppedBrackets(true);
                    parseExpDP = ParseUtilities.parseDoc(rta.getDocument(), objLang);
                    if (parseExpDP.size() != 1) {
                        if (!silent) {
                            treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                            EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression of " + objLangName + ".")));
                            treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else {
                        if (treeNode != rootNode || !langAllowDP) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Not a grammatical expression (check outer parentheses).")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    }
                }
                Expression exp = null;
                if (parseExp.size() == 1) exp = parseExp.get(0); else if (parseExpDP.size() == 1) exp = parseExpDP.get(0);
                if (!(exp instanceof Term) && !(exp instanceof Formula)) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
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
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is variable and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("TR(v)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Variable node has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (termType == TermType.CONSTANT) {
                        if (treeNode.getParents() != null && treeNode.getParents().size() != 0) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is constant and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("TR(c)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Constant node has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (termType == TermType.COMPLEX) {
                        FunctionSymbol fnSym = term.getMainFnSymbol();
                        int places = fnSym.getPlaces();
                        if (treeNode.getParents() != null && treeNode.getParents().size() != term.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText(String.valueOf(places) + "-place function symbol must be linked to " + String.valueOf(places) + " term(s).")));
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
                                objLang.setAllowDroppedBrackets(false);
                                List<Expression> linkParseList = ParseUtilities.parseDoc(termRTA.getDocument(), objLang);
                                if (linkParseList.isEmpty()) return false;
                                if (!subTerm.equals(linkParseList.get(0))) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
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
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
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
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("This is a sentence letter and so must be a basic member of the tree.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("FR(s)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Sentence letter has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else if (formula instanceof PrefixAtomic || formula instanceof InfixAtomic) {
                        RelationSymbol relSym = (formula instanceof PrefixAtomic) ? ((PrefixAtomic) formula).getMainRelation() : ((InfixAtomic) formula).getMainRelation();
                        if (treeNode.getParents() != null && treeNode.getParents().size() != formula.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText(String.valueOf(relSym.getPlaces()) + "-place relation symbol must be linked to " + String.valueOf(relSym.getPlaces()) + " term(s).")));
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
                                objLang.setAllowDroppedBrackets(false);
                                List<Expression> linkParseList = ParseUtilities.parseDoc(termRTA.getDocument(), objLang);
                                if (linkParseList.isEmpty()) return false;
                                if (!subTerm.equals(linkParseList.get(0))) {
                                    if (!silent) {
                                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                        List<Text> textList = new ArrayList<>(); textList.add(ParseUtilities.newRegularText("Term requires link to subterm ")); textList.addAll(subTerm.toTextList()); textList.add(ParseUtilities.newRegularText(" above."));
                                        EditorAlerts.showSimpleTxtListAlert("TreeContent", textList); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                    }
                                    return false;
                                }
                            }
                        }
                        if (checkJustification && (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals("FR(r)"))) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Atomic formula has incorrect justification.")));
                                treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                            }
                            return false;
                        }
                    } else {
                        Operator mainOp = formula.getMainOperator();
                        if (treeNode.getParents() != null && treeNode.getParents().size() != formula.getChildren().size()) {
                            if (!silent) {
                                treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                if (mainOp.isUnary()) EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Formula with unary main operator must be linked to a single formula.")));
                                else EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Formula with binary main operator must be linked to two formulas.")));
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
                                objLang.setAllowDroppedBrackets(false);
                                List<Expression> linkParseList = ParseUtilities.parseDoc(termRTA.getDocument(), objLang);
                                if (linkParseList.isEmpty()) return false;
                                if (!subForm.equals(linkParseList.get(0))) {
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
                            if (mainOp instanceof NegationOp || mainOp instanceof ConditionalOp || mainOp instanceof UniversalOp) justificationString = "FR(" + mainOp.getMainSymbol().toString() + ")";
                            else if (mainOp instanceof ConjunctionOp || mainOp instanceof DisjunctionOp || mainOp instanceof BiconditionalOp || mainOp instanceof ExistentialOp) justificationString = "FR\ue8bc(" + mainOp.getMainSymbol().toString() + ")";
                            if (lineJustifications.size() <= j || lineJustifications.get(j).size() <= i || !lineJustifications.get(j).get(i).equals(justificationString)) {
                                if (!silent) {
                                    treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                                    List<Text> messageList = new ArrayList<>(); messageList.add(ParseUtilities.newRegularText("Formula with main operator ")); messageList.addAll(mainOp.toTextList()); messageList.add(ParseUtilities.newRegularText(" has incorrect justification."));
                                    EditorAlerts.showSimpleTxtListAlert("Tree Content", messageList); treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                                }
                                return false;
                            }
                        }
                    }
                }
            }
        }
        if (targetExpression != null) {
            if (rootNode == null || rootNode.getMainTreeBox() == null || rootNode.getMainTreeBox().getFormulaBox() == null) return false;
            RichTextArea rootRTA = rootNode.getMainTreeBox().getFormulaBox().getRTA();
            if (rootRTA == null) return false;
            rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
            objLang.setAllowDroppedBrackets(langAllowDP);
            List<Expression> rootParseList = ParseUtilities.parseDoc(rootRTA.getDocument(), objLang);
            if (rootParseList.isEmpty()) return false;
            if (!rootParseList.get(0).equals(targetExpression)) {
                if (!silent) {
                    rootNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(rootNode.getMainTreeBox().getFormulaBox().getRTA());
                    EditorAlerts.showSimpleTxtListAlert("Tree Content", Collections.singletonList(ParseUtilities.newRegularText("Good tree, but this is not the expected target expression.")));
                    rootNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                }
                return false;
            }
        }
        objLang.setAllowDroppedBrackets(langAllowDP);
        return true;
    }

    private boolean checkTrueTree() {
        if (vtABExpExercise == null || formulaTree == null || formulaTree.isEmpty()) return false;
        List<ABExpTreeNode> bottomRow = formulaTree.get(formulaTree.size() - 1);
        if (bottomRow.size() > 1) {
            if (!silent) {
                scrollToNode(bottomRow.get(0).getMainTreeBox().getFormulaBox().getRTA());
                for (ABExpTreeNode treeNode : bottomRow) treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A tree can have only one bottom (root) node.")));
                for (ABExpTreeNode treeNode : bottomRow) treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
            }
            return false;
        }
        for (List<ABExpTreeNode> row : formulaTree) {
            for (ABExpTreeNode treeNode : row) {
                if (treeNode.getChildren().size() > 1) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("A node can be linked to at most one child (lower) node.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
            }
        }
        connectedNodes = new ArrayList<>();
        populateConnectedNodes(formulaTree.get(formulaTree.size() - 1).get(0));
        for (List<ABExpTreeNode> row : formulaTree) {
            for (ABExpTreeNode treeNode : row) {
                if (!connectedNodes.contains(treeNode)) {
                    if (!silent) {
                        treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(treeNode.getMainTreeBox().getFormulaBox().getRTA());
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Node is not an an ancestor connected all the way to the root node.")));
                        treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void populateConnectedNodes(ABExpTreeNode node) {
        if (node == null || connectedNodes == null) return;
        connectedNodes.add(node);
        if (node.getParents() != null) {
            for (ABExpTreeNode parent : node.getParents()) populateConnectedNodes(parent);
        }
    }

    private void populateQuantifiedSubformulas(ABExpTreeNode node) {
        if (node == null || objLang == null || quantifiedSubformulas == null) return;
        ABExpTreeFormulaBox nodeBox = node.getMainTreeBox();
        if (nodeBox == null || nodeBox.getFormulaBox() == null) return;
        RichTextArea rootRTA = nodeBox.getFormulaBox().getRTA();
        if (rootRTA == null) return;
        rootRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document nodeDoc = rootRTA.getDocument();
        objLang.setAllowDroppedBrackets(langAllowDP);
        List<Expression> nodeParseList = ParseUtilities.parseDoc(nodeDoc, objLang);
        if (nodeParseList.isEmpty()) return;
        Expression nodeExp = nodeParseList.get(0);
        if (nodeExp instanceof Formula) {
            Formula formula = (Formula) nodeExp;
            if (formula.getMainOperator() instanceof UniversalOp || formula.getMainOperator() instanceof ExistentialOp) quantifiedSubformulas.add(formula);
        }
        if (node.getParents() != null) {
            for (ABExpTreeNode parent : node.getParents()) populateQuantifiedSubformulas(parent);
        }
    }

    private boolean setParentsAndChildren() {
        if (formulaTree == null || checkNodeList == null) return false;
        for (List<ABExpTreeNode> rowList : formulaTree) {
            for (ABExpTreeNode node : rowList) {
                node.setParents(new ArrayList<>()); node.setChildren(new ArrayList<>());
                ABExpTreeFormulaBox mainTBox = node.getMainTreeBox();
                String formulaID = mainTBox.getId();
                String relativeID = "";
                for (Node link : checkNodeList) {
                    relativeID = "";
                    if (link instanceof ABExpClickableNodeLink) {
                        ABExpClickableNodeLink nodeLink = (ABExpClickableNodeLink) link;
                        if (nodeLink.getSourceId() != null && nodeLink.getSourceId().equals(formulaID)) relativeID = nodeLink.getTargetId();
                        if (nodeLink.getTargetId() != null && nodeLink.getTargetId().equals(formulaID)) relativeID = nodeLink.getSourceId();
                    }
                    if (!relativeID.equals("")) {
                        for (List<ABExpTreeNode> rList : formulaTree) {
                            for (ABExpTreeNode tNode : rList) {
                                ABExpTreeFormulaBox tBox = tNode.getMainTreeBox();
                                if (tBox.getId().equals(relativeID)) {
                                    if (tBox.getLayoutY() - mainTBox.getLayoutY() > equalsEpsilon) node.getChildren().add(tNode);
                                    else if (mainTBox.getLayoutY() - tBox.getLayoutY() > equalsEpsilon) node.getParents().add(tNode);
                                    else {
                                        if (!silent) {
                                            mainTBox.getFormulaBox().setVTtreeBoxHighlight(); scrollToNode(mainTBox.getFormulaBox().getRTA());
                                            tBox.getFormulaBox().setVTtreeBoxHighlight();
                                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Cannot have connected nodes in same row.")));
                                            mainTBox.getFormulaBox().resetVTtreeBoxHighlight(); tBox.getFormulaBox().resetVTtreeBoxHighlight();
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
        for (List<ABExpTreeNode> rowList : formulaTree) {
            for (ABExpTreeNode node : rowList) {
                if (node.getParents() != null) node.getParents().sort(Comparator.comparingDouble(parent -> parent.getMainTreeBox().getLayoutX()));
            }
        }
        return true;
    }

    private boolean populateTreeLists(List<Node> sortedFormulaBoxes) {
        if (sortedFormulaBoxes == null || vtABExpExercise == null) return false;
        int j = 0;
        List<ABExpTreeNode> treeList = new ArrayList<>();
        List<ABExpMapFormulaBox> justificationList = new ArrayList<>();
        lineJustifications = new ArrayList<>();
        for (int i = 0; i < sortedFormulaBoxes.size(); i++) {
            if (sortedFormulaBoxes.get(i) instanceof ABExpTreeFormulaBox) treeList.add(new ABExpTreeNode((ABExpTreeFormulaBox) sortedFormulaBoxes.get(i)));
            if (sortedFormulaBoxes.get(i) instanceof ABExpMapFormulaBox) justificationList.add((ABExpMapFormulaBox) sortedFormulaBoxes.get(i));
            if ((i + 1 < sortedFormulaBoxes.size() && Math.abs(sortedFormulaBoxes.get(i + 1).getLayoutY() - sortedFormulaBoxes.get(i).getLayoutY()) > equalsEpsilon) || i + 1 >= sortedFormulaBoxes.size()) {
                if (treeList.size() > 0) {
                    treeList.sort(Comparator.comparingDouble((treeBox -> treeBox.getMainTreeBox().getLayoutX())));
                    formulaTree.add(treeList);
                }
                if (treeList.size() == 0 && justificationList.size() != 0) {
                    if (!silent) {
                        BoxedDRTA justificationBDRTA = justificationList.get(0).getFormulaBox();
                        scrollToNode(justificationBDRTA.getRTA()); justificationBDRTA.setVTtreeBoxHighlight();
                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Justification without formula field on row.")));
                        for (ABExpMapFormulaBox mapBox : justificationList) mapBox.getFormulaBox().resetVTtreeBoxHighlight();
                    }
                    return false;
                }
                if (checkJustification || checkValueJustification) {
                    if (justificationList.size() == 0) {
                        if (!silent) {
                            if (!treeList.isEmpty()) scrollToNode(treeList.get(0).getMainTreeBox().getFormulaBox().getRTA());
                            for (ABExpTreeNode treeNode : treeList) treeNode.getMainTreeBox().getFormulaBox().setVTtreeBoxHighlight();
                            List<Text> messageList = new ArrayList<>(); messageList.add(ParseUtilities.newRegularText("Missing justification on row."));
                            if (checkType == VTCheckType.UNABB) messageList.add(ParseUtilities.newRegularText("\n\nIf formula results from (nothing or) linked nodes by TR or FR cite that rule.  Otherwise cite the AB rule (including an AB(i) to convert from abbreviating infix forms)."));
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", messageList);
                            for (ABExpTreeNode treeNode : treeList) treeNode.getMainTreeBox().getFormulaBox().resetVTtreeBoxHighlight();
                        }
                        return false;
                    } else if (justificationList.size() > 1) {
                        if (!silent) {
                            BoxedDRTA bdrta = justificationList.get(justificationList.size() - 1).getFormulaBox();
                            scrollToNode(bdrta.getRTA());
                            for (ABExpMapFormulaBox justificationBox : justificationList) justificationBox.getFormulaBox().setVTmapBoxHighlight();
                            EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Only one justification field for each row.")));
                            for (ABExpMapFormulaBox justificationBox : justificationList) justificationBox.getFormulaBox().resetVTmapBoxHighlight();
                        }
                        return false;
                    } else {
                        BoxedDRTA bdrta = justificationList.get(0).getFormulaBox();
                        RichTextArea rta = bdrta.getRTA();
                        rta.getActionFactory().saveNow().execute(new ActionEvent());
                        String boxText = rta.getDocument().getText();
                        if (checkJustification && (checkType == VTCheckType.FORMULA || checkType == VTCheckType.UNABB_AUX)) {
                            List<String> missingPrimes = getMissingPrime(boxText);
                            if (missingPrimes.size() > 0) {
                                if (!silent) {
                                    bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Do not forget prime on justification for abbreviating rules, as FR\ue8bc(\u2227).")));
                                    bdrta.resetVTmapBoxHighlight();
                                }
                                return false;
                            }
                            List<String> justifications = getJustificationStrings(boxText);
                            if (justifications.size() != treeList.size()) {
                                if (!silent) {
                                    bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one (allowable) justification, as FR(\u223c), for each member of row.")));
                                    bdrta.resetVTmapBoxHighlight();
                                }
                                return false;
                            }
                            lineJustifications.add(justifications);
                        } else if (checkJustification && checkType == VTCheckType.UNABB) {
                            List<String> justifications = getAbbJustificationStrings(boxText);
                            if (justifications.size() != treeList.size()) {
                                if (!silent) {
                                    bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one (allowable) justification for each member of row.\n\nIf formula results from (nothing or) linked nodes by TR or FR cite that rule.  Otherwise cite the AB rule (including an AB(i) to convert from abbreviating infix forms).")));
                                    bdrta.resetVTmapBoxHighlight();
                                }
                                return false;
                            }
                            lineJustifications.add(justifications);
                        } else if (checkValueJustification && checkType == VTCheckType.TRUTH_VALUE) {
                            if (j == 0) {
                                String[] firstLine = boxText.split(" ");
                                if (firstLine.length != 2 || !(firstLine[0].equals("From") || firstLine[0].equals("from")) || !mLang.getInterpretationVariables().contains(firstLine[1])) {
                                    if (!silent) {
                                        bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Top row should have single justification, as \"From \ud835\udda8\" - where the interpretation name is in the same (sans) font as in exercise statement.")));
                                        bdrta.resetVTmapBoxHighlight();
                                    }
                                    return false;
                                }
                                lineJustifications.add(Collections.singletonList(boxText));
                            } else {
                                List<String> justifications = getTruthValJustificationStrings(boxText);
                                if (justifications.size() != treeList.size()) {
                                    if (!silent) {
                                        bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA());
                                        EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Rows other than the top should have justifications (as \"T(\u223c) row 1\") for each member.")));
                                        bdrta.resetVTmapBoxHighlight();
                                    }
                                    return false;
                                }
                                lineJustifications.add(justifications);
                            }
                        } else if (checkValueJustification && checkType == VTCheckType.TERM_VALUE) {
                            List<String> justifications = getTermJustificationStrings(boxText);
                            if (justifications.size() != treeList.size()) {
                                if (!silent) {
                                    bdrta.setVTmapBoxHighlight(); scrollToNode(bdrta.getRTA());
                                    EditorAlerts.showSimpleTxtListAlert("Tree Structure", Collections.singletonList(ParseUtilities.newRegularText("Should be one justification for each member of row - as TA(v), TA(c) or TA(f).")));
                                    bdrta.resetVTmapBoxHighlight();
                                }
                                return false;
                            }
                            lineJustifications.add(justifications);
                        }
                    }
                }
                j++; treeList = new ArrayList<>(); justificationList = new ArrayList<>();
            }
        }
        return true;
    }

    List<String> getMissingPrime(String inputString) {
        if (inputString == null) return new ArrayList<>();
        String regexTarget = "FR\\(\\u2227\\)|FR\\(\\u2228\\)|FR\\(\\u2194\\)|FR\\(\\u2203\\)";
        Matcher matcher = Pattern.compile(regexTarget).matcher(inputString);
        List<String> justifications = new ArrayList<>();
        while (matcher.find()) justifications.add(matcher.group());
        return justifications;
    }

    List<String> getJustificationStrings(String inputString) {
        if (inputString == null) return new ArrayList<>();
        String regexTarget = "FR\\(s\\)|FR\\(r\\)|FR\\(\\u223c\\)|FR\\(\\u2192\\)|FR\\ue8bc\\(\\u2227\\)|FR\\ue8bc\\(\\u2228\\)|FR\\ue8bc\\(\\u2194\\)|FR\\(\\u2200\\)|FR\\ue8bc\\(\\u2203\\)|TR\\(v\\)|TR\\(c\\)|TR\\(f\\)";
        Matcher matcher = Pattern.compile(regexTarget).matcher(inputString);
        List<String> justifications = new ArrayList<>();
        while (matcher.find()) justifications.add(matcher.group());
        return justifications;
    }

    List<String> getAbbJustificationStrings(String inputString) {
        if (inputString == null) return new ArrayList<>();
        String regexTarget = "AB\\(i\\)|AB\\(\\u2228\\)|AB\\(\\u2227\\)|AB\\(\\u2194\\)|AB\\(\\u2203\\)|FR\\(s\\)|FR\\(r\\)|FR\\(\\u223c\\)|FR\\(\\u2192\\)|FR\\(\\u2200\\)|TR\\(v\\)|TR\\(c\\)|TR\\(f\\)";
        Matcher matcher = Pattern.compile(regexTarget).matcher(inputString);
        List<String> justifications = new ArrayList<>();
        while (matcher.find()) justifications.add(matcher.group());
        return justifications;
    }

    List<String> getTermJustificationStrings(String inputString) {
        if (inputString == null) return new ArrayList<>();
        String regexTarget = "TA\\(v\\)|TA\\(c\\)|TA\\(f\\)";
        Matcher matcher = Pattern.compile(regexTarget).matcher(inputString);
        List<String> justifications = new ArrayList<>();
        while (matcher.find()) justifications.add(matcher.group());
        return justifications;
    }

    List<String> getTruthValJustificationStrings(String inputString) {
        if (inputString == null || objLang == null) return new ArrayList<>();
        String neg = objLang.getNegation(); String cond = objLang.getConditional();
        String conj = objLang.getConjunction(); String disj = objLang.getDisjunction(); String bcnd = objLang.getBiconditional();
        String regexTarget = "(T\\(" + neg + "\\)\\s*,?\\s*row 1)|(T\\(" + neg + "\\)\\s*,?\\s*row 2)|" +
                "(T\\(" + cond + "\\)\\s*,?\\s*row 1)|(T\\(" + cond + "\\)\\s*,?\\s*row 2)|(T\\(" + cond + "\\)\\s*,?\\s*row 3)|(T\\(" + cond + "\\)\\s*,?\\s*row 4)|" +
                "(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 1)|(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 2)|(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 3)|(T\\ue8bc\\(" + conj + "\\)\\s*,?\\s*row 4)|" +
                "(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 1)|(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 2)|(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 3)|(T\\ue8bc\\(" + disj + "\\)\\s*,?\\s*row 4)|" +
                "(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 1)|(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 2)|(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 3)|(T\\ue8bc\\(" + bcnd + "\\)\\s*,?\\s*row 4)";
        Matcher matcher = Pattern.compile(regexTarget).matcher(inputString);
        List<String> justifications = new ArrayList<>();
        while (matcher.find()) justifications.add(matcher.group());
        return justifications;
    }

    private List<Node> getSortedFormulaBoxes() {
        if (checkNodeList == null) return new ArrayList<>();
        List<Node> sortedFormulaBoxes = new ArrayList<>();
        for (Node node : checkNodeList) {
            if (node instanceof ABExpTreeFormulaBox || node instanceof ABExpMapFormulaBox) sortedFormulaBoxes.add(node);
        }
        sortedFormulaBoxes.sort(Comparator.comparingDouble(node -> node.getLayoutY()));
        return sortedFormulaBoxes;
    }

    private List<Expression> getCircleText(ABExpTreeFormulaBox expTreeFormulaBox) {
        if (expTreeFormulaBox == null || expTreeFormulaBox.getFormulaBox() == null || checkSetup == null) return new ArrayList<>();
        RichTextArea rta = expTreeFormulaBox.getFormulaBox().getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        String text = doc.getText();
        int[] indexes = expTreeFormulaBox.getCircleIndexes();
        int startIndex = Math.min(indexes[0], indexes[1]);
        if (0 < startIndex && startIndex + 1 < text.length() && text.codePointCount(startIndex -1, startIndex +1) == 1) startIndex++;
        int endIndex = Math.max(indexes[0], indexes[1]);
        if (0 < endIndex && endIndex + 1 < text.length() && text.codePointCount(endIndex -1, endIndex +1) == 1) endIndex++;
        return ParseUtilities.parseSubDoc(doc, startIndex, endIndex, checkSetup.getObjLangName());
    }

    private List<List<Expression>> getUnderlineTexts(ABExpTreeFormulaBox treeFormulaBox) {
        if (treeFormulaBox == null || treeFormulaBox.getFormulaBox() == null || checkSetup == null) return new ArrayList<>();
        List<List<Expression>> underlineTexts = new ArrayList<>();
        RichTextArea rta = treeFormulaBox.getFormulaBox().getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        String text = doc.getText();
        for (Integer[] index : treeFormulaBox.getUlineIndexes()) {
            int startIndex = index[0]; int endIndex = index[1];
            if (0 < startIndex && startIndex + 1 < text.length() && text.codePointCount(startIndex -1, startIndex +1) == 1) startIndex++;
            if (0 < endIndex && endIndex + 1 < text.length() && text.codePointCount(endIndex -1, endIndex +1) == 1) endIndex++;
            List<Expression> lineList = ParseUtilities.parseSubDoc(doc, startIndex, endIndex, checkSetup.getObjLangName());
            underlineTexts.add(lineList);
        }
        return underlineTexts;
    }

    private void updateCheckCounter() {
        if (vtABExpExercise == null || vtABExpView == null || vtABExpExercise.getMainWindow() == null) return;
        if (checkMax != -1 && checkTries >= checkMax && !vtABExpExercise.getMainWindow().isInstructorFunctions()) {
            if (vtABExpView.getCheckButton() != null) vtABExpView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";
        if (vtABExpView.getCheckTriesLabel() != null) vtABExpView.getCheckTriesLabel().setText(checkString);
    }

    public void setLangAllowDP(boolean langAllowDP) {
        if (langAllowDP) this.langAllowDP = true; else this.langAllowDP = false;
    }

    public int getCheckTries() { return checkTries; }
    public boolean isCheckSuccess() { return checkSuccess; }
    public void setCheckType(VTCheckType checkType) { this.checkType = checkType; }
    public List<List<ABExpTreeNode>> getFormulaTree() { return formulaTree; }

    public List<List<? extends VTAuxTreeNode>> getAuxFormulaTree() {
        List<List<? extends VTAuxTreeNode>> newFormulaTree = new ArrayList<>();
        if (formulaTree != null) {
            for (int i = 0; i < formulaTree.size(); i++) newFormulaTree.add(formulaTree.get(i));
        }
        return newFormulaTree;
    }

    public String getObjLangName() { return objLangName; }
    public boolean isCheckFinal() { return checkFinal; }
}