package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

public class VTcheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    String objLangName;
    String metLangName;

    boolean checkSuccess;
    boolean choiceSuccess;
    boolean checkFinal;
    int checkMax;
    int checkTries;
    boolean checkJustifications;
    boolean checkMarkup;

    boolean staticHelp;
    Document staticHelpDoc;

    Document formulaTarget;
    String auxExerName;
    VTCheckType checkType;

    boolean checkChoices;
    boolean choiceA;
    boolean choiceB;
    boolean choiceE;
    boolean choiceF;


    public VTcheckSetup() {

        objLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (abv)
        metLangName = "LM Meta";
        checkSuccess = false;
        choiceSuccess = false;
        checkFinal = true;
        checkMax = 0;              //0
        checkTries = 0;
        checkJustifications = false;
        staticHelp = false;      //false
        staticHelpDoc = new Document();
        auxExerName = "";
        checkType = VTCheckType.NONE;
        checkMarkup = false;
        formulaTarget = new Document();
        checkChoices = false;
        choiceA = false;
        choiceB = false;
        choiceE = false;
        choiceF = false;

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

    public boolean isStaticHelp() {
        return staticHelp;
    }

    public void setStaticHelp(boolean staticHelp) {
        this.staticHelp = staticHelp;
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

    public boolean isCheckMarkup() {
        return checkMarkup;
    }

    public void setCheckMarkup(boolean checkMarkup) {
        this.checkMarkup = checkMarkup;
    }

    public boolean isCheckChoices() {
        return checkChoices;
    }

    public void setCheckChoices(boolean checkChoices) {
        this.checkChoices = checkChoices;
    }

    public boolean isChoiceA() {
        return choiceA;
    }

    public void setChoiceA(boolean choiceA) {
        this.choiceA = choiceA;
    }

    public boolean isChoiceB() {
        return choiceB;
    }

    public void setChoiceB(boolean choiceB) {
        this.choiceB = choiceB;
    }

    public boolean isChoiceE() {
        return choiceE;
    }

    public void setChoiceE(boolean choiceE) {
        this.choiceE = choiceE;
    }

    public boolean isChoiceF() {
        return choiceF;
    }

    public void setChoiceF(boolean choiceF) {
        this.choiceF = choiceF;
    }

    public boolean isChoiceSuccess() {
        return choiceSuccess;
    }

    public void setChoiceSuccess(boolean choiceSuccess) {
        this.choiceSuccess = choiceSuccess;
    }
}
