package slapp.editor.derivation.theorems;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TheoremSets implements Serializable {
    private static List<TheoremSet> theoremSets = new ArrayList<TheoremSet>();

    static {
            loadTheoremSets();
    }

    private static void loadTheoremSets() {
        theoremSets.add(getADs_ax());
        theoremSets.add(getADs_th());
        theoremSets.add(new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc5e ax"));
        theoremSets.add(new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc5e th"));
        theoremSets.add(new TheoremSet("\ud835\udc34\ud835\udc37 ax"));
        theoremSets.add(new TheoremSet("\ud835\udc34\ud835\udc37 th"));
        theoremSets.add(new TheoremSet("A* ax"));
        theoremSets.add(new TheoremSet("A* th"));
        theoremSets.add(new TheoremSet("Q(\ud835\udc41\ud835\udc37) ax"));
        theoremSets.add(new TheoremSet("Q(\ud835\udc41\ud835\udc37) th"));
        theoremSets.add(new TheoremSet("PA(\ud835\udc34\ud835\udc37) ax"));
        theoremSets.add(new TheoremSet("PA(\ud835\udc34\ud835\udc37) th"));
        theoremSets.add(new TheoremSet("PA(\ud835\udc41\ud835\udc37) ax"));
        theoremSets.add(new TheoremSet("PA(\ud835\udc41\ud835\udc37) th"));
    }

    public static TheoremSet getTheoremSet(String name)  {
        TheoremSet theoremSet = null;
        for (TheoremSet set : theoremSets) {
            if (name.equals(set.getSetName()))
                theoremSet = set;
        }
        return theoremSet;
    }



    public static List<TheoremSet> getTheoremSets() {
        return theoremSets;
    }

    private static TheoremSet getADs_ax() {
        TheoremSet ADs_ax = new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc60 ax");
        List<ThrmSetElement> elements = ADs_ax.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A1", "(\uD835\uDCAB → (\uD835\uDCAC → \uD835\uDCAB))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A2", "((\uD835\uDCAA → (\uD835\uDCAB → \uD835\uDCAC)) → ((\uD835\uDCAA → \uD835\uDCAB) → (\uD835\uDCAA → \uD835\uDCAC)))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A3", "((∼\uD835\uDCAC → ∼\uD835\uDCAB) → ((∼\uD835\uDCAC → \uD835\uDCAB) → \uD835\uDCAC))"));
        return ADs_ax;
    }

    private static TheoremSet getADs_th() {
        TheoremSet ADs_th = new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc60 th");
        List<ThrmSetElement> elements = ADs_th.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.1", "(\uD835\uDC9C → \uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "T3.2", "(\uD835\uDC9C → ℬ)", "(ℬ → \uD835\uDC9E)", "(\uD835\uDC9C → \uD835\uDC9E)"));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "T3.3", "(\uD835\uDC9C → (ℬ → \uD835\uDC9E))", "(ℬ → (\uD835\uDC9C → \uD835\uDC9E))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.4", "((ℬ → \uD835\uDC9E) → ((\uD835\uDC9C → ℬ) → (\uD835\uDC9C → \uD835\uDC9E)))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.5", "((\uD835\uDC9C → ℬ) → ((ℬ → \uD835\uDC9E) → (\uD835\uDC9C → \uD835\uDC9E)))"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "T3.6", "ℬ","(\uD835\uDC9C → (ℬ → \uD835\uDC9E))", "(\uD835\uDC9C → \uD835\uDC9E)" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.7", "((∼\uD835\uDC9C → \uD835\uDC9C) → \uD835\uDC9C)" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.8", "((∼ℬ → ∼\uD835\uDC9C) → (\uD835\uDC9C → ℬ))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.9", "(∼\uD835\uDC9C → (\uD835\uDC9C → ℬ))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.10", "(∼∼\uD835\uDC9C → \uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.11", "(\uD835\uDC9C → ∼∼\uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.12", "((\uD835\uDC9C → ℬ) → (∼∼\uD835\uDC9C → ∼∼ℬ))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.13", "((\uD835\uDC9C → ℬ) → (∼ℬ → ∼\uD835\uDC9C))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.14", "((∼\uD835\uDC9C → ℬ) → (∼ℬ → \uD835\uDC9C))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.15", "((\uD835\uDC9C → ∼ℬ) → (ℬ → ∼\uD835\uDC9C))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.16", "((\uD835\uDC9C → ℬ) → ((∼\uD835\uDC9C → ℬ) → ℬ))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.17", "(\uD835\uDC9C → (∼ℬ → ∼(\uD835\uDC9C → ℬ)))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.18", "(\uD835\uDC9C → (\uD835\uDC9C ∨ ℬ))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.19", "(\uD835\uDC9C → (ℬ ∨ \uD835\uDC9C))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.20", "((\uD835\uDC9C ∧ ℬ) → ℬ)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.21", "((\uD835\uDC9C ∧ ℬ) → \uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "T3.22", "(\uD835\uDC9C → (ℬ → \uD835\uDC9E))", "((\uD835\uDC9C ∧ ℬ) → \uD835\uDC9E)"));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "T3.23", "((\uD835\uDC9C ∧ ℬ) → \uD835\uDC9E)", "(\uD835\uDC9C → (ℬ → \uD835\uDC9E))"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "T3.24", "\uD835\uDC9C", "(\uD835\uDC9C ↔ ℬ)", "ℬ"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "T3.25", "ℬ", "(\uD835\uDC9C ↔ ℬ)", "\uD835\uDC9C"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "T3.26", "∼\uD835\uDC9C", "(\uD835\uDC9C ↔ ℬ)", "∼ℬ"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "T3.27", "∼ℬ", "(\uD835\uDC9C ↔ ℬ)", "∼\uD835\uDC9C"));

        return ADs_th;
    }




}
