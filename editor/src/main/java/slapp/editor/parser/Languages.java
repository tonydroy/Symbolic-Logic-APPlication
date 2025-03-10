package slapp.editor.parser;

import com.gluonhq.richtextarea.model.Document;

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
        fixedLanguages.add(Meta());
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

        lang.setUnabbForms( Map.ofEntries(
                entry(UnabbType.NEGATION, new Document("\u223c |0|")),
                entry(UnabbType.CONDITIONAL, new Document("(|0| \u2192 |1|)")),
                entry(UnabbType.SENTENCE_LET, new Document("\u2039s\u203a"))
        ));

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

        lang.setUnabbForms( Map.ofEntries(
                entry(UnabbType.NEGATION, new Document("\u223c |0|")),
                entry(UnabbType.CONDITIONAL, new Document("(|0| \u2192 |1|)")),
                entry(UnabbType.BICONDITIONAL, new Document("\u223c((|0| \u2192 |1|) \u2192 \u223c(|1| \u2192 |0|))")),
                entry(UnabbType.CONJUNCTION, new Document("\u223c(|0| \u2192 \u223c|1|)")),
                entry(UnabbType.DISJUNCTION, new Document("(\u223c|0| \u2192 |1|)")),
                entry(UnabbType.SENTENCE_LET, new Document("\u2039s\u203a"))
        ));

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

        lang.setUnabbForms( Map.ofEntries(
                entry(UnabbType.NEGATION, new Document("\u223c |0|")),
                entry(UnabbType.CONDITIONAL, new Document("(|0| \u2192 |1|)")),
                entry(UnabbType.UNIVERSAL, new Document("\u2200\u2039v\u203a|0|")),
                entry(UnabbType.VARIABLE, new Document("\u2039s\u203a")),
                entry(UnabbType.CONSTANT, new Document("\u2039s\u203a")),
                entry(UnabbType.COMPLEX_TERM, new Document("\u2039f\u203a|n|")),
                entry(UnabbType.SENTENCE_LET, new Document("\u2039s\u203a")),
                entry(UnabbType.RELATION_ATOMIC, new Document("\u2039r\u203a|n|"))

        ));

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

        lang.setUnabbForms( Map.ofEntries(
                entry(UnabbType.NEGATION, new Document("\u223c |0|")),
                entry(UnabbType.CONDITIONAL, new Document("(|0| \u2192 |1|)")),
                entry(UnabbType.BICONDITIONAL, new Document("\u223c((|0| \u2192 |1|) \u2192 \u223c(|1| \u2192 |0|))")),
                entry(UnabbType.CONJUNCTION, new Document("\u223c(|0| \u2192 \u223c|1|)")),
                entry(UnabbType.DISJUNCTION, new Document("(\u223c|0| \u2192 |1|)")),
                entry(UnabbType.UNIVERSAL, new Document("\u2200\u2039v\u203a|0|")),
                entry(UnabbType.EXISTENTIAL, new Document("\u223c\u2200\u2039v\u203a\u223c|0|")),
                entry(UnabbType.VARIABLE, new Document("\u2039s\u203a")),
                entry(UnabbType.CONSTANT, new Document("\u2039s\u203a")),
                entry(UnabbType.COMPLEX_TERM, new Document("\u2039f\u203a|n|")),
                entry(UnabbType.SENTENCE_LET, new Document("\u2039s\u203a")),
                entry(UnabbType.RELATION_ATOMIC, new Document("\u2039r\u203a|n|")),
                entry(UnabbType.BOUNDED_QUANTIFIER, new Document("\u2039b\u203a|0|"))
        ));




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

        lang.setUnabbForms( Map.ofEntries(
                entry(UnabbType.NEGATION, new Document("\u223c|0|")),
                entry(UnabbType.CONDITIONAL, new Document("(|0| \u2192 |1|)")),
                entry(UnabbType.BICONDITIONAL, new Document("\u223c((|0| \u2192 |1|) \u2192 \u223c(|1| \u2192 |0|))")),
                entry(UnabbType.CONJUNCTION, new Document("\u223c(|0| \u2192 \u223c|1|)")),
                entry(UnabbType.DISJUNCTION, new Document("(\u223c|0| \u2192 |1|)")),
                entry(UnabbType.UNIVERSAL, new Document("\u2200\u2039v\u203a|0|")),
                entry(UnabbType.EXISTENTIAL, new Document("\u223c\u2200\u2039v\u203a\u223c|0|")),
                entry(UnabbType.VARIABLE, new Document("\u2039s\u203a")),
                entry(UnabbType.CONSTANT, new Document("\u2039s\u203a")),
                entry(UnabbType.COMPLEX_TERM, new Document("\u2039f\u203a|n|")),
                entry(UnabbType.RELATION_ATOMIC, new Document("\u2039r\u203a|n|"))

        ));

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

        lang.setUnabbForms( Map.ofEntries(
                entry(UnabbType.NEGATION, new Document("\u223c|0|")),
                entry(UnabbType.CONDITIONAL, new Document("(|0| \u2192 |1|)")),
                entry(UnabbType.BICONDITIONAL, new Document("\u223c((|0| \u2192 |1|) \u2192 \u223c(|1| \u2192 |0|))")),
                entry(UnabbType.CONJUNCTION, new Document("\u223c(|0| \u2192 \u223c|1|)")),
                entry(UnabbType.DISJUNCTION, new Document("(\u223c|0| \u2192 |1|)")),
                entry(UnabbType.UNIVERSAL, new Document("\u2200\u2039v\u203a|0|")),
                entry(UnabbType.EXISTENTIAL, new Document("\u223c\u2200\u2039v\u203a\u223c|0|")),
                entry(UnabbType.VARIABLE, new Document("\u2039s\u203a")),
                entry(UnabbType.CONSTANT, new Document("\u2039s\u203a")),
                entry(UnabbType.COMPLEX_TERM, new Document("\u2039f\u203a|n|")),
                entry(UnabbType.RELATION_ATOMIC, new Document("\u2039r\u203a|n|")),
                entry(UnabbType.BOUNDED_QUANTIFIER, new Document("\u2039b\u203a|0|"))
        ));
        return lang;
    }

    private static Language Meta() {
        Language lang = new Language("Meta");
        lang.setNameTexts("\u2112", "","meta");
        lang.setMetalanguage(true);

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
        lang.setNand("\u2191");
        lang.setNor("\u2193");
        lang.setDividerSymbol(":");

        lang.setVariables(Alphabets.getCharacterRange("\ud835\udcca", "\ud835\udccf"));
        lang.setVariableSubs(true);
        lang.setConstants(Alphabets.getCharacterRange("\ud835\udcb6", "\u212f"));
        lang.setConstantSubs(true);

        lang.setOnePlaceFunctionSymbols(Collections.singletonList("\ud835\udc46"));
        lang.setTwoPlaceFunctionSymbols(Arrays.asList("\ue8b8", "\ue8ba"));
        lang.setAllowBinaryInfixFunctions(true);
        lang.setXfunctionSymbols(Arrays.asList("\ud835\udcbb", "\u210a", "\ud835\udcbd"));
        lang.setXfunctionSymbolSubs(true);
        lang.setmTermSymbols(Alphabets.getCharacterRange("\ud835\udcc2", "\ud835\udcc9"));

        lang.setSentenceLetters(Collections.singletonList("\ud835\udcae"));
        lang.setSentenceLetterSubs(true);
        lang.setXrelationSymbols(Collections.singletonList("\u211b"));
        lang.setXrelationSymbolSubs(true);
        lang.setXrelationSymbolsRequireSuper(false);

        lang.setAllowBinaryInfixRelations(true);
        lang.setAllowBinaryInfixNegations(true);
        lang.setInfixRelations( Map.ofEntries(
                entry("\ue8ac", "\ue8ad"),  //equal
                entry("\ue8a4", "\ue8a5"),  //less than
                entry("\ue8a6", "\ue8a7")   //leq
        ));

        lang.setmFormulaSymbols(Alphabets.getCharacterRange("\ud835\udc9c", "\ud835\udcac"));
        lang.setArbitraryExpressionSymbols(Collections.singletonList("\ud835\udcb3"));

        return lang;
    }


}
