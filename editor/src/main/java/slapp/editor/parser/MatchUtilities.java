package slapp.editor.parser;

import slapp.editor.parser.grammatical_parts.MFormula;
import slapp.editor.parser.grammatical_parts.MTerm;
import slapp.editor.parser.symbols.MConstant;
import slapp.editor.parser.symbols.MVariable;

public class MatchUtilities {

    public void clearMatching() {
        MVariable.clear();
        MFormula.clear();
        MTerm.clear();
        MConstant.clear();

    }

}
