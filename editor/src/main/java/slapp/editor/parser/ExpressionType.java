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
    COLON_DIVIDER_SYM("Colon Divider Sym"),

    MTERM_SYM("Mterm Sym"),
    MFORMULA_SYM("Mformula Sym"),
    MEXPRESSION_SYM("Mexpression Sym"),
    PMTERM_SYM("Pseudo Mterm Sym"),
    PMFORMULA_SYM("Pseudo Mformula Sym"),

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

    ALL_TERM_SUB("Term Sub"),
    SOME_TERM_SUB("Term Sub"),
    ONE_TERM_SUB("Term Sub"),
    ONE_FORMULA_SUB("Formula Sub"),

    VARIABLE_SYM("Var"),
    CONSTANT_SYM("Const"),
    SENTENCE_LETTER("Sent Let"),
    RELATION_SYMBOL("Rel Sym"),
    COMPLEMENT_REL_SYM("Neg Rel Sym"),
    FUNCTION_SYMBOL("Fn Sym"),
    CONTRADICTION_SYM("Cond Sym"),


    ANY_EXPRESSION("Any Expression"),
    ANGLE_OPEN_BRACKET("Angle Op Open Bracket"),
    ANGLE_CLOSE_BRACKET("Angle Close Bracket"),
    COMMA_DIVIDER("Comma Divider"),
    SLASH_DIVIDER("Slash Divider"),
    DOUBLE_SLASH_DIVIDER("Double Slash Divider"),


    TERM("Term"),
    FORMULA("Formula"),

   // -----------
    M_PROPER_NAME("M Proper Name"),
    M_BASIC_OBJ_VAR("M Object Variable"),
    M_VBAR_DIVIDER_SYM("M VBar Divider"),
    M_ASSIGNMENT_VARIABLE("M Assignment Variable"),
    M_INTERPRETATION_VARIABLE("M Interpretation Variable"),
    M_OPEN_SQUARE_BRACKET("M Open Square Bracket"),
    M_CLOSE_SQUARE_BRACKET("M Close Square Bracket"),
    M_OPEN_PARENTHESIS("M Open Parenthesis"),
    M_CLOSE_PARENTHESIS("M Close Parenthesis"),
    M_OPEN_BRACE("M Open Brace"),
    M_CLOSE_BRACE("M Close Brace"),
    M_OPEN_ANGLE_BRACKET("M Open Angle Bracket"),
    M_CLOSE_ANGLE_BRACKET("M Close Angle Bracket"),
    M_UNIVERSE_SYM("M Universe Symbol"),
    M_EMPTY_SET("M Empty Set"),
    M_ASSIGNMENT_MOD("M Assignment Mod"),
    M_ASSIGNMENT("M Assignment"),
    M_INTERP_ASSIGNMENT("M Interpretation with Assignment"),
    M_INTERP_FORMULA("M Interpretation of Formula"),
    M_INTERP_RELATION("M Interpretation of Relation"),
    M_INTERP_FUNCTION("M Interpretation of Function"),
    M_INTERP_CONSTANT("M Interpretation of Constant"),
    M_ASSIGN_VARIABLE("M Assignment of Variable"),
    M_INTERP_ASSIGN_TERM("M Interp/Assign of Term"),
    M_INTERP_ASSIGN_FORMULA("M Interp/Assign of Formula"),
    M_UNIVERSE("M Universe"),
    M_RELATION_TUPLE("M Relation Tuple"),
    M_FUNCTION_PAIR("M Function Pair"),
    M_RELATION("M Relation"),
    M_FUNCTION_CANDIDATE("M Function Candidate"),
    M_INTERP_UNIVERSE("M Unverse for Interp"),
    M_THE_TRUE("M The True"),
    M_THE_FALSE("M The False"),
    M_THE_SAT("M The Sat"),
    M_THE_NSAT("M The Nsat"),
    M_EQUALS("M Equals"),

    M_UNIVERSE_ASSIGN("M Unviverse Assign"),
    M_INTERP_SENTENCE_ASSIGN("M Interp Sentence Assign"),
    M_INTERP_CONSTANT_ASSIGN("M Interp Constant Assign"),
    M_INTERP_RELATION_ASSIGN("M Interp Relation Assign"),
    M_INTERP_FUNCTION_ASSIGN("M Interp Function Assign"),
    M_VASSIGN_VARIABLE_ASSIGN("M VAssign Variable Assign");



    public final String label;

    private ExpressionType(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }

}
