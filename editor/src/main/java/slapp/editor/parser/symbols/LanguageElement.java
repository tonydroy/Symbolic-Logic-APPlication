package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.ExpressionType;

import java.io.Serializable;
import java.util.List;

public class LanguageElement implements Serializable {
    private static final long serialVersionUID = 100L;

    public List<Text> toTextList() {
        return null;
    }

    public int getPlaces() { return 0; }

    public ExpressionType getType() {return null;}

    public String getBaseStr() {return null;}

    public String getSubscriptStr() {return null;}


}
