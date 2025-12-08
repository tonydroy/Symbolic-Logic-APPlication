package slapp.editor.m_parse.m_grammatical_parts;

import slapp.editor.m_parse.m_symbols.MInterpretationVariable;

import java.util.HashMap;
import java.util.Map;

public class MInterpretations {

    private Map<MInterpretationVariable, MInterpretation> interpretations = new HashMap<>();

    public MInterpretation getInterpretation(MInterpretationVariable variable) {
        if (interpretations.containsKey(variable)) return interpretations.get(variable);
        else {
            MInterpretation interpretation = new MInterpretation();
            interpretations.put(variable, interpretation);
            return interpretation;
        }
    }

    public MInterpretation getNBarInterpretation(MInterpretationVariable variable) {
        if (interpretations.containsKey(variable)) return interpretations.get(variable);
        else {
            NBarMinterpretation nBarMinterpretation = new NBarMinterpretation();
            interpretations.put(variable, nBarMinterpretation);
            return nBarMinterpretation;
        }
    }

    public Map<MInterpretationVariable, MInterpretation> getMap() {
        return interpretations;
    }
}
