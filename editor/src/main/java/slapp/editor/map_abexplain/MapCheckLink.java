package slapp.editor.map_abexplain;

import slapp.editor.parser.Expression;

import java.util.List;

public class MapCheckLink {

    Integer metaStartIndex;
    Integer metaEndIndex;
    List<Expression> metaExpressions;

    Integer objectStartIndex;
    Integer objectEndIndex;
    List<Expression> objectExpressions;

    MapABExpClickableMapLink link;


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
        return ("mStart " + metaStartIndex + " mEnd " + metaEndIndex + " mexp " + msb.toString() + " oStart " + objectStartIndex + " oEnd " + objectEndIndex + " oexp " + osb.toString() );
    }

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


}
