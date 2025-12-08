package slapp.editor.m_parse;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.grammatical_parts.TermType;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MParseUtilities {

    private MLanguage mLanguage = new MLanguage();
    private Language objLanguage;

    public List<Expression> parseObj(Document doc, String objLangName) {
        objLanguage = Languages.getLanguage(objLangName);
        List<OriginalElement> elements = ParseUtilities.getElements(doc);
        List<Expression> expressions = new ArrayList<>();
        for (OriginalElement element : elements) {
            expressions.add(element);
        }
        ParseUtilities.setLanguage(Languages.getLanguage(objLangName));
        expressions = getBasicObjectTerms(expressions);

        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (elementStr.equals(mLanguage.getmOpenSquareBracket())) {
                    expressions.set(i, new MOpenSquareBracket(elementStr));
                } else if (elementStr.equals(mLanguage.getmCloseSquareBracket())) {
                    expressions.set(i, new MCloseSquareBracket(elementStr));
                }
            }
        }

        return expressions;

    }


    public List<Expression> parseDoc(Document doc, String objLangName) {
        objLanguage = Languages.getLanguage(objLangName);
        List<OriginalElement> elements = ParseUtilities.getElements(doc);
        List<Expression> expressions = new ArrayList<>();
        for (OriginalElement element : elements) {
            expressions.add(element);
        }
        ParseUtilities.setLanguage(Languages.getLanguage(objLangName));
        expressions = getSimpleVariables(expressions);    //regular parse does not identify subscripted variables from variable assignment
        expressions = getSimpleObjects(expressions);  //vertical bar and others
        expressions = getInterpVariables(expressions);
        expressions = getAssignmentVariables(expressions);

        expressions = getBasicObjectTerms(expressions);
        expressions = getAssignmentMods(expressions);
        expressions = getAssignments(expressions);
        expressions = getInterpAssign(expressions);
        expressions = ParseUtilities.parseExpressions(expressions);


        expressions = getRemainingSimpleObjects(expressions);
        expressions = getInterpObjects(expressions);
        expressions = getVarAssignmentObjects(expressions);
        expressions = getInterpAsssignObjects(expressions);
        expressions = getUniverse(expressions);
        expressions = getRelationTuples(expressions);
        expressions = getFunctionPairs(expressions);
        expressions = getRelations(expressions);
        expressions = getFunctions(expressions);


        return expressions;
    }







    private List<Expression> getVarAssignmentObjects(List<Expression> expressions) {
        for (int i = 0; i + 3 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_ASSIGNMENT) {
                MAssignment varAssignment = (MAssignment) expressions.get(i);
                if (expressions.get(i + 2).getType() == ExpressionType.TERM && ((Term) expressions.get(i + 2)).getTermType() == TermType.VARIABLE) {
                    Term variableTerm = (Term) expressions.get(i + 2);
                    MOpenSquareBracket openBracket = (MOpenSquareBracket) expressions.get(i + 1);
                    MCloseSquareBracket closeBracket = (MCloseSquareBracket) expressions.get(i + 3);
                    expressions.set(i, new MAssignVariable(varAssignment, variableTerm, openBracket, closeBracket));
                    expressions.remove(i + 1);
                    expressions.remove(i + 1);
                    expressions.remove(i + 1);
                }
            }
        }
        return expressions;
    }

    private List<Expression> getInterpAsssignObjects(List<Expression> expressions) {
        for (int i = 0; i + 3 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_INTERP_ASSIGNMENT &&
                    expressions.get(i+1) instanceof MOpenSquareBracket && (expressions.get(i+2).getType() == ExpressionType.TERM || expressions.get(i+2).getType() == ExpressionType.FORMULA) &&  expressions.get(i+3) instanceof MCloseSquareBracket) {
                MInterpretationAssignment interpAssignment = (MInterpretationAssignment) expressions.get(i);
                MOpenSquareBracket openBracket = (MOpenSquareBracket) expressions.get(i + 1);
                MCloseSquareBracket closeBracket = (MCloseSquareBracket) expressions.get(i + 3);
                boolean replace = false;
                if (expressions.get(i + 2).getType() == ExpressionType.FORMULA) {
                    Formula formula = (Formula) expressions.get(i + 2);
                    expressions.set(i, new MInterpAssignFormula(interpAssignment, formula, openBracket, closeBracket));
                    replace = true;
                }
                else if (expressions.get(i + 2).getType() == ExpressionType.TERM) {
                    Term term = (Term) expressions.get(i + 2);
                    expressions.set(i, new MInterpAssignTerm(interpAssignment, term, openBracket, closeBracket));
                    replace = true;
                }
                if (replace) {
                    expressions.remove(i + 1);
                    expressions.remove(i + 1);
                    expressions.remove(i + 1);
                }
            }
        }
        return expressions;
    }


    private List<Expression> getInterpObjects(List<Expression> expressions) {
        for (int i = 0; i + 3 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_INTERPRETATION_VARIABLE) {
                MInterpretationVariable iVariable = (MInterpretationVariable) expressions.get(i);
                if (expressions.get(i + 1).getType() == ExpressionType.M_OPEN_SQUARE_BRACKET && expressions.get(i + 3).getType() == ExpressionType.M_CLOSE_SQUARE_BRACKET) {
                    MOpenSquareBracket openBracket = (MOpenSquareBracket) expressions.get(i + 1);
                    MCloseSquareBracket closeBracket = (MCloseSquareBracket) expressions.get(i + 3);
                    boolean replace = false;
                    if (expressions.get(i + 2).getType() == ExpressionType.FORMULA) {
                        Formula formula = (Formula) expressions.get(i + 2);
                        if (SyntacticalFns.sentence(formula, objLanguage.getNameString())) {
                            expressions.set(i, new MInterpFormula(iVariable, formula, openBracket, closeBracket));
                            replace = true;
                        }
                    } else if (expressions.get(i + 2).getType() == ExpressionType.RELATION_SYMBOL) {
                        RelationSymbol relationSymbol = (RelationSymbol) expressions.get(i + 2);
                        expressions.set(i, new MInterpRelation(iVariable, relationSymbol, openBracket, closeBracket));
                        replace = true;
                    } else if (expressions.get(i + 2).getType() == ExpressionType.FUNCTION_SYMBOL) {
                        FunctionSymbol functionSymbol = (FunctionSymbol) expressions.get(i + 2);
                        expressions.set(i, new MInterpFunction(iVariable, functionSymbol, openBracket, closeBracket));
                        replace = true;
                    } else if (expressions.get(i + 2).getType() == ExpressionType.TERM && ((Term) expressions.get(i + 2)).getTermType() == TermType.CONSTANT) {
                        Term constantTerm = (Term) expressions.get(i + 2);
                        expressions.set(i, new MInterpConstant(iVariable, constantTerm, openBracket, closeBracket));
                        replace = true;
                    }
                    if (replace) {
                        expressions.remove(i + 1);
                        expressions.remove(i + 1);
                        expressions.remove(i + 1);
                    }
                }
            }
        }
        return expressions;
    }

    private List<Expression> getFunctionPairs(List<Expression> expressions) {
        for (int i = 0; i + 4 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_OPEN_ANGLE_BRACKET && isInnerAngleBracket(expressions, i)) {
                if (expressions.get(i + 1).getType() == ExpressionType.M_RELATION_TUPLE &&
                        expressions.get(i + 2).getType() == ExpressionType.COMMA_DIVIDER &&
                        expressions.get(i + 3) instanceof MBasicObjTerm &&
                        expressions.get(i + 4).getType() == ExpressionType.M_CLOSE_ANGLE_BRACKET) {
                    MOpenAngleBracket openBracket = (MOpenAngleBracket) expressions.get(i);
                    MRelTuple input = (MRelTuple) expressions.get(i + 1);
                    CommaDivider divider = (CommaDivider) expressions.get(i + 2);
                    MBasicObjTerm output = (MBasicObjTerm) expressions.get(i + 3);
                    MCloseAngleBracket closeBraccket = (MCloseAngleBracket) expressions.get(i + 4);
                    expressions.set(i, new MFnPair(input, output, openBracket, closeBraccket, divider));
                    expressions.remove(i + 1);
                    expressions.remove(i + 1);
                    expressions.remove(i + 1);
                    expressions.remove(i + 1);
                }
            }
        }

        return expressions;
    }


    private List<Expression> getRelationTuples(List<Expression> expressions) {
        for (int i = 0; i + 2 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_OPEN_ANGLE_BRACKET && isInnerAngleBracket(expressions, i)) {
                CommaDivider divider = null;
                List<MBasicObjTerm> members = new ArrayList<>();
                boolean ok = true;
                int j = 1;
                int bracketCount = 1;
                while (i + j < expressions.size() && bracketCount > 0) {

                    if (expressions.get(i + j).getType() == ExpressionType.M_CLOSE_ANGLE_BRACKET) {
                        bracketCount--;
                    }
                    if (bracketCount > 0) {
                        if (j % 2 == 1) {
                            if (!(expressions.get(i + j) instanceof MBasicObjTerm)) {
                                ok = false;
                                break;
                            }
                            else members.add((MBasicObjTerm) expressions.get(i + j));
                        }
                        if (j % 2 == 0) {
                            if (expressions.get(i + j).getType() != ExpressionType.COMMA_DIVIDER) {
                                ok = false;
                                break;
                            }
                            else divider = (CommaDivider) expressions.get(i + j);
                        }
                    }
                    j++;
                }
                if (ok && bracketCount == 0) {
                    MOpenAngleBracket openBracket = (MOpenAngleBracket) expressions.get(i);
                    MCloseAngleBracket closeBracket = (MCloseAngleBracket) expressions.get(i + j - 1);
                    MRelTuple relTuple = new MRelTuple(members, openBracket, closeBracket, divider );
                    expressions.set(i, relTuple);
                    int k = i + 1;
                    while (k < i + j) {
                        expressions.remove(i + 1);
                        k++;
                    }
                }
            }
        }

        return expressions;
    }

    private List<Expression> getRelations(List<Expression> expressions) {
        for (int i = 0; i + 2 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_OPEN_BRACE) {
                CommaDivider divider = null;
                Set<MRelTuple> members = new LinkedHashSet<>();
                int places = -1;
                boolean ok = true;
                int j = 1;
                int bracketCount = 1;
                while (i + j < expressions.size() && bracketCount > 0) {
                    if (expressions.get(i + j).getType() == ExpressionType.M_OPEN_BRACE) {
                        bracketCount++;
                    }
                    else if (expressions.get(i + j).getType() == ExpressionType.M_CLOSE_BRACE) {
                        bracketCount--;
                    }
                    if (bracketCount > 0) {
                        if (j % 2 == 1) {
                            if (!(expressions.get(i + j) instanceof MRelTuple)) {
                                ok = false;
                                break;
                            }
                            if (places < 0) {places = ((MRelTuple) expressions.get(i + j)).getPlaces();}
                            else if (((MRelTuple) expressions.get(i + j)).getPlaces() != places) {
                                ok = false;
                                break;
                            }
                            members.add((MRelTuple) expressions.get(i + j));
                        }
                        if (j % 2 == 0) {
                            if (expressions.get(i + j).getType() != ExpressionType.COMMA_DIVIDER) {
                                ok = false;
                                break;
                            }
                            else divider = (CommaDivider) expressions.get(i + j);
                        }
                    }
                    j++;
                }
                if (ok && bracketCount == 0) {
                    MOpenBrace openBrace = (MOpenBrace) expressions.get(i);
                    MCloseBrace closeBrace = (MCloseBrace) expressions.get(i + j - 1);
                    MRelation relation = new MRelation(members, places, openBrace, closeBrace, divider );
                    expressions.set(i, relation);
                    int k = i + 1;
                    while (k < i + j) {
                        expressions.remove(i + 1);
                        k++;
                    }
                }
            }
        }
        return expressions;
    }

    private List<Expression> getFunctions(List<Expression> expressions) {
        for (int i = 0; i + 2 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_OPEN_BRACE) {
                CommaDivider divider = null;
                Set<MFnPair> members = new LinkedHashSet<>();
                int places = -1;
                boolean ok = true;
                int j = 1;
                int bracketCount = 1;
                while (i + j < expressions.size() && bracketCount > 0) {
                    if (expressions.get(i + j).getType() == ExpressionType.M_OPEN_BRACE) {
                        bracketCount++;
                    }
                    else if (expressions.get(i + j).getType() == ExpressionType.M_CLOSE_BRACE) {
                        bracketCount--;
                    }
                    if (bracketCount > 0) {
                        if (j % 2 == 1) {
                            if (!(expressions.get(i + j) instanceof MFnPair)) {
                                ok = false;
                                break;
                            }
                            if (places < 0) {places = ((MFnPair) expressions.get(i + j)).getPlaces();}
                            else if (((MFnPair) expressions.get(i + j)).getPlaces() != places) {
                                ok = false;
                                break;
                            }
                            members.add((MFnPair) expressions.get(i + j));
                        }
                        if (j % 2 == 0) {
                            if (expressions.get(i + j).getType() != ExpressionType.COMMA_DIVIDER) {
                                ok = false;
                                break;
                            }
                            else divider = (CommaDivider) expressions.get(i + j);
                        }
                    }
                    j++;
                }
                if (ok && bracketCount == 0) {
                    MOpenBrace openBrace = (MOpenBrace) expressions.get(i);
                    MCloseBrace closeBrace = (MCloseBrace) expressions.get(i + j - 1);
                    MFunctionCandidate function = new MFunctionCandidate(members, places, openBrace, closeBrace, divider );
                    expressions.set(i, function);
                    int k = i + 1;
                    while (k < i + j) {
                        expressions.remove(i + 1);
                        k++;
                    }
                }
            }
        }
        return expressions;
    }

    private List<Expression> getUniverse(List<Expression> expressions) {
        for (int i = 0; i + 2 < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_OPEN_BRACE) {
                CommaDivider divider = null;
                Set<MBasicObjTerm> members = new LinkedHashSet<>();
                boolean ok = true;
                int j = 1;
                int bracketCount = 1;
                while (i + j < expressions.size() && bracketCount > 0) {
                    if (expressions.get(i + j).getType() == ExpressionType.M_OPEN_BRACE) {
                        bracketCount++;
                    }
                    else if (expressions.get(i + j).getType() == ExpressionType.M_CLOSE_BRACE) {
                        bracketCount--;
                    }
                    if (bracketCount > 0) {
                        if (j % 2 == 1) {
                            if (!(expressions.get(i + j) instanceof MBasicObjTerm)) {
                                ok = false;
                                break;
                            }
                            else members.add((MBasicObjTerm) expressions.get(i + j));
                        }
                        if (j % 2 == 0) {
                            if (expressions.get(i + j).getType() != ExpressionType.COMMA_DIVIDER) {
                                ok = false;
                                break;
                            }
                            else divider = (CommaDivider) expressions.get(i + j);
                        }
                    }
                    j++;
                }
                if (ok && bracketCount == 0 && !members.isEmpty()) {
                    MOpenBrace openBrace = (MOpenBrace) expressions.get(i);
                    MCloseBrace closeBrace = (MCloseBrace) expressions.get(i + j - 1);
                    MUniverse universe = new MUniverse(members, openBrace, closeBrace, divider );
                    expressions.set(i, universe);
                    int k = i + 1;
                    while (k < i + j) {
                        expressions.remove(i + 1);
                        k++;
                    }
                }
            }
        }
        return expressions;
    }


    private List<Expression> getAssignments(List<Expression> expressions) {

        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.M_ASSIGNMENT_VARIABLE) {
                MAssignment assignment = new MAssignment((MAssignmentVariable) expressions.get(i));
          //      expressions.set(i, assignment);

                CommaDivider divider = null;
                if (i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.M_OPEN_PARENTHESIS ) {
                    Set<MAssignmentMod> assignmentMods = new LinkedHashSet<>();
                    boolean ok = true;
                    int j = 2;
                    int bracketCount = 1;
                    while (i + j < expressions.size() && bracketCount > 0) {
                        if (expressions.get(i + j).getType() == ExpressionType.M_OPEN_PARENTHESIS) {
                            bracketCount++;
                        }
                        else if (expressions.get(i + j).getType() == ExpressionType.M_CLOSE_PARENTHESIS) {
                            bracketCount--;
                        }
                        if (bracketCount > 0) {
                            if (j % 2 == 1) {
                                if (expressions.get(i + j).getType() != ExpressionType.COMMA_DIVIDER) {
                                    ok = false;
                                    break;
                                }
                                else divider = (CommaDivider) expressions.get(i + j);
                            }
                            if (j % 2 == 0) {
                                if (expressions.get(i + j).getType() != ExpressionType.M_ASSIGNMENT_MOD) {
                                    ok = false;
                                    break;
                                }
                                else assignmentMods.add((MAssignmentMod) expressions.get(i + j));
                            }
                        }
                        j++;
                    }
                    if (ok && bracketCount == 0) {
                        assignment.setAssignmentMods(assignmentMods);
                        assignment.setOpenParenthesis((MOpenParenthesis) expressions.get(i + 1));
                        assignment.setCloseParenthesis((MCloseParenthesis) expressions.get(i + j - 1));
                        assignment.setCommaDivider(divider);

                        //don't want to delete unless assignment actually goes in (depending on following square brackets)
                        if (i + j < expressions.size() && expressions.get(i + j).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                            String elementStr = ((OriginalElement) expressions.get(i + j)).getElementStr();
                            if (mLanguage.getmOpenSquareBracket().equals(elementStr)) {
                                String close = mLanguage.getmCloseSquareBracket();
                                int matePos = findBracketMate(expressions, i + j, elementStr, close);
                                if (matePos != -1) {
                                    int k = i + 1;
                                    while (k < i + j) {
                                        expressions.remove(i + 1);
                                        k++;
                                    }
                                }
                            }
                        }

                        /*
                        int k = i + 1;
                        while (k < i + j) {
                            expressions.remove(i + 1);
                            k++;
                        }
                         */
                    }
                }
                if (i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT ) {
                    String elementStr = ((OriginalElement) expressions.get(i + 1)).getElementStr();
                    if (mLanguage.getmOpenSquareBracket().equals(elementStr)) {
                        String close = mLanguage.getmCloseSquareBracket();
                        int matePos = findBracketMate(expressions, i + 1, elementStr, close);
                        if (matePos != -1) {
                            expressions.set(i, assignment);
                            expressions.set(matePos, new MCloseSquareBracket(close));
                            expressions.set(i + 1, new MOpenSquareBracket(elementStr));
                        }
                    }
                }
            }
        }
        return expressions;
    }

    private List<Expression> getAssignmentMods(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (i + 2 < expressions.size() && expressions.get(i).getType() == ExpressionType.TERM &&
                    ((Term) expressions.get(i)).getTermType() == TermType.VARIABLE &&
                    expressions.get(i + 1).getType() == ExpressionType.M_VBAR_DIVIDER_SYM &&
                    expressions.get(i + 2) instanceof MBasicObjTerm) {
                expressions.set(i, new MAssignmentMod((Term) expressions.get(i), (MVerticalDividerSym) expressions.get(i + 1), (MBasicObjTerm) expressions.get(i + 2)));
                expressions.remove(i + 1);
                expressions.remove(i + 1);
            }
        }
        return expressions;
    }

    private List<Expression> getRemainingSimpleObjects(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.M_UNIVERSE_SYM) {
                MUniverseSym universeSym = (MUniverseSym) expressions.get(i);
                if (i + 1 < expressions.size()) {
                    if (expressions.get(i + 1).getType() == ExpressionType.M_INTERPRETATION_VARIABLE) {
                        expressions.set(i, new MInterpUniv(universeSym.toString(), (MInterpretationVariable) expressions.get(i + 1)));
                        expressions.remove(i + 1);
                    }
                }
            }
        }
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (elementStr.equals(mLanguage.getTrueSym())) {
                    expressions.set(i, new MTheTrue(elementStr));
                }
                else if (elementStr.equals(mLanguage.getFalseSym())) {
                    expressions.set(i, new MTheFalse(elementStr));
                }
            }
        }

        return expressions;
    }

    private List<Expression> getSimpleObjects(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (elementStr.equals(mLanguage.getmVerticalDivider())) {
                    expressions.set(i, new MVerticalDividerSym(elementStr));
                }
                else if (elementStr.equals(objLanguage.getCommaDivider())) {
                    expressions.set(i, new CommaDivider(elementStr));
                }

                else if (elementStr.equals(mLanguage.getmOpenBrace())) {
                    if (i + 1 < expressions.size()) {
                        if (expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                            String nextStr = ((OriginalElement) expressions.get(i + 1)).getElementStr();
                            if (nextStr.equals(mLanguage.getmCloseBrace())) {
                                expressions.set(i, new MEmptySet(elementStr, nextStr));
                                expressions.remove(i + 1);
                                continue;
                            }
                        }
                    }
                }


                if (elementStr.equals(mLanguage.getmOpenBrace())) {
                    expressions.set(i, new MOpenBrace(elementStr));
                }
                else if (elementStr.equals(mLanguage.getmCloseBrace())) {
                    expressions.set(i, new MCloseBrace(elementStr));
                }
                else if (elementStr.equals(mLanguage.getmOpenAngleBracket())) {
                    expressions.set(i, new MOpenAngleBracket(elementStr));
                }
                else if (elementStr.equals(mLanguage.getmCloseAngleBracket())) {
                    expressions.set(i, new MCloseAngleBracket(elementStr));
                }
                else if (elementStr.equals(mLanguage.getEqualsSym())) {
                    expressions.set(i, new MEquals(elementStr));
                }
                else if (elementStr.equals(mLanguage.getmUniverseSym())) {
                    if (i + 1 < expressions.size()) {
                        if (expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                            String nextStr = ((OriginalElement) expressions.get(i  + 1)).getElementStr();
                            if (mLanguage.getInterpretationVariables() != null && mLanguage.getInterpretationVariables().contains(nextStr)) {
                                expressions.set(i, new MInterpUniv(elementStr, new MInterpretationVariable(nextStr)));
                                expressions.remove(i + 1);
                            }
                        }
                    }
                }

            }
        }
        return expressions;
    }

    //a simple variable is a subscript or normal without a subscript
    private List<Expression> getSimpleVariables(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                OriginalElement element = (OriginalElement) expressions.get(i);
                elementStr = element.getElementStr();
                if (objLanguage.getVariables() != null && objLanguage.getVariables().contains(elementStr)) {
                    boolean process = false;
                    if (element.isSubscript()) process = true;
                    if (element.isNormal() &&
                            (i + 1 == expressions.size() || !(i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i + 1)).isSubscript()))) {
                        process = true;
                    }
                    if (process) {
                        VariableSym variableSym = new VariableSym(elementStr, "");
                        Term variableTerm = new Term();
                        variableTerm.setLevel(0);
                        ArrayList children = new ArrayList();
                        children.add(variableSym);
                        variableTerm.setChildren(children);
                        variableTerm.setCombines(true);
                        variableTerm.setTermType(TermType.VARIABLE);
                        expressions.set(i, variableTerm);
                    }
                }
            }
        }
        return expressions;
    }

    private List<Expression> getInterpAssign(List<Expression> expressions) {
        for (int i = 0; i + 1 < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.M_INTERPRETATION_VARIABLE && expressions.get(i + 1).getType() == ExpressionType.M_ASSIGNMENT) {
                MInterpretationVariable variable = (MInterpretationVariable) expressions.get(i);
                MAssignment assignment = (MAssignment) expressions.get(i + 1);
                MInterpretationAssignment interpAssign = new MInterpretationAssignment(variable, assignment);
                expressions.set(i, interpAssign);
                expressions.remove(i + 1);
                /*
                if (i + 1 < expressions.size()) {
                    if (expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                        elementStr = ((OriginalElement) expressions.get(i + 1)).getElementStr();
                        if (mLanguage.getmOpenSquareBracket().equals(elementStr)) {
                            String close = mLanguage.getmCloseSquareBracket();
                            int matePos = findBracketMate(expressions, i + 1, elementStr, close);
                            if (matePos != -1) {
                                expressions.set(matePos, new MCloseSquareBracket(close));
                                expressions.set(i + 1, new MOpenSquareBracket(elementStr));
                            }
                        }
                    }
                }
                 */
            }
        }

        return expressions;
    }

    private List<Expression> getInterpVariables(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String ielementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                ielementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (mLanguage.getInterpretationVariables() != null && mLanguage.getInterpretationVariables().contains(ielementStr)) {
         //           expressions.set(i, new MInterpretationVariable(ielementStr));
                    if (i + 1 < expressions.size()) {
                        if (expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                            String belementStr = ((OriginalElement) expressions.get(i + 1)).getElementStr();
                            if (mLanguage.getmOpenSquareBracket().equals(belementStr)) {
                                String close = mLanguage.getmCloseSquareBracket();
                                int matePos = findBracketMate(expressions, i + 1, belementStr, close);
                                if (matePos != -1) {
                                    expressions.set(i, new MInterpretationVariable(ielementStr));
                                    expressions.set(matePos, new MCloseSquareBracket(close));
                                    expressions.set(i + 1, new MOpenSquareBracket(belementStr));
                                }
                            }
                        }
                        if (expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                            OriginalElement nextElement = (OriginalElement) expressions.get(i + 1);
                            String nextElementStr = nextElement.getElementStr();
                            if (mLanguage.getAssignmentVariables().contains(nextElementStr) && nextElement.isSubscript()) {
                                expressions.set(i, new MInterpretationVariable(ielementStr));
                            }
                        }
                    }
                }
            }
        }
        return expressions;
    }

    private List<Expression> getAssignmentVariables(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (mLanguage.getAssignmentVariables() != null && mLanguage.getAssignmentVariables().contains(elementStr)) {
    //                expressions.set(i, new MAssignmentVariable(elementStr));
                    if (i + 1 < expressions.size()) {
                        if (expressions.get(i + 1).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                            String belementStr = ((OriginalElement) expressions.get(i + 1)).getElementStr();
                            if (mLanguage.getmOpenSquareBracket().equals(belementStr)) {
                                expressions.set(i, new MAssignmentVariable(elementStr));
                            }
                            /*
                            if (mLanguage.getmOpenSquareBracket().equals(elementStr)) {
                                String close = mLanguage.getmCloseSquareBracket();
                                int matePos = findBracketMate(expressions, i + 1, elementStr, close );
                                if (matePos != -1) {
                                    expressions.set(matePos, new MCloseSquareBracket(close));
                                    expressions.set(i + 1, new MOpenSquareBracket(elementStr));
                                }
                            }
                             */
                            if (mLanguage.getmOpenParenthesis().equals(belementStr)) {
                                String close = mLanguage.getmCloseParenthesis();
                                int matePos = findBracketMate(expressions, i + 1, belementStr, close );
                                if (matePos != -1) {
                                    expressions.set(i, new MAssignmentVariable(elementStr));
                                    expressions.set(matePos, new MCloseParenthesis(close));
                                    expressions.set(i + 1, new MOpenParenthesis(belementStr));
                                }
                            }
                        }
                    }
                }
            }
        }

        return expressions;
    }

    private boolean isInnerAngleBracket(List<Expression> expressions, int pos) {
        int count = 1;
        if (pos + 1 < expressions.size()) {
            for (int i = pos + 1; i < expressions.size(); i++) {
                if (expressions.get(i).getType() == ExpressionType.M_OPEN_ANGLE_BRACKET) {
                    break;
                }
                if (expressions.get(i).getType() == ExpressionType.M_CLOSE_ANGLE_BRACKET) {
                    count--;
                    break;
                }
            }
        }
        return count == 0;
    }

    private int findBracketMate(List<Expression> expressions, int pos, String open, String close) {
        int count = 1;
        while (pos < expressions.size() && count > 0) {
            pos++;
            if (pos < expressions.size() && expressions.get(pos).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                OriginalElement element = (OriginalElement) expressions.get(pos);
                if (element.getElementStr().equals(open)) { count++; }
                if (element.getElementStr().equals(close)) { count--; }
            }
        }
        if (pos < expressions.size()) return pos;
        else return -1;
    }

    private List<Expression> getBasicObjectTerms(List<Expression> expressions) {

        //proper names
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (mLanguage.getNameUppercase().contains(elementStr) ) {
                    String nameStr = getNameStr(i, elementStr, expressions);
                    if (nameStr.codePointCount(0, nameStr.length()) > 1) {
                        expressions.set(i, new MProperName(nameStr));
                    }
                }
                if (mLanguage.getNameDigits().contains(elementStr)) {
                    String numString = getNumStr(i, elementStr, expressions);
                    expressions.set(i, new MProperName(numString));
                }
            }
        }

        //object variables
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (mLanguage.getBasicObjectVariables().contains(elementStr)) {
                    expressions.set(i, new MObjectVariable(elementStr));
                }
            }
        }


        return expressions;
    }

    private String getNumStr(int j, String firstDigit, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstDigit);
        j++;
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT && mLanguage.getNameDigits().contains(((OriginalElement) expressions.get(j)).getElementStr())) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }

    private String getNameStr(int j, String firstSymbol, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstSymbol);
        boolean sub = ((OriginalElement) expressions.get(j)).isSubscript();
        j++;
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                mLanguage.getNameAlphabet().contains(((OriginalElement) expressions.get(j)).getElementStr()) &&
                ((OriginalElement) expressions.get(j)).isSubscript() == sub) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }


}
