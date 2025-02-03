package slapp.editor.parser;

import java.util.ArrayList;
import java.util.List;

public class Languages {

    private static Languages instance = null;

    private static List<Language> fixedLanguages = new ArrayList<Language>();
    private static List<Language> optionalLanguages = new ArrayList<Language>();

    private Languages(){
        loadFixedLanguages();
    }

    public static Languages getInstance(){
        if (instance == null){
            instance = new Languages();
        }
        return instance;
    }

    private static void loadFixedLanguages(){
        fixedLanguages.add(Lq());
    }

    static Language getLanguage(String name) {
        for (Language l : fixedLanguages){
            if (l.getName().equals(name)) return l;
        }
        for (Language l : optionalLanguages){
            if (l.getName().equals(name)) return l;
        }
        return null;
    }


    private static Language Lq() {
        Language lang = new Language("Lq");

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
        lang.setXrelationSymbols(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setXrelationSymbolSubs(true);
        lang.setXrelationSymbolsRequireSuper(true);
        lang.setXfunctionSymbols(Alphabets.getCharacterRange("\ud835\udc4e", "\ud835\udc67"));
        lang.setXfunctionSymbolSubs(true);
        return lang;
    }

}
