package slapp.editor.parser;

import java.util.ArrayList;
import java.util.List;

public class Languages {



    private static List<Language> fixedLanguages = new ArrayList<Language>();
    private static List<Language> optionalLanguages = new ArrayList<Language>();


    static {
        loadFixedLanguages();
    }

    public Languages() {  }



    private static void loadFixedLanguages(){
        Language lq = Lq();
        fixedLanguages.add(lq);
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
        lang.setXrelationSymbolsRequireSuper(false);
        lang.setXfunctionSymbols(Alphabets.getCharacterRange("\ud835\udc4e", "\ud835\udc67"));
        lang.setXfunctionSymbolSubs(true);
        return lang;
    }

}
