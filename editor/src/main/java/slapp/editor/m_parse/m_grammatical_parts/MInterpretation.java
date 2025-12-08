package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.symbols.FunctionSymbol;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.*;

public class MInterpretation {


    private MUniverse mUuniverse;
    private Map<Formula, MTruthValue> formulaInterp = new LinkedHashMap<>();
    private Map<Term, MBasicObjTerm> constantInterp = new LinkedHashMap<>();
    private Map<RelationSymbol, MRelation> relationInterp = new LinkedHashMap<>();
    private Map<FunctionSymbol, MFunctionCandidate> functionInterp = new LinkedHashMap<>();



    public void addFormula(Formula formula, MTruthValue value) {
        formulaInterp.put(formula, value);
    }

    public MTruthValue getFormulaVal(Formula formula) {
        return formulaInterp.get(formula);
    }

    public void addConstant(Term constant, MBasicObjTerm term) {
        constantInterp.put(constant, term);
    }

    public MBasicObjTerm getConstantVal(Term constant) {
        return constantInterp.get(constant);
    }

    public void addRelation(RelationSymbol symbol, MRelation relation) {
        relationInterp.put(symbol, relation);
    }

    public MRelation getRelationVal(RelationSymbol symbol) {
        return relationInterp.get(symbol);
    }

    public void addFunction(FunctionSymbol symbol, MFunctionCandidate function) {
        functionInterp.put(symbol, function);
    }

    public MFunctionCandidate getFunctionVal(FunctionSymbol symbol) {
        return functionInterp.get(symbol);
    }

    public MUniverse getmUniverse() {
        return mUuniverse;
    }

    public void addUniverse(MUniverse universe) {
        this.mUuniverse = universe;
    }

    public void setConstantInterp(Map<Term, MBasicObjTerm> constantInterp) {
        this.constantInterp = constantInterp;
    }

    public void setFormulaInterp(Map<Formula, MTruthValue> formulaInterp) {
        this.formulaInterp = formulaInterp;
    }

    public void setFunctionInterp(Map<FunctionSymbol, MFunctionCandidate> functionInterp) {
        this.functionInterp = functionInterp;
    }

    public void setmUuniverse(MUniverse mUuniverse) {
        this.mUuniverse = mUuniverse;
    }

    public void setRelationInterp(Map<RelationSymbol, MRelation> relationInterp) {
        this.relationInterp = relationInterp;
    }

    public List<Text> toTextList(MInterpretationVariable var) {
        List<Text> textList = new ArrayList<>();
        if (mUuniverse != null) {
            textList.add(new Text("U : "));
            textList.addAll(mUuniverse.toTextList());
        }
        for (Map.Entry<Formula, MTruthValue> entry : formulaInterp.entrySet()) {
            textList.add(new Text("\n"));
            textList.addAll(var.toTextList());
            textList.add(new Text("["));
            textList.addAll(entry.getKey().toTextList());
            textList.add(new Text("] = "));
            textList.addAll(entry.getValue().toTextList());
        }
        for (Map.Entry<Term, MBasicObjTerm> entry : constantInterp.entrySet()) {
            textList.add(new Text("\n"));
            textList.addAll(var.toTextList());
            textList.add(new Text("["));
            textList.addAll(entry.getKey().toTextList());
            textList.add(new Text("] = "));
            textList.addAll(entry.getValue().toTextList());
        }
        for (Map.Entry<RelationSymbol, MRelation> entry : relationInterp.entrySet()) {
            textList.add(new Text("\n"));
            textList.addAll(var.toTextList());
            textList.add(new Text("["));
            textList.addAll(entry.getKey().toTextList());
            textList.add(new Text("] = "));
            textList.addAll(entry.getValue().toTextList());
        }
        for (Map.Entry<FunctionSymbol, MFunctionCandidate> entry : functionInterp.entrySet()) {
            textList.add(new Text("\n"));
            textList.addAll(var.toTextList());
            textList.add(new Text("["));
            textList.addAll(entry.getKey().toTextList());
            textList.add(new Text("] = "));
            textList.addAll(entry.getValue().toTextList());
        }
        return textList;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("U ");
        sb.append(" : ");
        if (mUuniverse != null) sb.append(mUuniverse.toString());
        for (Map.Entry<Formula, MTruthValue> entry : formulaInterp.entrySet()) {
            sb.append("\nformula = ");
            sb.append(entry.getKey().toString());
            sb.append(" : ");
            sb.append(entry.getValue().toString());
        }
        for (Map.Entry<Term, MBasicObjTerm> entry : constantInterp.entrySet()) {
            sb.append("\nterm = ");
            sb.append(entry.getKey().toString());
            sb.append(" : ");
            sb.append(entry.getValue().toString());
        }
        for (Map.Entry<RelationSymbol, MRelation> entry : relationInterp.entrySet()) {
            sb.append("\nrelation = ");
            sb.append(entry.getKey().toString());
            sb.append(" : ");
            sb.append(entry.getValue().toString());
        }
        for (Map.Entry<FunctionSymbol, MFunctionCandidate> entry : functionInterp.entrySet()) {
            sb.append("\nfunction = ");
            sb.append(entry.getKey().toString());
            sb.append(" : ");
            sb.append(entry.getValue().toString());
        }
        return sb.toString();
    }


}
