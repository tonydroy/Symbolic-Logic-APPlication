package slapp.editor.vert_tree_abefexplain;

import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.vert_tree_abexplain.ABExpTreeFormulaBox;
import slapp.editor.vertical_tree.VTAuxTreeNode;

import java.util.List;

public class ABEFExpTreeNode implements VTAuxTreeNode {
    ABEFExpTreeFormulaBox mainTreeBox;
    VTAuxTreeNode mate;
    List<ABEFExpTreeNode> children;
    List<ABEFExpTreeNode> parents;

    public ABEFExpTreeNode(ABEFExpTreeFormulaBox mainTreeBox) {
        this.mainTreeBox = mainTreeBox;
    }

    public ABEFExpTreeFormulaBox getMainTreeBox() {
        return mainTreeBox;
    }

    public VTAuxTreeNode getMate() {
        return mate;
    }

    public void setMate(VTAuxTreeNode mate) {
        this.mate = mate;
    }

    public List<ABEFExpTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ABEFExpTreeNode> children) {
        this.children = children;
    }

    public List<ABEFExpTreeNode> getParents() {
        return parents;
    }

    public void setParents(List<ABEFExpTreeNode> parents) {
        this.parents = parents;
    }

    @Override
    public BoxedDRTA getMainFormulaBox() {
        return mainTreeBox.getFormulaBox();
    }
}
