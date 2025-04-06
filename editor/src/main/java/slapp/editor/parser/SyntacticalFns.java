package slapp.editor.parser;

import com.gluonhq.richtextarea.model.Document;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.FunctionSymbol;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SyntacticalFns {
    private static List<Expression> nodeList = new ArrayList<>();
    private static Language lang;
    private static List<Expression> variableList;
    private static List<Expression> variableList2;
    private static List<Expression> removalList;

    public SyntacticalFns() {}

    public static boolean freeForDoc(Document formulaDoc, Document term1Doc, Document term2Doc, String langName) {

        //make sure we have a formula and two terms
        List<Expression> formulaList = new ArrayList<>();
        List<Expression> term1List = new ArrayList<>();
        List<Expression> term2List = new ArrayList<>();
        if (formulaDoc != null) formulaList = ParseUtilities.parseDoc(formulaDoc, langName);
        if (term1Doc != null) term1List = ParseUtilities.parseDoc(term1Doc, langName);
        if (term2Doc != null) term2List = ParseUtilities.parseDoc(term2Doc, langName);

        if (formulaList.size() == 0 || term1List.size() == 0 || term2List.size() == 0) {
            return false;
        }

        Expression formulaExp = formulaList.get(0);
        Expression term1Exp = term1List.get(0);
        Expression term2Exp = term2List.get(0);

        if (formulaList.size() > 1 || term1List.size() > 1 || term2List.size() > 1 ||
                formulaExp.getType() != ExpressionType.FORMULA || term1Exp.getType() != ExpressionType.TERM || term2Exp.getType() != ExpressionType.TERM) {
            return false;
        }
        return freeForExp(formulaExp, term1Exp, term2Exp, langName);

    }

    //remove from subformula list ones whose quantifier binds a variable in the term
    public static List<Expression> subsWithTermFree(Expression formulaExp, Expression term1Exp) {
        List<Expression> sortedSubformulas = subformulasExp(formulaExp);

        //get variables in term1
        variableList = new ArrayList();
        setTermVariableList(term1Exp);

        int i = sortedSubformulas.size() - 1;
        while (i >= 0) {
            Formula formula = (Formula) sortedSubformulas.get(i);
            if (!formula.isAtomic() && !(formula instanceof MFormula) && !(formula instanceof MComplexFormula) && formula.getChildren() != null) {
                ExpressionType type = formula.getMainOperator().getType();
                if (type == ExpressionType.UNIVERSAL_OP || type == ExpressionType.UNIV_BOUNDED_OP || type == ExpressionType.UNIV_RESTRICTED_OP ||
                        type == ExpressionType.EXISTENTIAL_OP || type == ExpressionType.EXIS_BOUNDED_OP || type == ExpressionType.EXIS_RESTRICTED_OP
                ) {
                    Term quantifiedVariable = formula.getMainOperator().getVariableTerm();
                    if (variableList.contains(quantifiedVariable)) {
                        removeTreeFromList(sortedSubformulas, formula.getChildren().get(0));
                        i = listIndexParticular(sortedSubformulas, formula);
                    }
                }
            }
            i = i - 1;
        }
        return sortedSubformulas;
    }

    //formula P has term term2 free for term1  (so term1 is the replaced term)
    public static boolean freeForExp(Expression formulaExp, Expression term1Exp, Expression term2Exp, String langName) {

        List<Expression> sortedSubformulas = subsWithTermFree(formulaExp, term1Exp);


        boolean freeFor = true;
        int index = 0;
        while (freeFor == true && index < sortedSubformulas.size()) {

            Formula formula = (Formula) sortedSubformulas.get(index);

            if (formula.isAtomic()) {
                index++;
                continue;
            }

            if (formula instanceof MFormula) {
                index++;
                continue;
            }

            if (formula instanceof MComplexFormula) {
                variableList2 = new ArrayList<>();
                setTermVariableList2(term2Exp);

                //term2 has a variable not in term1
                boolean extraVariable = false;
                for (Expression exp : variableList2) {
                    if (!variableList.contains(exp)) {
                        extraVariable = true;
                    }
                }
                if (extraVariable) {
                    freeFor = false;
                }
                index++;
                continue;
            }

            Operator mainOperator = formula.getMainOperator();
            ExpressionType opType = formula.getMainOperator().getType();

            if (opType == ExpressionType.NEG_OP || opType == ExpressionType.COND_OP ||
                    opType == ExpressionType.BICOND_OP || opType == ExpressionType.CONJ_OP || opType == ExpressionType.DISJ_OP || opType == ExpressionType.NAND_OP || opType == ExpressionType.NOR_OP)
            {
                boolean childrenOK = true;
                for (Expression child : formula.getChildren()) {
                    if (!listContainsParticular(sortedSubformulas, child) ) {
                        childrenOK = false;
                    }
                }
                if (!childrenOK) freeFor = false;
                index++;
                continue;
            }

            if (opType == ExpressionType.UNIVERSAL_OP || opType == ExpressionType.EXISTENTIAL_OP || opType == ExpressionType.UNIV_BOUNDED_OP || opType == ExpressionType.EXIS_BOUNDED_OP) {
                Term variable = mainOperator.getVariableTerm();

                //term1 is bound so no substitution
                if (variableList.contains(variable)) {
                    index++;
                    continue;
                }
                else {
                    Expression childExp = formula.getChildren().get(0);
                    if (expTermFreeInFormula(term2Exp, variable, langName) && particularTermsFreeInFormula(childExp, Collections.singletonList(term1Exp), langName)) { //expTermFreeInFormula(childExp, term1Exp, langName)
                        freeFor = false;
                        index++;
                        continue;
                    }
                }
            }

            if (opType == ExpressionType.UNIV_RESTRICTED_OP ||  opType == ExpressionType.EXIS_RESTRICTED_OP) {
                Term variable = mainOperator.getVariableTerm();
                if (variableList.contains(variable)) {
                    index++;
                    continue;
                }
                else {
                    Expression childExp = formula.getChildren().get(0);
                    if (expTermFreeInFormula(term2Exp, variable, langName) && particularTermsFreeInFormula(childExp, Collections.singletonList(term1Exp), langName)) { //expTermFreeInFormula(childExp, term1Exp, langName)
                        freeFor = false;
                        index++;
                        continue;
                    }
                }
                //check restricting formula
                Formula restrictingFormula = null;
                Operator restrictingQuantifier = null;
                if (mainOperator instanceof UnivRestrictedQuantOp) {
                    restrictingFormula = ((UnivRestrictedQuantOp) mainOperator).getRestrictingFormula();
                    restrictingQuantifier = ((UnivRestrictedQuantOp) mainOperator).getUniversalOp();
                }
                if (mainOperator instanceof ExisRestrictedQuantOp) {
                    restrictingFormula = ((ExisRestrictedQuantOp) mainOperator).getRestrictingFormula();
                    restrictingQuantifier = ((ExisRestrictedQuantOp) mainOperator).getExistentialOp();
                }

                Formula newFormula = new Formula();                                      //need to check with quantifier applied, since restricting formula is in its scope
                newFormula.setMainOperator(restrictingQuantifier);
                newFormula.setChildren(Collections.singletonList(restrictingFormula));

                boolean freeForResting = freeForExp(newFormula, term1Exp, term2Exp, langName);
                sortedSubformulas = subsWithTermFree(formulaExp, term1Exp);   //reset sorted formulas
                if (!freeForResting) {
                    freeFor = false;
                    index++;
                    continue;
                }
            }

            index = index + 1;
        }

        return freeFor;
    }

    public static void removeTreeFromList(List<Expression> subformulas, Expression formula) {
        removalList = new ArrayList<>();
        setRemovalList(subformulas, formula);
        for (Expression exp : removalList) {
            removeParticularFromList(subformulas, exp);
        }
    }

    public static void setRemovalList(List<Expression> subformulas, Expression formula) {
        removalList.add(formula);

        if (formula.getChildren() != null) {
            for (Expression child : formula.getChildren()) {
                if (listContainsParticular(subformulas, child)) {
                    setRemovalList(subformulas, child);
                }
            }
        }
    }

    public static boolean listContainsParticular(List list, Object o) {
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == o) {
                found = true;
            }
        }
        return found;
    }

    public static void removeParticularFromList(List list, Object o) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == o) {
                list.remove(i);
                break;
            }
        }
    }

    public static int listIndexParticular(List list, Object o) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == o) {
                index = i;
                break;
            }
        }
        return index;
    }





    //say if term t is free in formula P (P either term or formula)
    public static boolean docTermFreeInFormula(Document formulaDoc, Document termDoc, String langName) {

        boolean free = false;

        Expression formExp = ParseUtilities.parseDoc(formulaDoc, langName).get(0);
        Expression termExp = ParseUtilities.parseDoc(termDoc, langName).get(0);
        return expTermFreeInFormula(formExp, termExp, langName);
    }

    public static boolean expTermFreeInFormula(Expression formulaExp, Expression termExp, String langName) {
        boolean free = true;

        Expression dummyVariable = ParseUtilities.parseDoc(new Document(Languages.getLanguage(langName).getDummyVariableSym()), langName).get(0);
        Expression subExp = substituteExpTerms(formulaExp, termExp, dummyVariable);

        if (subExp.equals(formulaExp)) {
            free = false;
        }

        return free;
    }

    //formula P with term t replaced by term s (P either term or formula)
    public static Expression substituteDocTerms(Document formulaDoc, Document term1Doc, Document term2Doc, String langName) {

        //make sure we have a formula and two terms
        List<Expression> formulaList = new ArrayList<>();
        List<Expression> term1List = new ArrayList<>();
        List<Expression> term2List = new ArrayList<>();
        if (formulaDoc != null) formulaList = ParseUtilities.parseDoc(formulaDoc, langName);
        if (term1Doc != null) term1List = ParseUtilities.parseDoc(term1Doc, langName);
        if (term2Doc != null) term2List = ParseUtilities.parseDoc(term2Doc, langName);


        if (formulaList.size() == 0 || term1List.size() == 0 || term2List.size() == 0) {

            return null;
        }
        Expression formulaExp = formulaList.get(0);
        Expression term1Exp = term1List.get(0);
        Expression term2Exp = term2List.get(0);

        if (formulaList.size() > 1 || term1List.size() > 1 || term2List.size() > 1 ||
                (formulaExp.getType() != ExpressionType.FORMULA && formulaExp.getType() != ExpressionType.TERM) || term1Exp.getType() != ExpressionType.TERM || term2Exp.getType() != ExpressionType.TERM) {
            return null;
        }
        return substituteExpTerms(formulaExp, term1Exp, term2Exp);
    }

    public static Expression substituteExpTerms(Expression formulaExp, Expression term1Exp, Expression term2Exp) {

        //get variables in term1
        variableList = new ArrayList();
        setTermVariableList(term1Exp);

        //sort formula nodes by level
        nodeList = new ArrayList<Expression>();
        listNodes(formulaExp);
        Collections.sort(nodeList, new SortByLevel());

        //create parallel list
        List<Expression> parallelList = new ArrayList();

        for (int i = 0; i < nodeList.size(); i++) {
            Expression exp = nodeList.get(i);
            if (exp.getType() == ExpressionType.TERM) {
                Term term = (Term) exp;

                if (term.equals((Term) term1Exp)) {
                    parallelList.add(term2Exp);
                }
                else if ( term.getTermType() == TermType.VARIABLE || term.getTermType() == TermType.CONSTANT) {
                    parallelList.add(exp);
                }
                else if ( term.getTermType() == TermType.COMPLEX) {

                    FunctionSymbol fnSymbol = term.getMainFnSymbol();
                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < term.getChildren().size(); j++) {
                        Expression childExp = term.getChildren().get(j);
                        int index = nodeList.indexOf(childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    if (term instanceof InfixTerm) {
                        InfixTerm infixTerm = (InfixTerm) term;
                        InfixTerm newTerm = new InfixTerm();
                        newTerm.setOpenBracket(infixTerm.getOpenBracket());
                        newTerm.setCloseBracket(infixTerm.getCloseBracket());
                        newTerm.setMainFnSymbol(fnSymbol);
                        newTerm.setChildren(parallelChildList);
                        parallelList.add(newTerm);

                    } else {
                        Term newTerm = new Term();
                        newTerm.setMainFnSymbol(fnSymbol);
                        newTerm.setChildren(parallelChildList);
                        parallelList.add(newTerm);
                    }
                }
            }
            else if (exp.getType() == ExpressionType.FORMULA) {

                Formula formula = (Formula) exp;
                if (formula instanceof SentenceAtomic || formula instanceof MFormula) {
                    parallelList.add(exp);
                }
                else if (formula.isAtomic()) {
                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < formula.getChildren().size(); j++) {
                        Expression childExp = formula.getChildren().get(j);
                        int index = nodeList.indexOf(childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    if (formula instanceof PrefixAtomic) {
                        PrefixAtomic prefixAtomic = (PrefixAtomic) formula;
                        PrefixAtomic newPrefixAtomic = new PrefixAtomic(prefixAtomic.getMainRelation()) ;
                        newPrefixAtomic.setChildren(parallelChildList);
                        parallelList.add(newPrefixAtomic);
                    }
                    else if (formula instanceof InfixAtomic) {
                        InfixAtomic infixAtomic = (InfixAtomic) formula;
                        InfixAtomic newInfixAtomic = new InfixAtomic(infixAtomic.getMainRelation(), infixAtomic.getComplementRelation(), infixAtomic.isNegatingInfix());
                        newInfixAtomic.setOpenBracket(infixAtomic.getOpenBracket());
                        newInfixAtomic.setCloseBracket(infixAtomic.getCloseBracket());
                        newInfixAtomic.setChildren(parallelChildList);
                        parallelList.add(newInfixAtomic);
                    }
                }
                else if (formula instanceof MComplexFormula) {
                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < formula.getChildren().size(); j++) {
                        Expression childExp = formula.getChildren().get(j);
                        int index = listIndexParticular(nodeList, childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    MComplexFormula complexFormula = (MComplexFormula) formula;
                    MComplexFormula newComplexFormula = new MComplexFormula(complexFormula.getFormulaSym());
                    newComplexFormula.setChildren(parallelChildList);
                    newComplexFormula.setOpenBracket(complexFormula.getOpenBracket());
                    newComplexFormula.setCloseBracket(complexFormula.getCloseBracket());
                    parallelList.add(newComplexFormula);
                }

                else if (formula.getMainOperator().getType() == ExpressionType.UNIVERSAL_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);
                else if (formula.getMainOperator().getType() == ExpressionType.EXISTENTIAL_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);

                else if (formula.getMainOperator().getType() == ExpressionType.UNIV_BOUNDED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);
                else if (formula.getMainOperator().getType() == ExpressionType.EXIS_BOUNDED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);

                else if (formula.getMainOperator().getType() == ExpressionType.UNIV_RESTRICTED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);
                else if (formula.getMainOperator().getType() == ExpressionType.EXIS_RESTRICTED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);

                else {
                    Operator newMainOperator = null;
                    if (formula.getMainOperator().getType() == ExpressionType.UNIV_BOUNDED_OP) {
                        UnivBoundedQuantOp boundingOperator = (UnivBoundedQuantOp) formula.getMainOperator();
                        Term newBoundingTerm = (Term) substituteExpTerms(boundingOperator.getBoundingTerm(), term1Exp, term2Exp);
                        UnivBoundedQuantOp newBoundingOp = new UnivBoundedQuantOp(boundingOperator.getOpenBracket(), boundingOperator.getCloseBracket(), boundingOperator.getUniversalOp(),
                                boundingOperator.getInfixRelation(), newBoundingTerm);
                        newMainOperator = newBoundingOp;
                    } else if (formula.getMainOperator().getType() == ExpressionType.UNIV_RESTRICTED_OP) {
                        UnivRestrictedQuantOp restrictedOperator = (UnivRestrictedQuantOp) formula.getMainOperator();
                        Formula newRestrictingFormula = (Formula) substituteExpTerms(restrictedOperator.getRestrictingFormula(), term1Exp, term2Exp);
                        UnivRestrictedQuantOp newRestrictingOp = new UnivRestrictedQuantOp(restrictedOperator.getOpenBracket(), restrictedOperator.getCloseBracket(), restrictedOperator.getUniversalOp(),
                                restrictedOperator.getDivider(), newRestrictingFormula);
                        newMainOperator = newRestrictingOp;
                    } else if (formula.getMainOperator().getType() == ExpressionType.EXIS_BOUNDED_OP) {
                        ExisBoundedQuantOp boundingOperator = (ExisBoundedQuantOp) formula.getMainOperator();
                        Term newBoundingTerm = (Term) substituteExpTerms(boundingOperator.getBoundingTerm(), term1Exp, term2Exp);
                        ExisBoundedQuantOp newBoundingOp = new ExisBoundedQuantOp(boundingOperator.getOpenBracket(), boundingOperator.getCloseBracket(), boundingOperator.getExistentialOp(),
                                boundingOperator.getInfixRelation(), newBoundingTerm);
                        newMainOperator = newBoundingOp;
                    } else if (formula.getMainOperator().getType() == ExpressionType.EXIS_RESTRICTED_OP) {
                        ExisRestrictedQuantOp restrictingOp = (ExisRestrictedQuantOp) formula.getMainOperator();
                        Formula newRestrictingFormula = (Formula) substituteExpTerms(restrictingOp.getRestrictingFormula(), term1Exp, term2Exp);
                        ExisRestrictedQuantOp newRestrictingOp = new ExisRestrictedQuantOp(restrictingOp.getOpenBracket(), restrictingOp.getCloseBracket(), restrictingOp.getExistentialOp(),
                                restrictingOp.getDivider(), newRestrictingFormula);
                        newMainOperator = newRestrictingOp;
                    } else {
                        newMainOperator = formula.getMainOperator();
                    }

                    //reset node list
                    nodeList = new ArrayList<Expression>();
                    listNodes(formulaExp);
                    Collections.sort(nodeList, new SortByLevel());


                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < formula.getChildren().size(); j++) {
                        Expression childExp = formula.getChildren().get(j);
                        int index = nodeList.indexOf(childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    Formula newFormula = new Formula();
                    newFormula.setChildren(parallelChildList);
                    newFormula.setCombines(formula.isCombines());
                    newFormula.setMainOperator(newMainOperator);
                    newFormula.setOpenBracket(formula.getOpenBracket());
                    newFormula.setCloseBracket(formula.getCloseBracket());
                    newFormula.setNegatingInfix(formula.isNegatingInfix());
                    parallelList.add(newFormula);
                }
            }
        }

        return parallelList.get(parallelList.size() - 1);
    }

    public static boolean particularTermsFreeInFormula(Expression formulaExp, List<Expression> termList, String langName) {
        boolean free = true;

        Expression dummyVariable = ParseUtilities.parseDoc(new Document(Languages.getLanguage(langName).getDummyVariableSym()), langName).get(0);
        Expression subExp = substituteParticularTerms(formulaExp, dummyVariable, termList);

        if (subExp.equals(formulaExp)) {
            free = false;
        }

        return free;
    }


    //Formula (term) with T1 instances replaced by T2
    public static Expression substituteParticularTerms(Expression formulaExp, Expression term2Exp, List<Expression> term1List) {

        //get variables in term1
        variableList = new ArrayList();
        for (Expression term1Exp : term1List) {
            setTermVariableList(term1Exp);
        }


        //sort formula nodes by level
        nodeList = new ArrayList<Expression>();
        listNodes(formulaExp);
        Collections.sort(nodeList, new SortByLevel());

        //create parallel list
        List<Expression> parallelList = new ArrayList();

        for (int i = 0; i < nodeList.size(); i++) {
            Expression exp = nodeList.get(i);
            if (exp.getType() == ExpressionType.TERM) {
                Term term = (Term) exp;

                if (listContainsParticular(term1List, term)) {
                    parallelList.add(term2Exp);
                }

                else if ( term.getTermType() == TermType.VARIABLE || term.getTermType() == TermType.CONSTANT) {
                    parallelList.add(exp);
                }
                else if ( term.getTermType() == TermType.COMPLEX) {

                    FunctionSymbol fnSymbol = term.getMainFnSymbol();
                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < term.getChildren().size(); j++) {
                        Expression childExp = term.getChildren().get(j);
                        int index = listIndexParticular(nodeList, childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    if (term instanceof InfixTerm) {
                        InfixTerm infixTerm = (InfixTerm) term;
                        InfixTerm newTerm = new InfixTerm();
                        newTerm.setOpenBracket(infixTerm.getOpenBracket());
                        newTerm.setCloseBracket(infixTerm.getCloseBracket());
                        newTerm.setMainFnSymbol(fnSymbol);
                        newTerm.setChildren(parallelChildList);
                        parallelList.add(newTerm);

                    } else {
                        Term newTerm = new Term();
                        newTerm.setMainFnSymbol(fnSymbol);
                        newTerm.setChildren(parallelChildList);
                        parallelList.add(newTerm);
                    }
                }
            }
            else if (exp.getType() == ExpressionType.FORMULA) {

                Formula formula = (Formula) exp;
                if (formula instanceof SentenceAtomic || formula instanceof MFormula) {
                    parallelList.add(exp);
                }
                else if (formula.isAtomic()) {
                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < formula.getChildren().size(); j++) {
                        Expression childExp = formula.getChildren().get(j);
                        int index = listIndexParticular(nodeList, childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    if (formula instanceof PrefixAtomic) {
                        PrefixAtomic prefixAtomic = (PrefixAtomic) formula;
                        PrefixAtomic newPrefixAtomic = new PrefixAtomic(prefixAtomic.getMainRelation()) ;
                        newPrefixAtomic.setChildren(parallelChildList);
                        parallelList.add(newPrefixAtomic);
                    }
                    else if (formula instanceof InfixAtomic) {
                        InfixAtomic infixAtomic = (InfixAtomic) formula;
                        InfixAtomic newInfixAtomic = new InfixAtomic(infixAtomic.getMainRelation(), infixAtomic.getComplementRelation(), infixAtomic.isNegatingInfix());
                        newInfixAtomic.setOpenBracket(infixAtomic.getOpenBracket());
                        newInfixAtomic.setCloseBracket(infixAtomic.getCloseBracket());
                        newInfixAtomic.setChildren(parallelChildList);
                        parallelList.add(newInfixAtomic);
                    }
                }
                else if (formula instanceof MComplexFormula) {
                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < formula.getChildren().size(); j++) {
                        Expression childExp = formula.getChildren().get(j);
                        int index = listIndexParticular(nodeList, childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    MComplexFormula complexFormula = (MComplexFormula) formula;
                    MComplexFormula newComplexFormula = new MComplexFormula(complexFormula.getFormulaSym());
                    newComplexFormula.setChildren(parallelChildList);
                    newComplexFormula.setOpenBracket(complexFormula.getOpenBracket());
                    newComplexFormula.setCloseBracket(complexFormula.getCloseBracket());
                    parallelList.add(newComplexFormula);
                }

                else if (formula.getMainOperator().getType() == ExpressionType.UNIVERSAL_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);
                else if (formula.getMainOperator().getType() == ExpressionType.EXISTENTIAL_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);

                else if (formula.getMainOperator().getType() == ExpressionType.UNIV_BOUNDED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);
                else if (formula.getMainOperator().getType() == ExpressionType.EXIS_BOUNDED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);

                else if (formula.getMainOperator().getType() == ExpressionType.UNIV_RESTRICTED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);
                else if (formula.getMainOperator().getType() == ExpressionType.EXIS_RESTRICTED_OP && binds(formula.getMainOperator().getVariableTerm())) parallelList.add(formula);

                else {
                    Operator newMainOperator = null;
                    if (formula.getMainOperator().getType() == ExpressionType.UNIV_BOUNDED_OP) {
                        UnivBoundedQuantOp boundingOperator = (UnivBoundedQuantOp) formula.getMainOperator();
                        Term newBoundingTerm = (Term) substituteParticularTerms(boundingOperator.getBoundingTerm(), term2Exp, term1List);
                        UnivBoundedQuantOp newBoundingOp = new UnivBoundedQuantOp(boundingOperator.getOpenBracket(), boundingOperator.getCloseBracket(), boundingOperator.getUniversalOp(),
                                boundingOperator.getInfixRelation(), newBoundingTerm);
                        newMainOperator = newBoundingOp;
                    } else if (formula.getMainOperator().getType() == ExpressionType.UNIV_RESTRICTED_OP) {
                        UnivRestrictedQuantOp restrictedOperator = (UnivRestrictedQuantOp) formula.getMainOperator();
                        Formula newRestrictingFormula = (Formula) substituteParticularTerms(restrictedOperator.getRestrictingFormula(), term2Exp, term1List);
                        UnivRestrictedQuantOp newRestrictingOp = new UnivRestrictedQuantOp(restrictedOperator.getOpenBracket(), restrictedOperator.getCloseBracket(), restrictedOperator.getUniversalOp(),
                                restrictedOperator.getDivider(), newRestrictingFormula);
                        newMainOperator = newRestrictingOp;
                    } else if (formula.getMainOperator().getType() == ExpressionType.EXIS_BOUNDED_OP) {
                        ExisBoundedQuantOp boundingOperator = (ExisBoundedQuantOp) formula.getMainOperator();
                        Term newBoundingTerm = (Term) substituteParticularTerms(boundingOperator.getBoundingTerm(), term2Exp, term1List);
                        ExisBoundedQuantOp newBoundingOp = new ExisBoundedQuantOp(boundingOperator.getOpenBracket(), boundingOperator.getCloseBracket(), boundingOperator.getExistentialOp(),
                                boundingOperator.getInfixRelation(), newBoundingTerm);
                        newMainOperator = newBoundingOp;
                    } else if (formula.getMainOperator().getType() == ExpressionType.EXIS_RESTRICTED_OP) {
                        ExisRestrictedQuantOp restrictingOp = (ExisRestrictedQuantOp) formula.getMainOperator();
                        Formula newRestrictingFormula = (Formula) substituteParticularTerms(restrictingOp.getRestrictingFormula(), term2Exp, term1List);
                        ExisRestrictedQuantOp newRestrictingOp = new ExisRestrictedQuantOp(restrictingOp.getOpenBracket(), restrictingOp.getCloseBracket(), restrictingOp.getExistentialOp(),
                                restrictingOp.getDivider(), newRestrictingFormula);
                        newMainOperator = newRestrictingOp;
                    } else {
                        newMainOperator = formula.getMainOperator();
                    }

                    //reset node list
                    nodeList = new ArrayList<Expression>();
                    listNodes(formulaExp);
                    Collections.sort(nodeList, new SortByLevel());


                    List<Expression> parallelChildList = new ArrayList();
                    for (int j = 0; j < formula.getChildren().size(); j++) {
                        Expression childExp = formula.getChildren().get(j);
                        int index = listIndexParticular(nodeList, childExp);
                        parallelChildList.add(parallelList.get(index));
                    }
                    Formula newFormula = new Formula();
                    newFormula.setChildren(parallelChildList);
                    newFormula.setCombines(formula.isCombines());
                    newFormula.setMainOperator(newMainOperator);
                    newFormula.setOpenBracket(formula.getOpenBracket());
                    newFormula.setCloseBracket(formula.getCloseBracket());
                    newFormula.setNegatingInfix(formula.isNegatingInfix());
                    parallelList.add(newFormula);
                }
            }
        }

        return parallelList.get(parallelList.size() - 1);
    }



    private static boolean binds(Expression var) {
        boolean binds = false;
        for (Expression v : variableList) {
            if (var.equals(v)) {
                binds = true;
            }
        }
        return binds;
    }

    public static void setTermVariableList(Expression exp) {
        if (exp.getType() == ExpressionType.TERM && ((Term) exp).getTermType() == TermType.VARIABLE && !variableList.contains(exp)) {
            variableList.add(exp);
        }
        if (exp.getChildren() != null && exp.getLevel() >=0) {
            for (int i = 0; i < exp.getChildren().size(); i++) {
                setTermVariableList(exp.getChildren().get(i));
            }
        }
    }

    public static void setTermVariableList2(Expression exp) {
        if (exp.getType() == ExpressionType.TERM && ((Term) exp).getTermType() == TermType.VARIABLE && !variableList2.contains(exp)) {
            variableList2.add(exp);
        }
        if (exp.getChildren() != null && exp.getLevel() >=0) {
            for (int i = 0; i < exp.getChildren().size(); i++) {
                setTermVariableList2(exp.getChildren().get(i));
            }
        }
    }



    public static int atomicLevel(Document doc, String langName) {
        List<Expression> subformulas = SyntacticalFns.subFormulasDoc(doc, langName);
        int level = -1;
        if (subformulas != null && subformulas.size() > 0) {
            level = subformulas.get(0).getLevel();
        }
        return level;
    }


    public static Expression mainOperator(Document doc, String langName) {
        List<Expression> subformulas = subFormulasDoc(doc, langName);
        Expression mainOp = null;
        if (subformulas != null) {
            if (subformulas.size() <= 1) {
                return null;
            }
            else {
                Formula form = (Formula) subformulas.get(subformulas.size() - 1);
                mainOp = form.getMainOperator();
            }
        }
        return mainOp;
    }

    public static List<Expression> immediateSubformulas(Document doc, String langName) {
        List<Expression> subformulas = subFormulasDoc(doc, langName);
        List<Expression> immediateSubs = new ArrayList<>();
        if (subformulas != null) {
            if (subformulas.size() <= 1) {
                return null;
            } else {
                Formula form = (Formula) subformulas.get(subformulas.size() - 1);
                for (int i = 0; i < form.getChildren().size(); i++) {
                    immediateSubs.add(form.getChildren().get(i));
                }
            }
        }
        return immediateSubs;
    }


    public static List<Expression> atomicSubformulas(Document doc, String langName) {
        List<Expression> subformulas = subFormulasDoc(doc, langName);
        List<Expression> atomicSubformulas = new ArrayList<>();
        if (subformulas != null) {
            for (int i = 0; i < subformulas.size(); i++) {
                if (((Formula) subformulas.get(i)).isAtomic()) {
                    atomicSubformulas.add(subformulas.get(i));
                }
            }
        }
        return atomicSubformulas;
    }

    public static List<Expression> subFormulasDoc(Document doc, String langName) {
        lang = Languages.getLanguage(langName);

        //get sorted node list
        List<Expression> parsedList = ParseUtilities.parseDoc(doc, langName);
        if (parsedList.size() == 0) {
            return null;
        }
        Expression expression = parsedList.get(0);
        if (parsedList.size() > 1 || (expression.getType() != ExpressionType.TERM && expression.getType() != ExpressionType.FORMULA)) {
            return null;
        }
        return subformulasExp(expression);
    }

    public static List<Expression> subformulasExp(Expression expression) {
        //sort nodes of parsed expression by level
        nodeList = new ArrayList<Expression>();
        listNodes(expression);
        Collections.sort(nodeList, new SortByLevel());

        List<Expression> subs = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).getType() == ExpressionType.FORMULA) {
                subs.add(nodeList.get(i));
            }
        }
        return subs;
    }

    public static List<Expression> allElementsDoc(Document doc, String langName) {
        lang = Languages.getLanguage(langName);

        //get sorted node list
        List<Expression> parsedList = ParseUtilities.parseDoc(doc, langName);
        if (parsedList.size() == 0) {
            return null;
        }
        Expression expression = parsedList.get(0);
        if (parsedList.size() > 1 || (expression.getType() != ExpressionType.TERM && expression.getType() != ExpressionType.FORMULA)) {
            return null;
        }
        return allElementsExp(expression);
    }

    public static List<Expression> allElementsExp(Expression expression) {
        //sort nodes of parsed expression by level
        nodeList = new ArrayList<Expression>();
        listNodes(expression);
        Collections.sort(nodeList, new SortByLevel());

        return nodeList;

    }




    public static Expression unabbreviate(Document doc, String langName) {
        lang = Languages.getLanguage(langName);


        //get parsed expression
        List<Expression> parsedList = ParseUtilities.parseDoc(doc, langName);
        if (parsedList.size() == 0) {
            return null;
        }
        Expression expression = parsedList.get(0);
        if (parsedList.size() > 1 || (expression.getType() != ExpressionType.TERM && expression.getType() != ExpressionType.FORMULA)) {
            return null;
        }

        //sort nodes of parsed expression by level
        nodeList = new ArrayList<Expression>();
        listNodes(parsedList.get(0));
        Collections.sort(nodeList, new SortByLevel());



        //get unabbrevating forms for expressions in sorted nodeList
        List<List<OriginalElement>> formList = listForms();

        //get unabbreviated expressions for members of the sorted node list
        List<Expression> expressionList = listUnabbExpressions(formList);



        return expressionList.get(expressionList.size() - 1);

    }

    private static List<Expression> listUnabbExpressions(List<List<OriginalElement>> formsList) {

        List<Expression> unAbbList = new ArrayList<>();


        for (int i = 0; i < formsList.size(); i++) {
            List<Expression> populatedForm = new ArrayList<>();

            //get the original form
            List<OriginalElement> originalFormElements = formsList.get(i);

            //re populate the form
            for (int j = 0; j < originalFormElements.size(); j++) {

                if (originalFormElements.get(j).getElementStr().equals("|") && j + 2 < originalFormElements.size() && originalFormElements.get(j + 2).getElementStr().equals("|")) {
                    //get children of expression at index i from the original node list
                    Expression abbExpression = nodeList.get(i);
                    List<Expression> abbChildren = abbExpression.getChildren();
                    String control = originalFormElements.get(j + 1).getElementStr();
                    if (control.equals("0")) {
                        //find the child index
                        int index = nodeList.indexOf(abbChildren.get(0));
                        //add element at that same index to the form list
                        populatedForm.add(unAbbList.get(index));
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                    }
                    else if (control.equals("1")) {
                        int index = nodeList.indexOf(abbChildren.get(1));
                        populatedForm.add(unAbbList.get(index));
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                        continue;
                    }
                    else if (control.equals("n")) {
                        for (int k = 0; k < abbChildren.size(); k++) {
                            int index = nodeList.indexOf(abbChildren.get(k));
                            populatedForm.add((Expression) unAbbList.get(index));
                        }
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                    }
                }
                else if (originalFormElements.get(j).getElementStr().equals("\u2039") && j + 2 < originalFormElements.size() && originalFormElements.get(j + 2).getElementStr(). equals("\u203a")) {

                    Expression abbExpression = nodeList.get(i);
                    String control = originalFormElements.get(j + 1).getElementStr();
                    if (control.equals("s")) {
                        populatedForm.add(abbExpression);
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                    }
                    else if (control.equals("v")) {
                        Operator abbOperator = ((Formula) abbExpression).getMainOperator();
                        populatedForm.add((Expression) abbOperator.getVariableTerm());
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                    }
                    else if (control.equals("f")) {
                        Term abbTerm = ((Term) abbExpression);
                        populatedForm.add((Expression) abbTerm.getMainFnSymbol());
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                    }
                    else if (control.equals("b")) {
                        Formula abbFormula = ((Formula) abbExpression);
                        populatedForm.add((Expression) abbFormula.getMainOperator());
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                    }

                    else if (control.equals("r")) {
                        Formula abbFormula = ((Formula) abbExpression);
                        RelationSymbol relSym = abbFormula.getMainRelation();
                        relSym.setType(ExpressionType.RELATION_SYMBOL);
                        if (lang.getXrelationSymbols().contains(relSym.getBaseStr()) && relSym.getSuperscriptStr().isEmpty())  relSym.setSuperscriptStr(String.valueOf(relSym.getPlaces()));
                        populatedForm.add((Expression) relSym);
                        originalFormElements.remove(j + 1); originalFormElements.remove(j + 1);
                    }
                }
                else {
                    populatedForm.add(originalFormElements.get(j));
                }
            }

            List<Expression> parsedUnabbFormula = ParseUtilities.parseExpressions(populatedForm);
            if (parsedUnabbFormula.size() == 1) {
                unAbbList.add(parsedUnabbFormula.get(0));
            }
            else {
                for (Expression e : parsedUnabbFormula) {
          //          System.out.println("element: " + e.toString());
                }
          //      System.out.println();
            }
        }
        return unAbbList;
    }

    //from nodeList generate list with unabbrevationf form for each element.
    private static List<List<OriginalElement>> listForms() {
        List<List<OriginalElement>> forms = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            Expression expression = nodeList.get(i);
            if (expression.getType() == ExpressionType.TERM) {
                Term term = (Term) expression;
                if (term.getTermType() == TermType.CONSTANT) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.CONSTANT)));
                    continue;
                }
                if (term.getTermType() == TermType.VARIABLE) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.VARIABLE)));
                    continue;
                }
                if (term.getTermType() == TermType.COMPLEX) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.COMPLEX_TERM)));
                    continue;
                }
            }
            else if (expression.getType() == ExpressionType.FORMULA) {
                Formula formula = (Formula) expression;
                if (formula instanceof SentenceAtomic) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.SENTENCE_LET)));
                    continue;
                }
                if (formula.isAtomic()) {                                                 //relation atomic
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.RELATION_ATOMIC)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.NEG_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.NEGATION)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.COND_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.CONDITIONAL)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.BICOND_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.BICONDITIONAL)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.CONJ_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.CONJUNCTION)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.DISJ_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.DISJUNCTION)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.NAND_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.NAND)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.NOR_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.NOR)));
                    continue;
                }

                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.UNIVERSAL_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.UNIVERSAL)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.EXISTENTIAL_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.EXISTENTIAL)));
                    continue;
                }


                if (((Operator) formula.getMainOperator()).getType() == ExpressionType.UNIV_BOUNDED_OP ||
                        ((Operator) formula.getMainOperator()).getType() == ExpressionType.UNIV_RESTRICTED_OP ||
                        ((Operator) formula.getMainOperator()).getType() == ExpressionType.EXIS_BOUNDED_OP ||
                        ((Operator) formula.getMainOperator()).getType() == ExpressionType.EXIS_RESTRICTED_OP
                ) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.BOUNDED_QUANTIFIER)));
                    continue;
                }



                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.NAND_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.NAND)));
                    continue;
                }
                if (((Operator)formula.getMainOperator()).getType() == ExpressionType.NOR_OP) {
                    forms.add(ParseUtilities.getElements(lang.getUnabbForms().get(UnabbType.NOR)));
                    continue;
                }
            }
        }
        return forms;
    }

    private static void listNodes(Expression exp) {

        nodeList.add(exp);
        if (exp.getChildren() != null && exp.getLevel() > 0) {
            for (int i = 0; i < exp.getChildren().size(); i++) {
                listNodes(exp.getChildren().get(i));
            }
        }
    }

    private static void expressionListPopup(String messageLead, List<Expression> expressionList ) {
        List<Text> texts = new ArrayList<>();
        texts.add(ParseUtilities.newRegularText(messageLead + "\n\n"));
        for (Expression expr : expressionList) {
            texts.add(new Text(expr.getType() + ": "));
            texts.addAll(expr.toTextList());
            texts.add(new Text("\n"));
        }
        Text[] txt = new Text[texts.size()];
        texts.toArray(txt);
        EditorAlerts.showSimpleTxtFlowAlert("Parser Content", new TextFlow(txt));
    }

    static class SortByLevel implements Comparator<Expression> {
        @Override
        public int compare(Expression o1, Expression o2) {
            return Integer.compare(o1.getLevel(), o2.getLevel());
        }
    }



}
