package slapp.editor.vert_tree_explain;

import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.vertical_tree.VTAuxTreeNode;
import slapp.editor.vertical_tree.drag_drop.TreeFormulaBox;

import java.util.List;

public class ExpTreeNode implements VTAuxTreeNode {
    ExpTreeFormulaBox mainTreeBox;
    VTAuxTreeNode mate;
    List<ExpTreeNode> children;
    List<ExpTreeNode> parents;

    public ExpTreeNode(ExpTreeFormulaBox mainTreeBox) {
        this.mainTreeBox = mainTreeBox;
    }

    public ExpTreeFormulaBox getMainTreeBox() {
        return mainTreeBox;
    }

    public VTAuxTreeNode getMate() {
        return mate;
    }

    public void setMate(VTAuxTreeNode mate) {
        this.mate = mate;
    }

    public List<ExpTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ExpTreeNode> children) {
        this.children = children;
    }

    public List<ExpTreeNode> getParents() {
        return parents;
    }

    public void setParents(List<ExpTreeNode> parents) {
        this.parents = parents;
    }

    @Override
    public BoxedDRTA getMainFormulaBox() {
        return mainTreeBox.getFormulaBox();
    }
}
