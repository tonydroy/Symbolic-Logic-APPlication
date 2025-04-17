package slapp.editor;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;

import java.util.ArrayList;
import java.util.List;

public class ReplacementTxtMsgException extends TextMessageException {



    public ReplacementTxtMsgException(List<Text> messageList) {
        super(messageList);

    }

}
