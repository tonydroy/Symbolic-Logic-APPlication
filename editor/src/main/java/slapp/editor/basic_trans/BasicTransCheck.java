
package slapp.editor.basic_trans;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.ParaphraseService;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;
import slapp.editor.trans_tree.InterpFnElement;
import slapp.editor.trans_tree.TransTreeCheck;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.concurrent.atomic.AtomicBoolean;


public class BasicTransCheck {
    BasicTransExercise exercise;
    BasicTransView view;
    BasicTransModel model;
    BTransCheckSetup checkSetup;

    private int checkMax;
    private int checkTries;
    private boolean checkFinal;
    private boolean checkSuccess = false;
    private Language objLang;
    private String objLangName;

    private String mLangName = "Meta Sym";
    private Language mLang = Languages.getLanguage("Meta Sym");

    private RichTextArea interpFnRTA = null;
    private List<Formula> targetFormulas = new ArrayList<>();

    private Document keyInterpFnDoc;

    private ParaphraseService service;

    private Map<String, SimpleBooleanProperty> langMap;

    private KeyObject[] keyObjects = new KeyObject[4];
    private Thread checkThread;
    private DecoratedRTA transDRTA;

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    private boolean requireUniv = true;

    private List<Formula> targetMatchFormulas;

    private List<Expression> transExpressions;
    private List<List<Text>> transTextLists = new ArrayList<>();

    BasicTransCheck(BasicTransExercise exercise) {
        this.exercise = exercise;
        this.view = exercise.getExerciseView();
        this.model = exercise.getExerciseModel();


        checkSetup = model.getCheckSetup();
        if (checkSetup == null) {
            checkSetup = new BTransCheckSetup();
            model.setCheckSetup(checkSetup);
        }
        setRightControlBox();

        if (checkSetup.isCheckSuccess()) {
            checkSuccess = true;
            view.setCheckMessage(checkSetup.getCheckMessage());
            if (checkSetup.isCheckGreen()) {
                view.setCheckColor(Color.LAWNGREEN);
                view.setCheckSubtitleColor(Color.GREEN);
            } else {
                view.setCheckColor(Color.ORCHID);
                view.setCheckSubtitleColor(Color.PURPLE);
            }
            view.activateBigCheck();
        }

        checkTries = checkSetup.getCheckTries();
        checkMax = checkSetup.getCheckMax();
        setChecksCounter();
        this.objLangName = checkSetup.getObjLangName();
        this.objLang = Languages.getLanguage(objLangName);

        List<String> sentLangNames = Languages.getSententialLangNames();
        if (sentLangNames.contains(objLangName)) requireUniv = false;

        this.mLangName = checkSetup.getMetLangName();
        this.mLang = Languages.getLanguage(mLangName);

        targetFormulas = new ArrayList<>();
        keyInterpFnDoc = checkSetup.getKeyInterpFnDoc();
    }

