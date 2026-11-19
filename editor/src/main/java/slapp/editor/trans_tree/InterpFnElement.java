package slapp.editor.trans_tree;

import javafx.scene.text.Text;
import slapp.editor.parser.*;
import slapp.editor.parser.symbols.*;

import java.text.Normalizer;
import java.util.*;

public class InterpFnElement {

    private LanguageElement symbol;
    private List<Expression> assignmentExpList = new ArrayList<>();
    private List<Expression> expAndObjVarList = new ArrayList<>();
    private boolean metaSym;
    private String assignmentString;
    private List<Expression> setAntecedent = new ArrayList<>();
    private List<Expression> setCondition = new ArrayList<>();
    private List<Expression> setMemberCondition = new ArrayList<>();
    private List<ObjVar> objVars = new ArrayList<>();
    private UnivSym univSym;
    private boolean found = false;
    private String style = "";

    private Map<String, String> capToNameMap;

    public InterpFnElement(LanguageElement symbol, List<Expression> assignmentExpList) throws TextMessageException {

        this.symbol = symbol;
        this.assignmentExpList = assignmentExpList;
        metaSym = TransTreeCheck.getProcessInterpLang().equals("Meta Sym") ? true : false;
        if (metaSym) style = " italic";

        capToNameMap = Map.ofEntries(
                Map.entry("A", "Alice"), Map.entry("B", "Bob"), Map.entry("C", "Carl"), Map.entry("D", "Dan"), Map.entry("E", "Eve"),
                Map.entry("F", "Fay"), Map.entry("G", "Guy"), Map.entry("H", "Hal"), Map.entry("I", "Ivy"), Map.entry("J", "Joe"),
                Map.entry("K", "Ken"), Map.entry("L", "Lou"), Map.entry("M", "Max"), Map.entry("N", "Nora"), Map.entry("O", "Oscar"),
                Map.entry("P", "Paul"), Map.entry("Q", "Quinn"), Map.entry("R", "Roger"), Map.entry("S", "Sam"), Map.entry("T", "Tom"),
                Map.entry("U", "Uma"), Map.entry("V", "Vera"), Map.entry("W", "Walt"), Map.entry("X", "Xavier"), Map.entry("Y", "Yuri"),
                Map.entry("Z", "Zoe") );

        assignmentString = buildAssignmentString();

        try {
            if (symbol instanceof UnivSym) processUniverse();
            else if (symbol instanceof RelationSymbol) processRelation((RelationSymbol) symbol);
            else if (symbol instanceof FunctionSymbol) processFunction((FunctionSymbol) symbol);
        }
        catch (TextMessageException e) {
            throw e;
        }

     //   System.out.println("antecedent: " + setAntecedent + " condition: " + setCondition);
    }

    private void processFunction(FunctionSymbol funcSym) throws TextMessageException {
        boolean objPairs = true;
        boolean funcPairs = true;

        if (funcSym.getPlaces() == 1) {
            try {
                processRelationN(funcSym, 2);
            }
            catch (TextMessageException e) {
                objPairs = false;
            }
            if (objPairs == false) {
                try {
                    processFunctionN(funcSym);
                }
                catch (TextMessageException e) {
                    funcPairs = false;
                }
            }
            if (!objPairs && !funcPairs) {
                throw new TextMessageException(comboFunctionError(funcSym, assignmentString));
            }
        }
        else {
            try {
                processFunctionN(funcSym);
            }
            catch (TextMessageException e) {
                throw e;
            }
        }

    }

