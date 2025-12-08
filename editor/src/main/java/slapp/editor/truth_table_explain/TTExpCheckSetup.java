package slapp.editor.truth_table_explain;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

public class TTExpCheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    private String objLangName;
    private boolean checkSuccess;
    private boolean choiceSuccess;
    private String checkMessage;
    private boolean checkFinal;
    private int checkMax;
    private int checkTries;
    private boolean staticHelp;
    private Document staticHelpDoc;

    private boolean autoCheckValidity;
    private boolean checkChoices;
    private boolean choiceA;
    private boolean choiceB;

    private boolean skipBasicsOK;
    private boolean permitShortTable;
    private boolean shortTable;




    public TTExpCheckSetup() {
        objLangName = "\u2112\ud835\udcc8 (w/abv)";
        checkSuccess = false;
        choiceSuccess = false;
        checkMessage = "";
        checkFinal = true;
        checkMax = 0;  // 0
        checkTries = 0;
        staticHelp = false;
        staticHelpDoc = new Document();
        checkChoices = false;
        choiceA = false;
        choiceB = false;
        skipBasicsOK = false;
        permitShortTable = true;
        shortTable = false;

        autoCheckValidity = true;


        skipBasicsOK = true;
    }


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

    public boolean isSkipBasicsOK() {
        return skipBasicsOK;
    }

    public void setSkipBasicsOK(boolean skipBasicsOK) {
        this.skipBasicsOK = skipBasicsOK;
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

    public boolean isAutoCheckValidity() {
        return autoCheckValidity;
    }

    public void setAutoCheckValidity(boolean autoCheckValidity) {
        this.autoCheckValidity = autoCheckValidity;
    }

    public boolean isPermitShortTable() {
        return permitShortTable;
    }

    public void setPermitShortTable(boolean permitShortTable) {
        this.permitShortTable = permitShortTable;
    }

    public boolean isShortTable() {
        return shortTable;
    }

    public void setShortTable(boolean shortTable) {
        this.shortTable = shortTable;
    }
}
