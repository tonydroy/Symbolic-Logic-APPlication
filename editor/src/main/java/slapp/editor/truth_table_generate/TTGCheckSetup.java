package slapp.editor.truth_table_generate;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TTGCheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    boolean checkSuccess;
    String checkMessage;
    boolean checkFinal;
    int checkMax;
    int checkTries;
    private boolean staticHelp;
    private Document staticHelpDoc = new Document();
    Document keyInterpFnDoc = new Document();
    double keyInterpFnTextHeight;
    List<Document> keyFormulas = new ArrayList<>();

    private boolean autoCheckValidity;
    private boolean checkChoices;
    private boolean choiceA;
    private boolean choiceB;

    private boolean skipBasicsOK;
    private boolean permitShortTable;
    private boolean shortTable;

    public TTGCheckSetup() {
        checkSuccess = false;
        checkMessage = "Translate/Table";
        checkFinal = true;
        checkMax = 0;              //0
        checkTries = 0;
        staticHelp = false;      //false
        staticHelpDoc = new Document();
        choiceA = false;
        choiceB = false;

        checkChoices = false;
        choiceA = false;
        choiceB = false;
        skipBasicsOK = false;
        permitShortTable = true;
        shortTable = false;
    }

    public boolean isAutoCheckValidity() {
        return autoCheckValidity;
    }

    public void setAutoCheckValidity(boolean autoCheckValidity) {
        this.autoCheckValidity = autoCheckValidity;
    }

    public boolean isCheckChoices() {
        return checkChoices;
    }

    public void setCheckChoices(boolean checkChoices) {
        this.checkChoices = checkChoices;
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

    public boolean isSkipBasicsOK() {
        return skipBasicsOK;
    }

    public void setSkipBasicsOK(boolean skipBasicsOK) {
        this.skipBasicsOK = skipBasicsOK;
    }

    public Document getKeyInterpFnDoc() {
        return keyInterpFnDoc;
    }

    public void setKeyInterpFnDoc(Document keyInterpFnDoc) {
        this.keyInterpFnDoc = keyInterpFnDoc;
    }

    public double getKeyInterpFnTextHeight() {
        return keyInterpFnTextHeight;
    }

    public void setKeyInterpFnTextHeight(double keyInterpFnTextHeight) {
        this.keyInterpFnTextHeight = keyInterpFnTextHeight;
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

    public List<Document> getKeyFormulas() {
        return keyFormulas;
    }

    public void setKeyFormulas(List<Document> keyFormulas) {
        this.keyFormulas = keyFormulas;
    }
}