    private void setChecksCounter() {
        if (isTornDown || view == null || view.getCheckButton() == null) return;
        if (checkMax != -1 && checkTries >= checkMax && exercise != null && exercise.getMainWindow() != null && !exercise.getMainWindow().isInstructorFunctions()) {
            view.getCheckButton().setDisable(true);
        } else {
            view.getCheckButton().setDisable(false);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        if (view.getCheckTriesLabel() != null) view.getCheckTriesLabel().setText(checkString);
    }

    private void checkSentence() {

        if (isTornDown || view == null) return;
        checkSuccess = false;
        checkSetup.setCheckSuccess(false);
        if (view.getCheckButton() != null) view.getCheckButton().setDisable(true);
        view.prepareForNewCheck();
        if (exercise != null && exercise.getMainWindow() != null && !exercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        transDRTA = view.getExerciseResponse();
        if (transDRTA == null || transDRTA.getEditor() == null) {
            setChecksCounter();
            return;
        }
        RichTextArea transRTA = transDRTA.getEditor();
        if (transRTA.isModified() && exercise != null) exercise.setExerciseModified(true);
        transRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document transDoc = transRTA.getDocument();
        List<Expression> transExpressions = ParseUtilities.parseDoc(transDoc, checkSetup.getObjLangName());

        if (transExpressions.isEmpty()) {
            EditorAlerts.fleetingRedPopup("Empty translation field.");
            setChecksCounter();
            return;
        }

        Expression transSent = transExpressions.get(0);
        if (transExpressions.size() != 1 || !(transSent instanceof Formula) || !SyntacticalFns.sentence(transSent, objLangName)) {
            transDRTA.setBoxHighlight();
            List<Text> texts = new ArrayList<>();
            texts.add(ParseUtilities.newRegularText("Expression is not a sentence of "));
            texts.addAll(Languages.getLanguage(checkSetup.getObjLangName()).getNameTextList());
            texts.add(ParseUtilities.newRegularText(".\n\nFound:\n"));
            for (Expression exp : transExpressions) {
                texts.add(new Text("    "));
                texts.add(new Text(exp.getType() + ": "));
                texts.addAll(exp.toTextList());
                texts.add(new Text("\n"));
            }

            EditorAlerts.showSimpleTxtListAlert("Not a sentence:", texts);
            transDRTA.resetBoxHighlight();
            setChecksCounter();
            return;
        }

        setSuperscripts(transSent);

        List<Document> targetDocs = checkSetup.getTargetDocs();
        targetFormulas = new ArrayList<>();
        for (int i = 0; i < targetDocs.size(); i++) {
            List<Expression> targetExpressions = new ArrayList<>();
            if (!model.isInterpFnIncluded()) targetExpressions = ParseUtilities.parseDoc(targetDocs.get(i), mLang);
            else targetExpressions = ParseUtilities.parseDoc(targetDocs.get(i), "Mvar" + objLangName);

            if (!targetExpressions.isEmpty()) {
                Formula targetFormula = (Formula) (targetExpressions.get(0));
                targetFormulas.add(targetFormula);
            }
        }

        if (view.getExerciseInterpretation() == null) {
            setChecksCounter();
            return;
        }
        RichTextArea interpretationRTA = view.getExerciseInterpretation().getEditor();

        service = ParaphraseService.getInstance().join();

        // SCORCHED EARTH RULE 7: Safe Global State Clearance
        MatchUtilities.clearMatching();

        if (interpretationRTA.isModified() && exercise != null) exercise.setExerciseModified(true);
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        List<InterpFnElement> interpFnElements = TransTreeCheck.processInterpFn(interpretationRTA.getDocument(), objLang);
        if (interpFnElements == null) {
            setChecksCounter();
            return;
        } else {
            if (!TransTreeCheck.verifyInterpFnAdequacy(interpFnElements, transDoc, objLang, requireUniv)) {
                setChecksCounter();
                return;
            }
        }

        if (!model.isInterpFnIncluded()) {
            List<InterpFnElement> keyInterpFnElements = TransTreeCheck.processInterpFn(keyInterpFnDoc, mLang);
            if (keyInterpFnElements == null) {
                setChecksCounter();
                return;
            }

            for (InterpFnElement keyElement : keyInterpFnElements) {
                keyElement.setFound(false);
            }
            if (!interpFnInKey(interpFnElements, keyInterpFnElements)) {
                setChecksCounter();
                return;
            }
        }

        MVariable.clear();
        for (int i = 0; i < MVariable.getVariables().size(); i++) {
            MVariable mVar = MVariable.getVariables().get(i);
            try {
                mVar.setMatch(new VariableSym("\ud835\udc56", String.valueOf(i + 1)));
            } catch (TextMessageException e) {
                System.out.println("matching error");
            }
        }

        List<Formula> targetMatchFormulas = new ArrayList<>();
        for (Formula targetFormula : targetFormulas) {
            targetMatchFormulas.add(targetFormula.getMatch());
        }

        Expression standardizedTrans = renameVariables((Expression) transSent, new HashMap<>(), new int[]{1});

        for (Formula targetMatchFormula : targetMatchFormulas) {
            Expression standardizedTarget = renameVariables((Expression) targetMatchFormula, new HashMap<>(), new int[]{1});

            if (standardizedTarget.equals(standardizedTrans)) {
                String checkMessage = "Natural and Good";
                checkSetup.setCheckMessage(checkMessage);
                checkSetup.setCheckGreen(true);
                checkSetup.setCheckSuccess(true);
                view.setCheckColor(Color.LAWNGREEN);
                view.setCheckSubtitleColor(Color.GREEN);
                view.setCheckMessage(checkMessage);
                view.activateBigCheck();
                setChecksCounter();
                return;
            }
        }

        view.activateProcessingIndicator();

        checkKeyEquivalence(targetMatchFormulas.get(0), (Formula) transSent, checkSetup, 0, (Boolean result, Integer checkedCount) -> {
            if (isTornDown || view == null) return;
            view.deactivateProcessingIndicator();

            if (result == null) {
                // TIMEOUT OR INTERRUPTED

            } else if (result) {
                // FULL SUCCESS
            } else {
                // FAILED
                checkedCount = -1;
            }

            if (checkedCount > 1) {
                String checkMessage = "";
                if (requireUniv) {
                    checkMessage = "Good (univ up to " + checkedCount + ")";
                }
                else {
                    checkMessage = "Good";
                }
                checkSetup.setCheckMessage(checkMessage);
                checkSetup.setCheckGreen(false);
                checkSetup.setCheckSuccess(true);
                view.setCheckColor(Color.ORCHID);
                view.setCheckSubtitleColor(Color.PURPLE);
                view.setCheckMessage(checkMessage);
                view.activateBigCheck();
            }
            else if (result == null) {
                // ADD THIS FALLBACK FOR checkSentence()
                // Catches the NPE missing-symbol crash (or an extreme timeout on univ size 1)
                transDRTA.setBoxHighlight();
                EditorAlerts.showSimpleTxtListAlert("Translation", Collections.singletonList(
                        ParseUtilities.newRegularText("Problem in translation.  You may be missing a required symbol in your interpretation function.")
                ));
                transDRTA.resetBoxHighlight();
            }
            setChecksCounter();
        });
    }

    private void paraphraseTest() {
        List<Pair<String, String>> stringPairs = new ArrayList<>();
        Pair<String, String> pair1 = new Pair("bob loves sue", "sue loves bob");
        Pair<String, String> pair2 = new Pair("bob loves sue", "sue is loved by bob");
        Pair<String, String> pair3 = new Pair("the dog is chasing the cat", "the cat is chasing the dog");
        Pair<String, String> pair4 = new Pair("1 is greater than 2", "2 is greater than 1");
        Pair<String, String> pair5 = new Pair("one is greater than two", "two is greater than one");
        Pair<String, String> pair6 = new Pair("1 is greater than 2", "one is greater than two");
        stringPairs.add(pair1); stringPairs.add(pair2); stringPairs.add(pair3); stringPairs.add(pair4);   stringPairs.add(pair5);  stringPairs.add(pair6);

        for (Pair<String, String> pair : stringPairs) {
            String key = pair.getKey();
            String value = pair.getValue();
            System.out.println(key + " / " + value + " : " + service.getSimilarityScore(key, value));
        }
    }

    private void checkArgument() {

        if (isTornDown || view == null) return;
        checkSuccess = false;
        checkSetup.setCheckSuccess(false);
        if (view.getCheckButton() != null) view.getCheckButton().setDisable(true);
        view.prepareForNewCheck();
        if (exercise != null && exercise.getMainWindow() != null && !exercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }

        //process translation expressions
        transDRTA = view.getExerciseResponse();
        if (transDRTA == null || transDRTA.getEditor() == null) {
            setChecksCounter();
            return;
        }
        RichTextArea transRTA = transDRTA.getEditor();
        if (transRTA.isModified() && exercise != null) exercise.setExerciseModified(true);
        transRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document transDoc = transRTA.getDocument();
        transExpressions = ParseUtilities.parseDoc(transDoc, checkSetup.getObjLangName());

        if (!verifyTransExp(transExpressions)) {
            setChecksCounter();
            return;
        }

        transTextLists.clear();
        for (int i = 0; i < transExpressions.size(); i++) {
            Expression exp = transExpressions.get(i);
            transTextLists.add(exp.toTextList());
        }

        for (Expression exp : transExpressions) {
            setSuperscripts(exp);
        }


        //process target expressions
        List<Document> targetDocs = checkSetup.getTargetDocs();
        targetFormulas = new ArrayList<>();
        for (int i = 0; i < targetDocs.size(); i++) {
            List<Expression> targetExpressions = new ArrayList<>();
            if (!model.isInterpFnIncluded()) targetExpressions = ParseUtilities.parseDoc(targetDocs.get(i), mLang);
            else targetExpressions = ParseUtilities.parseDoc(targetDocs.get(i), "Mvar" + objLangName);

            if (!targetExpressions.isEmpty()) {
                Formula targetFormula = (Formula) (targetExpressions.get(0));
                targetFormulas.add(targetFormula);
            }
        }

        //process interpretation function
        if (view.getExerciseInterpretation() == null) {
            setChecksCounter();
            return;
        }
        RichTextArea interpretationRTA = view.getExerciseInterpretation().getEditor();
        // SCORCHED EARTH RULE 7: Safe Global State Clearance
        MatchUtilities.clearMatching();

        service = ParaphraseService.getInstance().join();

   //     paraphraseTest();

        if (interpretationRTA.isModified() && exercise != null) exercise.setExerciseModified(true);
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());

        List<InterpFnElement> interpFnElements = TransTreeCheck.processInterpFn(interpretationRTA.getDocument(), objLang);
        if (interpFnElements == null) {
            setChecksCounter();
            return;
        }
        else {
            for (Expression exp : transExpressions) {
                if (!TransTreeCheck.verifyInterpFnAdequacy(interpFnElements, (Formula) exp, objLang, requireUniv)) {
                    setChecksCounter();
                    return;
                }
            }
        }

        if (!model.isInterpFnIncluded()) {
            List<InterpFnElement> keyInterpFnElements = TransTreeCheck.processInterpFn(keyInterpFnDoc, mLang);
            if (keyInterpFnElements == null) {
                setChecksCounter();
                return;
            }

            for (InterpFnElement keyElement : keyInterpFnElements) {
                keyElement.setFound(false);
            }
            if (!interpFnInKey(interpFnElements, keyInterpFnElements)) {
                setChecksCounter();
                return;
            }
        }

        MVariable.clear();
        for (int i = 0; i < MVariable.getVariables().size(); i++) {
            MVariable mVar = MVariable.getVariables().get(i);
            try {
                mVar.setMatch(new VariableSym("\ud835\udc56", String.valueOf(i + 1)));
            } catch (TextMessageException e) {
                System.out.println("matching error");
            }
        }

        targetMatchFormulas = new ArrayList<>();
        for (Formula targetFormula : targetFormulas) {
            targetMatchFormulas.add(targetFormula.getMatch());
        }

        checkNextFormula(0, false, Integer.MAX_VALUE);

     //   setChecksCounter();
    }

    private void checkNextFormula(int index, boolean neededEquivalence, int minCheckedCount) {
        // 1. Safety check to prevent rogue callbacks acting on a closed view
        if (isTornDown || view == null) return;

        // 2. BASE CASE: The "Loop" is finished. We reached the end of the lists successfully!
        if (index >= targetMatchFormulas.size()) {
            view.deactivateProcessingIndicator();

            if (!neededEquivalence) {
                String checkMessage = "Natural and Good";
                checkSetup.setCheckMessage(checkMessage);
                checkSetup.setCheckGreen(true);
                checkSetup.setCheckSuccess(true);
                view.setCheckColor(Color.LAWNGREEN);
                view.setCheckSubtitleColor(Color.GREEN);
                view.setCheckMessage(checkMessage);
            } else {
                String checkMessage = requireUniv ? "Good (univ up to " + minCheckedCount + ")" : "Good";
                checkSetup.setCheckMessage(checkMessage);
                checkSetup.setCheckGreen(false);
                checkSetup.setCheckSuccess(true);
                view.setCheckColor(Color.ORCHID);
                view.setCheckSubtitleColor(Color.PURPLE);
                view.setCheckMessage(checkMessage);
            }
            view.activateBigCheck();

            setChecksCounter();
            return;
        }

        // 3. PROCESS THE CURRENT INDEX
        Expression standardizedTrans = renameVariables(transExpressions.get(index), new HashMap<>(), new int[]{1});
        Expression standardizedTarget = renameVariables((Expression) targetMatchFormulas.get(index), new HashMap<>(), new int[]{1});

        if (standardizedTarget.equals(standardizedTrans)) {
            // SYNCHRONOUS EXACT MATCH:
            // Immediately move to the next index, passing along the current state
            checkNextFormula(index + 1, neededEquivalence, minCheckedCount);
        }
        else {
            // ASYNCHRONOUS CHECK REQUIRED:
            view.activateProcessingIndicator();

            // Note: Casting to (Formula) depending on your method's exact signature
            checkKeyEquivalence((Formula) standardizedTarget, (Formula) standardizedTrans, checkSetup, 0, (Boolean result, Integer checkedCount) -> {
                if (isTornDown || view == null) return;

                if (result == null) {
                    // TIMEOUT OR INTERRUPTED

            //        view.deactivateProcessingIndicator();
             //       return;
                } else if (result) {
                    // FULL SUCCESS
                } else {
                    // FAILED
                    checkedCount = -1;
                }

                if (checkedCount > 1) {
                    // SUCCESS for this specific formula!
                    // Track the lowest universe size checked across all formulas in the list
                    int nextMinCount = Math.min(minCheckedCount, checkedCount);

                    // Trigger the check for the NEXT index from INSIDE the callback.
                    // We pass 'true' because equivalence was used to pass.
                    checkNextFormula(index + 1, true, nextMinCount);

                } else {
                    view.deactivateProcessingIndicator();
                    transDRTA.setBoxHighlight();
                    if (result == null) {
                        EditorAlerts.showSimpleTxtListAlert("Translation", Collections.singletonList(
                                ParseUtilities.newRegularText("Problem in translation.  You may be missing a required symbol in your interpretation function.")
                        ));
                    } else {
                        List<Text> texts = new ArrayList<>();
                        texts.addAll(transTextLists.get(index));
                        texts.add(ParseUtilities.newRegularText(" is not a good translation of sentence #"));
                        texts.addAll(Collections.singletonList(ParseUtilities.newRegularText(Integer.toString(index + 1) + ".")));
                        EditorAlerts.showSimpleTxtListAlert("Translation:", texts);
                    }
                    transDRTA.resetBoxHighlight();
                    setChecksCounter();
                }
            });
        }
    }

    private boolean verifyTransExp(List<Expression> transExpressions) {

        //check empty
        if (transExpressions.isEmpty()) {
            transDRTA.setBoxHighlight();
            EditorAlerts.showSimpleTxtListAlert("No Translation:", Collections.singletonList(ParseUtilities.newRegularText("Empty translation field.")));
            transDRTA.resetBoxHighlight();
            return false;
        }

        //remove divider line
        int start = -1;
        int end = -1;
        for (int i = 0; i < transExpressions.size(); i++) {
            Expression exp = transExpressions.get(i);
            if (exp.getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) exp).getElementStr().equals("-")) {
                start = i;
                break;
            }
        }
        if (start >= 0) {
            for (end = start + 1; end < transExpressions.size(); end++) {
                Expression exp = transExpressions.get(end);
                if (!(exp.getType() == ExpressionType.ORIGINAL_ELEMENT && ((OriginalElement) exp).getElementStr().equals("-"))) {
                    break;
                }
            }
            transExpressions.subList(start, end).clear();
        }