    private void processFunctionN(FunctionSymbol funcSym) throws TextMessageException {

        //check basic bracket/bar structure
        if (!assignmentString.matches("^\\{([^|}]*)\\|(.*?)\\s+and\\s+([^}]*)\\}$")) {     //{... | ... and ... }
            throw new TextMessageException(functionError(funcSym, assignmentString));
        }
        int places = funcSym.getPlaces();
        objVars.clear();
        setExpAndObjVarList();
        splitSetSpecification();

        //generate and check obj vars
        //do I need to verify comma characters -- currently ok with any (spaceholding) character in that place
        List<Expression> noSpaceAntecedent = noSpaceList(setAntecedent);
        int antecedentLength = 7 + (2 * (places - 1));

        if (noSpaceAntecedent.size() != antecedentLength) {
            throw new TextMessageException(functionError(funcSym, assignmentString));
        }

        if (noSpaceAntecedent.get(0).getType() != ExpressionType.ORIGINAL_ELEMENT ||
                noSpaceAntecedent.get(1).getType() != ExpressionType.ORIGINAL_ELEMENT ||
                noSpaceAntecedent.get(antecedentLength - 4).getType() != ExpressionType.ORIGINAL_ELEMENT ||
                noSpaceAntecedent.get(antecedentLength - 1).getType() != ExpressionType.ORIGINAL_ELEMENT   ) {
            throw new TextMessageException(functionError(funcSym, assignmentString));
        }

        if (!(((OriginalElement) noSpaceAntecedent.get(0)).getElementStr().equals("\u27e8")) ||
                !(((OriginalElement) noSpaceAntecedent.get(1)).getElementStr().equals("\u27e8")) ||
                !(((OriginalElement) noSpaceAntecedent.get(antecedentLength - 4)).getElementStr().equals("\u27e9")) ||
                !(((OriginalElement) noSpaceAntecedent.get(antecedentLength - 1)).getElementStr().equals("\u27e9"))  ) {
            throw new TextMessageException(functionError(funcSym, assignmentString));
        }

        int i = 2;
        while (i < antecedentLength - 4) {
            if (metaSym && noSpaceAntecedent.get(i) instanceof MetObjVar) {
                objVars.add((MetObjVar) noSpaceAntecedent.get(i));
            }
            else if (!metaSym && noSpaceAntecedent.get(i) instanceof ObjObjVar) {
                objVars.add((ObjObjVar) noSpaceAntecedent.get(i));
            }
            else {
                throw new TextMessageException(functionError(funcSym, assignmentString));
            }
            i = i + 2;
        }

        if (metaSym && noSpaceAntecedent.get(antecedentLength - 2) instanceof MetObjVar) {
            objVars.add((MetObjVar) noSpaceAntecedent.get(antecedentLength - 2));
        }
        else if (!metaSym && noSpaceAntecedent.get(antecedentLength - 2) instanceof ObjObjVar) {
            objVars.add((ObjObjVar) noSpaceAntecedent.get(antecedentLength - 2));
        }
        else {
            throw new TextMessageException(functionError(funcSym, assignmentString));
        }

        if (objVars.size() != places + 1) {
            throw new TextMessageException(functionError(funcSym, assignmentString));
        }

        //check membership condition
        List<Expression> noSpaceSetMemberCondition = noSpaceList(setMemberCondition);

        int objSize = objVars.size();
        int memSize = noSpaceSetMemberCondition.size();
        if (objSize + (objSize - 1) + 2 != memSize)
            throw new TextMessageException(functionError(symbol, assignmentString));

        for (int j = 0; j < objSize; j++) {
            if (!(noSpaceSetMemberCondition.get(2 * j) instanceof ObjVar))
                throw new TextMessageException(functionError(symbol, assignmentString));

            ObjVar mate = (ObjVar) noSpaceSetMemberCondition.get(2 * j);
            if (!objVars.get(j).equals(mate))
                throw new TextMessageException(functionError(symbol, assignmentString));
        }

        if (!noSpaceSetMemberCondition.get(memSize - 2).toString().equals("\u2208") || !(noSpaceSetMemberCondition.get(memSize - 1) instanceof UnivSym))
            throw new TextMessageException(functionError(symbol, assignmentString));

        if (noSpaceSetMemberCondition.get(memSize - 1) instanceof ObjUnivSym) univSym = (ObjUnivSym) (noSpaceSetMemberCondition.get(memSize - 1));
        else univSym = (MetUnivSym) (noSpaceSetMemberCondition.get(memSize - 1));

        //check obj vars in condition
        for (ObjVar objVar : objVars) {
            boolean hasVar = false;
            for (Expression exp : setCondition) {
                if (exp instanceof ObjVar) {
                    ObjVar ov = (ObjVar) exp;
                    if (objVar.equals(ov)) {
                        hasVar = true;
                        break;
                    }
                }
            }
            if (!hasVar) {
                throw new TextMessageException(functionError(funcSym, assignmentString));
            }
        }

        for (Expression exp : setCondition) {
            boolean onlyVar = false;
            if (exp instanceof ObjVar) {
                ObjVar ov = (ObjVar) exp;
                for (ObjVar objVar : objVars) {
                    if (objVar.equals(ov)) onlyVar = true;
                }
                if (!onlyVar) {
                    throw new TextMessageException(functionError(funcSym, assignmentString));
                }
            }
        }
        //check obj vars in condition
        for (ObjVar objVar : objVars) {
            boolean hasVar = false;
            for (Expression exp : setCondition) {
                if (exp instanceof ObjVar) {
                    ObjVar ov = (ObjVar) exp;
                    if (objVar.equals(ov)) {
                        hasVar = true;
                        break;
                    }
                }
            }
            if (!hasVar) {
                throw new TextMessageException(functionError(funcSym, assignmentString));
            }
        }

        for (Expression exp : setCondition) {
            boolean onlyVar = false;
            if (exp instanceof ObjVar) {
                ObjVar ov = (ObjVar) exp;
                for (ObjVar objVar : objVars) {
                    if (objVar.equals(ov)) onlyVar = true;
                }
                if (!onlyVar) {
                    throw new TextMessageException(functionError(funcSym, assignmentString));
                }
            }
        }
    }


