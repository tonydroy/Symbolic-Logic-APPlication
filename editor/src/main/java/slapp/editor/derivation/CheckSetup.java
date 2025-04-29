package slapp.editor.derivation;

import com.gluonhq.richtextarea.model.Document;
import javafx.util.Pair;
import slapp.editor.derivation.theorems.ThrmSetElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CheckSetup implements Serializable {

    String rulesetName;
    String objLangName;
    String metLangName;
    List<ThrmSetElement> thrmSetElements;

    boolean checkSuccess;
    boolean checkFinal;

    int checkMax;
    int checkTries;

    int helpMax;
    int helpTries;
    boolean contextualHelp;
    Document staticHelpDoc;
    List<Pair<String, Boolean>> theoremSets;
    boolean showMetalanguageButton;


    public CheckSetup() {
        rulesetName = "\ud835\udc41\ud835\udc37+";  //ND+
        objLangName = "\u2112\ud835\udcc6 (w/abv)";  //Lq (abv)
        metLangName = "LM Meta";

        checkSuccess = false;
        checkFinal = false;

        checkMax = 10;
        checkTries = 0;
        helpMax = 3;
        helpTries = 0;
        contextualHelp = true;
        staticHelpDoc = new Document();

        theoremSets = new ArrayList();
        thrmSetElements = new ArrayList();
        showMetalanguageButton = false;


    }



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

    public String getRulesetName() {
        return rulesetName;
    }

    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
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

    public int getHelpMax() {
        return helpMax;
    }

    public void setHelpMax(int helpMax) {
        this.helpMax = helpMax;
    }

    public int getHelpTries() {
        return helpTries;
    }

    public void setHelpTries(int helpTries) {
        this.helpTries = helpTries;
    }

    public boolean isContextualHelp() {
        return contextualHelp;
    }

    public void setContextualHelp(boolean contextualHelp) {
        this.contextualHelp = contextualHelp;
    }

    public Document getStaticHelpDoc() {
        return staticHelpDoc;
    }

    public void setStaticHelpDoc(Document staticHelpDoc) {
        this.staticHelpDoc = staticHelpDoc;
    }

    public List<Pair<String, Boolean>> getTheoremSets() {
        return theoremSets;
    }

    public void setTheoremSets(List<Pair<String, Boolean>> theoremSets) {
        this.theoremSets = theoremSets;
    }

    public boolean isShowMetalanguageButton() {
        return showMetalanguageButton;
    }

    public void setShowMetalanguageButton(boolean showMetalanguageButton) {
        this.showMetalanguageButton = showMetalanguageButton;
    }

    public void setThrmSetElements(List<ThrmSetElement> thrmSetElements) {
        this.thrmSetElements = thrmSetElements;
    }
}
