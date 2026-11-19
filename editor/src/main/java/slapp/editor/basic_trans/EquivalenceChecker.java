package slapp.editor.basic_trans;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.*;

import java.util.*;
import java.util.concurrent.*;


public class EquivalenceChecker {

    private static final byte UNKNOWN = 0, TRUE_VALUE = 1, FALSE_VALUE = 2;




    //controller  to get instructor key
    public static KeyObject getKeyObject(Formula keyFormula, int univSize) {

        Map<LanguageElement, Integer> freqMap = getFrequencyMap(keyFormula, null);
        List<LanguageElement> symList = getSortedSymList(freqMap);

        Map<LanguageElement, SymbolInfo> symbolInfoMap = getSymbolInfoMap(symList, univSize, null);

        // 3. Get the total bit size for the BitSet (from the last symbol)
        int totalBitSize = 0;
        for (SymbolInfo info : symbolInfoMap.values()) {
            totalBitSize += (info.getNumSlots() * info.getSlotBitWidth());
        }

     //   System.out.println("univ size: " + univSize + " total bits for key: " + totalBitSize);

        if (totalBitSize > 24) {
            return null;
        }

        // 4. Generate the BitSet
        BitSet bitSet = getKeyModelValues(keyFormula, totalBitSize, symList, symbolInfoMap, univSize);

        return new KeyObject(bitSet, symList, totalBitSize);
    }


