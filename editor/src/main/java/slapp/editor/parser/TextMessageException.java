package slapp.editor.parser;

import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TextMessageException extends Exception {

    private List<Text> messageList = new ArrayList<Text>();

    public TextMessageException(List<Text> messageList) {
        super();
        this.messageList = messageList;
    }


    public List<Text> getMessageList() {
        return messageList;
    }
}
