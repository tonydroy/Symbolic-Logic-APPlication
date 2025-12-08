package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.MInterpretationVariable;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

public class NBarMinterpretation extends MInterpretation {

    @Override
    public List<Text> toTextList(MInterpretationVariable var) {
        List<Text> textList = new ArrayList<>();
        textList.add(ParseUtilities.newRegularText("\ud835\uddb4\ue3bd = {\ud835\udfe2 . . . \ud835\udfeb\ud835\udfeb}\n"));
        textList.add(ParseUtilities.newRegularText("\ue3bd[\ue886] = \ud835\udfe2\n"));
        textList.add(ParseUtilities.newRegularText("\ue3bd[\ue8ac] = {\u27e8\ud835\uddc6, \ud835\uddc7\u27e9 | \ud835\uddc6,\ud835\uddc7 \u2208 \ud835\uddb4, and \ud835\uddc6 = \ud835\uddc7}\n" ));
        textList.add(ParseUtilities.newRegularText("\ue3bd[\ue8a4] = {\u27e8\ud835\uddc6, \ud835\uddc7\u27e9 | \ud835\uddc6,\ud835\uddc7 \u2208 \ud835\uddb4, and \ud835\uddc6 < \ud835\uddc7}\n" ));
        textList.add(ParseUtilities.newRegularText("\ue3bd[\ud835\udc46] = {⟨𝗆, 𝗇⟩ | 𝗆,𝗇 ∈ \ud835\uddb4 and \ud835\uddc6 + \ud835\udfe3 = \ud835\uddc7, mod \ud835\udfe3\ud835\udfe2\ud835\udfe2}\n"));
        textList.add(ParseUtilities.newRegularText("\ue3bd[\ue8b8] = {\u27e8\u27e8\ud835\uddc6, \ud835\uddc7\u27e9, \ud835\uddc8\u27e9 | \ud835\uddc6,\ud835\uddc7,\ud835\uddc8 \u2208 \ud835\uddb4 and \ud835\uddc6 + \ud835\uddc7 = \ud835\uddc8, mod \ud835\udfe3\ud835\udfe2\ud835\udfe2}\n"));
        textList.add(ParseUtilities.newRegularText("\ue3bd[\ue8ba] = {\u27e8\u27e8\ud835\uddc6, \ud835\uddc7\u27e9, \ud835\uddc8\u27e9 | \ud835\uddc6,\ud835\uddc7,\ud835\uddc8 \u2208 \ud835\uddb4 and \ud835\uddc6 \u00d7 \ud835\uddc7 = \ud835\uddc8, mod \ud835\udfe3\ud835\udfe2\ud835\udfe2}"));
        return textList;
    }
}
