package slapp.editor.simple_edit;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;


//static help stub
public class SECheckSetup implements Serializable {
    private static final long serialVersionUID = 100L;

    private boolean staticHelp;
    private Document staticHelpDoc = new Document();

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