    private void processRelation(RelationSymbol relSym) throws TextMessageException {
        boolean objSet = true;
        boolean tupleSet = true;

        if (relSym.getPlaces() == 1) {
            try {
                processSimpleRelation();
            } catch (TextMessageException e) {
                objSet = false;
            }
            if (objSet == false) {
                try {
                    processRelationN(relSym, 1);
                } catch (TextMessageException e) {
                    tupleSet = false;
                }
            }
            if (!objSet && !tupleSet) {
                throw new TextMessageException(comboRelationError(relSym, assignmentString));
            }
        }
        else {
            try {
                processRelationN(relSym, relSym.getPlaces());
            }
            catch (TextMessageException e) {
                throw e;
            }
        }

    }

    private void processSimpleRelation() throws TextMessageException {
        //check basic bracket/bar structure
        if (!assignmentString.matches("^\\{([^|}]*)\\|(.*?)\\s+and\\s+([^}]*)\\}$")) {     // {... | ... and ... }
            throw new TextMessageException(comboRelationError(symbol, assignmentString));
        }

        setExpAndObjVarList();
        splitSetSpecification();

        //generate and check objVars list
        objVars.clear();
        List<Expression> noSpaceAntecedent = noSpaceList(setAntecedent);
        if (noSpaceAntecedent.size() != 1) {
            throw new TextMessageException(comboRelationError(symbol, assignmentString));
        }
        if (metaSym && noSpaceAntecedent.get(0) instanceof MetObjVar) {
            objVars.add((MetObjVar) noSpaceAntecedent.get(0));
        }
        else if (!metaSym && noSpaceAntecedent.get(0) instanceof ObjObjVar) {
            objVars.add((ObjObjVar) noSpaceAntecedent.get(0));
        }
        else {
            throw new TextMessageException(comboRelationError(symbol, assignmentString));
        }

        //check membership condition
        List<Expression> noSpaceMemberCond = noSpaceList(setMemberCondition);
        if (!(noSpaceMemberCond.get(0) instanceof ObjVar) || !noSpaceMemberCond.get(1).toString().equals("\u2208") || !(noSpaceMemberCond.get(2) instanceof UnivSym))
            throw new TextMessageException(comboRelationError(symbol, assignmentString));

        if (noSpaceMemberCond.get(2) instanceof ObjUnivSym) univSym = (ObjUnivSym) (noSpaceMemberCond.get(2));
        else univSym = (MetUnivSym) (noSpaceMemberCond.get(2));

        if (!((ObjVar) noSpaceMemberCond.get(0)).equals(objVars.get(0)))
            throw new TextMessageException(comboRelationError(symbol, assignmentString));

        //check obj var in condition
        boolean hasVar = false;
        boolean onlyVar = true;
        for (Expression exp : setCondition) {
            if (exp instanceof ObjVar) {
                ObjVar objVar = (ObjVar) exp;
                if (objVars.get(0).equals(objVar)) hasVar = true;
                if (!objVars.get(0).equals(objVar)) onlyVar = false;
            }
        }
        if (!hasVar || !onlyVar) {
            throw new TextMessageException(comboRelationError(symbol, assignmentString));
        }
    }


