package com.gluonhq.richtextarea;

import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import static java.util.Map.entry;

public class CharModMaps {

    private final Map<String, String> overlineMap = initializeOverlineMap();
    private final Map<String, String> macMap = initializeMacMap();
    private final Map<String, String> vecMap = initializeVecMap();
    private final Map<String, String> hatMap = initializeHatMap();
    private final Map<String, String> slashBaseItalMap = initializeSlashBaseItalMap();
    private final Map<String, String> slashBaseScriptMap = initializeSlashBaseScriptMap();
    private final Map<String, String> slashBaseSansMap = initializeSlashBaseSansMap();
    private final Map<String, String> slashItalSansMap = initializeSlashItalSansMap();
    private final Map<String, String> slashItalBBMap = initializeSlashItalBBMap();
    private final Map<String, String> slashScriptItalMap = initializeSlashScriptItalMap();
    private final Map<String, String> slashScriptSansMap = initializeSlashScriptSansMap();
    private final Map<String, String> slashGreekFrakMap = initializeSlashGreekFrakMap();




    private Map<String, String> initializeOverlineMap() {
        return Map.ofEntries(

                //base map
                entry(" ", "\ue06f"),
                entry("a", "\ue050"),
                entry("b", "\ue051"),
                entry("c", "\ue052"),
                entry("d", "\ue053"),
                entry("e", "\ue054"),
                entry("f", "\ue055"),
                entry("g", "\ue056"),
                entry("h", "\ue057"),
                entry("i", "\ue058"),
                entry("j", "\ue059"),
                entry("k", "\ue05a"),
                entry("l", "\ue05b"),
                entry("m", "\ue05c"),
                entry("n", "\ue05d"),
                entry("o", "\ue05e"),
                entry("p", "\ue05f"),
                entry("q", "\ue060"),
                entry("r", "\ue061"),
                entry("s", "\ue062"),
                entry("t", "\ue063"),
                entry("u", "\ue064"),
                entry("v", "\ue065"),
                entry("w", "\ue066"),
                entry("x", "\ue067"),
                entry("y", "\ue068"),
                entry("z", "\ue069"),
                entry("A", "\ue030"),
                entry("B", "\ue031"),
                entry("C", "\ue032"),
                entry("D", "\ue033"),
                entry("E", "\ue034"),
                entry("F", "\ue035"),
                entry("G", "\ue036"),
                entry("H", "\ue037"),
                entry("I", "\ue038"),
                entry("J", "\ue039"),
                entry("K", "\ue03a"),
                entry("L", "\ue03b"),
                entry("M", "\ue03c"),
                entry("N", "\ue03d"),
                entry("O", "\ue03e"),
                entry("P", "\ue03f"),
                entry("Q", "\ue040"),
                entry("R", "\ue041"),
                entry("S", "\ue042"),
                entry("T", "\ue043"),
                entry("U", "\ue044"),
                entry("V", "\ue045"),
                entry("W", "\ue046"),
                entry("X", "\ue047"),
                entry("Y", "\ue048"),
                entry("Z", "\ue049"),
                entry("`", "\ue04f"),
                entry("1", "\ue020"),
                entry("2", "\ue021"),
                entry("3", "\ue022"),
                entry("4", "\ue023"),
                entry("5", "\ue024"),
                entry("6", "\ue025"),
                entry("7", "\ue026"),
                entry("8", "\ue027"),
                entry("9", "\ue028"),
                entry("0", "\ue01f"),

                entry("-", "\ue01c"),
                entry("=", "\ue02c"),
                entry("[", "\ue04a"),
                entry("]", "\ue04c"),
                entry("\\", "\ue04b"),
                entry(";", "\ue02a"),
                entry("'", "\ue016"),
                entry(",", "\ue01b"),
                entry(".", "\ue01d"),
                entry("/", "\ue01e"),
                entry("\u223c", "\ue22b"),             //til
                entry("!", "\ue010"),
                entry("@", "\ue02f"),
                entry("#", "\ue012"),
                entry("$", "\ue013"),
                entry("%", "\ue014"),
                entry("^", "\ue04d"),
                entry("&", "\ue015"),
                entry("*", "\ue019"),
                entry("(", "\ue017"),
                entry(")", "\ue018"),
                entry("_", "\ue04e"),
                entry("+", "\ue01a"),
                entry("{", "\ue06a"),
                entry("}", "\ue06c"),
                entry("|", "\ue06b"),
                entry(":", "\ue029"),
                entry("\"", "\ue011"),
                entry("<", "\ue02b"),
                entry(">", "\ue02d"),
                entry("?", "\ue02e"),

                //italic (alt map)
                entry("\ud835\udc4e", "\ue08a"),      //a
                entry("\ud835\udc4f", "\ue08b"),      //b
                entry("\ud835\udc50", "\ue08c"),      //c
                entry("\ud835\udc51", "\ue08d"),      //d
                entry("\ud835\udc52", "\ue08e"),      //e
                entry("\ud835\udc53", "\ue08f"),      //f
                entry("\ud835\udc54", "\ue090"),      //g
                entry("\u210e", "\ue0a3"),            //h
                entry("\ud835\udc56", "\ue091"),      //i
                entry("\ud835\udc57", "\ue092"),      //j
                entry("\ud835\udc58", "\ue093"),      //k
                entry("\ud835\udc59", "\ue094"),      //l
                entry("\ud835\udc5a", "\ue095"),      //m
                entry("\ud835\udc5b", "\ue096"),      //n
                entry("\ud835\udc5c", "\ue097"),      //o
                entry("\ud835\udc5d", "\ue098"),      //p
                entry("\ud835\udc5e", "\ue099"),      //q
                entry("\ud835\udc5f", "\ue09a"),      //r
                entry("\ud835\udc60", "\ue09b"),      //s
                entry("\ud835\udc61", "\ue09c"),      //t
                entry("\ud835\udc62", "\ue09d"),      //u
                entry("\ud835\udc63", "\ue09e"),      //v
                entry("\ud835\udc64", "\ue09f"),      //w
                entry("\ud835\udc65", "\ue0a0"),      //x
                entry("\ud835\udc66", "\ue0a1"),      //y
                entry("\ud835\udc67", "\ue0a2"),      //z

                entry("\ud835\udc34", "\ue070"),      //A
                entry("\ud835\udc35", "\ue071"),      //B
                entry("\ud835\udc36", "\ue072"),      //C
                entry("\ud835\udc37", "\ue073"),      //D
                entry("\ud835\udc38", "\ue074"),      //E
                entry("\ud835\udc39", "\ue075"),      //F
                entry("\ud835\udc3a", "\ue076"),      //G
                entry("\ud835\udc3b", "\ue077"),      //H
                entry("\ud835\udc3c", "\ue078"),      //I
                entry("\ud835\udc3d", "\ue079"),      //J
                entry("\ud835\udc3e", "\ue07a"),      //K
                entry("\ud835\udc3f", "\ue07b"),      //L
                entry("\ud835\udc40", "\ue07c"),      //M
                entry("\ud835\udc41", "\ue07d"),      //N
                entry("\ud835\udc42", "\ue07e"),      //O
                entry("\ud835\udc43", "\ue07f"),      //P
                entry("\ud835\udc44", "\ue080"),      //Q
                entry("\ud835\udc45", "\ue081"),      //R
                entry("\ud835\udc46", "\ue082"),      //S
                entry("\ud835\udc47", "\ue083"),      //T
                entry("\ud835\udc48", "\ue084"),      //U
                entry("\ud835\udc49", "\ue085"),      //V
                entry("\ud835\udc4a", "\ue086"),      //W
                entry("\ud835\udc4b", "\ue087"),      //X
                entry("\ud835\udc4c", "\ue088"),      //Y
                entry("\ud835\udc4d", "\ue089"),      //Z

                //script map
                entry("\ud835\udcb6", "\ue0d2"),       //a
                entry("\ud835\udcb7", "\ue0d3"),       //b
                entry("\ud835\udcb8", "\ue08c"),       //c
                entry("\ud835\udcb9", "\ue0d5"),       //d
                entry("\u212f", "\ue0e9"),             //e
                entry("\ud835\udcbb", "\ue0d6"),       //f
                entry("\u210a", "\ue0ea"),             //g
                entry("\ud835\udcbd", "\ue0d7"),       //h
                entry("\ud835\udcbe", "\ue0d8"),       //i
                entry("\ud835\udcbf", "\ue0d9"),       //j
                entry("\ud835\udcc0", "\ue0da"),       //k
                entry("\ud835\udcc1", "\ue0db"),       //l
                entry("\ud835\udcc2", "\ue0dc"),       //m
                entry("\ud835\udcc3", "\ue0dd"),       //n
                entry("\u2134", "\ue0eb"),             //o
                entry("\ud835\udcc5", "\ue0de"),       //p
                entry("\ud835\udcc6", "\ue0df"),       //q
                entry("\ud835\udcc7", "\ue0e0"),       //r
                entry("\ud835\udcc8", "\ue0e1"),       //s
                entry("\ud835\udcc9", "\ue0e2"),       //t
                entry("\ud835\udcca", "\ue0e3"),       //u
                entry("\ud835\udccb", "\ue0e4"),       //v
                entry("\ud835\udccc", "\ue0e5"),       //w
                entry("\ud835\udccd", "\ue0e6"),       //x
                entry("\ud835\udcce", "\ue0e7"),       //y
                entry("\ud835\udccf", "\ue0e8"),       //z
                entry("\ud835\udc9c", "\ue0c0"),       //A
                entry("\u212c", "\ue0ec"),             //B
                entry("\ud835\udc9e", "\ue0c1"),       //C
                entry("\ud835\udc9f", "\ue0c2"),       //D
                entry("\u2130", "\ue0ed"),             //E
                entry("\u2131", "\ue0ee"),             //F
                entry("\ud835\udca2", "\ue0c3"),       //G
                entry("\u210b", "\ue0ef"),             //H
                entry("\u2110", "\ue0f0"),             //I
                entry("\ud835\udca5", "\ue0c4"),       //J
                entry("\ud835\udca6", "\ue0c5"),       //K
                entry("\u2112", "\ue0f1"),             //L
                entry("\u2133", "\ue0f2"),             //M
                entry("\ud835\udca9", "\ue0c6"),       //N
                entry("\ud835\udcaa", "\ue0c7"),       //O
                entry("\ud835\udcab", "\ue0c8"),       //P
                entry("\ud835\udcac", "\ue0c9"),       //Q
                entry("\u211b", "\ue0f3"),             //R
                entry("\ud835\udcae", "\ue0ca"),       //S
                entry("\ud835\udcaf", "\ue0cb"),       //T
                entry("\ud835\udcb0", "\ue0cc"),       //U
                entry("\ud835\udcb1", "\ue0cd"),       //V
                entry("\ud835\udcb2", "\ue0ce"),       //W
                entry("\ud835\udcb3", "\ue0cf"),       //X
                entry("\ud835\udcb4", "\ue0d0"),       //Y
                entry("\ud835\udcb5", "\ue0d1"),      //Z

                //Greek
                entry("\u03b1", "\ue118"),      //alpha
                entry("\u03b2", "\ue119"),      //beta
                entry("\u03b3", "\ue11a"),      //gamma
                entry("\u03b4", "\ue11b"),      //delta
                entry("\u03b5", "\ue11c"),      //epsilon
                entry("\u03b6", "\ue11d"),      //zeta
                entry("\u03b7", "\ue11e"),      //eta
                entry("\u03b8", "\ue11f"),      //theta
                entry("\u03b9", "\ue120"),      //iota
                entry("\u03ba", "\ue121"),      //kappa
                entry("\u03bb", "\ue122"),      //lambda
                entry("\u03bc", "\ue123"),      //mu
                entry("\u03bd", "\ue124"),      //nu
                entry("\u03be", "\ue125"),      //xi
                entry("\u03bf", "\ue126"),      //omicron
                entry("\u03c0", "\ue127"),      //pi
                entry("\u03c1", "\ue128"),      //rho
                entry("\u03c2", "\ue129"),      //end sigma
                entry("\u03c3", "\ue12a"),      //sigma
                entry("\u03c4", "\ue12b"),      //tau
                entry("\u03c5", "\ue12c"),      //upsilon
                entry("\u03c6", "\ue12d"),      //phi
                entry("\u03c7", "\ue12e"),      //chi
                entry("\u03c8", "\ue12f"),      //psi
                entry("\u03c9", "\ue130"),      //omega
                entry("\uf8b4", "\ue131"),      //slanted beta
                entry("\uf8b5", "\ue132"),      //slanted mu
                entry("\u0391", "\ue100"),      //Alpha
                entry("\u0392", "\ue101"),      //Beta
                entry("\u0393", "\ue102"),      //Gamma
                entry("\u0394", "\ue103"),      //Delta
                entry("\u0395", "\ue104"),      //Epsilon
                entry("\u0396", "\ue105"),      //Zeta
                entry("\u0397", "\ue106"),      //Eta
                entry("\u0398", "\ue107"),      //Theta
                entry("\u0399", "\ue108"),      //Iota
                entry("\u039a", "\ue109"),      //Kappa
                entry("\u039b", "\ue10a"),      //Lambda
                entry("\u039c", "\ue14c"),      //Mu
                entry("\u039d", "\ue10c"),      //Nu
                entry("\u039e", "\ue10d"),      //Xi
                entry("\u039f", "\ue10e"),      //Omicron
                entry("\u03a0", "\ue10f"),      //Pi
                entry("\u03a1", "\ue110"),      //Rho
                entry("\u03a3", "\ue111"),      //Sigma
                entry("\u03a4", "\ue112"),      //Tau
                entry("\u03a5", "\ue113"),      //Upsilon
                entry("\u03a6", "\ue114"),      //Phi
                entry("\u03a7", "\ue115"),      //Chi
                entry("\u03a8", "\ue116"),      //Psi
                entry("\u03a9", "\ue117"),      //Omega
                entry("\uf8d5", "\ue210"),  //double mu

                //Sans
                entry("\ud835\uddba", "\ue15a"),      //a
                entry("\ud835\uddbb", "\ue15b"),      //b
                entry("\ud835\uddbc", "\ue15c"),      //c
                entry("\ud835\uddbd", "\ue15d"),      //d
                entry("\ud835\uddbe", "\ue15e"),      //e
                entry("\ud835\uddbf", "\ue15f"),      //f
                entry("\ud835\uddc0", "\ue160"),      //g
                entry("\ud835\uddc1", "\ue161"),      //h
                entry("\ud835\uddc2", "\ue162"),      //i
                entry("\ud835\uddc3", "\ue163"),      //j
                entry("\ud835\uddc4", "\ue164"),      //k
                entry("\ud835\uddc5", "\ue165"),      //l
                entry("\ud835\uddc6", "\ue166"),      //m
                entry("\ud835\uddc7", "\ue167"),      //n
                entry("\ud835\uddc8", "\ue168"),      //o
                entry("\ud835\uddc9", "\ue169"),      //p
                entry("\ud835\uddca", "\ue16a"),      //q
                entry("\ud835\uddcb", "\ue16b"),      //r
                entry("\ud835\uddcc", "\ue16c"),      //s
                entry("\ud835\uddcd", "\ue16d"),      //t
                entry("\ud835\uddce", "\ue16e"),      //u
                entry("\ud835\uddcf", "\ue16f"),      //v
                entry("\ud835\uddd0", "\ue170"),      //w
                entry("\ud835\uddd1", "\ue171"),      //x
                entry("\ud835\uddd2", "\ue172"),      //y
                entry("\ud835\uddd3", "\ue173"),      //z
                entry("\ud835\udfe2", "\ue174"),      //0
                entry("\ud835\udfe3", "\ue175"),      //1
                entry("\ud835\udfe4", "\ue176"),      //2
                entry("\ud835\udfe5", "\ue177"),      //3
                entry("\ud835\udfe6", "\ue178"),      //4
                entry("\ud835\udfe7", "\ue179"),      //5
                entry("\ud835\udfe8", "\ue17a"),      //6
                entry("\ud835\udfe9", "\ue17b"),      //7
                entry("\ud835\udfea", "\ue17c"),      //8
                entry("\ud835\udfeb", "\ue17d"),      //9
                entry("\ud835\udda0", "\ue140"),      //A
                entry("\ud835\udda1", "\ue141"),      //B
                entry("\ud835\udda2", "\ue142"),      //C
                entry("\ud835\udda3", "\ue143"),      //D
                entry("\ud835\udda4", "\ue144"),      //E
                entry("\ud835\udda5", "\ue145"),      //F
                entry("\ud835\udda6", "\ue146"),      //G
                entry("\ud835\udda7", "\ue147"),      //H
                entry("\ud835\udda8", "\ue148"),      //I
                entry("\ud835\udda9", "\ue149"),      //J
                entry("\ud835\uddaa", "\ue14a"),      //K
                entry("\ud835\uddab", "\ue14b"),      //L
                entry("\ud835\uddac", "\ue14c"),      //M
                entry("\ud835\uddad", "\ue14d"),      //N
                entry("\ud835\uddae", "\ue14e"),      //O
                entry("\ud835\uddaf", "\ue14f"),      //P
                entry("\ud835\uddb0", "\ue150"),      //Q
                entry("\ud835\uddb1", "\ue151"),      //R
                entry("\ud835\uddb2", "\ue152"),      //S
                entry("\ud835\uddb3", "\ue153"),      //T
                entry("\ud835\uddb4", "\ue154"),      //U
                entry("\ud835\uddb5", "\ue155"),      //V
                entry("\ud835\uddb6", "\ue156"),      //W
                entry("\ud835\uddb7", "\ue157"),      //X
                entry("\ud835\uddb8", "\ue158"),      //Y
                entry("\ud835\uddb9", "\ue159"),      //Z

                //Fraktur
                entry("\ud835\udd1e", "\ue1a5"),      //a
                entry("\ud835\udd1f", "\ue1a6"),      //b
                entry("\ud835\udd20", "\ue1a7"),      //c
                entry("\ud835\udd21", "\ue1a8"),      //d
                entry("\ud835\udd22", "\ue1a9"),      //e
                entry("\ud835\udd23", "\ue1aa"),      //f
                entry("\ud835\udd24", "\ue1ab"),      //g
                entry("\ud835\udd25", "\ue1ac"),      //h
                entry("\ud835\udd26", "\ue1ad"),      //i
                entry("\ud835\udd27", "\ue1ae"),      //j
                entry("\ud835\udd28", "\ue1af"),      //k
                entry("\ud835\udd29", "\ue1b0"),      //l
                entry("\ud835\udd2a", "\ue1b1"),      //m
                entry("\ud835\udd2b", "\ue1b2"),      //n
                entry("\ud835\udd2c", "\ue1b3"),      //o
                entry("\ud835\udd2d", "\ue1b4"),      //p
                entry("\ud835\udd2e", "\ue1b5"),      //q
                entry("\ud835\udd2f", "\ue1b6"),      //r
                entry("\ud835\udd30", "\ue1b7"),      //s
                entry("\ud835\udd31", "\ue1b8"),      //t
                entry("\ud835\udd32", "\ue1b9"),      //u
                entry("\ud835\udd33", "\ue1ba"),      //v
                entry("\ud835\udd34", "\ue1bb"),      //w
                entry("\ud835\udd35", "\ue1bc"),      //x
                entry("\ud835\udd36", "\ue1bd"),      //y
                entry("\ud835\udd37", "\ue1be"),      //z
                entry("\ud835\udd04", "\ue190"),      //A
                entry("\ud835\udd05", "\ue191"),      //B
                entry("\u212d", "\ue1fd"),            //C
                entry("\ud835\udd07", "\ue192"),      //D
                entry("\ud835\udd08", "\ue193"),      //E
                entry("\ud835\udd09", "\ue194"),      //F
                entry("\ud835\udd0a", "\ue195"),      //G
                entry("\u210c", "\ue1c0"),            //H
                entry("\u2111", "\ue1c1"),            //I
                entry("\ud835\udd0d", "\ue196"),      //J
                entry("\ud835\udd0e", "\ue197"),      //K
                entry("\ud835\udd0f", "\ue198"),      //L
                entry("\ud835\udd10", "\ue199"),      //M
                entry("\ud835\udd11", "\ue19a"),      //N
                entry("\ud835\udd12", "\ue19b"),      //O
                entry("\ud835\udd13", "\ue19c"),      //P
                entry("\ud835\udd14", "\ue19d"),      //Q
                entry("\u211c", "\ue1c2"),            //R
                entry("\ud835\udd16", "\ue19e"),      //S
                entry("\ud835\udd17", "\ue19f"),      //T
                entry("\ud835\udd18", "\ue1a0"),      //U
                entry("\ud835\udd19", "\ue1a1"),      //V
                entry("\ud835\udd1a", "\ue1a2"),      //W
                entry("\ud835\udd1b", "\ue1a3"),      //X
                entry("\ud835\udd1c", "\ue1a4"),      //Y
                entry("\u2128", "\ue1c3"),           //Z

                //Blackboard
                entry("\uf893", "\ue1e3"),      //a
                entry("\uf894", "\ue1e4"),      //b
                entry("\uf895", "\ue1e5"),      //c
                entry("\uf896", "\ue1e6"),      //d
                entry("\uf897", "\ue1e7"),      //e
                entry("\uf898", "\ue1e8"),      //f
                entry("\uf899", "\ue1e9"),      //g
                entry("\uf89a", "\ue1ea"),      //h
                entry("\uf89b", "\ue1eb"),      //i
                entry("\uf89c", "\ue1ec"),      //j
                entry("\uf89d", "\ue1ed"),      //k
                entry("\uf89e", "\ue1ee"),      //l
                entry("\uf89f", "\ue1ef"),      //m
                entry("\uf8a0", "\ue1f0"),      //n
                entry("\uf8a1", "\ue1f1"),      //o
                entry("\uf8a2", "\ue1f2"),      //p
                entry("\uf8a3", "\ue1f3"),      //q
                entry("\uf8a4", "\ue1f4"),      //r
                entry("\uf8a5", "\ue1f5"),      //s
                entry("\uf8a6", "\ue1f6"),      //t
                entry("\uf8a7", "\ue1f7"),      //u
                entry("\uf8a8", "\ue1f8"),      //v
                entry("\uf8a9", "\ue1f9"),      //w
                entry("\uf8aa", "\ue1fa"),      //x
                entry("\uf8ab", "\ue1fb"),      //y
                entry("\uf8ac", "\ue1fc"),      //z
                entry("\ud835\udfd8", "\ue204"),      //0
                entry("\ud835\udfd9", "\ue205"),      //1
                entry("\ud835\udfda", "\ue206"),      //2
                entry("\ud835\udfdb", "\ue207"),      //3
                entry("\ud835\udfdc", "\ue208"),      //4
                entry("\ud835\udfdd", "\ue209"),      //5
                entry("\ud835\udfde", "\ue20a"),      //6
                entry("\ud835\udfdf", "\ue20b"),      //7
                entry("\ud835\udfe0", "\ue20c"),      //8
                entry("\ud835\udfe1", "\ue20d"),      //9
                entry("\uf880", "\ue1d0"),      //A
                entry("\uf881", "\ue1d1"),      //B
                entry("\uf8ad", "\ue1fd"),      //C
                entry("\uf882", "\ue1d2"),      //D
                entry("\uf883", "\ue1d3"),      //E
                entry("\uf884", "\ue1d4"),      //F
                entry("\uf885", "\ue1d5"),      //G
                entry("\uf8ae", "\ue1fe"),      //H
                entry("\uf886", "\ue1d6"),      //I
                entry("\uf887", "\ue1d7"),      //J
                entry("\uf888", "\ue1d8"),      //K
                entry("\uf889", "\ue1d9"),      //L
                entry("\uf88a", "\ue1da"),      //M
                entry("\uf8af", "\ue1ff"),      //N
                entry("\uf88b", "\ue1db"),      //O
                entry("\uf8b0", "\ue200"),      //P
                entry("\uf8b1", "\ue201"),      //Q
                entry("\uf8b2", "\ue202"),      //R
                entry("\uf88c", "\ue1dc"),      //S
                entry("\uf88d", "\ue1dd"),      //T
                entry("\uf88e", "\ue1de"),      //U
                entry("\uf88f", "\ue1df"),      //V
                entry("\uf890", "\ue1e0"),      //W
                entry("\uf891", "\ue1e1"),      //X
                entry("\uf892", "\ue1e2"),      //Y
                entry("\uf8b3", "\ue203"),      //Z

                //Special
                entry("\u00ac", "\ue220"),      //hammer
                entry("\u25b3", "\ue221"),      //meta caret
                entry("\u25bd", "\ue222"),      //meta wedge
                entry("\u21d2", "\ue223"),      //meta arrow
                entry("\u21d4", "\ue224"),      //meta double arrow
                entry("\u234a", "\ue225"),      //meta bottom
                entry("\u22a2", "\ue226"),      //proves
                entry("\u22ac", "\ue227"),      //not proves
                entry("\u22a8", "\ue228"),      //entails
                entry("\u22ad", "\ue229"),      //not entails
                entry("\uf8d0", "\ue22a"),      //replacement rule
                entry("\u2227", "\ue22c"),      //caret
                entry("\u2228", "\ue22d"),      //wedge
                entry("\u2192", "\ue22e"),      //arrow
                entry("\u2194", "\ue22f"),      //double arrow
                entry("\u22a5", "\ue230"),      //bottom
                entry("\u2200", "\ue232"),      //universal
                entry("\u2203", "\ue233"),      //existential
                entry("\ue8a5", "\ue8af"),      //big not less than
                entry("\ue8a7", "\ue8b0"),      //big not less than or equal
                entry("\ue886", "\ue899"),      //zero with slash
                entry("\ue8ad", "\ue8b7"),      //big not equals
                entry("\ue8ac", "\ue8b6"),      //big equals

                entry("\u00d7", "\ue23b"),      //times
                entry("\ue8b8", "\ue8b9"),      //big plus
                entry("\ue8ba", "\ue8bb"),      //big times
                entry("\u2264", "\ue235"),      //less than or equal
                entry("\u2265", "\ue259"),      //greater than or equal
                entry("\ue8a4", "\ue8ae"),      //big less than
                entry("\ue8a6", "\ue8b0"),      //big less than or equal
                entry("\u2260", "\ue237"),      //not equal

                entry("\u25fb", "\ue240"),      //box
                entry("\u25ca", "\ue241"),      //diamond
                entry("\u2261", "\ue242"),      //triple bar
                entry("\u2283", "\ue243"),      //horseshoe
                entry("\u22c0", "\ue244"),      //big caret
                entry("\u22c1", "\ue245"),      //big wedge
                entry("\u2191", "\ue23d"),      //up arrow
                entry("\u2193", "\ue23e"),      //down arrow
 //               entry("\u2135", "\ue246"),      //aleph
 //               entry("\u2136", "\ue247"),      //beth
//                entry("\ud835\udcab", ""),       //powerset
                entry("\u2217", "\ue249"),      //asterisk
                entry("\u22c6", "\ue24a"),      //star
                entry("\u2205", "\ue24b"),      //empty set
                entry("\u00f7", "\ue24c"),      //divides
                entry("\u2238", "\ue264"),      //dot minus

                entry("\u21be", "\ue25a"),      //harpoon
                entry("\u2208", "\ue24f"),      //element
                entry("\u2209", "\ue250"),      //not element
                entry("\u2282", "\ue24d"),      //proper subset
                entry("\u2286", "\ue24e"),      //subset
                entry("\u222a", "\ue252"),      //union
                entry("\u22c3", "\ue254"),      //big union
                entry("\u2229", "\ue251"),      //intersection
                entry("\u22c2", "\ue253"),      //big intersection
                entry("\u27e6", "\ue238"),      //open double bracket
                entry("\u27e7", "\ue239"),      //close double bracket

                entry("\u2248", "\ue25b"),      //double wave
                entry("\u2243", "\ue25c"),      //wave above line
                entry("\u2245", "\ue25d"),      //wave above equals
                entry("\u2291", "\ue25e"),      //submodel
                entry("\uf8d1", "\ue25f"),      //embedding
                entry("\u227a", "\ue260"),      //set smaller than
                entry("\uf8d2", "\ue261"),      //elementary submodel
                entry("\u227e", "\ue262"),      //elementary embedding
                entry("\u227c", "\ue263"),      //set less than or equal
                entry("\u231c", "\ue255"),      //left corner
                entry("\u231d", "\ue256"),      //right corner

                entry("\u22a4", "\ue231"),      //top
                entry("\u2039", "\ue257"),      //french left
                entry("\u203a", "\ue258"),      //french right
                entry("\u27e8", "\ue265"),      //left angle
                entry("\u27e9", "\ue266"),      //right angle
                entry("\u00a0", "\ue267")       //unbreakable space
        );
    }

