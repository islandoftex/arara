/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2017, Paulo Roberto Massa Cereda
 * All rights reserved.
 *
 * Redistribution and  use in source  and binary forms, with  or without
 * modification, are  permitted provided  that the  following conditions
 * are met:
 *
 * 1. Redistributions  of source  code must  retain the  above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form  must reproduce the above copyright
 * notice, this list  of conditions and the following  disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither  the name  of the  project's author nor  the names  of its
 * contributors may be used to  endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS  PROVIDED BY THE COPYRIGHT  HOLDERS AND CONTRIBUTORS
 * "AS IS"  AND ANY  EXPRESS OR IMPLIED  WARRANTIES, INCLUDING,  BUT NOT
 * LIMITED  TO, THE  IMPLIED WARRANTIES  OF MERCHANTABILITY  AND FITNESS
 * FOR  A PARTICULAR  PURPOSE  ARE  DISCLAIMED. IN  NO  EVENT SHALL  THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE  LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY,  OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT  NOT LIMITED  TO, PROCUREMENT  OF SUBSTITUTE  GOODS OR  SERVICES;
 * LOSS  OF USE,  DATA, OR  PROFITS; OR  BUSINESS INTERRUPTION)  HOWEVER
 * CAUSED AND  ON ANY THEORY  OF LIABILITY, WHETHER IN  CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY  OUT  OF  THE USE  OF  THIS  SOFTWARE,  EVEN  IF ADVISED  OF  THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.cereda.arara;

import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.controller.LoggingController;
import com.github.cereda.arara.model.AraraException;
import com.github.cereda.arara.model.Configuration;
import com.github.cereda.arara.model.Directive;
import com.github.cereda.arara.model.Extractor;
import com.github.cereda.arara.model.Interpreter;
import com.github.cereda.arara.model.Parser;
import com.github.cereda.arara.model.StopWatch;
import com.github.cereda.arara.utils.CommonUtils;
import com.github.cereda.arara.utils.DirectiveUtils;
import com.github.cereda.arara.utils.DisplayUtils;
import java.util.List;

/**
 * Main class. This class wraps all classes from the application model as well
 * as utilitary classes in order to provide modularity.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Arara {

    /**
     * Main method. This is the application entry point.
     * @param args A string array containing all command line arguments.
     */
    public static void main(String[] args) {

        // the first component to be initialized is the
        // language controller; note that init() actually
        // has no body at all, but it's a dirty maneuver to
        // trigger the static class startup
        LanguageController.init();

        // the second component to be initalized is the
        // logging controller; note init() actually disables
        // the logging, so early exceptions won't generate
        // a lot of noise in the terminal
        LoggingController.init();

        // print the arara logo in the terminal; I just
        // hope people use this tool in a good terminal with
        // fixed-width fonts, otherwise the logo will be messed
        DisplayUtils.printLogo();

        try {

            // first of all, let's try to load a potential
            // configuration file located at the current
            // user's home directory; if there's a bad
            // configuration file, arara will panic and
            // end the execution
            Configuration.load();

            // if we are here, either there was no configuration
            // file at all or we managed to load the settings; now,
            // it's time to properly parse the command line arguments;
            // this is done by creating a brand new instance of arara's
            // command line parser and providing the string array to it
            Parser parser = new Parser(args);

            // now let's see if we are good to go; parse() will return
            // a boolean value indicating if the provided arguments
            // allow the tool to continue (we might reach some special
            // flags as well, like --help or --version, which simply
            // do their jobs and return false, since there's no point
            // of continuing processing with such flags)
            if (parser.parse()) {

                // let's print the current file information; it is a
                // basic display, just the file name, the size properly
                // formatted as a human readable format, and the last
                // modification date; also, in this point, the logging
                // feature starts to collect data (of course, if enabled
                // either through the configuration file or manually
                // in the command line)
                DisplayUtils.printFileInformation();

                // time to read the file and try to extract the directives;
                // this class does a pretty good job on finding directives,
                // including the multiline ones; it was a long awaited
                // feature people were asking me to implement, so here
                // it is!
                Extractor extractor = new Extractor();

                // extract() brings us a list of directives properly parsed
                // and almost ready to be handled; note that no directives
                // in the provided file will raise an exception; this is
                // by design and I opted to not include a default fallback
                // (although it wouldn't be so difficult to write one,
                // I decided not to take the risk)
                List<Directive> directives = extractor.extract();

                // once we have our nice list of directives, it is time to
                // actually validate them (for example, we have a couple of
                // keywords that cannot be used as directive parameters);
                // another interesting feature of the validate() method is
                // to replicate a directive that has the 'files' keyword on
                // it, since it's the whole point of having 'files' in the
                // first place; if you check the log file, you will see
                // that the list of extracted directives might differ from
                // the final list of directives to be effectively processed
                // by arara
                directives = DirectiveUtils.validate(directives);

                // arara features now a cool stopwatch, so we can see how
                // much time has passed since everything started; start(),
                // for obvious reasons, starts the stopwatch and keeps track
                // of time for us; internally, this class makes use of
                // nano time, so we might get an interesting precision here
                // (although timing is not a serious business in here, it's
                // just a cool addition)
                StopWatch.start();

                // this is surely the golden heart of arara; this class
                // implements a powerful interpreter that will handle all
                // rules and their corresponding tasks
                Interpreter interpreter = new Interpreter();

                // once we have this bad boy ready, let's provide the list
                // of directives previously extracted and validated; it is
                // like loading a cannon, I guess
                interpreter.setDirectives(directives);

                // time to shine, now the interpreter class will interpret
                // one directive at a time, get the corresponding rule,
                // set the parameters, evaluate it, get the tasks, run them,
                // evaluate the result and print the status; note that
                // arara, from this version on, will try to evaluate things
                // progressively, so in case of an error, the previous tasks
                // were already processed and potentially executed
                interpreter.execute();
            }

        } catch (AraraException exception) {

            // something bad just happened, so arara will print the proper
            // exception and provide details on it, if available; the idea
            // here is to propagate an exception throughout the whole
            // application and catch it here instead of a local treatment
            DisplayUtils.printException(exception);
        }

        // we are done here (with or without errors, that makes no difference
        // at this point), so let's stop our stopwatch; now it's just an easy
        // subtraction to be made (note that the values are internally
        // represented as nanoseconds, but the result is printed as seconds)
        StopWatch.stop();

        // this is the last command from arara; once the execution time is
        // available, print it; note that this notification is suppressed
        // when the command line parsing returns false as result (it makes
        // no sense to print the execution time for a help message, I guess)
        DisplayUtils.printTime();

        // gets the application exit status; the rule here is:
        // 0 : everything went just fine (note that the dry-run mode always
        //     makes arara exit with 0, unless it is an error in the directive
        //     builder itself).
        // 1 : one of the tasks failed, so the execution ended abruptly. This
        //     means the error relies on the command line call, not with arara.
        // 2 : arara just handled an exception, meaning that something bad
        //     just happened and might require user intervention.
        System.exit(CommonUtils.getExitStatus());

        // I owe David Carlisle $10
    }

}
