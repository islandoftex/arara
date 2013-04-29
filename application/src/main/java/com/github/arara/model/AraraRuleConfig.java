package com.github.arara.model;

// needed imports
import com.github.arara.utils.AraraUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the model for representing a plain Arara rule configuration based on
 * the YAML files. This class will be used to map YAML rules into Java objects.
 *
 * @since 1.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class AraraRuleConfig {

    // the rule identifier
    private String identifier;
    // the rule name
    private String name;
    // the rule command
    private String command;
    // the rule commands
    private List<String> commands;
    // the arguments list
    private List<AraraRuleArgument> arguments;

    /**
     * <p>Getter for the field <code>commands</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Setter for the rule commands.
     *
     * @param commands A list containing the rule commands.
     */
    public void setCommands(List<String> commands) {
        this.commands = commands;

        // for every entry
        for (int i = 0; i < commands.size(); i++) {

            // remove keyword and reinsert into the list
            String currentCommand = AraraUtils.removeKeyword(commands.get(i));
            commands.set(i, currentCommand);
        }
    }

    /**
     * Getter for the rule identifier.
     *
     * @return The rule identifier.
     */
    public String getIdentifier() {

        // return it
        return identifier;
    }

    /**
     * Setter for the rule identifier.
     *
     * @param identifier The rule identifier.
     */
    public void setIdentifier(String identifier) {

        // set the identifier
        this.identifier = identifier;
    }

    /**
     * Getter for the rule name.
     *
     * @return The rule name.
     */
    public String getName() {

        // return it
        return name;
    }

    /**
     * Setter for the rule name.
     *
     * @param name The rule name.
     */
    public void setName(String name) {

        // set the rule name
        this.name = name;
    }

    /**
     * Getter for the rule command.
     *
     * @return The rule command.
     */
    public String getCommand() {

        // return it
        return command;
    }

    /**
     * Setter for the rule command.
     *
     * @param command The rule command.
     */
    public void setCommand(String command) {

        // set the command
        this.command = AraraUtils.removeKeyword(command);
    }

    /**
     * Getter for the arguments list.
     *
     * @return The arguments list.
     */
    public List<AraraRuleArgument> getArguments() {

        // return the list
        return arguments;
    }

    /**
     * Setter for the arguments list.
     *
     * @param arguments The arguments list.
     */
    public void setArguments(List<AraraRuleArgument> arguments) {

        // set the list
        this.arguments = arguments;
    }

    /**
     * Returns a list containing all argument identifiers.
     *
     * @return A list containing all argument identifiers.
     */
    public List<String> getIdentifiersList() {

        // create a new list
        List<String> result = new ArrayList<String>();

        // for every argument
        for (AraraRuleArgument argument : getArguments()) {

            // add identifier
            result.add(argument.getIdentifier());
        }

        // return list
        return result;
    }
    
    /**
     * Returns the forbidden identifier, if any.
     *
     * @return The forbidden identifier, if any.
     */
    public String checkForForbiddenIdentifiers() {
        
        // for every argument
        for (AraraRuleArgument argument : getArguments()) {
            
            // get the current argument value
            String value = argument.getIdentifier().toLowerCase();
            
            // check if it's a forbidden argument identifier
            // TODO changed here
            if ((value.equals("file")) || (value.equals("files")) ||
                    (value.equals("item")) || (value.equals("items")) ||
                    (value.equals("systemutils"))) {
                
                // return the value
                return value;
            }
        }
        
        // nothing wrong was found
        return null;
    }
    
}
