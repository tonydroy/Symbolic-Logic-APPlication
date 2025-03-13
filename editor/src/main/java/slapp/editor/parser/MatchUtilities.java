package slapp.editor.parser;

import javafx.scene.text.Text;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class MatchUtilities {

    private static boolean relaxBracketMatch = true;



    public static boolean formMatch(Expression metaExp, Expression objectExp) throws TextMessageException {
        clearMatching();
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
                return true;
            } catch (TextMessageException e) {
                EditorAlerts.showSimpleTxtListAlert("Map Issue", e.getMessageList());
                return false;
            }
        }
        else {
            throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " and ", " do not both parse to formulas or both to terms."));
        }
    }

    private static void setMatching(Expression metaExp, Expression objectExp) throws TextMessageException {

        System.out.println("meta: " + metaExp.getType() + ": " + metaExp + " object: " + objectExp.getType() +": " + objectExp);

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
           if (mt.getMainFnSymbol() instanceof MFunctionSymbol) ((MFunctionSymbol) mt.getMainFnSymbol()).setMatch(ot.getMainFnSymbol());
           else if (!mt.getMainFnSymbol().getMatch().equals(ot.getMainFnSymbol()) ) {
               throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
           }
       }
       else if (metaExp instanceof SentenceAtomic && objectExp instanceof SentenceAtomic )
           ((MSentenceLetter) ((SentenceAtomic) metaExp).getMainLetter()).setMatch(((SentenceLetter) ((SentenceAtomic) objectExp).getMainLetter()));
       else if (metaExp instanceof MFormula && objectExp instanceof Formula)
           ((MFormula) metaExp).setMatch((Formula) objectExp);


       else if (metaExp instanceof PrefixAtomic && objectExp instanceof PrefixAtomic && metaExp.getChildren().size() == objectExp.getChildren().size() ) {
           PrefixAtomic metaAtomic = (PrefixAtomic) metaExp;
           PrefixAtomic objectAtomic = (PrefixAtomic) objectExp;
           if (metaAtomic.getMainRelation() instanceof MRelationSymbol) ((MRelationSymbol) metaAtomic.getMainRelation()).setMatch(objectAtomic.getMainRelation());
           else if (!metaAtomic.getMainRelation().getMatch().equals(objectAtomic.getMainRelation()) ) {
               throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
           }
       }


       else if (metaExp instanceof InfixAtomic && objectExp instanceof InfixAtomic && metaExp.getChildren().size() == objectExp.getChildren().size() &&
               bracketMatch(((InfixAtomic) metaExp).getOpenBracket(), ((InfixAtomic) metaExp).getCloseBracket(), ((InfixAtomic) objectExp).getOpenBracket(), ((InfixAtomic) objectExp).getCloseBracket())) {
           InfixAtomic metaAtomic = (InfixAtomic) metaExp;
           InfixAtomic objectAtomic = (InfixAtomic) objectExp;
           if (!metaAtomic.getMainRelation().getMatch().equals(objectAtomic.getMainRelation()) ){
               throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
           }
       }





       else if (metaExp instanceof Formula && objectExp instanceof Formula && ((Formula) metaExp).isAtomic() && ((Formula) objectExp).isAtomic() &&
               (metaExp instanceof InfixAtomic == objectExp instanceof InfixAtomic) && (metaExp instanceof PrefixAtomic == objectExp instanceof PrefixAtomic) &&
               bracketMatch(((Formula) metaExp).getOpenBracket(), ((Formula) metaExp).getCloseBracket(), ((Formula) objectExp).getOpenBracket(), ((Formula) objectExp).getCloseBracket()) &&
               metaExp.getChildren().size() == objectExp.getChildren().size() ) {
           Formula mf = (Formula) metaExp;
           Formula of = (Formula) objectExp;
           mf.setOpenBracket(of.getOpenBracket());
           mf.setCloseBracket(of.getCloseBracket());
           if (mf.getMainRelation() instanceof MRelationSymbol) ((MRelationSymbol) mf.getMainRelation()).setMatch(of.getMainRelation());
           else if (!mf.getMainRelation().getMatch().equals(of.getMainRelation()) ) {

               throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
           }
       }









       else if (metaExp instanceof Formula && objectExp instanceof Formula && !((Formula) metaExp).isAtomic() && !((Formula) objectExp).isAtomic() &&
               bracketMatch(((Formula) metaExp).getOpenBracket(), ((Formula) metaExp).getCloseBracket(), ((Formula) objectExp).getOpenBracket(), ((Formula) objectExp).getCloseBracket()) &&
               metaExp.getChildren().size() == objectExp.getChildren().size()  ) {
           ((Formula) metaExp).setOpenBracket(((Formula) objectExp).getOpenBracket());
           ((Formula) metaExp).setCloseBracket(((Formula) objectExp).getCloseBracket());
           if (((Formula) metaExp).getMainOperator() instanceof UniversalOp && ((Formula) objectExp).getMainOperator() instanceof UniversalOp) {
               ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
           }
           if (((Formula) metaExp).getMainOperator() instanceof ExistentialOp && ((Formula) objectExp).getMainOperator() instanceof ExistentialOp) {
               ((MVariable) ((Formula) metaExp).getMainOperator().getVariableTerm().getChildren().get(0)).setMatch((Variable) ((Formula) objectExp).getMainOperator().getVariableTerm().getChildren().get(0));
           }
           if (!((Formula) metaExp).getMainOperator().getMatch().equals(((Formula) objectExp).getMainOperator())) {

               throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
           }
       }


       else {

           throw new TextMessageException(getMessageTexts(metaExp, objectExp, "", " does not map to ", "."));
       }



       if (metaExp.getChildren() != null && metaExp.getLevel() > 0 && metaExp.getChildren().size() == objectExp.getChildren().size() &&
               !(metaExp.getType() == ExpressionType.FORMULA && ((Formula) metaExp).getSubTransform() != null)) {

           for (int i = 0; i < metaExp.getChildren().size(); i++) {
               setMatching(metaExp.getChildren().get(i), objectExp.getChildren().get(i));
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
