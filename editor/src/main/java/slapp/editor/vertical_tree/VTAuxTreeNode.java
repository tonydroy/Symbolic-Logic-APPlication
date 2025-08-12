package slapp.editor.vertical_tree;

import slapp.editor.decorated_rta.BoxedDRTA;

import java.util.List;

public interface VTAuxTreeNode {

    BoxedDRTA getMainFormulaBox();

    List<? extends VTAuxTreeNode> getParents();

}
