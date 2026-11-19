package slapp.editor.ab_explain;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

public class ABCheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    private boolean staticHelp;
    private Document staticHelpDoc = new Document();
    boolean checkSuccess;
    String checkMessage;
    boolean checkFinal;
    int checkMax;
    int checkTries;
    boolean choiceA;
    boolean choiceB;



    public ABCheckSetup() {
        checkSuccess = false;
        checkMessage = "Checkbox Choice";
        checkFinal = true;
        checkMax = 0;              //0
        checkTries = 0;
        staticHelp = false;      //false
        staticHelpDoc = new Document();
        choiceA = false;
        choiceB = false;
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

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public void setCheckTries(int checkTries) {
        this.checkTries = checkTries;
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
}
