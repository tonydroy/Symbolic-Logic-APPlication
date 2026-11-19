package slapp.editor;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import slapp.editor.parser.ParseUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SentenceValidator {
    private static SentenceValidator validatorInstance = null;

    private final TokenizerME tokenizer;
    private final POSTaggerME posTagger;

    private SentenceValidator() throws IOException {
        this.tokenizer = new TokenizerME(new TokenizerModel(loadModel("/en-token.bin")));
        this.posTagger = new POSTaggerME(new POSModel(loadModel("/en-pos-maxent.bin")));
    }

    public static SentenceValidator getInstance() {
        if (validatorInstance == null) {
            try {
                validatorInstance = new SentenceValidator();
            }
            catch (Exception e) {
                EditorAlerts.showSimpleTxtListAlert("Sentence Identification", Collections.singletonList(ParseUtilities.newRegularText("Cannot load SentenceValidator.")));

            }
        }
        return validatorInstance;
    }


    // 1. Single-word subordinators that create a fragment if not "resolved"
    private static final Set<String> SUBORDINATORS = new HashSet<>(Arrays.asList(
            "if", "because", "although", "while", "unless", "whereas", "whether",
            "since", "though", "after", "before", "until", "provided", "that", "then"
    ));

    // 2. Truth-functional operators (Multi-word)
    private static final String[] MULTI_WORD_CONNECTIVES = {
            "if and only if", "only if", "even if", "provided that", "so that", "as long as", "just in case"
    };


    private InputStream loadModel(String name) throws IOException {
        InputStream is = getClass().getResourceAsStream(name);
        if (is == null) throw new IOException("Model file not found: " + name);
        return is;
    }

    // Define a set of words that are verbs but often mistagged as nouns
    private static final Set<String> VERB_FALLBACKS = new HashSet<>(Arrays.asList(
            // Original test cases (added base forms)
            "sings", "sing", "laughs", "laugh", "cries", "cry", "walks", "walk",
            "runs", "run", "smiles", "smile", "talks", "talk", "barks", "bark",

            // Logic & Existence (Commonly mistagged as nouns)
            "exists", "exist", "lives", "live", "acts", "act", "suffers", "suffer",
            "feels", "feel", "knows", "know", "believes", "believe", "thinks", "think",
            "fights", "fight", "perceives", "perceive", "observe", "observes", "grasp", "grasps",
            "cause", "causes",

            // Truth-Functional & Relational
            "implies", "imply", "follows", "follow", "means", "mean", "entails", "entail",
            "requires", "require", "negates", "negate", "proves", "prove", "shows", "show",
            "holds", "hold", "equals", "equal", "differs", "differ", "shaves", "shave",

            // --- NEW: Added for your epistemological argument! ---
            "interact", "interacts"
    ));


    // 1. Define the Logic Verbs (The "Lean Dictionary")
    private static final Set<String> LOGIC_VERBS = new HashSet<>(Arrays.asList(
            // Singular forms (Note: removed a duplicate "proves" from your original code)
            "believes", "says", "implies", "proves", "knows", "means", "assumes",
            "asserts", "claims", "concludes", "indicates", "suggests", "supposes", "entails",
            "requires", "shows", "holds",

            // --- NEW: Base/Plural forms (Highly recommended addition!) ---
            // Essential so phrases like "People believe that..." trigger requiredVerbs = 2
            "believe", "say", "imply", "prove", "know", "mean", "assume",
            "assert", "claim", "conclude", "indicate", "suggest", "suppose", "entail",
            "require", "show", "hold"
    ));


    public boolean isCompleteSentence(String input) {
        if (input == null || input.trim().isEmpty()) return false;

        String lower = input.toLowerCase().trim();
        String[] tokens = tokenizer.tokenize(input);
        String[] tags = posTagger.tag(tokens);

        // --- STAGE 1: IDENTIFY STARTING OPERATOR ---
        // These words create a dependency if they start a string (P -> Q, P or Q, etc.)
        boolean startsWithOp = false;
        int starterWordCount = 0;

        // Check Multi-word first (e.g., "provided that", "only if")
        for (String conn : MULTI_WORD_CONNECTIVES) {
            if (lower.startsWith(conn + " ")) {
                startsWithOp = true;
                starterWordCount = conn.split(" ").length;
                break;
            }
        }

        // Check Single-word (if, because, then, and, or, but, neither...)
        if (!startsWithOp) {
            String firstWord = tokens[0].toLowerCase();
            if (SUBORDINATORS.contains(firstWord) ||
                    Arrays.asList("and", "or", "but", "nor", "yet", "either", "neither", "both").contains(firstWord)) {
                startsWithOp = true;
                starterWordCount = 1;
            }
        }

        // --- STAGE 2: UNIFIED COUNTING ---
        int totalVerbs = 0;
        int totalSubjects = 0;
        int verbsAfterStarter = 0;
        int requiredVerbs = 1;

        for (int i = 0; i < tokens.length; i++) {
            String word = tokens[i].toLowerCase();
            String tag = tags[i];

            // Logic Verb Check: "believes that..." requires a 2nd verb
            if (i < tokens.length - 1 && LOGIC_VERBS.contains(word) && tokens[i+1].equalsIgnoreCase("that")) {
                requiredVerbs = 2;
            }

            // Count Subjects (Robust tags)
            boolean isSubject = tag.startsWith("NN") || tag.startsWith("PRP") ||
                    tag.equals("PROPN") || tag.equals("NOUN") || tag.equals("PRON");

            // Count Verbs (Robust tags + Fallbacks + Exclude Subordinators)
            boolean isVerb = (tag.startsWith("VB") || tag.equals("VERB") || tag.equals("AUX") ||
                    VERB_FALLBACKS.contains(word)) && !SUBORDINATORS.contains(word);

            if (isSubject) totalSubjects++;
            if (isVerb) {
                totalVerbs++;
                // We only count "Resolution Verbs" that occur after the starting operator
                if (i >= starterWordCount) verbsAfterStarter++;
            }
        }

        // --- STAGE 3: BALANCE MATH (The "Penalty" Logic) ---
        long subCount = 0;
        for (String t : tokens) {
            if (SUBORDINATORS.contains(t.toLowerCase())) subCount++;
        }

        // Discount logical bridges in the middle so they don't "penalize" the verb count
        if (lower.contains("if and only if")) subCount--;
        if (lower.contains("only if") && !lower.startsWith("only if")) subCount--;
        if (lower.contains("provided that")) subCount--;
        if (lower.contains(" then ") || lower.contains(", then")) subCount--;

        // --- STAGE 4: FINAL EVALUATION ---

        // 1. Basic Subject/Verb Presence
        if (totalSubjects < 1 || totalVerbs < 1) return false;

        // 2. Logic Verb Check (Reporting verbs)
        if (totalVerbs < requiredVerbs) return false;

        // 3. Trailing Connector Safety (Cannot end with an operator)
        String last = tokens[tokens.length - 1].toLowerCase().replaceAll("[^a-z]", "");
        if (SUBORDINATORS.contains(last) || Arrays.asList("and", "or", "but", "nor").contains(last)) {
            return false;
        }

        // 4. The Resolution Rule (Catches "Or bob is happy", "If bob cries", etc.)
        if (startsWithOp) {
            boolean hasComma = Arrays.stream(tokens).anyMatch(t -> t.equals(","));
            // A leading operator needs a 2nd clause (verb) to be a full sentence
            if (verbsAfterStarter < 2 && !hasComma) return false;
        }

        // 5. The Final Balance Rule
        return totalVerbs > subCount;
    }


/*
    public static void main(String[] args) throws IOException {
        SentenceValidator v = new SentenceValidator();

        String[] testCases = {
                "Bob is happy",                             // YES
                "provided that Bob is happy",               // NO
                "Bob is happy provided that",               // NO
                "Bob is happy if and only if Sue is happy", // YES
                "if and only if Bob is happy",              // NO
                "Bob is happy only if Sue is happy",        // YES
                "Only if Bob is happy",
                "Provided that Bob is happy, Sue is happy",
                "Bob is happy iff Sue is happy",
                "It is not the case that Bob is happy",
                "Thanos believes that genocide is good, but it is not the case that genocide is good; and Thanos is an evil being.",
                "Thanos believes that Iron man than Hulk",
                "Iron Man believes that Iron man than Hulk but Iron Man is stronger than Hulk",
                "Bob is happy if",
                "if Bob is happy",
                "then Bob is happy",
                "if Bob is happy then Sue is happy",
                "Bob is happy if Sue is happy",
                "if Bob is happy, Sue is happy",
                "if Bob and Sue are happy then Jim is happy",
                "Bob is happy provided that",
                "Everyone believes that Bob is happy",
                "Bob is happy just in case Sue is happy",
                "just in case Bob is happy",
                "Bob is happy provided that Sue is happy",   // YES
                "Bob"
        };

        for (String test : testCases) {
            System.out.println("[" + test + "] -> " + (v.isCompleteSentence(test) ? "YES" : "NO"));
        }
    }
 */

}
