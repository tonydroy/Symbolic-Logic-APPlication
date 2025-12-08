package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;

import java.util.ArrayList;
import java.util.List;

public class MSentenceLetter extends SentenceLetter {

    private static List<MSentenceLetter> sentenceLetters = new ArrayList<MSentenceLetter>();
    private SentenceLetter matchLetter = null;

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


    public static List<MSentenceLetter> getSentenceLetters() {
        return sentenceLetters;
    }

    public SentenceLetter getMatchLetter() {
        return matchLetter;
    }

    @Override
    public SentenceLetter getMatch() { return matchLetter; }

    public void setMatch(SentenceLetter match) throws TextMessageException {
        if (matchLetter == null) { matchLetter = match; }
        else if (!matchLetter.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Meta symbol "));
            messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchLetter.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() {
        for (MSentenceLetter sentenceLetter : sentenceLetters) {
            sentenceLetter.clearMatch();
        }
    }

    private void clearMatch() {
        matchLetter = null;
    }

}
