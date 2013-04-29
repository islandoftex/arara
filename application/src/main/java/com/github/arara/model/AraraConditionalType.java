package com.github.arara.model;

/**
 * Enumeration that represents each type of arara conditional.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public enum AraraConditionalType {

    /**
     * Conditional to be evaluated before the command. Command will be executed
     * if and only if this conditional resolves to true.
     */
    IF,
    /**
     * Conditional to be evaluated before the command. Command will be executed
     * while this conditional resolves to true.
     */
    WHILE,
    /**
     * Conditional to be evaluated after the command. Command will be executed
     * until the conditional resolves to true.
     */
    UNTIL,
    /**
     * Well, this value does nothing. Ze googles, zhey do nothing!
     */
    NONE
}
