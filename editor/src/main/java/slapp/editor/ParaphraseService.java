package slapp.editor;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.inference.Predictor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


public class ParaphraseService {

    // ========================================================================
    // APP CONFIGURATION: LOGIC EXERCISE VOCABULARY
    // ========================================================================

    // 1. THE ACTORS (Logical Constants & Substituted Variables)
    // Rule: These MUST match exactly. They cannot be swapped or dropped.
    private static final String[] LOGICAL_CONSTANTS = {
            // Interpretation Function Placeholders (A-Z)
            "alice", "bob", "carl", "dan", "eve", "fay", "guy", "hal", "ivy", "joe",
            "ken", "lou", "max", "nora", "oscar", "paul", "quinn", "roger", "sam",
            "tom", "uma", "vera", "walt", "xavier", "yuri", "zoe",

            // Proper Names & Characters from all exercises
            "bingo", "spot", "plato", "aristotle", "ayn rand", "sue", "jim",
            "charlie", "snoopy", "patty", "lucy", "woodstock", "schroeder",
            "timmy", "lassie", "pa", "barack", "michelle", "mustard", "boddy",
            "ninfa", "harold", "albert", "carol", "daniel", "edward", "fred",
            "god", "hannah", "aaalph", "anselm",

            // Classes, Roles, & Specific Subject Nouns (Singular & Plural)
            "maid", "butler", "person", "people", "man", "men", "woman", "women",
            "alien", "aliens", "animal", "animals", "dog", "dogs", "cat", "cats",
            "rock", "rocks", "number", "numbers", "gun", "guns", "knife", "knives",
            "philosopher", "philosophers", "car", "cars", "ford", "fords",
            "toyota", "toyotas", "barber", "barbers", "pet", "pets",
            "snake", "snakes", "flea", "fleas", "spouse", "spouses", "homework partner", "homework partners",
            "father", "fathers", "californian", "californians", "new yorker", "new yorkers",
            "citizen", "citizens", "successor", "successors", "host", "hosts",

            // Specific Objects, Locations, & Identifiers
            "roswell", "nm", "boston", "boston red sox", "library", "parlor",
            "gasoline", "snow", "weapon", "weapons", "pipe", "pipes", "revolver", "revolvers",
            "junk", "grass", "platonism", "spacetime", "vegetarianism", "fair", "evil",
            "grade", "grades", "eighties", "identity",

            // Specific numeric counts (AI models are notoriously bad at math)
            "one", "two", "three", "four"
    };

    // 2. THE RULES (Mutually Exclusive Vocabulary)
    // Rule: If a student swaps a word for another in the SAME row, fail them.
    // NOTE: Quantifiers (all/some/any) are intentionally omitted to allow for
    // valid Quantifier Negation (e.g. "Not all" == "Some are not").
    private static final String[][] EXCLUSIVE_LOGIC_TERMS = {
            // Theological Attributes
            {"omnipotent", "omniscient", "omnipresent", "omnibenevolent"},

            // Numerical Boundaries
            {"least", "most", "exactly"},

            // Relational Math Properties (Forces exact terminology)
            {"reflexive", "irreflexive"},
            {"symmetric", "asymmetric"},
            {"even", "odd"},

            // Relational Directionality (Prevents AI Antonym Blindness)
            {"taller", "shorter"},
            {"higher", "lower"},
            {"greater", "less"},
            {"better", "worse"},
            {"biggest", "smallest"}
    };

    // ========================================================================
    // AI ENGINE SETUP & VARIABLES
    // ========================================================================

    private static ParaphraseService instance;
    private final ZooModel<String[], Classifications> model;
    private final Predictor<String[], Classifications> predictor; // <-- Variable restored!


    private static final CompletableFuture<ParaphraseService> INSTANCE_FUTURE =
            CompletableFuture.supplyAsync(() -> {
                try {
                    // This is the ONLY time 'new' is called in the entire app
                    return new ParaphraseService();
                } catch (Exception e) {
                    System.err.println("!!! CRITICAL AI STARTUP ERROR !!!");
                    e.printStackTrace();
                    throw new RuntimeException("AI Model failed to initialize", e);
                }
            });



