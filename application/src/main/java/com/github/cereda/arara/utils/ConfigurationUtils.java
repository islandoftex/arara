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
package com.github.cereda.arara.utils;

import com.github.cereda.arara.Arara;
import com.github.cereda.arara.controller.LanguageController;
import com.github.cereda.arara.model.AraraException;
import com.github.cereda.arara.model.FileType;
import com.github.cereda.arara.model.FileTypeResource;
import com.github.cereda.arara.model.Messages;
import com.github.cereda.arara.model.Resource;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.SystemUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Implements configuration utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class ConfigurationUtils {

    // the application messages obtained from the
    // language controller
    private static final LanguageController messages =
            LanguageController.getInstance();

    /**
     * Gets the list of default file types provided by nightingale, in order.
     * @return The list of default file types, in order.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static List<FileType> getDefaultFileTypes() throws AraraException {
        return Arrays.asList(
                new FileType("tex"),
                new FileType("dtx"),
                new FileType("ltx"),
                new FileType("drv"),
                new FileType("ins")
        );
    }

    /**
     * Gets the configuration file located at the user home directory, if any.
     * @return The file reference to the external configuration, if any.
     */
    public static File getConfigFile() {
        List<String> names = Arrays.asList(
                ".araraconfig.yaml",
                "araraconfig.yaml",
                ".arararc.yaml",
                "arararc.yaml"
        );
        
        // look for configuration files in the user's working directory first
        for (String name : names) {
            String path = CommonUtils.buildPath(SystemUtils.USER_DIR, name);
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        
        // if no configuration files are found in the user's working directory,
        // try to look up in a global directory, that is, the user home
        for (String name : names) {
            String path = CommonUtils.buildPath(SystemUtils.USER_HOME, name);
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    /**
     * Validates the configuration file.
     * @param file The configuration file.
     * @return The configuration file as a resource.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static Resource validateConfiguration(File file)
            throws AraraException {

        Representer representer = new Representer();
        representer.addClassTag(Resource.class, new Tag("!config"));
        Yaml yaml = new Yaml(new Constructor(Resource.class), representer);
        try {
            Resource resource = yaml.loadAs(new FileReader(file),
                    Resource.class);
            if (resource.getFiletypes() != null) {
                List<FileTypeResource> filetypes = resource.getFiletypes();
                if (CollectionUtils.exists(filetypes,
                        new Predicate<FileTypeResource>() {
                    public boolean evaluate(FileTypeResource filetype) {
                        return (filetype.getExtension() == null);
                    }
                })) {
                    throw new AraraException(
                            messages.getMessage(
                                    Messages.ERROR_CONFIGURATION_FILETYPE_MISSING_EXTENSION
                            )
                    );
                }
            }
            return resource;
        } catch (MarkedYAMLException yamlException) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_CONFIGURATION_INVALID_YAML
                    ),
                    yamlException
            );
        } catch (Exception exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_CONFIGURATION_GENERIC_ERROR
                    )
            );
        }
    }

    /**
     * Normalize a list of rule paths, removing all duplicates.
     * @param paths The list of rule paths.
     * @return A list of normalized paths, without duplicates.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static List<String> normalizePaths(List<String> paths)
            throws AraraException {
        paths.add(CommonUtils.buildPath(getApplicationPath(), "rules"));
        Set<String> set = new LinkedHashSet<String>(paths);
        List<String> result = new ArrayList<String>(set);
        return result;
    }

    /**
     * Normalize a list of file types, removing all duplicates.
     * @param types The list of file types.
     * @return A list of normalized file types, without duplicates.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static List<FileType> normalizeFileTypes(List<FileType> types)
            throws AraraException {
        types.addAll(getDefaultFileTypes());
        Set<FileType> set = new LinkedHashSet<FileType>(types);
        List<FileType> result = new ArrayList<FileType>(set);
        return result;
    }

    /**
     * Gets the canonical absolute application path.
     * @return The string representation of the canonical absolute application
     * path.
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    public static String getApplicationPath() throws AraraException {
        try {
            String path = Arara.class.getProtectionDomain().
                    getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            path = new File(path).getParentFile().getPath();
            return path;
        } catch (UnsupportedEncodingException exception) {
            throw new AraraException(
                    messages.getMessage(
                            Messages.ERROR_GETAPPLICATIONPATH_ENCODING_EXCEPTION
                    ),
                    exception
            );
        }
    }

    /**
     * Cleans the file name to avoid invalid entries.
     * @param name The file name.
     * @return A cleaned file name.
     */
    public static String cleanFileName(String name) {
        String result = (new File(name)).getName().trim();
        if (CommonUtils.checkEmptyString(result)) {
            return "arara";
        } else {
            return result.trim();
        }
    }

}
