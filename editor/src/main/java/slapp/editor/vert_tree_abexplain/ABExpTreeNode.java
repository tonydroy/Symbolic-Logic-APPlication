package slapp.editor.vert_tree_abexplain;

import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.vert_tree_explain.ExpTreeFormulaBox;
import slapp.editor.vertical_tree.VTAuxTreeNode;

import java.util.List;

public class ABExpTreeNode implements VTAuxTreeNode {
    ABExpTreeFormulaBox mainTreeBox;
    VTAuxTreeNode mate;
    List<ABExpTreeNode> children;
    List<ABExpTreeNode> parents;

    public ABExpTreeNode(ABExpTreeFormulaBox mainTreeBox) {
        this.mainTreeBox = mainTreeBox;
    }

    public ABExpTreeFormulaBox getMainTreeBox() {
        return mainTreeBox;
    }

    public VTAuxTreeNode getMate() {
        return mate;
    }

    public void setMate(VTAuxTreeNode mate) {
        this.mate = mate;
    }

    public List<ABExpTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ABExpTreeNode> children) {
        this.children = children;
    }

    public List<ABExpTreeNode> getParents() {
        return parents;
    }

    public void setParents(List<ABExpTreeNode> parents) {
        this.parents = parents;
    }

    @Override
    public BoxedDRTA getMainFormulaBox() {
        return mainTreeBox.getFormulaBox();
    }
}
