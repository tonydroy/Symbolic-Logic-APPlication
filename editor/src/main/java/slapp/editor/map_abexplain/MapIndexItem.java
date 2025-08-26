package slapp.editor.map_abexplain;

import java.io.Serializable;

public class MapIndexItem implements Serializable {
    private static final long serialVersionUID = 100L;

    public Integer startIndex;
    public Integer endIndex;
    public String linkID;

    public MapIndexItem(Integer startIndex, Integer endIndex, String linkID) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.linkID = linkID;
    }

    public Integer getStartIndex() {
        return startIndex;
    }

    public Integer getEndIndex() {
        return endIndex;
    }

    public String getLinkID() {
        return linkID;
    }

    @Override
    public String toString() {
        return "MapIndexItem [startIndex=" + startIndex + ", endIndex=" + endIndex + ", linkID=" + linkID + "]";
    }
}