    private void processRelationN(LanguageElement relSym, int places) throws TextMessageException {
        //check basic bracket/bar structure
        if (!assignmentString.matches("^\\{([^|}]*)\\|(.*?)\\s+and\\s+([^}]*)\\}$")) {     //{... | ... and ... }
            throw new TextMessageException(relationError(relSym, assignmentString));
        }

        setExpAndObjVarList();
        splitSetSpecification();

        //generate and check objVars list
        objVars.clear();

        List<Expression> noSpaceAntecedent = noSpaceList(setAntecedent);
        int antecedentLength = 3 + (2 * (places - 1));

        if (noSpaceAntecedent.size() != antecedentLength) {
            throw new TextMessageException(relationError(relSym, assignmentString));
        }

        if (noSpaceAntecedent.get(0).getType() != ExpressionType.ORIGINAL_ELEMENT || noSpaceAntecedent.get(antecedentLength - 1).getType() != ExpressionType.ORIGINAL_ELEMENT) {
            throw new TextMessageException(relationError(relSym, assignmentString));
        }

        if (!(((OriginalElement) noSpaceAntecedent.get(0)).getElementStr().equals("\u27e8")) || !(((OriginalElement) noSpaceAntecedent.get(antecedentLength - 1)).getElementStr().equals("\u27e9"))) {
            throw new TextMessageException(relationError(relSym, assignmentString));
        }



        int i = 1;

        while (i < antecedentLength - 1) {
            if (metaSym && noSpaceAntecedent.get(i) instanceof MetObjVar) {
                objVars.add((MetObjVar) noSpaceAntecedent.get(i));
            }
            else if (!metaSym && noSpaceAntecedent.get(i) instanceof ObjObjVar) {
                objVars.add((ObjObjVar) noSpaceAntecedent.get(i));
            }
            else {
                throw new TextMessageException(relationError(relSym, assignmentString));
            }



            i = i + 2;
        }

        if (objVars.size() != places) {
            throw new TextMessageException(relationError(relSym, assignmentString));
        }
        //check membership condition
        List<Expression> noSpaceSetMemberCondition = noSpaceList(setMemberCondition);

        int objSize = objVars.size();
        int memSize = noSpaceSetMemberCondition.size();
        if (objSize + (objSize - 1) + 2 != memSize)
            throw new TextMessageException(relationError(symbol, assignmentString));

        for (int j = 0; j < objSize; j++) {
            if (!(noSpaceSetMemberCondition.get(2 * j) instanceof ObjVar))
                throw new TextMessageException(relationError(symbol, assignmentString));

            ObjVar mate = (ObjVar) noSpaceSetMemberCondition.get(2 * j);
            if (!objVars.get(j).equals(mate))
                throw new TextMessageException(relationError(symbol, assignmentString));
        }

        if (!noSpaceSetMemberCondition.get(memSize - 2).toString().equals("\u2208") || !(noSpaceSetMemberCondition.get(memSize - 1) instanceof UnivSym))
            throw new TextMessageException(relationError(symbol, assignmentString));

        if (noSpaceSetMemberCondition.get(memSize - 1) instanceof ObjUnivSym) univSym = (ObjUnivSym) (noSpaceSetMemberCondition.get(memSize - 1));
        else univSym = (MetUnivSym) (noSpaceSetMemberCondition.get(memSize - 1));

        //check obj vars in condition
        for (ObjVar objVar : objVars) {
            boolean hasVar = false;
            for (Expression exp : setCondition) {
                if (exp instanceof ObjVar) {
                    ObjVar ov = (ObjVar) exp;
                    if (objVar.equals(ov)) {
                        hasVar = true;
                        break;
                    }
                }
            }
            if (!hasVar) {
                throw new TextMessageException(relationError(relSym, assignmentString));
            }
        }

        for (Expression exp : setCondition) {
            boolean onlyVar = false;
            if (exp instanceof ObjVar) {
                ObjVar ov = (ObjVar) exp;
                for (ObjVar objVar : objVars) {
                    if (objVar.equals(ov)) onlyVar = true;
                }
                if (!onlyVar) {
                    throw new TextMessageException(relationError(relSym, assignmentString));
                }
            }
        }



  //      System.out.println(relSym.toString() + " : " + setAntecedent + " / " + objVars + " / " + setCondition);
    }


