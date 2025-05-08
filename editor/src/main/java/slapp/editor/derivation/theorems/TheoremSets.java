package slapp.editor.derivation.theorems;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TheoremSets implements Serializable {
    private static final long serialVersionUID = 100L;
    private static List<TheoremSet> theoremSets = new ArrayList<TheoremSet>();

    static {
            loadTheoremSets();
    }

    private static void loadTheoremSets() {
        theoremSets.add(getAP_ax());
        theoremSets.add(getADs_ax());
        theoremSets.add(getADs_th());
        theoremSets.add(getADq_ax());
        theoremSets.add(getADq_th());
        theoremSets.add(getAD_ax());
        theoremSets.add(getAD_th());
        theoremSets.add(getAstar_ax());
        theoremSets.add(getAstar_th());
        theoremSets.add(getQND_ax());   //ND+
        theoremSets.add(getQND_th());  //ND+
        theoremSets.add(getADPA_ax());
        theoremSets.add(getADPA_th());
        theoremSets.add(getPAND_ax());
        theoremSets.add(new TheoremSet("PA(\ud835\udc41\ud835\udc37+) th"));
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


    public static TheoremSet getPAND_th() {
        TheoremSet PAND_th = new TheoremSet("PA(\ud835\udc41\ud835\udc37+) th");
        List<ThrmSetElement> elements = PAND_th.getElements();
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "IN", "\uD835\uDCAB‹\uD835\uDCCD, \uE886›", "∀\uD835\uDCCD(\uD835\uDCAB → \uD835\uDCAB‹\uD835\uDCCD, \uD835\uDC46\uD835\uDCCD›)", "∀\uD835\uDCCD\uD835\uDCAB" ));

        //theorems of Q and,
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.56", ""));





        return PAND_th;
    }

    public static TheoremSet getPAND_ax() {
        TheoremSet PAND_ax = new TheoremSet("PA(\ud835\udc41\ud835\udc37+) ax");
        List<ThrmSetElement> elements = PAND_ax.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.1", "∼\uE8AC\uD835\uDC46\uD835\uDC65\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.2", "(\uE8AC\uD835\uDC46\uD835\uDC65\uD835\uDC46\uD835\uDC66 → \uE8AC\uD835\uDC65\uD835\uDC66)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.3", "\uE8AC\uE8B8\uD835\uDC65\uE886\uD835\uDC65"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.4", "\uE8AC\uE8B8\uD835\uDC65\uD835\uDC46\uD835\uDC66\uD835\uDC46\uE8B8\uD835\uDC65\uD835\uDC66" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.5", "\uE8AC\uE8BA\uD835\uDC65\uE886\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.6", "\uE8AC\uE8BA\uD835\uDC65\uD835\uDC46\uD835\uDC66\uE8B8\uE8BA\uD835\uDC65\uD835\uDC66\uD835\uDC65"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.7", "((\uD835\uDCAB‹\uD835\uDCCD, \uE886› ∧ ∀\uD835\uDCCD(\uD835\uDCAB → \uD835\uDCAB‹\uD835\uDCCD, \uD835\uDC46\uD835\uDCCD›)) → ∀\uD835\uDCCD\uD835\uDCAB)"));
        return PAND_ax;
    }



    public static TheoremSet getQND_th() {
        TheoremSet QND_th = new TheoremSet("Q(\ud835\udc41\ud835\udc37+) th");
        List<ThrmSetElement> elements = QND_th.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.45", "∼\uE8AC\uD835\uDC46\uD835\uDCC9\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.46", "(\uE8AC\uD835\uDC46\uD835\uDCC9\uD835\uDC46\uD835\uDCC8 → \uE8AC\uD835\uDCC9\uD835\uDCC8)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.47", "\uE8AC\uE8B8\uD835\uDCC9\uE886\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.48", "\uE8AC\uE8B8\uD835\uDCC9\uD835\uDC46\uD835\uDCC8\uD835\uDC46\uE8B8\uD835\uDCC9\uD835\uDCC8"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.49", "\uE8AC\uE8BA\uD835\uDCC9\uE886\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.50", "\uE8AC\uE8BA\uD835\uDCC9\uD835\uDC46\uD835\uDCC8\uE8B8\uE8BA\uD835\uDCC9\uD835\uDCC8\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.QND_T51, "T6.51", ""));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.52", "\uE8AC\uE8B8\uD835\uDCC9\uD835\uDC46\uE886\uD835\uDC46\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.53", "(\uE8A6\uD835\uDCC9\uE886 → \uE8AC\uD835\uDCC9\uE886)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T6.54", "∼\uE8A4\uD835\uDCC9\uE886"));
        return QND_th;
    }

    public static TheoremSet getQND_ax() {
        TheoremSet QND_ax = new TheoremSet("Q(\ud835\udc41\ud835\udc37+) ax");
        List<ThrmSetElement> elements = QND_ax.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "Q.1", "∼\uE8AC\uD835\uDC46\uD835\uDC65\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "Q.2", "(\uE8AC\uD835\uDC46\uD835\uDC65\uD835\uDC46\uD835\uDC66 → \uE8AC\uD835\uDC65\uD835\uDC66)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "Q.3", "\uE8AC\uE8B8\uD835\uDC65\uE886\uD835\uDC65"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "Q.4", "\uE8AC\uE8B8\uD835\uDC65\uD835\uDC46\uD835\uDC66\uD835\uDC46\uE8B8\uD835\uDC65\uD835\uDC66" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "Q.5", "\uE8AC\uE8BA\uD835\uDC65\uE886\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "Q.6", "\uE8AC\uE8BA\uD835\uDC65\uD835\uDC46\uD835\uDC66\uE8B8\uE8BA\uD835\uDC65\uD835\uDC66\uD835\uDC65"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "Q.7", "(∼\uE8AC\uD835\uDC65\uE886 → ∃\uD835\uDC66\uE8AC\uD835\uDC65\uD835\uDC46\uD835\uDC66)"));
        return QND_ax;
    }

    public static TheoremSet getAP_ax() {
        TheoremSet AP_ax = new TheoremSet("\uD835\uDC34\uD835\uDC43 ax");
        List<ThrmSetElement> elements = AP_ax.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "∧.1", "((\uD835\uDCAB ∧ \uD835\uDCAC) → \uD835\uDCAB)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "∧.2", "((\uD835\uDCAB ∧ \uD835\uDCAC) → \uD835\uDCAC)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "∧.3", "(\uD835\uDCAB → (\uD835\uDCAC → (\uD835\uDCAB ∧ \uD835\uDCAC)))"));
        return AP_ax;
    }

    public static TheoremSet getADPA_th() {
        TheoremSet ADPA_th = new TheoremSet("PA(\ud835\udc34\ud835\udc37) th");
        List<ThrmSetElement> elements = ADPA_th.getElements();

        //theorems of AD and,
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "Ind", "\uD835\uDCAB‹\uD835\uDCCD, \uE886›", "∀\uD835\uDCCD(\uD835\uDCAB → \uD835\uDCAB‹\uD835\uDCCD, \uD835\uDC46\uD835\uDCCD›)", "∀\uD835\uDCCD\uD835\uDCAB" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.40", "∼\uE8AC\uD835\uDC46\uD835\uDCC9\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.41", "(\uE8AC\uD835\uDC46\uD835\uDCC9\uD835\uDC46\uD835\uDCC8 → \uE8AC\uD835\uDCC9\uD835\uDCC8)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.42", "\uE8AC\uE8B8\uD835\uDCC9\uE886\uD835\uDCC9",
                "\uE8AC\uD835\uDCC9\uE8B8\uD835\uDCC9\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.43", "\uE8AC\uE8B8\uD835\uDCC9\uD835\uDC46\uD835\uDCC8\uD835\uDC46\uE8B8\uD835\uDCC9\uD835\uDCC8",
                "\uE8AC\uD835\uDC46\uE8B8\uD835\uDCC9\uD835\uDCC8\uE8B8\uD835\uDCC9\uD835\uDC46\uD835\uDCC8"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.44", "\uE8AC\uE8BA\uD835\uDCC9\uE886\uE886",
                "\uE8AC\uE886\uE8BA\uD835\uDCC9\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.45", "\uE8AC\uE8BA\uD835\uDCC9\uD835\uDC46\uD835\uDCC8\uE8B8\uE8BA\uD835\uDCC9\uD835\uDCC8\uD835\uDCC9",
                "\uE8AC\uE8B8\uE8BA\uD835\uDCC9\uD835\uDCC8\uD835\uDCC9\uE8BA\uD835\uDCC9\uD835\uDC46\uD835\uDCC8"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.46", "\uE8AC\uE8B8\uE886\uD835\uDCC9\uD835\uDCC9",
                "\uE8AC\uD835\uDCC9\uE8B8\uE886\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.47", "\uE8AC\uE8B8\uD835\uDC46\uD835\uDCC9\uE886\uD835\uDC46\uE8B8\uD835\uDCC9\uE886",
                "\uE8AC\uD835\uDC46\uE8B8\uD835\uDCC9\uE886\uE8B8\uD835\uDC46\uD835\uDCC9\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.48", "\uE8AC\uE8B8\uD835\uDC46\uD835\uDCC9\uD835\uDCC8\uD835\uDC46\uE8B8\uD835\uDCC9\uD835\uDCC8",
                "\uE8AC\uD835\uDC46\uE8B8\uD835\uDCC9\uD835\uDCC8\uE8B8\uD835\uDC46\uD835\uDCC9\uD835\uDCC8"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.49", "\uE8AC\uE8B8\uD835\uDCC9\uD835\uDCC8\uE8B8\uD835\uDCC8\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.50", "\uE8AC\uE8B8\uE8B8\uD835\uDCC7\uD835\uDCC8\uE886\uE8B8\uD835\uDCC7\uE8B8\uD835\uDCC8\uE886",
                "\uE8AC\uE8B8\uD835\uDCC7\uE8B8\uD835\uDCC8\uE886\uE8B8\uE8B8\uD835\uDCC7\uD835\uDCC8\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.51", "\uE8AC\uE8B8\uE8B8\uD835\uDCC7\uD835\uDCC8\uD835\uDCC9\uE8B8\uD835\uDCC7\uE8B8\uD835\uDCC8\uD835\uDCC9",
                "\uE8AC\uE8B8\uD835\uDCC7\uE8B8\uD835\uDCC8\uD835\uDCC9\uE8B8\uE8B8\uD835\uDCC7\uD835\uDCC8\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.52", "\uE8AC\uE8BA\uE886\uD835\uDCC9\uE886",
                "\uE8AC\uE886\uE8BA\uE886\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.53", "\uE8AC\uE8BA\uD835\uDC46\uD835\uDCC9\uE886\uE8B8\uE8BA\uD835\uDCC9\uE886\uE886",
                "\uE8AC\uE8B8\uE8BA\uD835\uDCC9\uE886\uE886\uE8BA\uD835\uDC46\uD835\uDCC9\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.54", "\uE8AC\uE8B8\uE8BA\uD835\uDCC9\uD835\uDCCD\uE8B8\uD835\uDCCD\uD835\uDC46\uD835\uDCC9\uE8B8\uE8BA\uD835\uDCC9\uD835\uDC46\uD835\uDCCD\uD835\uDC46\uD835\uDCCD",
                "\uE8AC\uE8B8\uE8BA\uD835\uDCC9\uD835\uDC46\uD835\uDCCD\uD835\uDC46\uD835\uDCCD\uE8B8\uE8BA\uD835\uDCC9\uD835\uDCCD\uE8B8\uD835\uDCCD\uD835\uDC46\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT2, "T3.55", "\uE8AC\uE8BA\uD835\uDC46\uD835\uDCC9\uD835\uDCC8\uE8B8\uE8BA\uD835\uDCC9\uD835\uDCC8\uD835\uDCC8",
                "\uE8AC\uE8B8\uE8BA\uD835\uDCC9\uD835\uDCC8\uD835\uDCC8\uE8BA\uD835\uDC46\uD835\uDCC9\uD835\uDCC8"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.56", "\uE8AC\uE8BA\uD835\uDCC9\uD835\uDCC8\uE8BA\uD835\uDCC8\uD835\uDCC9"));
        return ADPA_th;
    }


    public static TheoremSet getADPA_ax() {
        TheoremSet ADPA_ax = new TheoremSet("PA(\ud835\udc34\ud835\udc37) ax");
        List<ThrmSetElement> elements = ADPA_ax.getElements();

        //axioms of PA and,
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.1", "∼\uE8AC\uD835\uDC46\uD835\uDC65\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.2", "(\uE8AC\uD835\uDC46\uD835\uDC65\uD835\uDC46\uD835\uDC66 → \uE8AC\uD835\uDC65\uD835\uDC66)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.3", "\uE8AC\uE8B8\uD835\uDC65\uE886\uD835\uDC65"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.4", "\uE8AC\uE8B8\uD835\uDC65\uD835\uDC46\uD835\uDC66\uD835\uDC46\uE8B8\uD835\uDC65\uD835\uDC66" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.5", "\uE8AC\uE8BA\uD835\uDC65\uE886\uE886"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.6", "\uE8AC\uE8BA\uD835\uDC65\uD835\uDC46\uD835\uDC66\uE8B8\uE8BA\uD835\uDC65\uD835\uDC66\uD835\uDC65"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "PA.7", "((\uD835\uDCAB‹\uD835\uDCCD, \uE886› ∧ ∀\uD835\uDCCD(\uD835\uDCAB → \uD835\uDCAB‹\uD835\uDCCD, \uD835\uDC46\uD835\uDCCD›)) → ∀\uD835\uDCCD\uD835\uDCAB)"));
        return ADPA_ax;
    }

    public static TheoremSet getAD_ax() {
        TheoremSet AD_ax = new TheoremSet("\ud835\udc34\ud835\udc37 ax");
        List<ThrmSetElement> elements = AD_ax.getElements();

        //axioms of ADq and,
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A.6", "(\uD835\uDCCD \uE8AC \uD835\uDCCD)"));
        elements.add(new ThrmSetElement(TheoremType.AD_A7,"A.7", "" ));
        elements.add(new ThrmSetElement(TheoremType.AD_A8,"A.8", ""));
        return AD_ax;
    }

    public static TheoremSet getAD_th() {
        TheoremSet AD_th = new TheoremSet("\ud835\udc34\ud835\udc37 th");
        List<ThrmSetElement> elements = AD_th.getElements();

        //theorems of ADq and,
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT,"T3.33", "\uE8AC\uD835\uDCC9\uD835\uDCC9" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.34", "(\uE8AC\uD835\uDCC9\uD835\uDCC8 → \uE8AC\uD835\uDCC8\uD835\uDCC9)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "T3.35", "(\uE8AC\uD835\uDCC7\uD835\uDCC8 → (\uE8AC\uD835\uDCC8\uD835\uDCC9 → \uE8AC\uD835\uDCC7\uD835\uDCC9))"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "T3.36", "\uE8AC\uD835\uDCC7\uD835\uDCC8", "\uE8AC\uD835\uDCC8\uD835\uDCC9", "\uE8AC\uD835\uDCC7\uD835\uDCC9"));
        elements.add(new ThrmSetElement(TheoremType.ADT3_37, "T3.37", ""));
        elements.add(new ThrmSetElement(TheoremType.ADT3_38, "T3.38", ""));
        return AD_th;
    }


    public static TheoremSet getADq_ax() {
        TheoremSet ADq_ax = new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc5e ax");
        List<ThrmSetElement> elements = ADq_ax.getElements();

        //axioms of ADs and,
        elements.add(new ThrmSetElement(TheoremType.ADQ_A4, "A.4", ""));
        elements.add(new ThrmSetElement(TheoremType.ADQ_A5, "A.5", ""));
        return ADq_ax;
    }

    public static TheoremSet getADq_th() {
        TheoremSet ADq_th = new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc5e th");
        List<ThrmSetElement> elements = ADq_th.getElements();

        //theorems of ADs and,
        elements.add(new ThrmSetElement(TheoremType.ADQT3_28, "T3.28", ""));
        elements.add(new ThrmSetElement(TheoremType.ADQT3_29, "T3.29", ""));
        elements.add(new ThrmSetElement(TheoremType.ADQT3_30, "T3.30", ""));
        elements.add(new ThrmSetElement(TheoremType.ADQT3_31, "T3.31", ""));
        elements.add(new ThrmSetElement(TheoremType.ADQT3_32, "T3.32", ""));
        return ADq_th;
    }

    public static TheoremSet getAstar_ax() {
        TheoremSet Astar_ax = new TheoremSet("A* ax");
        List<ThrmSetElement> elements = Astar_ax.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A.1", "(\uD835\uDCAB → (\uD835\uDCAB ∧ \uD835\uDCAB))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A.2", "((\uD835\uDCAB ∧ \uD835\uDCAC) → \uD835\uDCAB)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A.3", "((\uD835\uDCAA → \uD835\uDCAB) → [∼(\uD835\uDCAB ∧ \uD835\uDCAC) → ∼(\uD835\uDCAC ∧ \uD835\uDCAA)])"));
        return Astar_ax;
    }

    public static TheoremSet getAstar_th() {
        TheoremSet Astar_th = new TheoremSet("A* th");
        List<ThrmSetElement> elements = Astar_th.getElements();
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "(a)", "(\uD835\uDC9C → ℬ)", "(ℬ → \uD835\uDC9E)", "∼(∼\uD835\uDC9E ∧ \uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(b)", "∼(∼\uD835\uDC9C ∧ \uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(c)", "(∼∼\uD835\uDC9C → \uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(d)", "(∼(\uD835\uDC9C ∧ ℬ) → (ℬ → ∼\uD835\uDC9C))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(e)", "(\uD835\uDC9C → ∼∼\uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(f)", "((\uD835\uDC9C → ℬ) → (∼ℬ → ∼\uD835\uDC9C))"));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "(g)", "(∼\uD835\uDC9C → ∼ℬ)", "(ℬ → \uD835\uDC9C)"));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "(h)", "(\uD835\uDC9C → ℬ)", "((\uD835\uDC9E ∧ \uD835\uDC9C) → (ℬ ∧ \uD835\uDC9E))"));
        elements.add(new ThrmSetElement(TheoremType.TRIPLE_INPUT, "(i)", "(\uD835\uDC9C → ℬ)", "(ℬ → \uD835\uDC9E)", "(\uD835\uDC9E → \uD835\uDC9F)", "(\uD835\uDC9C → \uD835\uDC9F)" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(j)", "(\uD835\uDC9C → \uD835\uDC9C)" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(k)", "((\uD835\uDC9C ∧ ℬ) → (ℬ ∧ \uD835\uDC9C))"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "(l)", "(\uD835\uDC9C → ℬ)", "(ℬ → \uD835\uDC9E)", "(\uD835\uDC9C → \uD835\uDC9E)" ));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "(m)", "(∼ℬ → ℬ)", "ℬ" ));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "(n)", "(ℬ → ∼ℬ)", "∼ℬ"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(o)", "((\uD835\uDC9C ∧ ℬ) → ℬ)"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "(p)", "(\uD835\uDC9C → ℬ)", "(\uD835\uDC9E → \uD835\uDC9F)", "((\uD835\uDC9C ∧ \uD835\uDC9E) → (ℬ ∧ \uD835\uDC9F))"));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "(q)", "(ℬ → \uD835\uDC9E)", "((\uD835\uDC9C ∧ ℬ) → (\uD835\uDC9C ∧ \uD835\uDC9E))"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "(r)", "(\uD835\uDC9C → ℬ)", "(\uD835\uDC9C → \uD835\uDC9E)", "(\uD835\uDC9C → (ℬ ∧ \uD835\uDC9E))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(s)", "([(\uD835\uDC9C ∧ ℬ) ∧ \uD835\uDC9E] → [\uD835\uDC9C ∧ (ℬ ∧ \uD835\uDC9E)])"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(t)", "([\uD835\uDC9C ∧ (ℬ ∧ \uD835\uDC9E)] → [(\uD835\uDC9C ∧ ℬ) ∧ \uD835\uDC9E])"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(u)", "([\uD835\uDC9C → (ℬ → \uD835\uDC9E)] → [(\uD835\uDC9C ∧ ℬ) → \uD835\uDC9E])"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(v)", "([(\uD835\uDC9C ∧ ℬ) → \uD835\uDC9E] → [\uD835\uDC9C → (ℬ → \uD835\uDC9E)])"));
        elements.add(new ThrmSetElement(TheoremType.SINGLE_INPUT, "(w)", "(\uD835\uDC9C → (ℬ → \uD835\uDC9E))", "(ℬ → (\uD835\uDC9C → \uD835\uDC9E))"));
        elements.add(new ThrmSetElement(TheoremType.DOUBLE_INPUT, "(x)", "(\uD835\uDC9C → ℬ)", "(\uD835\uDC9C → (ℬ → \uD835\uDC9E))", "(\uD835\uDC9C → \uD835\uDC9E)" ));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(y)", "(\uD835\uDC9C → [ℬ → (\uD835\uDC9C ∧ ℬ)])"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "(z)", "(\uD835\uDC9C → (ℬ → \uD835\uDC9C))"));
        return Astar_th;
    }


    private static TheoremSet getADs_ax() {
        TheoremSet ADs_ax = new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc60 ax");
        List<ThrmSetElement> elements = ADs_ax.getElements();
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A.1", "(\uD835\uDCAB → (\uD835\uDCAC → \uD835\uDCAB))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A.2", "((\uD835\uDCAA → (\uD835\uDCAB → \uD835\uDCAC)) → ((\uD835\uDCAA → \uD835\uDCAB) → (\uD835\uDCAA → \uD835\uDCAC)))"));
        elements.add(new ThrmSetElement(TheoremType.ZERO_INPUT, "A.3", "((∼\uD835\uDCAC → ∼\uD835\uDCAB) → ((∼\uD835\uDCAC → \uD835\uDCAB) → \uD835\uDCAC))"));
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
