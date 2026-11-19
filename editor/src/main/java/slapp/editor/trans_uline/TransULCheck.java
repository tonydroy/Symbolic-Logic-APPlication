package slapp.editor.trans_uline;

import com.gluonhq.richtextarea.RichTextArea;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import slapp.editor.EditorAlerts;
import slapp.editor.SentenceValidator;
import slapp.editor.parser.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TransULCheck {

    private static SentenceValidator validator = SentenceValidator.getInstance();
    private TransULExercise transULExercise;
    private TransULView transULView;
    private TransULModel transULModel;
    private ULcheckSetup checkSetup;

    private int checkMax;
    private int checkTries;
    private boolean checkSuccess;

    private boolean checkChoices;
    private boolean choiceASet;
    private boolean choiceBSet;
    private boolean choiceSet;
    private boolean choiceAMade;
    private boolean choiceBMade;
    private boolean choiceMade;

    List<ULTreeObj> ulTreeObjs;
    ULTreeObj exerKeyTreeObj;
    private static boolean emptyOK = false;

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    private void scrollToNode(Node node) {
        if (isTornDown) return;
        // SCORCHED EARTH RULE 7: Safe execution of deep chains
        try {
            if (transULExercise != null
                    && transULExercise.getMainWindow() != null
                    && transULExercise.getMainWindow().getMainView() != null
                    && node != null) {

                transULExercise.getMainWindow().getMainView().scrollToNode(node);
            }
        } catch (Exception e) {}
    }

    TransULCheck(TransULExercise transULExercise) {
        this.transULExercise = transULExercise;
        this.transULView = transULExercise.getExerciseView();
        this.transULModel = transULExercise.getExerciseModel();

        checkSetup = transULModel.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new ULcheckSetup();
            transULModel.setCheckSetup(checkSetup);
        }

        exerKeyTreeObj = checkSetup.getExerKeyTreeObj();
        if (exerKeyTreeObj != null) {
            if (exerKeyTreeObj.getMainOperator().equals("")) choiceASet = true;
            else choiceBSet = true;
        }

        checkSuccess = checkSetup.isCheckSuccess();
        transULView.setCheckMessage(checkSetup.getCheckMessage());
        if (checkSuccess) transULView.activateBigCheck();
        else transULView.deactivateBigCheck();
        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        updateCheckCounter();
        setRightControlBox();
    }

    private void updateCheckCounter() {
        if (isTornDown || transULExercise == null || transULView == null || transULExercise.getMainWindow() == null) return;

        if (checkMax != -1 && checkTries >= checkMax && !transULExercise.getMainWindow().isInstructorFunctions()) {
            if (transULView.getCheckButton() != null) transULView.getCheckButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        if (transULView.getCheckTriesLabel() != null) transULView.getCheckTriesLabel().setText(checkString);
    }

    private void setRightControlBox() {
        if (isTornDown || transULView == null || transULView.getCheckButton() == null) return;

        checkButtonHandler = e -> {
            if (isTornDown || transULExercise == null || transULView == null) return;
            checkExer();
        };
        transULView.getCheckButton().setOnAction(checkButtonHandler);

        if (transULView.getstaticHelpButton() != null && checkSetup != null) {
            transULView.getstaticHelpButton().setDisable((!checkSetup.isStaticHelp()));

            staticHelpButtonHandler = e -> {
                if (isTornDown || transULExercise == null || transULView == null || checkSetup == null) return;
                Stage helpStage = transULView.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else transULView.showStaticHelp(checkSetup.getStaticHelpDoc());
            };
            transULView.getstaticHelpButton().setOnAction(staticHelpButtonHandler);
        }
    }

    private boolean checkExer() {
        if (isTornDown || transULExercise == null || transULView == null || transULExercise.getMainWindow() == null) return false;

        transULView.deactivateBigCheck();
        if (!transULExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            updateCheckCounter();
        }

        if (transULView.getUlineBox() != null) {
            ulTreeObjs = transULView.getUlineBox().getTreeObjects();
        }

        choiceAMade = transULView.getaCheckBox() != null && transULView.getaCheckBox().isSelected();
        choiceBMade = transULView.getbCheckBox() != null && transULView.getbCheckBox().isSelected();
        choiceMade = choiceAMade || choiceBMade;

        checkSuccess = true;

        if (!checkChoiceMade()) checkSuccess = false;
        else if (!checkChoice()) checkSuccess = false;
        else {
            ULTreeObj treeObj = processULTree(ulTreeObjs);
            if (treeObj == null) checkSuccess = false;

            else {
                try {
                    checkUnderlines(exerKeyTreeObj, treeObj);
                }
                catch (TextMessageException e) {
                    checkSuccess = false;
                    EditorAlerts.showSimpleTxtListAlert("Underline Structure", e.getMessageList());
                }
            }

            if (checkSuccess && !checkMainOp()) checkSuccess = false;
        }

        if (checkSuccess) {
            transULView.activateBigCheck();
            return true;
        }
        else {
            return false;
        }
    }

    private void checkUnderlines(ULTreeObj keyObj, ULTreeObj ansObj) throws TextMessageException {
        if (isTornDown) return;
        if (keyObj == null && ansObj == null) return;
        if (keyObj == null || ansObj == null) return;

        //check nodes
        if (!keyObj.getMainOperator().equals("") && ansObj.getMainOperator().equals("")) {
            List<Text> textList = new ArrayList<>();
            textList.add(ParseUtilities.newRegularText(ansObj.getText()));
            textList.add(ParseUtilities.newRegularText("\n\nis not simple.  Add line(s)."));
            throw new TextMessageException(textList);
        }
        if (keyObj.getMainOperator().equals("") && !ansObj.getMainOperator().equals("")) {
            List<Text> textList = new ArrayList<>();
            textList.add(ParseUtilities.newRegularText(ansObj.getText()));
            textList.add(ParseUtilities.newRegularText("\n\nis not compound.  Remove line(s)."));
            throw new TextMessageException(textList);
        }

        if (!keyObj.getMainOperator().equals(ansObj.getMainOperator()) || keyObj.getChildren().size() != ansObj.getChildren().size()) {
            List<Text> textList = new ArrayList<>();
            textList.add(ParseUtilities.newRegularText(ansObj.getText()));
            textList.add(ParseUtilities.newRegularText("\n\ndoes not break into\n\n"));
            for (int j = 0; j < ansObj.getChildren().size(); j++) {
                textList.add(ParseUtilities.newUnderlineText(ansObj.getChildren().get(j).getText() + "\n"));
            }
            textList.add(ParseUtilities.newRegularText("\n\nwith main operator: " + ansObj.getMainOperator()));
            throw new TextMessageException(textList);
        }

        for (int k = 0; k < keyObj.getChildren().size(); k++) {
            if (!cleanString(keyObj.getChildren().get(k).getText()).equals(cleanString(ansObj.getChildren().get(k).getText()))) {
                List<Text> textList = new ArrayList<>();
                textList.add(ParseUtilities.newRegularText(ansObj.getText()));
                textList.add(ParseUtilities.newRegularText("\n\ndoes not break into\n\n"));
                for (int j = 0; j < ansObj.getChildren().size(); j++) {
                    textList.add(ParseUtilities.newUnderlineText("    " + ansObj.getChildren().get(j).getText() + "\n"));
                }
                textList.add(ParseUtilities.newRegularText("\nwith main operator:  " + ansObj.getMainOperator()));
                throw new TextMessageException(textList);
            }
        }

        for (int i = 0; i < keyObj.getChildren().size(); i++) {
            ULTreeObj keyChild = keyObj.getChildren().get(i);
            ULTreeObj ansChild = ansObj.getChildren().get(i);
            checkUnderlines(keyChild, ansChild);
        }
    }

    private boolean checkMainOp() {
        if (isTornDown || transULView == null || transULView.getMainOperatorBDRTA() == null || transULView.getMainOperatorBDRTA().getRTA() == null) return false;

        RichTextArea mainOpRTA = transULView.getMainOperatorBDRTA().getRTA();
        mainOpRTA.getActionFactory().saveNow().execute(new ActionEvent());
        String answerString = mainOpRTA.getDocument().getText();
        String lowerCaseAnswer = answerString.toLowerCase();
        String punctFreeAnswer = lowerCaseAnswer.replaceAll("[^\\w\\s_]", "");
        String cleanAnswer = punctFreeAnswer.replaceAll("\\s+", " ").trim();

        String keyString = new String(exerKeyTreeObj.getMainOperator());

        String regexString1 = keyString.replace("__ ", "__\\s*");
        String regexString2 = regexString1.replace(" __", "\\s*__");
        String regexString3 = regexString2.replace("__", "\\_+");

        Pattern pattern;
        try {
            pattern = Pattern.compile(regexString3);
        }
        catch (Exception e) {
            System.out.println("bad main operator regex: " + keyString);
            return false;
        }

        Matcher matcher = pattern.matcher(cleanAnswer);
        if (matcher.matches()) {
            return true;
        }
        else {
            if (transULView.getMainOperatorBDRTA() != null) {
                transULView.getMainOperatorBDRTA().setVTtreeBoxHighlight();
                scrollToNode(transULView.getMainOperatorBDRTA().getRTA());
                EditorAlerts.showSimpleTxtListAlert("Main Operator:", Collections.singletonList(ParseUtilities.newRegularText("This blank does not contain the main operator (the operator not in the blank of any other).")));
                transULView.getMainOperatorBDRTA().resetVTtreeBoxHighlight();
            }
            return false;
        }
    }

    public static ULTreeObj processULTree(List<ULTreeObj> objList) {
        if (objList == null || objList.isEmpty()) return null;

        //adjust indexes, set texts
        String rootString = objList.get(0).getText();

        for (ULTreeObj treeObj : objList) {
            int startIndex = treeObj.getIndexMin();
            int endIndex = treeObj.getIndexMax();
            if (0 < startIndex && startIndex + 1 < rootString.length() && rootString.codePointCount(startIndex -1, startIndex + 1) == 1) startIndex++;
            if (0 < endIndex && endIndex + 1 < rootString.length() && rootString.codePointCount(endIndex -1, endIndex + 1) == 1) endIndex++;
            String objString = rootString.substring(startIndex, endIndex);
            treeObj.setText(objString);
            treeObj.getChildren().clear();
            treeObj.setMainOperator("");
        }



        //check inclusions, set children
        for (int i = 1; i < objList.size(); i++) {
            ULTreeObj candidate = objList.get(i);
            boolean included = false;
            for (ULTreeObj base : objList) {
                if (base.getLevel() + 1 == candidate.getLevel()) {
                    if (!base.getChildren().contains(candidate)) {
                        if ((candidate.getIndexMin() >= base.getIndexMin() && candidate.getIndexMax() < base.getIndexMax()) ||
                                (candidate.getIndexMin() > base.getIndexMin() && candidate.getIndexMax() <= base.getIndexMax())) {
                            included = true;

                            int index = 0;
                            List<ULTreeObj> children = base.getChildren();
                            for (int j = 0; j < children.size(); j++) {
                                ULTreeObj child = children.get(j);
                                if (child.getIndexMin() > candidate.getIndexMin()) {
                                    index = j;
                                    break;
                                }
                                index = j + 1;
                            }
                            children.add(index, candidate);
                        }
                    }
                    else included = true;
                }
            }
            if (!included) {
                EditorAlerts.showSimpleTxtListAlert("Underline Structure", Collections.singletonList(ParseUtilities.newRegularText("One or more lines not completely \"included\" in ones below.")));
                return null;
            }
        }

        ULTreeObj rObj = objList.get(0);

        //check for complete sentences
        if (rObj.getText().equals("")) {
            if (!emptyOK) {
                EditorAlerts.showSimpleTxtListAlert("Expression Content:", Collections.singletonList(ParseUtilities.newRegularText("Cannot process an empty expression.")));
                return null;
            }
        }
        else {
            if (validator != null) {
                if (!validator.isCompleteSentence(rObj.getText())) {
                    List<Text> textList = new ArrayList<>();
                    textList.add(ParseUtilities.newRegularText("Cannot identify the boxed expression\n\n    "));
                    textList.add(ParseUtilities.newRegularText(rObj.getText()));
                    textList.add(ParseUtilities.newRegularText("\n\nas a complete sentence."));
                    EditorAlerts.showSimpleTxtListAlert("Expression Content:", textList);
                    return null;
                }

                try {
                    traverseTreeForSentences(rObj);
                } catch (TextMessageException e) {
                    List<Text> textList = new ArrayList<>();
                    textList.add(ParseUtilities.newRegularText("Cannot identify the underlined expression\n\n    "));
                    textList.addAll(e.getMessageList());
                    textList.add(ParseUtilities.newRegularText("\n\nas a complete sentence."));
                    EditorAlerts.showSimpleTxtListAlert("Expression Content", textList);
                    return null;
                }
            } else {
                EditorAlerts.fleetingRedPopup("No sentence validator");
            }
        }

        //identify main operators
        updateTreeOperators(rObj);

        return rObj;
    }

    private static void updateTreeOperators(ULTreeObj obj) {
        if (obj == null) return;

        String cleanParent = cleanString(obj.getText());
        for (int i = 0; i < obj.getChildren().size(); i++) {
            ULTreeObj childObj = obj.getChildren().get(i);
            String cleanChild = cleanString(childObj.getText());
            cleanParent = cleanParent.replaceFirst(cleanChild, "__");
        }
        if (obj.getChildren().size() > 0) {
            obj.setMainOperator(cleanParent);
        }

        for (ULTreeObj child : obj.getChildren()) {
            updateTreeOperators(child);
        }
    }

    private static void traverseTreeForSentences(ULTreeObj obj) throws TextMessageException {
        if (obj == null) return;

        if (validator != null && !validator.isCompleteSentence(obj.getText())) {
            throw new TextMessageException(Collections.singletonList(ParseUtilities.newUnderlineText(obj.getText())));
        }

        for (ULTreeObj child : obj.getChildren()) {
            traverseTreeForSentences(child);
        }
    }

    //lowercase, remove punctuation, extra spaces
    private static String cleanString(String string) {
        if (string == null) return "";
        String lowercaseString = string.toLowerCase();
        String noPunctString = lowercaseString.replaceAll("[\\p{Punct}]", "");
        String noDSpaceString = noPunctString.replaceAll("\\s+", " ").trim();
        return noDSpaceString;
    }

    private boolean checkChoiceMade() {
        if (isTornDown || transULView == null) return false;

        boolean success = true;

        if (!choiceMade) {
            if (transULView.getaCheckBox() != null) transULView.getaCheckBox().setStyle("-fx-background-color: mistyrose");
            if (transULView.getbCheckBox() != null) transULView.getbCheckBox().setStyle("-fx-background-color: mistyrose");
            if (transULView.getaCheckBox() != null) scrollToNode(transULView.getaCheckBox());

            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("No choice made.")));

            if (transULView.getaCheckBox() != null) transULView.getaCheckBox().setStyle("-fx-background-color: white");
            if (transULView.getbCheckBox() != null) transULView.getbCheckBox().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    private boolean checkChoice() {
        if (isTornDown || transULView == null) return false;

        boolean success = true;

        if (choiceAMade && !choiceASet) {
            if (transULView.getaCheckBox() != null) {
                transULView.getaCheckBox().setStyle("-fx-background-color: mistyrose");
                scrollToNode(transULView.getaCheckBox());

            }
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            if (transULView.getaCheckBox() != null) transULView.getaCheckBox().setStyle("-fx-background-color: white");
            success = false;
        }
        if (choiceBMade && !choiceBSet) {
            if (transULView.getbCheckBox() != null) {
                transULView.getbCheckBox().setStyle("-fx-background-color: mistyrose");
                scrollToNode(transULView.getbCheckBox());
            }
            EditorAlerts.showSimpleTxtListAlert("Choices", Collections.singletonList(ParseUtilities.newRegularText("Mistaken choice.")));
            if (transULView.getbCheckBox() != null) transULView.getbCheckBox().setStyle("-fx-background-color: white");
            success = false;
        }
        return success;
    }

    public boolean isCheckSuccess() {
        return checkSuccess;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public static void setEmptyOK(boolean emptyOK) {
        TransULCheck.emptyOK = emptyOK;
    }

    // SCORCHED EARTH RULE 1 & 5: Cleanup cascading and isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Safe Global State & Null-Poisoning Prevention
        try { MatchUtilities.clearMatching(); } catch (Throwable e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try {
            if (transULView != null && transULView.getCheckButton() != null && checkButtonHandler != null) {
                transULView.getCheckButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (transULView != null && transULView.getstaticHelpButton() != null && staticHelpButtonHandler != null) {
                transULView.getstaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive Data Teardown
        // CRITICAL: Do NOT call .clear() on ulTreeObjs! It deletes the Model's persistent data!
        // We only break the local reference so the Check class can be garbage collected.
        try { this.ulTreeObjs = null; } catch (Exception e) {}
        try { this.exerKeyTreeObj = null; } catch (Exception e) {}

        try { this.transULExercise = null; } catch (Exception e) {}
        try { this.transULView = null; } catch (Exception e) {}
        try { this.transULModel = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
    }
}
