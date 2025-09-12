package slapp.editor.truth_table;

import java.io.Serializable;

public class TTcheckSetup implements Serializable {

    String objLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (abv)
    boolean checkSuccess;
    boolean choiceSuccess;
    String checkMessage;
    boolean checkFinal;
    int checkMax;
    int checkTries;


    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public boolean isChoiceSuccess() {
        return choiceSuccess;
    }

    public void setChoiceSuccess(boolean choiceSuccess) {
        this.choiceSuccess = choiceSuccess;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
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

    public String getObjLangName() {
        return objLangName;
    }

    public void setObjLangName(String objLangName) {
        this.objLangName = objLangName;
    }
}