    private Map<String, String> initializeMacMap() {
        return Map.ofEntries(

                //base map
                entry("a", "\ue2c0"),
                entry("b", "\ue2c1"),
                entry("c", "\ue2c2"),
                entry("d", "\ue2c3"),
                entry("e", "\ue2c4"),
                entry("f", "\ue2c5"),
                entry("g", "\ue2c6"),
                entry("h", "\ue2c7"),
                entry("i", "\ue2c8"),
                entry("j", "\ue2c9"),
                entry("k", "\ue2ca"),
                entry("l", "\ue2cb"),
                entry("m", "\ue2cc"),
                entry("n", "\ue2cd"),
                entry("o", "\ue2ce"),
                entry("p", "\ue2cf"),
                entry("q", "\ue2d0"),
                entry("r", "\ue2d1"),
                entry("s", "\ue2d2"),
                entry("t", "\ue2d3"),
                entry("u", "\ue2d4"),
                entry("v", "\ue2d5"),
                entry("w", "\ue2d6"),
                entry("x", "\ue2d7"),
                entry("y", "\ue2d8"),
                entry("z", "\ue2d9"),
                entry("A", "\ue2a0"),
                entry("B", "\ue2a1"),
                entry("C", "\ue2a2"),
                entry("D", "\ue2a3"),
                entry("E", "\ue2a4"),
                entry("F", "\ue2a5"),
                entry("G", "\ue2a6"),
                entry("H", "\ue2a7"),
                entry("I", "\ue2a8"),
                entry("J", "\ue2a9"),
                entry("K", "\ue2aa"),
                entry("L", "\ue2ab"),
                entry("M", "\ue2ac"),
                entry("N", "\ue2ad"),
                entry("O", "\ue2ae"),
                entry("P", "\ue2af"),
                entry("Q", "\ue2b0"),
                entry("R", "\ue2b1"),
                entry("S", "\ue2b2"),
                entry("T", "\ue2b3"),
                entry("U", "\ue2b4"),
                entry("V", "\ue2b5"),
                entry("W", "\ue2b6"),
                entry("X", "\ue2b7"),
                entry("Y", "\ue2b8"),
                entry("Z", "\ue2b9"),
                entry("1", "\ue290"),
                entry("2", "\ue291"),
                entry("3", "\ue292"),
                entry("4", "\ue293"),
                entry("5", "\ue294"),
                entry("6", "\ue295"),
                entry("7", "\ue296"),
                entry("8", "\ue297"),
                entry("9", "\ue298"),
                entry("0", "\ue28f"),

                //italic (alt map)
                entry("\ud835\udc4e", "\ue2fa"),      //a
                entry("\ud835\udc4f", "\ue2fb"),      //b
                entry("\ud835\udc50", "\ue2fc"),      //c
                entry("\ud835\udc51", "\ue2fd"),      //d
                entry("\ud835\udc52", "\ue2fe"),      //e
                entry("\ud835\udc53", "\ue2ff"),      //f
                entry("\ud835\udc54", "\ue300"),      //g
                entry("\u210e", "\ue313"),            //h
                entry("\ud835\udc56", "\ue301"),      //i
                entry("\ud835\udc57", "\ue302"),      //j
                entry("\ud835\udc58", "\ue303"),      //k
                entry("\ud835\udc59", "\ue304"),      //l
                entry("\ud835\udc5a", "\ue305"),      //m
                entry("\ud835\udc5b", "\ue306"),      //n
                entry("\ud835\udc5c", "\ue307"),      //o
                entry("\ud835\udc5d", "\ue308"),      //p
                entry("\ud835\udc5e", "\ue309"),      //q
                entry("\ud835\udc5f", "\ue30a"),      //r
                entry("\ud835\udc60", "\ue30b"),      //s
                entry("\ud835\udc61", "\ue30c"),      //t
                entry("\ud835\udc62", "\ue30d"),      //u
                entry("\ud835\udc63", "\ue30e"),      //v
                entry("\ud835\udc64", "\ue30f"),      //w
                entry("\ud835\udc65", "\ue310"),      //x
                entry("\ud835\udc66", "\ue311"),      //y
                entry("\ud835\udc67", "\ue312"),      //z
                entry("\u211d", "\ue485"),            //bb R
 //               entry("\u2112", ""),            //script L
                entry("\u2115", "\ue481"),            //bb N
                entry("\u2124", "\ue482"),            //bb Z
                entry("\u2119", "\ue483"),            //bb P
                entry("\u211a", "\ue484"),            //bb Q
                entry("\ud835\udc34", "\ue2e0"),      //A
                entry("\ud835\udc35", "\ue2e1"),      //B
                entry("\ud835\udc36", "\ue2e2"),      //C
                entry("\ud835\udc37", "\ue2e3"),      //D
                entry("\ud835\udc38", "\ue2e4"),      //E
                entry("\ud835\udc39", "\ue2e5"),      //F
                entry("\ud835\udc3a", "\ue2e6"),      //G
                entry("\ud835\udc3b", "\ue2e7"),      //H
                entry("\ud835\udc3c", "\ue2e8"),      //I
                entry("\ud835\udc3d", "\ue2e9"),      //J
                entry("\ud835\udc3e", "\ue2ea"),      //K
                entry("\ud835\udc3f", "\ue2eb"),      //L
                entry("\ud835\udc40", "\ue2ec"),      //M
                entry("\ud835\udc41", "\ue2ed"),      //N
                entry("\ud835\udc42", "\ue2ee"),      //O
                entry("\ud835\udc43", "\ue2ef"),      //P
                entry("\ud835\udc44", "\ue2f0"),      //Q
                entry("\ud835\udc45", "\ue2f1"),      //R
                entry("\ud835\udc46", "\ue2f2"),      //S
                entry("\ud835\udc47", "\ue2f3"),      //T
                entry("\ud835\udc48", "\ue2f4"),      //U
                entry("\ud835\udc49", "\ue2f5"),      //V
                entry("\ud835\udc4a", "\ue2f6"),      //W
                entry("\ud835\udc4b", "\ue2f7"),      //X
                entry("\ud835\udc4c", "\ue2f8"),      //Y
                entry("\ud835\udc4d", "\ue2f9"),      //Z


                //script map
                entry("\ud835\udcb6", "\ue342"),       //a
                entry("\ud835\udcb7", "\ue343"),       //b
                entry("\ud835\udcb8", "\ue344"),       //c
                entry("\ud835\udcb9", "\ue345"),       //d
                entry("\u212f", "\ue359"),             //e
                entry("\ud835\udcbb", "\ue346"),       //f
                entry("\u210a", "\ue35a"),             //g
                entry("\ud835\udcbd", "\ue347"),       //h
                entry("\ud835\udcbe", "\ue348"),       //i
                entry("\ud835\udcbf", "\ue349"),       //j
                entry("\ud835\udcc0", "\ue34a"),       //k
                entry("\ud835\udcc1", "\ue34b"),       //l
                entry("\ud835\udcc2", "\ue34c"),       //m
                entry("\ud835\udcc3", "\ue34d"),       //n
                entry("\u2134", "\ue35b"),             //o
                entry("\ud835\udcc5", "\ue34e"),       //p
                entry("\ud835\udcc6", "\ue34f"),       //q
                entry("\ud835\udcc7", "\ue350"),       //r
                entry("\ud835\udcc8", "\ue351"),       //s
                entry("\ud835\udcc9", "\ue352"),       //t
                entry("\ud835\udcca", "\ue353"),       //u
                entry("\ud835\udccb", "\ue354"),       //v
                entry("\ud835\udccc", "\ue355"),       //w
                entry("\ud835\udccd", "\ue356"),       //x
                entry("\ud835\udcce", "\ue357"),       //y
                entry("\ud835\udccf", "\ue358"),       //z
                entry("\ud835\udc9c", "\ue330"),       //A
                entry("\u212c", "\ue35c"),             //B
                entry("\ud835\udc9e", "\ue331"),       //C
                entry("\ud835\udc9f", "\ue332"),       //D
                entry("\u2130", "\ue35d"),                   //E
                entry("\u2131", "\ue35e"),             //F
                entry("\ud835\udca2", "\ue333"),       //G
                entry("\u210b", "\ue35f"),             //H
                entry("\u2110", "\ue360"),             //I
                entry("\ud835\udca5", "\ue334"),       //J
                entry("\ud835\udca6", "\ue335"),       //K
                entry("\u2112", "\ue361"),             //L
                entry("\u2133", "\ue362"),             //M
                entry("\ud835\udca9", "\ue336"),       //N
                entry("\ud835\udcaa", "\ue337"),       //O
                entry("\ud835\udcab", "\ue338"),       //P
                entry("\ud835\udcac", "\ue339"),       //Q
                entry("\u211b", "\ue363"),             //R
                entry("\ud835\udcae", "\ue33a"),       //S
                entry("\ud835\udcaf", "\ue33b"),       //T
                entry("\ud835\udcb0", "\ue33c"),       //U
                entry("\ud835\udcb1", "\ue33d"),       //V
                entry("\ud835\udcb2", "\ue33e"),       //W
                entry("\ud835\udcb3", "\ue33f"),       //X
                entry("\ud835\udcb4", "\ue340"),       //Y
                entry("\ud835\udcb5", "\ue341"),       //Z

                //Greek
                entry("\u03b1", "\ue388"),      //alpha
                entry("\u03b2", "\ue389"),      //beta
                entry("\u03b3", "\ue38a"),      //gamma
                entry("\u03b4", "\ue38b"),      //delta
                entry("\u03b5", "\ue38c"),      //epsilon
                entry("\u03b6", "\ue38d"),      //zeta
                entry("\u03b7", "\ue38e"),      //eta
                entry("\u03b8", "\ue38f"),      //theta
                entry("\u03b9", "\ue390"),      //iota
                entry("\u03ba", "\ue391"),      //kappa
                entry("\u03bb", "\ue392"),      //lambda
                entry("\u03bc", "\ue393"),      //mu
                entry("\u03bd", "\ue394"),      //nu
                entry("\u03be", "\ue395"),      //xi
                entry("\u03bf", "\ue396"),      //omicron
                entry("\u03c0", "\ue397"),      //pi
                entry("\u03c1", "\ue398"),      //rho
                entry("\u03c2", "\ue399"),      //end sigma
                entry("\u03c3", "\ue39a"),      //sigma
                entry("\u03c4", "\ue39b"),      //tau
                entry("\u03c5", "\ue39c"),      //upsilon
                entry("\u03c6", "\ue39d"),      //phi
                entry("\u03c7", "\ue39e"),      //chi
                entry("\u03c8", "\ue39f"),      //psi
                entry("\u03c9", "\ue3a0"),      //omega
                entry("\uf8b4", "\ue3a1"),      //slanted beta
                entry("\uf8b5", "\ue3a2"),      //slanted mu
                entry("\u0391", "\ue370"),      //Alpha
                entry("\u0392", "\ue371"),      //Beta
                entry("\u0393", "\ue372"),      //Gamma
                entry("\u0394", "\ue373"),      //Delta
                entry("\u0395", "\ue374"),      //Epsilon
                entry("\u0396", "\ue375"),      //Zeta
                entry("\u0397", "\ue376"),      //Eta
                entry("\u0398", "\ue377"),      //Theta
                entry("\u0399", "\ue378"),      //Iota
                entry("\u039a", "\ue379"),      //Kappa
                entry("\u039b", "\ue37a"),      //Lambda
                entry("\u039c", "\ue37b"),      //Mu
                entry("\u039d", "\ue37c"),      //Nu
                entry("\u039e", "\ue37d"),      //Xi
                entry("\u039f", "\ue37e"),      //Omicron
                entry("\u03a0", "\ue37f"),      //Pi
                entry("\u03a1", "\ue380"),      //Rho
                entry("\u03a3", "\ue381"),      //Sigma
                entry("\u03a4", "\ue382"),      //Tau
                entry("\u03a5", "\ue383"),      //Upsilon
                entry("\u03a6", "\ue384"),      //Phi
                entry("\u03a7", "\ue385"),      //Chi
                entry("\u03a8", "\ue386"),      //Psi
                entry("\u03a9", "\ue387"),      //Omega
                entry("\uf8d5", "\ue480"),  //double mu

                //Sans
                entry("\ud835\uddba", "\ue3ca"),      //a
                entry("\ud835\uddbb", "\ue3cb"),      //b
                entry("\ud835\uddbc", "\ue3cc"),      //c
                entry("\ud835\uddbd", "\ue3cd"),      //d
                entry("\ud835\uddbe", "\ue3ce"),      //e
                entry("\ud835\uddbf", "\ue3cf"),      //f
                entry("\ud835\uddc0", "\ue3d0"),      //g
                entry("\ud835\uddc1", "\ue3d1"),      //h
                entry("\ud835\uddc2", "\ue3d2"),      //i
                entry("\ud835\uddc3", "\ue3d3"),      //j
                entry("\ud835\uddc4", "\ue3d4"),      //k
                entry("\ud835\uddc5", "\ue3d5"),      //l
                entry("\ud835\uddc6", "\ue3d6"),      //m
                entry("\ud835\uddc7", "\ue3d7"),      //n
                entry("\ud835\uddc8", "\ue3d8"),      //o
                entry("\ud835\uddc9", "\ue3d9"),      //p
                entry("\ud835\uddca", "\ue3da"),      //q
                entry("\ud835\uddcb", "\ue3db"),      //r
                entry("\ud835\uddcc", "\ue3dc"),      //s
                entry("\ud835\uddcd", "\ue3dd"),      //t
                entry("\ud835\uddce", "\ue3de"),      //u
                entry("\ud835\uddcf", "\ue3df"),      //v
                entry("\ud835\uddd0", "\ue3e0"),      //w
                entry("\ud835\uddd1", "\ue3e1"),      //x
                entry("\ud835\uddd2", "\ue3e2"),      //y
                entry("\ud835\uddd3", "\ue3e3"),      //z
                entry("\ud835\udfe2", "\ue3e4"),      //0
                entry("\ud835\udfe3", "\ue3e5"),      //1
                entry("\ud835\udfe4", "\ue3e6"),      //2
                entry("\ud835\udfe5", "\ue3e7"),      //3
                entry("\ud835\udfe6", "\ue3e8"),      //4
                entry("\ud835\udfe7", "\ue3e9"),      //5
                entry("\ud835\udfe8", "\ue3ea"),      //6
                entry("\ud835\udfe9", "\ue3eb"),      //7
                entry("\ud835\udfea", "\ue3ec"),      //8
                entry("\ud835\udfeb", "\ue3ed"),      //9
                entry("\ud835\udda0", "\ue3b0"),      //A
                entry("\ud835\udda1", "\ue3b1"),      //B
                entry("\ud835\udda2", "\ue3b2"),      //C
                entry("\ud835\udda3", "\ue3b3"),      //D
                entry("\ud835\udda4", "\ue3b4"),      //E
                entry("\ud835\udda5", "\ue3b5"),      //F
                entry("\ud835\udda6", "\ue3b6"),      //G
                entry("\ud835\udda7", "\ue3b7"),      //H
                entry("\ud835\udda8", "\ue3b8"),      //I
                entry("\ud835\udda9", "\ue3b9"),      //J
                entry("\ud835\uddaa", "\ue3ba"),      //K
                entry("\ud835\uddab", "\ue3bb"),      //L
                entry("\ud835\uddac", "\ue3bc"),      //M
                entry("\ud835\uddad", "\ue3bd"),      //N
                entry("\ud835\uddae", "\ue3be"),      //O
                entry("\ud835\uddaf", "\ue3bf"),      //P
                entry("\ud835\uddb0", "\ue3c0"),      //Q
                entry("\ud835\uddb1", "\ue3c1"),      //R
                entry("\ud835\uddb2", "\ue3c2"),      //S
                entry("\ud835\uddb3", "\ue3c3"),      //T
                entry("\ud835\uddb4", "\ue3c4"),      //U
                entry("\ud835\uddb5", "\ue3c5"),      //V
                entry("\ud835\uddb6", "\ue3c6"),      //W
                entry("\ud835\uddb7", "\ue3c7"),      //X
                entry("\ud835\uddb8", "\ue3c8"),      //Y
                entry("\ud835\uddb9", "\ue3c9"),      //Z


                //Fraktur
                entry("\ud835\udd1e", "\ue415"),      //a
                entry("\ud835\udd1f", "\ue416"),      //b
                entry("\ud835\udd20", "\ue417"),      //c
                entry("\ud835\udd21", "\ue418"),      //d
                entry("\ud835\udd22", "\ue419"),      //e
                entry("\ud835\udd23", "\ue41a"),      //f
                entry("\ud835\udd24", "\ue41b"),      //g
                entry("\ud835\udd25", "\ue41c"),      //h
                entry("\ud835\udd26", "\ue41d"),      //i
                entry("\ud835\udd27", "\ue41e"),      //j
                entry("\ud835\udd28", "\ue41f"),      //k
                entry("\ud835\udd29", "\ue420"),      //l
                entry("\ud835\udd2a", "\ue421"),      //m
                entry("\ud835\udd2b", "\ue422"),      //n
                entry("\ud835\udd2c", "\ue423"),      //o
                entry("\ud835\udd2d", "\ue424"),      //p
                entry("\ud835\udd2e", "\ue425"),      //q
                entry("\ud835\udd2f", "\ue426"),      //r
                entry("\ud835\udd30", "\ue427"),      //s
                entry("\ud835\udd31", "\ue428"),      //t
                entry("\ud835\udd32", "\ue429"),      //u
                entry("\ud835\udd33", "\ue42a"),      //v
                entry("\ud835\udd34", "\ue42b"),      //w
                entry("\ud835\udd35", "\ue42c"),      //x
                entry("\ud835\udd36", "\ue42d"),      //y
                entry("\ud835\udd37", "\ue42e"),      //z
                entry("\ud835\udd04", "\ue400"),      //A
                entry("\ud835\udd05", "\ue401"),      //B
                entry("\u212d", "\ue42f"),            //C
                entry("\ud835\udd07", "\ue402"),      //D
                entry("\ud835\udd08", "\ue403"),      //E
                entry("\ud835\udd09", "\ue404"),      //F
                entry("\ud835\udd0a", "\ue405"),      //G
                entry("\u210c", "\ue430"),            //H
                entry("\u2111", "\ue431"),            //I
                entry("\ud835\udd0d", "\ue406"),      //J
                entry("\ud835\udd0e", "\ue407"),      //K
                entry("\ud835\udd0f", "\ue408"),      //L
                entry("\ud835\udd10", "\ue409"),      //M
                entry("\ud835\udd11", "\ue40a"),      //N
                entry("\ud835\udd12", "\ue40b"),      //O
                entry("\ud835\udd13", "\ue40c"),      //P
                entry("\ud835\udd14", "\ue40d"),      //Q
                entry("\u211c", "\ue432"),            //R
                entry("\ud835\udd16", "\ue40e"),      //S
                entry("\ud835\udd17", "\ue40f"),      //T
                entry("\ud835\udd18", "\ue410"),      //U
                entry("\ud835\udd19", "\ue411"),      //V
                entry("\ud835\udd1a", "\ue412"),      //W
                entry("\ud835\udd1b", "\ue413"),      //X
                entry("\ud835\udd1c", "\ue414"),      //Y
                entry("\u2128", "\ue433"),           //Z

                //Blackboard
                entry("\uf893", "\ue453"),      //a
                entry("\uf894", "\ue454"),      //b
                entry("\uf895", "\ue455"),      //c
                entry("\uf896", "\ue456"),      //d
                entry("\uf897", "\ue457"),      //e
                entry("\uf898", "\ue458"),      //f
                entry("\uf899", "\ue459"),      //g
                entry("\uf89a", "\ue45a"),      //h
                entry("\uf89b", "\ue45b"),      //i
                entry("\uf89c", "\ue45c"),      //j
                entry("\uf89d", "\ue45d"),      //k
                entry("\uf89e", "\ue45e"),      //l
                entry("\uf89f", "\ue45f"),      //m
                entry("\uf8a0", "\ue460"),      //n
                entry("\uf8a1", "\ue461"),      //o
                entry("\uf8a2", "\ue462"),      //p
                entry("\uf8a3", "\ue463"),      //q
                entry("\uf8a4", "\ue464"),      //r
                entry("\uf8a5", "\ue465"),      //s
                entry("\uf8a6", "\ue466"),      //t
                entry("\uf8a7", "\ue467"),      //u
                entry("\uf8a8", "\ue468"),      //v
                entry("\uf8a9", "\ue469"),      //w
                entry("\uf8aa", "\ue46a"),      //x
                entry("\uf8ab", "\ue46b"),      //y
                entry("\uf8ac", "\ue46c"),      //z
                entry("\ud835\udfd8", "\ue474"),      //0
                entry("\ud835\udfd9", "\ue475"),      //1
                entry("\ud835\udfda", "\ue476"),      //2
                entry("\ud835\udfdb", "\ue477"),      //3
                entry("\ud835\udfdc", "\ue478"),      //4
                entry("\ud835\udfdd", "\ue479"),      //5
                entry("\ud835\udfde", "\ue47a"),      //6
                entry("\ud835\udfdf", "\ue47b"),      //7
                entry("\ud835\udfe0", "\ue47c"),      //8
                entry("\ud835\udfe1", "\ue47d"),      //9
                entry("\uf880", "\ue440"),      //A
                entry("\uf881", "\ue441"),      //B
                entry("\uf8ad", "\ue46d"),      //C
                entry("\uf882", "\ue442"),      //D
                entry("\uf883", "\ue443"),      //E
                entry("\uf884", "\ue444"),      //F
                entry("\uf885", "\ue445"),      //G
                entry("\uf8ae", "\ue46e"),      //H
                entry("\uf886", "\ue446"),      //I
                entry("\uf887", "\ue447"),      //J
                entry("\uf888", "\ue448"),      //K
                entry("\uf889", "\ue449"),      //L
                entry("\uf88a", "\ue44a"),      //M
                entry("\uf8af", "\ue46f"),      //N
                entry("\uf88b", "\ue44b"),      //O
                entry("\uf8b0", "\ue470"),      //P
                entry("\uf8b1", "\ue471"),      //Q
                entry("\uf8b2", "\ue472"),      //R
                entry("\uf88c", "\ue44c"),      //S
                entry("\uf88d", "\ue44d"),      //T
                entry("\uf88e", "\ue44e"),      //U
                entry("\uf88f", "\ue44f"),      //V
                entry("\uf890", "\ue450"),      //W
                entry("\uf891", "\ue451"),      //X
                entry("\uf892", "\ue452"),      //Y
                entry("\uf8b3", "\ue473"),      //Z

                //Special
                entry("\u00d8", "\ue894"),      //zero with slash
                entry("\u2135", "\ue4b6"),      //aleph
                entry("\u2136", "\ue4b7"),      //beth
                entry("\u00a0", "\u00a0")            //unbreakable space
                );
    }
    private Map<String, String> initializeVecMap() {
        return Map.ofEntries(

                //base map
                entry("a", "\ue50f"),
                entry("b", "\ue510"),
                entry("c", "\ue511"),
                entry("d", "\ue512"),
                entry("e", "\ue513"),
                entry("f", "\ue514"),
                entry("g", "\ue515"),
                entry("h", "\ue516"),
                entry("i", "\ue517"),
                entry("j", "\ue518"),
                entry("k", "\ue519"),
                entry("l", "\ue51a"),
                entry("m", "\ue51b"),
                entry("n", "\ue51c"),
                entry("o", "\ue51d"),
                entry("p", "\ue51e"),
                entry("q", "\ue51f"),
                entry("r", "\ue520"),
                entry("s", "\ue521"),
                entry("t", "\ue522"),
                entry("u", "\ue523"),
                entry("v", "\ue524"),
                entry("w", "\ue525"),
                entry("x", "\ue526"),
                entry("y", "\ue527"),
                entry("z", "\ue528"),
                entry("A", "\ue4f5"),
                entry("B", "\ue4f6"),
                entry("C", "\ue4f7"),
                entry("D", "\ue4f8"),
                entry("E", "\ue4f9"),
                entry("F", "\ue4fa"),
                entry("G", "\ue4fb"),
                entry("H", "\ue4fc"),
                entry("I", "\ue4fd"),
                entry("J", "\ue4fe"),
                entry("K", "\ue4ff"),
                entry("L", "\ue500"),
                entry("M", "\ue501"),
                entry("N", "\ue502"),
                entry("O", "\ue503"),
                entry("P", "\ue504"),
                entry("Q", "\ue505"),
                entry("R", "\ue506"),
                entry("S", "\ue507"),
                entry("T", "\ue508"),
                entry("U", "\ue509"),
                entry("V", "\ue50a"),
                entry("W", "\ue50b"),
                entry("X", "\ue50c"),
                entry("Y", "\ue50d"),
                entry("Z", "\ue50e"),
                entry("1", "\ue4ec"),
                entry("2", "\ue4ed"),
                entry("3", "\ue4ee"),
                entry("4", "\ue4ef"),
                entry("5", "\ue4f0"),
                entry("6", "\ue4f1"),
                entry("7", "\ue4f2"),
                entry("8", "\ue4f3"),
                entry("9", "\ue4f4"),
                entry("0", "\ue4eb"),

                //italic (alt map)
                entry("\ud835\udc4e", "\ue543"),      //a
                entry("\ud835\udc4f", "\ue544"),      //b
                entry("\ud835\udc50", "\ue545"),      //c
                entry("\ud835\udc51", "\ue546"),      //d
                entry("\ud835\udc52", "\ue547"),      //e
                entry("\ud835\udc53", "\ue548"),      //f
                entry("\ud835\udc54", "\ue549"),      //g
                entry("\u210e", "\ue55c"),            //h
                entry("\ud835\udc56", "\ue54a"),      //i
                entry("\ud835\udc57", "\ue54b"),      //j
                entry("\ud835\udc58", "\ue54c"),      //k
                entry("\ud835\udc59", "\ue54d"),      //l
                entry("\ud835\udc5a", "\ue54e"),      //m
                entry("\ud835\udc5b", "\ue54f"),      //n
                entry("\ud835\udc5c", "\ue550"),      //o
                entry("\ud835\udc5d", "\ue551"),      //p
                entry("\ud835\udc5e", "\ue552"),      //q
                entry("\ud835\udc5f", "\ue553"),      //r
                entry("\ud835\udc60", "\ue554"),      //s
                entry("\ud835\udc61", "\ue555"),      //t
                entry("\ud835\udc62", "\ue556"),      //u
                entry("\ud835\udc63", "\ue557"),      //v
                entry("\ud835\udc64", "\ue558"),      //w
                entry("\ud835\udc65", "\ue559"),      //x
                entry("\ud835\udc66", "\ue55a"),      //y
                entry("\ud835\udc67", "\ue55b"),      //z
                entry("\u211d", "\ue67c"),            //bb R
                //               entry("\u2112", ""),            //script L
                entry("\u2115", "\ue678"),            //bb N
                entry("\u2124", "\ue679"),            //bb Z
                entry("\u2119", "\ue67a"),            //bb P
                entry("\u211a", "\ue67b"),            //bb Q
                entry("\ud835\udc34", "\ue529"),      //A
                entry("\ud835\udc35", "\ue52a"),      //B
                entry("\ud835\udc36", "\ue52b"),      //C
                entry("\ud835\udc37", "\ue52c"),      //D
                entry("\ud835\udc38", "\ue52d"),      //E
                entry("\ud835\udc39", "\ue52e"),      //F
                entry("\ud835\udc3a", "\ue52f"),      //G
                entry("\ud835\udc3b", "\ue530"),      //H
                entry("\ud835\udc3c", "\ue531"),      //I
                entry("\ud835\udc3d", "\ue532"),      //J
                entry("\ud835\udc3e", "\ue533"),      //K
                entry("\ud835\udc3f", "\ue534"),      //L
                entry("\ud835\udc40", "\ue535"),      //M
                entry("\ud835\udc41", "\ue536"),      //N
                entry("\ud835\udc42", "\ue537"),      //O
                entry("\ud835\udc43", "\ue538"),      //P
                entry("\ud835\udc44", "\ue539"),      //Q
                entry("\ud835\udc45", "\ue53a"),      //R
                entry("\ud835\udc46", "\ue53b"),      //S
                entry("\ud835\udc47", "\ue53c"),      //T
                entry("\ud835\udc48", "\ue53d"),      //U
                entry("\ud835\udc49", "\ue53e"),      //V
                entry("\ud835\udc4a", "\ue53f"),      //W
                entry("\ud835\udc4b", "\ue540"),      //X
                entry("\ud835\udc4c", "\ue541"),      //Y
                entry("\ud835\udc4d", "\ue542"),      //Z


                //script map
                entry("\ud835\udcb6", "\ue570"),       //a
                entry("\ud835\udcb7", "\ue571"),       //b
                entry("\ud835\udcb8", "\ue572"),       //c
                entry("\ud835\udcb9", "\ue573"),       //d
                entry("\u212f", "\ue587"),             //e
                entry("\ud835\udcbb", "\ue574"),       //f
                entry("\u210a", "\ue588"),             //g
                entry("\ud835\udcbd", "\ue575"),       //h
                entry("\ud835\udcbe", "\ue576"),       //i
                entry("\ud835\udcbf", "\ue577"),       //j
                entry("\ud835\udcc0", "\ue578"),       //k
                entry("\ud835\udcc1", "\ue579"),       //l
                entry("\ud835\udcc2", "\ue57a"),       //m
                entry("\ud835\udcc3", "\ue57b"),       //n
                entry("\u2134", "\ue589"),             //o
                entry("\ud835\udcc5", "\ue57c"),       //p
                entry("\ud835\udcc6", "\ue57d"),       //q
                entry("\ud835\udcc7", "\ue57e"),       //r
                entry("\ud835\udcc8", "\ue57f"),       //s
                entry("\ud835\udcc9", "\ue580"),       //t
                entry("\ud835\udcca", "\ue581"),       //u
                entry("\ud835\udccb", "\ue582"),       //v
                entry("\ud835\udccc", "\ue583"),       //w
                entry("\ud835\udccd", "\ue584"),       //x
                entry("\ud835\udcce", "\ue585"),       //y
                entry("\ud835\udccf", "\ue586"),       //z
                entry("\ud835\udc9c", "\ue55e"),       //A
                entry("\u212c", "\ue58a"),             //B
                entry("\ud835\udc9e", "\ue55f"),       //C
                entry("\ud835\udc9f", "\ue560"),       //D
                entry("\u2130", "\ue58b"),                   //E
                entry("\u2131", "\ue58c"),             //F
                entry("\ud835\udca2", "\ue561"),       //G
                entry("\u210b", "\ue58d"),             //H
                entry("\u2110", "\ue58e"),             //I
                entry("\ud835\udca5", "\ue562"),       //J
                entry("\ud835\udca6", "\ue563"),       //K
                entry("\u2112", "\ue58f"),             //L
                entry("\u2133", "\ue590"),             //M
                entry("\ud835\udca9", "\ue564"),       //N
                entry("\ud835\udcaa", "\ue565"),       //O
                entry("\ud835\udcab", "\ue566"),       //P
                entry("\ud835\udcac", "\ue567"),       //Q
                entry("\u211b", "\ue591"),             //R
                entry("\ud835\udcae", "\ue568"),       //S
                entry("\ud835\udcaf", "\ue569"),       //T
                entry("\ud835\udcb0", "\ue56a"),       //U
                entry("\ud835\udcb1", "\ue56b"),       //V
                entry("\ud835\udcb2", "\ue56c"),       //W
                entry("\ud835\udcb3", "\ue56d"),       //X
                entry("\ud835\udcb4", "\ue56e"),       //Y
                entry("\ud835\udcb5", "\ue56f"),       //Z

                //Greek
                entry("\u03b1", "\ue5aa"),      //alpha
                entry("\u03b2", "\ue5ab"),      //beta
                entry("\u03b3", "\ue5ac"),      //gamma
                entry("\u03b4", "\ue5ad"),      //delta
                entry("\u03b5", "\ue5ae"),      //epsilon
                entry("\u03b6", "\ue5af"),      //zeta
                entry("\u03b7", "\ue5b0"),      //eta
                entry("\u03b8", "\ue5b1"),      //theta
                entry("\u03b9", "\ue5b2"),      //iota
                entry("\u03ba", "\ue5b3"),      //kappa
                entry("\u03bb", "\ue5b4"),      //lambda
                entry("\u03bc", "\ue5b5"),      //mu
                entry("\u03bd", "\ue5b6"),      //nu
                entry("\u03be", "\ue5b7"),      //xi
                entry("\u03bf", "\ue5b8"),      //omicron
                entry("\u03c0", "\ue5b9"),      //pi
                entry("\u03c1", "\ue5ba"),      //rho
                entry("\u03c2", "\ue5bb"),      //end sigma
                entry("\u03c3", "\ue5bc"),      //sigma
                entry("\u03c4", "\ue5bd"),      //tau
                entry("\u03c5", "\ue5be"),      //upsilon
                entry("\u03c6", "\ue5bf"),      //phi
                entry("\u03c7", "\ue5c0"),      //chi
                entry("\u03c8", "\ue5c1"),      //psi
                entry("\u03c9", "\ue5c2"),      //omega
                entry("\uf8b4", "\ue5c3"),      //slanted beta
                entry("\uf8b5", "\ue5c4"),      //slanted mu
                entry("\u0391", "\ue592"),      //Alpha
                entry("\u0392", "\ue593"),      //Beta
                entry("\u0393", "\ue594"),      //Gamma
                entry("\u0394", "\ue595"),      //Delta
                entry("\u0395", "\ue596"),      //Epsilon
                entry("\u0396", "\ue597"),      //Zeta
                entry("\u0397", "\ue598"),      //Eta
                entry("\u0398", "\ue599"),      //Theta
                entry("\u0399", "\ue59a"),      //Iota
                entry("\u039a", "\ue59b"),      //Kappa
                entry("\u039b", "\ue59c"),      //Lambda
                entry("\u039c", "\ue59d"),      //Mu
                entry("\u039d", "\ue59e"),      //Nu
                entry("\u039e", "\ue59f"),      //Xi
                entry("\u039f", "\ue5a0"),      //Omicron
                entry("\u03a0", "\ue5a1"),      //Pi
                entry("\u03a1", "\ue5a2"),      //Rho
                entry("\u03a3", "\ue5a3"),      //Sigma
                entry("\u03a4", "\ue5a4"),      //Tau
                entry("\u03a5", "\ue5a5"),      //Upsilon
                entry("\u03a6", "\ue5a6"),      //Phi
                entry("\u03a7", "\ue5a7"),      //Chi
                entry("\u03a8", "\ue5a8"),      //Psi
                entry("\u03a9", "\ue5a9"),      //Omega
                entry("\uf8d5", "\ue677"),  //double mu

                //Sans
                entry("\ud835\uddba", "\ue5df"),      //a
                entry("\ud835\uddbb", "\ue5e0"),      //b
                entry("\ud835\uddbc", "\ue5e1"),      //c
                entry("\ud835\uddbd", "\ue5e2"),      //d
                entry("\ud835\uddbe", "\ue5e3"),      //e
                entry("\ud835\uddbf", "\ue5e4"),      //f
                entry("\ud835\uddc0", "\ue5e5"),      //g
                entry("\ud835\uddc1", "\ue5e6"),      //h
                entry("\ud835\uddc2", "\ue5e7"),      //i
                entry("\ud835\uddc3", "\ue5e8"),      //j
                entry("\ud835\uddc4", "\ue5e9"),      //k
                entry("\ud835\uddc5", "\ue5ea"),      //l
                entry("\ud835\uddc6", "\ue5eb"),      //m
                entry("\ud835\uddc7", "\ue5ec"),      //n
                entry("\ud835\uddc8", "\ue5ed"),      //o
                entry("\ud835\uddc9", "\ue5ee"),      //p
                entry("\ud835\uddca", "\ue5ef"),      //q
                entry("\ud835\uddcb", "\ue5f0"),      //r
                entry("\ud835\uddcc", "\ue5f1"),      //s
                entry("\ud835\uddcd", "\ue5f2"),      //t
                entry("\ud835\uddce", "\ue5f3"),      //u
                entry("\ud835\uddcf", "\ue5f4"),      //v
                entry("\ud835\uddd0", "\ue5f5"),      //w
                entry("\ud835\uddd1", "\ue5f6"),      //x
                entry("\ud835\uddd2", "\ue5f7"),      //y
                entry("\ud835\uddd3", "\ue5f8"),      //z
                entry("\ud835\udfe2", "\ue5f9"),      //0
                entry("\ud835\udfe3", "\ue5fa"),      //1
                entry("\ud835\udfe4", "\ue5fb"),      //2
                entry("\ud835\udfe5", "\ue5fc"),      //3
                entry("\ud835\udfe6", "\ue5fd"),      //4
                entry("\ud835\udfe7", "\ue5fe"),      //5
                entry("\ud835\udfe8", "\ue5ff"),      //6
                entry("\ud835\udfe9", "\ue600"),      //7
                entry("\ud835\udfea", "\ue601"),      //8
                entry("\ud835\udfeb", "\ue602"),      //9
                entry("\ud835\udda0", "\ue5c5"),      //A
                entry("\ud835\udda1", "\ue5c6"),      //B
                entry("\ud835\udda2", "\ue5c7"),      //C
                entry("\ud835\udda3", "\ue5c8"),      //D
                entry("\ud835\udda4", "\ue5c9"),      //E
                entry("\ud835\udda5", "\ue5ca"),      //F
                entry("\ud835\udda6", "\ue5cb"),      //G
                entry("\ud835\udda7", "\ue5cc"),      //H
                entry("\ud835\udda8", "\ue5cd"),      //I
                entry("\ud835\udda9", "\ue5ce"),      //J
                entry("\ud835\uddaa", "\ue5cf"),      //K
                entry("\ud835\uddab", "\ue5d0"),      //L
                entry("\ud835\uddac", "\ue5d1"),      //M
                entry("\ud835\uddad", "\ue5d2"),      //N
                entry("\ud835\uddae", "\ue5d3"),      //O
                entry("\ud835\uddaf", "\ue5d4"),      //P
                entry("\ud835\uddb0", "\ue5d5"),      //Q
                entry("\ud835\uddb1", "\ue5d6"),      //R
                entry("\ud835\uddb2", "\ue5d7"),      //S
                entry("\ud835\uddb3", "\ue5d8"),      //T
                entry("\ud835\uddb4", "\ue5d9"),      //U
                entry("\ud835\uddb5", "\ue5da"),      //V
                entry("\ud835\uddb6", "\ue5db"),      //W
                entry("\ud835\uddb7", "\ue5dc"),      //X
                entry("\ud835\uddb8", "\ue5dd"),      //Y
                entry("\ud835\uddb9", "\ue5de"),      //Z


                //Fraktur
                entry("\ud835\udd1e", "\ue618"),      //a
                entry("\ud835\udd1f", "\ue619"),      //b
                entry("\ud835\udd20", "\ue61a"),      //c
                entry("\ud835\udd21", "\ue61b"),      //d
                entry("\ud835\udd22", "\ue61c"),      //e
                entry("\ud835\udd23", "\ue61d"),      //f
                entry("\ud835\udd24", "\ue61e"),      //g
                entry("\ud835\udd25", "\ue61f"),      //h
                entry("\ud835\udd26", "\ue620"),      //i
                entry("\ud835\udd27", "\ue621"),      //j
                entry("\ud835\udd28", "\ue622"),      //k
                entry("\ud835\udd29", "\ue623"),      //l
                entry("\ud835\udd2a", "\ue624"),      //m
                entry("\ud835\udd2b", "\ue625"),      //n
                entry("\ud835\udd2c", "\ue626"),      //o
                entry("\ud835\udd2d", "\ue627"),      //p
                entry("\ud835\udd2e", "\ue628"),      //q
                entry("\ud835\udd2f", "\ue629"),      //r
                entry("\ud835\udd30", "\ue62a"),      //s
                entry("\ud835\udd31", "\ue62b"),      //t
                entry("\ud835\udd32", "\ue62c"),      //u
                entry("\ud835\udd33", "\ue62d"),      //v
                entry("\ud835\udd34", "\ue62e"),      //w
                entry("\ud835\udd35", "\ue62f"),      //x
                entry("\ud835\udd36", "\ue630"),      //y
                entry("\ud835\udd37", "\ue631"),      //z
                entry("\ud835\udd04", "\ue603"),      //A
                entry("\ud835\udd05", "\ue604"),      //B
                entry("\u212d", "\ue632"),            //C
                entry("\ud835\udd07", "\ue605"),      //D
                entry("\ud835\udd08", "\ue606"),      //E
                entry("\ud835\udd09", "\ue607"),      //F
                entry("\ud835\udd0a", "\ue608"),      //G
                entry("\u210c", "\ue633"),            //H
                entry("\u2111", "\ue634"),            //I
                entry("\ud835\udd0d", "\ue609"),      //J
                entry("\ud835\udd0e", "\ue60a"),      //K
                entry("\ud835\udd0f", "\ue60b"),      //L
                entry("\ud835\udd10", "\ue60c"),      //M
                entry("\ud835\udd11", "\ue60d"),      //N
                entry("\ud835\udd12", "\ue60e"),      //O
                entry("\ud835\udd13", "\ue60f"),      //P
                entry("\ud835\udd14", "\ue610"),      //Q
                entry("\u211c", "\ue635"),            //R
                entry("\ud835\udd16", "\ue611"),      //S
                entry("\ud835\udd17", "\ue612"),      //T
                entry("\ud835\udd18", "\ue613"),      //U
                entry("\ud835\udd19", "\ue614"),      //V
                entry("\ud835\udd1a", "\ue615"),      //W
                entry("\ud835\udd1b", "\ue616"),      //X
                entry("\ud835\udd1c", "\ue617"),      //Y
                entry("\u2128", "\ue636"),           //Z

                //Blackboard
                entry("\uf893", "\ue64a"),      //a
                entry("\uf894", "\ue64b"),      //b
                entry("\uf895", "\ue64c"),      //c
                entry("\uf896", "\ue64d"),      //d
                entry("\uf897", "\ue64e"),      //e
                entry("\uf898", "\ue64f"),      //f
                entry("\uf899", "\ue650"),      //g
                entry("\uf89a", "\ue651"),      //h
                entry("\uf89b", "\ue652"),      //i
                entry("\uf89c", "\ue653"),      //j
                entry("\uf89d", "\ue654"),      //k
                entry("\uf89e", "\ue655"),      //l
                entry("\uf89f", "\ue656"),      //m
                entry("\uf8a0", "\ue657"),      //n
                entry("\uf8a1", "\ue658"),      //o
                entry("\uf8a2", "\ue659"),      //p
                entry("\uf8a3", "\ue65a"),      //q
                entry("\uf8a4", "\ue65b"),      //r
                entry("\uf8a5", "\ue65c"),      //s
                entry("\uf8a6", "\ue65d"),      //t
                entry("\uf8a7", "\ue65e"),      //u
                entry("\uf8a8", "\ue65f"),      //v
                entry("\uf8a9", "\ue660"),      //w
                entry("\uf8aa", "\ue661"),      //x
                entry("\uf8ab", "\ue662"),      //y
                entry("\uf8ac", "\ue663"),      //z
                entry("\ud835\udfd8", "\ue66b"),      //0
                entry("\ud835\udfd9", "\ue66c"),      //1
                entry("\ud835\udfda", "\ue66d"),      //2
                entry("\ud835\udfdb", "\ue66e"),      //3
                entry("\ud835\udfdc", "\ue66f"),      //4
                entry("\ud835\udfdd", "\ue670"),      //5
                entry("\ud835\udfde", "\ue671"),      //6
                entry("\ud835\udfdf", "\ue672"),      //7
                entry("\ud835\udfe0", "\ue673"),      //8
                entry("\ud835\udfe1", "\ue674"),      //9
                entry("\uf880", "\ue637"),      //A
                entry("\uf881", "\ue638"),      //B
                entry("\uf8ad", "\ue664"),      //C
                entry("\uf882", "\ue639"),      //D
                entry("\uf883", "\ue63a"),      //E
                entry("\uf884", "\ue63b"),      //F
                entry("\uf885", "\ue63c"),      //G
                entry("\uf8ae", "\ue665"),      //H
                entry("\uf886", "\ue63d"),      //I
                entry("\uf887", "\ue63e"),      //J
                entry("\uf888", "\ue63f"),      //K
                entry("\uf889", "\ue640"),      //L
                entry("\uf88a", "\ue641"),      //M
                entry("\uf8af", "\ue666"),      //N
                entry("\uf88b", "\ue642"),      //O
                entry("\uf8b0", "\ue667"),      //P
                entry("\uf8b1", "\ue668"),      //Q
                entry("\uf8b2", "\ue669"),      //R
                entry("\uf88c", "\ue643"),      //S
                entry("\uf88d", "\ue644"),      //T
                entry("\uf88e", "\ue645"),      //U
                entry("\uf88f", "\ue646"),      //V
                entry("\uf890", "\ue647"),      //W
                entry("\uf891", "\ue648"),      //X
                entry("\uf892", "\ue649"),      //Y
                entry("\uf8b3", "\ue679"),      //Z

                //Special
                entry("\u00d8", "\ue895"),      //zero with slash
                entry("\u2135", "\ue67d"),      //aleph
                entry("\u2136", "\ue67e"),      //beth
                entry("\u00a0", "\u00a0")            //unbreakable space

        );
    }
    private Map<String, String> initializeHatMap() {
        return Map.ofEntries(

                //base map
                entry("a", "\ue6bf"),
                entry("b", "\ue6c0"),
                entry("c", "\ue6c1"),
                entry("d", "\ue6c2"),
                entry("e", "\ue6c3"),
                entry("f", "\ue6c4"),
                entry("g", "\ue6c5"),
                entry("h", "\ue6c6"),
                entry("i", "\ue6c7"),
                entry("j", "\ue6c8"),
                entry("k", "\ue6c9"),
                entry("l", "\ue6ca"),
                entry("m", "\ue6cb"),
                entry("n", "\ue6cc"),
                entry("o", "\ue6cd"),
                entry("p", "\ue6ce"),
                entry("q", "\ue6cf"),
                entry("r", "\ue6d0"),
                entry("s", "\ue6d1"),
                entry("t", "\ue6d2"),
                entry("u", "\ue6d3"),
                entry("v", "\ue6d4"),
                entry("w", "\ue6d5"),
                entry("x", "\ue6d6"),
                entry("y", "\ue6d7"),
                entry("z", "\ue6d8"),
                entry("A", "\ue6a5"),
                entry("B", "\ue6a6"),
                entry("C", "\ue6a7"),
                entry("D", "\ue6a8"),
                entry("E", "\ue6a9"),
                entry("F", "\ue6aa"),
                entry("G", "\ue6ab"),
                entry("H", "\ue6ac"),
                entry("I", "\ue6ad"),
                entry("J", "\ue6ae"),
                entry("K", "\ue6af"),
                entry("L", "\ue6b0"),
                entry("M", "\ue6b1"),
                entry("N", "\ue6b2"),
                entry("O", "\ue6b3"),
                entry("P", "\ue6b4"),
                entry("Q", "\ue6b5"),
                entry("R", "\ue6b6"),
                entry("S", "\ue6b7"),
                entry("T", "\ue6b8"),
                entry("U", "\ue6b9"),
                entry("V", "\ue6ba"),
                entry("W", "\ue6bb"),
                entry("X", "\ue6bc"),
                entry("Y", "\ue6bd"),
                entry("Z", "\ue6be"),
                entry("1", "\ue69c"),
                entry("2", "\ue69d"),
                entry("3", "\ue69e"),
                entry("4", "\ue69f"),
                entry("5", "\ue6a0"),
                entry("6", "\ue6a1"),
                entry("7", "\ue6a2"),
                entry("8", "\ue6a3"),
                entry("9", "\ue6a4"),
                entry("0", "\ue69b"),

                //italic (alt map)
                entry("\ud835\udc4e", "\ue6f3"),      //a
                entry("\ud835\udc4f", "\ue6f4"),      //b
                entry("\ud835\udc50", "\ue6f5"),      //c
                entry("\ud835\udc51", "\ue6f6"),      //d
                entry("\ud835\udc52", "\ue6f7"),      //e
                entry("\ud835\udc53", "\ue6f8"),      //f
                entry("\ud835\udc54", "\ue6f9"),      //g
                entry("\u210e", "\ue70c"),            //h
                entry("\ud835\udc56", "\ue6fa"),      //i
                entry("\ud835\udc57", "\ue6fb"),      //j
                entry("\ud835\udc58", "\ue6fc"),      //k
                entry("\ud835\udc59", "\ue6fd"),      //l
                entry("\ud835\udc5a", "\ue6fe"),      //m
                entry("\ud835\udc5b", "\ue6ff"),      //n
                entry("\ud835\udc5c", "\ue700"),      //o
                entry("\ud835\udc5d", "\ue701"),      //p
                entry("\ud835\udc5e", "\ue702"),      //q
                entry("\ud835\udc5f", "\ue703"),      //r
                entry("\ud835\udc60", "\ue704"),      //s
                entry("\ud835\udc61", "\ue705"),      //t
                entry("\ud835\udc62", "\ue706"),      //u
                entry("\ud835\udc63", "\ue707"),      //v
                entry("\ud835\udc64", "\ue708"),      //w
                entry("\ud835\udc65", "\ue709"),      //x
                entry("\ud835\udc66", "\ue70a"),      //y
                entry("\ud835\udc67", "\ue70b"),      //z
                entry("\u211d", "\ue82c"),            //bb R
                //               entry("\u2112", ""),            //script L
                entry("\u2115", "\ue828"),            //bb N
                entry("\u2124", "\ue829"),            //bb Z
                entry("\u2119", "\ue82a"),            //bb P
                entry("\u211a", "\ue82b"),            //bb Q
                entry("\ud835\udc34", "\ue6d9"),      //A
                entry("\ud835\udc35", "\ue6da"),      //B
                entry("\ud835\udc36", "\ue6db"),      //C
                entry("\ud835\udc37", "\ue6dc"),      //D
                entry("\ud835\udc38", "\ue6dd"),      //E
                entry("\ud835\udc39", "\ue6de"),      //F
                entry("\ud835\udc3a", "\ue6df"),      //G
                entry("\ud835\udc3b", "\ue6e0"),      //H
                entry("\ud835\udc3c", "\ue6e1"),      //I
                entry("\ud835\udc3d", "\ue6e2"),      //J
                entry("\ud835\udc3e", "\ue6e3"),      //K
                entry("\ud835\udc3f", "\ue6e4"),      //L
                entry("\ud835\udc40", "\ue6e5"),      //M
                entry("\ud835\udc41", "\ue6e6"),      //N
                entry("\ud835\udc42", "\ue6e7"),      //O
                entry("\ud835\udc43", "\ue6e8"),      //P
                entry("\ud835\udc44", "\ue6e9"),      //Q
                entry("\ud835\udc45", "\ue6ea"),      //R
                entry("\ud835\udc46", "\ue6eb"),      //S
                entry("\ud835\udc47", "\ue6ec"),      //T
                entry("\ud835\udc48", "\ue6ed"),      //U
                entry("\ud835\udc49", "\ue6ee"),      //V
                entry("\ud835\udc4a", "\ue6ef"),      //W
                entry("\ud835\udc4b", "\ue6f0"),      //X
                entry("\ud835\udc4c", "\ue6f1"),      //Y
                entry("\ud835\udc4d", "\ue6f2"),      //Z


                //script map
                entry("\ud835\udcb6", "\ue720"),       //a
                entry("\ud835\udcb7", "\ue721"),       //b
                entry("\ud835\udcb8", "\ue722"),       //c
                entry("\ud835\udcb9", "\ue723"),       //d
                entry("\u212f", "\ue737"),             //e
                entry("\ud835\udcbb", "\ue724"),       //f
                entry("\u210a", "\ue738"),             //g
                entry("\ud835\udcbd", "\ue725"),       //h
                entry("\ud835\udcbe", "\ue726"),       //i
                entry("\ud835\udcbf", "\ue727"),       //j
                entry("\ud835\udcc0", "\ue728"),       //k
                entry("\ud835\udcc1", "\ue729"),       //l
                entry("\ud835\udcc2", "\ue72a"),       //m
                entry("\ud835\udcc3", "\ue72b"),       //n
                entry("\u2134", "\ue739"),             //o
                entry("\ud835\udcc5", "\ue72c"),       //p
                entry("\ud835\udcc6", "\ue72d"),       //q
                entry("\ud835\udcc7", "\ue72e"),       //r
                entry("\ud835\udcc8", "\ue72f"),       //s
                entry("\ud835\udcc9", "\ue730"),       //t
                entry("\ud835\udcca", "\ue731"),       //u
                entry("\ud835\udccb", "\ue732"),       //v
                entry("\ud835\udccc", "\ue733"),       //w
                entry("\ud835\udccd", "\ue734"),       //x
                entry("\ud835\udcce", "\ue735"),       //y
                entry("\ud835\udccf", "\ue736"),       //z
                entry("\ud835\udc9c", "\ue70e"),       //A
                entry("\u212c", "\ue73a"),             //B
                entry("\ud835\udc9e", "\ue70f"),       //C
                entry("\ud835\udc9f", "\ue710"),       //D
                entry("\u2130", "\ue73b"),             //E
                entry("\u2131", "\ue73c"),             //F
                entry("\ud835\udca2", "\ue711"),       //G
                entry("\u210b", "\ue73d"),             //H
                entry("\u2110", "\ue73e"),             //I
                entry("\ud835\udca5", "\ue712"),       //J
                entry("\ud835\udca6", "\ue713"),       //K
                entry("\u2112", "\ue73f"),             //L
                entry("\u2133", "\ue740"),             //M
                entry("\ud835\udca9", "\ue714"),       //N
                entry("\ud835\udcaa", "\ue715"),       //O
                entry("\ud835\udcab", "\ue716"),       //P
                entry("\ud835\udcac", "\ue717"),       //Q
                entry("\u211b", "\ue741"),             //R
                entry("\ud835\udcae", "\ue718"),       //S
                entry("\ud835\udcaf", "\ue719"),       //T
                entry("\ud835\udcb0", "\ue71a"),       //U
                entry("\ud835\udcb1", "\ue71b"),       //V
                entry("\ud835\udcb2", "\ue71c"),       //W
                entry("\ud835\udcb3", "\ue71d"),       //X
                entry("\ud835\udcb4", "\ue71e"),       //Y
                entry("\ud835\udcb5", "\ue71f"),       //Z

                //Greek
                entry("\u03b1", "\ue75a"),      //alpha
                entry("\u03b2", "\ue75b"),      //beta
                entry("\u03b3", "\ue75c"),      //gamma
                entry("\u03b4", "\ue75d"),      //delta
                entry("\u03b5", "\ue75e"),      //epsilon
                entry("\u03b6", "\ue75f"),      //zeta
                entry("\u03b7", "\ue760"),      //eta
                entry("\u03b8", "\ue761"),      //theta
                entry("\u03b9", "\ue762"),      //iota
                entry("\u03ba", "\ue763"),      //kappa
                entry("\u03bb", "\ue764"),      //lambda
                entry("\u03bc", "\ue765"),      //mu
                entry("\u03bd", "\ue766"),      //nu
                entry("\u03be", "\ue767"),      //xi
                entry("\u03bf", "\ue768"),      //omicron
                entry("\u03c0", "\ue769"),      //pi
                entry("\u03c1", "\ue76a"),      //rho
                entry("\u03c2", "\ue76b"),      //end sigma
                entry("\u03c3", "\ue76c"),      //sigma
                entry("\u03c4", "\ue76d"),      //tau
                entry("\u03c5", "\ue76e"),      //upsilon
                entry("\u03c6", "\ue76f"),      //phi
                entry("\u03c7", "\ue770"),      //chi
                entry("\u03c8", "\ue771"),      //psi
                entry("\u03c9", "\ue772"),      //omega
                entry("\uf8b4", "\ue773"),      //slanted beta
                entry("\uf8b5", "\ue774"),      //slanted mu
                entry("\u0391", "\ue742"),      //Alpha
                entry("\u0392", "\ue743"),      //Beta
                entry("\u0393", "\ue744"),      //Gamma
                entry("\u0394", "\ue745"),      //Delta
                entry("\u0395", "\ue746"),      //Epsilon
                entry("\u0396", "\ue747"),      //Zeta
                entry("\u0397", "\ue748"),      //Eta
                entry("\u0398", "\ue749"),      //Theta
                entry("\u0399", "\ue74a"),      //Iota
                entry("\u039a", "\ue74b"),      //Kappa
                entry("\u039b", "\ue74c"),      //Lambda
                entry("\u039c", "\ue74d"),      //Mu
                entry("\u039d", "\ue74e"),      //Nu
                entry("\u039e", "\ue74f"),      //Xi
                entry("\u039f", "\ue750"),      //Omicron
                entry("\u03a0", "\ue751"),      //Pi
                entry("\u03a1", "\ue752"),      //Rho
                entry("\u03a3", "\ue753"),      //Sigma
                entry("\u03a4", "\ue754"),      //Tau
                entry("\u03a5", "\ue755"),      //Upsilon
                entry("\u03a6", "\ue756"),      //Phi
                entry("\u03a7", "\ue757"),      //Chi
                entry("\u03a8", "\ue758"),      //Psi
                entry("\u03a9", "\ue759"),      //Omega
                entry("\uf8d5", "\ue827"),  //double mu

                //Sans
                entry("\ud835\uddba", "\ue78f"),      //a
                entry("\ud835\uddbb", "\ue790"),      //b
                entry("\ud835\uddbc", "\ue791"),      //c
                entry("\ud835\uddbd", "\ue792"),      //d
                entry("\ud835\uddbe", "\ue793"),      //e
                entry("\ud835\uddbf", "\ue794"),      //f
                entry("\ud835\uddc0", "\ue795"),      //g
                entry("\ud835\uddc1", "\ue796"),      //h
                entry("\ud835\uddc2", "\ue797"),      //i
                entry("\ud835\uddc3", "\ue798"),      //j
                entry("\ud835\uddc4", "\ue799"),      //k
                entry("\ud835\uddc5", "\ue79a"),      //l
                entry("\ud835\uddc6", "\ue79b"),      //m
                entry("\ud835\uddc7", "\ue79c"),      //n
                entry("\ud835\uddc8", "\ue79d"),      //o
                entry("\ud835\uddc9", "\ue79e"),      //p
                entry("\ud835\uddca", "\ue79f"),      //q
                entry("\ud835\uddcb", "\ue7a0"),      //r
                entry("\ud835\uddcc", "\ue7a1"),      //s
                entry("\ud835\uddcd", "\ue7a2"),      //t
                entry("\ud835\uddce", "\ue7a3"),      //u
                entry("\ud835\uddcf", "\ue7a4"),      //v
                entry("\ud835\uddd0", "\ue7a5"),      //w
                entry("\ud835\uddd1", "\ue7a6"),      //x
                entry("\ud835\uddd2", "\ue7a7"),       //y
                entry("\ud835\uddd3", "\ue7a8"),      //z
                entry("\ud835\udfe2", "\ue7a9"),      //0
                entry("\ud835\udfe3", "\ue7aa"),      //1
                entry("\ud835\udfe4", "\ue7ab"),      //2
                entry("\ud835\udfe5", "\ue7ac"),      //3
                entry("\ud835\udfe6", "\ue7ad"),      //4
                entry("\ud835\udfe7", "\ue7ae"),      //5
                entry("\ud835\udfe8", "\ue7af"),      //6
                entry("\ud835\udfe9", "\ue7b0"),      //7
                entry("\ud835\udfea", "\ue7b1"),      //8
                entry("\ud835\udfeb", "\ue7b2"),      //9
                entry("\ud835\udda0", "\ue775"),      //A
                entry("\ud835\udda1", "\ue776"),      //B
                entry("\ud835\udda2", "\ue777"),      //C
                entry("\ud835\udda3", "\ue778"),      //D
                entry("\ud835\udda4", "\ue779"),      //E
                entry("\ud835\udda5", "\ue77a"),      //F
                entry("\ud835\udda6", "\ue77b"),      //G
                entry("\ud835\udda7", "\ue77c"),      //H
                entry("\ud835\udda8", "\ue77d"),      //I
                entry("\ud835\udda9", "\ue77e"),      //J
                entry("\ud835\uddaa", "\ue77f"),      //K
                entry("\ud835\uddab", "\ue780"),      //L
                entry("\ud835\uddac", "\ue781"),      //M
                entry("\ud835\uddad", "\ue782"),      //N
                entry("\ud835\uddae", "\ue783"),      //O
                entry("\ud835\uddaf", "\ue784"),      //P
                entry("\ud835\uddb0", "\ue785"),      //Q
                entry("\ud835\uddb1", "\ue786"),      //R
                entry("\ud835\uddb2", "\ue787"),      //S
                entry("\ud835\uddb3", "\ue788"),      //T
                entry("\ud835\uddb4", "\ue789"),      //U
                entry("\ud835\uddb5", "\ue78a"),      //V
                entry("\ud835\uddb6", "\ue78b"),      //W
                entry("\ud835\uddb7", "\ue78c"),      //X
                entry("\ud835\uddb8", "\ue78d"),      //Y
                entry("\ud835\uddb9", "\ue78e"),      //Z


                //Fraktur
                entry("\ud835\udd1e", "\ue7c8"),      //a
                entry("\ud835\udd1f", "\ue7c9"),      //b
                entry("\ud835\udd20", "\ue7ca"),      //c
                entry("\ud835\udd21", "\ue7cb"),      //d
                entry("\ud835\udd22", "\ue7cc"),      //e
                entry("\ud835\udd23", "\ue7cd"),      //f
                entry("\ud835\udd24", "\ue7ce"),      //g
                entry("\ud835\udd25", "\ue7cf"),      //h
                entry("\ud835\udd26", "\ue7d0"),      //i
                entry("\ud835\udd27", "\ue7d1"),      //j
                entry("\ud835\udd28", "\ue7d2"),      //k
                entry("\ud835\udd29", "\ue7d3"),      //l
                entry("\ud835\udd2a", "\ue7d4"),      //m
                entry("\ud835\udd2b", "\ue7d5"),      //n
                entry("\ud835\udd2c", "\ue7d6"),      //o
                entry("\ud835\udd2d", "\ue7d7"),      //p
                entry("\ud835\udd2e", "\ue7d8"),      //q
                entry("\ud835\udd2f", "\ue7d9"),      //r
                entry("\ud835\udd30", "\ue7da"),      //s
                entry("\ud835\udd31", "\ue7db"),      //t
                entry("\ud835\udd32", "\ue7dc"),      //u
                entry("\ud835\udd33", "\ue7dd"),      //v
                entry("\ud835\udd34", "\ue7de"),      //w
                entry("\ud835\udd35", "\ue7df"),      //x
                entry("\ud835\udd36", "\ue7e0"),      //y
                entry("\ud835\udd37", "\ue7e1"),      //z
                entry("\ud835\udd04", "\ue7b3"),      //A
                entry("\ud835\udd05", "\ue7b4"),      //B
                entry("\u212d", "\ue7e2"),            //C
                entry("\ud835\udd07", "\ue7b5"),      //D
                entry("\ud835\udd08", "\ue7b6"),      //E
                entry("\ud835\udd09", "\ue7b7"),      //F
                entry("\ud835\udd0a", "\ue7b8"),      //G
                entry("\u210c", "\ue7e3"),            //H
                entry("\u2111", "\ue7e4"),            //I
                entry("\ud835\udd0d", "\ue7b9"),      //J
                entry("\ud835\udd0e", "\ue7ba"),      //K
                entry("\ud835\udd0f", "\ue7bb"),      //L
                entry("\ud835\udd10", "\ue7bc"),      //M
                entry("\ud835\udd11", "\ue7bd"),      //N
                entry("\ud835\udd12", "\ue7be"),      //O
                entry("\ud835\udd13", "\ue7bf"),      //P
                entry("\ud835\udd14", "\ue7c0"),      //Q
                entry("\u211c", "\ue7e5"),            //R
                entry("\ud835\udd16", "\ue7c1"),      //S
                entry("\ud835\udd17", "\ue7c2"),      //T
                entry("\ud835\udd18", "\ue7c3"),      //U
                entry("\ud835\udd19", "\ue7c4"),      //V
                entry("\ud835\udd1a", "\ue7c5"),      //W
                entry("\ud835\udd1b", "\ue7c6"),      //X
                entry("\ud835\udd1c", "\ue7c7"),      //Y
                entry("\u2128", "\ue7e6"),           //Z

                //Blackboard
                entry("\uf893", "\ue7fa"),      //a
                entry("\uf894", "\ue7fb"),      //b
                entry("\uf895", "\ue7fc"),      //c
                entry("\uf896", "\ue7fd"),      //d
                entry("\uf897", "\ue7fe"),      //e
                entry("\uf898", "\ue7ff"),      //f
                entry("\uf899", "\ue800"),      //g
                entry("\uf89a", "\ue801"),      //h
                entry("\uf89b", "\ue802"),      //i
                entry("\uf89c", "\ue803"),      //j
                entry("\uf89d", "\ue804"),      //k
                entry("\uf89e", "\ue805"),      //l
                entry("\uf89f", "\ue806"),      //m
                entry("\uf8a0", "\ue807"),      //n
                entry("\uf8a1", "\ue808"),      //o
                entry("\uf8a2", "\ue809"),      //p
                entry("\uf8a3", "\ue80a"),      //q
                entry("\uf8a4", "\ue80b"),      //r
                entry("\uf8a5", "\ue80c"),      //s
                entry("\uf8a6", "\ue80d"),      //t
                entry("\uf8a7", "\ue80e"),      //u
                entry("\uf8a8", "\ue80f"),      //v
                entry("\uf8a9", "\ue810"),      //w
                entry("\uf8aa", "\ue811"),      //x
                entry("\uf8ab", "\ue812"),      //y
                entry("\uf8ac", "\ue813"),      //z
                entry("\ud835\udfd8", "\ue81b"),      //0
                entry("\ud835\udfd9", "\ue81c"),      //1
                entry("\ud835\udfda", "\ue81d"),      //2
                entry("\ud835\udfdb", "\ue81e"),      //3
                entry("\ud835\udfdc", "\ue81f"),      //4
                entry("\ud835\udfdd", "\ue820"),      //5
                entry("\ud835\udfde", "\ue821"),      //6
                entry("\ud835\udfdf", "\ue822"),      //7
                entry("\ud835\udfe0", "\ue823"),      //8
                entry("\ud835\udfe1", "\ue824"),      //9
                entry("\uf880", "\ue7e7"),      //A
                entry("\uf881", "\ue7e8"),      //B
                entry("\uf8ad", "\ue814"),      //C
                entry("\uf882", "\ue7e9"),      //D
                entry("\uf883", "\ue7ea"),      //E
                entry("\uf884", "\ue7eb"),      //F
                entry("\uf885", "\ue7ec"),      //G
                entry("\uf8ae", "\ue815"),      //H
                entry("\uf886", "\ue7ed"),      //I
                entry("\uf887", "\ue7ee"),      //J
                entry("\uf888", "\ue7ef"),      //K
                entry("\uf889", "\ue7f0"),      //L
                entry("\uf88a", "\ue7f1"),      //M
                entry("\uf8af", "\ue816"),      //N
                entry("\uf88b", "\ue7f2"),      //O
                entry("\uf8b0", "\ue817"),      //P
                entry("\uf8b1", "\ue818"),      //Q
                entry("\uf8b2", "\ue819"),      //R
                entry("\uf88c", "\ue7f3"),      //S
                entry("\uf88d", "\ue7f4"),      //T
                entry("\uf88e", "\ue7f5"),      //U
                entry("\uf88f", "\ue7f6"),      //V
                entry("\uf890", "\ue7f7"),      //W
                entry("\uf891", "\ue7f8"),      //X
                entry("\uf892", "\ue7f9"),      //Y
                entry("\uf8b3", "\ue81a"),      //Z

                //Special
                entry("\u00d8", "\ue897"),      //zero with slash
                entry("\u2135", "\ue82d"),      //aleph
                entry("\u2136", "\ue82e"),      //beth
                entry("\u00a0", "\u00a0")            //unbreakable space


        );
    }
    private Map<String, String> initializeSlashBaseItalMap() {
        return Map.ofEntries(

                //base
                entry("b", "\uf8b4"),
                entry("c", "\ue845"), //
                entry("i", "\ue849"),  //
                entry("m", "\uf8b5"),
                entry("n","\u03bc"),
                entry("s", "\ue843"), //
                entry("u", "\ue847"),  //
                entry("v", "\u03b2"),
                entry("A", "\u2135"), //
                entry("B", "\u2136"), //
                entry("C", "\ue844"), //
                entry("I", "\ue848"), //
                entry("N", "\u2115"), //
                entry("P", "\u2119"), //
                entry("Q", "\u211a"),  //
                entry("R", "\u211d"),  //
                entry("S", "\ue842"), //
                entry("U", "\ue846"), //
                entry("Z", "\u2124"), //
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //italic
                entry("\ud835\udc50", "\ue855"),      //c  *
                entry("\ud835\udc56", "\ue859"),      //i  *
                entry("\ud835\udc5a", "\uf8d5"),      //m  *
                entry("\ud835\udc5b", "\uf8db"),      //n  *
                entry("\ud835\udc60", "\ue853"),      //s  *
                entry("\ud835\udc62", "\ue857"),      //u  *
                entry("\ud835\udc36", "\ue854"),      //C  *
                entry("\ud835\udc3c", "\ue858"),      //I  *
                entry("\ud835\udc43", "\ud835\udcab"),  //P  *
                entry("\ud835\udc46", "\ue852"),      //S  *
                entry("\ud835\udc48", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),      //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space
        );
    }



