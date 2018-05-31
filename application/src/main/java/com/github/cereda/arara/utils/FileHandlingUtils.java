/**
 * Arara, the cool TeX automation tool
 * Copyright (c) 2012 -- 2018, Paulo Roberto Massa Cereda 
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * Implements file handling utilitary methods.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class FileHandlingUtils {

    /**
     * Writes the string to a file, using UTF-8 as default encoding.
     * @param file The file.
     * @param text The string to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    public static boolean writeToFile(File file, String text, boolean append) {
        try {

            // try to write the provided
            // string to the file, with
            // UTF-8 as encoding
            FileUtils.writeStringToFile(file, text, "UTF-8", append);
            return true;

        } catch (IOException nothandled) {

            // if something bad happens,
            // gracefully fallback to
            // reporting the failure
            return false;
        }
    }

    /**
     * Writes the string list to a file, using UTF-8 as default encoding.
     * @param file The file.
     * @param lines The string list to be written.
     * @param append A flag whether to append the content.
     * @return A logical value indicating whether it was successful.
     */
    public static boolean writeToFile(File file, List<String> lines,
            boolean append) {
        try {

            // try to write the provided
            // string lists to the file,
            // with UTF-8 as encoding
            FileUtils.writeLines(file, "UTF-8", lines, append);
            return true;

        } catch (IOException nothandled) {

            // if something bad happens,
            // gracefully fallback to
            // reporting the failure
            return false;
        }
    }

    /**
     * Reads the provided file into a list of strings.
     * @param file The file.
     * @return A list of strings.
     */
    public static List<String> readFromFile(File file) {
        try {

            // returns the contents of
            // the provided file as
            // a list of strings
            return FileUtils.readLines(file, "UTF-8");

        } catch (IOException nothandled) {

            // if something bad happens,
            // gracefully fallback to
            // an empty file list
            return new ArrayList<String>();
        }
    }

}
