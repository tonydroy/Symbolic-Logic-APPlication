package slapp.editor.vertical_tree;

import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TreeNodeException extends Exception {

    private List<Text> messageList = new ArrayList<Text>();
    private TreeNode treeNode;

    public TreeNodeException(TreeNode treeNode, List<Text> messageList) {
        super();
        this.treeNode = treeNode;
        this.messageList = messageList;
    }


    public List<Text> getMessageList() {
        return messageList;
    }

    public TreeNode getTreeNode() {
        return treeNode;
    }
}
