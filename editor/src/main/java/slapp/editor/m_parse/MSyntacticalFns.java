package slapp.editor.m_parse;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.m_parse.m_grammatical_parts.*;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.main_window.MainWindow;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.grammatical_parts.TermType;
import slapp.editor.parser.symbols.CommaDivider;
import slapp.editor.parser.symbols.ConstantSym;
import slapp.editor.parser.symbols.FunctionSymbol;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MSyntacticalFns {
    MainWindow mainWindow;
    List<Expression> expressions;
    String objlangName;
    Language objectLanguage;
    private MLanguage mLanguage = new MLanguage();
    private boolean sententialInterp = false;
    private MInterpretations interpretations = new MInterpretations();

    private MVarAssign baseVarAssign = new MVarAssign();
    private MParseUtilities mParseUtilities = new MParseUtilities();
    private FormulaPacket formulaPacket;


    public MSyntacticalFns(MainWindow mainWindow) {
        this.mainWindow = mainWindow;

    }

    //to be run only after setInterpBaseVarAssign on main doc

    public FormulaPacket setupFormulaPacket(BoxedDRTA bdrta) {
        if (setFormulaPacket(bdrta)) return formulaPacket;
        else return null;
    }

    public boolean setFormulaPacket(BoxedDRTA bdrta) {
        RichTextArea rta = bdrta.getRTA();
        rta.getActionFactory().saveNow().execute(new ActionEvent());
        Document doc = rta.getDocument();
        List<Expression> expList = mParseUtilities.parseDoc(doc, objlangName);
        if (expList.size() == 0) {
            bdrta.setVTtreeBoxHighlight();
            mainWindow.getMainView().scrollToNode(bdrta.getRTA());
            EditorAlerts.showSimpleTxtListAlert("Parse Issue", Collections.singletonList(ParseUtilities.newRegularText("Empty field.")));
            bdrta.resetVTtreeBoxHighlight();
            return false;
        }
        if (expList.size() > 1 || expList.get(0).getType() != ExpressionType.M_INTERP_ASSIGN_FORMULA) {
            bdrta.setVTtreeBoxHighlight();
            mainWindow.getMainView().scrollToNode(bdrta.getRTA());
            List<Text> texts = new ArrayList<>();
            texts.add(ParseUtilities.newRegularText("Parser Contents:\n"));
            for (Expression expr : expList) {
                texts.add(new Text(expr.getType() + ": "));
                texts.addAll(expr.toTextList());
                texts.add(new Text("\n"));
            }
            EditorAlerts.showSimpleTxtListAlert("Parse Issue", texts);
            bdrta.resetVTtreeBoxHighlight();
            return false;
        }
        else {
            MInterpAssignFormula interpAssignFormula = (MInterpAssignFormula) expList.get(0);
            Formula formula = interpAssignFormula.getFormula();
            if (formula == null) {
                bdrta.setVTtreeBoxHighlight();
                mainWindow.getMainView().scrollToNode(bdrta.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Missing Element", Collections.singletonList(ParseUtilities.newRegularText("Missing formula")));
                bdrta.resetVTtreeBoxHighlight();
                return false;
            }
            MInterpretationAssignment mInterpAssign = interpAssignFormula.getInterpAssign();
            MInterpretation interpretation = interpretations.getInterpretation(mInterpAssign.getInterpVariable());
            if (interpretation == null) {
                bdrta.setVTtreeBoxHighlight();
                mainWindow.getMainView().scrollToNode(bdrta.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Missing Element", Collections.singletonList(ParseUtilities.newRegularText("Interpretation does not exist.")));
                bdrta.resetVTtreeBoxHighlight();
                return false;
            }

            MAssignment assignments = mInterpAssign.getAssignment();
            formulaPacket = new FormulaPacket(interpretation, formula);
            formulaPacket.setInterpretationVariable(mInterpAssign.getInterpVariable());
            formulaPacket.setAssignmentVariable(mInterpAssign.getAssignment().getAssignmentVariable());

            if (!sententialInterp && interpretation.getmUniverse() == null) {
                bdrta.setVTtreeBoxHighlight();
                mainWindow.getMainView().scrollToNode(bdrta.getRTA());
                EditorAlerts.showSimpleTxtListAlert("Missing Universe", Collections.singletonList(ParseUtilities.newRegularText("A variable assignment exists only relative to a (stated) interpretation and non-empty universe.")));
                bdrta.resetVTtreeBoxHighlight();
                return false;
            }
            Map<Term, MBasicObjTerm> baseAssignments = baseVarAssign.getAssignment(mInterpAssign.getAssignment().getAssignmentVariable());
            for (Map.Entry<Term, MBasicObjTerm> entry : baseAssignments.entrySet()) {
                formulaPacket.putAssignment(entry.getKey(), entry.getValue());
            }
            Iterator<MAssignmentMod> mods = assignments.getAssignmentMods().iterator();
            while (mods.hasNext()) {
                MAssignmentMod mod = mods.next();
                MBasicObjTerm object = mod.getObject();
                if (!interpretation.getmUniverse().getUniverse().contains(object)) {
                    bdrta.setVTtreeBoxHighlight();
                    mainWindow.getMainView().scrollToNode(bdrta.getRTA());
                    List<Text> textList = new ArrayList<>();
                    textList.add(ParseUtilities.newRegularText("The universe of interpretation "));
                    textList.addAll(mInterpAssign.getInterpVariable().toTextList());
                    textList.add(ParseUtilities.newRegularText(" does not include the object "));
                    textList.addAll(object.toTextList());
                    textList.add(ParseUtilities.newRegularText(" assigned to variable "));
                    textList.addAll(mod.getVariable().toTextList());
                    textList.add(ParseUtilities.newRegularText("."));
                    EditorAlerts.showSimpleTxtListAlert("Assignment Error:", textList);
                    bdrta.resetVTtreeBoxHighlight();
                    return false;
                }
                else {
                    formulaPacket.putAssignment(mod.getVariable(), mod.getObject());
                }
            }
    //        System.out.println(formulaPacket);
            return true;
        }
    }


    public boolean setInterpBaseVarAssign(Document doc, String objLangName) {
        this.objlangName = objLangName;
        objectLanguage = Languages.getLanguage(objlangName);
        expressions = mParseUtilities.parseDoc(doc, objLangName);
        if (!setAssignmentSentences()) return false;


        interpretations = new MInterpretations();
        baseVarAssign = new MVarAssign();
        if (!containsNBar(doc.getText())) {
            if (!setInterp(expressions)) return false;
        }
        else {
            getNBarInterp();
        }

        if (!setBaseVarAssign(expressions)) return false;


        return true;
    }

    public boolean containsNBar(String str) {
        Pattern p = Pattern.compile("interpretation \ue3bd", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(str);
        return matcher.find();
    }

    private boolean setBaseVarAssign(List<Expression> expressions) {
        for (Expression exp : expressions) {
            if (exp.getType() == ExpressionType.M_VASSIGN_VARIABLE_ASSIGN) {
                MVAssignVariableAssign mvAssignVariableAssign = (MVAssignVariableAssign) exp;
                MAssignVariable assignVar = mvAssignVariableAssign.getAssignVariable();
                Term variableTerm = mvAssignVariableAssign.getAssignVariable().getVariableTerm();
                MBasicObjTerm basicObject = mvAssignVariableAssign.getBasicObj();

                if (interpretations.getMap().size() == 0) {
                    EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", Collections.singletonList(ParseUtilities.newRegularText("A variable assignment requires an interpretation and universe for that interpretation (as \ud835\uddb4\ud835\udda8 = {\ud835\udfe3, \ud835\udfe4}).")));
                    return false;
                }

                //this universe check works only in case there is a single interpretation - in case more than one recheck with Id
                if (interpretations.getMap().size() == 1) {
                    Map.Entry<MInterpretationVariable, MInterpretation> entry = interpretations.getMap().entrySet().iterator().next();
                    MInterpretationVariable interpVar = entry.getKey();
                    MInterpretation interp = entry.getValue();

                    if (!sententialInterp && interpretations.getInterpretation(interpVar).getmUniverse() == null) {
                        showMissingUniverseMessage();
                        return false;
                    }
                    else {
                        if (interp != null && !interp.getmUniverse().getUniverse().contains(basicObject)) {
                            List<Text> textList = new ArrayList<>();
                            textList.add(ParseUtilities.newRegularText("The universe of interpretation "));
                            textList.addAll(interpVar.toTextList());
                            textList.add(ParseUtilities.newRegularText(" does not include the object "));
                            textList.addAll(basicObject.toTextList());
                            textList.add(ParseUtilities.newRegularText(" assigned to variable "));
                            textList.addAll(variableTerm.toTextList());
                            textList.add(ParseUtilities.newRegularText("."));
                            EditorAlerts.showSimpleTxtListAlert("Assignment Error:", textList);
                            return false;
                        }
                        else baseVarAssign.addVariable(assignVar.getVariableAssignment().getAssignmentVariable(), variableTerm, basicObject);
                    }
                }
                else baseVarAssign.addVariable(assignVar.getVariableAssignment().getAssignmentVariable(), variableTerm, basicObject);
            }
        }
        return true;
    }

    private boolean setInterp(List<Expression> expressions) {
        boolean isUniverse = false;
        for (Expression exp : expressions) {
            if (exp.getType() == ExpressionType.M_UNIVERSE_ASSIGN) {
                MUniverseAssign mUniverseAssign = (MUniverseAssign) exp;
                interpretations.getInterpretation(mUniverseAssign.getInterpUniv().getInterpVar()).addUniverse(mUniverseAssign.getUniverseSet());
                isUniverse = true;
            }
        }
        if (!sententialInterp && !isUniverse) {
            EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", Collections.singletonList(ParseUtilities.newRegularText("A quantificational interpretation must have a non-empty universe.")));
            return false;
        }

        for (Expression exp : expressions) {
            if (exp.getType() == ExpressionType.M_INTERP_SENTENCE_ASSIGN) {
                MInterpSentenceAssign mInterpSentenceAssign = (MInterpSentenceAssign) exp;
                Formula formula = mInterpSentenceAssign.getInterpFormula().getFormula();
                interpretations.getInterpretation(mInterpSentenceAssign.getInterpFormula().getInterpVar()).addFormula(formula, mInterpSentenceAssign.getTruthValue());
            }

            if (exp.getType() == ExpressionType.M_INTERP_CONSTANT_ASSIGN) {
                MInterpConstantAssign mInterpConstantAssign = (MInterpConstantAssign) exp;
                MInterpretationVariable interpVar = mInterpConstantAssign.getInterpConstant().getInterpVar();
                if (!sententialInterp && interpretations.getInterpretation(interpVar).getmUniverse() == null) {
                    showMissingUniverseMessage();
                    return false;
                }
                else {
                    Term constantTerm = mInterpConstantAssign.getInterpConstant().getConstantTerm();
                    MBasicObjTerm basicObjet = mInterpConstantAssign.getBasicObject();
                    MInterpretation interp = interpretations.getInterpretation(interpVar);
                    if (!interp.getmUniverse().getUniverse().contains(mInterpConstantAssign.getBasicObject())) {
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("The universe of interpretation "));
                        textList.addAll(interpVar.toTextList());
                        textList.add(ParseUtilities.newRegularText(" does not include the object "));
                        textList.addAll(basicObjet.toTextList());
                        textList.add(ParseUtilities.newRegularText(" assigned to constant "));
                        textList.addAll(constantTerm.toTextList());
                        textList.add(ParseUtilities.newRegularText("."));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", textList);
                        return false;
                    }
                    else interp.addConstant(constantTerm, basicObjet);
                }
            }

            if (exp.getType() == ExpressionType.M_INTERP_RELATION_ASSIGN) {
                MInterpRelationAssign mInterpRelationAssign = (MInterpRelationAssign) exp;
                MInterpretationVariable interpVar = mInterpRelationAssign.getInterpRelation().getInterpVar();
                if (!sententialInterp && interpretations.getInterpretation(interpVar).getmUniverse() == null) {
                    showMissingUniverseMessage();
                    return false;
                }
                else {
                    RelationSymbol relSymbol = mInterpRelationAssign.getInterpRelation().getRelationSymbol();
                    MInterpretation interp = interpretations.getInterpretation(interpVar);
                    MRelation relation = mInterpRelationAssign.getRelation();
                    Set<MRelTuple> relationTuples = relation.getTuples();
                    for (MRelTuple relTuple : relationTuples) {
                        List<MBasicObjTerm> members = relTuple.getMembers();
                        for (MBasicObjTerm member : members) {
                            if (!interp.getmUniverse().getUniverse().contains(member)) {
                                List<Text> textList = new ArrayList<>();
                                textList.add(ParseUtilities.newRegularText("The universe of interpretation "));
                                textList.addAll(interpVar.toTextList());
                                textList.add(ParseUtilities.newRegularText(" does not include all the objects from the relation assigned to "));
                                textList.addAll(relSymbol.toTextList());
                                textList.add(ParseUtilities.newRegularText("."));
                                EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", textList);
                                return false;
                            }
                        }
                    }

                    interp.addRelation(relSymbol, relation);
                }
            }

            else if (exp.getType() == ExpressionType.M_INTERP_FUNCTION_ASSIGN) {
                MInterpFunctionAssign mInterpFunctionAssign = (MInterpFunctionAssign) exp;
                MInterpretationVariable interpVar = mInterpFunctionAssign.getInterpFunction().getInterpVar();
                if (!sententialInterp && interpretations.getInterpretation(interpVar).getmUniverse() == null) {
                    showMissingUniverseMessage();
                    return false;
                }
                else {
                    FunctionSymbol functionSymbol = mInterpFunctionAssign.getInterpFunction().getFunctionSymbol();
                    MInterpretation interp = interpretations.getInterpretation(interpVar);
                    MFunctionCandidate funcCandidate = mInterpFunctionAssign.getFunction();
                    Set <MFnPair> functionPairs = funcCandidate.getPairs();
                    List<List<MBasicObjTerm>> inputObjects = new ArrayList<>();
                    for (MFnPair pair : functionPairs) {
                        inputObjects.add(pair.getInput().getMembers());
                    }

                    //check members
                    boolean allInUniv = true;
                    for (MFnPair pair : functionPairs) {
                        if (!interp.getmUniverse().getUniverse().contains(pair.getOutput())) {
                            allInUniv = false;
                            break;
                        }
                    }
                    if (allInUniv) {
                        bigLoop:
                        for (List<MBasicObjTerm> inputObject : inputObjects) {
                            for (MBasicObjTerm objTerm : inputObject) {
                                if (!interp.getmUniverse().getUniverse().contains(objTerm)) {
                                    allInUniv = false;
                                    break bigLoop;
                                }
                            }
                        }
                    }
                    if (!allInUniv) {
                        List<Text> textList = new ArrayList<>();
                        textList.add(ParseUtilities.newRegularText("The universe of interpretation "));
                        textList.addAll(interpVar.toTextList());
                        textList.add(ParseUtilities.newRegularText(" does not include all the objects from the function assigned to "));
                        textList.addAll(functionSymbol.toTextList());
                        textList.add(ParseUtilities.newRegularText("."));
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", textList);
                        return false;
                    }

                    //check total function
                    List<MBasicObjTerm> universeList = new ArrayList<>(interp.getmUniverse().getUniverse());
                    List<List<MBasicObjTerm>> allTuples = generateNTuples(universeList, funcCandidate.getPlaces());
                    for (List<MBasicObjTerm> tuple : allTuples) {
                        if (!inputObjects.contains(tuple)) {
                            List<Text> textList = new ArrayList<>();
                            textList.add(ParseUtilities.newRegularText("The function "));
                            textList.addAll(funcCandidate.toTextList());
                            textList.add(ParseUtilities.newRegularText(" assigned to "));
                            textList.addAll(functionSymbol.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is not total and so cannot be an element of an interpretation."));
                            EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", textList);
                            return false;
                        }
                    }
                    interp.addFunction(functionSymbol, funcCandidate);
                }

            }
        }

        //check equality
        if (!sententialInterp) {
            Map<MInterpretationVariable, MInterpretation> interps = interpretations.getMap();
            for (Map.Entry<MInterpretationVariable, MInterpretation> entry : interps.entrySet()) {
                MInterpretation interpretation = entry.getValue();

                Set<MBasicObjTerm> universe = interpretation.getmUniverse().getUniverse();
                RelationSymbol equalsSym = new RelationSymbol("\ue8ac", "", "", 2);
                MRelation equalsRel = interpretation.getRelationVal(equalsSym);
                if (universe != null && equalsRel != null) {
                    Set<MRelTuple> relTuples = equalsRel.getTuples();

                    boolean bad = false;
                    if (universe.size() != relTuples.size()) {
                        bad = true;
                    }
                    for (MBasicObjTerm term : universe) {
                        List<MBasicObjTerm> termPair = new ArrayList<>();
                        termPair.add(term);
                        termPair.add(term);
                        CommaDivider commaDivider = new CommaDivider(objectLanguage.getCommaDivider());
                        MRelTuple pairTuple = new MRelTuple(termPair, new MOpenAngleBracket(mLanguage.getmOpenAngleBracket()), new MCloseAngleBracket(mLanguage.getmCloseAngleBracket()), commaDivider);
                        if (!relTuples.contains(pairTuple)) {
                            bad = true;
                            break;
                        }
                    }
                    if (bad) {
                        EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", Collections.singletonList(ParseUtilities.newRegularText("The interpretation of the equals symbol must be \u27e8\u2134, \u2134\u27e9 for each member \u2134 of the universe.")));
                        return false;
                    }
                }
            }
        }


        return true;
    }

    private List<List<MBasicObjTerm>> generateNTuples(List<MBasicObjTerm> set, int n) {
        List<List<MBasicObjTerm>> result = new ArrayList<>();

        if (n == 0) {
            result.add(new ArrayList<>());
            return result;
        }

        for (MBasicObjTerm objTerm : set) {
            List<List<MBasicObjTerm>> subTuples  = generateNTuples(set, n - 1);
            for (List<MBasicObjTerm> subTuple : subTuples) {
                List<MBasicObjTerm> newTuple = new ArrayList<>(subTuple);
                newTuple.add(0, objTerm);
                result.add(newTuple);
            }
        }
        return result;
    }



    private void showMissingUniverseMessage() {
        EditorAlerts.showSimpleTxtListAlert("Interpretation Error:", Collections.singletonList(ParseUtilities.newRegularText("A quantificational interpretation requires specification of a universe for the interpretation (as \ud835\uddb4\ud835\udda8 = {\ud835\udfe3, \ud835\udfe4}).")));

    }

    private boolean setAssignmentSentences() {
        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_EQUALS) {
                MEquals mEquals = (MEquals) expressions.get(i);
                if (i > 0 && i + 1 < expressions.size()) {
                    Expression left = expressions.get(i - 1);
                    Expression right = expressions.get(i + 1);

                    if (sententialInterp) {
                        if (left.getType() == ExpressionType.M_INTERP_FORMULA) {
                            MInterpFormula interpFormula = (MInterpFormula) left;
                            Formula formula = interpFormula.getFormula();
                            if (formula.isAtomic() && SyntacticalFns.sentence(formula, objlangName)) {
                                if (right instanceof MTruthValue) {
                                    MTruthValue mTruthValue = (MTruthValue) right;
                                    if (mTruthValue.getType() == ExpressionType.M_THE_TRUE)
                                        expressions.set(i - 1, new MInterpSentenceAssign(interpFormula, (MTheTrue) right, mEquals));
                                    else
                                        expressions.set(i - 1, new MInterpSentenceAssign(interpFormula, (MTheFalse) right, mEquals));
                                    expressions.remove(i);
                                    expressions.remove(i);
                                }
                            }
                        }
                    } else {
                        if (left.getType() == ExpressionType.M_INTERP_UNIVERSE) {
                            MInterpUniv interpUniv = (MInterpUniv) left;
                            if (right instanceof MUniverse) {
                                MUniverse univ = (MUniverse) right;
                                expressions.set(i - 1, new MUniverseAssign(interpUniv, univ, mEquals));
                                expressions.remove(i);
                                expressions.remove(i);
                            }
                        } else if (left.getType() == ExpressionType.M_INTERP_FORMULA) {
                            MInterpFormula interpFormula = (MInterpFormula) left;
                            Formula formula = interpFormula.getFormula();
                            if (formula.isAtomic() && SyntacticalFns.sentence(formula, objlangName)) {
                                if (right instanceof MTruthValue) {
                                    MTruthValue mTruthValue = (MTruthValue) right;
                                    if (mTruthValue.getType() == ExpressionType.M_THE_TRUE)
                                        expressions.set(i - 1, new MInterpSentenceAssign(interpFormula, (MTheTrue) right, mEquals));
                                    else
                                        expressions.set(i - 1, new MInterpSentenceAssign(interpFormula, (MTheFalse) right, mEquals));
                                    expressions.remove(i);
                                    expressions.remove(i);
                                }
                            }
                        } else if (left.getType() == ExpressionType.M_INTERP_CONSTANT) {
                            MInterpConstant interpConstant = (MInterpConstant) left;
                            if (right instanceof MBasicObjTerm) {
                                MBasicObjTerm basicObjTerm = (MBasicObjTerm) right;
                                expressions.set(i - 1, new MInterpConstantAssign(interpConstant, basicObjTerm, mEquals));
                                expressions.remove(i);
                                expressions.remove(i);
                            }
                        } else if (left.getType() == ExpressionType.M_INTERP_RELATION) {
                            MInterpRelation interpRelation = (MInterpRelation) left;
                            int places = interpRelation.getRelationSymbol().getPlaces();
                            if (right.getType() == ExpressionType.M_RELATION) {
                                MRelation mRelation = (MRelation) right;
                                if (places == mRelation.getPlaces()) {
                                    expressions.set(i - 1, new MInterpRelationAssign(interpRelation, mRelation, mEquals));
                                    expressions.remove(i);
                                    expressions.remove(i);
                                }
                            } else if (right.getType() == ExpressionType.M_UNIVERSE && places == 1) {
                                MUniverse mUniverse = (MUniverse) right;
                                Set<MRelTuple> oneTuples = new LinkedHashSet<>();
                                Set<MBasicObjTerm> univ = mUniverse.getUniverse();
                                Iterator<MBasicObjTerm> iterator = univ.iterator();
                                CommaDivider commaDivider = new CommaDivider(objectLanguage.getCommaDivider());
                                while (iterator.hasNext()) {
                                    List oneList = Arrays.asList(iterator.next());
                                    oneTuples.add(new MRelTuple(oneList, new MOpenAngleBracket(mLanguage.getmOpenAngleBracket()), new MCloseAngleBracket(mLanguage.getmCloseAngleBracket()), commaDivider));
                                }
                                MRelation relation = new MRelation(oneTuples, 1, new MOpenBrace(mLanguage.getmOpenBrace()), new MCloseBrace(mLanguage.getmCloseBrace()), commaDivider);
                                expressions.set(i - 1, new MInterpRelationAssign(interpRelation, relation, mEquals));
                                expressions.remove(i);
                                expressions.remove(i);
                            } else if (right.getType() == ExpressionType.M_EMPTY_SET) {
                                Set<MRelTuple> tuples = new LinkedHashSet<>();
                                CommaDivider commaDivider = new CommaDivider(objectLanguage.getCommaDivider());
                                MRelation relation = new MRelation(tuples, places, new MOpenBrace(mLanguage.getmOpenBrace()), new MCloseBrace(mLanguage.getmCloseBrace()), commaDivider);
                                expressions.set(i - 1, new MInterpRelationAssign(interpRelation, relation, mEquals));
                                expressions.remove(i);
                                expressions.remove(i);
                            }
                        } else if (left.getType() == ExpressionType.M_INTERP_FUNCTION) {
                            MInterpFunction interpFunction = (MInterpFunction) left;
                            int places = interpFunction.getFunctionSymbol().getPlaces();
                            if (right.getType() == ExpressionType.M_FUNCTION_CANDIDATE) {
                                MFunctionCandidate mFunctionCandidate = (MFunctionCandidate) right;
                                if (places == mFunctionCandidate.getPlaces() && mFunctionCandidate.isFunction()) {
                                    expressions.set(i - 1, new MInterpFunctionAssign(interpFunction, mFunctionCandidate, mEquals));
                                    expressions.remove(i);
                                    expressions.remove(i);
                                }
                            } else if (right.getType() == ExpressionType.M_RELATION) {
                                MRelation mRelation = (MRelation) right;
                                if (places == 1 && mRelation.getPlaces() == 2) {
                                    Set<MFnPair> newFunctionPairs = new LinkedHashSet<>();
                                    Set<MRelTuple> relationPairs = mRelation.getTuples();
                                    Iterator<MRelTuple> iterator = relationPairs.iterator();
                                    CommaDivider commaDivider = new CommaDivider(objectLanguage.getCommaDivider());
                                    MOpenAngleBracket openAngleBracket = new MOpenAngleBracket(mLanguage.getmOpenAngleBracket());
                                    MCloseAngleBracket closeAngleBracket = new MCloseAngleBracket(mLanguage.getmCloseAngleBracket());
                                    while (iterator.hasNext()) {
                                        MRelTuple relationTuple = iterator.next();
                                        List<MBasicObjTerm> singletonList = Collections.singletonList(relationTuple.getMembers().get(0));
                                        MRelTuple singleton = new MRelTuple(singletonList, openAngleBracket, closeAngleBracket, commaDivider);
                                        MFnPair fnPair = new MFnPair(singleton, relationTuple.getMembers().get(1), openAngleBracket, closeAngleBracket, commaDivider);
                                        newFunctionPairs.add(fnPair);
                                    }
                                    MFunctionCandidate newFunction = new MFunctionCandidate(newFunctionPairs, 1, new MOpenBrace(mLanguage.getmOpenBrace()), new MCloseBrace(mLanguage.getmCloseBrace()), commaDivider);
                                    if (newFunction.isFunction()) {
                                        expressions.set(i - 1, new MInterpFunctionAssign(interpFunction, newFunction, mEquals));
                                        expressions.remove(i);
                                        expressions.remove(i);
                                    } else {
                                        expressions.set(i + 1, newFunction);
                                    }
                                }
                            }
                        } else if (left.getType() == ExpressionType.M_ASSIGN_VARIABLE) {
                            MAssignVariable assignVariable = (MAssignVariable) left;
                            if (assignVariable.getVariableAssignment().getAssignmentMods().size() == 0) {
                                if (right instanceof MBasicObjTerm) {
                                    if (right.getType() == ExpressionType.M_BASIC_OBJ_VAR)
                                        expressions.set(i - 1, new MVAssignVariableAssign(assignVariable, (MObjectVariable) right, mEquals));
                                    if (right.getType() == ExpressionType.M_PROPER_NAME)
                                        expressions.set(i - 1, new MVAssignVariableAssign(assignVariable, (MProperName) right, mEquals));
                                    expressions.remove(i);
                                    expressions.remove(i);

                                }
                            }
                        }
                    }
                }
            }
        }
            //check for errors
        for (int i = 0; i < expressions.size(); i++) {
            List<Text> textList = new ArrayList<>();
            boolean error = false;
            if (expressions.get(i).getType() == ExpressionType.M_EQUALS) {
                MEquals mEquals = (MEquals) expressions.get(i);
                if (i > 0 && i + 1 < expressions.size()) {
                    Expression left = expressions.get(i - 1);
                    Expression right = expressions.get(i + 1);

                    if (sententialInterp) {
                        if (left.getType() == ExpressionType.M_INTERP_FORMULA) {
                            MInterpFormula interpFormula = (MInterpFormula) left;
                            Formula formula = interpFormula.getFormula();
                            if (formula.isAtomic() && SyntacticalFns.sentence(formula, objlangName)) {
                                textList.add(ParseUtilities.newRegularText("The interpretation of atomic sentence, "));
                                textList.addAll(interpFormula.toTextList());
                                textList.add(ParseUtilities.newRegularText(" is a truth value.\n\nFor \""));
                                textList.addAll(right.toTextList());
                                textList.add(ParseUtilities.newRegularText(" . . . \" expect truth value (\ud835\uddb3/\ud835\udda5)."));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            } else {
                                textList.add(ParseUtilities.newRegularText("The interpretation of a non-atomic sentence, "));
                                textList.addAll(interpFormula.toTextList());
                                textList.add(ParseUtilities.newRegularText(" is not an element of the interpretation.\n"));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            }
                        } else if (right instanceof MTruthValue) {
                            MTruthValue truthValue = (MTruthValue) right;
                            textList.add(ParseUtilities.newRegularText("Truth value "));
                            textList.addAll(truthValue.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is the interpretation of an atomic sentence.\n\n"));
                            textList.add(ParseUtilities.newRegularText("For \" . . . "));
                            textList.addAll(right.toTextList());
                            textList.add(ParseUtilities.newRegularText(" \" expect expression of the sort, \ud835\udda8[\ud835\udc9c] for interpretaton \ud835\udda8 and atomic sentence \ud835\udc9c of " + objlangName + ".\n"));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        } else {
                            textList.add(ParseUtilities.newRegularText("I cannot parse \" . . . "));
                            textList.addAll(left.toTextList());
                            textList.add(new Text(" "));
                            textList.addAll(mEquals.toTextList());
                            textList.add(new Text(" "));
                            textList.addAll(right.toTextList());
                            textList.add(ParseUtilities.newRegularText(" . . . \" into element of a sentential interpretation (as \ud835\udda8[\ud835\udc9c] " + mEquals.toString() + "𝖳/𝖥 (for interpretation \ud835\udda8 and atomic sentence \ud835\udc9c of " + objlangName + ")."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        }
                    }

                    // not sentential
                    else {

                        //universe
                        if (left.getType() == ExpressionType.M_INTERP_UNIVERSE) {
                            MInterpUniv interpUniv = (MInterpUniv) left;
                            textList.add(ParseUtilities.newRegularText("The universe for interpretation "));
                            textList.addAll(interpUniv.getInterpVar().toTextList());
                            textList.add(ParseUtilities.newRegularText((" is a nonempty set of objects.\n\nFor \"")));
                            textList.addAll(right.toTextList());
                            textList.add(ParseUtilities.newRegularText(" . . . \" expace a universe (set of basic objects)."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        } else if (right.getType() == ExpressionType.M_UNIVERSE) {
                            MUniverse universe = (MUniverse) right;
                            textList.addAll(universe.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is a universe for some interpretation (or the interpretation of a 1-place relation symbol).\n\n For \" . . . "));
                            textList.addAll(left.toTextList());
                            textList.add(ParseUtilities.newRegularText("\" expect expression of the sort \ud835\uddb4\ud835\udda8 or \ud835\udda8[\u211b"));
                            textList.add(ParseUtilities.newSuperscriptText("1"));
                            textList.add(ParseUtilities.newRegularText("] for some interpretation \ud835\udda8 or relation symbol of " + objlangName + "."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        }

                        //atomic
                        else if (left.getType() == ExpressionType.M_INTERP_FORMULA) {
                            MInterpFormula interpFormula = (MInterpFormula) left;
                            Formula formula = interpFormula.getFormula();
                            if (formula.isAtomic() && SyntacticalFns.sentence(formula, objlangName)) {
                                textList.add(ParseUtilities.newRegularText("The interpretation of atomic sentence, "));
                                textList.addAll(interpFormula.toTextList());
                                textList.add(ParseUtilities.newRegularText(" is a truth value.\n\nFor \""));
                                textList.addAll(right.toTextList());
                                textList.add(ParseUtilities.newRegularText(" . . . \" expect truth value (\ud835\uddb3/\ud835\udda5)."));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            } else {
                                textList.add(ParseUtilities.newRegularText("The interpretation of a non-atomic sentence, "));
                                textList.addAll(interpFormula.toTextList());
                                textList.add(ParseUtilities.newRegularText(" is not an element of an interpretation."));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            }
                        } else if (right instanceof MTruthValue) {
                            MTruthValue truthValue = (MTruthValue) right;
                            textList.add(ParseUtilities.newRegularText("Truth value "));
                            textList.addAll(truthValue.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is the interpretation of an atomic sentence.\n\n"));
                            textList.add(ParseUtilities.newRegularText("For \" . . . "));
                            textList.addAll(left.toTextList());
                            textList.add(ParseUtilities.newRegularText("\" expect expression of the sort, \ud835\udda8[\ud835\udc9c] (for interpretaton \ud835\udda8 and atomic sentence \ud835\udc9c of " + objlangName + ")."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        }


                        //constant / variable
                        else if (left.getType() == ExpressionType.M_INTERP_CONSTANT) {
                            MInterpConstant interpConstant = (MInterpConstant) left;
                            textList.add(ParseUtilities.newRegularText("The interpretation of a constant, "));
                            textList.addAll(interpConstant.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is a basic object.\n\nFor \""));
                            textList.addAll(right.toTextList());
                            textList.add(ParseUtilities.newRegularText(" . . . \" expect a basic object (as \ud835\udfe3 or \ud835\udda1\ud835\uddc8\ud835\uddbb)."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        } else if (left.getType() == ExpressionType.M_ASSIGN_VARIABLE) {
                            MAssignVariable assignVariable = (MAssignVariable) left;
                            if (assignVariable.getVariableAssignment().getAssignmentMods().size() == 0) {
                                textList.add(ParseUtilities.newRegularText("The assignment to a variable, "));
                                textList.addAll(assignVariable.toTextList());
                                textList.add(ParseUtilities.newRegularText(" is a basic object.\n\nFor \""));
                                textList.addAll(right.toTextList());
                                textList.add(ParseUtilities.newRegularText(" . . . \" expect basic object (as \ud835\udfe3 or \ud835\udda1\ud835\uddc8\ud835\uddbb)."));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            } else {
                                textList.add(ParseUtilities.newRegularText("The assignment, "));
                                textList.addAll(assignVariable.toTextList());
                                textList.add(ParseUtilities.newRegularText(" is not a basic part of a basic variable assignment.\n"));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            }
                        } else if (right instanceof MBasicObjTerm) {
                            MBasicObjTerm basicObjTerm = (MBasicObjTerm) right;
                            textList.add(ParseUtilities.newRegularText("The basic object "));
                            textList.addAll(basicObjTerm.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is the interpretation of a constant, or the assignment to a variable.\n\n"));

                            textList.add(ParseUtilities.newRegularText("For \" . . . "));
                            textList.addAll(left.toTextList());
                            textList.add(ParseUtilities.newRegularText("\" expect expression of the sort, \ud835\udda8[\ud835\udcb8] or \ud835\uddbd[\ud835\udccb] for interpretaton \ud835\udda8 or variable assignment \ud835\uddbd and language " + objlangName + "."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        }

                        //relation
                        else if (left.getType() == ExpressionType.M_INTERP_RELATION) {
                            MInterpRelation interpRelation = (MInterpRelation) left;
                            int places = interpRelation.getRelationSymbol().getPlaces();
                            textList.add(ParseUtilities.newRegularText("The interpretation of a relation symbol, "));
                            textList.addAll(interpRelation.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is a " + places + "-place relation.\n\nFor \""));
                            textList.addAll(right.toTextList());
                            textList.add(ParseUtilities.newRegularText(" . . . \" expect a " + places + "-place relation (a set of " + places + "-tuples.)"));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        } else if (right.getType() == ExpressionType.M_RELATION) {
                            MRelation relation = (MRelation) right;
                            int places = relation.getPlaces();
                            textList.addAll(relation.toTextList());
                            if (places == 2) {
                                textList.add(ParseUtilities.newRegularText(" is the interpretation of a " + places + "-place relation symbol (or a 1-place function symbol).\n\n"));
                                textList.add(ParseUtilities.newRegularText("For \" . . . "));
                                textList.addAll(left.toTextList());
                                textList.add(ParseUtilities.newRegularText("\" expect an expression of the sort \ud835\udda8[\u211b"));
                                textList.add(ParseUtilities.newSuperscriptText("2"));
                                textList.add(ParseUtilities.newRegularText("] or \ud835\udda8[\ud835\udcbb"));
                                textList.add(ParseUtilities.newSuperscriptText("1"));
                                textList.add(ParseUtilities.newRegularText("] for a relation symbol of language " + objlangName + " and some interpretation \ud835\udda8."));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            } else {
                                textList.add(ParseUtilities.newRegularText(" is the interpretation of a " + places + "-place relation symbol.\n\n"));
                                textList.add(ParseUtilities.newRegularText("For \" . . . "));
                                textList.addAll(left.toTextList());
                                textList.add(ParseUtilities.newRegularText("\" expect expression of the sort \ud835\udda8[\u211b"));
                                textList.add(ParseUtilities.newSuperscriptText(Integer.toString(places)));
                                textList.add(ParseUtilities.newRegularText("] for a relation symbol of language " + objlangName + " and some interpretation \ud835\udda8."));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            }
                        }
                        else if (right.getType() == ExpressionType.M_EMPTY_SET) {
                            MEmptySet emptySet = (MEmptySet) right;
                            textList.addAll(emptySet.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is the interpretation of relation symbol.\n\n"));
                            textList.add(ParseUtilities.newRegularText("For \" . . . "));
                            textList.addAll(left.toTextList());
                            textList.add(ParseUtilities.newRegularText("\" expect expression of the sort \ud835\udda8[\u211b] for some interpretation \ud835\udda8 and \ud835\udc5b-place relation symbol of language " + objlangName + "."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        }

                        //function
                        else if (right.getType() == ExpressionType.M_FUNCTION_CANDIDATE) {
                            MFunctionCandidate function = (MFunctionCandidate) right;
                            int places = function.getPlaces();
                            if (!function.isFunction()) {
                                textList.addAll(function.toTextList());
                                textList.add(ParseUtilities.newRegularText(" is \"function-like\" in that it pairs " + places + "-place inputs with outputs.  But it is not a function because it pairs some one input with different outputs.\n"));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            } else {
                                textList.addAll(function.toTextList());
                                textList.add(ParseUtilities.newRegularText("is the interpretation of a " + places + "-place function symbol.\n\n"));
                                textList.add(ParseUtilities.newRegularText("For \" . . . "));
                                textList.addAll(left.toTextList());
                                textList.add(ParseUtilities.newRegularText("\" expect expression of the sort \ud835\udda8[\ud835\udcbb"));
                                textList.add(ParseUtilities.newSuperscriptText(Integer.toString(places)));
                                textList.add(ParseUtilities.newRegularText("] for some function symbol of language " + objlangName + " and interpretation \ud835\udda8."));
                                EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                                return false;
                            }
                        } else if (left.getType() == ExpressionType.M_INTERP_FUNCTION) {
                            MInterpFunction interpFunction = (MInterpFunction) left;
                            int places = interpFunction.getFunctionSymbol().getPlaces();
                            textList.add(ParseUtilities.newRegularText("The interpretation of a function symbol, "));
                            textList.addAll(interpFunction.toTextList());
                            textList.add(ParseUtilities.newRegularText(" is a " + places + "-place function.\n\nFor \""));
                            textList.addAll(right.toTextList());
                            textList.add(ParseUtilities.newRegularText(" . . . \" expect a function (a set of pairs whose first member is a " + places + "-tuple."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        } else {
                            textList.add(ParseUtilities.newRegularText("Cannot parse \" . . . "));
                            textList.addAll(left.toTextList());
                            textList.add(new Text(" "));
                            textList.addAll(mEquals.toTextList());
                            textList.add(new Text(" "));
                            textList.addAll(right.toTextList());
                            textList.add(ParseUtilities.newRegularText(" . . . \" into element of a quantificational interpretation."));
                            EditorAlerts.showSimpleTxtListAlert("Parse Error", textList);
                            return false;
                        }
                    }
                }

            }
        }
        return true;

    }

    private void getNBarInterp() {
        int size = 100;
        MInterpretation nBarInterp = interpretations.getNBarInterpretation(new MInterpretationVariable("\ue3bd"));
        MOpenBrace openBrace = new MOpenBrace(mLanguage.getmOpenBrace());
        MCloseBrace closeBrace = new MCloseBrace(mLanguage.getmCloseBrace());
        MOpenAngleBracket openAngleBracket = new MOpenAngleBracket(mLanguage.getmOpenAngleBracket());
        MCloseAngleBracket closeAngleBracket = new MCloseAngleBracket(mLanguage.getmCloseAngleBracket());
        CommaDivider commaDivider = new CommaDivider(objectLanguage.getCommaDivider());

        //universe
        Set<MBasicObjTerm> uSet = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            uSet.add(new MProperName(stringNumberConverter(String.valueOf(i))));
        }
        MUniverse universe = new MUniverse(uSet, openBrace, closeBrace, commaDivider);
        nBarInterp.setmUuniverse(universe);

        //constant
        ConstantSym zeroSym = new ConstantSym("\ue886", "");
        Term zeroTerm = new Term();
        zeroTerm.setLevel(0);
        List<Expression> children = Collections.singletonList(zeroSym);
        zeroTerm.setChildren(children);
        zeroTerm.setCombines(true);
        zeroTerm.setTermType(TermType.CONSTANT);

        Map<Term, MBasicObjTerm> constantInterp = new HashMap<>();
        constantInterp.put(zeroTerm, new MProperName(stringNumberConverter(String.valueOf(0))));
        nBarInterp.setConstantInterp(constantInterp);

        //relation symbols
        Map<RelationSymbol, MRelation> relationInterp = new LinkedHashMap<>();

        RelationSymbol equalsSym = new RelationSymbol("\ue8ac", "", "", 2);
        Set<MRelTuple> eqTuples = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            MBasicObjTerm element = new MProperName(stringNumberConverter(String.valueOf(i)));
            List<MBasicObjTerm> member = List.of(element, element);
            MRelTuple eqTuple = new MRelTuple(member, openAngleBracket, closeAngleBracket, commaDivider );
            eqTuples.add(eqTuple);
        }
        MRelation eqRelation = new MRelation(eqTuples, 2, openBrace, closeBrace, commaDivider);
        relationInterp.put(equalsSym, eqRelation);

        RelationSymbol lessSym = new RelationSymbol("\ue8a4", "", "", 2);
        Set<MRelTuple> lessTuples = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            MBasicObjTerm element1 = new MProperName(stringNumberConverter(String.valueOf(i)));
            for (int j = i + 1; j < size; j++) {
                MBasicObjTerm element2 = new MProperName(stringNumberConverter(String.valueOf(j)));
                List<MBasicObjTerm> lmember = List.of(element1, element2);
                MRelTuple lessTuple = new MRelTuple(lmember, openAngleBracket, closeAngleBracket, commaDivider);
                lessTuples.add(lessTuple);
            }
        }
        MRelation lessRelation = new MRelation(lessTuples, 2, openBrace, closeBrace, commaDivider);
        relationInterp.put(lessSym, lessRelation);
        nBarInterp.setRelationInterp(relationInterp);

        //function symbols
        Map<FunctionSymbol, MFunctionCandidate> functionInterp = new LinkedHashMap<>();

        FunctionSymbol sucSym = new FunctionSymbol("\ud835\udc46", "", "", 1, false);
        Set<MFnPair> sucPairs = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            MBasicObjTerm element1 = new MProperName(stringNumberConverter(String.valueOf(i)));
            List inputList = List.of(element1);
            MRelTuple inputTuple = new MRelTuple(inputList, openAngleBracket, closeAngleBracket, commaDivider);
            MBasicObjTerm element2 = new MProperName(stringNumberConverter(String.valueOf((i + 1) % size)));
            MFnPair fnPair = new MFnPair(inputTuple, element2, openAngleBracket, closeAngleBracket, commaDivider);
            sucPairs.add(fnPair);
        }
        MFunctionCandidate sucCandidate = new MFunctionCandidate(sucPairs, 1, openBrace, closeBrace, commaDivider);
        functionInterp.put(sucSym, sucCandidate);

        FunctionSymbol plusSym = new FunctionSymbol("\ue8b8", "", "", 2, true);
        Set<MFnPair> plusPairs = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Boolean stringNumberConverteeString;
                List pInputList = List.of(new MProperName(stringNumberConverter(String.valueOf(i))), new MProperName(stringNumberConverter(String.valueOf(j))));
                MRelTuple pInputTuple = new MRelTuple(pInputList, openAngleBracket, closeAngleBracket, commaDivider);
                MBasicObjTerm pOutput = new MProperName(stringNumberConverter(String.valueOf((i + j) % size)));
                MFnPair pPair = new MFnPair(pInputTuple, pOutput, openAngleBracket, closeAngleBracket, commaDivider);
                plusPairs.add(pPair);
            }
        }
        MFunctionCandidate plusCandidate = new MFunctionCandidate(plusPairs, 2, openBrace, closeBrace, commaDivider);
        functionInterp.put(plusSym, plusCandidate);

        FunctionSymbol timesSym = new FunctionSymbol("\ue8ba", "", "", 2, true);
        Set<MFnPair> timesPairs = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                List tInputList = List.of(new MProperName(stringNumberConverter(String.valueOf(i))), new MProperName(stringNumberConverter(String.valueOf(j))));
                MRelTuple tInputTuple = new MRelTuple(tInputList, openAngleBracket, closeAngleBracket, commaDivider);
                MBasicObjTerm tOutput = new MProperName(stringNumberConverter(String.valueOf((i * j) % size)));
                MFnPair tPair = new MFnPair(tInputTuple, tOutput, openAngleBracket, closeAngleBracket, commaDivider);
                timesPairs.add(tPair);
            }
        }
        MFunctionCandidate timesCandidate = new MFunctionCandidate(timesPairs, 2, openBrace, closeBrace, commaDivider);
        functionInterp.put(timesSym, timesCandidate);
        nBarInterp.setFunctionInterp(functionInterp);

    }

    private String stringNumberConverter(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '0') sb.append("\ud835\udfe2");
            else if (c == '1') sb.append("\ud835\udfe3");
            else if (c == '2') sb.append("\ud835\udfe4");
            else if (c == '3') sb.append("\ud835\udfe5");
            else if (c == '4') sb.append("\ud835\udfe6");
            else if (c == '5') sb.append("\ud835\udfe7");
            else if (c == '6') sb.append("\ud835\udfe8");
            else if (c == '7') sb.append("\ud835\udfe9");
            else if (c == '8') sb.append("\ud835\udfea");
            else if (c == '9') sb.append("\ud835\udfeb");
        }
        return sb.toString();
    }




    public MInterpretations getInterpretations() {
        return interpretations;
    }

    public void setSententialInterp(boolean sententialInterp) {
        this.sententialInterp = sententialInterp;
    }

    public FormulaPacket getFormulaPacket() {
        return formulaPacket;
    }

    public MVarAssign getBaseVarAssign() {
        return baseVarAssign;
    }
}
