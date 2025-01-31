package slapp.editor.parser;

import java.io.Serializable;
import java.util.List;

public class Language implements Serializable {

    private static final long serialVersionUID = 100L;

    private String name;
    private Integer openBracket1, closeBracket1;
    private Integer openBracket2, closeBracket2;
    private Integer openBracket3, closeBracket3;
    private Integer negation;
    private Integer conditional;
    private Integer biconditional;
    private Integer conjunction;
    private Integer disjunction;
    private Integer nand; //up arrow
    private Integer nor; // down arrow
    private Integer universal;
    private Integer existential;
    private List<Integer> variables;
    private boolean intVariableSubs;
    private List<Integer> constants;
    private boolean intConstantSubs;
    private List<Integer> sentenceLetters;
    private boolean intSentenceLetterSubs;
    private List<Integer> xrelationSymbols;
    private boolean xrelationSymbolSubs;
    private boolean xrelationSymbolsRequireSuper;
    private List<Integer> onePlaceRelSymbols, twoPlaceRelSymbols;
    private List<Integer> xfunctionSymbols;
    private boolean xfunctionSymbolSubs;
    private List<Integer> onePlaceFunctionSymbols, twoPlaceFunctionSymbols;

    Language(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getOpenBracket1() {
        return openBracket1;
    }

    public void setOpenBracket1(int openBracket1) {
        this.openBracket1 = openBracket1;
    }

    public int getCloseBracket1() {
        return closeBracket1;
    }

    public void setCloseBracket1(int closeBracket1) {
        this.closeBracket1 = closeBracket1;
    }

    public int getOpenBracket2() {
        return openBracket2;
    }

    public void setOpenBracket2(int openBracket2) {
        this.openBracket2 = openBracket2;
    }

    public int getCloseBracket2() {
        return closeBracket2;
    }

    public void setCloseBracket2(int closeBracket2) {
        this.closeBracket2 = closeBracket2;
    }

    public int getOpenBracket3() {
        return openBracket3;
    }

    public void setOpenBracket3(int openBracket3) {
        this.openBracket3 = openBracket3;
    }

    public int getCloseBracket3() {
        return closeBracket3;
    }

    public void setCloseBracket3(int closeBracket3) {
        this.closeBracket3 = closeBracket3;
    }

    public int getNegation() {
        return negation;
    }

    public void setNegation(int negation) {
        this.negation = negation;
    }

    public int getConditional() {
        return conditional;
    }

    public void setConditional(int conditional) {
        this.conditional = conditional;
    }

    public int getBiconditional() {
        return biconditional;
    }

    public void setBiconditional(int biconditional) {
        this.biconditional = biconditional;
    }

    public int getConjunction() {
        return conjunction;
    }

    public void setConjunction(int conjunction) {
        this.conjunction = conjunction;
    }

    public int getDisjunction() {
        return disjunction;
    }

    public void setDisjunction(int disjunction) {
        this.disjunction = disjunction;
    }

    public int getNand() {
        return nand;
    }

    public void setNand(int nand) {
        this.nand = nand;
    }

    public int getNor() {
        return nor;
    }

    public void setNor(int nor) {
        this.nor = nor;
    }

    public int getUniversal() {
        return universal;
    }

    public void setUniversal(int universal) {
        this.universal = universal;
    }

    public int getExistential() {
        return existential;
    }

    public void setExistential(int existential) {
        this.existential = existential;
    }

    public List<Integer> getVariables() {
        return variables;
    }

    public void setVariables(List<Integer> variables) {
        this.variables = variables;
    }

    public boolean isIntVariableSubs() {
        return intVariableSubs;
    }

    public void setIntVariableSubs(boolean intVariableSubs) {
        this.intVariableSubs = intVariableSubs;
    }

    public List<Integer> getConstants() {
        return constants;
    }

    public void setConstants(List<Integer> constants) {
        this.constants = constants;
    }

    public boolean isIntConstantSubs() {
        return intConstantSubs;
    }

    public void setIntConstantSubs(boolean intConstantSubs) {
        this.intConstantSubs = intConstantSubs;
    }

    public List<Integer> getSentenceLetters() {
        return sentenceLetters;
    }

    public void setSentenceLetters(List<Integer> sentenceLetters) {
        this.sentenceLetters = sentenceLetters;
    }

    public boolean isIntSentenceLetterSubs() {
        return intSentenceLetterSubs;
    }

    public void setIntSentenceLetterSubs(boolean intSentenceLetterSubs) {
        this.intSentenceLetterSubs = intSentenceLetterSubs;
    }

    public List<Integer> getXrelationSymbols() {
        return xrelationSymbols;
    }

    public void setXrelationSymbols(List<Integer> xrelationSymbols) {
        this.xrelationSymbols = xrelationSymbols;
    }

    public boolean isXrelationSymbolSubs() {
        return xrelationSymbolSubs;
    }

    public void setXrelationSymbolSubs(boolean xrelationSymbolSubs) {
        this.xrelationSymbolSubs = xrelationSymbolSubs;
    }

    public boolean isXrelationSymbolsRequireSuper() {
        return xrelationSymbolsRequireSuper;
    }

    public void setXrelationSymbolsRequireSuper(boolean xrelationSymbolsRequireSuper) {
        this.xrelationSymbolsRequireSuper = xrelationSymbolsRequireSuper;
    }

    public List<Integer> getOnePlaceRelSymbols() {
        return onePlaceRelSymbols;
    }

    public void setOnePlaceRelSymbols(List<Integer> onePlaceRelSymbols) {
        this.onePlaceRelSymbols = onePlaceRelSymbols;
    }

    public List<Integer> getTwoPlaceRelSymbols() {
        return twoPlaceRelSymbols;
    }

    public void setTwoPlaceRelSymbols(List<Integer> twoPlaceRelSymbols) {
        this.twoPlaceRelSymbols = twoPlaceRelSymbols;
    }

    public List<Integer> getXfunctionSymbols() {
        return xfunctionSymbols;
    }

    public void setXfunctionSymbols(List<Integer> xfunctionSymbols) {
        this.xfunctionSymbols = xfunctionSymbols;
    }

    public boolean isXfunctionSymbolSubs() {
        return xfunctionSymbolSubs;
    }

    public void setXfunctionSymbolSubs(boolean xfunctionSymbolSubs) {
        this.xfunctionSymbolSubs = xfunctionSymbolSubs;
    }

    public List<Integer> getOnePlaceFunctionSymbols() {
        return onePlaceFunctionSymbols;
    }

    public void setOnePlaceFunctionSymbols(List<Integer> onePlaceFunctionSymbols) {
        this.onePlaceFunctionSymbols = onePlaceFunctionSymbols;
    }

    public List<Integer> getTwoPlaceFunctionSymbols() {
        return twoPlaceFunctionSymbols;
    }

    public void setTwoPlaceFunctionSymbols(List<Integer> twoPlaceFunctionSymbols) {
        this.twoPlaceFunctionSymbols = twoPlaceFunctionSymbols;
    }
}