    //controller to check equivalence against key
    public static void checkEquivalenceAgainstKey(KeyObject keyObject, Formula studentFormula, int univSize, List<LanguageElement> fullSymList, Map<LanguageElement, SymbolInfo> symbolInfoMap, LanguageElement[] bitToSymbolArray, int fullModelSize, long timeoutMillis) throws ModelContentException {
        BitSet keyModelValues = keyObject.getKeyModelValues();
        int keyModelSize = keyObject.getModelSize();

        if (fullModelSize == 0) {
            byte[] emptyModel = new byte[0];
            Tristate studentVal = evaluate(studentFormula, new HashMap<>(), emptyModel, symbolInfoMap, univSize, 0);
            boolean profVal = keyObject.getKeyModelValues().get(0);

            if ((studentVal == Tristate.T && !profVal) || studentVal == Tristate.F && profVal) {
                List<Text> nullTexts = Collections.singletonList(ParseUtilities.newRegularText("has the standard interpretation"));
                RelationSymbol eqSym = new RelationSymbol("\ue8ac", "", "", 2);
                Pair<LanguageElement, List<Text>> nullPair = new Pair(eqSym, nullTexts);
                List<Pair<LanguageElement, List<Text>>> nullInterp = Collections.singletonList(nullPair);
                throw new ModelContentException(profVal, universeTexts(univSize), nullInterp);
            }
            return;
        }

        SymbolInfo firstInfo = symbolInfoMap.get(bitToSymbolArray[0]);
        int branchLimit = firstInfo.isPredicate() ? 2 : univSize;

        List<Thread> workerThreads = java.util.Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> hikers = new ArrayList<>();

        for (byte val = 0; val < branchLimit; val++) {
     //       if (Thread.currentThread().isInterrupted()) {
      //          System.out.println("Hiker seeing interrupt in loop - check against key");
       //     }

            final byte startingVal = val;
            hikers.add(CompletableFuture.runAsync(() -> {
                Thread me = Thread.currentThread();
                workerThreads.add(me);
                try {
                    byte[] privateModel = new byte[fullModelSize];
                    solveWithKey(0, 0L, studentFormula, privateModel, fullModelSize, fullSymList, symbolInfoMap,
                            bitToSymbolArray, univSize, keyModelSize, keyModelValues, startingVal);
                }
                finally {workerThreads.remove(me);
                }
            }, java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()));
        }

        try {
            CompletableFuture.allOf(hikers.toArray(new CompletableFuture[0]))
                    .get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);

        } catch (java.util.concurrent.TimeoutException | InterruptedException | java.util.concurrent.CancellationException e) {
            synchronized (workerThreads) {
                for (Thread t : workerThreads) {
                    t.interrupt();
                }
            }
            Thread.currentThread().interrupt();
     //       System.out.println("Hikers manually interrupted (key)");

        } catch (java.util.concurrent.ExecutionException e) {
            synchronized (workerThreads) {
                for (Thread t : workerThreads) t.interrupt();
            }
            if (e.getCause() instanceof ModelContentException) throw (ModelContentException) e.getCause();
        }
    }


    public static void checkLargeParallel(Formula keyFormula, Formula studentFormula, int univSize, List<LanguageElement> fullSymList, Map<LanguageElement, SymbolInfo> infoMap, LanguageElement[] bitToSymbolArray, int fullModelSize, long timeoutMillis) throws ModelContentException {

        SymbolInfo firstInfo = infoMap.get(bitToSymbolArray[0]);
        int branchLimit = firstInfo.isPredicate() ? 2 : univSize;

        List<Thread> workerThreads = java.util.Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> hikers = new ArrayList<>();

        for (byte val = 0; val < branchLimit; val++) {
       //     if (Thread.currentThread().isInterrupted()) {
       //         System.out.println("Hiker seeing interrupt - chack large parallel");
       //     }

            final byte startingVal = val;
            hikers.add(CompletableFuture.runAsync(() -> {
                Thread me = Thread.currentThread();
                workerThreads.add(me);
                try {
                    byte[] privateModel = new byte[fullModelSize];
                    solveLargeParallel(0, keyFormula, studentFormula, privateModel,
                            fullModelSize, fullSymList, infoMap,
                            bitToSymbolArray, univSize, startingVal);
                } finally {
                    workerThreads.remove(me);
                }
            }, java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()));


        }

        try {
            CompletableFuture.allOf(hikers.toArray(new CompletableFuture[0]))
                    .get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
        catch (java.util.concurrent.TimeoutException | InterruptedException | java.util.concurrent.CancellationException e) {
            synchronized (workerThreads) {
                for (Thread t : workerThreads) {
                    t.interrupt();
                }
            }
            Thread.currentThread().interrupt();
     //       System.out.println("Hikers manually interrupted");
        }
        catch (java.util.concurrent.ExecutionException e) {
            synchronized (workerThreads) {
                for (Thread t : workerThreads) t.interrupt();
            }
            if (e.getCause() instanceof ModelContentException) throw (ModelContentException) e.getCause();
        }
    }


    public static List<LanguageElement> getCombinedSymbolList(Formula keyFormula, Formula studentFormula) {
        Map<LanguageElement, Integer> keyFreqMap = getFrequencyMap(keyFormula, null);
        List<LanguageElement> keySymList = getSortedSymList(keyFreqMap);

        Map<LanguageElement, Integer> studentFreqMap = getFrequencyMap(studentFormula, null);

        List<LanguageElement> studentSymList = new ArrayList<>(studentFreqMap.keySet());
        List<LanguageElement> fullSymList = new ArrayList<>(keySymList);
        for (LanguageElement element : studentSymList) {
            if (!keySymList.contains(element)) {
                fullSymList.add(element);
            }
        }
        return fullSymList;
    }

    private static boolean solveLargeParallel(int byteIndex, Formula formulaA, Formula formulaB, byte[] model, int fullModelSize, List<LanguageElement> fullSymList, Map<LanguageElement, SymbolInfo> infoMap, LanguageElement[] bitToSymbolArray, int univSize, byte forcedVal) throws ModelContentException {

        if (Thread.currentThread().isInterrupted()) {
     //       System.out.println("Hiker stopping due to interrupt");
            return false;
        }

        // 1. EARLY EVALUATION (Short-circuiting the 32-bit search)
        Tristate resA = evaluate(formulaA, new HashMap<>(), model, infoMap, univSize, byteIndex);
        Tristate resB = evaluate(formulaB, new HashMap<>(), model, infoMap, univSize, byteIndex);

        if (resA != Tristate.U && resB != Tristate.U && resA != resB) {
            throw new ModelContentException(resA == Tristate.T, universeTexts(univSize),
                    assignmentTexts(model, byteIndex, univSize, fullSymList, infoMap));
        }
        if (resA != Tristate.U && resB != Tristate.U) return true;

        // 2. BASE CASE
        if (byteIndex >= fullModelSize) return true;

        // 3. BRANCHING
        SymbolInfo info = infoMap.get(bitToSymbolArray[byteIndex]);
        int branchLimit = info.isPredicate() ? 2 : univSize;

        for (byte val = 0; val < branchLimit; val++) {
            if (Thread.currentThread().isInterrupted()) {
        //        System.out.println("Hiker seeing interrupt in loop at index " + byteIndex);
                return false;
            }

            if (byteIndex == 0 && val != forcedVal) continue;



            model[byteIndex] = info.isPredicate() ? (val == 0 ? TRUE_VALUE : FALSE_VALUE) : val;

            solveLargeParallel(byteIndex + 1, formulaA, formulaB, model,
                    fullModelSize, fullSymList, infoMap,
                    bitToSymbolArray, univSize, (byte)-1);

            if (Thread.currentThread().isInterrupted()) return false;
        }

        model[byteIndex] = UNKNOWN;

     //   if (Thread.currentThread().isInterrupted()) {
     //       System.out.println("Hiker thread " + Thread.currentThread().getName() + " finally exiting.");
    //    }
        return true;
    }



    private static List<Text> universeTexts(int univSize) {
        List<Text> textList = new ArrayList<>();
        textList.add(ParseUtilities.newRegularText("\ud835\uddb4 = {"));  //U = {

        for (int i = 0; i < univSize - 1; i++) {
            textList.add(objectValueText(i));
            textList.add(ParseUtilities.newRegularText(", "));
        }
        textList.add(objectValueText(univSize - 1));
        textList.add(ParseUtilities.newRegularText("}"));

        return textList;
    }

    private static List<Pair<LanguageElement, List<Text>>> assignmentTexts(
            byte[] model,
            int byteLimit, // Renamed from bitIndex to be clear
            int univSize,
            List<LanguageElement> fullSymList,
            Map<LanguageElement, SymbolInfo> symbolInfoMap) {

        List<Pair<LanguageElement, List<Text>>> assignmentList = new ArrayList<>();

        List<LanguageElement> resortedSymList = reStortFullSymList(fullSymList);
        for (int i = 0; i < resortedSymList.size(); i++) {
            LanguageElement sym = resortedSymList.get(i);

            SymbolInfo symInfo = symbolInfoMap.get(sym);

            // 1. Skip if the hiker never reached even the start of this symbol
            if (symInfo.getByteOffset() >= byteLimit) continue;

            List<Text> textList = new ArrayList<>();

            // CASE A: Constants or Sentence Letters (Arity 0)
            if (sym.getPlaces() == 0) {
                int slotStart = symInfo.getByteOffset();
                if (slotStart < byteLimit && slotStart < model.length) {
                    if (!symInfo.isPredicate()) { // Constant
                        int funcVal = model[slotStart] & 0xFF; // Direct read
                        textList.add(objectValueText(funcVal));
                    } else { // Sentence Letter
                        textList.add(truthValueText(model[slotStart]));
                    }
                    assignmentList.add(new Pair<>(sym, textList));
                }
            }
            // CASE B: Relations or Functions (Arity > 0)
            else {
                textList.add(new Text("{"));
                boolean first = true;
                boolean hasMember = false;
                int numSlots = symInfo.getNumSlots();

                for (int l = 0; l < numSlots; l++) {
                    // FIXED: In a byte-model, each slot is exactly +1 index away.
                    int slotStart = symInfo.getByteOffset() + l;

                    if (slotStart < byteLimit && slotStart < model.length) {
                        if (symInfo.isPredicate()) {
                            if (model[slotStart] == TRUE_VALUE) {
                                if (!first) textList.add(ParseUtilities.newRegularText(", "));
                                int[] args = getArgsForSlot(l, sym.getPlaces(), univSize);
                                textList.add(ParseUtilities.newRegularText("\u27e8")); // <
                                for (int k = 0; k < args.length; k++) {
                                    textList.add(objectValueText(args[k]));
                                    if (k < args.length - 1) textList.add(new Text(", "));
                                }
                                textList.add(ParseUtilities.newRegularText("\u27e9")); // >
                                first = false;
                                hasMember = true;
                            }
                        } else { // Function Symbol
                            if (!first) textList.add(ParseUtilities.newRegularText(", "));
                            int[] args = getArgsForSlot(l, sym.getPlaces(), univSize);
                            int funcVal = model[slotStart] & 0xFF;

                            textList.add(ParseUtilities.newRegularText("\u27e8\u27e8")); // <<
                            for (int k = 0; k < args.length; k++) {
                                textList.add(objectValueText(args[k]));
                                if (k < args.length - 1) textList.add(new Text(", "));
                            }
                            textList.add(ParseUtilities.newRegularText("\u27e9, ")); // >
                            textList.add(objectValueText(funcVal));
                            textList.add(ParseUtilities.newRegularText("\u27e9")); // >

                            first = false;
                            hasMember = true;
                        }
                    }
                }
                textList.add(new Text(hasMember ? "}" : " }"));
                assignmentList.add(new Pair<>(sym, textList));
            }
        }
        return assignmentList;
    }


    private static Text truthValueText(int i) {
        switch(i) {
            case 1: return new Text("\ud835\uddb3");  // T
            case 2: return new Text("\ud835\udda5");  // F
            default: return new Text("?");
        }
    }

    private static Text objectValueText(int i) {
        switch(i) {
            case 0: return new Text("\ud835\uddba");   //a
            case 1: return  new Text("\ud835\uddbb");    //b
            case 2: return new Text("\ud835\uddbc");   //c
            case 3: return new Text("\ud835\uddbd");  //d
            default: return new Text("?");
        }
    }





    private static int[] getArgsForSlot(int i, int places, int univSize) {
        int[] args = new int[places];
        int temp = i;

        for (int j = places - 1; j >= 0; j--) {
            args[j] = temp % univSize;
            temp /= univSize;
        }
        return args;
    }

    private static int getFuncValueFromModel(byte[] model, int offset, int univSize) {
        return model[offset] & 0xFF;
    }



    private static boolean solveWithKey(
            int byteIndex, long currentId, Formula studentFormula, byte[] model, int fullModelSize, List<LanguageElement> fullSymList,
            Map<LanguageElement, SymbolInfo> symbolInfoMap, LanguageElement[] bitToSymbolArray, int univSize, int keyModelSize, BitSet keyModelValues, byte forcedVal) throws ModelContentException {

        if (Thread.currentThread().isInterrupted()) return false;

        // 2. BASE CASE: If we've walked past the end, no counter-model found in this leaf.
        if (byteIndex >= fullModelSize) {
            Tristate studVal = evaluate(studentFormula, new HashMap<>(), model, symbolInfoMap, univSize, byteIndex);
            boolean profIsT = keyModelValues.get((int)currentId);

            if (studVal == Tristate.T && !profIsT) {
                List<Text> univTexts = universeTexts(univSize);
                List<Pair<LanguageElement, List<Text>>> assignmentList = assignmentTexts(model, byteIndex + 1,  univSize, fullSymList, symbolInfoMap);
                throw new ModelContentException(false, univTexts, assignmentList);
            }
            if (studVal == Tristate.F && profIsT) {
                List<Text> univTexts = universeTexts(univSize);
                List<Pair<LanguageElement, List<Text>>> assignmentList = assignmentTexts(model, byteIndex + 1, univSize, fullSymList, symbolInfoMap);
                throw new ModelContentException(true, univTexts, assignmentList);
            }
            return false;
        }


        LanguageElement sym = bitToSymbolArray[byteIndex];
        SymbolInfo info = symbolInfoMap.get(sym);

        int branchLimit = info.isPredicate() ? 2 : univSize;

        for (byte val = 0; val < branchLimit; val++) {
            //split for parallel run
            if (byteIndex == 0 && val != forcedVal) continue;

            // We use 0=U, 1=T, 2=F. Map val=0 to T(1), val=1 to F(2) for Relations.
            byte studentVal = info.isPredicate() ? (val == 0 ? TRUE_VALUE : FALSE_VALUE) : val;
            model[byteIndex] = studentVal;

            // Professor uses standard 0/1 bits.
            long profBit = info.isPredicate() ? (val == 0 ? 1L : 0L) : (long)val;

            int currentSlotOffset = byteIndex - info.getByteOffset();
            int bitPos = info.getBitOffset() + (currentSlotOffset * info.getSlotBitWidth());

            long nextId = (bitPos < keyModelSize) ? (currentId | (profBit << bitPos)) : currentId;

            // --- C. THE VERDICT ENGINE ---
            if (isEndOfSymbol(byteIndex, info)) {
                Tristate studVal = evaluate(studentFormula, new HashMap<>(), model, symbolInfoMap, univSize, byteIndex);

                if (studVal != Tristate.U) {
                    // Determine the next Decision Point in the Professor's BitSet
                    int nextDecisionPoint = bitPos + info.getSlotBitWidth();

                    boolean canProfBeTrue  = canProfessorBe(nextId, nextDecisionPoint, true, keyModelSize, keyModelValues);
                    boolean canProfBeFalse = canProfessorBe(nextId, nextDecisionPoint, false, keyModelSize, keyModelValues);

               //     System.out.println("Slot: " + byteIndex + " Stud: " + studVal + " ProfCanT: " + canProfBeTrue + " ProfCanF: " + canProfBeFalse);

                    boolean profLockedT = (canProfBeTrue && !canProfBeFalse);
                    boolean profLockedF = (!canProfBeTrue && canProfBeFalse);

                    if ((studVal == Tristate.T && profLockedT) || (studVal == Tristate.F && profLockedF)) {
                        continue; // Skip deeper recursion; they both agree on this path.
                    }
                }
            }

            // --- D. RECURSION ---
            solveWithKey(byteIndex + 1, nextId, studentFormula, model, fullModelSize, fullSymList, symbolInfoMap, bitToSymbolArray, univSize,
                    keyModelSize, keyModelValues, (byte)-1);
        }

        // --- E. BACKTRACK ---
        model[byteIndex] = UNKNOWN;

     //   if (Thread.currentThread().isInterrupted()) {
     //       System.out.println("Hiker thread " + Thread.currentThread().getName() + " finally exiting.");
    //    }
        return false;
    }



    //get the symbol info map, and along the way the bitToSymbolArray
    public static Map<LanguageElement, SymbolInfo> getSymbolInfoMap(List<LanguageElement> fullSymList, int univSize, LanguageElement[] bitToSymbolArray) {
        Map<LanguageElement, SymbolInfo> infoMap = new HashMap<>();
        int currentByteOffset = 0;
        int currentBitOffset = 0;

        for (LanguageElement sym : fullSymList) {
            int arity = sym.getPlaces();
            int numSlots = (int) Math.pow(univSize, arity);
            boolean isPred = (sym instanceof SentenceLetter || sym instanceof RelationSymbol);

            // Professor Bits: 1 for Predicates, 2 for Functions (if N > 2)
            int slotBitWidth = isPred ? 1 : (univSize > 2 ? 2 : 1);
            int totalBitWidth = numSlots * slotBitWidth;

            SymbolInfo info = new SymbolInfo(sym, currentByteOffset, currentBitOffset, numSlots, slotBitWidth, isPred);
            infoMap.put(sym, info);

            // POPULATE THE ARRAY: Every byte index in this symbol's range
            // must point back to the symbol.
            if (bitToSymbolArray != null) {
                for (int i = 0; i < numSlots; i++) {
                    bitToSymbolArray[currentByteOffset + i] = sym;
                }
            }

            currentByteOffset += numSlots;
            currentBitOffset += totalBitWidth;
        }
        return infoMap;
    }


    //redundant with getSymbolInfoMap -- but at small cost, and we need the size to define the bitToSymbolArray, required before we can call getSymbolInfoMap
    private static int getFullModelSize(List<LanguageElement> symbolList, int univSize) {
        int currentBit = 0;
        for (int i = 0; i < symbolList.size(); i++) {
            LanguageElement e = symbolList.get(i);
            int numSlots = (int) Math.pow(univSize, e.getPlaces());
            boolean isPredicate = (e.getType() == ExpressionType.SENTENCE_LETTER || e.getType() == ExpressionType.RELATION_SYMBOL);
            int slotWidth = (isPredicate || univSize <= 2) ? 1 : 2;
            int totalBitsForSymbol = numSlots * slotWidth;

            currentBit += totalBitsForSymbol;
        }
        return currentBit;
    }

    private static boolean canProfessorBe(long currentId, int bitIndex, boolean target, int totalKeyBits, BitSet profBitSet) {
        if (bitIndex >= totalKeyBits) {
            return profBitSet.get((int) currentId) == target;
        }

        long rangeStart = currentId;
        int remaining = totalKeyBits - bitIndex;
        long rangeEnd = rangeStart + (1L << remaining) - 1;

        if (target) {
            int hit = profBitSet.nextSetBit((int) rangeStart);
            return (hit != -1 && hit <= rangeEnd);
        }
        else {
            int hit = profBitSet.nextClearBit((int) rangeStart);
            return (hit <= rangeEnd);
        }
    }




    private static void resetBits(byte[] fullModel, int bitIndex, int width) {
        fullModel[bitIndex] = 0;
        if (width == 2) {
            fullModel[bitIndex + 1] = 0;
        }
    }

    /**
     * Determines if the current byteIndex is the final slot of the symbol being processed.
     *
     * @param byteIndex The current position of the "hiker" in the model array.
     * @param info      The metadata for the symbol currently being painted.
     * @return True if this is the last byte of the symbol's territory.
     */
    private static boolean isEndOfSymbol(int byteIndex, SymbolInfo info) {
        // The symbol starts at info.getByteOffset() and covers info.getNumSlots() bytes.
        // Therefore, the last valid index for this symbol is (offset + numSlots - 1).
        int lastSlotIndex = info.getByteOffset() + info.getNumSlots() - 1;

        return byteIndex == lastSlotIndex;
    }

    private static BitSet getKeyModelValues(Formula keyFormula, int totalBitSize, List<LanguageElement> symList, Map<LanguageElement, SymbolInfo> infoMap, int univSize) {

        if (totalBitSize == 0) {
            BitSet result = new BitSet();
            byte[] emptyModel = new byte[0];
            if (evaluate(keyFormula, new HashMap<>(), emptyModel, infoMap, univSize, 0) == Tristate.T) {
                result.set(0);
            }
            return result;
        }



        long maxId = 1L << totalBitSize;
        int numTasks = (int) Math.min(8L, maxId); // Optimal sharding for high-end CPUs
        long chunkSize = maxId / numTasks;

        // Calculate byte-size once for all threads
        int fullModelSize = infoMap.values().stream().mapToInt(SymbolInfo::getNumSlots).sum();

        List<CompletableFuture<BitSet>> futures = new ArrayList<>();

        for (int i = 0; i < numTasks; i++) {
            final long start = i * chunkSize;
            final long end = (i == numTasks - 1) ? maxId : (i + 1) * chunkSize;

            futures.add(CompletableFuture.supplyAsync(() -> {
                BitSet localBits = new BitSet();
                for (long id = start; id < end; id++) {
                    // Each thread works on its own model array
                    byte[] model = generateFullModelFromId(id, fullModelSize, symList, infoMap, univSize);

                    if (evaluate(keyFormula, new HashMap<>(), model, infoMap, univSize, fullModelSize) == Tristate.T) {
                        localBits.set((int)id);
                    }
                }
                return localBits;
            }, java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()));
        }

        // Combine results
        BitSet masterBitSet = new BitSet();
        for (CompletableFuture<BitSet> future : futures) {
            // .or() is very efficient for merging bitsets
            masterBitSet.or(future.join());
        }

        return masterBitSet;
    }



    private static Tristate evaluate(Formula formula, Map<LanguageElement, Integer> env, byte[] model, Map<LanguageElement, SymbolInfo> infoMap, int univSize, int hikerLimit) {
        if (formula.isAtomic()) {
            LanguageElement sym = null;
            int places = 0;
            RelationSymbol eqSym = new RelationSymbol("\ue8ac", "", "", 2);

            if (formula instanceof SentenceAtomic) sym = ((SentenceAtomic) formula).getMainLetter();
            else if (formula instanceof PrefixAtomic) sym = ((PrefixAtomic) formula).getMainRelation();
            else if (formula instanceof InfixAtomic) sym = ((InfixAtomic) formula).getMainRelation();

            if (sym != null) places = sym.getPlaces();

            if (!sym.equals(eqSym)) {
                int slotIndex = 0;
                for (int i = 0; i < places; i++) {
                    if (Thread.currentThread().isInterrupted()) return Tristate.U;
                    int val = evalTerm((Term) formula.getChildren().get(i), env, model, infoMap, univSize, hikerLimit);

                    // If any argument is Unknown, the whole atomic is Unknown
                    if (val == -1) return Tristate.U;

                    // Base-N coordinate flattening (e.g., x*N + y)
                    slotIndex = (slotIndex * univSize) + val;
                }

                SymbolInfo info = infoMap.get(sym);
                if (info == null) return Tristate.U;

                // USE BYTE OFFSET: 1 slot = 1 byte
                int physIndex = info.getByteOffset() + slotIndex;

                if (physIndex > hikerLimit) return Tristate.U;
                if (physIndex < 0 || physIndex >= model.length) return Tristate.U;

                byte val = model[physIndex];
                return (val == TRUE_VALUE) ? Tristate.T : (val == FALSE_VALUE) ? Tristate.F : Tristate.U;
            } else {
                // Identity Case: x = y
                int left = evalTerm((Term) formula.getChildren().get(0), env, model, infoMap, univSize, hikerLimit);
                int right = evalTerm((Term) formula.getChildren().get(1), env, model, infoMap, univSize, hikerLimit);
                if (left == -1 || right == -1) return Tristate.U;
                return (left == right) ? Tristate.T : Tristate.F;
            }
        } else {
            // --- LOGICAL OPERATORS (Remain standard 3-valued logic) ---
            ExpressionType type = formula.getMainOperator().getType();

            if (type == ExpressionType.NEG_OP) {
                if (Thread.currentThread().isInterrupted()) return Tristate.U;
                Tristate res = evaluate((Formula) formula.getChildren().get(0), env, model, infoMap, univSize, hikerLimit);
                return (res == Tristate.T) ? Tristate.F : (res == Tristate.F) ? Tristate.T : Tristate.U;
            }

            if (type == ExpressionType.CONJ_OP) {
                Tristate status = Tristate.T;
                for (Expression child : formula.getChildren()) {
                    if (Thread.currentThread().isInterrupted()) return Tristate.U;
                    Tristate res = evaluate((Formula) child, env, model, infoMap, univSize, hikerLimit);
                    if (res == Tristate.F) return Tristate.F;
                    if (res == Tristate.U) status = Tristate.U;
                }
                return status;
            }

            if (type == ExpressionType.DISJ_OP) {
                Tristate status = Tristate.F;
                for (Expression child : formula.getChildren()) {
                    if (Thread.currentThread().isInterrupted()) return Tristate.U;
                    Tristate res = evaluate((Formula) child, env, model, infoMap, univSize, hikerLimit);
                    if (res == Tristate.T) return Tristate.T;
                    if (res == Tristate.U) status = Tristate.U;
                }
                return status;
            }

            if (type == ExpressionType.COND_OP) {
                if (Thread.currentThread().isInterrupted()) return Tristate.U;
                Tristate left = evaluate((Formula) formula.getChildren().get(0), env, model, infoMap, univSize, hikerLimit);
                Tristate right = evaluate((Formula) formula.getChildren().get(1), env, model, infoMap, univSize, hikerLimit);
                if (left == Tristate.F || right == Tristate.T) return Tristate.T;
                if (left == Tristate.T && right == Tristate.F) return Tristate.F;
                return Tristate.U;
            }

            if (type == ExpressionType.BICOND_OP) {
                if (Thread.currentThread().isInterrupted()) return Tristate.U;
                Tristate left = evaluate((Formula) formula.getChildren().get(0), env, model, infoMap, univSize, hikerLimit);
                Tristate right = evaluate((Formula) formula.getChildren().get(1), env, model, infoMap, univSize, hikerLimit);
                if (left == Tristate.U || right == Tristate.U) return Tristate.U;
                return (left == right) ? Tristate.T : Tristate.F;
            }
            // --- QUANTIFIERS ---
            if (type == ExpressionType.UNIVERSAL_OP) {
                UniversalOp op = (UniversalOp) formula.getMainOperator();
                LanguageElement varSym = (LanguageElement) op.getVariableTerm().getChildren().get(0);
                Tristate status = Tristate.T;
                Integer prevVal = env.get(varSym);
                try {
                    for (int i = 0; i < univSize; i++) {
                        if (Thread.currentThread().isInterrupted()) return Tristate.U;
                        env.put(varSym, i);
                        Tristate result = evaluate((Formula) formula.getChildren().get(0), env, model, infoMap, univSize, hikerLimit);
                        if (result == Tristate.F) return Tristate.F;
                        if (result == Tristate.U) status = Tristate.U;
                    }
                } finally {
                    if (prevVal == null) env.remove(varSym);
                    else env.put(varSym, prevVal);
                }
                return status;
            }

            if (type == ExpressionType.EXISTENTIAL_OP) {
                ExistentialOp op = (ExistentialOp) formula.getMainOperator();
                LanguageElement varSym = (LanguageElement) op.getVariableTerm().getChildren().get(0);
                Tristate status = Tristate.F;
                Integer prevVal = env.get(varSym);
                try {
                    for (int i = 0; i < univSize; i++) {
                        if (Thread.currentThread().isInterrupted()) return Tristate.U;
                        env.put(varSym, i);
                        Tristate result = evaluate((Formula) formula.getChildren().get(0), env, model, infoMap, univSize, hikerLimit);
                        if (result == Tristate.T) return Tristate.T;
                        if (result == Tristate.U) status = Tristate.U;
                    }
                } finally {
                    if (prevVal == null) env.remove(varSym);
                    else env.put(varSym, prevVal);
                }
                return status;
            }

        }
        return Tristate.U;
    }

    private static int evalTerm(Term term, Map<LanguageElement, Integer> env, byte[] model, Map<LanguageElement, SymbolInfo> infoMap, int univSize, int hikerLimit) {
        // Variable check (highest priority)
        if (term.getTermType() == TermType.VARIABLE) {
            return env.getOrDefault(term.getChildren().get(0), -1);
        }

        // Determine the symbol and arguments
        LanguageElement sym;
        int places = 0;
        if (term.getTermType() == TermType.COMPLEX) {
            sym = (LanguageElement) term.getMainFnSymbol();
            places = sym.getPlaces();
        } else {
            sym = (LanguageElement) term.getChildren().get(0);
        }

        // Evaluate arguments recursively
        int slotIndex = 0;
        for (int i = 0; i < places; i++) {
            int val = evalTerm((Term) term.getChildren().get(i), env, model, infoMap, univSize, hikerLimit);
            if (val == -1) return -1; // Propagate Unknown
            slotIndex = (slotIndex * univSize) + val;
        }

        SymbolInfo info = infoMap.get(sym);
   //     if (info == null) return -1;


        int physIndex = info.getByteOffset() + slotIndex;
        if (physIndex > hikerLimit) return -1;
        if (physIndex >= model.length) return -1;

        return model[physIndex];
    }

    private static int getByteSymbolIndex(LanguageElement symbol, Map<LanguageElement, Integer> offsets, int univSize, int... args) {
        Integer baseOffset = offsets.get(symbol);
        if (baseOffset == null) return Integer.MAX_VALUE; // Symbol not in model yet

        int slotIndex = 0;
        for (int i = 0; i < args.length; i++) {
            slotIndex += args[i] * Math.pow(univSize, args.length - 1 - i);
        }

        // In the byte[] model, we always jump 1 index per slot.
        return baseOffset + slotIndex;
    }



    private static int getSymbolIndex(LanguageElement symbol, Map<LanguageElement, Integer> offsets, int univSize, int... args) {
        Integer baseOffset = offsets.get(symbol);

        int bitsPerSlot = 1;
        if (!((symbol.getType() == ExpressionType.SENTENCE_LETTER || symbol.getType() == ExpressionType.RELATION_SYMBOL))) {
            if (univSize > 2) bitsPerSlot = 2;
        }

        int slotIndex = 0;
        for (int i = 0; i < args.length; i++) {
            slotIndex += args[i] * Math.pow(univSize, args.length - 1 - i);
    //        System.out.println("baseOffset: " + baseOffset + " slot index: " + slotIndex + " bits per slot: " + bitsPerSlot);
        }
        return baseOffset + (slotIndex * bitsPerSlot);
    }



    private static byte[] generateFullModelFromId(long id, int fullModelSize, List<LanguageElement> fullSymList, Map<LanguageElement, SymbolInfo> infoMap, int univSize) {
        byte[] model = new byte[fullModelSize];
        for (LanguageElement sym : fullSymList) {
            SymbolInfo info = infoMap.get(sym);
            long symBits = (id >> info.getBitOffset());
            int mask = (info.getSlotBitWidth() == 2) ? 3 : 1;

            for (int i = 0; i < info.getNumSlots(); i++) {
                byte val = (byte) (symBits & mask);
                if (info.isPredicate()) {
                    // Map Prof 1/0 to Student 1/2 (T/F)
                    model[info.getByteOffset() + i] = (val == 1) ? TRUE_VALUE : FALSE_VALUE;
                } else {
                    model[info.getByteOffset() + i] = val;
                }
                symBits >>= info.getSlotBitWidth();
            }
        }
        return model;
    }







    //formula2 may be null
    public static Map<LanguageElement, Integer> getFrequencyMap(Formula formula1, Formula formula2) {
        Map<LanguageElement, Integer> freqMap = new HashMap<>();
        symFrequencyHelper(formula1, freqMap);
        if (formula2 != null) symFrequencyHelper(formula2, freqMap);
        return freqMap;
    }

    public static List<LanguageElement> getSortedSymList(Map<LanguageElement, Integer> freqMap) {
        //sort symbols by arity / fn vs predicate / frequency / symbol letter
        Comparator<LanguageElement> comp = new Comparator<>() {
            @Override
            public int compare(LanguageElement s1, LanguageElement s2) {
                if (s1.getPlaces() != s2.getPlaces()) {
                    return Integer.compare(s1.getPlaces(), s2.getPlaces());
                }

                boolean s1IsPredicate = (s1.getType() == ExpressionType.SENTENCE_LETTER || s1.getType() == ExpressionType.RELATION_SYMBOL);
                boolean s2IsPredicate = (s2.getType() == ExpressionType.SENTENCE_LETTER || s2.getType() == ExpressionType.RELATION_SYMBOL);
                if (s1IsPredicate != s2IsPredicate) {
                    return Boolean.compare(s1IsPredicate, s2IsPredicate);
                }

                int s1Freq = freqMap.get(s1);
                int s2Freq = freqMap.get(s2);
                if (s1Freq != s2Freq) {
                    return Integer.compare(s2Freq, s1Freq);
                }

                String s1Name = s1.getBaseStr() + s1.getSubscriptStr() + String.valueOf(s1.getPlaces());
                String s2Name = s2.getBaseStr() + s2.getSubscriptStr() + String.valueOf(s2.getPlaces());
                return s1Name.compareTo(s2Name);
            }
        };

        ArrayList<LanguageElement> symList = new ArrayList<>(freqMap.keySet());
        symList.sort(comp);
        return symList;
    }

    public static List<LanguageElement> reStortFullSymList(List<LanguageElement> fullSymList) {
        Comparator<LanguageElement> comp = new Comparator<>() {
            @Override
            public int compare(LanguageElement s1, LanguageElement s2) {
                if (s1.getPlaces() != s2.getPlaces()) {
                    return Integer.compare(s1.getPlaces(), s2.getPlaces());
                }

                boolean s1IsPredicate = (s1.getType() == ExpressionType.SENTENCE_LETTER || s1.getType() == ExpressionType.RELATION_SYMBOL);
                boolean s2IsPredicate = (s2.getType() == ExpressionType.SENTENCE_LETTER || s2.getType() == ExpressionType.RELATION_SYMBOL);
                if (s1IsPredicate != s2IsPredicate) {
                    return Boolean.compare(s1IsPredicate, s2IsPredicate);
                }

                String s1Name = s1.getBaseStr() + s1.getSubscriptStr() + String.valueOf(s1.getPlaces());
                String s2Name = s2.getBaseStr() + s1.getSubscriptStr() + String.valueOf(s2.getPlaces());
                return s1Name.compareTo(s2Name);
            }
        };
        ArrayList<LanguageElement> symList = new ArrayList<>(fullSymList);
        symList.sort(comp);
        return symList;
    }



    public static void symFrequencyHelper(Expression exp, Map<LanguageElement, Integer> map) {
        if (exp == null) return;

        RelationSymbol eqSym = new RelationSymbol("\ue8ac", "", "", 2);

        if (exp instanceof InfixAtomic) {
            RelationSymbol relSym = ((InfixAtomic) exp).getMainRelation();
            if (!relSym.equals(eqSym)) map.put(relSym, map.getOrDefault(relSym, 0) + 1);
        }
        else if (exp instanceof PrefixAtomic) {
            RelationSymbol relSym = ((PrefixAtomic) exp).getMainRelation();
            if (!relSym.equals(eqSym)) map.put(relSym, map.getOrDefault(relSym, 0) + 1);
        }
        else if (exp instanceof SentenceAtomic) {
            SentenceLetter sentLet = ((SentenceAtomic) exp).getMainLetter();
            map.put(sentLet, map.getOrDefault(sentLet, 0) + 1);
            return;
        }

        else if (exp instanceof Term) {
            Term term = (Term) exp;
            if (term.getTermType() == TermType.COMPLEX) {
                FunctionSymbol funcSym = term.getMainFnSymbol();
                map.put(funcSym, map.getOrDefault(funcSym, 0) + 1);
            }
            else if (term.getTermType() == TermType.CONSTANT) {
                ConstantSym conSym = (ConstantSym) term.getChildren().get(0);
                map.put(conSym, map.getOrDefault(conSym, 0) + 1);
                return;
            }
            else if (term.getTermType() == TermType.VARIABLE) return;
        }

        for (Expression childExp : exp.getChildren()) {
            symFrequencyHelper(childExp, map);
        }
    }

}