    private void processUniverse() throws TextMessageException {
        //check basic bracket/bar structure
        if (!assignmentString.matches("^\\{([^|}]*)\\|([^}]*)\\}$")) {     //{... | ... }
            throw new TextMessageException(universeError(assignmentString));
        }

        setExpAndObjVarList();
        splitSimpleSetSpecification();

        //generate and check objVars list
        objVars.clear();
        List<Expression> noSpaceAntecedent = noSpaceList(setAntecedent);
        if (noSpaceAntecedent.size() != 1) {
            throw new TextMessageException(universeError(assignmentString));
        }
        if (metaSym && noSpaceAntecedent.get(0) instanceof MetObjVar) {
            objVars.add((MetObjVar) noSpaceAntecedent.get(0));
        } else if (!metaSym && noSpaceAntecedent.get(0) instanceof ObjObjVar) {
            objVars.add((ObjObjVar) noSpaceAntecedent.get(0));
        } else {
            throw new TextMessageException(universeError(assignmentString));
        }

        //check obj var in condition
        boolean hasVar = false;
        boolean onlyVar = true;
        for (Expression exp : setCondition) {
            if (exp instanceof ObjVar) {
                ObjVar objVar = (ObjVar) exp;
                if (objVars.get(0).equals(objVar)) hasVar = true;
                if (!objVars.get(0).equals(objVar)) onlyVar = false;
            }
        }
        if (!hasVar || !onlyVar) {
            throw new TextMessageException(universeError(assignmentString));
        }
    }



    private void setExpAndObjVarList() {
        expAndObjVarList.clear();
        for (int i = 0; i < assignmentExpList.size(); i++) {
            Expression exp = assignmentExpList.get(i);
            Language metaSupp = Languages.getLanguage("Meta Supp");
            if (exp.getType() == ExpressionType.ORIGINAL_ELEMENT) {
                String elementStr = ((OriginalElement) exp).getElementStr();
                if (metaSupp.getMetObjVars().contains(elementStr)) expAndObjVarList.add(MetObjVar.getInstance(((OriginalElement) exp).getElementStr()));
                else if (metaSupp.getObjObjVars().contains(elementStr)) expAndObjVarList.add(new ObjObjVar(((OriginalElement) exp).getElementStr()));
                else if (metaSupp.getObjUnivSymbols().contains(elementStr)) expAndObjVarList.add(new ObjUnivSym(((OriginalElement) exp).getElementStr()));
                else if (metaSupp.getMetUnivSymbols().contains(elementStr)) expAndObjVarList.add(MetUnivSym.getInstance(((OriginalElement) exp).getElementStr()));
                else expAndObjVarList.add(exp);
            }
        }
    }




