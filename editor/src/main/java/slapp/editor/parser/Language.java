package slapp.editor.parser;

import com.gluonhq.richtextarea.model.Document;
import javafx.scene.text.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Language implements Serializable {

    private static final long serialVersionUID = 100L;

    private String nameString;
    private List<Text> nameTextList = new ArrayList();

    private String openBracket1, closeBracket1;
    private String openBracket2, closeBracket2;
    private String openBracket3, closeBracket3;
    private boolean allowDroppedBrackets = false;

    private String negation;
    private String conditional;
    private String biconditional;
    private String conjunction;
    private String disjunction;
    private String nand; //up arrow
    private String nor; // down arrow
    private String universalQuant;
    private String existentialQuant;

    private List<String> variables = new ArrayList();
    private boolean variableSubs = false;
    private List<String> constants = new ArrayList();
    private boolean constantSubs = false;

    private List<String> sentenceLetters = new ArrayList();
    private boolean sentenceLetterSubs = false;

    private List<String> xfunctionSymbols = new ArrayList();
    private boolean xfunctionSymbolSubs = false;
    private List<String> onePlaceFunctionSymbols = new ArrayList();
    private List<String> twoPlaceFunctionSymbols = new ArrayList();
    private boolean allowBinaryInfixFunctions = false;

    private List<String> xrelationSymbols = new ArrayList();
    private boolean xrelationSymbolSubs = false;
    private boolean xrelationSymbolsRequireSuper = false;
    private List<String> onePlaceRelSymbols = new ArrayList();
    private boolean allowBinaryInfixRelations = false;
    private boolean allowBinaryInfixNegations = false;
    private Map<String, String> infixRelations;

    private boolean allowBoundedQuantifiers = false;
    private String dividerSymbol; //for bounded quantifiers

    private Map<UnabbType, Document> unabbForms;

    //for metalanguage

    private List<String> termSymbols = new ArrayList();
    private List<String> formulaSymbols = new ArrayList();
    private List<String> arbitraryExpressionSymbols = new ArrayList();


    Language(String nameString) {
        this.nameString = nameString;
    }


    void setNameTexts(String baseStr, String supscriptStr, String subscriptStr ) {
        if (!baseStr.isEmpty()) nameTextList.add(ParseUtilities.newRegularText(baseStr));
        if (!supscriptStr.isEmpty() && subscriptStr.isEmpty()) nameTextList.add(ParseUtilities.newSuperscriptText(supscriptStr));
        if (supscriptStr.isEmpty() && !subscriptStr.isEmpty()) nameTextList.add(ParseUtilities.newSubscriptText(subscriptStr));
        if (!supscriptStr.isEmpty() && !subscriptStr.isEmpty()) nameTextList.addAll(Arrays.asList(ParseUtilities.newSupSubText(supscriptStr, subscriptStr)));
    }

    public String getInfixRelationKey(String value) {
        String relation = "";
        for (Map.Entry<String, String> entry : infixRelations.entrySet()) {
            if(entry.getValue().equals(value)) {
                value = entry.getKey();
                break;
            }
        }
        return value;
    }

    public String getNameString() {
        return nameString;
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

    public boolean isAllowDroppedBrackets() {
        return allowDroppedBrackets;
    }

    public void setAllowDroppedBrackets(boolean allowDroppedBrackets) {
        this.allowDroppedBrackets = allowDroppedBrackets;
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
        List<String> newVars = new ArrayList<>();
        newVars.addAll(variables);
        newVars.add("\u1d76");
        return newVars;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }

    public static String getDummyVariableSym() {
        return "\u1d76";
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

    public Map<String, String> getInfixRelations() {
        return infixRelations;
    }

    public void setInfixRelations(Map<String, String> infixRelations) {
        this.infixRelations = infixRelations;
    }

    public boolean isAllowBinaryInfixRelations() {
        return allowBinaryInfixRelations;
    }

    public void setAllowBinaryInfixRelations(boolean allowBinaryInfixRelations) {
        this.allowBinaryInfixRelations = allowBinaryInfixRelations;
    }

    public boolean isAllowBinaryInfixNegations() {
        return allowBinaryInfixNegations;
    }

    public void setAllowBinaryInfixNegations(boolean allowBinaryInfixNegations) {
        this.allowBinaryInfixNegations = allowBinaryInfixNegations;
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

    public boolean isAllowBoundedQuantifiers() {
        return allowBoundedQuantifiers;
    }

    public void setAllowBoundedQuantifiers(boolean allowBoundedQuantifiers) {
        this.allowBoundedQuantifiers = allowBoundedQuantifiers;
    }

    public String getDividerSymbol() {
        return dividerSymbol;
    }

    public void setDividerSymbol(String dividerSymbol) {
        this.dividerSymbol = dividerSymbol;
    }

    public Map<UnabbType, Document> getUnabbForms() {
        return unabbForms;
    }

    public void setUnabbForms(Map<UnabbType, Document> unabbForms) {
        this.unabbForms = unabbForms;
    }

    public List<String> getTermSymbols() {
        return termSymbols;
    }

    public void setTermSymbols(List<String> termSymbols) {
        this.termSymbols = termSymbols;
    }

    public List<String> getFormulaSymbols() {
        return formulaSymbols;
    }

    public void setFormulaSymbols(List<String> formulaSymbols) {
        this.formulaSymbols = formulaSymbols;
    }

    public List<String> getArbitraryExpressionSymbols() {
        return arbitraryExpressionSymbols;
    }

    public void setArbitraryExpressionSymbols(List<String> arbitraryExpressionSymbols) {
        this.arbitraryExpressionSymbols = arbitraryExpressionSymbols;
    }
}