    private Map<String, String> initializeSlashBaseScriptMap() {
        return Map.ofEntries(

                //base
                entry("b", "\uf8b4"),
                entry("c", "\ue845"), //
                entry("i", "\ue849"),  //
                entry("m", "\uf8b5"),
                entry("n","\u03bc"),
                entry("s", "\ue843"), //
                entry("u", "\ue847"),  //
                entry("v", "\u03b2"),
                entry("A", "\u2135"), //
                entry("B", "\u2136"), //
                entry("C", "\ue844"), //
                entry("I", "\ue848"), //
                entry("N", "\u2115"), //
                entry("P", "\u2119"), //
                entry("Q", "\u211a"),  //
                entry("R", "\u211d"),  //
                entry("S", "\ue842"), //
                entry("U", "\ue846"), //
                entry("Z", "\u2124"), //
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //script
                entry("\ud835\udcb8", "\ue855"),      //c  *
                entry("\ud835\udcbe", "\ue859"),      //i  *
                entry("\ud835\udcc2", "\uf8d5"),      //m  *
                entry("\ud835\udcc3", "\uf8db"),      //n  *
                entry("\ud835\udcc8", "\ue853"),      //s  *
                entry("\ud835\udcca", "\ue857"),      //u  *
                entry("\ud835\udc9e", "\ue854"),      //C  *
                entry("\u2110", "\ue858"),      //I  *
                entry("\ud835\udcab", "\ud835\udcab"),  //P  *
                entry("\ud835\udcae", "\ue852"),      //S  *
                entry("\ud835\udcb0", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),      //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space

        );
    }

