package slapp.editor.map_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class MapABExpCheck {
    private MapABExpExercise mapExercise;
    private MapABExpView mapView;
    private MapABExpModel mapModel;
    private MapCheckSetup checkSetup;

    String objLangName;
    private MapABExpFormulaBox metaBox;
    private MapABExpFormulaBox objectBox;
    private Document metaDocument;
    private Document objectDocument;
    private List<MapCheckLink> checkLinks = new ArrayList<>();
    private List<Expression> objectSyms;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess;
    private boolean choiceSuccess;
    boolean checkChoices;
    boolean choiceASet;
    boolean choiceBSet;
    boolean choiceSet;
    boolean choiceAMade;
    boolean choiceBMade;
    boolean choiceMade;



    public MapABExpCheck(MapABExpExercise mapExercise) {
        this.mapExercise = mapExercise;
        this.mapView = mapExercise.getExerciseView();
        this.mapModel = mapExercise.getExerciseModel();

        checkSetup = mapModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new MapCheckSetup();
            mapModel.setCheckSetup(checkSetup);
        }
        objLangName = checkSetup.getObjLangName();

        checkChoices = checkSetup.isCheckChoices();
        choiceASet = checkSetup.isChoiceA();
        choiceBSet = checkSetup.isChoiceB();
        choiceSet = choiceASet || choiceBSet;

        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            mapView.setCheckColor(Color.LAWNGREEN);
            mapView.setCheckElementsColor(Color.GREEN);
            mapView.setCheckMessage(checkSetup.getCheckMessage());
        }
        else {
            mapView.setCheckColor(Color.ORCHID);
            mapView.setCheckElementsColor(Color.PURPLE);
            mapView.setCheckMessage(checkSetup.getCheckMessage());
        }
        if (checkSuccess) mapView.activateBigCheck();
        else mapView.deactivateBigCheck();

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        setChecksCounter();

        setRightControlBox();
    }

    private void setRightControlBox() {
        mapView.getCheckButton().setOnAction(e -> {
            checkFinal = true;
            mapView.setCheckColor(Color.LAWNGREEN);
            mapView.setCheckMessage("Map");
            mapView.setCheckElementsColor(Color.GREEN);
            checkMap(true);
        });

        mapView.getCheckProgButton().setOnAction(e -> {
            checkFinal = false;
            mapView.setCheckColor(Color.ORCHID);
            mapView.setCheckMessage("Checked Links");
            mapView.setCheckElementsColor(Color.PURPLE);
            checkMap(false);
        });

        mapView.getstaticHelpButton().setDisable(!checkSetup.isStaticHelp());

        mapView.getstaticHelpButton().setOnAction(e -> {
            Stage helpStage = mapView.getStaticHelpStage();
            if (helpStage != null && helpStage.isShowing()) helpStage.close();
            else mapView.showStaticHelp(mapModel.getCheckSetup().getStaticHelpDoc());
        });
    }

    private void setChecksCounter() {
        if (checkMax != -1 && checkTries >= checkMax && !mapExercise.getMainWindow().isInstructorFunctions()) {
            mapView.getCheckButton().setDisable(true);
            mapView.getCheckProgButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        mapView.getCheckTriesLabel().setText(checkString);
    }



    public boolean checkMap(Boolean checkComplete) {

        checkSuccess = false;
        mapView.deactivateBigCheck();
        if (!mapExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        choiceAMade = mapExercise.getExerciseView().getaCheckBox().isSelected();
        choiceBMade = mapExercise.getExerciseView().getbCheckBox().isSelected();
        choiceMade = choiceAMade || choiceBMade;




        setMetaAndObjBoxes();
        populateCheckLinks();

        boolean checkOK = true;

        if (noLinks()) checkOK = false;
        else if (metaLinkToBracket()) checkOK = false;
        else if (crossedLinks()) checkOK = false;
        else if (emptyLink()) checkOK = false;
        else if (!checkMatching()) checkOK = false;
        else if (badGroup()) checkOK = false;

        else if (checkComplete && !checkCompleteMatch()) checkOK = false;

        if (checkOK) {
            if (choiceSet && !checkChoiceMade()) {}
            else if (choiceSet && (checkChoices || mapExercise.getMainWindow().isInstructorFunctions())) {
                if (checkChoice()) {
                    checkSuccess = true;
                    if (checkComplete) mapView.setCheckMessage("Map / Choice");
                    else mapView.setCheckMessage("Checked Links / Choice");
                }
            }
            else {
                checkSuccess = true;
                if (checkComplete) mapView.setCheckMessage("Map");
                else mapView.setCheckMessage("Checked Links");
            }
        }


        if (checkSuccess) {
            mapView.activateBigCheck();
            return true;
        }

        return false;
    }

    private boolean checkChoiceMade() {
        boolean success = true;

        if (!choiceMade) {
            mapView.getaCheckBox().setStyle("-fx-background-color: mistyrose");
            mapView.getbCheckBox().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));
            mapView.getaCheckBox().setStyle("-fx-background-color: white");
            mapView.getbCheckBox().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    private boolean checkChoice() {
        boolean success = true;

        if (choiceAMade && !choiceASet) {
            mapView.getaCheckBox().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            mapView.getaCheckBox().setStyle("-fx-background-color: white");
            success = false;
        }
        if (choiceBMade && !choiceBSet) {
            mapView.getbCheckBox().setStyle("-fx-background-color: mistyrose");
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            mapView.getbCheckBox().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    private boolean noLinks() {
        boolean noLinks = false;
        if (checkLinks.isEmpty()) {
            EditorAlerts.fleetingRedPopup("No map to check.");
            noLinks = true;
        }
        return noLinks;
    }

    private boolean checkCompleteMatch() {
        boolean good;
        boolean metaBoxGood = true;
        boolean objectBoxGood = true;

        //check unparsed
        for (MapCheckLink checkLink : checkLinks) {

            if (checkLink.getMetaExpSize() > 1) metaBoxGood = false;
            if (checkLink.getObjectExpSize() > 1 && !checkLink.isObjectGroup()) objectBoxGood = false;
        }

        //before first link
        if (!checkLinks.isEmpty() && checkLinks.get(0).getMetaStartIndex() > 0) {
            List<OriginalElement> metaElements = ParseUtilities.getSubElements(metaDocument, 0, checkLinks.get(0).getMetaStartIndex());
            if (metaElements.size() > 0) metaBoxGood = false;
        }
        if (!checkLinks.isEmpty() && checkLinks.get(0).getObjectStartIndex() > 0) {
            List<OriginalElement> objectElements = ParseUtilities.getSubElements(objectDocument, 0, checkLinks.get(0).getObjectStartIndex());
            if (objectElements.size() > 0) objectBoxGood = false;
        }

        //after group
        for (int i = 0; i < checkLinks.size(); i++) {
            MapCheckLink checkLink = checkLinks.get(i);
            if (checkLink.isObjectGroup()) {
                Integer endGroup = checkLink.getObjectEndIndex();
                Integer nextStart;
                if (i + 1 < checkLinks.size()) {
                    nextStart = checkLinks.get(i + 1).getObjectStartIndex();
                }
                else {
                    nextStart = objectDocument.getText().length();
                }
                if (nextStart - endGroup > 0) {
                    List<OriginalElement> objectElements = ParseUtilities.getSubElements(objectDocument, endGroup, nextStart);
                    if (objectElements.size() > 0) objectBoxGood = false;
                }
            }
        }

        good = metaBoxGood && objectBoxGood;
        if (!good) {
            if (!metaBoxGood) metaBox.getFormulaBox().setVTtreeBoxHighlight();
            if (!objectBoxGood) objectBox.getFormulaBox().setVTtreeBoxHighlight();
            EditorAlerts.showSimpleTxtListAlert("Map Problem", Collections.singletonList(ParseUtilities.newRegularText("Box not completely mapped.")));
            metaBox.getFormulaBox().resetVTtreeBoxHighlight();
            objectBox.getFormulaBox().resetVTtreeBoxHighlight();
        }
        return good;
    }

    private boolean badGroup() {

        for (MapCheckLink checkLink : checkLinks) {
            if (checkLink.isObjectGroup() && checkLink.getObjectExpSize() > 1) {
                checkLink.getLink().setMapLinkHighlight();
                EditorAlerts.showSimpleTxtListAlert("Match Issue", Collections.singletonList(ParseUtilities.newRegularText("Bad link (check extraneous content).")));
                checkLink.getLink().resetMapLinkHighlight();
                return true;
            }
        }
        return false;
    }

    private boolean checkMatching() {
        MatchUtilities.clearMatching();

        for (MapCheckLink checkLink : checkLinks) {

            Expression metaExp;
            Expression objectExp;

            Language metaLang = Languages.getLanguage("LM Meta");
            MapParser.setLanguage(metaLang);
            ParseUtilities.setLanguage(metaLang);
            List<Expression> metaSyms = MapParser.parseToSymbol(checkLink.getMetaExpressions());
            checkLink.setMetaExpSize(metaSyms.size());

            //parse meta symbol and (check in vocabulary)
            if (metaSyms.get(0).getType() == ExpressionType.ORIGINAL_ELEMENT) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("Linked meta symbol not in vocabulary of "));
                texts.addAll(metaLang.getNameTextList());
                texts.add(new Text("."));
                EditorAlerts.showSimpleTxtListAlert("Vocabulary Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                return false;
            }

            List<Expression> metaParse = new ArrayList<>();
            metaParse.add(metaSyms.get(0));
            ParseUtilities.parseExpressions(metaParse);
            metaExp = metaParse.get(0);

            //parse object expression
            Language objLang = Languages.getLanguage(objLangName);
            objLang.setXrelationSymbolsRequireSuper(true);
            MapParser.setLanguage(objLang);
            ParseUtilities.setLanguage(objLang);

            if (!checkLink.isObjectGroup()) {
                objectSyms = MapParser.parseToSymbol(checkLink.getObjectExpressions());
                checkLink.setObjectExpSize(objectSyms.size());

                List<Expression> objectParse = new ArrayList<>();
                objectParse.add(objectSyms.get(0));
                ParseUtilities.parseExpressions(objectParse);
                objectExp = objectParse.get(0);
            }
            else {
                objectSyms = ParseUtilities.parseExpressions(checkLink.getObjectExpressions());
                checkLink.setObjectExpSize(objectSyms.size());
                objectExp = objectSyms.get(0);
            }

           // System.out.println("meta: " + metaExp +" " + metaExp.getType() + " object " + objectExp + " " + objectExp.getType());

            if (!checkExpressionMatch(metaExp, objectExp, checkLink)) return false;
        }
        return true;
    }

    private boolean checkExpressionMatch(Expression metaExp, Expression objectExp, MapCheckLink checkLink) {

        boolean result = true;

        if (metaExp instanceof OpenBracket) {
            if (!((OpenBracket) metaExp).equals(objectExp)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("The '"));
                texts.addAll(metaExp.toTextList());
                texts.add(new Text("' symbol maps only to itself."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;

            }
        }
        else if (metaExp instanceof CloseBracket) {
            if (!((CloseBracket) metaExp).equals(objectExp)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("The '"));
                texts.addAll(metaExp.toTextList());
                texts.add(new Text("' symbol maps only to itself."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
        }
        else if (metaExp instanceof Operator) {
            if (!((Operator) metaExp).equals(objectExp)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("The '"));
                texts.addAll(metaExp.toTextList());
                texts.add(new Text("' symbol maps only to itself."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
        }
        else if (metaExp instanceof UniversalQuantifierSym) {
            if (!((UniversalQuantifierSym) metaExp).equals(objectExp)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("The '"));
                texts.addAll(metaExp.toTextList());
                texts.add(new Text("' symbol maps only to itself."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
        }
        else if (metaExp instanceof ExistentialQuantifierSym) {
            if (!((ExistentialQuantifierSym) metaExp).equals(objectExp)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("The '"));
                texts.addAll(metaExp.toTextList());
                texts.add(new Text("' symbol maps only to itself."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
        }
        else if (metaExp instanceof ContradictionSimple) {
            if (!((ContradictionSimple) metaExp).equals(objectExp)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("The '"));
                texts.addAll(metaExp.toTextList());
                texts.add(new Text("' symbol maps only to itself."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
        }
        else if (metaExp instanceof MFormula) {
            if (!(objectExp instanceof Formula)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a formula."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else {
                try {
                    ((MFormula) metaExp).setMatch((Formula) objectExp);
                }
                catch (TextMessageException e) {
                    checkLink.getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
                }
            }
        }

        else if (metaExp instanceof MTerm) {
            if (!(objectExp instanceof Term)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a term."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else {
                try {
                    ((MTerm) metaExp).setMatch((Term) objectExp);
                }
                catch (TextMessageException e) {
                    checkLink.getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
                }
            }
        }


        else if (metaExp instanceof Term && ((Term) metaExp).getChildren().size() > 0 && ((Term) metaExp).getChildren().get(0) instanceof MVariable) {
            if (!(objectExp instanceof Term && ((Term) objectExp).getTermType() == TermType.VARIABLE)) {
                    checkLink.getLink().setMapLinkHighlight();
                    List<Text> texts = new ArrayList<>();
                    texts.addAll(metaExp.toTextList());
                    texts.add(new Text(" must map to a variable."));
                    EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
            }
            else {
                try {
                    ((MVariable) ((Term) metaExp).getChildren().get(0)).setMatch((VariableSym) ((Term) objectExp).getChildren().get(0));
                }
                catch (TextMessageException e) {
                    checkLink.getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
                }
            }
        }

        else if (metaExp instanceof Term && ((Term) metaExp).getChildren().size() > 0 && ((Term) metaExp).getChildren().get(0) instanceof MConstant) {
            if (!(objectExp instanceof Term && ((Term) objectExp).getTermType() == TermType.CONSTANT)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a constant."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else {
                try {
                    ((MConstant) ((Term) metaExp).getChildren().get(0)).setMatch((Constant) ((Term) objectExp).getChildren().get(0));
                }
                catch (TextMessageException e) {
                    checkLink.getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
                }
            }
        }

        else if (metaExp instanceof SentenceAtomic && ((SentenceAtomic) metaExp).getMainLetter() instanceof MSentenceLetter) {
            if (!(objectExp instanceof SentenceAtomic)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a sentence letter."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else {
                try {
                    ((MSentenceLetter) ((SentenceAtomic) metaExp).getMainLetter()).setMatch((SentenceLetter) ((SentenceAtomic) objectExp).getMainLetter());
                }
                catch (TextMessageException e) {
                    checkLink.getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
                }
            }
        }

        else if (metaExp instanceof MRelationSymbol) {
            if (!(objectExp instanceof RelationSymbol)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a " + ((MRelationSymbol) metaExp).getPlaces() +"-place relation symbol."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else if (((MRelationSymbol) metaExp).getPlaces() != ((RelationSymbol) objectExp).getPlaces()) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a " + ((MRelationSymbol) metaExp).getPlaces() + "-place relation symbol."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else {
                try {
                    ((MRelationSymbol) metaExp).setMatch((RelationSymbol) objectExp);
                }
                catch (TextMessageException e) {
                    checkLink.getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
                }
            }
        }

        else if (metaExp instanceof MFunctionSymbol) {
            if (!(objectExp instanceof FunctionSymbol)) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a " + ((MFunctionSymbol) metaExp).getPlaces() +"-place function symbol."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else if (((MFunctionSymbol) metaExp).getPlaces() != ((FunctionSymbol) objectExp).getPlaces()) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to a " + ((MFunctionSymbol) metaExp).getPlaces() + "-place function symbol."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else {
                try {
                    ((MFunctionSymbol) metaExp).setMatch((FunctionSymbol) objectExp);
                }
                catch (TextMessageException e) {
                    checkLink.getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                    checkLink.getLink().resetMapLinkHighlight();
                    result = false;
                }
            }
        }

        else if (metaExp instanceof MAnyExpression) {

            boolean isExpression = true;
            for (Expression objExp : objectSyms) {
                if (objExp.getType() == ExpressionType.ORIGINAL_ELEMENT) {
                    isExpression = false;
                    break;
                }
            }
            if (!isExpression) {
                checkLink.getLink().setMapLinkHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(metaExp.toTextList());
                texts.add(new Text(" must map to an expression of "));
                texts.addAll(Languages.getLanguage(objLangName).getNameTextList());
                texts.add(new Text("."));
                EditorAlerts.showSimpleTxtListAlert("Match Issue:", texts);
                checkLink.getLink().resetMapLinkHighlight();
                result = false;
            }
            else {
                if (!checkLink.isObjectGroup()) {
                    try {
                        ((MAnyExpression) metaExp).setMatch(Collections.singletonList(objectSyms.get(0)));
                    }
                    catch (TextMessageException e) {
                        checkLink.getLink().setMapLinkHighlight();
                        EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                        checkLink.getLink().resetMapLinkHighlight();
                        result = false;
                    }
                }
                else {
                    try {
                        ((MAnyExpression) metaExp).setMatch(objectSyms);
                        checkLink.setObjectExpSize(1);
                    }
                    catch (TextMessageException e) {
                        checkLink.getLink().setMapLinkHighlight();
                        EditorAlerts.showSimpleTxtListAlert("Match Issue", e.getMessageList());
                        checkLink.getLink().resetMapLinkHighlight();
                        result = false;
                    }
                }
            }
        }

        else {
            EditorAlerts.showSimpleTxtListAlert("Match Issue", Collections.singletonList(ParseUtilities.newRegularText("Unknown match issue")));
            result = false;
        }

       // System.out.println(result);
        return result;
    }

    private boolean emptyLink() {
        boolean emptyLink = false;
        for (MapCheckLink link : checkLinks) {
            if (link.getMetaExpressions().isEmpty() || link.getObjectExpressions().isEmpty()) {
                link.getLink().setMapLinkHighlight();
                EditorAlerts.showSimpleTxtListAlert("Link Issue:", Collections.singletonList(ParseUtilities.newRegularText("Link goes to blank space.")));
                link.getLink().resetMapLinkHighlight();
                emptyLink = true;
                break;
            }
        }
        return emptyLink;
    }

    private boolean crossedLinks() {
        boolean crossed = false;

        for (int i = 0; i < checkLinks.size(); i++) {
            MapCheckLink link = checkLinks.get(i);
            if (i + 1 < checkLinks.size()) {
                if (link.getMetaStartIndex().equals(checkLinks.get(i + 1).getMetaStartIndex())) {
                    checkLinks.get(i + 1).getLink().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Link Issue:", Collections.singletonList(ParseUtilities.newRegularText("In a good map links do not cross or meet.")));
                    checkLinks.get(i + 1).getLink().resetMapLinkHighlight();
                    crossed = true;
                    return crossed;
                }
            }
        }

        List<Pair<Integer, MapABExpClickableMapLink>> objectIndexes = new ArrayList<>();
        for (MapCheckLink link : checkLinks) {
            objectIndexes.add(new Pair(link.getObjectStartIndex(), link.getLink()));
            if (link.getObjectEndIndex() != null) objectIndexes.add(new Pair(link.getObjectEndIndex() - 1, link.getLink()));
        }

        for (int i = 0; i < objectIndexes.size(); i++) {
            if (i + 1 < objectIndexes.size()) {
                if (objectIndexes.get(i).getKey() >= objectIndexes.get(i + 1).getKey()) {
                    objectIndexes.get(i + 1).getValue().setMapLinkHighlight();
                    EditorAlerts.showSimpleTxtListAlert("Link Issue:", Collections.singletonList(ParseUtilities.newRegularText("In a good map links do not cross or meet.")));
                    objectIndexes.get(i + 1).getValue().resetMapLinkHighlight();
                    crossed = true;
                    break;
                }
            }
        }
        return crossed;
    }

    private boolean metaLinkToBracket() {
        boolean bracket = false;
        for (MapCheckLink link : checkLinks) {
            if (link.getMetaEndIndex() != null) {
                link.getLink().setMapLinkHighlight();
                EditorAlerts.showSimpleTxtListAlert("Link Issue: ", Collections.singletonList(ParseUtilities.newRegularText("A good map does not include \"bracket\" links on the metalinguistic expression.")));
                link.getLink().resetMapLinkHighlight();
                bracket = true;
                break;
            }
        }
        return bracket;
    }

    private void populateCheckLinks() {

        checkLinks = new ArrayList<>();

        List<MapIndexItem> metaIndexItems = metaBox.getMapIndexItems();
        List<MapIndexItem> objectIndexItems = objectBox.getMapIndexItems();
        for (MapIndexItem metaIndexItem : metaIndexItems) {
            for (MapIndexItem objectIndexItem : objectIndexItems) {
                if (metaIndexItem.getLinkID().equals(objectIndexItem.getLinkID())) {
                    String linkID = metaIndexItem.getLinkID();
                    MapCheckLink checkLink = new MapCheckLink();
                    checkLink.setMetaStartIndex(metaIndexItem.getStartIndex());
                    checkLink.setMetaEndIndex(metaIndexItem.getEndIndex());
                    checkLink.setObjectStartIndex(objectIndexItem.getStartIndex());
                    checkLink.setObjectEndIndex(objectIndexItem.getEndIndex());
                    checkLink.setLink(getClickableMapLink(linkID));
                    checkLinks.add(checkLink);
                    break;
                }
            }
        }

        //sort on meta start indexes
        checkLinks.sort(Comparator.comparingInt(MapCheckLink::getMetaStartIndex));

        //populate expression lists
        for (int i = 0; i < checkLinks.size(); i++) {
            MapCheckLink checkLink = checkLinks.get(i);
            int metaStartIndex = checkLink.getMetaStartIndex();
            int metaEndIndex = 0;
            if (checkLink.isMetaGroup()) {
                metaEndIndex = checkLink.getMetaEndIndex();
            }
            else if (i + 1 < checkLinks.size()) {
                metaEndIndex = checkLinks.get(i + 1).getMetaStartIndex();
            }
            else metaEndIndex = metaDocument.getText().length();

            int objectStartIndex = checkLink.getObjectStartIndex();
            int objectEndIndex = 0;
            if (checkLink.isObjectGroup()) {
                objectEndIndex = checkLink.getObjectEndIndex();
            }
            else if (i + 1 < checkLinks.size()) {
                objectEndIndex = checkLinks.get(i + 1).getObjectStartIndex();
            }
            else objectEndIndex = objectDocument.getText().length();


            List<OriginalElement> metaElements = ParseUtilities.getSubElements(metaDocument, metaStartIndex, metaEndIndex);
            List<Expression> metaExpressions = new ArrayList<>();
            for (OriginalElement metaElement : metaElements) {
                metaExpressions.add(metaElement);
            }
            checkLink.setMetaExpressions(metaExpressions);

            List<OriginalElement> objectElements = ParseUtilities.getSubElements(objectDocument, objectStartIndex, objectEndIndex);
            List<Expression> objectExpressions = new ArrayList<>();
            for (OriginalElement objectElement : objectElements) {
                objectExpressions.add(objectElement);
            }
            checkLink.setObjectExpressions(objectExpressions);

        //    System.out.println("checklink: " + checkLink.toString());

        }


    }

    private void setMetaAndObjBoxes() {
        List<Node> nodes = mapView.getRootLayout().getMain_pane().getChildren();
        for (Node node : nodes) {
            if (node instanceof MapABExpFormulaBox) {
                MapABExpFormulaBox box = (MapABExpFormulaBox) node;
                if (box.isMetaBox()) {
                    metaBox = box;
                    RichTextArea metaRTA = metaBox.getFormulaBox().getRTA();
                    if (metaRTA.isModified()) mapExercise.setExerciseModified(true);
                    metaRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    metaDocument = metaRTA.getDocument();
                } else {
                    objectBox = box;
                    RichTextArea objectRTA = objectBox.getFormulaBox().getRTA();
                    if (objectRTA.isModified()) mapExercise.setExerciseModified(true);
                    objectRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    objectDocument = objectRTA.getDocument();
                }
            }
        }
    }

    private MapABExpClickableMapLink getClickableMapLink(String idString) {
        List<Node> nodes = mapView.getRootLayout().getMain_pane().getChildren();
        MapABExpClickableMapLink clickableMapLink = null;
        for (Node node : nodes) {
            if (node instanceof MapABExpClickableMapLink) {
                MapABExpClickableMapLink link = (MapABExpClickableMapLink) node;
                if (link.getIdString().equals(idString)) {
                    clickableMapLink = link;
                }
            }
        }
        return clickableMapLink;
    }




    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public boolean isChoiceSuccess() {
        return choiceSuccess;
    }

    public boolean isCheckFinal() {
        return checkFinal;
    }

    public int getCheckTries() {
        return checkTries;
    }

}
