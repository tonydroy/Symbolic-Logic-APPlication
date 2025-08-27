package slapp.editor.map_abexplain;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.vertical_tree.VTCheckType;
import slapp.editor.vertical_tree.VTcheckSetup;

import java.io.Serializable;

public class MapCheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    String objLangName;

    public void setCheckTries(int checkTries) {}

    public void setCheckSuccess(boolean checkSuccess) {}

    public void setChoiceSuccess(boolean choiceSuccess) {}

    public boolean isStaticHelp() {
        return false;
    }

    public Document getStaticHelpDoc() {
        return null;
    }

    public int getCheckMax() {
        return 0;
    }

    public void setCheckMax(int checkMax) {}

    public boolean isCheckChoices() {
        return false;
    }

    public void setCheckChoices(boolean checkChoices) {}



    public boolean isChoiceA() {
        return false;
    }

    public void setChoiceA(boolean choiceA) {}

    public boolean isChoiceB() {
        return false;
    }

    public void setChoiceB(boolean choiceB) {}

    public String getObjLangName() {
        return objLangName;
    }

    public void setObjLangName(String objLangName) {
        this.objLangName = objLangName;
    }

    public void setStaticHelpDoc(Document helpDoc) {}

    public void setStaticHelp(boolean help) {}







    //*************

    public String getAuxExerName() {
        return "";
    }

    public void setAuxExerName(String auxExerName) {}

    public VTCheckType getCheckType() {
        return null;
    }

    public void setCheckType(VTCheckType checkType) {}

    public boolean isCheckJustifications() {
        return false;
    }

    public void setCheckJustifications(boolean checkJustifications) {}

    public Document getFormulaTarget() {
        return null;
    }

    public void setFormulaTarget(Document formulaTarget) {}

    public boolean isCheckMarkup() {
        return false;
    }

    public void setCheckMarkup(boolean checkMarkup) {}

}