    private Map<String, String> initializeSlashBaseSansMap() {
        return Map.ofEntries(

                //base
                entry("b", "\uf8b4"),
                entry("c", "\ue845"), //
                entry("i", "\ue849"),  //
                entry("m", "\uf8b5"),
                entry("n","\u03bc"),
                entry("s", "\ue843"), //
                entry("u", "\ue847"),  //
                entry("v", "\u03b2"),
                entry("A", "\u2135"), //
                entry("B", "\u2136"), //
                entry("C", "\ue844"), //
                entry("I", "\ue848"), //
                entry("N", "\u2115"), //
                entry("P", "\u2119"), //
                entry("Q", "\u211a"),  //
                entry("R", "\u211d"),  //
                entry("S", "\ue842"), //
                entry("U", "\ue846"), //
                entry("Z", "\u2124"), //
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //sans
                entry("\ud835\uddbc", "\ue855"),      //c  *
                entry("\ud835\uddc2", "\ue859"),      //i  *
                entry("\ud835\uddc6", "\uf8d5"),      //m  *
                entry("\ud835\uddc7", "\uf8db"),      //n  *
                entry("\ud835\uddcc", "\ue853"),      //s  *
                entry("\ud835\uddce", "\ue857"),      //u  *
                entry("\ud835\udda2", "\ue854"),      //C  *
                entry("\ud835\udda8", "\ue858"),      //I  *
                entry("\ud835\uddaf", "\ud835\udcab"),  //P  *
                entry("\ud835\uddb2", "\ue852"),      //S  *
                entry("\ud835\uddb4", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),      //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space
        );
    }

