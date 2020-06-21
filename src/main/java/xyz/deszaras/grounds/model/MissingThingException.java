package xyz.deszaras.grounds.model;

/**
 * An exception thrown when a thing is expected to exist, but doesn't.
 */
public class MissingThingException extends Exception {
    /**
     * Creates a new exception.
     */
    public MissingThingException() {}

    /**
     * Creates a new exception with the given message.
     *
     * @param msg message
     */
    public MissingThingException(String msg) { super(msg); }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause cause
     */
    public MissingThingException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param msg message
     * @param cause cause
     */
    public MissingThingException(String msg, Throwable cause) { super(msg, cause); }
}
