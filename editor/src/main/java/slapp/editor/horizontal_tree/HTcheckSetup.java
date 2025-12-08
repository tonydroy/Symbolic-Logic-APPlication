package slapp.editor.horizontal_tree;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HTcheckSetup implements Serializable {
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
    private Document interpretationDoc;
    private double interpretationTextHeight;
    private double interpretationPrefHeight;
    private boolean emptyBranchesOK;
    private boolean emptyBranchesSelected;
    private List<Document> targetFormulas = new ArrayList<>();


    public HTcheckSetup() {
        objLangName = "\u2112\ud835\udcc6 (w/abv)";
        checkSuccess = false;
        choiceSuccess = false;
        checkMessage = "";
        checkFinal = true;
        checkMax = 0;  // 0
        checkTries = 0;
        staticHelp = false;
        staticHelpDoc = new Document();
        emptyBranchesOK = false;
        emptyBranchesSelected = false;
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

    public boolean isEmptyBranchesOK() {
        return emptyBranchesOK;
    }

    public void setEmptyBranchesOK(boolean emptyBranchesOK) {
        this.emptyBranchesOK = emptyBranchesOK;
    }

    public void setTargetFormulas(List<Document> targetFormulas) {
        this.targetFormulas = targetFormulas;
    }

    public List<Document> getTargetFormulas() {
        return targetFormulas;
    }

    public Document getInterpretationDoc() {
        return interpretationDoc;
    }

    public void setInterpretationDoc(Document interpretationDoc) {
        this.interpretationDoc = interpretationDoc;
    }

    public double getInterpretationTextHeight() {
        return interpretationTextHeight;
    }

    public void setInterpretationTextHeight(double interpretationTextHeight) {
        this.interpretationTextHeight = interpretationTextHeight;
    }

    public double getInterpretationPrefHeight() {
        return interpretationPrefHeight;
    }

    public void setInterpretationPrefHeight(double interpretationPrefHeight) {
        this.interpretationPrefHeight = interpretationPrefHeight;
    }

    public boolean isEmptyBranchesSelected() {
        return emptyBranchesSelected;
    }

    public void setEmptyBranchesSelected(boolean emptyBranchesSelected) {
        this.emptyBranchesSelected = emptyBranchesSelected;
    }
}
