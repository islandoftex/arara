/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012, Paulo Roberto Massa Cereda 
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
package com.github.cereda.arara.model;

import com.github.cereda.arara.controller.ConfigurationController;
import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.utils.CommonUtils;
import com.github.cereda.arara.utils.ConfigurationUtils;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implements the configuration model, which holds the default settings and can
 * load the configuration file.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class Configuration {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Loads the application configuration.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static void load() throws AraraException {

        // initialize both file type and language models,
        // since we can track errors from there instead
        // of relying on a check on this level
        FileType.init();
        Language.init();

        // reset everything
        reset();

        // get the configuration file, if any
        File file = ConfigurationUtils.getConfigFile();
        if (file != null) {
            
            // set the configuration file name for
            // logging purposes
            ConfigurationController.getInstance().
                    put("execution.configuration.name",
                            CommonUtils.getCanonicalPath(file)
                    );
            
            // then validate it and update the
            // configuration accordingly
            Resource resource = ConfigurationUtils.validateConfiguration(file);
            update(resource);
        }

        // just to be sure, update the
        // current locale in order to
        // display localized messages
        Locale locale = ((Language) ConfigurationController.
                getInstance().get("execution.language")).getLocale();
        LanguageController.getInstance().setLocale(locale);
    }

    /**
     * Resets the configuration to initial settings.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private static void reset() throws AraraException {

        // put everything in a map to be
        // later assigned to the configuration
        // controller, which holds the settings
        Map<String, Object> mapping = new HashMap<String, Object>();

        mapping.put("execution.loops", 10L);
        mapping.put("directives.charset", Charset.forName("UTF-8"));
        mapping.put("execution.errors.halt", true);
        mapping.put("execution.timeout", false);
        mapping.put("execution.timeout.value", 0L);
        mapping.put("execution.timeout.unit", TimeUnit.MILLISECONDS);
        mapping.put("application.version", "4.0");
        mapping.put("directives.linebreak.pattern", "^\\s*-->\\s(.*)$");

        String directive = "^\\s*(\\w+)\\s*(:\\s*(\\{.*\\})\\s*)?";
        String pattern = "(\\s+(if|while|until|unless)\\s+(\\S.*))?$";

        mapping.put("directives.pattern", directive.concat(pattern));
        mapping.put("application.pattern", "arara:\\s");
        mapping.put("application.width", 65);
        mapping.put("execution.database.name", "arara");
        mapping.put("execution.log.name", "arara");
        mapping.put("execution.verbose", false);

        mapping.put("trigger.halt", false);
        mapping.put("execution.language", new Language("en"));
        mapping.put("execution.logging", false);
        mapping.put("execution.dryrun", false);
        mapping.put("execution.status", 0);
        mapping.put("application.copyright.year", "2012");
        mapping.put("execution.filetypes", ConfigurationUtils.
                getDefaultFileTypes());
        mapping.put("execution.rule.paths",
                Arrays.asList(
                        CommonUtils.buildPath(
                                ConfigurationUtils.getApplicationPath(),
                                "rules"
                        )
                )
        );
        
        mapping.put("execution.preambles", new HashMap<String, String>());
        mapping.put("execution.preamble.active", false);
        mapping.put("execution.configuration.name", "[none]");
        mapping.put("execution.header", false);

        // get the configuration controller and
        // set every map key to it
        ConfigurationController controller =
                ConfigurationController.getInstance();
        for (String key : mapping.keySet()) {
            controller.put(key, mapping.get(key));
        }
    }

    /**
     * Update the configuration based on the provided map.
     * @param data Map containing the new configuration settings.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    private static void update(Resource resource) throws AraraException {

        ConfigurationController controller =
                ConfigurationController.getInstance();

        if (resource.getPaths() != null) {
            List<String> paths = resource.getPaths();
            paths = ConfigurationUtils.normalizePaths(paths);
            controller.put("execution.rule.paths", paths);
        }

        if (resource.getFiletypes() != null) {
            List<FileTypeResource> resources = resource.getFiletypes();
            List<FileType> filetypes = new ArrayList<FileType>();
            for (FileTypeResource type : resources) {
                if (type.getPattern() != null) {
                    filetypes.add(
                            new FileType(type.getExtension(), type.getPattern())
                    );
                } else {
                    filetypes.add(new FileType(type.getExtension()));
                }
            }
            filetypes = ConfigurationUtils.normalizeFileTypes(filetypes);
            controller.put("execution.filetypes", filetypes);
        }

        controller.put("execution.verbose", resource.isVerbose());
        controller.put("execution.logging", resource.isLogging());
        controller.put("execution.header", resource.isHeader());

        if (resource.getDbname() != null) {
            controller.put("execution.database.name",
                    ConfigurationUtils.cleanFileName(resource.getDbname()));
        }

        if (resource.getLogname() != null) {
            controller.put("execution.log.name",
                    ConfigurationUtils.cleanFileName(resource.getLogname()));
        }

        if (resource.getLanguage() != null) {
            controller.put("execution.language",
                    new Language(resource.getLanguage()));
        }

        long loops = resource.getLoops();
        if (loops > 0) {
            controller.put("execution.loops", loops);
        } else {
            if (loops < 0) {
                throw new AraraException(
                        messages.getMessage(
                                Messages.ERROR_CONFIGURATION_LOOPS_INVALID_RANGE
                        )
                );
            }
        }
        
        if (resource.getPreambles() != null) {
            controller.put("execution.preambles",
                    resource.getPreambles());
        }
        
    }

}
