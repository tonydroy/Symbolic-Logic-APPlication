/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.  If not, see
<https://www.gnu.org/licenses/>.
 */

package slapp.editor.truth_table;

import com.gluonhq.richtextarea.model.Document;
import javafx.geometry.HPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.ExtractSubText;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate from fromula Document list of truth table Text column head items.  Parses even in context where basic
 * sentences are not from a sentential language
 */
public class ParseDocForTTable {
    private static List<TableHeadItem> headItems = new ArrayList<>();
    private List<String> unaryOperators;
    private List<String> binaryOperators;
    private Document doc;
    private int start = 0;
    private int span = 0;
    private String formulaString;
    private int formulaLength;
    private ColumnConstraints constraints;

    /**
     * Construct the parser based on lists of sentential operators
     * @param unaryOperators the unary operator String list
     * @param binaryOperators the binary operator String list
     */
    public ParseDocForTTable(List unaryOperators, List binaryOperators ) {
        this.unaryOperators = unaryOperators;
        this.binaryOperators = binaryOperators;
        constraints = new ColumnConstraints();
        constraints.setMinWidth(20);
        constraints.setHalignment(HPos.CENTER);

    }

    /**
     * Populate the list of table head items
     * @param document The Document from which to extract the items
     * @return the head item list
     */
    public List<TableHeadItem> generateHeadItems(Document document) {
        headItems.clear();
        start = 0;
        span = 0;
        this.doc = document;
        formulaString = doc.getText();
        formulaLength = formulaString.length();

        while (start < formulaLength) {
            span = 1;
            char c = formulaString.charAt(start);
            if (Character.isWhitespace(c)) start++;
            else if (isOpenBracket(c)) openBracketSequence();
            else if (isOperator(c)) operatorSequence(c);
            else if (isRelationChar(c)) relationSequence();
            else if (isCloseBracket(c)) {
                TextFlow flow = ExtractSubText.getTextFromDoc(start, span, doc);
                TableHeadItem headItem = new TableHeadItem(flow, constraints);
                headItems.add(headItem);
                EditorAlerts.fleetingRedPopup("Unexpected close bracket.");
                return headItems;
            }
        }
        return headItems;
    }

    /*
     * Current character is an operator
     * @param c the current character
     */
    private void operatorSequence(char c) {
        TextFlow flow = ExtractSubText.getTextFromDoc(start, span, doc);
        flow.setTextAlignment(TextAlignment.CENTER);

        if (isBinaryOperator(c)) {
            flow.getChildren().add(0, new Text(" "));
            flow.getChildren().add(new Text(" "));
        }
        TableHeadItem headItem = new TableHeadItem(flow, constraints);
        headItems.add(headItem);
        start = start + span;
    }

    /*
     * Current character is an open bracket
     */
    private void openBracketSequence() {

        while (start + span < formulaLength && isOpenBracket(formulaString.charAt(start + span))) {
            span++;
        }
        if (start + span < formulaLength && isOperator(formulaString.charAt(start + span))) {
            span++;
            TextFlow flow = ExtractSubText.getTextFromDoc(start, span, doc);
            flow.setTextAlignment(TextAlignment.CENTER);

            TableHeadItem headItem = new TableHeadItem(flow, constraints);
            headItems.add(headItem);
            start = start + span;
            return;
        }
        while (start + span < formulaLength && isRelationChar(formulaString.charAt(start + span))) {
            span++;
            if (start + span < formulaLength) bracketMatch(formulaString.charAt(start + span));
        }
        TextFlow flow = ExtractSubText.getTextFromDoc(start, span, doc);
        flow.setTextAlignment(TextAlignment.CENTER);


        TableHeadItem headItem = new TableHeadItem(flow, constraints);
        headItems.add(headItem);
        start = start + span;
    }

    /*
     * Current character is a relation symbol
     */
    private void relationSequence() {
        while (start + span < formulaLength && isRelationChar(formulaString.charAt(start + span))) {
            span++;
            if (start + span < formulaLength) bracketMatch(formulaString.charAt(start + span));
        }
        if (start + span < formulaLength && isOperator(formulaString.charAt(start + span))) {
            TextFlow flow = ExtractSubText.getTextFromDoc(start, span, doc);
            flow.setTextAlignment(TextAlignment.CENTER);

            TableHeadItem headItem = new TableHeadItem(flow, constraints);
            headItems.add(headItem);
            start = start + span;
            return;
        }
        while (start + span < formulaLength && isCloseBracket(formulaString.charAt(start + span))) {
            span++;
        }
        TextFlow flow = ExtractSubText.getTextFromDoc(start, span, doc);
        flow.setTextAlignment(TextAlignment.CENTER);
        TableHeadItem headItem = new TableHeadItem(flow, constraints);
        headItems.add(headItem);
        start = start + span;
    }

    /*
     * An open bracket is one of '(' or '['
     * @param c the character to evaluate
     * @return true if is open bracket, and otherwise false
     */
    private boolean isOpenBracket(char c) {
        return (c == '(' || c == '[');
    }

    /*
     * A close bracket is one of ')' or ']'
     * @param c the character to evaluate
     * @return true if is close bracket, and otherwise false
     */
    private boolean isCloseBracket(char c) {
        return (c == ')' || c == ']');
    }

    /*
     * The list of unary operators isa parameter to the constructor
     * @param c the character to evaluate
     * @return true if unary operator, and otherwise false
     */
    private boolean isUnaryOperator(char c) {
        boolean isUnaryOperator = false;
        for (String ops : unaryOperators) {
            char op = ops.charAt(0);
            if (c == op) isUnaryOperator = true;
        }
        return isUnaryOperator;
    }

    /*
     * The list of binary operators is a parameter to the constructor
     * @param c the character to evaluate
     * @return true if binary operator, and otherwise false
     */
    private boolean isBinaryOperator(char c) {
        boolean isBinaryOperator = false;
        for (String ops : binaryOperators) {
            char op = ops.charAt(0);
            if (c == op) isBinaryOperator = true;
        }
        return isBinaryOperator;
    }

    /*
     * A symbol is an operator if it is a unary or binary sentential operator
     * @param c the character to evaluate
     * @return true if operator, and otherwise false
     */
    private boolean isOperator(char c) {
        return (isBinaryOperator(c) || isUnaryOperator(c));
    }

    /*
     * A relation character is one that is not a bracket or an operator
     * @param c the character to evaluate
     * @return true if relation symbol and otherwise false
     */
    private boolean isRelationChar(char c) {
        return (!isOpenBracket(c) && !isCloseBracket(c) && !isOperator(c)  );
    }

    /*
     * A bracket pair matches if they are '('  and ')' or '[' and ']'
     * @param open the 'open' character
     * @param test the 'close' character to test
     * @return true if matching pair and otherwise false
     */
    private boolean isMatchingBracket(char open, char test) {
        boolean match = false;
        if (open == '(' && test == ')') match = true;
        if (open == '[' && test == ']') match = true;
        return match;
    }

    /*
     * Increase span from an open bracket to its mate
     * @param c the starting character
     */
    private void bracketMatch(char c) {
        if (isOpenBracket(c)) {
            int count = 1;
            while (start + span < formulaLength && count != 0) {
                span++;
                if (start + span < formulaLength && formulaString.charAt(start + span) == c) count++;
                if (start + span < formulaLength && isMatchingBracket(c, formulaString.charAt(start + span)))  count--;
            }
            if (start + span + 1 < formulaLength) span++;
        }
    }

}
