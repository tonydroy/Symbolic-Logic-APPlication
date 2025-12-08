package slapp.editor.map_abexplain;

import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.vert_tree_abexplain.ABExpTreeFormulaBox;
import slapp.editor.vertical_tree.VTAuxTreeNode;

import java.util.List;

public class MapABExpTreeNode implements VTAuxTreeNode {
    slapp.editor.map_abexplain.MapABExpTreeFormulaBox mainTreeBox;
    VTAuxTreeNode mate;
    List<MapABExpTreeNode> children;
    List<MapABExpTreeNode> parents;

    public MapABExpTreeNode(slapp.editor.map_abexplain.MapABExpTreeFormulaBox mainTreeBox) {
        this.mainTreeBox = mainTreeBox;
    }

    public MapABExpTreeFormulaBox getMainTreeBox() {
        return mainTreeBox;
    }

    public VTAuxTreeNode getMate() {
        return mate;
    }

    public void setMate(VTAuxTreeNode mate) {
        this.mate = mate;
    }

    public List<MapABExpTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<MapABExpTreeNode> children) {
        this.children = children;
    }

    public List<MapABExpTreeNode> getParents() {
        return parents;
    }

    public void setParents(List<MapABExpTreeNode> parents) {
        this.parents = parents;
    }

    @Override
    public BoxedDRTA getMainFormulaBox() {
        return mainTreeBox.getFormulaBox();
    }
}
