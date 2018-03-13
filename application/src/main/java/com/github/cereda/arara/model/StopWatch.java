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
package com.github.cereda.arara.model;

import com.github.cereda.arara.controller.ConfigurationController;

/**
 * Implements a stopwatch.
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
 * @since 4.0
 */
public class StopWatch {

    // two variables indicating the
    // times the stopwatch started
    // and stopped
    private static long beginning = 0;
    private static long end = 0;
    
    // a variable to indicate the
    // stopwatch is enabled; so far,
    // it hasn't started, then it is
    // not enabled
    private static boolean enabled = false;

    /**
     * Starts the stopwatch.
     */
    public static void start() {
        beginning = System.nanoTime();
        enabled = true;
    }

    /**
     * Stops the stopwatch.
     */
    public static void stop() {
        end = System.nanoTime();
    }

    /**
     * Gets the string representation of the elapsed time.
     * @return A string representation of the elapsed time.
     */
    public static String getTime() {
        Language language = (Language) ConfigurationController.
                getInstance().get("execution.language");
        double result = enabled ? (double) (end - beginning) / 1000000000 : 0.0;
        return String.format(language.getLocale(), "%1.2f", result);
    }

}
