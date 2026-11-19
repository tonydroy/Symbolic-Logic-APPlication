package slapp.editor.free_form;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

public class FFCheckSetup implements Serializable {
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
