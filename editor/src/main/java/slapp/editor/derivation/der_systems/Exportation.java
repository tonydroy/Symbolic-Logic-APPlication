package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;

import java.util.Collections;
import java.util.List;

public class Exportation extends DerivationRule {

    public Exportation(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs) {

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();
        List<Expression> lineExpressions = ParseUtilities.parseDoc(lineDoc, objectLanguage.getNameString());
        Expression lineExpression = lineExpressions.get(0);

        ViewLine inputLine1;
        Pair<ViewLine, List<Text>> line1Pair = checker.getLineFromLabel(inputs[0]);
        if (line1Pair.getKey() != null) inputLine1 = line1Pair.getKey();
        else return new Pair(false, line1Pair.getValue());

        BoxedDRTA inputBDRTA1 = inputLine1.getLineContentBoxedDRTA();
        RichTextArea inputRTA1 = inputBDRTA1.getRTA();
        inputRTA1.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc1 = inputRTA1.getDocument();
        if (inputDoc1.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));
        List<Expression> inputExpressions = ParseUtilities.parseDoc(inputDoc1, objectLanguage.getNameString());
        Expression inputExpression = inputExpressions.get(0);

        Pair<Boolean, List<Text>> accessibilityPair1 = checker.lineIsAccessibleTo(inputLine1, line);
        if (!accessibilityPair1.getKey()) {
            return accessibilityPair1;
        }

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        Document leftForm = new Document(openBracketString + "\ud835\udcaa \u2192" + openBracketString + "\ud835\udcab \u2192 \ud835\udcac" + closeBracketString + closeBracketString );
        Document rightForm = new Document(openBracketString + openBracketString + "\ud835\udcaa \u2227 \ud835\udcab" + closeBracketString + "\u2192 \ud835\udcac" + closeBracketString);
        Expression leftFormExp = null;
        Expression rightFormExp = null;

        List<Expression> leftExpressions = ParseUtilities.parseDoc(leftForm, metaLanguage.getNameString());
        if (leftExpressions == null || leftExpressions.size() != 1) {
            System.out.println("problem parsing left meta form");
            return new Pair(false, "problem parsing left meta form");
        }
        else leftFormExp = leftExpressions.get(0);

        List<Expression> rightExpressions = ParseUtilities.parseDoc(rightForm, metaLanguage.getNameString());
        if (rightExpressions == null || rightExpressions.size() != 1) {
            System.out.println("problem parsing right meta form");
            return new Pair(false, "problem parsing right meta form");
        }
        else rightFormExp = rightExpressions.get(0);

        //try left to right
        MatchUtilities.clearFormMatch();

        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(leftFormExp, rightFormExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }


        //try right to left
        MatchUtilities.clearFormMatch();

        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(rightFormExp, leftFormExp, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + line.getLineNumberLabel().getText() + ") are not of the right form for application of " + getName() + "."))));


    }


}
