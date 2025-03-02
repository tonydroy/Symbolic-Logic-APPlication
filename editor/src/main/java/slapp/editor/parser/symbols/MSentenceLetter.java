package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;

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

    public void setMatch(SentenceLetter match) throws TextMessageException {
        if (matchLetter == null) { matchLetter = match; }
        else if (!matchLetter.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Variable "));
            messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(matchLetter.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() { sentenceLetters.clear(); }

}
