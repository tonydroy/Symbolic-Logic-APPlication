package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TTreeCheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    String objLangName;
    String metLangName;
    boolean checkSuccess;
    String checkMessage;
    boolean checkGreen;
    boolean checkNatural;
    boolean excludeFormalTree;
    boolean parseTreeFromAux;
    boolean treesOptional;
    boolean treesOmitted;
    List<Document> targetDocs;
    int checkMax = 0;
    int checkTries = 0;
    boolean staticHelp;
    Document staticHelpDoc;
    Document keyInterpFnDoc = new Document();
    double keyInterpFnTextHeight;





    public TTreeCheckSetup() {

        objLangName = "\u2112\ud835\udcc8 (w/abv)";  //Ls (abv)
        metLangName = "LM Meta";
        checkSuccess = false;
        checkMessage = "";
        checkNatural = false;
        checkMax = 0;              //0
        checkTries = 0;
        staticHelp = false;      //false
        staticHelpDoc = new Document();
        targetDocs = new ArrayList<>();
        treesOptional = false;
        treesOmitted = false;
    }


    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public void setCheckSuccess(boolean checkSuccess) {
        this.checkSuccess = checkSuccess;
    }

    public boolean isCheckNatural() {
        return checkNatural;
    }

    public void setCheckNatural(boolean checkNatural) {
        this.checkNatural = checkNatural;
    }

    public boolean isExcludeFormalTree() {
        return excludeFormalTree;
    }

    public boolean isParseTreeFromAux() {
        return parseTreeFromAux;
    }

    public void setParseTreeFromAux(boolean parseTreeFromAux) {
        this.parseTreeFromAux = parseTreeFromAux;
    }

    public void setExcludeFormalTree(boolean excludeFormalTree) {
        this.excludeFormalTree = excludeFormalTree;
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

    public List<Document> getTargetDocs() {
        return targetDocs;
    }


    public void setTargetDocs(List<Document> targetFormulas) {
        this.targetDocs = targetFormulas;
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

    public void setKeyInterpFnTextHeight(double mInterpFnTextHeight) {
        this.keyInterpFnTextHeight = mInterpFnTextHeight;
    }

    public boolean isCheckGreen() {
        return checkGreen;
    }

    public void setCheckGreen(boolean checkGreen) {
        this.checkGreen = checkGreen;
    }

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public boolean isTreesOptional() {
        return treesOptional;
    }

    public void setTreesOptional(boolean treesOptional) {
        this.treesOptional = treesOptional;
    }

    public boolean isTreesOmitted() {
        return treesOmitted;
    }

    public void setTreesOmitted(boolean treesOmitted) {
        this.treesOmitted = treesOmitted;
    }
}
