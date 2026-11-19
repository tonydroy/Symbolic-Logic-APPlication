package slapp.editor.basic_trans;

import slapp.editor.parser.symbols.LanguageElement;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

public class KeyObject implements Serializable {
    private static final long serialVersionUID = 100L;

    private BitSet keyModelValues;
    private List<LanguageElement> symList;
    private int modelSize;



    public KeyObject(BitSet keyModelValues, List<LanguageElement> symList, int modelSize) {
        this.keyModelValues = keyModelValues;
        this.symList = symList;
        this.modelSize = modelSize;
    }

    public BitSet getKeyModelValues() {
        return keyModelValues;
    }

    public List<LanguageElement> getSymList() {
        return symList;
    }

    public int getModelSize() {  return modelSize; }

}
