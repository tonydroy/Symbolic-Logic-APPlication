package slapp.editor.parser;

import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParseUtilities {
    private static Language language;
    private static int maxTermLevel;
    private static double baseFontSize = 12;
    private static boolean matchVal = false;

    public ParseUtilities() {}

    public static List<Expression> parseDoc(Document doc, String langName) {
        language = Languages.getLanguage(langName);
        List<OriginalElement> elements = getElements(doc);
        List<Expression> expressions = new ArrayList<>();
        for (OriginalElement element : elements) {
            expressions.add(element);
        }

        return parseExpressions(expressions);
    }

    public static List<Expression> parseExpressions(List<Expression> expressions) {
        maxTermLevel = 0;

        List<Expression> simpleSymbols = getSimpleSymbols(expressions);
        List<Expression> termSymbols = getTermSymbols(simpleSymbols);
        List<Expression> terms = getTerms(termSymbols);
        List<Expression> relSentSymbols = getRelSentSymbols(terms);

        List<Expression> operators = getOperators(relSentSymbols);
        List<Expression> atomics = getAtomics(operators);
        List<Expression> complexFormulas = getComplexFormulas(atomics);
        return complexFormulas;
    }

    public static List<Expression> getComplexFormulas(List<Expression> expressions) {

        boolean changes = true;
        while (changes) {
            changes = false;

            //unary
            boolean unaryChanges = true;
            while (unaryChanges) {
                unaryChanges = false;
                for (int i = 0; i < expressions.size(); i++) {
                    if (expressions.get(i) instanceof Operator && ((Operator) expressions.get(i)).isUnary()) {
                        Operator operator = (Operator) expressions.get(i);
                        if (i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.FORMULA && ((Formula) expressions.get(i + 1)).isCombines()) {
                            Formula formula = (Formula) expressions.get(i + 1);
                            Formula newFormula = new Formula();
                            newFormula.setChildren(Collections.singletonList(formula));
                            newFormula.setLevel(formula.getLevel() + 1);
                            newFormula.setMainOperator(operator);
                            expressions.set(i, newFormula);
                            expressions.remove(i + 1);
                            unaryChanges = true;
                            break;
                        }
                    }
                }
            }

            //binary
            for (int i = 0; i < expressions.size(); i++) {
                if (expressions.get(i) instanceof Operator && !((Operator) expressions.get(i)).isUnary()) {
                    Operator operator = (Operator) expressions.get(i);
                    if (i - 1 >= 0 && expressions.get(i - 1).getType() == ExpressionType.FORMULA && ((Formula) expressions.get(i - 1)).isCombines()) {
                        if (i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.FORMULA && ((Formula) expressions.get(i + 1)).isCombines()) {
                            Formula formula1 = (Formula) expressions.get(i - 1);
                            Formula formula2 = (Formula) expressions.get(i + 1);
                            Formula newFormula = new Formula();
                            newFormula.setChildren(Arrays.asList(formula1, formula2));
                            newFormula.setLevel(Math.max(formula1.getLevel(), formula2.getLevel()) + 1);
                            newFormula.setMainOperator(operator);
                            if (i - 2 >= 0 && i + 2 < expressions.size() &&
                                    ((expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET1 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET1) ||
                                            (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET2 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET2) ||
                                            (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET3 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET3))
                            ) {
                                newFormula.setOpenBracket((OpenBracket) expressions.get(i - 2));
                                newFormula.setCloseBracket((CloseBracket) expressions.get(i + 2));
                                expressions.set(i - 2, newFormula);
                                expressions.remove(i - 1);
                                expressions.remove(i - 1);
                                expressions.remove(i - 1);
                                expressions.remove(i - 1);
                                i = i - 1;
                                changes = true;
                                break;
                            } else {
                                if (language.isAllowDroppedBrackets()) {
                                    newFormula.setCombines(false);
                                    expressions.set(i - 1, newFormula);
                                    expressions.remove(i);
                                    expressions.remove(i);
                                    changes = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }


        /*

        //restricting formulas in bounded quantifiers

        if (language.isAllowBoundedQuantifiers()) {

            for (int i = expressions.size() - 1; i >= 0; i--) {
                if (expressions.get(i).getType() == ExpressionType.UNIV_RESTRICTED_OP) {

                    System.out.println("here");

                    UnivRestrictedQuantOp op = (UnivRestrictedQuantOp) expressions.get(i);
                    List<Expression> opAtomics = getAtomics(op.getRestrictorList());
                    List<Expression> opComplexFormulas = getComplexFormulas(opAtomics);
                    if (opComplexFormulas != null && opComplexFormulas.size() == 1 && opComplexFormulas.get(0).getType() == ExpressionType.FORMULA) {
                        op.setRestrictingFormula((Formula) opComplexFormulas.get(0));
                    }
                    else {
                        List<Expression> expressionList = new ArrayList<>();
                        expressionList.add(op.getOpenBracket());
                        expressionList.add(op.getUniversalOp());
                        expressionList.add(op.getDivider());
                        expressionList.addAll(opComplexFormulas);
                        expressionList.add(op.getCloseBracket());
                        expressions.remove(i);
                        expressions.addAll(i, expressionList);
                    }
                }
            }
        }

         */



        return expressions;
    }


    public static void isTreeNode(Expression exp, Expression match) {
 //       System.out.println("L: " + exp.getLevel() + " T: " + exp.getType() + " " + exp.toString());
        if (exp.equals(match)) {
 //           System.out.println("MATCH");
            matchVal = true;
        }
        if (exp.getChildren() != null && exp.getLevel() >= 0) {
            for (int i = 0; i < exp.getChildren().size(); i++) {
                isTreeNode(exp.getChildren().get(i), match);
            }
        }
    }



    public static List<Expression> getAtomics(List<Expression> expressions) {

        int i = 0;
        while (i < expressions.size()) {



            //sentence letter
            if (expressions.get(i).getType() == ExpressionType.SENTENCE_LETTER) {


                SentenceAtomc sentAtomic = new SentenceAtomc((SentenceLetter) expressions.get(i));
                sentAtomic.setLevel(maxTermLevel + 1);
                sentAtomic.setAtomic(true);
                sentAtomic.setCombines(true);
                expressions.set(i, sentAtomic);
                i = i + 1;
                continue;
            }

            //prefix atomic
            if (expressions.get(i).getType() == ExpressionType.RELATION_SYMBOL) {
                RelationSymbol relationSymbol = (RelationSymbol) expressions.get(i);

                int j = i + 1;
                if (areImmediateFollowingAtomicTerms(j, relationSymbol.getPlaces(), expressions)) {



                    List<Expression> children = new ArrayList<>();
                    for (int k = 0; k < relationSymbol.getPlaces(); k++) {
                        Term term = (Term) expressions.get(j);
                        expressions.remove(j);
                        children.add(term);
                    }
                    PrefixAtomic prefixAtomic = new PrefixAtomic(relationSymbol);
                    prefixAtomic.setChildren(children);
                    prefixAtomic.setLevel(maxTermLevel + 1);
                    prefixAtomic.setAtomic(true);
                    prefixAtomic.setCombines(true);
                    expressions.set(i, prefixAtomic);
                    i = i + 1;
                    continue;
                }
            }

            if (expressions.get(i).getType() == ExpressionType.COMPLEMENT_REL_SYM) {
                RelationSymbol relationSymbol = (RelationSymbol) expressions.get(i);

                int j = i + 1;
                if (areImmediateFollowingAtomicTerms(j, relationSymbol.getPlaces(), expressions)) {



                    List<Expression> children = new ArrayList<>();
                    for (int k = 0; k < relationSymbol.getPlaces(); k++) {
                        Term term = (Term) expressions.get(j);
                        expressions.remove(j);
                        children.add(term);
                    }
                    PrefixAtomic prefixAtomic = new PrefixAtomic(relationSymbol);
                    prefixAtomic.setChildren(children);
                    prefixAtomic.setLevel(maxTermLevel + 1);
                    prefixAtomic.setAtomic(true);
                    prefixAtomic.setCombines(true);

                    Formula negForm = new Formula();
                    negForm.setMainOperator(new NegationOp(new NegationSym(language.getNegation())));
                    negForm.setLevel(prefixAtomic.getLevel() + 1);
                    negForm.setChildren(Collections.singletonList(prefixAtomic));

                    expressions.set(i, negForm);
                    i = i + 1;
                    continue;
                }
            }

            //infix atomic
            if (expressions.get(i).getType() == ExpressionType.RELATION_SYMBOL) {
                RelationSymbol relationSymbol = (RelationSymbol) expressions.get(i);
                if (relationSymbol.isPermitInfix()) {
                    if (i - 1 >= 0 && expressions.get(i - 1).getType() == ExpressionType.TERM) {
                        if (i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.TERM) {

                            Term term1 = (Term) expressions.get(i - 1);
                            Term term2 = (Term) expressions.get(i + 1);
                            InfixAtomic infixAtomic;
                            boolean negating = false;
                            if (relationSymbol.getComplementSymbol() != null) negating = true;
                            infixAtomic = new InfixAtomic(relationSymbol, relationSymbol.getComplementSymbol(), negating);
                            infixAtomic.setLevel(maxTermLevel + 1);

                            infixAtomic.setAtomic(true);
                            infixAtomic.setCombines(true);
                            List<Expression> children = Arrays.asList(term1, term2);
                            infixAtomic.setChildren(children);
                            if (i - 2 >= 0 && i + 2 < expressions.size() &&
                                    ((expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET1 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET1) ||
                                    (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET2 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET2) ||
                                    (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET3 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET3))

                            ) {
                                infixAtomic.setOpenBracket((OpenBracket) expressions.get(i - 2));
                                infixAtomic.setCloseBracket((CloseBracket) expressions.get(i + 2));

                                expressions.set(i - 2, infixAtomic);
                                expressions.remove(i - 1);
                                expressions.remove(i - 1);
                                expressions.remove(i - 1);
                                expressions.remove(i - 1);

                                i = i - 1;
                                continue;
                            }
                            else {
                                expressions.set(i - 1, infixAtomic);
                                expressions.remove(i);
                                expressions.remove(i);

                                continue;
                            }
                        }
                    }
                }
            }

            if (expressions.get(i).getType() == ExpressionType.COMPLEMENT_REL_SYM) {
                RelationSymbol relationSymbol = (RelationSymbol) expressions.get(i);
                if (i - 1 >= 0 && expressions.get(i - 1).getType() == ExpressionType.TERM) {
                    if (i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.TERM) {



                        Term term1 = (Term) expressions.get(i - 1);
                        Term term2 = (Term) expressions.get(i + 1);
                        InfixAtomic infixAtomic = new InfixAtomic(relationSymbol, relationSymbol.getComplementSymbol(), true);
                        infixAtomic.setLevel(maxTermLevel + 1);
                        infixAtomic.setAtomic(true);
                        infixAtomic.setCombines(true);
                        List<Expression> children = Arrays.asList(term1, term2);
                        infixAtomic.setChildren(children);
                        if (i - 2 >= 0 && i + 2 < expressions.size() &&
                                ((expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET1 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET1) ||
                                (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET2 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET2) ||
                                (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET3 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET3))

                        ) {
                            infixAtomic.setOpenBracket((OpenBracket) expressions.get(i - 2));
                            infixAtomic.setCloseBracket((CloseBracket) expressions.get(i + 2));
                            Formula negForm = new Formula();
                            negForm.setMainOperator(new NegationOp(new NegationSym(language.getNegation())));
                            negForm.setLevel(infixAtomic.getLevel() + 1);
                            negForm.setChildren(Collections.singletonList(infixAtomic));
                            expressions.set(i - 2, negForm);
                            expressions.remove(i - 1);
                            expressions.remove(i - 1);
                            expressions.remove(i - 1);
                            expressions.remove(i - 1);
                            i = i - 1;
                            continue;
                        }
                        else {
                            Formula negForm = new Formula();
                            negForm.setMainOperator(new NegationOp(new NegationSym(language.getNegation())));
                            negForm.setLevel(infixAtomic.getLevel() + 1);
                            negForm.setChildren(Collections.singletonList(infixAtomic));
                            expressions.set(i - 1, negForm);
                            expressions.remove(i);
                            expressions.remove(i);
                            continue;
                        }
                    }
                }
            }
            i = i + 1;
        }

    return expressions;
    }


    public static List<Expression> getOperators(List<Expression> expressions) {

        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.NEG_SYM) {
                NegationOp neg = new NegationOp((NegationSym) expressions.get(i));
                expressions.set(i, neg);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.COND_SYM) {
                ConditionalOp cnd = new ConditionalOp((ConditionalSym) expressions.get(i));
                expressions.set(i, cnd);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.BICOND_SYM) {
                BiconditionalOp bcnd = new BiconditionalOp((BiconditionalSym) expressions.get(i));
                expressions.set(i, bcnd);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.CONJ_SYM) {
                ConjunctionOp op = new ConjunctionOp((ConjunctionSym) expressions.get(i));
                expressions.set(i, op);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.DISJ_SYM) {
                DisjunctionOp op = new DisjunctionOp((DisjunctionSym) expressions.get(i));
                expressions.set(i, op);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.NAND_SYM) {
                NandOp nand = new NandOp((NandSym) expressions.get(i));
                expressions.set(i, nand);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.NOR_SYM) {
                NorOp nor = new NorOp((NorSym) expressions.get(i));
                expressions.set(i, nor);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.UNIVERSAL_SYM && i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.TERM && ((Term) expressions.get(i + 1)).getTermType() == TermType.VARIABLE) {
                UniversalOp univ = new UniversalOp((UniversalQuantifierSym) expressions.get(i), (Term) expressions.get(i + 1));
                expressions.set(i, univ);
                expressions.remove(i + 1);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.EXISTENTIAL_SYM && i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.TERM && ((Term) expressions.get(i + 1)).getTermType() == TermType.VARIABLE) {
                ExistentialOp exis = new ExistentialOp((ExistentialQuantifierSym) expressions.get(i), (Term) expressions.get(i + 1));
                expressions.set(i, exis);
                expressions.remove(i + 1);
            }
        }

        //bounded quantifiers on separate sweep (after regular quantifiers identified)

        if (language.isAllowBoundedQuantifiers()) {
            matchVal = false;
            for (int i = 0; i < expressions.size(); i++) {
                if (i + 4 < expressions.size() &&
                        ((expressions.get(i).getType() == ExpressionType.OPEN_BRACKET1 && expressions.get(i + 4).getType() == ExpressionType.CLOSE_BRACKET1) ||
                                (expressions.get(i).getType() == ExpressionType.OPEN_BRACKET2 && expressions.get(i + 4).getType() == ExpressionType.CLOSE_BRACKET2) ||
                                (expressions.get(i).getType() == ExpressionType.OPEN_BRACKET3 && expressions.get(i + 4).getType() == ExpressionType.CLOSE_BRACKET3))
                ) {
                    if (expressions.get(i + 1).getType() == ExpressionType.UNIVERSAL_OP && expressions.get(i + 2).getType() == ExpressionType.RELATION_SYMBOL && ((RelationSymbol) expressions.get(i + 2)).isPermitInfix()
                            && expressions.get(i + 3).getType() == ExpressionType.TERM) {
                        isTreeNode(expressions.get(i + 3), ((UniversalOp) expressions.get(i + 1)).getVariableTerm());
                        if (!matchVal) {
                            UnivBoundedQuantOp op = new UnivBoundedQuantOp((OpenBracket) expressions.get(i), (CloseBracket) expressions.get(i + 4), (UniversalOp) expressions.get(i + 1),
                                    (RelationSymbol) expressions.get(i + 2), (Term) expressions.get(i + 3));
                            expressions.set(i, op);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                        }
                    } else if (expressions.get(i + 1).getType() == ExpressionType.EXISTENTIAL_OP && expressions.get(i + 2).getType() == ExpressionType.RELATION_SYMBOL && ((RelationSymbol) expressions.get(i + 2)).isPermitInfix()
                            && expressions.get(i + 3).getType() == ExpressionType.TERM) {
                        isTreeNode(expressions.get(i + 3), ((ExistentialOp) expressions.get(i + 1)).getVariableTerm());
                        if (!matchVal) {
                            ExisBoundedQuantOp op = new ExisBoundedQuantOp((OpenBracket) expressions.get(i), (CloseBracket) expressions.get(i + 4), (ExistentialOp) expressions.get(i + 1),
                                    (RelationSymbol) expressions.get(i + 2), (Term) expressions.get(i + 3));
                            expressions.set(i, op);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                        }
                    }
                }
            }

            //restricted quantifiers
            //come from end to process narrow scope first
            for (int i = expressions.size() - 1; i >= 0; i--) {
                if (expressions.get(i) instanceof OpenBracket && i + 2 < expressions.size() && (expressions.get(i+1).getType() == ExpressionType.UNIVERSAL_OP || expressions.get(i+1).getType() == ExpressionType.EXISTENTIAL_OP) &&
                        expressions.get(i+2).getType() == ExpressionType.DIVIDER_SYM) {
                    ExpressionType openBracketType = ExpressionType.OPEN_BRACKET1;
                    ExpressionType closeBracketType = ExpressionType.CLOSE_BRACKET1;
                    if (expressions.get(i).getType() == ExpressionType.OPEN_BRACKET2) {openBracketType = ExpressionType.OPEN_BRACKET2; closeBracketType = ExpressionType.CLOSE_BRACKET2;}
                    else if (expressions.get(i).getType() == ExpressionType.OPEN_BRACKET3) {openBracketType = ExpressionType.OPEN_BRACKET3; closeBracketType = ExpressionType.CLOSE_BRACKET3;}
                    int j = 3;
                    int bracketCount = 1;
                    while ( i + j < expressions.size() && bracketCount > 0) {
                        if (expressions.get( i + j).getType() == openBracketType) {bracketCount++;}
                        else if (expressions.get(i + j).getType() == closeBracketType) {bracketCount--;}
                        j = j + 1;
                    }
                    j--;
                    if (expressions.get(i + j).getType() == closeBracketType) {

                        List<Expression> restrictorList = expressions.subList(i + 3, i + j);

                        List<Expression> opAtomics = getAtomics(restrictorList);
                        List<Expression> opComplexFormulas = getComplexFormulas(opAtomics);
                        if (opComplexFormulas != null && opComplexFormulas.size() == 1 && opComplexFormulas.get(0).getType() == ExpressionType.FORMULA) {
                            Formula restrictingFormula = (Formula) opComplexFormulas.get(0);

                            if (expressions.get(i + 1).getType() == ExpressionType.UNIVERSAL_OP) {
                                UnivRestrictedQuantOp op = new UnivRestrictedQuantOp((OpenBracket) expressions.get(i), (CloseBracket) expressions.get(i + 4), (UniversalOp) expressions.get(i + 1),
                                        (DividerSym) expressions.get(i + 2), restrictingFormula);
                                expressions.set(i, op);
                            } else {
                                ExisRestrictedQuantOp op = new ExisRestrictedQuantOp((OpenBracket) expressions.get(i), (CloseBracket) expressions.get(i + 4), (ExistentialOp) expressions.get(i + 1),
                                        (DividerSym) expressions.get(i + 2), restrictingFormula);
                                expressions.set(i, op);
                            }
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                            expressions.remove(i + 1);
                        }
                    }




                }
            }
        }

        return expressions;
    }

    public static List<Expression> getRelSentSymbols(List<Expression> expressions) {

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

                        if (language.isAllowBinaryInfixNegations()) {
                            String complementStr = language.getInfixRelations().get(elementStr);
                            RelationSymbol complementSymbol = null;
                            if (!complementStr.isEmpty()) {
                                complementSymbol = new RelationSymbol(complementStr, "", "", 2);
                                complementSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());
                                relationSymbol.setComplementSymbol(complementSymbol);
                            }
                        }
                        expressions.set(i, relationSymbol);
                        continue;
                    }


                    if (language.isAllowBinaryInfixNegations()) {
                        if (language.getInfixRelations().containsValue(elementStr)) {
                            String relationString = language.getInfixRelationKey(elementStr);
                            RelationSymbol relationSymbol = new RelationSymbol(relationString, "", "", 2);
                            relationSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());
                            relationSymbol.setType(ExpressionType.COMPLEMENT_REL_SYM);

                            RelationSymbol complementSymbol = new RelationSymbol(elementStr, "", "", 2);
                            complementSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());
                            relationSymbol.setComplementSymbol(complementSymbol);
                            expressions.set(i, relationSymbol);
                            continue;
                        }
                    }
                }

                if (language.getXrelationSymbols() != null && language.getXrelationSymbols().contains(elementStr) ) {
                    int j = i + 1;
                    int places = areFollowingTerms(j, expressions);
                    if (isFollowingSuperscript(j, expressions) || (!language.isXrelationSymbolsRequireSuper() && places > 0)) {
                        if (language.isXrelationSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            subString = getSubString(j, expressions);
                            if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                supString = getSupString(j, expressions);
                        } else if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            supString = getSupString(j, expressions);
                            if (language.isXrelationSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                subString = getSubString(j, expressions);
                        }
                        if (!supString.isEmpty()) places = Integer.parseInt(supString);
                        RelationSymbol relationSymbol = new RelationSymbol(elementStr, subString, supString, places);
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
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = getSubString(j, expressions);
                    SentenceLetter letter = new SentenceLetter(elementStr, subString);

                    expressions.set(i, letter);
                }
            }
        }


        return expressions;


    }



    public static List<Expression> getTerms(List<Expression> expressions) {

        //variables and constants
        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.VARIABLE_SYM) {
                Term term = new Term();
                term.setLevel(0);
                ArrayList children = new ArrayList();
                children.add(expressions.get(i));
                term.setChildren(children);
                term.setCombines(true);
                term.setTermType(TermType.VARIABLE);
                expressions.set(i, term);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.CONSTANT_SYM) {
                Term term = new Term();
                term.setLevel(0);
                List<Expression> children = Collections.singletonList((Expression) expressions.get(i));
                term.setChildren(children);
                term.setCombines(true);
                term.setTermType(TermType.CONSTANT);
                expressions.set(i, term);
            }
        }

        boolean changes = true;
        while(changes) {
            changes = false;

            //regular "prefix" terms
            boolean prefixChanges = true;
            while (prefixChanges) {
                prefixChanges = false;
                for (int i = 0; i < expressions.size(); i++) {
                    if (expressions.get(i).getType() == ExpressionType.FUNCTION_SYMBOL) {
                        FunctionSymbol functionSymbol = (FunctionSymbol) expressions.get(i);
                        int j = i + 1;
                        if (areImmediateFollowingTerms(j, functionSymbol.getPlaces(), expressions)) {
                            List<Expression> children = new ArrayList();
                            int level = 0;
                            for (int k = 0; k < functionSymbol.getPlaces(); k++) {
                                Term term = (Term) expressions.get(j);
                                expressions.remove(j);
                                children.add(term);
                                level = Math.max(level, (term.getLevel()) + 1);
                                maxTermLevel = Math.max(maxTermLevel, level);
                            }
                            Term term = new Term();
                            term.setLevel(level);
                            term.setChildren(children);
                            term.setMainFnSymbol(functionSymbol);
                            term.setCombines(true);
                            expressions.set(i, term);
                            prefixChanges = true;
                            changes = true;
                            break;
                        }
                    }
                }
            }

            //infix terms
            for (int i = 0; i < expressions.size(); i++) {
                if (expressions.get(i).getType() == ExpressionType.FUNCTION_SYMBOL) {
                    FunctionSymbol functionSymbol = (FunctionSymbol) expressions.get(i);
                    if (functionSymbol.isPermitInfix()) {
                        if (i - 1 >=0 && expressions.get(i - 1).getType() == ExpressionType.TERM && ((Term) expressions.get(i - 1)).isCombines()) {
                            if (i + 1 < expressions.size() && expressions.get(i + 1).getType() == ExpressionType.TERM && ((Term) expressions.get(i + 1)).isCombines()) {
                                Term term1 = (Term) expressions.get(i - 1);
                                Term term2 = (Term) expressions.get(i + 1);
                                InfixTerm newTerm;
                                if (i - 2 >=0 && i + 2 < expressions.size() &&
                                        ((expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET1 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET1) ||
                                        (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET2 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET2) ||
                                        (expressions.get(i - 2).getType() == ExpressionType.OPEN_BRACKET3 && expressions.get(i + 2).getType() == ExpressionType.CLOSE_BRACKET3))
                                ) {
                                    newTerm = newInfixTerm(((OpenBracket) expressions.get(i - 2)), term1, functionSymbol, term2, ((CloseBracket) expressions.get(i + 2)));
                                    newTerm.setLevel(Math.max(term1.getLevel(), term2.getLevel()) + 1);

                                    maxTermLevel = Math.max(maxTermLevel, newTerm.getLevel());
                                    newTerm.setCombines(true);
                                    expressions.set(i - 2, newTerm);

                                    //remove takes account of indexes shifted after previous remove.
                                    expressions.remove(i - 1);
                                    expressions.remove(i - 1);
                                    expressions.remove(i - 1);
                                    expressions.remove(i - 1);
                                    changes = true;
                                    break;
                                }
                                else {
                                    newTerm = newInfixTerm(new OpenBracket(""), term1, functionSymbol, term2, new CloseBracket(""));
                                    newTerm.setLevel(Math.max(term1.getLevel(), term2.getLevel()) + 1);
                                    maxTermLevel = Math.max(maxTermLevel, newTerm.getLevel());
                                    newTerm.setCombines(false);
                                    expressions.set(i - 1, newTerm);
                                    expressions.remove(i);
                                    expressions.remove(i);
                                    changes = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return expressions;
    }

    //temp
    private static void dumpExpressions(List<Expression> expressions) {
        for (Expression expr : expressions) {
            System.out.print(expr.getType() + ": " + expr + "; ");
        }
    }

    private static InfixTerm newInfixTerm(OpenBracket openBracket, Term term1, FunctionSymbol fnSymbol, Term term2, CloseBracket closeBracket) {
        InfixTerm term = new InfixTerm();
        term.setOpenBracket(openBracket);

        term.setMainFnSymbol(fnSymbol);
        term.setCloseBracket(closeBracket);

        List<Expression> children = new ArrayList();
        children.add(term1); children.add(term2);

        term.setChildren(children);
        term.setLevel(Math.max(term1.getLevel(), term2.getLevel()) + 1);

        return term;
    }

    public static List<Expression> getTermSymbols(List<Expression> expressions) {

        //function symbols
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            String supString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getOnePlaceFunctionSymbols() != null && language.getOnePlaceFunctionSymbols().contains(elementStr)) {
                    expressions.set(i, new FunctionSymbol(elementStr, "", "", 1, false));
                    continue;
                }
                if (language.getTwoPlaceFunctionSymbols() != null && language.getTwoPlaceFunctionSymbols().contains(elementStr)) {
                    expressions.set(i, new FunctionSymbol(elementStr, "", "", 2, language.isAllowBinaryInfixFunctions()));
                    continue;
                }
                if (language.getXfunctionSymbols() != null && language.getXfunctionSymbols().contains(elementStr) ) {
                    int j = i + 1;
                    if (isFollowingSuperscript(j, expressions)) {
                        if (language.isXfunctionSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            subString = getSubString(j, expressions);
                            if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                supString = getSupString(j, expressions);
                        } else if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                            supString = getSupString(j, expressions);
                            if (language.isXfunctionSymbolSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                                    ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]"))
                                subString = getSubString(j, expressions);
                        }
                        FunctionSymbol functionSymbol = new FunctionSymbol(elementStr, subString, supString, Integer.parseInt(supString), false);
                        expressions.set(i, functionSymbol);
                    }
                }
            }
        }

        //variables
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getVariables() != null && language.getVariables().contains(elementStr)) {
                    int j = i + 1;
                    if (language.isVariableSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = getSubString(j, expressions);
                    Variable variable = new Variable(elementStr, subString);

                    expressions.set(i, variable);
                }
            }
        }

        //constants
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getConstants() != null && language.getConstants().contains(elementStr)) {
                    int j = i + 1;
                    if (language.isConstantSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = getSubString(j, expressions);
                    Constant constant = new Constant(elementStr, subString);
                    expressions.set(i, constant);
                }
            }
        }
        return expressions;
    }

    public static List<Expression> getSimpleSymbols(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i) instanceof OriginalElement) {
                String elementStr = ((OriginalElement) expressions.get(i)).getElementStr();

                if (elementStr.equals(language.getOpenBracket1()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new OpenBracket1(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getOpenBracket2()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new OpenBracket2(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getOpenBracket3()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new OpenBracket3(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getCloseBracket1()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new CloseBracket1(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getCloseBracket2()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new CloseBracket2(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getCloseBracket3()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new CloseBracket3(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getNegation()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new NegationSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getConditional()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new ConditionalSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getBiconditional()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new BiconditionalSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getConjunction()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new ConjunctionSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getDisjunction()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new DisjunctionSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getNand()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new NandSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getNor()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new NorSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getUniversalQuant()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new UniversalQuantifierSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getExistentialQuant()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new ExistentialQuantifierSym(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getDividerSymbol()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new DividerSym(elementStr));
                }
            }
        }
        return expressions;
    }


    //are places following terms (possibly after superscript/subscript)
    private static int areFollowingTerms(int j, List<Expression> expressions) {
        int places = 0;
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT ) {
            OriginalElement element = (OriginalElement) expressions.get(j);
            if (element.isSuperscript() || element.isSubscript())   j = j + 1;
            else break;
        }
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.TERM ) {
            places++;
            j = j + 1;
        }
        return places;
    }


    private static boolean areImmediateFollowingAtomicTerms(int index, int places, List<Expression> expressions) {
        boolean areFollowingTerms = true;
        for (int i = 0; i < places; i++) {
            if (index + i >= expressions.size() || expressions.get(index + i).getType() != ExpressionType.TERM ) areFollowingTerms = false;
        }
        return areFollowingTerms;
    }
    //are places immediately following terms
    private static boolean areImmediateFollowingTerms(int index, int places, List<Expression> expressions) {
        boolean areFollowingTerms = true;
        for (int i = 0; i < places; i++) {
            if (index + i >= expressions.size() || expressions.get(index + i).getType() != ExpressionType.TERM || !((Term) expressions.get(index + i)).isCombines()) areFollowingTerms = false;
        }
        return areFollowingTerms;
    }

    //is following superscript > 0, possibly after subscript
    private static boolean isFollowingSuperscript(int j, List<Expression> expressions) {
        boolean isFollowingSuperscript = false;
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT) {
            OriginalElement originalElement = (OriginalElement) expressions.get(j);
            if (originalElement.isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {isFollowingSuperscript = true; break;}
            else if (originalElement.isSubscript()) {j = j + 1;}
            else break;
        }
        return isFollowingSuperscript;
    }

    //get string of subscripts, deleting members from expressions
    private static String getSubString(int j, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[0-9]")) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }

    //get string of superscripts, deleting members from expressions
    private static String getSupString(int j, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[0-9]")) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }


    //get elements from document, strip space characters, and return list of remaining elements
    public static List<OriginalElement> getElements(Document doc) {
        List<OriginalElement> elements = new ArrayList<OriginalElement>();
        String text = doc.getText();
        String s;
        int i = 0;
        while (i < text.length()) {
            int len = 1;
            if (i + 2 <= text.length() && text.codePointCount(i, i + 2) == 1) {
                len = 2;
            }
            s = text.substring(i, i + len);
            TextDecoration dec = (TextDecoration) (getDecorationAtIndex(i, doc.getDecorations()).getDecoration());
            OriginalElement element = new OriginalElement(s, dec);
            elements.add(element);
            i = i + len;
        }
        i = 0;
        while (i < elements.size()) {
            String elementStr = elements.get(i).getElementStr();
            char ch = elementStr.charAt(0);
            boolean bad = elementStr.length() == 1 && 0xD800 <= (int) ch && (int) ch <= 0xDFFF;
            if (Character.isWhitespace(ch) || bad) {
                elements.remove(i);
                continue;
            }
            i = i + 1;
        }

        return elements;
    }

    /*
     * Get the decoration with application to the index position in Document
     * @param index position in document
     * @param decorations the document decorations
     * @return the applicable DecorationModel
     */
    private static DecorationModel getDecorationAtIndex(int index, List<DecorationModel> decorations) {
        DecorationModel decorationAtIndex = null;
        for (DecorationModel decoration : decorations) {
            if (decoration.getStart() <= index && index < decoration.getStart() + decoration.getLength()) {
                decorationAtIndex = decoration;
                break;
            }
        }
        return decorationAtIndex;
    }










    public static Text newRegularText(String string) {
        Text t = new Text();
        if (!string.isEmpty()) {
            t = new Text(string);
            t.setFont(new Font("Noto Serif Combo", baseFontSize));
        }
        return t;
    }

    public static Text newSuperscriptText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", baseFontSize * .72));
        t.setTranslateY(baseFontSize * -.4);
        return t;
    }

    public static Text newSubscriptText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", baseFontSize * .72));
        t.setTranslateY(baseFontSize * .17);
        return t;
    }

    public static Text[] newSupSubText(String supText, String subText) {
        Text t1 = new Text(supText);
        Text t2 = new Text(subText);
        t1.setFont(new Font("Noto Serif Combo", baseFontSize * .72));
        t2.setFont(new Font("Noto Serif Combo", baseFontSize * .72));
        t1.setTranslateY(baseFontSize * -.4);
        t2.setTranslateY(baseFontSize * .17);
        t1.applyCss();
        double offset = t1.getLayoutBounds().getWidth();
        t2.setTranslateX(-offset);
        return new Text[]{t1, t2};
    }

    public static void setLanguage(Language language) {
        ParseUtilities.language = language;
    }
}
