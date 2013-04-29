package com.github.arara.model;

/**
 * Represents the conditional element of an arara directive.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraConditional {

    // the conditional type
    private AraraConditionalType type;
    // the condition itself, to
    // be evaluated later
    private String condition;

    /**
     * Constructor.
     */
    public AraraConditional() {
        
        // set the default values
        condition = "";
        type = AraraConditionalType.NONE;
    }

    /**
     * Getter for the conditional type.
     *
     * @return A conditional type object.
     */
    public AraraConditionalType getType() {
        return type;
    }

    /**
     * Setter for the conditional type.
     *
     * @param type A conditional type object.
     */
    public void setType(AraraConditionalType type) {
        this.type = type;
    }

    /**
     * Checks if the object holds a valid conditional.
     *
     * @return A boolean value indicating if the object holds a valid
     * conditional structure.
     */
    public boolean isEmpty() {
        return condition.trim().equals("");
    }

    /**
     * Getter for the condition.
     *
     * @return A string representing the condition to be evaluated.
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Setter for the condition.
     *
     * @param value A string representing the condition to be evaluated later.
     */
    public void setCondition(String value) {
        
        // set values
        condition = value;
    }
    
}
