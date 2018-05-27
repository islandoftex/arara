/**
 * Rule converter, a tool for Arara
 * Copyright (c) 2018, Paulo Roberto Massa Cereda
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated documentation  files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, sublicense,  and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and this  permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT  SHALL THE AUTHORS OR COPYRIGHT HOLDERS  BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR  OTHER LIABILITY, WHETHER IN AN  ACTION OF CONTRACT,
 * TORT OR  OTHERWISE, ARISING  FROM, OUT  OF OR  IN CONNECTION  WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.cereda.arara.ruleconverter;

import com.github.cereda.arara.ruleconverter.model.NArgument;
import com.github.cereda.arara.ruleconverter.model.NCommand;
import com.github.cereda.arara.ruleconverter.model.NRule;
import com.github.cereda.arara.ruleconverter.model.OArgument;
import com.github.cereda.arara.ruleconverter.model.ORule;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Utilitary methods.
 * 
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Utils {

    /**
     * Gets the output file based on the input file name.
     * @param file The input file reference.
     * @return The new output file reference.
     */
    public static File getOutput(File file) {
        
        // first of all, we get
        // the absolute path
        String path = file.getAbsolutePath();
        
        // we build the output file based
        // on the name of the input file
        // plus a suffix of a new format
        return new File(path.substring(
                0,
                path.lastIndexOf(".")
        ).concat("_v4.yaml"));
    }

    /**
     * Updates the rule in the old format to the new format.
     * @param from Rule in the old format.
     * @return Rule in the new format.
     */
    public static NRule update(ORule from) {
        
        // creates the rule structure
        // in the new format and sets
        // a couple of keys
        NRule to = new NRule();
        to.setIdentifier(from.getIdentifier());
        to.setName(from.getName());
        List<NCommand> commands = new ArrayList<>();
        List<NArgument> arguments = new ArrayList<>();

        // creates the list of
        // rule commands
        if (from.getCommand() != null) {
            NCommand command = new NCommand();
            command.setCommand(from.getCommand());
            commands.add(command);
        } else {
            for (String c : from.getCommands()) {
                NCommand command = new NCommand();
                command.setCommand(c);
                commands.add(command);
            }
        }

        // creates the list of
        // rule arguments
        for (OArgument a : from.getArguments()) {
            NArgument argument = new NArgument();
            argument.setIdentifier(a.getIdentifier());
            
            // set the argument flag
            // if exists in the definition
            if (a.getFlag() != null) {
                argument.setFlag(a.getFlag());
            }
            
            // set the argument fallback
            // if exists in the definition
            if (a.getDefault() != null) {
                argument.setDefault(a.getDefault());
            }
            
            arguments.add(argument);
        }

        // sets both commands
        // and arguments to
        // the new rule
        to.setCommands(commands);
        to.setArguments(arguments);

        // return the rule
        // in the new format
        return to;
    }

    /**
     * Read rule from a file.
     * @param file File containing the rule.
     * @return Rule in the old format from a file.
     * @throws Exception An exception is thrown when something bad happened.
     */
    public static ORule read(File file) throws Exception {
        try {
            
            // create a file reader based
            // on the provided file
            FileReader reader = new FileReader(file);
            
            // we need to create a representer
            // to properly handle the tag
            Representer representer = new Representer();
            representer.addClassTag(ORule.class, new Tag("!config"));
            
            // then we can simply create the
            // YAML parser and provide both
            // representer and a constructor
            Yaml yaml = new Yaml(new Constructor(ORule.class), representer);
            
            // return the new
            // rule object
            return yaml.loadAs(reader, ORule.class);
        } catch (FileNotFoundException nothandled1) {
            
            // the input file
            // was not found
            throw new Exception("I am sorry to inform you that the "
                    + "provided file does not exist! Please, make sure "
                    + "to provide a valid YAML file containing the old "
                    + "rule as a parameter and try again.");
        } catch (MarkedYAMLException nothandled2) {
            
            // there was an error in the
            // provided YAML file
            throw new Exception("I am sorry to inform you that the YAML "
                    + "rule seems invalid! Please, make sure the rule "
                    + "contains the correct keys and try again. Sadly, "
                    + "I cannot help you on this issue.");
        }
    }

    /**
     * Writes the rule in the new format to a file.
     * @param rule The rule in the new format.
     * @param file The output file.
     * @throws Exception An exception is thrown when something bad happened.
     */
    public static void write(NRule rule, File file) throws Exception {
        try {
            
            // create a writer based
            // on the input file
            FileWriter writer = new FileWriter(file);
            
            // we need to create a representer
            // in order to properly handle
            // the configuration tag
            Representer representer = new Representer() {
                @Override
                protected NodeTuple representJavaBeanProperty(Object bean,
                        Property property, Object value, Tag tag) {
                    if (value == null) {
                        return null;
                    }
                    else {
                        return super.representJavaBeanProperty(
                                bean,
                                property,
                                value,
                                tag
                        );
                    }
                }
            };
            representer.addClassTag(NRule.class, new Tag("!config"));
            
            // as we are dumping the format to
            // a file, we need to ensure the flow
            // and scalar styles are properly set
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            
            // we also need to ensure the
            // flow is in a readable format
            options.setPrettyFlow(true);
            
            // create the YAML parser based
            // on the representer, the
            // constructor and dumper
            Yaml yaml = new Yaml(
                    new Constructor(NRule.class),
                    representer,
                    options
            );
            
            // dump the rule object
            // to the writer
            yaml.dump(rule, writer);
            
            // let us properly
            // close the writer
            writer.close();
            
        } catch (IOException nothandled) {
            
            // an IO exception was thrown,
            // we need to inform the user
            throw new Exception("I am sorry to inform you that the new rule "
                    + "could not be written! Please make sure the target "
                    + "directory has the proper permissions and try again.");
        }
    }

}