    private Map<String, String> initializeSlashItalSansMap() {
        return Map.ofEntries(

                //ital
                entry("\ud835\udc4f", "\uf8b4"),
                entry("\ud835\udc50", "\ue845"), //c
                entry("\ud835\udc56", "\ue849"),  //i
                entry("\ud835\udc5a", "\uf8b5"),
                entry("\ud835\udc5b", "\u03bc"),
                entry("\ud835\udc60", "\ue843"), //s
                entry("\ud835\udc62", "\ue847"),  //u
                entry("\ud835\udc63", "\u03b2"),

                entry("\ud835\udc34", "\u2135"), //A
                entry("\ud835\udc35", "\u2136"), //B
                entry("\ud835\udc36", "\ue844"), //C
                entry("\ud835\udc3c", "\ue848"), //I
                entry("\ud835\udc41", "\u2115"), //N
                entry("\ud835\udc43", "\u2119"), //P
                entry("\ud835\udc44", "\u211a"),  //Q
                entry("\ud835\udc45", "\u211d"),  //R
                entry("\ud835\udc46", "\ue842"), //S
                entry("\ud835\udc48", "\ue846"), //U
                entry("\ud835\udc4d", "\u2124"), //Z
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //sans
                entry("\ud835\uddbc", "\ue855"),      //c  *
                entry("\ud835\uddc2", "\ue859"),      //i  *
                entry("\ud835\uddc6", "\uf8d5"),      //m  *
                entry("\ud835\uddc7", "\uf8db"),      //n  *
                entry("\ud835\uddcc", "\ue853"),      //s  *
                entry("\ud835\uddce", "\ue857"),      //u  *
                entry("\ud835\udda2", "\ue854"),      //C  *
                entry("\ud835\udda8", "\ue858"),      //I  *
                entry("\ud835\uddaf", "\ud835\udcab"),  //P  *
                entry("\ud835\uddb2", "\ue852"),      //S  *
                entry("\ud835\uddb4", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),      //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space

        );
    }

