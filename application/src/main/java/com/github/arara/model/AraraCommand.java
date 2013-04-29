package com.github.arara.model;

/**
 * Provides the model for commands to be executed by the runtime.
 *
 * @since 1.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraCommand {

    // the name to be displayed in the command line
    private String name;
    // the command itself
    private String command;
    // the command conditional
    private AraraConditional conditional;
    // reference to the current filename
    private String filename;

    /**
     * Getter for command.
     *
     * @return The command to be executed by the runtime.
     */
    public String getCommand() {

        // return it
        return command;
    }

    /**
     * Setter for command.
     *
     * @param command The command, already expanded.
     */
    public void setCommand(String command) {

        // set the command
        this.command = command;
    }

    /**
     * Getter for the command name.
     *
     * @return The command name, provided by the rule.
     */
    public String getName() {

        // return it
        return name;
    }

    /**
     * Setter for the command name.
     *
     * @param name The command name.
     */
    public void setName(String name) {

        // set the name
        this.name = name;
    }

    /**
     * Constructor.
     */
    public AraraCommand() {
        
        // create a new conditional object
        conditional = new AraraConditional();
    }

    /**
     * Setter for the command conditional.
     *
     * @param text The condition itself.
     * @param type The type of the current condition.
     */
    public void setConditional(String text, AraraConditionalType type) {
        
        // set values
        conditional.setCondition(text);
        conditional.setType(type);
    }

    /**
     * Getter for the command conditional.
     *
     * @return A conditional object.
     */
    public AraraConditional getConditional() {
        
        // return the object
        return conditional;
    }

    /**
     * Getter for the filename reference.
     *
     * @return A string containing the reference to the filename.
     */
    public String getFilename() {
        
        // return the reference
        return filename;
    }

    /**
     * Setter for the filename reference.
     *
     * @param filename A string containing the reference to the filename.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Setter for the command conditional.
     *
     * @param conditional A conditional object.
     */
    public void setConditional(AraraConditional conditional) {
        this.conditional = conditional;
    }
    
}
