package slapp.editor.parser;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchUtilities {

    private static boolean relaxBracketMatch = true;
    private static List<Pair<Formula, Formula>> transformList = new ArrayList<>();
    private static List<Expression> matchedInstances = new ArrayList<>();
    private static List<Expression> allInstances = new ArrayList<>();



    public static boolean formMatch(Expression metaExp, Expression objectExp, String objectL, String metaL) throws TextMessageException {
        clearMatching();
        transformList.clear();
        matchedInstances.clear();
        allInstances.clear();

        if ((metaExp.getType() == ExpressionType.TERM && objectExp.getType() == ExpressionType.TERM) ||
                (metaExp.getType() == ExpressionType.FORMULA && objectExp.getType() == ExpressionType.FORMULA))
        {
            //conditionally compensate for dropped outer brackets by matching meta expression to object expression
            if (relaxBracketMatch) {
                if (metaExp.getType() == ExpressionType.TERM) {
                    Term mTerm = (Term) metaExp;
                    Term oTerm = (Term) objectExp;
                    if (mTerm.getOpenBracket().getType() != null && mTerm.getCloseBracket().getType() != null && oTerm.getOpenBracket().getType() == null && oTerm.getCloseBracket().getType() == null) {
                        mTerm.setOpenBracket(new OpenBracket(""));
                        mTerm.setCloseBracket(new CloseBracket(""));
                    }
                }
                if (metaExp.getType() == ExpressionType.FORMULA) {
                    Formula mFormula = (Formula) metaExp;
                    Formula oFormula = (Formula) objectExp;
                    if (mFormula.getOpenBracket().getType() != null && mFormula.getCloseBracket().getType() != null && oFormula.getOpenBracket().getType() == null && oFormula.getCloseBracket().getType() == null) {
                        mFormula.setOpenBracket(new OpenBracket(""));
                        mFormula.setCloseBracket(new CloseBracket(""));
                    }
                }
            }

            try {
                setMatching(metaExp, objectExp);

                processTransforms(objectL);


     //           System.out.println("meta exp: " + metaExp + " match: " + metaExp.getMatch());

                boolean match = metaExp.getMatch().equals(objectExp);

                if (!match) {
                    throw new TextMessageException(getMessageTexts(metaExp.getMatch(), objectExp, "Mapped expression ", " not the same as ", "."));
                }

                return match;

            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Map Issue", e.getMessageList());
                return false;
            }
        }
        else {
            throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " and ", " do not both parse to formulas or both to terms."));
        }
    }

    private static void processTransforms(String objectL) throws TextMessageException {
        for (int i = transformList.size() - 1; i >= 0; i--) {

  //          System.out.println(transformList.get(i));

            Pair<Formula, Formula> transformPair = transformList.get(i);

            Formula metaForm = transformPair.getKey();
            SubstitutionTransform subTransform = metaForm.getSubTransform();
            Formula targetFormula = transformPair.getValue();

            Expression sourceExp = metaForm.getChildren().get(0).getMatch();
            Expression exp1 = subTransform.getExp1().getMatch();

            if (sourceExp != null && !sourceExp.toString().equals("")) {

                if (exp1 != null && !exp1.toString().equals("")) {

                    findReplacements(sourceExp, targetFormula, subTransform);

         //            if (!allInstances.isEmpty()) {


                         boolean allFree = true;
                         for (Expression exp : allInstances) {
                             if (SyntacticalFns.particularTermsFreeInFormula(sourceExp, Collections.singletonList(exp), objectL) && !SyntacticalFns.listContainsParticular(matchedInstances, exp)) {
                                 allFree = false;
                                 break;
                             }
                         }

                         if (matchedInstances.isEmpty()) {
                         } else if (subTransform.getType() == ExpressionType.ONE_TERM_SUB) {
                             if (matchedInstances.size() != 1)
                                 throw new TextMessageException(getMessageTexts(subTransform.getExp1().getMatch(), subTransform.getExp2().getMatch(), "More than one instance of ", " is replaced by ", "."));
                         } else if (subTransform.getType() == ExpressionType.ALL_TERM_SUB) {
                             if (!allFree)
                                 throw new TextMessageException(getMessageTexts(subTransform.getExp1().getMatch(), subTransform.getExp2().getMatch(), "Not every free instance of ", " is replaced by ", "."));
                         }

              //           System.out.println("source Exp: " + sourceExp + " target formula: " + targetFormula + "transform match: " + subTransform.getExp2().getMatch() + " matched instances: " + matchedInstances + " all instances: " + allInstances);

                         Expression subExp = SyntacticalFns.substituteParticularTerms(sourceExp, subTransform.getExp2().getMatch(), matchedInstances);

                         metaForm.setMatchFormula((Formula) subExp);
                         /*

                     }
                     else {
                         throw new TextMessageException(getMessageTexts(subTransform, subTransform.getExp1(), "Cannot map ", "without prior specification of ", "."));
                     }

                          */
                }
                else {
                    throw new TextMessageException(getMessageTexts(subTransform, subTransform.getExp1(), "Cannot map ", "without prior specification of ", "."));
                }
            }
            else {
                throw new TextMessageException(getMessageTexts(metaForm, metaForm.getChildren().get(0), "Cannot map ", " without prior specification of ", "."));
            }



        }
    }

    private static void findReplacements(Expression sourceExp, Expression targetExp, SubstitutionTransform subTransform) throws TextMessageException {

        System.out.println("source: " + sourceExp + " target: " + targetExp + " trans: " + subTransform);

        Expression exp1 = subTransform.getExp1().getMatch();
        Expression exp2 = subTransform.getExp2();



        if (sourceExp != null && sourceExp.equals(exp1)) {
            allInstances.add(sourceExp);
            if (!sourceExp.equals(targetExp)) {
                setMatching(exp2, targetExp);
                matchedInstances.add(sourceExp);

 //               System.out.println("match exp2: " + exp2 + " targetExp: " + targetExp);
            }
            return;
        }
        else if (sourceExp instanceof Term && ((Term) sourceExp).getTermType() == TermType.VARIABLE && targetExp instanceof Term && ((Term) targetExp).getTermType() == TermType.VARIABLE) return;
        else if (sourceExp instanceof Term && ((Term) sourceExp).getTermType() == TermType.CONSTANT && targetExp instanceof Term && ((Term) targetExp).getTermType() == TermType.CONSTANT) return;
        else if (sourceExp instanceof MTerm && sourceExp instanceof MTerm) return;
        else if (sourceExp instanceof Term && ((Term) sourceExp).getTermType() == TermType.COMPLEX && targetExp instanceof Term && ((Term) targetExp).getTermType() == TermType.COMPLEX &&
                sourceExp instanceof InfixTerm == targetExp instanceof InfixTerm &&
                ((Term) sourceExp).getMainFnSymbol().equals(((Term) targetExp).getMainFnSymbol()) &&
                bracketMatch (((Term) sourceExp).getOpenBracket(), ((Term) sourceExp).getCloseBracket(), ((Term) targetExp).getOpenBracket(), ((Term) targetExp).getCloseBracket()) &&
                sourceExp.getChildren() != null && targetExp.getChildren() != null && sourceExp.getChildren().size() == targetExp.getChildren().size()) {
            for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                    findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform);
                }
            }
            return;
        }
        else if (sourceExp instanceof SentenceAtomic && targetExp instanceof SentenceAtomic) return;
        else if (sourceExp instanceof MFormula && targetExp instanceof MFormula &&
                ((MFormula) sourceExp).getFormulaSym().equals(((MFormula) targetExp).getFormulaSym())) {
            if (sourceExp.getChildren() != null && targetExp.getChildren() != null && sourceExp.getChildren().size() == targetExp.getChildren().size()) {
                for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                    if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                        findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform);
                    }
                }
            }
            return;
        }
        else if (sourceExp instanceof PrefixAtomic && targetExp instanceof PrefixAtomic &&
                ((PrefixAtomic) sourceExp).getMainRelation().equals(((PrefixAtomic) targetExp).getMainRelation()) &&
                sourceExp.getChildren().size() == targetExp.getChildren().size()) {
            for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                    findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform);
                }
            }
            return;
        }
        else if (sourceExp instanceof InfixAtomic && targetExp instanceof InfixAtomic &&
                ((InfixAtomic) sourceExp).getMainRelation().equals(((InfixAtomic) targetExp).getMainRelation()) &&
                sourceExp.getChildren().size() == targetExp.getChildren().size()) {
            for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                    findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform);
                }
            }
            return;
        }
        else if (sourceExp instanceof Formula && targetExp instanceof Formula && !((Formula) sourceExp).isAtomic() && !((Formula) targetExp).isAtomic() &&
                bracketMatch(((Formula) sourceExp).getOpenBracket(), ((Formula) sourceExp).getCloseBracket(), ((Formula) targetExp).getOpenBracket(), ((Formula) targetExp).getCloseBracket()) &&
                sourceExp.getChildren().size() == targetExp.getChildren().size()) {
            Formula sourceFormula = (Formula) sourceExp;
            Formula targetFormula = (Formula) targetExp;
            boolean good = false;
            if (sourceFormula.getMainOperator() instanceof UnivBoundedQuantOp && targetFormula.getMainOperator() instanceof UnivBoundedQuantOp) {
                findReplacements(((UnivBoundedQuantOp) sourceFormula.getMainOperator()).getBoundingTerm(), ((UnivBoundedQuantOp) targetFormula.getMainOperator()).getBoundingTerm(), subTransform);
                good = true;
            }
            else if (sourceFormula.getMainOperator() instanceof ExisBoundedQuantOp && targetFormula.getMainOperator() instanceof ExisBoundedQuantOp) {
                findReplacements(((ExisBoundedQuantOp) sourceFormula.getMainOperator()).getBoundingTerm(), ((ExisBoundedQuantOp) targetFormula.getMainOperator()).getBoundingTerm(), subTransform);
                good = true;
            }
            else if (sourceFormula.getMainOperator() instanceof UnivRestrictedQuantOp && targetFormula.getMainOperator() instanceof UnivRestrictedQuantOp) {
                findReplacements(((UnivRestrictedQuantOp) sourceFormula.getMainOperator()).getRestrictingFormula(), ((UnivRestrictedQuantOp) targetFormula.getMainOperator()).getRestrictingFormula(), subTransform);
                good = true;
            }
            else if (sourceFormula.getMainOperator() instanceof ExisRestrictedQuantOp && targetFormula.getMainOperator() instanceof ExisRestrictedQuantOp) {
                findReplacements(((ExisRestrictedQuantOp) sourceFormula.getMainOperator()).getRestrictingFormula(), ((ExisRestrictedQuantOp) targetFormula.getMainOperator()).getRestrictingFormula(), subTransform);
                good = true;
            }
            else if (sourceFormula.getMainOperator().equals(targetFormula.getMainOperator())) {
                good = true;
            }
            if (good) {
                for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                    if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                        findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform);
                    }
                }
            }
        }
        else {
            throw new TextMessageException(getMessageTexts(sourceExp, targetExp, "", " does not match ", "."));
        }
    }

    private static void setMatching(Expression metaExp, Expression objectExp) throws TextMessageException {
        boolean skip = false;

  //      System.out.println("meta exp: " + metaExp + " object exp: " + objectExp);

        if (metaExp.getType() != ExpressionType.FORMULA || (metaExp.getType() == ExpressionType.FORMULA && ((Formula) metaExp).getSubTransform() == null)) {



            if (metaExp instanceof Term && ((Term) metaExp).getTermType() == TermType.VARIABLE && objectExp instanceof Term && ((Term) objectExp).getTermType() == TermType.VARIABLE)
                ((MVariable) ((Term) metaExp).getChildren().get(0)).setMatch(((Variable) ((Term) objectExp).getChildren().get(0)));

            else if (metaExp instanceof Term && ((Term) metaExp).getTermType() == TermType.CONSTANT && objectExp instanceof Term && ((Term) objectExp).getTermType() == TermType.CONSTANT)
                ((MConstant) ((Term) metaExp).getChildren().get(0)).setMatch(((Constant) ((Term) objectExp).getChildren().get(0)));

            else if (metaExp instanceof MTerm && objectExp instanceof Term)
                ((MTerm) metaExp).setMatch((Term) objectExp);

            else if (metaExp instanceof Term && objectExp instanceof Term && ((Term) metaExp).getTermType() == TermType.COMPLEX && ((Term) objectExp).getTermType() == TermType.COMPLEX &&
                    metaExp instanceof InfixTerm == objectExp instanceof InfixTerm &&
                    bracketMatch(((Term) metaExp).getOpenBracket(), ((Term) metaExp).getCloseBracket(), ((Term) objectExp).getOpenBracket(), ((Term) objectExp).getCloseBracket()) &&
                    metaExp.getChildren().size() == objectExp.getChildren().size()) {
                Term mt = (Term) metaExp;
                Term ot = (Term) objectExp;
                mt.setOpenBracket(ot.getOpenBracket());
                mt.setCloseBracket(ot.getCloseBracket());
                if (mt.getMainFnSymbol() instanceof MFunctionSymbol)
                    ((MFunctionSymbol) mt.getMainFnSymbol()).setMatch(ot.getMainFnSymbol());
                else if (!mt.getMainFnSymbol().getMatch().equals(ot.getMainFnSymbol())) {
                    throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
                }
            }
            else if (metaExp instanceof SentenceAtomic && objectExp instanceof SentenceAtomic)
                ((MSentenceLetter) ((SentenceAtomic) metaExp).getMainLetter()).setMatch(((SentenceLetter) ((SentenceAtomic) objectExp).getMainLetter()));

            else if (metaExp instanceof MFormula && objectExp instanceof Formula)
                ((MFormula) metaExp).setMatch((Formula) objectExp);

            else if (metaExp instanceof PrefixAtomic && objectExp instanceof PrefixAtomic && metaExp.getChildren().size() == objectExp.getChildren().size()) {
                PrefixAtomic metaAtomic = (PrefixAtomic) metaExp;
                PrefixAtomic objectAtomic = (PrefixAtomic) objectExp;
                if (metaAtomic.getMainRelation() instanceof MRelationSymbol)
                    ((MRelationSymbol) metaAtomic.getMainRelation()).setMatch(objectAtomic.getMainRelation());
                else if (!metaAtomic.getMainRelation().getMatch().equals(objectAtomic.getMainRelation())) {
                    throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
                }
            } else if (metaExp instanceof InfixAtomic && objectExp instanceof InfixAtomic && metaExp.getChildren().size() == objectExp.getChildren().size()) {
                //    bracketMatch(((InfixAtomic) metaExp).getOpenBracket(), ((InfixAtomic) metaExp).getCloseBracket(), ((InfixAtomic) objectExp).getOpenBracket(), ((InfixAtomic) objectExp).getCloseBracket())) {
                InfixAtomic metaAtomic = (InfixAtomic) metaExp;
                InfixAtomic objectAtomic = (InfixAtomic) objectExp;
                if (!metaAtomic.getMainRelation().getMatch().equals(objectAtomic.getMainRelation())) {
                    throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
                }
            } else if (metaExp instanceof Formula && objectExp instanceof Formula && !((Formula) metaExp).isAtomic() && !((Formula) objectExp).isAtomic() &&
                    bracketMatch(((Formula) metaExp).getOpenBracket(), ((Formula) metaExp).getCloseBracket(), ((Formula) objectExp).getOpenBracket(), ((Formula) objectExp).getCloseBracket()) &&
                    metaExp.getChildren().size() == objectExp.getChildren().size()) {
                ((Formula) metaExp).setOpenBracket(((Formula) objectExp).getOpenBracket());
                ((Formula) metaExp).setCloseBracket(((Formula) objectExp).getCloseBracket());
                if (((Formula) metaExp).getMainOperator() instanceof UniversalOp && ((Formula) objectExp).getMainOperator() instanceof UniversalOp) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                }
                if (((Formula) metaExp).getMainOperator() instanceof ExistentialOp && ((Formula) objectExp).getMainOperator() instanceof ExistentialOp) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                }
                if (((Formula) metaExp).getMainOperator() instanceof UnivBoundedQuantOp && ((Formula) objectExp).getMainOperator() instanceof UnivBoundedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                    setMatching( ((UnivBoundedQuantOp) ((Formula) metaExp).getMainOperator()).getBoundingTerm(), ((UnivBoundedQuantOp) ((Formula) objectExp).getMainOperator()).getBoundingTerm());
                }
                if (((Formula) metaExp).getMainOperator() instanceof ExisBoundedQuantOp && ((Formula) objectExp).getMainOperator() instanceof ExisBoundedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                    setMatching( ((ExisBoundedQuantOp) ((Formula) metaExp).getMainOperator()).getBoundingTerm(), ((ExisBoundedQuantOp) ((Formula) objectExp).getMainOperator()).getBoundingTerm());
                }
                if (((Formula) metaExp).getMainOperator() instanceof UnivRestrictedQuantOp && ((Formula) objectExp).getMainOperator() instanceof UnivRestrictedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                    setMatching( ((UnivRestrictedQuantOp) ((Formula) metaExp).getMainOperator()).getRestrictingFormula(), ((UnivRestrictedQuantOp) ((Formula) objectExp).getMainOperator()).getRestrictingFormula());
                }
                if (((Formula) metaExp).getMainOperator() instanceof ExisRestrictedQuantOp && ((Formula) objectExp).getMainOperator() instanceof ExisRestrictedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                    setMatching( ((ExisRestrictedQuantOp) ((Formula) metaExp).getMainOperator()).getRestrictingFormula(), ((ExisRestrictedQuantOp) ((Formula) objectExp).getMainOperator()).getRestrictingFormula());
                }
                if (!((Formula) metaExp).getMainOperator().getMatch().equals(((Formula) objectExp).getMainOperator())) {
                    throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
                }

            } else {
                throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
            }
        }
        else {
            //process sub
            if (objectExp instanceof Formula) {
                Pair<Formula, Formula> pair = new Pair((Formula) metaExp, (Formula) objectExp);
                transformList.add(pair);
                skip = true;
            }
        }

       if (!skip && metaExp.getChildren() != null && metaExp.getLevel() > 0 && metaExp.getChildren().size() == objectExp.getChildren().size() ){

           for (int i = 0; i < metaExp.getChildren().size(); i++) {
               if (metaExp.getChildren().get(i).getType() != ExpressionType.FORMULA || (metaExp.getChildren().get(i).getType() == ExpressionType.FORMULA && ((Formula) metaExp.getChildren().get(i)).getSubTransform() == null)) {
                   setMatching(metaExp.getChildren().get(i), objectExp.getChildren().get(i));
               }
               else {
                   //process sub
                   if (objectExp.getChildren().get(i) instanceof Formula) {
                       Pair<Formula, Formula> pair = new Pair((Formula) metaExp.getChildren().get(i), (Formula) objectExp.getChildren().get(i));
                       transformList.add(pair);
                   }
               }
           }
       }
    }


    private static boolean bracketMatch(OpenBracket mOpen, CloseBracket mClose, OpenBracket oOpen, CloseBracket oClose) {
        if (!relaxBracketMatch) {
            return mOpen.equals(oOpen) && mClose.equals(oClose);
        }
        else {
            boolean match = (mOpen.equals(oOpen) && mClose.equals(oClose)) ||
                    ((mOpen.getType() == ExpressionType.OPEN_BRACKET1 && mClose.getType() == ExpressionType.CLOSE_BRACKET1 ||
                    mOpen.getType() == ExpressionType.OPEN_BRACKET2 && mClose.getType() == ExpressionType.CLOSE_BRACKET2 ||
                    mOpen.getType() == ExpressionType.OPEN_BRACKET3 && mClose.getType() == ExpressionType.CLOSE_BRACKET3 )
                    &&
                    (oOpen.getType() == ExpressionType.OPEN_BRACKET1 && oClose.getType() == ExpressionType.CLOSE_BRACKET1 ||
                    oOpen.getType() == ExpressionType.OPEN_BRACKET2 && oClose.getType() == ExpressionType.CLOSE_BRACKET2 ||
                    oOpen.getType() == ExpressionType.OPEN_BRACKET3 && oClose.getType() == ExpressionType.CLOSE_BRACKET3 ));
            return match;
        }
    }




    public static List<Text> getMessageTexts(Expression exp1, Expression exp2, String lead, String middle, String close) {
        List<Text> texts = new ArrayList<Text>();

        texts.add(new Text(lead));
        texts.addAll(exp1.toTextList());
        texts.add(new Text(middle));
        texts.addAll(exp2.toTextList());
        texts.add(new Text(close));
        return texts;
    }

    public static void clearMatching() {
        MVariable.clear();
        MFormula.clear();
        MTerm.clear();
        MConstant.clear();
        MFunctionSymbol.clear();
        MRelationSymbol.clear();
        MSentenceLetter.clear();
        MAnyExpression.clear();
    }

}
