package slapp.editor.parser;

import java.util.*;

import static java.util.Map.entry;

public class Languages {

    private static List<Language> fixedLanguages = new ArrayList<Language>();
    private static List<Language> optionalLanguages = new ArrayList<Language>();

    static {
        loadFixedLanguages();
    }

    public Languages() { }

    private static void loadFixedLanguages(){
        fixedLanguages.add(Ls());
        fixedLanguages.add(Ls_abv());
        fixedLanguages.add(Lq());
        fixedLanguages.add(Lq_abv());
        fixedLanguages.add(Lnt_lessthan());
        fixedLanguages.add(Lnt());
    }

    static Language getLanguage(String name) {
        for (Language l : fixedLanguages){
            if (l.getNameString().equals(name)) return l;
        }
        for (Language l : optionalLanguages){
            if (l.getNameString().equals(name)) return l;
        }
        return null;
    }

    private static Language Ls() {
        Language lang = new Language("Ls");
        lang.setNameTexts("\u2112", "","\ud835\udcc8");

        lang.setOpenBracket1("("); lang.setCloseBracket1(")");
        lang.setNegation("\u223c");
        lang.setConditional("\u2192");

        lang.setSentenceLetters(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setSentenceLetterSubs(true);

        return lang;
    }

    private static Language Ls_abv() {
        Language lang = new Language("Ls_abv");
        lang.setNameTexts("\u2112", "","\ud835\udcc8_abv");

        lang.setOpenBracket1("("); lang.setCloseBracket1(")");
        lang.setOpenBracket2("["); lang.setCloseBracket2("]");
        lang.setAllowDroppedBrackets(true);

        lang.setNegation("\u223c");
        lang.setConditional("\u2192");
        lang.setBiconditional("\u2194");
        lang.setConjunction("\u2227");
        lang.setDisjunction("\u2228");

        lang.setSentenceLetters(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setSentenceLetterSubs(true);

        return lang;
    }


    private static Language Lq() {
        Language lang = new Language("Lq");
        lang.setNameTexts("\u2112", "","\ud835\udcc6");

        lang.setOpenBracket1("("); lang.setCloseBracket1(")");
        lang.setNegation("\u223c");
        lang.setConditional("\u2192");
        lang.setUniversalQuant("\u2200");

        lang.setVariables(Alphabets.getCharacterRange("\ud835\udc56", "\ud835\udc67"));
        lang.setVariableSubs(true);
        lang.setConstants(Alphabets.getCharacterRange("\ud835\udc4e", "\u210e"));
        lang.setConstantSubs(true);

        lang.setSentenceLetters(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setSentenceLetterSubs(true);

        lang.setXfunctionSymbols(Alphabets.getCharacterRange("\ud835\udc4e", "\ud835\udc67"));
        lang.setXfunctionSymbolSubs(true);

        lang.setXrelationSymbols(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setXrelationSymbolSubs(true);
        lang.setXrelationSymbolsRequireSuper(true);

        lang.setInfixRelations( Map.ofEntries(entry("\ue8ac", "")));
        lang.setAllowBinaryInfixNegations(false);

        return lang;
    }

    private static Language Lq_abv() {
        Language lang = new Language("Lq_abv");
        lang.setNameTexts("\u2112", "","\ud835\udcc6_abv");

        lang.setOpenBracket1("("); lang.setCloseBracket1(")");
        lang.setOpenBracket2("["); lang.setCloseBracket2("]");
        lang.setAllowDroppedBrackets(true);

        lang.setNegation("\u223c");
        lang.setConditional("\u2192");
        lang.setBiconditional("\u2194");
        lang.setConjunction("\u2227");
        lang.setDisjunction("\u2228");
        lang.setUniversalQuant("\u2200");
        lang.setExistentialQuant("\u2203");

        lang.setVariables(Alphabets.getCharacterRange("\ud835\udc56", "\ud835\udc67"));
        lang.setVariableSubs(true);
        lang.setConstants(Alphabets.getCharacterRange("\ud835\udc4e", "\u210e"));
        lang.setConstantSubs(true);

        lang.setSentenceLetters(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setSentenceLetterSubs(true);

        lang.setXfunctionSymbols(Alphabets.getCharacterRange("\ud835\udc4e", "\ud835\udc67"));
        lang.setXfunctionSymbolSubs(true);

        lang.setXrelationSymbols(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setXrelationSymbolSubs(true);
        lang.setXrelationSymbolsRequireSuper(false);

        lang.setAllowBinaryInfixRelations(true);
        lang.setAllowBinaryInfixNegations(false);
        lang.setInfixRelations( Map.ofEntries(entry("\ue8ac", "")) );

        lang.setAllowBoundedQuantifiers(true);
        lang.setDividerSymbol(":");

        return lang;
    }

    private static Language Lnt_lessthan() {
        Language lang = new Language("Lnt<");
        lang.setNameTexts("\u2112", "","NT<");

        lang.setOpenBracket1("("); lang.setCloseBracket1(")");
        lang.setOpenBracket2("["); lang.setCloseBracket2("]");
        lang.setAllowDroppedBrackets(true);

        lang.setNegation("\u223c");
        lang.setConditional("\u2192");
        lang.setBiconditional("\u2194");
        lang.setConjunction("\u2227");
        lang.setDisjunction("\u2228");
        lang.setUniversalQuant("\u2200");
        lang.setExistentialQuant("\u2203");

        lang.setVariables(Alphabets.getCharacterRange("\ud835\udc56", "\ud835\udc67"));
        lang.setVariableSubs(true);
        lang.setConstants(Collections.singletonList("\ue886"));

        lang.setOnePlaceFunctionSymbols(Collections.singletonList("\ud835\udc46"));
        lang.setTwoPlaceFunctionSymbols(Arrays.asList("\ue8b8", "\ue8ba"));
        lang.setAllowBinaryInfixFunctions(true);

        lang.setAllowBinaryInfixRelations(true);
        lang.setAllowBinaryInfixNegations(false);
        lang.setInfixRelations( Map.ofEntries(entry("\ue8ac", ""), entry("\ue8a4", "")) );

        return lang;
    }

    private static Language Lnt() {
        Language lang = new Language("Lnt");
        lang.setNameTexts("\u2112", "","NT");

        lang.setOpenBracket1("("); lang.setCloseBracket1(")");
        lang.setOpenBracket2("["); lang.setCloseBracket2("]");
        lang.setAllowDroppedBrackets(true);

        lang.setNegation("\u223c");
        lang.setConditional("\u2192");
        lang.setBiconditional("\u2194");
        lang.setConjunction("\u2227");
        lang.setDisjunction("\u2228");
        lang.setUniversalQuant("\u2200");
        lang.setExistentialQuant("\u2203");

        lang.setVariables(Alphabets.getCharacterRange("\ud835\udc4e", "\ud835\udc67"));
        lang.setVariableSubs(true);
        lang.setConstants(Collections.singletonList("\ue886"));

        lang.setOnePlaceFunctionSymbols(Collections.singletonList("\ud835\udc46"));
        lang.setTwoPlaceFunctionSymbols(Arrays.asList("\ue8b8", "\ue8ba"));
        lang.setAllowBinaryInfixFunctions(true);

        lang.setAllowBinaryInfixRelations(true);
        lang.setAllowBinaryInfixNegations(true);
        lang.setInfixRelations( Map.ofEntries(
                entry("\ue8ac", "\ue8ad"),  //equal
                entry("\ue8a4", "\ue8a5"),  //less than
                entry("\ue8a6", "\ue8a7")   //leq
        ));

        lang.setAllowBoundedQuantifiers(true);
        lang.setDividerSymbol(":");

        return lang;
    }






}
