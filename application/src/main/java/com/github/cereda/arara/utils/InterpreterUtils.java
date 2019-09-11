/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2019, Paulo Roberto Massa Cereda 
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
package com.github.cereda.arara.utils;

import com.github.cereda.arara.controller.ConfigurationController;
import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.model.AraraException;
import com.github.cereda.arara.model.Argument;
import com.github.cereda.arara.model.Command;
import com.github.cereda.arara.model.Conditional;
import com.github.cereda.arara.model.Messages;
import com.github.cereda.arara.model.Rule;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer;

/**
 * Implements interpreter utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class InterpreterUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();
    
    // get the logger context from a factory
    private static final Logger logger =
            LoggerFactory.getLogger(InterpreterUtils.class);

    /**
     * Gets a list of all rule arguments.
     * @param rule The provided rule.
     * @return A list of strings containing all rule arguments.
     */
    public static List<String> getRuleArguments(Rule rule) {
        Collection<String> result = CollectionUtils.collect(
                rule.getArguments(), new Transformer<Argument, String>() {
            public String transform(Argument input) {
                return input.getIdentifier();
            }
        });
        return new ArrayList<String>(result);
    }

    /**
     * Checks if the current conditional has a prior evaluation.
     * @param conditional The current conditional object.
     * @return A boolean value indicating if the current conditional has a prior
     * evaluation.
     */
    public static boolean runPriorEvaluation(Conditional conditional) {
        if (((Boolean) ConfigurationController.getInstance().
                get("execution.dryrun")) == true) {
            return false;
        }
        switch (conditional.getType()) {
            case IF:
            case WHILE:
            case UNLESS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Runs the command in the underlying operating system.
     * @param command An object representing the command.
     * @return An integer value representing the exit code.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static int run(Object command) throws AraraException {
        boolean verbose = (Boolean) ConfigurationController.
                getInstance().get("execution.verbose");
        boolean timeout = (Boolean) ConfigurationController.
                getInstance().get("execution.timeout");
        long value = (Long) ConfigurationController.
                getInstance().get("execution.timeout.value");
        TimeUnit unit = (TimeUnit) ConfigurationController.
                getInstance().get("execution.timeout.unit");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ProcessExecutor executor = new ProcessExecutor();
        if (CommonUtils.checkClass(Command.class, command)) {
            executor = executor.command(((Command) command).getElements());
            if (((Command) command).hasWorkingDirectory()) {
                executor = executor.directory(
                        ((Command) command).getWorkingDirectory()
                );
            }
        } else {
            executor = executor.commandSplit((String) command);
        }
        if (timeout) {
            if (value == 0) {
                throw new AraraException(
                        messages.getMessage(
                                Messages.ERROR_RUN_TIMEOUT_INVALID_RANGE
                        )
                );
            }
            executor = executor.timeout(value, unit);
        }
        TeeOutputStream tee;
        if (verbose) {
            tee = new TeeOutputStream(System.out, buffer);
            executor = executor.redirectInput(System.in);
        } else {
            tee = new TeeOutputStream(buffer);
        }
        executor = executor.redirectOutput(tee).redirectError(tee);
        ShutdownHookProcessDestroyer hook = new ShutdownHookProcessDestroyer();
        executor = executor.addDestroyer(hook);
        try {
            int exit = executor.execute().getExitValue();
            logger.info(DisplayUtils.displayOutputSeparator(
                    messages.getMessage(
                            Messages.LOG_INFO_BEGIN_BUFFER
                    )
            ));
            logger.info(buffer.toString());
            logger.info(DisplayUtils.displayOutputSeparator(
                    messages.getMessage(
                            Messages.LOG_INFO_END_BUFFER
                    )
            ));
            return exit;
        } catch (IOException ioexception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_IO_EXCEPTION
                    ),
                    ioexception
            );
        } catch (InterruptedException iexception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_INTERRUPTED_EXCEPTION
                    ),
                    iexception
            );
        } catch (InvalidExitValueException ievexception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_INVALID_EXIT_VALUE_EXCEPTION
                    ),
                    ievexception
            );
        } catch (TimeoutException texception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_TIMEOUT_EXCEPTION
                    ),
                    texception
            );
        } catch (Exception exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_RUN_GENERIC_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Builds the rule path based on the rule name and returns the corresponding
     * file location.
     * @param name The rule name.
     * @return The rule file.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static File buildRulePath(String name) throws AraraException {
        @SuppressWarnings("unchecked")
        List<String> paths = (List<String>) ConfigurationController.
                getInstance().get("execution.rule.paths");
        for (String path : paths) {
            File location = new File(construct(path, name));
            if (location.exists()) {
                return location;
            }
        }
        return null;
    }

    /**
     * Constructs the path given the current path and the rule name.
     * @param path The current path.
     * @param name The rule name.
     * @return The constructed path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static String construct(String path, String name)
            throws AraraException {
        name = name.concat(".yaml");
        File location = new File(path);
        if (location.isAbsolute()) {
            return CommonUtils.buildPath(path, name);
        } else {
            File reference = (File) ConfigurationController.
                    getInstance().get("execution.reference");
            String parent = CommonUtils.buildPath(
                    CommonUtils.getParentCanonicalPath(reference), path);
            return CommonUtils.buildPath(parent, name);
        }
    }

}
