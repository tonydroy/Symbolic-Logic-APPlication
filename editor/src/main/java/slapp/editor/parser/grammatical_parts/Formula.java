package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CloseBracket;
import slapp.editor.parser.symbols.OpenBracket;
import slapp.editor.parser.symbols.OperatorSym;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.parser.ExpressionType.NULL;

public class Formula implements Expression {

    private ExpressionType type = ExpressionType.FORMULA;
    private List<Expression> children = new ArrayList<Expression>();
    boolean atomic = false;
    boolean combines = true;
    boolean negatingInfix = false;
    int level = 0;
    Operator mainOperator = null;
    OpenBracket openBracket = new OpenBracket("");
    CloseBracket closeBracket = new CloseBracket("");

    @Override
    public ExpressionType getType() {
        return type;
    }

    @Override
    public TextFlow toTextFlow() {
        List texts = new ArrayList();
        if (mainOperator == null) {
            return children.get(0).toTextFlow();
        }
        else if (mainOperator.isUnary()) {
            if (mainOperator.getType() == ExpressionType.NEG_OP && ((Formula) children.get(0)).isNegatingInfix()) {
                return ((InfixAtomic) children.get(0)).negatedTextFlow();
            }
            else {
                texts.addAll(mainOperator.toTextFlow().getChildren());
                texts.addAll(children.get(0).toTextFlow().getChildren());
            }
        }
        else {
            texts.addAll(openBracket.toTextFlow().getChildren());
            texts.addAll(children.get(0).toTextFlow().getChildren());
            texts.add(new Text(" "));
            texts.addAll(mainOperator.toTextFlow().getChildren());
            texts.add(new Text(" "));
            texts.addAll(children.get(1).toTextFlow().getChildren());
            texts.addAll(closeBracket.toTextFlow().getChildren());

        }
        Text[] txtArray = new Text[texts.size()];
        TextFlow textFlow = new TextFlow(txtArray);
        return textFlow;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mainOperator == null) {
            return children.get(0).toString();
        }
        else if (mainOperator.isUnary()) {
            if (mainOperator.getType() == ExpressionType.NEG_OP && ((Formula) children.get(0)).isNegatingInfix()) {
                return ((InfixAtomic) children.get(0)).negatedString();
            }
            else {
                sb.append(mainOperator.toString());
                sb.append(children.get(0).toString());
            }
        }
        else {
            sb.append(openBracket.toString());
            sb.append(children.get(0).toString());
            sb.append(" ");
            sb.append(mainOperator.toString());
            sb.append(children.get(1).toString());
            sb.append(closeBracket.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {return false;}

    @Override public int hashCode() {return 0;}



    public List<Expression> getChildren() {
        return children;
    }

    public void setChildren(List<Expression> children) {
        this.children = children;
    }

    public boolean isNegatingInfix() {
        return negatingInfix;
    }

    public void setNegatingInfix(boolean negatingInfix) {
        this.negatingInfix = negatingInfix;
    }

    public boolean isAtomic() {
        return atomic;
    }

    public void setAtomic(boolean atomic) {
        this.atomic = atomic;
    }

    public boolean isCombines() {
        return combines;
    }

    public void setCombines(boolean combines) {
        this.combines = combines;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Operator getMainOperator() {
        return mainOperator;
    }

    public void setMainOperator(Operator mainOperator) {
        this.mainOperator = mainOperator;
    }





}
