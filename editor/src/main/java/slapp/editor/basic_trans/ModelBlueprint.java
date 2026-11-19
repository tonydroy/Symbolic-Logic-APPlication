package slapp.editor.basic_trans;

import slapp.editor.parser.symbols.LanguageElement;

import java.util.List;
import java.util.Map;

public class ModelBlueprint {
    public final Map<LanguageElement, SymbolInfo> infoMap;
    public final LanguageElement[] bitToSymbolArray;
    public final List<LanguageElement> fullSymList;
    public final int fullModelSize;

    public ModelBlueprint(List<LanguageElement> fullSymList, int univSize) {
        this.fullSymList = fullSymList;
        int size = 0;
        for (LanguageElement sym : fullSymList) {
            size += (int) Math.pow(univSize, sym.getPlaces());
        }
        this.fullModelSize = size;
        this.bitToSymbolArray = new LanguageElement[size];
        this.infoMap = EquivalenceChecker.getSymbolInfoMap(fullSymList, univSize, bitToSymbolArray);
    }

}

