package com.github.arara.model;

// needed import
import java.util.HashMap;

/**
 * Provides the model for representing the Arara tasks based on the directives
 * and rules.
 *
 * @since 1.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraTask {

    // the task name
    private String name;
    // a map to hold the tasks parameters
    private HashMap parameters;
    // the directive conditional
    private AraraConditional conditional;

    /**
     * Empty constructor.
     */
    public AraraTask() {

        // create a new conditional object
        conditional = new AraraConditional();
    }

    /**
     * Getter for name.
     *
     * @return The task name.
     */
    public String getName() {

        // return it
        return name;
    }

    /**
     * Setter for name.
     *
     * @param name The task name.
     */
    public void setName(String name) {

        // set the name
        this.name = name;
    }

    /**
     * Getter for parameters.
     *
     * @return A map containing all the task parameters.
     */
    public HashMap getParameters() {

        // return it
        return parameters;
    }

    /**
     * Setter for parameters.
     *
     * @param parameters The task parameters as a map.
     */
    public void setParameters(HashMap parameters) {

        // set parameters
        this.parameters = parameters;
    }

    /**
     * Setter for the directive conditional.
     *
     * @param text The condition itself.
     * @param type The conditional type.
     */
    public void setConditional(String text, AraraConditionalType type) {

        // set values
        this.conditional.setCondition(text);
        this.conditional.setType(type);
    }

    /**
     * Getter for the directive conditional.
     *
     * @return A conditional object.
     */
    public AraraConditional getConditional() {
        return conditional;
    }
    
}
