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
        theoremSets.add(new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc60 ax"));
        theoremSets.add(new TheoremSet("\ud835\udc34\ud835\udc37\ud835\udc60 th"));
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



    public static List<TheoremSet> getTheoremSets() {
        return theoremSets;
    }

    private static TheoremSet getADs_ax() {
        TheoremSet ADs_ax = new TheoremSet("ADs ax");
        return ADs_ax;
    }




}
