package slapp.editor.vertical_tree;

import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.vertical_tree.drag_drop.TreeFormulaBox;

import java.util.ArrayList;
import java.util.List;

public class TreeNode implements VTAuxTreeNode {
    TreeFormulaBox mainTreeBox;
    VTAuxTreeNode mate;
    List<TreeNode> children;
    List<TreeNode> parents;

    public TreeNode(TreeFormulaBox mainTreeBox) {
        this.mainTreeBox = mainTreeBox;
    }

    public TreeFormulaBox getMainTreeBox() {
        return mainTreeBox;
    }

    public VTAuxTreeNode getMate() {
        return mate;
    }

    public void setMate(VTAuxTreeNode mate) {
        this.mate = mate;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public List<TreeNode> getParents() {
        return parents;
    }

    public void setParents(List<TreeNode> parents) {
        this.parents = parents;
    }

    @Override
    public BoxedDRTA getMainFormulaBox() {
        return mainTreeBox.getFormulaBox();
    }
}