    private List<Expression> noSpaceList(List<Expression> expList) {

        List<Expression> noSpaceList = new ArrayList<>();
        for (int i = 0; i < expList.size(); i++) {
            Expression exp = expList.get(i);
            if (exp.getType() != ExpressionType.ORIGINAL_ELEMENT) noSpaceList.add(exp);
            else if (!((OriginalElement) exp).getElementStr().equals(" ")) noSpaceList.add(exp);
        }
        return noSpaceList;
    }

    private List<Text> comboFunctionError(LanguageElement funcSym, String assignmentString) {

        List<Text> txtList = new ArrayList<>();
        int places = funcSym.getPlaces();

        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("Expect assignment to " + places + "-place function symbol ")));
        txtList.addAll(funcSym.toTextList());

        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" to be of the sort {")));
        txtList.addAll(sampleNTuple(2));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" | ")));
        txtList.addAll(sampleObjVarSeq(2));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText( " \u2208 \ud835\uddb4 and . . ")));
        txtList.addAll(sampleDottedObjVarSeq(2));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" . . } or {\u27e8")));

        txtList.addAll(sampleNTuple(1));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(", ")));
        txtList.add(sampleObjVar(places));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("\u27e9 | ")));
        txtList.addAll(sampleObjVarSeq(2));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText( " \u2208 \ud835\uddb4 and . . ")));
        txtList.addAll(sampleDottedObjVarSeq(2));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" . . } for some (lowercase sans" + style + ") object variables ")));
        txtList.addAll(sampleObjVarSeq(2));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" and (uppercase sans" + style + ") universe variable \ud835\uddb4.\n\nFound: " + assignmentString)));
        return txtList;

    }

    private List<Text> functionError(LanguageElement funcSym, String assignmentString) {

        List<Text> txtList = new ArrayList<>();
        int places = funcSym.getPlaces();
        List<Text> sampleSeq2 = sampleObjVarSeq(places + 1);
        List<Text> sampleSeq3 = sampleObjVarSeq(places + 1);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("Expect assignment to " + places + "-place function symbol ")));
        txtList.addAll(funcSym.toTextList());
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" to be of the sort {\u27e8")));
        txtList.addAll(sampleNTuple(places));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(", ")));
        txtList.add(sampleObjVar(places));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("\u27e9 | ")));
        txtList.addAll(sampleSeq2);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText( " \u2208 \ud835\uddb4 and . . ")));
        txtList.addAll(sampleDottedObjVarSeq(places + 1));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" . . } for some (lowercase sans" + style + ") object variables ")));
        txtList.addAll(sampleSeq3);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" and (uppercase sans" + style + ") universe variable \ud835\uddb4.\n\nFound: " + assignmentString)));
        return txtList;

    }


    private List<Text> comboRelationError(LanguageElement relSym, String assignmentString) {

        List<Text> txtList = new ArrayList<>();
        List<Text> sampleTuple = sampleNTuple(1);
        List<Text> sampleSeq1 = sampleObjVarSeq(1);
        List<Text> sampleSeq2 = sampleObjVarSeq(1);
        List<Text> sampleSeq3 = sampleObjVarSeq(1);
        List<Text> sampleSeq4 = sampleObjVarSeq(1);
        List<Text> sampleSeq5 = sampleObjVarSeq(1);
        List<Text> sampleSeq6 = sampleObjVarSeq(1);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("Expect assignment to " + relSym.getPlaces() + "-place relation symbol ")));
        txtList.addAll(relSym.toTextList());
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" to be of the sort {")));
        txtList.addAll(sampleSeq2);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" | ")));
        txtList.addAll(sampleSeq1);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText( " \u2208 \ud835\uddb4 and . . ")));
        txtList.addAll(sampleSeq3);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" . . } or {")));
        txtList.addAll(sampleTuple);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" | ")));
        txtList.addAll(sampleSeq4);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText( " \u2208 \ud835\uddb4 and . . ")));
        txtList.addAll(sampleSeq6);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" . . } for some (lowercase sans" + style + ") object variable ")));
        txtList.addAll(sampleSeq5);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(", and (uppercase sans" + style + ") universe variable \ud835\uddb4.\n\nFound: " + assignmentString)));
        return txtList;


    }

    private List<Text> relationError(LanguageElement relSym, String assignmentString) {

        List<Text> txtList = new ArrayList<>();
        int places = relSym.getPlaces();
        List<Text> sampleSeq2 = sampleObjVarSeq(places);
        List<Text> sampleSeq3 = sampleObjVarSeq(places);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("Expect assignment to " + places + "-place relation symbol ")));
        txtList.addAll(relSym.toTextList());
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" to be of the sort {")));
        txtList.addAll(sampleNTuple(relSym.getPlaces()));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" | ")));
        txtList.addAll(sampleSeq2);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText( " \u2208 \ud835\uddb4 and . . ")));
        txtList.addAll(sampleDottedObjVarSeq(places));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" . . } for some (lowercase sans" + style + ") object variables ")));
        txtList.addAll(sampleSeq3);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(", and (uppercase sans" + style + ") universe variable \ud835\uddb4.\n\nFound: " + assignmentString)));
        return txtList;

    }


    private List<Text> universeError(String assignmentString) {

        List<Text> txtList = new ArrayList<>();
        List<Text> sampleVar1 = sampleObjVarSeq(1);
        List<Text> sampleVar2= sampleObjVarSeq(1);
        List<Text> sampleVar3 = sampleObjVarSeq(1);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("Expect assignment to universe symbol of the sort, {")));
        txtList.addAll(sampleVar1);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" | . . ")));
        txtList.addAll(sampleVar2);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" . . } for some (lowercase sans" + style + ") object variable ")));
        txtList.addAll(sampleVar3);
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText(".\n\nFound: " + assignmentString)));
        return txtList;
    }

    private Text sampleObjVar(int i) {
        int startCodePoint = metaSym ? 0x1D622 : 0x1D5BA;
        int currentCP = startCodePoint + i;
        String str = new String(Character.toChars(currentCP));
        return ParseUtilities.newRegularText(str);
    }

    private List<Text> sampleObjVarSeq(int n) {
        List<Text> txtList = new ArrayList<>();

        int startCodePoint = metaSym ? 0x1D622 : 0x1D5BA;

        for (int i = 0; i < n; i++) {
            if (i > 0) {
                txtList.add(ParseUtilities.newRegularText(", "));
            }

            // Calculate next character code point and convert to String
            int currentCP = startCodePoint + i;
            String str = new String(Character.toChars(currentCP));

            txtList.add(ParseUtilities.newRegularText(str));
        }
        return txtList;
    }

    private List<Text> sampleDottedObjVarSeq(int n) {
        List<Text> txtList = new ArrayList<>();

        int startCodePoint = metaSym ? 0x1D622 : 0x1D5BA;

        for (int i = 0; i < n; i++) {
            if (i > 0) {
                txtList.add(ParseUtilities.newRegularText(" . . "));
            }

            // Calculate next character code point and convert to String
            int currentCP = startCodePoint + i;
            String str = new String(Character.toChars(currentCP));

            txtList.add(ParseUtilities.newRegularText(str));
        }
        return txtList;
    }






    private List<Text> sampleNTuple(int n) {
        List<Text> txtList = new ArrayList<>();
     //   boolean metaSym = TransTreeCheck.getProcessInterpLang().equals("Meta Sym") ? true : false;

        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("\u27e8")));
        txtList.addAll(sampleObjVarSeq(n));
        txtList.addAll(Collections.singletonList(ParseUtilities.newRegularText("\u27e9")));
        return txtList;
    }


    private String buildAssignmentString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < assignmentExpList.size(); i++) {
            Expression exp = assignmentExpList.get(i);
            String str = exp.toString();
            sb.append(str);
        }
        return sb.toString();
    }

    public String buildSetConditionString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < setCondition.size(); i++) {
            Expression exp = setCondition.get(i);
            String str = exp.toString();
            sb.append(str);
        }
        return sb.toString();
    }


    public String buildMatchedCapNameCondition() {
        List<Expression> matchedCondition = new ArrayList<>();
        for (int i = 0; i < setCondition.size(); i++) {
            if (setCondition.get(i) instanceof MetObjVar) matchedCondition.add(setCondition.get(i).getMatch());
            else matchedCondition.add(setCondition.get(i));
        }
        return buildCapNameCondition(matchedCondition);
    }

    public String buildCapNameCondition(List<Expression> setCondition) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < setCondition.size(); i++) {
            Expression exp = setCondition.get(i);

            if (exp instanceof ObjVar) {
                // 1. Get the raw symbol (e.g., "𝘢")
                String raw = ((ObjVar) exp).toString();

                // 2. Normalize to base ASCII (e.g., "𝘢" -> "a")
                String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC);

                String upper = normalized.toUpperCase();

                String name = capToNameMap.get(upper);

                // 3. Convert to Uppercase ASCII (e.g., "a" -> "A")
                sb.append(name);
            } else {
                sb.append(exp.toString());
            }
        }
        return sb.toString();
    }

    private void splitSimpleSetSpecification() {
        setAntecedent.clear();
        setCondition.clear();
        int i = 1;
        while (!expAndObjVarList.get(i).toString().equals("|")) {
            setAntecedent.add(expAndObjVarList.get(i));
            i++;
        }
        i++;
        while (i < expAndObjVarList.size() - 1) {
            setCondition.add(expAndObjVarList.get(i));
            i++;
        }
    }


    private void splitSetSpecification() {
        setAntecedent.clear();
        setCondition.clear();
        setMemberCondition.clear();
        int i = 1;
        while (!expAndObjVarList.get(i).toString().equals("|")) {
            setAntecedent.add(expAndObjVarList.get(i));
            i++;
        }
        i++;

        while (i + 2 < expAndObjVarList.size() && !isAnd(i)) {
            setMemberCondition.add(expAndObjVarList.get(i));
            i++;
        }
        i = i + 3;

        while (i < expAndObjVarList.size() - 1) {
            setCondition.add(expAndObjVarList.get(i));
            i++;
        }
    }

    private boolean isAnd(int i) {
        boolean isAnd = expAndObjVarList.get(i).toString().equals("a") && expAndObjVarList.get(i + 1).toString().equals("n") && expAndObjVarList.get(i + 2).toString().equals("d");
        if (isAnd) return true;
        return false;
    }

    public List<Expression> getSetCondition() {
        return setCondition;
    }

    public List<ObjVar> getObjVars() {
        return objVars;
    }

    public String getAssignmentString() {
        return assignmentString;
    }

    public LanguageElement getSymbol() {
        return symbol;
    }

    public List<Expression> getAssignmentExpList() {
        return assignmentExpList;
    }

    public void setAssignmentExpList(List<Expression> assignmentExpList) {
        this.assignmentExpList = assignmentExpList;
    }

    public List<Expression> getSetAntecedent() {
        return setAntecedent;
    }



    public UnivSym getUnivSym() {
        return univSym;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    @Override
    public String toString() {
        return symbol.toString() + " : " + assignmentExpList.toString();
    }

}
