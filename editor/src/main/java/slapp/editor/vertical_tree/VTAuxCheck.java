package slapp.editor.vertical_tree;

import java.util.List;

public interface VTAuxCheck {

    void setCheckType(VTCheckType type);

    boolean checkTree();

    List<List<TreeNode>> getFormulaTree();

    String getObjLangName();
}
