package slapp.editor.parser;

import com.gluonhq.richtextarea.model.Document;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SyntacticalFns {
    private static List<Expression> nodeList = new ArrayList<>();
    private static Language lang;

    public SyntacticalFns() {}


    public static Expression mainOperator(Document doc, String langName) {
        List<Expression> subformulas = subFormulas(doc, langName);
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
        List<Expression> subformulas = subFormulas(doc, langName);
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
        List<Expression> subformulas = subFormulas(doc, langName);
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

    public static List<Expression> subFormulas(Document doc, String langName) {
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

        //sort nodes of parsed expression by level
        nodeList = new ArrayList<Expression>();
        listNodes(parsedList.get(0));
        Collections.sort(nodeList, new SortByLevel());

        List<Expression> subs = new ArrayList<>();
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).getType() == ExpressionType.FORMULA) {
                subs.add(nodeList.get(i));
            }
        }
        return subs;
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

        for (Expression e : nodeList) {
            System.out.println("Parse list: " + e + " (" + e.getLevel() + ")");
        }
        System.out.println();

        //get unabbrevating forms for expressions in sorted nodeList
        List<List<OriginalElement>> formList = listForms();

        //get unabbreviated expressions for members of the sorted node list
        List<Expression> expressionList = listExpressions(formList);

        for (Expression e : expressionList) {
            System.out.println("Unabb list: " + e);
        }
        System.out.println();
        return expressionList.get(expressionList.size() - 1);

    }

    private static List<Expression> listExpressions(List<List<OriginalElement>> formsList) {

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
                System.out.println("parse failure");
                for (Expression e : parsedUnabbFormula) {
                    System.out.println("element: " + e.toString());
                }
                System.out.println();
            }
        }
        return unAbbList;
    }

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
                System.out.println("List forms term error");
            }
            else if (expression.getType() == ExpressionType.FORMULA) {
                Formula formula = (Formula) expression;
                if (formula instanceof SentenceAtomc) {
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
                System.out.println("List forms formula error");
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
