package slapp.editor.parser;

import slapp.editor.EditorAlerts;

import java.util.List;

public class Alphabets {

    private static List normalL;
    private static List normalU;
    private static List normalN;
    private static List italL;
    private static List italU;
    private static List sansL;
    private static List sansU;
    private static List sansN;
    private static List scriptL;
    private static List scriptU;
    private static List bbL;
    private static List bbU;
    private static List bbN;
    private static List frakL;
    private static List frakU;
    private static List greekL;
    private static List greekU;
    private static List[] alphabets;

    static {
        initializeLists();
    }

    private static void initializeLists() {
        normalL = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");  //a - z regular
        normalU = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"); //A - Z regular
        normalN = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"); // 0 - 9 regular
        italL = List.of("\ud835\udc4e", "\ud835\udc4f", "\ud835\udc50", "\ud835\udc51", "\ud835\udc52", "\ud835\udc53", "\ud835\udc54", "\u210e", "\ud835\udc56", "\ud835\udc57", "\ud835\udc58", "\ud835\udc59", "\ud835\udc5a",
                "\ud835\udc5b", "\ud835\udc5c", "\ud835\udc5d", "\ud835\udc5e", "\ud835\udc5f", "\ud835\udc60", "\ud835\udc61", "\ud835\udc62", "\ud835\udc63", "\ud835\udc64", "\ud835\udc65", "\ud835\udc66", "\ud835\udc67"); //a - z italic
        italU = List.of("\ud835\udc34", "\ud835\udc35", "\ud835\udc36", "\ud835\udc37", "\ud835\udc38", "\ud835\udc39", "\ud835\udc3a", "\ud835\udc3b", "\ud835\udc3c", "\ud835\udc3d", "\ud835\udc3e", "\ud835\udc3f", "\ud835\udc40",
                "\ud835\udc41", "\ud835\udc42", "\ud835\udc43", "\ud835\udc44", "\ud835\udc45", "\ud835\udc46", "\ud835\udc47", "\ud835\udc48", "\ud835\udc49", "\ud835\udc4a", "\ud835\udc4b", "\ud835\udc4c", "\ud835\udc4d"); //A - Z italic
        sansL = List.of("\ud835\uddba", "\ud835\uddbb", "\ud835\uddbc", "\ud835\uddbd", "\ud835\uddbe", "\ud835\uddbf", "\ud835\uddc0", "\ud835\uddc1", "\ud835\uddc2", "\ud835\uddc3", "\ud835\uddc4", "\ud835\uddc5", "\ud835\uddc6",
                "\ud835\uddc7", "\ud835\uddc8", "\ud835\uddc9", "\ud835\uddca", "\ud835\uddcb", "\ud835\uddcc", "\ud835\uddcd", "\ud835\uddce", "\ud835\uddcf", " \ud835\uddd0", "\ud835\uddd1", "\ud835\uddd2", "\ud835\uddd3"); //a - z sans
        sansU = List.of("\ud835\udda0", "\ud835\udda1", "\ud835\udda2", "\ud835\udda3", "\ud835\udda4", "\ud835\udda5", "\ud835\udda6", "\ud835\udda7", "\ud835\udda8", "\ud835\udda9", "\ud835\uddaa", "\ud835\uddab", "\ud835\uddac",
                "\ud835\uddad", "\ud835\uddae", "\ud835\uddaf", "\ud835\uddb0", "\ud835\uddb1", "\ud835\uddb2", "\ud835\uddb3", "\ud835\uddb4", "\ud835\uddb5", "\ud835\uddb6", "\ud835\uddb7", "\ud835\uddb8", "\ud835\uddb9"); //A - Z sans
        sansN = List.of("\ud835\udfe2", "\ud835\udfe3", "\ud835\udfe4", "\ud835\udfe5", "\ud835\udfe6", " \ud835\udfe7", "\ud835\udfe8", "\ud835\udfe9", "\ud835\udfea", "\ud835\udfeb"); //0 - 9 sans
        scriptL = List.of("\ue886", "\ud835\udcb6", "\ud835\udcb7", "\ud835\udcb8", "\ud835\udcb9", "\u212f", "\ud835\udcbb", "\u210a", "\ud835\udcbd", "\ud835\udcbe", "\ud835\udcbf", "\ud835\udcc0", "\ud835\udcc1", "\ud835\udcc2",
                "\ud835\udcc3", "\u2134", "\ud835\udcc5", "\ud835\udcc6", "\ud835\udcc7", "\ud835\udcc8", "\ud835\udcc9", "\ud835\udcca", "\ud835\udccb", "\ud835\udccc", "\ud835\udccd", "\ud835\udcce", "\ud835\udccf"); //0 slash and a - z script
        scriptU = List.of("\ud835\udc9c", "\u212c", "\ud835\udc9e", "\ud835\udc9f", "\u2130", "\u2131", "\ud835\udca2", "\u210b", "\u2110", "\ud835\udca5", "\ud835\udca6", "\u2112", "\u2133",
                "\ud835\udca9", " \ud835\udcaa", "\ud835\udcab", "\ud835\udcac", "\u211b", "\ud835\udcae", "\ud835\udcaf", "\ud835\udcb0", "\ud835\udcb1", "\ud835\udcb2", "\ud835\udcb3", "\ud835\udcb4", "\ud835\udcb5");  //A - Z script
        bbL = List.of("\uf893", "\uf894", "\uf895", "\uf896", "\uf897", "\uf898", "\uf899", "\uf89a", "\uf89b", "\uf89c", "\uf89d", "\uf89e", "\uf89f",
                "\uf8a0", "\uf8a1", "\uf8a2", "\uf8a3", "\uf8a4", "\uf8a5", "\uf8a6", "\uf8a7", "\uf8a8", "\uf8a9", "\uf8aa", "\uf8ab", "\uf8ac");  //a - z bb
        bbU = List.of("\uf880", "\uf881", "\uf8ad", "\uf882", "\uf883", "\uf884", "\uf885", "\uf8ae", "\uf886", "\uf887", "\uf888", "\uf889", "\uf88a",
                "\uf8af", "\uf88b", "\uf8b0", "\uf8b1", "\uf8b2", "\uf88c", "\uf88d", "\uf88e", "\uf88f", "\uf890", "\uf891", "\uf892", "\uf8b3");  //A - Z bb
        bbN = List.of("\ud835\udfd8", "\ud835\udfd9", "\ud835\udfda", "\ud835\udfdb", "\ud835\udfdc", "\ud835\udfdd", "\ud835\udfde", "\ud835\udfdf", "\ud835\udfe0", "\ud835\udfe1"); //0 - 9 bb
        frakL = List.of("\ud835\udd1e", "\ud835\udd1f", "\ud835\udd20", "\ud835\udd21", "\ud835\udd22", "\ud835\udd23", "\ud835\udd24", "\ud835\udd25", "\ud835\udd26", "\ud835\udd27", "\ud835\udd28", "\ud835\udd29", "\ud835\udd2a",
                "\ud835\udd2b", "\ud835\udd2c", "\ud835\udd2d", "\ud835\udd2e", "\ud835\udd2f", "\ud835\udd30", "\ud835\udd31", "\ud835\udd32", "\ud835\udd33", "\ud835\udd34", "\ud835\udd35", "\ud835\udd36", "\ud835\udd37"); //a - z frak
        frakU = List.of("\ud835\udd04", "\ud835\udd05", "\u212d", "\ud835\udd07", "\ud835\udd08", "\ud835\udd09", "\ud835\udd0a", "\u210c", "\u2111", "\ud835\udd0d", "\ud835\udd0e", "\ud835\udd0f", "\ud835\udd10",
                "\ud835\udd11", "\ud835\udd12", "\ud835\udd13", "\ud835\udd14", "\u211c", "\ud835\udd16", "\ud835\udd17", "\ud835\udd18", "\ud835\udd19", "\ud835\udd1a", "\ud835\udd1b", "\ud835\udd1c", "\u2128"); //A - Z frak
        greekL = List.of("\u03b1", "\u03b2", "\u03b3", "\u03b4", "\u03b5", "\u03b6", "\u03b7", "\u03b8", "\u03b9", "\u03ba", "\u03bb", "\u03bc", "\u03bd", "\u03be", "\u03bf",
                "\u03c0", "\u03c1", "\u03c3", "\u03c4", "\u03c5", "\u03c6", "\u03c7", "\u03c8", "\u03c9"); //alpha - omega greek
        greekU = List.of("\u0391", "\u0392", "\u0393", "\u0394", "\u0395", "\u0396", "\u0397", "\u0398", "\u0399", "\u039a", "\u039b", "\u039c", "\u039d", "\u039e", "\u039f",
                "\u03a0", "\u03a1", "\u03a3", "\u03a4", "\u03a5", "\u03a6", "\u03a7", "\u03a8", "\u03a9");  //Alpha - Omega greek

        alphabets = new List[] {normalL, normalU, normalN, italL, italU, sansL, sansU, sansN,scriptL,scriptU,bbL,bbU,bbN,frakL,frakU,greekL,greekU};
    }

    public static List<String> getCharacterRange(String start, String end) {

        String message = "Please select from a single uppercase, lowercase, or numeral alphabet.";
        for (int i = 0; i < alphabets.length; i++) {
            if (alphabets[i].contains(start) && alphabets[i].contains(end)) {
                int startIndex = alphabets[i].indexOf(start);
                int endIndex = alphabets[i].indexOf(end) + 1;
                if (endIndex > startIndex) {
                    return alphabets[i].subList(startIndex, endIndex);
                }
                else {
                    message = "Please select symbols in their standard order as a - z, 0 - 9.";
                    break;
                }
            }
        }
        EditorAlerts.showSimpleAlert("Error", start + "..." + end + " is not a valid character range.\n" + message);
        return null;
    }



}
