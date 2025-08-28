package slapp.editor.map_abexplain;

import slapp.editor.parser.Expression;
import slapp.editor.parser.Language;
import slapp.editor.parser.Languages;
import slapp.editor.parser.ParseUtilities;

import java.util.List;

public class MapCheckLink {

    Integer metaStartIndex;
    Integer metaEndIndex;
    List<Expression> metaExpressions;
    int metaExpSize;

    Integer objectStartIndex;
    Integer objectEndIndex;
    List<Expression> objectExpressions;
    int objectExpSize;

    MapABExpClickableMapLink link;

/*
    @Override
    public String toString() {
        StringBuilder msb = new StringBuilder();
        for (Expression e : metaExpressions) {
            msb.append(e.toString());
            msb.append(" ");
        }
        StringBuilder osb = new StringBuilder();
        for (Expression e : objectExpressions) {
            osb.append(e.toString());
            osb.append(" ");
        }
     //   return ("mStart " + metaStartIndex + " mEnd " + metaEndIndex + " mexp " + msb.toString() + " oStart " + objectStartIndex + " oEnd " + objectEndIndex + " oexp " + osb.toString() );



        MapParser.setLanguage(Languages.getLanguage("LM Meta"));
        ParseUtilities.setLanguage(Languages.getLanguage("LM Meta"));
        List<Expression> metaSyms = MapParser.parseToSymbol(metaExpressions);


        Language objLang = Languages.getLanguage("\u2112\ud835\udcc6 (w/abv)");
        objLang.setXrelationSymbolsRequireSuper(true);
        MapParser.setLanguage(objLang);
        ParseUtilities.setLanguage(objLang);
        List<Expression> objectSyms;
        if (!isObjectGroup())  objectSyms = MapParser.parseToSymbol(objectExpressions);
        else objectSyms = ParseUtilities.parseExpressions(objectExpressions);

      //  return("test");
        return "meta -- size: " + metaSyms.size() + " element " + metaSyms.get(0) + " type " + metaSyms.get(0).getType() + "\n" + "obj -- size: " + objectSyms.size() + " element " + objectSyms.get(0) + " type " + objectSyms.get(0).getType() + "\n";
      //  return "obj -- size: " + objectSyms.size() + " element " + objectSyms.get(0) + " type " + objectSyms.get(0).getType() + "\n";
    }

 */

    public boolean isMetaGroup() {
        return metaEndIndex != null;
    }

    public boolean isObjectGroup() {
        return objectEndIndex != null;
    }


    public Integer getMetaStartIndex() {
        return metaStartIndex;
    }

    public void setMetaStartIndex(Integer metaStartIndex) {
        this.metaStartIndex = metaStartIndex;
    }

    public Integer getMetaEndIndex() {
        return metaEndIndex;
    }

    public void setMetaEndIndex(Integer metaEndIndex) {
        this.metaEndIndex = metaEndIndex;
    }

    public Integer getObjectStartIndex() {
        return objectStartIndex;
    }

    public void setObjectStartIndex(Integer objectStartIndex) {
        this.objectStartIndex = objectStartIndex;
    }

    public Integer getObjectEndIndex() {
        return objectEndIndex;
    }

    public void setObjectEndIndex(Integer objectEndIndex) {
        this.objectEndIndex = objectEndIndex;
    }

    public MapABExpClickableMapLink getLink() {
        return link;
    }

    public void setLink(MapABExpClickableMapLink link) {
        this.link = link;
    }

    public List<Expression> getObjectExpressions() {
        return objectExpressions;
    }

    public void setObjectExpressions(List<Expression> objectExpressions) {
        this.objectExpressions = objectExpressions;
    }

    public List<Expression> getMetaExpressions() {
        return metaExpressions;
    }

    public void setMetaExpressions(List<Expression> metaExpressions) {
        this.metaExpressions = metaExpressions;
    }

    public int getMetaExpSize() {
        return metaExpSize;
    }

    public void setMetaExpSize(int metaExpSize) {
        this.metaExpSize = metaExpSize;
    }

    public int getObjectExpSize() {
        return objectExpSize;
    }

    public void setObjectExpSize(int objectExpSize) {
        this.objectExpSize = objectExpSize;
    }
}