    private Map<String, String> initializeSlashItalBBMap() {
        return Map.ofEntries(

                //ital
                entry("\ud835\udc4f", "\uf8b4"),
                entry("\ud835\udc50", "\ue845"), //c
                entry("\ud835\udc56", "\ue849"),  //i
                entry("\ud835\udc5a", "\uf8b5"),
                entry("\ud835\udc5b", "\u03bc"),
                entry("\ud835\udc60", "\ue843"), //s
                entry("\ud835\udc62", "\ue847"),  //u
                entry("\ud835\udc63", "\u03b2"),
                entry("\ud835\udc34", "\u2135"), //A
                entry("\ud835\udc35", "\u2136"), //B
                entry("\ud835\udc36", "\ue844"), //C
                entry("\ud835\udc3c", "\ue848"), //I
                entry("\ud835\udc41", "\u2115"), //N
                entry("\ud835\udc43", "\u2119"), //P
                entry("\ud835\udc44", "\u211a"),  //Q
                entry("\ud835\udc45", "\u211d"),  //R
                entry("\ud835\udc46", "\ue842"), //S
                entry("\ud835\udc48", "\ue846"), //U
                entry("\ud835\udc4d", "\u2124"), //Z
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //bb
                entry("\uf895", "\ue855"),      //c  *
                entry("\uf89b", "\ue859"),      //i  *
                entry("\uf89f", "\uf8d5"),      //m  *
                entry("\uf8a0", "\uf8db"),      //n  *
                entry("\uf8a5", "\ue853"),      //s  *
                entry("\uf8a7", "\ue857"),      //u  *
                entry("\uf8ad", "\ue854"),      //C  *
                entry("\uf886", "\ue858"),      //I  *
                entry("\uf8b0", "\ud835\udcab"),  //P  *
                entry("\uf88c", "\ue852"),      //S  *
                entry("\uf88e", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),      //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space

        );
    }