    private ParaphraseService() throws Exception {

        java.nio.file.Path modelDir = findModelsDirectory();
        boolean isExternal = (modelDir != null);

        java.net.URL modelUrl = isExternal ? modelDir.toUri().toURL() : ParaphraseService.class.getResource("/models/");
        if (modelUrl == null) {
            throw new RuntimeException("CRITICAL ERROR: Model folder not found! Make sure the 'models' folder is sitting next to the .jar file.");
        }

        Translator<String[], Classifications> translator = new Translator<>() {
            private HuggingFaceTokenizer tokenizer;

            @Override
            public void prepare(TranslatorContext ctx) {
                try {
                    if (isExternal) {
                        java.nio.file.Path tokenizerPath = modelDir.resolve("tokenizer.json");
                        tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
                    } else {
                        try (java.io.InputStream is = ParaphraseService.class.getResourceAsStream("/models/tokenizer.json")) {
                            if (is == null) throw new RuntimeException("Tokenizer not found in JAR.");
                            java.nio.file.Path tempTokenizer = java.nio.file.Files.createTempFile("tokenizer", ".json");
                            tempTokenizer.toFile().deleteOnExit();
                            java.nio.file.Files.copy(is, tempTokenizer, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            tokenizer = HuggingFaceTokenizer.newInstance(tempTokenizer);
                        }
                    }
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Failed to load tokenizer", e);
                }
            }

            @Override
            public NDList processInput(TranslatorContext ctx, String[] input) {
                Encoding encoding = tokenizer.encode(input[0], input[1]);
                NDManager manager = ctx.getNDManager();
                return new NDList(
                        manager.create(encoding.getIds()),
                        manager.create(encoding.getAttentionMask())
                );
            }

            @Override
            public Classifications processOutput(TranslatorContext ctx, NDList list) {
                float rawScore = list.get(0).toFloatArray()[0];
                double probability = Math.max(0.0, Math.min(1.0, (double) rawScore / 5.0));
                return new Classifications(java.util.Arrays.asList("is_paraphrase"), java.util.Arrays.asList(probability));
            }
        };

        Criteria<String[], Classifications> criteria = Criteria.builder()
                .setTypes(String[].class, Classifications.class)
                .optModelUrls(modelUrl.toString())
                .optModelName("models")
                .optEngine("PyTorch")
                .optTranslator(translator)
                .build();

        this.model = criteria.loadModel();
        this.predictor = model.newPredictor(); // <-- Initialized here!
    }

    public static CompletableFuture<ParaphraseService> getInstance() {
        return INSTANCE_FUTURE;
    }

    // ========================================================================
    // GRADING LOGIC & GUARDRAILS
    // ========================================================================

    public double getSimilarityScore(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;

        String t1 = text1.trim().toLowerCase();
        String t2 = text2.trim().toLowerCase();

        // 1. EXACT MATCH SHORT-CIRCUIT
        if (t1.equals(t2)) return 1.0;

        // 2. LOGICAL GUARDRAILS
        if (hasCriticalLogicMismatch(t1, t2)) return 0.0;
        if (hasConstantMismatch(t1, t2)) return 0.0;
        if (hasRelationalMismatch(t1, t2)) return 0.0;

        // 3. AI FALLBACK
        try {
            Classifications result = predictor.predict(new String[]{text1, text2}); // <-- Used here!
            return result.get("is_paraphrase").getProbability();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    private boolean hasCriticalLogicMismatch(String t1, String t2) {
        for (String[] category : EXCLUSIVE_LOGIC_TERMS) {
            String foundInT1 = null;
            String foundInT2 = null;
            for (String word : category) {
                if (t1.matches(".*\\b" + word + "\\b.*")) foundInT1 = word;
                if (t2.matches(".*\\b" + word + "\\b.*")) foundInT2 = word;
            }
            if (foundInT1 != null && foundInT2 != null && !foundInT1.equals(foundInT2)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasConstantMismatch(String t1, String t2) {
        for (String constant : LOGICAL_CONSTANTS) {
            boolean t1HasIt = t1.matches(".*\\b" + constant + "\\b.*");
            boolean t2HasIt = t2.matches(".*\\b" + constant + "\\b.*");

            if (t1HasIt != t2HasIt) return true;
        }
        return false;
    }

    private boolean hasRelationalMismatch(String t1, String t2) {
        java.util.List<String> constantsInPlay = new java.util.ArrayList<>();

        for (String constant : LOGICAL_CONSTANTS) {
            if (t1.matches(".*\\b" + constant + "\\b.*") && t2.matches(".*\\b" + constant + "\\b.*")) {
                constantsInPlay.add(constant);
            }
        }

        if (constantsInPlay.size() == 2) {
            if (t1.matches(".*\\b(and|or)\\b.*") || t2.matches(".*\\b(and|or)\\b.*")) {
                return false;
            }

            String c1 = constantsInPlay.get(0);
            String c2 = constantsInPlay.get(1);

            boolean t1Order = t1.indexOf(c1) < t1.indexOf(c2);
            boolean t2Order = t2.indexOf(c1) < t2.indexOf(c2);

            boolean t1Passive = isPassiveVoice(t1);
            boolean t2Passive = isPassiveVoice(t2);

            if ((t1Order == t2Order) != (t1Passive == t2Passive)) return true;
        }
        return false;
    }

    private boolean isPassiveVoice(String text) {
        return text.matches(".*\\b(is|are|was|were|am|been|being|gets|got)\\b.{1,25}\\bby\\b.*");
    }

    // ========================================================================
    // FILE UTILITIES
    // ========================================================================

    private java.nio.file.Path findModelsDirectory() {
        String[] possibleRelativePaths = {
                "models",
                "editor/models"
        };

        for (String pathString : possibleRelativePaths) {
            java.nio.file.Path p = java.nio.file.Paths.get(pathString).toAbsolutePath().normalize();
            if (isValidModelFolder(p)) {
                return p;
            }
        }

        try {
            java.net.URL location = ParaphraseService.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                java.nio.file.Path currentPath = java.nio.file.Paths.get(location.toURI());
                java.nio.file.Path parentDir = java.nio.file.Files.isDirectory(currentPath) ? currentPath : currentPath.getParent();

                for (int i = 0; i < 3 && parentDir != null; i++) {
                    java.nio.file.Path checkModelDir = parentDir.resolve("models");
                    if (isValidModelFolder(checkModelDir)) {
                        return checkModelDir.toAbsolutePath().normalize();
                    }
                    parentDir = parentDir.getParent();
                }
            }
        } catch (Exception e) {
            System.err.println("Could not check JAR location: " + e.getMessage());
        }

        return null;
    }

    private boolean isValidModelFolder(java.nio.file.Path dir) {
        return java.nio.file.Files.exists(dir) &&
                java.nio.file.Files.isDirectory(dir) &&
                java.nio.file.Files.exists(dir.resolve("models.pt")) &&
                java.nio.file.Files.exists(dir.resolve("tokenizer.json"));
    }
}