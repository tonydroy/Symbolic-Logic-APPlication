package slapp.editor;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

public class SLAPPdata implements Serializable {
    private static final long serialVersionUID = 100L;

    private Document metalanguageHelp = new Document("meta language help doc");


    public Document getMetalanguageHelp() {
        return metalanguageHelp;
    }

    public void setMetalanguageHelp(Document metalanguageHelp) {
        this.metalanguageHelp = metalanguageHelp;
    }
}
