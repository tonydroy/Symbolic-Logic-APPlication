package slapp.editor.parser;

public enum ExpressionType {

    ORIGINAL_ELEMENT("Not in vocab"),
    OPEN_BRACKET1("Bracket"),
    CLOSE_BRACKET1("Bracket"),
    OPEN_BRACKET2("Bracket"),
    CLOSE_BRACKET2("Bracket"),
    OPEN_BRACKET3("Bracket"),
    CLOSE_BRACKET3("Bracket"),

    NEG_SYM("Neg Sym"),
    COND_SYM("Cond Sym"),
    BICOND_SYM("Bicond Sym"),
    CONJ_SYM("Conj Sym"),
    DISJ_SYM("Disj Sym"),
    NAND_SYM("Nand Sym"),
    NOR_SYM("Nor Sym"),
    UNIVERSAL_SYM("Univ Sym"),
    EXISTENTIAL_SYM("Exis Sym"),
    DIVIDER_SYM("Divider Sym"),

    MTERM_SYM("Mterm Sym"),
    MFORMULA_SYM("Mformula Sym"),
    MEXPRESSION_SYM("Mexpression Sym"),

    OPERATOR(""),
    NEG_OP("Neg Op"),
    COND_OP("Cond Op"),
    BICOND_OP("Bicond Op"),
    CONJ_OP("Conj Op"),
    DISJ_OP("Disj Op"),
    NAND_OP("Nand Op"),
    NOR_OP("Nor Op"),
    UNIVERSAL_OP("Univ Op"),
    EXISTENTIAL_OP("Exis Op"),
    UNIV_RESTRICTED_OP("Univ Restricted Op"),
    EXIS_RESTRICTED_OP("Exis Restricted Op"),
    UNIV_BOUNDED_OP("Univ Bounded Op"),
    EXIS_BOUNDED_OP("Exis Bounded Op"),

    VARIABLE_SYM("Var"),
    CONSTANT_SYM("Const"),
    SENTENCE_LETTER("Sent Let"),
    RELATION_SYMBOL("Rel Sym"),
    COMPLEMENT_REL_SYM("Neg Rel Sym"),
    FUNCTION_SYMBOL("Fn Sym"),


    ANY_EXPRESSION("Any Expression"),


    TERM("Term"),
    FORMULA("Formula");

    public final String label;

    private ExpressionType(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }

}
