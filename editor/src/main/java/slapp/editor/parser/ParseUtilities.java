package slapp.editor.parser;

import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class ParseUtilities {
    private static Language language;

    public ParseUtilities() {
        //for now

        this.language = Languages.getLanguage("Lq");
    }

    public static List<Expression> parseDoc(Document doc) {
        List<OriginalElement> elements = getElements(doc);
        List<Expression> expressions = new ArrayList<>();
        for (OriginalElement element : elements) {
            expressions.add(element);
        }
        List<Expression> simpleSymbols = getSimpleSymbols(expressions);
        List<Expression> termSymbols = getTermSymbols(expressions);
        List<Expression> terms = getTerms(termSymbols);
        List<Expression> relSentSymbols = getRelSentSymbols(terms);

        return expressions;
    }

    public static List<Expression> getSimpleSymbols(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
            if (elementStr.equals(language.getOpenBracket1())) {
                expressions.set(i, new OpenBracket1(elementStr));
                continue;
            }
            if (elementStr.equals(language.getOpenBracket2())) {
                expressions.set(i, new OpenBracket2(elementStr));
                continue;
            }
            if (elementStr.equals(language.getOpenBracket3())) {
                expressions.set(i, new OpenBracket3(elementStr));
                continue;
            }
            if (elementStr.equals(language.getCloseBracket1())) {
                expressions.set(i, new CloseBracket1(elementStr));
                continue;
            }
            if (elementStr.equals(language.getCloseBracket2())) {
                expressions.set(i, new CloseBracket2(elementStr));
                continue;
            }
            if (elementStr.equals(language.getCloseBracket3())) {
                expressions.set(i, new CloseBracket3(elementStr));
                continue;
            }
            if (elementStr.equals(language.getNegation())) {
                expressions.set(i, new Negation(elementStr));
                continue;
            }
            if (elementStr.equals(language.getConditional())) {
                expressions.set(i, new Conditional(elementStr));
                continue;
            }
            if (elementStr.equals(language.getBiconditional())) {
                expressions.set(i, new Biconditional(elementStr));
                continue;
            }
            if (elementStr.equals(language.getConjunction())) {
                expressions.set(i, new Conjunction(elementStr));
                continue;
            }
            if (elementStr.equals(language.getDisjunction())) {
                expressions.set(i, new Disjunction(elementStr));
                continue;
            }
            if (elementStr.equals(language.getNand())) {
                expressions.set(i, new Nand(elementStr));
                continue;
            }
            if (elementStr.equals(language.getNor())) {
                expressions.set(i, new Nor(elementStr));
                continue;
            }
            if (elementStr.equals(language.getUniversalQuant())) {
                expressions.set(i, new UniversalQuantifier(elementStr));
                continue;
            }
            if (elementStr.equals(language.getExistentialQuant())) {
                expressions.set(i, new ExistentialQuantifier(elementStr));
            }
        }
        return expressions;
    }

    public static List<Expression> getRelSentSymbols(List<Expression> expressions) {

        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            String supString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getOnePlaceRelSymbols() != null && language.getOnePlaceRelSymbols().contains(elementStr)) {
                    expressions.set(i, new RelationSymbol(elementStr, "", "", 1));
                    continue;
                }
                if (language.getTwoPlaceRelSymbols() != null && language.getTwoPlaceRelSymbols().contains(elementStr)) {
                    expressions.set(i, new RelationSymbol(elementStr, "", "", 2));
                    continue;
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
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
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

        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i).getType() == ExpressionType.VARIABLE) {
                Term term = new Term();
                term.setLevel(0);
                ArrayList children = new ArrayList();
                children.add(expressions.get(i));
                term.setChildren(children);
                expressions.set(i, term);
                continue;
            }
            if (expressions.get(i).getType() == ExpressionType.CONSTANT) {
                Term term = new Term();
                term.setLevel(0);
                ArrayList children = new ArrayList();
                children.add(expressions.get(i));
                term.setChildren(children);
                expressions.set(i, term);
            }
        }

        boolean changes = true;
        while (changes) {
            changes = false;
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
                            level = Math.max(level, term.getLevel()) + 1;
                        }
                        Term term = new Term();
                        term.setLevel(level);
                        term.setChildren(children);
                        term.setMainFnSymbol(functionSymbol);
                        expressions.set(i, term);
                        changes = true;
                        break;
                    }
                }
            }
        }
        return expressions;
    }

    public static List<Expression> getTermSymbols(List<Expression> expressions) {

        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            String supString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getOnePlaceFunctionSymbols() != null && language.getOnePlaceFunctionSymbols().contains(elementStr)) {
                    expressions.set(i, new FunctionSymbol(elementStr, "", "", 1));
                    continue;
                }
                if (language.getTwoPlaceFunctionSymbols() != null && language.getTwoPlaceFunctionSymbols().contains(elementStr)) {
                    expressions.set(i, new FunctionSymbol(elementStr, "", "", 2));
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
                        FunctionSymbol functionSymbol = new FunctionSymbol(elementStr, subString, supString, Integer.parseInt(supString));
                        expressions.set(i, functionSymbol);
                    }
                }
            }
        }

        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
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

        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT) {
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


    private static int areFollowingTerms(int j, List<Expression> expressions) {
        int places = 0;
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT ) {
            OriginalElement element = (OriginalElement) expressions.get(j);
            if (element.isSuperscript() || element.isSubscript())   j = j + 1;
        }
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.TERM ) {
            places++;
            j = j + 1;
        }
        return places;
    }

    private static boolean areImmediateFollowingTerms(int index, int places, List<Expression> expressions) {
        boolean areFollowingTerms = true;
        for (int i = 0; i < places; i++) {
            if (index + i >= expressions.size() || expressions.get(index + i).getType() != ExpressionType.TERM) areFollowingTerms = false;
        }
        return areFollowingTerms;
    }

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

    private static String getSubString(int j, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[0-9]")) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }

    private static String getSupString(int j, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[0-9]")) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }


    //strip space characters and return list of remaining elements
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










    public static Text newRegularText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", 11));
        return t;
    }

    public static Text newSuperscriptText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", 11 * .72));
        t.setTranslateY(11 * -.4);
        return t;
    }

    public static Text newSubscriptText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", 11 * .72));
        t.setTranslateY(11 * .17);
        return t;
    }

    public static Text[] newSupSubText(String supText, String subText) {
        Text t1 = new Text(supText);
        Text t2 = new Text(subText);
        t1.setFont(new Font("Noto Serif Combo", 11 * .72));
        t2.setFont(new Font("Noto Serif Combo", 11 * .72));
        t1.setTranslateY(11 * -.4);
        t2.setTranslateY(11 * .17);
        t1.applyCss();
        double offset = t1.getLayoutBounds().getWidth();
        t2.setTranslateX(-offset);
        return new Text[]{t1, t2};
    }




}
