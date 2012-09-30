/**
 * \cond LICENSE
 * Arara -- the cool TeX automation tool
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
 * \endcond
 * 
 * AraraMethods: This class contains methods for the MVEL expression language to
 * be used in the rules.
 */
// package definition
package com.github.arara.utils;

// needed import
import java.io.File;
import org.apache.commons.lang3.SystemUtils;

/**
 * Contains methods for the MVEL expression language to be used in the rules.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class AraraMethods {

    /**
     * Checks if string is empty.
     *
     * @param string The string.
     * @return A logical value according to the condition.
     */
    public static boolean isEmpty(String string) {
        return string.isEmpty();
    }

    /**
     * Checks if string is not empty.
     *
     * @param string The string.
     * @return A logical value according to the condition.
     */
    public static boolean isNotEmpty(String string) {
        return !string.isEmpty();
    }

    /**
     * Checks if string is empty.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @return A string according the condition.
     */
    public static String isEmpty(String string, String ifTrue) {
        return (string.isEmpty() ? ifTrue : string);
    }

    /**
     * Checks if string is not empty.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @return A string according the condition.
     */
    public static String isNotEmpty(String string, String ifTrue) {
        return (!string.isEmpty() ? ifTrue : string);
    }

    /**
     * Checks if string is empty.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @param ifFalse Value if false.
     * @return A string according the condition.
     */
    public static String isEmpty(String string, String ifTrue, String ifFalse) {
        return (string.isEmpty() ? ifTrue : ifFalse);
    }

    /**
     * Checks if string is not empty.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @param ifFalse Value if false.
     * @return A string according the condition.
     */
    public static String isNotEmpty(String string, String ifTrue, String ifFalse) {
        return (!string.isEmpty() ? ifTrue : ifFalse);
    }

    /**
     * Checks if string resolves to a logic value representing true.
     *
     * @param string The string.
     * @return A string according the condition.
     */
    public static boolean isTrue(String string) {
        string = string.toLowerCase();
        if ((string.equals("yes")) || (string.equals("on")) || (string.equals("true"))
           || (string.equals("y")) || (string.equals("1"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if string resolves to a logic value representing false.
     *
     * @param string The string.
     * @return A string according the condition.
     */
    public static boolean isFalse(String string) {
        string = string.toLowerCase();
        if ((string.equals("no")) || (string.equals("off")) || (string.equals("false"))
           || (string.equals("n")) || (string.equals("0"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if string resolves to a logic value representing true.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @return A string according the condition.
     */
    public static String isTrue(String string, String ifTrue) {
        return (isTrue(string) ? ifTrue : "");
    }

    /**
     * Checks if string resolves to a logic value representing false.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @return A string according the condition.
     */
    public static String isFalse(String string, String ifTrue) {
        return (isFalse(string) ? ifTrue : "");
    }

    /**
     * Checks if string resolves to a logic value representing true.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @param ifFalse Value if false.
     * @return A string according the condition.
     */
    public static String isTrue(String string, String ifTrue, String ifFalse) {
        return (isTrue(string) ? ifTrue : ifFalse);
    }

    /**
     * Checks if string resolves to a logic value representing false.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @param ifFalse Value if false.
     * @return A string according the condition.
     */
    public static String isFalse(String string, String ifTrue, String ifFalse) {
        return (isFalse(string) ? ifTrue : ifFalse);
    }

    /**
     * Checks if string resolves to a logic value representing true.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @param ifFalse Value if false.
     * @param defaultValue Value if the string is neither true or false.
     * @return A string according the condition.
     */
    public static String isTrue(String string, String ifTrue, String ifFalse, String defaultValue) {
        if (isTrue(string)) {
            return ifTrue;
        }
        if (isFalse(string)) {
            return ifFalse;
        }
        return defaultValue;
    }

    /**
     * Checks if string resolves to a logic value representing false.
     *
     * @param string The string.
     * @param ifTrue Value if true.
     * @param ifFalse Value if false.
     * @param defaultValue Value if the string is neither true or false.
     * @return A string according the condition.
     */
    public static String isFalse(String string, String ifTrue, String ifFalse, String defaultValue) {
        if (isFalse(string)) {
            return ifTrue;
        }
        if (isTrue(string)) {
            return ifFalse;
        }
        return defaultValue;
    }

    /**
     * Trim spaces from the string.
     *
     * @param string The string.
     * @return The string with the trailing and leading spaces trimmed.
     */
    public static String trimSpaces(String string) {
        return string.trim();
    }

    /**
     * Returns the filename.
     *
     * @param f The string.
     * @return The filename.
     */
    public static String getFilename(String f) {
        try {
            return (new File(f)).getName();
        } catch (Exception exception) {
            return "";
        }
    }

    /**
     * Returns the base name.
     *
     * @param f The string.
     * @return The base name.
     */
    public static String getBasename(String f) {
        try {
            f = (new File(f)).getName();
            int i = f.lastIndexOf(".") != -1 ? f.lastIndexOf(".") : f.length();
            return f.substring(0, i);
        } catch (Exception exception) {
            return "";
        }
    }

    /**
     * Returns the filetype.
     *
     * @param f The string.
     * @return The filetype.
     */
    public static String getFiletype(String f) {
        try {
            f = (new File(f)).getName();
            if (f.lastIndexOf(".") != -1) {
                return f.substring(f.lastIndexOf(".") + 1, f.length());
            }
            return "";
        } catch (Exception exception) {
            return "";
        }
    }

    /**
     * Returns the directory name.
     *
     * @param f The string.
     * @return The directory name.
     */
    public static String getDirname(String f) {
        try {
            return (new File(f)).getParent();
        } catch (Exception exception) {
            return "";
        }
    }

    /**
     * Checks if the string is a valid existing file.
     *
     * @param f The string.
     * @return A logic value indicating if the string is a valid existing file.
     */
    public static boolean isFile(String f) {
        try {
            return (new File(f)).isFile();
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Checks if the string is a valid existing directory.
     *
     * @param f The string.
     * @return A logic value indicating if the string is a valid existing
     * directory.
     */
    public static boolean isDir(String f) {
        try {
            return (new File(f)).isDirectory();
        } catch (Exception exception) {
            return false;
        }
    }
    
    /**
     * Checks if the operating system is Windows.
     * 
     * @param ifTrue The value if true.
     * @param ifFalse The value if false.
     * @return A string according the condition.
     */
    public static String isWindows(String ifTrue, String ifFalse) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return ifTrue;
        }
        else {
            return ifFalse;
        }
    }
    
    /**
     * Checks if the operating system is Linux.
     * 
     * @param ifTrue The value if true.
     * @param ifFalse The value if false.
     * @return A string according the condition.
     */
    public static String isLinux(String ifTrue, String ifFalse) {
        if (SystemUtils.IS_OS_LINUX) {
            return ifTrue;
        }
        else {
            return ifFalse;
        }
    }
    
    /**
     * Checks if the operating system is Unix.
     * 
     * @param ifTrue The value if true.
     * @param ifFalse The value if false.
     * @return A string according the condition.
     */
    public static String isUnix(String ifTrue, String ifFalse) {
        if (SystemUtils.IS_OS_UNIX) {
            return ifTrue;
        }
        else {
            return ifFalse;
        }
    }
    
    /**
     * Checks if the operating system is Mac.
     * 
     * @param ifTrue The value if true.
     * @param ifFalse The value if false.
     * @return A string according the condition.
     */
    public static String isMac(String ifTrue, String ifFalse) {
        if (SystemUtils.IS_OS_MAC) {
            return ifTrue;
        }
        else {
            return ifFalse;
        }
    }

    /**
     * Checks if the logic condition is true.
     * 
     * @param value Logic condition to be evaluated.
     * @param ifTrue The value if true.
     * @return A string according the condition.
     */
    public static String isTrue(boolean value, String ifTrue) {
        if (value) {
            return ifTrue;
        }
        return "";
    }
    
    /**
     * Checks if the logic condition is true.
     * 
     * @param value Logic condition to be evaluated.
     * @param ifTrue The value if true.
     * @param ifFalse The value if false.
     * @return A string according the condition.
     */
    public static String isTrue(boolean value, String ifTrue, String ifFalse) {
        if (value) {
            return ifTrue;
        }
        return ifFalse;
    }
    
    /**
     * Checks if the logic condition is false.
     * 
     * @param value Logic condition to be evaluated.
     * @param ifTrue The value if true.
     * @return A string according the condition.
     */
    public static String isFalse(boolean value, String ifTrue) {
        if (!value) {
            return ifTrue;
        }
        return "";
    }
    
    /**
     * Checks if the logic condition is false.
     * 
     * @param value Logic condition to be evaluated.
     * @param ifTrue The value if true.
     * @param ifFalse The value if false.
     * @return A string according the condition.
     */
    public static String isFalse(boolean value, String ifTrue, String ifFalse) {
        if (!value) {
            return ifTrue;
        }
        return ifFalse;
    }

}
