package slapp.editor.parser.symbols;

import java.util.ArrayList;
import java.util.List;

public class MSentenceLetter extends SentenceLetter {

    private static List<MSentenceLetter> sentenceLetters = new ArrayList<MSentenceLetter>();
    private SentenceLetter matchLetter;

    private MSentenceLetter(String baseSymbol, String subscriptSymbol) {
        super(baseSymbol, subscriptSymbol);
        sentenceLetters.add(this);
    }

    public static MSentenceLetter getInstance(String baseSymbol, String subscriptSymbol) {
        MSentenceLetter let = null;
        for (MSentenceLetter sentenceLetter : sentenceLetters) {
            if (sentenceLetter.getBaseStr().equals(baseSymbol) && sentenceLetter.getSubscriptStr().equals(subscriptSymbol)) {
                let = sentenceLetter;
                break;
            }
        }
        if (let == null) { let = new MSentenceLetter(baseSymbol, subscriptSymbol);}
        return let;
    }

    @Override
    public SentenceLetter getMatch() { return matchLetter; }

    public void setMatch(SentenceLetter match) { this.matchLetter = match; }

}
