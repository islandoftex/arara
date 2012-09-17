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
 * AraraException: This class aims at reducing every exception that happens in the
 * whole application to a single self-contained exception.
 */
// package definition
package com.github.arara.exception;

/**
 * Aims at reducing every exception that happens in the whole application to a
 * single self-contained exception.
 *
 * @author Paulo Roberto Massa Cereda
 * @version 3.0
 * @since 3.0
 */
public class AraraException extends Exception {

    /**
     * Constructor.
     */
    public AraraException() {
        super();
    }

    /**
     * Constructor with a message.
     *
     * @param message A message containing the cause of the exception thrown.
     */
    public AraraException(String message) {
        super(message);
    }

    /**
     * Constructor with both message and the cause of the exception.
     *
     * @param message A message containing the cause of the exception thrown.
     * @param cause The throwable cause.
     */
    public AraraException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with the cause of the exception.
     *
     * @param cause The throwable cause.
     */
    public AraraException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with message, cause, and two logical values indicating
     * suppression and a writable stacktrace.
     *
     * @param message The message containing the cause of the exception thrown.
     * @param cause The throwable cause.
     * @param enableSuppression A logical value indicating suppression.
     * @param writableStackTrace A logical value indicating if the stacktrace is
     * writable.
     */
    public AraraException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
