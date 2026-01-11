package slapp.editor;

import com.gluonhq.richtextarea.model.Document;

import javax.crypto.SecretKey;
import java.io.Serializable;

public class SlappProgData implements Serializable {
    private static final long serialVersionUID = 100L;

    private Document metalanguageHelp = new Document("meta language help doc");
    private Document mapCreateHelp = new Document("mapping create help doc");

    private SecretKey jxsecretKey;
    private byte[] jxdata;
    private byte[] jxcipher;


    public Document getMetalanguageHelp() {
        return metalanguageHelp;
    }

    public void setMetalanguageHelp(Document metalanguageHelp) {
        this.metalanguageHelp = metalanguageHelp;
    }

    public Document getMapCreateHelp() {
        return mapCreateHelp;
    }

    public void setMapCreateHelp(Document mapCreateHelp) {
        this.mapCreateHelp = mapCreateHelp;
    }

    public byte[] getJxcipher() {
        return jxcipher;
    }

    public void setJxcipher(byte[] jxcipher) {
        this.jxcipher = jxcipher;
    }

    public byte[] getJxdata() {
        return jxdata;
    }

    public void setJxdata(byte[] jxdata) {
        this.jxdata = jxdata;
    }

    public SecretKey getJxsecretKey() {
        return jxsecretKey;
    }

    public void setJxsecretKey(SecretKey jxsecretKey) {
        this.jxsecretKey = jxsecretKey;
    }
}
