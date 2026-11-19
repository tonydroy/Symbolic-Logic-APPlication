package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.model.Document;
import javafx.util.Pair;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.grammatical_parts.Formula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TFOperatorList {

    private static List<Pair<String, List<Formula>>> tfOperatorList = new ArrayList<>();
    private static boolean initialized = false;
    private static List<Pair<String, List<String>>> initialList = new ArrayList<>();

    static {
        //for each tf operator one tf form -- as check is for equivalents

        initialList.add(new Pair("it is a fact that __", Arrays.asList("\ud835\udc9c")));   //A
        initialList.add(new Pair("it is true that __", Arrays.asList("\ud835\udc9c")));  //A
        initialList.add(new Pair("it is not the case that __", Arrays.asList("\u223c \ud835\udc9c"  )));  //tilde A
        initialList.add(new Pair("it is false that __", Arrays.asList("\u223c \ud835\udc9c")));  //tilde A

        initialList.add(new Pair("__ and __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));       //(A caret B)
        initialList.add(new Pair("__ but __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));    //A caret B
        initialList.add(new Pair("although __ __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));   // A caret B
        initialList.add(new Pair("__ although __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));  // A caret B
        initialList.add(new Pair("__ yet __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));  //A caret B
        initialList.add(new Pair("__ however __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));    // A caret B
        initialList.add(new Pair("__ still __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));  // A caret B
        initialList.add(new Pair("__ moreover __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));  // A caret B
        initialList.add(new Pair("both __ and __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));   // A caret B
        initialList.add(new Pair("__ while __", Arrays.asList("(\ud835\udc9c \u2227 \u212c)")));      //A caret B
        initialList.add(new Pair("__ and __ and __", Arrays.asList(
                "((\ud835\udc9c \u2227 \u212c) \u2227 \ud835\udc9e)",//(A caret B) caret C
                "(\ud835\udc9c \u2227 (\u212c \u2227 \ud835\udc9e))" )));  // A caret (B caret C)
        initialList.add(new Pair("__ and __ and __ and __", Arrays.asList(
                "(𝒜 ∧ (ℬ ∧ (𝒞 ∧ 𝒟)))",
                "(𝒜 ∧ ((ℬ ∧ 𝒞) ∧ 𝒟))",
                "((𝒜 ∧ ℬ) ∧ (𝒞 ∧ 𝒟))",
                "(((𝒜 ∧ ℬ) ∧ 𝒞) ∧ 𝒟)",
                "((𝒜 ∧ (ℬ ∧ 𝒞)) ∧ 𝒟)")));

        initialList.add(new Pair("__ or __", Arrays.asList("(\ud835\udc9c \u2228 \u212c)")));    //A wedge B
        initialList.add(new Pair("either __ or __", Arrays.asList("(\ud835\udc9c \u2228 \u212c)")));   //A wedge B
        initialList.add(new Pair("__ unless __", Arrays.asList(
                "(\ud835\udc9c \u2228 \u212c)",  //A wedge B
                "(\u223c\u212c \u2192 \ud835\udc9c)" )));   //tilde B arrow A

        initialList.add(new Pair("unless __ __", Arrays.asList(
                "(\ud835\udc9c \u2228 \u212c)",//A wedge B
                "(\u223c\ud835\udc9c \u2192 \u212c)" )));   //tilde A arrow B

        initialList.add(new Pair("__ or __ or __", Arrays.asList("" +
                "((\ud835\udc9c \u2228 \u212c) \u2228 \ud835\udc9e)", //(A wedge B) wedge C
                "(𝒜 ∨ (ℬ ∨ 𝒞))" )));

        initialList.add(new Pair("either __ or __ or __", Arrays.asList(
                "((\ud835\udc9c \u2228 \u212c) \u2228 \ud835\udc9e)", //(A wedge B) wedge C
                "(𝒜 ∨ (ℬ ∨ 𝒞))" )));

        initialList.add(new Pair("__ or __ or __ or __", Arrays.asList(
                "(𝒜 ∨ (ℬ ∨ (𝒞 ∨ 𝒟)))",
                "(𝒜 ∨ ((ℬ ∨ 𝒞) ∨ 𝒟))",
                "((𝒜 ∨ ℬ) ∨ (𝒞 ∨ 𝒟))",
                "(((𝒜 ∨ ℬ) ∨ 𝒞) ∨ 𝒟)",
                "((𝒜 ∨ (ℬ ∨ 𝒞)) ∨ 𝒟)"  )));
        initialList.add(new Pair("either __ or __ or __ or __", Arrays.asList(
                "(𝒜 ∨ (ℬ ∨ (𝒞 ∨ 𝒟)))",
                "(𝒜 ∨ ((ℬ ∨ 𝒞) ∨ 𝒟))",
                "((𝒜 ∨ ℬ) ∨ (𝒞 ∨ 𝒟))",
                "(((𝒜 ∨ ℬ) ∨ 𝒞) ∨ 𝒟)",
                "((𝒜 ∨ (ℬ ∨ 𝒞)) ∨ 𝒟)" )));

        initialList.add(new Pair("neither __ nor __", Arrays.asList(
                "\u223c(\ud835\udc9c \u2228 \u212c)",//tilde(A wedge B)
                "(\u223c\ud835\udc9c \u2227 \u223c\u212c)"   )));  //tilde A caret tilde B

        initialList.add(new Pair("neither __ nor __ nor __", Arrays.asList(
                "∼((𝒜 ∨ ℬ) ∨ 𝒞))",
                "∼(𝒜 ∨ (ℬ ∨ 𝒞))",

                "((∼𝒜 ∧ ∼ℬ) ∧ ∼𝒞)",
                "(∼𝒜 ∧ (∼ℬ ∧ ∼𝒞))" )));

        initialList.add(new Pair("neither __ nor __ nor __ nor __", Arrays.asList(

                "∼(𝒜 ∨ (ℬ ∨ (𝒞 ∨ 𝒟)))",
                "∼(𝒜 ∨ ((ℬ ∨ 𝒞) ∨ 𝒟))",
                "∼((𝒜 ∨ ℬ) ∨ (𝒞 ∨ 𝒟))",
                "∼(((𝒜 ∨ ℬ) ∨ 𝒞) ∨ 𝒟)",
                "∼((𝒜 ∨ (ℬ ∨ 𝒞)) ∨ 𝒟)",

                "(∼𝒜 ∧ (∼ℬ ∧ (∼𝒞 ∧ ∼𝒟)))",
                "(∼𝒜 ∧ ((∼ℬ ∧ ∼𝒞) ∧ ∼𝒟))",
                "((∼𝒜 ∧ ∼ℬ) ∧ (∼𝒞 ∧ ∼𝒟))",
                "(((∼𝒜 ∧ ∼ℬ) ∧ ∼𝒞) ∧ ∼𝒟)",
                "((∼𝒜 ∧ (∼ℬ ∧ ∼𝒞)) ∧ ∼𝒟)" )));

        initialList.add(new Pair("not both __ and __", Arrays.asList(
                "\u223c(\ud835\udc9c \u2227 \u212c)",//tilde(A caret B)
                "(\u223c\ud835\udc9c \u2228 \u223c\u212c)" )));  //tilde A wedge tilde B

        initialList.add(new Pair("if __ then __", Arrays.asList("(\ud835\udc9c \u2192 \u212c)")));  //A arrow B
        initialList.add(new Pair("if __ __", Arrays.asList("(\ud835\udc9c \u2192 \u212c)")));  //A arrow B
        initialList.add(new Pair("__ if __", Arrays.asList("(\u212c \u2192 \ud835\udc9c)")));  //B arrow A
        initialList.add(new Pair("__ only if __", Arrays.asList("(\ud835\udc9c \u2192 \u212c)")));  //A arrow B
        initialList.add(new Pair("only if __ __", Arrays.asList("(\u212c \u2192 \ud835\udc9c)")));  //B arrow A
        initialList.add(new Pair("provided that __ __", Arrays.asList("(\ud835\udc9c \u2192 \u212c)")));  //A arrow B
        initialList.add(new Pair("__ provided that __", Arrays.asList("(\u212c \u2192 \ud835\udc9c)")));  //B arrow A

        initialList.add(new Pair("__ if and only if __", Arrays.asList("(\ud835\udc9c \u2194 \u212c)")));  //A double arrow B
        initialList.add(new Pair("__ iff __", Arrays.asList("(\ud835\udc9c \u2194 \u212c)")));  //A double arrow B
        initialList.add(new Pair("__ just in case __", Arrays.asList("(\ud835\udc9c \u2194 \u212c)")));  //A double arrow B

        //     initialList.add(new Pair(" ", " "));

    }

    public static List<Pair<String, List<Formula>>> getTfOperatorList() {
        if (!initialized) initializeList();
        initialized = true;
        return tfOperatorList;
    }

    //assumes that each value in the above list is a formula
    private static void initializeList() {
        for (Pair<String, List<String>> pair : initialList) {
            String operator = pair.getKey();

            List<Formula> formulas = new ArrayList<>();
            for (String opString : pair.getValue()) {
                Document opDoc = new Document(opString);

                List<Expression> opExpList = ParseUtilities.parseDoc(opDoc, "LM Meta");
        //        System.out.println(opExpList);

                Formula opFormula = (Formula) opExpList.get(0);
                formulas.add(opFormula);
            }
            tfOperatorList.add(new Pair(operator, formulas));
        }
    }

}
