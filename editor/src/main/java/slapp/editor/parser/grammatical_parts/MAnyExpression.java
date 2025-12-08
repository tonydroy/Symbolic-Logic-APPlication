package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.TextMessageException;
import slapp.editor.parser.symbols.MAnyExpressionSym;

import java.util.ArrayList;
import java.util.List;

public class MAnyExpression extends AnyExpression {


    private static List<MAnyExpression> mAnyExpressions = new ArrayList<MAnyExpression>();
    private List<Expression> matchAnyExp;
    private MAnyExpressionSym expressionSym;

    private MAnyExpression(MAnyExpressionSym sym) {
        super();
        expressionSym = sym;
        mAnyExpressions.add(this);
    }

    public static MAnyExpression getInstance(MAnyExpressionSym sym) {
        MAnyExpression newInstance = null;
        for (MAnyExpression e : mAnyExpressions) {
            if (e.getExpressionSym().equals(sym)) {
                newInstance = e;
                break;
            }
        }
        if(newInstance == null) { newInstance = new MAnyExpression(sym); }
        return newInstance;
    }


    public MAnyExpressionSym getExpressionSym() {
        return expressionSym;
    }

    @Override
    public List<Text> toTextList() {

        List<Text> list = new ArrayList<Text>();
        list.addAll(expressionSym.toTextList());
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expressionSym.toString());
        return sb.toString();
    }

 //   @Override
  //  public Expression getMatch() { return null; }

    public void setMatch(List<Expression> match) throws TextMessageException {
        if (matchAnyExp == null) { matchAnyExp = match; }
        else if (!matchAnyExp.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Variable "));
            messageTxts.addAll(getExpressionSym().toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            for (Expression e : matchAnyExp) {
                messageTxts.addAll(e.toTextList());
            }
            messageTxts.add(new Text(" and "));
            for (Expression e : match) {
                messageTxts.addAll(e.toTextList());
            }
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() {
        for (MAnyExpression e : mAnyExpressions) {
            e.clearMatch();
        }
    }

    private void clearMatch() {
        matchAnyExp = null;
    }
}