        //verify all formulas
        for (Expression exp : transExpressions) {
            if (!(exp instanceof Formula)) {
                transDRTA.setBoxHighlight();
                List<Text> texts = new ArrayList<>();
                texts.add(ParseUtilities.newRegularText("Some expression is not a formula of "));
                texts.addAll(Languages.getLanguage(checkSetup.getObjLangName()).getNameTextList());
                String insertStr = "";
                if (start > 0) {texts.add(ParseUtilities.newRegularText(".\n\nExcluding divider, found:\n"));}
                else texts.add(ParseUtilities.newRegularText(".\n\nFound:\n"));
                for (Expression expr : transExpressions) {
                    texts.add(new Text("    "));
                    texts.add(new Text(expr.getType() + ": "));
                    texts.addAll(expr.toTextList());
                    texts.add(new Text("\n"));
                }
                EditorAlerts.showSimpleTxtListAlert("Not Formula:", texts);
                transDRTA.resetBoxHighlight();
                return false;
            }
        }

        for (Expression exp : transExpressions) {
            if (!SyntacticalFns.sentence(exp, objLangName)) {
                transDRTA.setBoxHighlight();
                List<Text> texts = new ArrayList<>();
                texts.addAll(exp.toTextList());
                texts.add(ParseUtilities.newRegularText((" is a formula but not a sentence.")));
                EditorAlerts.showSimpleTxtListAlert("Not Sentence:", texts);
                transDRTA.resetBoxHighlight();
                return false;
            }
        }

