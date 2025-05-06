package slapp.editor.parser;

import com.gluonhq.richtextarea.model.Document;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.ReplacementTxtMsgException;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchUtilities {

    private static boolean relaxBracketMatch = true;
    private static List<Pair<Expression, Expression>> transformList = new ArrayList<>();
    private static List<Expression> matchedInstances = new ArrayList<>();
    private static List<Expression> allInstances = new ArrayList<>();
    private static Boolean subFreeFor = true;
    private static List<Formula> replacementList = new ArrayList<>();


    /*
    For replacement check if one form has more "content" than another
    (as for abbreviation of inequality where u does not appear in abbreviation)
    f1 is the form with greater content.  This is because matching on the first
    form is used to identify a matches for the second.
     */
    public static boolean replacementCheck(Expression f1, Expression f2, Expression s, Expression t) throws TextMessageException {
        replacementList.clear();


        if (f1.getType() == ExpressionType.FORMULA && f2.getType() == ExpressionType.FORMULA && s.getType() == ExpressionType.FORMULA && t.getType() == ExpressionType.FORMULA) {
            Formula form1 = (Formula) f1;
            Formula form2 = (Formula) f2;
            Formula source = (Formula) s;
            Formula target = (Formula) t;


            //check with outer brackets on forms and formulas
            setParens(form1, form2, source, target);

            try {
                checkReplacements(form1, form2, source, target);
            }
            catch (TextMessageException e) { throw new TextMessageException(e.getMessageList());  }

        }
        else {
            throw new TextMessageException(getMessageTexts(null, null, "Replacement check applies just to formulas.", "", ""));
        }

        boolean replacementMade = false;
        if (replacementList.size() == 1) { replacementMade = true; }

        else if (replacementList.size() == 0) {
            throw new TextMessageException(getMessageTexts(null, null, "No replacements made.", "", ""));
        }
        else if (replacementList.size() > 1) {
            throw new TextMessageException(getMessageTexts(null, null, "More than one replacement is made.", "", ""));
        }

        return replacementMade;
    }

    private static void setParens(Formula form1, Formula form2, Formula form3, Formula form4) {
        ExpressionType opType = null;
        OpenBracket emptyOpen = new OpenBracket("");
        CloseBracket emptyClose = new CloseBracket("");

        if (form1.getMainOperator() != null) opType = form1.getMainOperator().getType();
        if (opType == ExpressionType.COND_OP || opType == ExpressionType.BICOND_OP || opType == ExpressionType.CONJ_OP || opType == ExpressionType.DISJ_OP ||
                opType == ExpressionType.NAND_OP || opType == ExpressionType.NOR_OP) {
            if (form1.getOpenBracket().equals(emptyOpen)) form1.setOpenBracket(new OpenBracket1("("));
            if (form1.getCloseBracket().equals(emptyClose)) form1.setCloseBracket(new CloseBracket1(")"));
        }


        if (form2.getMainOperator() != null) opType = form2.getMainOperator().getType();
        if (opType == ExpressionType.COND_OP || opType == ExpressionType.BICOND_OP || opType == ExpressionType.CONJ_OP || opType == ExpressionType.DISJ_OP ||
                opType == ExpressionType.NAND_OP || opType == ExpressionType.NOR_OP) {
            if (form2.getOpenBracket().equals(emptyOpen)) form2.setOpenBracket(new OpenBracket1("("));
            if (form2.getCloseBracket().equals(emptyClose)) form2.setCloseBracket(new CloseBracket1(")"));
        }

        if (form3.getMainOperator() != null) opType = form3.getMainOperator().getType();
        if (opType == ExpressionType.COND_OP || opType == ExpressionType.BICOND_OP || opType == ExpressionType.CONJ_OP || opType == ExpressionType.DISJ_OP ||
                opType == ExpressionType.NAND_OP || opType == ExpressionType.NOR_OP) {
            if (form3.getOpenBracket().equals(emptyOpen)) form3.setOpenBracket(new OpenBracket1("("));
            if (form3.getCloseBracket().equals(emptyClose)) form3.setCloseBracket(new CloseBracket1(")"));
        }

        if (form4.getMainOperator() != null) opType = form4.getMainOperator().getType();
        if (opType == ExpressionType.COND_OP || opType == ExpressionType.BICOND_OP || opType == ExpressionType.CONJ_OP || opType == ExpressionType.DISJ_OP ||
                opType == ExpressionType.NAND_OP || opType == ExpressionType.NOR_OP) {
            if (form4.getOpenBracket().equals(emptyOpen)) form4.setOpenBracket(new OpenBracket1("("));
            if (form4.getCloseBracket().equals(emptyClose)) form4.setCloseBracket(new CloseBracket1(")"));
        }
    }

    private static void checkReplacements(Formula form1, Formula form2, Formula source, Formula target) throws TextMessageException {

        clearMatching();
        try {
            setMatching(form1, source);
        }
        catch (TextMessageException e) {     }


        if (form2.getMatch() != null && form2.getMatch().equals(target)) {
            if (!replacementList.contains(form2.getMatch())) replacementList.add(form2.getMatch());
            return;
        }

        else if (source instanceof SentenceAtomic && target instanceof SentenceAtomic && source.equals(target)) { return;  }

        else if (source instanceof MFormula && target instanceof MFormula && source.equals(target)) { return; }

        else if (source instanceof PrefixAtomic && target instanceof PrefixAtomic && source.equals(target)) { return; }

        else if (source instanceof InfixAtomic && target instanceof InfixAtomic && source.equals(target)) { return; }

        else if (source instanceof Formula && target instanceof Formula && !source.isAtomic() && !target.isAtomic() &&
                bracketMatch(source.getOpenBracket(), source.getCloseBracket(), target.getOpenBracket(), target.getCloseBracket()) &&
                source.getChildren().size() == target.getChildren().size()) {
            boolean good = false;
            if (source.getMainOperator() instanceof UnivRestrictedQuantOp && target.getMainOperator() instanceof UnivRestrictedQuantOp) {
                Formula qSource = ((UnivRestrictedQuantOp) source.getMainOperator()).getRestrictingFormula();
                Formula qTarget = ((UnivRestrictedQuantOp) target.getMainOperator()).getRestrictingFormula();
                setParens(form1, form2, qSource, qTarget);
                checkReplacements(form1, form2, qSource, qTarget);
                good = true;
            }
            else if (source.getMainOperator() instanceof ExisRestrictedQuantOp && target.getMainOperator() instanceof ExisRestrictedQuantOp) {
                Formula qSource = ((ExisRestrictedQuantOp) source.getMainOperator()).getRestrictingFormula();
                Formula qTarget = ((ExisRestrictedQuantOp) target.getMainOperator()).getRestrictingFormula();
                setParens(form1, form2, qSource, qTarget);
                checkReplacements(form1, form2, qSource, qTarget);
                good = true;
            }
            else if (source.getMainOperator().equals(target.getMainOperator())) {
                good = true;
            }
            if (good) {
                for (int i = 0; i < source.getChildren().size(); i++) {
                    if (source.getChildren().get(i).getType() == ExpressionType.FORMULA && target.getChildren().get(i).getType() == ExpressionType.FORMULA) {
                        checkReplacements(form1, form2, (Formula) source.getChildren().get(i), (Formula) target.getChildren().get(i));
                    }
                }
            }
            else {
                throw new TextMessageException(getMessageTexts(target, source, "", " does not substitute into ", "."));
            }
        }

        else {
            throw new TextMessageException(getMessageTexts(target, source, "", " does not substitute into ", "."));
        }
    }

    public static void clearFormMatch() {
        clearMatching();
        transformList.clear();
        matchedInstances.clear();
        allInstances.clear();
        subFreeFor = true;
    }

    public static Pair<Boolean, Boolean> simpleFormMatch(Document metaDoc, Document objectDoc, String objectL, String metaL) throws TextMessageException {
        Expression metaExp = ParseUtilities.parseDoc(metaDoc, metaL).get(0);
        Expression objectExp = ParseUtilities.parseDoc(objectDoc, objectL).get(0);

//       System.out.println(ParseUtilities.parseDoc(metaDoc, metaL));

        transformList.clear();
        matchedInstances.clear();
        allInstances.clear();
        subFreeFor = true;
        replacementList.clear();


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
                    if (mFormula.getOpenBracket().getType() == null && mFormula.getCloseBracket().getType() == null && oFormula.getOpenBracket().getType() != null && oFormula.getCloseBracket().getType() != null) {
                        mFormula.setOpenBracket(new OpenBracket(Languages.getLanguage(metaL).getOpenBracket1()));
                        mFormula.setCloseBracket(new CloseBracket(Languages.getLanguage(metaL).getCloseBracket1()));
                    }
                }
            }


        setMatching(metaExp, objectExp);
        processTransforms(objectL);



 //       boolean formMatch = metaExp.getMatch().equals(objectExp);



        Pair<Boolean, Boolean> subResults = new Pair<>(true, subFreeFor);
        return subResults;
    }



    public static Pair<Boolean, Boolean> formMatch(Expression metaExp, Expression objectExp, String objectL, String metaL) throws TextMessageException {
  //      clearMatching();
        transformList.clear();
        matchedInstances.clear();
        allInstances.clear();
        subFreeFor = true;
        replacementList.clear();

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

  //          try {
                setMatching(metaExp, objectExp);
                processTransforms(objectL);

                boolean formMatch = metaExp.getMatch().equals(objectExp);

                Pair<Boolean, Boolean> subResults = new Pair<>(formMatch, subFreeFor);

                if (!formMatch) {
      //              throw new TextMessageException(getMessageTexts(metaExp.getMatch(), objectExp, "Mapped expression ", " not the same as ", "." ));
                }
                return subResults;

      //      }
            /*
            catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Map Issue", e.getMessageList());
                return false;
            }

             */
        }
        else {
            throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " and ", " do not both parse to formulas or both to terms."));
        }
    }

    private static void processTransforms(String objectL) throws TextMessageException {
        for (int i = transformList.size() - 1; i >= 0; i--) {

            Pair<Expression, Expression> transformPair = transformList.get(i);

            SubstitutionTransform subTransform;
            Expression metaExp = transformPair.getKey();
            if (metaExp.getType() == ExpressionType.FORMULA) subTransform = ((Formula) metaExp).getSubTransform();
            else subTransform = ((Term) metaExp).getSubTransform();
            Expression targetExp = transformPair.getValue();

            Expression sourceExp = metaExp.getChildren().get(0).getMatch();
            Expression exp1 = subTransform.getExp1().getMatch();

            boolean badReplacements = false;

            if (sourceExp != null) {
                if (exp1 != null) {
                    try {
                        findReplacements(sourceExp, targetExp, subTransform, objectL);

             //           System.out.println(" source: " + sourceExp + "target: " + targetExp.toString() + " transform: " + SyntacticalFns.substituteParticularTerms(sourceExp, subTransform.getExp2().getMatch(), matchedInstances));

            //            System.out.println("target: " + targetExp + " sub: " + SyntacticalFns.substituteParticularTerms(sourceExp, subTransform.getExp2().getMatch(), matchedInstances));
            //            System.out.println(matchedInstances);


                        if (!targetExp.equals(SyntacticalFns.substituteParticularTerms(sourceExp, subTransform.getExp2().getMatch(), matchedInstances))) {

                            badReplacements = true;
                        }
                    }
                    catch (ReplacementTxtMsgException e) {
                        throw new TextMessageException(e.getMessageList());

                    }
                    catch (TextMessageException e) {
                        badReplacements = true;

                    }

                    if (badReplacements) {
                        List<Text> list = new ArrayList<>();

                        if (subTransform.getExp2().getMatch() != null) {
                            list.addAll(targetExp.toTextList());
                            if (!objectL.equals("LM Obj")) list.add(ParseUtilities.newRegularText(" is not "));
                            else list.add(ParseUtilities.newRegularText(" is not (known to be) "));
                            list.addAll(sourceExp.toTextList());
                            list.addAll(new SubstitutionTransform(exp1, subTransform.getExp2().getMatch(), subTransform.getType(), subTransform.getDividerSymbol()).toTextList());
                            list.add(new Text("."));
                        }
                        else {
                            list.addAll(targetExp.toTextList());
                            if (!Languages.getLanguage(objectL).isObjectMetalanguage()) list.add(ParseUtilities.newRegularText(" is not "));
                            else list.add(ParseUtilities.newRegularText(" is not (known to be) "));
                            list.addAll(sourceExp.toTextList());
                            list.addAll(new SubstitutionTransform(exp1, subTransform.getExp2(), subTransform.getType(), subTransform.getDividerSymbol()).toTextList());
                            list.add(ParseUtilities.newRegularText(" for some "));
                            list.addAll(subTransform.getExp2().toTextList());
                            list.add(new Text("."));
                        }
                        throw new TextMessageException(list);

                    }

         //            if (!allInstances.isEmpty()) {

                         boolean allFree = true;
                         for (Expression exp : allInstances) {
                             if (SyntacticalFns.particularTermsFreeInFormula(sourceExp, Collections.singletonList(exp), objectL) && !SyntacticalFns.listContainsParticular(matchedInstances, exp)) {
                                 allFree = false;
                                 break;
                             }
                         }

                         if (matchedInstances.isEmpty()) {
                         }
                         else if (subTransform.getType() == ExpressionType.ONE_TERM_SUB) {
                             if (matchedInstances.size() != 1)
                                 throw new TextMessageException(getMessageTexts(subTransform.getExp1().getMatch(), subTransform.getExp2().getMatch(), "More than one instance of ", " is replaced by ", "."));
                         } else if (subTransform.getType() == ExpressionType.ALL_TERM_SUB) {
                             if (!allFree)
                                 throw new TextMessageException(getMessageTexts(subTransform.getExp1().getMatch(), subTransform.getExp2().getMatch(), "Not every free instance of ", " is replaced by ", "."));
                         }

                         Expression subExp = SyntacticalFns.substituteParticularTerms(sourceExp, subTransform.getExp2().getMatch(), matchedInstances);

                         if (metaExp.getType() == ExpressionType.FORMULA) { ((Formula) metaExp).setMatchFormula((Formula) subExp);}
                         else ((Term) metaExp).setMatchTerm((Term) subExp);

                         for (Expression exp : matchedInstances) {
                             if (SyntacticalFns.particularTermsFreeInFormula(sourceExp, Collections.singletonList(exp), objectL)) {



                                 if (!SyntacticalFns.freeForExp(sourceExp, exp, subTransform.getExp2().getMatch(), objectL)) {
                                     subFreeFor = false;
                                     List<Text> texts = new ArrayList<>();
                                     texts.add(ParseUtilities.newRegularText("Substituted instancess of "));
                                     texts.addAll(subTransform.getExp2().getMatch().toTextList());

                                     if (!Languages.getLanguage(objectL).isObjectMetalanguage()) texts.add(ParseUtilities.newRegularText(" not free for "));
                                     else texts.add(ParseUtilities.newRegularText(" not (known to be) free for "));
                                     texts.addAll(exp.toTextList());
                                     texts.add(ParseUtilities.newRegularText(" in "));
                                     texts.addAll(sourceExp.toTextList());
                                     texts.add(ParseUtilities.newRegularText("."));
                                     throw new TextMessageException(texts);

                               //      throw new TextMessageException(getMessageTexts(subTransform.getExp2().getMatch(), exp, "Substituted instance(s) of ", " not free for ", "."));
                                 }
                             }
                             else throw new TextMessageException(getMessageTexts(exp, sourceExp, "Replaced instnace(s) of ", " not free in ", "."));
                         }



                         /*
                     }
                     else {
                         throw new TextMessageException(getMessageTexts(subTransform, subTransform.getExp1(), "Cannot map ", "without prior specification of ", "."));
                     }

                          */
                }
                else {
                    throw new TextMessageException(getMessageTexts(subTransform, subTransform.getExp1(), "Cannot map ", " without prior specification of ", "."));
                }
            }
            else {
                throw new TextMessageException(getMessageTexts(metaExp, metaExp.getChildren().get(0), "Cannot map ", " without prior specification of ", "."));
            }



        }
    }

    private static void findReplacements(Expression sourceExp, Expression targetExp, SubstitutionTransform subTransform, String objectL) throws ReplacementTxtMsgException, TextMessageException {

     //   System.out.println("source: " + sourceExp + " target: " + targetExp + " transform: " + subTransform);

        Expression exp1 = subTransform.getExp1().getMatch();
        Expression exp2 = subTransform.getExp2();

    //    System.out.println("exp1: " + exp1 + " exp2: " + exp2);
    //    System.out.println("exp1 free in source: " + SyntacticalFns.expTermFreeInFormula(sourceExp, exp1, objectL));

        if (sourceExp instanceof Formula && exp1 instanceof Term && !SyntacticalFns.expTermFreeInFormula(sourceExp, exp1, objectL)) return;

        if (sourceExp != null && sourceExp.equals(exp1)) {
            allInstances.add(sourceExp);
            if (subTransform.getType() == ExpressionType.ALL_TERM_SUB || !sourceExp.equals(targetExp)) {  //if distinguish between original and sub, set matching when not the same.  Otherwise match.

                setMatching(exp2, targetExp);
                matchedInstances.add(sourceExp);
            }
            return;
        }
        else if (sourceExp instanceof Term && ((Term) sourceExp).getTermType() == TermType.VARIABLE && targetExp instanceof Term && ((Term) targetExp).getTermType() == TermType.VARIABLE) return;
        else if (sourceExp instanceof Term && ((Term) sourceExp).getTermType() == TermType.CONSTANT && targetExp instanceof Term && ((Term) targetExp).getTermType() == TermType.CONSTANT) return;
        else if (sourceExp instanceof PseudoMTerm && targetExp instanceof PseudoMTerm) return;


        else if (sourceExp instanceof Term && ((Term) sourceExp).getTermType() == TermType.COMPLEX && targetExp instanceof Term && ((Term) targetExp).getTermType() == TermType.COMPLEX &&
                sourceExp instanceof InfixTerm == targetExp instanceof InfixTerm &&
                ((Term) sourceExp).getMainFnSymbol().equals(((Term) targetExp).getMainFnSymbol()) &&
         //       bracketMatch (((Term) sourceExp).getOpenBracket(), ((Term) sourceExp).getCloseBracket(), ((Term) targetExp).getOpenBracket(), ((Term) targetExp).getCloseBracket()) &&
                sourceExp.getChildren() != null && targetExp.getChildren() != null && sourceExp.getChildren().size() == targetExp.getChildren().size()) {
            for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                    findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform, objectL);
                }
            }
            return;
        }
        else if (sourceExp instanceof SentenceAtomic && targetExp instanceof SentenceAtomic) return;
        else if (sourceExp instanceof MFormula && targetExp instanceof MFormula) return;
        else if (sourceExp instanceof ContradictionSimple && targetExp instanceof ContradictionSimple) return;

        else if (sourceExp instanceof MComplexFormula && targetExp instanceof MComplexFormula &&
                ((MComplexFormula) sourceExp).getFormulaSym().equals(((MComplexFormula) targetExp).getFormulaSym())) {
            if (sourceExp.getChildren() != null && targetExp.getChildren() != null && sourceExp.getChildren().size() == targetExp.getChildren().size()) {
                for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                    if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                        findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform, objectL);
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
                    findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform, objectL);
                }
            }
            return;
        }
        else if (sourceExp instanceof InfixAtomic && targetExp instanceof InfixAtomic &&
                ((InfixAtomic) sourceExp).getMainRelation().equals(((InfixAtomic) targetExp).getMainRelation()) &&
                sourceExp.getChildren().size() == targetExp.getChildren().size()) {
            for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                    findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform, objectL);
                }
            }
            return;
        }
        else if (sourceExp instanceof Formula && targetExp instanceof Formula && !((Formula) sourceExp).isAtomic() && !((Formula) targetExp).isAtomic() &&
                //took out because inner member might have parens where separately stated matcher does not -- ok??
       //         bracketMatch(((Formula) sourceExp).getOpenBracket(), ((Formula) sourceExp).getCloseBracket(), ((Formula) targetExp).getOpenBracket(), ((Formula) targetExp).getCloseBracket()) &&
                sourceExp.getChildren().size() == targetExp.getChildren().size()) {
            Formula sourceFormula = (Formula) sourceExp;
            Formula targetFormula = (Formula) targetExp;
            boolean good = false;
            if (sourceFormula.getMainOperator() != null & targetFormula.getMainOperator() != null) {
                if (sourceFormula.getMainOperator() instanceof UnivBoundedQuantOp && targetFormula.getMainOperator() instanceof UnivBoundedQuantOp) {
                    findReplacements(((UnivBoundedQuantOp) sourceFormula.getMainOperator()).getBoundingTerm(), ((UnivBoundedQuantOp) targetFormula.getMainOperator()).getBoundingTerm(), subTransform, objectL);
                    good = true;
                } else if (sourceFormula.getMainOperator() instanceof ExisBoundedQuantOp && targetFormula.getMainOperator() instanceof ExisBoundedQuantOp) {
                    findReplacements(((ExisBoundedQuantOp) sourceFormula.getMainOperator()).getBoundingTerm(), ((ExisBoundedQuantOp) targetFormula.getMainOperator()).getBoundingTerm(), subTransform, objectL);
                    good = true;
                } else if (sourceFormula.getMainOperator() instanceof UnivRestrictedQuantOp && targetFormula.getMainOperator() instanceof UnivRestrictedQuantOp) {
                    findReplacements(((UnivRestrictedQuantOp) sourceFormula.getMainOperator()).getRestrictingFormula(), ((UnivRestrictedQuantOp) targetFormula.getMainOperator()).getRestrictingFormula(), subTransform, objectL);
                    good = true;
                } else if (sourceFormula.getMainOperator() instanceof ExisRestrictedQuantOp && targetFormula.getMainOperator() instanceof ExisRestrictedQuantOp) {
                    findReplacements(((ExisRestrictedQuantOp) sourceFormula.getMainOperator()).getRestrictingFormula(), ((ExisRestrictedQuantOp) targetFormula.getMainOperator()).getRestrictingFormula(), subTransform, objectL);
                    good = true;
                } else if (sourceFormula.getMainOperator().equals(targetFormula.getMainOperator())) {
                    good = true;
                }
            }
            if (good) {
                for (int i = 0; i < sourceExp.getChildren().size(); i++) {
                    if (sourceExp.getChildren().get(i).getLevel() >= 0 && targetExp.getChildren().get(i).getLevel() >= 0) {
                        findReplacements(sourceExp.getChildren().get(i), targetExp.getChildren().get(i), subTransform, objectL);
                    }
                }
            }
            else {
                throw new TextMessageException(getMessageTexts(sourceExp, targetExp, "Substitution mismatch: from ", " to ", "."));
            }
        }
        else {
            throw new TextMessageException(getMessageTexts(sourceExp, targetExp, "Substitution mismatch: from ", " to ", "."));
        }
    }

    public static void setMatching(Expression metaExp, Expression objectExp) throws TextMessageException {
    //    System.out.println("meta: " + metaExp + " object: " + objectExp);

        boolean skip = false;

        if ( (metaExp.getType() == ExpressionType.FORMULA && ((Formula) metaExp).getSubTransform() == null) || (metaExp.getType() == ExpressionType.TERM && ((Term) metaExp).getSubTransform() == null)) {

            if (metaExp instanceof Term && ((Term) metaExp).getTermType() == TermType.VARIABLE && objectExp instanceof Term && ((Term) objectExp).getTermType() == TermType.VARIABLE)
                ((MVariable) ((Term) metaExp).getChildren().get(0)).setMatch(((VariableSym) ((Term) objectExp).getChildren().get(0)));

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

            else if (metaExp instanceof Term && objectExp instanceof Term && ((Term) metaExp).getTermType() == TermType.COMPLEX && ((Term) objectExp).getTermType() == TermType.COMPLEX &&
                    !(metaExp instanceof InfixTerm) && objectExp instanceof InfixTerm &&
    //                bracketMatch(((Term) metaExp).getOpenBracket(), ((Term) metaExp).getCloseBracket(), ((Term) objectExp).getOpenBracket(), ((Term) objectExp).getCloseBracket()) &&
                    metaExp.getChildren().size() == objectExp.getChildren().size()) {
                Term mt = (Term) metaExp;
                Term ot = (Term) objectExp;
  //              mt.setOpenBracket(ot.getOpenBracket());
 //               mt.setCloseBracket(ot.getCloseBracket());
                if (mt.getMainFnSymbol() instanceof MFunctionSymbol)
                    ((MFunctionSymbol) mt.getMainFnSymbol()).setMatch(ot.getMainFnSymbol());
                else if (!mt.getMainFnSymbol().getMatch().equals(ot.getMainFnSymbol())) {
                    throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
                }
            }

            else if (metaExp instanceof ContradictionSimple && objectExp instanceof ContradictionSimple) {}


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
                if (metaAtomic.getMainRelation() instanceof MRelationSymbol)
                    ((MRelationSymbol) metaAtomic.getMainRelation()).setMatch(objectAtomic.getMainRelation());
                if (!metaAtomic.getMainRelation().getMatch().equals(objectAtomic.getMainRelation())) {
                    throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
                }
            } else if (metaExp instanceof PrefixAtomic && objectExp instanceof InfixAtomic && metaExp.getChildren().size() == objectExp.getChildren().size()) {
                //    bracketMatch(((InfixAtomic) metaExp).getOpenBracket(), ((InfixAtomic) metaExp).getCloseBracket(), ((InfixAtomic) objectExp).getOpenBracket(), ((InfixAtomic) objectExp).getCloseBracket())) {
                PrefixAtomic metaAtomic = (PrefixAtomic) metaExp;
                InfixAtomic objectAtomic = (InfixAtomic) objectExp;
                if (metaAtomic.getMainRelation() instanceof MRelationSymbol)
                    ((MRelationSymbol) metaAtomic.getMainRelation()).setMatch(objectAtomic.getMainRelation());
                if (!metaAtomic.getMainRelation().getMatch().equals(objectAtomic.getMainRelation())) {
                    throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
                }
            } else if (metaExp instanceof Formula && objectExp instanceof Formula && !((Formula) metaExp).isAtomic() && !((Formula) objectExp).isAtomic() &&
                    bracketMatch(((Formula) metaExp).getOpenBracket(), ((Formula) metaExp).getCloseBracket(), ((Formula) objectExp).getOpenBracket(), ((Formula) objectExp).getCloseBracket()) &&
                    metaExp.getChildren().size() == objectExp.getChildren().size()) {
                ((Formula) metaExp).setOpenBracket(((Formula) objectExp).getOpenBracket());
                ((Formula) metaExp).setCloseBracket(((Formula) objectExp).getCloseBracket());
                if (((Formula) metaExp).getMainOperator() instanceof UniversalOp && ((Formula) objectExp).getMainOperator() instanceof UniversalOp) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((VariableSym) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                }
                if (((Formula) metaExp).getMainOperator() instanceof ExistentialOp && ((Formula) objectExp).getMainOperator() instanceof ExistentialOp) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((VariableSym) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                }
                if (((Formula) metaExp).getMainOperator() instanceof UnivBoundedQuantOp && ((Formula) objectExp).getMainOperator() instanceof UnivBoundedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((VariableSym) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                    setMatching( ((UnivBoundedQuantOp) ((Formula) metaExp).getMainOperator()).getBoundingTerm(), ((UnivBoundedQuantOp) ((Formula) objectExp).getMainOperator()).getBoundingTerm());
                }
                if (((Formula) metaExp).getMainOperator() instanceof ExisBoundedQuantOp && ((Formula) objectExp).getMainOperator() instanceof ExisBoundedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((VariableSym) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                    setMatching( ((ExisBoundedQuantOp) ((Formula) metaExp).getMainOperator()).getBoundingTerm(), ((ExisBoundedQuantOp) ((Formula) objectExp).getMainOperator()).getBoundingTerm());
                }
                if (((Formula) metaExp).getMainOperator() instanceof UnivRestrictedQuantOp && ((Formula) objectExp).getMainOperator() instanceof UnivRestrictedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((VariableSym) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
                    setMatching( ((UnivRestrictedQuantOp) ((Formula) metaExp).getMainOperator()).getRestrictingFormula(), ((UnivRestrictedQuantOp) ((Formula) objectExp).getMainOperator()).getRestrictingFormula());
                }
                if (((Formula) metaExp).getMainOperator() instanceof ExisRestrictedQuantOp && ((Formula) objectExp).getMainOperator() instanceof ExisRestrictedQuantOp ) {
                    ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((VariableSym) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
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
   //         System.out.println("trans1 meta: " + metaExp + " obj: " + objectExp);
            Pair<Expression, Expression> pair = new Pair(metaExp, objectExp);
            transformList.add(pair);
            skip = true;
        }

       if (!skip && metaExp.getChildren() != null && metaExp.getLevel() > 0 && metaExp.getChildren().size() == objectExp.getChildren().size() ){

           for (int i = 0; i < metaExp.getChildren().size(); i++) {
               if ((metaExp.getChildren().get(i).getType() == ExpressionType.FORMULA && ((Formula) metaExp.getChildren().get(i)).getSubTransform() == null) ||
                       (metaExp.getChildren().get(i).getType() == ExpressionType.TERM && ((Term) metaExp.getChildren().get(i)).getSubTransform() == null)) {
                   setMatching(metaExp.getChildren().get(i), objectExp.getChildren().get(i));
               }
               else {
                   //process sub
                   Pair<Expression, Expression> pair = new Pair(metaExp.getChildren().get(i), objectExp.getChildren().get(i));
                   transformList.add(pair);
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
        if (exp1 != null) texts.addAll(exp1.toTextList());
        texts.add(new Text(middle));
        if (exp2 != null) texts.addAll(exp2.toTextList());
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

    public static List<Pair<Expression, Expression>> getTransformList() {
        return transformList;
    }
}
