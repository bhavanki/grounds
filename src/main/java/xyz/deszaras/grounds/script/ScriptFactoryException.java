package xyz.deszaras.grounds.script;

/**
 * An exception thrown by {@link ScriptFactory} when building a script
 * fails.
 */
public class ScriptFactoryException extends Exception {
    /**
     * Creates a new exception.
     */
    public ScriptFactoryException() {}

    /**
     * Creates a new exception with the given message.
     *
     * @param msg message
     */
    public ScriptFactoryException(String msg) { super(msg); }

    /**
     * Creates a new exception with the given cause.
     *
     * @param cause cause
     */
    public ScriptFactoryException(Throwable cause) { super(cause); }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param msg message
     * @param cause cause
     */
    public ScriptFactoryException(String msg, Throwable cause) { super(msg, cause); }
}
