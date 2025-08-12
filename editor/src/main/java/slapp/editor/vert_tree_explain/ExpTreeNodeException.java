package slapp.editor.vert_tree_explain;

import javafx.scene.text.Text;
import slapp.editor.vertical_tree.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class ExpTreeNodeException extends Exception {

    private List<Text> messageList = new ArrayList<Text>();
    private ExpTreeNode expTreeNode;

    public ExpTreeNodeException(ExpTreeNode treeNode, List<Text> messageList) {
        super();
        this.expTreeNode = treeNode;
        this.messageList = messageList;
    }


    public List<Text> getMessageList() {
        return messageList;
    }

    public ExpTreeNode getExpTreeNode() {
        return expTreeNode;
    }
}
