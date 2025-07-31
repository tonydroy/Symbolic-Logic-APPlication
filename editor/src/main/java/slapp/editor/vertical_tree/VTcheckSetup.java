package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

public class VTcheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    String objLangName;
    String metLangName;

    boolean checkSuccess;
    boolean checkFinal;
    int checkMax;
    int checkTries;
    boolean checkJustifications;

    boolean staticHelpButton;
    Document staticHelpDoc;

    Document formulaTarget;
    String auxExerName;
    VTCheckType checkType;

    public VTcheckSetup() {
      //  objLangName = "\u2112\u0274\u1d1b<"; //Lnt<>
     //   objLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (abv)
        objLangName = "\u2112\ud835\udcc6";   //Lq
    //    objLangName = "\u2112\ud835\udcc8 (w/abv)";
    //    objLangName = "\u2112\ud835\udcc8"; //Ls
        metLangName = "LM Meta";
        checkSuccess = false;
        checkFinal = true;
        checkMax = -1;              //0
        checkTries = 0;
        checkJustifications = true;
        staticHelpButton = true;      //false
        staticHelpDoc = new Document();
        auxExerName = "VTLq-abv";
        checkType = VTCheckType.UNABB;

    }

    public String getAuxExerName() {return auxExerName;}

    public String getObjLangName() {
        return objLangName;
    }

    public void setObjLangName(String objLangName) {
        this.objLangName = objLangName;
    }

    public String getMetLangName() {
        return metLangName;
    }

    public void setMetLangName(String metLangName) {
        this.metLangName = metLangName;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public boolean isCheckFinal() {
        return checkFinal;
    }

    public void setCheckFinal(boolean checkFinal) {
        this.checkFinal = checkFinal;
    }

    public int getCheckMax() {
        return checkMax;
    }

    public void setCheckMax(int checkMax) {
        this.checkMax = checkMax;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public void setCheckTries(int checkTries) {
        this.checkTries = checkTries;
    }

    public boolean isStaticHelpButton() {
        return staticHelpButton;
    }

    public void setStaticHelpButton(boolean staticHelpButton) {
        this.staticHelpButton = staticHelpButton;
    }

    public Document getStaticHelpDoc() {
        return staticHelpDoc;
    }

    public void setStaticHelpDoc(Document staticHelpDoc) {
        this.staticHelpDoc = staticHelpDoc;
    }

    public Document getFormulaTarget() {
        return formulaTarget;
    }

    public void setFormulaTarget(Document formulaTarget) {
        this.formulaTarget = formulaTarget;
    }

    public VTCheckType getCheckType() {
        return checkType;
    }

    public boolean isCheckJustifications() {
        return checkJustifications;
    }

    public void setCheckJustifications(boolean checkJustifications) {
        this.checkJustifications = checkJustifications;
    }

    public void setAuxExerName(String auxExerName) {
        this.auxExerName = auxExerName;
    }

    public void setCheckType(VTCheckType checkType) {
        this.checkType = checkType;
    }
}
