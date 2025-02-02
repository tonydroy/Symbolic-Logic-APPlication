package slapp.editor.parser;

import java.io.Serializable;
import java.util.List;

public class Language implements Serializable {

    private static final long serialVersionUID = 100L;

    private String name;
    private String openBracket1, closeBracket1;
    private String openBracket2, closeBracket2;
    private String openBracket3, closeBracket3;
    private String negation;
    private String conditional;
    private String biconditional;
    private String conjunction;
    private String disjunction;
    private String nand; //up arrow
    private String nor; // down arrow
    private String universalQuant;
    private String existentialQuant;
    private List<String> variables;
    private boolean variableSubs;
    private List<String> constants;
    private boolean constantSubs;
    private List<String> sentenceLetters;
    private boolean sentenceLetterSubs;
    private List<String> xrelationSymbols;
    private boolean xrelationSymbolSubs;
    private boolean xrelationSymbolsRequireSuper;
    private List<String> onePlaceRelSymbols, twoPlaceRelSymbols;
    private boolean allowBinaryInfixRelations;
    private List<String> xfunctionSymbols;
    private boolean xfunctionSymbolSubs;
    private List<String> onePlaceFunctionSymbols, twoPlaceFunctionSymbols;
    private boolean allowBinaryInfixFunctions;

    Language(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getOpenBracket1() {
        return openBracket1;
    }

    public void setOpenBracket1(String openBracket1) {
        this.openBracket1 = openBracket1;
    }

    public String getCloseBracket1() {
        return closeBracket1;
    }

    public void setCloseBracket1(String closeBracket1) {
        this.closeBracket1 = closeBracket1;
    }

    public String getOpenBracket2() {
        return openBracket2;
    }

    public void setOpenBracket2(String openBracket2) {
        this.openBracket2 = openBracket2;
    }

    public String getCloseBracket2() {
        return closeBracket2;
    }

    public void setCloseBracket2(String closeBracket2) {
        this.closeBracket2 = closeBracket2;
    }

    public String getOpenBracket3() {
        return openBracket3;
    }

    public void setOpenBracket3(String openBracket3) {
        this.openBracket3 = openBracket3;
    }

    public String getCloseBracket3() {
        return closeBracket3;
    }

    public void setCloseBracket3(String closeBracket3) {
        this.closeBracket3 = closeBracket3;
    }

    public String getNegation() {
        return negation;
    }

    public void setNegation(String negation) {
        this.negation = negation;
    }

    public String getConditional() {
        return conditional;
    }

    public void setConditional(String conditional) {
        this.conditional = conditional;
    }

    public String getBiconditional() {
        return biconditional;
    }

    public void setBiconditional(String biconditional) {
        this.biconditional = biconditional;
    }

    public String getConjunction() {
        return conjunction;
    }

    public void setConjunction(String conjunction) {
        this.conjunction = conjunction;
    }

    public String getDisjunction() {
        return disjunction;
    }

    public void setDisjunction(String disjunction) {
        this.disjunction = disjunction;
    }

    public String getNand() {
        return nand;
    }

    public void setNand(String nand) {
        this.nand = nand;
    }

    public String getNor() {
        return nor;
    }

    public void setNor(String nor) {
        this.nor = nor;
    }

    public String getUniversalQuant() {
        return universalQuant;
    }

    public void setUniversalQuant(String universalQuant) {
        this.universalQuant = universalQuant;
    }

    public String getExistentialQuant() {
        return existentialQuant;
    }

    public void setExistentialQuant(String existentialQuant) {
        this.existentialQuant = existentialQuant;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public boolean isVariableSubs() {
        return variableSubs;
    }

    public void setVariableSubs(boolean variableSubs) {
        this.variableSubs = variableSubs;
    }

    public List<String> getConstants() {
        return constants;
    }

    public void setConstants(List<String> constants) {
        this.constants = constants;
    }

    public boolean isConstantSubs() {
        return constantSubs;
    }

    public void setConstantSubs(boolean constantSubs) {
        this.constantSubs = constantSubs;
    }

    public List<String> getSentenceLetters() {
        return sentenceLetters;
    }

    public void setSentenceLetters(List<String> sentenceLetters) {
        this.sentenceLetters = sentenceLetters;
    }

    public boolean isSentenceLetterSubs() {
        return sentenceLetterSubs;
    }

    public void setSentenceLetterSubs(boolean sentenceLetterSubs) {
        this.sentenceLetterSubs = sentenceLetterSubs;
    }

    public List<String> getXrelationSymbols() {
        return xrelationSymbols;
    }

    public void setXrelationSymbols(List<String> xrelationSymbols) {
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

    public List<String> getOnePlaceRelSymbols() {
        return onePlaceRelSymbols;
    }

    public void setOnePlaceRelSymbols(List<String> onePlaceRelSymbols) {
        this.onePlaceRelSymbols = onePlaceRelSymbols;
    }

    public List<String> getTwoPlaceRelSymbols() {
        return twoPlaceRelSymbols;
    }

    public void setTwoPlaceRelSymbols(List<String> twoPlaceRelSymbols) {
        this.twoPlaceRelSymbols = twoPlaceRelSymbols;
    }

    public boolean isAllowBinaryInfixRelations() {
        return allowBinaryInfixRelations;
    }

    public void setAllowBinaryInfixRelations(boolean allowBinaryInfixRelations) {
        this.allowBinaryInfixRelations = allowBinaryInfixRelations;
    }

    public List<String> getXfunctionSymbols() {
        return xfunctionSymbols;
    }

    public void setXfunctionSymbols(List<String> xfunctionSymbols) {
        this.xfunctionSymbols = xfunctionSymbols;
    }

    public boolean isXfunctionSymbolSubs() {
        return xfunctionSymbolSubs;
    }

    public void setXfunctionSymbolSubs(boolean xfunctionSymbolSubs) {
        this.xfunctionSymbolSubs = xfunctionSymbolSubs;
    }

    public List<String> getOnePlaceFunctionSymbols() {
        return onePlaceFunctionSymbols;
    }

    public void setOnePlaceFunctionSymbols(List<String> onePlaceFunctionSymbols) {
        this.onePlaceFunctionSymbols = onePlaceFunctionSymbols;
    }

    public List<String> getTwoPlaceFunctionSymbols() {
        return twoPlaceFunctionSymbols;
    }

    public void setTwoPlaceFunctionSymbols(List<String> twoPlaceFunctionSymbols) {
        this.twoPlaceFunctionSymbols = twoPlaceFunctionSymbols;
    }

    public boolean isAllowBinaryInfixFunctions() {
        return allowBinaryInfixFunctions;
    }

    public void setAllowBinaryInfixFunctions(boolean allowBinaryInfixFunctions) {
        this.allowBinaryInfixFunctions = allowBinaryInfixFunctions;
    }
}