        if (transExpressions.size() != checkSetup.getTargetDocs().size()) {
            transDRTA.setBoxHighlight();
            EditorAlerts.showSimpleTxtListAlert("Number Mismatch:", Collections.singletonList(ParseUtilities.newRegularText("Expect exactly one formal sentence for each ordinary sentence.")));
            transDRTA.resetBoxHighlight();
            return false;
        }
        return true;
    }



    private static Expression renameVariables(Expression node, Map<VariableSym, VariableSym> dictionary, int[] counter) {
        if (node instanceof Formula) {
            Formula formula = (Formula) node;

            if (formula.getMainOperator() instanceof UniversalOp || formula.getMainOperator() instanceof ExistentialOp) {
                Operator mainOperator = formula.getMainOperator();
                Term quantifiedVariableTerm = mainOperator.getVariableTerm();

                VariableSym quantVariableSym = (VariableSym) (quantifiedVariableTerm.getChildren().get(0));
                VariableSym newVariableSym = new VariableSym("\ud835\udc63", String.valueOf(counter[0]));
                counter[0]++;
                Map<VariableSym, VariableSym> photocopy = new HashMap<>(dictionary);
                photocopy.put(quantVariableSym, newVariableSym);
                quantifiedVariableTerm.getChildren().set(0, new VariableSym(newVariableSym.getBaseStr(), newVariableSym.getSubscriptStr()));

                Expression body = formula.getChildren().get(0);
                renameVariables(body, photocopy, counter);
                return formula;

            } else if (formula instanceof PrefixAtomic || formula instanceof InfixAtomic) {
                for (Expression term : formula.getChildren()) {
                    renameVariables(term, dictionary, counter);
                }
                return formula;
            } else if (formula instanceof SentenceAtomic) {
                return formula;
            } else if (formula.getMainOperator() instanceof NegationOp || formula.getMainOperator() instanceof ConditionalOp ||
                    formula.getMainOperator() instanceof ConjunctionOp || formula.getMainOperator() instanceof DisjunctionOp ||
                    formula.getMainOperator() instanceof BiconditionalOp) {

                for (Expression subFormula : formula.getChildren()) {
                    renameVariables(subFormula, dictionary, counter);
                }
                return formula;
            }
        } else if (node instanceof Term) {
            Term term = (Term) node;

            if (term.getTermType() == TermType.COMPLEX) {
                for (Expression subTerm : term.getChildren()) {
                    renameVariables(subTerm, dictionary, counter);
                }
                return term;
            } else if (term.getTermType() == TermType.CONSTANT) {
                return term;
            } else if (term.getTermType() == TermType.VARIABLE) {
                List<Expression> children = term.getChildren();
                VariableSym oldSymbol = (VariableSym) children.get(0);
                VariableSym newSymbol = dictionary.get(oldSymbol);
                if (newSymbol != null) children.set(0, new VariableSym(newSymbol.getBaseStr(), newSymbol.getSubscriptStr()));
                return term;
            }
        }

        return node;
    }

    private void setSuperscripts(Expression exp) {
        if (exp == null) return;
        if (exp instanceof PrefixAtomic) {
            PrefixAtomic atomic = (PrefixAtomic) exp;
            RelationSymbol relSym = atomic.getMainRelation();
            if (objLang != null && objLang.getXrelationSymbols().contains(relSym.getBaseStr())) relSym.setSuperscriptStr(String.valueOf(relSym.getPlaces()));
            return;
        }

        if (exp.getChildren() != null) {
            for (Expression child : exp.getChildren()) {
                setSuperscripts(child);
            }
        }
    }

    private void checkKeyEquivalence(Formula keyFormula, Formula formulaToCheck, BTransCheckSetup checkSetup, int targetIndex, BiConsumer<Boolean, Integer> onComplete) {
        if (isTornDown) return;

        checkThread = java.lang.Thread.startVirtualThread(() -> {
            final AtomicBoolean callbackFired = new AtomicBoolean(false);
            final BiConsumer<Boolean, Integer> safeCallback = (result, universesChecked) -> {
                if (isTornDown) return;
                if (callbackFired.compareAndSet(false, true)) {
                    if (onComplete != null && !isTornDown) {
                        Platform.runLater(() -> {
                            if (!isTornDown) onComplete.accept(result, universesChecked);
                        });
                    }
                }
            };

            final java.util.concurrent.atomic.AtomicInteger lastN = new java.util.concurrent.atomic.AtomicInteger(0);

            try {
                long startTime = System.currentTimeMillis();

                for (int i = 0; i < 4; i++) {
                    if (isTornDown || Thread.currentThread().isInterrupted()) {
                        safeCallback.accept(null, lastN.get());
                        return;
                    }
                    keyObjects[i] = EquivalenceChecker.getKeyObject(keyFormula, i + 1);

                    if (System.currentTimeMillis() - startTime > 15000) {
                        safeCallback.accept(null, lastN.get());
                        return;
                    }
                }

                if (isTornDown) {
                    safeCallback.accept(null, lastN.get());
                    return;
                }
                List<LanguageElement> combinedSyms = EquivalenceChecker.getCombinedSymbolList(keyFormula, formulaToCheck);

                for (int i = 0; i < 4; i++) {
                    final int currentIndex = i;

                    if (isTornDown || Thread.currentThread().isInterrupted()) {
                        safeCallback.accept(null, lastN.get());
                        return;
                    }

                    long timeElapsed = System.currentTimeMillis() - startTime;
                    long timeLeft = 15000 - timeElapsed;

                    if (timeLeft <= 0) {
                        safeCallback.accept(null, lastN.get());
                        return;
                    }

                    ModelBlueprint blueprint = new ModelBlueprint(combinedSyms, currentIndex + 1);
                    KeyObject keyObj = keyObjects[currentIndex];




                    if (keyObj != null) {
                        EquivalenceChecker.checkEquivalenceAgainstKey(keyObj, formulaToCheck, currentIndex + 1, combinedSyms, blueprint.infoMap, blueprint.bitToSymbolArray, blueprint.fullModelSize, timeLeft);
                    } else {
                        EquivalenceChecker.checkLargeParallel(keyFormula, formulaToCheck, currentIndex + 1, blueprint.fullSymList, blueprint.infoMap, blueprint.bitToSymbolArray, blueprint.fullModelSize, timeLeft);
                    }

                    if (isTornDown || Thread.currentThread().isInterrupted()) {
                        safeCallback.accept(null, lastN.get());
                        return;
                    }

                    lastN.set(currentIndex + 1);
                }

                if (!isTornDown && !java.lang.Thread.currentThread().isInterrupted()) {
                    safeCallback.accept(true, lastN.get());
                }

            } catch (ModelContentException modelExcep) {
                if (isTornDown) return;

                Platform.runLater(() -> {
                    if (!isTornDown && transDRTA != null) transDRTA.setBoxHighlight();
                });


                List<Text> modelMessageList = new ArrayList<>();

                if (requireUniv) {
                    modelMessageList.add(ParseUtilities.newRegularText("Consider a case where "));
                    modelMessageList.addAll(modelExcep.getUniverse());
                    modelMessageList.add(ParseUtilities.newRegularText(" and assignments are such that,\n"));
                } else {
                    modelMessageList.add(ParseUtilities.newRegularText("Consider a case where assignments are such that,\n"));
                }

                List<Pair<LanguageElement, List<Text>>> assignmentPairs = modelExcep.getAssignmentList();
                for (int j = 0; j < assignmentPairs.size(); j++) {
                    Pair<LanguageElement, List<Text>> pair = assignmentPairs.get(j);
                    modelMessageList.add(new Text("    "));
                    modelMessageList.addAll(pair.getKey().toTextList());
                    modelMessageList.add(new Text(": "));
                    modelMessageList.addAll(pair.getValue());
                    modelMessageList.add(new Text("\n"));
                }

                if (modelExcep.isProfTrue()) {
                    modelMessageList.add(ParseUtilities.newRegularText("Then the assigned sentence "));
                    modelMessageList.add(ParseUtilities.newRegularText("is true and the translation is false."));
                } else {
                    modelMessageList.add(ParseUtilities.newRegularText("Then the assigned sentence "));
                    modelMessageList.add(ParseUtilities.newRegularText("is false and the translation true."));
                }


                if (callbackFired.compareAndSet(false, true)) {
                    Platform.runLater(() -> {
                        if (isTornDown) return;
                        if (view != null) view.deactivateProcessingIndicator();
                        if (!checkSetup.isArgument()) EditorAlerts.showSimpleTxtListAlert("Equivalence Error:", modelMessageList);
                        if (transDRTA != null) transDRTA.resetBoxHighlight();

                        if (onComplete != null) {
                            onComplete.accept(false, lastN.get());
                        }
                    });
                }

            } catch (Exception e) {
                if (!java.lang.Thread.currentThread().isInterrupted() || e.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                } else {
                    Platform.runLater(() -> { if (!isTornDown) System.out.println("unexpected thread error"); });
                }
                safeCallback.accept(null, lastN.get());
            } finally {
                safeCallback.accept(null, lastN.get());
                endEquivThread();
            }
        });
    }

    private void endEquivThread() {
        try {
            if (checkThread != null) {
                if (checkThread.isAlive()) checkThread.interrupt();
                checkThread = null;
            }
        } catch (Exception e) {}
    }

    private boolean interpFnInKey(List<InterpFnElement> interpFnElements, List<InterpFnElement> keyInterpFnElements) {
        if (isTornDown || service == null) return false;
        for (InterpFnElement element : interpFnElements) {
            if (isTornDown) return false;
            String cleanAssignmentString = TransTreeCheck.cleanString(element.getAssignmentString());
            boolean good = false;

            if (element.getSymbol() instanceof SentenceLetter) {
                good = false;
                for (InterpFnElement keyElement : keyInterpFnElements) {
                    if (keyElement.getSymbol() instanceof SentenceLetter) {
                        String cleanKeyAssignmentString = TransTreeCheck.cleanString(keyElement.getAssignmentString());

                //        System.out.println(cleanAssignmentString + " / " + cleanKeyAssignmentString + " : " + service.getSimilarityScore(cleanAssignmentString, cleanKeyAssignmentString));

                        if (service.getSimilarityScore(cleanAssignmentString, cleanKeyAssignmentString) > .6) {
                            if (keyElement.isFound()) {
                                multipleInstanceError("the sentence '" + element.getAssignmentString() + "'");
                                return false;
                            } else {
                                keyElement.setFound(true);
                                try {
                                    ((MSentenceLetter) keyElement.getSymbol()).setMatch(((SentenceLetter) element.getSymbol()));
                                } catch (TextMessageException e) {
                                    System.out.println("matching error1");
                                    return false;
                                }
                                good = true;
                            }
                        }
                    }
                }
                if (!good) {
                    notInKeyError("the sentence '" + element.getAssignmentString() + "'");
                    return false;
                }
            }

            if (element.getSymbol() instanceof ConstantSym) {
                good = false;
                for (InterpFnElement keyElement: keyInterpFnElements) {
                    if (keyElement.getSymbol() instanceof ConstantSym) {
                        String cleanKeyAssignmentString = TransTreeCheck.cleanString((keyElement.getAssignmentString()));
                        if (cleanAssignmentString.equals(cleanKeyAssignmentString)) {
                            if (keyElement.isFound()) {
                                multipleInstanceError("the name '" + element.getAssignmentString() + "'");
                                return false;
                            } else {
                                keyElement.setFound(true);
                                try {
                                    ((MConstant) keyElement.getSymbol()).setMatch(((ConstantSym) element.getSymbol()));
                                } catch (TextMessageException e) {
                                    System.out.println("matching error2");
                                    return false;
                                }
                                good = true;
                            }
                        }
                    }
                }
                if (!good) {
                    notInKeyError("the name '" + element.getAssignmentString() + "'");
                    return false;
                }
            }

            if (element.getSymbol() instanceof RelationSymbol) {
                good = false;
                RelationSymbol relSym = (RelationSymbol) (element.getSymbol());

                for (InterpFnElement keyElement: keyInterpFnElements) {
                    if (keyElement.getSymbol() instanceof RelationSymbol) {
                        RelationSymbol keyRelSym = (RelationSymbol) (keyElement.getSymbol());
                        if (relSym.getPlaces() == keyRelSym.getPlaces()) {
                            MetObjVar.clear();
                            List<ObjVar> objVars = element.getObjVars();
                            List<ObjVar> keyObjVars = keyElement.getObjVars();
                            for (int i = 0; i < keyObjVars.size(); i++) {
                                try {
                                    ((MetObjVar) keyObjVars.get(i)).setMatch((ObjObjVar) (objVars.get(i)));
                                } catch (TextMessageException e) {
                                    System.out.println("match error 3");
                                }
                            }

                            String keyNameString = TransTreeCheck.cleanString(keyElement.buildMatchedCapNameCondition());
                            String nameString = TransTreeCheck.cleanString(element.buildCapNameCondition(element.getSetCondition()));

                            if (service.getSimilarityScore(keyNameString, nameString) > .6) {
                                if (keyElement.isFound()) {
                                    multipleInstanceError("the set condition '. . . " + element.getAssignmentString() + "}'");
                                    return false;
                                } else {
                                    keyElement.setFound(true);
                                    MRelationSymbol keySymbol = (MRelationSymbol) keyElement.getSymbol();
                                    RelationSymbol elementSymbol = (RelationSymbol) element.getSymbol();

                                    try {
                                        keySymbol.setMatch(elementSymbol);
                                    } catch (TextMessageException e) {
                                        System.out.println("matching error4");
                                        return false;
                                    }
                                    good = true;
                                }
                            }
                        }
                    }
                }
                if (!good) {
                    notInKeyError("the set condition '. . . " + element.getAssignmentString() + "}'");
                    return false;
                }
            }

            if (element.getSymbol() instanceof FunctionSymbol) {
                good = false;
                FunctionSymbol funcSym = (FunctionSymbol) (element.getSymbol());

                for (InterpFnElement keyElement: keyInterpFnElements) {
                    if (keyElement.getSymbol() instanceof FunctionSymbol) {
                        FunctionSymbol keyFuncSym = (FunctionSymbol) (keyElement.getSymbol());
                        if (funcSym.getPlaces() == keyFuncSym.getPlaces()) {
                            MetObjVar.clear();
                            List<ObjVar> objVars = element.getObjVars();
                            List<ObjVar> keyObjVars = keyElement.getObjVars();
                            for (int i = 0; i < keyObjVars.size(); i++) {
                                try {
                                    ((MetObjVar) keyObjVars.get(i)).setMatch((ObjObjVar) (objVars.get(i)));
                                } catch (TextMessageException e) {
                                    System.out.println("match error 5");
                                }
                            }

                            String keyNameString = TransTreeCheck.cleanString(keyElement.buildMatchedCapNameCondition());
                            String nameString = TransTreeCheck.cleanString(element.buildCapNameCondition(element.getSetCondition()));

                            if (service.getSimilarityScore(keyNameString, nameString) > .6) {
                                if (keyElement.isFound()) {
                                    multipleInstanceError("the set condition '. . . " + element.getAssignmentString() + "}'");
                                    return false;
                                } else {
                                    keyElement.setFound(true);
                                    try {
                                        ((MFunctionSymbol) keyElement.getSymbol()).setMatch(((FunctionSymbol) element.getSymbol()));
                                    } catch (TextMessageException e) {
                                        System.out.println("matching error6");
                                        return false;
                                    }
                                    good = true;
                                }
                            }
                        }
                    }
                }
                if (!good) {
                    notInKeyError("the set condition '. . . " + element.getAssignmentString() + "}'");
                    return false;
                }
            }
        }
        return true;
    }

    private void notInKeyError(String stringNotIncluded) {
        if (isTornDown) return;
        List<Text> textList = new ArrayList<>();
        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText("I do not recognize ")));
        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText(stringNotIncluded.trim())));
        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText(" as a proper member of the interpretation function (member of the key) for this exercise.")));
        EditorAlerts.showSimpleTxtListAlert("Interpretation Function", textList);
    }

    private void multipleInstanceError(String duplicateString) {
        if (isTornDown) return;
        List<Text> textList = new ArrayList<>();
        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText("There are multiple entries in this interpretation function matching ")));
        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText(duplicateString.trim())));
        textList.addAll(Collections.singletonList(ParseUtilities.newRegularText(".")));
        EditorAlerts.showSimpleTxtListAlert("Interpretation Function", textList);
    }

    private void setRightControlBox() {
        if (view == null || view.getCheckButton() == null) return;

        Button checkButton = view.getCheckButton();
        checkButton.setDisable(true);

        if (checkSetup.getCheckMax() == -1 || checkSetup.getCheckTries() < checkSetup.getCheckMax() || (exercise != null && exercise.getMainWindow() != null && exercise.getMainWindow().isInstructorFunctions())) {

            // 1. Capture the CompletableFuture
            CompletableFuture<ParaphraseService> serviceFuture = ParaphraseService.getInstance();

            // 2. Only show "Loading Check" if the service is genuinely still loading in the background
            if (serviceFuture.isDone()) {
                checkButton.setText("Check");
            } else {
                checkButton.setText("Loading Check");
                checkButton.setDisable(true); // Optional: Prevents users from clicking before it's ready!
            }



            //checkButton.setText("Loading Check");

            serviceFuture.thenAccept(service -> {
                Platform.runLater(() -> {
                    if (isTornDown || checkButton == null) return;
                    checkButton.setDisable(false);
                    checkButton.setText("Check");
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    if (!isTornDown && checkButton != null) checkButton.setText("AI Error!");
                });
                return null;
            });

            checkButtonHandler = e -> {
                if (isTornDown) return;
                if (!checkSetup.isArgument()) {
                    checkSentence();
                }
                else {
                    checkArgument();
                }
            };
            view.getCheckButton().setOnAction(checkButtonHandler);
        }

        if (view.getStaticHelpButton() != null) {
            view.getStaticHelpButton().setDisable(!checkSetup.isStaticHelp());
            staticHelpButtonHandler = e -> {
                if (isTornDown || view == null) return;
                try {
                    Stage helpStage = view.getStaticHelpStage();
                    if (helpStage != null && helpStage.isShowing()) helpStage.close();
                    else if (checkSetup != null) view.showStaticHelp(checkSetup.getStaticHelpDoc());
                } catch (Exception ex) {}
            };
            view.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
        }
    }

    // SCORCHED EARTH RULE 1 & 5: Cleanup cascading and isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // Force-stop ongoing background equivalence check threads
        try {
            if (checkThread != null) {
                if (checkThread.isAlive()) checkThread.interrupt();
                checkThread = null;
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 7: Safe Global State Clearance
        try {
            MatchUtilities.clearMatching();
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try {
            if (view != null && view.getCheckButton() != null) {
                if (checkButtonHandler != null) {
                    view.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                }
                view.getCheckButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (view != null && view.getStaticHelpButton() != null) {
                if (staticHelpButtonHandler != null) {
                    view.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                }
                view.getStaticHelpButton().setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1 & 10: Final Nullification
        try { this.exercise = null; } catch (Exception e) {}
        try { this.view = null; } catch (Exception e) {}
        try { this.checkSetup = null; } catch (Exception e) {}
        try { this.model = null; } catch (Exception e) {}
        try { this.objLang = null; } catch (Exception e) {}
        try { this.objLangName = null; } catch (Exception e) {}
        try { this.mLangName = null; } catch (Exception e) {}
        try { this.mLang = null; } catch (Exception e) {}
        try { this.interpFnRTA = null; } catch (Exception e) {}
        try { this.keyInterpFnDoc = null; } catch (Exception e) {}
        try { this.service = null; } catch (Exception e) {}
        try { this.checkThread = null; } catch (Exception e) {}
        try { this.transDRTA = null; } catch (Exception e) {}
        try { this.checkButtonHandler = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive Data Teardown (only clear pointers)
        // SCORCHED EARTH RULE 10: Non-destructive Data Teardown (only clear pointers)
        try { this.targetFormulas = null; } catch (Exception e) {}
        try { this.targetMatchFormulas = null; } catch (Exception e) {}
        try { this.transExpressions = null; } catch (Exception e) {}
        try { this.transTextLists = null; } catch (Exception e) {}
        try { this.langMap = null; } catch (Exception e) {}


        try {
            if (keyObjects != null) {
                Arrays.fill(keyObjects, null);
                keyObjects = null;
            }
        } catch (Exception e) {}
    }

    public int getCheckTries() { return checkTries; }
    public void setCheckTries(int checkTries) { this.checkTries = checkTries; }

    public boolean isRequireUniv() {
        return requireUniv;
    }
}