package slapp.editor.basic_trans;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BTransCheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    private String objLangName  = "\u2112\ud835\udcc6 (w/abv)";
    private String metLangName = "Meta Sym";
    private boolean checkSuccess;
    private String checkMessage;
    private boolean checkGreen;
    List<Document> targetDocs = new ArrayList<>();
    int checkMax = 0;
    int checkTries = 0;
    boolean staticHelp;
    Document staticHelpDoc = new Document();

    Document keyInterpFnDoc = new Document();
    double keyInterpFnTextHeight;
    private boolean argument;


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

    public String getMetLangName() {
        return metLangName;
    }

    public void setMetLangName(String metLangName) {
        this.metLangName = metLangName;
    }

    public String getObjLangName() {
        return objLangName;
    }

    public void setObjLangName(String objLangName) {
        this.objLangName = objLangName;
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

    public void setTargetDocs(List<Document> targetDocs) {
        this.targetDocs = targetDocs;
    }

    public boolean isArgument() {
        return argument;
    }

    public void setArgument(boolean argument) {
        this.argument = argument;
    }
}


