/**
 * CTAN builder, a tool for Arara
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
package com.github.cereda.arara.ctanbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

/**
 * Provides utilitary methods.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Utils {

    // maximum width
    private static final int WIDTH = 74;

    // file type for
    // further checks
    public enum Type {
        FILE,
        DIRECTORY
    }

    /**
     * Creates a shell script file for arara.
     * @param file The file reference.
     * @throws Exception The file could not be written.
     */
    static void createScript(File file) throws Exception {
        List<String> lines = new ArrayList<>();
        lines.add("#!/bin/bash");
        lines.add("jarpath=`kpsewhich --progname=arara "
                + "--format=texmfscripts arara.jar`");
        lines.add("java -jar \"$jarpath\" \"$@\"");
        try {
            FileUtils.writeLines(file, lines);
        } catch (IOException nothandled) {
            throw new Exception("I could not create the shell script for "
                    + "arara due to an IO error. Please make sure the "
                    + "current directory has the correct permissions "
                    + "and try again. The application will halt now.");
        }
    }

    /**
     * Checks whether the commands are available in the system path.
     * @param commands Array of commands to be checked.
     * @throws Exception One of the commands does not exist.
     */
    public static void available(String... commands) throws Exception {
        boolean result;
        try {
            result = new ProcessExecutor().command(commands).
                    execute().getExitValue() == 0;
        } catch (IOException | InterruptedException
                | TimeoutException | InvalidExitValueException nothandled) {
            result = false;
        }
        if (!result) {
            throw new Exception("The command has returned an invalid "
                    + "exit value. Chances are the command is not "
                    + "available in the system path. Make sure the "
                    + "command exists and try again. The application "
                    + "will halt now.");
        }
    }

    /**
     * Checks whether the provided file reference exists and the corresponding
     * type is correct.
     * @param type The file type.
     * @param path The file path to be dynamically build.
     * @throws Exception File does not exist or the type is incorrect.
     */
    public static void exists(Type type, String... path) throws Exception {
        File file = file(path);
        if (!file.exists() || !checkType(type, file)) {
            if (!file.exists()) {
                throw new Exception("The provided file reference does not "
                        + "exist. Make sure to provide the correct path "
                        + "and try again. The application will halt now.");
            } else {
                throw new Exception("The provided file reference does "
                        + "exist, but it has a wrong type! Make sure to "
                        + "provide the correct file type and try again. "
                        + "The application will halt now.");
            }
        }
    }

    /**
     * Removes the provided directory.
     * @param directory The directory to be removed.
     * @throws Exception The directory could not be removed.
     */
    public static void removeDirectory(File directory) throws Exception {
        try {
            FileUtils.deleteDirectory(directory);
        } catch (IOException nothandled) {
            throw new Exception("The directory could not be removed due to "
                    + "an IO error. Make sure the current directory has the "
                    + "correct permissions and try again. The application "
                    + "will halt now.");
        }
    }

    /**
     * Creates a file reference from a string path array.
     * @param path Array of string paths.
     * @return A file reference.
     */
    public static File file(String... path) {
        return new File(String.join(File.separator, path));
    }

    /**
     * Executes the command and arguments in the provided directory.
     * @param directory The working directory.
     * @param call The proper call with the command and arguments.
     * @throws Exception An error has occurred during the execution.
     */
    public static void execute(File directory, String... call)
            throws Exception {
        boolean result;
        try {
            result = new ProcessExecutor().command(call).
                    directory(directory).execute().getExitValue() == 0;
        } catch (IOException | InterruptedException
                | TimeoutException | InvalidExitValueException nothandled) {
            result = false;
        }
        if (!result) {
            throw new Exception("The command call has returned an invalid "
                    + "exit value. Chances are the arguments are incorrect. "
                    + "Make sure the call contains valid arguments and try "
                    + "again. The application will halt now.");
        }
    }

    /**
     * Creates a directory.
     * @param file The directory to be created.
     * @throws Exception The directory could not be created.
     */
    public static void mkdir(File file) throws Exception {
        try {
            Files.createDirectory(file.toPath());
        } catch (IOException nothandled) {
            throw new Exception("The provided directory could not be created "
                    + "due to an IO error. Make sure the top level directory "
                    + "has the correct permissions and try again. The "
                    + "application will halt now.");
        }
    }

    /**
     * Copies a directory to another.
     * @param from Source directory.
     * @param to Target directory.
     * @throws Exception The directory could not be copied.
     */
    public static void copyDirectory(File from, File to) throws Exception {
        try {
            FileUtils.copyDirectoryToDirectory(from, to);
        } catch (IOException nothandled) {
            throw new Exception("The directory could not be copied to a "
                    + "new location due to an IO error. Make sure the new "
                    + "location has the correct permissions and try again. "
                    + "The application will halt now.");
        }
    }

    /**
     * Copies a file to another location.
     * @param from Source file.
     * @param to Target file.
     * @throws Exception The file could not be copied.
     */
    public static void copyFile(File from, File to) throws Exception {
        try {
            FileUtils.copyFile(from, to);
        } catch (IOException nothandled) {
            throw new Exception("The provided file could not be copied to "
                    + "the new location due to an IO error. Make sure the "
                    + "new location has the correct permissions and try "
                    + "again. The application will halt now.");
        }
    }

    /**
     * Checks whether the provided file has the correct type.
     * @param type The file type.
     * @param file The file to be checked.
     * @return A logical boolean indicating whether the type is correct.
     */
    private static boolean checkType(Type type, File file) {
        if (type == Type.FILE) {
            return file.isFile();
        } else {
            return file.isDirectory();
        }
    }

    /**
     * Prints the task name in the terminal.
     * @param name The task name.
     */
    public static void task(String name) {
        System.out.print(StringUtils.
                rightPad(name.concat(" "), WIDTH - 8, "."));
    }

    /**
     * Prints the title in the terminal.
     * @param name The title to be printed.
     */
    public static void title(String name) {
        line();
        System.out.println(StringUtils.center(name, WIDTH));
        line();
    }

    /**
     * Prints a message in the terminal.
     * @param text The message itself.
     */
    public static void message(String text) {
        System.out.println(WordUtils.wrap(text, WIDTH, "\n", true));
    }

    /**
     * Prints a dashed line.
     */
    public static void line() {
        System.out.println(StringUtils.repeat("-", WIDTH));
    }

    /**
     * Adds a line break in the terminal.
     */
    public static void space() {
        System.out.println();
    }

    /**
     * Prints the task status according to the provided value.
     * @param value A logical value.
     */
    public static void status(boolean value) {
        System.out.println(" " + (value ? "SUCCESS" : "FAILURE"));
    }

    /**
     * Prints an array of files and corresponding file sizes.
     * @param files An array of files.
     */
    public static void sizes(File... files) {
        int i = max(files) + 1;
        for (File file : files) {
            String size = FileUtils.byteCountToDisplaySize(file.length());
            System.out.println(StringUtils.rightPad(file.getName().
                    concat(" "), WIDTH - i - 1, ".").
                    concat(StringUtils.leftPad(size, i)));
        }
    }

    /**
     * Gets the maximum length of a file size.
     * @param files The array of files to be checked.
     * @return An integer value.
     */
    private static int max(File... files) {
        int i = 0;
        for (File file : files) {
            String s = FileUtils.byteCountToDisplaySize(file.length());
            if (i < s.length()) {
                i = s.length();
            }
        }
        return i;
    }

    public static void draw() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ______ _______ _______ _______   __           __ __     __             ");
        sb.append("\n");
        sb.append("|      |_     _|   _   |    |  | |  |--.--.--.|__|  |.--|  |.-----.----.");
        sb.append("\n");
        sb.append("|   ---| |   | |       |       | |  _  |  |  ||  |  ||  _  ||  -__|   _|");
        sb.append("\n");
        sb.append("|______| |___| |___|___|__|____| |_____|_____||__|__||_____||_____|__|  ");
        System.out.println(sb.toString());                               
    }
    
}
