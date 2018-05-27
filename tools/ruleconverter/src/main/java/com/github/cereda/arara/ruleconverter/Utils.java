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

    public static File getOutput(File file) {
        String path = file.getAbsolutePath();
        return new File(path.substring(
                0,
                path.lastIndexOf(".")
        ).concat("_v4.yaml"));
    }

    public static NRule update(ORule from) {
        NRule to = new NRule();
        to.setIdentifier(from.getIdentifier());
        to.setName(from.getName());
        List<NCommand> commands = new ArrayList<>();
        List<NArgument> arguments = new ArrayList<>();

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

        for (OArgument a : from.getArguments()) {
            NArgument argument = new NArgument();
            argument.setIdentifier(a.getIdentifier());
            argument.setFlag(a.getFlag());
            arguments.add(argument);
        }

        to.setCommands(commands);
        to.setArguments(arguments);

        return to;
    }

    public static ORule read(File file) throws Exception {
        try {
            FileReader reader = new FileReader(file);
            Representer representer = new Representer();
            representer.addClassTag(ORule.class, new Tag("!config"));
            Yaml yaml = new Yaml(new Constructor(ORule.class), representer);
            return yaml.loadAs(reader, ORule.class);
        } catch (FileNotFoundException nothandled1) {
            throw new Exception("I am sorry to inform you that the "
                    + "provided file does not exist! Please, make sure "
                    + "to provide a valid YAML file containing the old "
                    + "rule as a parameter and try again.");
        } catch (MarkedYAMLException nothandled2) {
            throw new Exception("I am sorry to inform you that the YAML "
                    + "rule seems invalid! Please, make sure the rule "
                    + "contains the correct keys and try again. Sadly, "
                    + "I cannot help you on this issue.");
        }
    }

    public static void write(NRule rule, File file) throws Exception {
        try {
            FileWriter writer = new FileWriter(file);
            Representer representer = new Representer();
            representer.addClassTag(NRule.class, new Tag("!config"));
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            options.setPrettyFlow(true);
            Yaml yaml = new Yaml(
                    new Constructor(NRule.class),
                    representer,
                    options
            );
            yaml.dump(rule, writer);
        } catch (IOException nothandled) {
            throw new Exception("I am sorry to inform you that the new rule "
                    + "could not be written! Please make sure the target "
                    + "directory has the proper permissions and try again.");
        }
    }

}
