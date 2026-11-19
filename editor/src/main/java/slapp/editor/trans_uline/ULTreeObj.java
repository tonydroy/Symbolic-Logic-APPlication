package slapp.editor.trans_uline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class ULTreeObj implements Serializable {
    private static final long serialVersionUID = 100L;

    private String text = "";
    private String mainOperator = "";
    private boolean tfOperator = false;
    private double lineStartX;
    private double lineEndX;
    private int indexMin;
    private int indexMax;
    private int level;
    private List<ULTreeObj> children = new ArrayList<>();
    private boolean root = false;

    public ULTreeObj(double lineStartX, double lineEndX, int indexMin, int indexMax, int level) {
        this.lineStartX = lineStartX;
        this.lineEndX = lineEndX;
        this.indexMin = indexMin;
        this.indexMax = indexMax;
        this.level = level;
    }



    public List<ULTreeObj> getChildren() {
        return children;
    }

    public int getIndexMax() {
        return indexMax;
    }

    public int getIndexMin() {
        return indexMin;
    }

    public void setIndexMax(int indexMax) {
        this.indexMax = indexMax;
    }

    public int getLevel() {
        return level;
    }

    public double getLineEndX() {
        return lineEndX;
    }

    public double getLineStartX() {
        return lineStartX;
    }

    public String getMainOperator() {
        return mainOperator;
    }

    public void setMainOperator(String mainOperator) {
        this.mainOperator = mainOperator;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isTfOperator() {
        return tfOperator;
    }

    public void setTfOperator(boolean tfOperator) {
        this.tfOperator = tfOperator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("txt: ").append(text);
        sb.append(" mo: ").append(mainOperator);
        sb.append(" startX: ").append(lineStartX);
        sb.append(" endX: ").append(lineEndX);
        sb.append(" min idx: ").append(indexMin);
        sb.append(" max idx: ").append(indexMax);
        sb.append(" level: ").append(level);
        sb.append(" root: ").append(root).append("\n");
        if (children != null) {
            for (int i = 0; i < children.size(); i++) {
                ULTreeObj child = children.get(i);
                sb.append("child ").append(i).append(" ").append(child.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}