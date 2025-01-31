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
        normalL = List.of(97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122);  //a - z regular
        normalU = List.of(65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90); //A - Z regular
        normalN = List.of(48,49,50,51,52,53,54,55,56,57); // 0 - 9 regular
        italL = List.of(119886,119887,119888,119889,119890,119891,119892,8462,119894,119895,119896,119897,119898,119899,119900,119901,119902,119903,119904,119905,119906,119907,119908,119909,119910,119911); //a - z italic
        italU = List.of(119860,119861,119862,119863,119864,119865,119866,119867,119868,119869,119870,119871,119872,119873,119874,119875,119876,119877,119878,119879,119880,119881,119882,119883,119884,119885); //A - Z italic
        sansL = List.of(120250,120251,120252,120253,120254,120255,120256,120257,120258,120259,120260,120261,120262,120263,120264,120265,120266,120267,120268,120269,120270,120271,120272,120273,120274,120275); //a - z sans
        sansU = List.of(120224,120225,120226,120227,120228,120229,120230,120231,120232,120233,120234,120235,120236,120237,120238,120239,120240,120241,120242,120243,120244,120245,120246,120247,120248,120249); //A - Z sans
        sansN = List.of(120802,120803,120804,120805,120806,120807,120808,120809,120810,120811); //0 - 9 sans
        scriptL = List.of(119990,119991,119992,119993,8495,119995,8458,119997,119998,119999,120000,120001,120002,120003,8500,120005,120006,120007,120008,120009,120010,120011,120012,120013,120014,120015); //a - z script
        scriptU = List.of(119964,8492,119966,119967,8496,8497,119970,8459,8464,119973,119974,8466,8499,119977,119978,119979,119980,8475,119982,119983,119984,119985,119986,119987,119988,119989);  //A - Z script
        bbL = List.of(63635,63636,63637,63638,63639,63640,63641,63642,63643,63644,63645,63646,63647,63648,63649,63650,63651,63652,63653,63654,63655,63656,63657,63658,63659,63660);  //a - z bb
        bbU = List.of(63616,63617,63661,63618,63619,63620,63621,63662,63622,63623,63624,63625,63626,63663,63627,63664,63665,63666,63628,63629,63630,63631,63632,63633,63634,63667);  //A - Z bb
        bbN = List.of(120792,120793,120794,120795,120796,120797,120798,120799,120800,120801); //0 - 9 bb
        frakL = List.of(120094,120095,120096,120097,120098,120099,120100,120101,120102,120103,120104,120105,120106,120107,120108,120109,120110,120111,120112,120113,120114,120115,120116,120117,120118,120119); //a - z frak
        frakU = List.of(120068,120069,8493,120071,120072,120073,120074,8460,8465,120077,120078,120079,120080,120081,120082,120083,120084,8476,120086,120087,120088,120089,120090,120091,120092,8488); //A - Z frak
        greekL = List.of(945,946,947,948,949,950,951,952,953,954,955,956,957,958,959,960,961,963,964,965,966,967,968,969); //alpha - omega greek
        greekU = List.of(913,914,915,916,917,918,919,920,921,922,923,924,925,926,927,928,929,931,932,933,934,935,936,937);  //Alpha - Omega greek

        alphabets = new List[] {normalL, normalU, normalN, italL, italU, sansL, sansU, sansN,scriptL,scriptU,bbL,bbU,bbN,frakL,frakU,greekL,greekU};
    }

    public static List<Integer> getCharacterRange(int start, int end) {

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
        String startString = new String(Character.toChars(start));
        String endString = new String(Character.toChars(end));
        EditorAlerts.showSimpleAlert("Error", startString + "..." + endString + " is not a valid character range.\n" + message);
        return null;
    }



}
