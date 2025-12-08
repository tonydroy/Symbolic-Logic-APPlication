package slapp.editor.vertical_tree;

import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TreeNodeException extends Exception {

    private List<Text> messageList = new ArrayList<Text>();
    private VTAuxTreeNode auxTreeNode;

    public TreeNodeException(VTAuxTreeNode auxTreeNode, List<Text> messageList) {
        super();
        this.auxTreeNode = auxTreeNode;
        this.messageList = messageList;
    }


    public List<Text> getMessageList() {
        return messageList;
    }

    public VTAuxTreeNode getAuxTreeNode() {
        return auxTreeNode;
    }
}
