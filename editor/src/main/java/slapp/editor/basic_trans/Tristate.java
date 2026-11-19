package slapp.editor.basic_trans;

public enum Tristate {
    T, F, U;

    // Helper for AND logic
    public static Tristate and(Tristate a, Tristate b) {
        if (a == F || b == F) return F;
        if (a == U || b == U) return U;
        return T;
    }

    // Helper for OR logic
    public static Tristate or(Tristate a, Tristate b) {
        if (a == T || b == T) return T;
        if (a == U || b == U) return U;
        return F;
    }
}