    private Map<String, String> initializeSlashScriptItalMap() {
        return Map.ofEntries(

                //script
                entry("\ud835\udcb7", "\uf8b4"),  //b
                entry("\ud835\udcb8", "\ue845"), //c
                entry("\ud835\udcbe", "\ue849"),  //i
                entry("\ud835\udcc2", "\uf8b5"),  //m
                entry("\ud835\udcc3", "\u03bc"), //n
                entry("\ud835\udcc8", "\ue843"), //s
                entry("\ud835\udcca", "\ue847"),  //u
                entry("\ud835\udccb", "\u03b2"), //v
                entry("\ud835\udc9c", "\u2135"), // A
                entry("\u212c", "\u2136"), //  B
                entry("\ud835\udc9e", "\ue844"), //
                entry("\u2110", "\ue848"), //
                entry("\ud835\udca9", "\u2115"), //
                entry("\ud835\udcab", "\u2119"), //
                entry("\ud835\udcac", "\u211a"),  //
                entry("\u211b", "\u211d"),  //
                entry("\ud835\udcae", "\ue842"), //
                entry("\ud835\udcb0", "\ue846"), //
                entry("\ud835\udcb5", "\u2124"), //
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //italic
                entry("\ud835\udc50", "\ue855"),      //c  *
                entry("\ud835\udc56", "\ue859"),      //i  *
                entry("\ud835\udc5a", "\uf8d5"),      //m  *
                entry("\ud835\udc5b", "\uf8db"),      //n  *
                entry("\ud835\udc60", "\ue853"),      //s  *
                entry("\ud835\udc62", "\ue857"),      //u  *
                entry("\ud835\udc36", "\ue854"),      //C  *
                entry("\ud835\udc3c", "\ue858"),      //I  *
                entry("\ud835\udc43", "\ud835\udcab"),  //P  *
                entry("\ud835\udc46", "\ue852"),      //S  *
                entry("\ud835\udc48", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),     //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space

        );
    }

