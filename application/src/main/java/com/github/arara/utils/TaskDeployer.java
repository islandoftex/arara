package com.github.arara.utils;

// needed imports
import com.github.arara.exception.AraraException;
import com.github.arara.model.AraraCommand;
import com.github.arara.model.AraraRuleArgument;
import com.github.arara.model.AraraRuleConfig;
import com.github.arara.model.AraraTask;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SystemUtils;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Class responsible for taking the tasks and turning them into arara commands.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 */
public class TaskDeployer {

    // the logger
    /** Constant <code>logger</code> */
    final static Logger logger = LoggerFactory.getLogger(TaskDeployer.class);
    // the localization class
    /** Constant <code>localization</code> */
    final static AraraLocalization localization = AraraLocalization.getInstance();
    // the tasks list
    private List<AraraTask> tasks;
    //the output list with the proper commands
    private List<AraraCommand> commands;
    // the configuration
    private ConfigurationLoader configuration;

    /**
     * Constructor.
     *
     * @param tasks The arara tasks.
     * @param configuration The configuration loader object.
     */
    public TaskDeployer(List<AraraTask> tasks, ConfigurationLoader configuration) {

        // set everything
        this.tasks = tasks;

        // set the configuration
        this.configuration = configuration;

        // create the list
        commands = new ArrayList<AraraCommand>();

    }

    /**
     * Deploys the tasks and returns the commands list.
     *
     * @return The arara commands list.
     * @throws java.lang.Exception Raised if something bad happened.
     */
    public List<AraraCommand> deploy() throws Exception {

        // log message
        logger.info("Deploying tasks into commands.");

        // for each task
        for (AraraTask currentTask : tasks) {

            // get the index where the rule is located
            int pathIndex = findRule(currentTask);

            // if it's not a negative index, that is, the rule was found
            if (pathIndex != -1) {

                // deploy plain rule
                deployRule(currentTask, pathIndex);

            } else {

                // not found, throw exception
                throw new AraraException(localization.getMessage("Error_RuleNotFound", currentTask.getName()));
            }
        }

        // return the commands
        return commands;

    }

    /**
     * Finds the rule in the rules directory.
     *
     * @param task The task to find.
     * @return The rule location.
     */
    private int findRule(AraraTask task) {

        // get the search path
        List<String> paths = configuration.getPaths();

        // for every entry in the search path
        for (int i = 0; i < paths.size(); i++) {

            // if the current task is found in the current path
            if (new File(paths.get(i).concat(File.separator).concat(task.getName()).concat(".yaml")).exists()) {

                // add an entry to the logger
                logger.trace(localization.getMessage("Log_RuleFound", task.getName(), paths.get(i)));

                // return the index of the current path
                return i;
            }
        }

        // nothing was found, return a negative value
        return -1;

    }

