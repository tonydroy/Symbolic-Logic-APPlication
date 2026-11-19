package slapp.editor.basic_trans;

import slapp.editor.parser.symbols.LanguageElement;

public class SymbolInfo {
    // The symbol itself
    final LanguageElement symbol;

    // ADDRESS 1: For the Student's byte[] Model (1 Slot = 1 Byte)
    final int byteOffset;

    // ADDRESS 2: For the Professor's 64-bit ID (1 Slot = 1 or 2 Bits)
    final int bitOffset;

    // METADATA: Dimensions and Types
    final int numSlots;      // Total slots (N^arity)
    final int slotBitWidth;  // Width of ONE slot in the 64-bit ID (1 or 2)
    final boolean isPredicate; // True for Relations/Sentence Letters

    public SymbolInfo(LanguageElement symbol, int byteOffset, int bitOffset,
                      int numSlots, int slotBitWidth, boolean isPredicate) {
        this.symbol = symbol;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
        this.numSlots = numSlots;
        this.slotBitWidth = slotBitWidth;
        this.isPredicate = isPredicate;
    }

    // --- Getters ---
    public int getByteOffset() { return byteOffset; }
    public int getBitOffset() { return bitOffset; }
    public int getNumSlots() { return numSlots; }
    public int getSlotBitWidth() { return slotBitWidth; }
    public boolean isPredicate() { return isPredicate; }
}

