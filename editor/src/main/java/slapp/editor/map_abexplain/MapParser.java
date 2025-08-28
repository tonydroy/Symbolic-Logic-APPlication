package slapp.editor.map_abexplain;

import slapp.editor.parser.*;
import slapp.editor.parser.symbols.*;

import java.util.List;

public class MapParser {
    private static Language language;    //delete this and uncomment in constructor
    private static int maxTermLevel;
    private static double baseFontSize = 12;
    private static boolean matchVal = false;

    public MapParser() {}

    public static List<Expression> parseToSymbol(List<Expression> expressions) {
        maxTermLevel = 0;

        List<Expression> simpleSymbols = getSimpleSymbols(expressions);
        List<Expression> termSymbols = ParseUtilities.getTermSymbols(simpleSymbols);
        List<Expression> symbols = getRelSentSymbols(termSymbols);

        return symbols;
    }


    public static List<Expression> getRelSentSymbols(List<Expression> expressions) {

        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            String supString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getOnePlaceRelSymbols() != null && language.getOnePlaceRelSymbols().contains(elementStr)) {
                    expressions.set(i, new RelationSymbol(elementStr, "", "", 1));
                    continue;
                }

                if (language.getInfixRelations() != null) {

                    if (language.getInfixRelations().containsKey(elementStr)) {
                        RelationSymbol relationSymbol = new RelationSymbol(elementStr, "", "", 2);
                        relationSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());

                        if (language.isAllowBinaryInfixNegations()) {
                            String complementStr = language.getInfixRelations().get(elementStr);
                            RelationSymbol complementSymbol = null;
                            if (!complementStr.isEmpty()) {
                                complementSymbol = new RelationSymbol(complementStr, "", "", 2);
                                complementSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());
                                relationSymbol.setComplementSymbol(complementSymbol);
                            }
                        }
                        expressions.set(i, relationSymbol);
                        continue;
                    }

                    if (language.isAllowBinaryInfixNegations()) {
                        if (language.getInfixRelations().containsValue(elementStr)) {
                            String relationString = language.getInfixRelationKey(elementStr);
                            RelationSymbol relationSymbol = new RelationSymbol(relationString, "", "", 2);
                            relationSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());
                            relationSymbol.setType(ExpressionType.COMPLEMENT_REL_SYM);

                            RelationSymbol complementSymbol = new RelationSymbol(elementStr, "", "", 2);
                            complementSymbol.setPermitInfix(language.isAllowBinaryInfixRelations());
                            relationSymbol.setComplementSymbol(complementSymbol);
                            expressions.set(i, relationSymbol);
                            continue;
                        }
                    }
                }

                if (language.getXrelationSymbols() != null && language.getXrelationSymbols().contains(elementStr) ) {
                    int j = i + 1;
                    if (isFollowingSuperscript(j, expressions)) {
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
                        if (!supString.isEmpty()) {
                            int places = Integer.parseInt(supString);
                            RelationSymbol relationSymbol;
                            if (!language.isMetalanguage())
                                relationSymbol = new RelationSymbol(elementStr, subString, supString, places);
                            else relationSymbol = MRelationSymbol.getInstance(elementStr, subString, supString, places);
                            expressions.set(i, relationSymbol);
                        }
                    }
                }
            }
        }

        //sentence letters
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getSentenceLetters() != null && language.getSentenceLetters().contains(elementStr)) {
                    int j = i + 1;
                    if (language.isSentenceLetterSubs() && j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = getSubString(j, expressions);
                    SentenceLetter letter;
                    if (!language.isMetalanguage()) letter = new SentenceLetter(elementStr, subString);
                    else letter = MSentenceLetter.getInstance(elementStr, subString);
                    expressions.set(i, letter);
                }
            }
        }

        //mFormula symbols
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getmFormulaSymbols() != null && language.getmFormulaSymbols().contains(elementStr)) {
                    int j = i + 1;
                    if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = getSubString(j, expressions);
                    MFormulaSym mFormulaSym = new MFormulaSym(elementStr, subString);
                    expressions.set(i, mFormulaSym);
                }
            }
        }

        //pseudo mFormula symbols
        for (int i = 0; i < expressions.size(); i++) {
            String elementStr = "";
            String subString = "";
            if (expressions.get(i).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(i)).isNormal()) {
                elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                if (language.getpMFormulaSymbols() != null && language.getpMFormulaSymbols().contains(elementStr)) {
                    int j = i + 1;
                    if (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT &&
                            ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) subString = getSubString(j, expressions);
                    PseudoMFormulaSym pMFormulaSym = new PseudoMFormulaSym(elementStr, subString);
                    expressions.set(i, pMFormulaSym);
                }
            }
        }

        return expressions;
    }

    public static List<Expression> getSimpleSymbols(List<Expression> expressions) {
        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i) instanceof OriginalElement) {
                String elementStr = ((OriginalElement) expressions.get(i)).getElementStr();
                int position = ((OriginalElement) expressions.get(i)).getPosition();

                if (elementStr.equals(language.getOpenBracket1()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new OpenBracket1(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getOpenBracket2()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new OpenBracket2(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getOpenBracket3()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new OpenBracket3(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getCloseBracket1()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new CloseBracket1(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getCloseBracket2()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new CloseBracket2(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getCloseBracket3()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new CloseBracket3(elementStr));
                    continue;
                }
                if (elementStr.equals(language.getNegation()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new NegationSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getConditional()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new ConditionalSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getBiconditional()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new BiconditionalSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getConjunction()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new ConjunctionSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getDisjunction()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new DisjunctionSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getNand()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new NandSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getNor()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new NorSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getUniversalQuant()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new UniversalQuantifierSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getExistentialQuant()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new ExistentialQuantifierSym(elementStr, position));
                    continue;
                }
                if (elementStr.equals(language.getContradictionSymbol()) && ((OriginalElement) expressions.get(i)).isNormal()) {
                    expressions.set(i, new ContradictionSymbol(elementStr));
                }

            }
        }
        return expressions;
    }

    //is following superscript > 0, possibly after subscript
    private static boolean isFollowingSuperscript(int j, List<Expression> expressions) {
        boolean isFollowingSuperscript = false;
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT) {
            OriginalElement originalElement = (OriginalElement) expressions.get(j);
            if (originalElement.isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[1-9]")) {
                isFollowingSuperscript = true;
                break;
            }
            else if (originalElement.isSubscript()) {j = j + 1;}
            else break;
        }
        return isFollowingSuperscript;
    }

    //get string of subscripts, deleting members from expressions
    private static String getSubString(int j, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(j)).isSubscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[0-9]")) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }

    //get string of superscripts, deleting members from expressions
    private static String getSupString(int j, List<Expression> expressions) {
        StringBuilder sb = new StringBuilder();
        while (j < expressions.size() && expressions.get(j).getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) expressions.get(j)).isSuperscript() && ((OriginalElement) expressions.get(j)).getElementStr().matches("[0-9]")) {
            sb.append(((OriginalElement) expressions.get(j)).getElementStr());
            expressions.remove(j);
        }
        return sb.toString();
    }

    public static void setLanguage(Language language) {
        MapParser.language = language;
    }
}
