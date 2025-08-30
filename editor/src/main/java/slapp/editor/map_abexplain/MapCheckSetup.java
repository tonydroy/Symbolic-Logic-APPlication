package slapp.editor.map_abexplain;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.vertical_tree.VTCheckType;
import slapp.editor.vertical_tree.VTcheckSetup;

import java.io.Serializable;

public class MapCheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    String objLangName;
    boolean checkSuccess;
    boolean choiceSuccess;
    String checkMessage;
    boolean checkFinal;
    int checkMax;
    int checkTries;
    boolean staticHelp;
    Document staticHelpDoc;
    boolean checkChoices;
    boolean choiceA;
    boolean choiceB;

    public MapCheckSetup() {
        objLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (abv)
        checkSuccess = false;
        checkMessage = "";
        choiceSuccess = false;  //vestigial
        checkFinal = true;
        checkMax = 0;              //0
        checkTries = 0;
        staticHelp = false;      //false
        staticHelpDoc = new Document();
        checkChoices = false;
        choiceA = false;
        choiceB = false;
    }

    public String getObjLangName() {
        return objLangName;
    }

    public void setObjLangName(String objLangName) {
        this.objLangName = objLangName;
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
}
