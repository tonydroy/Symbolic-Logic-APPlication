package slapp.editor.parser;

import slapp.editor.parser.grammatical_parts.MAnyExpression;
import slapp.editor.parser.grammatical_parts.MFormula;
import slapp.editor.parser.grammatical_parts.MTerm;
import slapp.editor.parser.symbols.*;

public class MatchUtilities {

    public void clearMatching() {
        MVariable.clear();
        MFormula.clear();
        MTerm.clear();
        MConstant.clear();
        MFunctionSymbol.clear();
        MRelationSymbol.clear();
        MSentenceLetter.clear();
        MAnyExpression.clear();
    }

}
