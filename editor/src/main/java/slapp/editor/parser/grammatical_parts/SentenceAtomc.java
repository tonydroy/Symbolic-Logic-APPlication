package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.RelationSymbol;
import slapp.editor.parser.symbols.SentenceLetter;

import java.util.ArrayList;
import java.util.List;

public class SentenceAtomc extends Formula implements Expression {
    SentenceLetter mainLetter;

    public SentenceAtomc(SentenceLetter mainLetter) {
        super();
        setAtomic(true);
        this.mainLetter = mainLetter;
    }

    @Override
    public TextFlow toTextFlow() {
        return mainLetter.toTextFlow();
    }

    @Override
    public String toString() {
        return mainLetter.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof SentenceAtomc) {
            SentenceAtomc other = (SentenceAtomc) o;
            return mainLetter.equals(other.mainLetter);
        }
        return false;
    }

        @Override
        public int hashCode() {
            return mainLetter.hashCode();
        }
}
