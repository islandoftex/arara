/**
 * \cond LICENSE
 * ********************************************************************
 * This is a conditional block for preventing the DoxyGen documentation
 * tool to include this license header within the description of each
 * source code file. If you want to include this block, please define
 * the LICENSE parameter into the provided DoxyFile.
 * ********************************************************************
 *
 * Arara -- the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the project's author nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * ********************************************************************
 * End of the LICENSE conditional block
 * ********************************************************************
 * \endcond
 *
 * TaskDeployer.java: This class is responsible for taking the tasks and
 * turning them into Arara commands.
 */

// package definition
package com.github.arara.utils;

// needed imports
import com.github.arara.Arara;
import com.github.arara.api.AraraRule;
import com.github.arara.exceptions.AraraBadRuleParsing;
import com.github.arara.exceptions.AraraRuleNotFound;
import com.github.arara.model.AraraCommand;
import com.github.arara.model.AraraTask;
import com.github.arara.model.PlainAraraRuleArgument;
import com.github.arara.model.PlainAraraRuleConfig;
import java.io.File;
import java.io.FileReader;
import java.net.URLDecoder;
import java.util.*;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Class responsible for taking the tasks and turning them into Arara commands.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class TaskDeployer {

    // the logger
    final static Logger logger = LoggerFactory.getLogger(TaskDeployer.class);
    
    // enum to keep track of the rule location
    private enum RuleLocation {
        
        // Good old YAML file
        PLAIN,
        
        // Java file
        COMPILED,
        
        // None, rule not found
        NONE
    }
    
    // the tasks list
    private List<AraraTask> tasks;
    
    //the output list with the proper commands
    private List<AraraCommand> commands;
    
    // the Jar Class Loader for compiled rules
    private JarClassLoader jcl;

    // the application paths
    private String applicationPath;
    private String plainRulesPath;
    private String compiledRulesPath;
    
    /**
     * Constructor.
     * @param tasks The Arara tasks. 
     */
    public TaskDeployer(List<AraraTask> tasks) {
        
        // lets try
        try {
            
            // get the main class path
            applicationPath = Arara.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            
            // decode it
            applicationPath = URLDecoder.decode(applicationPath, "UTF-8");
            
            // create a new file from it to get path
            applicationPath = new File(applicationPath).getParentFile().getPath();
            
            // the rules directories are expected to be
            // in the root of the application path
            plainRulesPath = applicationPath + File.separator + "rules" + File.separator + "plain";
            compiledRulesPath = applicationPath + File.separator + "rules" + File.separator + "compiled";
        }
        catch (Exception ex) {
            // do nothing
        }
        
        // set everything
        this.tasks = tasks;
        
        // create the list
        commands = new ArrayList<AraraCommand>();

        // new loader
        jcl = new JarClassLoader();
        
        // add the rules directory
        jcl.add(compiledRulesPath);
    }

    /**
     * Deploys the tasks and returns the commands list.
     * @return The Arara commands list.
     * @throws AraraRuleNotFound Returned when the rule is not found.
     * @throws AraraBadRuleParsing The rule is found, but there's a bad parsing.
     */
    public List<AraraCommand> deploy() throws AraraRuleNotFound, AraraBadRuleParsing {

        // log message
        logger.info("Deploying tasks into commands.");
        
        // for each task
        for (AraraTask currentTask : tasks) {

            // check for their existence
            switch (findRule(currentTask)) {
                
                // plain rules have higher priority
                case PLAIN:
                    
                    // deploy plain rule
                    deployPlainRule(currentTask);
                    
                    break;
                    
                // compiled rules
                case COMPILED:
                    
                    // deploy compiled rule
                    deployCompiledRule(currentTask);
                    
                    break;
                    
                default:
                    
                    // not found, throw exception
                    throw new AraraRuleNotFound("ERROR: Rule '" + currentTask.getName() + "' was not found.\nPlease check the 'rules' directory.");
            }

        }

        // return the commands
        return commands;

    }

    /**
     * Finds the rule in the rules directory.
     * @param task The task to find.
     * @return The rule location.
     */
    private RuleLocation findRule(AraraTask task) {

        // look for YAML files
        if ((new File(plainRulesPath + File.separator + task.getName() + ".yaml")).exists()) {
            
            // found it
            return RuleLocation.PLAIN;
            
        } else {
            
            // look for compiled jars
            if ((new File(compiledRulesPath + File.separator + task.getName() + ".jar")).exists()) {
                
                // found it
                return RuleLocation.COMPILED;
                
            } else {
                
                // not found
                return RuleLocation.NONE;
            }
        }
        
    }

    /**
     * Deploys plain rule.
     * @param task The task.
     * @throws AraraBadRuleParsing The rule has a bad definition.
     */
    private void deployPlainRule(AraraTask task) throws AraraBadRuleParsing {

        try {
            
            // create a representer
            Representer representer = new Representer();
            
            // add a class tag
            representer.addClassTag(PlainAraraRuleConfig.class, new Tag("!config"));
            representer.addClassTag(PlainAraraRuleArgument.class, new Tag("!argument"));

            // create a new YAML parser
            Yaml yaml = new Yaml(new Constructor(PlainAraraRuleConfig.class), representer);
            
            // new file reader
            FileReader fileReader = new FileReader(plainRulesPath + File.separator + task.getName() + ".yaml");
            
            // load rule
            PlainAraraRuleConfig plainRule = (PlainAraraRuleConfig) yaml.load(fileReader);
            
            // close reader
            fileReader.close();
            
            // if the rule name doesn't match with the file name
            if (!task.getName().equals(plainRule.getIdentifier())) {
                
                // throw exception
                throw new AraraBadRuleParsing("ERROR: File '" + task.getName() + ".yaml' found, but it has the wrong identifier (using '" + plainRule.getIdentifier() + "'). ");
            }

            // new set for arguments
            Set<String> availableArguments = new HashSet<String>();
            
            // add file (mandatory)
            availableArguments.add("file");
            
            // create a rule map
            Map ruleArguments = new HashMap();
            
            // add the arguments to the set
            for (PlainAraraRuleArgument argument : plainRule.getArguments()) {
                
                // get id
                availableArguments.add(argument.getIdentifier());
                
                // add the flags to the map
                ruleArguments.put(argument.getIdentifier(), argument.getFlag());
            }

            // another set
            Set<String> calledArguments = new HashSet<String>();
            
            // get the keys
            for (Object theKey : task.getParameters().keySet()) {
                
                // add keys to the set
                calledArguments.add((String) theKey);
            }

            // new temp set
            Set<String> checkArguments = new HashSet<String>(calledArguments);
            
            // compute the difference
            checkArguments.removeAll(availableArguments);

            // if there are still arguments
            if (!checkArguments.isEmpty()) {
                
                // there are invalid arguments
                throw new AraraBadRuleParsing("ERROR: Parsing rule '" + task.getName() + "' failed.\nPlease check the directive syntax.");
            }

            // new map with config
            Map taskConfig = task.getParameters();
            
            // command map
            Map commandMapping = new HashMap();
            
            // remove file entry
            availableArguments.remove("file");

            // for every available argument
            for (String ruleArgument : availableArguments) {

                // create a temp map
                Map vars = new HashMap();
                
                // get the template
                String ruleTemplate = (String) ruleArguments.get(ruleArgument);
                
                // template output
                String templateOutput = "";
                
                // if the key exists
                if (taskConfig.containsKey(ruleArgument)) {
                    
                    // add value to the map
                    vars.put("value", taskConfig.get(ruleArgument));
                    
                    // merge
                    templateOutput = (String) TemplateRuntime.eval(ruleTemplate, vars);
                }
                
                // put the value in the command map
                commandMapping.put(ruleArgument, templateOutput);
            }

            // put file
            commandMapping.put("file", taskConfig.get("file"));

            // get the command template
            String commandTemplate = plainRule.getCommand();
            
            // merge
            String commandOutput = (String) TemplateRuntime.eval(commandTemplate, commandMapping);

            // create a new command
            AraraCommand araraCommand = new AraraCommand();
            
            // add the command
            araraCommand.setCommand(commandOutput);
            
            // add the name
            araraCommand.setName(plainRule.getName());

            // add to the list
            commands.add(araraCommand);
            
        } catch (RuntimeException re) {

            // throw exception
            throw new AraraBadRuleParsing("ERROR: Parsing rule '" + task.getName() + "' failed.\nIt might be a malformed directive or orb tag syntax.");
            
        } catch (Exception e) {
            
            // throw exception
            throw new AraraBadRuleParsing("ERROR: Parsing rule '" + task.getName() + "' failed.\nPlease check the directive syntax.");
        }

    }

    /**
     * Deploys the compiled rule.
     * @param task The Arara task.
     * @throws AraraRuleNotFound If the file is not properly defined or not
     * found, an exception is raised.
     */
    private void deployCompiledRule(AraraTask task) throws AraraRuleNotFound {

        try {

            // create a factory
            JclObjectFactory factory = JclObjectFactory.getInstance();

            // get the compiled rule
            Object rule = factory.create(jcl, "com.github.arara.contrib.AraraRule" + capitalize(task.getName()));
            
            // build the rule and add it to a command
            AraraCommand araraCommand = ((AraraRule) rule).build(task);
            
            // add the command to the list
            commands.add(araraCommand);
            
        } catch (Exception e) {
            
            //error, throw exception
            throw new AraraRuleNotFound("ERROR: Compiled rule '" + task.getName() + "' seems to be invalid.");
        }

    }

    /**
     * Capitalizes a string.
     * @param text The string.
     * @return The new string with the first letter in uppercase.
     */
    private String capitalize(String text) {
        
        // if it's an empty string
        if (text.length() == 0) {
            
            // return text
            return text;
        }
        
        // return the new string
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
