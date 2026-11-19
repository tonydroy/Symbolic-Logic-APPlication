package slapp.editor.trans_uline;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

public class ULcheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    private boolean checkSuccess;
    private boolean choiceSuccess;
    private String checkMessage;
    private int checkMax;
    private int checkTries;
    private boolean staticHelp;
    private Document staticHelpDoc;
    private boolean checkChoices;
    private ULTreeObj exerKeyTreeObj;







    public ULcheckSetup() {

        checkSuccess = false;
        choiceSuccess = false;  //vestigial
        checkMessage = "Choice / Lines / Op";
        checkMax = 0;              //0
        checkTries = 0;
        staticHelp = false;      //false
        staticHelpDoc = new Document();
        checkChoices = false;
        exerKeyTreeObj = null;
    }

    public boolean isCheckChoices() {
        return checkChoices;
    }

    public void setCheckChoices(boolean checkChoices) {
        this.checkChoices = checkChoices;
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

    public boolean isChoiceSuccess() {
        return choiceSuccess;
    }

    public void setChoiceSuccess(boolean choiceSuccess) {
        this.choiceSuccess = choiceSuccess;
    }

    public ULTreeObj getExerKeyTreeObj() {
        return exerKeyTreeObj;
    }

    public void setExerKeyTreeObj(ULTreeObj exerKeyTreeObj) {
        this.exerKeyTreeObj = exerKeyTreeObj;
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
}
