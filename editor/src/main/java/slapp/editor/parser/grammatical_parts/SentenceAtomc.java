package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.SentenceLetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SentenceAtomc extends Formula implements Expression {
    SentenceLetter mainLetter;

    public SentenceAtomc(SentenceLetter mainLetter) {
        super();
        setAtomic(true);
        this.mainLetter = mainLetter;
    }

    @Override
    public List<Text> toTextList() {

        List<Text> list = new ArrayList<Text>();
        list.addAll(mainLetter.toTextList());
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mainLetter.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof SentenceAtomc) {
            SentenceAtomc other = (SentenceAtomc) o;
            boolean equals = true;
            if (getSubTransform() == null) {
                if (other.getSubTransform() != null) equals = false;
            }
            else if (!getSubTransform().equals(other.getSubTransform())) {
                equals = false;
            }
            if (!mainLetter.equals(other.mainLetter)) equals = false;

            return equals;
        }
        return false;
    }

        @Override
        public int hashCode() {
            return mainLetter.hashCode() + Objects.hashCode(getSubTransform());
        }
}
