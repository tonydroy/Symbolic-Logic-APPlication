package slapp.editor.m_parse;

import slapp.editor.parser.Alphabets;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MLanguage {

    private List<String> nameUppercase;
    private List<String> nameAlphabet;
    private List<String> nameDigits;
    private List<String> basicObjectVariables;
    private List<String> assignmentVariables;
    private List<String> interpretationVariables;
    private String mVerticalDivider;
    private String mOpenSquareBracket, mCloseSquareBracket;
    private String mOpenParenthesis, mCloseParenthesis;
    private String mOpenBrace, mCloseBrace;
    private String mOpenAngleBracket, mCloseAngleBracket;
    private String mUniverseSym;
    private String trueSym;
    private String falseSym;
    private String equalsSym;
    private String satisfiedSym;
    private String notSatisfiedSym;




    public MLanguage() {
        nameUppercase = Alphabets.getCharacterRange("\ud835\udda0", "\ud835\uddb9");  //A - Z sans

        nameAlphabet = Stream.concat(Alphabets.getCharacterRange("\ud835\uddba", "\ud835\uddd3").stream(),
                Alphabets.getCharacterRange("\ud835\udda0", "\ud835\uddb9").stream()).collect(Collectors.toList());  // a - z, A - Z sans

        nameDigits = Alphabets.getCharacterRange("\ud835\udfe2", "\ud835\udfeb");   // 0 - 9 sans
        basicObjectVariables = Alphabets.getCharacterRange("\ud835\uddc6", "\ud835\uddcb");
        assignmentVariables = Arrays.asList("\ud835\uddbd", "\ud835\uddc1", "\ud835\uddc4");
        interpretationVariables = Arrays.asList("\ud835\udda8", "\ud835\udda9", "\ud835\uddaa", "\ud835\uddab", "\ud835\uddac", "\ud835\uddad", "\ue3bd");



        mVerticalDivider = "|";
        mOpenSquareBracket = "[";
        mCloseSquareBracket = "]";
        mOpenParenthesis = "(";
        mCloseParenthesis = ")";
        mOpenBrace = "{";
        mCloseBrace = "}";
        mOpenAngleBracket = "\u27e8";
        mCloseAngleBracket = "\u27e9";
        mUniverseSym = "\ud835\uddb4";
        trueSym = "\ud835\uddb3";
        falseSym = "\ud835\udda5";
        satisfiedSym = "\ud835\uddb2";
        notSatisfiedSym = "\ud835\uddad";
        equalsSym = "=";
        satisfiedSym = "\ud835\uddb2";
        notSatisfiedSym = "\ud835\uddad";



    }

    public List<String> getNameUppercase() {
        return nameUppercase;
    }

    public List<String> getNameAlphabet() {
        return nameAlphabet;
    }

    public List<String> getNameDigits() {
        return nameDigits;
    }

    public List<String> getBasicObjectVariables() {
        return basicObjectVariables;
    }

    public List<String> getAssignmentVariables() {
        return assignmentVariables;
    }

    public List<String> getInterpretationVariables() {
        return interpretationVariables;
    }

    public String getmVerticalDivider() {
        return mVerticalDivider;
    }

    public String getmOpenSquareBracket() {
        return mOpenSquareBracket;
    }

    public String getmCloseSquareBracket() {
        return mCloseSquareBracket;
    }

    public String getmOpenParenthesis() {
        return mOpenParenthesis;
    }

    public String getmCloseParenthesis() {
        return mCloseParenthesis;
    }

    public String getmOpenBrace() {
        return mOpenBrace;
    }

    public String getmCloseBrace() {
        return mCloseBrace;
    }

    public String getmOpenAngleBracket() {
        return mOpenAngleBracket;
    }

    public String getmCloseAngleBracket() {
        return mCloseAngleBracket;
    }

    public String getmUniverseSym() {
        return mUniverseSym;
    }

    public String getTrueSym() {
        return trueSym;
    }

    public String getFalseSym() {
        return falseSym;
    }

    public String getEqualsSym() {
        return equalsSym;
    }

    public String getNotSatisfiedSym() {
        return notSatisfiedSym;
    }

    public String getSatisfiedSym() {
        return satisfiedSym;
    }
}