    private Map<String, String> initializeSlashScriptSansMap() {
        return Map.ofEntries(

                //script
                entry("\ud835\udcb7", "\uf8b4"),  //b
                entry("\ud835\udcb8", "\ue845"), //c
                entry("\ud835\udcbe", "\ue849"),  //i
                entry("\ud835\udcc2", "\uf8b5"),  //m
                entry("\ud835\udcc3", "\u03bc"), //n
                entry("\ud835\udcc8", "\ue843"), //s
                entry("\ud835\udcca", "\ue847"),  //u
                entry("\ud835\udccb", "\u03b2"), //v
                entry("\ud835\udc9c", "\u2135"), //
                entry("\u212c", "\u2136"), //
                entry("\ud835\udc9e", "\ue844"), //
                entry("\u2110", "\ue848"), //
                entry("\ud835\udca9", "\u2115"), //
                entry("\ud835\udcab", "\u2119"), //
                entry("\ud835\udcac", "\u211a"),  //
                entry("\u211b", "\u211d"),  //
                entry("\ud835\udcae", "\ue842"), //
                entry("\ud835\udcb0", "\ue846"), //
                entry("\ud835\udcb5", "\u2124"), //
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //sans
                entry("\ud835\uddbc", "\ue855"),      //c  *
                entry("\ud835\uddc2", "\ue859"),      //i  *
                entry("\ud835\uddc6", "\uf8d5"),      //m  *
                entry("\ud835\uddc7", "\uf8db"),      //n  *
                entry("\ud835\uddcc", "\ue853"),      //s  *
                entry("\ud835\uddce", "\ue857"),      //u  *
                entry("\ud835\udda2", "\ue854"),      //C  *
                entry("\ud835\udda8", "\ue858"),      //I  *
                entry("\ud835\uddaf", "\ud835\udcab"),  //P  *
                entry("\ud835\uddb2", "\ue852"),      //S  *
                entry("\ud835\uddb4", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),      //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space

        );
    }

    private Map<String, String> initializeSlashGreekFrakMap() {
        return Map.ofEntries(

                entry("\u03b2", "\uf8b4"),  //b
                entry("\u03c8", "\ue845"), //c
                entry("\u03b9", "\ue849"),  //i
                entry("\u03bd", "\u03bc"),  //n
                entry("\u03bc", "\uf8b5"),  //m
                entry("\u03c3", "\ue843"), //s
                entry("\u03b8", "\ue847"),  //u
                entry("\u03c9", "\u03b2"),  //v
                entry("\u0391", "\u2135"), //A
                entry("\u0392", "\u2136"), //B
                entry("\u03a8", "\ue844"), //C
                entry("\u0399", "\ue848"), //I
                entry("\u039d", "\u2115"), //N
                entry("\u03a0", "\u2119"), //P
                entry("\uf8d5", "\u211a"),  //Q
                entry("\u03a1", "\u211d"),  //R
                entry("\u03a3", "\ue842"), //S
                entry("\u0398", "\ue846"), //U
                entry("\u0396", "\u2124"), //Z
                entry("-", "\u21d0"),  //
                entry("=", "\u27f8"),  //
                entry("[", "\u27f9"), //
                entry("]", "\u27fa"), //
                entry("\\", "\uf8d0"),
                entry("_", "\ue872"), //
                entry("+", "\ue873"),  //
                entry("{", "\ue874"), //
                entry("}", "\ue875"), //
                entry("|", "\ue898"), //
                entry("<", "\u226e"), //
                entry(">", "\u226f"), //

                //frak
                entry("\ud835\udd20", "\ue855"),      //c  *
                entry("\ud835\udd26", "\ue859"),      //i  *
                entry("\ud835\udd2a", "\uf8d5"),      //m  *
                entry("\ud835\udd2b", "\uf8db"),      //n  *
                entry("\ud835\udd30", "\ue853"),      //s  *
                entry("\ud835\udd32", "\ue857"),      //u  *
                entry("\u212d", "\ue854"),      //C  *
                entry("\u2111", "\ue858"),      //I  *
                entry("\ud835\udd13", "\ud835\udcab"),  //P  *
                entry("\ud835\udd16", "\ue852"),      //S  *
                entry("\ud835\udd18", "\ue856"),      //U  *

                //Special
                entry("\u21d2", "\ue876"),      //meta arrow
                entry("\u21d4", "\ue877"),      //meta double
                entry("\u2200", "\uf8d3"),      //universal
                entry("\u2203", "\uf8d4"),      //existential
                entry("\u00d7", "\u00b7"),       //times
                entry("\ue8ba", "\u2224"),      //big times
                entry("\u2264", "\u2270"),      //less than or equal
                entry("\u2265", "\u2271"),      //greater than or equal
                entry("\ue8a6", "\u22ee"),        //big less than or equal
                entry("\u2261", "\u2262"),      //triple bar
                entry("\u2283", "\u2285"),      //horseshoe
                entry("\u2282", "\u2284"),      //proper subset
                entry("\u2286", "\u2288"),      //subset
                entry("\u2248", "\ue87d"),      //double wave
                entry("\u2243", "\ue87e"),      //wave above line
                entry("\u2245", "\ue87f"),      //wave above equals
                entry("\u2291", "\ue880"),      //submodel
                entry("\uf8d1", "\ue881"),      //embedding
                entry("\u227a", "\ue882"),      //set smaller than
                entry("\uf8d2", "\ue883"),      //elementary submodel
                entry("\u227e", "\ue884"),      //elementary embedding
                entry("\u227c", "\ue885"),      //set less than or equal
                entry("\u00a0", "\u00a0")            //unbreakable space

        );
    }



    public Map<String, String> getOverlineMap() {
        return overlineMap;
    }

    public Map<String, String> getMacMap() {
        return macMap;
    }

    public Map<String, String> getVecMap() {
        return vecMap;
    }

    public Map<String, String> getHatMap() {
        return hatMap;
    }

    public Map<String, String> getSlashBaseItalMap() { return slashBaseItalMap;  }

    public Map<String, String> getSlashBaseScriptMap() {     return slashBaseScriptMap;  }

    public Map<String, String> getSlashBaseSansMap() {     return slashBaseSansMap;  }

    public Map<String, String> getSlashItalSansMap() {     return slashItalSansMap;  }

    public Map<String, String> getSlashItalBBMap() {     return slashItalBBMap;  }

    public Map<String, String> getSlashScriptItalMap() {    return slashScriptItalMap;   }

    public Map<String, String> getSlashScriptSansMap() {     return slashScriptSansMap;  }

    public Map<String, String> getSlashGreekFrakMap() {     return slashGreekFrakMap;  }
}