    /**
     * Deploys plain rule.
     *
     * @param task The task.
     * @throws java.lang.Exception The rule has a bad definition.
     * @param pathIndex a int.
     */
    private void deployRule(AraraTask task, int pathIndex) throws Exception {

        // create a representer
        Representer representer = new Representer();

        // add a class tag
        representer.addClassTag(AraraRuleConfig.class, new Tag("!config"));
        representer.addClassTag(AraraRuleArgument.class, new Tag("!argument"));

        // create a new YAML parser
        Yaml yaml = new Yaml(new Constructor(AraraRuleConfig.class), representer);

        FileReader fileReader = null;

        // new file reader
        fileReader = new FileReader(configuration.getPaths().get(pathIndex).concat(File.separator).concat(task.getName()).concat(".yaml"));

        // load rule
        AraraRuleConfig plainRule = null;

        try {

            // try to load the rule
            plainRule = (AraraRuleConfig) yaml.load(fileReader);

        } catch (MarkedYAMLException yamlException) {

            // YAML syntax error, throw exception
            throw new AraraException(localization.getMessage("Error_InvalidYAMLRule", task.getName(), configuration.getPaths().get(pathIndex)).concat("\n\n").concat(AraraUtils.extractInformationFromYAMLException(yamlException)));
        }

        try {

            // close reader
            fileReader.close();

        } catch (IOException ioException) {
            // do nothing here
        }

        // check if the rule is invalid
        if (plainRule == null) {

            // raise an error
            throw new AraraException(localization.getMessage("Error_InvalidRule", task.getName(), configuration.getPaths().get(pathIndex)));
        }

        // verify if the identifier is empty
        if (plainRule.getIdentifier() == null) {

            // raise error
            throw new AraraException(localization.getMessage("Error_EmptyIdentifierRule", task.getName(), configuration.getPaths().get(pathIndex)));

        } else {

            // check if the rule has a proper name
            if (plainRule.getName() == null) {

                // raise error
                throw new AraraException(localization.getMessage("Error_EmptyNameRule", task.getName(), configuration.getPaths().get(pathIndex)));

            } else {

                // check if the rule has a proper list of arguments
                if (plainRule.getArguments() == null) {

                    // raise error
                    throw new AraraException(localization.getMessage("Error_EmptyArgumentsListRule", task.getName(), configuration.getPaths().get(pathIndex)));

                }
            }
        }

        // if the rule name doesn't match with the file name
        if (!task.getName().equals(plainRule.getIdentifier())) {

            // throw exception
            throw new AraraException(localization.getMessage("Error_WrongIdentifierRule", task.getName(), configuration.getPaths().get(pathIndex), plainRule.getIdentifier()));
        }
        
        // check if the rule has any forbidden identifier
        if (plainRule.checkForForbiddenIdentifiers() != null) {
            
            // raise error
            throw new AraraException(localization.getMessage("Error_ForbiddenIdentifierRule", task.getName(), configuration.getPaths().get(pathIndex), plainRule.checkForForbiddenIdentifiers()));
        }

        // get the list of all arguments available in the rule
        List<String> availableArgumentsInRule = plainRule.getIdentifiersList();

        // add the two reserved keywords
        availableArgumentsInRule.add("file");
        availableArgumentsInRule.add("item");

        // get the parameters for the task
        HashMap directiveMap = task.getParameters();

        // for every entry in the parameters map of the task, add it to the
        // list of available parameters in the directive
        List<String> availableArgumentsInDirective = new ArrayList<String>();
        for (Object theKey : directiveMap.keySet()) {
            availableArgumentsInDirective.add((String) theKey);
        }

        // find out which arguments are not defined in the directive and in
        // the rule
        List<String> argumentsNotDefinedInDirective = (List<String>) CollectionUtils.subtract(availableArgumentsInRule, availableArgumentsInDirective);
        List<String> argumentsNotDefinedInRule = (List<String>) CollectionUtils.subtract(availableArgumentsInDirective, availableArgumentsInRule);

        // if there are invalid arguments in the directive
        if (!argumentsNotDefinedInRule.isEmpty()) {

            // raise error
            throw new AraraException(localization.getMessage("Error_ArgumentsNotDefinedInRule", task.getName(), configuration.getPaths().get(pathIndex), argumentsNotDefinedInRule.toString()));
        }

        // for all arguments in the rule not mapped in the directive, set
        // their default value to an empty string
        for (String currentKey : argumentsNotDefinedInDirective) {
            directiveMap.put(currentKey, "");
        }

        // add entries in the arguments map
        HashMap argumentMap = new HashMap();
        argumentMap.put("file", directiveMap.get("file"));
        argumentMap.put("item", directiveMap.get("item"));
        directiveMap.remove("file");
        directiveMap.remove("item");
        argumentMap.put("parameters", directiveMap);

        // helper functions added
        argumentMap.put("SystemUtils", SystemUtils.class);
        argumentMap.put("isEmpty", AraraMethods.class.getMethod("isEmpty", String.class));
        argumentMap.put("isNotEmpty", AraraMethods.class.getMethod("isNotEmpty", String.class));
        argumentMap.put("isEmpty", AraraMethods.class.getMethod("isEmpty", String.class, String.class));
        argumentMap.put("isNotEmpty", AraraMethods.class.getMethod("isNotEmpty", String.class, String.class));
        argumentMap.put("isEmpty", AraraMethods.class.getMethod("isEmpty", String.class, String.class, String.class));
        argumentMap.put("isNotEmpty", AraraMethods.class.getMethod("isNotEmpty", String.class, String.class, String.class));
        argumentMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class));
        argumentMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class));
        argumentMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class, String.class));
        argumentMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class, String.class));
        argumentMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class, String.class, String.class));
        argumentMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class, String.class, String.class));
        argumentMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class, String.class, String.class, String.class));
        argumentMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class, String.class, String.class, String.class));
        argumentMap.put("trimSpaces", AraraMethods.class.getMethod("trimSpaces", String.class));
        argumentMap.put("getFilename", AraraMethods.class.getMethod("getFilename", String.class));
        argumentMap.put("getBasename", AraraMethods.class.getMethod("getBasename", String.class));
        argumentMap.put("getFiletype", AraraMethods.class.getMethod("getFiletype", String.class));
        argumentMap.put("getDirname", AraraMethods.class.getMethod("getDirname", String.class));
        argumentMap.put("isFile", AraraMethods.class.getMethod("isFile", String.class));
        argumentMap.put("isDir", AraraMethods.class.getMethod("isDir", String.class));
        argumentMap.put("isWindows", AraraMethods.class.getMethod("isWindows", String.class, String.class));
        argumentMap.put("isLinux", AraraMethods.class.getMethod("isLinux", String.class, String.class));
        argumentMap.put("isUnix", AraraMethods.class.getMethod("isUnix", String.class, String.class));
        argumentMap.put("isMac", AraraMethods.class.getMethod("isMac", String.class, String.class));
        
        argumentMap.put("isTrue", AraraMethods.class.getMethod("isTrue", boolean.class, String.class));
        argumentMap.put("isTrue", AraraMethods.class.getMethod("isTrue", boolean.class, String.class, String.class));
        argumentMap.put("isFalse", AraraMethods.class.getMethod("isFalse", boolean.class, String.class));
        argumentMap.put("isFalse", AraraMethods.class.getMethod("isFalse", boolean.class, String.class, String.class));
        
        argumentMap.put("getOriginalFile", AraraMethods.class.getMethod("getOriginalFile"));
        
        // create a rule map
        HashMap ruleMap = new HashMap();

        // for every argument in the rule
        for (AraraRuleArgument currentArgument : plainRule.getArguments()) {

            // if there's not a default value
            if (currentArgument.getDefault() == null) {

                // set the default to an empty string
                ruleMap.put(currentArgument.getIdentifier(), "");

            } else {

                // there's actually a default value
                try {

                    // get the default value and apply the template
                    String defaultValue = (String) TemplateRuntime.eval(currentArgument.getDefault(), argumentMap);

                    // put the new default value into the rule map
                    ruleMap.put(currentArgument.getIdentifier(), defaultValue);

                } catch (RuntimeException runtimeException) {

                    // an error occurred, throw exception
                    throw new AraraException(localization.getMessage("Error_DefaultValueRuntimeErrorRule", currentArgument.getIdentifier(), task.getName(), configuration.getPaths().get(pathIndex), AraraUtils.getVariableFromException(runtimeException)));

                }
            }
        }

        // after setting the default values into the rule map, let's add
        // the directive parameters and both file and item references
        ruleMap.put("parameters", directiveMap);
        ruleMap.put("file", argumentMap.get("file"));
        ruleMap.put("item", argumentMap.get("item"));

        // helper classes
        ruleMap.put("SystemUtils", SystemUtils.class);
        ruleMap.put("isEmpty", AraraMethods.class.getMethod("isEmpty", String.class));
        ruleMap.put("isNotEmpty", AraraMethods.class.getMethod("isNotEmpty", String.class));
        ruleMap.put("isEmpty", AraraMethods.class.getMethod("isEmpty", String.class, String.class));
        ruleMap.put("isNotEmpty", AraraMethods.class.getMethod("isNotEmpty", String.class, String.class));
        ruleMap.put("isEmpty", AraraMethods.class.getMethod("isEmpty", String.class, String.class, String.class));
        ruleMap.put("isNotEmpty", AraraMethods.class.getMethod("isNotEmpty", String.class, String.class, String.class));
        ruleMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class));
        ruleMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class));
        ruleMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class, String.class));
        ruleMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class, String.class));
        ruleMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class, String.class, String.class));
        ruleMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class, String.class, String.class));
        ruleMap.put("isTrue", AraraMethods.class.getMethod("isTrue", String.class, String.class, String.class, String.class));
        ruleMap.put("isFalse", AraraMethods.class.getMethod("isFalse", String.class, String.class, String.class, String.class));
        ruleMap.put("trimSpaces", AraraMethods.class.getMethod("trimSpaces", String.class));
        ruleMap.put("getFilename", AraraMethods.class.getMethod("getFilename", String.class));
        ruleMap.put("getBasename", AraraMethods.class.getMethod("getBasename", String.class));
        ruleMap.put("getFiletype", AraraMethods.class.getMethod("getFiletype", String.class));
        ruleMap.put("getDirname", AraraMethods.class.getMethod("getDirname", String.class));
        ruleMap.put("isFile", AraraMethods.class.getMethod("isFile", String.class));
        ruleMap.put("isDir", AraraMethods.class.getMethod("isDir", String.class));
        ruleMap.put("isWindows", AraraMethods.class.getMethod("isWindows", String.class, String.class));
        ruleMap.put("isLinux", AraraMethods.class.getMethod("isLinux", String.class, String.class));
        ruleMap.put("isUnix", AraraMethods.class.getMethod("isUnix", String.class, String.class));
        ruleMap.put("isMac", AraraMethods.class.getMethod("isMac", String.class, String.class));
        
        ruleMap.put("isTrue", AraraMethods.class.getMethod("isTrue", boolean.class, String.class));
        ruleMap.put("isTrue", AraraMethods.class.getMethod("isTrue", boolean.class, String.class, String.class));
        ruleMap.put("isFalse", AraraMethods.class.getMethod("isFalse", boolean.class, String.class));
        ruleMap.put("isFalse", AraraMethods.class.getMethod("isFalse", boolean.class, String.class, String.class));

        ruleMap.put("getOriginalFile", AraraMethods.class.getMethod("getOriginalFile"));
        
        // remove references to file and item
        availableArgumentsInDirective.remove("file");
        availableArgumentsInDirective.remove("item");

        // for all arguments in the rule
        for (AraraRuleArgument currentArgument : plainRule.getArguments()) {

            // if the current argument is defined in the directive
            if (isArgumentDefinedInDirective(availableArgumentsInDirective, currentArgument)) {

                // if there is a valid flag
                if (currentArgument.getFlag() != null) {

                    try {

                        // get the flag and apply template
                        String defaultFlag = (String) TemplateRuntime.eval(currentArgument.getFlag(), ruleMap);

                        // put the new flag reference into the rule map
                        ruleMap.put(currentArgument.getIdentifier(), defaultFlag);
                    } catch (RuntimeException runtimeException) {

                        // an error occurred, throw exception
                        throw new AraraException(localization.getMessage("Error_FlagRuntimeErrorRule", currentArgument.getIdentifier(), task.getName(), configuration.getPaths().get(pathIndex), AraraUtils.getVariableFromException(runtimeException)));

                    }
                }
            }
        }

        // create a new list of commands to be executed by the current task
        List<String> commandsList = new ArrayList<String>();

        // if there is a command element in the rule
        if (plainRule.getCommand() != null) {

            // and if there isn't a list of commands
            if (plainRule.getCommands() == null) {

                // add the current command to the list of commands to
                // be executed
                commandsList.add(plainRule.getCommand());

            } else {

                // both are defined, throw an error
                throw new AraraException(localization.getMessage("Error_DuplicatedCommandElementsRule", task.getName(), configuration.getPaths().get(pathIndex)));

            }
        } else {

            // none of the fields is defined
            if (plainRule.getCommands() == null) {

                // neither of the elements is defined, throw an error
                throw new AraraException(localization.getMessage("Error_MissingCommandElementsRule", task.getName(), configuration.getPaths().get(pathIndex)));

            } else {

                // there's a list of commands, add them to the list
                commandsList.addAll(plainRule.getCommands());
            }
        }

        // for every command to be processed
        for (String commandTemplate : commandsList) {

            try {
                // get the current command and apply template
                commandTemplate = (String) TemplateRuntime.eval(commandTemplate, ruleMap);

            } catch (RuntimeException runtimeException) {

                // an error occurred, throw exception
                throw new AraraException(localization.getMessage("Error_CommandRuntimeErrorRule", task.getName(), configuration.getPaths().get(pathIndex), AraraUtils.getVariableFromException(runtimeException)));

            }
            
            // check if it's a valid command
            if (!"".equals(commandTemplate.trim())) {

                // create a new command
                AraraCommand araraCommand = new AraraCommand();

                // add the command
                araraCommand.setCommand(commandTemplate);

                // add the name
                araraCommand.setName(plainRule.getName());
                
                // TODO add filename?
                araraCommand.setFilename((String) argumentMap.get("file"));

                // get conditionals from task and set the command
                araraCommand.setConditional(task.getConditional());
                
                // add to the list
                commands.add(araraCommand);
                
            }
        }
    }

    /**
     * Checks if the current argument is included in the list of arguments from
     * the directive.
     *
     * @param arguments List of arguments from the directive.
     * @param arguments List of arguments from the directive.
     * @param argument The current argument to be analyzed.
     * @return A logic value indicating if the current argument is included in
     * the list of arguments from the directive.
     */
    private boolean isArgumentDefinedInDirective(List<String> arguments, AraraRuleArgument argument) {

        // for every argument in the list
        for (String current : arguments) {

            // if the identifiers match
            if (current.equalsIgnoreCase(argument.getIdentifier())) {

                // found it
                return true;
            }
        }

        // not found
        return false;
    }
    
}
