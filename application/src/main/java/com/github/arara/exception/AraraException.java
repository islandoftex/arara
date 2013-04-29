package com.github.arara.exception;

/**
 * Aims at reducing every exception that happens in the whole application to a
 * single self-contained exception.
 *
 * @since 3.0
 * @author Paulo Roberto Massa Cereda
 * @version 4.0
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

}
